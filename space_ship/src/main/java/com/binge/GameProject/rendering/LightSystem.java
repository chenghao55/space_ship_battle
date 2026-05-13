package com.binge.GameProject.rendering;

import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;

// LightSystem 負責設定 3D 場景中的光照效果
public class LightSystem {
    // 將燈光加入到 3D 世界的根節點 (root) 中
    public static void setupLights(Group root) {
        // 1. 環境光 (AmbientLight)
        // 將環境光改為白色 (這裡使用亮灰色 #cccccc 保留一點點立體陰影的對比，若用純白會完全沒有立體感)
        AmbientLight ambient = new AmbientLight(Color.web("#cccccc"));
        
        // 2. 點光源 (PointLight)
        // 為了極大幅度提升恆星的光源亮度，我們在原點重疊多盞橘色點光源
        PointLight sunLight1 = new PointLight(Color.ORANGE);
        PointLight sunLight2 = new PointLight(Color.ORANGE);
        PointLight sunLight3 = new PointLight(Color.ORANGE);
        PointLight sunLight4 = new PointLight(Color.ORANGE);
        PointLight sunLight5 = new PointLight(Color.ORANGE);
        
        // 將這幾盞燈都加入到遊戲世界中
        root.getChildren().addAll(ambient, sunLight1, sunLight2, sunLight3, sunLight4, sunLight5);
    }
}
