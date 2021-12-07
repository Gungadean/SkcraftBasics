package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
        this.plugin = plugin;
    }

    @EventHandler (ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        boolean hasSign = false;
        Sign sign;

        for(BlockFace blockFace : blockFaces) {
            if(Tag.WALL_SIGNS.getValues().contains(event.getBlock().getRelative(blockFace).getType())) {
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
            grindBlock(event.getBlocks().get(0), Material.GRAVEL);
            event.setCancelled(true);
        } else if(event.getBlocks().get(0).getType() == Material.GRAVEL) {
            grindBlock(event.getBlocks().get(0), Material.SAND);
            event.setCancelled(true);
        } else if(event.getBlocks().get(0).getType() == Material.RED_SANDSTONE) {
            grindBlock(event.getBlocks().get(0), Material.RED_SAND);
            event.setCancelled(true);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onSignPlace(SignChangeEvent event) {
        if(!Tag.WALL_SIGNS.getValues().contains(event.getBlock().getType())) {
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

    public void grindBlock(Block block, Material result) {
        block.setType(Material.AIR);
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(result, 1));
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;
        moduleEnabled = plugin.enabledModules.contains("BetterPistons");

        if(moduleEnabled) {
            HandlerList.unregisterAll(plugin.betterPistonsModule);
            plugin.pm.registerEvents(plugin.betterPistonsModule, plugin);

            plugin.logger.info("- BetterPistonsModule Enabled");
        } else {
            HandlerList.unregisterAll(plugin.betterPistonsModule);
        }
    }
}