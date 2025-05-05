package com.example.demo2.footgame;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.IOException;

public class MenuController {

    @FXML private AnchorPane root;
    @FXML private ImageView bgImage;
    @FXML private ImageView logo; // Pour l'intro
    @FXML private VBox menuBox;
    @FXML private Button btnJouer, btnPack, /* btnOptions,*/ btnQuitter; // btnOptions commenté/supprimé
    // Réintroduire les ImageView des boutons
    @FXML private ImageView imgCoupDenvoi, imgPack, /* imgOptions,*/ imgQuitter; // imgOptions commenté/supprimé
    @FXML private Label creditsLabel;

    @FXML
    public void initialize() {
        // Charger fond + images des boutons
        setImage(bgImage, "/footgame/fifaback.jpg");
        setImage(imgCoupDenvoi, "/footgame/btn-coup-denvoi.png");
        setImage(imgPack, "/footgame/btn-pack.png");
        // setImage(imgOptions, "/footgame/btn-options.png"); // <-- SUPPRIMÉ
        setImage(imgQuitter, "/footgame/btn-quitter.png");

        logo.setVisible(false);
        menuBox.setVisible(true);

        // Réintroduire l'effet de survol simple (opacité)
        setupHover(btnJouer);
        setupHover(btnPack);
        // setupHover(btnOptions); // <-- SUPPRIMÉ
        setupHover(btnQuitter);

        if (bgImage != null && root != null) { // Ajout vérif nullité bgImage
            bgImage.fitWidthProperty().bind(root.widthProperty());
            bgImage.fitHeightProperty().bind(root.heightProperty());
        } else if(bgImage == null) {
            System.err.println("Erreur: bgImage non injecté !");
        }
        updateCreditsDisplay();
    }

    // Effet de survol simple (opacité)
    private void setupHover(Button btn) {
        if (btn != null) { // Ajout vérif nullité bouton
            btn.setOnMouseEntered(e -> btn.setOpacity(0.75));
            btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        } else {
            System.err.println("Erreur: Tentative de setupHover sur un bouton null.");
        }
    }

    // Méthode setImage (inchangée)
    private void setImage(ImageView view, String path) {
        if (view == null) {
            System.err.println("Erreur: ImageView est null pour le chemin: " + path);
            return;
        }
        InputStream is = getClass().getResourceAsStream(path);
        if (is != null) {
            try {
                view.setImage(new Image(is));
            } catch (Exception e) {
                System.err.println("Erreur chargement image: " + path);
                e.printStackTrace();
            }
        } else {
            System.err.println("Image non trouvée : " + path);
        }
    }

    // Méthode updateCreditsDisplay (inchangée)
    private void updateCreditsDisplay() {
        if (creditsLabel != null) {
            creditsLabel.setText("💰 " + GameSettings.playerCredits);
            System.out.println("Affichage crédits menu mis à jour: " + GameSettings.playerCredits);
        } else {
            System.err.println("Erreur: creditsLabel n'est pas injecté !");
        }
    }

    @FXML
    void launchTeamSelection(ActionEvent e) {
        changeScene("/footgame/team_selection.fxml", e);
    }

    @FXML
    void openPacks(ActionEvent e) {
        // Vérifie si le fichier pack.fxml existe avant de changer
        if (getClass().getResource("/footgame/pack.fxml") != null) {
            changeScene("/footgame/pack.fxml", e);
        } else {
            System.err.println("Fichier /footgame/pack.fxml non trouvé.");
            showAlert("Fonctionnalité Indisponible", "La section des packs n'est pas encore prête.");
        }
    }

    @FXML
    void exitGame(ActionEvent e) {
        Platform.exit();
    }

    // Méthode changeScene (inchangée)
    private void changeScene(String fxmlPath, ActionEvent e) {
        try {
            String correctedPath = fxmlPath.startsWith("/") ? fxmlPath : "/" + fxmlPath;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(correctedPath));
            if (loader.getLocation() == null) {
                throw new IOException("Impossible de trouver le fichier FXML: " + correctedPath);
            }
            Parent newRoot = loader.load(); // Charger le nouveau contenu
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

    // Méthode showAlert (inchangée)
    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION); // Ou ERROR
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}