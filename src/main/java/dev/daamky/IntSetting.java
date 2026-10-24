package dev.daamky;

public final class IntSetting extends Setting<Integer> {
   public IntSetting(String name, int defaultValue) {
      super(name, defaultValue);
   }

   public boolean matchesKeyCode(int keyCode) {
      return keyCode != 0 && this.getValue() == keyCode;
   }
}

