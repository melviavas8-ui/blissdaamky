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
    private static final String pa857f6c2 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final int II1Il11l111II11IIl = 128;
   private static final Map<UUID, Supplier<SkinTextures>> l1I1IlllIlI = new LinkedHashMap<UUID, Supplier<SkinTextures>>(32, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<UUID, Supplier<SkinTextures>> entry) {
         return this.size() > 128;
      }
   };
// https://t.me/pasterplace

   private PlayerSkinTextureCache() {
   }
// https://t.me/pasterplace

   static Supplier<SkinTextures> II1Il11l111II11IIl(GameProfile gameProfile) {
      return l1I1IlllIlI.computeIfAbsent(gameProfile.id(), uUID -> MinecraftClient.getInstance().getSkinProvider().supplySkinTextures(gameProfile, false));
   }
// https://t.me/pasterplace

   static void II1Il11l111II11IIl() {
      l1I1IlllIlI.clear();
   }
}

