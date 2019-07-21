package com.ryanjhuston.Commands.ChatChannelCommands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

public class GlobalChatCommand {

    public static void command(Player player, String[] args, SkcraftBasics plugin) throws CommandException {
        if(!plugin.chatChannelsModule.inChannelPlayers.containsKey(player.getUniqueId().toString())) {
            throw new CommandException("You must be in a chat channel to use this command.");
        }

        if(args.length == 0) {
            throw new CommandException("Correct Usage: /g {message}");
        }

        String message = "";

        for(String arg : args) {
            message += arg + " ";
        }

        Bukkit.broadcastMessage("<" + player.getDisplayName() + "> " + message);
    }
}
