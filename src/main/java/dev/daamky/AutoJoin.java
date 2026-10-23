package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventTick;
import java.util.Locale;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoJoin extends ModuleStructure {
    private static final String p9b5c0ff = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Anarchy Number", 1.0F, 1.0F, 700.0F, 1.0F);
   private final FloatSetting IllI1l1l1 = new FloatSetting("Retry Delay", 8.0F, 3.0F, 60.0F, 1.0F, "s");
   private final Stopwatch l1lll11l1l = new Stopwatch();
   private static final int lIIl11l111lIIl1ll = 50;
   private volatile boolean l1IIl11lI;
   private int I1lIIII1III1lIlIl1l;
// https://t.me/pasterplace

   public AutoJoin() {
      super("AutoJoin", "Repeats joining the selected anarchy until successful", ModuleCategory.WORLD);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return "#" + this.l1I1IlllIlI.getValue().intValue();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.l1IIl11lI = false;
      this.I1lIIII1III1lIlIl1l = 0;
      this.l1lll11l1l.setElapsedMillis(4611686018427387903L);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventTick eventTick) {
      if (!this.l1IIl11lI) {
         if (!Spooky.getInstance().isReconnecting()) {
            if (CLIENT.player != null && CLIENT.getNetworkHandler() != null) {
               if (this.l1lll11l1l.hasElapsed((long)(this.IllI1l1l1.getValue() * 1000.0F))) {
                  if (++this.I1lIIII1III1lIlIl1l > 50) {
                     this.setEnabled(false);
                     return;
                  }
// https://t.me/pasterplace

                  Spooky.getInstance().startAnarchyReconnect(new String[]{String.valueOf(this.l1I1IlllIlI.getValue().intValue())});
                  this.l1lll11l1l.reset();
               }
            }
         } else {
            this.l1lll11l1l.reset();
         }
      } else {
         this.setEnabled(false);
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (eventPacket.isReceive()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof GameMessageS2CPacket) {
             GameMessageS2CPacket packet2 = (GameMessageS2CPacket)packet;
             String messageText = packet2.content().getString().toLowerCase(Locale.ROOT);
            if (messageText.contains("вы уже подключены") || messageText.contains("уже на этом сервере") || messageText.contains("добро пожаловать на анархию")) {
               this.l1IIl11lI = true;
            }
         }
      }
   }
}

