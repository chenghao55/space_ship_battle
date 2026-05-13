package com.binge.GameProject.engine;

import com.binge.GameProject.model.GameObject;
import com.binge.GameProject.model.OrbitalBase;
import com.binge.GameProject.model.Planet;
import com.binge.GameProject.model.Player;
import com.binge.GameProject.model.Projectile;
import com.binge.GameProject.physics.PhysicsEngine;
import com.binge.GameProject.rendering.CameraManager;
import com.binge.GameProject.utils.ResourceManager;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import java.util.ArrayList;
import java.util.List;

// GameManager 負責管理遊戲中所有的物件 (飛船、星球等)
public class GameManager {
    // 儲存所有會動的物件 (受物理影響，例如飛船、子彈)
    private List<GameObject> dynamicObjects = new ArrayList<>();
    // 儲存所有靜止的物件 (例如提供引力的星球)
    private List<GameObject> staticObjects = new ArrayList<>();
    
    // 當前的遊戲狀態
    private GameState currentState = GameState.MAIN_MENU;
    
    public void setCurrentState(GameState state) {
        this.currentState = state;
    }
    
    // 待加入與待移除的物件佇列 (避免在迴圈中直接修改 List 導致崩潰)
    private List<GameObject> objectsToAdd = new ArrayList<>();
    
    // 將 staticObjects 設為 public getter 以便 SignalManager 讀取
    public List<GameObject> getStaticObjects() { return staticObjects; }
    
    // 物理引擎，用來計算引力與移動
    private PhysicsEngine physicsEngine = new PhysicsEngine();
    
    // 玩家的飛船
    private Player player;
    // 3D 世界的根節點 (用來把物件畫到螢幕上)
    private Group worldRoot;
    // 攝影機管理員 (供爆炸特效使用)
    private CameraManager cameraManager;

    // 建構子：初始化世界
    public GameManager(Group worldRoot, CameraManager cameraManager) {
        this.worldRoot = worldRoot;
        this.cameraManager = cameraManager;
        initWorld(); // 呼叫初始化方法
    }

    // 建立遊戲初始場景
    private void initWorld() {
        // 1. 建立玩家飛船，設定初始座標 (拉遠一倍，即 -6000)
        player = new Player(0, -6000, this);
        addGameObject(player); 

        // 2. 建立中央恆星 (無材質或單純顏色，質量大幅降低，半徑放大 5 倍為 1500 -> 縮小為 1000)
        Planet sun = new Planet(0, 0, 1000, 200000, Color.web("#ffaa00"));
        addGameObject(sun);

        // 取得資源管理員，準備載入網路圖片做為星球表面
        ResourceManager rm = ResourceManager.getInstance();

        // 3. 建立類地球行星 (公轉半徑放大 1.5 倍)
        Planet earthLike = new Planet(3600, 0, 267, 500000, Color.BLUE);
        PhongMaterial earthMat = rm.getMaterial("earth", "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cb/The_Blue_Marble_%28remastered%29.jpg/600px-The_Blue_Marble_%28remastered%29.jpg", Color.BLUE);
        ((javafx.scene.shape.Sphere)earthLike.getView()).setMaterial(earthMat);
        addGameObject(earthLike);

        // 4. 建立類火星行星 (公轉半徑放大 1.5 倍)
        Planet marsLike = new Planet(-3000, 3000, 200, 300000, Color.RED);
        PhongMaterial marsMat = rm.getMaterial("mars", "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/OSIRIS_Mars_true_color.jpg/600px-OSIRIS_Mars_true_color.jpg", Color.RED);
        ((javafx.scene.shape.Sphere)marsLike.getView()).setMaterial(marsMat);
        addGameObject(marsLike);

        // 5. 建立類月球行星 (公轉半徑放大 1.5 倍)
        Planet moonLike = new Planet(0, 5400, 133, 100000, Color.GRAY);
        PhongMaterial moonMat = rm.getMaterial("moon", "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e1/FullMoon2010.jpg/600px-FullMoon2010.jpg", Color.GRAY);
        ((javafx.scene.shape.Sphere)moonLike.getView()).setMaterial(moonMat);
        addGameObject(moonLike);

        // 6. 在某顆行星附近加上軌道據點 (相對於 EarthLike 調整)
        addGameObject(new OrbitalBase(3800, 300, this, cameraManager));
    }

