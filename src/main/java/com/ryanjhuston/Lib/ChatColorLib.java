package com.ryanjhuston.Lib;

import org.bukkit.ChatColor;

import java.util.Random;

public class ChatColorLib {

    public static ChatColor[] colors = new ChatColor[]{ChatColor.AQUA,
            ChatColor.BLUE,
            ChatColor.DARK_AQUA,
            ChatColor.DARK_BLUE,
            ChatColor.DARK_GREEN,
            ChatColor.DARK_PURPLE,
            ChatColor.DARK_RED,
            ChatColor.GOLD,
            ChatColor.GREEN,
            ChatColor.LIGHT_PURPLE,
            ChatColor.YELLOW};

    public static ChatColor getRandomColor() {
        Random random = new Random();
        return colors[random.nextInt(colors.length-1)];
    }
}
