package org.newdawn.spaceinvaders.manager;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;

/**
 * 🧠 EntityManager
 * - 엔티티의 이동, 충돌 검사, 제거 관리
 * - Game.java의 entities/removeList를 직접 제어
 */
public class EntityManager {
    private final Game game;
    private final List<Entity> entities;
    private final List<Entity> removeList;
    private final CollisionManager collisionManager;

    public EntityManager(Game game, List<Entity> entities, List<Entity> removeList) {
        this.game = game;
        this.entities = entities;
        this.removeList = removeList;
        this.collisionManager = new CollisionManager(game, entities);
    }

    /**
     * 🔁 모든 엔티티 이동
     */
    public void moveEntities(long delta) {
        for (Entity e : new ArrayList<>(entities)) {
            try {
                e.move(delta);
            } catch (Exception ex) {
                System.err.println("⚠️ moveEntities: " + e.getClass().getSimpleName() + " 이동 중 오류 → " + ex.getMessage());
            }
        }
    }

    /**
     * 💥 충돌 검사 및 처리
     */
    public void checkCollisions() {
        collisionManager.checkCollisions();
    }

    /**
     * 🧹 제거 목록에 있는 엔티티 정리
     */
    public void cleanupEntities() {
        if (removeList.isEmpty()) return;

        for (Entity e : new ArrayList<>(removeList)) {
            entities.remove(e);
        }
        removeList.clear();
    }

    /**
     * ✅ 새로운 엔티티 추가
     */
    public void addEntity(Entity e) {
        entities.add(e);
    }

    /**
     * 🚫 엔티티 제거 예약
     */
    public void removeEntity(Entity e) {
        removeList.add(e);
    }
}
