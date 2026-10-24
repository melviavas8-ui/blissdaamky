package dev.daamky;

import net.minecraft.item.map.MapState.UpdateData;

public interface BotSessionCallback {
   BotSessionCallback NO_OP = new BotSessionCallback() {
      @Override
      public void onSessionReset() {
      }

      @Override
      public void onMapUpdate(int mapId, UpdateData updateData) {
      }

      @Override
      public void onServerMessage(String message) {
      }
   };

   void onSessionReset();

   void onMapUpdate(int mapId, UpdateData updateData);

   void onServerMessage(String message);

   default void onMapDecoration(int frameEntityId, double worldX, double worldY, double worldZ, int entityData) {
   }

   default void onMapColorUpdate(int frameEntityId, int mapId) {
   }

   default void onMapCursorUpdate(int frameEntityId, int rotation) {
   }

   default void onPlayerLookUpdate(double playerX, double playerY, double playerZ, float yawDegrees) {
   }
}
