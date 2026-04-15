package de.ricosw.orbismod.inventory;

import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerInventoryData {

    private List<ItemData> items = new ArrayList<>();
    private int activeHotbarSlot = 0;

    public static PlayerInventoryData fromInventory(CombinedItemContainer combinedEverything) {
        PlayerInventoryData data = new PlayerInventoryData();
        data.activeHotbarSlot = 0;
        if (combinedEverything != null) {
            int capacity = combinedEverything.getCapacity();
            for (int i = 0; i < capacity; i++) {
                ItemStack item = combinedEverything.getItemStack((short) i);
                if (item != null && !item.isEmpty()) {
                    data.items.add(new ItemData(i, item));
                }
            }
        }

        return data;
    }

    public void toInventory(CombinedItemContainer combinedEverything) {
        if (combinedEverything != null) {
            int capacity = combinedEverything.getCapacity();
            Map<Integer, ItemStack> items = new HashMap<>();

            for (ItemData itemData : this.items) {
                ItemStack restored = itemData.toItemStack();
                if (restored != null && !restored.isEmpty()) {
                    items.put(itemData.getSlot(), restored);
                }
            }

            for (int i = 0; i < capacity; i++) {
                ItemStack item = items.getOrDefault(i, ItemStack.EMPTY);
                combinedEverything.setItemStackForSlot((short)i, item);
            }

            //inventory.setActiveHotbarSlot((byte) this.activeHotbarSlot);
        }
    }

}
