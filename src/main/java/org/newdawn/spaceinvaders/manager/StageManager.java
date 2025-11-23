package org.newdawn.spaceinvaders.manager;

import java.util.HashMap;
import java.util.Map;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Stage.*;

/**
 * 🧩 StageManager — 스테이지 로드/업데이트 관리
 * - currentStage 전환 시 즉시 반영
 * - 리셋은 해당 스테이지만 적용
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

    /** ✅ 스테이지 로드 (현재 스테이지만 리셋) */
    public void loadStage(int stageId) {
        stages.computeIfAbsent(1, k -> new Stage1(game));
        stages.computeIfAbsent(2, k -> new Stage2(game));
        stages.computeIfAbsent(3, k -> new Stage3(game));
        stages.computeIfAbsent(4, k -> new Stage4(game));
        stages.computeIfAbsent(5, k -> new Stage5(game));

        current = stages.get(stageId);
        if (current == null) current = stages.get(1);

        current.resetStageFlags();
        current.init();
        System.out.println("🚀 Stage " + stageId + " 로드 완료 (현재 스테이지만 리셋)");
    }

    /** 매 프레임마다 호출 */
    public void spawnWave(int currentStage, long stageStartTime) {
        if (current == null || current.id() != currentStage) {
            System.out.println("🔁 StageManager: 스테이지 갱신 필요 → " + currentStage);
            loadStage(currentStage);
        }
        if (current != null) current.update();
    }

    /** ✅ 전체 리셋 (게임 전체 초기화 시 사용) */
    public void resetAllStageFlags() {
        for (Stage stage : stages.values()) {
            stage.resetStageFlags();
        }
        System.out.println("♻️ 모든 스테이지 상태 리셋 완료 (보스 및 웨이브 재활성화 가능)");
    }

    public Stage getCurrentStage() { return current; }
}