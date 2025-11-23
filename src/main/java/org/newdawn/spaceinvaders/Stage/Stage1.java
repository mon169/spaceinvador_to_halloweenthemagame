package org.newdawn.spaceinvaders.Stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.MonsterEntity;
import org.newdawn.spaceinvaders.entity.Boss.Boss1;

public class Stage1 implements Stage {
    private final Game game;
    private long lastAlienShotTime = 0;
    private boolean bossSpawned = false;
    private long startMillis; // ✅ final 제거

    public Stage1(Game game) {
        this.game = game;
        this.startMillis = System.currentTimeMillis();
    }

    @Override
    public int id() { return 1; }

    @Override
    public void init() {
        for (int i = 0; i < 6; i++) {
            MonsterEntity alien = new MonsterEntity(game, 100 + (i * 100), 80);
            alien.setShotType("normal");
            game.addEntity(alien);
        }
        startMillis = System.currentTimeMillis();
        System.out.println("👻 Stage 1 initialized with 6 basic monsters.");
    }

    @Override
    public void update() {
        long elapsedSec = (System.currentTimeMillis() - startMillis) / 1000;
        long now = System.currentTimeMillis();

        if (elapsedSec < 60 && now - lastAlienShotTime > 5000) {
            for (int i = 0; i < 6; i++) {
                MonsterEntity alien = new MonsterEntity(
                    game, 100 + (int)(Math.random() * 600),
                    80 + (int)(Math.random() * 50));
                alien.setShotType("shot");
                game.addEntity(alien);
            }
            lastAlienShotTime = now;
            System.out.println("👻 NORMAL 몬스터 생성 (5초 주기)");
        }

        if (elapsedSec >= 60 && elapsedSec < 80 && now - lastAlienShotTime > 10000) {
            for (int i = 0; i < 4; i++) {
                MonsterEntity alien = new MonsterEntity(
                    game, 100 + (int)(Math.random() * 600),
                    120 + (int)(Math.random() * 50));
                alien.setShotType("iceshot");
                game.addEntity(alien);
            }
            lastAlienShotTime = now;
            System.out.println("🧊 ICE 몬스터 생성 (10초 주기)");
        }

        if (elapsedSec >= 80 && now - lastAlienShotTime > 10000) {
            MonsterEntity boss = new MonsterEntity(
                game, 350 + (int)(Math.random() * 100 - 50), 150);
            boss.setShotType("bombshot");
            game.addEntity(boss);
            lastAlienShotTime = now;
            System.out.println("💣 BOMB 몬스터 생성 (10초 주기)");
        }

        if (elapsedSec >= 10 && !bossSpawned) {
            game.addEntity(new Boss1(game, 350, 120));
            bossSpawned = true;
            System.out.println("⚡ 프랑켄슈타인 보스 등장!");
        }
    }

    @Override
    public void resetStageFlags() {
        bossSpawned = false;
        lastAlienShotTime = 0;
        startMillis = System.currentTimeMillis(); // ✅ 재시작 시 타이머 리셋
        System.out.println("🔄 [Stage1] 보스 및 타이머 리셋 완료 (다시 10초 뒤 등장 예정)");
    }
}