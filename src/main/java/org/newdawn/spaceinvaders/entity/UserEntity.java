package org.newdawn.spaceinvaders.entity;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.shop.Item;

/**
 * 🎮 ShipEntity - 플레이어 캐릭터
 * 좌우 이동 시 스프라이트 전환, 축소 렌더링
 */
public class UserEntity extends Entity {
    private final Game game;

    private int maxHealth = 2000;
    private int currentHealth;
    private int defense = 0;
    private int attackPower = 15;
    private boolean isFrozen = false;
    private long frozenEndTime = 0;

    private int bombCount = 0;
    private int iceWeaponCount = 0;
    private int shieldCount = 0;

    private double moveSpeed = 300;
    private long firingInterval = 500;

    private boolean canAttack = true;
    private int money = 0;
    private List<Item> inventory = new ArrayList<>();

    // 방향 및 스프라이트 관리
    private boolean movingRight = true;
    private final String spriteRight = "sprites/userr.png";
    private final String spriteLeft  = "sprites/userl.png";

    public UserEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
        this.currentHealth = maxHealth;
        this.sprite = SpriteStore.get().getSprite(spriteRight);
    }

    // =====================================================
    // 🔹 이동 방향에 따라 스프라이트 변경
    // =====================================================
    @Override
    public void setHorizontalMovement(double speed) {
        super.setHorizontalMovement(speed);

        if (speed > 0 && !movingRight) {
            movingRight = true;
            this.sprite = SpriteStore.get().getSprite(spriteRight);
        } else if (speed < 0 && movingRight) {
            movingRight = false;
            this.sprite = SpriteStore.get().getSprite(spriteLeft);
        }
    }

    // =====================================================
    // 🔹 축소 렌더링
    // =====================================================
    @Override
    public void draw(Graphics g) {
        if (sprite == null) return;
        Graphics2D g2 = (Graphics2D) g;

        // NOTE: 원본 코드에서 0.13로 쓰던 비율 유지
        double scale = 0.13;
        int newW = (int) (sprite.getWidth() * scale);
        int newH = (int) (sprite.getHeight() * scale);

        Image scaled = sprite.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        g2.drawImage(scaled, (int) x, (int) y, null);
    }

    // =====================================================
    // 🔹 상태 관리
    // =====================================================
    public int getHealth() { return currentHealth; }

    public void heal(int amount) {
        currentHealth = Math.min(currentHealth + amount, maxHealth);
    }

    public void takeDamage(int damage) {
        // 🛡 방어막이 활성화되어 있으면 피해 무시 (무적)
        if (game.hasActiveShield()) {
            System.out.println("🛡 방어막이 플레이어 피해를 막았습니다! (무적 상태)");
            return;
        }
        
        int actualDamage = Math.max(1, damage - defense);
        currentHealth -= actualDamage;
        if (currentHealth <= 0) game.notifyDeath();
    }

    public void increaseMaxHealth(int amount) {
        maxHealth += amount;
        currentHealth = maxHealth;
    }

    public void increaseDefense(int amount) { defense += amount; }
    public int getDefense() { return defense; }

    public void increaseAttackPower(int amount) { attackPower += amount; }
    public int getAttackPower() { return attackPower; }

    public void freeze(long duration) {
        isFrozen = true;
        frozenEndTime = System.currentTimeMillis() + duration;
    }

    public void checkFrozenStatus() {
        if (isFrozen && System.currentTimeMillis() > frozenEndTime) {
            isFrozen = false;
        }
    }

    // =====================================================
    // 🔹 이동 제어
    // =====================================================
    @Override
    public void move(long delta) {
        checkFrozenStatus();
        if (isFrozen) return;

        if ((dx < 0) && (x < 10)) return;
        if ((dx > 0) && (x > 750)) return;

        super.move(delta);
    }

    // =====================================================
    // 🔹 충돌 처리
    // =====================================================
    @Override
    public void collidedWith(Entity other) {
        if (other instanceof MonsterEntity) {
            game.notifyDeath();
        }
    }

    // =====================================================
    // 🔹 아이템/상점 연동
    // =====================================================
    public void addItem(Item item) {
        this.inventory.add(item);
    }

    public boolean removeOneItem() {
        if (!inventory.isEmpty()) {
            inventory.remove(inventory.size() - 1);
            return true;
        }
        return false;
    }

    public void spendMoney(int amount) { 
        if (this.money >= amount) {
            this.money -= amount;
            System.out.println("💰 골드 차감: " + amount + " (남은 골드: " + this.money + ")");
        } else {
            System.out.println("⚠️ 골드 부족: 필요 " + amount + ", 보유 " + this.money);
        }
    }
    public void earnMoney(int amount)  { this.money += amount; }
    public int getMoney()              { return money; }

    // =====================================================
    // 🔹 무기 및 특수 기능
    //   (Game.itemsAllowed() 의존 제거 → 항상 사용 가능)
    //   나중에 제한을 다시 걸고 싶으면 조건만 추가해줘.
    // =====================================================
    public void giveBomb()      { 
        this.bombCount++; 
        System.out.println("💣 폭탄 획득! 현재 개수: " + this.bombCount);
    }
    public void giveIceWeapon() { 
        this.iceWeaponCount++; 
        System.out.println("🧊 얼음 무기 획득! 현재 개수: " + this.iceWeaponCount);
    }
    public void giveShield()    { 
        this.shieldCount++; 
        System.out.println("🛡 방어막 획득! 현재 개수: " + this.shieldCount + " (hasShield=" + hasShield() + ")");
    }

    public boolean hasBomb()       { return bombCount > 0; }
    public boolean hasIceWeapon()  { return iceWeaponCount > 0; }
    public boolean hasShield()     { return shieldCount > 0; }

    public int getBombCount()      { return bombCount; }
    public int getIceWeaponCount() { return iceWeaponCount; }
    public int getShieldCount()    { return shieldCount; }

    public void useBomb() {
        if (bombCount > 0) {
            game.addEntity(new BombShotEntity(game, "sprites/shot.png", (int) x, (int) y - 30));
            bombCount--;
        }
    }

    public void useIceWeapon() {
        if (iceWeaponCount > 0) {
            game.addEntity(new IceShotEntity(game, "sprites/shot.png", (int) x, (int) y));
            iceWeaponCount--;
        }
    }

    /** 요새 방어막 활성화 */
    public void activateShield() {
        System.out.println("🛡 activateShield() 호출됨 - shieldCount=" + shieldCount + ", fortress=" + (game.getFortress() != null));
        try {
            if (shieldCount > 0 && game.getFortress() != null) {
                // 지속시간은 항상 5초로 고정 (무적 시간)
                int duration = 5000;
                ShieldEntity shield = new ShieldEntity(game, game.getFortress(), duration);
                game.addEntity(shield);
                shieldCount--;
                System.out.println("✅ 요새 방어막 활성화 성공! (5초 무적, 남은 개수: " + shieldCount + ")");
            } else {
                if (shieldCount <= 0) {
                    System.out.println("⚠️ 방어막 사용 불가: 보유 개수가 0입니다. 상점에서 구매하거나 보상으로 받아주세요.");
                }
                if (game.getFortress() == null) {
                    System.out.println("⚠️ 방어막 사용 불가: 요새가 존재하지 않습니다.");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 방어막 활성화 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================
    // 🔹 상태 복사 (copyStateFrom)
    // =====================================================
    public void copyStateFrom(UserEntity other) {
        this.maxHealth      = other.maxHealth;
        this.currentHealth  = other.currentHealth;
        this.defense        = other.defense;
        this.attackPower    = other.attackPower;
        this.bombCount      = other.bombCount;
        this.iceWeaponCount = other.iceWeaponCount;
        this.shieldCount    = other.shieldCount;
        this.moveSpeed      = other.moveSpeed;
        this.firingInterval = other.firingInterval;
        this.money          = other.money;
        this.inventory      = new ArrayList<>(other.inventory);
        
        System.out.println("📋 상태 복사 완료 - shieldCount: " + other.shieldCount + " → " + this.shieldCount + " (hasShield=" + this.hasShield() + ")");
    }

    // =====================================================
    // 🔹 기타 유틸
    // =====================================================
    public void setFiringInterval(long interval) { this.firingInterval = interval; }
    public long getFiringInterval() { return this.firingInterval; }

    public void setMoveSpeed(double speed) { this.moveSpeed = speed; }
    public double getMoveSpeed() { return this.moveSpeed; }

    public void setCanAttack(boolean canAttack) { this.canAttack = canAttack; }
    public boolean canAttack() { return canAttack; }

    public int getWidth()  { return (int) (sprite.getWidth()  * 0.5); }
    public int getHeight() { return (int) (sprite.getHeight() * 0.5); }
}
