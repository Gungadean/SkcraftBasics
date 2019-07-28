package com.ryanjhuston.Types;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class SkcraftPlayer {

    private String uuid;
    private Material teleportItem;
    private boolean wasFlying;
    private List<String> pTeleAuthed;
    private List<String> teleAuthed;
    private boolean isAdmin;
    private Inventory savedInventory;
    private YamlConfiguration playerConfig;

    public SkcraftPlayer(String uuid, Material teleportItem, boolean wasFlying, List<String> pTeleAuthed, List<String> teleAuthed, boolean isAdmin, YamlConfiguration playerConfig) {
        this.uuid = uuid;
        this.teleportItem = teleportItem;
        this.wasFlying = wasFlying;
        this.pTeleAuthed = pTeleAuthed;
        this.teleAuthed = teleAuthed;
        this.isAdmin = isAdmin;
        savedInventory = null;
        this.playerConfig = playerConfig;

        for(String player : pTeleAuthed) {
            if(!teleAuthed.contains(player)) {
                teleAuthed.add(player);
            }
        }
    }

    public String getUuid() {
        return uuid;
    }

    public Material getTeleportItem() {
        return teleportItem;
    }

    public boolean wasFlying() {
        return wasFlying;
    }

    public List<String> getPTeleAuthed() {
        return pTeleAuthed;
    }

    public List<String> getTeleAuthed() {
        return teleAuthed;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public Inventory getSavedInventory() {
        if(savedInventory != null) {
            return savedInventory;
        }

        return null;
    }

    public YamlConfiguration getConfig() {
        return playerConfig;
    }

    public void setWasFlying(boolean wasFlying) {
        this.wasFlying = wasFlying;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setSavedInventory(Inventory savedInventory) {
        this.savedInventory = savedInventory;
    }
}
