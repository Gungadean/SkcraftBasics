package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.List;

public class CraftingModule implements Listener {

    private SkcraftBasics plugin;

    private List<BlastingRecipe> blastingRecipeList = new ArrayList<>();
    private List<ShapelessRecipe> shapelessRecipeList = new ArrayList<>();
    private List<FurnaceRecipe> furnaceRecipeList = new ArrayList<>();

    private boolean moduleEnabled;

    public CraftingModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler (ignoreCancelled = true)
    public void prepareItemCraft(PrepareItemCraftEvent event) {
        if(event.getInventory().getRecipe() == null) {
            return;
        }

        if(event.getInventory().getResult().getType() == Material.RAIL) {
            event.getInventory().setResult(new ItemStack(Material.RAIL, 64));
        }
    }

    public void addRecipes() {
        BlastingRecipe blastingRecipe;
        ShapelessRecipe shapelessRecipe;

        FurnaceRecipe leatherSmelting = new FurnaceRecipe(new NamespacedKey(plugin, "Leather"), new ItemStack(Material.LEATHER), Material.ROTTEN_FLESH, 0, 200);
        Bukkit.addRecipe(leatherSmelting);
        furnaceRecipeList.add(leatherSmelting);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "blackConcrete"), new ItemStack(Material.BLACK_CONCRETE), Material.BLACK_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "blueConcrete"), new ItemStack(Material.BLUE_CONCRETE), Material.BLUE_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "brownConcrete"), new ItemStack(Material.BROWN_CONCRETE), Material.BROWN_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "cyanConcrete"), new ItemStack(Material.CYAN_CONCRETE), Material.CYAN_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "grayConcrete"), new ItemStack(Material.GRAY_CONCRETE), Material.GRAY_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "greenConcrete"), new ItemStack(Material.GREEN_CONCRETE), Material.GREEN_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "lightBlueConcrete"), new ItemStack(Material.LIGHT_BLUE_CONCRETE), Material.LIGHT_BLUE_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "lightGrayConcrete"), new ItemStack(Material.LIGHT_GRAY_CONCRETE), Material.LIGHT_GRAY_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "limeConcrete"), new ItemStack(Material.LIME_CONCRETE), Material.LIME_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "magentaConcrete"), new ItemStack(Material.MAGENTA_CONCRETE), Material.MAGENTA_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "orangeConcrete"), new ItemStack(Material.ORANGE_CONCRETE), Material.ORANGE_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "pinkConcrete"), new ItemStack(Material.PINK_CONCRETE), Material.PINK_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "purpleConcrete"), new ItemStack(Material.PURPLE_CONCRETE), Material.PURPLE_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "redConcrete"), new ItemStack(Material.RED_CONCRETE), Material.RED_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "whiteConcrete"), new ItemStack(Material.WHITE_CONCRETE), Material.WHITE_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "yellowConcrete"), new ItemStack(Material.YELLOW_CONCRETE), Material.YELLOW_CONCRETE_POWDER, 0, 200);
        Bukkit.addRecipe(blastingRecipe);
        blastingRecipeList.add(blastingRecipe);

        shapelessRecipe = new ShapelessRecipe(new NamespacedKey(plugin, "string"), new ItemStack(Material.STRING, 4));
        shapelessRecipe.addIngredient(Material.WHITE_WOOL);
        Bukkit.addRecipe(shapelessRecipe);
        shapelessRecipeList.add(shapelessRecipe);
    }

    public void disableRecipes() {
        for(FurnaceRecipe furnaceRecipe : furnaceRecipeList) {
            Bukkit.removeRecipe(furnaceRecipe.getKey());
        }

        for(BlastingRecipe blastingRecipe : blastingRecipeList) {
            Bukkit.removeRecipe(blastingRecipe.getKey());
        }

        for(ShapelessRecipe shapelessRecipe : shapelessRecipeList) {
            Bukkit.removeRecipe(shapelessRecipe.getKey());
        }
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.enabledModules.contains("Crafting");

        disableRecipes();

        if(moduleEnabled) {
            HandlerList.unregisterAll(plugin.craftingModule);
            plugin.pm.registerEvents(plugin.craftingModule, plugin);

            addRecipes();
            plugin.logger.info("- CraftingModule Enabled");
        } else {
            HandlerList.unregisterAll(plugin.craftingModule);
        }
    }
}
