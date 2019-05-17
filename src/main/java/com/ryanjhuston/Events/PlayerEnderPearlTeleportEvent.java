package com.ryanjhuston.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerEnderPearlTeleportEvent extends Event implements Cancellable {

    private Player player;
    private Player target;
    private boolean playerTeleport;

    private boolean isCancelled;
    private static final HandlerList handlers = new HandlerList();

    public PlayerEnderPearlTeleportEvent(Player player, Player target, boolean playerTeleport) {
        this.player = player;
        this.target = target;
        this.playerTeleport = playerTeleport;
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

    public Player getTarget() {
        return target;
    }

    public boolean isPlayerTeleport() {
        return playerTeleport;
    }
}