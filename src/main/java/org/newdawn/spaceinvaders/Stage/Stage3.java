package org.newdawn.spaceinvaders.Stage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D; // Graphics2D 추가

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.UserEntity;
import org.newdawn.spaceinvaders.entity.MonsterEntity;
import org.newdawn.spaceinvaders.entity.Boss.Boss3;

public class Stage3 implements Stage {
    private final Game game;
    private long lastAlienShotTime = 0;
    private boolean bossSpawned = false;
    private long startMillis;
    // 모래 폭풍 관련
    private long lastSandstormTime = 0;
    private final long SANDSTORM_INTERVAL = 50_000; // 50초
    private final int SANDSTORM_INITIAL_WIDTH = 100; // 초기 폭을 줄임
    private final long SANDSTORM_DURATION = 250; // 1.5초 (더 빠르게)

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

        // 🔹 모래 폭풍: 50초 간격으로 생성, 1.5초 동안 아래->위로 솟아나며 확산
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
                final int initialW = SANDSTORM_INITIAL_WIDTH;
                final long duration = SANDSTORM_DURATION;
                final double startY = game.getHeight() - 50; // 화면 하단 근처에서 시작
                
                // 익명 Entity로 모래폭풍 생성 (Stage3 전용)
                Entity storm = new Entity("sprites/sandstorm.png", (game.getWidth() / 2) - (initialW / 2), (int) startY) {
                        private boolean damaged = false;
                        private long spawnTime = System.currentTimeMillis(); // 생성 시간 기록
                    private final int MAX_WIDTH = 400; // 최대 폭
                    private final double MAX_HEIGHT_MOVEMENT = game.getHeight() - 100; // 최대 상승 높이
                    private double currentW = initialW; // 현재 폭

                    {
                        // 폭발 효과를 위해 시작 시 속도를 줍니다.
                        this.dy = -MAX_HEIGHT_MOVEMENT / (duration / 1000.0); // 1.5초 동안 MAX_HEIGHT_MOVEMENT만큼 이동
                    }

                    @Override
                    public void move(long delta) {
                        super.move(delta);

                        long elapsedTime = System.currentTimeMillis() - spawnTime;
                        float progress = (float) elapsedTime / duration;

                        if (elapsedTime >= duration) {
                            game.removeEntity(this);
                            return;
                        }

                        // 1. 폭 (Width) 애니메이션: 초기W -> MAX_WIDTH로 증가 (퍼지는 효과)
                        currentW = initialW + (MAX_WIDTH - initialW) * progress;
                        
                        // 3. x 좌표 보정: 중앙을 유지하며 폭이 늘어나도록 합니다.
                        this.x = (game.getWidth() / 2) - (currentW / 2);
                    }

                    @Override
                    public void draw(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g;

                        // sprites/sandstorm.png 이미지를 사용하거나, 없을 경우 색상 박스를 그림
                        if (this.sprite != null) {
                            // 투명도 없이 불투명(opaque)으로 전체 영역을 채워 폭풍을 표현
                            this.sprite.drawScaled(g2d, (int) x, (int) y, (int) currentW, (int) (game.getHeight() - y));
                        } else {
                            Color sand = new Color(210, 180, 140); // 옅은 흙색
                            g2d.setColor(sand);
                            g2d.fillRect((int) x, (int) y, (int) currentW, (int) (game.getHeight() - y));
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
                System.out.println("💥 [Stage3] 모래 폭풍 폭발! (아래에서 위로 상승/확산)");
            }
        }

        // 🔹 생명 제한 모드 (플레이어 체력 3 이하 시 자동 패배)
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