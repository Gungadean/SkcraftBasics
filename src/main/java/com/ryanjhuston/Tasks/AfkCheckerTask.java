package com.ryanjhuston.Tasks;

import com.ryanjhuston.Modules.AfkModule;
import com.ryanjhuston.Types.AfkTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AfkCheckerTask extends BukkitRunnable {

    private final AfkModule afkModule;

    public AfkCheckerTask(AfkModule afkModule) {
        this.afkModule = afkModule;
    }

    @Override
    public void run() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            AfkTracker afkTracker = afkModule.getPlayerTracker().get(player.getUniqueId().toString());

            if(afkModule.getAfkPlayers().contains(player.getUniqueId().toString())) {
                if(!afkTracker.getStartLocation().equals(player.getLocation())) {
                    afkModule.removeAfk(player);
                }
                continue;
            }

            if(player.getLocation().equals(afkTracker.getStartLocation())) {
                afkTracker.setAfkTime(afkTracker.getAfkTime() + 5);
            } else {
                afkTracker.setStartLocation(player.getLocation());
                afkTracker.setAfkTime(0);
            }

            if(afkTracker.getAfkTime() >= afkModule.getAfkTime()) {
                afkModule.getAfkPlayers().add(player.getUniqueId().toString());
                player.setPlayerListName(ChatColor.GRAY + "[AFK] " + player.getName());
            }
        }
    }
}