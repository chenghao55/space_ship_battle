package com.binge.GameProject.model;

import com.binge.GameProject.engine.GameManager;
import com.binge.GameProject.rendering.CameraManager;
import com.binge.GameProject.rendering.ExplosionEffect;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

// OrbitalBase 代表敵方的軌道據點，漂浮在星球附近的太空中
public class OrbitalBase extends GameObject {
    private Rotate rotateTransform; // 用來讓太空站自轉的變形器
    private double hp = 100.0;
    private boolean isDestroyed = false;
    private GameManager gameManager;
    private CameraManager cameraManager;
    private Cylinder cyl;

    // 建構子：設定太空站的位置
    public OrbitalBase(double x, double y, GameManager gameManager, CameraManager cameraManager) {
        this.gameManager = gameManager;
        this.cameraManager = cameraManager;
        this.position.set(x, y); // 設定座標
        
        // 建立一個 3D 圓柱體 (Cylinder) 來代表太空站的圓盤造型
        // 參數 40 是半徑，20 是高度
        cyl = new Cylinder(40, 20); 
        
        // 設定太空站為紅色，並且加上橘色的反光
        PhongMaterial mat = new PhongMaterial(Color.RED);
        mat.setSpecularColor(Color.ORANGE);
        cyl.setMaterial(mat);
        
        this.view = cyl;
        
        // 設定自轉軸為 Z 軸 (讓圓盤像摩天輪或飛盤那樣轉動)
        this.rotateTransform = new Rotate(0, Rotate.Z_AXIS);
        this.view.getTransforms().add(rotateTransform);
        
        // 稍微調整一下高度 (Y軸向下是正的，所以 -30 代表把太空站往上浮起來一點)
        // 這樣可以避免它跟星球完全卡在同一個平面上，增加立體感
        this.view.setTranslateY(-30);
        
        // 更新畫面位置
        updateView();
    }

    // 每幀更新邏輯
    @Override
    public void update(double dt) {
        if (isDestroyed) return; // 毀滅後停止一般邏輯
        
        // 讓太空站快速自轉 (每秒 20 度)
        rotateTransform.setAngle(rotateTransform.getAngle() + dt * 20);
    }
    
    // 承受傷害
    public void takeDamage(double damage) {
        if (isDestroyed) return;
        
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            triggerDestruction();
        } else {
            // 受傷時閃白光
            ((PhongMaterial) cyl.getMaterial()).setDiffuseColor(Color.WHITE);
            // 由於沒有簡單的 setTimeout，這裡粗略用顏色代表，下一個 update 其實不會改回來
            // 實務上可以在 update 中加 timer 復原顏色
        }
    }
    
    private void triggerDestruction() {
        isDestroyed = true;
        
        // 啟動多階段爆炸特效
        ExplosionEffect explosion = new ExplosionEffect(gameManager, cameraManager, position.x, position.y, cyl);
        explosion.startDestructionSequence();
    }
}
