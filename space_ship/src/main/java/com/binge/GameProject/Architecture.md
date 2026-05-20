# Galactic Harmony - Architecture.md

> 本文件是給開發者使用的 Class-Based 架構設計規格書。  
> 目標：從最一開始的 `Inspiration.mdc` 與既有 `Architecture.md` 出發，不刪除原有太空漂移、引力、鏡頭、UI 架構，只新增 Galactic Harmony 的救援玩法、女團成員群集、HP 評級、永久流失、Bullet Time、雙層雷達與音效防重疊。

---

## 一、改版原則

### 1. 原本 Inspiration.mdc 不刪除的內容

以下設計保留：

- 2.5D 太空遊戲
- 飛船慣性與滑行
- Boost 加速
- 行星引力
- 引力彈弓
- 排斥力場
- 第三人稱追尾鏡頭
- 動態 FOV
- Camera Shake
- 雷達探索
- 科幻 HUD
- 太空背景與視差
- JavaFX 3D Node 架構

### 2. 原本 Architecture.md 不刪除的模組

以下五大模組保留：

1. Core Engine
2. Physics & Movement System
3. Rendering & Camera System
4. Game Logic & Entities
5. UI & Exploration System

新版只新增：

- Mission & Rating System
- Idol Rescue System
- RescueGroup / Snake Hitbox
- Enemy Combat System
- RadarSystem 雙層雷達
- AudioSystem 音樂淡入淡出與防 Echo Bug
- Bullet Time 結算演出

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
 ├── UIManager
 └── EntityManager
```

遊戲核心資料流：

```text
玩家輸入
 ↓
PlayerShip 推力 / Boost / 甩尾
 ↓
PhysicsEngine 行星引力 / 排斥力場 / 邊界阻力
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
UIManager 更新 HUD / 警告 / 結算
 ↓
CameraManager 慢動作、震動、FOV
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
    │   ├── DynamicObject.java
    │   ├── StaticObject.java
    │   ├── PlayerShip.java
    │   ├── Planet.java
    │   ├── Star.java
    │   ├── Idol.java
    │   ├── IdolState.java
    │   ├── IdolGroup.java
    │   ├── RescueGroup.java
    │   ├── Enemy.java
    │   ├── Bullet.java
    │   ├── EnemyBullet.java
    │   └── PlayerBullet.java

    ├── physics/
    │   ├── Vector2D.java
    │   ├── PhysicsEngine.java
    │   ├── CollisionUtil.java
    │   ├── BoundarySystem.java
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
    │   ├── LightSystem.java
    │   ├── ParticleRenderer.java
    │   ├── TrailEffect.java
    │   ├── SparkEffect.java
    │   ├── RescueEffect.java
    │   ├── BackgroundParallax.java
    │   └── FadeTransitionEffect.java

    ├── ui/
    │   ├── UIManager.java
    │   ├── HUDManager.java
    │   ├── RadarSystem.java
    │   ├── TimerUI.java
    │   ├── HeartUI.java
    │   ├── BoostUI.java
    │   ├── IdolPortraitUI.java
    │   ├── SoundWaveUI.java
    │   ├── WarningUI.java
    │   ├── ProgressUI.java
    │   ├── MissionResultUI.java
    │   └── MainMenuUI.java

    ├── audio/
    │   ├── AudioSystem.java
    │   ├── SoundSource.java
    │   ├── IdolVoiceController.java
    │   └── MusicMixer.java

    └── utils/
        ├── ResourceManager.java
        ├── MathUtil.java
        └── GameConfig.java
```

---

## 四、Class: GameManager

### 責任

掌控遊戲整體流程、GameState、勝利與失敗條件、60 秒倒數、邊界判定整合、Bullet Time 切換。

### GameState

```java
enum GameState {
    MAIN_MENU,
    STARTING_TRANSITION,
    PLAYING,
    BULLET_TIME,
    MISSION_CLEAR,
    GAME_OVER,
    PAUSED
}
```

### 主要欄位

```java
class GameManager {
    GameState state;

