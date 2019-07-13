package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
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

        if(event.getClickedBlock().getType().toString().endsWith("_BED") ||
                event.getClickedBlock().getType() == Material.CHEST ||
                event.getClickedBlock().getType() == Material.LEVER ||
                event.getClickedBlock().getType() == Material.PISTON_HEAD ||
                event.getClickedBlock().getType().toString().endsWith("_TRAPDOOR")||
                event.getClickedBlock().getType().toString().endsWith("_BUTTON")) {
            return;
        }

        if(event.getClickedBlock().getType() == Material.PISTON) {
            if(((Piston)event.getClickedBlock().getBlockData()).isExtended()) {
                return;
            }
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

        if(event.getPlayer().isSneaking()) {
            if(event.getClickedBlock().getType().toString().endsWith("_STAIRS")) {
                flipStairs(event.getClickedBlock());
                event.setCancelled(true);
                return;
            } else if(event.getClickedBlock().getType().toString().endsWith("_SLAB")) {
                flipSlab(event.getClickedBlock());
                event.setCancelled(true);
                return;
            }
        }

        if(!(event.getClickedBlock().getBlockData() instanceof Directional)) {
            return;
        }

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            rotateClockwise(event.getClickedBlock());
        } else {
            rotateCounterClockwise(event.getClickedBlock());
        }

        event.setCancelled(true);
    }

    public void rotateClockwise(Block block) {
        Directional directional = (Directional)block.getBlockData();
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

        block.setBlockData(directional);

        if(isWallBlock(block)) {
            if (block.getRelative(directional.getFacing().getOppositeFace()).getType() == Material.AIR) {
                rotateClockwise(block);
            }
        }
    }

    public void rotateCounterClockwise(Block block) {
        Directional directional = (Directional)block.getBlockData();
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

        block.setBlockData(directional);

        if(isWallBlock(block)) {
            if (block.getRelative(directional.getFacing().getOppositeFace()).getType() == Material.AIR) {
                rotateCounterClockwise(block);
            }
        }
    }

    public boolean isWallBlock(Block block) {
        if(block.getType() == Material.WALL_TORCH ||
                block.getType() == Material.REDSTONE_WALL_TORCH ||
                block.getType() == Material.LADDER ||
                block.getType() == Material.TRIPWIRE_HOOK ||
                block.getType().toString().endsWith("_WALL_SIGN")) {
            return true;
        }
        return false;
    }


    public void flipStairs(Block block) {
        Stairs stairs = (Stairs)block.getBlockData();

        if(stairs.getHalf() == Bisected.Half.BOTTOM) {
            stairs.setHalf(Bisected.Half.TOP);
        } else {
            stairs.setHalf(Bisected.Half.BOTTOM);
        }

        block.setBlockData(stairs);
    }

    public void flipSlab(Block block) {
        Slab slab = (Slab)block.getBlockData();

        if(slab.getType() == Slab.Type.BOTTOM) {
            slab.setType(Slab.Type.TOP);
        } else {
            slab.setType(Slab.Type.BOTTOM);
        }

        block.setBlockData(slab);
    }
}