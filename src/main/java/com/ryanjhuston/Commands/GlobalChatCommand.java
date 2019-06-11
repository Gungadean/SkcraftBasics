package com.ryanjhuston.Commands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GlobalChatCommand {

    public static void command(Player player, String[] args, SkcraftBasics plugin) {
        if(!plugin.chatChannelsModule.inChannelPlayers.containsKey(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "You must be in a chat channel to use this command.");
            return;
        }

        if(args.length == 0) {
            player.sendMessage(ChatColor.RED + "Correct Usage: /g {message}");
            return;
        }

        String message = "";

        for(String arg : args) {
            message += arg + " ";
        }

        Bukkit.broadcastMessage("<" + player.getDisplayName() + "> " + message);
    }
}
