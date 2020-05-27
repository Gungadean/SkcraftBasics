package com.ryanjhuston;

import com.ryanjhuston.Commands.AdminCommands.WorldManagerCommand;
import com.ryanjhuston.Commands.ChatChannelCommands.HereCommand;
import com.ryanjhuston.Commands.ChatChannelCommands.JoinCommand;
import com.ryanjhuston.Commands.ChatChannelCommands.LeaveCommand;
import com.ryanjhuston.Commands.InviteCommand;
import com.ryanjhuston.Commands.NetherCoordsCommand;
import com.ryanjhuston.Commands.SkcraftBasicAdminCommand;
import com.ryanjhuston.Commands.SkcraftBasicCommand;
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
            if (command.equalsIgnoreCase("invite")) {
                InviteCommand.command(commandSender, args, plugin);
            }

            if (command.equalsIgnoreCase("accept") && commandSender instanceof Player) {
                AcceptCommand.command((Player) commandSender, args, plugin.skcraftPlayerList.get(((Player) commandSender).getUniqueId().toString()));
            }

            if (command.equalsIgnoreCase("paccept") && commandSender instanceof Player) {
                PermanentAcceptCommand.command((Player) commandSender, args, plugin.skcraftPlayerList.get(((Player) commandSender).getUniqueId().toString()), plugin);
            }

            if (command.equalsIgnoreCase("nethercoords") && commandSender instanceof Player) {
                NetherCoordsCommand.command((Player) commandSender);
            }

            if (command.equalsIgnoreCase("here") && commandSender instanceof Player) {
                HereCommand.command((Player) commandSender, args, plugin);
            }

            if (command.equalsIgnoreCase("join") && commandSender instanceof Player) {
                JoinCommand.command((Player) commandSender, args, plugin);
            }

            if (command.equalsIgnoreCase("leave") && commandSender instanceof Player) {
                LeaveCommand.command((Player) commandSender, args, plugin);
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
