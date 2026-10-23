package dev.daamky.mixin.entity;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
   @Accessor("jumpingCooldown")
   void daamky$setNoJumpDelay(int jumpDelayTicks);
}
