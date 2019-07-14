package com.ryanjhuston.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand {

    public static void command(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.RED + "Information about commands and mechanics can be found on our wiki at: https://Gungadean.com/");
    }
}
