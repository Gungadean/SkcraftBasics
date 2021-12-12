package com.ryanjhuston.Types.Serializers;

import com.ryanjhuston.Types.JetbootBeacon;
import org.bukkit.Location;
import org.bukkit.Material;

public class SerializedJetbootBeacon {

    public SerializedLocation location;
    public int beaconTier;
    public Material baseTier;

    public SerializedJetbootBeacon() {
        super();
    }

    public SerializedJetbootBeacon(Location location, int beaconTier, Material baseTier) {
        this.location = new SerializedLocation(location);
        this.beaconTier = beaconTier;
        this.baseTier = baseTier;
    }

    public JetbootBeacon deserialize() {
        return new JetbootBeacon(location.deserialize(), beaconTier, baseTier);
    }
}
