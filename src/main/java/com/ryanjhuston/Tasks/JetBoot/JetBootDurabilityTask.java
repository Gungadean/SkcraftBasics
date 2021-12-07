package com.ryanjhuston.Tasks.JetBoot;

import com.ryanjhuston.Modules.JetBootModule;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JetBootDurabilityTask extends BukkitRunnable {

    private final JetBootModule jetBootModule;

    public JetBootDurabilityTask(JetBootModule jetBootModule) {
        this.jetBootModule = jetBootModule;
    }

    @Override
    public void run() {
        List<Player> forRemoval = new ArrayList<>();

        for(String uuid : jetBootModule.jetboots) {
            Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            if(player != null) {
                if(player.getGameMode() != GameMode.CREATIVE) {
                    if (player.isFlying()) {
                        if(!jetBootModule.flyTime.containsKey(player.getUniqueId().toString())) {
                            jetBootModule.flyTime.put(player.getUniqueId().toString(), 1);
                        }

                        int currentFlyTime = jetBootModule.flyTime.get(player.getUniqueId().toString());

                        if(currentFlyTime >= jetBootModule.durabilityFreq) {
                            boolean remove = jetBootModule.updateDurability(player);
                            if(remove) {
                                forRemoval.add(player);
                            }

                            jetBootModule.flyTime.replace(player.getUniqueId().toString(), 1);
                        } else {
                            jetBootModule.flyTime.replace(player.getUniqueId().toString(), (currentFlyTime + 1));
                        }
                    }
                }
            }
        }

        for(Player player : forRemoval) {
            jetBootModule.deactivateJetboots(player);
        }
    }
}