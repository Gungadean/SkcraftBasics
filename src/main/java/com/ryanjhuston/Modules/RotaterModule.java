package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class RotaterModule implements Listener {

    private SkcraftBasics plugin;
    private List<String> players = new ArrayList<>();

    public RotaterModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.BLAZE_ROD) {
            return;
        }

        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR) {
            return;
        }

        if (!(event.getClickedBlock().getBlockData() instanceof Directional)) {
            return;
        }

        if(players.contains(event.getPlayer().getUniqueId().toString())) {
            return;
        }

        players.add(event.getPlayer().getUniqueId().toString());

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                players.remove(event.getPlayer().getUniqueId().toString());
            }
        }, 2);

        Directional directional = (Directional) event.getClickedBlock().getBlockData();

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            directional = rotateClockwise(directional);
        } else {
            directional = rotateCounterClockwise(directional);
        }

        event.getClickedBlock().setBlockData(directional);
        event.setCancelled(true);
    }

    public Directional rotateClockwise(Directional directional) {
        BlockFace blockFace = directional.getFacing();

        if (blockFace == BlockFace.NORTH) {
            directional.setFacing(BlockFace.EAST);
        } else if (blockFace == BlockFace.EAST) {
            directional.setFacing(BlockFace.SOUTH);
        } else if (blockFace == BlockFace.SOUTH) {
            directional.setFacing(BlockFace.WEST);
        } else if (blockFace == BlockFace.WEST) {
            directional.setFacing(BlockFace.NORTH);
        }

        return directional;
    }

    public Directional rotateCounterClockwise(Directional directional) {
        BlockFace blockFace = directional.getFacing();

        if (blockFace == BlockFace.NORTH) {
            directional.setFacing(BlockFace.WEST);
        } else if (blockFace == BlockFace.WEST) {
            directional.setFacing(BlockFace.SOUTH);
        } else if (blockFace == BlockFace.SOUTH) {
            directional.setFacing(BlockFace.EAST);
        } else if (blockFace == BlockFace.EAST) {
            directional.setFacing(BlockFace.NORTH);
        }

        return directional;
    }

    public Directional flip(Directional directional) {
        BlockFace blockFace = directional.getFacing();

        directional.setFacing(BlockFace.UP);

        if(blockFace == BlockFace.UP) {
            directional.setFacing(BlockFace.DOWN);
        } else if(blockFace == BlockFace.DOWN){
            directional.setFacing(BlockFace.UP);
        }

        return directional;
    }
}