    double remainingTime;          // 60 秒倒數
    double normalTimeLimit = 60.0;

    InputManager inputManager;
    TimeScaleController timeScaleController;

    PhysicsEngine physicsEngine;
    MissionManager missionManager;
    RescueManager rescueManager;
    CombatManager combatManager;
    ScoreManager scoreManager;
    LevelManager levelManager;

    RadarSystem radarSystem;
    AudioSystem audioSystem;
    CameraManager cameraManager;
    UIManager uiManager;
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
- Bullet Time 時 `timeScale = 0.1`，持續 2.5 秒後進入結算。

### 勝利與失敗條件

```text
HP <= 0 → GAME_OVER / F
解救人數 = 0 且時間結束 → F
救到最後一名可救援 Idol → BULLET_TIME
Bullet Time 2.5 秒結束 → MISSION_CLEAR
時間歸零 → MISSION_CLEAR
```

---

## 五、Class: TimeScaleController

### 責任

控制正常時間與 Bullet Time。

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
最後救援瞬間：timeScale = 0.1
持續 2.5 秒
結束後 fade out → MISSION_CLEAR
```

---

## 六、Class: PlayerShip

### 責任

處理玩家飛船移動、Boost、HP、甩尾、Snake Hitbox、子彈受擊判定。

### 主要欄位

```java
class PlayerShip extends DynamicObject {
    int hp = 5;
    int maxHp = 5;

    double boostEnergy;
    boolean isBoosting;

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
Space / 滑鼠左鍵 → 射擊
```

### 甩尾物理

```text
飛船物理方向：由 velocity 決定
飛船視覺方向：平滑朝 velocity 轉動
大角度轉向：提高 driftIntensity
Boost 或 driftIntensity 高：啟動尾翼殘影
```

### Snake Hitbox

玩家受擊與救援都使用多段 Hitbox：

```text
PlayerShip 本體 Hitbox
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
```

---

## 七、Class: Idol / IdolGroup

### Class: IdolState

```java
enum IdolState {
    HIDDEN,
    DETECTED,
    RESCUED,
    SINGING,
    LOST
}
```

### Class: Idol

```java
class Idol extends StaticObject {
    String idolId;
    String groupId;
    String displayName;

    IdolState state;

    Planet parentPlanet;

    double detectRadius = 50.0;
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
HIDDEN → DETECTED：玩家進入半徑 50
DETECTED → RESCUED：玩家 Hitbox 觸碰 Idol
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

在 `HIDDEN` / `DETECTED` 狀態時，Idol 持續環繞母行星。  
進入 `RESCUED` 後由 `RescueGroup` 接管位置，不再跟隨行星軌道。

展示版實作要求：

1. Idol 的 JavaFX View 必須是可見的球形節點，未救援時也顯示在行星外側軌道上。
2. `detectRadius` 只負責聲音、雷達與 UI 提示。
3. 救援成功只能由 `PlayerShip.getRescueHitboxes()` 與 Idol 球體碰撞觸發。
4. 不能因為玩家進入偵測範圍就自動救援。
5. Idol 球體材質必須使用 `resources/photo` 的圖片貼圖。
6. 同一個 `IdolGroup` 的所有 Idol 必須使用同一張 photo 表皮，因為同一顆星球對應同一個女團。

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
    List<Idol> idols;

