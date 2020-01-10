package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

public class InkSignModule implements Listener {

    private SkcraftBasics plugin;

    private HashMap<String, String[]> savedSigns = new HashMap<>();

    private boolean moduleEnabled;

    public InkSignModule(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.enabledModules.contains("InkSign");

        if(moduleEnabled) {
            plugin.logger.info("- InkSignModule Enabled");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if(plugin.interactCooldown.contains(event.getPlayer().getUniqueId().toString())) {
            return;
        }

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if(event.getPlayer().getInventory().getItemInMainHand().getType() != Material.INK_SAC) {
            return;
        }

        if(!event.getClickedBlock().getType().toString().endsWith("_SIGN")) {
            return;
        }

        Sign sign = (Sign)event.getClickedBlock().getState();
        String uuid = event.getPlayer().getUniqueId().toString();

        if(event.getPlayer().isSneaking()) {
            if(!savedSigns.containsKey(uuid)) {
                event.getPlayer().sendMessage(ChatColor.RED + "You do not have anything saved to your clipboard.");
                return;
            }

            String[] lines = savedSigns.get(uuid);

            sign.setLine(0, lines[0]);
            sign.setLine(1, lines[1]);
            sign.setLine(2, lines[2]);
            sign.setLine(3, lines[3]);

            sign.update();

            event.getPlayer().sendMessage(ChatColor.YELLOW + "This sign has been updated from your clipboard.");
        } else {
            if(!savedSigns.containsKey(uuid)) {
                savedSigns.put(uuid, sign.getLines());
            } else {
                savedSigns.replace(uuid, sign.getLines());
            }

            event.getPlayer().sendMessage(ChatColor.YELLOW + "This sign text has been saved to your clipboard.");
        }
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;
        moduleEnabled = plugin.enabledModules.contains("InkSign");

        if(moduleEnabled) {
            plugin.logger.info("- InkSignModule Enabled");
        }
    }
}
