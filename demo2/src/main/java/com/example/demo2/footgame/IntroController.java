package com.example.demo2.footgame;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class IntroController {
    @FXML private ImageView introLogo;

    @FXML
    public void initialize() {
        introLogo.setImage(new Image(getClass().getResourceAsStream("/footgame/ea-sports-logo.gif")));

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), introLogo);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(Duration.seconds(1.2));

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5), introLogo);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition intro = new SequentialTransition(fadeIn, hold, fadeOut);
        intro.setOnFinished(e -> loadMenu());
        intro.play();
    }

    private void loadMenu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/footgame/menu.fxml"));
            Stage stage = (Stage) introLogo.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

