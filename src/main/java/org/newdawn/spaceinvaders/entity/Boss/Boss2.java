package org.newdawn.spaceinvaders.entity.Boss;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.FortressEntity; // FortressEntity 다시 추가
import org.newdawn.spaceinvaders.entity.EnemyShotEntity;
import org.newdawn.spaceinvaders.entity.MonsterEntity;

/**
 * Stage 2 Boss: 마녀
 * - 물약 폭탄 + 지그재그로 오는 공격 + HP 숫자
 * - 체력이 줄수록 공격 속도 증가
 * - 한글 폰트 정상 출력
 */
public class Boss2 extends MonsterEntity {
	private final Game game;
	private final int MAX_HEALTH = 5;
	private int health = MAX_HEALTH; // 보스 체력을 5로 설정
	private boolean enraged = false;

	// 궁극기 관련 (물약 폭탄 패턴)
	private long lastPotionAttack = 0;
	private long potionCooldown = 50000; // 50초마다 물약 패턴 발생
	private boolean usingPotion = false;
	private long potionDuration = 2500;
	private long potionEndTime = 0;

	private long lastPotionTick = 0;
	private long potionTickInterval = 400;

	// 이동 관련
	private double baseY;
	private double verticalMoveRange = 30;
	private boolean movingRight = true;

	// 화면 흔들림
	private double shakeIntensity = 8;
	private boolean shaking = false;
	private long shakeStartTime = 0;
	private long shakeDuration = 2500;

	// 🔥 시각 효과에 사용될 스프라이트
	private Sprite potionEffectSprite; // 물약 이미지 (스플래시 내부에서 사용)
	private Sprite bombEffectSprite; // 폭탄 이미지 (스플래시 내부에서 사용)
	private Sprite spriteLeft;
	private Sprite spriteRight;
	
	// ⚠️ 국소 시야 방해 효과를 추적하는 리스트 (PotionBomb이 생성)
	private List<SplashEffect> activeSplashes = new ArrayList<>(); 

	private long lastHitTime = 0;
	private static final long HIT_COOLDOWN = 200;

	// 공격 빈도 제어용
	private long lastShotTime = 0;
	private long shotInterval = 3000; // 기본 3초 간격

	public Boss2(Game game, int x, int y) {
		super(game, x, y);
		this.game = game;
		this.baseY = y;

		spriteLeft = SpriteStore.get().getSprite("sprites/witchl.png");
		spriteRight = SpriteStore.get().getSprite("sprites/witchr.png");
		sprite = spriteRight;

		// 시각 효과용 스프라이트 로드
		potionEffectSprite = SpriteStore.get().getSprite("sprites/poisonpotion.png");
		bombEffectSprite = SpriteStore.get().getSprite("sprites/poisionbomb.png");

		// 보스 등장 후 5초 이내에 랜덤한 시점에 물약들이 등장하도록 예약
		int count = 10; // 한 번에 등장하는 물약 수
		int minDist = 120; // 서로 겹치지 않게 배치
		java.util.List<int[]> placed = new java.util.ArrayList<>();
		for (int i = 0; i < count; i++) {
			int tries = 0;
			int px, py;
			while (true) {
				px = 40 + (int) (Math.random() * (game.getWidth() - 80));
				py = 80 + (int) (Math.random() * (game.getHeight() - 160));
				boolean ok = true;
				for (int[] p : placed) {
					int dx = p[0] - px;
					int dy = p[1] - py;
					if (dx * dx + dy * dy < minDist * minDist) { ok = false; break; }
				}
				tries++;
				if (ok || tries > 25) break;
			}
			placed.add(new int[] { px, py });
			// 즉시 PotionBomb 엔티티를 생성하되, PotionBomb 내부에서 1~3초 사이에 폭발하도록 랜덤화함
			game.addEntity(new PotionBomb(px, py));
		}
	}
	

