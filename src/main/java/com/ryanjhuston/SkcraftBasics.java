package com.ryanjhuston;

import com.ryanjhuston.Database.SqlHandler;
import com.ryanjhuston.Modules.CraftingModule;
import com.ryanjhuston.Modules.EnderPearlTeleportModule;
import com.ryanjhuston.Modules.StargateModule;
import com.ryanjhuston.Types.Stargate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    public HashMap<String, Stargate> stargateList = new HashMap<>();
    public HashMap<String, List<String>> networkList = new HashMap<>();

    private File playerItemsFile = new File(getDataFolder(), "playerItems.yml");
    private File stargatesFile = new File(getDataFolder(), "stargates.yml");
    private File networksFile = new File(getDataFolder(), "stargateNetworks.yml");
    private FileConfiguration playerItems;
    private FileConfiguration stargatesConfig;
    private FileConfiguration networksConfig;

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

        if(getConfig().getString("Spawn-Location").equalsIgnoreCase("")) {
            Location loc = Bukkit.getWorlds().get(0).getSpawnLocation();
            getConfig().set("Spawn-Location", loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "," + loc.getYaw() + "," + loc.getPitch());
        }

        loadStargatesFromFile();

        logger.info("has started.");
    }

    public void onDisable() {
        saveConfig();
        saveConfigs();
        saveStargatesToFile();
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

        if(!networksFile.exists()) {
            networksFile.getParentFile().mkdirs();
            saveResource("stargateNetworks.yml", false);
        }

        playerItems = new YamlConfiguration();
        stargatesConfig = new YamlConfiguration();
        networksConfig = new YamlConfiguration();

        try {
            playerItems.load(playerItemsFile);
            stargatesConfig.load(stargatesFile);
            networksConfig.load(networksFile);
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
            stargatesConfig.save(stargatesFile);
            networksConfig.save(networksFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadStargatesFromFile() {
        logger.info("[SkcraftBasics] Loading stargates from config.");

        List<String> networksList = (List<String>)networksConfig.getList("Networks-List");
        Iterator networkIt = networksList.iterator();

        while(networkIt.hasNext()) {
            String network = (String)networkIt.next();
            List<String> stargatesList = (List<String>)networksConfig.getList("Networks." + network);

            networkList.put(network, stargatesList);

            Iterator stargateIt = stargatesList.iterator();
            while(stargateIt.hasNext()) {
                String stargate = (String)stargateIt.next();
                String owner = stargatesConfig.getString(stargate  + ".Owner");
                network = stargatesConfig.getString(stargate + ".Network");
                String[] signLocationString = stargatesConfig.getString(stargate + ".Sign-Location").split(",");
                String[] buttonLocationString = stargatesConfig.getString(stargate + ".Button-Location").split(",");
                List<String> blocksString = (List<String>)stargatesConfig.getList(stargate + ".Blocks");

                Location signLocation = new Location(Bukkit.getWorld(signLocationString[0]), Double.valueOf(signLocationString[1]), Double.valueOf(signLocationString[2]), Double.valueOf(signLocationString[3]));
                Location buttonLocation = new Location(Bukkit.getWorld(buttonLocationString[0]), Double.valueOf(buttonLocationString[1]), Double.valueOf(buttonLocationString[2]), Double.valueOf(buttonLocationString[3]));

                List<Location> blocks = new ArrayList<>();

                Iterator it = blocksString.iterator();
                while(it.hasNext()) {
                    String[] blockLocation = ((String)it.next()).split(",");
                    Location block = new Location(Bukkit.getWorld(blockLocation[0]), Double.valueOf(blockLocation[1]), Double.valueOf(blockLocation[2]), Double.valueOf(blockLocation[3]));

                    block.getBlock().setMetadata("Stargate", new FixedMetadataValue(this, stargate));

                    blocks.add(block);
                }

                stargateList.put(stargate, new Stargate(owner, network, signLocation, buttonLocation, blocks));
            }
        }

        logger.info("[SkcraftBasics] Finished loading stargates from config.");
    }

    public void saveStargatesToFile() {
        logger.info("[SkcraftBasics] Saving stargates to config.");

        for(HashMap.Entry<String, List<String>> entry : networkList.entrySet()) {
            networksConfig.set(entry.getKey(), entry.getValue());
            System.out.println(entry.getKey() + " " + entry.getValue().toString());
        }

        for(HashMap.Entry<String, Stargate> entry : stargateList.entrySet()) {
            stargatesConfig.set(entry.getKey() + ".Owner", entry.getValue().getOwner());
            stargatesConfig.set(entry.getKey() + ".Network", entry.getValue().getNetwork());
            stargatesConfig.set(entry.getKey() + ".Sign-Location", entry.getValue().getSignLocation().getWorld().getName() + ","
                    + entry.getValue().getSignLocation().getX() + ","
                    + entry.getValue().getSignLocation().getY() + ","
                    + entry.getValue().getSignLocation().getZ());
            stargatesConfig.set(entry.getKey() + ".Button-Location", entry.getValue().getButtonLocation().getWorld().getName() + ","
                    + entry.getValue().getButtonLocation().getX() + ","
                    + entry.getValue().getButtonLocation().getY() + ","
                    + entry.getValue().getButtonLocation().getZ());

            List<String> blocks = new ArrayList<>();

            Iterator it = entry.getValue().getBlocks().iterator();
            while(it.hasNext()) {
                Location blockLocation = (Location)it.next();

                blocks.add(blockLocation.getWorld().getName() + ","
                        + blockLocation.getX() + ","
                        + blockLocation.getY() + ","
                        + blockLocation.getZ());
            }

            stargatesConfig.set(entry.getKey() + ".Blocks", blocks);
        }

        logger.info("[SkcraftBasics] Finished saving stargates to config.");
    }
}
