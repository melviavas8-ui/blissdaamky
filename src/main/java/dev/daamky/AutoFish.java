package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

public final class AutoFish extends ModuleStructure {
    private static final String pd6c36991 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Задержка подсечки", 150.0F, 0.0F, 1000.0F, 10.0F, "мс");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Задержка заброса", 600.0F, 100.0F, 3000.0F, 50.0F, "мс");
   private final BooleanSetting l1lll11l1l = new BooleanSetting("Авто заброс", true);
   private final Stopwatch lIIl11l111lIIl1ll = new Stopwatch();
   private boolean l1IIl11lI;
   private boolean I1lIIII1III1lIlIl1l;
// https://t.me/pasterplace

   public AutoFish() {
      super("AutoFish", "Автоматически подсекает рыбу и забрасывает удочку", ModuleCategory.PLAYER);
      this.IllI1l1l1.visibleWhen(this.l1lll11l1l::getValue);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1, this.l1lll11l1l);
   }
// https://t.me/pasterplace

   @Override
   protected void onDisable() {
      this.l1IIl11lI = false;
      this.I1lIIII1III1lIlIl1l = false;
      super.onDisable();
   }
// https://t.me/pasterplace

   @EventTarget
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (eventPacket.isReceive()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof PlaySoundS2CPacket) {
             PlaySoundS2CPacket packet2 = (PlaySoundS2CPacket)packet;
            if (CLIENT.player != null) {
               FishingBobberEntity fishingBobberEntity = CLIENT.player.fishHook;
                if (fishingBobberEntity != null && !this.l1IIl11lI && packet2.getSound().value() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH) {
                  if (fishingBobberEntity.squaredDistanceTo(packet2.getX(), packet2.getY(), packet2.getZ()) > 4.0) {
                     return;
                  }

                  this.l1IIl11lI = true;
                  this.lIIl11l111lIIl1ll.reset();
                  return;
               }
// https://t.me/pasterplace

               return;
            }
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && CLIENT.interactionManager != null) {
         Hand hand = this.l1I1IIl111Il1l1l();
         if (hand == null) {
            this.l1IIl11lI = false;
            this.I1lIIII1III1lIlIl1l = false;
         } else if (this.l1IIl11lI) {
            if (this.lIIl11l111lIIl1ll.hasElapsed(this.l1I1IlllIlI.getValue().longValue())) {
               this.l1IIl11lI = false;
               if (CLIENT.player.fishHook != null) {
                  CLIENT.interactionManager.interactItem(CLIENT.player, hand);
                  CLIENT.player.swingHand(hand);
               }

               this.I1lIIII1III1lIlIl1l = this.l1lll11l1l.getValue();
               this.lIIl11l111lIIl1ll.reset();
            }
         } else {
            if (this.I1lIIII1III1lIlIl1l && this.lIIl11l111lIIl1ll.hasElapsed(this.IllI1l1l1.getValue().longValue())) {
               this.I1lIIII1III1lIlIl1l = false;
               if (CLIENT.player.fishHook == null && CLIENT.currentScreen == null) {
                  CLIENT.interactionManager.interactItem(CLIENT.player, hand);
                  CLIENT.player.swingHand(hand);
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private Hand l1I1IIl111Il1l1l() {
      if (CLIENT.player.getMainHandStack().isOf(Items.FISHING_ROD)) {
         return Hand.MAIN_HAND;
      } else {
         return CLIENT.player.getOffHandStack().isOf(Items.FISHING_ROD) ? Hand.OFF_HAND : null;
      }
   }
}

