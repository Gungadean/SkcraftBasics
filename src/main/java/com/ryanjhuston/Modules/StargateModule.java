package com.ryanjhuston.Modules;

import com.ryanjhuston.Events.PlayerEnterStargateEvent;
import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.Stargate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class StargateModule implements Listener {

    private SkcraftBasics plugin;

    public HashMap<String, Stargate> stargateList = new HashMap<>();
    public HashMap<String, List<String>> networkList = new HashMap<>();

    private int resetDelay;

    private boolean moduleEnabled;

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
        String direction;

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
            } else {
                if(clicked.getRelative(x, y, z).getType() != Material.BEACON) {return false;}
            }
            blocks.add(clicked.getRelative(x, y, z).getLocation());
        }

        y = 4;

        if(x != 0) {
            for(x -= 1; x >= 0; x--) {
                if(clicked.getRelative(x, y, z).getType() != Material.DIAMOND_BLOCK) {return false;}
                blocks.add(clicked.getRelative(x, y, z).getLocation());
            }

            if(Tag.SIGNS.getValues().contains(clicked.getRelative(-1, 2, 1).getType()) && Tag.BUTTONS.getValues().contains(clicked.getRelative(2, 2, 1).getType())) {
                signLocation = clicked.getRelative(-1, 2, 1).getLocation();
                buttonLocation = clicked.getRelative(2, 2, 1).getLocation();
                teleportLocation.setYaw(0);
            } else if(Tag.SIGNS.getValues().contains(clicked.getRelative(2, 2, -1).getType()) && Tag.BUTTONS.getValues().contains(clicked.getRelative(-1, 2, -1).getType())) {
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

            if(Tag.SIGNS.getValues().contains(clicked.getRelative(1, 2, 1).getType()) && Tag.BUTTONS.getValues().contains(clicked.getRelative(1, 2, -2).getType())) {
                signLocation = clicked.getRelative(1, 2, 1).getLocation();
                buttonLocation = clicked.getRelative(1, 2, -2).getLocation();
                teleportLocation.setYaw(-90);
            } else if(Tag.SIGNS.getValues().contains(clicked.getRelative(-1, 2, -2).getType()) && Tag.BUTTONS.getValues().contains(clicked.getRelative(-1, 2, 1).getType())) {
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
            } else {
                if(clicked.getRelative(x, y, z).getType() != Material.BEACON) {return false;}
            }
            blocks.add(clicked.getRelative(x, y, z).getLocation());
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

            portalName = portalName.replaceAll("^[^\\w+( [\\w]+)*]$", "");

            if(stargateList.containsKey(portalName)) {
                player.sendMessage(ChatColor.RED + "A portal with this name already exists.");
                return false;
            }

            if(sign.getLine(2).isEmpty()) {
                network = "public";

                networkList.get(network).add(portalName);
            } else {
                network = sign.getLine(2);
                network = network.replaceAll("^[^\\w+( [\\w]+)*]$", "");
                if(!networkList.containsKey(network)) {
                    List<String> stargates = new ArrayList<>();
                    stargates.add(portalName);
                    networkList.put(network, stargates);
                } else {
                    networkList.get(network).add(portalName);
                }
            }
        }

        Stargate stargate = new Stargate(portalName, owner, network, teleportLocation, signLocation, buttonLocation, blocks, portalBlocks, direction);

        updateStargateBlocks(stargate);

        stargateList.put(portalName, stargate);

        player.sendMessage(ChatColor.YELLOW + "New stargate has been successfully created.");

        plugin.saveStargatesToFile();

        return true;
    }

    public void updateStargateBlocks(Stargate stargate) {
        Sign sign = (Sign)stargate.getSignLocation().getBlock().getState();

        sign.setLine(0, "-" + stargate.getName() + "-");
        sign.setLine(1, "Right click");
        sign.setLine(2, "to use gate");
        sign.setLine(3, "(" + stargate.getNetwork() + ")");
        sign.update();

        for(Location location : stargate.getBlocks()) {
            location.getBlock().setMetadata("Stargate", new FixedMetadataValue(plugin, stargate.getName()));
        }

        stargate.getSignLocation().getBlock().setMetadata("Stargate", new FixedMetadataValue(plugin, stargate.getName()));
        stargate.getButtonLocation().getBlock().setMetadata("Stargate", new FixedMetadataValue(plugin, stargate.getName()));
    }

    public void removeStargate(String portalName) {
        Stargate stargate = stargateList.get(portalName);

        networkList.get(stargate.getNetwork()).remove(portalName);

        for(Location location : stargate.getBlocks()) {
            location.getBlock().removeMetadata("Stargate", plugin);
        }

        stargate.getSignLocation().getBlock().removeMetadata("Stargate", plugin);

        Sign sign = (Sign)stargate.getSignLocation().getBlock().getState();
        sign.setLine(0, "[Stargate]");
        sign.setLine(1, portalName);
        if(stargate.getNetwork().equals("public")) {
            sign.setLine(2, "");
        } else {
            sign.setLine(2, stargate.getNetwork());
        }
        sign.setLine(3, "");
        sign.update();

        stargate.getButtonLocation().getBlock().removeMetadata("Stargate", plugin);

        stargateList.remove(portalName);

        plugin.saveStargatesToFile();
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if(event.getPlayer().getLocation().getBlock().getType() != Material.NETHER_PORTAL) {
            return;
        }

        if(!event.getPlayer().getLocation().getBlock().hasMetadata("Stargate")) {
            return;
        }

        PlayerEnterStargateEvent playerEnterStargateEvent = new PlayerEnterStargateEvent(event.getPlayer());
        Bukkit.getPluginManager().callEvent(playerEnterStargateEvent);
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if(event.getAction() == Action.PHYSICAL) {
            return;
        }

        if(event.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }

        if(plugin.interactCooldown.contains(event.getPlayer().getUniqueId().toString())) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.ARROW) {
            plugin.removeInteractCooldown(event.getPlayer().getUniqueId().toString());

            if(event.getClickedBlock().hasMetadata("Stargate")) {
                event.getPlayer().sendMessage(event.getClickedBlock().getMetadata("Stargate").get(0).asString());
            } else {
                event.getPlayer().sendMessage("No metadata present.");
            }
            return;
        }

        if(event.getClickedBlock().hasMetadata("Stargate")) {
            plugin.removeInteractCooldown(event.getPlayer().getUniqueId().toString());

            Stargate stargate = stargateList.get(event.getClickedBlock().getMetadata("Stargate").get(0).asString());
            if(stargate.isLocked()) {
                return;
            }
        }

        if(Tag.SIGNS.getValues().contains(event.getClickedBlock().getType())) {
            if(event.getClickedBlock().hasMetadata("Stargate")) {
                plugin.removeInteractCooldown(event.getPlayer().getUniqueId().toString());

                updateStargateSign(event.getClickedBlock(), event.getPlayer());
                return;
            }
        }

        if(Tag.BUTTONS.getValues().contains(event.getClickedBlock().getType())) {
            if(event.getClickedBlock().hasMetadata("Stargate")) {
                plugin.removeInteractCooldown(event.getPlayer().getUniqueId().toString());

                openPortal(event.getClickedBlock(), event.getPlayer());
                return;
            }
        }

        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.FLINT_AND_STEEL) {
            return;
        }

        if (event.getClickedBlock().getType() != Material.DIAMOND_BLOCK) {
            return;
        }

        plugin.removeInteractCooldown(event.getPlayer().getUniqueId().toString());

        if (event.getClickedBlock().hasMetadata("Stargate")) {
            event.getPlayer().sendMessage(ChatColor.RED + "This is already an active stargate.");
            event.setCancelled(true);
            return;
        }

        createStargate(event.getClickedBlock(), event.getPlayer());
        event.setCancelled(true);
    }

    @EventHandler (ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent event) {
        if(event.getBlock().hasMetadata("Stargate")) {
            String stargate = event.getBlock().getMetadata("Stargate").get(0).asString();
            if(stargateList.containsKey(stargate)) {
                removeStargate(stargate);
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Stargate successfully removed.");
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onEnterStargate(PlayerEnterStargateEvent event) {
        Stargate stargate = stargateList.get(event.getPlayer().getLocation().getBlock().getMetadata("Stargate").get(0).asString());
        event.getPlayer().teleport(stargate.getTeleportLocation());
    }

    public void updateStargateSign(Block clicked, Player clicker) {
        Sign sign = (Sign)clicked.getState();
        Stargate stargate = stargateList.get(clicked.getMetadata("Stargate").get(0).asString());
        List<String> networkList = new ArrayList<>();

        networkList.addAll(this.networkList.get(stargate.getNetwork()));

        networkList.remove(clicked.getMetadata("Stargate").get(0).asString());

        if(sign.getBlock().hasMetadata("Who-Clicked")) {
            if(!sign.getBlock().getMetadata("Who-Clicked").get(0).asString().equals(clicker.getUniqueId().toString())) {
                return;
            }
        } else {
            sign.getBlock().setMetadata("Who-Clicked", new FixedMetadataValue(plugin, clicker.getUniqueId().toString()));
        }

        if(stargate.getSignTask() != null) {
            stargate.getSignTask().cancel();
        }

        BukkitTask bukrun = new BukkitRunnable() {
            @Override
            public void run() {
                if(!clicked.hasMetadata("Stargate")) {
                    return;
                }

                if(clicked.getMetadata("Stargate").get(0) == null) {
                    return;
                }

                sign.setLine(0, "-" + clicked.getMetadata("Stargate").get(0).asString() + "-");
                sign.setLine(1, "Right click");
                sign.setLine(2, "to use gate");
                sign.setLine(3, "(" + stargate.getNetwork() + ")");
                sign.update();

                stargate.setSignTask(null);
                sign.getBlock().removeMetadata("Who-Clicked", plugin);
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
            int place = 0;

            String first = sign.getLine(0).substring(1, sign.getLine(0).length()-1);

            for(String portal : networkList) {
                if(first.equals(portal)) {
                    place++;
                    break;
                }

                place++;
            }

            if(place == networkList.size()) {
                sign.setLine(0, ">" + networkList.get(0) + "<");
                for(int i = 1; i < 4; i++) {
                    if(i >= networkList.size()) {
                        sign.setLine(i, "");
                    } else {
                        sign.setLine(i, networkList.get(i));
                    }
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    if (place >= networkList.size()) {
                        sign.setLine(i, "");
                        continue;
                    }

                    if (i == 0) {
                        sign.setLine(0, ">" + networkList.get(place) + "<");
                    } else {
                        sign.setLine(i, networkList.get(place));
                    }
                    place++;
                }
            }
        }
        sign.update();
    }

    public void openPortal(Block clicked, Player player) {
        Stargate stargate = stargateList.get(clicked.getMetadata("Stargate").get(0).asString());
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

        for(Location location : portalBlocks) {
            Block block = location.getBlock();
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
                for(Location location : portalBlocks) {
                    Block block = location.getBlock();
                    block.setType(Material.AIR);
                }
            }
        }.runTaskLater(plugin, (resetDelay*20));
    }

    public void openPortalNamed(Block portalBlock, String destination) {
        Stargate stargate = stargateList.get(portalBlock.getMetadata("Stargate").get(0).asString());
        Sign sign = (Sign)stargate.getSignLocation().getBlock().getState();

        if(portalBlock.getMetadata("Stargate").get(0).asString().equals(destination)) {
            return;
        }

        if(!networkList.get(stargate.getNetwork()).contains(destination)) {
            return;
        }

        sign.setLine(0,"Rail Destination:");
        sign.setLine(1, destination);
        sign.setLine(2, "");
        sign.setLine(3, "");
        sign.update();

        List<Location> portalBlocks = stargate.getPortalBlocks();

        for (Location location : portalBlocks) {
            Block block = location.getBlock();
            block.setType(Material.NETHER_PORTAL);

            block.setMetadata("Stargate", new FixedMetadataValue(plugin, destination));

            if(stargate.getDirection().equals("EW")) {
                Orientable portal = (Orientable)block.getBlockData();
                portal.setAxis(Axis.Z);
                block.setBlockData(portal);
            }
        }

        stargate.setLocked(true);

        new BukkitRunnable() {
            @Override
            public void run() {
                for(Location location : portalBlocks) {
                    Block block = location.getBlock();
                    block.setType(Material.AIR);
                }

                if(portalBlock.getMetadata("Stargate").get(0) == null) {
                    return;
                }

                sign.setLine(0, "-" + portalBlock.getMetadata("Stargate").get(0).asString() + "-");
                sign.setLine(1, "Right click");
                sign.setLine(2, "to use gate");
                sign.setLine(3, "(" + stargate.getNetwork() + ")");
                sign.update();

                stargate.setSignTask(null);
                sign.getBlock().removeMetadata("Who-Clicked", plugin);

                stargate.setLocked(false);
            }
        }.runTaskLater(plugin, (resetDelay*20));
    }

    @EventHandler (ignoreCancelled = true)
    public void playerTeleport(PlayerPortalEvent event) {
        for(int x = -1; x < 2; x++) {
            for(int z = -1; z < 2; z++) {
                Block block = event.getPlayer().getLocation().getBlock().getRelative(x, 0, z);

                if(block.getType() == Material.NETHER_PORTAL && block.hasMetadata("Stargate")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        resetDelay = plugin.getConfig().getInt("Module-Settings.Stargate-Module.Portal-Reset-Time");

        moduleEnabled = plugin.enabledModules.contains("Stargate");

        if(moduleEnabled) {
            HandlerList.unregisterAll(plugin.stargateModule);
            plugin.pm.registerEvents(plugin.stargateModule, plugin);

            plugin.logger.info("- StargateModule Enabled");
        } else {
            HandlerList.unregisterAll(plugin.stargateModule);
        }
    }
}