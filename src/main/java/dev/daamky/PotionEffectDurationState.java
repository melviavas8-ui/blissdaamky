package dev.daamky;

record PotionEffectDurationState(int amplifier, long durationTicks, boolean infinite) {

   PotionEffectDurationState(int amplifier, long durationTicks, boolean infinite) {
      this.amplifier = amplifier;
      this.durationTicks = durationTicks;
      this.infinite = infinite;
   }
}

