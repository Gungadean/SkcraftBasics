package com.ryanjhuston.Tasks;

import com.ryanjhuston.Modules.MobTurretModule;
import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.EnderTurret;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class MobTurretTask extends BukkitRunnable {

    private final MobTurretModule mobTurretModule;
    private SkcraftBasics plugin;

    public MobTurretTask(MobTurretModule mobTurretModule, SkcraftBasics plugin) {
        this.mobTurretModule = mobTurretModule;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for(EnderTurret enderTurret : mobTurretModule.getTurretList()) {
            if(enderTurret.getTarget() != null) {
                if(((LivingEntity)enderTurret.getTarget()).hasLineOfSight(enderTurret.getTurret())) {
                    Damageable damageable = (Damageable)enderTurret.getTarget();
                    damageable.damage(mobTurretModule.turretAttackDamage, enderTurret.getTurret());

                    enderTurret.updateBeam();

                    if(damageable.getHealth() <= 0) {
                        enderTurret.getTarget().removeMetadata("Turret", plugin);
                        enderTurret.setTarget(null);
                        damageable.remove();
                    }
                } else {
                    enderTurret.getTarget().removeMetadata("Turret", plugin);
                    enderTurret.setTarget(null);
                }
                continue;
            }

            for(Entity entity : enderTurret.getTurret().getNearbyEntities(mobTurretModule.turretRadius, mobTurretModule.turretRadius, mobTurretModule.turretRadius)) {
                if(mobTurretModule.attackableMobs.contains(entity.getType())) {
                    if(((LivingEntity)entity).hasLineOfSight(enderTurret.getTurret()) && !entity.hasMetadata("Turret")) {
                        enderTurret.setTarget(entity);
                        entity.setMetadata("Turret", new FixedMetadataValue(plugin, "True"));
                        break;
                    }
                }
            }
        }
    }
}
