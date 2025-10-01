package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;

import java.util.List;

/**
 * 스테이지별 장애물 엔티티
 */
public class ObstacleEntity extends Entity {
    private Game game;
    private int stage; // 장애물 단계 (1~4)
    private int hitCount = 0;
    private static final int[] hitToNextStage = {2, 2, 2, 2}; // 각 단계별 필요 타격 수
    private static final String[] spriteNames = {
        "sprites/obstacle1.png",
        "sprites/obstacle2.png",
        "sprites/obstacle3.png",
        "sprites/obstacle4.png"
    };

    public ObstacleEntity(Game game, int x, int y) {
        super(spriteNames[0], x, y);
        this.game = game;
        this.stage = 1;
    }

    @Override
    public void collidedWith(Entity other) {
        // 플레이어의 총알에 맞으면 단계 변경
        if (other instanceof ShotEntity) {
            hitCount++;
            if (hitCount >= hitToNextStage[stage-1]) {
                hitCount = 0;
                stage++;
                if (stage > 4) {
                    game.removeEntity(this); // 장애물 제거
                } else {
                    this.sprite = SpriteStore.get().getSprite(spriteNames[stage-1]);
                }
            }
            game.removeEntity(other); // 총알 제거
        }
        // EnemyShotEntity는 무시 (피해 없음)
    }

    // 장애물 모두 제거 여부 확인
    // Game에서 호출하는 용도
    public static boolean isObstacleClear(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entity instanceof ObstacleEntity) {
                return false;
            }
        }
        return true;
    }
}
