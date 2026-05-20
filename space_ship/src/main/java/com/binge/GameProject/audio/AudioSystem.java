package com.binge.GameProject.audio;

import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.IdolGroup;
import com.binge.GameProject.model.Player;
import com.binge.GameProject.physics.Vector2D;

import java.util.List;

public class AudioSystem {
    private final MusicMixer mixer = new MusicMixer();
    private final IdolVoiceController voiceController = new IdolVoiceController();
    private String lastSfx = "";

    public void updateAmbient(double nearestIdolVolume) {
        mixer.fadeAmbient(1.0 - Math.max(0, Math.min(1, nearestIdolVolume)));
    }

    public void updateIdolGroupVoices(List<IdolGroup> groups, Player player) {
        double loudest = 0.0;
        for (IdolGroup group : groups) {
            Idol nearest = group.getNearestDetectableIdol(player);
            double volume = voiceController.calculateVolume(nearest, player);
            mixer.setGroupSongVolume(group.getGroupId(), group.getSongPath(), volume);
            loudest = Math.max(loudest, volume);
        }
        updateAmbient(loudest);
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

    public MusicMixer getMixer() { return mixer; }
    public String getLastSfx() { return lastSfx; }
}
