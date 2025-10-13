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
 * ShipEntity - 플레이어 캐릭터.
 * 좌우 이동 시 스프라이트 전환, 크기 축소 버전으로 그려진다.
 */
public class ShipEntity extends Entity {
    private Game game;

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
    private String spriteRight = "sprites/userr.png";
    private String spriteLeft = "sprites/userl.png";

    public ShipEntity(Game game, String ref, int x, int y) {
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

    // 크기 줄인 그리기 (0.13배 스케일 적용)
    @Override
    public void draw(Graphics g) {
        if (sprite == null) return;
        Graphics2D g2 = (Graphics2D) g;

        double scale = 0.13;
        int newW = (int)(sprite.getWidth() * scale);
        int newH = (int)(sprite.getHeight() * scale);

        Image scaled = sprite.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        g2.drawImage(scaled, (int) x, (int) y, null);
    }

    // 상태 관리

    public int getHealth() { return currentHealth; }

    public void heal(int amount) { currentHealth = Math.min(currentHealth + amount, maxHealth); }

    public void takeDamage(int damage) {
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

    // 이동 제어
    @Override
    public void move(long delta) {
        checkFrozenStatus();
        if (isFrozen) return;

        // 경계 체크
        if ((dx < 0) && (x < 10)) return;
        if ((dx > 0) && (x > 750)) return;

        super.move(delta);
    }

    // 충돌 처리
    @Override
    public void collidedWith(Entity other) {
        if (other instanceof AlienEntity) {
            game.notifyDeath();
        }
    }

    // 아이템/상점 연동용
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
    public void earnMoney(int amount) { this.money += amount; }
    public int getMoney() { return money; }

    // 무기 및 특수 기능
    public void giveBomb() { this.bombCount++; }
    public void giveIceWeapon() { this.iceWeaponCount++; }
    public void giveShield() { this.shieldCount++; }

    public boolean hasBomb() { return bombCount > 0; }
    public boolean hasIceWeapon() { return iceWeaponCount > 0; }
    public boolean hasShield() { return shieldCount > 0; }

    // Game.java에서 호출하는 getter들
    public int getBombCount() { return bombCount; }
    public int getIceWeaponCount() { return iceWeaponCount; }
    public int getShieldCount() { return shieldCount; }

    public void useBomb() {
        if (bombCount > 0 && game.itemsAllowed()) {
            game.addEntity(new BombEntity(game, "sprites/shot.png", (int) x, (int) y - 30));
            bombCount--;
        }
    }

    public void useIceWeapon() {
        if (iceWeaponCount > 0 && game.itemsAllowed()) {
            game.addEntity(new IceEntity(game, "sprites/shot.png", (int) x, (int) y));
            iceWeaponCount--;
        }
    }

    public void activateShield() {
        if (game.itemsAllowed() && shieldCount > 0) {
            // 방어력에 따라 쉴드 지속 시간 증가 (최소 3초)
            int duration = Math.max(3000, defense * 1000); 
            game.addEntity(new ShieldEntity(game, this, duration));
            shieldCount--;
            System.out.println("방어막 활성화 (" + duration / 1000 + "초)");
        }
    }

    // 상태 복사 (copyStateFrom)
    public void copyStateFrom(ShipEntity other) {
        this.maxHealth = other.maxHealth;
        this.currentHealth = other.currentHealth;
        this.defense = other.defense;
        this.attackPower = other.attackPower;
        this.bombCount = other.bombCount;
        this.iceWeaponCount = other.iceWeaponCount;
        this.shieldCount = other.shieldCount;
        this.moveSpeed = other.moveSpeed;
        this.firingInterval = other.firingInterval;
        this.money = other.money;
        this.inventory = new ArrayList<>(other.inventory);
    }

    // 기타 유틸
    public void setFiringInterval(long interval) { this.firingInterval = interval; }
    public long getFiringInterval() { return this.firingInterval; }

    public void setMoveSpeed(double speed) { this.moveSpeed = speed; }
    public double getMoveSpeed() { return this.moveSpeed; }

    public void setCanAttack(boolean canAttack) { this.canAttack = canAttack; }
    public boolean canAttack() { return canAttack; }

    public int getWidth() { return (int)(sprite.getWidth() * 0.5); }
    public int getHeight() { return (int)(sprite.getHeight() * 0.5); }
}