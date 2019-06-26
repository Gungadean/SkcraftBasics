package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

public class CraftingModule implements Listener {

    private SkcraftBasics plugin;

    public CraftingModule(SkcraftBasics plugin) {
        this.plugin = plugin;
        addRecipes();
    }

    @EventHandler
    public void prepareItemCraft(PrepareItemCraftEvent event) {
        if(event.getInventory().getRecipe() == null) {
            return;
        }

        if(event.getInventory().getResult().getType() == Material.RAIL) {
            event.getInventory().setResult(new ItemStack(Material.RAIL, 64));
        }
    }

    public void addRecipes() {
        FurnaceRecipe furnaceRecipe = new FurnaceRecipe(plugin.namespacedKey, new ItemStack(Material.LEATHER), Material.ROTTEN_FLESH, 0, 200);
        BlastingRecipe blastingRecipe = new BlastingRecipe(plugin.namespacedKey, new ItemStack(Material.LEATHER), Material.ROTTEN_FLESH, 0, 100);
        Bukkit.addRecipe(furnaceRecipe);
        Bukkit.addRecipe(blastingRecipe);
    }
}
