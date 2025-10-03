package org.newdawn.spaceinvaders.entity;
import java.util.ArrayList;
import java.util.List;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.shop.Item;

/**
 * The entity that represents the players ship
 * 
 * @author Kevin Glass
 */
public class ShipEntity extends Entity {
	/** The game in which the ship exists */
	private Game game;
	private int maxHealth = 10; // 초기 체력을 10으로 설정
	private int currentHealth;
	private int defense = 0;
	private int attackPower = 1;
	private boolean isFrozen = false;
	private long frozenEndTime = 0;
	private int bombCount = 0;
	private int iceWeaponCount = 0;
	private int shieldCount = 0;
	public boolean hasShield() {
		return shieldCount > 0;
	}

	public int getShieldCount() {
		return shieldCount;
	}

	public void giveShield() {
		this.shieldCount++;
	}

	public void useShield(int duration) {
		if (shieldCount > 0) {
			game.addEntity(new ShieldEntity(game, this, duration));
			shieldCount--;
		}
	}
	
	/**
	 * Create a new entity to represent the players ship
	 *  
	 * @param game The game in which the ship is being created
	 * @param ref The reference to the sprite to show for the ship
	 * @param x The initial x location of the player's ship
	 * @param y The initial y location of the player's ship
	 */
	public ShipEntity(Game game,String ref,int x,int y) {
		super(ref,x,y);
		this.game = game;
		this.currentHealth = maxHealth;
	}
	
	public int getHealth() {
		return currentHealth;
	}
	
	public void heal(int amount) {
		currentHealth = Math.min(currentHealth + amount, maxHealth);
	}
	
	public void takeDamage(int damage) {
		int actualDamage = Math.max(1, damage - defense);
		currentHealth -= actualDamage;
		// 체력이 0 이하면 게임 오버 (스테이지별 추가 제한은 Game 클래스에서 처리)
		if (currentHealth <= 0) {
			game.notifyDeath();
		}
	}
	
	public void increaseMaxHealth(int amount) {
		maxHealth += amount;
		currentHealth = maxHealth;
	}
	
	public void increaseDefense(int amount) {
		defense += amount;
	}
	
	public int getDefense() {
		return defense;
	}
	
	public void increaseAttackPower(int amount) {
		attackPower += amount;
	}
	
	public int getAttackPower() {
		return attackPower;
	}
	
	public void freeze(long duration) {
		isFrozen = true;
		frozenEndTime = System.currentTimeMillis() + duration;
	}
	
	public void checkFrozenStatus() {
		if (isFrozen && System.currentTimeMillis() > frozenEndTime) {
			isFrozen = false;
		}
	}

	private int money = 0; // 플레이어의 초기 돈을 0으로 설정
    private List<Item> inventory = new ArrayList<>();

    // 돈을 사용하는 메서드
    public void spendMoney(int amount) {
        this.money -= amount;
    }

    // 돈을 얻는 메서드 (예: 외계인 처치 시 호출)
    public void earnMoney(int amount) {
        this.money += amount;
    }
    
    // 인벤토리에 아이템을 추가하는 메서드
    public void addItem(Item item) {
        this.inventory.add(item);
    }

    // 현재 돈을 확인하는 메서드
    public int getMoney() {
        return money;
    }
    
    private long firingInterval = 500; // 발사 간격 변수
    public void setFiringInterval(long interval) {
        this.firingInterval = interval;
    }
    
    public long getFiringInterval() {
        return this.firingInterval;
    }
    
    private double moveSpeed = 300; // 이동 속도 변수
    
    public double getMoveSpeed() {
        return moveSpeed;
    }
    
    public void setMoveSpeed(double speed) {
        this.moveSpeed = speed;
    }
    
    @Override
    public void setHorizontalMovement(double speed) {
        super.setHorizontalMovement(speed);
    }

    @Override
    public double getHorizontalMovement() {
        return super.getHorizontalMovement();
    }
	
	/**
	 * Request that the ship move itself based on an elapsed ammount of
	 * time
	 * 
	 * @param delta The time that has elapsed since last move (ms)
	 */
	public void move(long delta) {
		checkFrozenStatus();
		if (isFrozen) {
			// 얼려진 상태에서는 움직이지 않음
			return;
		}
		// if we're moving left and have reached the left hand side
		// of the screen, don't move
		if ((dx < 0) && (x < 10)) {
			return;
		}
		// if we're moving right and have reached the right hand side
		// of the screen, don't move
		if ((dx > 0) && (x > 750)) {
			return;
		}
		
		super.move(delta);
	}
	
	/**
	 * Notification that the player's ship has collided with something
	 * 
	 * @param other The entity with which the ship has collided
	 */
	public void collidedWith(Entity other) {
		// if its an alien, notify the game that the player
		// is dead
		if (other instanceof AlienEntity) {
			game.notifyDeath();
		}
	}
	
	/**
	 * 이전 우주선의 상태를 현재 우주선으로 복사
	 */
	public void copyStateFrom(ShipEntity other) {
		this.maxHealth = other.maxHealth;
		this.currentHealth = other.currentHealth;
		this.defense = other.defense;
		this.attackPower = other.attackPower;
		this.moveSpeed = other.moveSpeed;
		this.firingInterval = other.firingInterval;
		this.money = other.money;
		this.bombCount = other.bombCount;
		this.iceWeaponCount = other.iceWeaponCount;
		this.shieldCount = other.shieldCount; // 방어막 카운트도 복사
	}

	public void giveBomb() {
		this.bombCount++;
	}

	public void giveIceWeapon() {
		this.iceWeaponCount++;
	}

	public boolean hasBomb() {
		return bombCount > 0;
	}

	public boolean hasIceWeapon() {
		return iceWeaponCount > 0;
	}

	public int getBombCount() {
		return bombCount;
	}

	public int getIceWeaponCount() {
		return iceWeaponCount;
	}

	public void useBomb() {
		if (bombCount > 0 && game.itemsAllowed()) {
			// 폭탄을 배 앞쪽에 생성하여 충돌을 방지 (y-30으로 배 위쪽에 생성)
			game.addEntity(new BombEntity(game, "sprites/shot.png", (int)x, (int)y-30));
			bombCount--;
		}
	}

	public void useIceWeapon() {
		if (iceWeaponCount > 0 && game.itemsAllowed()) {
			game.addEntity(new IceEntity(game, "sprites/shot.png", (int)x, (int)y));
			iceWeaponCount--;
		}
	}
	
	// ShipEntity의 아이템 하나 제거 메서드 추가
    public boolean removeOneItem() {
        if (!inventory.isEmpty()) {
            inventory.remove(inventory.size() - 1);
            return true;
        }
        return false;
    }

    // ShipEntity에 방어막 활성화 메서드 수정
    public void activateShield() {
        if (game.itemsAllowed()) {
            // 방어막 인벤토리가 있는지 확인
            if (shieldCount > 0) {
                // 방어력이 0이라도 최소 3초는 지속되도록 함
                int duration = Math.max(3000, defense * 1000); // 방어력 1당 1초, 최소 3초
                game.addEntity(new ShieldEntity(game, this, duration));
                shieldCount--; // 방어막 사용
                System.out.println("방어막 활성화! 지속시간: " + duration/1000 + "초");
            } else {
                System.out.println("방어막이 없습니다!");
            }
        }
    }
    
    private boolean canAttack = true;
    public void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }
    public boolean canAttack() {
        return canAttack;
    }
}