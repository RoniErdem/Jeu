package com.example.demo2.footgame;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ResultController {
    @FXML private Label resultLabel;

    @FXML
    public void initialize() {
        resultLabel.setText("Score : " + GameSettings.scoreJoueur + " - " + GameSettings.scoreIA);
    }

    @FXML
    void retourMenu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/footgame/menu.fxml"));
            Stage stage = (Stage) resultLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
