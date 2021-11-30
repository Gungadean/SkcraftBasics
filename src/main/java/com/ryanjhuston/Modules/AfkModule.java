package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Tasks.AfkCheckerTask;
import com.ryanjhuston.Types.AfkTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AfkModule implements Listener {

    private SkcraftBasics plugin;
    private boolean moduleEnabled;

    private HashMap<String, AfkTracker> playerTracker = new HashMap<>();
    private List<String> afkPlayers = new ArrayList<>();

    private int afkTime;

    private BukkitTask afkCheckerTask;

    public AfkModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerTracker.put(event.getPlayer().getUniqueId().toString(), new AfkTracker(0, event.getPlayer().getLocation()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerTracker.remove(event.getPlayer().getUniqueId().toString());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        removeAfk(event.getPlayer());
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        removeAfk(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        removeAfk(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        removeAfk(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getPlayer().getInventory().getItemInMainHand().getType() != Material.FISHING_ROD) {
            removeAfk(event.getPlayer());
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if(event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();

            if(afkPlayers.contains(player.getUniqueId().toString())) {
                event.setCancelled(true);
            }
        }
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.getConfig().getList("Enabled-Modules").contains("Afk");

        this.afkTime = plugin.getConfig().getInt("Module-Settings.Afk-Module.Afk-Time-Seconds");

        if(moduleEnabled) {
            System.out.println(HandlerList.getHandlerLists().contains(plugin.betterPistonsModule));
            if(!HandlerList.getHandlerLists().contains(plugin.afkModule)) {
                plugin.pm.registerEvents(plugin.afkModule, plugin);
            }

            if(afkCheckerTask != null) {
                afkCheckerTask.cancel();
            }

            afkCheckerTask = new AfkCheckerTask(this).runTaskTimer(plugin, 0, 100);
            plugin.logger.info("- AfkModule Enabled");
        } else {
            HandlerList.unregisterAll(plugin.afkModule);

            if(afkCheckerTask != null) {
                afkCheckerTask.cancel();
            }

            List<String> forRemoval = new ArrayList<>();
            for(String uuid : afkPlayers) {
                forRemoval.add(uuid);
            }

            for(String uuid : forRemoval) {
                removeAfk(Bukkit.getPlayer(UUID.fromString(uuid)));
            }
            afkPlayers.clear();
        }
    }

    public void removeAfk(Player player) {
        if(!afkPlayers.contains(player.getUniqueId().toString())) {
            return;
        }

        afkPlayers.remove(player.getUniqueId().toString());
        AfkTracker tracker = playerTracker.get(player.getUniqueId().toString());

        player.setPlayerListName(ChatColor.WHITE + player.getName());

        tracker.setStartLocation(player.getLocation());
        tracker.setAfkTime(0);
    }

    public int getAfkTime() {
        return afkTime;
    }

    public void setAfkTime(int afkTime) {
        this.afkTime = afkTime;
    }

    public List<String> getAfkPlayers() {
        return afkPlayers;
    }

    public HashMap<String, AfkTracker> getPlayerTracker() {
        return playerTracker;
    }
}