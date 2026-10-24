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

   @Accessor("itemUseCooldown")
   void daamky$setRightClickDelay(int delayTicks);

   @Accessor("cameraEntity")
   Entity daamky$getCameraEntity();

   @Accessor("cameraEntity")
   void daamky$setCameraEntity(Entity entity);

   @Mutable
   @Accessor("session")
   void daamky$setUser(Session session);

   @Mutable
   @Accessor("gameProfileFuture")
   void daamky$setProfileFuture(CompletableFuture<ProfileResult> profileFuture);

   @Mutable
   @Accessor("framebuffer")
   void daamky$setMainRenderTarget(Framebuffer framebuffer);

   @Mutable
   @Accessor("worldRenderer")
   void daamky$setLevelRenderer(WorldRenderer worldRenderer);

   @Invoker("doAttack")
   boolean daamky$startAttack();

   @Invoker("doItemUse")
   void daamky$startUseItem();
}
