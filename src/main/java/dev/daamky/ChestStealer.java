package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;

public final class ChestStealer extends ModuleStructure {
    private static final String p2cd20bd = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Задержка", 80.0F, 0.0F, 500.0F, 10.0F, "мс");
   private final BooleanSetting IllI1l1l1 = new BooleanSetting("Случайная задержка", true);
   private final BooleanSetting l1lll11l1l = new BooleanSetting("Закрывать после", true);
   private final BooleanSetting lIIl11l111lIIl1ll = new BooleanSetting("Шалкеры", true);
   private final Stopwatch l1IIl11lI = new Stopwatch();
   private long I1lIIII1III1lIlIl1l;
   private int l11lIlIIll1III1 = -1;
// https://t.me/pasterplace

   public ChestStealer() {
      super("ChestStealer", "Быстро забирает всё из сундуков", ModuleCategory.PLAYER);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1, this.l1lll11l1l, this.lIIl11l111lIIl1ll);
   }
// https://t.me/pasterplace

   @EventTarget
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         Screen screen = CLIENT.currentScreen;
         if (screen instanceof HandledScreen) {
             HandledScreen<?> screen2 = (HandledScreen<?>)screen;
            ScreenHandler containerScreenHandler = screen2.getScreenHandler();
            int containerSlotCount;
            if (containerScreenHandler instanceof GenericContainerScreenHandler) {
                GenericContainerScreenHandler containerHandler = (GenericContainerScreenHandler)containerScreenHandler;
               containerSlotCount = containerHandler.getRows() * 9;
            } else {
               if (!this.lIIl11l111lIIl1ll.getValue() || !(containerScreenHandler instanceof ShulkerBoxScreenHandler)) {
                  return;
               }

               containerSlotCount = 27;
            }

            if (containerScreenHandler.syncId != this.l11lIlIIll1III1) {
               this.l11lIlIIll1III1 = containerScreenHandler.syncId;
               this.l1IIl11lI.reset();
               this.I1lIIII1III1lIlIl1l = Math.max(100L, this.l1I1IlllIlI.getValue().longValue());
               return;
            }
// https://t.me/pasterplace

            if (this.l1IIl11lI.hasElapsed(this.I1lIIII1III1lIlIl1l) && containerScreenHandler.getCursorStack().isEmpty()) {
               for (int slotIndex = 0; slotIndex < containerSlotCount && slotIndex < containerScreenHandler.slots.size(); slotIndex++) {
                  ItemStack lootStack = ((Slot)containerScreenHandler.slots.get(slotIndex)).getStack();
                  if (!lootStack.isEmpty()) {
                     InventoryUtils.quickMoveSlot(slotIndex, Priority.PriorityCategory.DEFAULT);
                     this.l1IIl11lI.reset();
                     this.I1lIIII1III1lIlIl1l = this.IllI1l1l1.getValue()
                        ? (long)(this.l1I1IlllIlI.getValue() * (0.75F + ThreadLocalRandom.current().nextFloat() * 0.5F))
                        : this.l1I1IlllIlI.getValue().longValue();
                     return;
                  }
               }

               if (this.l1lll11l1l.getValue()) {
                  CLIENT.player.closeHandledScreen();
               }

               return;
            }

            return;
         }
      }

      this.l11lIlIIll1III1 = -1;
   }
}

