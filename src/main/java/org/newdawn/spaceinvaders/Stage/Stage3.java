package org.newdawn.spaceinvaders.Stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.MonsterEntity;
import org.newdawn.spaceinvaders.entity.Boss.Boss3;

/**
 * 💀 Stage3 — 중간 난이도 스테이지
 * - 공격 주기 동일하지만 몬스터 체력/속도가 더 높음
 * - Boss3 (미라) 등장
 * - 플레이어 체력이 3 이하일 경우 자동 패배 처리
 */
public class Stage3 implements Stage {
    private final Game game;
    private long lastAlienShotTime = 0;
    private boolean bossSpawned = false;
    private final long startMillis;

    public Stage3(Game game) {
        this.game = game;
        this.startMillis = System.currentTimeMillis();
    }

    @Override
    public int id() {
        return 3;
    }

    @Override
    public void init() {
        // 👾 기본 몬스터 6마리 배치
        for (int i = 0; i < 6; i++) {
            MonsterEntity alien = new MonsterEntity(game, 100 + (i * 100), 80);
            alien.setShotType("normal");
            game.addEntity(alien);
        }
        System.out.println("🎃 [Stage3] 초기 몬스터 6마리 생성 완료");
    }

    @Override
    public void update() {
        long elapsedSec = (System.currentTimeMillis() - startMillis) / 1000;
        long now = System.currentTimeMillis();

        // 🔹 Normal 몬스터 (5초 주기)
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
            System.out.println("👻 [Stage3] NORMAL 몬스터 생성");
        }

        // 🔹 Ice 몬스터 (10초 주기, 60~80초)
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
            System.out.println("🧊 [Stage3] ICE 몬스터 생성");
        }

        // 🔹 Bomb 몬스터 (10초 주기, 80초 이후)
        if (elapsedSec >= 80 && now - lastAlienShotTime > 10000) {
            MonsterEntity m = new MonsterEntity(
                game,
                350 + (int)(Math.random() * 100 - 50),
                150
            );
            m.setShotType("bombshot");
            game.addEntity(m);
            lastAlienShotTime = now;
            System.out.println("💣 [Stage3] BOMB 몬스터 생성");
        }

        // 🔹 60초 이후 보스 등장 (한 번만)
        if (elapsedSec >= 60 && !bossSpawned) {
            game.addEntity(new Boss3(game, 350, 120));
            bossSpawned = true;
            System.out.println("⚡ [Stage3] 보스 등장! (Boss3 생성 완료)");
        }

        // 🔹 생명 제한 모드 (플레이어 체력 3 이하 시 자동 패배)
        if (game.getShip() != null && game.getShip().getHealth() <= 3) {
            System.out.println("❌ [Stage3] 플레이어 체력 3 이하 — 게임 오버!");
            game.notifyDeath();
        }
    }
}