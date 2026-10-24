package dev.daamky;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Setting<T> {
   private final String name;
   private T value;
   private Supplier<Boolean> visibilityCondition = () -> true;
   private Consumer<T> changeListener;
   private String description = "";
   private float animationProgress = 0.0F;

   public Setting(String name, T initialValue) {
      this.name = name;
      this.value = initialValue;
   }

   public String getName() {
      return this.name;
   }

   public T getValue() {
      return this.value;
   }

   public void setValue(T newValue) {
      this.value = newValue;
      if (this.changeListener != null) {
         this.changeListener.accept(newValue);
      }
   }

   public String getDescription() {
      return this.description;
   }

   public Setting<T> withDescription(String description) {
      this.description = description == null ? "" : description;
      return this;
   }

   public boolean isVisible() {
      return this.visibilityCondition == null || this.visibilityCondition.get();
   }

   public Setting<T> visibleWhen(Supplier<Boolean> visibilityCondition) {
      this.visibilityCondition = visibilityCondition;
      return this;
   }

   public Setting<T> onChanged(Consumer<T> changeListener) {
      this.changeListener = changeListener;
      return this;
   }

   public float getAnimationProgress() {
      return this.animationProgress;
   }

   public void setAnimationProgress(float progress) {
      this.animationProgress = progress;
   }
}

