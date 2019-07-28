package com.ryanjhuston.Types;

import org.bukkit.Location;
import org.bukkit.Material;

public class Shop {

    String owner;

    private Material product;
    private Material price;

    private int productAmount;
    private int priceAmount;

    private Location shopLocation;

    public Shop(String owner, Material product, int productAmount, Material price, int priceAmount, Location shopLocation) {
        this.owner = owner;
        this.product = product;
        this.productAmount = productAmount;
        this.price = price;
        this.priceAmount = priceAmount;
        this.shopLocation = shopLocation;
    }

    public String getOwner() {
        return owner;
    }

    public Material getProduct() {
        return product;
    }

    public int getProductAmount() {
        return productAmount;
    }

    public Material getPrice() {
        return price;
    }

    public int getPriceAmount() {
        return priceAmount;
    }

    public Location getShopLocation() {
        return shopLocation;
    }
}
