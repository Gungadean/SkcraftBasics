package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Tasks.JetBoot.BeaconCheckTask;
import com.ryanjhuston.Tasks.JetBoot.JetBootDurabilityTask;
import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class JetBootModule implements Listener {

    private SkcraftBasics plugin;

    public List<Location> activeBeacons = new ArrayList<>();
    public List<String> jetboots = new ArrayList<>();
    public HashMap<String, Integer> flyTime = new HashMap<>();
    public List<String> fallGraceCheck = new ArrayList<>();

    public double tierOneRadius;
    public double tierTwoRadius;
    public double tierThreeRadius;
    public double tierFourRadius;

    public int jetbootDamage;
    public int durabilityFreq;

    private BukkitTask beaconCheckTask;
    private BukkitTask jetBootDurabilityTask;

    private boolean moduleEnabled;

    public JetBootModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler (ignoreCancelled = true)
    public void onBeaconPlace(BlockPlaceEvent event) {
        if(!event.canBuild()) {
            return;
        }

        if(event.getBlockPlaced().getType() != Material.BEACON) {
            return;
        }

        Location location = event.getBlockPlaced().getLocation();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if(!(location.getBlock().getState() instanceof Beacon)) {
                return;
            }

            Beacon beacon = (Beacon)location.getBlock().getState();
            if(beacon.getTier() == 0) {
                return;
            }

            activeBeacons.add(beacon.getLocation());

            plugin.saveBeaconsToFile();
        }, 100);
    }

    @EventHandler (ignoreCancelled = true)
    public void onBaseBlockPlace(BlockPlaceEvent event) {
        if(!event.canBuild()) {
            return;
        }

        if(!Tag.BEACON_BASE_BLOCKS.getValues().contains(event.getBlockPlaced().getType())) {
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for(int x = (event.getBlock().getChunk().getX()-1); x <= 1; x++) {
                for(int z = (event.getBlock().getChunk().getZ()-1); z <=1; z++) {
                    Chunk chunk = event.getBlock().getWorld().getChunkAt(x, z);
                    for(BlockState state : chunk.getTileEntities()) {
                        if(state instanceof Beacon) {
                            if(((Beacon) state).getTier() == 0) {
                                continue;
                            }

                            if(!activeBeacons.contains(state.getLocation())) {
                                activeBeacons.add(state.getLocation());
                            }
                        }
                    }
                }
            }
        }, 100);
    }

    @EventHandler (ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getBlock().getType() != Material.BEACON) {
            return;
        }

        if(!activeBeacons.contains(event.getBlock().getLocation())) {
            return;
        }

        activeBeacons.remove(event.getBlock().getLocation());

        plugin.saveBeaconsToFile();
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

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if(event.getClickedBlock().getType() != Material.BEACON) {
            return;
        }

        if(item.getType() == Material.IRON_BOOTS ||
                item.getType() == Material.CHAINMAIL_BOOTS ||
                item.getType() == Material.GOLDEN_BOOTS ||
                item.getType() == Material.DIAMOND_BOOTS ||
                item.getType() == Material.NETHERITE_BOOTS) {

            plugin.removeInteractCooldown(event.getPlayer().getUniqueId().toString());

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

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        if(jetboots.contains(event.getPlayer().getUniqueId().toString())) {
            event.getPlayer().setAllowFlight(true);
        }
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        if(jetboots.contains(event.getEntity().getUniqueId().toString())) {
            event.getEntity().setAllowFlight(false);
            event.getEntity().setFlying(false);
        }
    }

    @EventHandler (ignoreCancelled = true)
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
                event.getCurrentItem().getType() != Material.CHAINMAIL_BOOTS &&
                event.getCurrentItem().getType() != Material.GOLDEN_BOOTS &&
                event.getCurrentItem().getType() != Material.DIAMOND_BOOTS &&
                event.getCurrentItem().getType() != Material.NETHERITE_BOOTS) {
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

    @EventHandler (ignoreCancelled = true)
    public void fallDamage(EntityDamageEvent event) {
        if(event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if(!(event.getEntity() instanceof Player)) {
            return;
        }

        String uuid = event.getEntity().getUniqueId().toString();

        if(!fallGraceCheck.contains(uuid)) {
            return;
        }

        fallGraceCheck.remove(uuid);
        event.setCancelled(true);
    }

    public boolean checkBeaconList(String uuid) {
        List<Location> forRemoval = new ArrayList<>();

        for(Location location : activeBeacons) {
            if(!(location.getBlock().getType() == Material.BEACON)) {
                forRemoval.add(location);
                continue;
            }

            if(!(location.getBlock().getState() instanceof Beacon)) {
                forRemoval.add(location);
                continue;
            }

            Beacon beacon = (Beacon)location.getBlock().getState();

            if(beacon.getTier() == 0) {
                forRemoval.add(beacon.getLocation());
            } else {
                double radius = getRadius(beacon.getTier());
                for(Entity entity : beacon.getWorld().getNearbyEntities(beacon.getLocation(), radius, radius, radius)) {
                    if(entity == null) {
                        continue;
                    }

                    if(!(entity instanceof Player)) {
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
        if(!jetboots.contains(player.getUniqueId().toString())) {
            jetboots.add(player.getUniqueId().toString());
        }
        player.setAllowFlight(true);
    }

    public void deactivateJetboots(Player player) {
        String uuid = player.getUniqueId().toString();

        jetboots.remove(uuid);

        if(player.getGameMode() != GameMode.CREATIVE) {
            player.setFlying(false);
            player.setAllowFlight(false);

            fallGraceCheck.add(uuid);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> fallGraceCheck.remove(uuid), 200);
        }
    }

    public boolean updateDurability(Player player) {
        ItemMeta meta = player.getInventory().getBoots().getItemMeta();
        ((Damageable)meta).setDamage(((Damageable)meta).getDamage() + jetbootDamage);
        player.getInventory().getBoots().setItemMeta(meta);

        return ((Damageable) meta).getDamage() >= (player.getInventory().getBoots().getType().getMaxDurability() - jetbootDamage);
    }

    public double getRadius(int tier) {
        switch(tier) {
            case 1:
                return tierOneRadius;
            case 2:
                return tierTwoRadius;
            case 3:
                return tierThreeRadius;
            case 4:
                return tierFourRadius;
            default:
                return 0;
        }
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        tierOneRadius = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Tier-One-Radius");
        tierTwoRadius = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Tier-Two-Radius");
        tierThreeRadius = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Tier-Three-Radius");
        tierFourRadius = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Tier-Four-Radius");

        jetbootDamage = plugin.getConfig().getInt("Module-Settings.JetBoot-Module.JetBoot-Damage");
        durabilityFreq = plugin.getConfig().getInt("Module-Settings.JetBoot-Module.Durability-Update-Freq");

        moduleEnabled = plugin.enabledModules.contains("JetBoot");

        if(moduleEnabled) {
            HandlerList.unregisterAll(plugin.jetBootModule);
            plugin.pm.registerEvents(plugin.jetBootModule, plugin);

            if(beaconCheckTask != null) {
                beaconCheckTask.cancel();
            }

            if(jetBootDurabilityTask != null) {
                jetBootDurabilityTask.cancel();
            }

            beaconCheckTask = new BeaconCheckTask(this).runTaskTimer(plugin, 0, 20);
            jetBootDurabilityTask = new JetBootDurabilityTask(this).runTaskTimer(plugin, 0, 20);

            plugin.logger.info("- JetBootModule Enabled");
        } else {
            HandlerList.unregisterAll(plugin.jetBootModule);

            List<String> forRemoval = new ArrayList<>();
            forRemoval.addAll(jetboots);

            for(String uuid : forRemoval) {
                deactivateJetboots(Bukkit.getPlayer(UUID.fromString(uuid)));
            }
            jetboots.clear();

            if(beaconCheckTask != null) {
                beaconCheckTask.cancel();
            }

            if(jetBootDurabilityTask != null) {
                jetBootDurabilityTask.cancel();
            }
        }
    }
}