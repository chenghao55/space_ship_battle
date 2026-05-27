# Kpop Universe - Architecture.md

> 本文件是給開發者使用的 Class-Based 架構設計規格書。  
> 目標：從最一開始的 `Inspiration.mdc` 與既有 `Architecture.md` 出發，保留太空漂移、行星引力、鏡頭、UI 架構，並新增 Kpop Universe 的救援玩法、女團成員群集、HP 評級、永久流失、Ending Freeze、雙層雷達與音效防重疊。本版恢復飛船受行星引力與重力彈弓影響。

---

## 二、系統總覽

```text
Main
 ↓
GameManager
 ├── InputManager
 ├── MissionManager
 ├── PhysicsEngine
 ├── RescueManager
 ├── CombatManager
 ├── ScoreManager
 ├── RadarSystem
 ├── AudioSystem
 ├── CameraManager
 ├── HUDManager / MainMenuUI / MissionResultUI / CreditsUI
 └── EntityManager
```

遊戲核心資料流：

```text
玩家輸入
 ↓
Player 推力 / Boost / 甩尾
 ↓
PhysicsEngine 行星引力 / 重力彈弓 / 星體碰撞推出 / 邊界阻力
 ↓
RescueManager 偵測與救援 Idol
 ↓
CombatManager 敵人、子彈、受傷、敵人死亡
 ↓
ScoreManager 救援比例、HP、評級
 ↓
RadarSystem 內圈危險 / 外圈 Idol 脈衝
 ↓
AudioSystem 同團最近音源 / 多團混音 / 環境音淡出
 ↓
HUD / Menu / Result / Credits UI 更新
 ↓
CameraManager 停格演出、震動、FOV
 ↓
Render JavaFX Nodes
```

---

## 三、建議 Package 結構

```text
src/
└── com/binge/GameProject/
    ├── Main.java

    ├── engine/
    │   ├── GameLoop.java
    │   ├── GameManager.java
    │   ├── GameState.java
    │   ├── InputManager.java
    │   └── TimeScaleController.java

    ├── model/
    │   ├── GameObject.java
    │   ├── Player.java
    │   ├── Planet.java
    │   ├── Idol.java
    │   ├── IdolState.java
    │   ├── IdolGroup.java
    │   ├── RescueGroup.java
    │   ├── Enemy.java
    │   ├── MobileEnemy.java
    │   ├── EnemyView.java
    │   ├── Bullet.java
    │   ├── BulletOwner.java
    │   ├── EnemyBullet.java
    │   ├── PlayerBullet.java
    │   ├── Projectile.java
    │   └── OrbitalBase.java

    ├── physics/
    │   ├── Vector2D.java
    │   ├── PhysicsEngine.java
    │   └── Hitbox.java

    ├── gameplay/
    │   ├── MissionManager.java
    │   ├── RescueManager.java
    │   ├── CombatManager.java
    │   ├── ScoreManager.java
    │   ├── ScoreResult.java
    │   └── LevelManager.java

    ├── rendering/
    │   ├── CameraManager.java
    │   ├── EnemyDeathExplosion.java
    │   ├── ExplosionEffect.java
    │   ├── IdolBillboardView.java
    │   ├── LightSystem.java
    │   └── ParticleRenderer.java

    ├── ui/
    │   ├── HUDManager.java
    │   ├── RadarSystem.java
    │   ├── MissionResultUI.java
    │   ├── CreditsUI.java
    │   ├── ResultSequenceManager.java
    │   ├── MainMenu.java
    │   ├── MainMenuUI.java
    │   ├── PauseUI.java
    │   └── SciFiSlider.java

    ├── audio/
    │   ├── AudioSystem.java
    │   ├── SoundSource.java
    │   ├── IdolVoiceController.java
    │   └── MusicMixer.java

    └── utils/
        ├── ResourceManager.java
        ├── TextureRegistry.java
        ├── GroupConfig.java
        ├── OBJImporter.java
        └── GameConfig.java
```

---

## 三之一、遊戲視窗、縮放與全螢幕設計 (Windowing, Scaling, and Fullscreen Design)

### 視窗基礎設定
- 遊戲預設設計解析度（Canvas Size）為 **1280 x 720**。
- 視窗設為可調整大小（`resizable = true`），以利玩家在視窗模式下自由拖拉。

### 畫面等比例縮放與置中（Aspect-Ratio Centering）
- 主場景的根節點為 `Pane`，內部包裹一個 `Group gameScaleGroup`。
- 透過 `javafx.scene.transform.Scale`，監聽 `scene` 的寬度與高度，動態計算最小的縮放比率（`finalScale = Math.min(scaleX, scaleY)`），保持 16:9 畫面長寬比。
- 根據縮放後的尺寸與實際視窗解析度的差額，動態計算置中偏移量並進行位移（`translateX`/`translateY`），自動產生 Pillarbox（左右黑邊）或 Letterbox（上下黑邊），以確保在各種螢幕尺寸下遊戲畫面不變形。

### 全螢幕模式 (Fullscreen Mode)
- **快捷鍵切換**：遊戲透過鍵盤事件過濾器監聽 `F11` 或 `Alt + Enter`，在按下時會切換 Stage 的全螢幕模式。
- **防止 ESC 鍵誤觸退出**：
  - 將 Stage 的 `fullScreenExitKeyCombination` 設定為 `KeyCombination.NO_MATCH`。這能防止 JavaFX 預設「按下 `ESC` 即退出全螢幕」的行為。
  - 此設計的必要性在於本遊戲需要以 `ESC` 鍵呼叫暫停選單，如此可確保玩家在全螢幕下按下 `ESC` 時僅會觸發暫停選單，而不會退出全螢幕。
  - 使用 `fullScreenExitHint = ""` 隱藏系統預設的「按 ESC 退出全螢幕」文字提示，使介面更加乾淨原生。

---

## 四、Class: GameManager

### 責任

掌控遊戲整體流程、GameState、勝利與失敗條件、180 秒倒數、邊界判定整合、Ending Freeze 切換。

### GameState

```java
enum GameState {
    MAIN_MENU,
    CREDITS,
    STARTING_TRANSITION,
    PLAYING,
    ENDING_FREEZE,
    BULLET_TIME, // 保留舊狀態相容；新版最後救援不再進入此狀態
    MISSION_CLEAR,
    GAME_OVER,
    PAUSED
}
```

### 主要欄位

```java
class GameManager {
    GameState state;

    double remainingTime;          // 180 秒倒數
    double normalTimeLimit = 180.0;

    InputManager inputManager;
    TimeScaleController timeScaleController;

    PhysicsEngine physicsEngine;
    MissionManager missionManager;
    RescueManager rescueManager;
    CombatManager combatManager;
    ScoreManager scoreManager;
    LevelManager levelManager;

    AudioSystem audioSystem;
    CameraManager cameraManager;
    // HUD / menu / result / credits UI are created in Main.java
}
```

### 每幀更新順序

