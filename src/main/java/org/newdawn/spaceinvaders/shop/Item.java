package org.newdawn.spaceinvaders.shop;
import org.newdawn.spaceinvaders.entity.UserEntity;

public abstract class Item {
    private String name;
    private String description;
    private int cost;

    public Item(String name, String description, int cost) {
        this.name = name;
        this.description = description;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    // 아이템 효과를 플레이어에게 적용하는 추상 메서드
    public abstract void applyEffect(UserEntity ship);
}
