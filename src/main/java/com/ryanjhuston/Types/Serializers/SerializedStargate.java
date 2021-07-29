package com.ryanjhuston.Types.Serializers;

import com.ryanjhuston.Types.Stargate;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class SerializedStargate {

    public String name;
    public String owner;
    public String network;

    public SerializedLocation teleportLocation;
    public SerializedLocation signLocation;
    public SerializedLocation buttonLocation;
    public List<SerializedLocation> blocks = new ArrayList<>();
    public List<SerializedLocation> portalBlocks = new ArrayList<>();

    public boolean isLocked;

    public String direction;

    public SerializedStargate() {
        super();
    }

    public SerializedStargate(Stargate stargate) {
        this.name = stargate.getName();
        this.network = stargate.getNetwork();
        this.owner = stargate.getOwner();
        this.teleportLocation = new SerializedLocation(stargate.getTeleportLocation());
        this.signLocation = new SerializedLocation(stargate.getSignLocation());
        this.buttonLocation = new SerializedLocation(stargate.getButtonLocation());

        for(Location block : stargate.getBlocks()) {
            blocks.add(new SerializedLocation(block));
        }

        for(Location portalBlock : stargate.getPortalBlocks()) {
            portalBlocks.add(new SerializedLocation(portalBlock));
        }

        this.isLocked = stargate.isLocked();
        this.direction = stargate.getDirection();
    }

    public Stargate deserialize() {
        List<Location> blocksDes = new ArrayList<>();
        List<Location> portalBlocksDes = new ArrayList<>();

        for(SerializedLocation location : blocks) {
            blocksDes.add(location.deserialize());
        }

        for(SerializedLocation location : portalBlocks) {
            portalBlocksDes.add(location.deserialize());
        }

        return new Stargate(name, owner, network, teleportLocation.deserialize(), signLocation.deserialize(), buttonLocation.deserialize(), blocksDes, portalBlocksDes, direction);
    }
}
