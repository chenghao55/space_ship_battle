package com.binge.GameProject.model;

import com.binge.GameProject.rendering.IdolBillboardView;
import com.binge.GameProject.utils.TextureRegistry;
import javafx.scene.Group;
import javafx.scene.paint.Color;

public class Idol extends GameObject {
    private final String idolId;
    private final String groupId;
    private final String displayName;
    private final int memberIndex;
    private final String portraitTexturePath;
    private final String groupLogoTexturePath;
    private final String musicFile;
    private final int totalGroupMemberCount;
    private final Planet parentPlanet;
    private double volume;
    private double singTimer;
    private boolean permanentlyLost;
    private IdolState state = IdolState.AVAILABLE;
    private double orbitRadius;
    private double orbitAngle;
    private double orbitSpeed;
    private double lostTimer;
    private final IdolBillboardView billboardView;

    public Idol(String idolId, String groupId, String displayName, Planet parentPlanet, double x, double y, Color color) {
        this(idolId, groupId, displayName, parentPlanet, x, y, color, null);
    }

    public Idol(String idolId, String groupId, String displayName, Planet parentPlanet, double x, double y, Color color, String texturePath) {
        this(idolId, groupId, 0, displayName, parentPlanet, x, y, color, texturePath, texturePath, null, 0);
    }

    public Idol(String idolId, String groupId, int memberIndex, String displayName, Planet parentPlanet,
                double x, double y, Color color, String portraitTexturePath, String groupLogoTexturePath,
                String musicFile, int totalGroupMemberCount) {
        this.idolId = idolId;
        this.groupId = groupId;
        this.displayName = displayName;
        this.memberIndex = memberIndex;
        this.portraitTexturePath = portraitTexturePath;
        this.groupLogoTexturePath = groupLogoTexturePath;
        this.musicFile = musicFile;
        this.totalGroupMemberCount = totalGroupMemberCount;
        this.parentPlanet = parentPlanet;
        this.position.set(x, y);

        TextureRegistry textures = TextureRegistry.getInstance();
        billboardView = new IdolBillboardView(
                textures.loadOrPlaceholder(portraitTexturePath),
                textures.loadOrPlaceholder(groupLogoTexturePath),
                color
        );
        Group wrapper = billboardView.getRoot();
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

    public Idol(String idolId, String groupId, int memberIndex, String displayName, Planet parentPlanet,
                double orbitRadius, double orbitAngle, double orbitSpeed, Color color,
                String portraitTexturePath, String groupLogoTexturePath, String musicFile, int totalGroupMemberCount) {
        this(idolId, groupId, memberIndex, displayName, parentPlanet, 0, 0, color,
                portraitTexturePath, groupLogoTexturePath, musicFile, totalGroupMemberCount);
        this.orbitRadius = Math.max(orbitRadius, parentPlanet.getRadius() + 90);
        this.orbitAngle = orbitAngle;
        this.orbitSpeed = orbitSpeed;
        updateOrbitPosition(0);
    }

    @Override
    public void update(double dt) {
        if (state == IdolState.AVAILABLE && orbitRadius > 0) {
            updateOrbitPosition(dt);
        }
        if (state == IdolState.SINGING) {
            singTimer -= dt;
            if (singTimer <= 0) {
                state = IdolState.RESCUED;
            }
        } else if (state == IdolState.LOST) {
            lostTimer -= dt;
            if (lostTimer <= 0) {
                isDead = true;
            }
        }
        billboardView.update(state, dt);
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

    public void rescue() {
        if (state == IdolState.AVAILABLE) {
            state = IdolState.RESCUED;
            billboardView.update(state, 0);
            if (view != null) view.setVisible(true);
        }
    }

    public void startSinging(double duration) {
        if (state == IdolState.RESCUED) {
            state = IdolState.SINGING;
            singTimer = duration;
            billboardView.update(state, 0);
        }
    }

    public void markLost() {
        state = IdolState.LOST;
        permanentlyLost = true;
        lostTimer = 0.7;
        billboardView.update(state, 0);
    }

    public void updateBillboardFacing(double playerRotationAngle, double dt) {
        billboardView.faceCamera(playerRotationAngle);
    }

    public boolean isAvailable() {
        return state == IdolState.AVAILABLE;
    }

    public boolean isVisibleToPlayer() {
        return state != IdolState.LOST;
    }

    public String getIdolId() { return idolId; }
    public String getGroupId() { return groupId; }
    public String getDisplayName() { return displayName; }
    public int getMemberIndex() { return memberIndex; }
    public String getPortraitTexturePath() { return portraitTexturePath; }
    public String getGroupLogoTexturePath() { return groupLogoTexturePath; }
    public String getMusicFile() { return musicFile; }
    public int getTotalGroupMemberCount() { return totalGroupMemberCount; }
    public Planet getParentPlanet() { return parentPlanet; }
    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }
    public boolean isPermanentlyLost() { return permanentlyLost; }
    public IdolState getState() { return state; }
    public double getOrbitRadius() { return orbitRadius; }
    public double getOrbitAngle() { return orbitAngle; }
    public double getOrbitSpeed() { return orbitSpeed; }
}
