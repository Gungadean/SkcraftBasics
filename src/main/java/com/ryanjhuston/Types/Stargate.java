package com.ryanjhuston.Types;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class Stargate {

    private String name;
    private String owner;
    private String network;

    private Location teleportLocation;
    private Location signLocation;
    private Location buttonLocation;
    private List<Location> blocks;
    private List<Location> portalBlocks;

    private boolean isLocked;

    private String direction;

    private BukkitTask signTask;

    public Stargate(String name, String owner, String network, Location teleportLocation, Location signLocation, Location buttonLocation, List<Location> blocks, List<Location> portalBlocks, String direction) {
        this.name = name;
        this.owner = owner;
        this.network = network;
        this.teleportLocation = teleportLocation;
        this.signLocation = signLocation;
        this.buttonLocation = buttonLocation;
        this.blocks = blocks;
        this.portalBlocks = portalBlocks;
        this.signTask = null;
        this.direction = direction;
        this.isLocked = false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setTeleportLocation(Location teleportLocation) {
        this.teleportLocation = teleportLocation;
    }

    public void setSignLocation(Location signLocation) {
        this.signLocation = signLocation;
    }

    public void setButtonLocation(Location buttonLocation) {
        this.buttonLocation = buttonLocation;
    }

    public void setBlocks(List<Location> blocks) {
        this.blocks = blocks;
    }

    public void setPortalBlocks(List<Location> portalBlocks) {
        this.portalBlocks = portalBlocks;
    }

    public void setSignTask(BukkitTask signTask) {
        this.signTask = signTask;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setLocked(boolean isSignLocked) {
        this.isLocked = isSignLocked;
    }

    public String getName() {
        return name;
    }

    public String getNetwork() {
        return network;
    }

    public String getOwner() {
        return owner;
    }

    public Location getSignLocation() {
        return signLocation;
    }

    public Location getButtonLocation() {
        return buttonLocation;
    }

    public List<Location> getBlocks() {
        return blocks;
    }

    public List<Location> getPortalBlocks() {
        return portalBlocks;
    }

    public BukkitTask getSignTask() {
        return signTask;
    }

    public String getDirection() {
        return direction;
    }

    public Location getTeleportLocation() {
        return teleportLocation;
    }

    public boolean isLocked() {
        return isLocked;
    }
}
