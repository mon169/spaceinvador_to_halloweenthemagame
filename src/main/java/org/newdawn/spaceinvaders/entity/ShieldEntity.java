package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

/**
 * 🛡 요새 방어막 엔티티 (ShieldEntity)
 * - barrier.png를 사용하여 요새를 보호합니다
 * - candybucket.png보다 크게 표시되어 요새를 감싸는 효과
 * - 적 공격 1회 또는 몬스터 충돌 1회 방어 후 즉시 소멸
 * - 지속시간 동안 유지되지만, 충돌 시 즉시 제거됨
 */
public class ShieldEntity extends Entity {
    private final Game game;
    private final FortressEntity fortress;
    private final int duration;      // 방어막 지속 시간 (ms)
    private final long endTime;      // 종료 시간
    private boolean active = false;

    public ShieldEntity(Game game, FortressEntity fortress, int duration) {
        // fortress의 중심 위치 기준으로 생성
        super("sprites/barrier.png",
              fortress.getX() + fortress.getWidth() / 2 - 50,
              fortress.getY() + fortress.getHeight() / 2 - 50);
        
        this.game = game;
        this.fortress = fortress;
        this.duration = duration;
        this.endTime = System.currentTimeMillis() + duration;
        this.active = true;
    }

    @Override
    public void move(long delta) {
        // 🚀 fortress 위치 따라다니기
        // FortressEntity는 scale 0.65로 그려지므로 실제 표시 크기 계산
        double fortressScale = 0.65;
        int fortressActualWidth = (int)(fortress.getWidth() * fortressScale);
        int fortressActualHeight = (int)(fortress.getHeight() * fortressScale);
        
        int fortressCenterX = fortress.getX() + fortressActualWidth / 2;
        int fortressCenterY = fortress.getY() + fortressActualHeight / 2;
        
        // barrier.png가 candybucket.png보다 크게 보이도록 중심 맞춤
        this.x = fortressCenterX - sprite.getWidth() / 2;
        this.y = fortressCenterY - sprite.getHeight() / 2;

        // ⏱ 지속시간 끝나면 자동 제거
        if (System.currentTimeMillis() > endTime) {
            active = false;
            game.removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // 🛡 적 총알 또는 몬스터 충돌 시 1회 방어 후 방어막 제거
        if (other instanceof EnemyShotEntity) {
            EnemyShotEntity shot = (EnemyShotEntity) other;
            onBlocked(shot);               // 💫 효과용 콜백
            shot.setBlockedByShield();     // 총알에 "막혔다" 표시
            game.removeEntity(shot);       // 총알 제거
            game.removeEntity(this);       // 방어막도 제거 (1회 방어만 가능)
            active = false;
            System.out.println("🛡 방어막이 적 공격을 막았습니다! (방어막 소멸)");
        }
        // 🛡 몬스터와 충돌 시 몬스터 제거, 방어막도 제거 (1회 방어)
        if (other instanceof MonsterEntity) {
            MonsterEntity monster = (MonsterEntity) other;
            onBlockedMonster(monster);     // 💫 효과용 콜백
            game.removeEntity(monster);    // 몬스터 제거
            game.removeEntity(this);       // 방어막도 제거 (1회 방어만 가능)
            active = false;
            System.out.println("🛡 방어막이 몬스터 충돌을 막았습니다! (방어막 소멸)");
        }
    }

    /** 💥 총알이 방어막에 막혔을 때 호출되는 콜백 */
    public void onBlocked(EnemyShotEntity shot) {
        System.out.println("🛡 요새 방어막이 " + shot.getShotKind() + " 차단!");
    }

    /** 💥 몬스터가 방어막에 막혔을 때 호출되는 콜백 */
    public void onBlockedMonster(MonsterEntity monster) {
        System.out.println("🛡 요새 방어막이 몬스터 충돌을 차단!");
    }

    @Override
    public void draw(java.awt.Graphics g) {
        if (sprite == null) return;
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;

        // 💫 방어막 완전 불투명 (투명도 100%)
        float alpha = 1.0f; // 완전 불투명

        java.awt.AlphaComposite ac = java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha);
        g2.setComposite(ac);

        // barrier.png를 candybucket.png보다 크게 표시
        // candybucket은 0.65 scale이므로, barrier는 그것보다 크게 (약 0.225배)
        double scale = 0.225;
        int newW = (int) (sprite.getWidth() * scale);
        int newH = (int) (sprite.getHeight() * scale);
        java.awt.Image scaled = sprite.getImage().getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
        
        // 요새 중심에 맞춰 그리기
        double fortressScale = 0.65;
        int fortressActualWidth = (int)(fortress.getWidth() * fortressScale);
        int fortressActualHeight = (int)(fortress.getHeight() * fortressScale);
        int fortressCenterX = fortress.getX() + fortressActualWidth / 2;
        int fortressCenterY = fortress.getY() + fortressActualHeight / 2;
        int drawX = fortressCenterX - newW / 2;
        int drawY = fortressCenterY - newH / 2;
        
        g2.drawImage(scaled, drawX, drawY, null);

        g2.setComposite(java.awt.AlphaComposite.SrcOver);
    }

    public boolean isActive() {
        return active;
    }
}
