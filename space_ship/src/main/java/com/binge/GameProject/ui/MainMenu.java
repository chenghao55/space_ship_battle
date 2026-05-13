package com.binge.GameProject.ui;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

// MainMenu 負責處理主選單的畫面顯示 (目前保留給未來擴充)
public class MainMenu {
    // 顯示遊戲標題的方法
    public static void showTitle(Group uiRoot) {
        // 建立標題文字
        Text title = new Text("ORBITAL HUNTER");
        title.setFill(Color.WHITE); // 白色
        title.setFont(new Font("Consolas", 64)); // 超大字體
        title.setTranslateX(400); // 放在畫面中間
        title.setTranslateY(300);
        
        // TODO: 未來可以加上「開始遊戲」、「離開遊戲」等按鈕與點擊邏輯
        // 目前初版直接跳過選單，進入遊戲實機畫面
    }
}
