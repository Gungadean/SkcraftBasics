package com.ryanjhuston.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand {

    public static void command(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.GREEN + "Information about commands and mechanics can be found on our wiki at: http://gungadean.com/");
    }
}
