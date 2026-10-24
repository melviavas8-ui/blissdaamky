package dev.daamky;

public interface DisconnectScreenExtension {
   boolean hasPictureInPicture(BotSessionInfo sessionInfo);

   void closePictureInPicture();
}
