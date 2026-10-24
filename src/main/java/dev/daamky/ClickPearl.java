package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventKey;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ClickPearl extends ModuleStructure {
   private static final int l1I1IlllIlI = 20;
   private final ReallyWorld IllI1l1l1 = new ReallyWorld();
   private final IntSetting l1lll11l1l = new IntSetting("Бинд броска", dev.daamky.KeybindManager.encodeMouseButton(2));
   private final MultiOptionSetting lIIl11l111lIIl1ll = new MultiOptionSetting("Режим", "Обычный", "Обычный", "Инвентарь");
   private final BooleanSetting l1IIl11lI = new BooleanSetting("Возвращать слот", true);
   private final FloatSetting I1lIIII1III1lIlIl1l = new FloatSetting("Задержка", 250.0F, 0.0F, 1000.0F, 10.0F, "мс");
   private final Stopwatch l11lIlIIll1III1 = new Stopwatch();
   private boolean enabled;
   private int Ill111IlI11l = -1;
   private int I1l11IIlIIIll1l1lI1II = -1;
   private int I1I1lI111lII1;
   private boolean lIll1Il1lll111l11l1I1llllI;

   public ClickPearl() {
      super("ClickPearl", "Бросает эндер-жемчуг по бинду", ModuleCategory.PLAYER);
      this.addSettings(this.l1lll11l1l, this.lIIl11l111lIIl1ll, this.l1IIl11lI, this.I1lIIII1III1lIlIl1l, this.IllI1l1l1);
   }

   @Override
   public String getDisplayInfo() {
      return this.lIIl11l111lIIl1ll.getValue();
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.enabled = false;
      this.IIIlll111l111();
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventWorldChange eventWorldChange) {
      this.enabled = false;
      this.IIIlll111l111();
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventKey eventKey) {
      int keyCode = dev.daamky.KeybindManager.encodeKey(eventKey.key(), eventKey.isMouse());
      if (this.l1lll11l1l.matchesKeyCode(keyCode) && this.hasActiveClientWorld() && CLIENT.currentScreen == null) {
          ClickFriend clickFriend = (ClickFriend)ModuleRegistry.getInstance().getModuleByClass(ClickFriend.class);
          if (clickFriend != null && clickFriend.isEnabled() && clickFriend.matchesKey(keyCode)) {
            HitResult hitResult = CLIENT.crosshairTarget;
            if (hitResult instanceof EntityHitResult) {
                EntityHitResult hitResult2 = (EntityHitResult)hitResult;
               if (hitResult2.getEntity() instanceof PlayerEntity) {
                  return;
               }
            }
         }

         if (this.Ill111IlI11l == -1 && this.l11lIlIIll1III1.hasElapsed(this.I1lIIII1III1lIlIl1l.getValue().longValue())) {
            this.enabled = true;
         }
      }
   }

   @EventTarget
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         if (this.Ill111IlI11l != -1) {
            this.l1I1IIl111Il1l1l();
         } else if (this.enabled) {
            this.enabled = false;
            if (!CLIENT.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.ENDER_PEARL))) {
               float yaw = CLIENT.player.getYaw();
               float pitch = CLIENT.player.getPitch();
               if (CLIENT.player.getOffHandStack().isOf(Items.ENDER_PEARL)) {
                  InventoryUtils.interactItemWithRotation(Hand.OFF_HAND, yaw, pitch);
                  this.l11lIlIIll1III1.reset();
               } else {
                  int pearlHotbarSlot = InventoryUtils.findHotbarSlot(candidateStack -> candidateStack.isOf(Items.ENDER_PEARL));
                  if (pearlHotbarSlot != -1) {
                     int previousHotbarSlot = InventoryUtils.getHotbarSlotToRestore();
                     InventoryUtils.setSelectedHotbarSlot(pearlHotbarSlot);
                     InventoryUtils.interactItemWithRotation(Hand.MAIN_HAND, yaw, pitch);
                     if (this.l1IIl11lI.getValue() && previousHotbarSlot != pearlHotbarSlot) {
                        InventoryUtils.rememberHotbarSlotForRestore(previousHotbarSlot);
                     }

                     this.l11lIlIIll1III1.reset();
                  } else {
                     if (this.lIIl11l111lIIl1ll.isSelected("Инвентарь") && InventoryUtils.canSwapInventorySlots()) {
                        int pearlInventorySlot = InventoryUtils.findInventorySlot(candidateStack -> candidateStack.isOf(Items.ENDER_PEARL));
                        if (pearlInventorySlot == -1) {
                           return;
                        }

                        this.Ill111IlI11l = pearlInventorySlot;
                        this.I1l11IIlIIIll1l1lI1II = InventoryUtils.getSelectedHotbarSlot();
                        this.I1I1lI111lII1 = 0;
                        this.lIll1Il1lll111l11l1I1llllI = false;
                        InventoryUtils.swapInventorySlotWithHotbar(pearlInventorySlot, this.I1l11IIlIIIll1l1lI1II, this.IllI1l1l1.getInventorySwapCategory());
                        this.l11lIlIIll1III1.reset();
                     }
                  }
               }
            }
         }
      }
   }

   private void l1I1IIl111Il1l1l() {
      if (++this.I1I1lI111lII1 > 20 || InventoryUtils.getSelectedHotbarSlot() != this.I1l11IIlIIIll1l1lI1II) {
         this.IIIlll111l111();
      } else if (!Priority.getInstance().hasQueuedActions()) {
         if (!this.lIll1Il1lll111l11l1I1llllI) {
            if (CLIENT.player.getMainHandStack().isOf(Items.ENDER_PEARL)) {
               InventoryUtils.interactItemWithRotation(Hand.MAIN_HAND, CLIENT.player.getYaw(), CLIENT.player.getPitch());
               this.lIll1Il1lll111l11l1I1llllI = true;
               if (!this.l1IIl11lI.getValue()) {
                  this.IIIlll111l111();
               }
            }
         } else {
            if (InventoryUtils.canSwapInventorySlots()) {
               InventoryUtils.swapInventorySlotWithHotbar(this.Ill111IlI11l, this.I1l11IIlIIIll1l1lI1II, this.IllI1l1l1.getInventorySwapCategory());
            }

            this.IIIlll111l111();
         }
      }
   }

   private void IIIlll111l111() {
      this.Ill111IlI11l = -1;
      this.I1l11IIlIIIll1l1lI1II = -1;
      this.I1I1lI111lII1 = 0;
      this.lIll1Il1lll111l11l1I1llllI = false;
   }
}

