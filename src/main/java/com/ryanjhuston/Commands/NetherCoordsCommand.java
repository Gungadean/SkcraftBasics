package com.ryanjhuston.Commands;

import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

public class NetherCoordsCommand {

    public static void command(Player player) throws CommandException {
        if(player.getWorld().getBiome(0, 0) == Biome.THE_END || player.getWorld().getName().equals("Mining"))
        {
            throw new CommandException("You cannot use this command in this world.");
        }

        if(player.getWorld().getBiome(0, 0) == Biome.NETHER) {
            player.sendMessage(ChatColor.YELLOW + "Portal for Overworld should be placed at x=" + (int)(player.getLocation().getX()*8) + " y=" + (int)(player.getLocation().getY()) + " z=" + (int)(player.getLocation().getZ()*8) + ".");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Portal for Nether should be placed at x=" + (int)(player.getLocation().getX()/8) + " y=" + (int)(player.getLocation().getY()) + " z=" + (int)(player.getLocation().getZ()/8) + ".");
        }
    }
}
