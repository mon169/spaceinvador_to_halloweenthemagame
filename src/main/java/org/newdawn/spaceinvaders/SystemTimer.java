package org.newdawn.spaceinvaders;

public final class SystemTimer {
	/** 프로그램이 시작된 기준 시점(나노초) */
	private static final long START_NANOS = System.nanoTime();
	/** 1초 = 10억 나노초 (시간 계산용) */
	@SuppressWarnings("unused")
	private static final long TIMER_TICKS_PER_SECOND = 1_000_000_000L;

	private SystemTimer() { }

	/**
	 * 프로그램이 시작된 이후의 경과 시간을 밀리초 단위로 반환
	 */
	public static long getTime() {
		return (System.nanoTime() - START_NANOS) / 1_000_000L;
	}

	/**
	 * 지정한 시간(ms) 동안 잠시 멈춤
	 */
	public static void sleep(long duration) {
		if (duration <= 0) return;

		final long end = System.nanoTime() + duration * 1_000_000L;
		while (true) {
			long remain = end - System.nanoTime();
			if (remain <= 0) break;

			long ms = remain / 1_000_000L;
			int ns = (int) (remain - ms * 1_000_000L);
			try {
				Thread.sleep(ms, ns);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}
}
