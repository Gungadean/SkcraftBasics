package com.ryanjhuston.Types.Serializers;

import org.bukkit.entity.Player;

public class SerializedPlayer {

    public float exp;
    public int level;
    public double health;
    public double healthScale;
    public int food;
    public float saturation;
    public SerializedInventory inventory;

    public SerializedPlayer() {
        super();
    }

    public SerializedPlayer(Player player) {
        this.exp = player.getExp();
        this.level = player.getLevel();
        this.health = player.getHealth();
        this.healthScale = player.getHealthScale();
        this.food = player.getFoodLevel();
        this.saturation = player.getSaturation();

        this.inventory = new SerializedInventory(player.getInventory());
    }

    public void deserialize(Player player) {
        player.setExp(exp);
        player.setLevel(level);
        player.setHealth(health);
        player.setHealthScale(healthScale);
        player.setFoodLevel(food);
        player.setSaturation(saturation);

        player.getInventory().setContents(inventory.deserialize());
    }
}
