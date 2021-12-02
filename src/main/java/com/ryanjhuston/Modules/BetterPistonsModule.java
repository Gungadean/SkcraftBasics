package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BetterPistonsModule implements Listener {

    private SkcraftBasics plugin;

    private static final Set<BlockFace> blockFaces = new HashSet<>(Arrays.asList(BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.UP,
            BlockFace.DOWN));

    private boolean moduleEnabled;

    public BetterPistonsModule(SkcraftBasics plugin) {
        updateConfig(plugin);
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        boolean hasSign = false;
        Sign sign;

        for(BlockFace blockFace : blockFaces) {
            if(event.getBlock().getRelative(blockFace).getType().toString().contains("_WALL_SIGN")) {
                sign = (Sign)event.getBlock().getRelative(blockFace).getState();
                Directional directional = (Directional)sign.getBlockData();

                if(sign.getBlock().getRelative(directional.getFacing().getOppositeFace()).equals(event.getBlock())) {
                    if(sign.getLine(1).equals("[Grind]")) {
                        hasSign = true;
                    }
                }
            }
        }

        if(!hasSign) {
            return;
        }

        if(event.getBlocks().isEmpty()) {
            return;
        }

        if(event.getBlocks().get(0).getType() == Material.COBBLESTONE) {
            event.getBlocks().get(0).setType(Material.AIR);
            event.getBlocks().get(0).getWorld().dropItemNaturally(event.getBlocks().get(0).getLocation(), new ItemStack(Material.GRAVEL));
            event.setCancelled(true);
        } else if(event.getBlocks().get(0).getType() == Material.GRAVEL) {
            event.getBlocks().get(0).setType(Material.AIR);
            event.getBlocks().get(0).getWorld().dropItemNaturally(event.getBlocks().get(0).getLocation(), new ItemStack(Material.SAND));
            event.setCancelled(true);
        } else if(event.getBlocks().get(0).getType() == Material.RED_SANDSTONE) {
            event.getBlocks().get(0).setType(Material.AIR);
            event.getBlocks().get(0).getWorld().dropItemNaturally(event.getBlocks().get(0).getLocation(), new ItemStack(Material.RED_SAND));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if(event.getBlock().getType() != Material.OAK_WALL_SIGN) {
            return;
        }

        if(event.getBlock().getRelative(((Directional)event.getBlock().getBlockData()).getFacing().getOppositeFace()).getType() != Material.PISTON) {
            return;
        }

        if(event.getLine(1).equalsIgnoreCase("[Grind]")) {
            event.setLine(1, "[Grind]");
            event.getPlayer().sendMessage(ChatColor.GOLD + "Piston Grind Mechanic Created!");
        }
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;
        moduleEnabled = plugin.enabledModules.contains("BetterPistons");

        if(moduleEnabled) {
            plugin.logger.info("- BetterPistonsModule Enabled");
        }
    }
}