package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventTick;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.registry.Registries;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class LootAlert extends ModuleStructure {
    private static final String pb93e5a84 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final Set<Item> l1I1IlllIlI = Set.of(
      Items.ELYTRA, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.PLAYER_HEAD, Items.TOTEM_OF_UNDYING, Items.SHULKER_BOX
   );
   private final BooleanSetting IllI1l1l1 = new BooleanSetting("Зелья", true);
   private final BooleanSetting l1lll11l1l = new BooleanSetting("Незерит", true);
   private final BooleanSetting lIIl11l111lIIl1ll = new BooleanSetting("Все предметы", false);
   private final Map<Integer, ItemStack> l1IIl11lI = new HashMap<>();
   private final ConcurrentLinkedQueue<int[]> I1lIIII1III1lIlIl1l = new ConcurrentLinkedQueue<>();
// https://t.me/pasterplace

   public LootAlert() {
      super("LootAlert", "Оповещает о подборе ценных предметов", ModuleCategory.RENDER);
      this.addSettings(this.IllI1l1l1, this.l1lll11l1l, this.lIIl11l111lIIl1ll);
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.l1IIl11lI.clear();
      this.I1lIIII1III1lIlIl1l.clear();
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (eventPacket.isReceive()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof ItemPickupAnimationS2CPacket) {
             ItemPickupAnimationS2CPacket packet2 = (ItemPickupAnimationS2CPacket)packet;
            this.I1lIIII1III1lIlIl1l.add(new int[]{packet2.getEntityId(), packet2.getCollectorEntityId(), packet2.getStackAmount()});
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   public void II1Il11l111II11IIl(EventTick eventTick) {
      if (CLIENT.player != null && CLIENT.world != null) {
          int[] pickupData;
          while ((pickupData = this.I1lIIII1III1lIlIl1l.poll()) != null) {
             if (pickupData[1] == CLIENT.player.getId()) {
               ItemStack pickedUpStack = ItemStack.EMPTY;
                Entity entity = CLIENT.world.getEntityById(pickupData[0]);
                if (entity instanceof ItemEntity itemEntity) {
                   pickedUpStack = itemEntity.getStack().copy();
               }

               if (pickedUpStack.isEmpty()) {
                   ItemStack cachedStack = this.l1IIl11lI.get(pickupData[0]);
                  if (cachedStack != null) {
                     pickedUpStack = cachedStack.copy();
                  }
               }
// https://t.me/pasterplace

                this.l1IIl11lI.remove(pickupData[0]);
               if (!pickedUpStack.isEmpty() && this.II1Il11l111II11IIl(pickedUpStack)) {
                  int pickedUpCount = Math.max(1, pickupData[2]);
                  NotificationScheduler.II1Il11l111II11IIl("Подобрано", pickedUpStack.getName().getString() + (pickedUpCount > 1 ? " x" + pickedUpCount : ""), 3000L);
               }
            }
         }

         this.l1IIl11lI.clear();

          for (Entity nearbyEntity : CLIENT.world.getEntities()) {
             if (nearbyEntity instanceof ItemEntity itemEntity) {
                if (nearbyEntity.squaredDistanceTo(CLIENT.player) <= 4096.0) {
                   ItemStack worldItemStack = itemEntity.getStack();
                  if (!worldItemStack.isEmpty()) {
                     this.l1IIl11lI.put(nearbyEntity.getId(), worldItemStack.copy());
                  }
               }
            }
         }
      } else {
         this.I1lIIII1III1lIlIl1l.clear();
      }
   }
// https://t.me/pasterplace

   private boolean II1Il11l111II11IIl(ItemStack candidateStack) {
      if (!this.lIIl11l111lIIl1ll.getValue() && !l1I1IlllIlI.contains(candidateStack.getItem())) {
         return !this.IllI1l1l1.getValue()
               || !candidateStack.isOf(Items.POTION) && !candidateStack.isOf(Items.SPLASH_POTION) && !candidateStack.isOf(Items.LINGERING_POTION)
            ? this.l1lll11l1l.getValue() && Registries.ITEM.getId(candidateStack.getItem()).getPath().contains("netherite")
            : true;
      } else {
         return true;
      }
   }
}

