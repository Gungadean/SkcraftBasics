package com.ryanjhuston.Tasks;

import com.ryanjhuston.Lib.ChunkUtil;
import com.ryanjhuston.Lib.WorldChunkCoord;
import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;

public class ChunkLoaderTask extends BukkitRunnable {

    public SkcraftBasics plugin;

    public ChunkLoaderTask (SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<WorldChunkCoord, Long>> it = plugin.chunkLoaderModule.forceLoaded.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<WorldChunkCoord, Long> entry = it.next();

            long deltaTime = now - entry.getValue();

            if (deltaTime > 1000 * 30) {
                Chunk chunk = ChunkUtil.getPotentialChunk(entry.getKey());
                if (chunk != null && !plugin.chunkLoaderModule.isNearSpawn(chunk)) {
                    chunk.setForceLoaded(false);
                    chunk.unload(true);
                }
                it.remove();
            } else if (deltaTime > 1000 * 5) {
                Chunk chunk = ChunkUtil.getPotentialChunk(entry.getKey());
                if (chunk != null && !plugin.chunkLoaderModule.isNearSpawn(chunk) && !plugin.chunkLoaderModule.shouldKeepLoaded(chunk)) {
                    chunk.setForceLoaded(false);
                    chunk.unload(true);
                }
                it.remove();
            }
        }
    }
}
