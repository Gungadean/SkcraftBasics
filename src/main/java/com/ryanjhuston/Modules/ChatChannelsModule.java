package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class ChatChannelsModule implements Listener {

    private SkcraftBasics plugin;

    public HashMap<String, List<String>> chatChannels = new HashMap<>();
    public HashMap<String, String> inChannelPlayers = new HashMap<>();

    private String chatFormat;
    private String globalChatFormat;
    private String leaveFormat;
    private String joinFormat;

    private boolean moduleEnabled;

    public ChatChannelsModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    public void joinChatChannel(String player, String channel) {
        if(inChannelPlayers.containsKey(player)) {
            chatChannels.get(inChannelPlayers.get(player)).remove(player);
            inChannelPlayers.remove(player);
        }

        if(chatChannels.containsKey(channel)) {
            sendJoinMessage(Bukkit.getPlayer(UUID.fromString(player)), channel);
            chatChannels.get(channel).add(player);
        } else {
            List<String> players = new ArrayList<>();
            players.add(player);

            channel = channel.replaceAll("[^a-zA-z0-9]", "");

            sendJoinMessage(Bukkit.getPlayer(UUID.fromString(player)), channel);
            chatChannels.put(channel, players);
        }
        inChannelPlayers.put(player, channel);

        plugin.saveChatChannelsToFile();
    }

    public void leaveChatChannel(String player) {
        chatChannels.get(inChannelPlayers.get(player)).remove(player);

        sendLeaveMessage(Bukkit.getPlayer(UUID.fromString(player)), inChannelPlayers.get(player));

        if(chatChannels.get(inChannelPlayers.get(player)).isEmpty()) {
            chatChannels.remove(inChannelPlayers.get(player));
        }
        inChannelPlayers.remove(player);

        plugin.saveChatChannelsToFile();
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        if(inChannelPlayers.containsKey(event.getPlayer().getUniqueId().toString())) {
            sendJoinMessage(event.getPlayer(), inChannelPlayers.get(event.getPlayer().getUniqueId().toString()));
            return;
        }

        for(Map.Entry<String, List<String>> entry : chatChannels.entrySet()) {
            if(entry.getValue().contains(event.getPlayer().getUniqueId().toString())) {
                inChannelPlayers.put(event.getPlayer().getUniqueId().toString(), entry.getKey());
                sendJoinMessage(event.getPlayer(), inChannelPlayers.get(event.getPlayer().getUniqueId().toString()));
                return;
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void playerChat(AsyncPlayerChatEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        if(inChannelPlayers.containsKey(uuid)) {
            if(event.getMessage().startsWith("\\")) {
                sendGlobalMessage(event.getPlayer(), event.getMessage());
            } else {
                sendMessageToChannel(event.getPlayer(), inChannelPlayers.get(uuid), event.getMessage());
            }
            event.setCancelled(true);
        }
    }

    public void sendJoinMessage(Player sender, String channel) {
        if(!chatChannels.containsKey(channel)) {
            return;
        }

        List<String> players = chatChannels.get(channel);

        for(String receiver : players) {
            if(plugin.checkOnline(receiver)) {
                Player player = Bukkit.getPlayer(UUID.fromString(receiver));

                player.sendMessage(useChatFormat(joinFormat, channel, sender, ""));
            }
        }
    }

    public void sendLeaveMessage(Player sender, String channel) {
        List<String> players = chatChannels.get(channel);

        for(String receiver : players) {
            if(plugin.checkOnline(receiver)) {
                Player player = Bukkit.getPlayer(UUID.fromString(receiver));

                player.sendMessage(useChatFormat(leaveFormat, channel, sender, ""));
            }
        }
    }

    public void sendMessageToChannel(Player sender, String channel, String message) {
        List<String> players = chatChannels.get(channel);

        for(String receiver : players) {
            if(plugin.checkOnline(receiver)) {
                Player player = Bukkit.getPlayer(UUID.fromString(receiver));

                player.sendMessage(useChatFormat(chatFormat, channel, sender, message));
            }
        }
    }

    public void sendGlobalMessage(Player player, String message) {
        Bukkit.broadcastMessage(useChatFormat(globalChatFormat, "", player, message));
    }

    public String useChatFormat(String format, String channel, Player player, String message) {
        String result = format;

        result = result.replaceAll("%channel%", channel);
        result = result.replaceAll("%player%", player.getName());
        result = result.replaceAll("%playerDisplay%", player.getDisplayName());
        result = ChatColor.translateAlternateColorCodes('&', result);
        result = result.replaceAll("%message%", message);

        return result;
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        chatFormat = plugin.getConfig().getString("Module-Settings.ChatChannels-Module.Chat-Format");
        globalChatFormat = plugin.getConfig().getString("Module-Settings.ChatChannels-Module.Global-Chat-Format");
        leaveFormat = plugin.getConfig().getString("Module-Settings.ChatChannels-Module.Leave-Format");
        joinFormat = plugin.getConfig().getString("Module-Settings.ChatChannels-Module.Join-Format");

        moduleEnabled = plugin.enabledModules.contains("ChatChannels");

        if(moduleEnabled) {
            HandlerList.unregisterAll(plugin.chatChannelsModule);
            plugin.pm.registerEvents(plugin.chatChannelsModule, plugin);

            plugin.logger.info("- ChatChannelModule Enabled");
        } else {
            HandlerList.unregisterAll(plugin.chatChannelsModule);
        }
    }
}
