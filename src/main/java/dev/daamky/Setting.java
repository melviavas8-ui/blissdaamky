package dev.daamky;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Setting<T> {
    private static final String p275c8cac = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final String name;
   private T value;
   private Supplier<Boolean> visibilityCondition = () -> true;
   private Consumer<T> changeListener;
   private String description = "";
   private float animationProgress = 0.0F;
// https://t.me/pasterplace

   public Setting(String name, T initialValue) {
      this.name = name;
      this.value = initialValue;
   }
// https://t.me/pasterplace

   public String getName() {
      return this.name;
   }
// https://t.me/pasterplace

   public T getValue() {
      return this.value;
   }
// https://t.me/pasterplace

   public void setValue(T newValue) {
      this.value = newValue;
      if (this.changeListener != null) {
         this.changeListener.accept(newValue);
      }
   }
// https://t.me/pasterplace

   public String getDescription() {
      return this.description;
   }
// https://t.me/pasterplace

   public Setting<T> withDescription(String description) {
      this.description = description == null ? "" : description;
      return this;
   }
// https://t.me/pasterplace

   public boolean isVisible() {
      return this.visibilityCondition == null || this.visibilityCondition.get();
   }
// https://t.me/pasterplace

   public Setting<T> visibleWhen(Supplier<Boolean> visibilityCondition) {
      this.visibilityCondition = visibilityCondition;
      return this;
   }
// https://t.me/pasterplace

   public Setting<T> onChanged(Consumer<T> changeListener) {
      this.changeListener = changeListener;
      return this;
   }
// https://t.me/pasterplace

   public float getAnimationProgress() {
      return this.animationProgress;
   }
// https://t.me/pasterplace

   public void setAnimationProgress(float progress) {
      this.animationProgress = progress;
   }
}

