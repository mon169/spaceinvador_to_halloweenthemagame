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
    
    // 그룹별(캔디월 단계별) 4프레임 이미지 경로
    // onestep: obstacle1 대체, twostep: obstacle2 대체
    private static final String[][] GROUP_TO_FRAMES = new String[][]{
        // onestep
        {
            "sprites/candywall/onestep/candy_spritesheet_4x1.jpg",
            "sprites/candywall/onestep/candy_spritesheet_4x1 2.jpg",
            "sprites/candywall/onestep/candy_spritesheet_4x1 3.jpg",
            "sprites/candywall/onestep/candy_spritesheet_4x1 4.jpg"
        },
        // twostep
        {
            "sprites/candywall/twostep/Pixel candy.png",
            "sprites/candywall/twostep/Pixel candy 2.png",
            "sprites/candywall/twostep/Pixel candy 3.png",
            "sprites/candywall/twostep/Pixel candy 4.png"
        }
    };
    
    // 현재 장애물이 사용할 프레임 세트 (기본 onestep)
    private String[] frames = GROUP_TO_FRAMES[0];

    public ObstacleEntity(Game game, int x, int y) {
        super(GROUP_TO_FRAMES[0][0], x, y);
        this.game = game;
        this.stage = 1;
    }
    
    /**
     * 특정 그룹을 명시하는 생성자 ("onestep" | "twostep")
     */
    public ObstacleEntity(Game game, int x, int y, String group) {
        super(selectGroupFrames(group)[0], x, y);
        this.game = game;
        this.stage = 1;
        this.frames = selectGroupFrames(group);
    }
    
    private static String[] selectGroupFrames(String group) {
        if (group == null) return GROUP_TO_FRAMES[0];
        switch (group.toLowerCase()) {
            case "twostep":
                return GROUP_TO_FRAMES[1];
            case "onestep":
            default:
                return GROUP_TO_FRAMES[0];
        }
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
                    this.sprite = SpriteStore.get().getSprite(frames[stage-1]);
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
