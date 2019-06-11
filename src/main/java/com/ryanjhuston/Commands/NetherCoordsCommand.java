package com.ryanjhuston.Commands;

import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

public class NetherCoordsCommand {

    public static void command(Player player) {
        if(player.getWorld().getBiome(0, 0) == Biome.THE_END)
        {
            player.sendMessage(ChatColor.RED + "You cannot use this command in this world.");
            return;
        }

        if(player.getWorld().getBiome(0, 0) == Biome.NETHER) {
            player.sendMessage(ChatColor.YELLOW + "Portal for Overworld should be placed at x=" + (int)(player.getLocation().getX()*8) + " y=" + (int)(player.getLocation().getY()) + " z=" + (int)(player.getLocation().getZ()*8) + ".");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Portal for Nether should be placed at x=" + (int)(player.getLocation().getX()/8) + " y=" + (int)(player.getLocation().getY()) + " z=" + (int)(player.getLocation().getZ()/8) + ".");
        }
    }
}
