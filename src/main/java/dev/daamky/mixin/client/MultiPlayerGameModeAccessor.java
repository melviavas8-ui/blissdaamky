package dev.daamky.mixin.client;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerInteractionManager.class)
public interface MultiPlayerGameModeAccessor {
   @Accessor("blockBreakingCooldown")
   void daamky$setDestroyDelay(int destroyDelay);
// https://t.me/pasterplace

   @Accessor("currentBreakingProgress")
   float daamky$getDestroyProgress();
// https://t.me/pasterplace

   @Accessor("currentBreakingProgress")
   void daamky$setDestroyProgress(float destroyProgress);
// https://t.me/pasterplace

   @Invoker("syncSelectedSlot")
   void daamky$syncSelectedSlot();
}
