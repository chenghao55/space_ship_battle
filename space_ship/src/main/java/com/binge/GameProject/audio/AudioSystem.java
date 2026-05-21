package com.binge.GameProject.audio;

import com.binge.GameProject.model.Enemy;
import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.IdolGroup;
import com.binge.GameProject.model.Player;
import com.binge.GameProject.physics.Vector2D;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.List;

public class AudioSystem {
    private static AudioSystem instance;

    private final MusicMixer mixer = new MusicMixer();
    private final IdolVoiceController voiceController = new IdolVoiceController();
    private String lastSfx = "";

    // AudioClip 實例 (適用一次性短音效)
    private AudioClip laserSfx;
    private AudioClip buttonPressSfx;
    private AudioClip explosionSfx;
    private AudioClip hitSfx;
    private AudioClip switchSfx;
    private AudioClip endingImpactSfx;
    private AudioClip freezeGlitchSfx;

    // MediaPlayer 實例 (適用長背景音與環境音)
    private MediaPlayer menuMusicPlayer;
    private MediaPlayer enemyAmbientPlayer;
    private MediaPlayer enginePlayer;
    private MediaPlayer scifiHumPlayer;

    private double silenceTimer = 0.0;

    public AudioSystem() {
        instance = this;
        loadAudioResources();
    }

    public static AudioSystem getInstance() {
        return instance;
    }

    private void loadAudioResources() {
        try {
            // 載入 AudioClip
            laserSfx = new AudioClip(getClass().getResource("/音效/BlasterSound.wav").toExternalForm());
            buttonPressSfx = new AudioClip(getClass().getResource("/音效/buttonPress.mp3").toExternalForm());
            explosionSfx = new AudioClip(getClass().getResource("/音效/explosion.mp3").toExternalForm());
            hitSfx = new AudioClip(getClass().getResource("/音效/hit.mp3").toExternalForm());
            switchSfx = new AudioClip(getClass().getResource("/音效/switchSound.mp3").toExternalForm());
            endingImpactSfx = safeLoadClip("/music/sfx_end_impact.mp3");
            freezeGlitchSfx = safeLoadClip("/music/sfx_freeze_glitch.mp3");

            // 載入 MediaPlayer
            menuMusicPlayer = createLoopingPlayer("/音效/background_scifi.mp3", 0.7);
            enemyAmbientPlayer = createLoopingPlayer("/音效/enermySound.mp3", 0.0);
            enginePlayer = createLoopingPlayer("/音效/engineSound.mp3", 0.0);
            scifiHumPlayer = createLoopingPlayer("/音效/scifi_hum.mp3", 0.5);
        } catch (Exception e) {
            System.err.println("Error loading audio resources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private AudioClip safeLoadClip(String resourcePath) {
        var url = getClass().getResource(resourcePath);
        return url == null ? null : new AudioClip(url.toExternalForm());
    }

    private MediaPlayer createLoopingPlayer(String resourcePath, double defaultVol) {
        var url = AudioSystem.class.getResource(resourcePath);
        if (url == null) return null;
        MediaPlayer player = new MediaPlayer(new Media(url.toExternalForm()));
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setVolume(defaultVol);
        return player;
    }

    public void updateAmbient(double nearestIdolVolume) {
        mixer.fadeAmbient(1.0 - Math.max(0, Math.min(1, nearestIdolVolume)));
    }

    public void updateIdolGroupVoices(List<IdolGroup> groups, Player player, double dt) {
        double loudest = 0.0;
        for (IdolGroup group : groups) {
            Idol nearest = group.getNearestDetectableIdol(player);
            double volume = voiceController.calculateVolume(nearest, player);
            
            // 計算左右聲道平衡度 (Stereo Pan Balance)
            double balance = 0.0;
            if (nearest != null && player != null) {
                Vector2D diff = nearest.getPosition().subtract(player.getPosition());
                double dist = diff.magnitude();
                if (dist > 0.1) {
                    Vector2D dir = diff.normalize();
                    double rad = Math.toRadians(player.getRotationAngle());
                    Vector2D right = new Vector2D(Math.cos(rad), -Math.sin(rad));
                    balance = dir.dot(right);
                }
            }
            
            mixer.setGroupSongVolume(group.getGroupId(), group.getSongPath(), volume, balance);
            loudest = Math.max(loudest, volume);
        }
        updateAmbient(loudest);
        updateSilenceTimer(loudest, dt);
    }

    // 播放具備距離音量衰減與方向 Balance 的 3D 立體音效
    public void playSpatialClip(AudioClip clip, Vector2D emitterPos, Player player, double maxVolume) {
        if (clip == null || player == null || emitterPos == null) return;
        
        Vector2D diff = emitterPos.subtract(player.getPosition());
        double dist = diff.magnitude();
        
        // 3D 音效距離衰減 (超過 9000.0 就聽不見)
        double senseRange = 9000.0;
        double volume = Math.max(0, 1.0 - dist / senseRange) * maxVolume;
        
        if (volume > 0.01) {
            double balance = 0.0;
            if (dist > 0.1) {
                Vector2D dir = diff.normalize();
                double rad = Math.toRadians(player.getRotationAngle());
                Vector2D right = new Vector2D(Math.cos(rad), -Math.sin(rad));
                balance = dir.dot(right);
            }
            // 播放 3D 立體環繞音效 (JavaFX AudioClip: play(volume, balance, rate, pan, priority))
            clip.play(volume, balance, 1.0, balance, 0);
        }
    }

    // 處理敵人接近感應音效 (3D 立體環繞，小聲)
    public void updateEnemyAmbient(List<Enemy> enemies, Player player) {
        if (enemyAmbientPlayer == null || player == null) return;

        Enemy nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                double dist = enemy.getPosition().distance(player.getPosition());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = enemy;
                }
            }
        }

