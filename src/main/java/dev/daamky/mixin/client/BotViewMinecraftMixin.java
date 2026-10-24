package dev.daamky.mixin.client;

import dev.daamky.BotSession;
import dev.daamky.BotClientPlayHandler;
import dev.daamky.ModuleToggleProfileManager;
import dev.daamky.SavedBots;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketApplyBatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class BotViewMinecraftMixin {
   @Shadow
   public ClientWorld world;

   @Shadow
   public abstract void setScreen(Screen screen);

   @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("HEAD"), cancellable = true)
   private void daamky$botSetLevel(ClientWorld world, CallbackInfo callbackInfo) {
      if (BotClientPlayHandler.isClientViewStateIsolationActive()) {
         this.world = world;
         callbackInfo.cancel();
      }
   }

   @Inject(method = "setScreenAndRender(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"), cancellable = true)
   private void daamky$botSetScreenAndShow(Screen screen, CallbackInfo callbackInfo) {
      if (BotClientPlayHandler.isClientStateIsolationActive()) {
         this.setScreen(screen);
         callbackInfo.cancel();
      }
   }

   @ModifyVariable(method = "setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"), argsOnly = true)
   private Screen daamky$botSetScreen(Screen screen) {
      if (ModuleToggleProfileManager.isProfileScopeActive()) {
         BotSession activeSession = SavedBots.getActiveSession();
         String scopedProfileName = ModuleToggleProfileManager.getScopedProfileName();
         boolean sameBotSession = activeSession != null && scopedProfileName.equalsIgnoreCase(activeSession.getUsername());
         if (!sameBotSession && this.world != null) {
            return null;
         }
      }

      return screen;
   }

   @Redirect(method = "render(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketApplyBatcher;apply()V"))
   private void daamky$mainPackets(PacketApplyBatcher packetBatcher) {
      SavedBots.runWithSessionStateIsolation(packetBatcher::apply);
   }

   @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;ZZ)V", at = @At("HEAD"))
   private void daamky$botDisconnect(Screen screen, boolean transferring, boolean showTitleScreen, CallbackInfo callbackInfo) {
      if (SavedBots.getActiveSession() != null) {
         SavedBots.restorePreviousSession();
      }
   }
}

