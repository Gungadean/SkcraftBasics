package com.ryanjhuston.Commands;

import com.ryanjhuston.Commands.AdminCommands.AdminCommand;
import com.ryanjhuston.Commands.AdminCommands.ReloadCommand;
import com.ryanjhuston.Commands.AdminCommands.SetSpawnCommand;
import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkcraftBasicCommand {

    public static void command(SkcraftBasics plugin, CommandSender sender, String[] args) {
        if(sender instanceof Player) {
            if (!plugin.skcraftPlayerList.get(((Player) sender).getUniqueId().toString()).isAdmin()) {
                throw new CommandException("You do not have permission for this command.");
            }
        }

        if(args.length == 0 || (args[0].equalsIgnoreCase("help"))) {
            sender.sendMessage(ChatColor.GREEN + "Admin Command List:");
            sender.sendMessage(ChatColor.GREEN + "/sb admin {name} " + ChatColor.GRAY + "- Make or remove a player as an admin.");
            sender.sendMessage(ChatColor.GREEN + "/sb reload " + ChatColor.GRAY + "- Reloads config file.");
            sender.sendMessage(ChatColor.GREEN + "/sb setspawn " + ChatColor.GRAY + "- Set spawn for the server.");
            sender.sendMessage(ChatColor.GREEN + "/worldmanager" + ChatColor.GRAY + "- WorldManager basic command.");
            return;
        }

        if(args[0].equalsIgnoreCase("admin")) {
            AdminCommand.command(plugin, sender, args);
            return;
        }

        if(args[0].equalsIgnoreCase("reload")) {
            ReloadCommand.command(plugin, sender);
        }

        if(args[0].equalsIgnoreCase("setspawn")) {
            SetSpawnCommand.command(plugin, sender);
        }
    }
}