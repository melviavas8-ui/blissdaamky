package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventTick;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class BackTrack extends ModuleStructure {
    private static final String pb32a6374 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Задержка", 150.0F, 50.0F, 500.0F, 10.0F, "мс");
   private final Deque<BackTrack.QueuedPacket> IllI1l1l1 = new ArrayDeque<>();
   private volatile int l1lll11l1l = -1;
// https://t.me/pasterplace

   public BackTrack() {
      super("BackTrack", "Задерживает движение цели KillAura для лишней дистанции", ModuleCategory.COMBAT);
      this.addSetting(this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return Math.round(this.l1I1IlllIlI.getValue()) + "мс";
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventTick eventTick) {
      Aura aura = Aura.getInstance();
      LivingEntity targetEntity = aura != null && aura.isEnabled() ? aura.getTarget() : null;
      this.l1lll11l1l = targetEntity == null ? -1 : targetEntity.getId();
      long currentTimeMillis = System.currentTimeMillis();
      synchronized (this.IllI1l1l1) {
         while (
            !this.IllI1l1l1.isEmpty() && (this.l1lll11l1l == -1 || (float)(currentTimeMillis - this.IllI1l1l1.peekFirst().queuedAtNanos()) >= this.l1I1IlllIlI.getValue())
         ) {
            this.II1Il11l111II11IIl(this.IllI1l1l1.pollFirst().packet());
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (!eventPacket.isReceive() || this.l1lll11l1l == -1 || CLIENT.world == null) {
         return;
      }

      Packet<?> packet = eventPacket.packet();
      int packetEntityId;
      if (packet instanceof EntityS2CPacket entityPacket) {
         packetEntityId = ((dev.daamky.mixin.network.ClientboundMoveEntityPacketAccessor)entityPacket).daamky$getEntityId();
      } else if (packet instanceof EntityPositionSyncS2CPacket positionSyncPacket) {
         packetEntityId = positionSyncPacket.id();
      } else if (packet instanceof EntityPositionS2CPacket positionPacket) {
         packetEntityId = positionPacket.entityId();
      } else {
         packetEntityId = -1;
      }

      if (packetEntityId != this.l1lll11l1l) {
         return;
      }

      eventPacket.cancel();
      synchronized (this.IllI1l1l1) {
         this.IllI1l1l1.addLast(new BackTrack.QueuedPacket((Packet)packet, System.currentTimeMillis()));
      }

   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventWorldChange eventWorldChange) {
      synchronized (this.IllI1l1l1) {
         this.IllI1l1l1.clear();
      }

      this.l1lll11l1l = -1;
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.l1lll11l1l = -1;
      synchronized (this.IllI1l1l1) {
         while (!this.IllI1l1l1.isEmpty()) {
            this.II1Il11l111II11IIl(this.IllI1l1l1.pollFirst().packet());
         }
      }
   }
// https://t.me/pasterplace

   private void II1Il11l111II11IIl(Packet<? super ClientPlayPacketListener> packet) {
      ClientPlayNetworkHandler clientPlayNetworkHandler = CLIENT.getNetworkHandler();
      if (clientPlayNetworkHandler != null && CLIENT.world != null) {
         packet.apply(clientPlayNetworkHandler);
      }
   }
// https://t.me/pasterplace

   private record QueuedPacket(Packet<? super ClientPlayPacketListener> packet, long queuedAtNanos) {
// https://t.me/pasterplace

      private QueuedPacket(Packet<? super ClientPlayPacketListener> packet, long queuedAtNanos) {
         this.packet = packet;
         this.queuedAtNanos = queuedAtNanos;
      }
   }
}

