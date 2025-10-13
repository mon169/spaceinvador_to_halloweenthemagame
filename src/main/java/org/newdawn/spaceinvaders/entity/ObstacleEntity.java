package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;

import java.util.List;

/**
 * 스테이지별 장애물 엔티티
 */
public class ObstacleEntity extends Entity {
    private Game game;
    private int stage; // 장애물의 현재 단계 (1~4)
    private int hitCount = 0;
    
    // 각 단계별로 다음 단계로 넘어가기 위해 필요한 타격 횟수
    private static final int[] hitToNextStage = {2, 2, 2, 2}; 
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
        // 플레이어의 총알에 맞았을 때 처리
        if (other instanceof ShotEntity) {
            hitCount++;
            
            // 필요한 타격 횟수를 채우면 다음 단계로 진행
            if (hitCount >= hitToNextStage[stage-1]) {
                hitCount = 0;
                stage++;
                
                if (stage > 4) {
                    game.removeEntity(this); // 최종 단계 파괴, 장애물 제거
                } else {
                    // 다음 단계 스프라이트로 업데이트
                    this.sprite = SpriteStore.get().getSprite(spriteNames[stage-1]);
                }
            }
            game.removeEntity(other); // 총알 제거
        }
        // EnemyShotEntity는 충돌해도 피해 없음 (무시)
    }

    /**
     * 현재 게임 엔티티 목록에서 모든 장애물이 제거되었는지 확인하는 유틸리티 메서드
     * Game 클래스 등에서 호출
     */
    public static boolean isObstacleClear(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entity instanceof ObstacleEntity) {
                return false;
            }
        }
        return true;
    }
}