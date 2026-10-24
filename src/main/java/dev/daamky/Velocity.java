package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import java.util.Optional;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Velocity extends ModuleStructure {
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Режим", "ReallyWorld", "Отмена", "Процент", "ReallyWorld", "Прыжок");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Горизонталь", 0.0F, 0.0F, 100.0F, 1.0F, "%");
   private final FloatSetting l1lll11l1l = new FloatSetting("Вертикаль", 0.0F, 0.0F, 100.0F, 1.0F, "%");
   private final BooleanSetting lIIl11l111lIIl1ll = new BooleanSetting("Взрывы", true);
   private final BooleanSetting l1IIl11lI = new BooleanSetting("Только на земле", false);
   private volatile int I1lIIII1III1lIlIl1l;
   private volatile boolean l11lIlIIll1III1;
   private volatile int intValue;

   public Velocity() {
      super("Velocity", "Уменьшает или убирает отдачу от ударов", ModuleCategory.COMBAT);
      this.IllI1l1l1.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Процент"));
      this.l1lll11l1l.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Процент"));
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1, this.l1lll11l1l, this.lIIl11l111lIIl1ll, this.l1IIl11lI);
   }

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.isSelected("Процент")
         ? Math.round(this.IllI1l1l1.getValue()) + "% " + Math.round(this.l1lll11l1l.getValue()) + "%"
         : this.l1I1IlllIlI.getValue();
   }

   @EventTarget
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (eventPacket.isReceive()) {
         ClientPlayerEntity clientPlayerEntity = CLIENT.player;
         if (clientPlayerEntity != null) {
            if (eventPacket.packet() instanceof PlayerPositionLookS2CPacket) {
               this.intValue = 3;
            } else if (this.intValue <= 0) {
               if (!this.l1IIl11lI.getValue() || clientPlayerEntity.isOnGround()) {
                  Packet packet = eventPacket.packet();
                  if (packet instanceof EntityVelocityUpdateS2CPacket) {
                     EntityVelocityUpdateS2CPacket packet2 = (EntityVelocityUpdateS2CPacket)packet;
                     if (packet2.getEntityId() == clientPlayerEntity.getId()) {
                        switch ((String)this.l1I1IlllIlI.getValue()) {
                           case "Отмена":
                              eventPacket.cancel();
                              return;
                           case "Процент":
                              eventPacket.cancel();
                              Vec3d packetVelocity = packet2.getVelocity();
                              double horizontalScale = this.IllI1l1l1.getValue().floatValue() / 100.0;
                              double verticalScale = this.l1lll11l1l.getValue().floatValue() / 100.0;
                              if (horizontalScale > 0.0 || verticalScale > 0.0) {
                                 CLIENT.execute(
                                    () -> {
                                       if (CLIENT.player != null) {
                                          Vec3d currentVelocity = CLIENT.player.getVelocity();
                                          CLIENT.player
                                             .setVelocity(
                                                horizontalScale > 0.0 ? packetVelocity.x * horizontalScale : currentVelocity.x,
                                                verticalScale > 0.0 ? packetVelocity.y * verticalScale : currentVelocity.y,
                                                horizontalScale > 0.0 ? packetVelocity.z * horizontalScale : currentVelocity.z
                                             );
                                       }
                                    }
                                 );
                              }

                              return;
                           case "ReallyWorld":
                              if (this.I1lIIII1III1lIlIl1l <= 0) {
                                 eventPacket.cancel();
                                 this.I1lIIII1III1lIlIl1l = 2;
                              }

                              return;
                           case "Прыжок":
                              this.l11lIlIIll1III1 = true;
                              return;
                           default:
                              return;
                        }
                     }
                  }

                  if (this.lIIl11l111lIIl1ll.getValue()) {
                     packet = eventPacket.packet();
                     if (packet instanceof ExplosionS2CPacket) {
                        ExplosionS2CPacket packet4 = (ExplosionS2CPacket)packet;
                        if (packet4.playerKnockback().isPresent()) {
                           if (this.l1I1IlllIlI.isSelected("Прыжок") || this.l1I1IlllIlI.isSelected("ReallyWorld")) {
                              return;
                           }

                           eventPacket.cancel();
                           double horizontalKnockbackScale = this.l1I1IlllIlI.isSelected("Процент") ? this.IllI1l1l1.getValue().floatValue() / 100.0 : 0.0;
                           double verticalKnockbackScale = this.l1I1IlllIlI.isSelected("Процент") ? this.l1lll11l1l.getValue().floatValue() / 100.0 : 0.0;
                           Vec3d knockback = packet4.playerKnockback().get();
                           ExplosionS2CPacket explosionS2CPacket = new ExplosionS2CPacket(
                              packet4.center(),
                              packet4.radius(),
                              packet4.blockCount(),
                              !(horizontalKnockbackScale > 0.0) && !(verticalKnockbackScale > 0.0)
                                 ? Optional.empty()
                                 : Optional.of(
                                    new Vec3d(knockback.x * horizontalKnockbackScale, knockback.y * verticalKnockbackScale, knockback.z * horizontalKnockbackScale)
                                 ),
                              packet4.explosionParticle(),
                              packet4.explosionSound(),
                              packet4.blockParticles()
                           );
                           CLIENT.execute(() -> {
                              if (CLIENT.getNetworkHandler() != null) {
                                 CLIENT.getNetworkHandler().onExplosion(explosionS2CPacket);
                              }
                           });
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         if (this.I1lIIII1III1lIlIl1l > 0) {
            this.I1lIIII1III1lIlIl1l--;
         }

         if (this.intValue > 0) {
            this.intValue--;
         }

         if (this.l11lIlIIll1III1) {
            this.l11lIlIIll1III1 = false;
            if (CLIENT.player.isOnGround() && CLIENT.player.hurtTime > 0) {
               CLIENT.player.jump();
            }
         }
      }
   }
}

