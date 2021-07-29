package com.ryanjhuston.Types.Serializers;

import java.util.List;

public class SerializedChatChannel {

    public String channel;
    public List<String> players;

    public SerializedChatChannel() {
        super();
    }

    public SerializedChatChannel(String channel, List<String> players) {
        this.channel = channel;
        this.players = players;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }
}
