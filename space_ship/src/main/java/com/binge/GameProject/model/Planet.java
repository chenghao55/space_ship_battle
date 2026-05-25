package com.binge.GameProject.model;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

// Planet 代表遊戲中的星球 (包含提供光照的恆星，以及提供引力、碰撞與救援配置的行星)
public class Planet extends GameObject {
    private double mass;   // 星球的質量 (質量越大，引力越強)
    private double radius; // 星球的半徑 (大小)
    private Rotate rotateTransform; // 用來讓星球自轉的變形器
    private Planet orbitCenter;
    private double orbitRadius;
    private double orbitAngle;
    private double orbitSpeed;
    private boolean orbitPaused;

    // 建構子：設定星球的位置、大小、質量與顏色
    public Planet(double x, double y, double radius, double mass, Color color) {
        this.position.set(x, y); // 設定座標
        this.mass = mass;
        this.radius = radius;
        
        // 建立一個 3D 球體來代表星球
        Sphere sphere = new Sphere(radius);
        // 設定星球的材質和顏色
        PhongMaterial mat = new PhongMaterial(color);
        mat.setSpecularColor(color.brighter()); // 讓星球表面有些微的高光反光
        sphere.setMaterial(mat); // 套用材質
        
        this.view = sphere; // 將球體設定為畫面代表
        
        // 加入 Y 軸的旋轉變形器，用來模擬星球自轉
        this.rotateTransform = new Rotate(0, Rotate.Y_AXIS);
        this.view.getTransforms().add(rotateTransform);
        
        // 更新畫面到正確位置
        updateView();
    }

    // 建構子：設定星球的位置、大小、質量、顏色與貼圖
    public Planet(double x, double y, double radius, double mass, Color color, String texturePath) {
        this(x, y, radius, mass, color);
        setTexture(texturePath);
    }

    // 設定星球的貼圖
    public void setTexture(String texturePath) {
        if (texturePath != null && this.view instanceof Sphere sphere) {
            if (sphere.getMaterial() instanceof PhongMaterial mat) {
                var imageUrl = Planet.class.getResource(texturePath);
                if (imageUrl != null) {
                    mat.setDiffuseMap(new Image(imageUrl.toExternalForm()));
                }
            }
        }
    }

    // 每幀更新星球的邏輯
    @Override
    public void update(double dt) {
        updateOrbit(dt);
        // 讓星球隨著時間緩慢自轉 (每秒轉 10 度)
        rotateTransform.setAngle(rotateTransform.getAngle() + dt * 10);
    }

    public void enableOrbitAround(Planet center, double orbitSpeed) {
        this.orbitCenter = center;
        this.orbitSpeed = orbitSpeed;
        double dx = position.x - center.getPosition().x;
        double dy = position.y - center.getPosition().y;
        this.orbitRadius = Math.sqrt(dx * dx + dy * dy);
        this.orbitAngle = Math.toDegrees(Math.atan2(dy, dx));
    }

    private void updateOrbit(double dt) {
        if (orbitCenter == null) return;
        if (!orbitPaused) {
            orbitAngle += orbitSpeed * dt;
        }
        double rad = Math.toRadians(orbitAngle);
        position.set(
                orbitCenter.getPosition().x + Math.cos(rad) * orbitRadius,
                orbitCenter.getPosition().y + Math.sin(rad) * orbitRadius
        );
        updateView();
    }

    // 提供給外部 (PhysicsEngine) 取得星球質量的方法，用來計算引力
    public double getMass() { return mass; }
    
    // 提供給外部取得星球半徑的方法，用來判斷是否撞到星球表面
    public double getRadius() { return radius; }

    public double getOrbitRadius() { return orbitRadius; }
    public double getOrbitAngle() { return orbitAngle; }
    public double getOrbitSpeed() { return orbitSpeed; }
    public boolean hasOrbit() { return orbitCenter != null; }
    public boolean isOrbitPaused() { return orbitPaused; }
    public void setOrbitPaused(boolean orbitPaused) { this.orbitPaused = orbitPaused; }
}
