package org.newdawn.spaceinvaders.Stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.MonsterEntity;
import org.newdawn.spaceinvaders.entity.Boss.Boss3;

public class Stage3 implements Stage {
    private final Game game;
    private long lastAlienShotTime = 0;
    private boolean bossSpawned = false;
    private long startMillis;

    public Stage3(Game game) {
        this.game = game;
        this.startMillis = System.currentTimeMillis();
    }

    @Override
    public int id() { return 3; }

    @Override
    public void init() {
        for (int i = 0; i < 6; i++) {
            MonsterEntity alien = new MonsterEntity(game, 100 + (i * 100), 80);
            alien.setShotType("normal");
            game.addEntity(alien);
        }
        startMillis = System.currentTimeMillis();
        System.out.println("🎃 [Stage3] 초기 몬스터 6마리 생성 완료");
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
            System.out.println("👻 [Stage3] NORMAL 몬스터 생성");
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
            System.out.println("🧊 [Stage3] ICE 몬스터 생성");
        }

        if (elapsedSec >= 80 && now - lastAlienShotTime > 10000) {
            MonsterEntity m = new MonsterEntity(
                game, 350 + (int)(Math.random() * 100 - 50), 150);
            m.setShotType("bombshot");
            game.addEntity(m);
            lastAlienShotTime = now;
            System.out.println("💣 [Stage3] BOMB 몬스터 생성");
        }

        if (elapsedSec >= 10 && !bossSpawned) {
            game.addEntity(new Boss3(game, 350, 120));
            bossSpawned = true;
            System.out.println("⚡ [Stage3] 보스 등장! (Boss3 생성 완료)");
        }

        if (game.getShip() != null && game.getShip().getHealth() <= 3) {
            System.out.println("❌ [Stage3] 플레이어 체력 3 이하 — 게임 오버!");
            game.notifyDeath();
        }
    }

    @Override
    public void resetStageFlags() {
        bossSpawned = false;
        lastAlienShotTime = 0;
        startMillis = System.currentTimeMillis();
        System.out.println("🔄 [Stage3] 보스 및 타이머 리셋 완료 (다시 10초 뒤 등장 예정)");
    }
}