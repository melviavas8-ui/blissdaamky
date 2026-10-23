package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class UseTracker extends ModuleStructure {
    private static final String p892b4bc7 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiToggleSetting l1I1IlllIlI = new MultiToggleSetting("Сообщать о")
      .addOption("Золотых яблоках", true)
      .addOption("Зельях", true)
      .addOption("Тотемах", true);
   private final FloatSetting IllI1l1l1 = new FloatSetting("Дистанция", 30.0F, 5.0F, 100.0F, 1.0F, "бл.");
   private final Set<UUID> l1lll11l1l = new HashSet<>();
// https://t.me/pasterplace

   public UseTracker() {
      super("UseTracker", "Сообщает в чат, когда соперник ест гепл, пьёт зелье или ломает тотем", ModuleCategory.WORLD);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
          for (PlayerEntity player : CLIENT.world.getPlayers()) {
            if (player == CLIENT.player
                || FriendManager.getInstance().isFriend(player)
               || player.distanceTo(CLIENT.player) > this.IllI1l1l1.getValue()) {
               this.l1lll11l1l.remove(player.getUuid());
            } else {
               boolean isUsingItem;
               if ((isUsingItem = player.isUsingItem()) && this.l1lll11l1l.add(player.getUuid())) {
                   this.II1Il11l111II11IIl(player, player.getActiveItem());
               } else if (!isUsingItem) {
                  this.l1lll11l1l.remove(player.getUuid());
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private void II1Il11l111II11IIl(PlayerEntity playerEntity, ItemStack activeItemStack) {
      String playerName = playerEntity.getGameProfile().name();
      if (!this.l1I1IlllIlI.isOptionEnabled("Золотых яблоках") || !activeItemStack.isOf(Items.GOLDEN_APPLE) && !activeItemStack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
         if (this.l1I1IlllIlI.isOptionEnabled("Зельях") && activeItemStack.isOf(Items.POTION)) {
            ClientChatLogger.II1Il11l111II11IIl(playerName + " пьёт " + activeItemStack.getName().getString());
         }
      } else {
         ClientChatLogger.II1Il11l111II11IIl(playerName + " ест " + activeItemStack.getName().getString());
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (eventPacket.isReceive() && this.l1I1IlllIlI.isOptionEnabled("Тотемах")) {
         Packet packet = eventPacket.packet();
         if (packet instanceof EntityStatusS2CPacket) {
             EntityStatusS2CPacket packet2 = (EntityStatusS2CPacket)packet;
            if (packet2.getStatus() == 35) {
               CLIENT.execute(() -> {
                  if (CLIENT.world != null) {
                     Entity entity = packet2.getEntity(CLIENT.world);
                     if (entity instanceof PlayerEntity) {
                      PlayerEntity entity2 = (PlayerEntity)entity;
                        if (entity2 != CLIENT.player) {
                           ClientChatLogger.II1Il11l111II11IIl(entity2.getGameProfile().name() + " потерял тотем");
                        }
                     }
                  }
               });
               return;
            }
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventWorldChange eventWorldChange) {
      this.l1lll11l1l.clear();
   }
}

