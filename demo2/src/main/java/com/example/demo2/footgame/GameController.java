package com.example.demo2.footgame;

// --- Imports ---
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameController {

    private enum ShotResult { PENDING, SCORED, MISSED_SAVED }

    // --- FXML Injections ---
    @FXML private AnchorPane root;
    @FXML private ImageView background;
    @FXML private ImageView goalkeeper;
    @FXML private ImageView ball;
    @FXML private ImageView player;
    @FXML private Label statusLabel;
    @FXML private ImageView aimingArrow;
    // @FXML private ImageView goalNet; // <-- SUPPRIMÉ
    @FXML private Rectangle flashOverlay;
    @FXML private HBox scoreDisplayBox;
    @FXML private ImageView logoTeam1Display;
    @FXML private Label nameTeam1Display;
    @FXML private Label scoreDisplay;
    @FXML private Label nameTeam2Display;
    @FXML private ImageView logoTeam2Display;
    @FXML private HBox playerShotIndicatorBox;
    @FXML private HBox iaShotIndicatorBox;
    @FXML private VBox endGamePopup;
    @FXML private Label popupTitleLabel;
    @FXML private Label popupMessageLabel;
    @FXML private Button returnMenuButton;
    // --- AJOUT: Injections pour le menu pause ---
    @FXML private Button pauseMenuButton;
    @FXML private VBox pauseMenuPopup;
    @FXML private Button resumeButton; // Pas d'action spécifique, utilise handlePauseMenuButton
    @FXML private Button restartButton;
    @FXML private Button quitToMenuButton;


    // --- CHEMINS DES IMAGES ---
    private static final String PLAYER_PATH = "/footgame/player.png";
    private static final String BALL_PATH = "/footgame/ball.png";
    private static final String GOALKEEPER_PATH = "/footgame/goalkeeper.png";
    private static final String ARROW_PATH = "/footgame/arrow.png";
    private static final String TEAM_ICON_BASE_PATH = "/footgame/icons/";
    private static final String STADIUM_BACKGROUND_BASE_PATH = "/footgame/backgrounds/";
    private static final String CAMP_NOU_BACKGROUND_PATH = STADIUM_BACKGROUND_BASE_PATH + "camp_nou_bg.png";

    // --- RATIOS & CONSTANTES ---
    // --- Joueur --- (Tes valeurs)
    private static final double PLAYER_X_RATIO = 0.40;
    private static final double PLAYER_Y_RATIO = 0.75;
    private static final double PLAYER_WIDTH_RATIO = 0.20;
    private static final double PLAYER_HEIGHT_RATIO = 0.55;
    // --- Balle --- (Tes valeurs)
    private static final double BALL_X_RATIO = 0.49;
    private static final double BALL_Y_RATIO = 0.65 ;
    private static final double BALL_WIDTH_RATIO = 0.030;
    private static final double BALL_HEIGHT_RATIO = 0.045;
    // --- Gardien --- (Tes valeurs)
    private static final double GOALIE_X_RATIO = 0.49;
    private static final double GOALIE_Y_RATIO = 0.47;
    private static final double GOALIE_WIDTH_RATIO = 0.085;
    private static final double GOALIE_HEIGHT_RATIO = 0.21;
    // --- ZONE LOGIQUE DU BUT ---
    private static final double NET_X_RATIO = 0.50;
    private static final double NET_Y_RATIO = 0.42;      // SUGGESTION (Plus grand = plus bas)
    private static final double NET_WIDTH_RATIO = 0.33;   // SUGGESTION (Largeur réduite)
    private static final double NET_HEIGHT_RATIO = 0.28;  // SUGGESTION (Hauteur réduite)
    // --- Flèche --- (Suggestions pour taille/distance)
    private static final double ARROW_WIDTH_RATIO = 0.08;
    private static final double ARROW_HEIGHT_RATIO = 0.12;
    private static final double ARROW_DISTANCE_FROM_BALL_RATIO = -0.02;
    // --- TRAJECTOIRE DU TIR ---
    private static final double BALL_SHOT_VERTICAL_RATIO = 0.20; // Suggestion (Réduit)
    // Autres constantes de jeu
    private static final double ARROW_OSCILLATION_SPEED = 0.005;
    private static final double MAX_AIM_ROTATION = 45.0;
    private static final double GOALIE_MAX_OFFSET_RATIO = 0.15;
    private static final double GOAL_BOUNDS_TOLERANCE = 0.5;
    private static final double POST_THICKNESS_RATIO = 0.0; // Mis à 0.0
    private static final double MAX_SHOT_HORIZONTAL_OFFSET_RATIO = 0.32; // Suggestion (Réduit)
    private static final double GOALIE_CORE_SAVE_RATIO = 0.33;
    private static final double CORE_SAVE_CHANCE = 0.95;
    private static final double REACH_SAVE_CHANCE = 0.60;
    private static final int MAX_STANDARD_SHOTS = 5;
    private static final double INDICATOR_CIRCLE_RADIUS = 12.0;
    private static final Color PENDING_COLOR = Color.rgb(100, 100, 100, 0.7);
    private static final Color SCORED_COLOR = Color.rgb(0, 200, 0, 0.9);
    private static final Color MISSED_SAVED_COLOR = Color.rgb(220, 0, 0, 0.9);
    private static final double GOALIE_DIVE_VERTICAL_OFFSET = 15.0;
    private static final Duration GOALIE_ROTATION_DURATION = Duration.millis(250);
    private static final int WIN_CREDITS = 100;

    // --- VARIABLES DE JEU ---
    private boolean isPlayerTurn = true; private double normalizedAim = 0.0; private boolean arrowMovingRight = true;
    private double goalieBaseX = 0; private double playerGoalieDiveTargetX = 0; private Random random = new Random();
    private AnimationTimer gameLoop; private double w = 0; private double h = 0;
    private final Map<String, String> teamNameToKeyMap = new HashMap<>();
    private int playerShotsTaken = 0; private int iaShotsTaken = 0;
    private boolean isSuddenDeath = false; private boolean gameOver = false;
    private final List<ShotResult> playerShotResults = new ArrayList<>();
    private final List<ShotResult> iaShotResults = new ArrayList<>();
    // --- AJOUT: Drapeau pour l'état de pause ---
    private boolean isPaused = false;

    @FXML
    public void initialize() {
        teamNameToKeyMap.put("PSG", "psg"); teamNameToKeyMap.put("Real Madrid", "real");
        teamNameToKeyMap.put("FC Barcelone", "barca"); teamNameToKeyMap.put("Bayern Munich", "bayern");

        try { // Chargement images
            System.out.println("Chargement fond fixe: " + CAMP_NOU_BACKGROUND_PATH);
            Image bgImage = loadImage(CAMP_NOU_BACKGROUND_PATH);
            Image playerImage = loadImage(PLAYER_PATH); Image ballImage = loadImage(BALL_PATH);
            Image goalieImage = loadImage(GOALKEEPER_PATH); Image arrowImage = loadImage(ARROW_PATH);

            if (bgImage == null || playerImage == null || ballImage == null || goalieImage == null || arrowImage == null) {
                System.err.println("ERREUR CRITIQUE: Images manquantes."); showAlert("Erreur de chargement", "Ressources graphiques manquantes."); return;
            }
            background.setImage(bgImage); player.setImage(playerImage); ball.setImage(ballImage); goalkeeper.setImage(goalieImage); aimingArrow.setImage(arrowImage);
            aimingArrow.setVisible(false);
            System.out.println("Images chargées.");

        } catch (Exception e) { System.err.println("Erreur chargement images."); e.printStackTrace(); showAlert("Erreur Inattendue", "Erreur initialisation images."); return; }

        // Bindings et Listeners
        background.fitWidthProperty().bind(root.widthProperty()); background.fitHeightProperty().bind(root.heightProperty()); background.setPreserveRatio(false);
        flashOverlay.widthProperty().bind(root.widthProperty()); flashOverlay.heightProperty().bind(root.heightProperty()); flashOverlay.setOpacity(0.0);

        Platform.runLater(() -> {
            root.requestFocus(); root.setOnKeyPressed(this::handleKeyPressed);
            repositionElements();
            resetShootoutState(); // Assure que le menu pause est caché au début
            root.widthProperty().addListener((obs, oldVal, newVal) -> repositionElements());
            root.heightProperty().addListener((obs, oldVal, newVal) -> repositionElements());
            setupGameLoop(); gameLoop.start();
            System.out.println("Listeners et GameLoop initialisés.");
        });
        updateStatus();
        System.out.println("GameController initialisé (Mode Camp Nou Fixe, sans filet logique).");
    }

    /** Configure l'AnimationTimer. */
    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override public void handle(long now) {
                // Ne met à jour que si le jeu n'est PAS en pause ET pas terminé
                if (isPlayerTurn && !gameOver && !isPaused) {
                    updateArrowOscillation();
                    updateAimArrowVisuals();
                }
            }
        };
    }

    /** Met à jour l'oscillation de la flèche. */
    private void updateArrowOscillation() {
        if (arrowMovingRight) {
            normalizedAim += ARROW_OSCILLATION_SPEED;
            if (normalizedAim >= 1.0) { normalizedAim = 1.0; arrowMovingRight = false; }
        } else {
            normalizedAim -= ARROW_OSCILLATION_SPEED;
            if (normalizedAim <= -1.0) { normalizedAim = -1.0; arrowMovingRight = true; }
        }
    }

    /** Gère l'appui sur Espace. */
    private void handleKeyPressed(KeyEvent e) {
        // Ajout de la condition !isPaused
        if (!gameOver && isPlayerTurn && e.getCode() == KeyCode.SPACE && !isPaused) {
            shoot();
        }
        // Optionnel: Permettre de mettre en pause/reprendre avec Echap ?
        // else if (e.getCode() == KeyCode.ESCAPE) {
        //     handlePauseMenuButton(null); // Appeler avec null ou créer un ActionEvent vide
        // }
    }

    /** Charge une image. */
    private Image loadImage(String path) {
        String correctedPath = path.startsWith("/") ? path : "/" + path;
        try {
            InputStream stream = getClass().getResourceAsStream(correctedPath);
            if (stream == null) { System.err.println("Ressource Image non trouvée : " + correctedPath); return null; }
            return new Image(stream);
        } catch (Exception e) { System.err.println("Erreur lors du chargement de l'image : " + correctedPath); e.printStackTrace(); return null; }
    }

    /** Met à jour visuellement la flèche. */
    private void updateAimArrowVisuals() {
        // Ajout condition !isPaused
        if (w <= 0 || h <= 0 || gameOver || isPaused) {
            aimingArrow.setVisible(false);
            return;
        }
        positionAimingArrow();
        double rotationAngle = normalizedAim * MAX_AIM_ROTATION;
        aimingArrow.setRotate(rotationAngle);
        // Affiche seulement si c'est le tour du joueur ET pas en pause ET pas game over
        aimingArrow.setVisible(isPlayerTurn && !gameOver && !isPaused);
    }

    /** Déclenche le tir du joueur ET le plongeon (avec rotation) du gardien IA. */
    private void shoot() {
        // Ajout de la condition !isPaused
        if (gameOver || !isPlayerTurn || isPaused) return;
        isPlayerTurn = false; aimingArrow.setVisible(false); updateStatus();

        // Plongeon Gardien IA
        int diveDirection = random.nextInt(3); double maxDiveDistance = w * GOALIE_MAX_OFFSET_RATIO;
        double targetDiveTranslateX = 0, targetDiveTranslateY = 0, targetRotation = 0;
        switch (diveDirection) {
            case 0: targetDiveTranslateX = -maxDiveDistance; targetDiveTranslateY = GOALIE_DIVE_VERTICAL_OFFSET; targetRotation = -90; break;
            case 1: targetDiveTranslateX = 0; targetDiveTranslateY = 0; targetRotation = 0; break;
            case 2: targetDiveTranslateX = maxDiveDistance; targetDiveTranslateY = GOALIE_DIVE_VERTICAL_OFFSET; targetRotation = 90; break;
        }
        playerGoalieDiveTargetX = goalieBaseX + targetDiveTranslateX;
        final double finalTargetDiveTranslateY = targetDiveTranslateY;

        TranslateTransition gt = new TranslateTransition(Duration.seconds(0.45), goalkeeper); gt.setToX(targetDiveTranslateX); gt.setToY(finalTargetDiveTranslateY); gt.play();
        if (targetRotation != 0) { RotateTransition gr = new RotateTransition(GOALIE_ROTATION_DURATION, goalkeeper); gr.setToAngle(targetRotation); gr.play(); }
        System.out.println("Gardien IA plonge vers X = " + playerGoalieDiveTargetX + ", TranslateY = " + finalTargetDiveTranslateY + " (relatif), Rotation = " + targetRotation);

        // Tir Balle Joueur
        double maxOffsetX = w * MAX_SHOT_HORIZONTAL_OFFSET_RATIO;
        double currentAimOffset = normalizedAim * maxOffsetX;
        TranslateTransition ballShot = new TranslateTransition(Duration.seconds(0.5), ball);
        double verticalDistance = h * BALL_SHOT_VERTICAL_RATIO; // Utilise la constante réduite
        ballShot.setByY(-verticalDistance);
        ballShot.setByX(currentAimOffset);

        ballShot.setOnFinished(event -> {
            // Ne rien faire si on est entré en pause PENDANT l'animation
            if (gameOver || isPaused) return;
            playerShotsTaken++;
            boolean inBounds = isBallInGoalBounds();
            boolean stopped = false; ShotResult result;
            if (inBounds) { stopped = isBallStoppedByGoalie(playerGoalieDiveTargetX, finalTargetDiveTranslateY); }
            if (inBounds && !stopped) { GameSettings.scoreJoueur++; result = ShotResult.SCORED; flashGreenOverlay(); System.out.println("   RESULTAT JOUEUR: BUT"); }
            else if (inBounds && stopped) { result = ShotResult.MISSED_SAVED; flashRedOverlay(); System.out.println("   RESULTAT JOUEUR: ARRET"); }
            else { result = ShotResult.MISSED_SAVED; flashBackground(false); System.out.println("   RESULTAT JOUEUR: HORS CADRE"); }
            if (playerShotResults.size() < playerShotsTaken) { playerShotResults.add(result); } else { playerShotResults.set(playerShotsTaken - 1, result); }
            updateScoreDisplay(); updateShotIndicators();
            if (!checkWinner()) { prepareNextTurn(); }
        });
        ballShot.play();
    }

    /** Vérifie si la balle est dans les limites LOGIQUES du but (ratios NET_...). */
    private boolean isBallInGoalBounds() {
        if (ball == null || w <= 0 || h <= 0) return false;
        double ballFinalX = ball.getLayoutX() + ball.getTranslateX() + ball.getFitWidth() / 2;
        double ballFinalY = ball.getLayoutY() + ball.getTranslateY() + ball.getFitHeight() / 2;

        double goalWidthPixels = w * NET_WIDTH_RATIO; double goalHeightPixels = h * NET_HEIGHT_RATIO;
        double goalTopLeftX = w * NET_X_RATIO - goalWidthPixels / 2; double goalTopLeftY = h * NET_Y_RATIO;

        double goalOpeningLeftX = goalTopLeftX; double goalOpeningRightX = goalTopLeftX + goalWidthPixels;
        double goalOpeningTopY = goalTopLeftY; double goalOpeningBottomY = goalTopLeftY + goalHeightPixels;

        boolean withinX = ballFinalX >= (goalOpeningLeftX - GOAL_BOUNDS_TOLERANCE) && ballFinalX <= (goalOpeningRightX + GOAL_BOUNDS_TOLERANCE);
        boolean withinY = ballFinalY >= (goalOpeningTopY - GOAL_BOUNDS_TOLERANCE) && ballFinalY <= (goalOpeningBottomY + GOAL_BOUNDS_TOLERANCE);

        // Décommenter pour debug
        // System.out.printf("      Check Limites BUT LOGIQUE: Balle(%.1f, %.1f) vs But X[%.1f-%.1f] Y[%.1f-%.1f] -> In? X:%b Y:%b%n",
        //         ballFinalX, ballFinalY, goalOpeningLeftX, goalOpeningRightX, goalOpeningTopY, goalOpeningBottomY, withinX, withinY);

        return withinX && withinY;
    }

    /** Logique d'arrêt réaliste. */
    private boolean isBallStoppedByGoalie(double goalieFinalTargetX, double goalieRelativeTranslateY) {
        if(goalkeeper == null || ball == null) return false;
        double ballFinalCenterX = ball.getLayoutX() + ball.getTranslateX() + (ball.getFitWidth() / 2);
        double ballFinalCenterY = ball.getLayoutY() + ball.getTranslateY() + (ball.getFitHeight() / 2);
        double goalieWidth = goalkeeper.getFitWidth(); double goalieHeight = goalkeeper.getFitHeight();
        double goalieFinalStartX = goalieFinalTargetX; double goalieFinalEndX = goalieFinalTargetX + goalieWidth;
        double goalieFinalStartY = goalkeeper.getLayoutY() + goalieRelativeTranslateY;
        double goalieFinalEndY = goalieFinalStartY + goalieHeight;
        double goalieFinalCenterX = goalieFinalStartX + goalieWidth / 2;
        boolean inGoalieXRange = ballFinalCenterX >= goalieFinalStartX && ballFinalCenterX <= goalieFinalEndX;
        boolean inGoalieYRange = ballFinalCenterY >= goalieFinalStartY && ballFinalCenterY <= goalieFinalEndY;

        if (inGoalieXRange && inGoalieYRange) {
            double distanceToCenterX = Math.abs(ballFinalCenterX - goalieFinalCenterX);
            double coreSaveThreshold = goalieWidth * GOALIE_CORE_SAVE_RATIO;
            if (distanceToCenterX < coreSaveThreshold) { return random.nextDouble() < CORE_SAVE_CHANCE; }
            else { return random.nextDouble() < REACH_SAVE_CHANCE; }
        }
        return false;
    }

    /** Prépare le tour de l'IA. */
    private void prepareNextTurn() {
        // Vérifie si on est entré en pause juste avant
        if (isPaused || gameOver) return;

        aimingArrow.setVisible(false);
        PauseTransition pause = new PauseTransition(Duration.seconds(1.0));
        pause.setOnFinished(e -> {
            // iaTurn vérifiera aussi la pause/gameOver au début
            if (!gameOver) {
                resetGoalkeeperPosition();
                iaTurn();
            }
        });
        pause.play();
    }


    /** Gère le tour de l'IA. */
    private void iaTurn() {
        // Ajout de la condition !isPaused au début
        if (gameOver || isPaused) return;
        System.out.println("Début du tour de l'IA."); // Ajout log
        resetBall(); resetGoalkeeperPosition();
        double maxOffsetIA = w * 0.30; double iaAimOffset = (random.nextDouble() - 0.5) * 2 * maxOffsetIA;

        // Plongeon Gardien Joueur
        int playerDiveDirection = random.nextInt(3); double playerMaxDiveDistance = w * GOALIE_MAX_OFFSET_RATIO;
        double playerTargetDiveTranslateX = 0, playerTargetRelativeDiveTranslateY = 0, playerTargetRotation = 0;
        switch (playerDiveDirection) { case 0: playerTargetDiveTranslateX = -playerMaxDiveDistance; playerTargetRelativeDiveTranslateY = GOALIE_DIVE_VERTICAL_OFFSET; playerTargetRotation = -90; break; case 1: playerTargetDiveTranslateX = 0; playerTargetRelativeDiveTranslateY = 0; playerTargetRotation = 0; break; case 2: playerTargetDiveTranslateX = playerMaxDiveDistance; playerTargetRelativeDiveTranslateY = GOALIE_DIVE_VERTICAL_OFFSET; playerTargetRotation = 90; break; }
        double goaliePlayerFinalTargetX = goalieBaseX + playerTargetDiveTranslateX;
        final double finalPlayerTargetRelativeDiveTranslateY = playerTargetRelativeDiveTranslateY;

        TranslateTransition pgt = new TranslateTransition(Duration.seconds(0.45), goalkeeper); pgt.setToX(playerTargetDiveTranslateX); pgt.setToY(finalPlayerTargetRelativeDiveTranslateY); pgt.play();
        if (playerTargetRotation != 0) { RotateTransition pgr = new RotateTransition(GOALIE_ROTATION_DURATION, goalkeeper); pgr.setToAngle(playerTargetRotation); pgr.play(); }
        System.out.println("Gardien Joueur plonge vers X = " + goaliePlayerFinalTargetX + ", TranslateY = " + finalPlayerTargetRelativeDiveTranslateY + " (relatif), Rotation = " + playerTargetRotation);

        // Tir Balle IA
        TranslateTransition ttIA = new TranslateTransition(Duration.seconds(0.5), ball);
        double verticalDistanceIA = h * BALL_SHOT_VERTICAL_RATIO; // Utilise constante réduite
        ttIA.setByY(-verticalDistanceIA);
        ttIA.setByX(iaAimOffset);

        ttIA.setOnFinished(event -> {
            // Ne rien faire si on est entré en pause PENDANT l'animation
            if (gameOver || isPaused) return;
            iaShotsTaken++;
            boolean iaInBounds = isBallInGoalBounds();
            boolean playerStopped = false; ShotResult resultIA;
            if (iaInBounds) { playerStopped = isBallStoppedByGoalie(goaliePlayerFinalTargetX, finalPlayerTargetRelativeDiveTranslateY); }
            if (iaInBounds && !playerStopped) { GameSettings.scoreIA++; resultIA = ShotResult.SCORED; flashGreenOverlay(); System.out.println("   RESULTAT IA: BUT"); }
            else if (iaInBounds && playerStopped) { resultIA = ShotResult.MISSED_SAVED; flashRedOverlay(); System.out.println("   RESULTAT IA: ARRET"); }
            else { resultIA = ShotResult.MISSED_SAVED; flashBackground(false); System.out.println("   RESULTAT IA: HORS CADRE"); }
            if (iaShotResults.size() < iaShotsTaken) { iaShotResults.add(resultIA); } else { iaShotResults.set(iaShotsTaken - 1, resultIA); }
            updateScoreDisplay(); updateShotIndicators();
            if (!checkWinner()) { resetBall(); resetGoalkeeperPosition(); isPlayerTurn = true; updateStatus(); }
        });
        ttIA.play();
    }

    /** Vérifie si un gagnant peut être déclaré. */
    private boolean checkWinner() {
        // Pas besoin de vérifier isPaused ici, les tours s'arrêtent avant
        int score1 = GameSettings.scoreJoueur; int score2 = GameSettings.scoreIA;
        int shotsLeft1 = MAX_STANDARD_SHOTS - playerShotsTaken; int shotsLeft2 = MAX_STANDARD_SHOTS - iaShotsTaken;
        if (shotsLeft1 < 0) shotsLeft1 = 0; if (shotsLeft2 < 0) shotsLeft2 = 0;

        if (isSuddenDeath) {
            if (playerShotsTaken == iaShotsTaken && playerShotsTaken > MAX_STANDARD_SHOTS) {
                if (score1 > score2) { endGame(GameSettings.team1); return true; }
                if (score2 > score1) { endGame(GameSettings.team2); return true; }
            } return false;
        }
        if (score1 > score2 + shotsLeft2) { endGame(GameSettings.team1); return true; }
        if (score2 > score1 + shotsLeft1) { endGame(GameSettings.team2); return true; }
        if (playerShotsTaken >= MAX_STANDARD_SHOTS && iaShotsTaken >= MAX_STANDARD_SHOTS) {
            if (score1 > score2) { endGame(GameSettings.team1); return true; }
            if (score2 > score1) { endGame(GameSettings.team2); return true; }
            isSuddenDeath = true; updateStatus();
        } return false;
    }

    /** Termine la partie. */
    private void endGame(String winnerName) {
        if (gameOver) return; gameOver = true; isPlayerTurn = false; aimingArrow.setVisible(false); if (gameLoop != null) gameLoop.stop();
        isPaused = false; // Sortir de l'état pause si on y était
        if (pauseMenuButton != null) pauseMenuButton.setVisible(false); // Masque le bouton pause
        if (pauseMenuPopup != null) pauseMenuPopup.setVisible(false); // Masque le menu pause

        String winnerDisplayName = (winnerName != null && !winnerName.isEmpty()) ? winnerName : "Équipe Inconnue";
        String playerTeamName = (GameSettings.team1 != null && !GameSettings.team1.isEmpty()) ? GameSettings.team1 : "DOM.";
        boolean playerWon = winnerDisplayName.equals(playerTeamName); int earnedCredits = playerWon ? WIN_CREDITS : 0;
        GameSettings.playerCredits += earnedCredits; GameSettings.saveCredits(); String popupTitle = playerWon ? "🎉 VICTOIRE ! 🎉" : "😥 Défaite 😥";
        String popupMessage = String.format( (playerWon ? "%s gagne %d - %d !\n\nVous remportez %d crédits !\nCrédits Totaux : %d" : "%s gagne %d - %d...\n\nVous remportez %d crédits.\nCrédits Totaux : %d"), winnerDisplayName, GameSettings.scoreJoueur, GameSettings.scoreIA, earnedCredits, GameSettings.playerCredits);
        System.out.println("FIN DE PARTIE | Gagnant: " + winnerDisplayName + " | Score: " + GameSettings.scoreJoueur + "-" + GameSettings.scoreIA + " | Crédits: " + earnedCredits + "/" + GameSettings.playerCredits);
        statusLabel.setText("Fin de partie !"); statusLabel.setStyle("-fx-background-color: rgba(50, 50, 50, 0.8); -fx-font-size: 24px; -fx-text-fill: white;");
        if(endGamePopup != null && popupTitleLabel != null && popupMessageLabel != null && returnMenuButton != null) { popupTitleLabel.setText(popupTitle); popupMessageLabel.setText(popupMessage); endGamePopup.setVisible(true); returnMenuButton.setVisible(true); }
        else { showAlert(popupTitle, popupMessage); }
    }

    // --- NOUVELLES MÉTHODES POUR LE MENU PAUSE ---

    /** Gère l'affichage/masquage du menu pause et met le jeu en pause/reprend. */
    @FXML
    private void handlePauseMenuButton(ActionEvent event) {
        isPaused = !isPaused; // Inverse l'état de pause
        pauseMenuPopup.setVisible(isPaused); // Affiche/masque le popup

        if (isPaused) {
            if (gameLoop != null) gameLoop.stop();
            aimingArrow.setVisible(false); // Masque la flèche en pause
            System.out.println("Jeu en Pause");
        } else {
            if (gameLoop != null && !gameOver) gameLoop.start();
            aimingArrow.setVisible(isPlayerTurn && !gameOver); // Réaffiche si besoin
            System.out.println("Jeu Repris");
            root.requestFocus(); // Redonne le focus pour la touche Espace
        }
    }

    /** Recommence une nouvelle séance de tirs au but. */
    @FXML
    private void handleRestartGame(ActionEvent event) {
        System.out.println("Recommencement de la partie...");
        isPaused = false; // Assure qu'on n'est plus en pause
        pauseMenuPopup.setVisible(false); // Masque le menu pause
        resetShootoutState(); // Réinitialise tout
        root.requestFocus(); // Focus pour Espace
    }

    /** Quitte la partie en cours et retourne au menu principal. */
    @FXML
    private void handleQuitToMenu(ActionEvent event) {
        System.out.println("Retour au menu principal depuis la pause...");
        isPaused = false; pauseMenuPopup.setVisible(false);
        stopGameLoop();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/footgame/menu.fxml"));
            if (loader.getLocation() == null) throw new IOException("menu.fxml introuvable");
            Parent menuRoot = loader.load();
            Scene currentScene = root.getScene(); // Utilise root pour obtenir la scène actuelle
            if(currentScene != null) {
                currentScene.setRoot(menuRoot);
            } else { // Fallback
                Stage stage = (Stage) root.getScene().getWindow();
                stage.setScene(new Scene(menuRoot));
                stage.setFullScreen(true); stage.setFullScreenExitHint("");
            }
        } catch (IOException e) { System.err.println("Erreur chargement menu: " + e.getMessage()); showAlert("Erreur", "Impossible de retourner au menu."); }
    }

    // --- FIN NOUVELLES MÉTHODES ---


    /** Gère le clic sur le bouton Retour Menu (celui de fin de partie). */
    @FXML private void handleReturnToMenu(ActionEvent event) {
        // Renommé pour éviter confusion avec handleQuitToMenu
        handleQuitToMenu(event); // Fait la même chose : retourne au menu
    }

    /** Flash de l'arrière-plan (pour HORS CADRE). */
    private void flashBackground(boolean goalScored) { if (!goalScored && !isPaused) { FadeTransition ft = new FadeTransition(Duration.seconds(0.15), background); ft.setFromValue(1.0); ft.setToValue(0.6); ft.setCycleCount(2); ft.setAutoReverse(true); ft.play(); } }
    // --- MÉTHODES D'ANIMATION ---
    private void flashRedOverlay() { if (flashOverlay != null && !isPaused) { flashOverlay.setFill(Color.rgb(255, 80, 80)); FadeTransition ft = new FadeTransition(Duration.millis(350), flashOverlay); ft.setFromValue(0.0); ft.setToValue(0.75); ft.setCycleCount(2); ft.setAutoReverse(true); ft.setOnFinished(e -> flashOverlay.setOpacity(0.0)); ft.play(); } }
    private void flashGreenOverlay() { if (flashOverlay != null && !isPaused) { flashOverlay.setFill(Color.rgb(100, 255, 100)); FadeTransition ft = new FadeTransition(Duration.millis(350), flashOverlay); ft.setFromValue(0.0); ft.setToValue(0.75); ft.setCycleCount(2); ft.setAutoReverse(true); ft.setOnFinished(e -> flashOverlay.setOpacity(0.0)); ft.play(); } }
    // --- FIN MÉTHODES D'ANIMATION ---

    /** Replace la balle et réinitialise la visée. */
    private void resetBall() {
        if (ball != null && w > 0 && h > 0) {
            double ballWidth = w * BALL_WIDTH_RATIO; double ballHeight = h * BALL_HEIGHT_RATIO; ball.setFitWidth(ballWidth); ball.setFitHeight(ballHeight);
            ball.setLayoutX(w * BALL_X_RATIO - ballWidth / 2); ball.setLayoutY(h * BALL_Y_RATIO - ballHeight / 2);
            ball.setTranslateX(0); ball.setTranslateY(0);
        } normalizedAim = 0.0; arrowMovingRight = random.nextBoolean(); positionAimingArrow();
    }

    /** Replace le gardien à sa position de base et réinitialise transformations. */
    private void resetGoalkeeperPosition() {
        if (goalkeeper != null && w > 0 && h > 0) {
            double goalieWidth = w * GOALIE_WIDTH_RATIO; double goalieHeight = h * GOALIE_HEIGHT_RATIO; goalkeeper.setFitWidth(goalieWidth); goalkeeper.setFitHeight(goalieHeight);
            goalieBaseX = w * GOALIE_X_RATIO - goalieWidth / 2; goalkeeper.setLayoutX(goalieBaseX); goalkeeper.setLayoutY(h * GOALIE_Y_RATIO - goalieHeight / 2);
            goalkeeper.setTranslateX(0); goalkeeper.setTranslateY(0); goalkeeper.setRotate(0);
        }
    }

    /** Met à jour l'affichage complet du score. */
    private void updateScoreDisplay() {
        String team1 = (GameSettings.team1 != null && !GameSettings.team1.isEmpty()) ? GameSettings.team1 : "DOM.";
        String team2 = (GameSettings.team2 != null && !GameSettings.team2.isEmpty()) ? GameSettings.team2 : "EXT.";
        int score1 = GameSettings.scoreJoueur; int score2 = GameSettings.scoreIA; nameTeam1Display.setText(team1); scoreDisplay.setText(score1 + " - " + score2); nameTeam2Display.setText(team2);
        String key1 = teamNameToKeyMap.get(team1); String key2 = teamNameToKeyMap.get(team2);
        if (key1 != null) { logoTeam1Display.setImage(loadImage(TEAM_ICON_BASE_PATH + key1 + ".png")); } else { logoTeam1Display.setImage(null); }
        if (key2 != null) { logoTeam2Display.setImage(loadImage(TEAM_ICON_BASE_PATH + key2 + ".png")); } else { logoTeam2Display.setImage(null); }
    }

    /** Met à jour les indicateurs visuels de tirs (cercles). */
    private void updateShotIndicators() {
        if (playerShotIndicatorBox == null || iaShotIndicatorBox == null) return; playerShotIndicatorBox.getChildren().clear(); iaShotIndicatorBox.getChildren().clear();
        int totalIndicators = Math.max(MAX_STANDARD_SHOTS, Math.max(playerShotsTaken, iaShotsTaken));
        for (int i = 0; i < totalIndicators; i++) {
            playerShotIndicatorBox.getChildren().add(createIndicatorCircle(i < playerShotResults.size() ? playerShotResults.get(i) : ShotResult.PENDING));
            iaShotIndicatorBox.getChildren().add(createIndicatorCircle(i < iaShotResults.size() ? iaShotResults.get(i) : ShotResult.PENDING));
        }
    }

    /** Crée un cercle indicateur basé sur le résultat. */
    private Circle createIndicatorCircle(ShotResult result) { Circle c = new Circle(INDICATOR_CIRCLE_RADIUS); c.setStroke(Color.WHITE); switch (result) { case SCORED: c.setFill(SCORED_COLOR); break; case MISSED_SAVED: c.setFill(MISSED_SAVED_COLOR); break; default: c.setFill(PENDING_COLOR); break; } return c; }

    /** Met à jour le label de statut. */
    private void updateStatus() { if (gameOver || isPaused) return; String statusText; if (isSuddenDeath) { statusText = "⚡ MORT SUBITE - Tir " + (Math.max(playerShotsTaken, iaShotsTaken) + 1) + " ⚡"; } else { int currentShot = Math.min((isPlayerTurn ? playerShotsTaken : iaShotsTaken) + 1, MAX_STANDARD_SHOTS); statusText = "Tir " + currentShot + "/" + MAX_STANDARD_SHOTS; } statusText += isPlayerTurn ? " - 🎯 À vous ! (Espace)" : " - 🔒 L'IA prépare son tir..."; statusLabel.setText(statusText); statusLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-font-size: 20px; -fx-text-fill: white;"); }

    /** Réinitialise l'état de la séance de tirs. */
    private void resetShootoutState() {
        playerShotsTaken = 0; iaShotsTaken = 0;
        GameSettings.scoreJoueur = 0; GameSettings.scoreIA = 0;
        isSuddenDeath = false; gameOver = false; isPaused = false; // Assure sortie de pause
        playerShotResults.clear(); iaShotResults.clear();
        if (pauseMenuPopup != null) pauseMenuPopup.setVisible(false); // Cache menu pause
        if (pauseMenuButton != null) pauseMenuButton.setVisible(true); // Montre bouton pause
        if (endGamePopup != null) endGamePopup.setVisible(false);
        if (returnMenuButton != null) returnMenuButton.setVisible(false);
        updateScoreDisplay(); updateShotIndicators(); updateStatus();
        repositionElements(); resetBall(); resetGoalkeeperPosition();
        if(gameLoop != null) { try { gameLoop.stop(); } catch (Exception ignore) {} gameLoop.start(); } // Redémarre boucle
        System.out.println("Nouvelle séance de tirs initialisée (Camp Nou Fixe).");
        Platform.runLater(() -> root.requestFocus()); // Redemande le focus après reset
    }

    /** Repositionne tous les éléments. */
    private void repositionElements() {
        w = root.getWidth(); h = root.getHeight(); if (w <= 0 || h <= 0) return;
        // JOUEUR
        if (player != null) { double pw = w * PLAYER_WIDTH_RATIO, ph = h * PLAYER_HEIGHT_RATIO; player.setFitWidth(pw); player.setFitHeight(ph); player.setLayoutX(w * PLAYER_X_RATIO - pw / 2); player.setLayoutY(h * PLAYER_Y_RATIO - ph / 2); }
        // BALLON
        if (ball != null) { double bw = w * BALL_WIDTH_RATIO, bh = h * BALL_HEIGHT_RATIO; ball.setFitWidth(bw); ball.setFitHeight(bh); ball.setLayoutX(w * BALL_X_RATIO - bw / 2); ball.setLayoutY(h * BALL_Y_RATIO - bh / 2); }
        // GARDIEN
        resetGoalkeeperPosition(); // Positionne et reset transfo gardien
        // FLÈCHE
        if (aimingArrow != null) { double aw = w * ARROW_WIDTH_RATIO, ah = h * ARROW_HEIGHT_RATIO; aimingArrow.setFitWidth(aw); aimingArrow.setFitHeight(ah); positionAimingArrow(); }
    }

    /** Positionne la flèche par rapport à la balle. */
    private void positionAimingArrow() { if (w <= 0 || h <= 0 || ball == null || aimingArrow == null) return; double bcx = ball.getLayoutX() + ball.getFitWidth() / 2; double bty = ball.getLayoutY(); double ah = aimingArrow.getFitHeight(), aw = aimingArrow.getFitWidth(); double vGap = h * ARROW_DISTANCE_FROM_BALL_RATIO; aimingArrow.setLayoutX(bcx - aw / 2); aimingArrow.setLayoutY(bty - ah - vGap); }

    /** Affiche une alerte standard. */
    private void showAlert(String title, String content) { Platform.runLater(() -> { Alert alert = new Alert(Alert.AlertType.ERROR); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait(); }); }

    /** Arrête le game loop. */
    public void stopGameLoop() { if (gameLoop != null) { gameLoop.stop(); System.out.println("GameLoop arrêté."); } }
}