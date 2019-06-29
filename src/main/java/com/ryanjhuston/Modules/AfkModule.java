package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.AfkTracker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AfkModule implements Listener {

    private SkcraftBasics plugin;

    private HashMap<String, AfkTracker> playerTracker = new HashMap();
    private List<String> afkPlayers = new ArrayList<>();


    public AfkModule(SkcraftBasics plugin) {
        this.plugin = plugin;
        scheduleTask();
    }

    private void scheduleTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    AfkTracker afkTracker = playerTracker.get(player.getUniqueId().toString());

                    if(afkPlayers.contains(player.getUniqueId().toString())) {
                        if(afkTracker.getStartLocation().equals(player.getLocation())) {
                            continue;
                        } else {
                            removeAfk(player);
                            continue;
                        }
                    }

                    if(player.getLocation().equals(afkTracker.getStartLocation())) {
                        afkTracker.setAfkTime(afkTracker.getAfkTime() + 5);
                    } else {
                        afkTracker.setStartLocation(player.getLocation());
                        afkTracker.setAfkTime(0);
                    }

                    if(afkTracker.getAfkTime() >= 300) {
                        afkPlayers.add(player.getUniqueId().toString());
                    }
                }
            }
        }, 0, 100);
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
        if(event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();

            if(afkPlayers.contains(player.getUniqueId().toString())) {
                event.setCancelled(true);
            }
        }
    }

    public void removeAfk(Player player) {
        if(!afkPlayers.contains(player.getUniqueId().toString())) {
            return;
        }

        afkPlayers.remove(player.getUniqueId().toString());
        AfkTracker tracker = playerTracker.get(player.getUniqueId().toString());

        tracker.setStartLocation(player.getLocation());
        tracker.setAfkTime(0);
    }

    public List<String> getAfkPlayers() {
        return afkPlayers;
    }

}