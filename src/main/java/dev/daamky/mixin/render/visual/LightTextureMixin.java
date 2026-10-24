package dev.daamky.mixin.render.visual;

import dev.daamky.FullBright;
import dev.daamky.NoRender;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LightmapTextureManager.class)
public abstract class LightTextureMixin {
   @ModifyArg(
      method = "update",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal = 3)
   )
   private float daamky$nightVision(float lightValue) {
      return FullBright.IllI1l1l1(lightValue);
   }

   @ModifyArg(
      method = "update",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal = 4)
   )
   private float daamky$darknessScale(float darknessScale) {
      if (NoRender.II1Il11l111II11IIl("Плохие эффекты")) {
         darknessScale = 0.0F;
      }

      return FullBright.l1lll11l1l(darknessScale);
   }

   @ModifyArg(
      method = "update",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal = 6)
   )
   private float daamky$brightness(float brightness) {
      return FullBright.lIIl11l111lIIl1ll(brightness);
   }
}

