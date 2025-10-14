package org.newdawn.spaceinvaders.manager;

import java.util.HashMap;
import java.util.Map;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Stage.*;

/**
 * 🧩 StageManager — 스테이지 로드/업데이트 관리
 * - 각 Stage 클래스는 MonsterEntity & Boss 기반
 */
public class StageManager {
    private final Game game;
    private final EntityManager entityManager;
    private final Map<Integer, Stage> stages = new HashMap<>();
    private Stage current;

    public StageManager(Game game, EntityManager entityManager) {
        this.game = game;
        this.entityManager = entityManager;
    }

    /** 스테이지 로드 (필요 시 초기화) */
    public void loadStage(int stageId) {
        stages.computeIfAbsent(1, k -> new Stage1(game));
        stages.computeIfAbsent(2, k -> new Stage2(game));
        stages.computeIfAbsent(3, k -> new Stage3(game));
        stages.computeIfAbsent(4, k -> new Stage4(game));
        stages.computeIfAbsent(5, k -> new Stage5(game));

        current = stages.get(stageId);
        if (current == null) current = stages.get(1);

        current.init();
        System.out.println("🚀 Stage " + stageId + " 로드 완료");
    }

    /** 매 프레임마다 호출 (wave 업데이트) */
    public void spawnWave(int currentStage, long stageStartTime) {
        if (current == null || current.id() != currentStage) {
            loadStage(currentStage);
        }
        current.update();
    }
}
