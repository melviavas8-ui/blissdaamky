package dev.daamky.mixin.entity;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFrameEntity.class)
public interface ItemFrameAccessor {
   @Accessor("ROTATION")
   static TrackedData<Integer> daamky$getRotationData() {
      throw new AssertionError();
   }
}