        double senseRange = 3000.0;
        if (nearest != null && minDist <= senseRange) {
            // 音量衰減 (最大音量 0.25，低沉警示音)
            double volume = Math.max(0, 1.0 - minDist / senseRange) * 0.25;

            // 計算左右聲道平衡
            Vector2D diff = nearest.getPosition().subtract(player.getPosition());
            double balance = 0.0;
            double dist = diff.magnitude();
            if (dist > 0.1) {
                Vector2D dir = diff.normalize();
                double rad = Math.toRadians(player.getRotationAngle());
                Vector2D right = new Vector2D(Math.cos(rad), -Math.sin(rad));
                balance = dir.dot(right);
            }

            enemyAmbientPlayer.setVolume(volume);
            enemyAmbientPlayer.setBalance(balance);

            if (volume > 0.01 && enemyAmbientPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                enemyAmbientPlayer.play();
            }
        } else {
            // 漸漸淡出
            double currentVol = enemyAmbientPlayer.getVolume();
            if (currentVol > 0.01) {
                enemyAmbientPlayer.setVolume(currentVol * 0.85);
            } else {
                enemyAmbientPlayer.setVolume(0);
                if (enemyAmbientPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    enemyAmbientPlayer.pause();
                }
            }
        }
    }

    // 處理加速與 Boost 引擎聲音
    public void updateEngineSound(boolean isAccelerating, boolean isBoosting) {
        if (enginePlayer == null) return;

        if (isAccelerating || isBoosting) {
            double targetVol = isBoosting ? 0.7 : 0.4;
            double curVol = enginePlayer.getVolume();
            enginePlayer.setVolume(curVol + (targetVol - curVol) * 0.15);

            if (enginePlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                enginePlayer.play();
            }
        } else {
            double curVol = enginePlayer.getVolume();
            if (curVol > 0.05) {
                enginePlayer.setVolume(curVol * 0.8);
            } else {
                enginePlayer.setVolume(0);
                if (enginePlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    enginePlayer.pause();
                }
            }
        }
    }

    // 處理 60 秒太空無聲環境音
    private void updateSilenceTimer(double loudestVolume, double dt) {
        if (scifiHumPlayer == null) return;

        if (loudestVolume <= 0.01) {
            silenceTimer += dt;
            if (silenceTimer > 60.0) {
                if (scifiHumPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                    scifiHumPlayer.play();
                }
            }
        } else {
            silenceTimer = 0.0;
            if (scifiHumPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                scifiHumPlayer.stop();
            }
        }
    }

    // 播放選單背景音樂
    public void playMenuMusic() {
        if (menuMusicPlayer != null && menuMusicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            menuMusicPlayer.play();
        }
    }

    // 停止選單背景音樂
    public void stopMenuMusic() {
        if (menuMusicPlayer != null) {
            menuMusicPlayer.stop();
        }
    }

    // 按鈕與一般音效播放方法
    public void playButtonPress() {
        if (buttonPressSfx != null) buttonPressSfx.play();
    }

    public void playSwitch() {
        if (switchSfx != null) switchSfx.play();
    }

    public void playExplosion() {
        if (explosionSfx != null) explosionSfx.play();
    }

    public void playHit() {
        if (hitSfx != null) hitSfx.play();
    }

    public void playEndingImpact() {
        if (endingImpactSfx != null) endingImpactSfx.play(0.9);
    }

    public void playResultTick() {
        if (freezeGlitchSfx != null) freezeGlitchSfx.play(0.18);
    }

    public void playRatingReveal(String rating) {
        double volume = switch (rating) {
            case "S" -> 1.0;
            case "A" -> 0.82;
            case "B" -> 0.62;
            case "C" -> 0.46;
            default -> 0.32;
        };
        if (endingImpactSfx != null) endingImpactSfx.play(volume);
    }

    public void playSfx(String id) {
        lastSfx = id;
    }

    public void playLostVoice(Idol idol) {
        lastSfx = idol == null ? "lost-voice" : "lost-voice-" + idol.getIdolId();
    }

    public void playHintSound(Vector2D direction) {
        lastSfx = "hint";
    }

    public void stopInGameLoopingSounds() {
        mixer.pauseAll();
        if (enemyAmbientPlayer != null) {
            enemyAmbientPlayer.stop();
            enemyAmbientPlayer.setVolume(0);
        }
        if (enginePlayer != null) {
            enginePlayer.stop();
            enginePlayer.setVolume(0);
        }
        if (scifiHumPlayer != null) {
            scifiHumPlayer.stop();
        }
    }

    public void stopAll() {
        mixer.stopAll();
        if (enemyAmbientPlayer != null) {
            enemyAmbientPlayer.stop();
            enemyAmbientPlayer.setVolume(0);
        }
        if (enginePlayer != null) {
            enginePlayer.stop();
            enginePlayer.setVolume(0);
        }
        if (scifiHumPlayer != null) {
            scifiHumPlayer.stop();
        }
        silenceTimer = 0.0;
    }

    // Getters for AudioClips
    public AudioClip getLaserSfx() { return laserSfx; }
    public AudioClip getExplosionSfx() { return explosionSfx; }
    public AudioClip getHitSfx() { return hitSfx; }

    public MusicMixer getMixer() { return mixer; }
    public String getLastSfx() { return lastSfx; }
}
