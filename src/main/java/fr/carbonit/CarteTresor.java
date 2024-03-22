package fr.carbonit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.opengamma.strata.collect.tuple.Pair;

public class CarteTresor {

	Carte carte;
	Pair<String, Set<Montagne>> montagnes = Pair.of("montagne", new LinkedHashSet<>());
	Pair<String, Set<Tresor>> tresors = Pair.of("tresor", new LinkedHashSet<>());
	Pair<String, Set<Aventurier>> aventuriers = Pair.of("aventurier", new LinkedHashSet<>());
	Pair<Set<Tresor>, Set<Aventurier>> infosTresorsJoueurs;
	Set<String> linesCarteAndMontagnes = new HashSet<>();
	int espacesMatrice;
	
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
                    	montagnes.getSecond().add(new Montagne(Integer.valueOf(parts[1]), Integer.valueOf(parts[2])));
                    	linesCarteAndMontagnes.add(line);
                    }
                    case "T" -> tresors.getSecond().add(new Tresor(Integer.valueOf(parts[1]), Integer.valueOf(parts[2]), Integer.valueOf(parts[3])));
                    case "A" -> aventuriers.getSecond().add(new Aventurier(parts[1], Integer.valueOf(parts[2]), 
                            Integer.valueOf(parts[3]), parts[4], parts[5]));
                }
            }
        });
        return allLines;
    }
    
    public Pair<Set<Tresor>, Set<Aventurier>> partieCarteTresor(List<Pair<String, Object>> debutMap) {
        Set<Montagne> montagnesList = montagnes.getSecond();
        Set<Tresor> tresorsList = tresors.getSecond();
        Set<Aventurier> joueurs = aventuriers.getSecond();
        
        List<Integer> tours = joueurs.stream().map(joueur -> joueur.parcours().length()).toList();
        int nbTours = Collections.min(tours);
        
        for (int i=0; i <= nbTours; i++) {
            int nbTour = i;
            joueurs.stream().forEach(joueur -> {
                String action = joueur.parcours().substring(nbTour, nbTour);
                infosTresorsJoueurs = this.action(action, joueur, carte, montagnesList, tresorsList, joueurs);
                
            });
        }
        return infosTresorsJoueurs;
    }
    
	public Pair<Set<Tresor>, Set<Aventurier>> action(String action, Aventurier joueurDebut, Carte carte, Set<Montagne> montagnes, Set<Tresor> tresors, Set<Aventurier> joueurs) {
        //do action
        Aventurier joueur = mapActionByOrientation(joueurDebut, action); 
        //check limites
        if (joueur.x() < 0 || joueur.x() > carte.largeurX() || joueur.y() < 0 || joueur.y() > carte.hauteurY()) {
            return Pair.of(tresors, joueurs);
        }
        //check collision montagne
        for (Montagne montagne : montagnes) {
            if (joueur.x() == montagne.x() && joueur.y() == montagne.y()) {
                return Pair.of(tresors, joueurs);
            }
        }
        
        //modifier nb tresors
        for (Tresor tresor : tresors) {
            if (tresor.getX() == joueur.x() && tresor.getY() == joueur.y() && tresor.nbTresors > 0) {
                tresor.setNbTresors(tresor.getNbTresors() - 1);
            }
        }
        //retirer le joueur ancien + rajouter = maj
        for (Aventurier a : new ArrayList<>(joueurs)) {
        	if (a.nom().equalsIgnoreCase(joueur.nom())) {
        		joueurs.remove(a);
            }
        }
        List<Aventurier> newList = new ArrayList<>(joueurs);
        newList.add(joueur);
        return Pair.of(tresors, newList.stream().collect(Collectors.toSet()));
    }
    
    public Aventurier mapActionByOrientation(Aventurier a, String action) {
    	Aventurier newPosition = a;
        switch (a.orientation()) {
            case "S" :
                switch (action) {
                    case "A" -> newPosition = new Aventurier(a.nom(), a.x(), a.y() + 1, a.orientation(), a.parcours());
                    case "D" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "W", a.parcours());
                    case "G" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "E", a.parcours());
                };
                return newPosition;
            case "N" :
                switch (action) {
                    case "A" -> newPosition = new Aventurier(a.nom(), a.x(), a.y() - 1, a.orientation(), a.parcours());
                    case "D" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "E", a.parcours());
                    case "G" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "W", a.parcours());
                };
                return newPosition;
            case "E" :
                switch (action) {
                    case "A" -> newPosition = new Aventurier(a.nom(), a.x() + 1, a.y(), a.orientation(), a.parcours());
                    case "D" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "S", a.parcours());
                    case "G" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "N", a.parcours());
                };
                return newPosition;
            case "W" :
                switch (action) {
                    case "A" -> newPosition = new Aventurier(a.nom(), a.x() - 1, a.y(), a.orientation(), a.parcours());
                    case "D" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "N", a.parcours());
                    case "G" -> newPosition = new Aventurier(a.nom(), a.x(), a.y(), "S", a.parcours());
                }
                return newPosition;
            default : break;
        }
        return newPosition;
    }
    
    public void createFile() {
        
    }

    public record Carte(int largeurX, int hauteurY) {
    }

}
