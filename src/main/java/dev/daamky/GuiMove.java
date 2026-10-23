package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventInput;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.mixin.client.KeyMappingAccessor;
import java.util.ArrayDeque;
import java.util.Queue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class GuiMove extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final BooleanSetting allowSneakingMovement = new BooleanSetting("Приседание", false);
   private final BooleanSetting allowMovementInClientGui = new BooleanSetting("В меню клиента", true);
   private final ReallyWorld inventorySwapRules = new ReallyWorld();
   private static final int PACKET_RELEASE_DELAY_TICKS = 2;
   private final Queue<Packet<?>> queuedPackets = new ArrayDeque<>();
   private int queueStartPlayerAge;
   private int packetReleaseDelayTicks;
   private boolean handledContainerOpen;
// https://t.me/pasterplace

   public GuiMove() {
      super("GuiMove", "Позволяет двигаться с открытыми меню", ModuleCategory.MOVEMENT);
      this.addSettings(this.allowSneakingMovement, this.allowMovementInClientGui, this.inventorySwapRules);
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.flushQueuedPackets();
      this.packetReleaseDelayTicks = 0;
   }
// https://t.me/pasterplace

   private boolean hasMovementInputOrSprinting() {
      return CLIENT.player.getVelocity().horizontalLengthSquared() >= 4.0E-4 || CLIENT.player.isSprinting();
   }
// https://t.me/pasterplace

   @EventTarget
   public void onPacket(EventPacket eventPacket) {
      if (eventPacket.isSend() && this.hasActiveClientWorld()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof ClickSlotC2SPacket) {
            ClickSlotC2SPacket packet2 = (ClickSlotC2SPacket)packet;
            if (packet2.syncId() == 0) {
               this.handledContainerOpen = true;
            }
         }

         packet = eventPacket.packet();
         if (packet instanceof CloseHandledScreenC2SPacket) {
            CloseHandledScreenC2SPacket packet4 = (CloseHandledScreenC2SPacket)packet;
            if (packet4.getSyncId() == 0) {
               boolean enabled = this.handledContainerOpen;
               this.handledContainerOpen = false;
               if (!enabled && this.queuedPackets.isEmpty()) {
                  eventPacket.cancel();
                  return;
               }
            }

            if (!this.queuedPackets.isEmpty() || this.hasMovementInputOrSprinting()) {
               if (this.queuedPackets.isEmpty()) {
                  this.queueStartPlayerAge = CLIENT.player.age;
               }

               this.queuedPackets.add(eventPacket.packet());
               eventPacket.cancel();
            }
         } else {
            packet = eventPacket.packet();
            if (packet instanceof ClickSlotC2SPacket) {
               ClickSlotC2SPacket packet6 = (ClickSlotC2SPacket)packet;
               if (CLIENT.currentScreen != null && this.inventorySwapRules.getInventorySwapCategory() != Priority.PriorityCategory.DEFAULT) {
                  if (packet6.syncId() != CLIENT.player.currentScreenHandler.syncId) {
                     return;
                  }

                  if (this.queuedPackets.isEmpty() && this.packetReleaseDelayTicks <= 0 && !this.hasMovementInputOrSprinting()) {
                     return;
                  }

                  if (this.queuedPackets.isEmpty()) {
                     this.queueStartPlayerAge = CLIENT.player.age;
                  }

                  this.queuedPackets.add(eventPacket.packet());
                  eventPacket.cancel();
                  return;
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      if (!this.hasActiveClientWorld()) {
         this.queuedPackets.clear();
      } else {
         if (this.packetReleaseDelayTicks > 0) {
            this.packetReleaseDelayTicks--;
         }

         if (!this.queuedPackets.isEmpty()) {
            int ticksSinceInput = CLIENT.player.age - this.queueStartPlayerAge;
            if (CLIENT.currentScreen != null) {
               int stationaryFlag = !(CLIENT.player.getVelocity().horizontalLengthSquared() < 4.0E-4) ? 0 : 1;
               if (ticksSinceInput > 2 && stationaryFlag != 0 || ticksSinceInput > 8) {
                  this.flushQueuedPackets();
               }
            } else {
               if (ticksSinceInput >= 1 && !CLIENT.player.isSprinting() || ticksSinceInput > 8) {
                  this.flushQueuedPackets();
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private void flushQueuedPackets() {
      if (!this.queuedPackets.isEmpty() && CLIENT.player != null) {
         if (CLIENT.player.isSprinting()) {
            CLIENT.player.setSprinting(false);
            PacketSender.l1I1IlllIlI(new ClientCommandC2SPacket(CLIENT.player, Mode.STOP_SPRINTING));
         }

         while (!this.queuedPackets.isEmpty()) {
            PacketSender.l1I1IlllIlI(this.queuedPackets.poll());
         }
// https://t.me/pasterplace

         this.packetReleaseDelayTicks = 2;
      } else {
         this.queuedPackets.clear();
      }
   }
// https://t.me/pasterplace

   @EventTarget(priority = 3)
   public void suppressInputWhileFlushing(EventInput eventInput) {
      if (!this.queuedPackets.isEmpty() || this.packetReleaseDelayTicks > 0) {
         eventInput.setForward(0.0F);
         eventInput.setStrafe(0.0F);
         eventInput.setJump(false);
         eventInput.setSprint(false);
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void applyGuiMovementInput(EventInput eventInput) {
      Screen screen = CLIENT.currentScreen;
      if (this.hasActiveClientWorld()
         && screen != null
         && !(screen instanceof ChatScreen)
         && !(screen instanceof AbstractSignEditScreen)
         && !(screen instanceof AnvilScreen)) {
         if (!(screen instanceof DaamkyClickGuiScreen) || this.allowMovementInClientGui.getValue() && !Search.getInstance().isTextInputFocused()) {
            boolean forwardPressed = isKeyPhysicallyPressed(CLIENT.options.forwardKey);
            boolean backPressed = isKeyPhysicallyPressed(CLIENT.options.backKey);
            boolean leftPressed = isKeyPhysicallyPressed(CLIENT.options.leftKey);
            boolean rightPressed = isKeyPhysicallyPressed(CLIENT.options.rightKey);
            eventInput.setForward(forwardPressed != backPressed ? (!forwardPressed ? -1.0F : 1.0F) : 0.0F);
            eventInput.setStrafe(leftPressed != rightPressed ? (!leftPressed ? -1.0F : 1.0F) : 0.0F);
            eventInput.setJump(isKeyPhysicallyPressed(CLIENT.options.jumpKey));
            eventInput.setSprint(isKeyPhysicallyPressed(CLIENT.options.sprintKey));
            if (this.allowSneakingMovement.getValue()) {
               eventInput.setSneak(isKeyPhysicallyPressed(CLIENT.options.sneakKey));
            }
         }
      }
   }
// https://t.me/pasterplace

   private static boolean isKeyPhysicallyPressed(KeyBinding keyBinding) {
      net.minecraft.client.util.InputUtil.Key key = ((KeyMappingAccessor)keyBinding).daamky$getKey();
      return key.getCategory() == Type.KEYSYM && key.getCode() > 0 && key.getCode() < 512
         ? InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), key.getCode())
         : false;
   }
}

