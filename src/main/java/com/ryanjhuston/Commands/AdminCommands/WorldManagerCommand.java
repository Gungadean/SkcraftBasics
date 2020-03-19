package com.ryanjhuston.Commands.AdminCommands;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldManagerCommand {

    public static void command(SkcraftBasics plugin, CommandSender commandSender, String[] args) throws CommandException{
        if(commandSender instanceof Player) {
            if (!plugin.skcraftPlayerList.get(((Player) commandSender).getUniqueId().toString()).isAdmin()) {
                throw new CommandException("You do not have permission for this command.");
            }
        }

        if(args.length == 0) {
            commandSender.sendMessage(ChatColor.GREEN + "WorldManager Commands:");
            commandSender.sendMessage(ChatColor.GREEN + "worldmanager create - Create new world.");
            commandSender.sendMessage(ChatColor.GREEN + "worldmanager delete - Delete a world.");
            commandSender.sendMessage(ChatColor.GREEN + "worldmanager load - Load existing world.");
            commandSender.sendMessage(ChatColor.GREEN + "worldmanager unload - Unload world.");
            commandSender.sendMessage(ChatColor.GREEN + "worldmanager tp - Teleport to world.");
            return;
        }

        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("help")) {
                commandSender.sendMessage(ChatColor.GREEN + "WorldManager Commands:");
                commandSender.sendMessage(ChatColor.GREEN + "worldmanager create - Create new world.");
                commandSender.sendMessage(ChatColor.GREEN + "worldmanager delete - Delete a world.");
                commandSender.sendMessage(ChatColor.GREEN + "worldmanager load - Load existing world.");
                commandSender.sendMessage(ChatColor.GREEN + "worldmanager unload - Unload world.");
                commandSender.sendMessage(ChatColor.GREEN + "worldmanager tp - Teleport to world.");
                return;
            }
            if(args[0].equalsIgnoreCase("list")) {
                listCommand(plugin, commandSender);
                return;
            } else {
                throw new CommandException("Correct Usage: /worldmanager {create/delete/load/unload} {world}");
            }
        }

        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("tp")) {
                tpCommand(commandSender, args);
                return;
            }
        }

        switch(args[0].toLowerCase()) {
            case "create":
                createCommand(plugin, args);
                commandSender.sendMessage(ChatColor.GOLD + "World successfully created.");
                break;
            case "delete":
                deleteCommand(plugin, args);
                commandSender.sendMessage(ChatColor.GOLD + "World successfully deleted.");
                break;
            case "load":
                loadCommand(plugin, args);
                commandSender.sendMessage(ChatColor.GOLD + "World successfully loaded.");
                break;
            case "unload":
                unloadCommand(plugin, args);
                commandSender.sendMessage(ChatColor.GOLD + "World successfully unloaded.");
                break;
            default:
                throw new CommandException("Correct Usage: /worldmanager {create/delete/load/unload} {world}");
        }
    }

    private static void tpCommand(CommandSender commandSender, String[] args) throws CommandException{
        if(!(commandSender instanceof Player)) {
            throw new CommandException("This command can only be executed by players.");
        }

        World world = Bukkit.getWorld(args[1]);

        if(world == null) {
            throw new CommandException("The world " + args[1] + " does not exist to teleport.");
        }

        ((Player) commandSender).teleport(world.getSpawnLocation());
    }

    private static void createCommand(SkcraftBasics plugin, String[] args) throws CommandException {
        WorldType worldType = WorldType.NORMAL;
        World.Environment environment = World.Environment.NORMAL;

        if(args.length > 4) {
            throw new CommandException("Too many arguments.");
        }

        if(args.length >= 3) {
            if(WorldType.getByName(args[2]) != null) {
                worldType = WorldType.getByName(args[2]);
            } else {
                throw new CommandException(args[2] + " is not a valid world type.");
            }
        }

        if(args.length == 4) {
            switch(args[3].toLowerCase()) {
                case "normal":
                    environment = World.Environment.NORMAL;
                    break;
                case "nether":
                    environment = World.Environment.NETHER;
                    break;
                case "the_end":
                    environment = World.Environment.THE_END;
                    break;
                default:
                    throw new CommandException(args[3] + " is not a valid world environment.");
            }
        }

        plugin.worldManager.createWorld(args[1], worldType, environment);
    }

    private static void deleteCommand(SkcraftBasics plugin, String[] args) throws CommandException {
        plugin.worldManager.deleteWorld(args[1]);
    }

    private static void loadCommand(SkcraftBasics plugin, String[] args) throws CommandException {
        plugin.worldManager.loadWorld(args[1]);
    }

    private static void unloadCommand(SkcraftBasics plugin, String[] args) throws CommandException {
        plugin.worldManager.unloadWorld(args[1]);
    }

    private static void listCommand(SkcraftBasics plugin, CommandSender commandSender) throws CommandException {
        String worldList = "";

        for(int i = 0; i < plugin.worlds.size(); i++) {
            if(i == (plugin.worlds.size()-1)) {
                worldList += plugin.worlds.get(i).getName();
            } else {
                worldList += plugin.worlds.get(i).getName() + ", ";
            }
        }

        if(worldList.equals("")) {
            commandSender.sendMessage(ChatColor.YELLOW + "There are currently no extra worlds loaded.");
        } else {
            commandSender.sendMessage(ChatColor.YELLOW + "Worlds: " + worldList);
        }
    }
}