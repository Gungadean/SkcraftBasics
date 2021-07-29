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

        if(plugin.worldGenerators.containsKey(worldName)) {
            if (plugin.worldGenerators.get(worldName) != null) {
                worldCreator.generator(plugin.worldGenerators.get(worldName));
            }
        }

        Bukkit.broadcastMessage(ChatColor.YELLOW + "A world is being mounted, standby.");
        world = worldCreator.createWorld();
        plugin.worlds.add(world);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "The world has finished being mounted.");

        return world;
    }

    public World createWorld(String worldName, WorldType worldType, World.Environment environment, Long seed, String generator) throws CommandException {
        worldName = worldName.replaceAll("[^A-Za-z0-9_\\\\-]", "");
        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            throw new CommandException("The world '" + worldName + "' is already loaded.");
        }

        if (new File(worldName, "level.dat").exists()) {
            return loadWorld(worldName);
        }

        WorldCreator worldCreator = WorldCreator.name(worldName);

        if(seed != null) {
            worldCreator.seed(seed);
        }

        if(generator != null) {
            worldCreator.generator(generator);
        }

        if (worldType != null) {
            worldCreator.type(worldType);
        } else {
            worldCreator.type(WorldType.NORMAL);
        }

        if(environment != null) {
            worldCreator.environment(environment);
        }

        Bukkit.broadcastMessage(ChatColor.YELLOW + "A world is being created, standby.");

        world = worldCreator.createWorld();
        plugin.worlds.add(world);
        plugin.worldGenerators.put(worldName, generator);

        Bukkit.broadcastMessage(ChatColor.YELLOW + "The world has finished being created.");
        return world;
    }

    public void unloadWorld(String worldName) throws CommandException {
        worldName = worldName.replaceAll("[^A-Za-z0-9_\\\\-]", "");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            throw new CommandException("The world '" + worldName + "' is not loaded.");
        }

        if(isDefaultWorld(worldName)) {
            throw new CommandException("You cannot unload a default world.");
        }

        if(!plugin.worlds.contains(world)) {
            throw new CommandException("You cannot unload '" + worldName + "'.");
        }

        for(Player player : world.getPlayers()) {
            player.teleport(plugin.spawnLocation);
        }

        Bukkit.broadcastMessage(ChatColor.YELLOW + "World '" + worldName + "' is being unloaded.");

        plugin.worlds.remove(world);
        Bukkit.unloadWorld(world, true);

        Bukkit.broadcastMessage(ChatColor.YELLOW + "The world has finished being unloaded.");
    }

    public void deleteWorld(String worldName) throws CommandException {
        worldName = worldName.replaceAll("[^A-Za-z0-9_\\\\-]", "");
        World world = Bukkit.getWorld(worldName);

        if(isDefaultWorld(worldName)) {
            throw new CommandException("You cannot delete a default world.");
        }

        if(world != null) {
            throw new CommandException("Worlds cannot be deleted while loaded on server.");
        }

        if(!new File(worldName, "level.dat").exists()) {
            throw new CommandException("The world '" + worldName + "' does not exist.");
        }

        Bukkit.broadcastMessage(ChatColor.YELLOW + "A world is being deleted, standby.");

        if(world != null) {
            throw new CommandException("You must unload the world before deleting.");
        }

        File worldFolder = new File(worldName);

        plugin.worlds.remove(world);
        plugin.worldGenerators.remove(worldName);
        plugin.getConfig().set("World-Settings." + worldName, null);

        Bukkit.unloadWorld(world, true);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "The world has finished being deleted.");

        deleteDirectory(worldFolder);
    }

    public World resetWorld(String worldName) throws CommandException {
        worldName = worldName.replaceAll("[^A-Za-z0-9_\\\\-]", "");
        World world = Bukkit.getWorld(worldName);

        if(world == null) {
            throw new CommandException("The world '" + worldName + "' is not loaded.");
        }

        if(isDefaultWorld(worldName)) {
            throw new CommandException("You cannot reset a default world.");
        }

        if(!plugin.worlds.contains(world)) {
            throw new CommandException("You cannot unload '" + worldName + "'.");
        }

        for(Player player : world.getPlayers()) {
            player.teleport(plugin.spawnLocation);
        }

        resetting.add(worldName);
        plugin.worlds.remove(world);

        WorldType worldType = world.getWorldType();
        World.Environment environment = world.getEnvironment();

        Bukkit.broadcastMessage(ChatColor.YELLOW + "World '" + worldName + "' is being reset.");
        File worldFolder = world.getWorldFolder();

        Bukkit.unloadWorld(world, false);
        deleteDirectory(worldFolder);

        WorldCreator worldCreator = WorldCreator.name(worldName);
        worldCreator.type(worldType);
        worldCreator.environment(environment);
        worldCreator.generator(plugin.worldGenerators.get(worldName));

        world = Bukkit.createWorld(worldCreator);
        plugin.worlds.add(world);

        Bukkit.broadcastMessage(ChatColor.YELLOW + "The world has finished being reset.");
        resetting.remove(worldName);

        return world;
    }

    private boolean isDefaultWorld(String worldName) {
        return Bukkit.getWorlds().get(0).getName().equals(worldName) || Bukkit.getWorlds().get(1).getName().equals(worldName) || Bukkit.getWorlds().get(2).getName().equals(worldName);
    }

    private boolean deleteDirectory(File file) {
        if(file.exists()) {
            File[] files = file.listFiles();

            for(File dirFile : files) {
                if(dirFile.isDirectory()) {
                    deleteDirectory(dirFile);
                } else {
                    dirFile.delete();
                }
            }
        }
        return file.delete();
    }
}