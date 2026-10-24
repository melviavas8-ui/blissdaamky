package dev.daamky;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ClientSounds extends ModuleStructure {
   private static final SoundEvent UI_CLICK_SOUND = SoundEvent.of(Identifier.of("daamky", "click"));
   private static final SoundEvent MODULE_ENABLE_SOUND = SoundEvent.of(Identifier.of("daamky", "enable"));
   private static final SoundEvent SLIDER_TICK_SOUND = SoundEvent.of(Identifier.of("daamky", "tick"));
   private static final SoundEvent MODULE_DISABLE_SOUND = SoundEvent.of(Identifier.of("daamky", "disable"));
   private final FloatSetting volume = new FloatSetting("Volume", 0.6F, 0.1F, 1.0F, 0.05F);
   private final BooleanSetting playGuiClickSounds = new BooleanSetting("GUI Clicks", true);
   private final BooleanSetting playModuleToggleSounds = new BooleanSetting("Module Toggles", true);
   private final BooleanSetting playSliderSounds = new BooleanSetting("Sliders", true);
   private long lastSliderSoundMillis;

   public ClientSounds() {
      super("ClientSounds", "Plays sounds for GUI clicks and module toggles", ModuleCategory.WORLD);
      this.addSettings(this.volume, this.playGuiClickSounds, this.playModuleToggleSounds, this.playSliderSounds);
      this.setEnabled(true);
   }

   private static ClientSounds getEnabledInstance() {
      ClientSounds module = (ClientSounds)ModuleRegistry.getInstance().getModuleByClass(ClientSounds.class);
      return module != null && module.isEnabled() ? module : null;
   }

   public static void playModuleToggleSound(ModuleStructure moduleStructure) {
      ClientSounds clientSounds = getEnabledInstance();
      if (clientSounds != null && moduleStructure != clientSounds && clientSounds.playModuleToggleSounds.getValue()) {
         clientSounds.playSound(moduleStructure.isEnabled() ? MODULE_ENABLE_SOUND : MODULE_DISABLE_SOUND, 1.0F);
      }
   }

   public static void playGuiClickSound() {
      ClientSounds clientSounds = getEnabledInstance();
      if (clientSounds != null && clientSounds.playGuiClickSounds.getValue()) {
         clientSounds.playSound(UI_CLICK_SOUND, 0.97F + ThreadLocalRandom.current().nextFloat() * 0.06F);
      }
   }

   public static void playSliderSound(float volume) {
      ClientSounds clientSounds = getEnabledInstance();
      long currentTimeMillis = System.currentTimeMillis();
      if (clientSounds != null && clientSounds.playSliderSounds.getValue() && currentTimeMillis - clientSounds.lastSliderSoundMillis >= 35L) {
         clientSounds.lastSliderSoundMillis = currentTimeMillis;
         clientSounds.playSound(SLIDER_TICK_SOUND, 0.8F + Math.clamp(volume, 0.0F, 1.0F) * 0.6F);
      }
   }

   private void playSound(SoundEvent soundEvent, float volume) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.getSoundManager() != null) {
         client.getSoundManager().play(PositionedSoundInstance.ui(soundEvent, volume, this.volume.getValue()));
      }
   }
}

