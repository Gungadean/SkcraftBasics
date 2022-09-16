package com.ryanjhuston.Commands.AdminCommands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand {

    public static void command(SkcraftBasics plugin, CommandSender commandSender) throws CommandException {
        if(commandSender instanceof ConsoleCommandSender) {
            throw new CommandException("This command cannot be executed from the console.");
        }

        if(!commandSender.hasPermission("skcraftbasics.admin")) {
            throw new CommandException("You do not have permission to use this command.");
        }

        plugin.spawnLocation = ((Player) commandSender).getLocation();
        ((Player) commandSender).getWorld().setSpawnLocation(((Player) commandSender).getLocation());
        commandSender.sendMessage(ChatColor.YELLOW + "Spawn location set.");
    }
}