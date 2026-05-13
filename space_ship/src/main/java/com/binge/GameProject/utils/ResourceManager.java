package com.binge.GameProject.utils;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import java.util.HashMap;
import java.util.Map;

// ResourceManager 負責管理遊戲中載入的所有圖片和材質
// 使用單例模式，這樣在任何地方都可以輕易取得同一個資源管理員
public class ResourceManager {
    private static ResourceManager instance;
    
    // 用來儲存載入過的圖片 (避免重複下載浪費時間與記憶體)
    private Map<String, Image> textures;
    // 用來儲存建立過的 3D 材質
    private Map<String, PhongMaterial> materials;

    private ResourceManager() {
        textures = new HashMap<>();
        materials = new HashMap<>();
    }

    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    // 載入網路圖片做為材質貼圖
    public Image loadTexture(String name, String url) {
        // 如果這張圖片還沒被載入過
        if (!textures.containsKey(name)) {
            try {
                // backgroundLoading = false，表示我們要求程式等到圖片載入完再繼續
                Image img = new Image(url, false);
                // 把載入好的圖片存起來
                textures.put(name, img);
            } catch (Exception e) {
                // 如果載入失敗 (例如沒網路)，在控制台印出錯誤訊息
                System.out.println("Failed to load texture: " + url);
            }
        }
        // 回傳圖片
        return textures.get(name);
    }
    
    // 取得 3D 球體的材質
    public PhongMaterial getMaterial(String name, String url, Color fallbackColor) {
        // 如果這個材質還沒被建立過
        if (!materials.containsKey(name)) {
            // 先建立一個基礎顏色 (Fallback) 的材質，以防萬一圖片載入失敗
            PhongMaterial material = new PhongMaterial(fallbackColor);
            try {
                // 嘗試去載入網路圖片
                Image img = loadTexture(name, url);
                // 如果圖片載入成功且沒有錯誤
                if (img != null && !img.isError()) {
                    // 就把這張圖片貼到材質的表面 (DiffuseMap) 上
                    material.setDiffuseMap(img);
                }
            } catch (Exception e) {
                // 若發生異常，什麼都不做，因為已經有預設顏色 fallbackColor 了
            }
            // 存起來以便下次直接取用
            materials.put(name, material);
        }
        return materials.get(name);
    }
}
