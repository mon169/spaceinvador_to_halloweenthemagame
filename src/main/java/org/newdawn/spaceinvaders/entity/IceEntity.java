package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

public class IceEntity extends Entity {
    private Game game;
    private double moveSpeed = -300;
    private int freezeDuration = 3000;  // 3초간 얼림

    public IceEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
    }

    public void move(long delta) {
        super.setVerticalMovement(moveSpeed);
        super.move(delta);

        if (y < -50) {
            game.removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // 외계인과 충돌하면 얼림 효과 적용
        if (other instanceof AlienEntity) {
            AlienEntity alien = (AlienEntity) other;
            alien.freeze(freezeDuration);
            game.removeEntity(this);
        }
    }
}