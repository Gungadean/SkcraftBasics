package com.ryanjhuston;

import com.ryanjhuston.Database.SqlHandler;
import com.ryanjhuston.Modules.CraftingModule;
import com.ryanjhuston.Modules.EnderPearlTeleportModule;
import com.ryanjhuston.Modules.StargateModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class SkcraftBasics extends JavaPlugin {

    public Logger logger = Logger.getLogger("Minecraft");
    public PluginManager pm = Bukkit.getPluginManager();

    public SqlHandler sql;

    public boolean useMysql;
    public String username;
    public String password;
    public String address;
    public int port;
    public String database;

    public EnderPearlTeleportModule enderPearlTeleportModule;
    public CraftingModule craftingModule;
    public StargateModule stargateModule;

    public HashMap<String, ArrayList<String>> teleportAuth = new HashMap<>();

    private File playerItemsFile = new File(getDataFolder(), "playerItems.yml");
    private File stargatesFile = new File(getDataFolder(), "stargates.yml");
    private FileConfiguration playerItems;
    private FileConfiguration stargates;

    public boolean debug = true;

    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        createCustomConfigs();
        saveConfigs();

        enderPearlTeleportModule = new EnderPearlTeleportModule(this);
        craftingModule = new CraftingModule(this);
        stargateModule = new StargateModule(this);

        /*if(useMysql) {
            sql = new SqlHandler(username, password, address, port, database, this);
        } else {
            sql = new SqlHandler(this);
        }*/

        pm.registerEvents(new SkcraftEventHandler(this), this);

        this.getCommand("invite").setExecutor(new SkcraftCommandHandler(this));
        this.getCommand("accept").setExecutor(new SkcraftCommandHandler(this));
        this.getCommand("setspawn").setExecutor(new SkcraftCommandHandler(this));

        logger.info("has started.");
    }

    public void onDisable() {
        saveConfig();
        saveConfigs();
        logger.info("has stopped.");
    }

    public void loadConfig() {
        this.useMysql = getConfig().getBoolean("Use-Mysql");
        if(useMysql) {
            this.username = getConfig().getString("Mysql.Username");
            this.password = getConfig().getString("Mysql.Password");
            this.address = getConfig().getString("Mysql.Address");
            this.port = getConfig().getInt("Mysql.Port");
            this.database = getConfig().getString("Database");
        }
    }

    public void reloadPlugin() {
        boolean useMysqlOld = useMysql;

        loadConfig();

        if(useMysqlOld != useMysql) {
            if(useMysql) {
                sql.reloadConnection(username, password, address, port, database);
            } else {
                sql.reloadConnection();
            }
        }
    }

    private void createCustomConfigs() {
        if (!playerItemsFile.exists()) {
            playerItemsFile.getParentFile().mkdirs();
            saveResource("playerItems.yml", false);
        }

        if(!stargatesFile.exists()) {
            stargatesFile.getParentFile().mkdirs();
            saveResource("stargates.yml", false);
        }

        playerItems = new YamlConfiguration();
        stargates = new YamlConfiguration();

        try {
            playerItems.load(playerItemsFile);
            stargates.load(stargatesFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getPlayerItemsList() {
        return playerItems;
    }

    public void saveConfigs() {
        saveConfig();
        try {
            playerItems.save(playerItemsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
