/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.ryanjhuston.Modules;

import com.ryanjhuston.Lib.ChunkUtil;
import com.ryanjhuston.Lib.WorldChunkCoord;
import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChunkLoaderModule implements Listener {

    private Map<WorldChunkCoord, Long> forceLoaded = new LinkedHashMap<>();

    private SkcraftBasics plugin;

    private boolean moduleEnabled;

    public ChunkLoaderModule (SkcraftBasics plugin) {
        this.plugin = plugin;

        this.moduleEnabled = plugin.enabledModules.contains("ChunkLoader");

        initializeChunkUnloader();
    }

    private static boolean isNearSpawn(Chunk chunk) {
        Location loc = chunk.getWorld().getSpawnLocation();
        double xDelta = loc.getX() - chunk.getX();
        double zDelta = loc.getZ() - chunk.getZ();
        return xDelta < 128 && xDelta > -128 && zDelta < 128 && zDelta > -128;
    }

    private void loadChunk(World world, int x, int z) {
        if (!world.isChunkLoaded(x, z)) {
            if (world.loadChunk(x, z, false)) {
                world.getChunkAt(x, z).setForceLoaded(true);
                forceLoaded.put(new WorldChunkCoord(world, x, z), System.currentTimeMillis());
            }
        }
    }

    public static boolean shouldKeepLoaded(Chunk chunk) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                Chunk c = ChunkUtil.getPotentialChunk(chunk.getWorld(), chunk.getX() + x, chunk.getZ() + z);
                if (c != null && hasKeepAliveEntities(c)) return true;
            }
        }

        return false;
    }

    public static boolean hasKeepAliveEntities(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof StorageMinecart && entity.getVelocity().lengthSquared() > 0.04) {
                return true;
            }
        }

        return false;
    }

    private void initializeChunkUnloader() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if(!moduleEnabled) {
                    return;
                }

                long now = System.currentTimeMillis();

                Iterator<Map.Entry<WorldChunkCoord, Long>> it = forceLoaded.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<WorldChunkCoord, Long> entry = it.next();

                    long deltaTime = now - entry.getValue();

                    if (deltaTime > 1000 * 30) {
                        Chunk chunk = ChunkUtil.getPotentialChunk(entry.getKey());
                        if (chunk != null && !isNearSpawn(chunk)) {
                            chunk.setForceLoaded(false);
                            chunk.unload(true);
                        }
                        it.remove();
                    } else if (deltaTime > 1000 * 5) {
                        Chunk chunk = ChunkUtil.getPotentialChunk(entry.getKey());
                        if (chunk != null && !isNearSpawn(chunk) && !shouldKeepLoaded(chunk)) {
                            chunk.setForceLoaded(false);
                            chunk.unload(true);
                        }
                        it.remove();
                    }
                }
            }
        },20, 80);
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if(!moduleEnabled) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        Vehicle vehicle = event.getVehicle();

        if (vehicle instanceof StorageMinecart) {
            if (from.getBlockX() != to.getBlockX()
                    || from.getBlockY() != to.getBlockY()
                    || from.getBlockZ() != to.getBlockZ()) {
                Block block = to.getBlock();

                Chunk chunk = block.getChunk();
                for (int x = -3; x <= 3; x++) {
                    for (int z = -3; z <= 3; z++) {
                        loadChunk(chunk.getWorld(), chunk.getX() + x, chunk.getZ() + z);
                    }
                }
            }
        }
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.enabledModules.contains("ChunkLoader");
    }
}
