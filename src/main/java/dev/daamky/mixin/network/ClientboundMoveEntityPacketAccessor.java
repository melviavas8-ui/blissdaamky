package dev.daamky.mixin.network;

import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityS2CPacket.class)
public interface ClientboundMoveEntityPacketAccessor {
   @Accessor("id")
   int daamky$getEntityId();
}
