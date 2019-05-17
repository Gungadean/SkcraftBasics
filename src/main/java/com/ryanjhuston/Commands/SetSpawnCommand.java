package com.ryanjhuston.Commands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand {

    public static void command(CommandSender commandSender, SkcraftBasics plugin) {
        if(!commandSender.isOp()) {
            commandSender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        Location loc = ((Player) commandSender).getLocation();
        plugin.getConfig().set("Spawn-Location", loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "," + loc.getYaw() + "," + loc.getPitch());
        commandSender.sendMessage(ChatColor.YELLOW + "Spawn location set.");
    }
}
