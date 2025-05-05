//package com.example.demo2.footgame;
//
//import javafx.scene.image.Image;
//import java.util.Random;
//
//public class GameUtils {
//
//    public static Image chargerImage(String chemin) {
//        return new Image(GameUtils.class.getResourceAsStream(chemin));
//    }
//
//    public static int genererNombreAleatoire(int min, int max) {
//        Random rand = new Random();
//        return rand.nextInt((max - min) + 1) + min;
//    }
//}
package com.example.demo2.footgame;

import javafx.scene.image.Image;

import java.util.Random;

public class GameUtils {
    public static Image chargerImage(String chemin) {
        return new Image(GameUtils.class.getResourceAsStream(chemin));
    }

    public static int genererNombreAleatoire(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static boolean tirRéussi() {
        return Math.random() < 0.7; // 70% de chance de marquer
    }
}
