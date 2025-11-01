package org.newdawn.spaceinvaders.entity.Boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;

public abstract class BossEntity extends Entity {
    protected int health = 20;
    protected Game game;

    public BossEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.bossDefeated();
            game.removeEntity(this);
        }
    }

    public int getHealth() {
        return health;
    }
}
