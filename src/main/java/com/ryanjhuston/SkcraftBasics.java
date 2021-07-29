package com.ryanjhuston;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ryanjhuston.Database.SqlHandler;
import com.ryanjhuston.Modules.*;
import com.ryanjhuston.Types.EnderTurret;
import com.ryanjhuston.Types.Serializers.*;
import com.ryanjhuston.Types.Shop;
import com.ryanjhuston.Types.SkcraftPlayer;
import com.ryanjhuston.Types.Stargate;
import net.luckperms.api.LuckPerms;
import org.bukkit.*;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class SkcraftBasics extends JavaPlugin {

    public Logger logger = Logger.getLogger("Minecraft");
    final PluginManager pm = Bukkit.getPluginManager();
    private SkcraftCommandHandler skcraftCommandHandler;

    public LuckPerms luckPerms;

    private SqlHandler sql;

    public boolean useMysql;
    private String username;
    private String password;
    private String address;
    private int port;
    private String database;

    public EnderPearlTeleportModule enderPearlTeleportModule;
    public CraftingModule craftingModule;
    public StargateModule stargateModule;
    public JetBootModule jetBootModule;
    public CaptureBallModule captureBallModule;
    public ChatChannelsModule chatChannelsModule;
    public GoldToolModule goldToolModule;
    public RailModule railModule;
    public RotatorModule rotatorModule;
    public BetterPistonsModule betterPistonsModule;
    public AfkModule afkModule;
    public MobTurretModule mobTurretModule;
    public ShopModule shopModule;
    public GalapagosModule galapagosModule;
    public InkSignModule inkSignModule;
    public MiningWorldModule miningWorldModule;
    public ChunkLoaderModule chunkLoaderModule;

    public final File stargateDir = new File(getDataFolder(), "Stargates");
    public final File worldsDir = new File(getDataFolder(), "WorldManager");
    public final File playersDir = new File(getDataFolder(), "Players");
    public final File inventoriesDir = new File(playersDir, "Inventories");

    private final File legacyStargatesFile = new File(getDataFolder(), "stargates.yml");
    private final File legacyNetworksFile = new File(getDataFolder(), "stargateNetworks.yml");
    private final File legacyTurretsFile = new File(getDataFolder(), "enderTurrets.yml");
    private final File legacyShopsFile = new File(getDataFolder(), "shops.yml");

    private final File stargatesFile = new File(stargateDir, "stargates.json");
    private final File networksFile = new File(stargateDir, "stargateNetworks.json");
    private final File turretsFile = new File(getDataFolder(), "enderTurrets.json");
    private final File shopsFile = new File(getDataFolder(), "shops.json");
    private final File beaconsFile = new File(getDataFolder(), "beacons.json");
    private final File chatChannelsFile = new File(getDataFolder(), "chatchannels.json");
    private final File worldsFile = new File(getDataFolder(), "worldmanager.json");

    private FileConfiguration stargatesConfig;
    private FileConfiguration networksConfig;
    private FileConfiguration turretsConfig;
    private FileConfiguration shopsConfig;

    private HashMap<String, SkcraftPlayer> skcraftPlayerList = new HashMap<>();
    public Location spawnLocation;
    public SkcraftWorldManager worldManager;
    public List<World> worlds = new ArrayList<>();

    public HashMap<String, String> worldGenerators = new HashMap<>();

    public List<String> disabledCommands = new ArrayList<>();

    public List<String> enabledModules = new ArrayList<>();

    public List<String> interactCooldown = new ArrayList<>();

    public String debug = "[SkcraftBasics Debug] ";

    public ObjectMapper mapper = new ObjectMapper();

    public void onEnable() {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        createCustomConfigs();

        enabledModules = getConfig().getStringList("Enabled-Modules");

        enderPearlTeleportModule = new EnderPearlTeleportModule(this);

        if(enabledModules.contains("Crafting")) {
            craftingModule = new CraftingModule(this);
        }

        stargateModule = new StargateModule(this);
        jetBootModule = new JetBootModule(this);
        captureBallModule = new CaptureBallModule(this);
        chatChannelsModule = new ChatChannelsModule(this);
        goldToolModule = new GoldToolModule(this);
        railModule = new RailModule(this);
        rotatorModule = new RotatorModule(this);
        betterPistonsModule = new BetterPistonsModule(this);
        afkModule = new AfkModule(this);
        mobTurretModule = new MobTurretModule(this);
        shopModule = new ShopModule(this);
        galapagosModule = new GalapagosModule(this);
        inkSignModule = new InkSignModule(this);
        miningWorldModule = new MiningWorldModule(this);
        chunkLoaderModule = new ChunkLoaderModule(this);

        /*if(useMysql) {
            sql = new SqlHandler(username, password, address, port, database, this);
        } else {
            sql = new SqlHandler(this);
        }*/

        pm.registerEvents(new SkcraftEventHandler(this), this);

        pm.registerEvents(enderPearlTeleportModule, this);

        if(enabledModules.contains("Crafting")) {
            pm.registerEvents(craftingModule, this);
        }

        pm.registerEvents(stargateModule, this);
        pm.registerEvents(jetBootModule, this);
        pm.registerEvents(captureBallModule, this);
        pm.registerEvents(chatChannelsModule, this);
        pm.registerEvents(goldToolModule, this);
        pm.registerEvents(railModule, this);
        pm.registerEvents(rotatorModule, this);
        pm.registerEvents(betterPistonsModule, this);
        pm.registerEvents(afkModule, this);
        pm.registerEvents(mobTurretModule, this);
        pm.registerEvents(shopModule, this);
        pm.registerEvents(galapagosModule, this);
        pm.registerEvents(inkSignModule, this);
        pm.registerEvents(miningWorldModule, this);
        pm.registerEvents(chunkLoaderModule, this);

        if(getConfig().getString("Spawn-Location").equalsIgnoreCase("")) {
            spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
        } else {
            spawnLocation = stringToLocation(getConfig().getString("Spawn-Location"));
        }

        logger.info("[SkcraftBasics] Loading data from configs.");
        stargateDir.mkdirs();
        playersDir.mkdirs();
        inventoriesDir.mkdirs();
        loadChatChannelsFromFile();
        logger.info("[SkcraftBasics] Finished loading data from configs.");

        jetBootModule.registerJetbootDurabilityCheck();
        jetBootModule.registerBeaconCheck();

        skcraftCommandHandler = new SkcraftCommandHandler(this);
        worldManager = new SkcraftWorldManager(this);

        for(String world : getConfig().getStringList("Worlds")) {
            try {
                String generator = getConfig().getString("World-Settings." + world + ".Generator");
                boolean loaded = getConfig().getBoolean("World-Settings." + world + ".Loaded");

                if(generator != null) {
                    worldGenerators.put(world, generator);
                }

                if(loaded) {
                    worldManager.loadWorld(world);
                }
            } catch (CommandException e) {
                e.printStackTrace();
            }
        }

        boolean containsMining = false;
        for(World world : worlds) {
                if(world.getName().equals("Mining")) {
                    containsMining = true;
                    break;
                }
        }

        if(!containsMining && enabledModules.contains("MiningWorld")) {
            logger.info("[SkcraftBasics] Mining World Module enabled but Mining world not found. Creating world:");
            worldManager.createWorld("Mining", WorldType.NORMAL, World.Environment.NORMAL, null, null);
        }

        if(!enabledModules.contains("ChatChannels")) {
            disabledCommands.add("here");
            disabledCommands.add("join");
            disabledCommands.add("leave");
        }

        if(!enabledModules.contains("Invite")) {
            disabledCommands.add("invite");
        }

        if(legacyNetworksFile.exists() && legacyStargatesFile.exists()) {
            System.out.println("[SkcraftBasics] Legacy stargates files found. Converting to new format.");
            legacyLoadStargatesFromFile();
            legacyNetworksFile.renameTo(new File(getDataFolder(), "stargateNetworks.yml.legacy"));
            legacyStargatesFile.renameTo(new File(getDataFolder(), "stargates.yml.legacy"));
            saveStargatesToFile();
            System.out.println("[SkcraftBasics] Stargates successfully converted.");
        } else {
            loadStargatesFromFile();
        }

        if(getConfig().contains("Active-Beacons")) {
            System.out.println("[SkcraftBasics] Legacy jetboot beacons found. Converting to new format.");
            legacyLoadBeaconsFromFile();
            getConfig().set("Active-Beacons", null);
            saveBeaconsToFile();
            System.out.println("[SkcraftBasics] Beacons successfully converted.");
        } else {
            loadBeaconsFromFile();
        }

        if(legacyTurretsFile.exists()) {
            System.out.println("[SkcraftBasics] Legacy ender turrets files found. Converting to new format.");
            legacyLoadTurretsFromFile();
            legacyTurretsFile.renameTo(new File(getDataFolder(), "enderTurrets.yml.legacy"));
            saveTurretsToFile();
            System.out.println("[SkcraftBasics] Turrets successfully converted.");
        } else {
            loadTurretsFromFile();
        }

        if(legacyShopsFile.exists()) {
            System.out.println("[SkcraftBasics] Legacy shops files found. Converting to new format.");
            legacyLoadShopsFromFile();
            legacyShopsFile.renameTo(new File(getDataFolder(), "shops.yml.legacy"));
            saveShopsToFile();
            System.out.println("[SkcraftBasics] Shops successfully converted.");
        } else {
            loadShopsFromFile();
        }

        /*if(!worldsFile.exists() && getConfig().contains("Worlds")) {
            legacyLoadWorldsFromFile();
        } else {
            loadWorldsFromFile();
        }*/

        if(getConfig().contains("Chat-Channels-List")) {
            System.out.println("[SkcraftBasics] Legacy chat channels data found. Converting to new format.");
            legacyLoadChatChannelsFromFile();
            getConfig().set("Chat-Channels-List", null);
            saveChatChannelsToFile();
            System.out.println("[SkcraftBasics] Chat channels successfully converted.");
        } else {
            loadChatChannelsFromFile();
        }

        this.getCommand("invite").setExecutor(skcraftCommandHandler);
        this.getCommand("accept").setExecutor(skcraftCommandHandler);
        this.getCommand("paccept").setExecutor(skcraftCommandHandler);
        this.getCommand("sb").setExecutor(skcraftCommandHandler);
        this.getCommand("sba").setExecutor(skcraftCommandHandler);
        this.getCommand("nethercoords").setExecutor(skcraftCommandHandler);
        this.getCommand("here").setExecutor(skcraftCommandHandler);
        this.getCommand("join").setExecutor(skcraftCommandHandler);
        this.getCommand("leave").setExecutor(skcraftCommandHandler);
        this.getCommand("mod").setExecutor(skcraftCommandHandler);
        this.getCommand("worldmanager").setExecutor(skcraftCommandHandler);
        this.getCommand("wm").setExecutor(skcraftCommandHandler);

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if(provider != null) {
            logger.info("LuckPerms found, loading api.");

            luckPerms = provider.getProvider();
        }

        logger.info("has started.");
    }

    public void onDisable() {

        for(Player player : Bukkit.getOnlinePlayers()) {
            savePlayerToFile(player);
        }

        logger.info("[SkcraftBasics] Saving data to configs.");
        saveStargatesToFile();
        saveBeaconsToFile();
        saveChatChannelsToFile();
        saveTurretsToFile();
        saveShopsToFile();
        logger.info("[SkcraftBasics] Finished saving data to configs.");

        getConfig().set("Spawn-Location", locationToString(spawnLocation));

        List<String> worldNames = new ArrayList<>();

        for(World world : worlds) {
            worldNames.add(world.getName());
        }

        getConfig().set("Worlds", worldNames);

        for(String world : worldNames) {
            getConfig().set("World-Settings." + world + ".Generator", worldGenerators.get(world));
            getConfig().set("World-Settings." + world + ".Loaded", (Bukkit.getWorld(world) != null));
        }

        saveConfig();
        logger.info("[SkcraftBasics] has stopped.");
    }

    private void loadConfig() {
        this.useMysql = getConfig().getBoolean("Use-Mysql");
        if(useMysql) {
            this.username = getConfig().getString("Mysql.Username");
            this.password = getConfig().getString("Mysql.Password");
            this.address = getConfig().getString("Mysql.Address");
            this.port = getConfig().getInt("Mysql.Port");
            this.database = getConfig().getString("Database");
        }

        this.disabledCommands = getConfig().getStringList("DisabledCommands");
        this.enabledModules = getConfig().getStringList("Enabled-Modules");
    }

    public void reloadPlugin() {
        logger.info("[SkcraftBasics] Reloading config...");
        reloadConfig();
        loadConfig();

        if(enabledModules.contains("ChatChannels")) {
            disabledCommands.remove("here");
            disabledCommands.remove("join");
            disabledCommands.remove("leave");
        } else {
            disabledCommands.add("here");
            disabledCommands.add("join");
            disabledCommands.add("leave");
        }

        if(enabledModules.contains("Invite")) {
            disabledCommands.remove("invite");
        } else {
            disabledCommands.add("invite");
        }

        createCustomConfigs();

        betterPistonsModule.updateConfig(this);
        captureBallModule.updateConfig(this);
        chatChannelsModule.updateConfig(this);
        chunkLoaderModule.updateConfig(this);
        craftingModule.updateConfig(this);
        enderPearlTeleportModule.updateConfig(this);
        galapagosModule.updateConfig(this);
        goldToolModule.updateConfig(this);
        inkSignModule.updateConfig(this);
        jetBootModule.updateConfig(this);
        miningWorldModule.updateConfig(this);
        mobTurretModule.updateConfig(this);
        railModule.updateConfig(this);
        rotatorModule.updateConfig(this);
        shopModule.updateConfig(this);
        stargateModule.updateConfig(this);

        logger.info("[SkcraftBasics] Config reloaded.");
    }

    private void createCustomConfigs() {
        if(!stargatesFile.exists()) {
            stargatesFile.getParentFile().mkdirs();
        }

        if(!networksFile.exists()) {
            networksFile.getParentFile().mkdirs();
        }

        if(!turretsFile.exists()) {
            turretsFile.getParentFile().mkdirs();
        }

        if(!shopsFile.exists()) {
            shopsFile.getParentFile().mkdirs();
        }

        if(legacyNetworksFile.exists()) {
            networksConfig = new YamlConfiguration();
            try {
                networksConfig.load(legacyNetworksFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        if(legacyStargatesFile.exists()) {
            stargatesConfig = new YamlConfiguration();
            try {
                stargatesConfig.load(legacyStargatesFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        if(legacyTurretsFile.exists()) {
            turretsConfig = new YamlConfiguration();
            try {
                turretsConfig.load(legacyTurretsFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        if(legacyShopsFile.exists()) {
            shopsConfig = new YamlConfiguration();
            try {
                shopsConfig.load(legacyShopsFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadChatChannelsFromFile() {
        if(chatChannelsFile.exists()) {
            try {
                SerializedChatChannel[] channels = mapper.readValue(chatChannelsFile, SerializedChatChannel[].class);

                for(SerializedChatChannel channel : channels) {
                    chatChannelsModule.chatChannels.put(channel.getChannel(), channel.getPlayers());
                }
            } catch (Exception e) {
                System.out.println("[SkcraftBasics] ERROR: It appears that the chat channel file's data is corrupted. Creating new chat channel file.");
            }
        }
    }

    public void legacyLoadChatChannelsFromFile() {
        List<String> channels = getConfig().getStringList("Chat-Channels-List");

        for(String channel : channels) {
            chatChannelsModule.chatChannels.put(channel, getConfig().getStringList("Chat-Channels." + channel));
        }
    }

    public void saveChatChannelsToFile() {
        List<SerializedChatChannel> serializedChatChannels = new ArrayList<>();

        for(Map.Entry pair : chatChannelsModule.chatChannels.entrySet()) {
            serializedChatChannels.add(new SerializedChatChannel((String)pair.getKey(), (List<String>)pair.getValue()));
        }

        try {
            mapper.writeValue(chatChannelsFile, serializedChatChannels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public void saveChatChannelsToFile() {
        getConfig().set("Chat-Channels", null);

        List<String> channels = new ArrayList<>();

        for(Map.Entry<String, List<String>> entry : chatChannelsModule.chatChannels.entrySet()) {
            channels.add(entry.getKey());
            getConfig().set("Chat-Channels." + entry.getKey(), entry.getValue());
        }
        getConfig().set("Chat-Channels-List", channels);
    }*/

    public void loadShopsFromFile() {
        if(shopsFile.exists()) {
            try {
                SerializedShop[] serializedShops = mapper.readValue(shopsFile, SerializedShop[].class);

                for(SerializedShop serializedShop : serializedShops) {
                    Shop shop = serializedShop.deserialize();

                    shopModule.getShops().put(shop.getShopLocation(), shop);
                }
            } catch (Exception e) {
                System.out.println("[SkcraftBasics] ERROR: It appears that the shop file's data is corrupted. Creating new shop file.");
            }
        }
    }

    public void legacyLoadShopsFromFile() {
        List<String> shopLocations = shopsConfig.getStringList("Shops");

        for(String stringLocation : shopLocations) {
            String path = stringLocation.replace(".", "^");
            shopModule.getShops().put(stringToLocation(stringLocation),
                    new Shop(shopsConfig.getString(path + ".Owner"),
                            Material.matchMaterial(shopsConfig.getString(path + ".ProductMaterial")),
                            shopsConfig.getInt(path + ".ProductAmount"),
                            Material.matchMaterial(shopsConfig.getString(path + ".PriceMaterial")),
                            shopsConfig.getInt(path + ".PriceAmount"),
                            stringToLocation(stringLocation).add(0, -1, 0)));
        }
    }

    public void saveShopsToFile() {
        List<SerializedShop> serializedShops = new ArrayList<>();

        for(Shop shop : shopModule.getShops().values()) {
            serializedShops.add(new SerializedShop(shop));
        }

        try {
            mapper.writeValue(shopsFile, serializedShops);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public void saveShopsToFile() {
        List<String> shopLocations = new ArrayList<>();

        Iterator it = shopModule.getShops().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String stringLocation = locationToString((Location)pair.getKey()).replace(".", "^");
            Shop shop = (Shop)pair.getValue();

            shopsConfig.set(stringLocation + ".Owner", shop.getOwner());
            shopsConfig.set(stringLocation + ".ProductMaterial", shop.getProduct().toString());
            shopsConfig.set(stringLocation + ".ProductAmount", shop.getProductAmount());
            shopsConfig.set(stringLocation + ".PriceMaterial", shop.getPrice().toString());
            shopsConfig.set(stringLocation + ".PriceAmount", shop.getPriceAmount());

            shopLocations.add(locationToString((Location)pair.getKey()));
        }

        shopsConfig.set("Shops", shopLocations);

        try {
            shopsConfig.save(shopsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public void loadBeaconsFromFile() {
        if(beaconsFile.exists()) {
            try {
                SerializedLocation[] beacons = mapper.readValue(beaconsFile, SerializedLocation[].class);

                for(SerializedLocation serializedLocation : beacons) {
                    jetBootModule.activeBeacons.add(serializedLocation.deserialize());
                }
            } catch (Exception e) {
                System.out.println("[SkcraftBasics] ERROR: It appears that the beacons file's data is corrupted. Creating new beacons file.");
            }
        }
    }

    public void legacyLoadBeaconsFromFile() {
        List<String> beacons = getConfig().getStringList("Active-Beacons");

        for(String locationString : beacons) {
            if(stringToLocation(locationString).getBlock().getType() == Material.BEACON) {
                jetBootModule.activeBeacons.add(stringToLocation(locationString));
            }
        }
    }

    public void saveBeaconsToFile() {
        List<SerializedLocation> beaconLocations = new ArrayList<>();

        for(Location location : jetBootModule.activeBeacons) {
            beaconLocations.add(new SerializedLocation(location));
        }

        try {
            mapper.writeValue(beaconsFile, beaconLocations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void saveBeaconsToFile() {
        List<String> beaconLocations = new ArrayList<>();
        for(Location location : jetBootModule.activeBeacons) {
            beaconLocations.add(locationToString(location));
        }

        getConfig().set("Active-Beacons", beaconLocations);

        saveConfig();
    }*/

    public void loadStargatesFromFile() {
        if(networksFile.exists()) {
            try {
                SerializedNetworks networks = mapper.readValue(networksFile, SerializedNetworks.class);

                if(networks.getNetworksList().isEmpty()) {
                    networks.getNetworksList().add("public");
                }

                if(networks.getNetworks().isEmpty()) {
                    stargateModule.networkList.put("public", new ArrayList<>());
                }

                for(SerializedNetwork serializedNetwork : networks.getNetworks()) {
                    stargateModule.networkList.put(serializedNetwork.getName(), serializedNetwork.getPortals());
                }
            } catch (Exception e) {
                System.out.println("[SkcraftBasics] ERROR: It appears that the stargate network file's data is corrupted. Creating new stargate network file.");
            }
        }

        if(stargatesFile.exists()) {
            try {
                SerializedStargate[] stargates = mapper.readValue(stargatesFile, SerializedStargate[].class);

                for(SerializedStargate serializedStargate : stargates) {
                    Stargate stargate = serializedStargate.deserialize();
                    stargateModule.updateStargateBlocks(stargate);
                    stargateModule.stargateList.put(stargate.getName(), stargate);
                }
            } catch (Exception e) {
                System.out.println("[SkcraftBasics] ERROR: It appears that the stargate file's data is corrupted. Creating new stargate file.");
            }
        }
    }

    public void legacyLoadStargatesFromFile() {
        List<String> networksList = (List<String>)networksConfig.getList("Networks-List");

        if(networksList.isEmpty()) {
            networksList.add("public");
        }

        for(String network : networksList) {
            List<String> stargatesList = (List<String>)networksConfig.getList("Networks." + network);

            stargateModule.networkList.put(network, stargatesList);

            for(String name : stargatesList) {
                String owner = stargatesConfig.getString(name  + ".Owner");
                network = stargatesConfig.getString(name + ".Network");
                String direction = stargatesConfig.getString(name + ".Direction");

                List<String> blocksString = (List<String>)stargatesConfig.getList(name + ".Blocks");
                List<String> portalBlocksString = (List<String>)stargatesConfig.getList(name + ".Portal-Blocks");

                Location teleportLocation = stringToLocation(stargatesConfig.getString(name + ".Teleport-Location"));
                Location signLocation = stringToLocation(stargatesConfig.getString(name + ".Sign-Location"));
                Location buttonLocation = stringToLocation(stargatesConfig.getString(name + ".Button-Location"));

                List<Location> blocks = new ArrayList<>();
                List<Location> portalBlocks = new ArrayList<>();

                for(String stringLocation : blocksString) {
                    blocks.add(stringToLocation(stringLocation));
                }

                for(String stringLocation : portalBlocksString) {
                    portalBlocks.add(stringToLocation(stringLocation));
                }

                Stargate stargate = new Stargate(name, owner, network, teleportLocation, signLocation, buttonLocation, blocks, portalBlocks, direction);

                stargateModule.updateStargateBlocks(stargate);

                stargateModule.stargateList.put(name, stargate);
            }
        }
    }

    public void saveStargatesToFile() {
        SerializedNetworks networks = new SerializedNetworks(stargateModule.networkList);
        List<SerializedStargate> stargates = new ArrayList<>();

        for(Stargate stargate : stargateModule.stargateList.values()) {
            stargates.add(new SerializedStargate(stargate));
        }

        try {
            mapper.writeValue(networksFile, networks);
            mapper.writeValue(stargatesFile, stargates);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void saveStargatesToFile() {
        List<String> networks = new ArrayList<>();

        for(HashMap.Entry<String, List<String>> entryNetwork : stargateModule.networkList.entrySet()) {
            networksConfig.set("Networks." + entryNetwork.getKey(), entryNetwork.getValue());
            networks.add(entryNetwork.getKey());
        }

        networksConfig.set("Networks-List", networks);

        for(HashMap.Entry<String, Stargate> entryStargate : stargateModule.stargateList.entrySet()) {
            stargatesConfig.set(entryStargate.getKey() + ".Owner", entryStargate.getValue().getOwner());
            stargatesConfig.set(entryStargate.getKey() + ".Network", entryStargate.getValue().getNetwork());
            stargatesConfig.set(entryStargate.getKey() + ".Teleport-Location", locationToString(entryStargate.getValue().getTeleportLocation()) + ","
                    + entryStargate.getValue().getTeleportLocation().getYaw());
            stargatesConfig.set(entryStargate.getKey() + ".Sign-Location", locationToString(entryStargate.getValue().getSignLocation()));
            stargatesConfig.set(entryStargate.getKey() + ".Button-Location", locationToString(entryStargate.getValue().getButtonLocation()));
            stargatesConfig.set(entryStargate.getKey() + ".Direction", entryStargate.getValue().getDirection());

            List<String> blocks = new ArrayList<>();

            Iterator it = entryStargate.getValue().getBlocks().iterator();
            while(it.hasNext()) {
                blocks.add(locationToString((Location)it.next()));
            }

            List<String> portalBlocks = new ArrayList<>();

            it = entryStargate.getValue().getPortalBlocks().iterator();
            while(it.hasNext()) {
                portalBlocks.add(locationToString((Location)it.next()));
            }

            stargatesConfig.set(entryStargate.getKey() + ".Blocks", blocks);
            stargatesConfig.set(entryStargate.getKey() + ".Portal-Blocks", portalBlocks);
        }

        try {
            stargatesConfig.save(stargatesFile);
            networksConfig.save(networksFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public void loadTurretsFromFile() {
        if(turretsFile.exists()) {
            try {
                SerializedLocation[] turrets = mapper.readValue(turretsFile, SerializedLocation[].class);

                for(SerializedLocation serializedLocation : turrets) {
                    Location location = serializedLocation.deserialize();

                    for(Entity entity : location.getWorld().getNearbyEntities(location, 1, 1, 1)) {
                        if(entity.getType() == EntityType.ENDER_CRYSTAL) {
                            mobTurretModule.getTurretList().add(new EnderTurret((EnderCrystal)entity));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("[SkcraftBasics] ERROR: It appears that the turret file's data is corrupted. Creating new turret file.");
            }
        }
    }

    public void legacyLoadTurretsFromFile() {
        List<String> turretLocations = turretsConfig.getStringList("Turrets");

        for(String locationString : turretLocations) {
            Location location = stringToLocation(locationString);

            for(Entity entity : location.getWorld().getNearbyEntities(location, 1, 1, 1)) {
                if(entity.getType() == EntityType.ENDER_CRYSTAL) {
                    mobTurretModule.getTurretList().add(new EnderTurret((EnderCrystal)entity));
                }
            }
        }
    }

    public void saveTurretsToFile() {
        List<SerializedLocation> turrets = new ArrayList<>();

        for(EnderTurret turret : mobTurretModule.getTurretList()) {
            turrets.add(new SerializedLocation(turret.getTurretLocation()));
        }

        try {
            mapper.writeValue(turretsFile, turrets);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void saveTurretsToFile() {
        List<String> locationList = new ArrayList<>();

        for(EnderTurret turret : mobTurretModule.getTurretList()) {
            locationList.add(locationToString(turret.getTurretLocation()));
        }

        turretsConfig.set("Turrets", locationList);

        try {
            turretsConfig.save(turretsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public static String locationToString(Location location) {
        return (location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ());
    }

    public static Location stringToLocation(String location) {
        String[] args = location.split(",");
        if(args.length == 4) {
            return new Location(Bukkit.getWorld(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]));
        } else {
            return new Location(Bukkit.getWorld(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Float.parseFloat(args[4]), -0);
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

    public void removeInteractCooldown(String uuid) {
        interactCooldown.add(uuid);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            interactCooldown.remove(uuid);
        }, 1);
    }

    public void savePlayerToFile(Player player) {
        String uuid = player.getUniqueId().toString();
        SkcraftPlayer skcraftPlayer = skcraftPlayerList.get(uuid);

        File playerFile = new File(playersDir, uuid + ".json");

        try {
            mapper.writeValue(playerFile, skcraftPlayer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        skcraftPlayerList.remove(uuid);
    }

    public SkcraftPlayer getSkcraftPlayer(Player player) {
        return skcraftPlayerList.get(player.getUniqueId().toString());
    }

    public void addSkcraftPlayer(SkcraftPlayer skcraftPlayer) {
        skcraftPlayerList.put(skcraftPlayer.getUuid(), skcraftPlayer);
    }

    public void removeSkcraftPlayer(Player player) {
        skcraftPlayerList.remove(player.getUniqueId().toString());
    }

    public void checkSleep() {
        SkcraftBasics plugin = this;
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            int sleeping = 0;
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(player.isSleeping() || afkModule.getAfkPlayers().contains(player.getUniqueId().toString()) || player.isSleepingIgnored() || !player.getWorld().equals(Bukkit.getWorlds().get(0)) || (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)) {
                    sleeping++;
                }
            }

            if(Bukkit.getOnlinePlayers().size() == 0) {
                return;
            }

            int percent = (sleeping*100)/Bukkit.getOnlinePlayers().size();

            if(percent >= 50) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.setStatistic(Statistic.TIME_SINCE_REST, 0);
                    }
                    Bukkit.getWorlds().get(0).setTime(1000);
                    Bukkit.getWorlds().get(0).setStorm(false);
                }, 20);
            }
        }, 1);
    }
}
