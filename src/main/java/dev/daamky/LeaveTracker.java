package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventTick;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class LeaveTracker extends ModuleStructure {
    private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Вывод", "Уведомление", "Уведомление", "Чат");
    private final FloatSetting IllI1l1l1 = new FloatSetting("Мин. дистанция", 100.0F, 1.0F, 500.0F, 1.0F);
    private final Map<String, Vec3d> l1lll11l1l = new HashMap<>();

    public LeaveTracker() {
        super("LeaveTracker", "Сообщает, где игрок пропал из прорисовки", ModuleCategory.RENDER);
        this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
    }

    @Allow2Generation(level = GenerationLevel.FAST)
    @Override
    protected void onEnable() {
        this.l1lll11l1l.clear();
    }

    @Allow2Generation(level = GenerationLevel.FAST)
    @Override
    protected void onDisable() {
        this.l1lll11l1l.clear();
    }

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void II1Il11l111II11IIl(EventWorldChange eventWorldChange) {
        this.l1lll11l1l.clear();
    }

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void II1Il11l111II11IIl(EventTick eventTick) {
        if (CLIENT.player != null && CLIENT.world != null) {
            HashSet<String> hashSet = new HashSet<>();

            for (PlayerEntity player : CLIENT.world.getPlayers()) {
                if (player != CLIENT.player) {
                    String playerName = player.getName().getString();
                    hashSet.add(playerName);
                    this.l1lll11l1l.put(playerName, player.getEntityPos());
                }
            }

            Iterator<Entry<String, Vec3d>> leaveEntryIterator = this.l1lll11l1l.entrySet().iterator();

            while (leaveEntryIterator.hasNext()) {
                Entry<String, Vec3d> leaveEntry = leaveEntryIterator.next();
                if (!hashSet.contains(leaveEntry.getKey())) {
                    Vec3d value = leaveEntry.getValue();
                    if (CLIENT.player.getEntityPos().distanceTo(value) >= this.IllI1l1l1.getValue().floatValue()) {
                        this.II1Il11l111II11IIl(leaveEntry.getKey(), value);
                    }

                    leaveEntryIterator.remove();
                }
            }
        }
    }

    private void II1Il11l111II11IIl(String playerName, Vec3d lastKnownPosition) {
        String positionText = String.format("%d, %d, %d", (int)lastKnownPosition.x, (int)lastKnownPosition.y, (int)lastKnownPosition.z);

        this.l1I1IlllIlI.getValue();
        NotificationScheduler.II1Il11l111II11IIl(playerName + " пропал", positionText, 5000L);
    }
}

