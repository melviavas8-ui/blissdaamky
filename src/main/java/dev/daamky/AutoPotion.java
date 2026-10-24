package dev.daamky;

import net.minecraft.entity.LivingEntity;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import java.util.Iterator;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoPotion extends ModuleStructure {
   private static final float THROW_PITCH_DEGREES = 88.0F;
   private final MultiOptionSetting useMode = new MultiOptionSetting("Режим", "Взрывные", "Взрывные", "Питьевые");
   private final MultiToggleSetting throwEffectSettings = new MultiToggleSetting("Зелья")
      .addOption("Сила", true)
      .addOption("Скорость", true)
      .addOption("Огнестойкость", true)
      .addOption("Исцеление", true)
      .addOption("Регенерация", false);
   private final FloatSetting healingHealthThreshold = new FloatSetting("Здоровье для исцеления", 8.0F, 2.0F, 20.0F, 0.5F, "HP");
   private final BooleanSetting onlyThrowOnGround = new BooleanSetting("Только на земле", true);
   private final BooleanSetting pauseWhileAttacking = new BooleanSetting("Пауза при атаке", true);
   private final FloatSetting delayMillis = new FloatSetting("Задержка", 400.0F, 100.0F, 2000.0F, 50.0F, "мс");
   private final FloatSetting hotbarSlotSetting = new FloatSetting("Слот хотбара", 9.0F, 1.0F, 9.0F, 1.0F);
   private final MultiToggleSetting refillEffectSettings = new MultiToggleSetting("Пополнять")
      .addOption("Сила", true)
      .addOption("Скорость", true)
      .addOption("Огнестойкость", true)
      .addOption("Регенерация", false)
      .addOption("Исцеление", false);
   private final ReallyWorld reallyWorldSettings = new ReallyWorld();
   private final Stopwatch potionUseTimer = new Stopwatch();
   private int activePotionSlot = -1;
   private int throwAttemptTicks;
   private boolean refillAfterPotionUsePending;
   private RegistryEntry<StatusEffect> lastPotionEffectUsed;

   public AutoPotion() {
      super("AutoPotion", "Кидает взрывные зелья или пополняет слот с питьевыми", ModuleCategory.COMBAT);
      this.throwEffectSettings.visibleWhen(() -> this.useMode.isSelected("Взрывные"));
      this.healingHealthThreshold.visibleWhen(() -> this.useMode.isSelected("Взрывные") && this.throwEffectSettings.isOptionEnabled("Исцеление"));
      this.onlyThrowOnGround.visibleWhen(() -> this.useMode.isSelected("Взрывные"));
      this.pauseWhileAttacking.visibleWhen(() -> this.useMode.isSelected("Взрывные"));
      this.hotbarSlotSetting.visibleWhen(() -> this.useMode.isSelected("Питьевые"));
      this.refillEffectSettings.visibleWhen(() -> this.useMode.isSelected("Питьевые"));
      this.reallyWorldSettings.visibleWhen(() -> this.useMode.isSelected("Питьевые"));
      this.addSettings(
         this.useMode,
         this.throwEffectSettings,
         this.healingHealthThreshold,
         this.onlyThrowOnGround,
         this.pauseWhileAttacking,
         this.hotbarSlotSetting,
         this.refillEffectSettings,
         this.reallyWorldSettings,
         this.delayMillis
      );
   }

   @Override
   public String getDisplayInfo() {
      return this.useMode.getValue();
   }

   @EventTarget
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.useMode.isSelected("Питьевые")) {
         this.activePotionSlot = -1;
         if (this.hasActiveClientWorld()) {
            this.refillPotionSlot();
         }
      } else if (this.hasActiveClientWorld() && CLIENT.currentScreen == null && !CLIENT.player.isUsingItem()) {
         if (this.activePotionSlot == -1) {
            if (!this.potionUseTimer.hasElapsed(this.delayMillis.getValue().longValue())) {
               return;
            }

            if (this.onlyThrowOnGround.getValue() && !CLIENT.player.isOnGround()) {
               return;
            }

            if (this.pauseWhileAttacking.getValue()) {
               Aura aura = Aura.getInstance();
               if (aura != null && aura.isEnabled() && aura.getTarget() != null && CombatUtils.getAttackCooldownProgress() > 0.8F) {
                  return;
               }
            }

            this.activePotionSlot = this.findBestSplashPotionSlot();
            this.throwAttemptTicks = 0;
            if (this.activePotionSlot == -1) {
               return;
            }
         }

         this.throwAttemptTicks++;
         if (this.throwAttemptTicks > 8) {
            this.activePotionSlot = -1;
         } else {
            Rotation lIll1Il1lll111l11l1I1llllI2 = new Rotation(
               RotationPriorityManager.getInstance().getCurrentRotation().getYaw(), 88.0F
            );
            RotationPriorityManager.getInstance().requestRotation(lIll1Il1lll111l11l1I1llllI2, FocusMode.FREE, 60.0F, 5, false, this);
            Rotation lIll1Il1lll111l11l1I1llllI22 = RotationPriorityManager.getInstance().getLastMotionRotation();
            if (this.throwAttemptTicks >= 2 && !(lIll1Il1lll111l11l1I1llllI22.getPitch() < 80.0F)) {
               ItemStack potionStack = CLIENT.player.getInventory().getStack(this.activePotionSlot);
               if (potionStack.isOf(Items.SPLASH_POTION)) {
                  InventoryUtils.useHotbarSlot(
                     this.activePotionSlot, lIll1Il1lll111l11l1I1llllI22.getYaw(), lIll1Il1lll111l11l1I1llllI22.getPitch()
                  );
               }

               this.activePotionSlot = -1;
               this.potionUseTimer.reset();
            }
         }
      } else {
         this.activePotionSlot = -1;
      }
   }

   private int findBestSplashPotionSlot() {
      float health = CombatUtils.getEffectiveHealth((LivingEntity)CLIENT.player);
      if (this.throwEffectSettings.isOptionEnabled("Исцеление") && health <= this.healingHealthThreshold.getValue()) {
         int healingPotionSlot = this.findSplashPotionSlot(StatusEffects.INSTANT_HEALTH);
         if (healingPotionSlot != -1) {
            return healingPotionSlot;
         }
      }

      if (this.throwEffectSettings.isOptionEnabled("Регенерация")
         && health <= this.healingHealthThreshold.getValue() + 4.0F
         && !CLIENT.player.hasStatusEffect(StatusEffects.REGENERATION)) {
         int regenerationPotionSlot = this.findSplashPotionSlot(StatusEffects.REGENERATION);
         if (regenerationPotionSlot != -1) {
            return regenerationPotionSlot;
         }
      }

      if (this.throwEffectSettings.isOptionEnabled("Сила") && !CLIENT.player.hasStatusEffect(StatusEffects.STRENGTH)) {
         int strengthPotionSlot = this.findSplashPotionSlot(StatusEffects.STRENGTH);
         if (strengthPotionSlot != -1) {
            return strengthPotionSlot;
         }
      }

      if (this.throwEffectSettings.isOptionEnabled("Скорость") && !CLIENT.player.hasStatusEffect(StatusEffects.SPEED)) {
         int speedPotionSlot = this.findSplashPotionSlot(StatusEffects.SPEED);
         if (speedPotionSlot != -1) {
            return speedPotionSlot;
         }
      }

      return this.throwEffectSettings.isOptionEnabled("Огнестойкость") && !CLIENT.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)
         ? this.findSplashPotionSlot(StatusEffects.FIRE_RESISTANCE)
         : -1;
   }

   private void refillPotionSlot() {
      int hotbarSlot = this.hotbarSlotSetting.getValue().intValue() - 1;
      ItemStack potionStack = CLIENT.player.getInventory().getStack(hotbarSlot);
      boolean isUsingSelectedPotion = CLIENT.player.isUsingItem() && InventoryUtils.getSelectedHotbarSlot() == hotbarSlot && potionStack.isOf(Items.POTION);
      if (isUsingSelectedPotion) {
         this.lastPotionEffectUsed = this.getFirstPotionEffect(potionStack);
         this.refillAfterPotionUsePending = true;
      } else if (this.refillAfterPotionUsePending) {
         if (!potionStack.isEmpty() && !potionStack.isOf(Items.GLASS_BOTTLE)) {
            this.refillAfterPotionUsePending = false;
         } else if (CLIENT.currentScreen == null
            && InventoryUtils.canSwapInventorySlots()
            && this.potionUseTimer.hasElapsed(this.delayMillis.getValue().longValue())) {
            int replacementInventorySlot = this.findReplacementPotionSlot(hotbarSlot);
            this.refillAfterPotionUsePending = false;
            if (replacementInventorySlot != -1) {
               InventoryUtils.swapInventorySlotWithHotbar(replacementInventorySlot, hotbarSlot, this.reallyWorldSettings.getInventorySwapCategory());
               this.potionUseTimer.reset();
            }
         }
      }
   }

   private int findReplacementPotionSlot(int hotbarSlot) {
      int fallbackPotionSlot = -1;

      for (String effectName : new String[]{"Сила", "Скорость", "Огнестойкость", "Регенерация", "Исцеление"}) {
         if (this.refillEffectSettings.isOptionEnabled(effectName)) {
            RegistryEntry registryEntry = this.getStatusEffectByName(effectName);
            int inventorySlot = this.findPotionSlotForEffect(registryEntry, hotbarSlot);
            if (inventorySlot != -1) {
               if (!registryEntry.equals(this.lastPotionEffectUsed) && !CLIENT.player.hasStatusEffect(registryEntry)) {
                  return inventorySlot;
               }

               if (fallbackPotionSlot == -1) {
                  fallbackPotionSlot = inventorySlot;
               }
            }
         }
      }

      return fallbackPotionSlot;
   }

   private RegistryEntry<StatusEffect> getStatusEffectByName(String effectName) {
      return switch (effectName) {
         case "Сила" -> StatusEffects.STRENGTH;
         case "Скорость" -> StatusEffects.SPEED;
         case "Огнестойкость" -> StatusEffects.FIRE_RESISTANCE;
         case "Регенерация" -> StatusEffects.REGENERATION;
         default -> StatusEffects.INSTANT_HEALTH;
      };
   }

   private int findPotionSlotForEffect(RegistryEntry<StatusEffect> registryEntry, int excludedSlot) {
      for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
         if (inventorySlot != excludedSlot) {
            ItemStack potionStack = CLIENT.player.getInventory().getStack(inventorySlot);
            if (potionStack.isOf(Items.POTION) && this.containsPotionEffect(potionStack, registryEntry)) {
               return inventorySlot;
            }
         }
      }

      return -1;
   }

   private RegistryEntry<StatusEffect> getFirstPotionEffect(ItemStack potionStack) {
       PotionContentsComponent potionContents = potionStack.get(DataComponentTypes.POTION_CONTENTS);
      if (potionContents == null) {
         return null;
      } else {
          Iterator<StatusEffectInstance> effectIterator = potionContents.getEffects().iterator();
         if (effectIterator.hasNext()) {
             StatusEffectInstance effectInstance = effectIterator.next();
            return effectInstance.getEffectType();
         } else {
            return null;
         }
      }
   }

   private boolean containsPotionEffect(ItemStack potionStack, RegistryEntry<StatusEffect> registryEntry) {
       PotionContentsComponent potionContents = potionStack.get(DataComponentTypes.POTION_CONTENTS);
      if (potionContents == null) {
         return false;
      }

       for (StatusEffectInstance effectInstance : potionContents.getEffects()) {
         if (effectInstance.getEffectType().equals(registryEntry)) {
            return true;
         }
      }

      return false;
   }

   private int findSplashPotionSlot(RegistryEntry<StatusEffect> registryEntry) {
      return InventoryUtils.findHotbarSlot(potionStack -> {
         if (!potionStack.isOf(Items.SPLASH_POTION)) {
            return false;
         }

          PotionContentsComponent potionContents = potionStack.get(DataComponentTypes.POTION_CONTENTS);
         if (potionContents == null) {
            return false;
         }

          for (StatusEffectInstance effectInstance : potionContents.getEffects()) {
            if (effectInstance.getEffectType().equals(registryEntry)) {
               return true;
            }
         }

         return false;
      });
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.activePotionSlot = -1;
      this.refillAfterPotionUsePending = false;
      this.lastPotionEffectUsed = null;
      RotationPriorityManager.getInstance().clearRotationRequest(this);
   }
}

