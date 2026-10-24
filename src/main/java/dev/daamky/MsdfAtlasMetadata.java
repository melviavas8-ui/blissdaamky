package dev.daamky;

public record MsdfAtlasMetadata(String atlasName, float distanceRange, float atlasSize, int width, int height) {

   public MsdfAtlasMetadata(String atlasName, float distanceRange, float atlasSize, int width, int height) {
      this.atlasName = atlasName;
      this.distanceRange = distanceRange;
      this.atlasSize = atlasSize;
      this.width = width;
      this.height = height;
   }
}

