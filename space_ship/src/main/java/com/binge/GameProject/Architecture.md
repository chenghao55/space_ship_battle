# Orbital Hunter / Gravity Drift - 系統製作架構

本文件基於《Inspiration.md》的遊戲初步想法，規劃出適合 JavaFX 實作的 2.5D 太空物理遊戲系統架構。

## 一、 系統總覽 (System Overview)

本遊戲為基於 JavaFX 的 2.5D 遊戲（3D 視覺渲染，2D 遊玩邏輯）。為達成「宇宙漂移感」與「引力彈弓」等核心體驗，系統分為以下五大模組：

1. **核心引擎 (Core Engine)**：負責 Game Loop (遊戲主迴圈) 與實體生命週期管理。
2. **物理與運動系統 (Physics & Movement System)**：處理飛船慣性、推力、行星引力計算。
3. **渲染與鏡頭系統 (Rendering & Camera System)**：處理 JavaFX 3D Node 渲染、第三人稱追尾鏡頭與光影特效。
4. **遊戲邏輯與實體 (Game Logic & Entities)**：敵機生成、特殊行星效果、據點邏輯。
5. **UI 與探索系統 (UI & Exploration System)**：訊號追蹤、雜訊干擾、HUD 儀表板顯示。

---

## 二、 核心模組架構設計

### 1. 遊戲主迴圈與狀態管理 (Game Loop & State)
* 使用 JavaFX 的 `AnimationTimer` 作為核心迴圈。
* 引入 **`GameState`** 列舉 (`MAIN_MENU`, `STARTING_TRANSITION`, `PLAYING`, `MISSION_CLEAR`) 來管理遊戲階段。
* **GameManager** 負責串接所有子系統，每幀 (Frame, 約 16ms) 依序呼叫：
  - `processInput()`: 處理玩家鍵盤/滑鼠輸入 (如 W: 推進, A/D: 轉向)。
  - `updatePhysics(dt)`: 更新所有動態實體的物理狀態 (速度、位置)。在 `MISSION_CLEAR` 時暫停。
  - `updateLogic(dt)`: 更新遊戲規則、據點爆炸序列 (Multi-stage destruction)。
  - `updateCamera()`: 更新鏡頭位置、延遲跟隨、動態 FOV、爆炸震動 (Camera Shake)。
  - `render()`: 將資料模型同步至 JavaFX 畫面物件。

### 2. 物理系統 (Physics System)
* **核心概念**：2D 向量運算 (Vector2D)。所有的位置計算皆在軌道平面 (X-Y 或 X-Z 平面) 上進行。
* 每個動態實體擁有 `position`, `velocity`, `acceleration`, `mass`。
* **引力計算與分層區域 (Layered Gravity)**：
  - **外圍區 (Outer Zone)**：輕微向心加速度，軌道微彎。
  - **漂移區 (Drift Zone)**：引力顯著，獲得大量速度，執行「引力彈弓」的核心區。
  - **危險區 / 排斥力場 (Repulsive Force Field)**：極近距離時（星球半徑 + 50 單位），不再採用切線加速。而是設定一個**絕對排斥力場**，飛船只要碰到這個邊界就會被強制阻擋在外，無法再靠近星球，且朝向星球內部飛行的速度分量會被抵消（讓飛船只能沿著力場表面滑行或飛離）。此力場效果與引力已調整減半，給予玩家更多操作空間。
  - **無硬體碰撞**：不實作傳統剛體破壞，而是用過熱懲罰、畫面震動以及排斥力場來確保玩家不會死於撞擊。
* **行星間距緊湊化**：星球的公轉軌道半徑經過調整，確保引力圈交疊適中，帶來開闊且具有漂移空間的探索環境。玩家初始位置設定在遙遠的外圍 (0, -6000)。
* **慣性與阻力**：極低的環境阻力，以確保飛船有「滑行」與「難以剎車」的手感。
* **視覺朝向 (Orientation)**：飛船機頭 (Nose) 的轉向與其物理移動方向分開。玩家的操控轉向極為靈敏（提升三倍），並透過 `Group` 容器包裝模型，確保 3D 模型水平旋轉翻正後，仍能正確地對齊物理與視覺的轉向。

