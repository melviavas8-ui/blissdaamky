package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class DragonFly extends ModuleStructure {
    private static final String pe7e5ae38 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Режим", "Обычный", "Обычный", "Свой");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Скорость XZ", 1.1F, 0.1F, 5.0F, 0.01F);
   private final FloatSetting l1lll11l1l = new FloatSetting("Скорость Y", 0.3F, 0.1F, 5.0F, 0.05F);
   private final FloatSetting lIIl11l111lIIl1ll = new FloatSetting("Скорость по диагонали", 1.01F, 0.1F, 5.0F, 0.01F);
   private final BooleanSetting l1IIl11lI = new BooleanSetting("AutoFly", false);
   private final Stopwatch I1lIIII1III1lIlIl1l = new Stopwatch();
   private boolean l11lIlIIll1III1;
// https://t.me/pasterplace

   public DragonFly() {
      super("DragonFly", "Ускоряет полёт в режиме /fly", ModuleCategory.MOVEMENT);
      this.lIIl11l111lIIl1ll.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Свой"));
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1, this.l1lll11l1l, this.lIIl11l111lIIl1ll, this.l1IIl11lI);
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.getValue();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.l11lIlIIll1III1 = CLIENT.player != null && this.l1I1IIl111Il1l1l();
   }
// https://t.me/pasterplace

   private boolean l1I1IIl111Il1l1l() {
      return CLIENT.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
   }
// https://t.me/pasterplace

   private void IIIlll111l111() {
      boolean elytraEquipped = this.l1I1IIl111Il1l1l();
      boolean newlyEquipped = elytraEquipped && !this.l11lIlIIll1III1;
      this.l11lIlIIll1III1 = elytraEquipped;
      if (newlyEquipped && this.l1IIl11lI.getValue() && CLIENT.getNetworkHandler() != null) {
         if (!CLIENT.player.getAbilities().allowFlying
            && !Aceopxykhbtm.II1Il11l111II11IIl()
            && this.I1lIIII1III1lIlIl1l.hasElapsed(3000L)) {
            CLIENT.getNetworkHandler().sendChatCommand("fly");
            this.I1lIIII1III1lIlIl1l.reset();
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         this.IIIlll111l111();
         if (CLIENT.player.getAbilities().flying) {
            double verticalVelocity = 0.0;
            if (CLIENT.options.jumpKey.isPressed()) {
               verticalVelocity = 0.0 + this.l1lll11l1l.getValue().floatValue();
            }

            if (CLIENT.options.sneakKey.isPressed()) {
               verticalVelocity -= this.l1lll11l1l.getValue().floatValue();
            }
// https://t.me/pasterplace

            Vec3d playerVelocity = CLIENT.player.getVelocity();
            CLIENT.player.setVelocity(playerVelocity.x, verticalVelocity, playerVelocity.z);
            MovementUtils.setHorizontalSpeed(this.lI1IlIl1I11II111l11l());
         }
      }
   }
// https://t.me/pasterplace

   private double lI1IlIl1I11II111l11l() {
      if (!this.l1I1IlllIlI.isSelected("Свой")) {
         return this.IllI1l1l1.getValue().floatValue();
      }

      PlayerInput playerInput = CLIENT.player.input.playerInput;
      boolean diagonalMovement = playerInput.forward() != playerInput.backward() && playerInput.left() != playerInput.right();
      return (diagonalMovement ? this.lIIl11l111lIIl1ll.getValue() : this.IllI1l1l1.getValue()).floatValue();
   }
}

