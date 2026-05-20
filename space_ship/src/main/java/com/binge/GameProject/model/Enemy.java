package com.binge.GameProject.model;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class Enemy extends GameObject {
    private int hp = 3;
    private final double aggroRadius = 1650.0;
    private double shootCooldown = 1.2;
    private boolean alive = true;

    public Enemy(double x, double y) {
        this.position.set(x, y);
        Group group = new Group();
        Box core = new Box(70, 35, 70);
        PhongMaterial material = new PhongMaterial(Color.web("#ff3355"));
        material.setSpecularColor(Color.WHITE);
        core.setMaterial(material);
        group.getChildren().add(core);
        this.view = group;
        updateView();
    }

    @Override
    public void update(double dt) {
        shootCooldown -= dt;
    }

    public boolean canShoot() {
        return alive && shootCooldown <= 0;
    }

    public void resetShootCooldown() {
        shootCooldown = 1.34;
    }

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            alive = false;
            isDead = true;
        }
    }

    public double getAggroRadius() { return aggroRadius; }
    public boolean isAlive() { return alive; }
}
