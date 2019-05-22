package com.ryanjhuston;

import com.ryanjhuston.Lib.ChatColorLib;
import com.ryanjhuston.Events.PlayerEnderPearlTeleportEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    }

    @EventHandler
    public void onCraftEvent(PrepareItemCraftEvent event) {
        plugin.craftingModule.prepareItemCraft(event);
    }

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
            item = plugin.visibleMaterials.get(random.nextInt(plugin.visibleMaterials.size()-1)).toString();
            plugin.getPlayerItemsList().set(event.getPlayer().getUniqueId().toString(), item);
        }

        plugin.teleportAuth.put(event.getPlayer().getUniqueId().toString(), new ArrayList<String>());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        plugin.teleportAuth.remove(event.getPlayer().getUniqueId().toString());
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
        plugin.enderPearlTeleportModule.inventoryInteract(event);
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
    public void onBlockBreak(BlockBreakEvent event) {
        plugin.stargateModule.blockBreak(event);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        plugin.stargateModule.playerMove(event);
    }

    @EventHandler
    public void onPistonPush(BlockPistonExtendEvent event) {
        for(int i = 0; i < event.getBlocks().size(); i++) {
            if(event.getBlocks().get(i).hasMetadata("Stargate")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPistonPull(BlockPistonRetractEvent event) {
        for(int i = 0; i < event.getBlocks().size(); i++) {
            if(event.getBlocks().get(i).hasMetadata("Stargate")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        List<Block> cleanup = new ArrayList<Block>();


        Iterator it = event.blockList().iterator();
        while(it.hasNext()) {
            Block block = (Block)it.next();
            if(block.hasMetadata("Stargate")) {
                cleanup.add(block);
            }
        }

        for(int i = 0; i < cleanup.size(); i++) {
            event.blockList().remove(cleanup.get(i));
        }
    }

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        Iterator it = Bukkit.getOnlinePlayers().iterator();
        int sleeping = 0;

        while(it.hasNext()) {
            Player player = (Player)it.next();
            if(player.isSleeping()) {
                sleeping++;
            }
        }

        if(sleeping >= (Bukkit.getOnlinePlayers().size()/2)) {
            Bukkit.getWorlds().get(0).setTime(1000);

            it = Bukkit.getOnlinePlayers().iterator();
            while(it.hasNext()) {
                Player player = (Player)it.next();
                player.setStatistic(Statistic.TIME_SINCE_REST, 0);
            }
        }
    }
}
