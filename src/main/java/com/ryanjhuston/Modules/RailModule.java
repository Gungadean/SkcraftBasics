package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.Stargate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RailModule implements Listener {

    private SkcraftBasics plugin;

    private List<String> players = new ArrayList<>();

    private boolean moduleEnabled;

    public RailModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isInvulnerable() && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            setInvulnerable(event.getPlayer(), false);
        }
    }

    @EventHandler
    public void onItemDispense(BlockDispenseEvent event) {
        if(event.getItem().getType() != Material.MINECART || event.getBlock().getType() != Material.DISPENSER) {
            return;
        }

        Dispenser dispenser = (Dispenser) event.getBlock().getBlockData();
        Minecart minecart;
        Vector direction;

        if(isRail(event.getBlock().getRelative(0, 2, 0).getType())) {
            if(!isRail(event.getBlock().getRelative(0, 2, 0).getRelative(dispenser.getFacing()).getType())) {
                return;
            }

            minecart = (Minecart)event.getBlock().getWorld().spawnEntity(event.getBlock().getRelative(0, 2, 0).getLocation().add(0.5, 0.5, 0.5), EntityType.MINECART);

            direction = event.getBlock().getRelative(0, 2, 0).getRelative(dispenser.getFacing()).getLocation().toVector().subtract(event.getBlock().getRelative(0, 2, 0).getLocation().toVector());
        } else {
            if(!isRail(event.getBlock().getRelative(dispenser.getFacing()).getType())) {
                return;
            }

            minecart = (Minecart)event.getBlock().getWorld().spawnEntity(event.getBlock().getRelative(dispenser.getFacing()).getLocation().add(0.5, 0.5, 0.5), EntityType.MINECART);

            direction = event.getBlock().getRelative(dispenser.getFacing()).getLocation().toVector().subtract(event.getBlock().getLocation().toVector());
        }

        Vector velocity = direction.multiply(0.4);

        minecart.setVelocity(velocity);

        event.setCancelled(true);
    }

    @EventHandler (ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) {
        Vehicle vehicle = event.getVehicle();
        Entity attacker = event.getAttacker();

        if (attacker instanceof Arrow && vehicle instanceof Minecart) {
            event.setCancelled(true);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void portalTeleport(VehicleMoveEvent event) {
        if(event.getTo().getBlock().getType() != Material.NETHER_PORTAL) {
            return;
        }

        if(event.getVehicle().getType() != EntityType.MINECART) {
            return;
        }

        if(event.getVehicle().getPassengers().isEmpty()) {
            event.getVehicle().remove();
            return;
        }

        Entity passenger = event.getVehicle().getPassengers().get(0);

        if(!(passenger instanceof Player)) {
            return;
        }

        if(players.contains(passenger.getUniqueId().toString())) {
            return;
        }

        Location to = event.getTo();

        players.add(passenger.getUniqueId().toString());

        if(event.getTo().getBlock().hasMetadata("Stargate")) {
            Stargate stargate = plugin.stargateModule.stargateList.get(to.getBlock().getMetadata("Stargate").get(0).asString());
            to = stargate.getTeleportLocation();
            teleportThroughStargate(event.getVehicle(), to);
        } else {
            passenger.setMetadata("PortalLocation", new FixedMetadataValue(plugin, plugin.locationToString(event.getVehicle().getLocation())));
            passenger.setMetadata("xVel", new FixedMetadataValue(plugin, event.getVehicle().getVelocity().getX()));
            passenger.setMetadata("zVel", new FixedMetadataValue(plugin, event.getVehicle().getVelocity().getZ()));

            setInvulnerable((Player) passenger, true);

            event.getVehicle().eject();

            passenger.teleport(event.getTo());

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                players.remove(passenger.getUniqueId().toString());
                setInvulnerable((Player) passenger, false);
            }, 3);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onEntityTeleport(PlayerPortalEvent event) {
        if(!event.getPlayer().hasMetadata("PortalLocation")) {
            return;
        }

        Player player = event.getPlayer();

        Location from = plugin.stringToLocation(event.getPlayer().getMetadata("PortalLocation").get(0).asString());

        Vector vector = new Vector(event.getPlayer().getMetadata("xVel").get(0).asDouble(), 0, event.getPlayer().getMetadata("zVel").get(0).asDouble());

        player.removeMetadata("PortalLocation", plugin);
        player.removeMetadata("xVel", plugin);
        player.removeMetadata("zVel", plugin);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> teleportThroughPortal(player, from, player.getLocation(), vector), 2);
    }

    @EventHandler (ignoreCancelled = true)
    public void stargateActivator(BlockRedstoneEvent event) {
        if(!plugin.enabledModules.contains("Stargate")) {
            return;
        }

        if(event.getBlock().getType() != Material.DETECTOR_RAIL) {
            return;
        }

        if(event.getNewCurrent() == 0) {
            return;
        }

        if(!Tag.SIGNS.getValues().contains(event.getBlock().getRelative(0, -2, 0).getType())) {
            return;
        }

        Sign sign = (Sign)event.getBlock().getRelative(0, -2, 0).getState();

        if(!sign.getLine(0).equals("[Stargate Rail]")) {
            return;
        }

        if(sign.getLine(1).equals("")) {
            return;
        }

        Location stargateLocation = checkForStargate(event.getBlock().getLocation());

        if(stargateLocation == null) {
            return;
        }

        plugin.stargateModule.openPortalNamed(stargateLocation.getBlock(), sign.getLine(1));
    }

    @EventHandler (ignoreCancelled = true)
    public void onSignPlace(SignChangeEvent event) {
        if(!event.getLine(0).equalsIgnoreCase("[Stargate Rail]")) {
            return;
        }

        if(!plugin.enabledModules.contains("Stargate")) {
            event.getPlayer().sendMessage(ChatColor.RED + "The stargate module is not enabled on this server.");
            return;
        }

        String destination = event.getLine(1);

        if(destination.equals("")) {
            event.getPlayer().sendMessage(ChatColor.RED + "You must set a destination for the portal.");
            return;
        }

        Sign sign = (Sign)event.getBlock().getState();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            sign.setLine(0, "[Stargate Rail]");
            sign.setLine(1, destination);
            sign.update();
        }, 2);
    }

    public Location findAdjacentRail(Vector vector, Location base, Material match) {
        Location first = base.clone();
        Location last = base.clone();

        int x = 0;
        int z = 0;

        int xVel = (int)(vector.getX()/Math.abs(vector.getX()));
        int zVel = (int)(vector.getZ()/Math.abs(vector.getZ()));

        if(match == null && isRail(base.getBlock().getRelative(xVel, 0, zVel).getType())) {
            return base.getBlock().getRelative(xVel, 0, zVel).getLocation();
        }

        if(base.getBlock().getRelative(1, 0, 0).getType() == Material.NETHER_PORTAL || base.getBlock().getRelative(-1, 0, 0).getType() == Material.NETHER_PORTAL) {
            x = -1;
        } else {
            z = -1;
        }

        while(first.getBlock().getRelative(x, 0, z).getType() == Material.NETHER_PORTAL) {
            first = first.add(x, 0, z);
        }

        while(last.getBlock().getRelative((x*-1), 0, (z*-1)).getType() == Material.NETHER_PORTAL) {
            last = last.add((x*-1), 0, (z*-1));
        }

        first.add(0, -1, 0);

        if(zVel != 0) {
            first.add(0, 0, zVel);
        } else if(xVel != 0) {
            first.add(xVel, 0, 0);
        }

        int portalBlocks;

        if(x != 0) {
            portalBlocks = last.getBlockX()-first.getBlockX();
        } else {
            portalBlocks = last.getBlockZ()-first.getBlockZ();
        }

        for(int i = 0; i <= portalBlocks; i++) {
            if(match != null) {
                if(first.getBlock().getType() != match) {
                    first.add((x*-1), 0, (z*-1));
                    continue;
                }
            }

            if(isRail(first.getBlock().getRelative(0, 1, 0).getType())) {
                return first.add(0, 1, 0);
            } else {
                first.add((x*-1), 0, (z*-1));
            }
        }

        if(match != null) {
            return findAdjacentRail(vector, base, null);
        }

        return null;
    }

    public void teleportThroughPortal(Entity passenger, Location from, Location to, Vector vector) {
        Location railLocation;
        Location underRailLocation = from.clone();

        double xVel = vector.getX();
        double zVel = vector.getZ();

        if(xVel != 0) {
            underRailLocation.add((-1*(xVel/Math.abs(xVel))), -1, 0);
        } else {
            underRailLocation.add(0, -1, (-1*(zVel/Math.abs(zVel))));
        }

        if (isConcrete(underRailLocation.getBlock().getType())) {
            railLocation = findAdjacentRail(vector, to, underRailLocation.getBlock().getType());
        } else {
            railLocation = findAdjacentRail(vector, to, null);
        }

        if(railLocation != null) {
            Location portal = getOffsetPortal(railLocation);

            Vector velocity = new Vector(0, 0, 0);
            if(xVel != 0) {
                velocity.setX(xVel/Math.abs(xVel));
            }

            if(zVel != 0) {
                velocity.setZ(zVel/Math.abs(zVel));
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                passenger.teleport(railLocation);
                Minecart minecart = to.getWorld().spawn(railLocation, Minecart.class);
                minecart.addPassenger(passenger);
                minecart.setVelocity(velocity);
            }, 2);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                passenger.teleport(to);
                Minecart minecart = to.getWorld().spawn(to, Minecart.class);
                minecart.addPassenger(passenger);
            }, 2);
        }
    }

    public void teleportThroughStargate(Vehicle vehicle, Location to) {
        Entity passenger = vehicle.getPassengers().get(0);

        Location railLocation = null;

        for(int x = -1; x < 2; x++) {
            for(int z = -1; z < 2; z++) {
                if(isRail(to.getBlock().getRelative(x, 0, z).getType())) {
                    railLocation = to.getBlock().getRelative(x, 0, z).getLocation();
                }
            }
        }

        if(railLocation != null) {
            Vector direction = railLocation.toVector().subtract(to.toVector());

            Vector velocity = direction.multiply(1);
            vehicle.eject();
            vehicle.remove();

            Location finalRailLocation = railLocation;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                passenger.teleport(finalRailLocation);
                Minecart minecart = to.getWorld().spawn(finalRailLocation, Minecart.class);
                minecart.addPassenger(passenger);
                minecart.setVelocity(velocity);
            }, 2);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                passenger.teleport(to);
                Minecart minecart = to.getWorld().spawn(to, Minecart.class);
                minecart.addPassenger(passenger);
            }, 2);
        }
    }

    private static boolean isRail(Material mat) {
        return Tag.RAILS.getValues().contains(mat);
    }

    private static boolean isConcrete(Material mat) {
        return mat.toString().endsWith("_CONCRETE");
    }

    private Location getOffsetPortal(Location location) {
        if(location.getBlock().getRelative(1, 0, 0).getType() == Material.NETHER_PORTAL) {
            return location.getBlock().getRelative(1, 0, 0).getLocation();
        } else if(location.getBlock().getRelative(-1, 0, 0).getType() == Material.NETHER_PORTAL) {
            return location.getBlock().getRelative(-1, 0, 0).getLocation();
        } else if(location.getBlock().getRelative(0, 0, 1).getType() == Material.NETHER_PORTAL) {
            return location.getBlock().getRelative(0, 0, 1).getLocation();
        } else if(location.getBlock().getRelative(0, 0, -1).getType() == Material.NETHER_PORTAL) {
            return location.getBlock().getRelative(0, 0, -1).getLocation();
        } else {
            return null;
        }
    }

    private Location checkForStargate(Location location) {
        if(location.getBlock().getRelative(1, -1, 0).hasMetadata("Stargate")) {
            return location.getBlock().getRelative(1, -1, 0).getLocation();
        } else if(location.getBlock().getRelative(-1, -1, 0).hasMetadata("Stargate")) {
            return location.getBlock().getRelative(-1, -1, 0).getLocation();
        }else if(location.getBlock().getRelative(0, -1, 1).hasMetadata("Stargate")) {
            return location.getBlock().getRelative(0, -1, 1).getLocation();
        } else if(location.getBlock().getRelative(0, -1, -1).hasMetadata("Stargate")) {
            return location.getBlock().getRelative(0, -1, -1).getLocation();
        }
        return null;
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.enabledModules.contains("Rail");

        if(moduleEnabled) {
            HandlerList.unregisterAll(plugin.railModule);
            plugin.pm.registerEvents(plugin.railModule, plugin);

            plugin.logger.info("- RailModule Enabled");
        } else {
            HandlerList.unregisterAll(plugin.railModule);
        }
    }

    //"Borrowed" from InstantNetherPortals

    private boolean playerAbilitiesOldType;
    private String playerAbilitiesName = null;
    private String invulnerableName = null;

    public void setInvulnerable(Player player, boolean bool) {
        if(player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (playerAbilitiesName == null) {
            setPlayerAbilitiesMethodName(player);
        }

        if (playerAbilitiesName != null && invulnerableName == null) {
            setInvulnerableBooleanName(player);
        }

        if (playerAbilitiesName != null && invulnerableName != null) {
            try {
                Object playerAbilitiesObject = getPlayerAbilitiesObject(player);
                Field invulnerableField = playerAbilitiesObject.getClass().getDeclaredField(invulnerableName);

                invulnerableField.setAccessible(true);
                invulnerableField.setBoolean(playerAbilitiesObject, bool);
                invulnerableField.setAccessible(false);
            } catch (IllegalAccessException | NoSuchFieldException exception) {
                exception.printStackTrace();
            }
        } else {
            System.out.println("Error: Deobfuscation failed");
        }
    }

    public Object getPlayerAbilitiesObject(Player player) {
        try {
            Object playerObject = player.getClass().getMethod("getHandle").invoke(player);

            return playerAbilitiesOldType ? playerObject.getClass().getSuperclass()
                    .getDeclaredField(playerAbilitiesName).get(playerObject) : playerObject.getClass().getSuperclass()
                    .getMethod(playerAbilitiesName).invoke(playerObject);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException
                 | NoSuchFieldException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    private void setPlayerAbilitiesMethodName(Player player) {
        try {
            Object playerObject = player.getClass().getMethod("getHandle").invoke(player);

            for (Method method : playerObject.getClass().getSuperclass().getMethods()) {
                if (method.getReturnType().getName().endsWith("PlayerAbilities")) {
                    playerAbilitiesOldType = false;
                    playerAbilitiesName = method.getName();

                    return;
                }
            }

            for (Field field : playerObject.getClass().getSuperclass().getDeclaredFields()) {
                if (field.getType().getName().endsWith("PlayerAbilities")) {
                    playerAbilitiesOldType = true;
                    playerAbilitiesName = field.getName();

                    return;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
            exception.printStackTrace();
        }
    }

    private void setInvulnerableBooleanName(Player player) {
        Object playerAbilitiesObject = getPlayerAbilitiesObject(player);

        invulnerableName = playerAbilitiesObject.getClass().getDeclaredFields()[0].getName();
    }

    public boolean isNetherPortal(Block block) {
        return block.getType().name().equals("NETHER_PORTAL") || block.getType().name().equals("PORTAL");
    }

    public String getServerVersion() {
        String version;
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "unknown";
        }
        return version;
    }
}