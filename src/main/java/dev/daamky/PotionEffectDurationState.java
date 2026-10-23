package dev.daamky;

record PotionEffectDurationState(int amplifier, long durationTicks, boolean infinite) {
    private static final String pc56a6a45 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
// https://t.me/pasterplace

   PotionEffectDurationState(int amplifier, long durationTicks, boolean infinite) {
      this.amplifier = amplifier;
      this.durationTicks = durationTicks;
      this.infinite = infinite;
   }
}

