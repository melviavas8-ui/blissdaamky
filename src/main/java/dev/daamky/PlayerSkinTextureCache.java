package dev.daamky;

import com.mojang.authlib.GameProfile;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.SkinTextures;

final class PlayerSkinTextureCache {
   private static final int II1Il11l111II11IIl = 128;
   private static final Map<UUID, Supplier<SkinTextures>> l1I1IlllIlI = new LinkedHashMap<UUID, Supplier<SkinTextures>>(32, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<UUID, Supplier<SkinTextures>> entry) {
         return this.size() > 128;
      }
   };

   private PlayerSkinTextureCache() {
   }

   static Supplier<SkinTextures> II1Il11l111II11IIl(GameProfile gameProfile) {
      return l1I1IlllIlI.computeIfAbsent(gameProfile.id(), uUID -> MinecraftClient.getInstance().getSkinProvider().supplySkinTextures(gameProfile, false));
   }

   static void II1Il11l111II11IIl() {
      l1I1IlllIlI.clear();
   }
}

