package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Tasks.JetBoot.BeaconCheckTask;
import com.ryanjhuston.Tasks.JetBoot.JetBootDurabilityTask;
import com.ryanjhuston.Types.JetbootBeacon;
import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
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

import static org.bukkit.Material.*;

public class JetBootModule implements Listener {

    private SkcraftBasics plugin;

    public List<JetbootBeacon> activeBeacons = new ArrayList<>();
    public List<String> jetboots = new ArrayList<>();
    public HashMap<String, Integer> flyTime = new HashMap<>();
    public List<String> fallGraceCheck = new ArrayList<>();

    public double tierOneRadius;
    public double tierTwoRadius;
    public double tierThreeRadius;
    public double tierFourRadius;

    public double ironMultiplier;
    public double goldMultiplier;
    public double diamondMultiplier;
    public double emeraldMultiplier;
    public double netheriteMultiplier;

    public int jetbootDamage;
    public int durabilityFreq;
    public boolean ignoreUnbreaking;

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

        if(event.getBlockPlaced().getType() != BEACON) {
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

            activeBeacons.add(createJetbootBeacon(beacon.getLocation(), beacon.getTier()));

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
            for(int x = (event.getBlock().getChunk().getX()-1); x <= (event.getBlock().getChunk().getX()+1); x++) {
                for(int z = (event.getBlock().getChunk().getZ()-1); z <= (event.getBlock().getChunk().getZ()+1); z++) {
                    Chunk chunk = event.getBlock().getWorld().getChunkAt(x, z);
                    for(BlockState state : chunk.getTileEntities()) {

                        if(state instanceof Beacon) {
                            int tier = ((Beacon) state).getTier();
                            if(tier == 0) {
                                continue;
                            }

                            JetbootBeacon beacon = getJetbootBeacon(state.getLocation());

                            if(beacon != null) {
                                activeBeacons.remove(beacon);
                            }

                            activeBeacons.add(createJetbootBeacon(state.getLocation(), tier));

                            plugin.saveBeaconsToFile();
                        }
                    }
                }
            }
        }, 100);
    }

    @EventHandler (ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getBlock().getType() != BEACON) {
            return;
        }

        if(!event.getBlock().hasMetadata("Jetboots")) {
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

        if(event.getClickedBlock().getType() != BEACON) {
            return;
        }

        if(item.getType() == IRON_BOOTS ||
                item.getType() == CHAINMAIL_BOOTS ||
                item.getType() == GOLDEN_BOOTS ||
                item.getType() == DIAMOND_BOOTS ||
                item.getType() == NETHERITE_BOOTS) {

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

        if(event.getCurrentItem().getType() != IRON_BOOTS &&
                event.getCurrentItem().getType() != CHAINMAIL_BOOTS &&
                event.getCurrentItem().getType() != GOLDEN_BOOTS &&
                event.getCurrentItem().getType() != DIAMOND_BOOTS &&
                event.getCurrentItem().getType() != NETHERITE_BOOTS) {
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

    public int getMaterialTier(Material material) {
        switch(material) {
            case IRON_BLOCK: return 1;
            case GOLD_BLOCK: return 2;
            case DIAMOND_BLOCK: return 3;
            case EMERALD_BLOCK: return 4;
            case NETHERITE_BLOCK: return 5;
            default: return 6;
        }
    }

    public JetbootBeacon createJetbootBeacon(Location location, int tier) {
        Material lowestMaterial = location.getBlock().getRelative(0, -1, 0).getType();

        Block currentBlock;
        for(int i = 1; i <= tier; i++) {
            for (int x = -i; x <= i; x++) {
                for (int z = -i; z <= i; z++) {
                    currentBlock = location.getBlock().getRelative(x, -i, z);
                    if (getMaterialTier(lowestMaterial) > getMaterialTier(currentBlock.getType())) {
                        lowestMaterial = currentBlock.getType();
                    }
                }
            }
        }

        return new JetbootBeacon(location, tier, lowestMaterial);
    }

    public boolean checkBeaconList(String uuid) {
        List<JetbootBeacon> forRemoval = new ArrayList<>();

        for(JetbootBeacon jetbootBeacon : activeBeacons) {
            if(!(jetbootBeacon.getLocation().getBlock().getType() == BEACON)) {
                forRemoval.add(jetbootBeacon);
                continue;
            }

            if(!(jetbootBeacon.getLocation().getBlock().getState() instanceof Beacon)) {
                forRemoval.add(jetbootBeacon);
                continue;
            }

            Beacon beacon = (Beacon)jetbootBeacon.getLocation().getBlock().getState();

            if(beacon.getTier() == 0) {
                forRemoval.add(jetbootBeacon);
            } else {
                double radius = (getRadius(beacon.getTier()) * getMultiplier(jetbootBeacon.getBaseTier()));
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

        for(JetbootBeacon beacon : forRemoval) {
            activeBeacons.remove(beacon);
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

        if(!ignoreUnbreaking) {
            if(meta.getEnchants().containsKey(Enchantment.DURABILITY)) {
                double chance;
                switch(meta.getEnchants().get(Enchantment.DURABILITY).intValue()) {
                    case 1:
                        chance = 80;
                        break;
                    case 2:
                        chance = 73.3;
                        break;
                    case 3:
                        chance = 70;
                        break;
                    default:
                        chance = 100;
                }

                double rand = Math.random() * 100;

                if(rand > chance) {
                    return false;
                }
            }
        }

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

    public double getMultiplier(Material material) {
        switch(material) {
            case IRON_BLOCK:
                return ironMultiplier;
            case GOLD_BLOCK:
                return goldMultiplier;
            case DIAMOND_BLOCK:
                return diamondMultiplier;
            case EMERALD_BLOCK:
                return emeraldMultiplier;
            case NETHERITE_BLOCK:
                return netheriteMultiplier;
            default:
                return 1;
        }
    }

    public JetbootBeacon getJetbootBeacon(Location location) {
        JetbootBeacon result = null;

        for(JetbootBeacon beacon : activeBeacons) {
            if(beacon.getLocation().equals(location)) {
                result = beacon;
            }
        }
        return result;
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        tierOneRadius = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Tiers.Tier-One-Radius");
        tierTwoRadius = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Tiers.Tier-Two-Radius");
        tierThreeRadius = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Tiers.Tier-Three-Radius");
        tierFourRadius = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Tiers.Tier-Four-Radius");

        ironMultiplier = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Multipliers.Iron-Multiplier");
        goldMultiplier = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Multipliers.Gold-Multiplier");;
        diamondMultiplier = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Multipliers.Diamond-Multiplier");;
        emeraldMultiplier = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Multipliers.Emerald-Multiplier");;
        netheriteMultiplier = plugin.getConfig().getDouble("Module-Settings.JetBoot-Module.Multipliers.Netherite-Multiplier");;

        jetbootDamage = plugin.getConfig().getInt("Module-Settings.JetBoot-Module.JetBoot-Damage");
        durabilityFreq = plugin.getConfig().getInt("Module-Settings.JetBoot-Module.Durability-Update-Freq");
        ignoreUnbreaking = plugin.getConfig().getBoolean("Module-Settings.JetBoot-Module.Ignore-Unbreaking");

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