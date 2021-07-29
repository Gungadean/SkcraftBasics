package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Calendar;
import java.util.Date;

public class MiningWorldModule implements Listener {

    private SkcraftBasics plugin;

    private boolean moduleEnabled;

    private int resetTimer;

    public MiningWorldModule(SkcraftBasics plugin) {
        updateConfig(plugin);

        initializeWorldResetTimer();
    }

    public void initializeWorldResetTimer() {
        resetTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if(!moduleEnabled) {
                return;
            }

            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);

            if(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
                return;
            }

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);

            if(hour == 6) {
                if(minute == 0 && second == 0) {
                    resetWorld();
                }
            } else if(hour == 5) {
                if(minute == 0 && second == 0) {
                    Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 1 hour.");
                } else if(minute == 45 && second == 0) {
                    Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 15 minutes.");
                } else if(minute == 50 && second == 0) {
                    Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 10 minutes.");
                } else if(minute == 55 && second == 0) {
                    Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 5 minutes.");
                } else if(minute == 59 && second == 0) {
                    Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 1 minute.");
                } else if(minute == 59 && second == 30) {
                    Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 30 seconds.");
                } else if(minute == 59 && second == 50) {
                    Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 10 seconds.");
                } else if(minute == 59 && second == 55) {
                    Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 5 seconds.");
                }
            }
        }, 0, 20);
    }

    public void resetWorld() {
        plugin.worldManager.resetWorld("Mining");
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if(event.getPlayer().getWorld().getName().equals("Mining")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot use nether portals in this world.");
            return;
        }

        if(event.getPlayer().getInventory().getItemInMainHand().getType() != Material.ENDER_PEARL) {
            return;
        }

        if(event.getPlayer().getWorld().getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        World world = Bukkit.getWorld("Mining");

        if(world == null && !plugin.worldManager.resetting.contains("Mining")) {
            event.getPlayer().sendMessage(ChatColor.GOLD + "Mining world is currently not loaded. Please standby.");
            plugin.worldManager.createWorld("Mining", WorldType.NORMAL, World.Environment.NORMAL, null, null);
            event.getPlayer().sendMessage(ChatColor.GOLD + "Mining world successfully created.");
        }

        event.setCancelled(true);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Location location = Bukkit.getWorld("Mining").getHighestBlockAt((int)event.getPlayer().getLocation().getX(), (int)event.getPlayer().getLocation().getZ()).getLocation();

            while(location.getBlock().getType() == Material.WATER) {
                location = location.add(0, 1, 0);
            }

            if(location.add(0, -1, 0).getBlock().getType() == Material.WATER) {
                location.add(0, 0, 0).getBlock().setType(Material.OAK_PLANKS);
                location = location.add(0, 1, 0);
            }

            event.getPlayer().teleport(location);
            event.getPlayer().getInventory().removeItem(new ItemStack(Material.ENDER_PEARL, 1));
            event.getPlayer().sendMessage(ChatColor.RED + "This is the mining world, this world will be reset once a week so it is not advised to build here. Any items lost because of a reset will not be refunded.");
        }, 1);
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.enabledModules.contains("MiningWorld");

        if(moduleEnabled) {
            plugin.logger.info("- MiningWorldModule Enabled");
        }
    }
}
