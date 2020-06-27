package com.ryanjhuston;

import com.ryanjhuston.Lib.ChatColorLib;
import com.ryanjhuston.Types.SkcraftPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class SkcraftEventHandler implements Listener {

    private SkcraftBasics plugin;

    public SkcraftEventHandler(SkcraftBasics plugin) { this.plugin = plugin; }

    public void reload(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setDisplayName(ChatColorLib.getRandomColor() + event.getPlayer().getDisplayName() + ChatColor.WHITE);

        if(!event.getPlayer().isWhitelisted()) {
            event.getPlayer().setWhitelisted(true);
        }

        if(!event.getPlayer().hasPlayedBefore()) {
            event.getPlayer().teleport(plugin.spawnLocation);
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

            Material teleportItem;

            if(playerConfig.getString("TeleportItem") == null) {
                Random random = new Random();
                do {
                    teleportItem = Material.values()[random.nextInt(Material.values().length - 1)];
                } while (teleportItem.toString().contains("Legacy") || teleportItem == Material.AIR || !teleportItem.isItem());
            } else {
                teleportItem = Material.matchMaterial(playerConfig.getString("TeleportItem"));
            }

            plugin.skcraftPlayerList.put(uuid, new SkcraftPlayer(uuid, teleportItem, playerConfig.getBoolean("WasFlying"), playerConfig.getStringList("PermanentTeleAuthed"), playerConfig.getStringList("TeleAuthed"), playerConfig.getBoolean("IsAdmin"), playerConfig));
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
            } while (teleportItem.toString().contains("Legacy") || teleportItem == Material.AIR || !teleportItem.isItem());

            plugin.skcraftPlayerList.put(uuid, new SkcraftPlayer(uuid, teleportItem, false, new ArrayList<>(), new ArrayList<>(), false, playerConfig));
        }

        if(plugin.skcraftPlayerList.get(uuid).wasFlying()) {
            plugin.jetBootModule.activateJetboots(event.getPlayer());
            event.getPlayer().setFlying(true);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        plugin.savePlayerToFile(event.getPlayer());
        plugin.checkSleep();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if(event.getPlayer().getBedSpawnLocation() != null) {
            event.setRespawnLocation(event.getPlayer().getBedSpawnLocation());
            return;
        }

        event.setRespawnLocation(plugin.spawnLocation);
    }

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if(Bukkit.getWorlds().get(0).getTime() < 12545 && !Bukkit.getWorlds().get(0).hasStorm()) {
            return;
        }

       plugin.checkSleep();
    }

    @EventHandler
    public void onItemDispense(BlockDispenseEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!plugin.enabledModules.contains("InfiniteCarts")) {
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
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.isCancelled()) {
            return;
        }

        if (!plugin.enabledModules.contains("InfiniteSigns")) {
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
            slot = 40;
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

        if (!plugin.enabledModules.contains("InfiniteSigns")) {
            return;
        }

        if(!(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
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

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (!plugin.enabledModules.contains("DisableExplosions")) {
            return;
        }

        if(event.getEntity().getWorld().getName().equals("Mining")) {
            return;
        }

        event.blockList().clear();
    }

    @EventHandler
    public void onMobGrief(EntityChangeBlockEvent event) {
        if(event.isCancelled()) {
            return;
        }

        if (!plugin.enabledModules.contains("DisableExplosions")) {
            return;
        }

        if(event.getEntity().getWorld().getName().equals("Mining")) {
            return;
        }

        if(event.getEntityType() == EntityType.ENDERMAN ||
                event.getEntityType() == EntityType.RABBIT ||
                event.getEntityType() == EntityType.ZOMBIE ||
                event.getEntityType() == EntityType.ZOMBIE_VILLAGER) {
            event.setCancelled(true);
            return;
        }

        if(event.getEntity() instanceof ComplexEntityPart) {
            if(((ComplexEntityPart)event.getEntity()).getParent().getType() == EntityType.ENDER_DRAGON) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.enabledModules.contains("EnderEyeChests")) {
            return;
        }

        if(event.getHand().equals(EquipmentSlot.OFF_HAND) && event.getPlayer().getInventory().getItemInOffHand().getType() == Material.ENDER_EYE && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
            return;
        }

        if(plugin.interactCooldown.contains(event.getPlayer().getUniqueId().toString())) {
            return;
        }

        if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.ENDER_EYE && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.END_PORTAL_FRAME) {
                return;
            }

            plugin.removeInteractCooldown(event.getPlayer().getUniqueId().toString());

            event.getPlayer().openInventory(event.getPlayer().getEnderChest());
            event.setCancelled(true);
        }
    }
}