```text
double scaledDt = dt * timeScaleController.getTimeScale();

processInput()
updateMissionTimer(dt)
updatePlayer(scaledDt)
updatePhysics(scaledDt)
updateBoundary(scaledDt)
updateRescue(scaledDt)
updateCombat(scaledDt)
updateScore(scaledDt)
updateRadarAndAudio(scaledDt)
updateCamera(scaledDt)
updateUI(dt)
render()
```

注意：

- 遊戲物理與特效受 `timeScale` 影響。
- UI 倒數與淡出可視需求使用原始 `dt` 或 `scaledDt`。
- 新版最後救援時只進入 `ENDING_FREEZE` 0.15 秒，播放 impact 後直接進結算，不再進入結算前 Bullet Time。

### 勝利與失敗條件

```text
HP <= 0 → GAME_OVER / MISSION_CLEAR，評級由 ScoreManager 依成果扣分計算，不強制 F
解救人數 = 0 且沒有擊殺或存活成果 → 通常落入 F
救到最後一名可救援 Idol → ENDING_FREEZE
ENDING_FREEZE 0.15 秒結束 → MISSION_CLEAR
時間歸零 → MISSION_CLEAR
```

### 初始玩家位置

玩家出生點應位於恆星下方的安全空域，附近不可有敵方攻擊單位。  
初始機頭朝向恆星方向，讓玩家一開始就能看到完整恆星並理解星系中心。新版起點接近偏離軌道判定範圍，但仍保持恆星完整入鏡。

---

## 五、Class: TimeScaleController

### 責任

控制正常時間；`Bullet Time` 介面僅作舊流程相容，新版結算前不啟用。

```java
class TimeScaleController {
    double timeScale = 1.0;
    double bulletTimeTimer = 0.0;

    void startBulletTime(double duration); // 2.5 sec
    void update(double dt);
    double getTimeScale();
    boolean isBulletTimeFinished();
}
```

### 規則

```text
一般遊戲：timeScale = 1.0
最後救援瞬間：進入 ENDING_FREEZE，遊戲物理與戰鬥更新暫停
持續 0.15 秒
結束後直接 fade out / 切換 → MISSION_CLEAR
```

---

## 六、Class: Player

### 責任

處理玩家飛船移動、Boost、HP、甩尾、Snake Hitbox、子彈受擊判定。

### 主要欄位

```java
class Player extends GameObject {
    int hp = 5;
    int maxHp = 5;

    double boostEnergy;
    boolean isBoosting;
    boolean forwardThrustActive;
    double minFlightSpeed; // 約等於按 W 1 秒的速度
    double maxFlightSpeed; // 約等於最低巡航後再按 W + Boost 1 秒的速度

    double headingAngle;
    double visualAngle;
    double driftIntensity;

    RescueGroup rescueGroup;
}
```

### 操作映射

```text
WASD / 方向鍵 → 移動方向
Shift → Boost
E / 滑鼠左鍵 → 射擊
```

### 甩尾物理

```text
飛船物理方向：由 velocity 決定
飛船視覺方向：平滑朝 velocity 轉動
大角度轉向：提高 driftIntensity
滑鼠移動不得影響飛船左右方向；轉向只由鍵盤方向輸入、速度方向與物理狀態決定，滑鼠左鍵僅作射擊輸入。
A / D 的鍵盤轉向角速度為上一版目前值的一半，降低瞬間旋轉量並保留太空漂移慣性。
Boost 或 driftIntensity 高：啟動尾翼殘影
Boost 的 Camera FOV 只做輕微放大，讓玩家感覺速度上升但不造成明顯畫面縮小或視野跳動。
按住 W 推進時，若速度低於最低巡航速度，物理層會把速度補到最低巡航速度。
任何狀態下若速度高於最高速度，物理層會把速度 clamp 回最高速度，避免 Boost 或重力彈弓造成無限制暴衝。
```

### Snake Hitbox

玩家受擊與救援都使用多段 Hitbox：

```text
Player 本體 Hitbox
+ RescueGroup 每位 Idol 的 Hitbox
```

### 需要提供的方法

```java
List<Hitbox> getFullBodyHitboxes();
List<Hitbox> getRescueHitboxes();
void takeDamage(int amount);
boolean isAlive();
int getHp();
double getBoostEnergy();
double getDriftIntensity();
boolean isForwardThrustActive();
void enforceFlightSpeedLimits();
```

---

## 七、Class: Idol / IdolGroup

### Class: IdolState

```java
enum IdolState {
    AVAILABLE,
    RESCUED,
    SINGING,
    LOST
}
```

### Class: Idol

```java
class Idol extends GameObject {
    String idolId;
    String groupId;
    String displayName;
    int memberIndex;
    String portraitTexturePath;
    String groupLogoTexturePath;
    String musicFile;
    int totalGroupMemberCount;

    IdolState state;

    Planet parentPlanet;

    double volume;
    double singTimer;

    boolean permanentlyLost;

    double orbitRadius;
    double orbitAngle;
    double orbitSpeed;
}
```

### 狀態轉換

```text
AVAILABLE → RESCUED：玩家 Hitbox / 救援吸引範圍接觸 Idol
RESCUED → SINGING：隨機唱歌事件
RESCUED / SINGING → LOST：玩家受擊時最後一位 Idol 流失
```

### 軌道生成規則

Idol 不可生成在行星內部。  
每位 Idol 必須以母行星為中心，使用 `orbitRadius > parentPlanet.radius` 的軌道座標更新位置：

```text
idol.x = planet.x + cos(orbitAngle) * orbitRadius
idol.y = planet.y + sin(orbitAngle) * orbitRadius
orbitAngle += orbitSpeed * dt
```

在 `AVAILABLE` 狀態時，Idol 持續環繞母行星。  
進入 `RESCUED` 後由 `RescueGroup` 接管位置，不再跟隨行星軌道。

展示版實作要求：

