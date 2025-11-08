package org.newdawn.spaceinvaders.Stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.MonsterEntity;
import org.newdawn.spaceinvaders.entity.ObstacleEntity;
import org.newdawn.spaceinvaders.entity.Boss.Boss5;

/**
 * 🩸 Stage5 — 최종 스테이지 (보스전)
 * - 장애물 2줄 생성 (방어 강화)
 * - 모든 몬스터 타입 등장
 * - Boss5 (뱀파이어) 등장
 */
public class Stage5 implements Stage {
    private final Game game;
    private long lastAlienShotTime = 0;
    private boolean bossSpawned = false;
    private final long startMillis;

    public Stage5(Game game) {
        this.game = game;
        this.startMillis = System.currentTimeMillis();
    }

    @Override
    public int id() {
        return 5;
    }

    @Override
    public void init() {
        // 👾 기본 몬스터 6마리 생성
        for (int i = 0; i < 6; i++) {
            MonsterEntity alien = new MonsterEntity(game, 100 + (i * 100), 80);
            alien.setShotType("normal");
            game.addEntity(alien);
        }

        // 🧱 장애물 2줄 생성
        int panelWidth = 800;
        int w = 32;
        int count = panelWidth / w;

        for (int row = 0; row < 2; row++) {
            for (int x = 0; x < count; x++) {
                game.addEntity(new ObstacleEntity(game, x * w, 380 + row * 40));
            }
        }

        System.out.println("🧱 [Stage5] 장애물 2줄 생성 완료");
    }

    @Override
    public void update() {
        long elapsedSec = (System.currentTimeMillis() - startMillis) / 1000;
        long now = System.currentTimeMillis();

        // 🔹 Normal 몬스터 생성 (5초 주기)
        if (elapsedSec < 60 && now - lastAlienShotTime > 5000) {
            for (int i = 0; i < 6; i++) {
                MonsterEntity alien = new MonsterEntity(
                    game,
                    100 + (int)(Math.random() * 600),
                    80 + (int)(Math.random() * 50)
                );
                alien.setShotType("shot");
                game.addEntity(alien);
            }
            lastAlienShotTime = now;
            System.out.println("👻 [Stage5] NORMAL 몬스터 생성");
        }

        // 🔹 Ice 몬스터 생성 (60~80초)
        if (elapsedSec >= 60 && elapsedSec < 80 && now - lastAlienShotTime > 10000) {
            for (int i = 0; i < 4; i++) {
                MonsterEntity alien = new MonsterEntity(
                    game,
                    100 + (int)(Math.random() * 600),
                    120 + (int)(Math.random() * 50)
                );
                alien.setShotType("iceshot");
                game.addEntity(alien);
            }
            lastAlienShotTime = now;
            System.out.println("🧊 [Stage5] ICE 몬스터 생성");
        }


        // 🔹 최종 보스 등장 (한 번만)
        if (elapsedSec >= 10 && !bossSpawned) {
            game.addEntity(new Boss5(game, 350, 120));
            bossSpawned = true;
            System.out.println("🩸 [Stage5] 최종 보스 등장! (Boss5 생성 완료)");
        }
    }
}