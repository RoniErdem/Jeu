package com.example.demo2.footgame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Charge l'intro comme premier écran
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/footgame/intro.fxml"));
            if (loader.getLocation() == null) { throw new IOException("Impossible de trouver /footgame/intro.fxml"); }
            Parent initialScreen = loader.load();
            Scene scene = new Scene(initialScreen, 1280, 720); // Taille initiale, sera ajustée par plein écran
            primaryStage.setTitle("Mon Mini-Jeu de Foot");
            primaryStage.setScene(scene);

            // Appliquer le Plein Écran SANS message
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreen(true);

            primaryStage.setResizable(true); // Ou false si tu préfères bloquer la taille
            primaryStage.show();

        } catch (Exception e) { // Capture Exception plus large pour être sûr
            System.err.println("Erreur critique au démarrage : " + e.getMessage());
            e.printStackTrace(); Platform.exit();
        }
    }
    public static void main(String[] args) { launch(args); }
}