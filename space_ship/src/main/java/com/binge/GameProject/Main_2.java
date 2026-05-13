package com.binge.GameProject;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.scene.PointLight;
import javafx.scene.AmbientLight;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;

import java.util.ArrayList;
import java.util.List;

public class Main_2 extends Application {

    private Group root;
    private Group fighterGroup;
    private PerspectiveCamera camera;

    // 攝影機階層節點
    private Group cameraPivot;
    private Group cameraPitchRig;
    private Group cameraRollRig;
    private double currentCameraRoll = 0.0;
    private double currentFov = 60.0;

    // 控制狀態
    private boolean goUp, goDown, goLeft, goRight;
    private boolean isFiring = false;
    private boolean isBoosting = false;

    // 戰鬥機狀態
    private double baseSpeed = 10.0;
    private double speed = baseSpeed;
    private double currentPitch = 0.0; // 俯仰角，控制機頭上下
    private final double PITCH_SPEED = 2.0;

    // 子彈管理
    private List<Projectile> projectiles = new ArrayList<>();
    private long lastFireTime = 0;
    private final long FIRE_COOLDOWN = 150_000_000; // 0.15秒冷卻 (奈秒)

    @Override
    public void start(Stage primaryStage) {
        root = new Group();

        // 建立並設定攝影機階層架構
        cameraPivot = new Group();
        cameraPitchRig = new Group();
        cameraRollRig = new Group();

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(20000.0);
        camera.setFieldOfView(60.0);

        // 將攝影機向後與向上拉開距離
        camera.setTranslateZ(-1200);
        camera.setTranslateY(-200);

        // 設定 PitchRig 預設向下俯視
        cameraPitchRig.getTransforms().add(new Rotate(-10, Rotate.X_AXIS));

        // 組裝階層
        cameraRollRig.getChildren().add(camera);
        cameraPitchRig.getChildren().add(cameraRollRig);
        cameraPivot.getChildren().add(cameraPitchRig);
        root.getChildren().add(cameraPivot);

        // 建立場景
        Scene scene = new Scene(root, 1024, 768, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK); // 太空背景
        scene.setCamera(camera);

        // 加入環境光，確保戰機與宇宙不會有任何一面變成純黑
        AmbientLight ambientLight = new AmbientLight(Color.rgb(40, 40, 45));
        root.getChildren().add(ambientLight);

        setupEnvironment();
        setupFighter();

        setupInputHandlers(scene);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateFighterPosition();
                updateCameraPosition(); // 新增：更新攝影機追蹤

                if (isFiring && now - lastFireTime > FIRE_COOLDOWN) {
                    fireProjectiles();
                    lastFireTime = now;
                }

                updateProjectiles();
            }
        };
        timer.start();

        primaryStage.setTitle("3D Space Fighter");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupEnvironment() {
        Sphere sun = new Sphere(2000);
        sun.setTranslateZ(12000); // 太陽放遠一點
        sun.setTranslateY(-1000);

        // 建立純橘色的發光材質
        PhongMaterial sunMaterial = new PhongMaterial();
        sunMaterial.setDiffuseColor(Color.ORANGE);
        sunMaterial.setSpecularColor(Color.YELLOW);

        // 製作一個 1x1 像素的純橘色圖片作為自發光貼圖 (完美解決載入圖片變黑的問題)
        WritableImage glowImage = new WritableImage(1, 1);
        PixelWriter writer = glowImage.getPixelWriter();
        writer.setColor(0, 0, Color.ORANGE);
        sunMaterial.setSelfIlluminationMap(glowImage);

        sun.setMaterial(sunMaterial);

        // 第一盞光源：主要的強烈點光源 (橘色光)
        PointLight sunLight1 = new PointLight(Color.ORANGE);
        sunLight1.setTranslateZ(8000);
        sunLight1.setTranslateY(-500);

        // 第二盞光源：疊加光芒，讓整體環境更明亮 (深橘色)
        PointLight sunLight2 = new PointLight(Color.DARKORANGE);
        sunLight2.setTranslateZ(8000);
        sunLight2.setTranslateY(-500);

        root.getChildren().addAll(sun, sunLight1, sunLight2);
    }

    private void setupFighter() {
        fighterGroup = new Group();

        // 參考圖配色
        PhongMaterial bodyMat = new PhongMaterial(Color.web("#E0E0E0")); // 亮灰/白
        PhongMaterial blueMat = new PhongMaterial(Color.web("#1E5692")); // 深藍色機鼻/部件
        PhongMaterial cockpitMat = new PhongMaterial(Color.web("#F1C40F")); // 黃色駕駛艙
        PhongMaterial darkMat = new PhongMaterial(Color.web("#2C3E50")); // 深灰色引擎

        // 1. 修長機鼻 (分層)
        Box noseTip = new Box(15, 10, 100);
        noseTip.setTranslateZ(200);
        noseTip.setMaterial(blueMat);

        Box noseMid = new Box(30, 15, 80);
        noseMid.setTranslateZ(110);
        noseMid.setMaterial(bodyMat);

        // 駕駛艙
        Box cockpit = new Box(25, 15, 40);
        cockpit.setTranslateZ(100);
        cockpit.setTranslateY(-10); // 凸起
        cockpit.setMaterial(cockpitMat);

        // 2. 主機身 (較寬扁)
        Box mainBody = new Box(60, 25, 120);
        mainBody.setTranslateZ(-10);
        mainBody.setMaterial(bodyMat);

        // 3. 雙側大型引擎室 (如圖中厚重感)
        Group leftEngineBlock = createAdvancedEngineBlock(bodyMat, blueMat, darkMat, -55, -5, -30);
        Group rightEngineBlock = createAdvancedEngineBlock(bodyMat, blueMat, darkMat, 55, -5, -30);

        // 4. 主翼 (在引擎外側)
        Box leftWing = new Box(80, 5, 60);
        leftWing.setTranslateX(-115);
        leftWing.setTranslateZ(-30);
        leftWing.getTransforms().add(new Rotate(-15, Rotate.Y_AXIS)); // 稍微後掠
        leftWing.setMaterial(bodyMat);

        Box rightWing = new Box(80, 5, 60);
        rightWing.setTranslateX(115);
        rightWing.setTranslateZ(-30);
        rightWing.getTransforms().add(new Rotate(15, Rotate.Y_AXIS)); // 稍微後掠
        rightWing.setMaterial(bodyMat);

        // 5. V型尾翼
        Box leftTail = new Box(5, 40, 40);
        leftTail.setTranslateX(-40);
        leftTail.setTranslateY(-25);
        leftTail.setTranslateZ(-80);
        leftTail.getTransforms().add(new Rotate(-30, Rotate.Z_AXIS));
        leftTail.setMaterial(blueMat);

        Box rightTail = new Box(5, 40, 40);
        rightTail.setTranslateX(40);
        rightTail.setTranslateY(-25);
        rightTail.setTranslateZ(-80);
        rightTail.getTransforms().add(new Rotate(30, Rotate.Z_AXIS));
        rightTail.setMaterial(blueMat);

        // 發射口 (移至引擎區前方下側)
        Box leftGunPort = new Box(5, 5, 20);
        leftGunPort.setTranslateX(-60);
        leftGunPort.setTranslateY(15);
        leftGunPort.setTranslateZ(40);
        leftGunPort.setMaterial(darkMat);

        Box rightGunPort = new Box(5, 5, 20);
        rightGunPort.setTranslateX(60);
        rightGunPort.setTranslateY(15);
        rightGunPort.setTranslateZ(40);
        rightGunPort.setMaterial(darkMat);

        // 戰機補光燈 (跟著戰機走)
        PointLight fillLight = new PointLight(Color.WHITE);
        fillLight.setTranslateY(-150);
        fillLight.setTranslateZ(-300);

        // 注意：這裡移除了 camera，讓它獨立在世界中
        fighterGroup.getChildren().addAll(
                noseTip, noseMid, cockpit, mainBody,
                leftEngineBlock, rightEngineBlock,
                leftWing, rightWing,
                leftTail, rightTail,
                leftGunPort, rightGunPort,
                fillLight
        );

        root.getChildren().add(fighterGroup);
    }

    // 建立較為複雜的引擎模塊
    private Group createAdvancedEngineBlock(PhongMaterial bodyMat, PhongMaterial blueMat, PhongMaterial darkMat, double tx, double ty, double tz) {
        Group engineGroup = new Group();

        // 引擎主體
        Box mainBlock = new Box(50, 35, 100);
        mainBlock.setMaterial(bodyMat);

        // 前方進氣口 (藍色區域)
        Box intakeFront = new Box(52, 37, 40);
        intakeFront.setTranslateZ(50);
        intakeFront.setMaterial(blueMat);

        // 黑色進氣孔洞
        Box intakeHole = new Box(40, 20, 10);
        intakeHole.setTranslateZ(70);
        intakeHole.setMaterial(darkMat);

        // 後方噴嘴
        Cylinder nozzle = new Cylinder(15, 30);
        nozzle.setTranslateZ(-60);
        nozzle.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        nozzle.setMaterial(darkMat);

        engineGroup.getChildren().addAll(mainBlock, intakeFront, intakeHole, nozzle);
        engineGroup.setTranslateX(tx);
        engineGroup.setTranslateY(ty);
        engineGroup.setTranslateZ(tz);
        return engineGroup;
    }

    private void setupInputHandlers(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.W) goUp = true;
            if (event.getCode() == KeyCode.S) goDown = true;
            if (event.getCode() == KeyCode.A) goLeft = true;
            if (event.getCode() == KeyCode.D) goRight = true;
            if (event.getCode() == KeyCode.SHIFT) isBoosting = true;
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.W) goUp = false;
            if (event.getCode() == KeyCode.S) goDown = false;
            if (event.getCode() == KeyCode.A) goLeft = false;
            if (event.getCode() == KeyCode.D) goRight = false;
            if (event.getCode() == KeyCode.SHIFT) isBoosting = false;
        });

        scene.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) isFiring = true;
        });

        scene.setOnMouseReleased(event -> {
            isFiring = false;
        });
    }

    private void updateFighterPosition() {
        // 加速邏輯
        double targetSpeed = isBoosting ? baseSpeed * 3.0 : baseSpeed;
        speed += (targetSpeed - speed) * 0.1; // 平滑過渡速度

        // W/S 改變機頭俯仰角 (W機頭往上飛，S機頭往下飛)
        if (goUp) {
            currentPitch -= PITCH_SPEED;
        }
        if (goDown) {
            currentPitch += PITCH_SPEED;
        }

        // 限制俯仰角度
        currentPitch = Math.max(-60, Math.min(60, currentPitch));

        // 戰機自身的旋轉 (現在使用者看得到這個旋轉了)
        fighterGroup.getTransforms().clear();
        fighterGroup.getTransforms().add(new Rotate(currentPitch, Rotate.X_AXIS));

        // 計算持續推進的速度分量
        double pitchRad = Math.toRadians(currentPitch);
        double dx = 0;
        double dy = -speed * Math.sin(pitchRad);
        double dz = speed * Math.cos(pitchRad);

        // A/D 左右移動
        if (goLeft) dx -= speed;
        if (goRight) dx += speed;

        fighterGroup.setTranslateX(fighterGroup.getTranslateX() + dx);
        fighterGroup.setTranslateY(fighterGroup.getTranslateY() + dy);
        fighterGroup.setTranslateZ(fighterGroup.getTranslateZ() + dz);
    }

    private void updateCameraPosition() {
        // 目標位置：戰鬥機本身 (實際拉遠由 PerspectiveCamera 的 Translate Z 處理)
        double targetX = fighterGroup.getTranslateX();
        double targetY = fighterGroup.getTranslateY();
        double targetZ = fighterGroup.getTranslateZ();

        // 位置平滑插值 (Lerp) - 產生追蹤延遲感
        double smoothing = 0.1;
        cameraPivot.setTranslateX(cameraPivot.getTranslateX() + (targetX - cameraPivot.getTranslateX()) * smoothing);
        cameraPivot.setTranslateY(cameraPivot.getTranslateY() + (targetY - cameraPivot.getTranslateY()) * smoothing);
        cameraPivot.setTranslateZ(cameraPivot.getTranslateZ() + (targetZ - cameraPivot.getTranslateZ()) * smoothing);

        // 動態 Roll 傾斜 (當按下 A/D 時)
        double targetRoll = 0;
        if (goLeft) targetRoll = -15;
        if (goRight) targetRoll = 15;
        currentCameraRoll += (targetRoll - currentCameraRoll) * 0.1; // 平滑過渡 Roll
        
        cameraRollRig.getTransforms().clear();
        cameraRollRig.getTransforms().add(new Rotate(currentCameraRoll, Rotate.Z_AXIS));

        // 動態 FOV 拉伸 (加速時增加 FOV)
        double targetFov = isBoosting ? 95.0 : 60.0;
        currentFov += (targetFov - currentFov) * 0.08;
        camera.setFieldOfView(currentFov);
    }

    private void fireProjectiles() {
        // 取得戰機當前位置
        double fX = fighterGroup.getTranslateX();
        double fY = fighterGroup.getTranslateY();
        double fZ = fighterGroup.getTranslateZ();

        // 為了讓子彈從槍管射出，把戰機的俯仰角考慮進去來計算發射點的偏移
        double pitchRad = Math.toRadians(currentPitch);

        // 配合新模組的槍管位置
        double gunOffsetX = 60;
        double gunOffsetY = 15;
        double gunOffsetZ = 40;

        // 根據俯仰角旋轉計算出真實的世界座標偏移
        double rotatedGunOffsetY = gunOffsetY * Math.cos(pitchRad) - gunOffsetZ * Math.sin(pitchRad);
        double rotatedGunOffsetZ = gunOffsetY * Math.sin(pitchRad) + gunOffsetZ * Math.cos(pitchRad);

        // 左子彈
        Projectile leftProj = new Projectile(
                fX - gunOffsetX,
                fY + rotatedGunOffsetY,
                fZ + rotatedGunOffsetZ,
                currentPitch);

        // 右子彈
        Projectile rightProj = new Projectile(
                fX + gunOffsetX,
                fY + rotatedGunOffsetY,
                fZ + rotatedGunOffsetZ,
                currentPitch);

        projectiles.add(leftProj);
        projectiles.add(rightProj);

        // 將子彈加入根節點 (不能加進戰機群組，否則子彈會跟著戰機一起相對移動)
        root.getChildren().addAll(leftProj.getNode(), rightProj.getNode());
    }

    private void updateProjectiles() {
        // 這裡修復了你先前編輯器誤加註解導致的錯誤
        List<Projectile> toRemove = new ArrayList<>();
        for (Projectile p : projectiles) {
            p.update();
            if (p.isExpired()) {
                toRemove.add(p);
                root.getChildren().remove(p.getNode());
            }
        }
        projectiles.removeAll(toRemove);
    }

    // 子彈類別 (內部類別方便管理)
    class Projectile {
        private Cylinder shape;
        private double velocityX, velocityY, velocityZ;
        private int lifeTime = 0;
        private final int MAX_LIFE = 300; // 存活幀數，拉遠三倍範圍

        public Projectile(double x, double y, double z, double pitchAngle) {
            // 細長圓柱形的高速雷射
            shape = new Cylinder(1.5, 400); // 變細、大幅拉長
            shape.setTranslateX(x);
            shape.setTranslateY(y);
            shape.setTranslateZ(z);

            // 調整子彈朝向與發射角度一致
            shape.getTransforms().add(new Rotate(pitchAngle, Rotate.X_AXIS));
            shape.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

            // 設定純高對比度螢光材質 (不受光照與重力影響)
            PhongMaterial mat = new PhongMaterial(Color.web("#00FFFF")); // 亮青色
            mat.setSpecularColor(Color.WHITE);
            shape.setMaterial(mat);

            // 高速筆直飛行
            double projSpeed = 750.0; // 速度加快三倍
            double pitchRad = Math.toRadians(pitchAngle);

            this.velocityX = 0;
            this.velocityY = -projSpeed * Math.sin(pitchRad);
            this.velocityZ = projSpeed * Math.cos(pitchRad);
        }

        public void update() {
            shape.setTranslateX(shape.getTranslateX() + velocityX);
            shape.setTranslateY(shape.getTranslateY() + velocityY);
            shape.setTranslateZ(shape.getTranslateZ() + velocityZ);
            lifeTime++;
        }

        public boolean isExpired() {
            return lifeTime > MAX_LIFE;
        }

        public Cylinder getNode() {
            return shape;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}