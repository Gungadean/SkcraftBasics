package com.ryanjhuston.Modules;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GalapagosModule implements Listener {

    private SkcraftBasics plugin;

    public GalapagosModule(SkcraftBasics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if(event.getEntityType() != EntityType.TURTLE) {
            return;
        }

        Random rand = new Random();
        if(rand.nextInt(20) != 1) {
            return;
        }

        event.getEntity().setCustomNameVisible(true);
        event.getEntity().setCustomName("Galapagos Tortoise");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if(event.getEntityType() != EntityType.TURTLE) {
            return;
        }

        if(event.getDamager().getType() != EntityType.PLAYER) {
            return;
        }

        if(event.getEntity().getCustomName() == null) {
            return;
        }

        if(!event.getEntity().getCustomName().equals("Galapagos Tortoise")) {
            return;
        }

        event.getDamager().sendMessage(ChatColor.RED + "Warning! You are hurting an endangered species.");
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if(event.getEntityType() != EntityType.TURTLE) {
            return;
        }

        if(event.getEntity().getCustomName() == null) {
            return;
        }

        if(!event.getEntity().getCustomName().equals("Galapagos Tortoise")) {
            return;
        }

        event.getDrops().clear();

        ItemStack tastyMeat = new ItemStack(Material.DRIED_KELP, 1);
        ItemMeta meatMeta = tastyMeat.getItemMeta();
        List<String> lore = new ArrayList<>();

        meatMeta.setDisplayName("Tasty Galapagos Meat");
        lore.add("I hope it's worth it you monster");
        meatMeta.setLore(lore);

        tastyMeat.setItemMeta(meatMeta);

        Bukkit.getWorld(event.getEntity().getWorld().getUID()).dropItemNaturally(event.getEntity().getLocation(), tastyMeat);

        if(event.getEntity().getKiller() != null) {
            Bukkit.broadcastMessage(ChatColor.RED + event.getEntity().getKiller().getName() + " has killed an endangered species");
        }
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        if(event.getItem().getType() != Material.DRIED_KELP) {
            return;
        }

        if(!event.getItem().hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = event.getItem().getItemMeta();

        if(!itemMeta.hasLore()) {
            return;
        }

        if(!itemMeta.getLore().get(0).equals("I hope it's worth it you monster")) {
            return;
        }

        event.getPlayer().setFoodLevel(20);
        event.getPlayer().setSaturation(20);
    }
}