1. Idol 的 JavaFX View 必須改為直立式人物紙片人 / 全身立牌，不再使用 Sphere、厚重橢球、普通卡牌或 UI 卡片。
2. AVAILABLE 是唯一的救援前狀態；聲音與雷達可依距離計算，但不可控制 Idol 是否顯示。
3. 救援成功由 `Player.getRescueHitboxes()` 與 Idol 救援吸引半徑觸發。
4. AVAILABLE 狀態從開局就顯示完整人物 panel，group logo badge 只作為輔助標記。
5. 每個 Idol 使用 `IdolPic/{memberPortraitPrefix}{memberIndex}.png|jpg|jpeg` 的個別人物圖片。
6. 若個別人物圖片不存在，fallback 到該 group logo；若 group logo 不存在，使用 placeholder。
7. 同一個 `IdolGroup` 的行星使用 group logo texture，但救援單位使用各自成員人物 texture。
8. 每幀依玩家 / camera 方向更新 billboard 朝向，確保立牌永遠面向飛機螢幕畫面，人物圖正面可讀。
9. LevelManager 建立 Idol 後必須立即 `consumer.add(idol)`，GameManager 必須把 idol view 加入 worldRoot；救援 UI 不可取代世界物件本體。
10. AVAILABLE / RESCUED world paper doll 大小改為目前世界立牌的 2 倍，仍保持人物可讀性；此設定只作用於宇宙世界物件，不改變左下角 HUD 已救援牌子大小。
11. 禁用小型水平 marker / 圓點式 world rescue icon；世界中的救援單位主體必須是清楚可見的直立人物紙片立牌。
12. `IdolBillboardView` / `RescueUnitView` 必須以 JavaFX `ImageView` 顯示人物 texture，保留透明背景與原比例，並搭配淡光外框、group logo badge、唱歌 pulse 與 rescued trail；不得再使用厚 Box 卡牌本體、方形面板或單純 UI card 取代。
13. Idol view 必須參與 JavaFX 3D depth buffer，因為它是世界中的實體 / 可遮擋物件；不得使用 `DepthTest.DISABLE` 讓它永遠蓋在行星或敵人上。
14. 人物立牌世界尺寸調整後，`RescueManager` 的救援半徑、`RescueGroup` 跟隨間距與 rescue hitbox 需一起調整，讓互動範圍符合目前 2 倍大小的世界立牌；HUD portrait 尺寸由 `HUDManager` 獨立控制，不跟著改。
15. Idol 的軌道座標必須對齊世界人物立牌的中心點；`IdolBillboardView` 不可把整張立牌向上偏移到讓 orbit position 落在底部或底座。
16. GameManager 首次 flush world objects 後輸出 rescue unit debug log，若 AVAILABLE / RESCUED Idol view 為 null、未加入 worldRoot、不可見、opacity < 0.5、沒有 parentPlanet，必須印出 warning。

### 永久流失

`LOST` 狀態代表該 Idol 從本局可救援名單中永久移除。  
不可再被二次救援。

---

### Class: IdolGroup

### 責任

處理同一女團的多名 Idol，以及音樂防重疊。

```java
class IdolGroup {
    String groupId;
    String songId;
    String planetTexturePath;
    String memberPortraitPrefix;
    int memberCount;
    List<Idol> idols;

    Idol getNearestAvailableIdol(Player player);
}
```

### Audio Overlap Fix

同一女團只允許最近的一位 Idol 作為聲源：

```text
Group A 有 3 位 Idol 靠近玩家
→ 只播放最近那一位對應的 Group A 歌曲音量
```

不同女團可以同時發聲。

---

## 八、Class: RescueGroup

### 責任

管理已救援 Idol 的跟隨隊伍、Snake Hitbox、流失最後一位 Idol。

```java
class RescueGroup {
    List<Idol> rescuedIdols;
    List<Vector2D> trailPositions;

    void add(Idol idol);
    Idol removeLastAsLost();
    List<Hitbox> getHitboxes();
    int size();
}
```

### 需要執行

1. 新 Idol 加入隊伍尾端。
2. 每幀更新隊伍跟隨位置。
3. 每位 Idol 都提供 Hitbox。
4. 被擊中時最後一位 Idol 變為 `LOST`。
5. `LOST` Idol 播放流失動畫與音效後移出場景。

---

## 九、Class: Planet

### 責任

行星 3D 視覺、女團成員管理、引力彈弓、碰撞推出、外圈公轉與敵人配置。

```java
class Planet extends GameObject {
    String groupId;
    List<Idol> idols;

    double gravityRadius;
    double driftRadius;
    double repulsiveRadius;
    double gravityStrength;

    Planet orbitCenter;
    double orbitRadius;
    double orbitAngle;
    double orbitSpeed;
}
```

### 需要執行

1. 每個 Planet 對應一個女團。
2. Planet 內部持有 `List<Idol>`，LevelManager 建立每位 Idol 後必須呼叫 `planet.addIdol(idol)`。
3. 初始化時在行星軌道或周邊星塵中生成多名 Idol。
4. 對 Player 施加引力、切線加速與重力彈弓效果。
5. 玩家貼近時施加碰撞推出力場。
6. 玩家靠近危險區時通知 UI 顯示警告。
7. Bullet 撞到 Planet 時通知 CombatManager 銷毀 Bullet。

### 行星公轉規則

1. 中央恆星不公轉。
2. 其他所有行星以中央恆星為 `orbitCenter` 慢速公轉。
3. 初始化時，每顆行星相對恆星的公轉半徑必須比目前版本更大，且每顆行星之間要有明顯半徑差異。
4. 每幀更新：

```text
orbitAngle += orbitSpeed * dt
planet.x = star.x + cos(orbitAngle) * orbitRadius
planet.y = star.y + sin(orbitAngle) * orbitRadius
```

5. Idol 的軌道位置以母行星最新位置為中心，因此會跟著公轉中的行星移動。
6. Idol 軌道半徑中的「行星表面外距離」改為上一版目前距離的 1/2，也就是 `orbitRadius = planet.radius + surfaceClearance * 0.5` 的概念。

### 行星配置

`LevelManager` 目前建立 1 顆中央恆星與 8 顆女團行星：

1. 8 顆女團行星由 `GroupConfig.defaults()` 決定，依序為 nmixx、itzy、ive、aespa、twice、lesserafim、babymonster、blackpink。
2. 每顆女團行星半徑、質量、顏色、初始角度與公轉半徑皆不同，公轉半徑約為 5600 到 14000。
3. 每顆女團行星都建立一個 `IdolGroup`、多名軌道 Idol、固定 photo logo 表皮與多座 Enemy 守衛。
4. 中央恆星是暖橘色、不透明且高亮的 `Planet`。

### 行星公轉避讓

`LevelManager` 每幀檢查非恆星行星之間的距離：

1. 若兩顆行星距離低於 `planetOrbitPauseDistance`，判斷軌道位置較後方的一顆。
2. 後方行星呼叫 `setOrbitPaused(true)` 暫停公轉。
3. 暫停行星仍可自轉、保留碰撞與救援配置。
4. 當距離大於 `planetOrbitResumeDistance`，呼叫 `setOrbitPaused(false)` 恢復公轉。
5. `planetOrbitResumeDistance` 必須大於暫停距離，避免反覆抖動。

### 行星碰撞規則

1. 子彈接觸行星半徑時立即死亡，不可被排斥力場推出或反彈。
2. 玩家飛船接觸行星表面碰撞圈時扣 1 HP。
3. 玩家扣血後仍由排斥力場推出，避免卡在行星內。
4. 行星碰撞扣血需要短暫冷卻，避免單次接觸造成多次連續扣血。
5. `Bullet` 必須跳過行星 / 恆星引力與排斥力計算。
6. `PhysicsEngine` 必須使用 bullet path segment vs planet/star circle 的掃描碰撞，避免高速子彈穿透或被 `dangerRadius` 彈開。

### 重力彈弓飛行規則

1. `PhysicsEngine` 需要把行星與恆星的質量轉成飛船加速度。
2. 需要計算並套用與玩家速度方向一致的切線重力彈弓加速。
3. 飛船速度來自玩家推進、Boost、慣性、星體引力、阻力與邊界回推。
4. 行星碰撞推出仍保留：飛船進入星體碰撞圈時扣 HP，並被推回安全半徑。
5. 子彈仍維持直線飛行，撞行星或恆星立即消失。
6. 本版大幅降低重力彈弓引力和切線加速度，保留輕微牽引感，不可強吸或暴衝。

