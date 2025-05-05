package com.example.demo2.footgame;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.application.Platform; // Pour showAlert

import java.io.IOException;

public class PackController {

    @FXML private Button backButton; // Injection du bouton retour

    // Méthode appelée par le bouton "Retour au Menu"
    @FXML
    void goBackToMenu(ActionEvent event) {
        System.out.println("Retour au menu depuis la section Packs.");
        changeScene("/footgame/menu.fxml", event);
    }

    // --- Méthodes Utilitaires (copiées depuis MenuController) ---
    // Assure-toi qu'elles sont présentes ici ou dans une classe partagée

    private void changeScene(String fxmlPath, ActionEvent e) {
        try {
            String correctedPath = fxmlPath.startsWith("/") ? fxmlPath : "/" + fxmlPath;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(correctedPath));
            if (loader.getLocation() == null) {
                throw new IOException("Impossible de trouver le fichier FXML: " + correctedPath);
            }
            Parent newRoot = loader.load();
            Scene currentScene = ((Node) e.getSource()).getScene();
            currentScene.setRoot(newRoot);

        } catch (IOException ex) {
            System.err.println("Erreur lors du chargement de la scène FXML: " + fxmlPath);
            ex.printStackTrace();
            showAlert("Erreur de Navigation", "Impossible de charger l'écran demandé.");
        } catch (IllegalStateException ex) {
            System.err.println("Erreur d'état lors du chargement: " + ex.getMessage());
            ex.printStackTrace();
            showAlert("Erreur Interne", "Une erreur interne est survenue.");
        }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR); // Mis en ERROR pour les pbs de nav
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}