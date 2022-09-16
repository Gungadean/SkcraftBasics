package com.ryanjhuston.Modules;

import com.ryanjhuston.Events.PlayerEnderPearlTeleportEvent;
import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.SkcraftPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
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

import java.util.ArrayList;
import java.util.List;

public class EnderPearlTeleportModule implements Listener {

    private SkcraftBasics plugin;

    private boolean moduleEnabled;

    private List<String> inView = new ArrayList<>();

    public EnderPearlTeleportModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler (ignoreCancelled = true)
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

        if(!inView.contains(event.getWhoClicked().getUniqueId().toString())) {
            return;
        }

        if(event.getCurrentItem() == null) {
            return;
        }

        if(event.getCurrentItem().getType() != Material.AIR) {
            Player player = (Player)event.getWhoClicked();
            if(event.getCurrentItem().getType() == Material.RED_BED && event.getCurrentItem().getItemMeta().getDisplayName().equals("Home")) {
                if(player.getBedSpawnLocation() == null) {
                    player.sendMessage(ChatColor.RED + "You do not have a home set yet.");
                    player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
                    event.setCancelled(true);
                    return;
                } else {
                    //Fix for "Removing ticking entity" bug when teleporting between dimensions.
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.teleport(player.getBedSpawnLocation()), 1L);
                }
            } else if(event.getCurrentItem().getType() == Material.CAMPFIRE && event.getCurrentItem().getItemMeta().getDisplayName().equals("Spawn")) {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.teleport(plugin.spawnLocation), 1L);
            } else {
                String playerName = event.getCurrentItem().getItemMeta().getDisplayName();

                if (Bukkit.getPlayer(playerName) == null) {
                    event.setCancelled(true);
                    player.closeInventory();
                    openTeleportMenu(player);
                    return;
                }

                SkcraftPlayer skcraftPlayer = plugin.getSkcraftPlayer(Bukkit.getPlayer(playerName));

                if (skcraftPlayer.getTeleAuthed().contains(player.getUniqueId().toString())) {
                    player.teleport(Bukkit.getPlayer(playerName));

                    player.getInventory().removeItem(new ItemStack(Material.ENDER_PEARL, 1));

                    PlayerEnderPearlTeleportEvent teleportEvent = new PlayerEnderPearlTeleportEvent(player, Bukkit.getPlayer(playerName), true);
                    Bukkit.getPluginManager().callEvent(teleportEvent);
                    inView.remove(player.getUniqueId().toString());
                } else {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "This player has not accepted you.");
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
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

            event.getPlayer().openInventory(openTeleportMenu(event.getPlayer()));
            inView.add(event.getPlayer().getUniqueId().toString());
            event.setCancelled(true);
        }

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()) {
            if(Tag.BEDS.getValues().contains(event.getClickedBlock().getType())) {
                event.getPlayer().setBedSpawnLocation(event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage(ChatColor.GOLD + "Spawn point has been set!");
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void playerTeleportEnderPearl(PlayerEnderPearlTeleportEvent event) {
        if(event.isPlayerTeleport()) {
            event.getPlayer().closeInventory();

            SkcraftPlayer skcraftPlayer = plugin.getSkcraftPlayer(event.getPlayer());

            if(!skcraftPlayer.getPTeleAuthed().contains(event.getTarget().getUniqueId().toString())) {
                skcraftPlayer.getTeleAuthed().remove(event.getTarget().getUniqueId().toString());
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void playerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if(event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        if(player.getLocation().getPitch() <= 70) {
            return;
        }

        if(Math.abs(player.getLocation().getX()-event.getTo().getX()) >= 1 || Math.abs(player.getLocation().getY()-event.getTo().getY()) >= 1 || Math.abs(player.getLocation().getZ()-event.getTo().getZ()) >= 1) {
            return;
        }

        //noinspection deprecation
        if(player.getLocation().getBlock().getType() != Material.WATER && player.isOnGround() && player.getInventory().getItemInOffHand().getType() != Material.WATER_BUCKET) {
            if(player.getBedSpawnLocation() == null) {
                player.sendMessage(ChatColor.RED + "You do not have a home set yet.");
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
                return;
            } else {
                //Fix for "Removing ticking entity" bug when teleporting between dimensions.
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.teleport(player.getBedSpawnLocation()), 1L);
            }
        } else {
            //Fix for "Removing ticking entity" bug when teleporting between dimensions.
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.teleport(plugin.spawnLocation), 1L);
        }

        PlayerEnderPearlTeleportEvent teleportEvent = new PlayerEnderPearlTeleportEvent(player, null, false);
        Bukkit.getPluginManager().callEvent(teleportEvent);

        event.setCancelled(true);
    }

    public Inventory openTeleportMenu(Player target) {
        int inventorySize = 9;

        if(Bukkit.getOnlinePlayers().size() > 1) {
            while ((double) inventorySize / (double) (Bukkit.getOnlinePlayers().size() - 1) < 1) {
                inventorySize += 9;
            }
            inventorySize += 9;
        }

        Inventory inv = Bukkit.createInventory(null, inventorySize , "Teleport Menu:");

        for(Player player : Bukkit.getOnlinePlayers()) {
            SkcraftPlayer skcraftPlayer = plugin.getSkcraftPlayer(player);

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

        ItemStack defaultItem = new ItemStack(Material.RED_BED, 1);
        ItemMeta defaultMeta = defaultItem.getItemMeta();

        defaultMeta.setDisplayName("Home");
        defaultItem.setItemMeta(defaultMeta);

        if(Bukkit.getOnlinePlayers().size() > 1) {
            inv.setItem(inv.getSize() - 2, defaultItem);
        } else {
            inv.setItem(3, defaultItem);
        }

        defaultItem = new ItemStack(Material.CAMPFIRE, 1);
        defaultMeta = defaultItem.getItemMeta();

        defaultMeta.setDisplayName("Spawn");
        defaultItem.setItemMeta(defaultMeta);

        if(Bukkit.getOnlinePlayers().size() > 1) {
            inv.setItem(inv.getSize() - 1, defaultItem);
        } else {
            inv.setItem(5, defaultItem);
        }

        return inv;
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.enabledModules.contains("EnderPearlTeleport");

        if(moduleEnabled) {
            HandlerList.unregisterAll(plugin.enderPearlTeleportModule);
            plugin.pm.registerEvents(plugin.enderPearlTeleportModule, plugin);

            plugin.logger.info("- EnderPearlTeleportModule Enabled");
        } else {
            HandlerList.unregisterAll(plugin.enderPearlTeleportModule);
        }
    }
}