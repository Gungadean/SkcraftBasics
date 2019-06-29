package com.ryanjhuston.Types;

import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;

public class EnderTurret {

    private EnderCrystal turret;
    private Entity target;

    public EnderTurret(EnderCrystal turret) {
        this.turret = turret;
    }

    public EnderCrystal getTurret() {
        return turret;
    }

    public Location getTurretLocation() {
        return turret.getLocation();
    }

    public Entity getTarget() {
        return target;
    }

    public void setTarget(Entity target) {
        if(target != null) {
            turret.setBeamTarget(new Location(target.getWorld(), target.getLocation().getX(), target.getLocation().getY() - 2, target.getLocation().getZ()));
        } else {
            turret.setBeamTarget(null);
        }
        this.target = target;
    }

    public void updateBeam() {
        if(target != null) {
            turret.setBeamTarget(new Location(target.getWorld(), target.getLocation().getX(), target.getLocation().getY() - 2, target.getLocation().getZ()));
        }
    }
}
