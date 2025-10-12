package org.newdawn.spaceinvaders.entity;

import java.util.ArrayList;
import org.newdawn.spaceinvaders.Game;

public class BombEntity extends Entity {
    private Game game;
    private double moveSpeed = -300;
    private int explosionRadius = 100;

    public BombEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
        dy = moveSpeed;
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        if (y < 10) explode();
    }

    private void explode() {
        ArrayList<Entity> hit = new ArrayList<>();
        for (Entity e : game.getEntities()) {
            if (e instanceof AlienEntity) {
                double d = Math.sqrt(Math.pow(e.getX() - x, 2) + Math.pow(e.getY() - y, 2));
                if (d <= explosionRadius) hit.add(e);
            }
        }
        for (Entity e : hit) {
            game.removeEntity(e);
            game.notifyAlienKilled();
        }
        game.removeEntity(this);
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof AlienEntity) explode();
    }
}
