package org.newdawn.spaceinvaders.entity.Boss;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.EnemyShotEntity;
import org.newdawn.spaceinvaders.entity.MonsterEntity;

/**
 * Stage 2 Boss: 마녀
 * - 물약 폭탄 궁극기 + 화면 흔들림 + 폭발 연출
 */
public class Boss2 extends BossEntity {
	// --------------------------
	// 🧪 궁극기 (포션 폭탄) - Boss2 고유 필드
	// --------------------------
	private boolean usingPotion = false;
	private long lastPotionAttack = 0;
	private long potionCooldown = 50000; // 초기값
	private long potionDuration = 2500;
	private long potionEndTime = 0;
	private long lastPotionTick = 0;
	private long potionTickInterval = 400;

	// 스프라이트
	private Sprite spriteLeft;
	private Sprite spriteRight;
	private Sprite bombSprite;

	// 폭발 연출
	private final List<SplashEffect> activeSplashes = new ArrayList<>();

	// --------------------------
	// 🎃 생성자
	// --------------------------
	public Boss2(Game game, int x, int y) {
		super(game, "sprites/witchr.png", x, y);
		// game, baseY, health 등은 BossEntity에서 초기화됨

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

		SpriteStore.get().getSprite("sprites/poisonpotion.png");
		bombSprite = SpriteStore.get().getSprite("sprites/poisionbomb.png");

		// 보스 등장 시 배경 변경
        game.setBackground("bg/wbg.jpg");
	}

	// --------------------------
	// 🌡 초반 필드에 포션 배치 (Boss2 고유 기능)
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
	// 🎯 이동 로직 오버라이드 (스프라이트 방향 결정)
	// --------------------------
	@Override
	protected void updateMovement(long delta) {
		super.updateMovement(delta);
		// 부모 클래스에서 계산된 movingRight에 따라 스프라이트 변경
		sprite = movingRight ? spriteRight : spriteLeft;
	}

	// --------------------------
	// 💢 분노 로직 오버라이드
	// --------------------------
	@Override
	protected void updateEnrage() {
		super.updateEnrage(); // 부모의 분노 상태 체크 (체력 750 이하)
		if (enraged) { // 부모 클래스의 enraged 필드 사용
			potionCooldown = 30000;
			// 이 메시지는 BossEntity에서 출력되므로 제거하거나, 고유한 메시지로 대체 가능
			// System.out.println("💢 마녀 분노 상태!"); 
		}
	}

	// --------------------------
	// 🔫 공격 빈도 로직 오버라이드 (Boss2 고유의 값 사용)
	// --------------------------
	@Override
	protected void updateShotInterval() {
		// Boss2는 800/500/200 체력 기준을 사용함 (BossEntity는 700/400/200)
		if (health > 800) shotInterval = 3000;
		else if (health > 500) shotInterval = 2000;
		else if (health > 200) shotInterval = 1200;
		else shotInterval = 800;
	}

	// --------------------------
	// 🧪 궁극기 로직 구현 (BossEntity의 추상 메서드 오버라이드)
	// --------------------------
	@Override
	protected void updateSpecialAttack() {
		long now = System.currentTimeMillis();

		// 궁극기 발동 체크
		if (!usingPotion && now - lastPotionAttack >= potionCooldown) {
			startPotionAttack();
		}

		// 궁극기 지속 처리
		if (usingPotion) {
			if (now - lastPotionTick >= potionTickInterval) {
				lastPotionTick = now;
				dealPotionDamage();
			}
			// 지속 시간 종료 체크
			if (now >= potionEndTime) {
				usingPotion = false;
				shaking = false; // shaking은 부모 필드
			}
		}

		// 폭발 효과 정리
		cleanupEffects();
	}

	private void cleanupEffects() {
		long now = System.currentTimeMillis();
		activeSplashes.removeIf(s -> s.isExpired(now));
	}

	// --------------------------
	// ☠ 궁극기 시작
	// --------------------------
	private void startPotionAttack() {
		usingPotion = true;
		shaking = true; // shaking은 부모 필드
		shakeStartTime = System.currentTimeMillis(); // shakeStartTime도 부모 필드

		lastPotionAttack = System.currentTimeMillis();
		potionEndTime = lastPotionAttack + potionDuration;
		lastPotionTick = lastPotionAttack;

		System.out.println("🧪 마녀의 물약 폭탄 발동!");

		dealPotionDamage(); // 즉시 피해 1회
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
	// 💥 피격 (BossEntity.takeDamage()를 호출하며 고유 메시지만 추가)
	// --------------------------
	@Override
	public void takeDamage(int damage) {
		super.takeDamage(damage);
		if (health > 0) { // 부모에서 health 체크
			System.out.println("🧪 마녀 피격! 남은 HP: " + health);
		}
	}

	@Override
	public void collidedWith(Entity other) {
		// 충돌 방지 대상 체크
		if (other instanceof EnemyShotEntity || other instanceof MonsterEntity) return;

		// 아이템 데미지 적용은 부모의 collidedWithItem에서 처리
		super.collidedWith(other);
	}

	// --------------------------
	// 🎨 그리기 (BossEntity의 추상 메서드 오버라이드)
	// --------------------------
	@Override
	protected void drawSpecialEffect(Graphics2D g2) {
		long now = System.currentTimeMillis();

		for (SplashEffect s : activeSplashes) {
			double progress = Math.min(1.0, (now - s.startTime) / (double)s.duration);
			int r = (int)(s.maxRadius * progress);

			// 분홍색 폭발 효과
			g2.setColor(new Color(255, 105, 180, 255));
			g2.fillOval(s.x - r, s.y - r, r * 2, r * 2);
		}
	}
	// BossEntity의 draw()가 drawSpecialEffect를 호출하며, applyShakeEffect/drawBossBody/drawHpBar를 처리함.

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

			Entity fort = game.getFortress();
			if (fort != null && this.collidesWith(fort)) {
				// FortressEntity를 직접 참조하지 않도록 Entity 타입으로 변경
				// 단, damage() 메서드 사용을 위해 실제 타입이 FortressEntity임을 가정함
				// 안전을 위해 if (fort instanceof FortressEntity) {...} 가 필요할 수 있으나,
				// 기존 코드의 기능을 유지하기 위해 그대로 둠.
				try {
					((org.newdawn.spaceinvaders.entity.FortressEntity) fort).damage(50);
				} catch (ClassCastException e) {
					// 무시
				}
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