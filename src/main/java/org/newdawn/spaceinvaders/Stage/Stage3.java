package org.newdawn.spaceinvaders.Stage;

import java.awt.Color;
import java.awt.Graphics;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.UserEntity;
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
    // 모래 폭풍 관련
    private long lastSandstormTime = 0;
    private final long SANDSTORM_INTERVAL = 50_000; // 50초
    private final int SANDSTORM_WIDTH = 320; // 폭을 넓힘 (기존 200 -> 320)
    private final long SANDSTORM_DURATION = 2000; // 2초

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
        if (elapsedSec >= 10 && !bossSpawned) {
            game.addEntity(new Boss3(game, 350, 120));
            bossSpawned = true;
            System.out.println("⚡ [Stage3] 보스 등장! (Boss3 생성 완료)");
        }

    // 🔹 모래 폭풍: 50초 간격으로 생성, 2초 동안 좌->우로 지나가며 플레이어에게 100 데미지
        if (now - lastSandstormTime >= SANDSTORM_INTERVAL) {
            // 중복 생성 방지
            boolean exists = false;
            for (Entity e : game.getEntities()) {
                if (e.getClass().getSimpleName().equals("SandstormEntityStage3")) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                lastSandstormTime = now;
                final int stormW = SANDSTORM_WIDTH;
                final long duration = SANDSTORM_DURATION;
                // 익명 Entity로 모래폭풍 생성 (Stage3 전용)
                Entity storm = new Entity("sprites/sandstorn.png", -stormW, 0) {
                    private boolean damaged = false;
                    private final double dxVal = (800.0 + stormW) / (duration / 1000.0);

                    @Override
                    public void move(long delta) {
                        if (this.dx == 0) this.dx = dxVal;
                        super.move(delta);
                        if (this.x > 800) {
                            game.removeEntity(this);
                        }
                    }

                    @Override
                    public void draw(Graphics g) {
                        // 가능하면 sprites/sandstorn.png 이미지를 전체 높이로 스케일해서 그림
                        if (this.sprite != null) {
                            this.sprite.drawScaled(g, (int) x, 0, stormW, game.getHeight());
                        } else {
                            Color sand = new Color(194, 178, 128, 180);
                            g.setColor(sand);
                            g.fillRect((int) x, 0, stormW, game.getHeight());
                        }
                    }

                    @Override
                    public void collidedWith(Entity other) {
                        if (damaged) return;
                        if (other instanceof UserEntity) {
                            UserEntity user = (UserEntity) other;
                            user.takeDamage(100 + user.getDefense());
                            damaged = true;
                        }
                    }

                    @Override
                    public String toString() { return "SandstormEntityStage3"; }
                };

                game.addEntity(storm);
                System.out.println("🌪️ [Stage3] 모래 폭풍 발생!");
            }
        }

        // 🔹 생명 제한 모드 (플레이어 체력 3 이하 시 자동 패배)
        if (game.getShip() != null && game.getShip().getHealth() <= 3) {
            System.out.println("❌ [Stage3] 플레이어 체력 3 이하 — 게임 오버!");
            game.notifyDeath();
        }
    }
}