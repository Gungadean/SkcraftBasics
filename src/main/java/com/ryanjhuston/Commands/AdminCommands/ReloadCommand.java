package com.ryanjhuston.Commands.AdminCommands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadCommand {

    public static void command(SkcraftBasics plugin, CommandSender sender) {
        plugin.reloadPlugin();
        sender.sendMessage(ChatColor.GOLD + "Plugin configs have been successfully reloaded.");
    }
}