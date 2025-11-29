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
        try {
            for (int i = 0; i < 6; i++) {
                MonsterEntity alien = new MonsterEntity(game, 100 + (i * 100), 80);
                alien.setShotType("normal");
                game.addEntity(alien);
            }

            int panelWidth = 800, w = 32, count = panelWidth / w;
            for (int row = 0; row < 2; row++) {
                for (int x = 0; x < count; x++) {
                    try {
                        game.addEntity(new ObstacleEntity(game, x * w, 380 + row * 40));
                    } catch (Exception e) {
                        System.err.println("⚠️ ObstacleEntity 생성 실패: " + e.getMessage());
                    }
                }
            }
            startMillis = System.currentTimeMillis();
            System.out.println("🧱 [Stage5] 장애물 2줄 생성 완료");
        } catch (Exception e) {
            System.err.println("⚠️ Stage5 init() 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        try {
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


            // 🔹 최종 보스 등장 (한 번만)
            if (elapsedSec >= 10 && !bossSpawned) {
                try {
                    game.addEntity(new Boss5(game, 350, 120));
                    bossSpawned = true;
                    System.out.println("🩸 [Stage5] 최종 보스 등장! (Boss5 생성 완료)");
                } catch (Exception e) {
                    System.err.println("⚠️ Boss5 생성 실패: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Stage5 update() 오류: " + e.getMessage());
            e.printStackTrace();
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