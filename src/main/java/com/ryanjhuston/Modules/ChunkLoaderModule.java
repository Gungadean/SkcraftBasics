/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.ryanjhuston.Modules;

import com.ryanjhuston.Lib.ChunkUtil;
import com.ryanjhuston.Lib.WorldChunkCoord;
import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Tasks.ChunkLoaderTask;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChunkLoaderModule implements Listener {

    public Map<WorldChunkCoord, Long> forceLoaded = new LinkedHashMap<>();

    private SkcraftBasics plugin;

    private boolean moduleEnabled;

    private BukkitTask chunkLoaderTask;

    public ChunkLoaderModule (SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    public static boolean isNearSpawn(Chunk chunk) {
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

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
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

        if(moduleEnabled) {
            if(!HandlerList.getHandlerLists().contains(plugin.chunkLoaderModule)) {
                plugin.pm.registerEvents(plugin.chunkLoaderModule, plugin);
            }

            if(chunkLoaderTask != null) {
                chunkLoaderTask.cancel();
            }

            chunkLoaderTask = new ChunkLoaderTask(plugin).runTaskTimer(plugin, 20, 80);
        } else {
            HandlerList.unregisterAll(plugin.chunkLoaderModule);

            if(chunkLoaderTask != null) {
                chunkLoaderTask.cancel();
            }
        }
    }
}
