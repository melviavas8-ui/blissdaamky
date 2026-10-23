package dev.daamky;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

final class PotionEffectCache {
    private static final String p23629b7a = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private ItemStack II1Il11l111II11IIl = ItemStack.EMPTY;
   private long l1I1IlllIlI;
   private int IllI1l1l1 = Integer.MAX_VALUE;
   private boolean l1lll11l1l;
   private final Map<RegistryEntry<StatusEffect>, PotionEffectDurationState> lIIl11l111lIIl1ll = new HashMap<>();
// https://t.me/pasterplace

   private PotionEffectCache() {
   }
}

