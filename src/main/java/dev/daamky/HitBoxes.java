package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class HitBoxes extends ModuleStructure {
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Размер", 0.25F, 0.05F, 1.5F, 0.05F, "бл.");
   private final BooleanSetting IllI1l1l1 = new BooleanSetting("Только игроки", true);
   private final BooleanSetting l1lll11l1l = new BooleanSetting("Игнорировать друзей", true);

   public HitBoxes() {
      super("HitBoxes", "Увеличивает хитбоксы сущностей для прицела", ModuleCategory.COMBAT);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1, this.l1lll11l1l);
   }

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.formatValue();
   }

   public static float II1Il11l111II11IIl(Entity entity) {
      HitBoxes moduleStructure = (HitBoxes)ModuleRegistry.getInstance().getModuleByClass(HitBoxes.class);
      if (moduleStructure == null || !moduleStructure.isEnabled() || entity == MinecraftClient.getInstance().player || !(entity instanceof LivingEntity)) {
         return 0.0F;
      } else if (moduleStructure.IllI1l1l1.getValue() && !(entity instanceof PlayerEntity)) {
         return 0.0F;
      } else {
         return moduleStructure.l1lll11l1l.getValue() && FriendManager.getInstance().isFriend(entity)
            ? 0.0F
            : moduleStructure.l1I1IlllIlI.getValue();
      }
   }
}

