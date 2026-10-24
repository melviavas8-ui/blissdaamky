package dev.daamky;

import java.util.Locale;
import java.util.function.Predicate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public final class InventoryUtils {
    public static final int OFFHAND_SCREEN_HANDLER_SLOT = 45;
    public static final int OFFHAND_SWAP_BUTTON = 40;
    private static int deferredHotbarSlot = -1;
    private static int slotRestoreDelayAge;
    private static int pendingUseInventorySlot = -1;
    private static int hotbarSlotBeforePendingUse;
    private static int pendingUseStage;
    private static int pendingUseStageAge;
    private static Priority.PriorityCategory pendingUseSwapPriority = Priority.PriorityCategory.DEFAULT;

    private InventoryUtils() {
    }

    private static MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    public static int findHotbarSlot(Predicate<ItemStack> itemPredicate) {
        ClientPlayerEntity clientPlayerEntity = getClient().player;
        if (clientPlayerEntity == null) {
            return -1;
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            if (itemPredicate.test(clientPlayerEntity.getInventory().getStack(hotbarSlot))) {
                return hotbarSlot;
            }
        }

        return -1;
    }

    public static int findInventorySlot(Predicate<ItemStack> itemPredicate) {
        ClientPlayerEntity clientPlayerEntity = getClient().player;
        if (clientPlayerEntity == null) {
            return -1;
        }

        for (int inventorySlot = 9; inventorySlot < 36; inventorySlot++) {
            if (itemPredicate.test(clientPlayerEntity.getInventory().getStack(inventorySlot))) {
                return inventorySlot;
            }
        }

        return -1;
    }

    public static int findItemSlot(Predicate<ItemStack> itemPredicate) {
        int matchingSlot = findHotbarSlot(itemPredicate);
        return matchingSlot != -1 ? matchingSlot : findInventorySlot(itemPredicate);
    }

    public static int countMatchingItems(Predicate<ItemStack> itemPredicate) {
        ClientPlayerEntity clientPlayerEntity = getClient().player;
        if (clientPlayerEntity == null) {
            return 0;
        }

        int itemCount = 0;

        for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
            ItemStack stack = clientPlayerEntity.getInventory().getStack(inventorySlot);
            if (itemPredicate.test(stack)) {
                itemCount += stack.getCount();
            }
        }

        if (itemPredicate.test(clientPlayerEntity.getOffHandStack())) {
            itemCount += clientPlayerEntity.getOffHandStack().getCount();
        }

        return itemCount;
    }

    public static int toScreenHandlerSlot(int inventorySlot) {
        return inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
    }

    public static int getSelectedHotbarSlot() {
        ClientPlayerEntity clientPlayerEntity = getClient().player;
        return clientPlayerEntity == null ? 0 : clientPlayerEntity.getInventory().getSelectedSlot();
    }

    public static void setSelectedHotbarSlot(int hotbarSlot) {
        ClientPlayerEntity clientPlayerEntity = getClient().player;
        if (clientPlayerEntity != null && hotbarSlot >= 0 && hotbarSlot <= 8) {
            if (clientPlayerEntity.getInventory().getSelectedSlot() != hotbarSlot) {
                clientPlayerEntity.getInventory().setSelectedSlot(hotbarSlot);
            }
        }
    }

    public static void rememberHotbarSlotForRestore(int hotbarSlot) {
        ClientPlayerEntity clientPlayerEntity = getClient().player;
        if (clientPlayerEntity != null && hotbarSlot >= 0 && hotbarSlot <= 8) {
            if (deferredHotbarSlot == -1) {
                deferredHotbarSlot = hotbarSlot;
            }

        }
    }

    public static int getHotbarSlotToRestore() {
        return deferredHotbarSlot != -1 ? deferredHotbarSlot : getSelectedHotbarSlot();
    }

    public static void updateDeferredHotbarSlotRestore() {
        updatePendingItemUse();
        ClientPlayerEntity clientPlayerEntity = getClient().player;
        if (deferredHotbarSlot != -1) {
            if (clientPlayerEntity == null) {
                deferredHotbarSlot = -1;
            } else {
                if (clientPlayerEntity.age != slotRestoreDelayAge) {
                    int hotbarSlotToRestore = deferredHotbarSlot;
                    deferredHotbarSlot = -1;
                    if (!clientPlayerEntity.isUsingItem()) {
                        setSelectedHotbarSlot(hotbarSlotToRestore);
                    } else {
                        deferredHotbarSlot = hotbarSlotToRestore;
                    }
                }
            }
        }
    }

    public static void sendSelectedSlotPacket(int hotbarSlot) {
        if (hotbarSlot >= 0 && hotbarSlot <= 8) {
            PacketSender.II1Il11l111II11IIl(new UpdateSelectedSlotC2SPacket(hotbarSlot));
        }
    }

    public static void clickSlot(int screenHandlerSlot, int button, SlotActionType slotActionType) {
        MinecraftClient client = getClient();
        if (client.player != null && client.interactionManager != null) {
            client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, screenHandlerSlot, button, slotActionType, client.player);
        }
    }

    public static void swapInventorySlotWithOffhand(int inventorySlot, Priority.PriorityCategory priorityCategory) {
        Priority.getInstance().scheduleAction(priorityCategory, () -> {
            stopUsingOffhand();
            clickSlot(toScreenHandlerSlot(inventorySlot), 40, SlotActionType.SWAP);
        });
    }

    public static void stopUsingOffhand() {
        MinecraftClient client = getClient();
        ClientPlayerEntity clientPlayerEntity = client.player;
        if (clientPlayerEntity != null && client.interactionManager != null && clientPlayerEntity.isUsingItem() && clientPlayerEntity.getActiveHand() == Hand.OFF_HAND) {
            client.interactionManager.stopUsingItem(clientPlayerEntity);
        }
    }

    public static void swapInventorySlotWithHotbar(int inventorySlot, int hotbarButton, Priority.PriorityCategory priorityCategory) {
        Priority.getInstance().scheduleAction(priorityCategory, () -> clickSlot(toScreenHandlerSlot(inventorySlot), hotbarButton, SlotActionType.SWAP));
    }

    public static void quickMoveSlot(int screenHandlerSlot, Priority.PriorityCategory priorityCategory) {
        Priority.getInstance().scheduleAction(priorityCategory, () -> clickSlot(screenHandlerSlot, 0, SlotActionType.QUICK_MOVE));
    }

    public static void interactItemWithRotation(Hand hand, float yaw, float pitch) {
        MinecraftClient client = getClient();
        ClientPlayerEntity clientPlayerEntity = client.player;
        if (clientPlayerEntity != null && client.interactionManager != null) {
            float originalYaw = clientPlayerEntity.getYaw();
            float originalPitch = clientPlayerEntity.getPitch();
            clientPlayerEntity.setYaw(yaw);
            clientPlayerEntity.setPitch(pitch);

            try {
                client.interactionManager.interactItem(clientPlayerEntity, hand);
            } finally {
                clientPlayerEntity.setYaw(originalYaw);
                clientPlayerEntity.setPitch(originalPitch);
            }
        }
    }

    public static void useHotbarSlot(int hotbarSlot, float yaw, float pitch) {
        if (hotbarSlot >= 0 && hotbarSlot <= 8) {
            int previousHotbarSlot = getHotbarSlotToRestore();
            setSelectedHotbarSlot(hotbarSlot);
            interactItemWithRotation(Hand.MAIN_HAND, yaw, pitch);
            if (previousHotbarSlot != hotbarSlot) {
                rememberHotbarSlotForRestore(previousHotbarSlot);
            }
        }
    }

    public static boolean useItemFromInventory(Predicate<ItemStack> itemPredicate, Priority.PriorityCategory priorityCategory) {
        ClientPlayerEntity clientPlayerEntity = getClient().player;
        if (clientPlayerEntity == null) {
            return false;
        }

        float yaw = clientPlayerEntity.getYaw();
        float pitch = clientPlayerEntity.getPitch();
        if (itemPredicate.test(clientPlayerEntity.getOffHandStack())) {
            interactItemWithRotation(Hand.OFF_HAND, yaw, pitch);
            return true;
        }

        int hotbarSlot = findHotbarSlot(itemPredicate);
        if (hotbarSlot != -1) {
            useHotbarSlot(hotbarSlot, yaw, pitch);
            return true;
        }

        int inventorySlot = findInventorySlot(itemPredicate);
        if (inventorySlot == -1 || !canSwapInventorySlots()) {
            return false;
        }

        if (pendingUseInventorySlot != -1) {
            return false;
        }

        pendingUseInventorySlot = inventorySlot;
        hotbarSlotBeforePendingUse = getSelectedHotbarSlot();

        swapInventorySlotWithHotbar(inventorySlot, hotbarSlotBeforePendingUse, priorityCategory);
        return true;
    }

    private static void updatePendingItemUse() {
        ClientPlayerEntity clientPlayerEntity = getClient().player;
        if (pendingUseInventorySlot != -1) {
            if (clientPlayerEntity == null || clientPlayerEntity.age - pendingUseStageAge > 40) {
                pendingUseInventorySlot = -1;
            } else if (!Priority.getInstance().hasQueuedActions() && clientPlayerEntity.age != pendingUseStageAge) {
                if (pendingUseStage == 0) {
                    if (getSelectedHotbarSlot() != hotbarSlotBeforePendingUse) {
                        pendingUseInventorySlot = -1;
                    } else {
                        interactItemWithRotation(Hand.MAIN_HAND, clientPlayerEntity.getYaw(), clientPlayerEntity.getPitch());
                        pendingUseStage = 1;
                        pendingUseStageAge = clientPlayerEntity.age;
                    }
                } else if (!clientPlayerEntity.isUsingItem() && canSwapInventorySlots()) {
                    int inventorySlotToRestore = pendingUseInventorySlot;
                    pendingUseInventorySlot = -1;
                    swapInventorySlotWithHotbar(inventorySlotToRestore, hotbarSlotBeforePendingUse, pendingUseSwapPriority);
                }
            }
        }
    }

    public static boolean itemNameContains(ItemStack stack, String nameFragment) {
        return !stack.isEmpty() && stack.getName().getString().toLowerCase(Locale.ROOT).contains(nameFragment.toLowerCase(Locale.ROOT));
    }

    public static boolean canSwapInventorySlots() {
        ClientPlayerEntity clientPlayerEntity = getClient().player;
        return clientPlayerEntity != null && clientPlayerEntity.currentScreenHandler == clientPlayerEntity.playerScreenHandler && !Priority.getInstance().hasQueuedActions();
    }
}

