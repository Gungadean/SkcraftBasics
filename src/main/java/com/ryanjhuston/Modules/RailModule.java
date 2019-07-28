package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.Stargate;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RailModule implements Listener {

    private SkcraftBasics plugin;

    private List<String> players = new ArrayList<>();

    public RailModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemDispense(BlockDispenseEvent event) {
        if(event.getItem().getType() == Material.MINECART && event.getBlock().getType() == Material.DISPENSER) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                @Override
                public void run() {
                    InventoryHolder dispenser = (InventoryHolder)event.getBlock().getState();
                    dispenser.getInventory().addItem(new ItemStack(Material.MINECART));
                }
            }, 1);
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        Vehicle vehicle = event.getVehicle();
        Entity attacker = event.getAttacker();

        if (attacker instanceof Arrow && vehicle instanceof Minecart) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void portalTeleport(VehicleMoveEvent event) {
        if(event.getTo().getBlock().getType() != Material.NETHER_PORTAL) {
            return;
        }

        if(event.getVehicle().getType() != EntityType.MINECART) {
            return;
        }

        if(event.getVehicle().getPassengers().isEmpty()) {
            return;
        }

        Entity passenger = event.getVehicle().getPassengers().get(0);

        if(players.contains(passenger.getUniqueId().toString())) {
            return;
        }

        Location to = event.getTo();

        if(event.getTo().getBlock().hasMetadata("Stargate")) {
            Stargate stargate = plugin.stargateModule.stargateList.get(to.getBlock().getMetadata("Stargate").get(0).asString());
            to = stargate.getTeleportLocation();
            teleportThroughPortal(event.getVehicle(), to);
            return;
        }

        players.add(passenger.getUniqueId().toString());
        Entity entity = event.getTo().getWorld().spawnEntity(to, EntityType.SILVERFISH);
        entity.setMetadata("PortalCheck", new FixedMetadataValue(plugin, passenger.getUniqueId().toString()));

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                players.remove(passenger.getUniqueId().toString());
                if(entity != null) {
                    entity.remove();
                }
            }
        }, 10L);
    }

    @EventHandler
    public void onEntityTeleport(EntityPortalEvent event) {
        if(!event.getEntity().hasMetadata("PortalCheck")) {
            return;
        }

        Entity passenger = Bukkit.getEntity(UUID.fromString(event.getEntity().getMetadata("PortalCheck").get(0).asString()));

        if(passenger != null) {
            if(passenger.isInsideVehicle()) {
                teleportThroughPortal((Vehicle)passenger.getVehicle(), event.getTo());
            }
        }

        event.getEntity().remove();
    }

    public Location findAdjacentRail(Vehicle vehicle, Location base, Material match) {
        Location first = base.clone();
        Location last = base.clone();

        int x = 0;
        int z = 0;

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

        if(vehicle.getVelocity().getZ() > 0) {
            first.add((z*-1), 0, (x*-1));
        } else if(vehicle.getVelocity().getZ() < 0) {
            first.add(z, 0, x);
        }

        int portalBlocks;

        if(x != 0) {
            portalBlocks = last.getBlockX()-first.getBlockX();
        } else {
            portalBlocks = last.getBlockZ()-first.getBlockZ();
        }

        for(int i = 0; i < portalBlocks; i++) {
            if(match != null) {
                if(first.getBlock().getType() != match) {
                    continue;
                }
            }

            if(isRail(first.getBlock().getRelative(0, 1, 0).getType())) {
                return first.add(0, 1, 0);
            } else {
                first.add((x*-1), 0, (z*-1));
            }
        }

        return null;
    }

    public void teleportThroughPortal(Vehicle vehicle, Location to) {
        Entity passenger = vehicle.getPassengers().get(0);

        Location railLocation;
        Location underRailLocation = vehicle.getLocation().clone();

        double xVel = vehicle.getVelocity().getX();
        double zVel = vehicle.getVelocity().getZ();

        if(xVel != 0) {
            underRailLocation.add((xVel/Math.abs(xVel)), 0, 0);
        } else {
            underRailLocation.add((zVel/Math.abs(zVel)), 0, 0);
        }

        if(isConcrete(underRailLocation.getBlock().getType())) {
            railLocation = findAdjacentRail(vehicle, to, underRailLocation.getBlock().getType());
        } else {
            railLocation = findAdjacentRail(vehicle, to, null);
        }

        if(railLocation != null) {
            Location portal = getOffsetPortal(railLocation);

            Vector direction = railLocation.toVector().subtract(portal.toVector());
            Vector velocity = direction.multiply(0.5);
            vehicle.eject();
            vehicle.remove();

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    passenger.teleport(railLocation);
                    Minecart minecart = to.getWorld().spawn(railLocation.add(0.5, 0.5, 0.5), Minecart.class);
                    minecart.addPassenger(passenger);
                    minecart.setVelocity(velocity);
                    railLocation.getWorld().playSound(minecart.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1);
                }
            }, 2);
        }
    }

    private static boolean isRail(Material mat) {
        return mat == Material.RAIL || mat == Material.POWERED_RAIL || mat == Material.DETECTOR_RAIL || mat == Material.ACTIVATOR_RAIL;
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
}