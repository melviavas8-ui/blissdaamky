package dev.daamky.mixin.render.visual;

import dev.daamky.HudModule;
import dev.daamky.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class HudVisualMixin {
   @Inject(method = "renderBossBarHud", at = @At("HEAD"), cancellable = true)
   private void daamky$noBossBar(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Босс-бар")) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
   private void daamky$noScoreboard(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Скорборд")) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
   private void daamky$customScoreboard(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
      if (HudModule.isElementEnabled("Scoreboard")) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderMainHud", at = @At("HEAD"), cancellable = true)
   private void daamky$customHotbar(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.interactionManager != null && client.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR && HudModule.isElementEnabled("Hotbar")) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderTitleAndSubtitle", at = @At("HEAD"), cancellable = true)
   private void daamky$noTitle(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Тайтлы")) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
   private void daamky$noVignette(DrawContext drawContext, Entity cameraEntity, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Виньетка")) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
   private void daamky$noTextureOverlay(DrawContext drawContext, Identifier overlayTexture, float overlayOpacity, CallbackInfo callbackInfo) {
      String overlaySettingName = overlayTexture.getPath().contains("powder_snow") ? "Заморозка" : "Тыква на экране";
      if (NoRender.II1Il11l111II11IIl(overlaySettingName)) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
   private void daamky$noPortal(DrawContext drawContext, float overlayStrength, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Плохие эффекты")) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true)
   private void daamky$noConfusion(DrawContext drawContext, float overlayStrength, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Плохие эффекты")) {
         callbackInfo.cancel();
      }
   }
}

