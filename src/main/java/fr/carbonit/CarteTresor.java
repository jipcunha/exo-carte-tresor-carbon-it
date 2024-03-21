package fr.carbonit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.opengamma.strata.collect.tuple.Pair;

public class CarteTresor {

    public List<Pair<String, Object>> parseFile(File file) throws IOException {
        List<Pair<String, Object>> mapObjects = new ArrayList<>();
        
        List<String> allLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        allLines.stream().forEach(line -> {
            if (!line.substring(0, 0).equals("#")) {
                String[] parts = line.split(" - "); 
                switch (parts[0]) {
                    case "C" -> mapObjects.add(Pair.of("carte", new Carte(Integer.valueOf(parts[1]), Integer.valueOf(parts[2]))));
                    case "M" -> mapObjects.add(Pair.of("montagne", new Montagne(Integer.valueOf(parts[1]), Integer.valueOf(parts[2]))));
                    case "T" -> mapObjects.add(Pair.of("tresor", new Tresor(Integer.valueOf(parts[1]), Integer.valueOf(parts[2]), Integer.valueOf(parts[3]))));
                    case "A" -> mapObjects.add(Pair.of("aventurier", new Aventurier(parts[1], Integer.valueOf(parts[2]), 
                            Integer.valueOf(parts[3]), parts[4], parts[5])));
                }
            }
        });
        return mapObjects;
    }
    
    public List<Object> partieCarteTresor(List<Pair<String, Object>> debutMap) {
        Carte carte = (Carte) debutMap.stream()
                .filter(map -> map.getFirst().equals("carte"))
                .findFirst()
                .get()
                .getSecond();
        List<Montagne> montagnes = debutMap.stream()
                .filter(map -> map.getFirst().equals("montagne"))
                .map(pair -> (Montagne) pair.getSecond())
                .collect(Collectors.toList());
        List<Tresor> tresors = debutMap.stream()
                .filter(map -> map.getFirst().equals("tresor"))
                .map(pair -> (Tresor) pair.getSecond())
                .collect(Collectors.toList());
        List<Aventurier> joueurs = debutMap.stream()
                .filter(map -> map.getFirst().equals("aventurier"))
                .map(pair -> (Aventurier) pair.getSecond())
                .collect(Collectors.toList());
        
        List<Integer> tours = joueurs.stream().map(joueur -> joueur.parcours().length()).toList();
        int nbTours = Collections.min(tours);
        
        for (int i=0; i <= nbTours; i++) {
            int nbTour = i;
            joueurs.stream().forEach(joueur -> {
                String action = joueur.parcours().substring(nbTour, nbTour);
                Pair<List<Tresor>, List<Aventurier>> infosTresorsJoueurs = this.action(action, joueur, carte, montagnes,
                        tresors, joueurs);
                //tresors.addAll(infosTresorsJoueurs.getFirst());
                //joueurs.addAll(infosTresorsJoueurs.getSecond());
                
            });
        }
        
        return null;
        
    }
    
    public Pair<List<Tresor>, List<Aventurier>> action(String action, Aventurier joueurDebut, Carte carte, List<Montagne> montagnes, List<Tresor> tresors, List<Aventurier> joueurs) {
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
            } else {
                return Pair.of(tresors, joueurs);
            }
        }
        //retirer le joueur ancien + rajouter = maj
        int index = IntStream.range(0, joueurs.size())
                .filter(i -> joueurs.get(i).nom().equals(joueur.nom()))
                .findFirst()
                .orElse(-1);
        joueurs.remove(index);
        joueurs.add(joueur);
        return Pair.of(tresors, joueurs);
    }
    
    public Aventurier mapActionByOrientation(Aventurier a, String action) {
        switch (a.orientation()) {
            case "S" :
                switch (action) {
                    case "A" -> a = new Aventurier(a.nom(), a.x(), a.y() + 1, a.orientation(), a.parcours());
                    case "D" -> a = new Aventurier(a.nom(), a.x(), a.y(), "W", a.parcours());
                    case "G" -> a = new Aventurier(a.nom(), a.x(), a.y(), "E", a.parcours());
                };
                return a;
            case "N" :
                switch (action) {
                    case "A" -> a = new Aventurier(a.nom(), a.x(), a.y() - 1, a.orientation(), a.parcours());
                    case "D" -> a = new Aventurier(a.nom(), a.x(), a.y(), "E", a.parcours());
                    case "G" -> a = new Aventurier(a.nom(), a.x(), a.y(), "W", a.parcours());
                };
                return a;
            case "E" :
                switch (action) {
                    case "A" -> a = new Aventurier(a.nom(), a.x() + 1, a.y(), a.orientation(), a.parcours());
                    case "D" -> a = new Aventurier(a.nom(), a.x(), a.y(), "S", a.parcours());
                    case "G" -> a = new Aventurier(a.nom(), a.x(), a.y(), "N", a.parcours());
                };
                return a;
            case "W" :
                switch (action) {
                    case "A" -> a = new Aventurier(a.nom(), a.x() - 1, a.y(), a.orientation(), a.parcours());
                    case "D" -> a = new Aventurier(a.nom(), a.x(), a.y(), "N", a.parcours());
                    case "G" -> a = new Aventurier(a.nom(), a.x(), a.y(), "S", a.parcours());
                }
                return a;
            default : break;
        }
        return a;
    }
    
    public void createFile(List<Object> infosForFile) {
        
    }

    public record Carte(int largeurX, int hauteurY) {
    }

}
