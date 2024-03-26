package fr.carbonit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.opengamma.strata.collect.tuple.Pair;

import fr.carbonit.pojo.Aventurier;
import fr.carbonit.pojo.Montagne;
import fr.carbonit.pojo.Tresor;

public class CarteTresor {

	Carte carte;
	List<Montagne> montagnes = new ArrayList<>();
	List<Tresor> tresors = new ArrayList<>();
	List<Aventurier> aventuriers = new ArrayList<>();
	Pair<List<Tresor>, List<Aventurier>> infosTresorsJoueurs;
	List<String> linesCarteAndMontagnes = new ArrayList<>();
	int ordreFichier = -1;
	List<Aventurier> playersInGame = new ArrayList<>();

	public List<String> parseFile(String fileName) throws IOException {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream(fileName);
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		List<String> allLines = new ArrayList<>();
		String l;
		while ((l = r.readLine()) != null) {
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
					case "A" -> {
						ordreFichier++;
						aventuriers.add(new Aventurier(parts[1], Integer.valueOf(parts[2]), Integer.valueOf(parts[3]),
								parts[4], parts[5], 0, ordreFichier));
					}
					default -> throw new IllegalArgumentException();
				}
			}
		});
		return allLines;
	}

	public Pair<List<Tresor>, List<Aventurier>> partieCarteTresor() {
		List<Integer> tours = aventuriers.stream().map(joueur -> joueur.parcours().length()).toList();
		int nbTours = Collections.min(tours);

		playersInGame = aventuriers;
		for (int i = 0; i <= nbTours; i++) {
			int nbTour = i;
			if (i == nbTours) {
				return infosTresorsJoueurs;
			}
			List<Aventurier> players = new ArrayList<>(playersInGame);
			players.stream().forEach(joueur -> {
				String action = joueur.parcours().substring(nbTour, nbTour + 1);
				infosTresorsJoueurs = action(action, joueur);
				playersInGame = infosTresorsJoueurs.getSecond();
			});
		}
		return infosTresorsJoueurs;
	}

	public Pair<List<Tresor>, List<Aventurier>> action(String action, Aventurier joueurDebut) {
		Aventurier joueur = mapActionByOrientation(joueurDebut, action);
		//check limites
		var hitLimit = joueur.x() < 0 || joueur.x() > carte.largeurX() || joueur.y() < 0 || joueur.y() > carte.hauteurY();
		if (hitLimit) {
			return Pair.of(tresors, aventuriers);
		}
		// check collision montagne
		for (Montagne montagne : montagnes) {
			if (joueur.x() == montagne.x() && joueur.y() == montagne.y()) {
				return Pair.of(tresors, aventuriers);
			}
		}

		//check position : si pareille mais orientation diff alors changer uniquement l'orientation et return
		var samePosition = joueur.x() == joueurDebut.x() && joueur.y() == joueurDebut.y() && !joueurDebut.orientation().equals(joueur.orientation());
		if (samePosition) {
			//retourner joueur sans retirer de tresor
			joueur = new Aventurier(joueur.nom(), joueur.x(), joueur.y(), joueur.orientation(), joueur.parcours(),
					joueurDebut.nbTresors(), joueur.ordre());
			List<Aventurier> newList = aventuriers;
			List<Aventurier> playersToRemove = new ArrayList<>();
			for (Aventurier a : aventuriers) { // TODO stream filter findFirst ?
				if (a.nom().equalsIgnoreCase(joueur.nom())) {
					playersToRemove.add(a);
				}
			}
			newList.removeAll(playersToRemove);
			newList.add(joueur);
			List<Aventurier> orderedListByAdvOrder = new ArrayList<>();
			newList.stream().sorted(Comparator.comparing(Aventurier::ordre)).forEach(adv -> orderedListByAdvOrder.add(adv));
			return Pair.of(tresors, orderedListByAdvOrder);
		}
		// modifier nb tresors
		for (Tresor tresor : tresors) {
			var positionActuelleEgaleATresor = tresor.getX() == joueur.x() && tresor.getY() == joueur.y();
			if (positionActuelleEgaleATresor && tresor.getNbTresors() > 0) {
				tresor.setNbTresors(tresor.getNbTresors() - 1);
				// calcul nb tresor joueur
				joueur = new Aventurier(joueur.nom(), joueur.x(), joueur.y(), joueur.orientation(), joueur.parcours(),
						joueur.nbTresors() + 1, joueur.ordre());
			}
		}
		//maj liste des joueurs avec nouvelles positions, orientation et tresors recoltés
		List<Aventurier> newList = aventuriers;
		if (!joueurDebut.equals(joueur)) {
			List<Aventurier> newListToMaj = aventuriers;
			List<Aventurier> newPlayersToRemove = new ArrayList<>();
			// retirer le joueur ancien + rajouter = maj si le joueur a changé
			for (Aventurier a : aventuriers) {
				if (a.nom().equalsIgnoreCase(joueur.nom())) {
					newPlayersToRemove.add(a);
				}
			}
			newListToMaj.removeAll(newPlayersToRemove);
			newListToMaj.add(joueur);
			newList = newListToMaj;
		}
		List<Aventurier> orderedListByAdvOrder = new ArrayList<>();
		newList.stream().sorted(Comparator.comparing(Aventurier::ordre)).forEach(adv -> orderedListByAdvOrder.add(adv));
		return Pair.of(tresors, orderedListByAdvOrder);
	}

	public Aventurier mapActionByOrientation(Aventurier a, String action) {
		Aventurier newPosition = a;
		switch (a.orientation()) {
		case "S":
			switch (action) {
			case "A" -> newPosition = new Aventurier(a.nom(), a.x(), a.y() + 1, a.orientation(), a.parcours(),
					a.nbTresors(), a.ordre());
			case "D" ->
				newPosition = new Aventurier(a.nom(), a.x(), a.y(), "W", a.parcours(), a.nbTresors(), a.ordre());
			case "G" ->
				newPosition = new Aventurier(a.nom(), a.x(), a.y(), "E", a.parcours(), a.nbTresors(), a.ordre());
			};
			return newPosition;
		case "N":
			switch (action) {
			case "A" -> newPosition = new Aventurier(a.nom(), a.x(), a.y() - 1, a.orientation(), a.parcours(),
					a.nbTresors(), a.ordre());
			case "D" ->
				newPosition = new Aventurier(a.nom(), a.x(), a.y(), "E", a.parcours(), a.nbTresors(), a.ordre());
			case "G" ->
				newPosition = new Aventurier(a.nom(), a.x(), a.y(), "W", a.parcours(), a.nbTresors(), a.ordre());
			}
			;
			return newPosition;
		case "E":
			switch (action) {
			case "A" -> newPosition = new Aventurier(a.nom(), a.x() + 1, a.y(), a.orientation(), a.parcours(),
					a.nbTresors(), a.ordre());
			case "D" ->
				newPosition = new Aventurier(a.nom(), a.x(), a.y(), "S", a.parcours(), a.nbTresors(), a.ordre());
			case "G" ->
				newPosition = new Aventurier(a.nom(), a.x(), a.y(), "N", a.parcours(), a.nbTresors(), a.ordre());
			}
			;
			return newPosition;
		case "W":
			switch (action) {
			case "A" -> newPosition = new Aventurier(a.nom(), a.x() - 1, a.y(), a.orientation(), a.parcours(),
					a.nbTresors(), a.ordre());
			case "D" ->
				newPosition = new Aventurier(a.nom(), a.x(), a.y(), "N", a.parcours(), a.nbTresors(), a.ordre());
			case "G" ->
				newPosition = new Aventurier(a.nom(), a.x(), a.y(), "S", a.parcours(), a.nbTresors(), a.ordre());
			}
			return newPosition;
		default:
			break;
		}
		return newPosition;
	}

	public void createFile(String fileNameOutput) throws IOException {
		String separator = " - ";
		List<String> allLines = new ArrayList<>();
		allLines.addAll(linesCarteAndMontagnes);
		tresors.stream()
				.map(tresor -> "T - " + tresor.getX() + separator + tresor.getY() + separator + tresor.getNbTresors())
				.forEach(line -> allLines.add(line));
		aventuriers.stream()
				.map(a -> "A - " + a.nom() + separator + a.x() + separator + a.y() + separator + a.orientation() + separator + a.nbTresors())
				.forEach(line -> allLines.add(line));
		//empty line
		allLines.add("");
		
		List<Integer> listTailleNoms = aventuriers.stream().map(a -> a.nom().length()).toList();
		int espacesMax = Collections.max(listTailleNoms) + 3; // Lettre + ( + )

		String[][] matrice = new String[carte.largeurX()][carte.hauteurY()];

		PrintStream fileStream = new PrintStream(fileNameOutput);
		System.setOut(fileStream);
		allLines.stream().forEach(line -> System.out.println(line));

		for (int row = 0; row < matrice.length; row++) {
			for (int col = 0; col < matrice[row].length; col++) {
				int index = row;
				int indexJ = col;
				
				// par défaut on sette des points sur toute la matrice
				matrice[index][indexJ] = "." + StringUtils.repeat(" ", espacesMax);
				montagnes.stream().forEach(m -> {
					if (m.x() == index && m.y() == indexJ) {
						matrice[index][indexJ] = "M" + StringUtils.repeat(" ", espacesMax);
					}
				});
				tresors.stream().forEach(t -> {
					if (t.getX() == index && t.getY() == indexJ) {
						var stringNrTresors = String.valueOf(t.getNbTresors());
						var spacesToAdd = espacesMax - stringNrTresors.length() - 2; //2 parentheses
						matrice[index][indexJ] = "T(" + t.getNbTresors() + ")" + StringUtils.repeat(" ", spacesToAdd);
					}
				});
				aventuriers.stream().forEach(a -> {
					if (a.x() == index && a.y() == indexJ) {
						var spacesToAdd = espacesMax - a.nom().length() - 2;
						matrice[index][indexJ] = "A(" + a.nom() + ")" + StringUtils.repeat(" ", spacesToAdd);
					}
				});
				
				System.out.println(matrice[index][indexJ]);
			}
		}
		System.setOut(fileStream);
	}

	public record Carte(int largeurX, int hauteurY) {
	}

}
