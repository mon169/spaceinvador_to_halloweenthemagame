package org.newdawn.spaceinvaders.sound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SoundEffect {
    private Clip clip;                 // 기본 속도용
    private Clip variantClip;          // 가속/감속용(필요할 때 새로 연다)
    private final String resourcePath; // 클래스패스 리소스 경로(재열기용)

    public SoundEffect(String resourcePath) {
        this.resourcePath = resourcePath;
        try {
            clip = AudioSystem.getClip();
            AudioInputStream in = loadAudioStream(resourcePath);
            AudioInputStream pcm = toPcmSigned(in);
            clip.open(pcm);
        } catch (Exception e) {
            System.err.println("⚠️ 사운드 로드 실패: " + resourcePath);
            e.printStackTrace();
        }
    }

    /**
     * 클래스패스 리소스 또는 파일 경로에서 오디오 스트림 로드
     */
    private AudioInputStream loadAudioStream(String path) throws Exception {
        // 클래스패스 리소스로 먼저 시도 (/sounds/...)
        InputStream is = getClass().getResourceAsStream(path);
        if (is != null) {
            return AudioSystem.getAudioInputStream(is);
        }
        // 파일 경로로 폴백 (개발 환경용)
        File file = new File(path);
        if (file.exists()) {
            return AudioSystem.getAudioInputStream(file);
        }
        // src/main/resources 경로로도 시도
        file = new File("src/main/resources" + path);
        if (file.exists()) {
            return AudioSystem.getAudioInputStream(file);
        }
        throw new IOException("사운드 파일을 찾을 수 없습니다: " + path);
    }

    // 기본 API 그대로 유지
    public void play() {
        if (clip == null) return;
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }
    public void loop() {
        if (clip == null) return;
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }
    public void stop() {
        if (clip != null) { clip.stop(); }
        if (variantClip != null) { variantClip.stop(); }
    }

    public long getMicrosecondPosition() {
        return (variantClip != null && variantClip.isRunning())
                ? variantClip.getMicrosecondPosition()
                : (clip != null ? clip.getMicrosecondPosition() : 0);
    }
    public void setMicrosecondPosition(long us) {
        if (variantClip != null && variantClip.isOpen()) {
            variantClip.setMicrosecondPosition(Math.max(0, us));
        } else if (clip != null && clip.isOpen()) {
            clip.setMicrosecondPosition(Math.max(0, us));
        }
    }

    /** ★ 배속으로 무한 루프 (예: rate=2.0이면 2배속) */
    public void loopAtRate(double rate) {
        if (rate <= 0) rate = 1.0;
        try {
            // 기존 변형 클립 정리
            if (variantClip != null) {
                variantClip.stop();
                variantClip.close();
                variantClip = null;
            }
            // 원본 스트림 다시 열기
            AudioInputStream in = loadAudioStream(resourcePath);
            AudioInputStream pcm = toPcmSigned(in);

            AudioFormat src = pcm.getFormat();
            // 샘플레이트/프레임레이트만 rate배로 키운 타깃 포맷
            AudioFormat target = new AudioFormat(
                    src.getEncoding(),                     // PCM_SIGNED
                    src.getSampleRate() * (float) rate,    // ★ 샘플레이트 변경
                    src.getSampleSizeInBits(),
                    src.getChannels(),
                    src.getFrameSize(),
                    src.getFrameRate() * (float) rate,     // (보통 = sampleRate)
                    src.isBigEndian()
            );

            AudioInputStream converted = AudioSystem.getAudioInputStream(target, pcm);

            variantClip = AudioSystem.getClip();
            variantClip.open(converted);
            variantClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            // 변환 프로바이더가 없는 환경이면 실패할 수 있음 → 폴백: 그냥 기본 loop
            e.printStackTrace();
            loop();
        }
    }

    /** 배속 루프 종료하고 기본 속도로 복귀 */
    public void backToNormalLoop() {
        try {
            if (variantClip != null) {
                variantClip.stop();
                variantClip.close();
                variantClip = null;
            }
            loop();
        } catch (Exception e) {
            e.printStackTrace();
            loop();
        }
    }

    // ===== 내부 유틸: 어떤 WAV라도 PCM_SIGNED로 보정 =====
    private static AudioInputStream toPcmSigned(AudioInputStream source) throws Exception {
        AudioFormat base = source.getFormat();
        if (base.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
            return source;
        }
        AudioFormat pcm = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                base.getSampleRate(),
                16,                                  // 16-bit로 표준화
                base.getChannels(),
                base.getChannels() * 2,              // frameSize = ch * 2 bytes
                base.getSampleRate(),
                false
        );
        return AudioSystem.getAudioInputStream(pcm, source);
    }

    public void playOnce() {
        if (clip == null) return;
        if (clip.isRunning()) clip.stop(); // 이미 재생 중이면 중단
        clip.setFramePosition(0);          // 처음부터 재생
        clip.start();                      // 한 번만 재생 (loop 아님)
    }

    //점진적으로 배속 변화 (1.0 → 1.5)
    private volatile double currentRate = 1.0;
    private Thread rampThread;

    /**
     * targetRate: 목표 배속 (예: 1.5)
     * durationMs: 총 소요시간 (예: 1200ms)
     * stepMs: 한 단계마다의 간격 (예: 100~200ms)
     */
    public synchronized void rampToRate(double targetRate, long durationMs, long stepMs) {
        if (rampThread != null && rampThread.isAlive()) {
            rampThread.interrupt();
        }
        final double startRate = currentRate;
        final int steps = (int) Math.max(1, durationMs / Math.max(1, stepMs));

        rampThread = new Thread(() -> {
            double prevRate = startRate;
            for (int i = 1; i <= steps; i++) {
                if (Thread.currentThread().isInterrupted()) return;

                double r = startRate + (targetRate - startRate) * (i / (double) steps);
                long pos = getMicrosecondPosition();

                // 안전하게 synchronized 블록으로 묶기
                synchronized (SoundEffect.this) {
                    stop();
                    loopAtRate(r);
                    long adj = (long) (pos * (prevRate / r));
                    setMicrosecondPosition(Math.max(0L, adj));
                    currentRate = r;
                    prevRate = r;
                }

                try {
                    Thread.sleep(Math.max(1, stepMs));
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        rampThread.setDaemon(true);
        rampThread.start();
    }
}