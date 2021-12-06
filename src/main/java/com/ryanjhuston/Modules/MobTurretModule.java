package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Tasks.MobTurretTask;
import com.ryanjhuston.Types.EnderTurret;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class MobTurretModule implements Listener {

    private SkcraftBasics plugin;

    private List<EnderTurret> turretList = new ArrayList<>();
    public List<EntityType> attackableMobs = new ArrayList<>();

    public double turretRadius;
    public double turretAttackDamage;
    public int turretAttackInterval;

    private BukkitTask mobTurretTask;

    private boolean moduleEnabled;

    public MobTurretModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }
    

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if(!(event.getEntity() instanceof EnderCrystal)) {
            return;
        }

        if(!(event.getDamager() instanceof Player)) {
            return;
        }

        EnderTurret turret = turretFromLocation(event.getEntity().getLocation());

        if(turret == null) {
            return;
        }

        event.setCancelled(true);
        event.getEntity().remove();
        event.getEntity().getLocation().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.END_CRYSTAL, 1));

        turretList.remove(turret);

        event.getEntity().sendMessage(ChatColor.RED + "Ender Turret removed.");

        plugin.saveTurretsToFile();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() == Action.PHYSICAL) {
            return;
        }

        if(event.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }

        if(plugin.interactCooldown.contains(event.getPlayer().getUniqueId().toString())) {
            return;
        }

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if(event.getPlayer().getInventory().getItemInMainHand().getType() != Material.END_CRYSTAL) {
            return;
        }

        if(event.getClickedBlock().getType() != Material.OBSIDIAN) {
            return;
        }

        if(!checkIfClear(event.getClickedBlock().getLocation())) {
            return;
        }

        plugin.removeInteractCooldown(event.getPlayer().getUniqueId().toString());

        Location location = event.getClickedBlock().getRelative(0, 1, 0).getLocation();
        EnderCrystal enderCrystal = (EnderCrystal)event.getClickedBlock().getWorld().spawnEntity(new Location(location.getWorld(), (location.getX() + 0.5), location.getY(), (location.getZ() + 0.5)), EntityType.ENDER_CRYSTAL);

        enderCrystal.setShowingBottom(false);

        turretList.add(new EnderTurret(enderCrystal));

        event.getPlayer().getInventory().removeItem(new ItemStack(event.getPlayer().getInventory().getItemInMainHand().getType(), 1));
        event.setCancelled(true);

        event.getPlayer().sendMessage(ChatColor.YELLOW + "Ender Turret created.");

        plugin.saveTurretsToFile();
    }

    public boolean checkIfClear(Location location) {
        for(int x = -1; x < 2; x++) {
            for(int y = 1; y < 3; y++) {
                for(int z = -1; z < 2; z++) {
                    if(location.getBlock().getRelative(x, y, z).getType() != Material.AIR) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public EnderTurret turretFromLocation(Location location) {
        for(EnderTurret turret : turretList) {
            if(turret.getTurretLocation().equals(location)) {
                return turret;
            }
        }
        return null;
    }

    public List<EnderTurret> getTurretList() {
        return turretList;
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        turretRadius = plugin.getConfig().getDouble("Module-Settings.MobTurret-Module.Turret-Radius");
        turretAttackDamage = plugin.getConfig().getDouble("Module-Settings.MobTurret-Module.Turret-Attack-Damage");
        turretAttackInterval = plugin.getConfig().getInt("Module-Settings.MobTurret-Module.Turret-Attack-Interval");

        List<String> attackableMobsString = plugin.getConfig().getStringList("Module-Settings.MobTurret-Module.Attackable-Mobs");
        
        for(String attackableMobName : attackableMobsString) {
            attackableMobs.add(EntityType.valueOf(attackableMobName));
        }

        moduleEnabled = plugin.enabledModules.contains("MobTurret");

        if(moduleEnabled) {
            if(!HandlerList.getHandlerLists().contains(plugin.mobTurretModule)) {
                plugin.pm.registerEvents(plugin.mobTurretModule, plugin);
            }

            if(mobTurretTask != null) {
                mobTurretTask.cancel();
            }

            mobTurretTask = new MobTurretTask(this, plugin).runTaskTimer(plugin, 0, turretAttackInterval);

            plugin.logger.info("- MobTurretModule Enabled");
        } else {
            HandlerList.unregisterAll(plugin.mobTurretModule);

            if(mobTurretTask != null) {
                mobTurretTask.cancel();
            }
        }
    }
}
