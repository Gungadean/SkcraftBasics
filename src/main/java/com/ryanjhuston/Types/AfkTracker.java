package com.ryanjhuston.Types;

import org.bukkit.Location;

public class AfkTracker {

    private int afkTime;
    private Location startLocation;

    public AfkTracker(int afkTime, Location startLocation) {
        this.afkTime = afkTime;
        this.startLocation = startLocation;
    }

    public void setAfkTime(int afkTime) {
        this.afkTime = afkTime;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public int getAfkTime() {
        return afkTime;
    }

    public Location getStartLocation() {
        return startLocation;
    }
}