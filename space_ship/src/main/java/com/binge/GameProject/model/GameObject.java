package com.binge.GameProject.model;

import com.binge.GameProject.physics.Vector2D;
import javafx.scene.Node;

// GameObject 是遊戲中所有實體物件 (飛船、星球、太空站) 的「藍圖」(抽象基底類別)
// 只要繼承這個類別，就會自動擁有座標、速度、加速度以及 3D 畫面 (Node)
public abstract class GameObject {
    // 物理屬性
    protected Vector2D position = new Vector2D(); // 目前位置
    protected Vector2D velocity = new Vector2D(); // 移動速度
    protected Vector2D acceleration = new Vector2D(); // 加速度 (推力或引力)
    
    // 畫面屬性
    protected Node view; // 對應的 JavaFX 3D 模型 (例如 Box 或 Sphere)
    
    // 生命週期標記，若設為 true，GameManager 會在下一幀將其移除
    protected boolean isDead = false;
    
    // 抽象方法：強迫所有繼承的子類別都必須自己實作 update 邏輯
    public abstract void update(double dt);
    
    // 將物理運算算出來的座標 (position.x, position.y)，套用到真正的 3D 畫面上
    public void updateView() {
        if (view != null) {
            view.setTranslateX(position.x); // X 軸對應左右
            // 注意：我們用 X-Z 平面來當作 2D 遊戲的軌道平面，所以 y 座標其實是套用到 Z 軸 (前後深度)
            view.setTranslateZ(position.y); 
        }
    }

    // 提供給外部讀取屬性的 Getters
    public Vector2D getPosition() { return position; }
    public Vector2D getVelocity() { return velocity; }
    public Vector2D getAcceleration() { return acceleration; }
    public Node getView() { return view; }
    
    // 取得或設定死亡狀態
    public boolean isDead() { return isDead; }
    public void setDead(boolean dead) { this.isDead = dead; }
}
