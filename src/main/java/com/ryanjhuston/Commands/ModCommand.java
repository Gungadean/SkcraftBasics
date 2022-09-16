package com.ryanjhuston.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.Serializers.SerializedPlayer;
import com.ryanjhuston.Types.SkcraftPlayer;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class ModCommand {

    public static void command(Player player, SkcraftBasics plugin) {
        if (!player.hasPermission("skcraft.mod")) {
            throw new CommandException("You do not have permission to use this command.");
        }

        ObjectMapper mapper = new ObjectMapper();

        SkcraftPlayer skcraftPlayer = plugin.getSkcraftPlayer(player);

        SerializedPlayer toSaveSerializedPlayer = new SerializedPlayer(player);
        SerializedPlayer fromSaveSerializedPlayer;

        File invFile = new File(plugin.inventoriesDir, player.getUniqueId() + ".inventory.json");

        if(!invFile.exists()) {
            try {
                invFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            player.setLevel(0);
            player.setExp(0);
            player.getInventory().clear();
        } else {
            try {
                fromSaveSerializedPlayer = mapper.readValue(invFile, SerializedPlayer.class);
                fromSaveSerializedPlayer.deserialize(player);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            mapper.writeValue(invFile, toSaveSerializedPlayer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(skcraftPlayer.getInModMode()) {
            skcraftPlayer.setInModMode(false);
            player.setGameMode(GameMode.SURVIVAL);
            player.sendMessage(ChatColor.YELLOW + "You have left mod mode.");
        } else {
            skcraftPlayer.setInModMode(true);
            player.setGameMode(GameMode.CREATIVE);
            player.sendMessage(ChatColor.YELLOW + "You have entered mod mode.");
        }

        if(plugin.luckPerms != null) {
            plugin.luckPerms.switchGroup(player);
        }
    }
}
