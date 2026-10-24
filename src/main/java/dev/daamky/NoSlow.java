package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class NoSlow extends ModuleStructure {
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Режим", "ReallyWorld", "ReallyWorld", "Matrix");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Скорость", 100.0F, 20.0F, 100.0F, 1.0F, "%");
   private final BooleanSetting l1lll11l1l = new BooleanSetting("Разрешить бег", true);
   private int lIIl11l111lIIl1ll;

   public NoSlow() {
      super("NoSlow", "Убирает замедление при использовании предметов", ModuleCategory.MOVEMENT);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1, this.l1lll11l1l);
   }

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.getValue();
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && CLIENT.player.isUsingItem()) {
         this.lIIl11l111lIIl1ll++;
      } else {
         this.lIIl11l111lIIl1ll = 0;
      }
   }

   private float l1lll11l1l(float inputMultiplier) {
      return switch ((String)this.l1I1IlllIlI.getValue()) {
         case "ReallyWorld" -> {
            ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
            boolean canSkipSlowdown = clientPlayerEntity != null && !clientPlayerEntity.isTouchingWater() && this.lIIl11l111lIIl1ll > 2 && this.lIIl11l111lIIl1ll % 3 == 0;
            yield canSkipSlowdown ? 1.0F : inputMultiplier;
         }
         case "Matrix" -> {
            ClientPlayerEntity clientPlayerEntity2 = MinecraftClient.getInstance().player;
            yield clientPlayerEntity2 != null && clientPlayerEntity2.isOnGround() ? Math.max(inputMultiplier, 0.55F) : Math.max(inputMultiplier, 0.9F);
         }
         default -> inputMultiplier;
      };
   }

   public static float IllI1l1l1(float inputMultiplier) {
      NoSlow module = (NoSlow)ModuleRegistry.getInstance().getModuleByClass(NoSlow.class);
      return module != null && module.isEnabled() ? module.l1lll11l1l(inputMultiplier) : inputMultiplier;
   }

   public static boolean l1I1IIl111Il1l1l() {
      NoSlow module = (NoSlow)ModuleRegistry.getInstance().getModuleByClass(NoSlow.class);
      return module != null && module.isEnabled() && module.l1lll11l1l.getValue();
   }
}

