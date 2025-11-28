package org.newdawn.spaceinvaders.entity.Boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.BombShotEntity;
import org.newdawn.spaceinvaders.entity.MonsterEntity;
import org.newdawn.spaceinvaders.entity.EnemyShotEntity; 
// 필요시 다른 ShotEntity 파생 클래스도 import 할 수 있지만, 아래 코드는 클래스 이름으로 광범위하게 방어합니다.


public abstract class BossEntity extends Entity {
    protected int health = 1000;
    protected Game game;

    public BossEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.bossDefeated();
            game.removeEntity(this);
        }
    }

    public int getHealth() {
        return health;
    }

    /**
     * 🛡️ 충돌 방어 로직 (모든 BossX.java에 자동으로 적용됩니다.)
     * 플레이어 아이템/탄환에 충돌 시, 보스가 스스로 삭제되는 것을 방지합니다.
     */
    @Override
    public void collidedWith(Entity other) {
        String otherClassName = other.getClass().getSimpleName();
        
        // 1. 아이템/폭탄/총알 계열 충돌 시:
        // - 보스가 스스로를 삭제(removeEntity)하는 것을 막기 위해 아무것도 하지 않고 종료합니다.
        // - 아이템의 효과(데미지/동결)는 해당 아이템 클래스(BombShotEntity 등)에서 별도로 처리합니다.
        if (other instanceof BombShotEntity ||           // 명시적인 BombShotEntity
            otherClassName.contains("Item") ||    // 모든 ShotEntity 파생 클래스 (IceShot 포함)
            otherClassName.contains("SheildEntity")) {          // Item 엔티티
            
            return; // 보스는 무시
        }

        // 2. 적 총알이나 다른 몬스터는 무시합니다. (Boss1.java에서 가져온 기존 로직)
        if (other instanceof EnemyShotEntity || other instanceof MonsterEntity) {
            return;
        }

        // 이외의 충돌(예: 플레이어 기체와의 충돌 등)은 여기에 추가 로직을 넣을 수 있습니다.
    }
}