	@Override
	public void move(long delta) {
		double oldX = x;
		x += Math.sin(System.currentTimeMillis() / 600.0) * 0.6 * delta; // 지그재그 이동
		y = baseY + Math.sin(System.currentTimeMillis() / 800.0) * verticalMoveRange;

		if (x < 60) x = 60;
		if (x > 680) x = 680;

		movingRight = x > oldX;
		sprite = movingRight ? spriteRight : spriteLeft;

		// 💢 체력 750 이하 시 분노 모드
		if (!enraged && health <= 750) {
			enraged = true;
			potionCooldown = 30000; // 분노 시 30초로 단축
			System.out.println("💢 마녀 분노 상태!");
		}

		long now = System.currentTimeMillis();

		// ☠️ 궁극기 발동
		if (!usingPotion && now - lastPotionAttack >= potionCooldown) {
			startPotionAttack();
		}

		// ☠️ 궁극기 지속
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

		// 🔫 일반 공격 (HP에 따라 빈도 가변)
		updateShotInterval();
		if (!usingPotion && now - lastShotTime >= shotInterval) {
			lastShotTime = now;
			fireShot(); // MonsterEntity 제공
		}
		
		// 💥 국소 효과 정리: 만료된 스플래시 제거
		activeSplashes.removeIf(s -> s.isExpired(now));
	}

	private void updateShotInterval() {
		// 체력 줄수록 공격 빈도 증가 (1000 기준)
		if (health > 800) shotInterval = 3000;
		else if (health > 500) shotInterval = 2000;
		else if (health > 200) shotInterval = 1200;
		else shotInterval = 800;
	}

	private void startPotionAttack() {
		usingPotion = true;
		shaking = true; // 폭탄 발동 시 화면 흔들림 유지
		shakeStartTime = System.currentTimeMillis();

		long now = System.currentTimeMillis();
		lastPotionAttack = now;
		potionEndTime = lastPotionAttack + potionDuration;
		lastPotionTick = lastPotionAttack;

		System.out.println("🧪 마녀의 물약 폭탄 발동!");

		// 기존의 즉시 데미지는 유지 (첫 틱)
		dealPotionDamage();

		// 보스가 궁극기를 시작할 때, 즉시 PotionBomb들을 생성한다.
		// 각 PotionBomb은 내부에서 1~3초 사이에 폭발하도록 랜덤화되어 있다.
		int count = 12;
		for (int i = 0; i < count; i++) {
			int px = 40 + (int) (Math.random() * (game.getWidth() - 80));
			int py = 80 + (int) (Math.random() * (game.getHeight() - 160));
			game.addEntity(new PotionBomb(px, py));
		}
	}

	private void dealPotionDamage() {
		if (game.getShip() != null) {
			game.getShip().takeDamage(15);
		}
		if (game.getFortress() != null) {
			game.getFortress().damage(8);
		}
	}

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
	public void collidedWith(Entity other) {
		if (other instanceof EnemyShotEntity || other instanceof MonsterEntity) return;
	}

	@Override
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		// 🎯 현재 transform 저장
		AffineTransform oldTransform = g2.getTransform();

		// 🔥 흔들림 효과 (보스만)
		if (shaking) {
			double elapsed = System.currentTimeMillis() - shakeStartTime;
			if (elapsed < shakeDuration) {
				int offsetX = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
				int offsetY = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
				g2.translate(offsetX, offsetY);
			}
		}

		// 👻 보스 본체
		Image img = sprite.getImage().getScaledInstance(
				(int)(sprite.getWidth() * 0.5),
				(int)(sprite.getHeight() * 0.5),
				Image.SCALE_SMOOTH
		);
		g2.drawImage(img, (int)x - 40, (int)y - 40, null);

		// 🔄 transform 원복
		g2.setTransform(oldTransform);
		
		long now = System.currentTimeMillis();

		// 💥 국소 시야 방해 효과 (동그란 핑크 오버레이) - PotionBomb이 터진 자리에서 발생
		for (SplashEffect splash : activeSplashes) {
			long elapsed = now - splash.startTime;
			double progress = Math.max(0.0, Math.min(1.0, (double) elapsed / splash.duration));

			// 1. 반경 계산: 선형적으로 커지도록 (최대 splash.maxRadius)
			double currentRadius = splash.maxRadius * progress;

			// 2. 핑크 오버레이는 투명도 없이 불투명하게 그린다
			int alpha = 255;
			g2.setColor(new Color(255, 105, 180, alpha));

			// 동그라미 그리기 (시야를 완전히 가리는 핑크 오버레이)
			int r = (int) currentRadius;
			g2.fillOval(splash.x - r, splash.y - r, 2 * r, 2 * r);

			// 3. 내부 연출 스프라이트: 
            // ⚠️ [요청 사항 반영] 핑크색 오버레이가 불투명하므로 이 부분은 삭제하여 물약/폭탄 이미지가 가려지도록 함.
            // 필요시 나중에 투명도(alpha)를 조절하여 물약 이미지가 비치도록 구현할 수 있습니다.
            /*
			Sprite currentEffectSprite = elapsed > 1000 ? bombEffectSprite : potionEffectSprite;
			int lw = currentEffectSprite.getWidth() / 2;
			int lh = currentEffectSprite.getHeight() / 2;
			g2.drawImage(currentEffectSprite.getImage(), splash.x - lw/2, splash.y - lh/2, lw, lh, null);
            */
		}

