package com.ryanjhuston;

import com.ryanjhuston.Database.SqlHandler;
import com.ryanjhuston.Modules.*;
import com.ryanjhuston.Types.Stargate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class SkcraftBasics extends JavaPlugin {

    public Logger logger = Logger.getLogger("Minecraft");
    public PluginManager pm = Bukkit.getPluginManager();
    public SkcraftCommandHandler skcraftCommandHandler;

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
    public JetBootModule jetBootModule;
    public CaptureBallModule captureBallModule;
    public ChatChannelsModule chatChannelsModule;
    public GoldToolModule goldToolModule;

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
        jetBootModule = new JetBootModule(this);
        captureBallModule = new CaptureBallModule(this);
        chatChannelsModule = new ChatChannelsModule(this);
        goldToolModule = new GoldToolModule(this);

        /*if(useMysql) {
            sql = new SqlHandler(username, password, address, port, database, this);
        } else {
            sql = new SqlHandler(this);
        }*/

        pm.registerEvents(new SkcraftEventHandler(this), this);

        if(getConfig().getString("Spawn-Location").equalsIgnoreCase("")) {
            Location loc = Bukkit.getWorlds().get(0).getSpawnLocation();
            getConfig().set("Spawn-Location", loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "," + loc.getYaw() + "," + loc.getPitch());
        }

        logger.info("[SkcraftBasics] Loading data from configs.");
        loadStargatesFromFile();
        loadFlyersFromFile();
        loadBeaconsFromFile();
        loadChatChannelsFromFile();
        logger.info("[SkcraftBasics] Finished loading data from configs.");

        jetBootModule.registerJetbootDurabilityCheck();
        jetBootModule.registerBeaconCheck();

        skcraftCommandHandler = new SkcraftCommandHandler(this);

        this.getCommand("invite").setExecutor(skcraftCommandHandler);
        this.getCommand("accept").setExecutor(skcraftCommandHandler);
        this.getCommand("setspawn").setExecutor(skcraftCommandHandler);
        this.getCommand("nethercoords").setExecutor(skcraftCommandHandler);
        this.getCommand("here").setExecutor(skcraftCommandHandler);
        this.getCommand("join").setExecutor(skcraftCommandHandler);
        this.getCommand("leave").setExecutor(skcraftCommandHandler);
        this.getCommand("g").setExecutor(skcraftCommandHandler);

        logger.info("has started.");
    }

    public void onDisable() {

        logger.info("[SkcraftBasics] Saving data to configs.");
        saveStargatesToFile();
        saveFlyersToFile();
        saveBeaconsToFile();
        saveChatChannelsToFile();
        logger.info("[SkcraftBasics] Finished saving data to configs.");

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

    public void loadChatChannelsFromFile() {
        List<String> channels = getConfig().getStringList("Chat-Channels-List");

        for(String channel : channels) {
            chatChannelsModule.chatChannels.put(channel, getConfig().getStringList("Chat-Channels." + channel));
        }
    }

    public void saveChatChannelsToFile() {
        getConfig().set("Chat-Channels", null);

        List<String> channels = new ArrayList<>();

        for(Map.Entry<String, List<String>> entry : chatChannelsModule.chatChannels.entrySet()) {
            channels.add(entry.getKey());
            getConfig().set("Chat-Channels." + entry.getKey(), entry.getValue());
        }
        getConfig().set("Chat-Channels-List", channels);
    }

    public void loadBeaconsFromFile() {
        List<String> beacons = getConfig().getStringList("Active-Beacons");

        for(String locationString : beacons) {
            String[] args = locationString.split(",");
            Location location = new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
            jetBootModule.activeBeacons.add(location);
        }
    }

    public void saveBeaconsToFile() {
        List<String> beaconLocations = new ArrayList<>();
        for(Location location : jetBootModule.activeBeacons) {
            beaconLocations.add(location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ());
        }

        getConfig().set("Active-Beacons", beaconLocations);
    }

    public void loadFlyersFromFile() {
        List<String> stringData = getConfig().getStringList("Jetboot-Players");
        Iterator it = stringData.iterator();

        while(it.hasNext()) {
            String player = (String)it.next();
            jetBootModule.jetboots.add(player);
        }

        stringData = getConfig().getStringList("Flying-Players");
        it = stringData.iterator();

        while(it.hasNext()) {
            String player = (String)it.next();
            jetBootModule.flyingPlayers.add(player);
        }
    }

    public void saveFlyersToFile() {
        Iterator it = jetBootModule.jetboots.iterator();
        List<String> stringData = new ArrayList<>();

        while(it.hasNext()) {
            String player = (String)it.next();
            stringData.add(player);
        }

        getConfig().set("Jetboot-Players", stringData);

        it = jetBootModule.flyingPlayers.iterator();
        stringData = new ArrayList<>();

        while(it.hasNext()) {
            String player = (String)it.next();
            stringData.add(player);
        }

        getConfig().set("Flying-Players", stringData);
    }

    public void loadStargatesFromFile() {
        List<String> networksList = (List<String>)networksConfig.getList("Networks-List");
        Iterator networkIt = networksList.iterator();

        while(networkIt.hasNext()) {
            String network = (String)networkIt.next();
            List<String> stargatesList = (List<String>)networksConfig.getList("Networks." + network);

            stargateModule.networkList.put(network, stargatesList);

            Iterator stargateIt = stargatesList.iterator();
            while(stargateIt.hasNext()) {
                String stargate = (String)stargateIt.next();
                String owner = stargatesConfig.getString(stargate  + ".Owner");
                network = stargatesConfig.getString(stargate + ".Network");
                String[] teleportLocationString = stargatesConfig.getString(stargate + ".Teleport-Location").split(",");
                String[] signLocationString = stargatesConfig.getString(stargate + ".Sign-Location").split(",");
                String[] buttonLocationString = stargatesConfig.getString(stargate + ".Button-Location").split(",");
                String direction = stargatesConfig.getString(stargate + ".Direction");

                List<String> blocksString = (List<String>)stargatesConfig.getList(stargate + ".Blocks");
                List<String> portalBlocksString = (List<String>)stargatesConfig.getList(stargate + ".Portal-Blocks");

                Location teleportLocation = new Location(Bukkit.getWorld(teleportLocationString[0]), Double.valueOf(teleportLocationString[1]), Double.valueOf(teleportLocationString[2]), Double.valueOf(teleportLocationString[3]), Float.valueOf(teleportLocationString[4]), -0);
                Location signLocation = new Location(Bukkit.getWorld(signLocationString[0]), Double.valueOf(signLocationString[1]), Double.valueOf(signLocationString[2]), Double.valueOf(signLocationString[3]));
                Location buttonLocation = new Location(Bukkit.getWorld(buttonLocationString[0]), Double.valueOf(buttonLocationString[1]), Double.valueOf(buttonLocationString[2]), Double.valueOf(buttonLocationString[3]));

                List<Location> blocks = new ArrayList<>();
                List<Location> portalBlocks = new ArrayList<>();

                Iterator it = blocksString.iterator();
                while(it.hasNext()) {
                    String[] blockLocation = ((String)it.next()).split(",");
                    Location block = new Location(Bukkit.getWorld(blockLocation[0]), Double.valueOf(blockLocation[1]), Double.valueOf(blockLocation[2]), Double.valueOf(blockLocation[3]));

                    block.getBlock().setMetadata("Stargate", new FixedMetadataValue(this, stargate));

                    blocks.add(block);
                }

                it = portalBlocksString.iterator();
                while(it.hasNext()) {
                    String[] blockLocation = ((String)it.next()).split(",");
                    Location block = new Location(Bukkit.getWorld(blockLocation[0]), Double.valueOf(blockLocation[1]), Double.valueOf(blockLocation[2]), Double.valueOf(blockLocation[3]));

                    portalBlocks.add(block);
                }

                signLocation.getBlock().setMetadata("Stargate", new FixedMetadataValue(this, stargate));

                Sign sign = ((Sign)signLocation.getBlock().getState());
                sign.setLine(0, "-" + stargate + "-");
                sign.setLine(1, "Right click");
                sign.setLine(2, "to use gate");
                sign.setLine(3, "(" + network + ")");
                sign.update();

                buttonLocation.getBlock().setMetadata("Stargate", new FixedMetadataValue(this, stargate));

                stargateModule.stargateList.put(stargate, new Stargate(owner, network, teleportLocation, signLocation, buttonLocation, blocks, portalBlocks, direction));
            }
        }
    }

    public void saveStargatesToFile() {
        List<String> networks = new ArrayList<>();

        for(HashMap.Entry<String, List<String>> entryNetwork : stargateModule.networkList.entrySet()) {
            networksConfig.set("Networks." + entryNetwork.getKey(), entryNetwork.getValue());
            networks.add(entryNetwork.getKey());
        }

        networksConfig.set("Networks-List", networks);

        for(HashMap.Entry<String, Stargate> entryStargate : stargateModule.stargateList.entrySet()) {
            stargatesConfig.set(entryStargate.getKey() + ".Owner", entryStargate.getValue().getOwner());
            stargatesConfig.set(entryStargate.getKey() + ".Network", entryStargate.getValue().getNetwork());
            stargatesConfig.set(entryStargate.getKey() + ".Teleport-Location", entryStargate.getValue().getTeleportLocation().getWorld().getName() + ","
                    + entryStargate.getValue().getTeleportLocation().getX() + ","
                    + entryStargate.getValue().getTeleportLocation().getY() + ","
                    + entryStargate.getValue().getTeleportLocation().getZ() + ","
                    + entryStargate.getValue().getTeleportLocation().getYaw());
            stargatesConfig.set(entryStargate.getKey() + ".Sign-Location", entryStargate.getValue().getSignLocation().getWorld().getName() + ","
                    + entryStargate.getValue().getSignLocation().getX() + ","
                    + entryStargate.getValue().getSignLocation().getY() + ","
                    + entryStargate.getValue().getSignLocation().getZ());
            stargatesConfig.set(entryStargate.getKey() + ".Button-Location", entryStargate.getValue().getButtonLocation().getWorld().getName() + ","
                    + entryStargate.getValue().getButtonLocation().getX() + ","
                    + entryStargate.getValue().getButtonLocation().getY() + ","
                    + entryStargate.getValue().getButtonLocation().getZ());
            stargatesConfig.set(entryStargate.getKey() + ".Direction", entryStargate.getValue().getDirection());

            List<String> blocks = new ArrayList<>();

            Iterator it = entryStargate.getValue().getBlocks().iterator();
            while(it.hasNext()) {
                Location blockLocation = (Location)it.next();

                blocks.add(blockLocation.getWorld().getName() + ","
                        + blockLocation.getX() + ","
                        + blockLocation.getY() + ","
                        + blockLocation.getZ());
            }

            List<String> portalBlocks = new ArrayList<>();

            it = entryStargate.getValue().getPortalBlocks().iterator();
            while(it.hasNext()) {
                Location blockLocation = (Location)it.next();

                portalBlocks.add(blockLocation.getWorld().getName() + ","
                        + blockLocation.getX() + ","
                        + blockLocation.getY() + ","
                        + blockLocation.getZ());
            }

            stargatesConfig.set(entryStargate.getKey() + ".Blocks", blocks);
            stargatesConfig.set(entryStargate.getKey() + ".Portal-Blocks", portalBlocks);
        }
    }

    public boolean checkOnline(String uuid) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.getUniqueId().toString().equals(uuid)) {
                return true;
            }
        }
        return false;
    }
}
