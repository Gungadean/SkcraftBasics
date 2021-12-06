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

        if(plugin.getConfig().getBoolean("Chat-Colors")) {
            event.getPlayer().setDisplayName(ChatColorLib.getRandomColor() + event.getPlayer().getDisplayName() + ChatColor.WHITE);
        }

        if(!event.getPlayer().isWhitelisted()) {
            event.getPlayer().setWhitelisted(true);
        }

        if(!event.getPlayer().hasPlayedBefore()) {
            event.getPlayer().teleport(plugin.spawnLocation);
        }

        String uuid = event.getPlayer().getUniqueId().toString();
        File playerFileLegacy = new File(plugin.playersDir, uuid + ".yml");
        File playerFile = new File (plugin.playersDir, uuid + ".json");
        YamlConfiguration playerConfig = new YamlConfiguration();

        SkcraftPlayer skcraftPlayer;

        if(playerFile.exists()) {
            try {
                skcraftPlayer = plugin.mapper.readValue(playerFile, SkcraftPlayer.class);
            } catch (Exception e) {
                System.out.println("[SkcraftBasics] ERROR: It appears that " + event.getPlayer().getName() + "'s data file is corrupted. Creating new SkcraftPlayer instance.");
                skcraftPlayer = new SkcraftPlayer(uuid, null, false, false, new ArrayList<>(), new ArrayList<>(), false, false);
            }
        }else if(playerFileLegacy.exists()) {
            try {
                playerConfig.load(playerFileLegacy);
            } catch(Exception e) {
                e.printStackTrace();
            }

            skcraftPlayer = new SkcraftPlayer(uuid, Material.matchMaterial(playerConfig.getString("TeleportItem")), playerConfig.getBoolean("WasFlying"), false, playerConfig.getStringList("PermanentTeleAuthed"), playerConfig.getStringList("TeleAuthed"), (playerConfig.contains("InModeMode") && playerConfig.getBoolean("InModMode")), playerConfig.getBoolean("IsAdmin"));
            playerFileLegacy.delete();
        } else {
            try {
                playerFile.createNewFile();
                playerConfig.load(playerFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            skcraftPlayer = new SkcraftPlayer(uuid, null, false, false, new ArrayList<>(), new ArrayList<>(), false, false);
        }

        Material teleportItem = skcraftPlayer.getTeleportItem();
        if(teleportItem == null) {
            Random random = new Random();
            do {
                teleportItem = Material.values()[random.nextInt(Material.values().length - 1)];
            } while (teleportItem.toString().contains("Legacy") || teleportItem == Material.AIR || !teleportItem.isItem());
        }

        skcraftPlayer.setTeleportItem(teleportItem);

        plugin.addSkcraftPlayer(skcraftPlayer);

        if(plugin.getSkcraftPlayer(event.getPlayer()).getWasFlying()) {
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
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                InventoryHolder dispenser = (InventoryHolder)event.getBlock().getState();
                dispenser.getInventory().addItem(new ItemStack(Material.MINECART));
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

        if(!event.getBlockPlaced().getType().toString().endsWith("_SIGN")) {
            return;
        }

        Material newsign = Material.getMaterial(event.getBlockPlaced().getType().toString().replace("_WALL", ""));

        ItemStack sign = new ItemStack(newsign, 1);
        event.getPlayer().getInventory().setItem(event.getHand(), sign);
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

        if(!event.getItem().getItemStack().getType().toString().endsWith("_SIGN")) {
            return;
        }

        if(player.getInventory().contains(event.getItem().getItemStack().getType())) {
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
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.enabledModules.contains("EnderEyeChests")) {
            return;
        }

        if(event.getAction() == Action.PHYSICAL) {
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