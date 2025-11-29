package org.newdawn.spaceinvaders.Stage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.geom.AffineTransform;

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
    private final long SANDSTORM_INTERVAL = 5_000; // 5초 간격
    private final int SANDSTORM_INITIAL_WIDTH = 80;
    private final long SANDSTORM_DURATION = 1500; // 밀리초 (더 빠르고 위협적인 느낌을 위해 유지)

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

    // 🔹 모래 폭풍: 스테이지 시작 10초 뒤부터 5초 간격으로 반복 발생
    if (elapsedSec >= 10 && now - lastSandstormTime >= SANDSTORM_INTERVAL && game.getEntities().stream().noneMatch(e -> e.getClass().getSimpleName().equals("SandstormEntityStage3"))) {
            
            lastSandstormTime = now;
            final int initialW = SANDSTORM_INITIAL_WIDTH;
            final long duration = SANDSTORM_DURATION;
            final int fixedBottomY = game.getHeight(); 
            
            // 수정: 폭풍의 높이를 화면 전체 높이로 다시 설정하여 더 길고 위협적으로 보이게 함
            final int stormHeight = game.getHeight(); 

            // 랜덤한 가로 중심을 선택
            final double centerX = initialW / 2.0 + Math.random() * (game.getWidth() - initialW);

            Entity storm = new Entity("sprites/sandstorm.png", (int)(centerX - (initialW / 2)), fixedBottomY - stormHeight) {
                private boolean damaged = false;
                private long spawnTime = System.currentTimeMillis();
                private final int MAX_WIDTH = 240;
                private double currentW = Math.max(initialW, MAX_WIDTH);
                private final double originCenterX = centerX;
                private final int originY = fixedBottomY - stormHeight; 

                @Override
                public void move(long delta) {
                    long elapsedTime = System.currentTimeMillis() - spawnTime;

                    if (elapsedTime >= duration) {
                        game.removeEntity(this);
                        return;
                    }

                    this.x = (int) (originCenterX - (currentW / 2.0));
                    this.y = originY; 
                }

                @Override
                public void draw(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    AffineTransform old = g2d.getTransform();

                    long elapsedTime = System.currentTimeMillis() - spawnTime;
                    double progress = Math.min(1.0, (double) elapsedTime / duration);

                    // 1) 모래폭풍 스프라이트 회전 및 투명도 조정
                    AlphaComposite acSprite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f);
                    g2d.setComposite(acSprite);

                    int drawW = (int) currentW;
                    int drawH = stormHeight; // 폭풍 높이 사용

                    // 수정: 1.5초 동안 총 8회전 (회전 속도를 두 배로 증가시켜 더욱 위협적으로 보이게 함)
                    double angle = progress * (Math.PI * 2) * 64; 
                    double scaleX = Math.cos(angle); 
                    
                    double cx = x + drawW / 2.0;
                    double bottomY = y + drawH; // 하단 기준으로 고정

                    // 하단 중심을 기준으로 수직축 고정 변환 (바닥에 닿도록 유지)
                    g2d.translate(cx, bottomY);
                    g2d.scale(scaleX, 1.0);

                    if (this.sprite != null) {
                        // 이미지의 하단이 bottomY에 위치하도록 그리기
                        g2d.drawImage(this.sprite.getImage(), -drawW / 2, -drawH, drawW, drawH, null);
                    } else {
                        Color sand = new Color(210, 180, 140);
                        g2d.setColor(sand);
                        g2d.fillRect(-drawW / 2, -drawH, drawW, drawH);
                    }

                    // 원래 transform 복원
                    g2d.setTransform(old);

                    // 2) 배경에 황사 낀 듯한 연출: 화면 전체에 얇은 베이지 오버레이를 추가
                    AlphaComposite acOverlay = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f);
                    g2d.setComposite(acOverlay);
                    Color haze = new Color(210, 190, 150);
                    g2d.setColor(haze);
                    g2d.fillRect(0, 0, game.getWidth(), game.getHeight());

                    // 3) 원래 상태로 복원
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }

                @Override
                public void collidedWith(Entity other) {
                    if (damaged) return;
                    if (other instanceof UserEntity) {
                        UserEntity user = (UserEntity) other;
                        user.takeDamage(100 - user.getDefense()); // 충돌 시 데미지를 입힘
                        damaged = true;
                    }
                }

                @Override
                public String toString() { return "SandstormEntityStage3"; }
            };

            game.addEntity(storm);
            System.out.println("💥 [Stage3] 모래 폭풍 발생! (화면 전체 높이로, 더 빠르게 회전)");
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
        lastSandstormTime = 0;
        System.out.println("🔄 [Stage3] 보스 및 타이머 리셋 완료 (다시 10초 뒤 등장 예정)");
    }
}