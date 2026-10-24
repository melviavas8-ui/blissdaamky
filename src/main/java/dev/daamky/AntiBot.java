package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Uuids;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AntiBot extends ModuleStructure {
   private final MultiToggleSetting l1I1IlllIlI = new MultiToggleSetting("Проверки")
      .addOption("Нет в табе", true)
      .addOption("Нулевой пинг", false)
      .addOption("Неверный ник", true)
      .addOption("ReallyWorld UUID", false)
      .addOption("Без брони и невидимый", false)
      .addOption("Появился рядом", false);
   private final Map<UUID, Integer> IllI1l1l1 = new ConcurrentHashMap<>();

   public AntiBot() {
      super("AntiBot", "Не даёт модулям атаковать ботов античита", ModuleCategory.COMBAT);
      this.addSetting(this.l1I1IlllIlI);
   }

   public static boolean II1Il11l111II11IIl(Entity entity) {
      AntiBot moduleStructure = (AntiBot)ModuleRegistry.getInstance().getModuleByClass(AntiBot.class);
      return moduleStructure != null && moduleStructure.isEnabled() && moduleStructure.l1I1IlllIlI(entity);
   }

   private boolean l1I1IlllIlI(Entity entity) {
      if (entity instanceof PlayerEntity player && entity != CLIENT.player && CLIENT.getNetworkHandler() != null) {
         PlayerListEntry playerListEntry = CLIENT.getNetworkHandler().getPlayerListEntry(player.getUuid());
         if (this.l1I1IlllIlI.isOptionEnabled("Нет в табе") && playerListEntry == null) {
            return true;
         }

         if (this.l1I1IlllIlI.isOptionEnabled("Нулевой пинг") && playerListEntry != null && playerListEntry.getLatency() <= 0) {
            return true;
         }

         if (this.l1I1IlllIlI.isOptionEnabled("Неверный ник")) {
            String username = player.getGameProfile().name();
            if (username == null || !username.matches("[A-Za-z0-9_]{3,16}")) {
               return true;
            }
         }

         if (this.l1I1IlllIlI.isOptionEnabled("ReallyWorld UUID")) {
            String username = player.getGameProfile().name();
            if (username != null && !Uuids.getOfflinePlayerUuid(username).equals(player.getUuid())) {
               return true;
            }
         }

         if (this.l1I1IlllIlI.isOptionEnabled("Без брони и невидимый") && player.isInvisible() && II1Il11l111II11IIl(player)) {
            return true;
         }

         if (!this.l1I1IlllIlI.isOptionEnabled("Появился рядом")) {
            return false;
         }

         Integer joinedWorldTicks = this.IllI1l1l1.get(player.getUuid());
         return joinedWorldTicks != null && joinedWorldTicks < 20 && player.distanceTo(CLIENT.player) < 6.0F;
      } else {
         return false;
      }
   }

   private static boolean II1Il11l111II11IIl(PlayerEntity playerEntity) {
      return playerEntity.getEquippedStack(EquipmentSlot.HEAD).isEmpty()
         && playerEntity.getEquippedStack(EquipmentSlot.CHEST).isEmpty()
         && playerEntity.getEquippedStack(EquipmentSlot.LEGS).isEmpty()
         && playerEntity.getEquippedStack(EquipmentSlot.FEET).isEmpty();
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         for (PlayerEntity player : CLIENT.world.getPlayers()) {
            this.IllI1l1l1.merge(player.getUuid(), 1, Integer::sum);
         }

         this.IllI1l1l1.keySet().removeIf(uUID -> CLIENT.world.getPlayerByUuid(uUID) == null);
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventWorldChange eventWorldChange) {
      this.IllI1l1l1.clear();
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.IllI1l1l1.clear();
   }
}

