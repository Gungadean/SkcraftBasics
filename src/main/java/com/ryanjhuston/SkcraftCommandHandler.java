package com.ryanjhuston;

import com.ryanjhuston.Commands.AcceptCommand;
import com.ryanjhuston.Commands.InviteCommand;
import com.ryanjhuston.Commands.SetSpawnCommand;
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
            AcceptCommand.command((Player)commandSender, args, plugin.teleportAuth);
        }

        if(command.equalsIgnoreCase("setspawn") && commandSender instanceof Player) {
            SetSpawnCommand.command(commandSender, plugin);
        }
        return false;
    }
}
