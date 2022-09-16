package com.ryanjhuston.Commands;

import com.ryanjhuston.Commands.AdminCommands.AdminCommand;
import com.ryanjhuston.Commands.AdminCommands.ReloadCommand;
import com.ryanjhuston.Commands.AdminCommands.SetSpawnCommand;
import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class SkcraftBasicAdminCommand {

    public static void command(SkcraftBasics plugin, CommandSender sender, String[] args) {
        if(sender instanceof Player) {
            if(!sender.hasPermission("skcraftbasics.admin")) {
                throw new CommandException("You do not have permission to use this command.");
            }
        }

        if(args.length == 0 || (args[0].equalsIgnoreCase("help"))) {
            sender.sendMessage(ChatColor.GREEN + "SkcraftBasics Admin Command List:");
            sender.sendMessage(ChatColor.GREEN + "/sba admin {name} " + ChatColor.GRAY + "- Make or remove a player as an admin.");
            sender.sendMessage(ChatColor.GREEN + "/sba reload " + ChatColor.GRAY + "- Reloads config file.");
            sender.sendMessage(ChatColor.GREEN + "/sba setspawn " + ChatColor.GRAY + "- Set spawn for the server.");
            sender.sendMessage(ChatColor.GREEN + "/sba miningreset " + ChatColor.GRAY + "- Forces Mining world reset.");
            sender.sendMessage(ChatColor.GREEN + "/sba spawn" + ChatColor.GRAY + "- Teleports you to spawn.");
            sender.sendMessage(ChatColor.GREEN + "/sba home" + ChatColor.GRAY + "- Teleports you home.");
            sender.sendMessage(ChatColor.GREEN + "/worldmanager" + ChatColor.GRAY + "- WorldManager basic command.");
            return;
        }

        if(args[0].equalsIgnoreCase("spawn")) {
            if(sender instanceof ConsoleCommandSender) {
                throw new CommandException("You must be a player to execute this command.");
            }

            Player player = (Player)sender;

            player.teleport(plugin.spawnLocation);
            player.sendMessage(ChatColor.YELLOW + "Teleported to spawn.");
            return;
        }

        if(args[0].equalsIgnoreCase("home")) {
            if(sender instanceof ConsoleCommandSender) {
                throw new CommandException("You must be a player to execute this command.");
            }

            Player player = (Player)sender;

            player.teleport(player.getBedSpawnLocation());
            player.sendMessage(ChatColor.YELLOW + "Teleported to your bed.");
            return;
        }

        if(args[0].equalsIgnoreCase("admin")) {
            AdminCommand.command(plugin, sender, args);
            return;
        }

        if(args[0].equalsIgnoreCase("reload")) {
            ReloadCommand.command(plugin, sender);
            return;
        }

        if(args[0].equalsIgnoreCase("setspawn")) {
            SetSpawnCommand.command(plugin, sender);
            return;
        }

        if(args[0].equalsIgnoreCase("miningreset")) {
            plugin.miningWorldModule.resetWorld();
            return;
        }
    }
}