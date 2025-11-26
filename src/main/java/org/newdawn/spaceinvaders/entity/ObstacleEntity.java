package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;

import java.util.List;
import java.util.Random;

/**
 * 스테이지별 장애물 엔티티
 * candywall의 a, b, c 그룹 중 랜덤 선택하여 사용
 * a_1 = obstacle1, a_2 = obstacle2, a_3 = obstacle3, a_4 = obstacle4
 */
public class ObstacleEntity extends Entity {
    private Game game;
    private int stage; // 장애물 단계 (1~4)
    private int hitCount = 0;
    private static final int[] hitToNextStage = {2, 2, 2, 2}; // 각 단계별 필요 타격 수
    private static final Random random = new Random();
    
    // candywall 그룹별 4프레임 이미지 경로
    // a_1 = obstacle1, a_2 = obstacle2, a_3 = obstacle3, a_4 = obstacle4
    private static final String[][] GROUP_TO_FRAMES = new String[][]{
        // a 그룹
        {
            "sprites/candywall/a/a_1.png",  // obstacle1
            "sprites/candywall/a/a_2.png",  // obstacle2
            "sprites/candywall/a/a_3.png",  // obstacle3
            "sprites/candywall/a/a_4.png"   // obstacle4
        },
        // b 그룹
        {
            "sprites/candywall/b/b_1.png",  // obstacle1
            "sprites/candywall/b/b_2.png",  // obstacle2
            "sprites/candywall/b/b_3.png",  // obstacle3
            "sprites/candywall/b/b_4.png"   // obstacle4
        },
        // c 그룹
        {
            "sprites/candywall/c/c_1.png",  // obstacle1
            "sprites/candywall/c/c_2.png",  // obstacle2
            "sprites/candywall/c/c_3.png",  // obstacle3
            "sprites/candywall/c/c_4.png"   // obstacle4
        }
    };
    
    // 현재 장애물이 사용할 프레임 세트 (랜덤 선택된 그룹)
    private String[] frames;

    /**
     * 기본 생성자 - a, b, c 중 랜덤 선택
     */
    public ObstacleEntity(Game game, int x, int y) {
        // 빈 경로로 먼저 생성 (Entity 생성자가 빈 경로를 허용하도록 수정됨)
        super("", x, y);
        this.game = game;
        this.stage = 1;
        // 랜덤 그룹 선택하여 frames 배열 저장
        this.frames = getRandomGroupFrames();
        // 첫 번째 프레임으로 sprite 로드
        this.sprite = SpriteStore.get().getSprite(frames[0]);
        
        // sprite 로드 확인
        if (this.sprite == null) {
            System.err.println("❌ ObstacleEntity 생성 실패: " + frames[0] + "를 로드할 수 없습니다.");
        } else {
            System.out.println("✅ ObstacleEntity 생성 성공: " + frames[0] + " 로드됨, 위치(" + x + "," + y + ")");
        }
    }
    
    /**
     * 특정 그룹을 명시하는 생성자 ("a" | "b" | "c")
     * group이 null이거나 유효하지 않으면 랜덤 선택
     */
    public ObstacleEntity(Game game, int x, int y, String group) {
        super(selectGroupFrames(group)[0], x, y);
        this.game = game;
        this.stage = 1;
        this.frames = selectGroupFrames(group);
    }
    
    /**
     * a, b, c 중 랜덤하게 그룹 선택
     */
    private static String[] getRandomGroupFrames() {
        int groupIndex = random.nextInt(3); // 0=a, 1=b, 2=c
        return GROUP_TO_FRAMES[groupIndex];
    }
    
    /**
     * 특정 그룹의 프레임 선택 ("a" | "b" | "c")
     */
    private static String[] selectGroupFrames(String group) {
        if (group == null) return getRandomGroupFrames();
        switch (group.toLowerCase()) {
            case "a":
                return GROUP_TO_FRAMES[0];
            case "b":
                return GROUP_TO_FRAMES[1];
            case "c":
                return GROUP_TO_FRAMES[2];
            default:
                return getRandomGroupFrames(); // 유효하지 않으면 랜덤
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
