package com.binge.GameProject.engine;

public enum GameState {
    MAIN_MENU,          // 主選單畫面
    STARTING_TRANSITION,// 遊戲啟動過場動畫 (鏡頭推進)
    PLAYING,            // 正常遊玩中
    BULLET_TIME,        // 最後救援後的慢動作演出
    MISSION_CLEAR,      // 任務完成結算
    GAME_OVER,          // HP 歸零
    PAUSED
}
