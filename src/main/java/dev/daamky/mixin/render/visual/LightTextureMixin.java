package dev.daamky.mixin.render.visual;

import dev.daamky.FullBright;
import dev.daamky.NoRender;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LightmapTextureManager.class)
public abstract class LightTextureMixin {
    private static final String p457c6bc = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @ModifyArg(
      method = "update",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal = 3)
   )
   private float daamky$nightVision(float lightValue) {
      return FullBright.IllI1l1l1(lightValue);
   }
// https://t.me/pasterplace

   @ModifyArg(
      method = "update",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal = 4)
   )
   private float daamky$darknessScale(float darknessScale) {
      if (NoRender.II1Il11l111II11IIl("Плохие эффекты")) {
         darknessScale = 0.0F;
      }
// https://t.me/pasterplace

      return FullBright.l1lll11l1l(darknessScale);
   }
// https://t.me/pasterplace

   @ModifyArg(
      method = "update",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal = 6)
   )
   private float daamky$brightness(float brightness) {
      return FullBright.lIIl11l111lIIl1ll(brightness);
   }
}

