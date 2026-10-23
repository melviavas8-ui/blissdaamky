package dev.daamky;

public final class IntSetting extends Setting<Integer> {
    private static final String pd90dc182 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public IntSetting(String name, int defaultValue) {
      super(name, defaultValue);
   }
// https://t.me/pasterplace

   public boolean matchesKeyCode(int keyCode) {
      return keyCode != 0 && this.getValue() == keyCode;
   }
}

