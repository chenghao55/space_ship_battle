package com.binge.GameProject.ui;

import com.binge.GameProject.audio.AudioSystem;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class SciFiSlider extends StackPane {
    private double width;
    private double height;
    private double value = 1.0;
    
    private Rectangle bg;
    private Rectangle activeFill;
    private Text text;
    private String labelPrefix;
    
    private java.util.function.Consumer<Double> onValueChange;

    public SciFiSlider(String labelPrefix, double width, double height, double initialValue) {
        this.labelPrefix = labelPrefix;
        this.width = width;
        this.height = height;
        this.value = initialValue;
        
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
        
        // 1. Semi-transparent background
        bg = new Rectangle(width, height);
        bg.setFill(Color.web("#001122", 0.5));
        bg.setStroke(Color.web("#005588"));
        bg.setStrokeWidth(2);
        
        // 2. Active filled part
        activeFill = new Rectangle(width - 4, height - 4);
        activeFill.setFill(Color.web("#005588", 0.6));
        activeFill.setTranslateX(2); // Padding offset
        
        // 3. Text label overlay (centered)
        text = new Text();
        text.setFont(new Font("Consolas", 18));
        text.setFill(Color.web("#00ffff"));
        
        // Create a wrapper pane for centered text
        StackPane textWrapper = new StackPane(text);
        textWrapper.setPrefSize(width, height);
        textWrapper.setAlignment(Pos.CENTER);
        
        // Add components to the StackPane
        getChildren().addAll(bg, activeFill, textWrapper);
        
        // Align children explicitly
        StackPane.setAlignment(bg, Pos.CENTER_LEFT);
        StackPane.setAlignment(activeFill, Pos.CENTER_LEFT);
        StackPane.setAlignment(textWrapper, Pos.CENTER);
        
        // Set initial value visual representation
        setValue(value);
        
        // Hover effects
        DropShadow hoverGlow = new DropShadow(20, Color.web("#00ffff"));
        
        setOnMouseEntered(e -> {
            bg.setStroke(Color.web("#00ffff"));
            bg.setEffect(hoverGlow);
            setCursor(javafx.scene.Cursor.HAND);
            if (AudioSystem.getInstance() != null) {
                AudioSystem.getInstance().playSwitch();
            }
        });
        
        setOnMouseExited(e -> {
            bg.setStroke(Color.web("#005588"));
            bg.setEffect(null);
        });
        
        // Mouse drag and press events
        setOnMousePressed(e -> {
            updateValue(e.getX());
            if (AudioSystem.getInstance() != null) {
                AudioSystem.getInstance().playButtonPress();
            }
        });
        
        setOnMouseDragged(e -> {
            updateValue(e.getX());
        });
        
        // Mouse scroll wheel support (increase/decrease by 5% per tick)
        setOnScroll((ScrollEvent e) -> {
            double change = e.getDeltaY() > 0 ? 0.05 : -0.05;
            double newVal = Math.max(0.0, Math.min(1.0, value + change));
            setValue(newVal);
            if (onValueChange != null) {
                onValueChange.accept(newVal);
            }
            e.consume();
        });
    }

    private void updateValue(double mouseX) {
        double val = mouseX / width;
        val = Math.max(0.0, Math.min(1.0, val));
        setValue(val);
        if (onValueChange != null) {
            onValueChange.accept(val);
        }
    }

    public void setValue(double val) {
        this.value = val;
        // Prevent negative or zero width rendering bugs
        activeFill.setWidth(Math.max(0.1, (width - 4) * val));
        text.setText(labelPrefix + (int)(val * 100) + "%");
    }

    public double getValue() {
        return value;
    }

    public void setOnValueChange(java.util.function.Consumer<Double> callback) {
        this.onValueChange = callback;
    }
}
