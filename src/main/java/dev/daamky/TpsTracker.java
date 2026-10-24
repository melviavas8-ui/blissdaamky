package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventWorldChange;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public final class TpsTracker {
   private static final TpsTracker INSTANCE = new TpsTracker();
   private static final int tpsSamples = 10;
   private final float[] nextSampleIndex = new float[10];
   private int sampleCount;
   private int lastTimeUpdateMillis;
   private long currentTps = -1L;
   private volatile float I1lIIII1III1lIlIl1l = 20.0F;

   private TpsTracker() {
   }

   public static TpsTracker getInstance() {
      return INSTANCE;
   }

   public float getTps() {
      return this.I1lIIII1III1lIlIl1l;
   }

   @EventTarget
   public void onPacket(EventPacket eventPacket) {
      if (eventPacket.isReceive() && eventPacket.packet() instanceof WorldTimeUpdateS2CPacket) {
         long currentTimeMillis = System.currentTimeMillis();
         if (this.currentTps != -1L) {
            float intervalSeconds = (float)(currentTimeMillis - this.currentTps) / 1000.0F;
            if (intervalSeconds > 0.0F) {
               this.nextSampleIndex[this.sampleCount] = Math.clamp(20.0F / intervalSeconds, 0.0F, 20.0F);
               this.sampleCount = (this.sampleCount + 1) % 10;
               this.lastTimeUpdateMillis = Math.min(this.lastTimeUpdateMillis + 1, 10);
               float averageTps = 0.0F;

               for (int sampleIndex = 0; sampleIndex < this.lastTimeUpdateMillis; sampleIndex++) {
                  averageTps += this.nextSampleIndex[sampleIndex];
               }

               this.I1lIIII1III1lIlIl1l = averageTps / this.lastTimeUpdateMillis;
            }
         }

         this.currentTps = currentTimeMillis;
      }
   }

   @EventTarget
   public void onWorldChange(EventWorldChange eventWorldChange) {
      this.currentTps = -1L;
      this.lastTimeUpdateMillis = 0;
      this.sampleCount = 0;
      this.I1lIIII1III1lIlIl1l = 20.0F;
   }
}

