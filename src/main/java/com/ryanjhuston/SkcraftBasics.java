package com.ryanjhuston;

import com.ryanjhuston.Database.SqlHandler;
import com.ryanjhuston.Modules.*;
import com.ryanjhuston.Types.EnderTurret;
import com.ryanjhuston.Types.Shop;
import com.ryanjhuston.Types.SkcraftPlayer;
import com.ryanjhuston.Types.Stargate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class SkcraftBasics extends JavaPlugin {

    public Logger logger = Logger.getLogger("Minecraft");
    private PluginManager pm = Bukkit.getPluginManager();
    private SkcraftCommandHandler skcraftCommandHandler;

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

    private File stargatesFile = new File(getDataFolder(), "stargates.yml");
    private File networksFile = new File(getDataFolder(), "stargateNetworks.yml");
    private File turretsFile = new File(getDataFolder(), "enderTurrets.yml");
    private File shopsFile = new File(getDataFolder(), "shops.yml");
    public File playersDir = new File(getDataFolder(), "Players");
    private FileConfiguration stargatesConfig;
    private FileConfiguration networksConfig;
    private FileConfiguration turretsConfig;
    private FileConfiguration shopsConfig;

    public HashMap<String, SkcraftPlayer> skcraftPlayerList = new HashMap<>();
    public Location spawnLocation;
    public SkcraftWorldManager worldManager;
    public List<World> worlds = new ArrayList<>();
    public List<String> interactCooldown = new ArrayList<>();
    public List<String> disabledCommands = new ArrayList<>();

    public List<String> enabledModules = new ArrayList<>();

    public String debug = "[SkcraftBasics Debug] ";

    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        createCustomConfigs();
        saveConfigs();

        enabledModules = getConfig().getStringList("Enabled-Modules");

        enderPearlTeleportModule = new EnderPearlTeleportModule(this);
        craftingModule = new CraftingModule(this);
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
        pm.registerEvents(craftingModule, this);
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
        playersDir.mkdirs();
        loadStargatesFromFile();
        loadBeaconsFromFile();
        loadChatChannelsFromFile();
        loadTurretsFromFile();
        loadShopsFromFile();
        logger.info("[SkcraftBasics] Finished loading data from configs.");

        jetBootModule.registerJetbootDurabilityCheck();
        jetBootModule.registerBeaconCheck();

        skcraftCommandHandler = new SkcraftCommandHandler(this);
        worldManager = new SkcraftWorldManager(this);

        for(String world : getConfig().getStringList("Loaded-Worlds")) {
            try {
                worldManager.loadWorld(world);
            } catch (CommandException e) {
                e.printStackTrace();
            }
        }

        this.getCommand("invite").setExecutor(skcraftCommandHandler);
        this.getCommand("accept").setExecutor(skcraftCommandHandler);
        this.getCommand("paccept").setExecutor(skcraftCommandHandler);
        this.getCommand("sb").setExecutor(skcraftCommandHandler);
        this.getCommand("nethercoords").setExecutor(skcraftCommandHandler);
        this.getCommand("here").setExecutor(skcraftCommandHandler);
        this.getCommand("join").setExecutor(skcraftCommandHandler);
        this.getCommand("leave").setExecutor(skcraftCommandHandler);
        this.getCommand("g").setExecutor(skcraftCommandHandler);
        this.getCommand("help").setExecutor(skcraftCommandHandler);
        this.getCommand("worldmanager").setExecutor(skcraftCommandHandler);
        this.getCommand("wm").setExecutor(skcraftCommandHandler);

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

        getConfig().set("Loaded-Worlds", worldNames);

        saveConfig();
        saveConfigs();
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

        createCustomConfigs();

        betterPistonsModule.updateConfig(this);
        captureBallModule.updateConfig(this);
        chatChannelsModule.updateConfig(this);
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
            saveResource("stargates.yml", false);
        }

        if(!networksFile.exists()) {
            networksFile.getParentFile().mkdirs();
            saveResource("stargateNetworks.yml", false);
        }

        if(!turretsFile.exists()) {
            turretsFile.getParentFile().mkdirs();
            saveResource("enderTurrets.yml", false);
        }

        if(!shopsFile.exists()) {
            shopsFile.getParentFile().mkdirs();
            saveResource("shops.yml", false);
        }

        stargatesConfig = new YamlConfiguration();
        networksConfig = new YamlConfiguration();
        turretsConfig = new YamlConfiguration();
        shopsConfig = new YamlConfiguration();

        try {
            stargatesConfig.load(stargatesFile);
            networksConfig.load(networksFile);
            turretsConfig.load(turretsFile);
            shopsConfig.load(shopsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void saveConfigs() {
        saveConfig();
        try {
            stargatesConfig.save(stargatesFile);
            networksConfig.save(networksFile);
            turretsConfig.save(turretsFile);
            shopsConfig.save(shopsFile);
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

    public void loadShopsFromFile() {
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
    }

    public void loadBeaconsFromFile() {
        List<String> beacons = getConfig().getStringList("Active-Beacons");

        for(String locationString : beacons) {
            if(stringToLocation(locationString).getBlock().getType() == Material.BEACON) {
                jetBootModule.activeBeacons.add(stringToLocation(locationString));
            }
        }
    }

    public void saveBeaconsToFile() {
        List<String> beaconLocations = new ArrayList<>();
        for(Location location : jetBootModule.activeBeacons) {
            beaconLocations.add(locationToString(location));
        }

        getConfig().set("Active-Beacons", beaconLocations);
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
                String direction = stargatesConfig.getString(stargate + ".Direction");

                List<String> blocksString = (List<String>)stargatesConfig.getList(stargate + ".Blocks");
                List<String> portalBlocksString = (List<String>)stargatesConfig.getList(stargate + ".Portal-Blocks");

                Location teleportLocation = stringToLocation(stargatesConfig.getString(stargate + ".Teleport-Location"));
                Location signLocation = stringToLocation(stargatesConfig.getString(stargate + ".Sign-Location"));
                Location buttonLocation = stringToLocation(stargatesConfig.getString(stargate + ".Button-Location"));

                List<Location> blocks = new ArrayList<>();
                List<Location> portalBlocks = new ArrayList<>();

                Iterator it = blocksString.iterator();
                while(it.hasNext()) {
                    Location block = stringToLocation((String)it.next());

                    block.getBlock().setMetadata("Stargate", new FixedMetadataValue(this, stargate));

                    blocks.add(block);
                }

                it = portalBlocksString.iterator();
                while(it.hasNext()) {
                    portalBlocks.add(stringToLocation((String)it.next()));
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
    }

    public void loadTurretsFromFile() {
        List<String> turretLocations = turretsConfig.getStringList("Turrets");

        for(String locationString : turretLocations) {
            Location location = stringToLocation(locationString);

            FallingBlock dummy = location.getWorld().spawnFallingBlock(location, location.getBlock().getBlockData());
            dummy.setHurtEntities(false);

            for(Entity entity : dummy.getNearbyEntities(1, 1, 1)) {
                if(entity.getType() == EntityType.ENDER_CRYSTAL) {
                    mobTurretModule.getTurretList().add(new EnderTurret((EnderCrystal)entity));
                }
            }

            dummy.remove();
        }
    }

    public void saveTurretsToFile() {
        List<String> locationList = new ArrayList<>();

        for(EnderTurret turret : mobTurretModule.getTurretList()) {
            locationList.add(locationToString(turret.getTurretLocation()));
        }

        turretsConfig.set("Turrets", locationList);
    }

    public String locationToString(Location location) {
        return (location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ());
    }

    public Location stringToLocation(String location) {
        String[] args = location.split(",");
        if(args.length == 4) {
            return new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
        } else {
            return new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]), Float.valueOf(args[4]), -0);
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

    public void savePlayerToFile(Player player) {
        String uuid = player.getUniqueId().toString();
        SkcraftPlayer skcraftPlayer = skcraftPlayerList.get(uuid);

        skcraftPlayer.getConfig().set("TeleportItem" , skcraftPlayer.getTeleportItem().toString());
        skcraftPlayer.getConfig().set("WasFlying", player.isFlying());
        skcraftPlayer.getConfig().set("PermanentTeleAuthed", skcraftPlayer.getPTeleAuthed());
        skcraftPlayer.getConfig().set("TeleAuthed", skcraftPlayer.getTeleAuthed());
        skcraftPlayer.getConfig().set("IsAdmin", skcraftPlayer.isAdmin());

        File playerFile = new File(playersDir, uuid + ".yml");

        try {
            skcraftPlayer.getConfig().save(playerFile);
        }catch (IOException e){
            e.printStackTrace();
        }

        skcraftPlayerList.remove(uuid);
    }
}
