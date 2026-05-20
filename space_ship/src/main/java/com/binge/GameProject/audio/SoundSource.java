package com.binge.GameProject.audio;

import com.binge.GameProject.physics.Vector2D;

public class SoundSource {
    private final String id;
    private final Vector2D position;
    private final double volume;

    public SoundSource(String id, Vector2D position, double volume) {
        this.id = id;
        this.position = position;
        this.volume = volume;
    }

    public String getId() { return id; }
    public Vector2D getPosition() { return position; }
    public double getVolume() { return volume; }
}