		// ❤️ HP바
		g2.setColor(Color.red);
		g2.fillRect((int)x - 50, (int)y - 70, 100, 6);
		g2.setColor(Color.green);
	int hpWidth = (int)(100 * (health / (double)MAX_HEALTH));
	g2.fillRect((int)x - 50, (int)y - 70, Math.max(0, Math.min(100, hpWidth)), 6);

		// 🧠 한글 폰트 정상 표시
		g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
		g2.setColor(Color.white);
	g2.drawString(health + " / " + MAX_HEALTH, (int)x - 25, (int)y - 80);
	}

	/** Boss2 전용: 화면에 생성되어 1초 후에 poisionbomb으로 변하는 물약 폭탄 */
	private class PotionBomb extends Entity {
		private final long spawnTime;
		private boolean switched = false;

		private final long explodeDelay; // 1~3초 사이 랜덤으로 폭발 타이밍

		private final int origX;
		private final int origY;
		private final int shakeAmp = 40; // ⚠️ [요청 사항 반영] 좌우 흔들림 폭을 40으로 증가
		private final double shakeSpeed = 8.0; // 흔들림 속도 증가

		public PotionBomb(int px, int intpy) {
			super("sprites/poisonpotion.png", px, intpy);
			this.origX = px;
			this.origY = intpy;
			this.spawnTime = System.currentTimeMillis();
			// 초기에는 정지하여 좌우로 흔들리도록 함
			this.dx = 0;
			this.dy = 0;
			// 폭발 타이밍을 1000ms ~ 3000ms 사이로 랜덤화
			this.explodeDelay = 1000 + (int)(Math.random() * 2000);
		}

		@Override
		public void move(long delta) {
			long elapsed = System.currentTimeMillis() - spawnTime;

			// during first explodeDelay: shake left-right around origX
			if (!switched && elapsed < explodeDelay) {
				double t = (double) elapsed / 1000.0; // 초 단위로 변환하여 흔들림 속도 제어
				// ⚠️ [요청 사항 반영] Math.PI * 6 대신 shakeSpeed를 사용하여 더 빠른 흔들림
				x = origX + Math.sin(t * Math.PI * shakeSpeed) * shakeAmp;
				y = origY;
				return;
			}

			// switch to bomb after explodeDelay
			if (!switched && elapsed >= explodeDelay) {
				this.sprite = SpriteStore.get().getSprite("sprites/poisionbomb.png");
				switched = true;
				// create splash/cloud effect centered at this entity
				activeSplashes.add(new SplashEffect((int)x + getWidth()/2, (int)y + getHeight()/2, System.currentTimeMillis()));
				// fortress damage if overlapping
				FortressEntity fort = game.getFortress();
				if (fort != null && this.collidesWith(fort)) {
					fort.damage(50);
				}
			}

			// after some time remove
			if (elapsed >= 3000) {
				game.removeEntity(this);
			}
		}

		@Override
		public void collidedWith(Entity other) {
			// 연출용이므로 충돌 시 특별 동작 없음
		}
        
        // draw 메소드를 오버라이드하여 Bomb으로 전환된 후에는 draw하지 않도록 하거나,
        // Entity의 기본 draw를 사용하되, Bomb으로 전환된 후 일정 시간 뒤에 제거되도록 설정해야 함.
        // 현재 로직은 Bomb으로 전환 후 3초 뒤에 제거되므로, 별도의 draw 오버라이드는 필요 없습니다.
	}

	/** 국소적인 시야 방해 효과를 위한 내부 클래스 */
	private class SplashEffect {
		int x, y;
		long startTime;
		double maxRadius = 60; // 핑크 오버레이 반경 더 축소
		long duration = 2000; // 지속시간을 더 줄임

		public SplashEffect(int x, int y, long startTime) {
			this.x = x;
			this.y = y;
			this.startTime = startTime;
		}

		public boolean isExpired(long now) {
			return now > startTime + duration;
		}
	}
}