### 3. 渲染與鏡頭系統 (Camera & Rendering)
* **場景結構與鏡頭層級 (Camera Transform Hierarchy)**：
  - `Root (Group)`
    - `Environment`: 多層背景天空盒 (具備上下漸層防迷失)、軌道平面星塵、微弱的 AmbientLight。
    - `World`: 包含行星、飛船、據點、特效 (3D Transforms)。
      - `PlayerNode (Group)`: 飛船本體。
      - `CameraPivot (Group)`: 負責平滑追蹤 Player 的位置與旋轉 (Lagging/Smoothing)。
        - `CameraPitchRig (Group)`: 固定向下傾斜 (Pitch)，將飛船置於畫面中下。
          - `CameraRollRig (Group)`: 動態轉彎傾斜 (Roll)。
            - `Camera (PerspectiveCamera)`: 沿 -Z 軸後退，實現追尾視角。
    - `UI (Overlay)`: 保持在螢幕前方的 2D Canvas，繪製 HUD。
* **鏡頭更新邏輯**：在主迴圈後段，使用 Lerp 進行位置插值 (快速跟隨)，使用 Slerp 進行旋轉插值 (緩慢跟隨，營造漂移感)；並根據飛船速度動態調整 `PerspectiveCamera` 的 FOV (如 60度 -> 90度)。
* **光影設計**：中央放置 `PointLight` 作為唯一主要光源（恆星），使行星產生強烈的亮面與黑暗面，創造「陰影區可躲藏」的 Gameplay。
* **視覺特效與武器**：使用 2D 圖片搭配 Billboard 技術（永遠朝向相機）與 Additive Blending 實作尾焰、爆炸；子彈採用高對比純色的 3D 小球形體，並從飛機機頭前方 80 單位處精準發射，確保射擊方向與機頭視覺完全吻合。

---

## 三、 類別階層設計 (Class Hierarchy)

### 1. 實體基底
```java
abstract class GameObject {
    Vector2D position;
    Vector2D velocity;
    Node view; // 對應的 JavaFX 3D 節點
    
    abstract void update(double dt);
    abstract void render();
}
```

### 2. 實體繼承樹
* **GameObject**
  * **DynamicObject** (受物理、慣性、引力影響)
    * `PlayerShip` (玩家：接收 Input，產生 Thrust)
    * `EnemyShip` (敵人：基礎 AI，追蹤玩家)
    * `Projectile` (子彈：高速直線或受微弱引力彎曲)
  * **StaticObject** (靜止或僅自轉，提供引力或環境效果)
    * `Planet` (行星：具備質量、半徑、特殊環境屬性，如過熱、干擾)
    * `Star` (恆星：發光體)
    * `OrbitalBase` (敵方軌道據點：生成敵人、發射訊號)

### 3. 系統管理員 (Managers)
* `EntityManager`: 統一管理 GameObject 的增刪改查與碰撞偵測。
* `InputManager`: 記錄按鍵狀態供 PlayerShip 讀取。
* `SignalManager`: 根據玩家方位與目標據點（考慮行星遮蔽），計算訊號強度與雜訊波形。

---

## 四、 專案目錄結構 (Project Structure)

建議的 Package 劃分方式：