    Idol getNearestDetectableIdol(PlayerShip player);
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

行星 3D 視覺、女團成員管理、引力彈弓、排斥力場。

```java
class Planet extends StaticObject {
    String groupId;
    List<Idol> idols;

    double gravityRadius;
    double driftRadius;
    double repulsiveRadius;
    double gravityStrength;
}
```

### 需要執行

1. 每個 Planet 對應一個女團。
2. Planet 內部持有 `List<Idol>`。
3. 初始化時在行星軌道或周邊星塵中生成多名 Idol。
4. 對 PlayerShip 施加引力。
5. 玩家貼近時施加排斥力場。
6. 玩家靠近危險區時通知 UI 顯示警告。
7. Bullet 撞到 Planet 時通知 CombatManager 銷毀 Bullet。

### 行星碰撞規則

1. 子彈接觸行星半徑時立即死亡，不可被排斥力場推出或反彈。
2. 玩家飛船接觸行星表面碰撞圈時扣 1 HP。
3. 玩家扣血後仍由排斥力場推出，避免卡在行星內。
4. 行星碰撞扣血需要短暫冷卻，避免單次接觸造成多次連續扣血。

---

## 十、Class: Enemy / Bullet

### Class: Enemy

```java
class Enemy extends StaticObject {
    int hp = 3;
    double aggroRadius;
    double shootCooldown;
    boolean alive;
}
```

### Enemy 規則

1. 靜態射擊型。
2. 玩家進入大範圍 `aggroRadius` 後射擊。
3. 玩家子彈擊中敵人：Enemy HP -1。
4. Enemy HP <= 0：停止射擊、關閉碰撞、播放爆炸、移除。
5. 玩家本體或隊伍撞到敵人：Enemy 消滅，但 Player 受傷。
6. 敵人必須更積極：縮短 `shootCooldown`，提高子彈速度，並用玩家目前速度做預判瞄準。
7. 預判射擊可保留少量誤差，但不可再只朝玩家當前座標慢速發射。

---

### Class: Bullet

```java
abstract class Bullet extends DynamicObject {
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

---

## 十一、Class: MissionManager / ScoreManager

## MissionManager

### 責任

判斷任務流程、勝利、失敗、進入 Bullet Time 與結算。

```java
class MissionManager {
    int totalIdolCount;
    boolean finalRescueTriggered;