---

## 十、Class: Enemy / Bullet

### Class: Enemy

```java
class Enemy extends GameObject {
    int hp = 3;
    double aggroRadius;
    double shootCooldown;
    boolean alive;
    boolean mobile;
    boolean isDestroyed;
    boolean isDying;

    void destroy();
    void onDeathExplosionFinished();
}
```

### Enemy 規則

1. 靜態射擊型。
2. 玩家進入中距離 `aggroRadius` 後射擊。
3. 玩家子彈擊中敵人：Enemy HP -1。
4. Enemy HP <= 0：停止射擊、關閉碰撞、播放爆炸、移除。
5. 玩家本體或隊伍撞到敵人：Enemy 消滅，但 Player 受傷。
6. 敵人使用預判射擊，但攻擊頻率不可過高。
7. 預判射擊可保留少量誤差，但不可再只朝玩家當前座標慢速發射。
8. 本版調整：`aggroRadius` 縮小，`shootCooldown` 比上一版增加 1 秒，降低壓迫感。
9. 砲台型敵人數量調整為上一版目前配置的一半；展示版 8 顆女團行星合計約 12 座砲台，分散成每顆 1~2 座。
10. 新增移動型敵人，使用和砲台相同的 HP、預判射擊、受擊與爆炸邏輯。
11. 移動型敵人的核心為綠色，定位為快速攔截單位。
12. 移動型敵人的偵測 / 攻擊半徑目前為砲台型敵人射程的 6 倍；進入範圍後快速飛向玩家目前視角 / 機頭方向前方的攔截點。
13. 移動型敵人靠近玩家後維持 `preferredDistanceFromPlayer` 的前方壓迫距離，不直接貼臉；若玩家轉向或拉開距離會重新追蹤新的前方攔截點。
14. 移動型敵人射擊頻率為前一版移動型敵人的 1.5 倍，等價於射擊冷卻除以 1.5。
15. 移動型敵人數量為前一版的 4 倍，LevelManager 必須分散擺放到不同星球外側與不同角度。
14. 敵人位置必須大幅散開，不可在每顆行星旁密集擠成小團。
15. 砲台型與移動型敵人的程序化模型目前使用 `EnemyView.MODEL_SCALE = 4.0 / 3.0`，包含核心、環、砲管與 muzzle flash；碰撞半徑由 `CombatManager` 的常數管理。
16. HP <= 0 後立刻關閉射擊、碰撞與被攻擊判定，但保留死亡爆炸特效直到播放完畢。

### EnemyView

Enemy 不使用外部 OBJ。  
模型封裝為 `EnemyView` 或 `Enemy.createView()`，回傳 JavaFX `Group`。

必要組件：

1. 中央核心：`Sphere`，作為發光核心。
2. 外圈旋轉環：`Cylinder` / `Box` / 多個小 `Sphere` 排成圓形，圍繞核心旋轉。
3. 砲管：4 根 `Cylinder`，朝前、後、左、右伸出，表示 360 度射擊。
4. 不顯示警戒範圍或 Aggro Zone 視覺提示。
5. 射擊前核心短暫變亮，射擊瞬間砲口 flash。
6. 被玩家子彈擊中時模型短暫閃紅或閃白。
7. HP 歸零時播放明顯爆炸效果，包含高亮爆炸 flash、核心放大淡出、砲塔碎片外散、短暫 shock flash，再標記移除；死亡 flash 半徑必須比目前版本大 5 倍。

### EnemyDeathExplosion

在 `rendering` package 新增 `EnemyDeathExplosion`：

```java
public class EnemyDeathExplosion {
    public EnemyDeathExplosion(Vector2D position);
    public void update(double dt);
    public boolean isFinished();
    public Group getView();
}
```

流程：

1. 死亡瞬間先讓敵人模型閃白或閃紅約 0.08~0.12 秒。
2. 主爆炸 flash 快速擴張並淡出，時間約 0.25~0.4 秒。
3. 震波環平行於 X/Z 遊戲平面向外擴散。
4. 生成 8~16 個碎片，隨機速度、旋轉與生命時間，0.5~1.0 秒後淡出或縮小。
5. 主爆炸後 0.1~0.2 秒觸發 2~3 個小型二次爆光。
6. 爆炸開始呼叫 `AudioSystem.playSfx("enemy_destroy")`；二次爆炸可呼叫 `AudioSystem.playSfx("explosion_small")`。
7. 若音效檔不存在，`AudioSystem.playSfx` 不可丟出例外。

---

### Class: Bullet

```java
abstract class Bullet extends GameObject {
    double damage;
    double lifeTime;
    BulletOwner owner;
}
```

```java
enum BulletOwner {
    PLAYER,
    ENEMY
}
```

### Bullet 規則

1. 玩家子彈擊中敵人：敵人 HP -1，子彈消失。
2. 敵人子彈擊中玩家任一 Hitbox：玩家 HP -1，子彈消失。
3. 任意子彈撞到行星：播放火花，子彈消失。
4. 子彈不可反彈。
5. 子彈生命時間結束後消失。
6. 玩家子彈有效距離只比敵方 `aggroRadius` 長一些；以目前敵方射程 1650 為基準，玩家子彈基礎射程約 1800 左右，避免超遠距離安全掃射。

---

## 十一、Class: MissionManager / ScoreManager

## MissionManager

### 責任

判斷任務流程、勝利、失敗、結束停格與結算。

```java
class MissionManager {
    int totalIdolCount;
    boolean finalRescueTriggered;

    void update(double dt);
    boolean shouldStartEndingFreeze();
    boolean shouldEndMission();
}
```

### 任務完成

```text
所有未流失 Idol 都已 RESCUED
且 HP > 0
→ 觸發 ENDING_FREEZE 0.15 秒
```

如果時間歸零，直接結算。

---

## ScoreManager

### 責任

依照救援比例、仍存活在隊伍上的救援單位、擊敗敵人數量與 HP 計算總分與評級。

```java
class ScoreResult {
    int totalRescueTargetCount;
    int rescuedCount;
    int lostRescueCount;
    int aliveRescuedCount;
    int enemyKillCount;
    int playerHp;
    double rescueRate;
    double totalScore;
    String rating;
}
```

### 評級邏輯

```text
rescueScore = rescueRate * 60
aliveScore = min(aliveRescuedCount * 5, 25)
killScore = min(enemyKillCount * 3, 15)
hpPenalty = 20 if playerHp <= 0, 8 if playerHp == 1, 4 if playerHp == 2, else 0
totalScore = clamp(rescueScore + aliveScore + killScore - hpPenalty, 0, 100)
S = 95-100, A = 80-94, B = 60-79, C = 20-59, F = 0-19
```

注意：

