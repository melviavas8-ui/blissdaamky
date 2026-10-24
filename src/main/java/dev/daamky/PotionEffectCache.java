package dev.daamky;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

final class PotionEffectCache {
   private ItemStack II1Il11l111II11IIl = ItemStack.EMPTY;
   private long l1I1IlllIlI;
   private int IllI1l1l1 = Integer.MAX_VALUE;
   private boolean l1lll11l1l;
   private final Map<RegistryEntry<StatusEffect>, PotionEffectDurationState> lIIl11l111lIIl1ll = new HashMap<>();

   private PotionEffectCache() {
   }
}