    void update(double dt);
    boolean shouldStartBulletTime();
    boolean shouldEndMission();
}
```

### 任務完成

```text
所有未流失 Idol 都已 RESCUED
且 HP > 0
→ 觸發 Bullet Time
```

如果時間歸零，直接結算。

---

## ScoreManager

### 責任

依照救援比例與 HP 計算評級。

```java
class ScoreResult {
    int rescuedCount;
    int totalCount;
    int lostCount;
    int remainingHp;
    double rescuedRatio;
    String rating;
}
```

### 評級邏輯

```java
if (remainingHp <= 0 || rescuedCount == 0) rating = "F";
else if (rescuedRatio == 1.0 && remainingHp == 5) rating = "S";
else if ((rescuedRatio == 1.0 && remainingHp >= 3) ||
         (rescuedRatio >= 0.8 && remainingHp >= 4)) rating = "A";
else if ((rescuedRatio >= 0.8 && remainingHp >= 1) ||
         (rescuedRatio >= 0.5 && remainingHp >= 3)) rating = "B";
else rating = "C";
```

注意：

- `totalCount` 是全地圖原始 Idol 總數。
- `lostCount` 是被永久流失的 Idol 數。
- 即使 Idol 流失，分母仍以原始總數計算，代表評級會下降。

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

## 十三、Class: UIManager / AudioSystem

## UIManager

### 責任

管理所有 UI 元件。

```text
TimerUI
HeartUI
BoostUI
RadarSystem / RadarView
SoundWaveUI
IdolPortraitUI
WarningUI
ProgressUI
MissionResultUI
MainMenuUI
```

### 必須顯示

1. HP：滿血 5 顆心。
2. 60 秒倒數。
3. Boost 條。
4. 已救援人數 / 總人數。
5. 內圈危險雷達。
6. 外圈 Idol 脈衝雷達。
7. 聲波視覺化。
8. 警告提示。
9. Bullet Time 與淡出。
10. 結算真實資料：救援人數、流失人數、剩餘 HP、評級。

---

## AudioSystem

### 責任

管理環境音、女團歌曲、同團最近聲源、提示音、受擊與流失語音。

```java
class AudioSystem {
    void updateAmbient(double nearestIdolVolume);
    void updateIdolGroupVoices(List<IdolGroup> groups, PlayerShip player);
    void playSfx(String id);
    void playLostVoice(Idol idol);
    void playHintSound(Vector2D direction);
}
```

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
    nearest = group.getNearestDetectableIdol(player)
    volume = calculateVolume(nearest)
    play only group.songId at this volume
```

不同女團的歌曲可同時播放。  
同一女團不允許多軌重疊。

### Resource Binding

```text
resources/photo/*.jpg → Idol 球形貼圖
resources/music/song.mp3 → 展示版所有 IdolGroup 的共用歌曲
```

每個 `IdolGroup` 需要保存固定 `texturePath` 與 `songPath`：

```java
class IdolGroup {
    String texturePath;
    String songPath;
}
```

目前 `songPath` 全部使用 `/music/song.mp3`。  
音量來源必須與雷達脈衝強度一致：`volume = pulseAmplitude` 或相同距離公式，確保雷達震動越強，歌曲越大聲。

---

## 十四、從原本 Inspiration.mdc 與 Architecture.md 修改的重點

### Inspiration.mdc 需要改的內容

| 原本內容 | 修改後 |
|---|---|
| 敵方據點與摧毀核心 | 改為女團成員救援 |
| 據點訊號追蹤 | 改為 Idol 歌聲 + 外圈脈衝雷達 |
| 據點爆炸結算 | 改為最後救援 Bullet Time + 結算 |
| 單一目標 | 改為多星球、多女團、多成員 |
| 戰鬥主軸 | 改為救援主軸，敵人是干擾 |
| 得分重點 | 改為救援比例 + HP |

### Architecture.md 需要加的內容

| 原架構位置 | 新增內容 |
|---|---|
| `GameManager` | 60 秒倒數、HP 失敗、Bullet Time |
| `PlayerShip` | 5 顆心、Snake Hitbox、隊伍受擊 |
| `Planet` | `List<Idol>`、女團節點管理 |
| `GameObject` 繼承樹 | `Idol`, `IdolGroup`, `RescueGroup`, `Bullet` |
| `PhysicsEngine` | 邊界阻力、行星排斥力、隊伍 Hitbox |
| `SignalManager` / `RadarSystem` | 雙層雷達 |
| `HUDManager` | HP、進度、頭像、聲波、結算 |
| 新增 `AudioSystem` | Ambient、K-Pop、Echo Bug 修正 |
| 新增 `ScoreManager` | 救援比例 + HP 評級 |

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
physics/CollisionUtil.java
physics/BoundarySystem.java
physics/Hitbox.java

model/GameObject.java
model/DynamicObject.java
model/StaticObject.java
model/PlayerShip.java
model/Planet.java

utils/GameConfig.java
```

### 從 Inspiration.mdc 要改的部分

1. 保留太空漂移、引力彈弓、Boost。
2. 把玩家目標從打據點改成救援 Idol。
3. 加入地圖固定邊界與脫離航道阻力。
4. 保留第三人稱物理移動需求，並支援 Snake Hitbox。

### 從 Architecture.md 要改的部分

1. `GameState` 新增 `BULLET_TIME`、`GAME_OVER`。
2. `GameManager` 新增 60 秒倒數與勝敗流程。
3. `PlayerShip` 新增 HP = 5、Boost、driftIntensity。
4. `PhysicsEngine` 支援 Player + RescueGroup 的多段 Hitbox。
5. `BoundarySystem` 顯示脫離航道警告並給阻力。
6. `TimeScaleController` 支援 0.1 倍 Bullet Time。

### A 的具體任務

#### A-1 建立基礎遊戲流程

- 建立 `GameState`。
- 確保 `MAIN_MENU → PLAYING → BULLET_TIME / MISSION_CLEAR / GAME_OVER` 可切換。
- `GameLoop` 每幀呼叫 `GameManager.update(dt)`。

#### A-2 60 秒倒數

- 在 `GameManager` 或 `MissionManager` 中維護 `remainingTime`。
- 遊戲開始時設定 60 秒。
- 時間歸零時進入結算流程。

#### A-3 PlayerShip

- 實作 WASD / 方向鍵控制。
- 實作 Shift Boost。
- 實作慣性滑行。
- 實作機頭平滑朝速度方向。
- 實作 `driftIntensity`。
- 實作 HP = 5。
- 實作 `takeDamage(int amount)`。

#### A-4 Snake Hitbox 支援

- 建立 `Hitbox.java`。
- `PlayerShip.getFullBodyHitboxes()` 回傳：
  - 飛船本體 Hitbox
  - RescueGroup 的所有 Hitbox
- 提供給 B 的 `CombatManager` 與 `RescueManager` 使用。

#### A-5 行星與邊界

- 實作行星引力。
- 實作排斥力場。
- 實作子彈撞行星的碰撞查詢接口。
- 實作 `BoundarySystem`。

#### A-6 Bullet Time

- 建立 `TimeScaleController`。
- 最後救援時將 `timeScale` 設為 0.1。
- 持續 2.5 秒後通知 `GameManager` 切換到結算。

### A 需要提供給 B / C 的接口

```java
PlayerShip getPlayer();
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
- 每顆行星建立一個 `IdolGroup`。
- 每個 `IdolGroup` 生成多名環繞行星的 `Idol`，不得把 Idol 放在行星內部。
- 同一個 `IdolGroup` 生成的所有 Idol 使用同一張 `photo` 貼圖。
- 展示版所有 `IdolGroup` 的音樂先固定為 `/music/song.mp3`，音量依雷達脈衝強度等比調整。
- 生成靜態射擊型敵人。
- 記錄全地圖原始 Idol 總數。

#### B-2 Idol 狀態機

實作狀態：

```java
HIDDEN,
DETECTED,
RESCUED,
SINGING,
LOST
```

規則：

- 玩家距離 <= 50：`HIDDEN → DETECTED`
- 玩家任一救援 Hitbox 碰到 Idol：`DETECTED → RESCUED`
- 已救援 Idol 隨機唱歌：`RESCUED ↔ SINGING`
- 玩家受擊且該 Idol 是最後一位：`RESCUED / SINGING → LOST`

#### B-3 IdolGroup

- 一個 `IdolGroup` 對應一個女團與一首歌。
- 管理該團所有 Idol。
- 提供 `getNearestDetectableIdol(player)` 給 C 的 `AudioSystem` 使用。
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
- 敵人有大範圍 Aggro Zone。
- 玩家進入範圍後敵人高頻率射擊。
- 敵人射擊需預判玩家位置，並提高子彈速度與命中威脅。
- 玩家子彈打中敵人：敵人 HP -1，子彈消失。
- 敵人 HP <= 0：播放爆炸、移除敵人。
- 玩家本體或隊伍撞敵人：敵人消滅，但玩家受傷。
- 敵方子彈打中玩家任一 Hitbox：玩家受傷。
- 任意子彈撞行星：播放火花並消失，不反彈；物理層不得再對已碰撞子彈施加排斥力。

#### B-7 MissionManager

- 判斷是否所有未流失 / 全部目標完成。
- 若最後一名可救援 Idol 被救到，通知 `GameManager` 進入 `BULLET_TIME`。
- 時間歸零或 HP 歸零時進入結算 / 失敗。

#### B-8 ScoreManager

- 計算：
  - 原始 Idol 總數
  - 已救援人數
  - LOST 人數
  - 剩餘 HP
  - 救援比例
  - Rating

評級規則：

```java
if (hp <= 0 || rescuedCount == 0) rating = "F";
else if (rescuedRatio == 1.0 && hp == 5) rating = "S";
else if ((rescuedRatio == 1.0 && hp >= 3) ||
         (rescuedRatio >= 0.8 && hp >= 4)) rating = "A";
else if ((rescuedRatio >= 0.8 && hp >= 1) ||
         (rescuedRatio >= 0.5 && hp >= 3)) rating = "B";
else rating = "C";
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

C 負責玩家看見與聽見的所有回饋：HUD、雷達、聲音、結算、慢動作演出、背景視差、特效。

### 主要負責檔案

```text
ui/UIManager.java
ui/HUDManager.java
ui/RadarSystem.java
ui/TimerUI.java
ui/HeartUI.java
ui/BoostUI.java
ui/IdolPortraitUI.java
ui/SoundWaveUI.java
ui/WarningUI.java
ui/ProgressUI.java
ui/MissionResultUI.java
ui/MainMenuUI.java

audio/AudioSystem.java
audio/SoundSource.java
audio/IdolVoiceController.java
audio/MusicMixer.java

rendering/CameraManager.java
rendering/LightSystem.java
rendering/ParticleRenderer.java
rendering/TrailEffect.java
rendering/SparkEffect.java
rendering/RescueEffect.java
rendering/BackgroundParallax.java
rendering/FadeTransitionEffect.java

utils/ResourceManager.java
```

### 從 Inspiration.mdc 要改的部分

1. 原本雷達追蹤據點，改成內圈危險 + 外圈 Idol 探測。
2. 原本任務完成爆炸，改成最後救援 Bullet Time + 淡出。
3. 保留 Boost、漂移、行星危險、鏡頭震動等回饋。
4. 加入 K-Pop 歌聲、聲波 UI、Idol 頭像。

### 從 Architecture.md 要改的部分

1. `HUDManager` 新增 HP、Progress、IdolPortrait、SoundWave。
2. `SignalManager` 或 `RadarSystem` 改為雙層雷達。
3. 新增 `AudioSystem`, `MusicMixer`, `IdolVoiceController`。
4. `CameraManager` 支援 Bullet Time 視覺與受擊震動。
5. `ParticleRenderer` 新增尾翼、火花、救援、流失星塵。

### C 的具體任務

#### C-1 UIManager / HUD

整合並顯示：

- HP 5 顆心。
- 60 秒倒數。
- Boost 條。
- 已救援人數 / 原始總人數。
- 內圈危險雷達。
- 外圈 Idol 脈衝雷達。
- 聲波視覺化。
- Idol 頭像列。
- WarningUI。
- MissionResultUI。

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

#### C-4 SoundWaveUI

- 顯示主要聲源方向。
- 顯示聲源強度。
- 若多女團同時發聲，可顯示多條波或最強波。

#### C-5 IdolPortraitUI

- 救援成功時新增頭像。
- Idol 唱歌時頭像發光。
- Idol LOST 時頭像閃爍、變暗或消失。

#### C-6 WarningUI

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
- Bullet 撞行星火花。
- Enemy 消滅爆炸。
- Idol 救援特效。
- Idol LOST 星塵流失。
- Bullet Time 慢動作視覺。
- 2.5 秒後淡出到結算。

### C 需要提供給 A / B 的接口

```java
void showWarning(String text);
void showIdolRescued(Idol idol);
void showIdolLost(Idol idol);
void playHitShake();
void playBulletTimeEffect();
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

- A：GameLoop、GameManager、PlayerShip、Physics。
- B：建立 Idol / IdolGroup / LevelManager 的資料雛型。
- C：建立 TimerUI、HeartUI、BoostUI。

### Phase 2：救援主玩法

- A：提供 Hitbox 與 Player position。
- B：完成 Idol 偵測、救援、RescueGroup。
- C：完成 IdolPortraitUI 與基本救援特效。

### Phase 3：敵人與懲罰

- A：完成 HP 與受擊接口。
- B：完成 Enemy、Bullet、CombatManager、LOST 流失。
- C：完成受擊 Camera Shake、Lost 音效與警告。

### Phase 4：雷達與音效

- A：提供玩家方向、速度、邊界狀態。
- B：提供 IdolGroup、敵人、子彈資料。
- C：完成雙層雷達、Audio Overlap Fix、SoundWaveUI。

### Phase 5：結算與展示打磨

- A：完成 Bullet Time。
- B：完成 ScoreManager 與 ScoreResult。
- C：完成 MissionResultUI、FadeTransition、尾翼、火花、星塵。

---

## 十八、MVP 最小可交付版本

必須完成：

1. 飛船可移動、可 Boost。
2. 60 秒倒數。
3. HP 5 顆心。
4. 多顆星球與多名 Idol。
5. Idol 半徑 50 偵測。
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
16. 最後救援 Bullet Time。
