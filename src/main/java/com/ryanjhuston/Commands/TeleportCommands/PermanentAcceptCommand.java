package com.ryanjhuston.Commands.TeleportCommands;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.SkcraftPlayer;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

public class PermanentAcceptCommand {

    private static final Pattern UUID_FIX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    public static void command(Player player, String[] args, SkcraftPlayer skcraftPlayer, SkcraftBasics plugin) throws CommandException {
        if(args.length < 1) {
            throw new CommandException("Correct Usage: /paccept {player-name}");
        }

        String username = args[0];

        getUuidFromName(skcraftPlayer, player, username, plugin);
    }

    public static void getUuidFromName(SkcraftPlayer skcraftPlayer, Player player, String username, SkcraftBasics plugin) throws CommandException{
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                String url = "https://api.mojang.com/users/profiles/minecraft/"+username;

                String uuid = null;

                try {
                    uuid = IOUtils.toString(new URL(url), "UTF-8");
                    if(uuid.isEmpty()) {
                        uuid = null;
                    } else {
                        uuid = ((JSONObject)JSONValue.parseWithException(uuid)).get("id").toString();
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }

                if(uuid == null) {
                    throw new CommandException("This player does not exist.");
                }

                uuid = UUID_FIX.matcher(uuid.replace("-", "")).replaceAll("$1-$2-$3-$4-$5");


                if(player.getUniqueId().toString().equals(uuid)) {
                    throw new CommandException("You cannot accept yourself.");
                }

                if(skcraftPlayer.getPTeleAuthed().contains(uuid)) {
                    if(skcraftPlayer.getTeleAuthed().contains(uuid)) {
                        skcraftPlayer.getTeleAuthed().remove(uuid);
                    }
                    skcraftPlayer.getPTeleAuthed().remove(uuid);
                    player.sendMessage(ChatColor.YELLOW + username + " has been removed from accepted list.");
                    return;
                } else {
                    if(!skcraftPlayer.getTeleAuthed().contains(uuid)) {
                        skcraftPlayer.getTeleAuthed().add(uuid);
                    }
                    skcraftPlayer.getPTeleAuthed().add(uuid);
                    player.sendMessage(ChatColor.YELLOW + username + " has been added to the accepted list.");
                    return;
                }
            }
        }, 0);
    }
}
