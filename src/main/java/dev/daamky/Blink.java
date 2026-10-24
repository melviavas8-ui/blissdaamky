package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Blink extends ModuleStructure {
   private final BooleanSetting l1I1IlllIlI = new BooleanSetting("Пульс", false);
   private final FloatSetting IllI1l1l1 = new FloatSetting("Интервал пульса", 500.0F, 100.0F, 3000.0F, 50.0F, "мс");
   private final FloatSetting l1lll11l1l = new FloatSetting("Лимит пакетов", 200.0F, 20.0F, 1000.0F, 10.0F);
   private final List<Packet<?>> lIIl11l111lIIl1ll = new ArrayList<>();
   private final Stopwatch l1IIl11lI = new Stopwatch();

   public Blink() {
      super("Blink", "Задерживает отправку движения на сервер", ModuleCategory.MOVEMENT);
      this.IllI1l1l1.visibleWhen(this.l1I1IlllIlI::getValue);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1, this.l1lll11l1l);
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(this.lIIl11l111lIIl1ll.size());
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.lIIl11l111lIIl1ll.clear();
      this.l1IIl11lI.reset();
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.l1I1IIl111Il1l1l();
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (eventPacket.isSend() && CLIENT.player != null) {
         Packet packet = eventPacket.packet();
         if (packet instanceof PlayerMoveC2SPacket || packet instanceof ClientCommandC2SPacket || packet instanceof PlayerInputC2SPacket) {
            eventPacket.cancel();
            this.lIIl11l111lIIl1ll.add(packet);
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.lIIl11l111lIIl1ll.size() >= this.l1lll11l1l.getValue()
         || this.l1I1IlllIlI.getValue() && this.l1IIl11lI.hasElapsed(this.IllI1l1l1.getValue().longValue())) {
         this.l1I1IIl111Il1l1l();
         this.l1IIl11lI.reset();
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventWorldChange eventWorldChange) {
      this.lIIl11l111lIIl1ll.clear();
   }

   private void l1I1IIl111Il1l1l() {
      Priority3.getInstance().queuePackets(this.lIIl11l111lIIl1ll);
      this.lIIl11l111lIIl1ll.clear();
   }
}

