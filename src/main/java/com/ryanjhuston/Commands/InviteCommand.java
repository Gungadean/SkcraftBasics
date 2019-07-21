package com.ryanjhuston.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InviteCommand {

    public static void command(CommandSender commandSender, String[] args) throws CommandException{

        if(args.length != 1) {
            throw new CommandException("Correct Usage: /invite {player-name}");
        }

        if(commandSender instanceof Player && commandSender.getName().equals(args[0])) {
            throw new CommandException("You cannot invite yourself.");
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if(target.isWhitelisted()) {
            throw new CommandException("This player is already invited.");
        }

        if(target != null) {
            target.setWhitelisted(true);
            commandSender.sendMessage(ChatColor.YELLOW + "Player has been successfully invited.");
            return;
        }

        throw new CommandException("Player was not found. Please check the spelling and try again.");
    }
}
