package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Button;

public class StargateModule {

    private SkcraftBasics plugin;

    public StargateModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    public boolean createStargate(Block clicked, Player player) {
        int x = 0;
        int z = 0;
        int y = 0;
        Sign sign;

        if(clicked.getRelative(1, 0, 0).getType() == Material.DIAMOND_BLOCK && clicked.getRelative(1, 1, 0).getType() == Material.AIR) {
            x = 1;
        } else if(clicked.getRelative(0, 0, -1).getType() == Material.DIAMOND_BLOCK && clicked.getRelative(0, 1, -1).getType() == Material.AIR) {
            z = -1;
        } else if(clicked.getRelative(-1, 0, 0).getType() == Material.DIAMOND_BLOCK && clicked.getRelative(-1, 1, 0).getType() == Material.AIR) {
            clicked = clicked.getRelative(-1, 0, 0);
            x = 1;
        } else if(clicked.getRelative(0, 0, 1).getType() == Material.DIAMOND_BLOCK && clicked.getRelative(0, 1, 1).getType() == Material.AIR) {
            clicked = clicked.getRelative(0, 0, 1);
            z = -1;
        } else {
            return false;
        }

        x += x;
        z += z;

        for(y += 1; y <= 3; y++) {
            if(y != 3) {
                if(clicked.getRelative(x, y, z).getType() != Material.DIAMOND_BLOCK) {return false;}
            } else {
                if(clicked.getRelative(x, y, z).getType() != Material.BEACON) {return false;}
            }
        }

        y = 4;

        if(x != 0) {
            for(x -= 1; x >= 0; x--) {
                if(clicked.getRelative(x, y, z).getType() != Material.DIAMOND_BLOCK) {return false;}
            }

            if((clicked.getRelative(-1, 2, 1) instanceof Sign && clicked.getRelative(2, 2, 1) instanceof Button)) {
                sign = (Sign)clicked.getRelative(-1, 2, 1);
            }
        }

        if(z != 0) {
            for(z += 1; z <= 0; z++) {
                if (clicked.getRelative(x, y, z).getType() != Material.DIAMOND_BLOCK) {return false;}
            }
        }

        for(y -= 1; y >= 1; y--) {
            if(y != 3) {
                if(clicked.getRelative(x, y, z).getType() != Material.DIAMOND_BLOCK) {return false;}
            } else {
                if(clicked.getRelative(x, y, z).getType() != Material.BEACON) {return false;}
            }
        }

        if(x != 0) {
            for(y = 1; y <= 3; y++) {
                for(x = 0; x <= 1; x++) {
                    if(clicked.getRelative(x, y, 0).getType() != Material.AIR) {return false;}
                    clicked.getRelative(x, y, 0).setType(Material.NETHER_PORTAL);
                }
            }
        }

        if(z != 0) {
            for(y = 1; y <= 3; y++) {
                for(z = 0; z >= -1; z--) {
                    if(clicked.getRelative(0, y, z).getType() != Material.AIR) {return false;}

                    clicked.getRelative(0, y, z).setType(Material.NETHER_PORTAL);
                    Orientable portal = (Orientable)clicked.getRelative(0, y, z).getBlockData();
                    portal.setAxis(Axis.Z);
                    clicked.getRelative(0, y, z).setBlockData(portal);
                }
            }
        }

        return true;
    }

    public void removeStargate() {

    }

    public void playerInteract(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if(event.getPlayer().getInventory().getItemInMainHand().getType() != Material.FLINT_AND_STEEL) {
            return;
        }

        if(event.getClickedBlock().getType() != Material.DIAMOND_BLOCK) {
            return;
        }

        createStargate(event.getClickedBlock(), event.getPlayer());
    }
}
