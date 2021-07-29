package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.Shop;
import com.ryanjhuston.Types.SkcraftPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShopModule implements Listener {

    private SkcraftBasics plugin;

    private HashMap<Location, Shop> shops = new HashMap<>();

    private boolean moduleEnabled;

    public ShopModule(SkcraftBasics plugin) {
        updateConfig(plugin);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if(!event.getLine(0).equalsIgnoreCase("[Shop]")) {
            return;
        }

        Block block = event.getBlock().getRelative(0, -1, 0);
        Player player = event.getPlayer();

        if(block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST && block.getType() != Material.BARREL) {
            player.sendMessage(ChatColor.RED + "There must be a chest placed below this sign to make a shop.");
            event.getBlock().breakNaturally();
            return;
        }

        if(event.getLine(1).isEmpty() || event.getLine(2).isEmpty()) {
            player.sendMessage(ChatColor.RED + "You must define a price and item to sell.");
            event.getBlock().breakNaturally();
            return;
        }

        String[] product = event.getLine(1).split(" ");
        String[] price = event.getLine(2).split(" ");

        if(product.length == 1 || price.length == 1) {
            correctUsage(player);
            event.getBlock().breakNaturally();
            return;
        }

        if(!isInteger(product[0]) || !isInteger(price[0])) {
            correctUsage(player);
            event.getBlock().breakNaturally();
            return;
        }

        int productAmount = Integer.parseInt(product[0]);
        int priceAmount = Integer.parseInt(price[0]);

        Material productMat = Material.matchMaterial(parseMaterialName(product[1]));
        Material priceMat = Material.matchMaterial(parseMaterialName(price[1]));

        if(productMat == null) {
            player.sendMessage(ChatColor.RED + "Invalid product.");
            event.getBlock().breakNaturally();
            return;
        }

        if(priceMat == null) {
            player.sendMessage(ChatColor.RED + "Invalid price.");
            event.getBlock().breakNaturally();
            return;
        }

        if(productAmount <= 0 || priceAmount <= 0) {
            player.sendMessage(ChatColor.RED + "The item amount can not be less than or equal to zero.");
            event.getBlock().breakNaturally();
            return;
        }

        event.setLine(0, "[Shop]");
        event.setLine(1, event.getLine(1).toLowerCase());
        event.setLine(2, event.getLine(2).toLowerCase());
        event.setLine(3, player.getName());
        shops.put(event.getBlock().getLocation(), new Shop(player.getUniqueId().toString(), productMat, productAmount, priceMat, priceAmount, event.getBlock().getRelative(0, -1, 0).getLocation()));
        player.sendMessage(ChatColor.YELLOW + "You successfully created a shop.");

        plugin.saveShopsToFile();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Shop shop = getShopFromBlock(event.getBlock());

        if(shop == null) {
            return;
        }

        SkcraftPlayer skcraftPlayer = plugin.getSkcraftPlayer(event.getPlayer());

        if(!skcraftPlayer.getUuid().equals(shop.getOwner()) && !skcraftPlayer.getIsAdmin()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to remove this shop.");
            return;
        }

        if(event.getBlock().getLocation().equals(shop.getShopLocation())) {
            shops.remove(shop.getShopLocation().add(0, 1, 0));
        } else {
            shops.remove(event.getBlock().getLocation());
        }
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Shop has been successfully removed.");

        plugin.saveShopsToFile();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if(event.getAction() == Action.PHYSICAL) {
            return;
        }

        if(event.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }

        if(plugin.interactCooldown.contains(event.getPlayer().getUniqueId().toString())) {
            return;
        }

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if(!event.getClickedBlock().getType().toString().endsWith("_SIGN")) {
            return;
        }

        if(!shops.containsKey(event.getClickedBlock().getLocation())) {
            return;
        }

        plugin.removeInteractCooldown(event.getPlayer().getUniqueId().toString());

        Shop shop = shops.get(event.getClickedBlock().getLocation());
        Player player = event.getPlayer();
        ItemStack price = new ItemStack(shop.getPrice(), shop.getPriceAmount());
        ItemStack product = new ItemStack(shop.getProduct(), shop.getProductAmount());

        if(player.isSneaking()) {
            player.sendMessage(ChatColor.YELLOW + "[Shop Info]");
            player.sendMessage(ChatColor.YELLOW + "Product: " + product.getAmount() + " " + product.getType().toString());
            player.sendMessage(ChatColor.YELLOW + "Price: " + price.getAmount() + " " + price.getType().toString());
            event.setCancelled(true);
            return;
        }

        if(!event.getPlayer().getInventory().containsAtLeast(price, shop.getPriceAmount())) {
            player.sendMessage(ChatColor.RED + "You do not have sufficient funds to buy this.");
            return;
        }

        InventoryHolder inventoryHolder = (InventoryHolder)shop.getShopLocation().getBlock().getState();
        List<ItemStack> productList = hasSufficientStock(inventoryHolder.getInventory(), product.getType(), shop.getProductAmount());

        List<ItemStack> priceList = new ArrayList<>();
        priceList.add(price);

        if(productList.isEmpty()) {
            player.sendMessage(ChatColor.RED + "This shop does not have enough of the product to sell.");
            return;
        }

        if(!spaceForTrade(inventoryHolder.getInventory(), productList, player.getInventory(), priceList)) {
            player.sendMessage(ChatColor.RED + "There is not enough space for this trade.");
            return;
        }

        player.getInventory().removeItem(price);

        for(ItemStack item : productList) {
            inventoryHolder.getInventory().removeItem(item);
            player.getInventory().addItem(item);
        }

        inventoryHolder.getInventory().addItem(price);

        player.sendMessage(ChatColor.GREEN + "Transaction successful.");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        Shop shop = getShopFromBlock(event.getBlock());

        if(shop == null) {
            return;
        }

        SkcraftPlayer skcraftPlayer = plugin.getSkcraftPlayer(event.getPlayer());

        if(!skcraftPlayer.getUuid().equals(shop.getOwner()) && !skcraftPlayer.getIsAdmin()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot add a chest to this shop.");
            return;
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if(event.getInventory().getType() != InventoryType.CHEST && event.getInventory().getType() != InventoryType.BARREL) {
            return;
        }

        if(event.getInventory().getLocation() == null) {
            return;
        }

        Block block = event.getInventory().getLocation().getBlock();

        Shop shop = getShopFromBlock(block);

        if(shop == null) {
            return;
        }

        SkcraftPlayer skcraftPlayer = plugin.getSkcraftPlayer((Player)event.getPlayer());

        if(!skcraftPlayer.getUuid().equals(shop.getOwner()) && !skcraftPlayer.getIsAdmin()) {
            event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to open this shop.");
            event.setCancelled(true);
            return;
        }
    }

    public Shop getShopFromBlock(Block block) {
        if(block.getType().toString().endsWith("_SIGN")) {
            if(shops.containsKey(block.getLocation())) {
                return shops.get(block.getLocation());
            }
        }

        if(block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            Chest chest = (Chest)block.getState();
            DoubleChest doubleChest;

            if(block.getRelative(0, 1, 0).getType().toString().endsWith("_SIGN")) {
                Location sign = block.getRelative(0, 1, 0).getLocation();
                if(shops.containsKey(sign)) {
                    return shops.get(sign);
                }
            }

            if(chest.getInventory() instanceof DoubleChestInventory) {
                doubleChest = ((DoubleChestInventory) chest.getInventory()).getHolder();

                Location rightLocation = doubleChest.getRightSide().getInventory().getLocation();

                if(doubleChest.getRightSide().getInventory().getLocation().getBlock().getRelative(0, 1, 0).getType().toString().endsWith("_SIGN")) {
                    Location sign = doubleChest.getRightSide().getInventory().getLocation().getBlock().getRelative(0, 1, 0).getLocation();
                    if(shops.containsKey(sign)) {
                        return shops.get(sign);
                    }
                }

                BlockFace[] blockFaces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

                for(BlockFace blockFace : blockFaces) {
                    if(rightLocation.getBlock().getRelative(blockFace).getType() == Material.CHEST || rightLocation.getBlock().getRelative(blockFace).getType() == Material.TRAPPED_CHEST) {
                        Chest otherChest = (Chest) rightLocation.getBlock().getRelative(blockFace).getState();

                        if(otherChest.getInventory() instanceof DoubleChestInventory) {
                            DoubleChest otherDoubleChest = ((DoubleChestInventory)chest.getInventory()).getHolder();

                            if(otherDoubleChest.getRightSide().getInventory().getLocation().equals(doubleChest.getRightSide().getInventory().getLocation())) {
                                Location sign = otherChest.getBlock().getRelative(0, 1, 0).getLocation();
                                if(shops.containsKey(sign)) {
                                    return shops.get(sign);
                                }
                            }
                        }
                    }
                }
            }
        } else if(block.getType() == Material.BARREL) {
            if(block.getRelative(0, 1, 0).getType().toString().endsWith("_SIGN")) {
                if(shops.containsKey(block.getRelative(0, 1, 0).getLocation())) {
                    return shops.get(block.getRelative(0, 1, 0).getLocation());
                }
            }
        }

        return null;
    }

    @EventHandler
    public void onItemMoveInventory(InventoryMoveItemEvent event) {
        if(!moduleEnabled) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if(event.getSource().getType() != InventoryType.CHEST && event.getSource().getType() != InventoryType.BARREL) {
            return;
        }

        if(event.getDestination().getType() != InventoryType.PLAYER) {
            return;
        }

        if(!shops.containsKey(event.getSource().getLocation().getBlock().getRelative(0, 1, 0).getLocation())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPistonPush(BlockPistonExtendEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for(Block block : event.getBlocks()) {
            Shop shop = getShopFromBlock(block);

            if(shop != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonPull(BlockPistonRetractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for(Block block : event.getBlocks()) {
            Shop shop = getShopFromBlock(block);

            if(shop != null) {
                event.setCancelled(true);
            }
        }
    }

    private void correctUsage(Player player) {
        player.sendMessage(ChatColor.RED + "Correct Usage:");
        player.sendMessage(ChatColor.RED + "[Shop]");
        player.sendMessage(ChatColor.RED + "{quantity} {product}");
        player.sendMessage(ChatColor.RED + "{quantity} {cost}");
    }

    private boolean spaceForTrade(Inventory shop, List<ItemStack> product, Inventory player, List<ItemStack> price) {
        Inventory shopClone = Bukkit.createInventory(null, shop.getSize(), "");
        Inventory playerClone = Bukkit.createInventory(null, 36, "");

        shopClone.setContents(shop.getContents());
        for(int i = 0; i < 36; i++) {
            playerClone.setItem(i, player.getItem(i));
        }

        for(ItemStack item : product) {
            shopClone.removeItem(item);
        }

        for(ItemStack item : price) {
            playerClone.removeItem(item);
        }

        boolean hasSpace = true;

        for(ItemStack item : price) {
            HashMap<Integer, ItemStack> excessItems = shopClone.addItem(item);

            if(!excessItems.isEmpty()) {
                hasSpace = false;
            }
        }

        for(ItemStack item : product) {
            HashMap<Integer, ItemStack> excessItems = playerClone.addItem(item);

            if(!excessItems.isEmpty()) {
                hasSpace = false;
            }
        }
        return hasSpace;
    }

    private boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private List<ItemStack> hasSufficientStock(Inventory inventory, Material material, int amount) {
        List<ItemStack> items = new ArrayList<>();

        for(ItemStack item : inventory.getContents()) {
            if(item == null) {
                continue;
            }

            if(amount <= 0) {
                break;
            }

            if(item.getType() == material) {
                if(amount-item.getAmount() >= 0) {
                    items.add(item);
                    amount -= item.getAmount();
                } else {
                    ItemStack temp = item.clone();
                    temp.setAmount(amount);
                    items.add(temp);
                    amount -= temp.getAmount();
                }
            }
        }

        if(amount > 0) {
            items.clear();
        }

        return items;
    }

    public HashMap<Location, Shop> getShops() {
        return shops;
    }

    public String parseMaterialName(String material) {
        material = material.toLowerCase();

        if(material.startsWith("a_")) {
            material = material.replaceFirst("a_", "acacia_");
        }
        else if(material.startsWith("b_")) {
            material = material.replaceFirst("b_", "birch_");
        }
        else if(material.startsWith("do_")) {
            material = material.replaceFirst("do_", "dark_oak_");
        }
        else if(material.startsWith("j_")) {
            material = material.replaceFirst("j_", "jungle_");
        }
        else if(material.startsWith("o_")) {
            material = material.replaceFirst("o_", "oak_");
        }
        else if(material.startsWith("s_")) {
            material = material.replaceFirst("s_", "spruce_");
        }
        else if(material.startsWith("c_")) {
            material = material.replaceFirst("c_", "cooked_");
        }
        else if(material.startsWith("dp_")) {
            material = material.replaceFirst("dp_", "dark_prismarine_");
        }
        else if(material.startsWith("es_")) {
            material = material.replaceFirst("es_", "end_stone_");
        }
        else if(material.startsWith("rn_")) {
            material = material.replaceFirst("rn_", "red_nether_");
        }
        else if(material.startsWith("rs_")) {
            material = material.replaceFirst("rs_", "red_sandstone_");
        }
        else if(material.startsWith("ss_")) {
            material = material.replaceFirst("ss_", "smooth_sandstone_");
        }
        else if(material.startsWith("srs_")) {
            material = material.replaceFirst("srs_", "smooth_red_sandstone_");
        }
        else if(material.startsWith("f_")) {
            material = material.replaceFirst("f_", "firework_");
        }
        else if(material.startsWith("cbl_")) {
            material = material.replaceFirst("cbl_", "cobblestone_");
        }
        else if(material.startsWith("md_")) {
            material = material.replaceFirst("md_", "music_disc_");
        }

        if(material.endsWith("_pp")) {
            material = material.replace("_pp", "_pressure_plate");
        }
        else if(material.endsWith("_fg")) {
            material = material.replace("_fg", "_fence_gate");
        }
        else if(material.endsWith("_se")) {
            material = material.replace("_se", "_spawn_egg");
        }
        else if(material.endsWith("_cp")) {
            material = material.replace("_cp", "_concrete_powder");
        }
        else if(material.endsWith("_gt")) {
            material = material.replace("_gt", "_glazed_terracotta");
        }
        else if(material.endsWith("_t")) {
            material = material.replace("_t", "_terracotta");
        }
        else if(material.endsWith("_sb")) {
            material = material.replace("_sb", "_shulker_box");
        }
        else if(material.endsWith("_sg")) {
            material = material.replace("_sg", "_stained_glass");
        }
        else if(material.endsWith("_sgp")) {
            material = material.replace("_sgp", "_stained_glass_pane");
        }
        else if(material.endsWith("_cb")) {
            material = material.replace("_cb", "_coral_block");
        }
        else if(material.endsWith("_cf")) {
            material = material.replace("_cf", "_coral_fan");
        }
        else if(material.endsWith("_bp")) {
            material = material.replace("_bp", "_banner_pattern");
        }

        return material;
    }

    public void updateConfig(SkcraftBasics plugin) {
        this.plugin = plugin;

        moduleEnabled = plugin.enabledModules.contains("Shop");

        if(moduleEnabled) {
            plugin.logger.info("- ShopModule Enabled");
        }
    }
}
