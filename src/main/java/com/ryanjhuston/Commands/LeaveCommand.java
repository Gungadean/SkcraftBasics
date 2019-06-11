package com.ryanjhuston.Commands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LeaveCommand {

    public static void command(Player player, String[] args, SkcraftBasics plugin) {
        if(args.length != 0) {
            player.sendMessage(ChatColor.RED + "Correct Usage: /leave");
            return;
        }

        if(!plugin.chatChannelsModule.inChannelPlayers.containsKey(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "You are not currently in a chat channel.");
            return;
        }

        plugin.chatChannelsModule.leaveChatChannel(player.getUniqueId().toString());
        player.sendMessage(ChatColor.YELLOW + "You have successfully left the channel.");
    }
}
