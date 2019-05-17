package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Material;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class CraftingModule {

    private SkcraftBasics plugin;

    public CraftingModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    public void prepareItemCraft(PrepareItemCraftEvent event) {
        if(event.getInventory().getRecipe() == null) {
            return;
        }

        if(event.getInventory().getResult().getType() == Material.RAIL) {
            event.getInventory().setResult(new ItemStack(Material.RAIL, 64));
        }
    }
}
