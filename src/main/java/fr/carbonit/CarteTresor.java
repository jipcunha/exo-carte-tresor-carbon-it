package fr.carbonit;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.opengamma.strata.collect.tuple.Pair;

public class CarteTresor {

	Carte carte;
	Set<Montagne> montagnes = new LinkedHashSet<>();
	Set<Tresor> tresors = new LinkedHashSet<>();
	Set<Aventurier> aventuriers = new LinkedHashSet<>();
	Pair<Set<Tresor>, Set<Aventurier>> infosTresorsJoueurs;
	Set<String> linesCarteAndMontagnes = new LinkedHashSet<>();
	
    public List<String> parseFile(String fileName) throws IOException {
    	ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    	InputStream is = classloader.getResourceAsStream(fileName);
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        List<String> allLines = new ArrayList<>();
        String l;
        while ((l=r.readLine()) != null) {
        	allLines.add(l);
        }

        allLines.stream().forEach(line -> {
            if (!line.substring(0, 0).equals("#")) {
                String[] parts = line.split(" - "); 
                switch (parts[0]) {
                    case "C" -> { 
                    	carte = new Carte(Integer.valueOf(parts[1]), Integer.valueOf(parts[2]));
                    	linesCarteAndMontagnes.add(line);
                    }
                    case "M" -> {
                    	montagnes.add(new Montagne(Integer.valueOf(parts[1]), Integer.valueOf(parts[2])));
                    	linesCarteAndMontagnes.add(line);
                    }
                    case "T" -> tresors.add(new Tresor(Integer.valueOf(parts[1]), Integer.valueOf(parts[2]), Integer.valueOf(parts[3])));
                    case "A" -> aventuriers.add(new Aventurier(parts[1], Integer.valueOf(parts[2]), 
                            Integer.valueOf(parts[3]), parts[4], parts[5], 0));
                }
            }
        });
        return allLines;
    }
    
    public Pair<Set<Tresor>, Set<Aventurier>> partieCarteTresor(List<Pair<String, Object>> debutMap) {
        List<Integer> tours = aventuriers.stream().map(joueur -> joueur.parcours().length()).toList();
        int nbTours = Collections.min(tours);
        
        for (int i=0; i <= nbTours; i++) {
            int nbTour = i;
            aventuriers.stream().forEach(joueur -> {
                String action = joueur.parcours().substring(nbTour, nbTour);
                infosTresorsJoueurs = this.action(action, joueur);
                
            });
        }
        return infosTresorsJoueurs;
    }
    
	public Pair<Set<Tresor>, Set<Aventurier>> action(String action, Aventurier joueurDebut) {
        Aventurier joueur = mapActionByOrientation(joueurDebut, action); 
        //check limites
        if (joueur.x() < 0 || joueur.x() > carte.largeurX() || joueur.y() < 0 || joueur.y() > carte.hauteurY()) {
            return Pair.of(tresors, aventuriers);
        }
        //check collision montagne
        for (Montagne montagne : montagnes) {
            if (joueur.x() == montagne.x() && joueur.y() == montagne.y()) {
                return Pair.of(tresors, aventuriers);
            }
        }
        
        //modifier nb tresors
        for (Tresor tresor : tresors) {
            if (tresor.getX() == joueur.x() && tresor.getY() == joueur.y() && tresor.nbTresors > 0) {
                tresor.setNbTresors(tresor.getNbTresors() - 1);
                // calcul nb tresor joueur
                joueur = new Aventurier(joueur.nom(), joueur.x(), joueur.y(), joueur.orientation(), joueur.parcours(), joueur.nbTresors() + 1);
            }
        }
        Set<Aventurier> newSet = aventuriers;
        if (!joueurDebut.equals(joueur)) {
	        //retirer le joueur ancien + rajouter = maj si le joueur a changé
        	//TODO au lieu de supprimer + rajouter : changer en update car on perds l'ordre des aventuriers
	        for (Aventurier a : new ArrayList<>(aventuriers)) {
	        	if (a.nom().equalsIgnoreCase(joueur.nom())) {
	        		aventuriers.remove(a);
	            }
	        }
	        newSet.add(joueur);
        	
        }
        return Pair.of(tresors, newSet);
    }
    