- `totalCount` 是全地圖原始 Idol 總數。
- `lostCount` 是被永久流失的 Idol 數。
- 即使 Idol 流失，分母仍以原始總數計算，代表評級會下降。
- `rescuedCount` 表示曾經救援成功的總數，包含後來流失者。
- `aliveRescuedCount` 表示結算瞬間仍在隊伍上的救援單位。
- `enemyKillCount` 由 CombatManager 在敵人死亡爆炸觸發時累加。

---

## 十二、Class: RadarSystem

### 責任

雙層雷達邏輯與 UI 資料輸出。

### 內圈危險雷達

偵測：

- 敵人
- 敵方子彈

輸出：

```java
class RadarThreat {
    double angle;
    double distance;
    double intensity;
}
```

當敵人或子彈進入危險範圍時，內圈相對方向亮紅。

---

### 外圈 Idol 脈衝雷達

偵測：

- 最近未救援 Idol
- 必要時可忽略已 LOST Idol

輸出：

```java
class PulseData {
    double angle;
    double distance;
    double pulseSpeed;
    double amplitude;
}
```

距離越近：

```text
pulseSpeed 越快
amplitude 越大
```

### 防呆引導

若玩家長時間未偵測或救援 Idol：

```text
AudioSystem 播放提示音
RadarSystem 暫時提高脈衝強度
```

---

## 十三、HUD / Screen UI / AudioSystem

## HUD 與畫面 UI

### 責任

目前沒有獨立 `UIManager.java`；UI 由 `Main.java` 建立並串接多個具體 UI 類別。

```text
HUDManager
RadarSystem
MissionResultUI
ResultSequenceManager
MainMenuUI
PauseUI
CreditsUI
SciFiSlider
```

### 必須顯示

1. HP：滿血 5 顆心。
2. 180 秒倒數。
3. Boost 條。
4. 已救援人數 / 總人數。
5. 內圈危險雷達。
6. 外圈 Idol 脈衝雷達。
7. 聲波視覺化。
8. 警告提示。
9. Ending Freeze 與淡出。
10. 結算真實資料：救援人數、救援比例、存活救援數、流失人數、擊殺數、剩餘 HP、總分、評級。

### ResultSequenceManager

建立 `ResultSequenceManager` 或 `ResultAnimationController`，由 UI / MissionResultUI 呼叫：

```java
void start(ScoreResult scoreResult);
```

流程：

1. Result Screen 不使用假資料，只讀取 `ScoreResult`。
2. 數字依序跳動顯示：
   - `rescuedCount / totalRescueTargetCount`
   - `rescueRate`
   - `aliveRescuedCount`
   - `lostRescueCount`
   - `enemyKillCount`
   - `playerHp`
   - `totalScore`
3. 數字跳動時播放 tick 音效，但限制每 0.04~0.08 秒一次。
4. 所有數字跳完後延遲 0.3 秒顯示 Rating。
5. Rating 依 F / C / B / A / S 播放不同強度 reveal 音效。

---

## AudioSystem

### 責任

管理環境音、選單背景音樂、女團歌曲、同團最近聲源、提示音、受擊與流失語音。

```java
class AudioSystem {
    void updateAmbient(double nearestIdolVolume);
    void updateIdolGroupVoices(List<IdolGroup> groups, Player player);
    void playSfx(String id);
    void playLostVoice(Idol idol);
    void playHintSound(Vector2D direction);
    void playMenuMusic();
    void stopMenuMusic();
}
```

### 選單背景音樂 (BGM) 控制規則

1. 選單背景音樂 (BGM) 的啟動與關閉狀態由高階遊戲狀態變化（如 `Main.java` 或 `GameManager` 中的狀態改變）進行控制。
2. **啟動播放時機**：
   - 遊戲初次啟動進入主選單。
   - 玩家暫停遊戲（`pauseGame`）。
   - 從結算選單或暫停選單返回主選單（`setOnReturnToMenu`）。
3. **停止播放時機**：
   - 從主選單開始任務過渡（`setOnStartMission`）。
   - 恢復遊戲繼續遊玩（`resumeGame`）。
   - 在暫停選單中選擇重新開始任務（`setOnRestart`）。
4. **無縫過渡與防靜音設計**：
   - 為了在暫停選單與主選單之間提供無縫的音樂體驗，並避免 JavaFX `MediaPlayer` 非同步 `stop()` 和 `play()` 狀態切換時可能發生的競態條件（Race Condition）導致背景音樂靜音之問題。
   - `GameManager.resetGame()` **不得**主動停止選單音樂播放。在點擊返回主選單時，保持音樂連續播放而不執行「先停止後重新播放」的過程。

### MusicMixer

```java
class MusicMixer {
    void fadeAmbient(double targetVolume);
    void setGroupSongVolume(String groupId, double volume);
}
```

### Audio Overlap Fix

```text
for each IdolGroup:
    nearest = group.getNearestAvailableIdol(player)
    volume = calculateVolume(nearest)
    play only group.songId at this volume
```

不同女團的歌曲可同時播放。  
同一女團不允許多軌重疊。

### Resource Binding

```text
resources/photo/*.jpg → 行星女團 logo texture
resources/IdolPic/*.{png,jpg,jpeg} → 個別成員人物 portrait texture
resources/pop_musics/*.mp3|*.MP3 → 各 group 固定歌曲
```

每個 `IdolGroup` 需要保存固定 `texturePath` 與 `songPath`：

```java
class IdolGroup {
    String texturePath;
    String songPath;
}
```

目前 `songPath` 由 GroupConfig 指定，不再全部使用 `/pop_musics/supernova.mp3`。  
音量來源必須與雷達脈衝強度一致：`volume = pulseAmplitude` 或相同距離公式，確保雷達震動越強，歌曲越大聲。

### GroupConfig / TextureRegistry

`GroupConfig` 是女團資料來源真相：

```java
record GroupConfig(
    String groupId,
    String musicFile,
    String planetTexture,
    int memberCount,
    String memberPortraitPrefix
) {}
```

固定配置：

```text
nmixx        bluevalentine.MP3  nmixx.jpg        6  nmixx
itzy         cake.MP3           e.jpg            4  itzy
ive          iam.MP3            ive.jpg          2  ive
aespa        supernova.mp3      a.jpg            4  aespa
twice        thisisfor.MP3      twice.jpg        4  twice
lesserafim   spaghetti.MP3      lesserafim.jpg   6  lesserafim
babymonster  drip.MP3           babymonster.jpg  5  babymonster
blackpink    dududu.MP3         blackpink.jpg    4  blackpink
```

`TextureRegistry` 集中處理：

1. `resolvePortrait(prefix, memberIndex, logoPath)` 依序嘗試 png / jpg / jpeg。
2. 找不到人物圖時回傳 logo。
3. 找不到 logo 時回傳 placeholder image。
4. 所有圖片使用快取，缺圖不可讓遊戲崩潰。

更新規則：音量與雷達感應必須使用明顯線性關係。  
雷達未感測到 Idol 時 `volume = 0` 並 pause 該組音樂；進入感測距離後使用：

```text
volume = 1.0 - distance / radarSenseRange
```

再 clamp 到 `0..1`。

---

