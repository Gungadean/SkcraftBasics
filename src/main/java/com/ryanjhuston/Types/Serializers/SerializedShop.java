package com.ryanjhuston.Types.Serializers;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.Shop;
import org.bukkit.Material;

public class SerializedShop {

    public String owner;
    public String shopLocation;

    public String product;
    public String price;

    public int productAmount;
    public int priceAmount;

    public SerializedShop() {
        super();
    }

    public SerializedShop(Shop shop) {
        this.owner = shop.getOwner();
        this.shopLocation = SkcraftBasics.locationToString(shop.getShopLocation());
        this.product = shop.getProduct().toString();
        this.productAmount = shop.getProductAmount();
        this.price = shop.getPrice().toString();
        this.priceAmount = shop.getPriceAmount();
    }

    public Shop deserialize() {
        return new Shop(owner, Material.valueOf(product), productAmount, Material.valueOf(price), priceAmount, SkcraftBasics.stringToLocation(shopLocation));
    }
}
