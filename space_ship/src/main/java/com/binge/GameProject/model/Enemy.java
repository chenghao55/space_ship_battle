package com.binge.GameProject.model;

public class Enemy extends GameObject {
    private int hp = 3;
    private final double aggroRadius = 1650.0;
    private double shootCooldown = 1.2;
    private boolean alive = true;
    private final EnemyView enemyView;

    public Enemy(double x, double y) {
        this.position.set(x, y);
        this.enemyView = new EnemyView(aggroRadius);
        this.view = enemyView.getRoot();
        updateView();
    }

    @Override
    public void update(double dt) {
        shootCooldown -= dt;
        enemyView.update(dt);
        if (!alive && enemyView.isExplosionFinished()) {
            isDead = true;
        }
    }

    public boolean canShoot() {
        return alive && shootCooldown <= 0;
    }

    public void resetShootCooldown() {
        shootCooldown = 1.34;
        enemyView.playChargeFlash();
    }

    public void takeDamage(int amount) {
        if (!alive) return;
        hp -= amount;
        enemyView.playHitFlash();
        if (hp <= 0) {
            alive = false;
            enemyView.playExplosion();
        }
    }

    public void playMuzzleFlash() {
        enemyView.playMuzzleFlash();
    }

    public double getAggroRadius() { return aggroRadius; }
    public boolean isAlive() { return alive; }
}
