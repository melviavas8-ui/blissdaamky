package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class TriggerBot extends ModuleStructure {
    private static final String p32abae49 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Режим", "Умный", "Обычный", "Умный", "Задержка");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Дистанция атаки", 3.0F, 2.0F, 6.0F, 0.1F, "бл.");
   private final FloatSetting l1lll11l1l = new FloatSetting("Мин. задержка", 90.0F, 0.0F, 500.0F, 10.0F, "мс");
   private final FloatSetting lIIl11l111lIIl1ll = new FloatSetting("Макс. задержка", 160.0F, 0.0F, 600.0F, 10.0F, "мс");
   private final MultiToggleSetting l1IIl11lI = new MultiToggleSetting("Цели")
      .addOption("Игроки", true)
      .addOption("Мобы", true)
      .addOption("Животные", false)
      .addOption("Друзья", false);
   private final BooleanSetting I1lIIII1III1lIlIl1l = new BooleanSetting("Только криты", true);
   private final BooleanSetting l11lIlIIll1III1 = new BooleanSetting("Только с оружием", true);
   private final BooleanSetting l1IIl11lI2 = new BooleanSetting("Ломать щит", false);
   private final Stopwatch Ill111IlI11l = new Stopwatch();
   private long I1l11IIlIIIll1l1lI1II;
   private int I1I1lI111lII1 = -1;
// https://t.me/pasterplace

   public TriggerBot() {
      super("TriggerBot", "Атакует цель, на которую наведён прицел", ModuleCategory.COMBAT);
      this.l1lll11l1l.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Задержка"));
      this.lIIl11l111lIIl1ll.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Задержка"));
      this.addSettings(
         this.l1I1IlllIlI,
         this.IllI1l1l1,
         this.l1lll11l1l,
         this.lIIl11l111lIIl1ll,
         this.l1IIl11lI,
         this.I1lIIII1III1lIlIl1l,
         this.l11lIlIIll1III1,
         this.l1IIl11lI2
      );
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.getValue();
   }
// https://t.me/pasterplace

   @EventTarget
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && CLIENT.currentScreen == null) {
         if (this.I1I1lI111lII1 != -1) {
            InventoryUtils.setSelectedHotbarSlot(this.I1I1lI111lII1);
            this.I1I1lI111lII1 = -1;
         }
// https://t.me/pasterplace

         HitResult hitResult = CLIENT.crosshairTarget;
         if (hitResult instanceof EntityHitResult) {
            EntityHitResult hitResult2 = (EntityHitResult)hitResult;
            Entity entity = hitResult2.getEntity();
            if (entity instanceof LivingEntity) {
               Entity entity2 = (LivingEntity)entity;
               if (this.II1Il11l111II11IIl(entity2)) {
                  if (CLIENT.player.getEyePos().distanceTo(hitResult2.getPos()) > this.IllI1l1l1.getValue().floatValue()) {
                     return;
                  }

                  if (this.l11lIlIIll1III1.getValue() && !CombatUtils.isInInvalidCriticalState(CLIENT.player)) {
                     return;
                  }

                  if (CLIENT.player.isUsingItem()) {
                     return;
                  }

                  if (CombatUtils.getAttackCooldownProgress() < 0.93F) {
                     return;
                  }

                  if (this.l1I1IlllIlI.isSelected("Задержка") && !this.Ill111IlI11l.hasElapsed(this.I1l11IIlIIIll1l1lI1II)) {
                     return;
                  }

                  if (this.I1lIIII1III1lIlIl1l.getValue()
                     && !CombatUtils.isInInvalidCriticalState(CLIENT.player)
                     && !CombatUtils.isFallingForCriticalHit(CLIENT.player)) {
                     boolean smartTriggerAllowed = this.l1I1IlllIlI.isSelected("Умный")
                        && CLIENT.player.isOnGround()
                        && !CLIENT.options.jumpKey.isPressed();
                     if (!smartTriggerAllowed) {
                        return;
                     }
                  }

                  if (this.l1IIl11lI2.getValue() && entity2 instanceof PlayerEntity) {
                  PlayerEntity entity3 = (PlayerEntity)entity2;
                     if (entity3.isBlocking()) {
                        int axeSlot = InventoryUtils.findHotbarSlot(candidateAxe -> candidateAxe.isIn(ItemTags.AXES));
                        if (axeSlot != -1 && axeSlot != InventoryUtils.getSelectedHotbarSlot()) {
                           this.I1I1lI111lII1 = InventoryUtils.getSelectedHotbarSlot();
                           InventoryUtils.setSelectedHotbarSlot(axeSlot);
                        }
                     }
                  }

                  CombatUtils.attackEntity(entity2);
                  this.Ill111IlI11l.reset();
                  float minimumAttackDelay = Math.min(this.l1lll11l1l.getValue(), this.lIIl11l111lIIl1ll.getValue());
                  float maximumAttackDelay = Math.max(this.l1lll11l1l.getValue(), this.lIIl11l111lIIl1ll.getValue());
                  this.I1l11IIlIIIll1l1lI1II = (long)(minimumAttackDelay + ThreadLocalRandom.current().nextFloat() * (maximumAttackDelay - minimumAttackDelay));
                  return;
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private boolean II1Il11l111II11IIl(Entity entity) {
      if (!entity.isAlive() || entity instanceof ArmorStandEntity) {
         return false;
      } else if (entity instanceof PlayerEntity player) {
         return this.l1IIl11lI.isOptionEnabled("Игроки") && !AntiBot.II1Il11l111II11IIl(player)
            ? this.l1IIl11lI.isOptionEnabled("Друзья") || !FriendManager.getInstance().isFriend(player)
            : false;
      } else if (entity instanceof Monster) {
         return this.l1IIl11lI.isOptionEnabled("Мобы");
      } else {
         return entity instanceof AnimalEntity ? this.l1IIl11lI.isOptionEnabled("Животные") : this.l1IIl11lI.isOptionEnabled("Мобы");
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      if (this.I1I1lI111lII1 != -1 && CLIENT.player != null) {
         InventoryUtils.setSelectedHotbarSlot(this.I1I1lI111lII1);
      }

      this.I1I1lI111lII1 = -1;
   }
}