```text
src/
└── com/binge/GameProject/
    ├── Main.java             // 程式進入點，初始化 Stage 與 Scene
    ├── engine/               // 核心引擎層
    │   ├── GameLoop.java     // AnimationTimer 實作
    │   ├── GameManager.java  // 系統協調與狀態管理
    │   └── InputManager.java // 鍵盤滑鼠事件處理
    ├── model/                // 遊戲資料與實體
    │   ├── GameObject.java   
    │   ├── Player.java       
    │   ├── Planet.java       
    │   └── OrbitalBase.java  
    ├── physics/              // 物理與數學計算
    │   ├── Vector2D.java     // 基礎向量工具
    │   └── PhysicsEngine.java// 引力疊加、邊界與碰撞判定
    ├── rendering/            // 畫面與鏡頭控制
    │   ├── CameraManager.java// 第三人稱視角、動態 FOV、畫面震動
    │   ├── LightSystem.java  // 光源與陰影設定
    │   ├── ParticleRenderer.java // 假粒子(Billboard)渲染
    │   └── ExplosionEffect.java  // 處理據點多階段毀滅特效與震波
    ├── ui/                   // 使用者介面
    │   ├── HUDManager.java   // 繪製訊號波形、速度表、雷達
    │   ├── MissionResultUI.java // 任務結算畫面 (提供 Retry 與 Return to Menu 重置功能)
    │   └── MainMenuUI.java   // 電影級科幻主選單 (具備待機攝影機與推進過場動畫)
    └── utils/                // 工具與資源
        └── ResourceManager.java // 3D模型(.obj)、貼圖載入與快取
```

---

## 五、 資料流與系統互動 (Data Flow)

一個完整 Frame 的資料流動順序：
1. **Input**: `InputManager` 收集玩家按鍵操作 (W: 加速, A/D: 旋轉機頭)。
2. **Player Logic**: `Player` 處理推力與 Boost 爆發加速 (Shift)。飛船的「3D 視覺朝向」會透過平滑插值 (Slerp) 對齊「當前移動的速度向量 (Velocity Vector)」，展現重力滑行感。
3. **Physics**: `PhysicsEngine` 讀取 `Player` 與各 `Planet` 座標，套用引力公式，疊加出最終加速度，並更新速度與位置。
4. **Collision**: 檢查飛船是否撞擊行星或被敵方子彈擊中。
5. **Exploration & UI**: `SignalManager` 依據位置/角度/遮蔽物更新訊號。`HUDManager` 在畫面左下角繪製未來感「圓形動態雷達 (Circular Radar)」，展示掃描脈衝、方向指示與雜訊 (Glitch)。
6. **Camera**: `CameraManager` 讀取 `Player` 的位置、旋轉與速度。執行平滑插值 (Lerp/Slerp) 更新 `CameraPivot`；依轉向角速度更新 `CameraRollRig` 的傾斜；依飛行速度比例動態放大 `Camera` 的 FOV 以增強速度感。
7. **Render & UI**: 遍歷所有 `GameObject`，將 2D 邏輯座標映射至 JavaFX `Node` 的 3D Translate 屬性，並更新 HUD 的訊號圖形。

---

## 六、 建議的開發階段規劃 (Development Phases)

### Phase 1: 基礎核心與 2D 物理 (Foundation)
* 建立專案、設置 JavaFX 3D 場景空間。
* 實作 Vector2D 與沒有引力的基礎飛行器 (推進、轉向、慣性滑行)。
* **目標**：確認「宇宙滑行」的手感正確（飛船不會立刻停止，操作以推力為主）。

### Phase 2: 引力與天體系統 (Gravity & Planets)
* 加入靜態 Planet，實作牛頓萬有引力邏輯。
* 調整引力常數與飛船推力比例。
* **目標**：玩家可以順利做出「引力彈弓」(靠近星球加速並被甩出) 與「重力漂移」。

### Phase 3: 3D 視覺與鏡頭 (Visuals & Camera)
* 將簡單的幾何形狀替換為 Low-poly 模型 (或基礎的 Sphere/Box 加上貼圖)。
* 設置 `PerspectiveCamera` 實現第三人稱追尾視角。
* 佈置中央恆星 PointLight，產生亮面與陰影區。

### Phase 4: 探索系統與戰鬥 (Exploration & Combat)
* 實作 `OrbitalBase` 的訊號發送，並在 HUD 實作訊號強弱與掃描機制。
* 加入射擊機制與會生成的 `EnemyShip`。
* 實作特殊行星效果（如火星過熱、冰星增加滑行感）。

### Phase 5: 打磨與體驗強化 (Polish)
* 加入 Billboard 粒子特效 (尾焰、爆炸)。
* 加入星塵與背景 Parallax 效果，增強空間感與速度感。
* UI 美化與音效整合。
