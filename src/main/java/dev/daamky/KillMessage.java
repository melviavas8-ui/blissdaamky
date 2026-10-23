package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventAttack;
import dev.daamky.core.event.impl.EventUpdate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class KillMessage extends ModuleStructure {
    private static final String p4b1f47ef = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final List<String> l1I1IlllIlI = List.of("gg, %s", "%s, good game", "ez %s", "%s sent to respawn");
   private final MultiOptionSetting IllI1l1l1 = new MultiOptionSetting("Channel", "Chat", "Chat", "PM");
   private PlayerEntity l1lll11l1l;
   private long lIIl11l111lIIl1ll;
// https://t.me/pasterplace

   public KillMessage() {
      super("KillMessage", "Sends a message after killing a player", ModuleCategory.WORLD);
      this.addSetting(this.IllI1l1l1);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventAttack eventAttack) {
      if (eventAttack.isPost()) {
         Entity entity = eventAttack.target();
         if (entity instanceof PlayerEntity) {
         PlayerEntity entity2 = (PlayerEntity)entity;
            this.l1lll11l1l = entity2;
            this.lIIl11l111lIIl1ll = System.currentTimeMillis();
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.l1lll11l1l != null && this.hasActiveClientWorld()) {
         if (System.currentTimeMillis() - this.lIIl11l111lIIl1ll > 1500L) {
            this.l1lll11l1l = null;
         } else {
            if (this.l1lll11l1l.isDead()) {
               String playerName = this.l1lll11l1l.getGameProfile().name();
               String chatMessage = String.format(l1I1IlllIlI.get(ThreadLocalRandom.current().nextInt(l1I1IlllIlI.size())), playerName);
               if (!this.IllI1l1l1.isSelected("PM") && !this.IllI1l1l1.isSelected("Личка")) {
                  CLIENT.getNetworkHandler().sendChatMessage(chatMessage);
               } else {
                  CLIENT.getNetworkHandler().sendChatCommand("msg " + playerName + " " + chatMessage);
               }
// https://t.me/pasterplace

               this.l1lll11l1l = null;
            }
         }
      }
   }
}

