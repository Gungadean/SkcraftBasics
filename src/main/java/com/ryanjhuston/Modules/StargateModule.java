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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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

        List<Location> blocks = new ArrayList<>();
        List<Location> portalBlocks = new ArrayList<>();
        Location signLocation = clicked.getLocation();
        Location buttonLocation = clicked.getLocation();
        Location teleportLocation;

        String portalName = "";
        String network = "public";
        String owner = player.getUniqueId().toString();
        String direction = "";

        if(clicked.getRelative(1, 0, 0).getType() == Material.DIAMOND_BLOCK && clicked.getRelative(1, 1, 0).getType() == Material.AIR) {
            blocks.add(clicked.getRelative(1, 0, 0).getLocation());
            teleportLocation = clicked.getLocation().getBlock().getRelative(1, 1, 0).getLocation();
            direction = "NS";
            x = 1;
        } else if(clicked.getRelative(0, 0, -1).getType() == Material.DIAMOND_BLOCK && clicked.getRelative(0, 1, -1).getType() == Material.AIR) {
            blocks.add(clicked.getRelative(0, 0, -1).getLocation());
            teleportLocation = clicked.getLocation().getBlock().getRelative(0, 1, 0).getLocation();
            direction = "EW";
            z = -1;
        } else if(clicked.getRelative(-1, 0, 0).getType() == Material.DIAMOND_BLOCK && clicked.getRelative(-1, 1, 0).getType() == Material.AIR) {
            blocks.add(clicked.getLocation());
            clicked = clicked.getRelative(-1, 0, 0);
            teleportLocation = clicked.getLocation().getBlock().getRelative(1, 1, 0).getLocation();
            direction = "NS";
            x = 1;
        } else if(clicked.getRelative(0, 0, 1).getType() == Material.DIAMOND_BLOCK && clicked.getRelative(0, 1, 1).getType() == Material.AIR) {
            blocks.add(clicked.getLocation());
            clicked = clicked.getRelative(0, 0, 1);
            teleportLocation = clicked.getLocation().getBlock().getRelative(0, 1, 0).getLocation();
            direction = "EW";
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
                teleportLocation.setYaw(0);
            } else if(clicked.getRelative(2, 2, -1).getType().toString().contains("_SIGN") && clicked.getRelative(-1, 2, -1).getType().toString().contains("_BUTTON")) {
                signLocation = clicked.getRelative(2, 2, -1).getLocation();
                buttonLocation = clicked.getRelative(-1, 2, -1).getLocation();
                teleportLocation.setYaw(-180);
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
                teleportLocation.setYaw(-90);
            } else if(clicked.getRelative(-1, 2, -2).getType().toString().contains("_SIGN") && clicked.getRelative(-1, 2, 1).getType().toString().contains("_BUTTON")) {
                signLocation = clicked.getRelative(-1, 2, -2).getLocation();
                buttonLocation = clicked.getRelative(-1, 2, 1).getLocation();
                teleportLocation.setYaw(90);
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

        if(x != 0) {
            for(y = 1; y <= 3; y++) {
                for(x = 0; x <= 1; x++) {
                    if(clicked.getRelative(x, y, 0).getType() != Material.AIR) {return false;}
                    portalBlocks.add(clicked.getRelative(x, y, 0).getLocation());
                }
            }
        }

        if(z != 0) {
            for(y = 1; y <= 3; y++) {
                for(z = 0; z >= -1; z--) {
                    if(clicked.getRelative(0, y, z).getType() != Material.AIR) {return false;}
                    portalBlocks.add(clicked.getRelative(0, y, z).getLocation());
                }
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
                plugin.networkList.get(network).add(portalName);
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

        sign.setLine(0, "-" + portalName + "-");
        sign.setLine(1, "Right click");
        sign.setLine(2, "to use gate");
        sign.setLine(3, "(" + network + ")");
        sign.update();

        Iterator it = blocks.iterator();
        while(it.hasNext()) {
            ((Location)it.next()).getBlock().setMetadata("Stargate", new FixedMetadataValue(plugin, portalName));
        }

        signLocation.getBlock().setMetadata("Stargate", new FixedMetadataValue(plugin, portalName));
        buttonLocation.getBlock().setMetadata("Stargate", new FixedMetadataValue(plugin, portalName));

        plugin.stargateList.put(portalName, new Stargate(owner, network, teleportLocation, signLocation, buttonLocation, blocks, portalBlocks, direction));

        player.sendMessage(ChatColor.GOLD + "New stargate has been successfully created.");
        return true;
    }

    public void removeStargate(String portalName) {
        Stargate stargate = plugin.stargateList.get(portalName);

        plugin.networkList.get(stargate.getNetwork()).remove(portalName);

        Iterator it = stargate.getBlocks().iterator();
        while(it.hasNext()) {
            ((Location)it.next()).getBlock().removeMetadata("Stargate", plugin);
        }

        if(stargate.getNetwork().isEmpty()) {
            plugin.networkList.remove(stargate.getNetwork());
        }

        stargate.getSignLocation().getBlock().removeMetadata("Stargate", plugin);

        Sign sign = (Sign)stargate.getSignLocation().getBlock().getState();
        sign.setLine(0, "[Stargate]");
        sign.setLine(1, portalName);
        if(stargate.getNetwork() == "public") {
            sign.setLine(2, "");
        } else {
            sign.setLine(2, stargate.getNetwork());
        }
        sign.setLine(3, "");
        sign.update();

        stargate.getButtonLocation().getBlock().removeMetadata("Stargate", plugin);

        plugin.stargatesConfig.set(portalName, null);

        plugin.stargateList.remove(portalName);
    }

    public void playerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if(event.getClickedBlock().getType().toString().contains("_SIGN")) {
            if(event.getClickedBlock().hasMetadata("Stargate")) {
                updateStargateSign(event.getClickedBlock());
            }
        }

        if(event.getClickedBlock().getType().toString().contains("_BUTTON")) {
            if(event.getClickedBlock().hasMetadata("Stargate")) {
                openPortal(event.getClickedBlock(), event.getPlayer());
            }
        }

        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.FLINT_AND_STEEL) {
            return;
        }

        if (event.getClickedBlock().getType() != Material.DIAMOND_BLOCK) {
            return;
        }

        if (event.getClickedBlock().hasMetadata("Stargate")) {
            event.getPlayer().sendMessage(ChatColor.RED + "This is already an active stargate.");
            event.setCancelled(true);
            return;
        }

        createStargate(event.getClickedBlock(), event.getPlayer());
        event.setCancelled(true);
    }

    public void blockBreak(BlockBreakEvent event) {
        if(event.getBlock().hasMetadata("Stargate")) {
            String stargate = event.getBlock().getMetadata("Stargate").get(0).asString();
            if(plugin.stargateList.containsKey(stargate)) {
                removeStargate(stargate);
            }
        }
    }

    public void playerMove(PlayerMoveEvent event) {
        if(event.getPlayer().getLocation().getBlock().getType() != Material.NETHER_PORTAL) {
            return;
        }

        if(!event.getPlayer().getLocation().getBlock().hasMetadata("Stargate")) {
            return;
        }

        Stargate stargate = plugin.stargateList.get(event.getPlayer().getLocation().getBlock().getMetadata("Stargate").get(0).asString());
        event.getPlayer().teleport(stargate.getTeleportLocation());
    }

    public void updateStargateSign(Block clicked) {
        Sign sign = (Sign)clicked.getState();
        Stargate stargate = plugin.stargateList.get(clicked.getMetadata("Stargate").get(0).asString());
        List<String> networkList = new ArrayList<>();

        for(int i = 0; i < plugin.networkList.get(stargate.getNetwork()).size(); i++) {
            networkList.add(plugin.networkList.get(stargate.getNetwork()).get(i));
        }

        networkList.remove(clicked.getMetadata("Stargate").get(0).asString());

        if(stargate.getSignTask() != null) {
            stargate.getSignTask().cancel();
        }

        BukkitTask bukrun = new BukkitRunnable() {
            @Override
            public void run() {
                sign.setLine(0, "-" + clicked.getMetadata("Stargate").get(0).asString() + "-");
                sign.setLine(1, "Right click");
                sign.setLine(2, "to use gate");
                sign.setLine(3, "(" + stargate.getNetwork() + ")");
                sign.update();

                stargate.setSignTask(null);
            }
        }.runTaskLater(plugin, 200);

        stargate.setSignTask(bukrun);

        if(networkList.size() == 0) {
            sign.setLine(0,"No other portals");
            sign.setLine(1, "are on this");
            sign.setLine(2, "network");
            sign.setLine(3, "");
            sign.update();
            return;
        }

        if(sign.getLine(1).equals("Right click")) {
            sign.setLine(0, ">" + networkList.get(0) + "<");
            for(int i = 1; i < 4; i++) {
                if(i >= networkList.size()) {
                    sign.setLine(i, "");
                } else {
                    sign.setLine(i, networkList.get(i));
                }
            }
        } else {
            if (sign.getLine(1).isEmpty()) {
                sign.setLine(0, ">" + networkList.get(0) + "<");
                for(int i = 1; i < 4; i++) {
                    if(i >= networkList.size()) {
                        sign.setLine(i, "");
                    } else {
                        sign.setLine(i, networkList.get(i));
                    }
                }
            } else {
                String start = sign.getLine(0).substring(1, sign.getLine(0).length()-1);

                sign.setLine(1, "");
                sign.setLine(2, "");
                sign.setLine(3, "");

                sign.setLine(0, ">" + networkList.get(networkList.indexOf(start)+1) + "<");
                for(int i = networkList.indexOf(start) + 2; i < networkList.indexOf(start)+4; i++) {
                    if(i >= networkList.size()) {
                        sign.setLine(i, "");
                    } else {
                        sign.setLine(i, networkList.get(i));
                    }
                }
            }
        }
        sign.update();
    }

    public void openPortal(Block clicked, Player player) {
        Stargate stargate = plugin.stargateList.get(clicked.getMetadata("Stargate").get(0).asString());
        Sign sign = (Sign)stargate.getSignLocation().getBlock().getState();

        if(sign.getLine(0).equals("-" + clicked.getMetadata("Stargate").get(0).asString() + "-")) {
            player.sendMessage(ChatColor.RED + "You must first select a destination.");
            return;
        }

        if(sign.getLine(0).equals("No other portals")) {
            player.sendMessage(ChatColor.RED + "There are no other portals on this network.");
            return;
        }

        List<Location> portalBlocks = stargate.getPortalBlocks();

        for(int i = 0; i < portalBlocks.size(); i++) {
            Block block = portalBlocks.get(i).getBlock();
            block.setType(Material.NETHER_PORTAL);

            block.setMetadata("Stargate", new FixedMetadataValue(plugin, sign.getLine(0).substring(1, sign.getLine(0).length()-1)));

            if(stargate.getDirection().equals("EW")) {
                Orientable portal = (Orientable)block.getBlockData();
                portal.setAxis(Axis.Z);
                block.setBlockData(portal);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for(int i = 0; i < portalBlocks.size(); i++) {
                    Block block = portalBlocks.get(i).getBlock();
                    block.setType(Material.AIR);
                }
            }
        }.runTaskLater(plugin, 80);
    }
}