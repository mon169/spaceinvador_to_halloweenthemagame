package org.newdawn.spaceinvaders.entity.Boss;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.spaceinvaders.*;
import org.newdawn.spaceinvaders.entity.*;

public class Boss2 extends MonsterEntity {
	// --------------------------
	//  🔧 기본 설정
	// --------------------------
	private final Game game;

	private static final int MAX_HEALTH = 1000;
	private int health = MAX_HEALTH;

	private boolean enraged = false;
	private long lastHitTime = 0;
	private static final long HIT_COOLDOWN = 200;

	// 이동
	private double baseY;
	private double verticalMoveRange = 30;
	private boolean movingRight = true;

	// 공격 빈도
	private long lastShotTime = 0;
	private long shotInterval = 3000;

	// --------------------------
	//  🧪 궁극기 (포션 폭탄)
	// --------------------------
	private boolean usingPotion = false;
	private long lastPotionAttack = 0;
	private long potionCooldown = 50000;
	private long potionDuration = 2500;
	private long potionEndTime = 0;
	private long lastPotionTick = 0;
	private long potionTickInterval = 400;

	// 화면 흔들림
	private double shakeIntensity = 8;
	private boolean shaking = false;
	private long shakeStartTime = 0;
	private long shakeDuration = 2500;

	// 스프라이트
	private Sprite spriteLeft;
	private Sprite spriteRight;
	private Sprite potionSprite;
	private Sprite bombSprite;

	// 폭발 연출
	private List<SplashEffect> activeSplashes = new ArrayList<>();

	// --------------------------
	//  🎃 생성자
	// --------------------------
	public Boss2(Game game, int x, int y) {
		super(game, x, y);
		this.game = game;
		this.baseY = y;

		loadSprites();
		spawnInitialPotionBombs();
	}

	// --------------------------
	// 🖼 스프라이트 로딩
	// --------------------------
	private void loadSprites() {
		spriteLeft = SpriteStore.get().getSprite("sprites/witchl.png");
		spriteRight = SpriteStore.get().getSprite("sprites/witchr.png");
		sprite = spriteRight;

		potionSprite = SpriteStore.get().getSprite("sprites/poisonpotion.png");
		bombSprite = SpriteStore.get().getSprite("sprites/poisionbomb.png");

		// 보스 등장 시 배경 변경
        game.setBackground("bg/wbg.jpg");
	}

