package com.ryanjhuston.Commands.AdminCommands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

public class ReloadCommand {

    public static void command(SkcraftBasics plugin, CommandSender sender) {
        if(!sender.hasPermission("skcraftbasics.admin")) {
            throw new CommandException("You do not have permission to use this command.");
        }

        plugin.reloadPlugin();
        sender.sendMessage(ChatColor.YELLOW + "Plugin configs have been successfully reloaded.");
    }
}