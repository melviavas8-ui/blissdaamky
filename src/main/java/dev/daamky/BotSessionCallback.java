package dev.daamky;

import net.minecraft.item.map.MapState.UpdateData;

public interface BotSessionCallback {
   BotSessionCallback NO_OP = new BotSessionCallback() {
      @Override
      public void onSessionReset() {
      }
// https://t.me/pasterplace

      @Override
      public void onMapUpdate(int mapId, UpdateData updateData) {
      }

      @Override
      public void onServerMessage(String message) {
      }
   };
// https://t.me/pasterplace

   void onSessionReset();
// https://t.me/pasterplace

   void onMapUpdate(int mapId, UpdateData updateData);
// https://t.me/pasterplace

   void onServerMessage(String message);
// https://t.me/pasterplace

   default void onMapDecoration(int frameEntityId, double worldX, double worldY, double worldZ, int entityData) {
   }
// https://t.me/pasterplace

   default void onMapColorUpdate(int frameEntityId, int mapId) {
   }
// https://t.me/pasterplace

   default void onMapCursorUpdate(int frameEntityId, int rotation) {
   }
// https://t.me/pasterplace

   default void onPlayerLookUpdate(double playerX, double playerY, double playerZ, float yawDegrees) {
   }
}
