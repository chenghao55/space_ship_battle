package com.binge.GameProject.model;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class Idol extends GameObject {
    private final String idolId;
    private final String groupId;
    private final String displayName;
    private final Planet parentPlanet;
    private final double detectRadius = 50.0;
    private double volume;
    private double singTimer;
    private boolean permanentlyLost;
    private IdolState state = IdolState.HIDDEN;
    private double orbitRadius;
    private double orbitAngle;
    private double orbitSpeed;

    public Idol(String idolId, String groupId, String displayName, Planet parentPlanet, double x, double y, Color color) {
        this(idolId, groupId, displayName, parentPlanet, x, y, color, null);
    }

    public Idol(String idolId, String groupId, String displayName, Planet parentPlanet, double x, double y, Color color, String texturePath) {
        this.idolId = idolId;
        this.groupId = groupId;
        this.displayName = displayName;
        this.parentPlanet = parentPlanet;
        this.position.set(x, y);

        Group wrapper = new Group();
        Sphere glow = new Sphere(28);
        PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(Color.WHITE);
        if (texturePath != null) {
            var imageUrl = Idol.class.getResource(texturePath);
            if (imageUrl != null) {
                material.setDiffuseMap(new Image(imageUrl.toExternalForm()));
            }
        }
        glow.setMaterial(material);
        wrapper.getChildren().add(glow);
        wrapper.setVisible(true);
        this.view = wrapper;
        updateView();
    }

    public Idol(String idolId, String groupId, String displayName, Planet parentPlanet,
                double orbitRadius, double orbitAngle, double orbitSpeed, Color color) {
        this(idolId, groupId, displayName, parentPlanet, orbitRadius, orbitAngle, orbitSpeed, color, null);
    }

    public Idol(String idolId, String groupId, String displayName, Planet parentPlanet,
                double orbitRadius, double orbitAngle, double orbitSpeed, Color color, String texturePath) {
        this(idolId, groupId, displayName, parentPlanet, 0, 0, color, texturePath);
        this.orbitRadius = Math.max(orbitRadius, parentPlanet.getRadius() + 90);
        this.orbitAngle = orbitAngle;
        this.orbitSpeed = orbitSpeed;
        updateOrbitPosition(0);
    }

    @Override
    public void update(double dt) {
        if ((state == IdolState.HIDDEN || state == IdolState.DETECTED) && orbitRadius > 0) {
            updateOrbitPosition(dt);
        }
        if (state == IdolState.SINGING) {
            singTimer -= dt;
            if (singTimer <= 0) {
                state = IdolState.RESCUED;
            }
        }
    }

    private void updateOrbitPosition(double dt) {
        orbitAngle += orbitSpeed * dt;
        double rad = Math.toRadians(orbitAngle);
        position.set(
                parentPlanet.getPosition().x + Math.cos(rad) * orbitRadius,
                parentPlanet.getPosition().y + Math.sin(rad) * orbitRadius
        );
        updateView();
    }

    public void detect() {
        if (state == IdolState.HIDDEN) {
            state = IdolState.DETECTED;
        }
    }

    public void rescue() {
        if (state == IdolState.DETECTED) {
            state = IdolState.RESCUED;
            if (view != null) view.setVisible(true);
        }
    }

    public void startSinging(double duration) {
        if (state == IdolState.RESCUED) {
            state = IdolState.SINGING;
            singTimer = duration;
        }
    }

    public void markLost() {
        state = IdolState.LOST;
        permanentlyLost = true;
        if (view != null) view.setVisible(false);
    }

    public boolean isAvailable() {
        return state == IdolState.HIDDEN || state == IdolState.DETECTED;
    }

    public boolean isVisibleToPlayer() {
        return state != IdolState.HIDDEN && state != IdolState.LOST;
    }

    public String getIdolId() { return idolId; }
    public String getGroupId() { return groupId; }
    public String getDisplayName() { return displayName; }
    public Planet getParentPlanet() { return parentPlanet; }
    public double getDetectRadius() { return detectRadius; }
    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }
    public boolean isPermanentlyLost() { return permanentlyLost; }
    public IdolState getState() { return state; }
    public double getOrbitRadius() { return orbitRadius; }
    public double getOrbitAngle() { return orbitAngle; }
    public double getOrbitSpeed() { return orbitSpeed; }
}
