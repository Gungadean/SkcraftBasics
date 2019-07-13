package com.ryanjhuston;

import com.ryanjhuston.Events.PlayerEnterStargateEvent;
import com.ryanjhuston.Lib.ChatColorLib;
import com.ryanjhuston.Types.SkcraftPlayer;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class SkcraftEventHandler implements Listener {

    private SkcraftBasics plugin;

    public SkcraftEventHandler(SkcraftBasics plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setDisplayName(ChatColorLib.getRandomColor() + event.getPlayer().getDisplayName() + ChatColor.WHITE);

        if(!event.getPlayer().isWhitelisted()) {
            event.getPlayer().setWhitelisted(true);
        }

        if(!event.getPlayer().hasPlayedBefore()) {
            String[] location = plugin.getConfig().getString("Spawn-Location").split(",");
            event.getPlayer().teleport(new Location(Bukkit.getWorld(location[0]), Double.valueOf(location[1]), Double.valueOf(location[2]), Double.valueOf(location[3]), Float.valueOf(location[4]), Float.valueOf(location[5])));
        }

        String uuid = event.getPlayer().getUniqueId().toString();
        File playerFile = new File(plugin.playersDir, uuid + ".yml");
        YamlConfiguration playerConfig = new YamlConfiguration();

        if(playerFile.exists()) {
            try {
                playerConfig.load(playerFile);
            } catch(Exception e) {
                e.printStackTrace();
            }

            plugin.skcraftPlayerList.put(uuid, new SkcraftPlayer(uuid, Material.matchMaterial(playerConfig.getString("TeleportItem")), playerConfig.getBoolean("WasFlying"), playerConfig.getStringList("PermanentTeleAuthed"), playerConfig.getStringList("TeleAuthed"), playerConfig));
        } else {
            try {
                playerFile.createNewFile();
                playerConfig.load(playerFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Material teleportItem;
            Random random = new Random();
            do {
                teleportItem = Material.values()[random.nextInt(Material.values().length - 1)];
            } while (teleportItem.toString().contains("Legacy") || teleportItem == Material.AIR);

            plugin.skcraftPlayerList.put(uuid, new SkcraftPlayer(uuid, teleportItem, false, new ArrayList<>(), new ArrayList<>(), playerConfig));
        }

        if(plugin.skcraftPlayerList.get(uuid).wasFlying()) {
            plugin.jetBootModule.activateJetboots(event.getPlayer());
            event.getPlayer().setFlying(true);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        SkcraftPlayer skcraftPlayer = plugin.skcraftPlayerList.get(uuid);

        skcraftPlayer.getConfig().set("TeleportItem" , skcraftPlayer.getTeleportItem().toString());
        skcraftPlayer.getConfig().set("WasFlying", event.getPlayer().isFlying());
        skcraftPlayer.getConfig().set("PermanentTeleAuthed", skcraftPlayer.getPTeleAuthed());
        skcraftPlayer.getConfig().set("TeleAuthed", skcraftPlayer.getTeleAuthed());

        File playerFile = new File(plugin.playersDir, uuid + ".yml");

        try {
            skcraftPlayer.getConfig().save(playerFile);
        }catch (IOException e){
            e.printStackTrace();
        }

        plugin.skcraftPlayerList.remove(uuid);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if(event.getPlayer().getBedSpawnLocation() != null) {
            event.setRespawnLocation(event.getPlayer().getBedSpawnLocation());
            return;
        }

        String[] location = plugin.getConfig().getString("Spawn-Location").split(",");
        event.setRespawnLocation(new Location(Bukkit.getWorld(location[0]), Double.valueOf(location[1]), Double.valueOf(location[2]), Double.valueOf(location[3]), Float.valueOf(location[4]), Float.valueOf(location[5])));
    }


    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if(Bukkit.getWorlds().get(0).getTime() < 12545 && !Bukkit.getWorlds().get(0).hasStorm()) {
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                int sleeping = 0;
                Iterator it = Bukkit.getOnlinePlayers().iterator();

                while(it.hasNext())
                {
                    Player player = (Player)it.next();
                    if(player.isSleeping() || plugin.afkModule.getAfkPlayers().contains(player.getUniqueId().toString()) || player.isSleepingIgnored() || !player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                        sleeping++;
                    }
                }

                int percent = (sleeping*100)/Bukkit.getOnlinePlayers().size();

                if(percent >= 50) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                        @Override
                        public void run() {
                            Iterator it = Bukkit.getOnlinePlayers().iterator();

                            while(it.hasNext()) {
                                Player player = (Player)it.next();
                                player.setStatistic(Statistic.TIME_SINCE_REST, 0);
                            }

                            Bukkit.getWorlds().get(0).setTime(1000);
                            Bukkit.getWorlds().get(0).setStorm(false);
                        }
                    }, 20);
                }
            }
        }, 1);
    }

    @EventHandler
    public void onItemDispense(BlockDispenseEvent event) {
        if (event.isCancelled()) {
            return;
        }

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
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }

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
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.isCancelled()) {
            return;
        }

        if(event.getBlock().getType() != Material.OAK_WALL_SIGN && event.getBlock().getType() != Material.OAK_SIGN) {
            return;
        }

        Player player = event.getPlayer();
        int slot;

        if(player.getInventory().getItemInMainHand().getType() == Material.OAK_SIGN || player.getInventory().getItemInMainHand().getType() == Material.OAK_WALL_SIGN) {
            slot = player.getInventory().getHeldItemSlot();
        } else if(player.getInventory().getItemInOffHand().getType() == Material.OAK_SIGN || player.getInventory().getItemInOffHand().getType() == Material.OAK_WALL_SIGN) {
            slot = -106;
        } else {
            return;
        }

        ItemStack sign = new ItemStack(Material.OAK_SIGN, 1);
        event.getPlayer().getInventory().setItem(slot, sign);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if(!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player)event.getEntity();

        if(event.getItem().getItemStack().getType() != Material.OAK_SIGN) {
            return;
        }

        if(player.getInventory().contains(Material.OAK_SIGN)) {
            event.getItem().remove();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Random rand = new Random();

        if(event.toWeatherState()) {
            if(rand.nextInt(2) != 0) {
                event.setCancelled(true);
                return;
            }
        }
    }
}