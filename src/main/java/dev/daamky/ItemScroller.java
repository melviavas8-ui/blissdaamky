package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public final class ItemScroller extends ModuleStructure {
    private static final String p26a8868b = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Задержка", 0.0F, 0.0F, 200.0F, 5.0F, "мс");
   private final Stopwatch IllI1l1l1 = new Stopwatch();
   private Slot l1lll11l1l;
// https://t.me/pasterplace

   public ItemScroller() {
      super("ItemScroller", "Shift + зажатая ЛКМ быстро перекладывает предметы", ModuleCategory.PLAYER);
      this.addSetting(this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

   private static ItemScroller l1I1IIl111Il1l1l() {
      ModuleStructure moduleStructure = ModuleRegistry.getInstance().getModuleByClass(ItemScroller.class);
      return moduleStructure != null && moduleStructure.isEnabled() ? (ItemScroller)moduleStructure : null;
   }
// https://t.me/pasterplace

   private static boolean IIIlll111l111() {
      return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 340)
         || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 344);
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl(HandledScreen<?> handledScreen, Slot slot) {
      ItemScroller itemScroller = l1I1IIl111Il1l1l();
      if (itemScroller != null) {
         itemScroller.l1lll11l1l = slot;
      }
   }
// https://t.me/pasterplace

   public static boolean II1Il11l111II11IIl(HandledScreen<?> handledScreen, Click click, Slot slot) {
      ItemScroller itemScroller = l1I1IIl111Il1l1l();
      if (itemScroller == null
         || CLIENT.player == null
         || CLIENT.interactionManager == null
         || click.button() != 0
         || !click.hasShift() && !IIIlll111l111()
         || !handledScreen.getScreenHandler().getCursorStack().isEmpty()) {
         return false;
      } else if (slot == null || slot == itemScroller.l1lll11l1l) {
         return true;
      } else if (slot.hasStack() && itemScroller.IllI1l1l1.hasElapsed(itemScroller.l1I1IlllIlI.getValue().longValue())) {
         CLIENT.interactionManager
            .clickSlot(handledScreen.getScreenHandler().syncId, slot.id, 0, SlotActionType.QUICK_MOVE, CLIENT.player);
         itemScroller.l1lll11l1l = slot;
         itemScroller.IllI1l1l1.reset();
         return true;
      } else {
         return true;
      }
   }
}

