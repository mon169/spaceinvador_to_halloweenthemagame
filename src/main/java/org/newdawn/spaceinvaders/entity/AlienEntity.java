package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;

/**
 * An entity which represents one of our space invader aliens.
 * 
 * @author Kevin Glass (modified)
 */
public class AlienEntity extends Entity {
	/** The speed at which the alien moves horizontally */
	private double moveSpeed = 75;
	/** The game in which the entity exists */
	private Game game;
	/** The alien's current health */
	private int health = 1;
	/** Frozen 상태 여부 */
	private boolean frozen = false;
	/** 얼음 효과가 풀리는 시간 */
	private long freezeEndTime = 0;

	/**
	 * Create a new alien entity
	 * 
	 * @param game The game in which this entity is being created
	 * @param x    The initial x location of this alien
	 * @param y    The initial y location of this alien
	 */
	public AlienEntity(Game game, int x, int y) {
		// choose a random sprite among 3 monsters
		super(getRandomSprite(), x, y);
		this.game = game;
		dx = -moveSpeed; // start moving left
	}

	/**
	 * Pick one of 3 sprites randomly when alien is created
	 */
	private static String getRandomSprite() {
		int rand = (int) (Math.random() * 3); // 0~2
		if (rand == 0)
			return "sprites/monster1.png";
		if (rand == 1)
			return "sprites/monster2.png";
		return "sprites/monster3.png";
	}

	/**
	 * Freeze the alien for a given duration (ms)
	 */
	public void freeze(int duration) {
		frozen = true;
		freezeEndTime = System.currentTimeMillis() + duration;
		dx = 0; // 움직임 중지
	}

	/**
	 * Request that this alien moved based on time elapsed
	 * 
	 * @param delta The time that has elapsed since last move
	 */
	@Override
	public void move(long delta) {
		// 얼어있는지 확인하고 시간이 지났으면 해제
		if (frozen && System.currentTimeMillis() > freezeEndTime) {
			frozen = false;
			dx = (dx >= 0) ? moveSpeed : -moveSpeed;
		}

		// 화면 경계 체크 (스프라이트 실제 폭 사용)
		int w = (sprite != null ? sprite.getWidth() : 35);

		// if we have reached the left hand side of the screen and
		// are moving left then request a logic update
		if ((dx < 0) && (x <= 10)) {
			x = 10;
			game.updateLogic();
		}
		// and vice versa, if we have reached the right hand side of
		// the screen and are moving right, request a logic update
		if ((dx > 0) && (x >= 800 - w - 10)) {
			x = 800 - w - 10;
			game.updateLogic();
		}

		// proceed with normal move (unless frozen)
		if (!frozen) {
			super.move(delta);
		}
	}

	/**
	 * Update the game logic related to aliens
	 */
	@Override
	public void doLogic() {
		// swap over horizontal movement and move down the
		// screen a bit
		dx = -dx;
		y += 10;

		// if dx somehow became 0, restore it
		if (dx == 0) {
			dx = (Math.random() < 0.5 ? -1 : 1) * moveSpeed;
		}

		// if we've reached the bottom of the screen then the player dies
		if (y > 570) {
			game.notifyDeath();
		}
	}

	/**
	 * Take damage from a hit
	 * 
	 * @param damage The amount of damage to take
	 * @return true if the alien died from this damage, false otherwise
	 */
	public boolean takeDamage(int damage) {
		health -= damage;
		return health <= 0;
	}

	/**
	 * Notification that this alien has collided with another entity
	 * 
	 * @param other The other entity
	 */
	@Override
	public void collidedWith(Entity other) {
		// collisions with aliens are handled elsewhere
	}
}