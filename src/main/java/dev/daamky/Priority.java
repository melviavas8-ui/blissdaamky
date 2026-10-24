package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventInput;
import dev.daamky.core.event.impl.EventMotion;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import dev.daamky.mixin.client.LocalPlayerAccessor;
import java.util.ArrayDeque;
import java.util.Queue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

public final class Priority {
   private static final Priority INSTANCE = new Priority();
   private static final int MAX_QUEUED_ACTIONS = 8;
   private static final long MIN_SCREEN_CLOSE_INTERVAL_MILLIS = 1000L;
   private final Queue<Priority.PriorityAction> pendingActions = new ArrayDeque<>();
   private int sprintSuppressionTicks = -1;
   private boolean executingPriorityAction;
   private long lastInteractionMillis;

   private Priority() {
   }

   public static Priority getInstance() {
      return INSTANCE;
   }

   public boolean hasQueuedActions() {
      return !this.pendingActions.isEmpty();
   }

   private static int getSprintSuppressionTicks(Priority.PriorityCategory category) {
      return category == Priority.PriorityCategory.FUNTIME ? 2 : 1;
   }

   public void scheduleAction(Priority.PriorityCategory category, Runnable runnable) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         if (!this.executingPriorityAction && category != Priority.PriorityCategory.DEFAULT && client.currentScreen == null) {
            if (this.pendingActions.size() < 8) {
               if (this.pendingActions.isEmpty()) {
                  this.beginSprintSuppression(getSprintSuppressionTicks(category));
               }

               this.pendingActions.add(new Priority.PriorityAction(category, runnable));
            }
         } else {
            this.runActionWithGuard(runnable);
         }
      }
   }

   private void runActionWithGuard(Runnable runnable) {
      boolean wasExecutingAction = this.executingPriorityAction;
      this.executingPriorityAction = true;

      try {
         runnable.run();
      } finally {
         this.executingPriorityAction = wasExecutingAction;
      }
   }

   private void beginSprintSuppression(int sprintTicks) {
      this.sprintSuppressionTicks = sprintTicks;
      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
      if (clientPlayerEntity != null) {
         clientPlayerEntity.setSprinting(false);
      }
   }

   private static void stopSprinting(ClientPlayerEntity clientPlayerEntity) {
      clientPlayerEntity.setSprinting(false);
      LocalPlayerAccessor playerAccessor = (LocalPlayerAccessor)clientPlayerEntity;
      if (playerAccessor.daamky$wasSprinting()) {
         PacketSender.II1Il11l111II11IIl(new ClientCommandC2SPacket(clientPlayerEntity, Mode.STOP_SPRINTING));
         playerAccessor.daamky$setWasSprinting(false);
      }
   }

   @EventTarget(priority = 4)
   public void blockMovementInput(EventInput eventInput) {
      if (this.sprintSuppressionTicks >= 0 || !this.pendingActions.isEmpty()) {
         eventInput.setForward(0.0F);
         eventInput.setStrafe(0.0F);
         eventInput.setJump(false);
         eventInput.setSprint(false);
         eventInput.setSneak(false);
      }
   }

   @EventTarget
   public void updateSprintSuppression(EventUpdate eventUpdate) {
      InventoryUtils.updateDeferredHotbarSlotRestore();
      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
      if (clientPlayerEntity == null) {
         this.pendingActions.clear();
         this.sprintSuppressionTicks = -1;
      } else {
         if (this.sprintSuppressionTicks > 0) {
            clientPlayerEntity.setSprinting(false);
            this.sprintSuppressionTicks--;
         } else if (this.sprintSuppressionTicks == 0 && this.pendingActions.isEmpty()) {
            this.sprintSuppressionTicks = -1;
         }
      }
   }

   @EventTarget(priority = 1)
   public void executeNextQueuedAction(EventMotion eventMotion) {
      if (!eventMotion.isPost() && !this.pendingActions.isEmpty() && this.sprintSuppressionTicks <= 0) {
         MinecraftClient client = MinecraftClient.getInstance();
         ClientPlayerEntity clientPlayerEntity = client.player;
         if (clientPlayerEntity != null) {
            if (clientPlayerEntity.currentScreenHandler != clientPlayerEntity.playerScreenHandler) {
               this.pendingActions.clear();
               this.sprintSuppressionTicks = 0;
            } else {
               Object priorityAction = this.pendingActions.poll();
               stopSprinting(clientPlayerEntity);
               this.runActionWithGuard(((Priority.PriorityAction)priorityAction).action());
               if (client.currentScreen == null
                  && clientPlayerEntity.currentScreenHandler == clientPlayerEntity.playerScreenHandler
                  && System.currentTimeMillis() - this.lastInteractionMillis > 1000L) {
                  PacketSender.II1Il11l111II11IIl(new CloseHandledScreenC2SPacket(clientPlayerEntity.playerScreenHandler.syncId));
               }

               Object nextPriorityAction = this.pendingActions.peek();
               this.sprintSuppressionTicks = nextPriorityAction != null ? getSprintSuppressionTicks(((Priority.PriorityAction)nextPriorityAction).category()) : 0;
            }
         }
      }
   }

   @EventTarget
   public void recordInteractionTime(EventPacket eventPacket) {
      if (eventPacket.isSend()
         && (eventPacket.packet() instanceof PlayerInteractBlockC2SPacket || eventPacket.packet() instanceof PlayerInteractEntityC2SPacket)) {
         this.lastInteractionMillis = System.currentTimeMillis();
      }
   }

   @EventTarget
   public void clearQueuedActions(EventWorldChange eventWorldChange) {
      this.pendingActions.clear();
      this.sprintSuppressionTicks = -1;
   }

   public enum PriorityCategory {
      DEFAULT,
      REALLYWORLD,
      FUNTIME;

      // $VF: synthetic method
      private static Priority.PriorityCategory[] II1Il11l111II11IIl() {
         return new Priority.PriorityCategory[]{DEFAULT, REALLYWORLD, FUNTIME};
      }
   }

   private record PriorityAction(Priority.PriorityCategory category, Runnable action) {

      private PriorityAction(Priority.PriorityCategory category, Runnable action) {
         this.category = category;
         this.action = action;
      }
   }
}

