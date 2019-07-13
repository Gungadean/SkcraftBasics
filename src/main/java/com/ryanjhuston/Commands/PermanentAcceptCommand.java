package com.ryanjhuston.Commands;

import com.ryanjhuston.Types.SkcraftPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class PermanentAcceptCommand {

    public static void command(Player player, String[] args, SkcraftPlayer skcraftPlayer) {
        if(args.length < 2) {
            player.sendMessage(ChatColor.RED + "Correct Usage: /paccept {add/remove} {player-name}");
            return;
        }

        if(args[0].equalsIgnoreCase("add")) {
            addCommand(player, args, skcraftPlayer);
            return;
        } else if(args[0].equalsIgnoreCase("remove")) {
            removeCommand(player, args, skcraftPlayer);
            return;
        }

        player.sendMessage(ChatColor.RED + "Correct Usage: /paccept {add/remove} {player-name}");
    }

    public static void addCommand(Player player, String[] args, SkcraftPlayer skcraftPlayer) {
        String username = args[1];

        OfflinePlayer target = Bukkit.getOfflinePlayer(username);

        if(player.getName().equals(target.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot accept yourself.");
            return;
        }

        if(skcraftPlayer.getPTeleAuthed().contains(target.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "This player is already accepted.");
            return;
        }

        skcraftPlayer.getPTeleAuthed().add(target.getUniqueId().toString());
        player.sendMessage(ChatColor.YELLOW + target.getName() + " has been added to your accepted list.");
        return;
    }

    public static void removeCommand(Player player, String[] args, SkcraftPlayer skcraftPlayer) {
        String username = args[1];

        OfflinePlayer target = Bukkit.getOfflinePlayer(username);

        if(player.getName().equals(target.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot accept yourself.");
            return;
        }

        if(!skcraftPlayer.getPTeleAuthed().contains(target.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "This player is not currently accepted.");
            return;
        }

        skcraftPlayer.getPTeleAuthed().remove(target.getUniqueId().toString());
        player.sendMessage(ChatColor.YELLOW + target.getName() + " has been removed from accepted list.");
        return;
    }
}
