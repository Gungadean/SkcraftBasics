package com.ryanjhuston.Commands;

import com.ryanjhuston.SkcraftBasics;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

public class InviteCommand {

    private static final Pattern UUID_FIX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    public static void command(CommandSender commandSender, String[] args, SkcraftBasics plugin) throws CommandException{
        if(args.length != 1) {
            throw new CommandException("Correct Usage: /invite {player-name}");
        }

        if(commandSender instanceof Player && commandSender.getName().equals(args[0])) {
            throw new CommandException("You cannot invite yourself.");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String url = "https://api.mojang.com/users/profiles/minecraft/"+args[0];

            String uuid = null;

            try {
                uuid = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
                if(uuid.isEmpty()) {
                    uuid = null;
                } else {
                    uuid = ((JSONObject) JSONValue.parseWithException(uuid)).get("id").toString();
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }

            if(uuid == null) {
                commandSender.sendMessage(ChatColor.RED + "This player does not exist.");
                return;
            }

            uuid = UUID_FIX.matcher(uuid.replace("-", "")).replaceAll("$1-$2-$3-$4-$5");

            OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

            if(target.isWhitelisted()) {
                commandSender.sendMessage(ChatColor.RED + "This player is already invited.");
                return;
            }

            if(target != null) {
                target.setWhitelisted(true);
                commandSender.sendMessage(ChatColor.YELLOW + "Player has been successfully invited.");
                return;
            }

            commandSender.sendMessage(ChatColor.RED + "Player was not found. Please check the spelling and try again.");
        });
    }
}
