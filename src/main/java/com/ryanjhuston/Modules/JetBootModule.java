package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JetBootModule {

    private SkcraftBasics plugin;

    public JetBootModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    public void onBeaconPlace(BlockPlaceEvent event) {
        if(!event.canBuild()) {
            return;
        }

        if(event.getBlockPlaced().getType() != Material.BEACON) {
            return;
        }

        Location location = event.getBlockPlaced().getLocation();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                Beacon beacon = (Beacon)location.getBlock().getState();
                if(beacon.getTier() == 0) {
                    return;
                }

                plugin.activeBeacons.add(beacon.getLocation());
            }
        }, 100);
    }

    public void onDiamondBlockPlace(BlockPlaceEvent event) {
        if(!event.canBuild()) {
            return;
        }

        if(event.getBlockPlaced().getType() != Material.DIAMOND_BLOCK) {
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                for(int x = (event.getBlock().getChunk().getX()-1); x <= 1; x++) {
                    for(int z = (event.getBlock().getChunk().getZ()-1); z <=1; z++) {
                        Chunk chunk = event.getBlock().getWorld().getChunkAt(x, z);
                        for(BlockState state : chunk.getTileEntities()) {
                            if(state instanceof Beacon) {
                                if(((Beacon) state).getTier() == 0) {
                                    continue;
                                }

                                if(!plugin.activeBeacons.contains(state.getLocation())) {
                                    plugin.activeBeacons.add(state.getLocation());
                                }
                            }
                        }
                    }
                }
            }
        }, 100);
    }

    public void onBlockBreak(BlockBreakEvent event) {
        if(event.isCancelled()) {
            return;
        }

        if(event.getBlock().getType() != Material.BEACON) {
            return;
        }

        if(!plugin.activeBeacons.contains(event.getBlock().getLocation())) {
            return;
        }

        plugin.activeBeacons.remove(event.getBlock().getLocation());
    }

    public void registerJetbootDurabilityCheck() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for(String uuid : plugin.jetboots) {
                    Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                    if(player != null) {
                        if(player.getGameMode() != GameMode.CREATIVE) {
                            if (player.isFlying()) {
                                updateDurability(player);
                            }
                        }
                    }
                }
            }
        }, 0, 160);
    }

    public void registerBeaconCheck() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                List<String> playerRemoval = new ArrayList<>();

                for(Player player : Bukkit.getOnlinePlayers()) {
                    String uuid = player.getUniqueId().toString();

                    if(!checkBeaconList(uuid)) {
                        if(plugin.jetboots.contains(uuid)) {
                            playerRemoval.add(uuid);
                        }
                    } else {
                        if (player.getGameMode() == GameMode.CREATIVE) {
                            continue;
                        }

                        if (player.getInventory().getBoots() == null) {
                            deactivateJetboots(player);
                            continue;
                        }

                        if (!player.getInventory().getBoots().hasItemMeta()) {
                            deactivateJetboots(player);
                            continue;
                        }

                        if (!player.getInventory().getBoots().getItemMeta().getLore().contains("Jetboots")) {
                            deactivateJetboots(player);
                            continue;
                        }

                        activateJetboots(player);
                    }
                }

                for(String uuid : playerRemoval) {
                    deactivateJetboots(Bukkit.getPlayer(UUID.fromString(uuid)));
                }
            }
        }, 0, 20);
    }

    public void playerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if(event.getClickedBlock().getType() != Material.BEACON) {
            return;
        }

        if(item.getType() == Material.IRON_BOOTS ||
                item.getType() == Material.GOLDEN_BOOTS ||
                item.getType() == Material.DIAMOND_BOOTS) {

            ItemMeta meta = item.getItemMeta();
            List<String> lore;

            if(meta.hasLore()) {
                if (meta.getLore().contains("Jetboots")) {
                    return;
                }
                lore = meta.getLore();
            } else {
                lore = new ArrayList<>();
            }

            lore.add("Jetboots");
            meta.setLore(lore);
            item.setItemMeta(meta);
            event.setCancelled(true);
        }
    }

    public void playerDisconnect(PlayerQuitEvent event) {
        if(plugin.jetboots.contains(event.getPlayer().getUniqueId().toString())) {
            if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                if(event.getPlayer().isFlying()) {
                    plugin.flyingPlayers.add(event.getPlayer().getUniqueId().toString());
                }
            }
        }
    }

    public void playerJoin(PlayerJoinEvent event) {
        if(plugin.flyingPlayers.contains(event.getPlayer().getUniqueId().toString())) {
            event.getPlayer().setAllowFlight(true);
            event.getPlayer().setFlying(true);
            plugin.flyingPlayers.remove(event.getPlayer().getUniqueId().toString());
        }

        if(plugin.jetboots.contains(event.getPlayer().getUniqueId().toString())) {
            event.getPlayer().setAllowFlight(true);
        }
    }

    public void playerDeath(PlayerDeathEvent event) {
        if(plugin.jetboots.contains(event.getEntity().getUniqueId().toString())) {
            event.getEntity().setAllowFlight(false);
            event.getEntity().setFlying(false);
        }
    }

    public void removeJetboots(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if(event.getClickedInventory() == null) {
            return;
        }

        if(event.getSlot() != 36) {
            return;
        }

        if(event.getCurrentItem().getType() != Material.IRON_BOOTS &&
            event.getCurrentItem().getType() != Material.GOLDEN_BOOTS &&
            event.getCurrentItem().getType() != Material.DIAMOND_BOOTS) {
            return;
        }

        if(!event.getCurrentItem().hasItemMeta()) {
            return;
        }

        if(!event.getCurrentItem().getItemMeta().hasLore()) {
            return;
        }

        if(!event.getCurrentItem().getItemMeta().getLore().contains("Jetboots")) {
            return;
        }

        deactivateJetboots((Player)event.getWhoClicked());
    }

    public boolean checkBeaconList(String uuid) {
        List<Location> forRemoval = new ArrayList<>();

        for(Location location : plugin.activeBeacons) {
            Beacon beacon = (Beacon)location.getBlock().getState();

            if(beacon.getTier() == 0) {
                forRemoval.add(beacon.getLocation());
            } else {
                for(LivingEntity entity : beacon.getEntitiesInRange()) {
                    if(entity == null) {
                        continue;
                    }

                    if (entity.getUniqueId().toString().equals(uuid)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void activateJetboots(Player player) {
        if(!plugin.jetboots.contains(player.getUniqueId().toString())) {
            plugin.jetboots.add(player.getUniqueId().toString());
        }
        player.setAllowFlight(true);
    }

    public void deactivateJetboots(Player player) {
        if(plugin.jetboots.contains(player.getUniqueId().toString())) {
            plugin.jetboots.remove(player.getUniqueId().toString());
        }

        if(player.getGameMode() != GameMode.CREATIVE) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    public void updateDurability(Player player) {
        ItemMeta meta = player.getInventory().getBoots().getItemMeta();
        ((Damageable)meta).setDamage(((Damageable)meta).getDamage()+1);
        player.getInventory().getBoots().setItemMeta(meta);

        if(((Damageable)meta).getDamage() >= player.getInventory().getBoots().getType().getMaxDurability()) {
            deactivateJetboots(player);
        }
    }
}