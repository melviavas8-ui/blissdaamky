package dev.daamky;

import net.minecraft.util.Identifier;

public final class AvatarTexture {
   private final String name;
   private final Identifier textureIdentifier;

   AvatarTexture(String name, Identifier textureIdentifier) {
      this.name = name;
      this.textureIdentifier = textureIdentifier;
   }

   public String name() {
      return this.name;
   }

   public Identifier textureIdentifier() {
      return this.textureIdentifier;
   }

   public TextureSize textureSize() {
      return Avatar.getTextureSize(this.textureIdentifier);
   }

   public int width() {
      return this.textureSize().l1I1IlllIlI();
   }

   public int height() {
      return this.textureSize().IllI1l1l1();
   }

   public float aspectRatio() {
      return this.textureSize().II1Il11l111II11IIl();
   }

   public TextureQuadBuilder textureQuad() {
      return TextureQuadBuilder.of(this);
   }
}

