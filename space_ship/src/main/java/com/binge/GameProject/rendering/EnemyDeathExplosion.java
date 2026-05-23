package com.binge.GameProject.rendering;

import com.binge.GameProject.audio.AudioSystem;
import com.binge.GameProject.physics.Vector2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyDeathExplosion {
    private static final double FINAL_HIT_FLASH_TIME = 0.10;
    private static final double TOTAL_TIME = 1.05;
    private static final double MAIN_FLASH_TIME = 0.38;
    private static final double SECONDARY_START_TIME = 0.16;

    private final Group view = new Group();
    private final Sphere mainFlash = new Sphere(18);
    private final Cylinder shockwave = new Cylinder(18, 4);
    private final List<DebrisParticle> debris = new ArrayList<>();
    private final List<SecondaryFlash> secondaryFlashes = new ArrayList<>();
    private double elapsed;
    private boolean started;
    private boolean smallSfxPlayed;

    public EnemyDeathExplosion(Vector2D position) {
        view.setTranslateX(position.x);
        view.setTranslateZ(position.y);
        buildMainFlash();
        buildShockwave();
        buildDebris();
        buildSecondaryFlashes();
        view.setVisible(false);
    }

    private void buildMainFlash() {
        PhongMaterial material = new PhongMaterial(Color.web("#fff7c4"));
        material.setSpecularColor(Color.WHITE);
        mainFlash.setMaterial(material);
        mainFlash.setOpacity(0);
        view.getChildren().add(mainFlash);
    }

    private void buildShockwave() {
        PhongMaterial material = new PhongMaterial(Color.web("#7dfff4"));
        material.setSpecularColor(Color.WHITE);
        shockwave.setMaterial(material);
        shockwave.setOpacity(0);
        view.getChildren().add(shockwave);
    }

    private void buildDebris() {
        Random random = new Random(37);
        PhongMaterial material = new PhongMaterial(Color.web("#ff9b4a"));
        material.setSpecularColor(Color.WHITE);
        for (int i = 0; i < 14; i++) {
            Box shard = new Box(14 + random.nextDouble() * 10, 8 + random.nextDouble() * 6, 22 + random.nextDouble() * 20);
            shard.setMaterial(material);
            shard.setVisible(false);
            double angle = Math.PI * 2 * i / 14.0 + random.nextDouble() * 0.28;
            double speed = 360 + random.nextDouble() * 360;
            double ySpeed = -80 + random.nextDouble() * 160;
            double life = 0.55 + random.nextDouble() * 0.45;
            debris.add(new DebrisParticle(shard, Math.cos(angle) * speed, Math.sin(angle) * speed, ySpeed,
                    -240 + random.nextDouble() * 480, life));
            view.getChildren().add(shard);
        }
    }

    private void buildSecondaryFlashes() {
        Random random = new Random(91);
        PhongMaterial material = new PhongMaterial(Color.web("#ffdf7a"));
        material.setSpecularColor(Color.WHITE);
        for (int i = 0; i < 3; i++) {
            Sphere flash = new Sphere(18 + random.nextDouble() * 14);
            flash.setMaterial(material);
            flash.setVisible(false);
            flash.setOpacity(0);
            double delay = SECONDARY_START_TIME + i * 0.07 + random.nextDouble() * 0.04;
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 70 + random.nextDouble() * 130;
            flash.setTranslateX(Math.cos(angle) * distance);
            flash.setTranslateZ(Math.sin(angle) * distance);
            secondaryFlashes.add(new SecondaryFlash(flash, delay));
            view.getChildren().add(flash);
        }
    }

    public void update(double dt) {
        elapsed += dt;
        if (!started && elapsed >= FINAL_HIT_FLASH_TIME) {
            started = true;
            view.setVisible(true);
            AudioSystem audio = AudioSystem.getInstance();
            if (audio != null) audio.playSfx("enemy_destroy");
        }
        if (!started) return;

        double t = elapsed - FINAL_HIT_FLASH_TIME;
        updateMainFlash(t);
        updateShockwave(t);
        updateDebris(t);
        updateSecondaryFlashes(t);
    }

    private void updateMainFlash(double t) {
        double progress = clamp(t / MAIN_FLASH_TIME);
        double scale = 0.35 + progress * 9.5;
        mainFlash.setScaleX(scale);
        mainFlash.setScaleY(scale);
        mainFlash.setScaleZ(scale);
        mainFlash.setOpacity(Math.max(0, 0.95 - progress * 1.15));
    }

    private void updateShockwave(double t) {
        double progress = clamp(t / 0.62);
        double scale = 0.7 + progress * 16.0;
        shockwave.setScaleX(scale);
        shockwave.setScaleZ(scale);
        shockwave.setScaleY(0.08);
        shockwave.setOpacity(Math.max(0, 0.72 - progress * 0.86));
    }

    private void updateDebris(double t) {
        for (DebrisParticle particle : debris) {
            particle.update(t);
        }
    }

    private void updateSecondaryFlashes(double t) {
        for (SecondaryFlash flash : secondaryFlashes) {
            flash.update(t);
        }
        if (!smallSfxPlayed && t >= SECONDARY_START_TIME) {
            smallSfxPlayed = true;
            AudioSystem audio = AudioSystem.getInstance();
            if (audio != null) audio.playSfx("explosion_small");
        }
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    public boolean isFinished() {
        return elapsed >= TOTAL_TIME;
    }

    public Group getView() {
        return view;
    }

    private static class DebrisParticle {
        private final Node node;
        private final double vx;
        private final double vz;
        private final double vy;
        private final double spin;
        private final double life;

        private DebrisParticle(Node node, double vx, double vz, double vy, double spin, double life) {
            this.node = node;
            this.vx = vx;
            this.vz = vz;
            this.vy = vy;
            this.spin = spin;
            this.life = life;
        }

        private void update(double t) {
            if (t > life) {
                node.setVisible(false);
                return;
            }
            node.setVisible(true);
            double fade = 1.0 - t / life;
            node.setTranslateX(vx * t);
            node.setTranslateZ(vz * t);
            node.setTranslateY(vy * t);
            node.setRotate(spin * t);
            node.setOpacity(Math.max(0, fade));
            double scale = 0.35 + fade * 0.65;
            node.setScaleX(scale);
            node.setScaleY(scale);
            node.setScaleZ(scale);
        }
    }

    private static class SecondaryFlash {
        private final Sphere node;
        private final double delay;

        private SecondaryFlash(Sphere node, double delay) {
            this.node = node;
            this.delay = delay;
        }

        private void update(double t) {
            double local = t - delay;
            if (local < 0 || local > 0.28) {
                if (local < 0) node.setVisible(false);
                return;
            }
            node.setVisible(true);
            double progress = local / 0.28;
            double scale = 0.5 + progress * 5.0;
            node.setScaleX(scale);
            node.setScaleY(scale);
            node.setScaleZ(scale);
            node.setOpacity(Math.max(0, 0.75 - progress));
        }
    }
}
