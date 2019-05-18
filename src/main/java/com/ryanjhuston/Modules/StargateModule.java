package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.Stargate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StargateModule {

    private SkcraftBasics plugin;

    public StargateModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    public boolean createStargate(Block clicked, Player player) {
        int x = 0;
        int z = 0;
        int y = 0;

        List<Location> blocks = new ArrayList<Location>();
        Location signLocation = clicked.getLocation();
        Location buttonLocation = clicked.getLocation();

        String portalName = "";
        String network = "public";
        String owner = player.getUniqueId().toString();

        if(clicked.getRelative(1, 0, 0).getType() == Material.DIAMOND_BLOCK && clicked.getRelative(1, 1, 0).getType() == Material.AIR) {
            blocks.add(clicked.getRelative(1, 0, 0).getLocation());
            x = 1;
        } else if(clicked.getRelative(0, 0, -1).getType() == Material.DIAMOND_BLOCK && clicked.getRelative(0, 1, -1).getType() == Material.AIR) {
            blocks.add(clicked.getRelative(0, 0, -1).getLocation());
            z = -1;
        } else if(clicked.getRelative(-1, 0, 0).getType() == Material.DIAMOND_BLOCK && clicked.getRelative(-1, 1, 0).getType() == Material.AIR) {
            blocks.add(clicked.getLocation());
            clicked = clicked.getRelative(-1, 0, 0);
            x = 1;
        } else if(clicked.getRelative(0, 0, 1).getType() == Material.DIAMOND_BLOCK && clicked.getRelative(0, 1, 1).getType() == Material.AIR) {
            blocks.add(clicked.getLocation());
            clicked = clicked.getRelative(0, 0, 1);
            z = -1;
        } else {
            return false;
        }

        blocks.add(clicked.getLocation());

        x += x;
        z += z;

        for(y += 1; y <= 3; y++) {
            if(y != 3) {
                if(clicked.getRelative(x, y, z).getType() != Material.DIAMOND_BLOCK) {return false;}
                blocks.add(clicked.getRelative(x, y, z).getLocation());
            } else {
                if(clicked.getRelative(x, y, z).getType() != Material.BEACON) {return false;}
                blocks.add(clicked.getRelative(x, y, z).getLocation());
            }
        }

        y = 4;

        if(x != 0) {
            for(x -= 1; x >= 0; x--) {
                if(clicked.getRelative(x, y, z).getType() != Material.DIAMOND_BLOCK) {return false;}
                blocks.add(clicked.getRelative(x, y, z).getLocation());
            }

            if(clicked.getRelative(-1, 2, 1).getType().toString().contains("_SIGN") && clicked.getRelative(2, 2, 1).getType().toString().contains("_BUTTON")) {
                signLocation = clicked.getRelative(-1, 2, 1).getLocation();
                buttonLocation = clicked.getRelative(2, 2, 1).getLocation();
            } else if(clicked.getRelative(2, 2, -1).getType().toString().contains("_SIGN") && clicked.getRelative(-1, 2, -1).getType().toString().contains("_BUTTON")) {
                signLocation = clicked.getRelative(2, 2, -1).getLocation();
                buttonLocation = clicked.getRelative(-1, 2, -1).getLocation();
            } else {
                return false;
            }
        }

        if(z != 0) {
            for(z += 1; z <= 0; z++) {
                if (clicked.getRelative(x, y, z).getType() != Material.DIAMOND_BLOCK) {return false;}
                blocks.add(clicked.getRelative(x, y, z).getLocation());
            }

            if(clicked.getRelative(1, 2, 1).getType().toString().contains("_SIGN") && clicked.getRelative(1, 2, -2).getType().toString().contains("_BUTTON")) {
                signLocation = clicked.getRelative(1, 2, 1).getLocation();
                buttonLocation = clicked.getRelative(1, 2, -2).getLocation();
            } else if(clicked.getRelative(-1, 2, -2).getType().toString().contains("_SIGN") && clicked.getRelative(-1, 2, 1).getType().toString().contains("_BUTTON")) {
                signLocation = clicked.getRelative(-1, 2, -2).getLocation();
                buttonLocation = clicked.getRelative(-1, 2, 1).getLocation();
            } else {
                return false;
            }
        }

        for(y -= 1; y >= 1; y--) {
            if(y != 3) {
                if(clicked.getRelative(x, y, z).getType() != Material.DIAMOND_BLOCK) {return false;}
                blocks.add(clicked.getRelative(x, y, z).getLocation());
            } else {
                if(clicked.getRelative(x, y, z).getType() != Material.BEACON) {return false;}
                blocks.add(clicked.getRelative(x, y, z).getLocation());
            }
        }

        Sign sign = (Sign)signLocation.getBlock().getState();

        if(sign.getLine(0).equalsIgnoreCase("[Stargate]")) {
            if(sign.getLine(1).isEmpty()) {
                player.sendMessage(ChatColor.RED + "You must put a name for the portal.");
                return false;
            }

            portalName = sign.getLine(1);

            if(plugin.stargateList.containsKey(portalName)) {
                player.sendMessage(ChatColor.RED + "A portal with this name already exists.");
                return false;
            }

            if(sign.getLine(2).isEmpty()) {
                network = "public";
            } else {
                network = sign.getLine(2);
                if(!plugin.networkList.containsKey(network)) {
                    List<String> stargates = new ArrayList<>();
                    stargates.add(portalName);
                    plugin.networkList.put(network, stargates);
                } else {
                    plugin.networkList.get(network).add(portalName);
                }
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

        Iterator it = blocks.iterator();
        while(it.hasNext()) {
            ((Location)it.next()).getBlock().setMetadata("Stargate", new FixedMetadataValue(plugin, portalName));
        }

        plugin.stargateList.put(portalName, new Stargate(owner, network, signLocation, buttonLocation, blocks));
        return true;
    }

    public void removeStargate(String portalName) {
        Stargate stargate = plugin.stargateList.get(portalName);

        plugin.networkList.get(stargate.getNetwork()).remove(portalName);

        Iterator it = stargate.getBlocks().iterator();
        while(it.hasNext()) {
            ((Location)it.next()).getBlock().removeMetadata("Stargate", plugin);
        }

        plugin.stargateList.remove(portalName);
    }

    public void playerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.ARROW) {
            if(event.getClickedBlock().hasMetadata("Stargate")) {
                event.getPlayer().sendMessage(event.getClickedBlock().getMetadata("Stargate").get(0).asString());
            } else {
                event.getPlayer().sendMessage("No metadata present.");
            }
        }

        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.FLINT_AND_STEEL) {
            return;
        }

        if (event.getClickedBlock().getType() != Material.DIAMOND_BLOCK) {
            return;
        }

        if (!createStargate(event.getClickedBlock(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    public void blockBreak(BlockBreakEvent event) {
        if(event.getBlock().hasMetadata("Stargate")) {
            String stargate = event.getBlock().getMetadata("Stargate").get(0).asString();
            if(plugin.stargateList.containsKey(stargate)) {
                removeStargate(stargate);
            }
        }
    }
}