## 十四、從原本 Inspiration.mdc 與 Architecture.md 修改的重點

### Inspiration.mdc 需要改的內容

| 原本內容 | 修改後 |
|---|---|
| 敵方據點與摧毀核心 | 改為女團成員救援 |
| 據點訊號追蹤 | 改為 Idol 歌聲 + 外圈脈衝雷達 |
| 據點爆炸結算 | 改為最後救援停格 + 真實資料結算 |
| 單一目標 | 改為多星球、多女團、多成員 |
| 戰鬥主軸 | 改為救援主軸，敵人是干擾 |
| 得分重點 | 改為救援比例 + HP |

### Architecture.md 需要加的內容

| 原架構位置 | 新增內容 |
|---|---|
| `GameManager` | 180 秒倒數、HP 失敗、Ending Freeze |
| `Player` | 5 顆心、Snake Hitbox、隊伍受擊 |
| `Planet` | `List<Idol>`、女團節點管理 |
| `GameObject` 繼承樹 | `Idol`, `IdolGroup`, `RescueGroup`, `Bullet` |
| `PhysicsEngine` | 邊界阻力、行星排斥力、隊伍 Hitbox |
| `SignalManager` / `RadarSystem` | 雙層雷達 |
| `HUDManager` | HP、進度、頭像、聲波、結算 |
| 新增 `AudioSystem` | Ambient、K-Pop、Echo Bug 修正 |
| 新增 `ScoreManager` | 救援比例 + 存活救援數 + 擊殺數 + HP 的總分評級 |

---

# 十五、三人 GitHub 分工

以下分工以「避免同時改同一檔案」為核心。  
每個人主要修改自己的 package，跨系統透過 public methods 溝通。

---

## 成員 A：核心引擎、物理、玩家控制

### 負責範圍

A 負責把原本的太空飛行雛型變成可玩的穩定底層。

### 主要負責檔案

```text
engine/GameLoop.java
engine/GameManager.java
engine/GameState.java
engine/InputManager.java
engine/TimeScaleController.java

physics/Vector2D.java
physics/PhysicsEngine.java
physics/Hitbox.java

model/GameObject.java
model/Player.java
model/Planet.java

utils/GameConfig.java
```

### 從 Inspiration.mdc 要改的部分

1. 保留太空漂移、行星引力、重力彈弓與 Boost。
2. 把玩家目標從打據點改成救援 Idol。
3. 加入地圖固定邊界與脫離航道阻力。
4. 保留第三人稱物理移動需求，並支援 Snake Hitbox。

### 從 Architecture.md 要改的部分

1. `GameState` 新增 `ENDING_FREEZE`、`GAME_OVER`，`BULLET_TIME` 僅保留舊流程相容。
2. `GameManager` 新增 180 秒倒數與勝敗流程。
3. `Player` 新增 HP = 5、Boost、driftIntensity。
4. `PhysicsEngine` 支援 Player + RescueGroup 的多段 Hitbox。
5. `GameManager.applyBoundarySystem()` 顯示脫離航道警告並給阻力。
6. `TimeScaleController` 可保留舊介面，但新版結算前不啟用 Bullet Time。

### A 的具體任務

#### A-1 建立基礎遊戲流程

- 建立 `GameState`。
- 確保 `MAIN_MENU → PLAYING → BULLET_TIME / MISSION_CLEAR / GAME_OVER` 可切換。
- `GameLoop` 每幀呼叫 `GameManager.update(dt)`。

#### A-2 180 秒倒數

- 在 `GameManager` 或 `MissionManager` 中維護 `remainingTime`。
- 遊戲開始時設定 180 秒。
- 時間歸零時進入結算流程。

#### A-3 Player

- 實作 WASD / 方向鍵控制。
- 實作 Shift Boost。
- 實作慣性滑行。
- 實作機頭平滑朝速度方向。
- 實作 `driftIntensity`。
- 實作 HP = 5。
- 實作 `takeDamage(int amount)`。

#### A-4 Snake Hitbox 支援

- 建立 `Hitbox.java`。
- `Player.getFullBodyHitboxes()` 回傳：
  - 飛船本體 Hitbox
  - RescueGroup 的所有 Hitbox
- 提供給 B 的 `CombatManager` 與 `RescueManager` 使用。

#### A-4a Rescue Attraction

- 救援目標不必完全碰到飛船或隊伍才救援。
- 當 Idol 進入救援吸引範圍時，自動吸附並救援。
- 吸引範圍約為目前救援碰撞判定半徑的 2 倍。
- 吸引發生時直接走 `AVAILABLE → RESCUED` 流程，並加入 `RescueGroup`。

#### A-5 行星與邊界

- 實作行星引力與重力彈弓加速。
- 實作排斥力場。
- 實作非恆星行星慢速公轉，且每顆公轉半徑都比目前版本更大並有明顯差異。
- 實作子彈撞行星的碰撞查詢接口。
- 實作 `GameManager.applyBoundarySystem()`。

#### A-6 Ending Freeze

- 建立 `TimeScaleController`。
- 最後救援時進入 `ENDING_FREEZE` 0.15 秒。
- 停格結束後通知 `GameManager` 切換到結算。

### A 需要提供給 B / C 的接口

```java
Player getPlayer();
List<Planet> getPlanets();
double getRemainingTime();
GameState getGameState();
double getTimeScale();
boolean isPlayerOutOfBounds();
double getPlayerSpeed();
double getPlayerDriftIntensity();
```

---

## 成員 B：玩法邏輯、Idol、敵人、評分

### 負責範圍

B 負責遊戲「玩什麼」：Idol 生成、救援、流失、敵人、子彈、任務完成與評級。

### 主要負責檔案

```text
gameplay/MissionManager.java
gameplay/RescueManager.java
gameplay/CombatManager.java
gameplay/ScoreManager.java
gameplay/ScoreResult.java
gameplay/LevelManager.java

model/Idol.java
model/IdolState.java
model/IdolGroup.java
model/RescueGroup.java
model/Enemy.java
model/Bullet.java
model/EnemyBullet.java
model/PlayerBullet.java
```

### 從 Inspiration.mdc 要改的部分

1. 原本的敵方據點目標改為多名 Idol 救援。
2. 一顆星球對應一個女團。
3. 每顆星球生成多名成員。
4. 戰鬥不再是主要勝利條件，而是阻礙救援的危險因素。

### 從 Architecture.md 要改的部分

1. `OrbitalBase` 可不刪，但展示版不再作為主要目標。
2. 新增 `Idol`, `IdolGroup`, `RescueGroup`。
3. 新增 `MissionManager`, `ScoreManager`, `ScoreResult`。
4. 新增 `CombatManager` 處理 Enemy / Bullet。
5. `Projectile` 拆成或擴充為 `PlayerBullet` 與 `EnemyBullet`。

### B 的具體任務

#### B-1 LevelManager

