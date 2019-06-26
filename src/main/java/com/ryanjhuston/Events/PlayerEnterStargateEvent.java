package com.ryanjhuston.Events;

import com.ryanjhuston.Types.Stargate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerEnterStargateEvent extends Event implements Cancellable {

    private Player player;

    private boolean isCancelled;
    private static final HandlerList handlers = new HandlerList();

    public PlayerEnterStargateEvent(Player player) {
        this.player = player;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public static void onPlayerMoveEvent(PlayerMoveEvent event) {
        if(event.getPlayer().getLocation().getBlock().getType() != Material.NETHER_PORTAL) {
            return;
        }

        if(!event.getPlayer().getLocation().getBlock().hasMetadata("Stargate")) {
            return;
        }

        PlayerEnterStargateEvent playerEnterStargateEvent = new PlayerEnterStargateEvent(event.getPlayer());
        Bukkit.getPluginManager().callEvent(playerEnterStargateEvent);
    }
}
