package com.ryanjhuston.Types;

import org.bukkit.Location;

import java.util.List;

public class Stargate {

    private String network;
    private String owner;

    private Location signLocation;
    private Location buttonLocation;
    private List<Location> blocks;

    public Stargate(String owner, String network, Location signLocation, Location buttonLocation, List<Location> blocks) {
        this.owner = owner;
        this.network = network;
        this.signLocation = signLocation;
        this.buttonLocation = buttonLocation;
        this.blocks = blocks;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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
}
