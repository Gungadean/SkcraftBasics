package com.ryanjhuston;

import com.ryanjhuston.Lib.ChatColorLib;
import com.ryanjhuston.Events.PlayerEnderPearlTeleportEvent;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;
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
            do {
                item = Material.values()[random.nextInt(Material.values().length-1)].toString();
            } while(item.contains("Legacy") || item.contains("Air"));
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
}
