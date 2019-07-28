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
            if(!commandSender.isOp()) {
                throw new CommandException("You do not have permission for this command.");
            }
        }

        if(args.length == 0) {
            throw new CommandException("You must specify a name.");
        }

        if(args.length > 1) {
            throw new CommandException("Too many args.");
        }

        Player target = Bukkit.getPlayer(args[0]);

        if(target == null) {
            throw new CommandException("The player must be online to make them an admin.");
        }

        SkcraftPlayer skcraftPlayer = plugin.skcraftPlayerList.get(target.getUniqueId().toString());
        if(!skcraftPlayer.isAdmin()) {
            skcraftPlayer.setIsAdmin(true);
            commandSender.sendMessage(ChatColor.GOLD + target.getName() + " has been made an admin.");
            target.sendMessage(ChatColor.GOLD + "You are now an admin.");
        } else {
            skcraftPlayer.setIsAdmin(false);
            commandSender.sendMessage(ChatColor.GOLD + target.getName() + " has been removed as an admin.");
            target.sendMessage(ChatColor.GOLD + "You are no longer an admin.");
        }
    }
}
