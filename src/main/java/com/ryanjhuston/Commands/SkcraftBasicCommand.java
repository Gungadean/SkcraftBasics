package com.ryanjhuston.Commands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkcraftBasicCommand {

    public static void command(SkcraftBasics plugin, CommandSender sender, String[] args) {
        if(sender instanceof Player) {
            if (!plugin.getSkcraftPlayer((Player)sender).getIsAdmin()) {
                throw new CommandException("You do not have permission for this command.");
            }
        }

        if(args.length == 0 || (args[0].equalsIgnoreCase("help"))) {
            sender.sendMessage(ChatColor.GREEN + "SkcraftBasics Commands:");
            sender.sendMessage(ChatColor.GREEN + " /accept {player-name}" + ChatColor.GRAY + " - Allow a player to teleport to you once.");
            sender.sendMessage(ChatColor.GREEN + " /paccept {player-name}" + ChatColor.GRAY + " - Toggle a player being able to teleport to you indefinitely.");
            sender.sendMessage(ChatColor.GREEN + " /invite {player-name}" + ChatColor.GRAY + " - Invite a player to be whitelisted on the server.");
            sender.sendMessage(ChatColor.GREEN + " /join {channel-name}" + ChatColor.GRAY + " - Join a private chat channel.");
            sender.sendMessage(ChatColor.GREEN + " /leave {channel-name}" + ChatColor.GRAY + " - Leave a private chat channel.");
            sender.sendMessage(ChatColor.GREEN + " /here" + ChatColor.GRAY + " - List players who are currently in your chat channel.");
            sender.sendMessage(ChatColor.GREEN + "Information about commands and mechanics can be found on our wiki at: https://skcraft.com/wiki/vincent/");
            return;
        }
    }
}
