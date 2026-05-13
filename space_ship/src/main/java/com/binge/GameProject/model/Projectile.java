package com.binge.GameProject.model;

import com.binge.GameProject.physics.Vector2D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

// Projectile 代表飛船發射出去的子彈
public class Projectile extends GameObject {
    private double lifespan = 2.0; // 子彈存活時間 (秒)，避免無止盡飛行消耗效能

    // 建構子：需要傳入發射的起點與初始速度
    public Projectile(double x, double y, Vector2D startVelocity) {
        this.position.set(x, y);
        this.velocity.set(startVelocity);
        
        // 建立一個小球代表雷射子彈 (半徑為 5.0，比原先的圓柱體稍大)
        Sphere sphere = new Sphere(5.0); 
        
        // 設定純高對比度螢光材質 (不受光照影響的純色)
        PhongMaterial mat = new PhongMaterial(Color.web("#00FFFF")); // 亮青色
        mat.setSpecularColor(Color.WHITE);
        sphere.setMaterial(mat);
        
        this.view = sphere;
        updateView();
    }

    @Override
    public void update(double dt) {
        // 扣除存活時間
        lifespan -= dt;
        if (lifespan <= 0) {
            // 時間到了，將其標記為死亡，交由 GameManager 回收
            this.isDead = true;
        }
    }
}
