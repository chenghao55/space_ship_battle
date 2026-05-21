package com.binge.GameProject.model;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

public class EnemyView {
    private final Group root = new Group();
    private final Group ring = new Group();
    private final Group flashGroup = new Group();
    private final Group debrisGroup = new Group();
    private final Sphere core = new Sphere(34);
    private final PhongMaterial normalMaterial = new PhongMaterial(Color.web("#ff3355"));
    private final PhongMaterial hitMaterial = new PhongMaterial(Color.WHITE);
    private final PhongMaterial chargeMaterial = new PhongMaterial(Color.web("#ffeb8a"));
    private double hitFlashTimer;
    private double chargeTimer;
    private double muzzleFlashTimer;
    private double explosionTimer;
    private boolean exploding;
    private double ringAngle;

    public EnemyView(double aggroRadius) {
        buildRing();
        buildCannons();
        buildDebris();
        buildCore();
        root.getChildren().addAll(debrisGroup, ring, flashGroup, core);
    }

    private void buildCore() {
        normalMaterial.setSpecularColor(Color.WHITE);
        core.setMaterial(normalMaterial);
    }

    private void buildRing() {
        PhongMaterial ringMaterial = new PhongMaterial(Color.web("#8df7ff"));
        ringMaterial.setSpecularColor(Color.WHITE);
        for (int i = 0; i < 14; i++) {
            double angle = Math.PI * 2 * i / 14.0;
            Sphere node = new Sphere(i % 2 == 0 ? 8 : 6);
            node.setMaterial(ringMaterial);
            node.setTranslateX(Math.cos(angle) * 78);
            node.setTranslateZ(Math.sin(angle) * 78);
            ring.getChildren().add(node);
        }
    }

    private void buildCannons() {
        addCannon(0, -1);
        addCannon(0, 1);
        addCannon(-1, 0);
        addCannon(1, 0);

        PhongMaterial flashMaterial = new PhongMaterial(Color.web("#fff4a5"));
        for (int i = 0; i < 4; i++) {
            Sphere flash = new Sphere(14);
            flash.setMaterial(flashMaterial);
            flash.setVisible(false);
            flashGroup.getChildren().add(flash);
        }
        flashGroup.getChildren().get(0).setTranslateZ(-130);
        flashGroup.getChildren().get(1).setTranslateZ(130);
        flashGroup.getChildren().get(2).setTranslateX(-130);
        flashGroup.getChildren().get(3).setTranslateX(130);
    }

    private void addCannon(double dirX, double dirZ) {
        Cylinder cannon = new Cylinder(9, 96);
        cannon.setMaterial(new PhongMaterial(Color.web("#cdd6e6")));
        if (dirX != 0) {
            cannon.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        } else {
            cannon.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        }
        cannon.setTranslateX(dirX * 72);
        cannon.setTranslateZ(dirZ * 72);
        root.getChildren().add(cannon);

        Box brace = new Box(20, 18, 24);
        brace.setMaterial(new PhongMaterial(Color.web("#515b70")));
        brace.setTranslateX(dirX * 38);
        brace.setTranslateZ(dirZ * 38);
        root.getChildren().add(brace);
    }

    private void buildDebris() {
        PhongMaterial debrisMaterial = new PhongMaterial(Color.web("#ff7a55"));
        for (int i = 0; i < 10; i++) {
            Box shard = new Box(12, 8, 26);
            shard.setMaterial(debrisMaterial);
            shard.setVisible(false);
            debrisGroup.getChildren().add(shard);
        }
    }

    public void update(double dt) {
        ringAngle = (ringAngle + dt * 42.0) % 360;
        ring.getTransforms().setAll(new Rotate(ringAngle, Rotate.Y_AXIS));
        hitFlashTimer = Math.max(0, hitFlashTimer - dt);
        chargeTimer = Math.max(0, chargeTimer - dt);
        muzzleFlashTimer = Math.max(0, muzzleFlashTimer - dt);

        if (muzzleFlashTimer <= 0) {
            for (Node flash : flashGroup.getChildren()) flash.setVisible(false);
        }

        if (exploding) {
            explosionTimer -= dt;
            double progress = 1.0 - Math.max(0, explosionTimer) / 0.55;
            double scale = 1.0 + progress * 1.5;
            root.setScaleX(scale);
            root.setScaleY(scale);
            root.setScaleZ(scale);
            root.setOpacity(Math.max(0, 1.0 - progress));
            updateExplosionDebris(progress);
            return;
        }

        if (hitFlashTimer > 0) {
            core.setMaterial(hitMaterial);
        } else if (chargeTimer > 0) {
            core.setMaterial(chargeMaterial);
        } else {
            core.setMaterial(normalMaterial);
        }
    }

    public void playChargeFlash() {
        chargeTimer = 0.18;
    }

    public void playMuzzleFlash() {
        muzzleFlashTimer = 0.12;
        for (Node flash : flashGroup.getChildren()) flash.setVisible(true);
    }

    public void playHitFlash() {
        hitFlashTimer = 0.16;
    }

    public void playExplosion() {
        exploding = true;
        explosionTimer = 0.55;
        for (Node flash : flashGroup.getChildren()) flash.setVisible(true);
        for (int i = 0; i < debrisGroup.getChildren().size(); i++) {
            Node shard = debrisGroup.getChildren().get(i);
            shard.setVisible(true);
            shard.setRotate(i * 31);
        }
    }

    private void updateExplosionDebris(double progress) {
        for (int i = 0; i < debrisGroup.getChildren().size(); i++) {
            Node shard = debrisGroup.getChildren().get(i);
            double angle = Math.PI * 2 * i / debrisGroup.getChildren().size();
            double distance = 36 + progress * 220;
            shard.setTranslateX(Math.cos(angle) * distance);
            shard.setTranslateY((i % 3 - 1) * progress * 55);
            shard.setTranslateZ(Math.sin(angle) * distance);
            shard.setRotate(shard.getRotate() + 8);
        }
    }

    public boolean isExplosionFinished() {
        return exploding && explosionTimer <= 0;
    }

    public Group getRoot() {
        return root;
    }
}
