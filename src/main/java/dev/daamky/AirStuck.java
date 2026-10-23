package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AirStuck extends ModuleStructure {
    private static final String p4dcecf26 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final BooleanSetting l1I1IlllIlI = new BooleanSetting("Не отправлять движение", true);
   private Vec3d IllI1l1l1;
   private ClientPlayerEntity l1lll11l1l;
// https://t.me/pasterplace

   public AirStuck() {
      super("AirStuck", "Замораживает игрока в воздухе", ModuleCategory.MOVEMENT);
      this.addSetting(this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.IllI1l1l1 = CLIENT.player == null ? null : CLIENT.player.getEntityPos();
      this.l1lll11l1l = CLIENT.player;
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.IllI1l1l1 = null;
      this.l1lll11l1l = null;
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         if (!CLIENT.player.isDead() && (this.l1lll11l1l == null || this.l1lll11l1l == CLIENT.player)) {
            if (this.IllI1l1l1 == null) {
               this.IllI1l1l1 = CLIENT.player.getEntityPos();
               this.l1lll11l1l = CLIENT.player;
            }
// https://t.me/pasterplace

            CLIENT.player.setVelocity(Vec3d.ZERO);
            CLIENT.player.setPosition(this.IllI1l1l1.x, this.IllI1l1l1.y, this.IllI1l1l1.z);
            CLIENT.player.fallDistance = 0.0;
         } else {
            this.setEnabled(false);
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (this.l1I1IlllIlI.getValue()
         && eventPacket.isSend()
         && CLIENT.player != null
         && CLIENT.player == this.l1lll11l1l
         && !CLIENT.player.isDead()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof PlayerMoveC2SPacket) {
             PlayerMoveC2SPacket packet2 = (PlayerMoveC2SPacket)packet;
            if (packet2.changesPosition()) {
               eventPacket.cancel();
            }
         }
      }
   }
}

