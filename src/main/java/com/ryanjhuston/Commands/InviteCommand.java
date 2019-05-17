package com.ryanjhuston.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InviteCommand {

    public static void command(CommandSender commandSender, String[] args) {

        if(args.length != 1) {
            commandSender.sendMessage(ChatColor.RED + "Correct Usage: /invite {player-name}");
            return;
        }

        if(commandSender instanceof Player && commandSender.getName().equals(args[0])) {
            commandSender.sendMessage(ChatColor.RED + "You cannot invite yourself.");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if(target.isWhitelisted()) {
            commandSender.sendMessage(ChatColor.RED + "This player is already invited.");
            return;
        }

        if(target != null) {
            target.setWhitelisted(true);
            commandSender.sendMessage(ChatColor.YELLOW + "Player has been successfully invited.");
            return;
        }

        commandSender.sendMessage(ChatColor.RED + "Player was not found. Please check the spelling and try again.");
    }
}
