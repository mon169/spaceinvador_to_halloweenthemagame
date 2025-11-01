package org.newdawn.spaceinvaders.Stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.MonsterEntity;
import org.newdawn.spaceinvaders.entity.Boss.Boss2;

/**
 * 🕸 Stage2 — 난이도 상승 스테이지
 * - 몬스터 출현 간격이 1스테이지보다 빠름 (0.8배)
 * - 보스2 등장 (마녀 컨셉)
 */
public class Stage2 implements Stage {
    private final Game game;
    private long lastAlienShotTime = 0;
    private boolean bossSpawned = false;
    private final long startMillis;

    public Stage2(Game game) {
        this.game = game;
        this.startMillis = System.currentTimeMillis();
    }

    @Override
    public int id() {
        return 2;
    }

    @Override
    public void init() {
        // 👻 기본 몬스터 6마리 배치
        for (int i = 0; i < 6; i++) {
            MonsterEntity alien = new MonsterEntity(game, 100 + (i * 100), 80);
            alien.setShotType("normal");
            game.addEntity(alien);
        }
        System.out.println("🎃 [Stage2] 초기 몬스터 6마리 생성 완료");
    }

    @Override
    public void update() {
        long elapsedSec = (System.currentTimeMillis() - startMillis) / 1000;
        long now = System.currentTimeMillis();

        // 스테이지2는 모든 생성 주기가 0.8배 (더 빠름)
        int normalGap = (int)(5000 * 0.8);
        int iceGap    = (int)(10000 * 0.8);
        int bombGap   = (int)(10000 * 0.8);

        // 🔹 Normal 몬스터 생성 (초기 60초 동안)
        if (elapsedSec < 60 && now - lastAlienShotTime > normalGap) {
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
            System.out.println("👻 [Stage2] NORMAL 몬스터 생성 (" + normalGap/1000.0 + "초 주기)");
        }

        // 🔹 Ice 몬스터 생성 (60~80초)
        if (elapsedSec >= 60 && elapsedSec < 80 && now - lastAlienShotTime > iceGap) {
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
            System.out.println("🧊 [Stage2] ICE 몬스터 생성 (" + iceGap/1000.0 + "초 주기)");
        }

        // 🔹 Bomb 몬스터 생성 (80초 이후)
        if (elapsedSec >= 80 && now - lastAlienShotTime > bombGap) {
            MonsterEntity m = new MonsterEntity(
                game,
                350 + (int)(Math.random() * 100 - 50),
                150
            );
            m.setShotType("bombshot");
            game.addEntity(m);
            lastAlienShotTime = now;
            System.out.println("💣 [Stage2] BOMB 몬스터 생성 (" + bombGap/1000.0 + "초 주기)");
        }

        // 🔹 60초 이후 보스 등장 (한 번만)
        if (elapsedSec >= 10 && !bossSpawned) {
            game.addEntity(new Boss2(game, 350, 120));
            bossSpawned = true;
            System.out.println("⚡ [Stage2] 보스 등장! (Boss2 생성 완료)");
        }
    }
}