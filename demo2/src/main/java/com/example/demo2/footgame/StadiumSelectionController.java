//package com.example.demo2.footgame;
//
//import javafx.fxml.FXML;
//import javafx.scene.control.ComboBox;
//import javafx.scene.Parent;
//import javafx.stage.Stage;
//import javafx.fxml.FXMLLoader;
//import javafx.event.ActionEvent;
//
//public class StadiumSelectionController {
//
//    @FXML private ComboBox<String> comboStadium;
//
//    @FXML
//    public void initialize() {
//        comboStadium.getItems().addAll("Camp Nou", "Santiago Bernabeu", "Old Trafford");
//        comboStadium.getSelectionModel().selectFirst();
//    }
//
//    @FXML
//    private void startMatch(ActionEvent e) {
//        GameSettings.stadium = comboStadium.getValue();
//        try {
//            Parent gameRoot = FXMLLoader.load(getClass().getResource("/footgame/game.fxml"));
//            Stage stage = (Stage) comboStadium.getScene().getWindow();
//            stage.getScene().setRoot(gameRoot);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//}
package com.example.demo2.footgame;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.application.Platform;


import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap; // Pour garder l'ordre d'insertion
import java.util.Map;

public class StadiumSelectionController {

    @FXML private AnchorPane root; // Référence à la racine si besoin
    @FXML private ImageView stadiumPreviewImage;
    @FXML private ComboBox<String> stadiumComboBox;
    @FXML private Button startMatchButton;
    @FXML private Button backButton;

    // Map pour lier nom du stade et chemin de l'image preview
    private final Map<String, String> stadiumData = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        // Définir les stades et leurs images de prévisualisation
        // Assurez-vous que ces images existent dans /resources/footgame/stadiums/
        stadiumData.put("Camp Nou", "/footgame/stadiums/camp_nou_preview.jpg"); // Exemple
        stadiumData.put("Santiago Bernabéu", "/footgame/stadiums/bernabeu_preview.jpg"); // Exemple
        stadiumData.put("Old Trafford", "/footgame/stadiums/parc_des_princes_preview.jpg"); // Exemple
        stadiumData.put("Stade Par Défaut", "/footgame/background.png"); // Utiliser le fond par défaut
        stadiumData.put("Parc des Princes", "/footgame/stadiums/parc_des_princes_preview.jpg"); // AJOUTEZ CETTE IMAGE PREVIEW
        stadiumData.put("Allianz Arena", "/footgame/stadiums/allianz_arena_preview.jpg"); // AJOUTEZ CETTE IMAGE PREVIEW

        // Remplir la ComboBox avec les noms des stades
        stadiumComboBox.getItems().addAll(stadiumData.keySet());

        // Sélectionner le premier stade par défaut et afficher sa preview
        if (!stadiumData.isEmpty()) {
            stadiumComboBox.getSelectionModel().selectFirst();
            updatePreview(); // Mettre à jour l'image initiale
        } else {
            System.err.println("Aucun stade défini dans stadiumData !");
            startMatchButton.setDisable(true); // Désactiver le bouton si aucun stade
        }
    }

    /** Met à jour l'image de prévisualisation quand un stade est sélectionné. */
    @FXML
    private void updatePreview() {
        String selectedStadiumName = stadiumComboBox.getValue();
        if (selectedStadiumName != null) {
            String imagePath = stadiumData.get(selectedStadiumName);
            setImage(stadiumPreviewImage, imagePath);
        } else {
            stadiumPreviewImage.setImage(null); // Pas de sélection, pas d'image
        }
    }

    /** Lance le match avec le stade sélectionné. */
    @FXML
    private void handleStartMatch(ActionEvent event) {
        String selectedStadium = stadiumComboBox.getValue();
        if (selectedStadium == null || selectedStadium.isEmpty()) {
            showAlert("Sélection incomplète", "Veuillez choisir un stade.");
            return;
        }

        GameSettings.stadium = selectedStadium; // Enregistrer le nom du stade
        System.out.println("Stade sélectionné: " + GameSettings.stadium);
        System.out.println("Lancement du match...");

        // Changer de scène vers game.fxml
        changeScene("/footgame/game.fxml", event);
    }

    /** Retourne à l'écran de sélection des équipes. */
    @FXML
    private void handleGoBack(ActionEvent event) {
        System.out.println("Retour à la sélection des équipes.");
        changeScene("/footgame/team_selection.fxml", event);
    }

    // --- Méthodes Utilitaires (Copier depuis autres contrôleurs ou centraliser) ---

    private void setImage(ImageView view, String path) {
        if (view == null) { System.err.println("Erreur: ImageView preview est null!"); return; }
        if (path == null || path.isEmpty()) { view.setImage(null); return; }
        try {
            String correctedPath = path.startsWith("/") ? path : "/" + path;
            InputStream is = getClass().getResourceAsStream(correctedPath);
            if (is != null) {
                view.setImage(new Image(is));
                System.out.println("Image preview chargée: " + correctedPath);
            } else {
                System.err.println("Erreur: Image preview non trouvée: " + correctedPath);
                view.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Erreur critique chargement image preview: " + path);
            e.printStackTrace();
            view.setImage(null);
        }
    }

    private void changeScene(String fxmlPath, ActionEvent e) {
        try {
            String correctedPath = fxmlPath.startsWith("/") ? fxmlPath : "/" + fxmlPath;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(correctedPath));
            if (loader.getLocation() == null) { throw new IOException("Impossible de trouver le fichier FXML: " + correctedPath); }
            Parent newRoot = loader.load();
            Scene currentScene = ((Node) e.getSource()).getScene();
            currentScene.setRoot(newRoot);
        } catch (IOException ex) { System.err.println("Erreur lors du chargement de la scène FXML: " + fxmlPath); ex.printStackTrace(); showAlert("Erreur de Navigation", "Impossible de charger l'écran demandé."); }
        catch (IllegalStateException ex) { System.err.println("Erreur d'état lors du chargement: " + ex.getMessage()); ex.printStackTrace(); showAlert("Erreur Interne", "Une erreur interne est survenue."); }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
        });
    }
}
