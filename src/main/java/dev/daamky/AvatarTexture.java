package dev.daamky;

import net.minecraft.util.Identifier;

public final class AvatarTexture {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final String name;
   private final Identifier textureIdentifier;
// https://t.me/pasterplace

   AvatarTexture(String name, Identifier textureIdentifier) {
      this.name = name;
      this.textureIdentifier = textureIdentifier;
   }
// https://t.me/pasterplace

   public String name() {
      return this.name;
   }
// https://t.me/pasterplace

   public Identifier textureIdentifier() {
      return this.textureIdentifier;
   }
// https://t.me/pasterplace

   public TextureSize textureSize() {
      return Avatar.getTextureSize(this.textureIdentifier);
   }
// https://t.me/pasterplace

   public int width() {
      return this.textureSize().l1I1IlllIlI();
   }
// https://t.me/pasterplace

   public int height() {
      return this.textureSize().IllI1l1l1();
   }
// https://t.me/pasterplace

   public float aspectRatio() {
      return this.textureSize().II1Il11l111II11IIl();
   }
// https://t.me/pasterplace

   public TextureQuadBuilder textureQuad() {
      return TextureQuadBuilder.of(this);
   }
}