    // 每幀由 GameLoop 呼叫的方法
    public void update(double dt) {
        // 安全機制：如果電腦卡頓 (dt 變很大)，限制最大為 0.05 秒避免物理爆炸
        if (dt > 0.05) dt = 0.05;

        // 如果進入結算畫面，則凍結所有背景的邏輯與物理運算
        if (currentState == GameState.MISSION_CLEAR) {
            return;
        }

        // --- 處理新增的物件 (例如剛發射的子彈) ---
        for (GameObject obj : objectsToAdd) {
            if (obj instanceof Planet || obj instanceof OrbitalBase) {
                staticObjects.add(obj);
            } else {
                dynamicObjects.add(obj);
            }
            if (obj.getView() != null) {
                worldRoot.getChildren().add(obj.getView());
            }
        }
        objectsToAdd.clear();

        // --- 處理死亡的物件 (清理垃圾) ---
        List<GameObject> deadObjects = new ArrayList<>();
        for (GameObject obj : dynamicObjects) {
            if (obj.isDead()) deadObjects.add(obj);
        }
        for (GameObject obj : deadObjects) {
            dynamicObjects.remove(obj);
            if (obj.getView() != null) {
                worldRoot.getChildren().remove(obj.getView());
            }
        }

        // 1. 邏輯更新：讓飛船接收輸入、讓星球自轉、讓子彈飛行
        for (GameObject obj : dynamicObjects) {
            obj.update(dt);
        }
        for (GameObject obj : staticObjects) {
            obj.update(dt);
        }

        // 2. 碰撞偵測 (子彈與據點)
        for (GameObject dynObj : dynamicObjects) {
            if (dynObj instanceof Projectile && !dynObj.isDead()) {
                for (GameObject statObj : staticObjects) {
                    if (statObj instanceof OrbitalBase) {
                        double dist = dynObj.getPosition().subtract(statObj.getPosition()).magnitude();
                        // 假設據點半徑為 100
                        if (dist < 100) {
                            ((OrbitalBase) statObj).takeDamage(10.0);
                            dynObj.setDead(true);
                            break;
                        }
                    }
                }
            }
        }

        // 3. 物理計算：將所有物件丟給物理引擎，計算引力並更新最終的位置
        physicsEngine.updatePhysics(dynamicObjects, staticObjects, dt);

        // 4. 探索系統：更新雷達訊號
        SignalManager.getInstance().updateSignals(player, staticObjects);
    }

    // 將物件加入佇列，留待下一幀正式加入世界中
    public void addGameObject(GameObject obj) {
        objectsToAdd.add(obj);
    }

    // 讓外部 (如 CameraManager) 可以取得玩家飛船
    public Player getPlayer() {
        return player;
    }
    
    public void resetGame() {
        for (GameObject obj : dynamicObjects) {
            if (obj.getView() != null) worldRoot.getChildren().remove(obj.getView());
        }
        for (GameObject obj : staticObjects) {
            if (obj.getView() != null) worldRoot.getChildren().remove(obj.getView());
        }
        for (GameObject obj : objectsToAdd) {
            if (obj.getView() != null) worldRoot.getChildren().remove(obj.getView());
        }
        dynamicObjects.clear();
        staticObjects.clear();
        objectsToAdd.clear();
        
        initWorld();
        currentState = GameState.PLAYING;
    }
    
    public GameState getCurrentState() {
        return currentState;
    }
    
    public Group getWorldRoot() {
        return worldRoot;
    }
}
