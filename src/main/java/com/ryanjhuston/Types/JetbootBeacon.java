package com.ryanjhuston.Types;

import org.bukkit.Location;
import org.bukkit.Material;

public class JetbootBeacon {

    private Location location;
    private int beaconTier;
    private Material baseTier;

    public JetbootBeacon(Location location, int beaconTier, Material baseTier) {
        this.location = location;
        this.beaconTier = beaconTier;
        this.baseTier = baseTier;
    }

    public Location getLocation() {
        return location;
    }

    public int getBeaconTier() {
        return beaconTier;
    }

    public void setBeaconTier() {
        this.beaconTier = beaconTier;
    }

    public Material getBaseTier() {
        return baseTier;
    }

    public void setBaseTier(Material baseTier) {
        this.baseTier = baseTier;
    }
}
