package com.ryanjhuston.Commands;

import com.ryanjhuston.Types.SkcraftPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AcceptCommand {

    public static void command(Player player, String[] args, SkcraftPlayer skcraftPlayer) {
        if(args.length != 1) {
            player.sendMessage(ChatColor.RED + "Correct Usage: /accept {player-name}");
            return;
        }

        String username = args[0];

        for(Iterator iterator = Bukkit.getOnlinePlayers().iterator(); iterator.hasNext();) {
            Player target = (Player)iterator.next();
            if(target.getName().toLowerCase().startsWith(username.toLowerCase())) {
                if(player.getName().equals(target.getName())) {
                    player.sendMessage(ChatColor.RED + "You cannot accept yourself.");
                    return;
                }

                if(skcraftPlayer.getTeleAuthed().contains(target.getUniqueId().toString())) {
                    player.sendMessage(ChatColor.RED + "This player is already accepted.");
                    return;
                }

                skcraftPlayer.getTeleAuthed().add(target.getUniqueId().toString());
                player.sendMessage(ChatColor.YELLOW + target.getName() + " has been accepted.");
                return;
            }
        }

        player.sendMessage(ChatColor.RED + "Error: This player is not online.");
    }
}
