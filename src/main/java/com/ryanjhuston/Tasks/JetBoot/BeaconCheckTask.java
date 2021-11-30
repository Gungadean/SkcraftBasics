package com.ryanjhuston.Tasks.JetBoot;

import com.ryanjhuston.Modules.JetBootModule;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BeaconCheckTask extends BukkitRunnable {

    private final JetBootModule jetBootModule;

    public BeaconCheckTask(JetBootModule jetBootModule) {
        this.jetBootModule = jetBootModule;
    }

    @Override
    public void run() {
        List<String> playerRemoval = new ArrayList<>();

        for(Player player : Bukkit.getOnlinePlayers()) {
            String uuid = player.getUniqueId().toString();

            if(!jetBootModule.checkBeaconList(uuid)) {
                if(jetBootModule.jetboots.contains(uuid)) {
                    playerRemoval.add(uuid);
                }
            } else {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    continue;
                }

                if (player.getInventory().getBoots() == null) {
                    jetBootModule.deactivateJetboots(player);
                    continue;
                }

                if (!player.getInventory().getBoots().hasItemMeta()) {
                    jetBootModule.deactivateJetboots(player);
                    continue;
                }

                if (!player.getInventory().getBoots().getItemMeta().hasLore()) {
                    jetBootModule.deactivateJetboots(player);
                    continue;
                }

                if (!player.getInventory().getBoots().getItemMeta().getLore().contains("Jetboots")) {
                    jetBootModule.deactivateJetboots(player);
                    continue;
                }

                if(((Damageable)player.getInventory().getBoots().getItemMeta()).getDamage() >= (player.getInventory().getBoots().getType().getMaxDurability()-1)) {
                    jetBootModule.deactivateJetboots(player);
                    continue;
                }

                jetBootModule.activateJetboots(player);
            }
        }

        for(String uuid : playerRemoval) {
            jetBootModule.deactivateJetboots(Bukkit.getPlayer(UUID.fromString(uuid)));
        }
    }
}