package com.ryanjhuston.Types;

import org.bukkit.Material;

import java.util.List;

public class SkcraftPlayer {

    private String uuid;
    private Material teleportItem;
    private boolean wasFlying;
    private List<String> pTeleAuthed;
    private List<String> teleAuthed;
    private boolean inModMode;
    private boolean isAdmin;

    public SkcraftPlayer() {
        super();
    }

    public SkcraftPlayer(String uuid, Material teleportItem, boolean wasFlying, List<String> pTeleAuthed, List<String> teleAuthed, boolean inModMode, boolean isAdmin) {
        this.uuid = uuid;
        this.teleportItem = teleportItem;
        this.wasFlying = wasFlying;
        this.pTeleAuthed = pTeleAuthed;
        this.teleAuthed = teleAuthed;
        this.inModMode = inModMode;
        this.isAdmin = isAdmin;

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

    public void setTeleportItem(Material teleportItem) {
        this.teleportItem = teleportItem;
    }

    public boolean getWasFlying() {
        return wasFlying;
    }

    public void setWasFlying(boolean wasFlying) {
        this.wasFlying = wasFlying;
    }

    public List<String> getPTeleAuthed() {
        return pTeleAuthed;
    }

    public void setPTeleAuthed(List<String> pTeleAuthed) {
        this.pTeleAuthed = pTeleAuthed;
    }

    public List<String> getTeleAuthed() {
        return teleAuthed;
    }

    public void setTeleAuthed(List<String> teleAuthed) {
        this.teleAuthed = teleAuthed;
    }

    public void setInModMode(boolean inModMode) {
        this.inModMode = inModMode;
    }

    public boolean getInModMode() {
        return inModMode;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