	// --------------------------
	// 🌡 초반 필드에 포션 배치
	// --------------------------
	private void spawnInitialPotionBombs() {
		int count = 10;
		int minDist = 120;

		List<int[]> placed = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			int px, py, tries = 0;

			while (true) {
				px = 40 + (int)(Math.random() * (game.getWidth() - 80));
				py = 80 + (int)(Math.random() * (game.getHeight() - 160));

				if (isFarEnough(px, py, placed, minDist) || tries++ > 25) break;
			}

			placed.add(new int[]{px, py});
			game.addEntity(new PotionBomb(px, py));
		}
	}

	private boolean isFarEnough(int x, int y, List<int[]> placed, int minDist) {
		for (int[] p : placed) {
			int dx = p[0] - x;
			int dy = p[1] - y;
			if (dx * dx + dy * dy < minDist * minDist) return false;
		}
		return true;
	}

	// --------------------------
	// 🎯 이동 + 공격 로직
	// --------------------------
	@Override
	public void move(long delta) {
		updateMovement(delta);
		updateEnrage();
		updateUltimateSkill();
		updateNormalAttack();
		cleanupEffects();
	}

	private void updateMovement(long delta) {
		double oldX = x;

		// 지그재그 이동
		x += Math.sin(System.currentTimeMillis() / 600.0) * 0.6 * delta;
		y = baseY + Math.sin(System.currentTimeMillis() / 800.0) * verticalMoveRange;

		x = Math.max(60, Math.min(680, x));
		movingRight = x > oldX;
		sprite = movingRight ? spriteRight : spriteLeft;
	}

	private void updateEnrage() {
		if (!enraged && health <= 750) {
			enraged = true;
			potionCooldown = 30000;
			System.out.println("💢 마녀 분노 상태!");
		}
	}

	private void updateUltimateSkill() {
		long now = System.currentTimeMillis();

		if (!usingPotion && now - lastPotionAttack >= potionCooldown) {
			startPotionAttack();
		}

		if (usingPotion) {
			if (now - lastPotionTick >= potionTickInterval) {
				lastPotionTick = now;
				dealPotionDamage();
			}
			if (now >= potionEndTime) {
				usingPotion = false;
				shaking = false;
			}
		}
	}

	private void updateNormalAttack() {
		updateShotInterval();
		long now = System.currentTimeMillis();

		if (!usingPotion && now - lastShotTime >= shotInterval) {
			lastShotTime = now;
			fireShot();
		}
	}

	private void cleanupEffects() {
		long now = System.currentTimeMillis();
		activeSplashes.removeIf(s -> s.isExpired(now));
	}

	// --------------------------
	// 🔫 공격 빈도
	// --------------------------
	private void updateShotInterval() {
		if (health > 800) shotInterval = 3000;
		else if (health > 500) shotInterval = 2000;
		else if (health > 200) shotInterval = 1200;
		else shotInterval = 800;
	}

	// --------------------------
	// ☠ 궁극기 시작
	// --------------------------
	private void startPotionAttack() {
		usingPotion = true;
		shaking = true;
		shakeStartTime = System.currentTimeMillis();

		lastPotionAttack = System.currentTimeMillis();
		potionEndTime = lastPotionAttack + potionDuration;
		lastPotionTick = lastPotionAttack;

		System.out.println("🧪 마녀의 물약 폭탄 발동!");

		dealPotionDamage();
		spawnUltimatePotionBombs();
	}

	private void spawnUltimatePotionBombs() {
		int count = 12;
		for (int i = 0; i < count; i++) {
			int px = 40 + (int)(Math.random() * (game.getWidth() - 80));
			int py = 80 + (int)(Math.random() * (game.getHeight() - 160));
			game.addEntity(new PotionBomb(px, py));
		}
	}

	private void dealPotionDamage() {
		if (game.getShip() != null) game.getShip().takeDamage(15);
		if (game.getFortress() != null) game.getFortress().damage(8);
	}

	// --------------------------
	// 💥 피격
	// --------------------------
	@Override
	public boolean takeDamage(int damage) {
		long now = System.currentTimeMillis();
		if (now - lastHitTime < HIT_COOLDOWN) return false;

		lastHitTime = now;
		health -= damage;

		System.out.println("🧪 마녀 피격! 남은 HP: " + health);

		if (health <= 0) {
			System.out.println("💀 마녀 사망!");
			game.removeEntity(this);
			game.bossDefeated();
			return true;
		}
		return false;
	}

	@Override
	public void collidedWith(Entity other) {}

	// --------------------------
	// 🎨 그리기
	// --------------------------
	@Override
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform oldTx = g2.getTransform();

		applyShake(g2);
		drawBoss(g2);
		g2.setTransform(oldTx);

		drawSplashEffects(g2);
		drawHP(g2);
	}

	private void applyShake(Graphics2D g2) {
		if (shaking && System.currentTimeMillis() - shakeStartTime < shakeDuration) {
			int ox = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
			int oy = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
			g2.translate(ox, oy);
		}
	}

	private void drawBoss(Graphics2D g2) {
		Image img = sprite.getImage().getScaledInstance(
				(int) (sprite.getWidth() * 0.5),
				(int) (sprite.getHeight() * 0.5),
				Image.SCALE_SMOOTH
		);
		g2.drawImage(img, (int)x - 40, (int)y - 40, null);
	}

	private void drawSplashEffects(Graphics2D g2) {
		long now = System.currentTimeMillis();

		for (SplashEffect s : activeSplashes) {
			double progress = Math.min(1.0, (now - s.startTime) / (double)s.duration);
			int r = (int)(s.maxRadius * progress);

			g2.setColor(new Color(255, 105, 180, 255));
			g2.fillOval(s.x - r, s.y - r, r * 2, r * 2);
		}
	}

	private void drawHP(Graphics2D g2) {
		g2.setColor(Color.red);
		g2.fillRect((int)x - 50, (int)y - 70, 100, 6);

		g2.setColor(Color.green);
		int hpWidth = (int)(100 * (health / (double)MAX_HEALTH));
		g2.fillRect((int)x - 50, (int)y - 70, hpWidth, 6);

		g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
		g2.setColor(Color.white);
		g2.drawString(health + " / " + MAX_HEALTH, (int)x - 25, (int)y - 80);
	}

	// --------------------------
	// 💧 포션 폭탄 엔티티 (내부 클래스)
	// --------------------------
	private class PotionBomb extends Entity {
		private final long spawnTime;
		private final long explodeDelay;
		private boolean switched = false;

		private final int origX, origY;
		private final int shakeAmp = 40;
		private final double shakeSpeed = 8.0;

		public PotionBomb(int px, int py) {
			super("sprites/poisonpotion.png", px, py);

			this.origX = px;
			this.origY = py;

			this.spawnTime = System.currentTimeMillis();
			this.dx = 0;
			this.dy = 0;

			this.explodeDelay = 1000 + (int)(Math.random() * 2000);
		}

		@Override
		public void move(long delta) {
			long elapsed = System.currentTimeMillis() - spawnTime;

			if (!switched && elapsed < explodeDelay) {
				updateShaking(elapsed);
				return;
			}

			if (!switched) switchToBomb();

			if (elapsed >= 3000) game.removeEntity(this);
		}

		private void updateShaking(long elapsed) {
			double t = elapsed / 1000.0;
			x = origX + Math.sin(t * Math.PI * shakeSpeed) * shakeAmp;
			y = origY;
		}

		private void switchToBomb() {
			sprite = bombSprite;
			switched = true;

			activeSplashes.add(
					new SplashEffect(
							(int)x + getWidth()/2,
							(int)y + getHeight()/2,
							System.currentTimeMillis()
					)
			);

			FortressEntity fort = game.getFortress();
			if (fort != null && this.collidesWith(fort)) {
				fort.damage(50);
			}
		}

		@Override
		public void collidedWith(Entity other) {}
	}

	// --------------------------
	// 🌫 폭발 시야 방해 효과
	// --------------------------
	private class SplashEffect {
		int x, y;
		long startTime;
		double maxRadius = 60;
		long duration = 2000;

		public SplashEffect(int x, int y, long startTime) {
			this.x = x;
			this.y = y;
			this.startTime = startTime;
		}

		boolean isExpired(long now) {
			return now > startTime + duration;
		}
	}
}
