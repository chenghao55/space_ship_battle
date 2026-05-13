package com.binge.GameProject.physics;

// Vector2D 是一個用來代表 2D 向量的數學工具類別
// 在遊戲中，位置、速度、加速度都是具有「方向」與「大小」的向量
public class Vector2D {
    // 向量的 X 軸與 Y 軸數值
    public double x;
    public double y;

    // 建構子：可以指定 X 和 Y 的初始值
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // 無參數建構子：預設建立一個 (0, 0) 的向量
    public Vector2D() {
        this(0, 0);
    }

    // 向量加法 (回傳一個新的向量，不改變原本的)
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    // 向量減法 (回傳一個新的向量)
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }

    // 向量乘上一個純量 (例如：將速度放大兩倍)
    public Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    // 向量除以一個純量
    public Vector2D divide(double scalar) {
        if (scalar == 0) return new Vector2D(); // 避免除以零的錯誤
        return new Vector2D(x / scalar, y / scalar);
    }

    // 直接改變自己本身的數值 (Mutating 方法)，用來提升效能，避免一直創造新的物件
    public void addMut(Vector2D other) {
        this.x += other.x;
        this.y += other.y;
    }

    // 改變自己：減法
    public void subtractMut(Vector2D other) {
        this.x -= other.x;
        this.y -= other.y;
    }

    // 改變自己：乘法
    public void multiplyMut(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
    }

    // 直接設定新的 X 和 Y 值
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // 複製另一個向量的值
    public void set(Vector2D v) {
        this.x = v.x;
        this.y = v.y;
    }

    // 取得向量的總長度 (利用畢氏定理：長度 = 根號(x平方 + y平方))
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    // 取得長度的平方 (比計算根號快很多，常被用來比較距離大小)
    public double magnitudeSquared() {
        return x * x + y * y;
    }

    // 將向量「正規化」(保留方向，但把長度縮短或拉長為 1)
    // 這在計算方向性時非常重要 (例如求出飛船指向哪個方向的 1 單位力道)
    public Vector2D normalize() {
        double mag = magnitude();
        return mag == 0 ? new Vector2D() : new Vector2D(x / mag, y / mag);
    }

    // 計算自己與另一個向量之間的直線距離
    public double distance(Vector2D other) {
        return this.subtract(other).magnitude();
    }
    
    // 計算內積 (Dot Product)：可以用來求投影或判斷兩個向量是否夾角小於 90 度
    public double dot(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }
}
