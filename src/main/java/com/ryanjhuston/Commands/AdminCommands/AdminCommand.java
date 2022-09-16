package com.ryanjhuston.Commands.AdminCommands;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.SkcraftPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand {

    public static void command(SkcraftBasics plugin, CommandSender commandSender, String[] args) {
        if(commandSender instanceof Player) {
            if(!commandSender.hasPermission("skcraftbasics.admin")) {
                throw new CommandException("You do not have permission for this command.");
            }
        }

        if(args.length == 1) {
            throw new CommandException("You must specify a name.");
        }

        if(args.length > 2) {
            throw new CommandException("Too many args.");
        }

        Player target = Bukkit.getPlayer(args[1]);

        if(target == null) {
            throw new CommandException("The player must be online to make them an admin.");
        }

        SkcraftPlayer skcraftPlayer = plugin.getSkcraftPlayer(target);
        if(!skcraftPlayer.getIsAdmin()) {
            skcraftPlayer.setIsAdmin(true);
            commandSender.sendMessage(ChatColor.YELLOW + target.getName() + " has been made an admin.");
            target.sendMessage(ChatColor.YELLOW + "You are now an admin.");
        } else {
            if(commandSender instanceof Player) {
                if(((Player)commandSender).getUniqueId().toString().equals(target.getUniqueId().toString())) {
                    throw new CommandException("You cannot remove yourself as an admin.");
                }
            }

            skcraftPlayer.setIsAdmin(false);
            commandSender.sendMessage(ChatColor.RED + target.getName() + " has been removed as an admin.");
            target.sendMessage(ChatColor.RED + "You are no longer an admin.");
        }
    }
}