    public Aventurier mapActionByOrientation(Aventurier a, String action) {
    	Aventurier newPosition = a;
        switch (a.orientation()) {
            case "S" :
                switch (action) {
                    case "A" -> newPosition = new Aventurier(a.nom(), a.x(), a.y() + 1, a.orientation(), a.parcours(), a.nbTresors());
                    case "D" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "W", a.parcours(), a.nbTresors());
                    case "G" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "E", a.parcours(), a.nbTresors());
                };
                return newPosition;
            case "N" :
                switch (action) {
                    case "A" -> newPosition = new Aventurier(a.nom(), a.x(), a.y() - 1, a.orientation(), a.parcours(), a.nbTresors());
                    case "D" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "E", a.parcours(), a.nbTresors());
                    case "G" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "W", a.parcours(), a.nbTresors());
                };
                return newPosition;
            case "E" :
                switch (action) {
                    case "A" -> newPosition = new Aventurier(a.nom(), a.x() + 1, a.y(), a.orientation(), a.parcours(), a.nbTresors());
                    case "D" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "S", a.parcours(), a.nbTresors());
                    case "G" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "N", a.parcours(), a.nbTresors());
                };
                return newPosition;
            case "W" :
                switch (action) {
                    case "A" -> newPosition = new Aventurier(a.nom(), a.x() - 1, a.y(), a.orientation(), a.parcours(), a.nbTresors());
                    case "D" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "N", a.parcours(), a.nbTresors());
                    case "G" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "S", a.parcours(), a.nbTresors());
                }
                return newPosition;
            default : break;
        }
        return newPosition;
    }
    
    public void createFile(String fileNameOutput) throws IOException {
    	String separator = " - ";
        Set<String> tresorsLines = tresors
        		.stream()
        		.map(tresor -> "T - " + tresor.getX() + separator + tresor.getY() + separator + tresor.getNbTresors())
        		.collect(Collectors.toSet());
        Set<String> joueursLines = aventuriers
        		.stream()
        		.map(a -> "A - " + a.nom() + separator + a.x() + separator + a.y() + separator + a.orientation() + separator + a.nbTresors())
        		.collect(Collectors.toSet());
        Set<String> allLines = linesCarteAndMontagnes;
        allLines.addAll(tresorsLines);
        allLines.addAll(joueursLines);
        
    	List<Integer> listTailleNoms = aventuriers.stream().map(a -> a.nom().length()).toList();
    	int espacesMax = Collections.max(listTailleNoms) + 1;
    	
    	String[][] matrice = new String[carte.largeurX()][carte.hauteurY()];
    	
    	for (int i = 0; i < matrice.length; i++) {
    		for (int j = 0; j < matrice[i].length; j++) {
    			int index = i;
    			int indexJ = j;
    			//par défaut la matrice setter des points sur toute la matrice
    			matrice[index][indexJ] = "." + StringUtils.repeat(" ", espacesMax);
    			tresors.stream().forEach(t -> {
    				if (t.getX() == index && t.getY() == indexJ) {
    					var stringNrTresors = String.valueOf(t.getNbTresors());
    					var spacesToAdd = espacesMax - stringNrTresors.length();
    					matrice[index][indexJ] = "T(" + t.nbTresors + ")" + StringUtils.repeat(" ", spacesToAdd);
    				}
    			});
    			aventuriers.stream().forEach(a -> {
    				if (a.x() == index && a.y() == indexJ) {
    					var spacesToAdd = espacesMax - a.nom().length();
    					matrice[index][indexJ] = "A(" + a.nom() + ")"  + StringUtils.repeat(" ", spacesToAdd);
    				}
    			});
    		}
    	}
    	
    	FileWriter fileWriter = new FileWriter(fileNameOutput);
    	for (String str : allLines) {
    	    fileWriter.write(str);
    	}
    	// TODO addMatrice
    	fileWriter.close();
    	
    }

    public record Carte(int largeurX, int hauteurY) {}

}
