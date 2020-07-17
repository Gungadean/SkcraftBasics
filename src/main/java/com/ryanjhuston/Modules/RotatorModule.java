package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

public class RotatorModule implements Listener {

    private SkcraftBasics plugin;

    private boolean moduleEnabled;

    private Material tool;

    public RotatorModule(SkcraftBasics plugin) {
        updateConfig(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if(event.getAction() == Action.PHYSICAL) {
            return;
        }

        if(event.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }

        if(plugin.interactCooldown.contains(event.getPlayer().getUniqueId().toString())) {
            return;
        }

        if (event.getPlayer().getInventory().getItemInMainHand().getType() != tool) {
            return;
        }

        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR) {
            return;
        }

        if(event.getClickedBlock().getType().toString().endsWith("_BED") ||
                event.getClickedBlock().getType() == Material.CHEST ||
                event.getClickedBlock().getType() == Material.LEVER ||
                event.getClickedBlock().getType() == Material.PISTON_HEAD ||
                event.getClickedBlock().getType() == Material.NETHER_PORTAL ||
                event.getClickedBlock().getType().toString().endsWith("_TRAPDOOR")||
                event.getClickedBlock().getType().toString().endsWith("_BUTTON")) {
            return;
        }

        if(event.getClickedBlock().getType() == Material.PISTON) {
            if(((Piston)event.getClickedBlock().getBlockData()).isExtended()) {
                return;
            }
        }

        plugin.removeInteractCooldown(event.getPlayer().getUniqueId().toString());

        if(event.getClickedBlock().getBlockData() instanceof Directional) {
            if(event.getPlayer().isSneaking()) {
                if(event.getClickedBlock().getType().toString().endsWith("_STAIRS")) {
                    flipStairs(event.getClickedBlock());
                } else if(event.getClickedBlock().getType().toString().endsWith("_SLAB")) {
                    flipSlab(event.getClickedBlock());
                } else if(event.getClickedBlock().getType() == Material.PISTON ||
                        event.getClickedBlock().getType() == Material.STICKY_PISTON ||
                        event.getClickedBlock().getType() == Material.DISPENSER ||
                        event.getClickedBlock().getType() == Material.DROPPER) {
                    flipSpecialShift(event.getClickedBlock(), event.getBlockFace(), event.getAction());
                } else {
                    return;
                }
            } else {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    rotateClockwise(event.getClickedBlock());
                } else {
                    rotateCounterClockwise(event.getClickedBlock());
                }
            }

            event.setCancelled(true);
            return;
        } else if(event.getClickedBlock().getBlockData() instanceof Orientable) {
            if(event.getPlayer().isSneaking()) {
                flipLogsShift(event.getClickedBlock(), event.getBlockFace());
            } else {
                flipLogs(event.getClickedBlock());
            }

            event.setCancelled(true);
            return;
        }
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

    public void flipLogsShift(Block block, BlockFace clicked) {
        Orientable orientable = (Orientable)block.getBlockData();

        Vector direction = block.getRelative(clicked.getOppositeFace()).getLocation().toVector().subtract(block.getLocation().toVector());

        if(orientable.getAxis() == Axis.Y) {
            if(direction.getX() != 0) {
                orientable.setAxis(Axis.X);
            } else {
                orientable.setAxis(Axis.Z);
            }
        } else {
            orientable.setAxis(Axis.Y);
        }

        block.setBlockData(orientable);
    }

    public void flipLogs(Block block) {
        Orientable orientable = (Orientable)block.getBlockData();

        if(orientable.getAxis() == Axis.X) {
            orientable.setAxis(Axis.Z);
        } else {
            orientable.setAxis(Axis.X);
        }

        block.setBlockData(orientable);
    }

    public void flipSpecialShift(Block block, BlockFace clicked, Action action) {
        Directional directional = (Directional) block.getBlockData();

        if(clicked != BlockFace.DOWN && clicked != BlockFace.UP) {
            if (directional.getFacing() != BlockFace.DOWN && directional.getFacing() != BlockFace.UP) {
                if (action == Action.LEFT_CLICK_BLOCK) {
                    if (directional.getFacing() == clicked) {
                        directional.setFacing(BlockFace.UP);
                    } else {
                        directional.setFacing(BlockFace.DOWN);
                    }
                } else {
                    if (directional.getFacing() == clicked) {
                        directional.setFacing(BlockFace.DOWN);
                    } else {
                        directional.setFacing(BlockFace.UP);
                    }
                }
            } else {
                if (action == Action.LEFT_CLICK_BLOCK) {
                    if (directional.getFacing() == BlockFace.UP) {
                        directional.setFacing(clicked.getOppositeFace());
                    } else {
                        directional.setFacing(clicked);
                    }
                } else {
                    if (directional.getFacing() == BlockFace.DOWN) {
                        directional.setFacing(clicked.getOppositeFace());
                    } else {
                        directional.setFacing(clicked);
                    }
                }
            }
        } else {
            if (directional.getFacing() == BlockFace.UP) {
                if(action == Action.LEFT_CLICK_BLOCK) {
                    directional.setFacing(BlockFace.NORTH);
                } else {
                    directional.setFacing(BlockFace.SOUTH);
                }
            } else if (directional.getFacing() == BlockFace.DOWN){
                if(action == Action.LEFT_CLICK_BLOCK) {
                    directional.setFacing(BlockFace.SOUTH);
                } else {
                    directional.setFacing(BlockFace.NORTH);
                }
            } else if (directional.getFacing() == BlockFace.NORTH) {
                if(action == Action.LEFT_CLICK_BLOCK) {
                    directional.setFacing(BlockFace.DOWN);
                } else {
                    directional.setFacing(BlockFace.UP);
                }
            } else {
                if(action == Action.LEFT_CLICK_BLOCK) {
                    directional.setFacing(BlockFace.UP);
                } else {
                    directional.setFacing(BlockFace.DOWN);
                }
            }
        }

        block.setBlockData(directional);
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

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        tool = Material.getMaterial(plugin.getConfig().getString("Module-Settings.Rotator-Module.Tool-Material"));

        moduleEnabled = plugin.enabledModules.contains("Rotator");

        if(moduleEnabled) {
            plugin.logger.info("- RotatorModule Enabled");
        }
    }
}