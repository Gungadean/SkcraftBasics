package com.ryanjhuston.Modules;

import com.ryanjhuston.Events.PlayerEnderPearlTeleportEvent;
import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.SkcraftPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Iterator;

public class EnderPearlTeleportModule implements Listener {

    private SkcraftBasics plugin;

    private boolean moduleEnabled;

    public EnderPearlTeleportModule(SkcraftBasics plugin) {
        updateConfig(plugin);
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

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

            if(Bukkit.getPlayer(playerName) == null) {
                event.setCancelled(true);
                event.getWhoClicked().closeInventory();
                openTeleportMenu((Player)event.getWhoClicked());
                return;
            }

            SkcraftPlayer skcraftPlayer = plugin.skcraftPlayerList.get(Bukkit.getPlayer(playerName).getUniqueId().toString());

            if(skcraftPlayer.getTeleAuthed().contains(event.getWhoClicked().getUniqueId().toString())) {
                event.getWhoClicked().teleport(Bukkit.getPlayer(playerName));

                event.getWhoClicked().getInventory().removeItem(new ItemStack(Material.ENDER_PEARL, 1));

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
        if(!moduleEnabled) {
            return;
        }

        if(event.getAction() == Action.PHYSICAL) {
            return;
        }

        if(event.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }

        if(plugin.interactCooldown.contains(event.getPlayer().getUniqueId().toString())) {
            return;
        }

        if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.ENDER_PEARL && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            if(!event.getPlayer().isSneaking()) {
                return;
            }

            plugin.removeInteractCooldown(event.getPlayer().getUniqueId().toString());

            if(Bukkit.getOnlinePlayers().size() != 1) {
                event.getPlayer().openInventory(openTeleportMenu(event.getPlayer()));
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "There is no one to teleport to.");
            }
            event.setCancelled(true);
        }

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()) {
            if(event.getClickedBlock().getType().toString().contains("_BED")) {
                event.getPlayer().setBedSpawnLocation(event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage(ChatColor.GOLD + "Spawn point has been set!");
            }
        }
    }

    @EventHandler
    public void playerTeleportEnderPearl(PlayerEnderPearlTeleportEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if(event.isCancelled()) {
            return;
        }

        if(event.isPlayerTeleport()) {
            event.getPlayer().closeInventory();

            SkcraftPlayer skcraftPlayer = plugin.skcraftPlayerList.get(event.getPlayer().getUniqueId().toString());

            if(!skcraftPlayer.getPTeleAuthed().contains(event.getTarget().getUniqueId().toString())) {
                skcraftPlayer.getTeleAuthed().remove(event.getTarget().getUniqueId().toString());
            }
        }
    }

    @EventHandler
    public void playerTeleport(PlayerTeleportEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        if(event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        if(player.getLocation().getPitch() <= 70) {
            return;
        }

        if(Math.abs(player.getLocation().getX()-event.getTo().getX()) >= 2 || Math.abs(player.getLocation().getY()-event.getTo().getY()) >= 2 || Math.abs(player.getLocation().getZ()-event.getTo().getZ()) >= 2) {
            return;
        }

        if(player.getLocation().getBlock().getType() != Material.WATER && player.isOnGround() && player.getInventory().getItemInOffHand().getType() != Material.WATER_BUCKET) {
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
            //Fix for "Removing ticking entity" bug when teleporting between dimensions.
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.teleport(plugin.spawnLocation);
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
            SkcraftPlayer skcraftPlayer = plugin.skcraftPlayerList.get(player.getUniqueId().toString());

            if(player.hasMetadata("vanished")) {
                if(player.getMetadata("vanished").get(0).asBoolean()) {
                    continue;
                }
            }

            if(!target.getDisplayName().equals(player.getDisplayName())) {
                ItemStack item = new ItemStack(skcraftPlayer.getTeleportItem(), 1);
                ItemMeta meta = item.getItemMeta();

                meta.setDisplayName(ChatColor.stripColor(player.getDisplayName()));
                item.setItemMeta(meta);

                inv.addItem(item);
            }
        }

        return inv;
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.enabledModules.contains("EnderPearlTeleport");

        if(moduleEnabled) {
            plugin.logger.info("- EnderPearlTeleportModule Enabled");
        }
    }
}