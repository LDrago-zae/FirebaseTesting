package com.example.firebasetesting;

public class ShoppingItem {
    String name, price, description;

    public ShoppingItem() {} // Needed for Firebase

    public ShoppingItem(String name, String price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getDescription() { return description; }
}
