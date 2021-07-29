package com.ryanjhuston.Types.Serializers;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SerializedInventory {

    public List<SerializedItemStack> itemStacks = new ArrayList<>();

    public SerializedInventory() {
        super();
    }

    public SerializedInventory(Inventory inventory) {
        for(ItemStack item : inventory.getContents()) {
            itemStacks.add(new SerializedItemStack(item));
        }
    }

    public ItemStack[] deserialize() {
        List<ItemStack> items = new ArrayList<>();

        for(SerializedItemStack item : itemStacks) {
            items.add(item.deserialize());
        }

        return items.toArray(new ItemStack[0]);
    }
}
