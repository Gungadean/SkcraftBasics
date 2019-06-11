package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CaptureBallModule {

    private SkcraftBasics plugin;

    private List<EntityType> entityList = new ArrayList<>();
    private List<Entity> hitEntity = new ArrayList<>();

    public CaptureBallModule(SkcraftBasics plugin) {
        entityList.add(EntityType.ARMOR_STAND);
        entityList.add(EntityType.ENDER_DRAGON);
        entityList.add(EntityType.PLAYER);
        entityList.add(EntityType.SHULKER);
        entityList.add(EntityType.WANDERING_TRADER);
        entityList.add(EntityType.TRADER_LLAMA);
        entityList.add(EntityType.VILLAGER);
        entityList.add(EntityType.WITHER);

        this.plugin = plugin;
    }

    public void onProjectileHit(ProjectileHitEvent event) {
        if(event.getHitEntity() == null) {
            return;
        }

        if (event.getEntity().getType() != EntityType.EGG) {
            return;
        }

        if (entityList.contains(event.getHitEntity().getType())) {
            return;
        }

        if (!(event.getHitEntity() instanceof LivingEntity)) {
            return;
        }

        hitEntity.add(event.getHitEntity());
    }

    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if(event.getDamager().getType() == EntityType.EGG) {
            event.setCancelled(true);
        }
    }

    public void onCreatureSpawn(CreatureSpawnEvent event) {
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
        if(Material.matchMaterial(entity.getType().toString() + "_SPAWN_EGG") == null) {
            return null;
        }

        ItemStack spawnEgg = new ItemStack(Material.matchMaterial(entity.getType().toString() + "_SPAWN_EGG"));
        return spawnEgg;
    }
}
