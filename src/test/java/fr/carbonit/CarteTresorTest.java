package fr.carbonit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
    
    String fileName = "src/main/resources/final.txt";
    
    @AfterEach
    @BeforeEach
    public void cleanUpFiles() {
        File targetFile = new File(fileName);
        targetFile.delete();
    }
    
    @Test
    void parseFile_shouldInjectListsWithValuesFromFile() throws IOException {
        List<String> allLines = carteTresor.parseFile("debut.txt");
        
        assertThat(carteTresor.carte, is(new Carte(3, 4)));
        var tresor1 = new Tresor(0, 3, 2);
        var tresor2 = new Tresor(1, 3, 3);
        assertThat(carteTresor.tresors, contains(tresor1, tresor2));
        var m1 = new Montagne(1, 0);
        var m2 = new Montagne(2, 1);
        assertThat(carteTresor.montagnes, contains(m1, m2));
        var a1 = new Aventurier("Lara", 1, 1, "S", "AADADAGGA", 0, 0);
        assertThat(carteTresor.aventuriers, contains(a1));
        assertThat(allLines, contains("C - 3 - 4", 
        		"M - 1 - 0",
    			"M - 2 - 1",
        		"T - 0 - 3 - 2",
        		"T - 1 - 3 - 3",
        		"A - Lara - 1 - 1 - S - AADADAGGA"));
        assertThat(carteTresor.linesCarteAndMontagnes, contains("C - 3 - 4", 
        		"M - 1 - 0",
    			"M - 2 - 1"));
    }
    
    @Test 
    void action_shouldReturnNewPositionsOfPlayersAndNumberOfTresors_playerLara() {
    	setupParameters();
    	Aventurier lara = new Aventurier("Lara", 0, 2, "S", "AA", 0, 0);
        Aventurier bern =  new Aventurier("Bern", 3, 2, "N", "DA", 0, 1);
        
        Pair<Set<Tresor>, Set<Aventurier>> result = carteTresor.action("A", lara);
        assertThat(result.getFirst(), contains(new Tresor(0, 3, 1), new Tresor(1, 3, 3)));
        Aventurier newLara = new Aventurier("Lara", 0, 3, "S", "AA", 1, 0);
        assertThat(result.getSecond(), contains(newLara, bern));
        
    }
    
    @Test 
    void action_shouldReturnNewPositionsOfPlayersAndNumberOfTresors_playerBern() {
    	setupParameters();
    	Aventurier lara = new Aventurier("Lara", 0, 2, "S", "AA", 0, 0);
        Aventurier bern =  new Aventurier("Bern", 3, 2, "N", "DA", 0, 1);
        Pair<Set<Tresor>, Set<Aventurier>> result2 = carteTresor.action("D", bern);
        assertThat(result2.getFirst(), contains(new Tresor(0, 3, 2), new Tresor(1, 3, 3)));
        Aventurier newBern =  new Aventurier("Bern", 3, 2, "E", "DA", 0, 1);
        assertThat(result2.getSecond(), contains(lara, newBern));
        
    }
    
    @Test 
    void action_shouldReturnNewPositionsOfPlayersAndNumberOfTresors_collision() {
    	setupParameters();
    	Aventurier lara = new Aventurier("Lara", 3, 1, "E", "AA", 0, 0);
    	carteTresor.aventuriers = new LinkedHashSet<>();
        carteTresor.aventuriers.add(lara);
        Pair<Set<Tresor>, Set<Aventurier>> result2 = carteTresor.action("A", lara);
        assertThat(result2.getFirst(), contains(new Tresor(0, 3, 2), new Tresor(1, 3, 3)));
        assertThat(result2.getSecond(), contains(lara));
    }
    
    @Test 
    void action_shouldReturnNewPositionsOfPlayersAndNumberOfTresors_checkLimites() {
    	setupParameters();
    	Aventurier lara = new Aventurier("Lara", 0, 0, "N", "AA", 0, 0);
    	carteTresor.aventuriers = new LinkedHashSet<>();
        carteTresor.aventuriers.add(lara);
        Pair<Set<Tresor>, Set<Aventurier>> result2 = carteTresor.action("A", lara);
        assertThat(result2.getFirst(), contains(new Tresor(0, 3, 2), new Tresor(1, 3, 3)));
        assertThat(result2.getSecond(), contains(lara));
    }
    
    @ParameterizedTest 
    @ValueSource(strings = {"S", "W"})
    void mapActionByOrientation_shouldReturnNewPosition(String orientation) {
        var a1 = new Aventurier("Lara", 2, 1, orientation, "AADADAGGA", 0, 0);
        Aventurier result = carteTresor.mapActionByOrientation(a1, "D");
        switch (orientation) {
            case "S" -> assertThat(result.orientation(), is("W"));
            case "W" -> assertThat(result.orientation(), is("N"));
        }
    }
    
    @Test
    void createFile_shouldCreateFileAfterPlay() throws IOException {
    	setupParameters();
    	carteTresor.carte = new Carte(5, 4);
    	carteTresor.linesCarteAndMontagnes = Arrays.asList("C - 5 - 4",
    			"M - 1 - 0",
    			"M - 3 - 1");
    	carteTresor.createFile(fileName);
    	//le fichier apparaitra dans /src/main/resources
    	InputStream expected = getClass().getClassLoader().getResourceAsStream("expected_create.txt");
    	InputStream result = getClass().getClassLoader().getResourceAsStream("final.txt");
    	assertThat(expected.equals(result), is(true));
    }
    
    private void setupParameters() {
    	carteTresor.carte = new Carte(3, 4);
    	carteTresor.tresors = Stream.of(new Tresor(0, 3, 2), new Tresor(1, 3, 3)).collect(Collectors.toSet());
    	carteTresor.montagnes = Stream.of(new Montagne(1, 0), new Montagne(3, 1)).collect(Collectors.toSet());
    	Aventurier lara = new Aventurier("Lara", 0, 2, "S", "AA", 0, 0);
        Aventurier bern =  new Aventurier("Bern", 3, 2, "N", "DA", 0, 1);
        carteTresor.aventuriers = new LinkedHashSet<>();
        carteTresor.aventuriers.add(lara);
        carteTresor.aventuriers.add(bern);
    }
    
}