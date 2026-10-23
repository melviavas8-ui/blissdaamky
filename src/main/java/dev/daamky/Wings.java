package dev.daamky;

public enum Wings {
   WINGS("wings", "Крылья", true),
   BACK("back", "Спина", true),
   PETS("pet", "Питомцы", true),
   HATS("hat", "Шляпы", false),
   WATCHES("watch", "Часы", true);
// https://t.me/pasterplace

   private final String identifier;
   private final String displayName;
   private final boolean attachToBody;
// https://t.me/pasterplace

   Wings(String identifier, String displayName, boolean attachToBody) {
      this.identifier = identifier;
      this.displayName = displayName;
      this.attachToBody = attachToBody;
   }
// https://t.me/pasterplace

   public String getIdentifier() {
      return this.identifier;
   }
// https://t.me/pasterplace

   public String getDisplayName() {
      return this.displayName;
   }
// https://t.me/pasterplace

   public boolean isAttachedToBody() {
      return this.attachToBody;
   }
// https://t.me/pasterplace

   public static Wings fromIdentifier(String identifier) {
      for (Wings category : values()) {
         if (category.identifier.equalsIgnoreCase(identifier)) {
            return category;
         }
      }
// https://t.me/pasterplace

      return null;
   }
// https://t.me/pasterplace

   // $VF: synthetic method
   private static Wings[] createValuesArray() {
        return new Wings[]{WINGS, BACK, PETS, HATS, WATCHES};
   }
}
