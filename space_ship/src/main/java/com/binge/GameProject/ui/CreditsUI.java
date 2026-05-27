package com.binge.GameProject.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class CreditsUI {
    private final StackPane root = new StackPane();
    private Runnable onBack;

    public CreditsUI(Group uiRoot, int width, int height) {
        root.setPrefSize(width, height);
        root.setVisible(false);
        root.setOpacity(0.0);

        Pane background = createBackground(width, height);

        Rectangle frame = new Rectangle(850, 590);
        frame.setFill(Color.web("#050816", 0.78));
        frame.setStroke(Color.web("#66f8ff", 0.78));
        frame.setStrokeWidth(2.2);
        frame.setEffect(new DropShadow(34, Color.web("#7f5cff", 0.52)));

        VBox content = new VBox(14);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setMaxWidth(740);
        content.setPadding(new Insets(34, 52, 28, 52));

        Text title = text("Credits", 58, "#ffffff");
        DropShadow titleGlow = new DropShadow(34, Color.web("#ff5cf7", 0.8));
        titleGlow.setInput(new Glow(0.55));
        title.setEffect(titleGlow);

        content.getChildren().addAll(
                title,
                section("Project Title:", "K-pop Universe"),
                section("Development Team:", "Deacon\nIris\nRyan"),
                section("Contribution Statement:",
                        "This project was jointly designed, developed, and refined by all team members.\n"
                                + "All members contributed equally to the final production of the game."),
                section("Tools:", "JavaFX\nIntelliJ IDEA\nGitHub"),
                section("Assets:",
                        "Character images, group logos, sound effects, and music are used for educational and non-commercial course project purposes only.\n"
                                + "All rights belong to their respective owners."),
                new BackButton("BACK")
        );

        Node button = content.getChildren().get(content.getChildren().size() - 1);
        VBox.setMargin(button, new Insets(8, 0, 0, 0));
        button.setOnMouseClicked(e -> {
            if (onBack != null) {
                onBack.run();
            }
        });

        StackPane panel = new StackPane(frame, content);
        panel.setMaxSize(850, 590);
        root.getChildren().addAll(background, panel);
        uiRoot.getChildren().add(root);
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void show() {
        root.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.seconds(0.28), root);
        ft.setToValue(1.0);
        ft.play();
    }

    public void hide() {
        FadeTransition ft = new FadeTransition(Duration.seconds(0.18), root);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> root.setVisible(false));
        ft.play();
    }

    private Pane createBackground(int width, int height) {
        Pane pane = new Pane();
        pane.setPrefSize(width, height);

        Rectangle space = new Rectangle(width, height);
        space.setFill(Color.web("#02030b", 0.96));
        pane.getChildren().add(space);

        for (int i = 0; i < 86; i++) {
            double x = (i * 149) % width;
            double y = (i * 83) % height;
            double radius = 0.9 + (i % 4) * 0.45;
            Circle star = new Circle(x, y, radius, Color.web(i % 5 == 0 ? "#ffb8ff" : "#bff9ff", 0.34 + (i % 3) * 0.13));
            pane.getChildren().add(star);
        }

        for (int y = 36; y < height; y += 36) {
            Line line = new Line(0, y, width, y + 8);
            line.setStroke(Color.web("#74f9ff", 0.045));
            line.setStrokeWidth(1);
            pane.getChildren().add(line);
        }

        Rectangle horizon = new Rectangle(width, 90);
        horizon.setTranslateY(height - 120);
        horizon.setFill(Color.web("#ff4fe5", 0.05));
        pane.getChildren().add(horizon);
        return pane;
    }

    private VBox section(String heading, String body) {
        Text head = text(heading, 18, "#77f7ff");
        Text value = text(body, 17, "#f4fbff");
        value.setWrappingWidth(720);
        value.setLineSpacing(2);
        VBox box = new VBox(4, head, value);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Text text(String value, int size, String color) {
        Text text = new Text(value);
        text.setFont(new Font("Consolas", size));
        text.setFill(Color.web(color));
        text.setEffect(new DropShadow(10, Color.web("#55e8ff", 0.35)));
        return text;
    }

    private static class BackButton extends StackPane {
        BackButton(String label) {
            Rectangle bg = new Rectangle(170, 42);
            bg.setFill(Color.web("#090f26", 0.76));
            bg.setStroke(Color.web("#77f7ff"));
            bg.setStrokeWidth(2);

            Text text = new Text(label);
            text.setFont(new Font("Consolas", 19));
            text.setFill(Color.web("#ffffff"));
            text.setEffect(new DropShadow(12, Color.web("#ff5cf7", 0.55)));

            setAlignment(Pos.CENTER);
            getChildren().addAll(bg, text);
            setOnMouseEntered(e -> {
                bg.setFill(Color.web("#17214f", 0.94));
                bg.setStroke(Color.web("#ff86f7"));
                setCursor(javafx.scene.Cursor.HAND);
            });
            setOnMouseExited(e -> {
                bg.setFill(Color.web("#090f26", 0.76));
                bg.setStroke(Color.web("#77f7ff"));
            });
            setOnMousePressed(e -> {
                if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
                    com.binge.GameProject.audio.AudioSystem.getInstance().playButtonPress();
                }
            });
        }
    }
}
