package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

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

    public JetBootModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBeaconPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

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

                activeBeacons.add(beacon.getLocation());
            }
        }, 100);
    }

    @EventHandler
    public void onBaseBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if(!event.canBuild()) {
            return;
        }

        if(event.getBlockPlaced().getType() != Material.IRON_BLOCK &&
                event.getBlockPlaced().getType() != Material.GOLD_BLOCK &&
                event.getBlockPlaced().getType() != Material.DIAMOND_BLOCK &&
                event.getBlockPlaced().getType() != Material.EMERALD_BLOCK) {
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

                                if(!activeBeacons.contains(state.getLocation())) {
                                    activeBeacons.add(state.getLocation());
                                }
                            }
                        }
                    }
                }
            }
        }, 100);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.isCancelled()) {
            return;
        }

        if(event.getBlock().getType() != Material.BEACON) {
            return;
        }

        if(!activeBeacons.contains(event.getBlock().getLocation())) {
            return;
        }

        activeBeacons.remove(event.getBlock().getLocation());
    }

    public void registerJetbootDurabilityCheck() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                List<Player> forRemoval = new ArrayList<>();

                for(String uuid : jetboots) {
                    Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                    if(player != null) {
                        if(player.getGameMode() != GameMode.CREATIVE) {
                            if (player.isFlying()) {
                                if(!flyTime.containsKey(player.getUniqueId().toString())) {
                                    flyTime.put(player.getUniqueId().toString(), 1);
                                }

                                int currentFlyTime = flyTime.get(player.getUniqueId().toString());

                                if(currentFlyTime >= 10) {
                                    boolean remove = updateDurability(player);
                                    if(remove) {
                                        forRemoval.add(player);
                                    }

                                    flyTime.replace(player.getUniqueId().toString(), 1);
                                } else {
                                    flyTime.replace(player.getUniqueId().toString(), (currentFlyTime + 1));
                                }
                            }
                        }
                    }
                }

                for(Player player : forRemoval) {
                    deactivateJetboots(player);
                }
            }
        }, 0, 20);
    }

    public void registerBeaconCheck() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                List<String> playerRemoval = new ArrayList<>();

                for(Player player : Bukkit.getOnlinePlayers()) {
                    String uuid = player.getUniqueId().toString();

                    if(!checkBeaconList(uuid)) {
                        if(jetboots.contains(uuid)) {
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

                        if (!player.getInventory().getBoots().getItemMeta().hasLore()) {
                            deactivateJetboots(player);
                            continue;
                        }

                        if (!player.getInventory().getBoots().getItemMeta().getLore().contains("Jetboots")) {
                            deactivateJetboots(player);
                            continue;
                        }

                        if(((Damageable)player.getInventory().getBoots().getItemMeta()).getDamage() >= (player.getInventory().getBoots().getType().getMaxDurability()-1)) {
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

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
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

    @EventHandler
    public void removeJetboots(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }

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

    @EventHandler
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
        if(!jetboots.contains(player.getUniqueId().toString())) {
            jetboots.add(player.getUniqueId().toString());
        }
        player.setAllowFlight(true);
    }

    public void deactivateJetboots(Player player) {
        String uuid = player.getUniqueId().toString();

        if(jetboots.contains(uuid)) {
            jetboots.remove(uuid);
        }

        if(player.getGameMode() != GameMode.CREATIVE) {
            player.setFlying(false);
            player.setAllowFlight(false);

            fallGraceCheck.add(uuid);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    if(fallGraceCheck.contains(uuid)) {
                        fallGraceCheck.remove(uuid);
                    }
                }
            }, 200);
        }
    }

    public boolean updateDurability(Player player) {
        ItemMeta meta = player.getInventory().getBoots().getItemMeta();
        ((Damageable)meta).setDamage(((Damageable)meta).getDamage()+1);
        player.getInventory().getBoots().setItemMeta(meta);

        if(((Damageable)meta).getDamage() >= (player.getInventory().getBoots().getType().getMaxDurability()-1)) {
            return true;
        }
        return false;
    }
}