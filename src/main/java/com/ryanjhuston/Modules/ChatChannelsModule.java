package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class ChatChannelsModule implements Listener {

    private SkcraftBasics plugin;

    public HashMap<String, List<String>> chatChannels = new HashMap<>();
    public HashMap<String, String> inChannelPlayers = new HashMap<>();

    public ChatChannelsModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    public void joinChatChannel(String player, String channel) {
        if(inChannelPlayers.containsKey(player)) {
            chatChannels.get(inChannelPlayers.get(player)).remove(player);
            inChannelPlayers.remove(player);
        }

        if(chatChannels.containsKey(channel)) {
            sendJoinMessage(Bukkit.getPlayer(player).getName(), channel);
            chatChannels.get(channel).add(player);
        } else {
            List<String> players = new ArrayList<>();
            players.add(player);

            channel = channel.replaceAll("[^a-zA-z0-9]", "");

            sendJoinMessage(Bukkit.getPlayer(player).getName(), channel);
            chatChannels.put(channel, players);
        }
        inChannelPlayers.put(player, channel);
    }

    public void leaveChatChannel(String player) {
        chatChannels.get(inChannelPlayers.get(player)).remove(player);

        sendLeaveMessage(Bukkit.getPlayer(player).getName(), inChannelPlayers.get(player));

        if(chatChannels.get(inChannelPlayers.get(player)).isEmpty()) {
            chatChannels.remove(inChannelPlayers.get(player));
        }
        inChannelPlayers.remove(player);
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        if(inChannelPlayers.containsKey(event.getPlayer().getUniqueId().toString())) {
            return;
        }

        for(Map.Entry<String, List<String>> entry : chatChannels.entrySet()) {
            if(entry.getValue().contains(event.getPlayer().getUniqueId().toString())) {
                inChannelPlayers.put(event.getPlayer().getUniqueId().toString(), entry.getKey());
                return;
            }
        }
    }

    @EventHandler
    public void playerChat(AsyncPlayerChatEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        if(inChannelPlayers.containsKey(uuid)) {
            sendMessageToChannel(uuid, inChannelPlayers.get(uuid), event.getMessage());
            event.setCancelled(true);
        }
    }

    public void sendJoinMessage(String name, String channel) {
        List<String> players = chatChannels.get(channel);

        for(String receiver : players) {
            if(plugin.checkOnline(receiver)) {
                Player player = Bukkit.getPlayer(UUID.fromString(receiver));

                player.sendMessage(ChatColor.YELLOW + "[" + channel + "] User " + name + " has joined the channel.");
            }
        }
    }

    public void sendLeaveMessage(String name, String channel) {
        List<String> players = chatChannels.get(channel);

        for(String receiver : players) {
            if(plugin.checkOnline(receiver)) {
                Player player = Bukkit.getPlayer(UUID.fromString(receiver));

                player.sendMessage(ChatColor.YELLOW + "[" + channel + "] User " + name + " has left the channel.");
            }
        }
    }

    public void sendMessageToChannel(String name, String channel, String message) {
        List<String> players = chatChannels.get(channel);

        for(String receiver : players) {
            if(plugin.checkOnline(receiver)) {
                Player player = Bukkit.getPlayer(UUID.fromString(receiver));

                player.sendMessage(ChatColor.GREEN + "[" + channel + "] " + Bukkit.getPlayer(UUID.fromString(name)).getName() + ": " + message);
            }
        }
    }
}
