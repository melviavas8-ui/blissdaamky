package dev.daamky.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayerEntity.class)
public interface LocalPlayerAccessor {
   @Accessor("lastSprinting")
   boolean daamky$wasSprinting();

   @Accessor("lastSprinting")
   void daamky$setWasSprinting(boolean wasSprinting);
}
