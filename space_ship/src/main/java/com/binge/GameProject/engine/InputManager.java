package com.binge.GameProject.engine;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.input.MouseButton;

// InputManager 負責記錄目前玩家按下了哪些鍵
// 使用單例模式 (Singleton Pattern) 設計，確保整個遊戲只會有一個 InputManager
public class InputManager {
    // 儲存唯一的一個實例
    private static InputManager instance;
    
    // 使用 HashSet 來儲存目前被按住的按鍵
    // Set 是一種集合，裡面的元素不會重複，非常適合用來記錄按鍵狀態
    private Set<KeyCode> activeKeys;
    private Set<MouseButton> activeMouseButtons;

    // 私有建構子，防止別人用 new InputManager() 來建立第二個實例
    private InputManager() {
        activeKeys = new HashSet<>();
        activeMouseButtons = new HashSet<>();
    }

    // 取得唯一實例的方法
    public static InputManager getInstance() {
        if (instance == null) {
            instance = new InputManager();
        }
        return instance;
    }

    // 將輸入管理器綁定到遊戲的場景 (Scene) 上
    // 這樣當玩家在視窗內按鍵盤時，我們才能收到通知
    public void attachToScene(Scene scene) {
        // 當鍵盤被按下去時，將該按鍵的代碼 (例如 W, A, D) 加進 Set 中
        scene.setOnKeyPressed(e -> activeKeys.add(e.getCode()));
        // 當鍵盤被放開時，將該按鍵的代碼從 Set 中移除
        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));
        
        // 滑鼠事件
        scene.setOnMousePressed(e -> activeMouseButtons.add(e.getButton()));
        scene.setOnMouseReleased(e -> activeMouseButtons.remove(e.getButton()));
    }

    // 檢查某個按鍵是否正被按住 (供飛船 Player.java 呼叫)
    // 傳入 "W", "A", "D" 等字串
    public boolean isPressed(String keyName) {
        try {
            // 將字串轉換為 JavaFX 的 KeyCode 列舉
            KeyCode code = KeyCode.valueOf(keyName.toUpperCase());
            // 檢查 Set 中是否包含這個按鍵
            return activeKeys.contains(code);
        } catch (IllegalArgumentException e) {
            // 如果傳入不認識的按鍵名稱，直接回傳 false
            return false;
        }
    }
    
    // 檢查滑鼠按鍵
    public boolean isMousePressed(MouseButton button) {
        return activeMouseButtons.contains(button);
    }
}
