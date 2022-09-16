package com.ryanjhuston;

import com.ryanjhuston.Commands.*;
import com.ryanjhuston.Commands.AdminCommands.WorldManagerCommand;
import com.ryanjhuston.Commands.ChatChannelCommands.HereCommand;
import com.ryanjhuston.Commands.ChatChannelCommands.JoinCommand;
import com.ryanjhuston.Commands.ChatChannelCommands.LeaveCommand;
import com.ryanjhuston.Commands.TeleportCommands.AcceptCommand;
import com.ryanjhuston.Commands.TeleportCommands.PermanentAcceptCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkcraftCommandHandler implements CommandExecutor {

    private SkcraftBasics plugin;

    public SkcraftCommandHandler (SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender commandSender, Command cmd, String command, String[] args) {

        if(plugin.disabledCommands.contains(command.toLowerCase())) {
            return false;
        }

        try {
            if (command.equalsIgnoreCase("invite") && !plugin.disabledCommands.contains("invite")) {
                InviteCommand.command(commandSender, args, plugin);
            }

            if (command.equalsIgnoreCase("accept") && commandSender instanceof Player && !plugin.disabledCommands.contains("accept")) {
                AcceptCommand.command((Player) commandSender, args, plugin.getSkcraftPlayer((Player)commandSender));
            }

            if (command.equalsIgnoreCase("paccept") && commandSender instanceof Player && !plugin.disabledCommands.contains("paccept")) {
                PermanentAcceptCommand.command((Player) commandSender, args, plugin.getSkcraftPlayer((Player)commandSender), plugin);
            }

            if (command.equalsIgnoreCase("nethercoords") && commandSender instanceof Player && !plugin.disabledCommands.contains("nethercoords")) {
                NetherCoordsCommand.command((Player) commandSender);
            }

            if (command.equalsIgnoreCase("here") && commandSender instanceof Player && !plugin.disabledCommands.contains("here")) {
                HereCommand.command((Player) commandSender, args, plugin);
            }

            if (command.equalsIgnoreCase("join") && commandSender instanceof Player && !plugin.disabledCommands.contains("join")) {
                JoinCommand.command((Player) commandSender, args, plugin);
            }

            if (command.equalsIgnoreCase("leave") && commandSender instanceof Player && !plugin.disabledCommands.contains("leave")) {
                LeaveCommand.command((Player) commandSender, args, plugin);
            }

            if (command.equalsIgnoreCase("mod") && commandSender instanceof Player && !plugin.disabledCommands.contains("mod")) {
                ModCommand.command((Player) commandSender, plugin);
            }

            if (command.equalsIgnoreCase("worldmanager") || command.equalsIgnoreCase("wm")) {
                WorldManagerCommand.command(plugin, commandSender, args);
            }

            if (command.equalsIgnoreCase("sb")) {
                SkcraftBasicCommand.command(plugin, commandSender, args);
            }

            if (command.equalsIgnoreCase("sba")) {
                SkcraftBasicAdminCommand.command(plugin, commandSender, args);
            }
        } catch(CommandException e) {
            commandSender.sendMessage(ChatColor.RED + e.getMessage());
        }
        return false;
    }
}
