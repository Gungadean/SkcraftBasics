package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.Shop;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ShopModule implements Listener {

    private SkcraftBasics plugin;

    private HashMap<Location, Shop> shops = new HashMap<>();

    public ShopModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
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
            return;
        }

        if(event.getLine(1).isEmpty() || event.getLine(2).isEmpty()) {
            player.sendMessage(ChatColor.RED + "You must define a price and item to sell.");
            return;
        }

        String[] product = event.getLine(1).split(" ");
        String[] price = event.getLine(2).split(" ");

        if(product.length == 1 || price.length == 1) {
            correctUsage(player);
            return;
        }

        if(!isInteger(product[0]) || !isInteger(price[0])) {
            correctUsage(player);
            return;
        }

        int productAmount = Integer.parseInt(product[0]);
        int priceAmount = Integer.parseInt(price[0]);

        Material productMat = Material.matchMaterial(product[1]);
        Material priceMat = Material.matchMaterial(price[1]);

        if(productMat == null) {
            player.sendMessage(ChatColor.RED + "Invalid product.");
            return;
        }

        if(priceMat == null) {
            player.sendMessage(ChatColor.RED + "Invalid price.");
            return;
        }

        if(productAmount <= 0 || priceAmount <= 0) {
            player.sendMessage(ChatColor.RED + "The item amount can not be less than or equal to zero.");
            return;
        }

        event.setLine(3, player.getName());
        shops.put(event.getBlock().getLocation(), new Shop(player.getUniqueId().toString(), productMat, productAmount, priceMat, priceAmount, event.getBlock().getRelative(0, -1, 0).getLocation()));
        player.sendMessage(ChatColor.GOLD + "You successfully created a shop.");
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

        if(!event.getPlayer().getUniqueId().toString().equals(shop.getOwner())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to remove this shop.");
            return;
        }

        if(!event.getBlock().getLocation().equals(shop.getShopLocation())) {
            return;
        }

        shops.remove(shop.getShopLocation().add(0, 1, 0));
        event.getPlayer().sendMessage(ChatColor.GOLD + "Shop has been successfully removed.");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if(!event.getClickedBlock().getType().toString().endsWith("_SIGN")) {
            return;
        }

        if(!shops.containsKey(event.getClickedBlock().getLocation())) {
            return;
        }

        Shop shop = shops.get(event.getClickedBlock().getLocation());
        Player player = event.getPlayer();
        ItemStack price = new ItemStack(shop.getPrice(), shop.getPriceAmount());
        ItemStack product = new ItemStack(shop.getProduct(), shop.getProductAmount());

        if(!event.getPlayer().getInventory().containsAtLeast(price, shop.getPriceAmount())) {
            player.sendMessage(ChatColor.RED + "You do not have sufficient funds to buy this.");
            return;
        }

        InventoryHolder inventoryHolder = (InventoryHolder)shop.getShopLocation().getBlock().getState();

        if(!inventoryHolder.getInventory().containsAtLeast(product, shop.getProductAmount())) {
            player.sendMessage(ChatColor.RED + "This shop does not have enough of the product to sell.");
            return;
        }

        if(!inventoryHasSpace(player.getInventory(), product)) {
            player.sendMessage(ChatColor.RED + "You do not have enough inventory space for this transaction.");
            return;
        }

        if(!inventoryHasSpace(inventoryHolder.getInventory(), price)) {
            player.sendMessage(ChatColor.RED + "This shop does not have enough inventory space for this transaction.");
            return;
        }

        player.getInventory().removeItem(price);
        inventoryHolder.getInventory().removeItem(product);

        player.getInventory().addItem(product);
        inventoryHolder.getInventory().addItem(price);

        player.sendMessage(ChatColor.GREEN + "Transaction successful.");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Shop shop = getShopFromBlock(event.getBlock());

        if(shop == null) {
            return;
        }

        if(!event.getPlayer().getUniqueId().toString().equals(shop.getOwner())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot add a chest to this shop.");
            return;
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
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

        if(!event.getPlayer().getUniqueId().toString().equals(shop.getOwner())) {
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

            if(chest.getInventory() instanceof DoubleChestInventory) {
                doubleChest = ((DoubleChestInventory) chest.getInventory()).getHolder();

                if(doubleChest.getRightSide().getInventory().getLocation().getBlock().getRelative(0, 1, 0).getType().toString().endsWith("_SIGN")) {
                    Location sign = doubleChest.getRightSide().getInventory().getLocation().getBlock().getRelative(0, 1, 0).getLocation();
                    if(shops.containsKey(sign)) {
                        return shops.get(sign);
                    }
                }

                if(doubleChest.getLeftSide().getInventory().getLocation().getBlock().getRelative(0, 1, 0).getType().toString().endsWith("_SIGN")) {
                    Location sign = doubleChest.getLeftSide().getInventory().getLocation().getBlock().getRelative(0, 1, 0).getLocation();
                    if(shops.containsKey(sign)) {
                        return shops.get(sign);
                    }
                }
            } else {
                if(chest.getBlock().getRelative(0, 1, 0).getType().toString().endsWith("_SIGN")) {
                    Location sign = chest.getBlock().getRelative(0, 1, 0).getLocation();
                    if(shops.containsKey(sign)) {
                        return shops.get(sign);
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
        if (event.isCancelled()) {
            return;
        }

        if(event.getSource().getType() != InventoryType.CHEST && event.getSource().getType() != InventoryType.BARREL) {
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

    private boolean inventoryHasSpace(Inventory inventory, ItemStack itemStack) {
        int neededSpace = itemStack.getAmount();
        for(ItemStack item : inventory.getContents()) {
            if(item == null) {
                neededSpace -= 64;
                continue;
            }

            if(item.getType() == itemStack.getType()) {
                if((itemStack.getType().getMaxStackSize() - item.getAmount()) >= 1) {
                    neededSpace -= (itemStack.getType().getMaxStackSize() - item.getAmount());
                }
            }
        }

        if(neededSpace <= 0) {
            return true;
        }

        return false;
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

    public HashMap<Location, Shop> getShops() {
        return shops;
    }
}
