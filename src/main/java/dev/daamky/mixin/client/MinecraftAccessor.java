package dev.daamky.mixin.client;

import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.session.Session;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftAccessor {
   @Accessor("itemUseCooldown")
   int daamky$getRightClickDelay();
// https://t.me/pasterplace

   @Accessor("itemUseCooldown")
   void daamky$setRightClickDelay(int delayTicks);
// https://t.me/pasterplace

   @Accessor("cameraEntity")
   Entity daamky$getCameraEntity();
// https://t.me/pasterplace

   @Accessor("cameraEntity")
   void daamky$setCameraEntity(Entity entity);
// https://t.me/pasterplace

   @Mutable
   @Accessor("session")
   void daamky$setUser(Session session);
// https://t.me/pasterplace

   @Mutable
   @Accessor("gameProfileFuture")
   void daamky$setProfileFuture(CompletableFuture<ProfileResult> profileFuture);
// https://t.me/pasterplace

   @Mutable
   @Accessor("framebuffer")
   void daamky$setMainRenderTarget(Framebuffer framebuffer);
// https://t.me/pasterplace

   @Mutable
   @Accessor("worldRenderer")
   void daamky$setLevelRenderer(WorldRenderer worldRenderer);
// https://t.me/pasterplace

   @Invoker("doAttack")
   boolean daamky$startAttack();
// https://t.me/pasterplace

   @Invoker("doItemUse")
   void daamky$startUseItem();
}
