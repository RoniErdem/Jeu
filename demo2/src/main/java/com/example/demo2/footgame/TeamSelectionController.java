package com.example.demo2.footgame;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.io.InputStream; // Import pour charger images

public class TeamSelectionController {

    @FXML private AnchorPane root;
    @FXML private ImageView bgImage;
    // Les ComboBox sont supprimés du FXML
    @FXML private ImageView logoTeam1, logoTeam2;
    @FXML private Label team1Stats, team2Stats;
    @FXML private Button btnContinue;
    @FXML private Button backButton;
    // Labels et boutons pour la sélection par flèches
    @FXML private Label nameTeam1Label;
    @FXML private Label nameTeam2Label;
    @FXML private Button prevTeam1Button;
    @FXML private Button nextTeam1Button;
    @FXML private Button prevTeam2Button;
    @FXML private Button nextTeam2Button;

    // Constante pour le chemin des icônes
    private static final String TEAM_ICON_BASE_PATH = "/footgame/icons/";

    // Données des équipes
    private final String[] teams = {"PSG", "Real Madrid", "FC Barcelone", "Bayern Munich"};
    private final String[] keys  = {"psg", "real", "barca", "bayern"};
    // Stats pour chaque équipe (dans le même ordre que 'teams')
    private final int[] attackStats = {91, 89, 88, 90};
    private final int[] defenseStats = {85, 86, 84, 87};
    private final int numTeams = teams.length;

    // Index des équipes sélectionnées
    private int selectedTeam1Index = 0;
    private int selectedTeam2Index = 1; // Commencer avec des équipes différentes

    @FXML
    public void initialize() {
        // Fond
        setImage(bgImage, "/footgame/fifaback.jpg");
        if (bgImage != null && root != null) { // Vérifier nullité avant bind
            bgImage.fitWidthProperty().bind(root.widthProperty());
            bgImage.fitHeightProperty().bind(root.heightProperty());
        }

        // Initialiser l'affichage des équipes sélectionnées
        updateTeamDisplay(1);
        updateTeamDisplay(2);

        // Forcer le bouton devant (si nécessaire)
        if (btnContinue != null) {
            Platform.runLater(() -> btnContinue.toFront());
        }
    }

    // Méthode pour charger les images
    private void setImage(ImageView view, String path) {
        if (view == null) {
            System.err.println("Erreur: ImageView non injectée pour le chemin: " + path);
            return;
        }
        try {
            String correctedPath = path.startsWith("/") ? path : "/" + path;
            InputStream is = getClass().getResourceAsStream(correctedPath);
            if (is != null) {
                view.setImage(new Image(is));
                System.out.println("Image chargée: " + correctedPath);
            } else {
                System.err.println("Erreur: Image non trouvée pour " + view.getId() + " au chemin : " + correctedPath);
                view.setImage(null); // Mettre à null si non trouvée
            }
        } catch (Exception e) {
            System.err.println("Erreur critique lors du chargement de l'image: " + path);
            e.printStackTrace();
            view.setImage(null); // Mettre à null en cas d'erreur
        }
    }


    // Logique pour bouton Précédent Équipe 1
    @FXML
    void selectPrevTeam1(ActionEvent event) {
        selectedTeam1Index = (selectedTeam1Index - 1 + numTeams) % numTeams;
        // Éviter d'avoir la même équipe que l'équipe 2
        if (selectedTeam1Index == selectedTeam2Index) {
            selectedTeam1Index = (selectedTeam1Index - 1 + numTeams) % numTeams; // Reculer encore
        }
        updateTeamDisplay(1);
    }

    // Logique pour bouton Suivant Équipe 1
    @FXML
    void selectNextTeam1(ActionEvent event) {
        selectedTeam1Index = (selectedTeam1Index + 1) % numTeams;
        // Éviter d'avoir la même équipe que l'équipe 2
        if (selectedTeam1Index == selectedTeam2Index) {
            selectedTeam1Index = (selectedTeam1Index + 1) % numTeams; // Avancer encore
        }
        updateTeamDisplay(1);
    }

    // Logique pour bouton Précédent Équipe 2
    @FXML
    void selectPrevTeam2(ActionEvent event) {
        selectedTeam2Index = (selectedTeam2Index - 1 + numTeams) % numTeams;
        // Éviter d'avoir la même équipe que l'équipe 1
        if (selectedTeam2Index == selectedTeam1Index) {
            selectedTeam2Index = (selectedTeam2Index - 1 + numTeams) % numTeams;
        }
        updateTeamDisplay(2);
    }

