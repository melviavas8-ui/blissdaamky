package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class AimAssist extends ModuleStructure {
    private static final String pd660394a = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Дистанция", 4.5F, 2.0F, 8.0F, 0.1F, "бл.");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Угол обзора", 60.0F, 10.0F, 180.0F, 5.0F, "°");
   private final FloatSetting l1lll11l1l = new FloatSetting("Скорость", 4.0F, 0.5F, 20.0F, 0.5F, "°");
   private final BooleanSetting lIIl11l111lIIl1ll = new BooleanSetting("По вертикали", false);
   private final BooleanSetting l1IIl11lI = new BooleanSetting("Только при атаке", true);
   private final BooleanSetting I1lIIII1III1lIlIl1l = new BooleanSetting("Только с оружием", false);
// https://t.me/pasterplace

   public AimAssist() {
      super("AimAssist", "Мягко доводит прицел до ближайшего игрока", ModuleCategory.COMBAT);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1, this.l1lll11l1l, this.lIIl11l111lIIl1ll, this.l1IIl11lI, this.I1lIIII1III1lIlIl1l);
   }
// https://t.me/pasterplace

   @EventTarget
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && CLIENT.currentScreen == null) {
         if (!this.l1IIl11lI.getValue() || CLIENT.options.attackKey.isPressed()) {
            if (!this.I1lIIII1III1lIlIl1l.getValue() || CombatUtils.isInInvalidCriticalState(CLIENT.player)) {
               Vec3d eyePosition = CLIENT.player.getEyePos();
               Rotation lIll1Il1lll111l11l1I1llllI2 = new Rotation(
                  CLIENT.player.getYaw(), CLIENT.player.getPitch()
               );
               Object targetPlayer = null;
               float bestAngularDistance = this.IllI1l1l1.getValue();
// https://t.me/pasterplace

                for (PlayerEntity candidatePlayer : CLIENT.world.getPlayers()) {
                  if (candidatePlayer != CLIENT.player
                     && candidatePlayer.isAlive()
                     && !candidatePlayer.isSpectator()
                     && !AntiBot.II1Il11l111II11IIl((Entity)candidatePlayer)
                     && !FriendManager.getInstance().isFriend((Entity)candidatePlayer)
                     && !(RotationUtils.distanceToBox(eyePosition, candidatePlayer.getBoundingBox()) > this.l1I1IlllIlI.getValue().floatValue())
                     && CLIENT.player.canSee((Entity)candidatePlayer)) {
                     float angularDistance = RotationUtils.angularDistance(lIll1Il1lll111l11l1I1llllI2, eyePosition, candidatePlayer.getBoundingBox().getCenter());
                     if (angularDistance < bestAngularDistance) {
                        bestAngularDistance = angularDistance;
                        targetPlayer = candidatePlayer;
                     }
                  }
               }

               if (targetPlayer != null) {
                  Rotation lIll1Il1lll111l11l1I1llllI22 = RotationUtils.lookAt(
                     eyePosition, RotationUtils.getAimPoint((Entity)targetPlayer, eyePosition, false)
                  );
                  float maxYawStep = this.l1lll11l1l.getValue() * (0.85F + ThreadLocalRandom.current().nextFloat() * 0.3F);
                  float yawDelta = MathHelper.clamp(lIll1Il1lll111l11l1I1llllI2.yawDeltaTo(lIll1Il1lll111l11l1I1llllI22), -maxYawStep, maxYawStep);
                  float pitchDelta = this.lIIl11l111lIIl1ll.getValue()
                     ? MathHelper.clamp(lIll1Il1lll111l11l1I1llllI2.pitchDeltaTo(lIll1Il1lll111l11l1I1llllI22), -maxYawStep * 0.6F, maxYawStep * 0.6F)
                     : 0.0F;
                  Rotation lIll1Il1lll111l11l1I1llllI23 = RotationUtils.snapToSensitivityGrid(
                     lIll1Il1lll111l11l1I1llllI2,
                     new Rotation(lIll1Il1lll111l11l1I1llllI2.getYaw() + yawDelta, lIll1Il1lll111l11l1I1llllI2.getPitch() + pitchDelta)
                  );
                  CLIENT.player.setYaw(lIll1Il1lll111l11l1I1llllI23.getYaw());
                  CLIENT.player.setPitch(lIll1Il1lll111l11l1I1llllI23.getPitch());
               }
            }
         }
      }
   }
}

