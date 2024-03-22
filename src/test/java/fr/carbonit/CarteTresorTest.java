package fr.carbonit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.opengamma.strata.collect.tuple.Pair;

import fr.carbonit.CarteTresor.Carte;


@ExtendWith(MockitoExtension.class)
class CarteTresorTest {

    @TempDir
    public Path testFolder;
    
    private CarteTresor carteTresor = new CarteTresor();
    
    @Test
    void parseFile_shouldInjectListsWithValuesFromFile() throws IOException {
        List<String> allLines = carteTresor.parseFile("debut.txt");
        
        assertThat(carteTresor.carte, is(new Carte(3, 4)));
        var tresor1 = new Tresor(0, 3, 2);
        var tresor2 = new Tresor(1, 3, 3);
        assertThat(carteTresor.tresors.getSecond(), contains(tresor1, tresor2));
        var m1 = new Montagne(1, 0);
        var m2 = new Montagne(2, 1);
        assertThat(carteTresor.montagnes.getSecond(), contains(m1, m2));
        var a1 = new Aventurier("Lara", 1, 1, "S", "AADADAGGA");
        assertThat(carteTresor.aventuriers.getSecond(), contains(a1));
        assertThat(allLines, contains("C - 3 - 4", 
        		"M - 1 - 0",
    			"M - 2 - 1",
        		"T - 0 - 3 - 2",
        		"T - 1 - 3 - 3",
        		"A - Lara - 1 - 1 - S - AADADAGGA"));
    }
    
    @Test 
    void action_shouldReturnNewPositionsOfPlayersAndNumberOfTresors_playerLara() {
        var carte = new Carte(3, 4);
        Set<Tresor> t = Stream.of(new Tresor(0, 3, 2), new Tresor(1, 3, 3)).collect(Collectors.toSet());
        Set<Montagne> m = Stream.of(new Montagne(1, 0), new Montagne(3, 1)).collect(Collectors.toSet());
        Aventurier lara = new Aventurier("Lara", 0, 2, "S", "AA");
        Aventurier bern =  new Aventurier("Bern", 3, 2, "N", "DA");
        Set<Aventurier> a = Stream.of(lara, bern).collect(Collectors.toSet());
        
        Pair<Set<Tresor>, Set<Aventurier>> result = carteTresor.action("A", lara, carte, m, t, a);
        assertThat(result.getFirst(), contains(new Tresor(0, 3, 1), new Tresor(1, 3, 3)));
        Aventurier newLara = new Aventurier("Lara", 0, 3, "S", "AA");
        assertThat(result.getSecond(), contains(newLara, bern));
        
    }
    
    @Test 
    void action_shouldReturnNewPositionsOfPlayersAndNumberOfTresors_playerBern() {
        var carte = new Carte(3, 4);
        Set<Tresor> t = Stream.of(new Tresor(0, 3, 2), new Tresor(1, 3, 3)).collect(Collectors.toSet());
        Set<Montagne> m = Stream.of(new Montagne(1, 0), new Montagne(3, 1)).collect(Collectors.toSet());
        Aventurier lara = new Aventurier("Lara", 0, 2, "S", "AA");
        Aventurier bern =  new Aventurier("Bern", 3, 2, "N", "DA");
        Set<Aventurier> a = Stream.of(lara, bern).collect(Collectors.toSet());
        
        Pair<Set<Tresor>, Set<Aventurier>> result2 = carteTresor.action("D", bern, carte, m, t, a);
        assertThat(result2.getFirst(), contains(new Tresor(0, 3, 2), new Tresor(1, 3, 3)));
        Aventurier newBern =  new Aventurier("Bern", 3, 2, "E", "DA");
        assertThat(result2.getSecond(), contains(lara, newBern));
        
    }
    
    @ParameterizedTest 
    @ValueSource(strings = {"S", "W"})
    void mapActionByOrientation_shouldReturnNewPosition(String orientation) {
        var a1 = new Aventurier("Lara", 1, 1, orientation, "AADADAGGA");
        Aventurier result = carteTresor.mapActionByOrientation(a1, "D");
        switch (orientation) {
            case "S" -> assertThat(result.orientation(), is("W"));
            case "W" -> assertThat(result.orientation(), is("N"));
        }
    }
  
}