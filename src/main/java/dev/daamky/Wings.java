package dev.daamky;

public enum Wings {
   WINGS("wings", "Крылья", true),
   BACK("back", "Спина", true),
   PETS("pet", "Питомцы", true),
   HATS("hat", "Шляпы", false),
   WATCHES("watch", "Часы", true);

   private final String identifier;
   private final String displayName;
   private final boolean attachToBody;

   Wings(String identifier, String displayName, boolean attachToBody) {
      this.identifier = identifier;
      this.displayName = displayName;
      this.attachToBody = attachToBody;
   }

   public String getIdentifier() {
      return this.identifier;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public boolean isAttachedToBody() {
      return this.attachToBody;
   }

   public static Wings fromIdentifier(String identifier) {
      for (Wings category : values()) {
         if (category.identifier.equalsIgnoreCase(identifier)) {
            return category;
         }
      }

      return null;
   }

   // $VF: synthetic method
   private static Wings[] createValuesArray() {
        return new Wings[]{WINGS, BACK, PETS, HATS, WATCHES};
   }
}
