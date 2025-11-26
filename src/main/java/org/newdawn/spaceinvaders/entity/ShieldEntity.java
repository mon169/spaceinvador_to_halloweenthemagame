package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Boss.BossEntity;

/**
 * 🛡 요새 방어막 엔티티 (ShieldEntity)
 * - barrier.png를 사용하여 요새를 보호합니다
 * - candybucket.png보다 크게 표시되어 요새를 감싸는 효과
 * - 지속시간 5초 동안 모든 피해를 무시 (무적 상태)
 * - 지속시간이 끝나면 자동 제거
 */
public class ShieldEntity extends Entity {
    private final Game game;
    private final FortressEntity fortress;
    private final long endTime;      // 종료 시간
    private boolean active = false;

    public ShieldEntity(Game game, FortressEntity fortress, int duration) {
        // fortress의 중심 위치 기준으로 생성
        // super("sprites/shield.png", // NOTE: 주석에서 barrier.png, 코드에서 shield.png. 여기선 코드를 따름
        super("sprites/shield.png", 
              fortress.getX() + fortress.getWidth() / 2 - 50,
              fortress.getY() + fortress.getHeight() / 2 - 50);
        
        this.game = game;
        this.fortress = fortress;
        this.endTime = System.currentTimeMillis() + duration;
        this.active = true;
        
        // sprite 로드 확인
        // NOTE: 주석과 달리 코드에서는 "shield.png"를 사용
        if (this.sprite == null) {
            System.err.println("❌ ShieldEntity 생성 실패: shield.png를 로드할 수 없습니다.");
        } else {
            System.out.println("✅ ShieldEntity 생성 성공: shield.png 로드됨, 지속시간=" + (duration / 1000) + "초");
        }
    }

    @Override
    public void move(long delta) {
        // sprite가 null이면 처리하지 않음
        if (sprite == null) {
            System.err.println("⚠️ ShieldEntity: sprite가 null입니다. shield.png를 로드할 수 없습니다.");
            active = false;
            game.removeEntity(this);
            return;
        }
        
        // fortress가 null이면 처리하지 않음
        if (fortress == null) {
            System.err.println("⚠️ ShieldEntity: fortress가 null입니다.");
            active = false;
            game.removeEntity(this);
            return;
        }
        
        // 🚀 fortress 위치 따라다니기
        // FortressEntity는 scale 0.65로 그려지므로 실제 표시 크기 계산
        double fortressScale = 0.65;
        int fortressActualWidth = (int)(fortress.getWidth() * fortressScale);
        int fortressActualHeight = (int)(fortress.getHeight() * fortressScale);
        
        int fortressCenterX = fortress.getX() + fortressActualWidth / 2;
        int fortressCenterY = fortress.getY() + fortressActualHeight / 2;
        
        // shield.png가 candybucket.png보다 크게 보이도록 중심 맞춤
        // NOTE: draw() 메서드에서 실제 그리기 위치가 재계산되므로, 여기서는 FortressEntity의 중심에 맞춥니다.
        this.x = fortressCenterX - sprite.getWidth() / 2;
        this.y = fortressCenterY - sprite.getHeight() / 2;

        // ⏱ 지속시간 끝나면 자동 제거
        if (System.currentTimeMillis() > endTime) {
            active = false;
            game.removeEntity(this);
            System.out.println("⏱ 방어막 지속시간 종료 - 자동 제거");
        }
    }

    @Override
    public boolean collidesWith(Entity other) {
        // 🛡 방어막은 총알만 감지하고, 몬스터/보스와는 충돌하지 않음 (히트박스 축소)
        if (other instanceof MonsterEntity || other instanceof BossEntity) {
            return false; // 몬스터/보스와는 충돌하지 않음
        }
        // 총알만 충돌 감지 (히트박스 크기 축소)
        if (other instanceof EnemyShotEntity) {
            // 요새 주변 작은 영역만 충돌 감지
            int marginX = sprite.getWidth() / 3; // 히트박스 크기를 1/3로 축소
            int marginY = sprite.getHeight() / 3;
            
            java.awt.Rectangle me = new java.awt.Rectangle(
                (int) x + marginX,
                (int) y + marginY,
                sprite.getWidth() - marginX * 2,
                sprite.getHeight() - marginY * 2
            );
            
            java.awt.Rectangle him = new java.awt.Rectangle(
                (int) other.x,
                (int) other.y,
                other.sprite.getWidth(),
                other.sprite.getHeight()
            );
            
            return me.intersects(him);
        }
        return super.collidesWith(other);
    }

    @Override
    public void collidedWith(Entity other) {
        // 🛡 지속시간 동안 모든 피해 무시 (무적 상태)
        // 충돌해도 방어막은 제거되지 않고 지속시간이 끝날 때까지 유지
        if (other instanceof EnemyShotEntity) {
            EnemyShotEntity shot = (EnemyShotEntity) other;
            onBlocked(shot); 			// 💫 효과용 콜백
            shot.setBlockedByShield(); 	// 총알에 "막혔다" 표시
            game.removeEntity(shot); 	// 총알만 제거 (방어막은 유지)
            System.out.println("🛡 방어막이 적 공격을 막았습니다! (방어막 유지)");
        }
        // 🛡 몬스터와 충돌 시 몬스터만 제거, 방어막은 유지
        if (other instanceof MonsterEntity) {
            MonsterEntity monster = (MonsterEntity) other;
            onBlockedMonster(monster); 	// 💫 효과용 콜백
            game.removeEntity(monster); 	// 몬스터만 제거 (방어막은 유지)
            System.out.println("🛡 방어막이 몬스터 충돌을 막았습니다! (방어막 유지)");
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
        if (fortress == null || sprite == null) return;
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;

        // 💡 투명도(AlphaComposite) 설정을 제거하여 원래 이미지대로 불투명하게 그립니다.

        // shield.png를 원래 크기로 표시 (크기 조정 제거)
        double scale = 1.0; 
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
        
        // 불투명하게 그리기 (AlphaComposite 설정 제거)
        g2.drawImage(scaled, drawX, drawY, null);

    }

    public boolean isActive() {
        return active && System.currentTimeMillis() < endTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
}