package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ClickFriend extends ModuleStructure {
    private static final String pc6979212 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final IntSetting l1I1IlllIlI = new IntSetting("Bind", dev.daamky.KeybindManager.encodeMouseButton(2));
// https://t.me/pasterplace

   public ClickFriend() {
      super("ClickFriend", "Adds or removes players from friends by keybind", ModuleCategory.WORLD);
      this.addSetting(this.l1I1IlllIlI);
      this.setEnabled(true);
   }
// https://t.me/pasterplace

   public boolean matchesKey(int key) {
      return this.l1I1IlllIlI.matchesKeyCode(key);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventKey eventKey) {
      if (this.l1I1IlllIlI.matchesKeyCode(dev.daamky.KeybindManager.encodeKey(eventKey.key(), eventKey.isMouse()))
         && this.hasActiveClientWorld()
         && CLIENT.currentScreen == null) {
         HitResult hitResult = CLIENT.crosshairTarget;
         if (hitResult instanceof EntityHitResult) {
             EntityHitResult hitResult2 = (EntityHitResult)hitResult;
            Entity entity = hitResult2.getEntity();
             if (entity instanceof PlayerEntity player) {
                String playerName = player.getGameProfile().name();
               boolean friendAdded = FriendManager.getInstance().toggleFriend(playerName);
               ClientConfigManager.getInstance().saveConfig();
               ClientChatLogger.II1Il11l111II11IIl(!friendAdded ? playerName + " removed from friends" : playerName + " added to friends");
               return;
            }
         }
      }
   }
}

