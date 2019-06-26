package com.ryanjhuston;

import com.ryanjhuston.Lib.ChatColorLib;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
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
    public void onPlayerJoin(PlayerJoinEvent event) {
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
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if(event.getPlayer().getBedSpawnLocation() != null) {
            event.setRespawnLocation(event.getPlayer().getBedSpawnLocation());
            return;
        }

        String[] location = plugin.getConfig().getString("Spawn-Location").split(",");
        event.setRespawnLocation(new Location(Bukkit.getWorld(location[0]), Double.valueOf(location[1]), Double.valueOf(location[2]), Double.valueOf(location[3]), Float.valueOf(location[4]), Float.valueOf(location[5])));
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
}