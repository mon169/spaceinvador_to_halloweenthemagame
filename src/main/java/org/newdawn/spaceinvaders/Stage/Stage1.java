package org.newdawn.spaceinvaders.Stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.MonsterEntity;
import org.newdawn.spaceinvaders.entity.Boss.Boss1;

/**
 * 🪐 Stage1 — 기본 튜토리얼 스테이지
 * - 시간 경과에 따라 다른 타입의 몬스터 생성
 * - 60초 이후 보스(프랑켄슈타인) 등장
 */
public class Stage1 implements Stage {
    private final Game game;
    private long lastAlienShotTime = 0;
    private boolean bossSpawned = false;
    private final long startMillis;

    public Stage1(Game game) {
        this.game = game;
        this.startMillis = System.currentTimeMillis();
    }

    @Override
    public int id() { 
        return 1; 
    }

    @Override
    public void init() {
        // 🎃 기본 몬스터 6마리 배치
        for (int i = 0; i < 6; i++) {
            MonsterEntity alien = new MonsterEntity(game, 100 + (i * 100), 80);
            alien.setShotType("normal");
            game.addEntity(alien);
        }
        System.out.println("👻 Stage 1 initialized with 6 basic monsters.");
    }

    @Override
    public void update() {
        long elapsedSec = (System.currentTimeMillis() - startMillis) / 1000;
        long now = System.currentTimeMillis();

        // 0~60초: normal 몬스터 5초 주기 생성
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
            System.out.println("👻 NORMAL 몬스터 생성 (5초 주기)");
        }

        // 60~80초: ice 몬스터 10초 주기 생성
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
            System.out.println("🧊 ICE 몬스터 생성 (10초 주기)");
        }

        // 80초 이후: bomb 몬스터 10초 주기 생성
        if (elapsedSec >= 80 && now - lastAlienShotTime > 10000) {
            MonsterEntity boss = new MonsterEntity(
                game,
                350 + (int)(Math.random() * 100 - 50),
                150
            );
            boss.setShotType("bombshot");
            game.addEntity(boss);
            lastAlienShotTime = now;
            System.out.println("💣 BOMB 몬스터 생성 (10초 주기)");
        }

        // 60초 이후 보스1(프랑켄슈타인) 등장 (한 번만)
        if (elapsedSec >= 60 && !bossSpawned) {
            game.addEntity(new Boss1(game, 350, 120));
            bossSpawned = true;
            System.out.println("⚡ 프랑켄슈타인 보스 등장!");
        }
    }
}