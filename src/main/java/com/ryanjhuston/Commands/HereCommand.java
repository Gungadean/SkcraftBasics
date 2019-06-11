package com.ryanjhuston.Commands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HereCommand {

    public static void command(Player player, String[] args, SkcraftBasics plugin) {
        if(args.length > 0) {
            player.sendMessage(ChatColor.RED + "Correct Usage: /here");
            return;
        }

        if(!plugin.chatChannelsModule.inChannelPlayers.containsKey(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "You must be in a chat channel to use this command.");
            return;
        }

        String playerList = "";
        List<String> onlinePlayers = new ArrayList<>();

        for(String uuid : plugin.chatChannelsModule.chatChannels.get(plugin.chatChannelsModule.inChannelPlayers.get(player.getUniqueId().toString()))) {
            if(plugin.checkOnline(uuid)) {
                onlinePlayers.add(Bukkit.getPlayer(UUID.fromString(uuid)).getName());
            }
        }

        for(int i = 0; i < onlinePlayers.size(); i++) {
            if(i == (onlinePlayers.size()-1)) {
                playerList += onlinePlayers.get(i);
            } else {
                playerList += onlinePlayers.get(i) + ", ";
            }
        }

        player.sendMessage(ChatColor.YELLOW + "In Channel " + plugin.chatChannelsModule.inChannelPlayers.get(player.getUniqueId().toString()) + ": " + playerList);
    }
}