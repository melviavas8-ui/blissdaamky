package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import java.util.Locale;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoAccept extends ModuleStructure {
    private static final String p30bf48a1 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Server", "ReallyWorld", "ReallyWorld", "FunTime", "Normal");
   private final BooleanSetting IllI1l1l1 = new BooleanSetting("Only Friends", true);
// https://t.me/pasterplace

   public AutoAccept() {
      super("AutoAccept", "Automatically accepts teleport requests", ModuleCategory.WORLD);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.getValue();
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (eventPacket.isReceive()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof GameMessageS2CPacket) {
             GameMessageS2CPacket packet2 = (GameMessageS2CPacket)packet;
             if (!packet2.overlay()) {
                String chatMessage = packet2.content().getString();
               String lowercaseMessage = chatMessage.toLowerCase(Locale.ROOT);
               if (!lowercaseMessage.contains("телепортироваться") && !lowercaseMessage.contains("teleport")) {
                  return;
               }

               if (this.IllI1l1l1.getValue() && !II1Il11l111II11IIl(chatMessage)) {
                  return;
               }
// https://t.me/pasterplace

               String acceptCommand = !this.l1I1IlllIlI.isSelected("FunTime") ? "tpaccept" : "tpyes";
               CLIENT.execute(() -> {
                  if (CLIENT.getNetworkHandler() != null) {
                     CLIENT.getNetworkHandler().sendChatCommand(acceptCommand);
                  }
               });
               return;
            }
         }
      }
   }
// https://t.me/pasterplace

   private static boolean II1Il11l111II11IIl(String message) {
      for (Object friendName : FriendManager.getInstance().getFriendNames()) {
         if (message.toLowerCase(Locale.ROOT).contains((CharSequence)friendName)) {
            return true;
         }
      }

      return false;
   }
}

