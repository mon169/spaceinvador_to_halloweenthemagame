package org.newdawn.spaceinvaders.manager;

import java.util.List;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;

/** ⚔️ CollisionManager — 모든 엔티티 충돌 검사 */
public class CollisionManager {
    private final Game game;
    private final List<Entity> entities;

    public CollisionManager(Game game, List<Entity> entities) {
        this.game = game;
        this.entities = entities;
    }

    public void checkCollisions() {
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            Entity me = entities.get(i);
            for (int j = i + 1; j < size; j++) {
                Entity him = entities.get(j);
                try {
                    if (me.collidesWith(him)) {
                        me.collidedWith(him);
                        him.collidedWith(me);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ 충돌 검사 오류: " + e.getMessage());
                }
            }
        }
    }
}
