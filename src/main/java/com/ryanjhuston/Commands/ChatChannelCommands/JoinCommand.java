package com.ryanjhuston.Commands.ChatChannelCommands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

public class JoinCommand {

    public static void command(Player player, String[] args, SkcraftBasics plugin) throws CommandException {
        if(args.length == 0) {
            throw new CommandException("Correct Usage: /join {channel}");
        }

        if(args.length > 1) {
            throw new CommandException("Correct Usage: /join {channel}");
        }

        if(plugin.chatChannelsModule.inChannelPlayers.containsKey(player.getUniqueId().toString())) {
            if(plugin.chatChannelsModule.inChannelPlayers.get(player.getUniqueId().toString()).equals(args[0])) {
                throw new CommandException("You are already a member of this channel.");
            }
        }

        plugin.chatChannelsModule.joinChatChannel(player.getUniqueId().toString(), args[0]);
        player.sendMessage(ChatColor.YELLOW + "You have successfully joined the chat channel " + args[0] + ".");
    }
}
