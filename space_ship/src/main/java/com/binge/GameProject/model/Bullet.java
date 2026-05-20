package com.binge.GameProject.model;

import com.binge.GameProject.physics.Vector2D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public abstract class Bullet extends GameObject {
    private final BulletOwner owner;
    private final double damage;
    private double lifeTime;
    private final double radius;

    protected Bullet(double x, double y, Vector2D velocity, BulletOwner owner, Color color, double radius, double lifeTime) {
        this.position.set(x, y);
        this.velocity.set(velocity);
        this.owner = owner;
        this.damage = 1.0;
        this.radius = radius;
        this.lifeTime = lifeTime;
        Sphere sphere = new Sphere(radius);
        PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(Color.WHITE);
        sphere.setMaterial(material);
        this.view = sphere;
        updateView();
    }

    @Override
    public void update(double dt) {
        lifeTime -= dt;
        if (lifeTime <= 0) {
            isDead = true;
        }
    }

    public BulletOwner getOwner() { return owner; }
    public double getDamage() { return damage; }
    public double getRadius() { return radius; }
}
