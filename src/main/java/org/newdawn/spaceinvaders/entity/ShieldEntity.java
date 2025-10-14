package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

/**
 * 🛡 ShipEntity의 방어막(Shield) 엔티티
 * - 일정 시간 동안 유지되며, 적의 총알(EnemyShotEntity)을 차단한다.
 * - 총알은 사라지지만 방어막은 즉시 사라지지 않는다.
 * - 💫 onBlocked() 메서드 추가: 총알 차단 시 효과/로그용
 */
public class ShieldEntity extends Entity {
    private final Game game;
    private final UserEntity ship;
    private final int duration;      // 방어막 지속 시간 (ms)
    private final long endTime;      // 종료 시간
    private boolean active = false;

    public ShieldEntity(Game game, UserEntity ship, int duration) {
        // ship의 중심 위치 기준으로 생성
        super("sprites/shield.png",
              ship.getX() + ship.sprite.getWidth() / 2 - 24,
              ship.getY() + ship.sprite.getHeight() / 2 - 48);

        this.game = game;
        this.ship = ship;
        this.duration = duration;
        this.endTime = System.currentTimeMillis() + duration;
        this.active = true;
    }

    @Override
    public void move(long delta) {
        // 🚀 ship 위치 따라다니기
        this.x = ship.getX() + ship.sprite.getWidth() / 2 - sprite.getWidth() / 2;
        this.y = ship.getY() + ship.sprite.getHeight() / 2 - sprite.getHeight() / 2;

        // ⏱ 지속시간 끝나면 자동 제거
        if (System.currentTimeMillis() > endTime) {
            active = false;
            game.removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // 적 총알과 충돌 시 총알 제거, 방어막은 유지
        if (other instanceof EnemyShotEntity) {
            EnemyShotEntity shot = (EnemyShotEntity) other;
            onBlocked(shot);               // 💫 효과용 콜백
            shot.setBlockedByShield();     // 총알에 "막혔다" 표시
            game.removeEntity(shot);       // 총알 제거
            // ❗ 방어막은 지속시간 동안 유지됨
        }
    }

    /** 💥 총알이 방어막에 막혔을 때 호출되는 콜백 */
    public void onBlocked(EnemyShotEntity shot) {
        // 🔊 시각 효과나 로그를 여기에 넣을 수 있음
        System.out.println("🛡 방어막이 " + shot.getShotKind() + " 차단!");
        // 💡 추후 spark 이펙트나 사운드 추가 가능
    }

    @Override
    public void draw(java.awt.Graphics g) {
        if (sprite == null) return;
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;

        // 💫 방어막 지속시간에 따라 투명도 변화
        long now = System.currentTimeMillis();
        float progress = Math.max(0f, Math.min(1f, (endTime - now) / (float) duration));
        float alpha = 0.3f + 0.4f * progress; // 남은 시간에 따라 점점 희미해짐

        java.awt.AlphaComposite ac = java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha);
        g2.setComposite(ac);

        // 방어막 그리기 (약간 확대 효과)
        double scale = 1.2;
        int newW = (int) (sprite.getWidth() * scale);
        int newH = (int) (sprite.getHeight() * scale);
        java.awt.Image scaled = sprite.getImage().getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
        g2.drawImage(scaled,
                (int) x - (newW - sprite.getWidth()) / 2,
                (int) y - (newH - sprite.getHeight()) / 2,
                null);

        g2.setComposite(java.awt.AlphaComposite.SrcOver);
    }

    public boolean isActive() {
        return active;
    }
}
