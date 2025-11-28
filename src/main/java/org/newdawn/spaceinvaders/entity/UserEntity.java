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
 * ShipEntity - 플레이어 캐릭터
 * 좌우 이동 시 스프라이트 전환, 축소 렌더링
 */
public class UserEntity extends Entity {

    // =====================================================
    // Constants
    // =====================================================
    private static final int DEFAULT_MAX_HEALTH = 2000;
    private static final int DEFAULT_ATTACK_POWER = 15;
    private static final double DEFAULT_MOVE_SPEED = 300;
    private static final long DEFAULT_FIRING_INTERVAL = 500;
    
    private static final int BOUNDARY_LEFT = 10;
    private static final int BOUNDARY_RIGHT = 750;
    
    private static final int MIN_DAMAGE = 1;
    private static final double DRAW_SCALE = 0.13;
    private static final double SIZE_SCALE = 0.5;
    
    private static final int BOMB_Y_OFFSET = 30;
    private static final int SHIELD_DURATION = 5000;
    private static final int MILLIS_TO_SECONDS = 1000;

    // =====================================================
    // Fields
    // =====================================================
    private final Game game;

    private int maxHealth = DEFAULT_MAX_HEALTH;
    private int currentHealth;
    private int defense = 0;
    private int attackPower = DEFAULT_ATTACK_POWER;
    private boolean isFrozen = false;
    private long frozenEndTime = 0;

    private int bombCount = 0;
    private int iceWeaponCount = 0;
    private int shieldCount = 0;

    private double moveSpeed = DEFAULT_MOVE_SPEED;
    private long firingInterval = DEFAULT_FIRING_INTERVAL;

    private boolean canAttack = true;
    private int money = 0;
    private List<Item> inventory = new ArrayList<>();

    // 방향 및 스프라이트 관리
    private boolean movingRight = true;
    private final String spriteRight = "sprites/user1r.png";
    private final String spriteLeft  = "sprites/user1l.png";

    public UserEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
        this.currentHealth = maxHealth;
        this.sprite = SpriteStore.get().getSprite(spriteRight);
    }

    // 이동 방향에 따라 스프라이트 변경
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

    @Override
    public void draw(Graphics g) {
        if (sprite == null) return;
        Graphics2D g2 = (Graphics2D) g;

        int newW = (int) (sprite.getWidth() * DRAW_SCALE);
        int newH = (int) (sprite.getHeight() * DRAW_SCALE);

        Image scaled = sprite.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        g2.drawImage(scaled, (int) x, (int) y, null);
    }

    // 상태 관리

    public int getHealth() { return currentHealth; }

    public void heal(int amount) {
        currentHealth = Math.min(currentHealth + amount, maxHealth);
    }

    public void takeDamage(int damage) {
        if (game.hasActiveShield()) {
            return;
        }
        
        int actualDamage = Math.max(MIN_DAMAGE, damage - defense);
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

    @Override
    public void move(long delta) {
        checkFrozenStatus();
        if (isFrozen) return;

        if ((dx < 0) && (x < BOUNDARY_LEFT)) return;
        if ((dx > 0) && (x > BOUNDARY_RIGHT)) return;

        super.move(delta);
    }

    // 충돌 처리
    @Override
    public void collidedWith(Entity other) {
        if (other instanceof MonsterEntity) {
            game.notifyDeath();
        }
    }

    // =====================================================
    // 아이템/상점 연동
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

    public void spendMoney(int amount) { this.money -= amount; }
    public void earnMoney(int amount)  { this.money += amount; }
    public int getMoney()              { return money; }

    // =====================================================
    // 무기 및 특수 기능
    // =====================================================
    public void giveBomb()      { this.bombCount++; }
    public void giveIceWeapon() { this.iceWeaponCount++; }
    public void giveShield()    { this.shieldCount++; }

    public boolean hasBomb()       { return bombCount > 0; }
    public boolean hasIceWeapon()  { return iceWeaponCount > 0; }
    public boolean hasShield()     { return shieldCount > 0; }

    public int getBombCount()      { return bombCount; }
    public int getIceWeaponCount() { return iceWeaponCount; }
    public int getShieldCount()    { return shieldCount; }

    public void useBomb() {
        if (bombCount > 0) {
            System.out.println("💣 useBomb 호출 — 폭탄 발사 시도 (남은: " + bombCount + ")");
            game.addEntity(new org.newdawn.spaceinvaders.entity.BombShotEntity(game, "sprites/bombshot.png", (int) x, (int) y - BOMB_Y_OFFSET));
            bombCount--;
            System.out.println("💣 폭탄 생성 완료 — 남은 폭탄: " + bombCount);
        }
    }

    public void useIceWeapon() {
        if (iceWeaponCount > 0) {
            game.addEntity(new IceShotEntity(game, "sprites/iceshot.png", (int) x, (int) y));
            iceWeaponCount--;
        }
    }

    public void activateShield() {
        if (shieldCount > 0 && game.getFortress() != null) {
            game.addEntity(new ShieldEntity(game, game.getFortress(), SHIELD_DURATION));
            shieldCount--;
            System.out.println("방어막 활성화 (" + SHIELD_DURATION / MILLIS_TO_SECONDS + "초)");
        }
    }

    // =====================================================
    // 상태 복사 (copyStateFrom)
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
    }

    // 기타 유틸
    public void setFiringInterval(long interval) { this.firingInterval = interval; }
    public long getFiringInterval() { return this.firingInterval; }

    public void setMoveSpeed(double speed) { this.moveSpeed = speed; }
    public double getMoveSpeed() { return this.moveSpeed; }

    public void setCanAttack(boolean canAttack) { this.canAttack = canAttack; }
    public boolean canAttack() { return canAttack; }

    public int getWidth()  { return (int) (sprite.getWidth()  * SIZE_SCALE); }
    public int getHeight() { return (int) (sprite.getHeight() * SIZE_SCALE); }

    // =====================================================
    // Game.java와 연동용 Getter / Setter (네트워크용)
    // =====================================================
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getHp() { return this.currentHealth; }

    private int score = 0;
    public int getScore() { return this.score; }
    public void addScore(int value) { this.score += value; }
}