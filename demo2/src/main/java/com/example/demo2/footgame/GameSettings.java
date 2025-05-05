package com.example.demo2.footgame;

import java.util.prefs.Preferences; // Importer Preferences

public class GameSettings {
    public static String team1;
    public static String team2;
    public static String stadium;
    public static int scoreJoueur = 0;
    public static int scoreIA = 0;

    // Variable pour stocker les crédits du joueur
    public static int playerCredits = 0; // Default value

    // NOUVEAU: Bloc static pour charger les crédits au démarrage
    static {
        loadCredits();
    }

    // Méthode privée pour charger les crédits depuis les préférences
    private static void loadCredits() {
        try {
            // Utilise la classe GameSettings comme noeud de référence pour les préférences
            Preferences prefs = Preferences.userNodeForPackage(GameSettings.class);
            // Récupère la valeur sauvegardée, utilise 0 comme défaut si non trouvée
            playerCredits = prefs.getInt("playerCredits", 0);
            System.out.println("Crédits chargés au démarrage: " + playerCredits);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des crédits: " + e.getMessage());
            playerCredits = 0; // Remettre à 0 en cas d'erreur
        }
    }

    // Optionnel: Méthode pour sauvegarder (sera appelée depuis GameController)
    public static void saveCredits() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(GameSettings.class);
            prefs.putInt("playerCredits", playerCredits);
            prefs.flush(); // Important pour s'assurer de l'écriture
            System.out.println("Crédits sauvegardés: " + playerCredits);
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde des crédits: " + e.getMessage());
        }
    }
}