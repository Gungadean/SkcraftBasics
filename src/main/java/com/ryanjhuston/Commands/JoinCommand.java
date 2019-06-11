package com.ryanjhuston.Commands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class JoinCommand {

    public static void command(Player player, String[] args, SkcraftBasics plugin) {
        if(args.length == 0) {
            player.sendMessage(ChatColor.RED + "Correct Usage: /join {channel}");
            return;
        }

        if(args.length > 1) {
            player.sendMessage(ChatColor.RED + "Correct Usage: /join {channel}");
            return;
        }

        if(plugin.chatChannelsModule.inChannelPlayers.containsKey(player.getUniqueId().toString())) {
            if(plugin.chatChannelsModule.inChannelPlayers.get(player.getUniqueId().toString()).equals(args[0])) {
                player.sendMessage(ChatColor.RED + "You are already a member of this channel.");
                return;
            }
        }

        plugin.chatChannelsModule.joinChatChannel(player.getUniqueId().toString(), args[0]);
        player.sendMessage(ChatColor.YELLOW + "You have successfully joined the chat channel " + args[0] + ".");
    }
}
