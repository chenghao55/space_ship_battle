package com.binge.GameProject.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IdolGroup {
    private final String groupId;
    private final String songId;
    private final String texturePath;
    private final String songPath;
    private final List<Idol> idols = new ArrayList<>();

    public IdolGroup(String groupId, String songId) {
        this(groupId, songId, null, "/music/supernova.mp3");
    }

    public IdolGroup(String groupId, String songId, String texturePath, String songPath) {
        this.groupId = groupId;
        this.songId = songId;
        this.texturePath = texturePath;
        this.songPath = songPath;
    }

    public void add(Idol idol) {
        idols.add(idol);
    }

    public Idol getNearestDetectableIdol(Player player) {
        Idol nearest = null;
        double bestDistanceSq = Double.MAX_VALUE;
        for (Idol idol : idols) {
            if (!idol.isAvailable()) continue;
            double distSq = idol.getPosition().subtract(player.getPosition()).magnitudeSquared();
            if (distSq < bestDistanceSq) {
                bestDistanceSq = distSq;
                nearest = idol;
            }
        }
        return nearest;
    }

    public String getGroupId() { return groupId; }
    public String getSongId() { return songId; }
    public String getTexturePath() { return texturePath; }
    public String getSongPath() { return songPath; }
    public List<Idol> getIdols() { return Collections.unmodifiableList(idols); }
}
