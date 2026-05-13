package com.binge.GameProject.rendering;

import com.binge.GameProject.engine.GameManager;
import com.binge.GameProject.engine.GameState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

// ExplosionEffect 處理據點毀滅的多階段電影級特效
public class ExplosionEffect {
    private Group worldRoot;
    private CameraManager cameraManager;
    private GameManager gameManager;
    private double x, z;
    private Cylinder baseView; // 原始的據點模型

    public ExplosionEffect(GameManager gameManager, CameraManager cameraManager, double x, double z, Cylinder baseView) {
        this.gameManager = gameManager;
        this.worldRoot = gameManager.getWorldRoot();
        this.cameraManager = cameraManager;
        this.x = x;
        this.z = z;
        this.baseView = baseView;
    }

    public void startDestructionSequence() {
        Timeline timeline = new Timeline();

        // Stage 1: 核心崩潰 (0s ~ 1s) - 閃爍紅光
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.1), e -> {
            ((PhongMaterial) baseView.getMaterial()).setDiffuseColor(Color.DARKRED);
        }));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.3), e -> {
            ((PhongMaterial) baseView.getMaterial()).setDiffuseColor(Color.RED);
        }));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5), e -> {
            ((PhongMaterial) baseView.getMaterial()).setDiffuseColor(Color.DARKRED);
        }));
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.8), e -> {
            ((PhongMaterial) baseView.getMaterial()).setDiffuseColor(Color.WHITE);
        }));

        // Stage 2: 連鎖爆破 (1s ~ 2.5s) - 產生小爆炸與碎片 (簡化為產生幾個小球體代表碎片)
        for (int i = 0; i < 5; i++) {
            double delay = 1.0 + Math.random() * 1.5;
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(delay), e -> {
                createSmallExplosion(x + (Math.random() - 0.5) * 80, z + (Math.random() - 0.5) * 80);
            }));
        }

        // Stage 3: 主核引爆 (2.5s) - 巨大閃光、攝影機震動、震波環
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2.5), e -> {
            createMainExplosion();
            cameraManager.addCameraShake(30.0, 1.5); // 強烈震動 1.5 秒
            
            // 讓原本的據點消失，替換成焦黑的殘骸
            ((PhongMaterial) baseView.getMaterial()).setDiffuseColor(Color.web("#222222"));
            ((PhongMaterial) baseView.getMaterial()).setSpecularColor(Color.BLACK);
        }));

        // Stage 4: 任務結算 (4.0s) - 切換狀態
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(4.0), e -> {
            gameManager.setCurrentState(GameState.MISSION_CLEAR);
        }));

        timeline.play();
    }

    private void createSmallExplosion(double ex, double ez) {
        Sphere spark = new Sphere(10);
        spark.setMaterial(new PhongMaterial(Color.ORANGE));
        spark.setTranslateX(ex);
        spark.setTranslateZ(ez);
        spark.setTranslateY(-30);
        worldRoot.getChildren().add(spark);

        // 簡單的擴大與消失動畫
        Timeline t = new Timeline(
            new KeyFrame(Duration.seconds(0.2), e -> spark.setRadius(20)),
            new KeyFrame(Duration.seconds(0.4), e -> worldRoot.getChildren().remove(spark))
        );
        t.play();
    }

    private void createMainExplosion() {
        // 巨大的發光球體
        Sphere flash = new Sphere(20);
        PhongMaterial mat = new PhongMaterial(Color.WHITE);
        mat.setSpecularColor(Color.WHITE);
        flash.setMaterial(mat);
        flash.setTranslateX(x);
        flash.setTranslateZ(z);
        flash.setTranslateY(-30);
        worldRoot.getChildren().add(flash);

        // 震波環 (用扁平的圓柱體模擬)
        Cylinder ring = new Cylinder(20, 2);
        ring.setMaterial(new PhongMaterial(Color.CYAN));
        ring.setTranslateX(x);
        ring.setTranslateZ(z);
        ring.setTranslateY(-30);
        worldRoot.getChildren().add(ring);

        // 動畫：球體急速膨脹後消失，震波環擴散
        Timeline t = new Timeline(
            new KeyFrame(Duration.seconds(0.1), e -> {
                flash.setRadius(150);
                mat.setDiffuseColor(Color.YELLOW);
            }),
            new KeyFrame(Duration.seconds(0.5), e -> {
                worldRoot.getChildren().remove(flash);
            }),
            new KeyFrame(Duration.seconds(1.0), e -> {
                ring.setRadius(400);
                ring.setHeight(1);
            }),
            new KeyFrame(Duration.seconds(1.2), e -> {
                worldRoot.getChildren().remove(ring);
            })
        );
        t.play();
    }
}
