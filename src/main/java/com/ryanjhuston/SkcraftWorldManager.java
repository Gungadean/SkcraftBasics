package com.ryanjhuston;

import org.bukkit.*;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SkcraftWorldManager {

    private SkcraftBasics plugin;

    public List<String> resetting = new ArrayList<>();

    public SkcraftWorldManager(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    public World loadWorld(String worldName) throws CommandException {
        worldName = worldName.replaceAll("[^A-Za-z0-9_\\\\-]", "");
        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            throw new CommandException("The world '" + worldName + "' is already loaded.");
        }

        if (!new File(worldName, "level.dat").exists()) {
            throw new CommandException("The world '" + worldName + "' does not exist on the disk.");
        }

        WorldCreator worldCreator = WorldCreator.name(worldName);

        Bukkit.broadcastMessage(ChatColor.RED + "A world is being mounted, standby.");
        world = Bukkit.createWorld(worldCreator);
        plugin.worlds.add(world);
        Bukkit.broadcastMessage(ChatColor.RED + "The world has finished being mounted.");

        return world;
    }

    public World createWorld(String worldName, WorldType worldType, World.Environment environment) throws CommandException {
        worldName = worldName.replaceAll("[^A-Za-z0-9_\\\\-]", "");
        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            throw new CommandException("The world '" + worldName + "' is already loaded.");
        }

        if (new File(worldName, "level.dat").exists()) {
            return loadWorld(worldName);
        }

        WorldCreator worldCreator = WorldCreator.name(worldName);

        worldCreator.type(worldType);
        worldCreator.environment(environment);

        Bukkit.broadcastMessage(ChatColor.RED + "A world is being created, standby.");

        world = Bukkit.createWorld(worldCreator);
        plugin.worlds.add(world);

        Bukkit.broadcastMessage(ChatColor.RED + "The world has finished being created.");
        return world;
    }

    public void unloadWorld(String worldName) throws CommandException {
        worldName = worldName.replaceAll("[^A-Za-z0-9_\\\\-]", "");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            throw new CommandException("The world '" + worldName + "' is not loaded.");
        }

        if(!plugin.worlds.contains(world)) {
            throw new CommandException("You cannot unload '" + worldName + "'.");
        }

        for(Player player : world.getPlayers()) {
            player.teleport(plugin.spawnLocation);
        }

        Bukkit.broadcastMessage(ChatColor.RED + "World '" + worldName + "' is being unloaded.");

        plugin.worlds.remove(world);
        Bukkit.unloadWorld(world, true);

        Bukkit.broadcastMessage(ChatColor.RED + "The world has finished being unloaded.");
    }

    public void deleteWorld(String worldName) throws CommandException {
        worldName = worldName.replaceAll("[^A-Za-z0-9_\\\\-]", "");
        World world = Bukkit.getWorld(worldName);

        if(!new File(worldName, "level.dat").exists()) {
            throw new CommandException("The world '" + worldName + "' does not exist.");
        }

        Bukkit.broadcastMessage(ChatColor.RED + "A world is being deleted, standby.");

        if(world != null) {
            throw new CommandException("You must unload the world before deleting.");
        }

        File worldFolder = new File(worldName);

        plugin.worlds.remove(world);

        Bukkit.unloadWorld(world, true);
        Bukkit.broadcastMessage(ChatColor.RED + "The world has finished being deleted.");

        deleteDirectory(worldFolder);
    }

    public World resetWorld(String worldName) throws CommandException {
        worldName = worldName.replaceAll("[^A-Za-z0-9_\\\\-]", "");
        World world = Bukkit.getWorld(worldName);

        if(world == null) {
            throw new CommandException("The world '" + worldName + "' is not loaded.");
        }

        if(!plugin.worlds.contains(world)) {
            throw new CommandException("You cannot unload '" + worldName + "'.");
        }

        for(Player player : world.getPlayers()) {
            player.teleport(plugin.spawnLocation);
        }

        resetting.add(worldName);

        WorldType worldType = world.getWorldType();
        World.Environment environment = world.getEnvironment();

        Bukkit.broadcastMessage(ChatColor.RED + "World '" + worldName + "' is being reset.");
        File worldFolder = world.getWorldFolder();

        Bukkit.unloadWorld(world, false);
        deleteDirectory(worldFolder);

        WorldCreator worldCreator = WorldCreator.name(worldName);
        worldCreator.type(worldType);
        worldCreator.environment(environment);

        world = Bukkit.createWorld(worldCreator);
        plugin.worlds.add(world);

        Bukkit.broadcastMessage(ChatColor.RED + "The world has finished being reset.");
        resetting.remove(worldName);

        return world;
    }

    private boolean deleteDirectory(File file) {
        if(file.exists()) {
            File[] files = file.listFiles();

            for(int i = 0; i < files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return file.delete();
    }
}