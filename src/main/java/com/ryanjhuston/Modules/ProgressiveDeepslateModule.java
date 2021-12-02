package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class ProgressiveDeepslateModule implements Listener {

    private SkcraftBasics plugin;

    private boolean moduleEnabled;

    private List<Material> usablePicks = new ArrayList<>();

    private static Set<Material> deepslateOres = new HashSet<>(Arrays.asList(Material.DEEPSLATE_COAL_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_EMERALD_ORE));

    public void ProgressiveDeepslateModule (SkcraftBasics plugin) {
        updateConfig(plugin);
    }

    @EventHandler
    public void onStartBlockBreak(BlockDamageEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if(!deepslateOres.contains(event.getBlock().getType())) {
            return;
        }

        if(usablePicks.contains(event.getPlayer().getItemInUse().getType())) {
            return;
        }

        event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(-1);
    }

    @EventHandler
    public void onPlayerLook(PlayerInteractEvent event) {
        if(event.getAction() != Action.LEFT_CLICK_BLOCK) {
            if(event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).getValue() == -1) {
                event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1);
            }
            return;
        }

        if(!deepslateOres.contains(event.getClickedBlock().getType())) {
            if(event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).getValue() == -1) {
                event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1);
            }
            return;
        }

        if(event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).getValue() != -1) {
            event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(-1);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if(!deepslateOres.contains(event.getBlock().getType())) {
            return;
        }

        if(!usablePicks.contains(event.getPlayer().getItemInUse().getType())) {
            event.getBlock().getDrops().clear();
            return;
        }

        //TO-DO: add mining fatigue to make it impossible to mine these blocks without the correct tool.

        event.getBlock().getDrops().addAll(event.getBlock().getDrops());
    }

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        event.getItems().addAll(event.getItems());
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.enabledModules.contains("ProgressiveDeepslate");

        List<String> usablePicksString = plugin.getConfig().getStringList("Module-Settings.ProgressiveDeepslate-Module.Usable-Picks");

        if(usablePicksString.isEmpty()) {
            moduleEnabled = false;
        } else {
            if(moduleEnabled) {
                plugin.logger.info("- ProgressiveDeepslateModule Enabled");
            }
        }
    }
}