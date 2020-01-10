package com.ryanjhuston.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand {

    public static void command(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.GREEN + "Server Commands:");
        commandSender.sendMessage(ChatColor.GREEN + " /accept {player-name}" + ChatColor.GRAY + " - Allow a player to teleport to you once.");
        commandSender.sendMessage(ChatColor.GREEN + " /paccept {player-name}" + ChatColor.GRAY + " - Toggle a player being able to teleport to you indefinitely.");
        commandSender.sendMessage(ChatColor.GREEN + " /invite {player-name}" + ChatColor.GRAY + " - Invite a player to be whitelisted on the server.");
        commandSender.sendMessage(ChatColor.GREEN + " /join {channel-name}" + ChatColor.GRAY + " - Join a private chat channel.");
        commandSender.sendMessage(ChatColor.GREEN + " /leave {channel-name}" + ChatColor.GRAY + " - Leave a private chat channel.");
        commandSender.sendMessage(ChatColor.GREEN + " /here" + ChatColor.GRAY + " - List players who are currently in your chat channel.");
        commandSender.sendMessage(ChatColor.GREEN + " /g {message}" + ChatColor.GRAY + " - Send a message to the entire server without leaving the chat channel.");
        commandSender.sendMessage(ChatColor.GREEN + "Information about commands and mechanics can be found on our wiki at: http://gungadean.com/");
    }
}
