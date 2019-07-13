package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.EnderTurret;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class MobTurretModule implements Listener {

    private SkcraftBasics plugin;

    private List<EnderTurret> turretList = new ArrayList<>();

    public MobTurretModule(SkcraftBasics plugin) {
        this.plugin = plugin;
        scheduleTask();
    }

    public void scheduleTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for(EnderTurret enderTurret : turretList) {
                    if(enderTurret.getTarget() != null) {
                        if(((LivingEntity)enderTurret.getTarget()).hasLineOfSight(enderTurret.getTurret())) {
                            Damageable damageable = (Damageable)enderTurret.getTarget();
                            damageable.damage(10, enderTurret.getTurret());

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

                    for(Entity entity : enderTurret.getTurret().getNearbyEntities(40, 40, 40)) {
                        if(targetableMob(entity)) {
                            if(((LivingEntity)entity).hasLineOfSight(enderTurret.getTurret()) && !entity.hasMetadata("Turret")) {
                                enderTurret.setTarget(entity);
                                entity.setMetadata("Turret", new FixedMetadataValue(plugin, "True"));
                                break;
                            }
                        }
                    }
                }
            }
        }, 0, 10);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if(!(event.getEntity() instanceof EnderCrystal)) {
            return;
        }

        if(!(event.getDamager() instanceof Player)) {
            return;
        }

        EnderTurret turret = turretFromLocation(event.getEntity().getLocation());

        if(turret == null) {
            return;
        }

        event.setCancelled(true);
        event.getEntity().remove();
        event.getEntity().getLocation().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.END_CRYSTAL, 1));

        turretList.remove(turret);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if(event.getPlayer().getInventory().getItemInMainHand().getType() != Material.END_CRYSTAL) {
            return;
        }

        if(event.getClickedBlock().getType() != Material.OBSIDIAN) {
            return;
        }

        if(!checkIfClear(event.getClickedBlock().getLocation())) {
            return;
        }


        Location location = event.getClickedBlock().getRelative(0, 1, 0).getLocation();
        EnderCrystal enderCrystal = (EnderCrystal)event.getClickedBlock().getWorld().spawnEntity(new Location(location.getWorld(), (location.getX() + 0.5), location.getY(), (location.getZ() + 0.5)), EntityType.ENDER_CRYSTAL);

        enderCrystal.setShowingBottom(false);

        turretList.add(new EnderTurret(enderCrystal));

        event.getPlayer().getInventory().remove(event.getPlayer().getInventory().getItemInMainHand());
        event.setCancelled(true);

        event.getPlayer().sendMessage(ChatColor.GOLD + "Ender Turret created.");
    }

    public boolean targetableMob(Entity entity) {
        if(entity.getType() == EntityType.BLAZE ||
                entity.getType() == EntityType.CAVE_SPIDER ||
                entity.getType() == EntityType.CREEPER ||
                entity.getType() == EntityType.DROWNED ||
                entity.getType() == EntityType.EVOKER ||
                entity.getType() == EntityType.ENDERMAN ||
                entity.getType() == EntityType.GHAST ||
                entity.getType() == EntityType.GUARDIAN ||
                entity.getType() == EntityType.HUSK ||
                entity.getType() == EntityType.ILLUSIONER ||
                entity.getType() == EntityType.MAGMA_CUBE ||
                entity.getType() == EntityType.PHANTOM ||
                entity.getType() == EntityType.PILLAGER ||
                entity.getType() == EntityType.RAVAGER ||
                entity.getType() == EntityType.SILVERFISH ||
                entity.getType() == EntityType.SKELETON ||
                entity.getType() == EntityType.SLIME ||
                entity.getType() == EntityType.SPIDER ||
                entity.getType() == EntityType.VEX ||
                entity.getType() == EntityType.VINDICATOR ||
                entity.getType() == EntityType.WITCH ||
                entity.getType() == EntityType.WITHER_SKELETON||
                entity.getType() == EntityType.ZOMBIE ||
                entity.getType() == EntityType.ZOMBIE_VILLAGER) {
            return true;
        }
        return false;
    }

    public boolean checkIfClear(Location location) {
        for(int x = -1; x < 2; x++) {
            for(int y = 1; y < 3; y++) {
                for(int z = -1; z < 2; z++) {
                    if(location.getBlock().getRelative(x, y, z).getType() != Material.AIR) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public EnderTurret turretFromLocation(Location location) {
        for(EnderTurret turret : turretList) {
            if(turret.getTurretLocation().equals(location)) {
                return turret;
            }
        }
        return null;
    }

    public List<EnderTurret> getTurretList() {
        return turretList;
    }
}
