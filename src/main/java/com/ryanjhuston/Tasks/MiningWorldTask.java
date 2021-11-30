package com.ryanjhuston.Tasks;

import com.ryanjhuston.Modules.MiningWorldModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Date;

public class MiningWorldTask extends BukkitRunnable {
    private final MiningWorldModule miningWorldModule;

    public MiningWorldTask(MiningWorldModule miningWorldModule) {
        this.miningWorldModule = miningWorldModule;
    }

    @Override
    public void run() {
        Date now = new Date();

        Duration diff = Duration.between(now.toInstant(), miningWorldModule.getResetDate().toInstant());
        if((diff.toHours() == 1 || diff.toHours() == 0) && !diff.isNegative()) {
            long second = diff.getSeconds();
            if(second == (60 * 60)) {
                Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 1 hour.");
            } else if(second == (15 * 60)) {
                Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 15 minutes.");
            } else if(second == (10 * 60)) {
                Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 10 minutes.");
            } else if(second == (5 * 60)) {
                Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 5 minutes.");
            } else if(second == 60) {
                Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 1 minute.");
            } else if(second == 30) {
                Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 30 seconds.");
            } else if(second == 10) {
                Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 10 seconds.");
            } else if(second == 5) {
                Bukkit.broadcastMessage(ChatColor.RED + "The mining world will reset in 5 seconds.");
            } else if(second == 0) {
                miningWorldModule.resetWorld();
            }
        }
    }
}