package com.ryanjhuston.Commands.ChatChannelCommands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

public class LeaveCommand {

    public static void command(Player player, String[] args, SkcraftBasics plugin) throws CommandException {
        if(args.length != 0) {
            throw new CommandException("Correct Usage: /leave");
        }

        if(!plugin.chatChannelsModule.inChannelPlayers.containsKey(player.getUniqueId().toString())) {
            throw new CommandException("You are not currently in a chat channel.");
        }

        plugin.chatChannelsModule.leaveChatChannel(player.getUniqueId().toString());
        player.sendMessage(ChatColor.YELLOW + "You have successfully left the channel.");
    }
}
