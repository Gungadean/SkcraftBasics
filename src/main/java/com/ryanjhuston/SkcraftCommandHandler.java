package com.ryanjhuston;

import com.ryanjhuston.Commands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkcraftCommandHandler implements CommandExecutor {

    private SkcraftBasics plugin;

    public SkcraftCommandHandler (SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender commandSender, Command cmd, String command, String[] args) {

        if(command.equalsIgnoreCase("invite")) {
            InviteCommand.command(commandSender, args);
        }

        if(command.equalsIgnoreCase("accept") && commandSender instanceof Player) {
            AcceptCommand.command((Player)commandSender, args, plugin.skcraftPlayerList.get(((Player) commandSender).getUniqueId().toString()));
        }

        if(command.equalsIgnoreCase("paccept") && commandSender instanceof  Player) {
            PermanentAcceptCommand.command((Player)commandSender, args, plugin.skcraftPlayerList.get(((Player) commandSender).getUniqueId().toString()));
        }

        if(command.equalsIgnoreCase("setspawn") && commandSender instanceof Player) {
            SetSpawnCommand.command(commandSender, plugin);
        }

        if(command.equalsIgnoreCase("nethercoords") && commandSender instanceof Player) {
            NetherCoordsCommand.command((Player)commandSender);
        }

        if(command.equalsIgnoreCase("here") && commandSender instanceof  Player) {
            HereCommand.command((Player)commandSender, args, plugin);
        }

        if(command.equalsIgnoreCase("join") && commandSender instanceof Player) {
            JoinCommand.command((Player)commandSender, args, plugin);
        }

        if(command.equalsIgnoreCase("leave") && commandSender instanceof Player) {
            LeaveCommand.command((Player)commandSender, args, plugin);
        }

        if(command.equalsIgnoreCase("g") && commandSender instanceof Player) {
            GlobalChatCommand.command((Player)commandSender, args, plugin);
        }
        return false;
    }
}
