package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

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
    public void onVehicleMove(VehicleMoveEvent event) {
        if(event.getTo().getBlock().getType() != Material.NETHER_PORTAL) {
            return;
        }

        if(event.getVehicle().getType() != EntityType.MINECART) {
            return;
        }

        if(event.getVehicle().getPassengers().isEmpty()) {
            return;
        }

        if(!(event.getVehicle().getPassengers().get(0) instanceof Player)) {
            return;
        }

        Player player = (Player)event.getVehicle().getPassengers().get(0);

        if(players.contains(player.getUniqueId().toString())) {
            return;
        }

        Location otherWorld;

        if(player.getWorld().getName().equals("world")) {
            otherWorld = new Location(Bukkit.getWorld("world_nether"), (player.getLocation().getX()/8), 40, (player.getLocation().getZ()/8));
        } else {
            otherWorld = new Location(Bukkit.getWorld("world"), (player.getLocation().getX()*8), 40, (player.getLocation().getZ()*8));
        }

        List<Chunk> chunks = new ArrayList<>();

        for(int x = -8; x <= 8; x++) {
            for(int z = -8; z <= 8; z++) {
                chunks.add(otherWorld.getWorld().getChunkAt((otherWorld.getChunk().getX()+x), (otherWorld.getChunk().getZ()+z)));
            }
        }

        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                Location toLocation = event.getTo();

                for(Chunk chunk : chunks) {
                    for(int x = 0; x < 16; x++) {
                        for(int y = 0; y < 256; y++) {
                            for (int z = 0; z < 16; z++) {
                                if(chunk.getBlock(x, y, z).getType() == Material.NETHER_PORTAL && chunk.getBlock(x, (y-1), z).getType() == Material.OBSIDIAN) {
                                    toLocation = chunk.getBlock(x, y, z).getLocation();
                                    break;
                                }
                            }
                        }
                    }
                }

                if(toLocation.equals(event.getTo())) {
                    return;
                }

                players.add(player.getUniqueId().toString());

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        players.remove(player.getUniqueId().toString());
                    }
                }, 60);

                teleportThroughPortal(event.getVehicle(), toLocation);
            }
        });
    }

    public Location findAdjacentRail(Location base) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    Location test = base.clone().add(x, y, z);
                    if (isRail(test.getBlock().getType())) {
                        return test;
                    }
                }
            }
        }
        return null;
    }

    public void teleportThroughPortal(Vehicle vehicle, Location to) {
        Entity passenger = vehicle.getPassengers().get(0);

        Location railLocation = findAdjacentRail(to);

        if (railLocation != null) {
            Vector direction = railLocation.toVector().subtract(to.toVector());
            Vector velocity = direction.multiply(5);
            vehicle.eject();
            vehicle.remove();

            passenger.teleport(railLocation);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
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
}