package com.ryanjhuston.Types.Serializers;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class SerializedItemStack {

    public Map<String, Object> serializedItem;
    public Map<String, Object> serializedMetaData;

    public SerializedItemStack() {
        super();
    }

    public SerializedItemStack(ItemStack item) {
        if(item == null) {
            item = new ItemStack(Material.AIR);
        } else {
            serializedMetaData = (item.hasItemMeta() ? item.getItemMeta().serialize() : null);
            item.setItemMeta(null);
        }

        serializedItem = item.serialize();
    }

    public ItemStack deserialize() {
        ItemStack item = ItemStack.deserialize(serializedItem);

        if(serializedMetaData != null) {
            item.setItemMeta((ItemMeta) ConfigurationSerialization.deserializeObject(serializedMetaData, ConfigurationSerialization.getClassByAlias("ItemMeta")));
        }

        return item;
    }
}