- 生成多顆行星。
- 建立 8 顆女團行星；它們半徑、質量、顏色、初始角度與公轉半徑彼此不同，並由 `GroupConfig.defaults()` 綁定女團資料。
- 所有行星初始角度必須盡量均勻分散環繞恆星，避免隨機靠太近或集中在同一側。
- 每顆行星建立一個 `IdolGroup`。
- 每個 `IdolGroup` 生成多名環繞行星的 `Idol`，不得把 Idol 放在行星內部。
- 同一個 `IdolGroup` 的 Planet 使用 group logo texture；該 group 的 Idol 依 memberIndex 使用對應成員人物圖。
- 展示版所有 `IdolGroup` 的音樂由 GroupConfig 固定指定，音量依雷達脈衝強度等比調整。
- 生成靜態射擊型敵人。
- 靜態砲台總量為上一版目前配置的一半；展示版 8 顆女團行星合計約 12 座砲台，使用每顆 1~2 座方式分散配置。
- 生成多個分散擺放的移動型敵人，作為追蹤壓力來源；本版數量為前一版配置的 4 倍。
- 移動型敵人數量為前一版的 4 倍，使用不同星球、不同角度與不同外側距離分散擺放。
- 所有敵人的初始位置需要大幅散開，使用更寬的角度與距離階梯。
- 記錄全地圖原始 Idol 總數。

#### B-2 Idol 狀態機

實作狀態：

```java
AVAILABLE,
RESCUED,
SINGING,
LOST
```

規則：

- 玩家任一救援 Hitbox / 吸引範圍碰到 Idol：`AVAILABLE → RESCUED`
- 已救援 Idol 隨機唱歌：`RESCUED ↔ SINGING`
- 玩家受擊且該 Idol 是最後一位：`RESCUED / SINGING → LOST`

#### B-3 IdolGroup

- 一個 `IdolGroup` 對應一個女團與一首歌。
- 管理該團所有 Idol。
- 提供 `getNearestAvailableIdol(player)` 給 C 的 `AudioSystem` 使用。
- 同團只回傳最近一位，避免 Echo Bug。

#### B-4 RescueManager

- 每幀檢查玩家與 Idol 的距離。
- 每幀檢查玩家 Snake Hitbox 是否碰到 Idol。
- 救援成功後加入 `RescueGroup`。
- 玩家受擊時，讓 `RescueGroup.removeLastAsLost()`。
- LOST Idol 不可再次救援。

#### B-5 RescueGroup

- 管理已救援 Idol 隊伍。
- 更新跟隨飛船的隊伍座標。
- 回傳所有 Idol 隊伍 Hitbox。
- 最後一位 Idol 流失時移除並設為 LOST。

#### B-6 CombatManager

- 敵人有 HP，例如 3。
- 敵人有中距離 Aggro Zone。
- 玩家進入範圍後敵人預判射擊，但射擊間隔需比上一版延長 1 秒。
- 敵人射擊需預判玩家位置，並提高子彈速度與命中威脅。
- 玩家子彈打中敵人：敵人 HP -1，子彈消失。
- 敵人 HP <= 0：播放爆炸、移除敵人。
- 玩家本體或隊伍撞敵人：敵人消滅，但玩家受傷。
- 敵方子彈打中玩家任一 Hitbox：玩家受傷。
- 任意子彈撞行星或恆星：播放火花並消失，不反彈；物理層不得再對已碰撞子彈施加排斥力。

#### B-6a Star Visual / Collision

- 中央恆星屬於 `Planet` 類地形。
- 恆星材質必須是暖橘色、不透明且非常明亮，使用高亮 diffuse / specular / self illumination。
- 玩家或敵人的子彈碰到恆星半徑時，和碰到行星一樣立即消失。

#### B-7 MissionManager

- 判斷是否所有未流失 / 全部目標完成。
- 若最後一名可救援 Idol 被救到，通知 `GameManager` 進入 `ENDING_FREEZE`。
- 時間歸零或 HP 歸零時進入結算 / 失敗。

#### B-8 ScoreManager

- 計算：
  - 原始 Idol 總數
  - 已救援人數
  - LOST 人數
  - 仍存活在隊伍上的救援人數
  - 擊敗敵人數
  - 剩餘 HP
  - 救援比例
  - totalScore
  - Rating

評級規則：

```text
rescueScore = rescueRate * 60
aliveScore = min(aliveRescuedCount * 5, 25)
killScore = min(enemyKillCount * 3, 15)
hpPenalty = 20 if playerHp <= 0, 8 if playerHp == 1, 4 if playerHp == 2, else 0
totalScore = clamp(rescueScore + aliveScore + killScore - hpPenalty, 0, 100)
S = 95-100, A = 80-94, B = 60-79, C = 20-59, F = 0-19
```

### B 需要提供給 A / C 的接口

```java
List<Idol> getAllIdols();
List<Idol> getVisibleIdols();
List<Idol> getUnrescuedIdols();
List<Idol> getRescuedIdols();
List<Idol> getLostIdols();
List<IdolGroup> getIdolGroups();

int getOriginalTotalIdolCount();
int getRescuedCount();
int getLostCount();

List<Enemy> getEnemies();
List<Bullet> getBullets();

ScoreResult getScoreResult();
```

---

## 成員 C：UI、雷達、音效、鏡頭、特效

### 負責範圍

C 負責玩家看見與聽見的所有回饋：HUD、雷達、聲音、結算、停格演出、背景視差、特效。

### 主要負責檔案

```text
ui/HUDManager.java
ui/RadarSystem.java
ui/MissionResultUI.java
ui/CreditsUI.java
ui/MainMenuUI.java
ui/PauseUI.java
ui/ResultSequenceManager.java
ui/SciFiSlider.java

audio/AudioSystem.java
audio/SoundSource.java
audio/IdolVoiceController.java
audio/MusicMixer.java

rendering/CameraManager.java
rendering/EnemyDeathExplosion.java
rendering/ExplosionEffect.java
rendering/IdolBillboardView.java
rendering/LightSystem.java
rendering/ParticleRenderer.java

utils/ResourceManager.java
utils/TextureRegistry.java
```

### 從 Inspiration.mdc 要改的部分

1. 原本雷達追蹤據點，改成內圈危險 + 外圈 Idol 探測。
2. 原本任務完成爆炸，改成最後救援停格 + 淡出。
3. 保留 Boost、漂移、行星危險、鏡頭震動等回饋。
4. 加入 K-Pop 歌聲、聲波 UI、Idol 頭像。

### 從 Architecture.md 要改的部分

1. `HUDManager` 新增 HP、Progress、IdolPortrait、SoundWave。
2. `SignalManager` 或 `RadarSystem` 改為雙層雷達。
3. 新增 `AudioSystem`, `MusicMixer`, `IdolVoiceController`。
4. `CameraManager` 支援結束停格、淡出與受擊震動。
5. `ParticleRenderer` 新增尾翼、火花、救援、流失星塵。

### C 的具體任務

#### C-1 HUD / Screen UI

整合並顯示：

- HP 5 顆心。
- 180 秒倒數。
- Boost 條。
- 已救援人數 / 原始總人數。
- 內圈危險雷達。
- 外圈 Idol 脈衝雷達。
- 聲波視覺化。
- Idol 頭像列。
- HUDManager warning text。
- MissionResultUI。
- CreditsUI。

