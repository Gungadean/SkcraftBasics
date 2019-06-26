package com.ryanjhuston.Modules;

import com.ryanjhuston.Events.PlayerEnderPearlTeleportEvent;
import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class EnderPearlTeleportModule implements Listener {

    private SkcraftBasics plugin;

    public HashMap<String, ArrayList<String>> teleportAuth = new HashMap<>();

    public EnderPearlTeleportModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == null) {
            return;
        }

        if(event.getClickedInventory().getType() != InventoryType.CHEST) {
            return;
        }

        if(!event.getView().getTitle().equalsIgnoreCase("Teleport Menu:")) {
            return;
        }

        if(event.getCurrentItem() == null) {
            return;
        }

        if(event.getCurrentItem().getType() != Material.AIR) {
            String playerName = event.getCurrentItem().getItemMeta().getDisplayName();
            if(teleportAuth.get(Bukkit.getPlayer(playerName).getUniqueId().toString()).contains(event.getWhoClicked().getUniqueId().toString())) {
                event.getWhoClicked().teleport(Bukkit.getPlayer(playerName));

                PlayerEnderPearlTeleportEvent teleportEvent = new PlayerEnderPearlTeleportEvent((Player)event.getWhoClicked(), Bukkit.getPlayer(playerName), true);
                Bukkit.getPluginManager().callEvent(teleportEvent);
            } else {
                event.getWhoClicked().sendMessage(ChatColor.RED + "This player has not accepted you.");
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.ENDER_PEARL && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            if(!event.getPlayer().isSneaking()) {
                return;
            }

            if(Bukkit.getOnlinePlayers().size() != 1) {
                event.getPlayer().openInventory(openTeleportMenu(event.getPlayer()));
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "There is no one to teleport to.");
            }
            event.setCancelled(true);
        }

        if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.ENDER_EYE && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.getPlayer().openInventory(event.getPlayer().getEnderChest());
            event.setCancelled(true);
        }

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(event.getClickedBlock().getType().toString().contains("_BED")) {
                event.getPlayer().setBedSpawnLocation(event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage(ChatColor.GOLD + "Spawn point has been set!");
            }
        }
    }

    @EventHandler
    public void playerTeleportEnderPearl(PlayerEnderPearlTeleportEvent event) {
        if(event.isCancelled()) {
            return;
        }

        if(event.isPlayerTeleport()) {
            event.getPlayer().closeInventory();
            teleportAuth.get(event.getTarget().getUniqueId().toString()).remove(event.getPlayer().getUniqueId().toString());
        }
    }

    @EventHandler
    public void playerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if(event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        if(player.getLocation().getPitch() <= 70) {
            return;
        }

        if(player.getLocation().getBlock().getType() != Material.WATER && player.isOnGround()) {
            if(player.getBedSpawnLocation() == null) {
                player.sendMessage(ChatColor.RED + "You do not have a home set yet.");
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
                return;
            } else {
                //Fix for "Removing ticking entity" bug when teleporting between dimensions.
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        player.teleport(player.getBedSpawnLocation());
                    }
                }, 1L);
            }
        } else {
            String[] location = plugin.getConfig().getString("Spawn-Location").split(",");

            //Fix for "Removing ticking entity" bug when teleporting between dimensions.
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.teleport(new Location(Bukkit.getWorld(location[0]), Double.valueOf(location[1]), Double.valueOf(location[2]), Double.valueOf(location[3]), Float.valueOf(location[4]), Float.valueOf(location[5])));
                }
            }, 1L);
        }

        PlayerEnderPearlTeleportEvent teleportEvent = new PlayerEnderPearlTeleportEvent(player, null, false);
        Bukkit.getPluginManager().callEvent(teleportEvent);

        event.setCancelled(true);
    }

    public Inventory openTeleportMenu(Player target) {

        int inventorySize = 9;

        while((double)inventorySize/(double)(Bukkit.getOnlinePlayers().size()-1) < 1) {
            inventorySize += 9;
        }

        Inventory inv = Bukkit.createInventory(null, inventorySize , "Teleport Menu:");

        for(Iterator iterator = Bukkit.getOnlinePlayers().iterator(); iterator.hasNext();) {
            Player player = (Player) iterator.next();

            if(!target.getDisplayName().equals(player.getDisplayName())) {
                ItemStack item = new ItemStack(Material.getMaterial(plugin.getPlayerItemsList().getString(player.getUniqueId().toString())), 1);
                ItemMeta meta = item.getItemMeta();

                meta.setDisplayName(ChatColor.stripColor(player.getDisplayName()));
                item.setItemMeta(meta);

                inv.addItem(item);
            }
        }

        return inv;
    }
}