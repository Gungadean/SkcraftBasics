package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.SkcraftPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

/*
This module is a work in progress. Need to figure out why changing gamemode to adventure works but
doesn't have the typical characteristics of adventure (not being able to mine blocks). I may
return to this at some point because I at least think it's kinda an interesting concept.

Config Block:
  ProgressiveDeepslate-Module:
    OnlyLimitOres: false
    Usable-Picks:
      - DIAMOND_PICKAXE
      - NETHERITE_PICKAXE
 */

public class ProgressiveDeepslateModule implements Listener {

    private SkcraftBasics plugin;

    private boolean moduleEnabled;

    private boolean onlyLimitOres;

    private List<Material> usablePicks = new ArrayList<>();

    private static Set<Material> deepslateBlocks = new HashSet<>(Arrays.asList(Material.DEEPSLATE_COAL_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_EMERALD_ORE));

    public ProgressiveDeepslateModule (SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onStartBlockBreak(BlockDamageEvent event) {
        Player player = event.getPlayer();
        SkcraftPlayer skcraftPlayer = plugin.getSkcraftPlayer(player);

        if(player.getGameMode().equals(GameMode.CREATIVE)) {
            if(skcraftPlayer.getHasMiningFatigue()) {
                skcraftPlayer.setHasMiningFatigue(false);
            }
            return;
        }

        if(!deepslateBlocks.contains(event.getBlock().getType())) {
            if(skcraftPlayer.getHasMiningFatigue()) {
                removeMiningFatigue(player);
            }
            return;
        }

        if(usablePicks.contains(player.getInventory().getItemInMainHand().getType())) {
            if(skcraftPlayer.getHasMiningFatigue()) {
                removeMiningFatigue(player);
            }
            return;
        }

        addMiningFatigue(player);
    }

    @EventHandler
    public void onPlayerLook(PlayerInteractEvent event) {
        if(event.getAction() != Action.LEFT_CLICK_BLOCK) {
            if(event.getPlayer().getGameMode().equals(GameMode.ADVENTURE) && plugin.getSkcraftPlayer(event.getPlayer()).getHasMiningFatigue()) {
                removeMiningFatigue(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        if(deepslateBlocks.contains(event.getBlock().getType())) {
            event.getItems().addAll(event.getItems());
        }
    }

    public void addMiningFatigue(Player player) {
        plugin.getSkcraftPlayer(player).setHasMiningFatigue(true);
        player.setGameMode(GameMode.ADVENTURE);
    }

    public void removeMiningFatigue(Player player) {
        plugin.getSkcraftPlayer(player).setHasMiningFatigue(false);
        player.setGameMode(GameMode.SURVIVAL);
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.enabledModules.contains("ProgressiveDeepslate");

        onlyLimitOres = plugin.getConfig().getBoolean("Module-Settings.ProgressiveDeepslate-Module.OnlyLimitOres");

        if(onlyLimitOres) {
            deepslateBlocks.remove(Material.DEEPSLATE);
            deepslateBlocks.remove(Material.INFESTED_DEEPSLATE);
        } else {
            if(!deepslateBlocks.contains(Material.DEEPSLATE)) {
                deepslateBlocks.add(Material.DEEPSLATE);
                deepslateBlocks.add(Material.INFESTED_DEEPSLATE);
            }
        }

        List<String> usablePicksString = plugin.getConfig().getStringList("Module-Settings.ProgressiveDeepslate-Module.Usable-Picks");

        if(usablePicksString.isEmpty()) {
            moduleEnabled = false;
        } else {
            if(moduleEnabled) {
                for(String pick: usablePicksString) {
                    usablePicks.add(Material.getMaterial(pick));
                }

                if(!HandlerList.getHandlerLists().contains(plugin.progressiveDeepslateModule)) {
                    plugin.pm.registerEvents(plugin.progressiveDeepslateModule, plugin);
                }

                plugin.logger.info("- ProgressiveDeepslateModule Enabled");
            } else {
                HandlerList.unregisterAll(plugin.progressiveDeepslateModule);
            }
        }
    }
}