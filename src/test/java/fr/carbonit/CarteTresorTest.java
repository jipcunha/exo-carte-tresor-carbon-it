package fr.carbonit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

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
    void parseFile_shouldCreateObjectListFromFile() throws IOException {
        File file = getFileFromRessource("/debut.txt", "test.txt");
        List<Pair<String, Object>> objects = carteTresor.parseFile(file);
        var carte = new Carte(3, 4);
        var tresor1 = new Tresor(0, 3, 2);
        var tresor2 = new Tresor(1, 3, 3);
        var a1 = new Aventurier("Lara", 1, 1, "S", "AADADAGGA");
        assertThat(objects, contains(carte, tresor1, tresor2, a1));
    }
    
    @ParameterizedTest 
    @ValueSource(strings = {"A", "D"})
    void action_shouldReturnNewPositionsOfPlayersAndNumberOfTresors(String action) {
        var carte = new Carte(3, 4);
        List<Tresor> t = Arrays.asList(new Tresor(0, 3, 2), new Tresor(1, 3, 3));
        List<Montagne> m = Arrays.asList(new Montagne(1, 0), new Montagne(3, 1));
        Aventurier lara = new Aventurier("Lara", 0, 2, "S", "AA");
        Aventurier bern =  new Aventurier("Bern", 3, 2, "N", "DA");
        List<Aventurier> a = Arrays.asList(lara, bern);
        Pair<List<Tresor>, List<Aventurier>> result = carteTresor.action(action, lara, carte, m, t, a);
        
        switch (action) {
            case "A" -> {
                assertThat(result.getFirst(), contains(new Tresor(0, 3, 1), new Tresor(1, 3, 3)));
                Aventurier newLara = new Aventurier("Lara", 0, 3, "S", "AA");
                assertThat(result.getSecond(), contains(newLara, bern));
            }
            case "D" -> {
                assertThat(result.getFirst(), contains(new Tresor(0, 3, 2), new Tresor(1, 3, 3)));
                Aventurier newBern =  new Aventurier("Bern", 3, 2, "E", "DA");
                assertThat(result.getSecond(), contains(lara, newBern));
            }
        }
        
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
  
    private File getFileFromRessource(String path, String fileName) throws IOException {
        InputStream initialStream = this.getClass().getResourceAsStream(path);
        Path file = Files.createFile(testFolder.resolve(fileName));
        //IOUtils.closeQuietly(initialStream);
        return file.toFile();
    }
}