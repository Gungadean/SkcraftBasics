package com.ryanjhuston.Types.Serializers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class SerializedLocation {

    public String uuid;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public SerializedLocation() {
        super();
    }

    public SerializedLocation(Location location) {
        this.uuid = location.getWorld().getUID().toString();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public SerializedLocation(World world, double x, double y, double z) {
        this.uuid = world.getUID().toString();
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0;
        this.pitch = 0;
    }

    public SerializedLocation(World world, double x, double y, double z, float yaw, float pitch) {
        this.uuid = world.getUID().toString();
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location deserialize() {
        return new Location(Bukkit.getWorld(UUID.fromString(uuid)), x, y, z, yaw, pitch);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
