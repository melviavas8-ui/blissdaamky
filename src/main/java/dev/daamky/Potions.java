package dev.daamky;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

public final class Potions extends AnimatedListHudElement {
    private static final String p9c904258 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final Map<StatusEffect, Integer> l1lll11l1l = new HashMap<>();
// https://t.me/pasterplace

   public Potions() {
      super("Potions", "potions", 7.0F, 120.0F);
   }
// https://t.me/pasterplace

   @Override
   protected String getHeaderGlyph() {
      return "O";
   }
// https://t.me/pasterplace

   @Override
   protected boolean shouldShowDetailProgress() {
      return true;
   }
// https://t.me/pasterplace

   @Override
   protected String getHeaderLabel() {
      return "Potions";
   }
// https://t.me/pasterplace

   @Override
   protected List<AnimatedListHudElement.HudListEntry> getEntries(boolean chatScreenOpen) {
      ArrayList arrayList = new ArrayList();
      if (CLIENT.player == null) {
         return arrayList;
      }
// https://t.me/pasterplace

      List<StatusEffectInstance> effects = new ArrayList<>(CLIENT.player.getStatusEffects());
      this.l1lll11l1l
         .keySet()
         .removeIf(statusEffect -> effects.stream().noneMatch(statusEffectInstance -> statusEffectInstance.getEffectType().value() == statusEffect));
      effects.sort(Comparator.comparingInt(StatusEffectInstance::getDuration));

      for (StatusEffectInstance effectInstance : effects) {
         StatusEffect statusEffect = effectInstance.getEffectType().value();
         String effectName = statusEffect.getName().getString();
         if (effectInstance.getAmplifier() > 0) {
            effectName = effectName + " " + BrokkeIcons.toRomanNumeral(effectInstance.getAmplifier() + 1);
         }

         String durationText = effectInstance.isInfinite() ? "**:**" : BrokkeIcons.formatDurationTicks(effectInstance.getDuration());
         RgbaColor l1I1IlllIlI4 = RgbaColor.fromArgb(0xFF000000 | statusEffect.getColor());
         RgbaColor durationColor = statusEffect.isBeneficial() ? null : RgbaColor.rgb(255, 120, 120);
         if (!effectInstance.isInfinite() && effectInstance.getDuration() < 200) {
            durationColor = RgbaColor.rgb(255, 200, 120);
         }

         float durationProgress = 1.0F;
         if (!effectInstance.isInfinite()) {
            int maxDurationTicks = Math.max(effectInstance.getDuration(), this.l1lll11l1l.getOrDefault(statusEffect, 0));
            this.l1lll11l1l.put((StatusEffect)statusEffect, maxDurationTicks);
            durationProgress = (float)effectInstance.getDuration() / Math.max(1, maxDurationTicks);
         }

         arrayList.add(new AnimatedListHudElement.HudListEntry(effectName, durationText, null, l1I1IlllIlI4, durationColor, durationProgress, InGameHud.getEffectTexture(effectInstance.getEffectType())));
      }

      return arrayList;
   }
}

