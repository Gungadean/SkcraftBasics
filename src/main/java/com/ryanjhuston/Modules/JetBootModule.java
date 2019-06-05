package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class JetBootModule {

    private SkcraftBasics plugin;

    public JetBootModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    public void onPotionEffect(EntityPotionEffectEvent event) {
        if(!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player)event.getEntity();

        if(player.getInventory().getBoots() == null) {
            return;
        }

        if(!player.getInventory().getBoots().hasItemMeta()) {
            return;
        }

        if(!player.getInventory().getBoots().getItemMeta().getLore().contains("Jetboots")) {
            return;
        }

        if(event.getAction() == EntityPotionEffectEvent.Action.ADDED || event.getAction() == EntityPotionEffectEvent.Action.CHANGED) {
            if(event.getCause() != EntityPotionEffectEvent.Cause.BEACON) {
                return;
            }

            if(plugin.jetboots.containsKey(player.getUniqueId().toString())) {
                if(!player.isFlying()) {
                    return;
                }

                updateDurability(player);
                return;
            }

            activateJetboots(player, event.getNewEffect().getType());
        } else if(event.getCause() == EntityPotionEffectEvent.Cause.EXPIRATION){
            if(!plugin.jetboots.containsKey(player.getUniqueId().toString())) {
                return;
            }

            if(plugin.jetboots.get(player.getUniqueId().toString()) != event.getOldEffect().getType()) {
                return;
            }

            deactivateJetboots(player);
        }
    }

    public void playerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if(event.getClickedBlock().getType() != Material.BEACON) {
            return;
        }

        if(item.getType() == Material.IRON_BOOTS ||
                item.getType() == Material.GOLDEN_BOOTS ||
                item.getType() == Material.DIAMOND_BOOTS) {

            ItemMeta meta = item.getItemMeta();
            List<String> lore;

            if(meta.hasLore()) {
                if (meta.getLore().contains("Jetboots")) {
                    return;
                }
                lore = meta.getLore();
            } else {
                lore = new ArrayList<>();
            }

            lore.add("Jetboots");
            meta.setLore(lore);
            item.setItemMeta(meta);
            event.setCancelled(true);
        }
    }

    public void playerJoin(PlayerJoinEvent event) {
        if(plugin.jetboots.containsKey(event.getPlayer().getUniqueId().toString())) {
            event.getPlayer().setAllowFlight(true);
            event.getPlayer().setFlying(true);
        }
    }

    public void playerDeath(PlayerDeathEvent event) {
        if(plugin.jetboots.containsKey(event.getEntity().getUniqueId().toString())) {
            event.getEntity().setAllowFlight(false);
            event.getEntity().setFlying(false);
        }
    }

    public void removeJetboots(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if(event.getClickedInventory() == null) {
            return;
        }

        if(event.getSlot() != 36) {
            return;
        }

        if(event.getCurrentItem().getType() != Material.IRON_BOOTS &&
            event.getCurrentItem().getType() != Material.GOLDEN_BOOTS &&
            event.getCurrentItem().getType() != Material.DIAMOND_BOOTS) {
            return;
        }

        if(!event.getCurrentItem().hasItemMeta()) {
            return;
        }

        if(!event.getCurrentItem().getItemMeta().hasLore()) {
            return;
        }

        if(!event.getCurrentItem().getItemMeta().getLore().contains("Jetboots")) {
            return;
        }

        deactivateJetboots((Player)event.getWhoClicked());
    }

    public void activateJetboots(Player player, PotionEffectType effect) {
        if(plugin.jetboots.get(player.getUniqueId().toString()) == null) {
            plugin.jetboots.put(player.getUniqueId().toString(), effect);
        }
        player.setAllowFlight(true);
    }

    public void deactivateJetboots(Player player) {
            plugin.jetboots.remove(player.getUniqueId().toString());
            player.setFlying(false);
            player.setAllowFlight(false);
    }

    public void updateDurability(Player player) {
        ItemMeta meta = player.getInventory().getBoots().getItemMeta();
        ((Damageable)meta).setDamage(((Damageable)meta).getDamage()+1);
        player.getInventory().getBoots().setItemMeta(meta);
    }
}