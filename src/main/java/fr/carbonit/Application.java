package fr.carbonit;

import java.io.IOException;

public class Application {

    public static void main(String[] args) throws IOException {
        //passer en param le chemin du fichier d'entrée + le chemin de sortie
    	CarteTresor instance = new CarteTresor();
    	//remplissage des variables de la classe
    	instance.parseFile(args[0]);
    	//partie
    	instance.partieCarteTresor();
    	//creation fichier
    	instance.createFile("final_partie.txt");
    }

}
