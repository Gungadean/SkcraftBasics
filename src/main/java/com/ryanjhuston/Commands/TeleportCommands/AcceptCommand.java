package com.ryanjhuston.Commands.TeleportCommands;

import com.ryanjhuston.Types.SkcraftPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

public class AcceptCommand {

    public static void command(Player player, String[] args, SkcraftPlayer skcraftPlayer) throws CommandException {
        if(args.length != 1) {
            throw new CommandException("Correct Usage: /accept {player-name}");
        }

        String username = args[0];

        for(Player target : Bukkit.getOnlinePlayers()) {
            if(target.getName().toLowerCase().startsWith(username.toLowerCase())) {
                if(player.getName().equals(target.getName())) {
                    throw new CommandException("You cannot accept yourself.");
                }

                if(skcraftPlayer.getTeleAuthed().contains(target.getUniqueId().toString())) {
                    throw new CommandException("This player is already accepted.");
                }

                skcraftPlayer.getTeleAuthed().add(target.getUniqueId().toString());
                player.sendMessage(ChatColor.YELLOW + target.getName() + " has been accepted.");
                return;
            }
        }

        throw new CommandException("Error: This player is not online.");
    }
}
