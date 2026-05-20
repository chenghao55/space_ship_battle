package com.binge.GameProject.physics;

public class Hitbox {
    private final Vector2D center;
    private final double radius;

    public Hitbox(Vector2D center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public Vector2D getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public boolean intersects(Vector2D point, double otherRadius) {
        double combined = radius + otherRadius;
        return center.subtract(point).magnitudeSquared() <= combined * combined;
    }

    public boolean intersects(Hitbox other) {
        return intersects(other.center, other.radius);
    }
}
