package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Tasks.MiningWorldTask;
import org.apache.logging.log4j.core.util.CronExpression;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class MiningWorldModule implements Listener {

    private SkcraftBasics plugin;

    private boolean moduleEnabled;

    private BukkitTask miningWorldTask;

    private Date resetDate;
    private Calendar resetCal;

    private CronExpression resetCronExpression;

    public MiningWorldModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    public void resetWorld() {
        plugin.worldManager.resetWorld("Mining");
        try {
            calculateResetDate();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
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
            event.getPlayer().sendMessage(ChatColor.RED + "This is the mining world, this world will be reset once a week so it is not advised to build here. Any items lost as a result of a reset will not be refunded.");
        }, 1);
    }

    public void calculateResetDate() throws ParseException {
        String resetExpression = plugin.getConfig().getString("Module-Settings.MiningWorld-Module.World-Reset-Period");
        if(CronExpression.isValidExpression(resetExpression)) {
            resetCronExpression = new CronExpression(resetExpression);
        } else {
            plugin.logger.severe("[SkcraftBasics] Entered cron expression is not valid. Defaulting to Friday resets at 0200.");
            resetCronExpression = new CronExpression("0 0 2 ? * FRI");
        }

        resetDate = resetCronExpression.getNextValidTimeAfter(new Date());
        resetCal = Calendar.getInstance();
        resetCal.setTime(resetDate);
    }

    public Date getResetDate() {
        return resetDate;
    }

    public Calendar getResetCal() {
        return resetCal;
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.enabledModules.contains("MiningWorld");

        try {
            calculateResetDate();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(moduleEnabled) {
            HandlerList.unregisterAll(plugin.miningWorldModule);
            plugin.pm.registerEvents(plugin.miningWorldModule, plugin);

            if(miningWorldTask != null) {
                miningWorldTask.cancel();
            }

            miningWorldTask = new MiningWorldTask(this).runTaskTimer(plugin, 0, 20);

            plugin.logger.info("- MiningWorldModule Enabled");
        } else {
            HandlerList.unregisterAll(plugin.miningWorldModule);

            if(miningWorldTask != null) {
                miningWorldTask.cancel();
            }
        }
    }
}
