package com.binge.GameProject.rendering;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

// ParticleRenderer 負責產生一些裝飾性的特效，例如背景星星
public class ParticleRenderer {
    // 產生靜態的星星背景，用來裝飾 2D UI 層
    public static void generateStarfield(Group uiRoot, double width, double height) {
        // 使用迴圈隨機產生 300 顆星星
        for (int i = 0; i < 300; i++) {
            // 隨機決定星星的大小 (半徑 0.5 到 2.0 之間)
            Circle star = new Circle(Math.random() * 1.5 + 0.5);
            
            // 隨機決定星星的顏色 (微藍、微黃或純白)
            double colorType = Math.random();
            if (colorType > 0.8) star.setFill(Color.LIGHTBLUE);
            else if (colorType > 0.6) star.setFill(Color.LIGHTYELLOW);
            else star.setFill(Color.WHITE);
            
            // 隨機設定星星的透明度 (0.2 到 1.0 之間)，營造星星閃爍的深淺不一感
            star.setOpacity(Math.random() * 0.8 + 0.2);
            
            // 隨機將星星放置在螢幕的任何一個角落
            star.setTranslateX(Math.random() * width);
            star.setTranslateY(Math.random() * height);
            
            // 將畫好的星星加到 UI 畫布上
            uiRoot.getChildren().add(star);
        }
    }
}
