package com.ryanjhuston;

import com.ryanjhuston.Lib.ChatColorLib;
import com.ryanjhuston.Events.PlayerEnderPearlTeleportEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class SkcraftEventHandler implements Listener {

    private SkcraftBasics plugin;

    public SkcraftEventHandler(SkcraftBasics plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        plugin.enderPearlTeleportModule.playerTeleport(event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        plugin.enderPearlTeleportModule.playerInteract(event);
        plugin.stargateModule.playerInteract(event);
        plugin.jetBootModule.playerInteract(event);
    }

    @EventHandler
    public void onCraftEvent(PrepareItemCraftEvent event) {
        plugin.craftingModule.prepareItemCraft(event);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.jetBootModule.playerJoin(event);
        plugin.chatChannelsModule.playerJoin(event);

        event.getPlayer().setDisplayName(ChatColorLib.getRandomColor() + event.getPlayer().getDisplayName() + ChatColor.WHITE);

        if(!event.getPlayer().isWhitelisted()) {
            event.getPlayer().setWhitelisted(true);
        }

        if(!event.getPlayer().hasPlayedBefore()) {
            String[] location = plugin.getConfig().getString("Spawn-Location").split(",");
            event.getPlayer().teleport(new Location(Bukkit.getWorld(location[0]), Double.valueOf(location[1]), Double.valueOf(location[2]), Double.valueOf(location[3]), Float.valueOf(location[4]), Float.valueOf(location[5])));
        }

        String item;
        if(!plugin.getPlayerItemsList().contains(event.getPlayer().getUniqueId().toString())) {
            Random random = new Random();
            do {
                item = Material.values()[random.nextInt(Material.values().length-1)].toString();
            } while(item.contains("Legacy") || item.contains("Air"));
                plugin.getPlayerItemsList().set(event.getPlayer().getUniqueId().toString(), item);
        }

        plugin.enderPearlTeleportModule.teleportAuth.put(event.getPlayer().getUniqueId().toString(), new ArrayList<String>());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        plugin.enderPearlTeleportModule.teleportAuth.remove(event.getPlayer().getUniqueId().toString());
        plugin.jetBootModule.playerDisconnect(event);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        plugin.enderPearlTeleportModule.inventoryClick(event);
        plugin.jetBootModule.removeJetboots(event);
    }

    @EventHandler
    public void onPlayerTeleportEnderPearl(PlayerEnderPearlTeleportEvent event) {
        plugin.enderPearlTeleportModule.playerTeleportEnderPearl(event);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        String[] location = plugin.getConfig().getString("Spawn-Location").split(",");
        event.setRespawnLocation(new Location(Bukkit.getWorld(location[0]), Double.valueOf(location[1]), Double.valueOf(location[2]), Double.valueOf(location[3]), Float.valueOf(location[4]), Float.valueOf(location[5])));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        plugin.jetBootModule.onBeaconPlace(event);
        plugin.jetBootModule.onBaseBlockPlace(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        plugin.stargateModule.blockBreak(event);
        plugin.jetBootModule.onBlockBreak(event);
        plugin.goldToolModule.playerBreakBlock(event);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        plugin.stargateModule.playerMove(event);
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        plugin.stargateModule.playerTeleport(event);
    }

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        if(Bukkit.getWorlds().get(0).getTime() < 12545) {
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                int sleeping = 0;
                Iterator it = Bukkit.getOnlinePlayers().iterator();

                while(it.hasNext())
                {
                    Player player = (Player)it.next();
                    if(player.isSleeping()) {
                        sleeping++;
                    }
                }

                int percent = (sleeping*100)/Bukkit.getOnlinePlayers().size();

                if(percent >= 50) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                        @Override
                        public void run() {
                            Iterator it = Bukkit.getOnlinePlayers().iterator();

                            while(it.hasNext()) {
                                Player player = (Player)it.next();
                                player.setStatistic(Statistic.TIME_SINCE_REST, 0);
                            }

                            Bukkit.getWorlds().get(0).setTime(1000);
                        }
                    }, 20);
                }
            }
        }, 1);
    }

    @EventHandler
    public void onItemDispense(BlockDispenseEvent event) {
        if(event.getItem().getType() == Material.MINECART && event.getBlock().getType() == Material.DISPENSER) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                @Override
                public void run() {
                    InventoryHolder dispenser = (InventoryHolder)event.getBlock().getState();
                    dispenser.getInventory().addItem(new ItemStack(Material.MINECART));
                }
            }, 1);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        plugin.jetBootModule.playerDeath(event);
    }

    @EventHandler
    public void onProjectHit(ProjectileHitEvent event) {
        plugin.captureBallModule.onProjectileHit(event);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        plugin.captureBallModule.onCreatureSpawn(event);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        plugin.captureBallModule.onEntityDamage(event);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        plugin.chatChannelsModule.playerChat(event);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        plugin.goldToolModule.playerKillEntity(event);
    }
}