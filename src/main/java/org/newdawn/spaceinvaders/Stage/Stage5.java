package org.newdawn.spaceinvaders.Stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.MonsterEntity;
import org.newdawn.spaceinvaders.entity.ObstacleEntity;
import org.newdawn.spaceinvaders.entity.Boss.Boss5;

public class Stage5 implements Stage {
    private final Game game;
    private long lastAlienShotTime = 0;
    private boolean bossSpawned = false;
    private long startMillis;

    public Stage5(Game game) {
        this.game = game;
        this.startMillis = System.currentTimeMillis();
    }

    @Override
    public int id() { return 5; }

    @Override
    public void init() {
        for (int i = 0; i < 6; i++) {
            MonsterEntity alien = new MonsterEntity(game, 100 + (i * 100), 80);
            alien.setShotType("normal");
            game.addEntity(alien);
        }

        int panelWidth = 800, w = 32, count = panelWidth / w;
        for (int row = 0; row < 2; row++) {
            for (int x = 0; x < count; x++) {
                game.addEntity(new ObstacleEntity(game, x * w, 380 + row * 40));
            }
        }
        startMillis = System.currentTimeMillis();
        System.out.println("🧱 [Stage5] 장애물 2줄 생성 완료");
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
            System.out.println("👻 [Stage5] NORMAL 몬스터 생성");
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
            System.out.println("🧊 [Stage5] ICE 몬스터 생성");
        }

        if (elapsedSec >= 10 && !bossSpawned) {
            game.addEntity(new Boss5(game, 350, 120));
            bossSpawned = true;
            System.out.println("🩸 [Stage5] 최종 보스 등장! (Boss5 생성 완료)");
        }
    }

    @Override
    public void resetStageFlags() {
        bossSpawned = false;
        lastAlienShotTime = 0;
        startMillis = System.currentTimeMillis();
        System.out.println("🔄 [Stage5] 보스 및 타이머 리셋 완료 (다시 10초 뒤 등장 예정)");
    }
}