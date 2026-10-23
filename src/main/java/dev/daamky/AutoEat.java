package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoEat extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting foodSelection = new MultiOptionSetting("Еда", "Любая", "Любая", "Золотая", "Сытная");
   private final FloatSetting hungerThreshold = new FloatSetting("Порог голода", 16.0F, 1.0F, 19.0F, 1.0F, "ед.");
   private final BooleanSetting eatAtLowHealth = new BooleanSetting("Есть при низком HP", true);
   private final FloatSetting healthThreshold = new FloatSetting("Порог здоровья", 12.0F, 2.0F, 20.0F, 1.0F, "HP");
   private final BooleanSetting restorePreviousHotbarSlot = new BooleanSetting("Возвращать слот", true);
   private final BooleanSetting avoidEatingDuringCombat = new BooleanSetting("Не есть в бою", false);
   private final BooleanSetting tryFeedCommandFirst = new BooleanSetting("Сначала /feed", true);
   private int previousHotbarSlot = -1;
   private boolean isEating;
   private boolean awaitingFeedResponse;
   private int hungerBeforeFeedCommand;
   private final Stopwatch feedCommandStopwatch = new Stopwatch();
   private final Stopwatch feedCooldownStopwatch = new Stopwatch();
   private boolean feedCommandPending;
// https://t.me/pasterplace

   public AutoEat() {
      super("AutoEat", "Автоматически ест при голоде или низком здоровье", ModuleCategory.PLAYER);
      this.healthThreshold.visibleWhen(this.eatAtLowHealth::getValue);
      this.addSettings(
         this.foodSelection, this.hungerThreshold, this.eatAtLowHealth, this.healthThreshold, this.restorePreviousHotbarSlot, this.avoidEatingDuringCombat, this.tryFeedCommandFirst
      );
   }
// https://t.me/pasterplace

   @EventTarget
   public void onUpdate(EventUpdate eventUpdate) {
      if (!this.hasActiveClientWorld() || CLIENT.currentScreen != null) {
         this.stopEatingAndRestoreSlot();
      } else if (this.isEating) {
         if (this.shouldEat() && isValidFood(CLIENT.player.getMainHandStack())) {
            CLIENT.options.useKey.setPressed(true);
         } else {
            this.stopEatingAndRestoreSlot();
         }
      } else if (!this.shouldEat()) {
         this.awaitingFeedResponse = false;
      } else if (!CLIENT.player.isUsingItem() && !this.tryFeedCommand()) {
         if (this.avoidEatingDuringCombat.getValue()) {
            Aura aura = Aura.getInstance();
            if (aura != null && aura.isEnabled() && aura.getTarget() != null) {
               return;
            }
         }

         int foodSlot = this.findBestFoodSlot();
         if (foodSlot != -1) {
            this.previousHotbarSlot = InventoryUtils.getSelectedHotbarSlot();
            InventoryUtils.setSelectedHotbarSlot(foodSlot);
            CLIENT.options.useKey.setPressed(true);
            this.isEating = true;
         }
      }
   }
// https://t.me/pasterplace

   private boolean tryFeedCommand() {
      if (this.tryFeedCommandFirst.getValue() && CLIENT.getNetworkHandler() != null) {
         int currentHunger = CLIENT.player.getHungerManager().getFoodLevel();
         if (this.awaitingFeedResponse) {
            if (currentHunger > this.hungerBeforeFeedCommand) {
               this.awaitingFeedResponse = false;
               return true;
            }

            if (!this.feedCommandStopwatch.hasElapsed(2000L)) {
               return true;
            }

            this.awaitingFeedResponse = false;
            this.feedCommandPending = true;
            this.feedCooldownStopwatch.reset();
            return false;
         } else {
            if (this.feedCommandPending && !this.feedCooldownStopwatch.hasElapsed(60000L)) {
               return false;
            }

            this.feedCommandPending = false;
            this.hungerBeforeFeedCommand = currentHunger;
            this.awaitingFeedResponse = true;
            this.feedCommandStopwatch.reset();
            CLIENT.getNetworkHandler().sendChatCommand("feed");
            return true;
         }
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   private boolean shouldEat() {
      boolean hungerBelowThreshold = CLIENT.player.getHungerManager().getFoodLevel() <= this.hungerThreshold.getValue();
      boolean healthBelowThreshold = this.eatAtLowHealth.getValue() && CombatUtils.getEffectiveHealth((LivingEntity)CLIENT.player) <= this.healthThreshold.getValue();
      return healthBelowThreshold && !hungerBelowThreshold && !this.foodSelection.isSelected("Золотая")
         ? CLIENT.player.getHungerManager().isNotFull() || this.findGoldenAppleSlot() != -1
         : hungerBelowThreshold || healthBelowThreshold;
   }
// https://t.me/pasterplace

   private int findGoldenAppleSlot() {
      return InventoryUtils.findHotbarSlot(foodStack -> foodStack.isOf(Items.GOLDEN_APPLE) || foodStack.isOf(Items.ENCHANTED_GOLDEN_APPLE));
   }
// https://t.me/pasterplace

   private int findBestFoodSlot() {
      if (this.foodSelection.isSelected("Золотая")) {
         return this.findGoldenAppleSlot();
      }

      boolean shouldPrioritizeGoldenApple = this.eatAtLowHealth.getValue() && CombatUtils.getEffectiveHealth((LivingEntity)CLIENT.player) <= this.healthThreshold.getValue();
      if (shouldPrioritizeGoldenApple) {
         int goldenAppleSlot = this.findGoldenAppleSlot();
         if (goldenAppleSlot != -1) {
            return goldenAppleSlot;
         }
      }

      int bestFoodSlot = -1;
      float bestNutritionScore = -1.0F;

      for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
         ItemStack candidateFood = CLIENT.player.getInventory().getStack(hotbarSlot);
         if (isValidFood(candidateFood)) {
            FoodComponent food = candidateFood.get(DataComponentTypes.FOOD);
            float foodScore = this.foodSelection.isSelected("Сытная") ? food.nutrition() + food.saturation() : food.nutrition();
            if (candidateFood.isOf(Items.GOLDEN_APPLE) || candidateFood.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
               foodScore -= 100.0F;
            }

            if (foodScore > bestNutritionScore) {
               bestNutritionScore = foodScore;
               bestFoodSlot = hotbarSlot;
            }
         }
      }
// https://t.me/pasterplace

      return bestFoodSlot;
   }
// https://t.me/pasterplace

   private static boolean isValidFood(ItemStack foodStack) {
      FoodComponent food = foodStack.get(DataComponentTypes.FOOD);
      return food != null
         && !foodStack.isOf(Items.ROTTEN_FLESH)
         && !foodStack.isOf(Items.SPIDER_EYE)
         && !foodStack.isOf(Items.POISONOUS_POTATO)
         && !foodStack.isOf(Items.PUFFERFISH)
         && !foodStack.isOf(Items.CHORUS_FRUIT)
         && !foodStack.isOf(Items.SUSPICIOUS_STEW);
   }
// https://t.me/pasterplace

   private void stopEatingAndRestoreSlot() {
      if (this.isEating) {
         CLIENT.options.useKey.setPressed(CLIENT.mouse.wasRightButtonClicked());
         if (this.restorePreviousHotbarSlot.getValue() && this.previousHotbarSlot != -1 && CLIENT.player != null) {
            InventoryUtils.setSelectedHotbarSlot(this.previousHotbarSlot);
         }
      }

      this.isEating = false;
      this.previousHotbarSlot = -1;
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.stopEatingAndRestoreSlot();
   }
}