    // Logique pour bouton Suivant Équipe 2
    @FXML
    void selectNextTeam2(ActionEvent event) {
        selectedTeam2Index = (selectedTeam2Index + 1) % numTeams;
        // Éviter d'avoir la même équipe que l'équipe 1
        if (selectedTeam2Index == selectedTeam1Index) {
            selectedTeam2Index = (selectedTeam2Index + 1) % numTeams;
        }
        updateTeamDisplay(2);
    }

    /** Met à jour l'affichage (logo, nom, stats) pour une équipe donnée. */
    private void updateTeamDisplay(int teamNumber) {
        int index;
        String teamName;
        String teamKey;
        ImageView logoView;
        Label nameLabel;
        Label statsLabel;
        int attack;
        int defense;

        if (teamNumber == 1) {
            index = selectedTeam1Index;
            logoView = logoTeam1; nameLabel = nameTeam1Label; statsLabel = team1Stats;
        } else { // teamNumber == 2
            index = selectedTeam2Index;
            logoView = logoTeam2; nameLabel = nameTeam2Label; statsLabel = team2Stats;
        }

        // Vérifier index et composants valides
        if (index < 0 || index >= numTeams || logoView == null || nameLabel == null || statsLabel == null) {
            System.err.printf("Erreur updateTeamDisplay: Index (%d) invalide ou Composant(s) FXML null pour équipe %d.%n", index, teamNumber);
            return;
        }

        teamName = teams[index];
        teamKey = keys[index];
        // Sécurité si jamais les tableaux de stats n'ont pas la bonne taille
        attack = (index < attackStats.length) ? attackStats[index] : 0;
        defense = (index < defenseStats.length) ? defenseStats[index] : 0;

        // Mettre à jour les éléments UI
        nameLabel.setText(teamName);
        setImage(logoView, TEAM_ICON_BASE_PATH + teamKey + ".png");
        // Afficher les stats
        statsLabel.setText(String.format("⚽ Att: %d | 🛡 Déf: %d", attack, defense));

        // Mettre à jour GameSettings
        if (teamNumber == 1) {
            GameSettings.team1 = teamName;
        } else {
            GameSettings.team2 = teamName;
        }
        System.out.println("Équipe " + teamNumber + " mise à jour: " + teamName + " (Att: " + attack + ", Def: " + defense + ")");
    }


    @FXML
    public void startGame(ActionEvent e) { // Renommé pour plus de clarté
        System.out.println("✅ Bouton Continuer cliqué - Lancement du jeu");
        if (GameSettings.team1 != null && GameSettings.team2 != null && GameSettings.team1.equals(GameSettings.team2)) {
            showAlert("Sélection invalide", "Veuillez choisir deux équipes différentes.");
            return;
        }
        if (GameSettings.team1 == null || GameSettings.team2 == null) {
            showAlert("Sélection incomplète", "Veuillez sélectionner les deux équipes.");
            return;
        }
        // *** MODIFIÉ ICI : Changer la destination vers game.fxml ***
        changeScene("/footgame/game.fxml", e);
    }

    @FXML
    void goBackToMenu(ActionEvent e) {
        System.out.println("⬅ Bouton Retour cliqué");
        changeScene("/footgame/menu.fxml", e);
    }

    // Méthode utilitaire pour changer de scène (utilise setRoot)
    private void changeScene(String fxmlPath, ActionEvent e) {
        try {
            String correctedPath = fxmlPath.startsWith("/") ? fxmlPath : "/" + fxmlPath;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(correctedPath));
            if (loader.getLocation() == null) { throw new IOException("Impossible de trouver le fichier FXML: " + correctedPath); }
            Parent newRoot = loader.load();
            Scene currentScene = ((Node) e.getSource()).getScene();
            currentScene.setRoot(newRoot); // Remplacer le root de la scène actuelle
        } catch (IOException ex) { System.err.println("Erreur lors du chargement de la scène FXML: " + fxmlPath); ex.printStackTrace(); showAlert("Erreur de Navigation", "Impossible de charger l'écran demandé."); }
        catch (IllegalStateException ex) { System.err.println("Erreur d'état lors du chargement: " + ex.getMessage()); ex.printStackTrace(); showAlert("Erreur Interne", "Une erreur interne est survenue."); }
    }

    // Méthode utilitaire showAlert
    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
        });
    }
}