底部 Idol 頭像列規則：

```text
每 10 個已救援成員為一排
第 11 個開始往上新增第二排
第 21 個開始往上新增第三排
每張底部救援牌尺寸放大為目前 HUD 牌子尺寸的 3 倍
```

Settings panel 只保留實際可操作的 `VOLUME`、`SCREEN` 與 `BACK TO MENU`。不得顯示靜態的 `GRAPHICS: CINEMATIC` 文案，避免形成不可互動的假設定項。

#### C-1a Credits Screen

`MainMenuUI` 需要提供 `CREDITS` 按鈕，點擊後讓 `Main` / `GameManager` 切換到 `GameState.CREDITS` 並顯示獨立 `CreditsUI`。Credits 畫面使用 K-pop 太空科幻視覺：深色星空背景、淡淡星點、掃描線、霓虹發光邊框、白 / 青 / 淡藍 / 粉紫文字與輕微 glow。

Credits 內容必須顯示：

```text
Credits

Project Title:
Kpop Universe

Development Team:
Deacon
Iris
Ryan

Contribution Statement:
This project was jointly designed, developed, and refined by all team members.
All members contributed equally to the final production of the game.

Tools:
JavaFX
IntelliJ IDEA
GitHub

Assets:
Character images, group logos, sound effects, and music are used for educational and non-commercial course project purposes only.
All rights belong to their respective owners.
```

`CreditsUI` 必須提供 `BACK` 按鈕，點擊後回到 `MAIN_MENU` 並重新顯示 `MainMenuUI`。若目前狀態為 `CREDITS` 且玩家按下 ESC，也必須隱藏 Credits 並回主選單。

#### C-2 RadarSystem

內圈危險雷達：

- 讀取敵人與子彈。
- 計算相對玩家方向。
- 對應方向亮紅光。

外圈 Idol 脈衝：

- 讀取最近未救援 Idol。
- 計算距離、角度、pulseSpeed、amplitude。
- 越近越強。
- 太久找不到 Idol 時加強脈衝並要求 AudioSystem 播提示音。

#### C-3 AudioSystem

- 播放 Ambient Base Track。
- 若玩家遠離所有 Idol，Ambient 音量最大。
- 玩家靠近 Idol 時，K-Pop 歌曲變大，Ambient 自動 Fade-out。
- 不同女團可同時播放。
- 同一女團只播放最近 Idol，避免 Echo Bug。
- Idol LOST 時播放「啊啊啊啊~~~」。
- 太久找不到 Idol 時播放提示音。

#### C-4 HUD Sound Wave

- 顯示主要聲源方向。
- 顯示聲源強度。
- 若多女團同時發聲，可顯示多條波或最強波。

#### C-5 HUD Idol Portraits

- 救援成功時新增頭像。
- Idol 唱歌時頭像發光。
- Idol LOST 時頭像閃爍、變暗或消失。

#### C-6 HUD Warnings

顯示：

- 你已脫離航道！
- Boost Low
- Danger
- Incoming Bullet
- Idol Lost
- HP Low

#### C-7 MissionResultUI

結算畫面必須讀取 `ScoreResult`，顯示：

- 原始總人數。
- 成功救援人數。
- 永久流失人數。
- 剩餘 HP。
- 救援比例。
- 最終評級。
- Retry / Return to Menu。

#### C-8 Camera 與特效

- 受擊 Camera Shake。
- 靠近行星危險區震動。
- Boost 尾翼。
- 大角度 Drift 殘影。
- ENDING_FREEZE 停格 0.15 秒與 impact 音效。
- 本版刪除結算動畫前 Bullet Time，停格後直接進 Result Screen。
- Bullet 撞行星火花。
- Enemy 消滅爆炸。
- Idol 救援特效。
- Idol LOST 星塵流失。
- 停格後淡出到結算。

### C 需要提供給 A / B 的接口

```java
void showWarning(String text);
void showIdolRescued(Idol idol);
void showIdolLost(Idol idol);
void playHitShake();
void playEndingFreezeEffect();
void playRescueEffect(Idol idol);
void playSpark(Vector2D position);
void playEnemyExplosion(Vector2D position);
void updateResult(ScoreResult result);
```

---

## 十六、GitHub 協作規則

### Branch 命名

```text
feature/a-game-core
feature/a-player-physics
feature/a-boundary-bullet-time

feature/b-idol-system
feature/b-combat-system
feature/b-score-manager

feature/c-radar-ui
feature/c-audio-system
feature/c-result-ui-effects
```

### Commit 格式

```text
[A] Add bullet time state
[B] Implement idol lost state
[C] Add dual layer radar UI
```

### 避免衝突

1. `GameManager.java` 由 A 主導，B/C 要接功能時先提出需要的 public method。
2. `model/GameObject.java`、`Vector2D.java`、`Hitbox.java` 是共用底層，修改前先告知。
3. B 不直接改 UI，C 不直接改玩法狀態。
4. C 的 UI 只讀取 B 提供的資料，不自行計算任務結果。
5. 每個 Pull Request 只做一個功能。
6. 合併前至少確認遊戲能啟動。

---

## 十七、開發階段

### Phase 1：保留原飛行雛型，整理底層

- A：GameLoop、GameManager、Player、Physics。
- B：建立 Idol / IdolGroup / LevelManager 的資料雛型。
- C：建立 HUDManager 的 HP、時間、Boost 顯示。

### Phase 2：救援主玩法

- A：提供 Hitbox 與 Player position。
- B：完成 Idol 偵測、救援、RescueGroup。
- C：完成 HUDManager 的 Idol portrait 列與基本救援特效。

### Phase 3：敵人與懲罰

- A：完成 HP 與受擊接口。
- B：完成 Enemy、Bullet、CombatManager、LOST 流失。
- C：完成受擊 Camera Shake、Lost 音效與警告。

### Phase 4：雷達與音效

- A：提供玩家方向、速度、邊界狀態。
- B：提供 IdolGroup、敵人、子彈資料。
- C：完成雙層雷達、Audio Overlap Fix、HUD sound wave。

### Phase 5：結算與展示打磨

- A：完成 Ending Freeze。
- B：完成 ScoreManager 與 ScoreResult。
- C：完成 MissionResultUI、FadeTransition、尾翼、火花、星塵。

---

## 十八、MVP 最小可交付版本

必須完成：

1. 飛船可移動、可 Boost。
2. 180 秒倒數。
3. HP 5 顆心。
4. 多顆星球與多名 Idol。
5. Idol 開局可見，雷達與音樂只作距離提示。
6. 同團只播放最近 Idol 歌聲。
7. 觸碰救援。
8. RescueGroup 跟隨飛船。
9. Snake Hitbox 救援與受擊。
10. 敵人射擊。
11. 玩家受擊扣 HP。
12. 最後救援 Idol 永久 LOST。
13. 子彈撞行星消失。
14. 雙層雷達。
15. 真實資料結算評級。
16. 最後救援停格後直接結算。
