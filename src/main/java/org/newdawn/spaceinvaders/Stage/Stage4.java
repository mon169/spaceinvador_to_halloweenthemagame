package org.newdawn.spaceinvaders.Stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.MonsterEntity;
import org.newdawn.spaceinvaders.entity.ObstacleEntity;
import org.newdawn.spaceinvaders.entity.Boss.Boss4;

/**
 * 🧱 Stage4 — 고난이도 방어 스테이지
 * - 장애물이 새로 등장 (플레이어 보호용)
 * - 몬스터 수, 체력, 속도 모두 증가
 * - Boss4 (호박 괴물) 등장
 */
public class Stage4 implements Stage {
    private final Game game;
    private long lastAlienShotTime = 0;
    private boolean bossSpawned = false;
    private final long startMillis;

    public Stage4(Game game) {
        this.game = game;
        this.startMillis = System.currentTimeMillis();
    }

    @Override
    public int id() {
        return 4;
    }

    @Override
    public void init() {
        // 👾 기본 몬스터 6마리 생성
        for (int i = 0; i < 6; i++) {
            MonsterEntity alien = new MonsterEntity(game, 100 + (i * 100), 80);
            alien.setShotType("normal");
            game.addEntity(alien);
        }

        // 🧱 장애물 1줄 생성 (방어용)
        int panelWidth = 800;
        int w = 32;
        int count = panelWidth / w;
        for (int x = 0; x < count; x++) {
            game.addEntity(new ObstacleEntity(game, x * w, 380));
        }

        System.out.println("🧱 [Stage4] 장애물 1줄 생성 완료");
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
            System.out.println("👻 [Stage4] NORMAL 몬스터 생성");
        }

        // 🔹 Ice 몬스터 생성 (60~80초, 10초 주기)
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
            System.out.println("🧊 [Stage4] ICE 몬스터 생성");
        }

        // 🔹 Bomb 몬스터 생성 (80초 이후, 10초 주기)
        if (elapsedSec >= 80 && now - lastAlienShotTime > 10000) {
            MonsterEntity m = new MonsterEntity(
                game,
                350 + (int)(Math.random() * 100 - 50),
                150
            );
            m.setShotType("bombshot");
            game.addEntity(m);
            lastAlienShotTime = now;
            System.out.println("💣 [Stage4] BOMB 몬스터 생성");
        }

        // 🔹 보스 등장 (한 번만)
        if (elapsedSec >= 10 && !bossSpawned) {
            game.addEntity(new Boss4(game, 350, 120));
            bossSpawned = true;
            System.out.println("⚡ [Stage4] 보스 등장! (Boss4 생성 완료)");
        }
    }
}