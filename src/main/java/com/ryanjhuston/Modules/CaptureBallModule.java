package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CaptureBallModule implements Listener {

    private SkcraftBasics plugin;

    private List<EntityType> bannedMobs = new ArrayList<>();
    private List<Entity> hitEntity = new ArrayList<>();

    private boolean moduleEnabled;

    public CaptureBallModule(SkcraftBasics plugin) {
        updateConfig(plugin);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if(!moduleEnabled) {
            return;
        }

        Entity entity = event.getHitEntity();

        if(entity == null) {
            return;
        }

        if (event.getEntity().getType() != EntityType.EGG) {
            return;
        }

        if (bannedMobs.contains(entity.getType())) {
            return;
        }

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        hitEntity.add(entity);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if(event.getDamager().getType() == EntityType.EGG) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.EGG) {
            return;
        }

        for(Entity entity : event.getEntity().getNearbyEntities(2, 2, 2)) {
            if(hitEntity.contains(entity)) {
                ItemStack spawnEgg = makeSpawnEgg(entity);
                if(spawnEgg == null) {
                    return;
                }

                event.getEntity().getWorld().dropItem(event.getLocation(), spawnEgg);

                hitEntity.remove(entity);
                entity.remove();
                event.setCancelled(true);
                return;
            }
        }
    }

    public ItemStack makeSpawnEgg(Entity entity) {
        Material material;

        if(entity.getType() == EntityType.MUSHROOM_COW) {
            material = Material.MOOSHROOM_SPAWN_EGG;
        } else if(Material.matchMaterial(entity.getType().toString() + "_SPAWN_EGG") != null) {
            material = Material.matchMaterial(entity.getType().toString() + "_SPAWN_EGG");
        } else {
            return null;
        }

        ItemStack spawnEgg = new ItemStack(material);
        return spawnEgg;
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        List<String> bannedMobsString = plugin.getConfig().getStringList("Module-Settings.CaptureBall-Module.Banned-Mobs");

        for(String bannedMobName : bannedMobsString) {
            bannedMobs.add(EntityType.valueOf(bannedMobName));
        }

        moduleEnabled = plugin.enabledModules.contains("CaptureBall");

        if(moduleEnabled) {
            plugin.logger.info("- CaptureBallModule Enabled");
        }
    }
}
