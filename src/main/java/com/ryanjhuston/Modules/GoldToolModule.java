package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GoldToolModule implements Listener {

    private SkcraftBasics plugin;

    private List<Material> foodstuff = new ArrayList<>();

    private boolean moduleEnabled;

    public GoldToolModule(SkcraftBasics plugin) {
        foodstuff.add(Material.PORKCHOP);
        foodstuff.add(Material.BEEF);
        foodstuff.add(Material.CHICKEN);
        foodstuff.add(Material.RABBIT);
        foodstuff.add(Material.MUTTON);
        foodstuff.add(Material.COD);
        foodstuff.add(Material.SALMON);

        updateConfig(plugin);
    }

    @EventHandler
    public void playerBreakBlock(BlockBreakEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        Location location = event.getBlock().getLocation();

        if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.GOLDEN_PICKAXE ||
            event.getPlayer().getInventory().getItemInMainHand().getType() == Material.GOLDEN_SHOVEL ||
            event.getPlayer().getInventory().getItemInMainHand().getType() == Material.GOLDEN_AXE ||
            event.getPlayer().getInventory().getItemInMainHand().getType() == Material.GOLDEN_SWORD ||
            event.getPlayer().getInventory().getItemInMainHand().getType() == Material.GOLDEN_HOE) {

            if(event.getBlock().getType() == Material.SAND) {
                event.setDropItems(false);
                location.getWorld().dropItem(location, new ItemStack(Material.GLASS, 1));
            } else if(event.getBlock().getType().toString().endsWith("_LOG")) {
                event.setDropItems(false);
                location.getWorld().dropItem(location, new ItemStack(Material.CHARCOAL, 1));
            } else if(event.getBlock().getType() == Material.WET_SPONGE) {
                event.setDropItems(false);
                location.getWorld().dropItem(location, new ItemStack(Material.SPONGE, 1));
            }
        }

        if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.GOLDEN_PICKAXE) {
            if(event.getBlock().getType() == Material.STONE || event.getBlock().getType() == Material.COBBLESTONE) {
                event.setDropItems(false);
                location.getWorld().dropItem(location, new ItemStack(Material.STONE, 1));
            } else if(event.getBlock().getType() == Material.IRON_ORE) {
                event.setDropItems(false);
                location.getWorld().dropItem(location, new ItemStack(Material.IRON_INGOT, 1));
            } else if(event.getBlock().getType() == Material.GOLD_ORE) {
                event.setDropItems(false);
                location.getWorld().dropItem(location, new ItemStack(Material.GOLD_INGOT, 1));
            } else if(event.getBlock().getType() == Material.NETHERRACK) {
                event.setDropItems(false);
                location.getWorld().dropItem(location, new ItemStack(Material.NETHER_BRICK, 1));
            }
        }
    }

    @EventHandler
    public void playerKillEntity(EntityDeathEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if(!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Player player = event.getEntity().getKiller();

        if(player.getInventory().getItemInMainHand().getType() == Material.GOLDEN_SWORD ||
            player.getInventory().getItemInMainHand().getType() == Material.GOLDEN_AXE ||
            player.getInventory().getItemInMainHand().getType() == Material.GOLDEN_SHOVEL ||
            player.getInventory().getItemInMainHand().getType() == Material.GOLDEN_PICKAXE) {

            List<ItemStack> newDrops = new ArrayList<>();

            for(int i = 0; i < event.getDrops().size(); i++) {
                if(foodstuff.contains(event.getDrops().get(i).getType())) {
                    newDrops.add(new ItemStack(Material.matchMaterial("COOKED_" + event.getDrops().get(i).getType().toString()), event.getDrops().get(i).getAmount()));
                } else if(event.getDrops().get(i).getType() == Material.ROTTEN_FLESH) {
                    newDrops.add(new ItemStack(Material.LEATHER, event.getDrops().get(i).getAmount()));
                } else {
                    newDrops.add(event.getDrops().get(i));
                }
            }
            event.getDrops().clear();

            for(ItemStack item : newDrops) {
                event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), item);
            }
        }
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.enabledModules.contains("GoldTools");

        if(moduleEnabled) {
            plugin.logger.info("- GoldToolsModule Enabled");
        }
    }
}
