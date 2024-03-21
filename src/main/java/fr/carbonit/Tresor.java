package fr.carbonit;

public class Tresor {
    int x;
    int y;
    int nbTresors;
    
    public Tresor(int x, int y, int nbTresors) {
        super();
        this.x = x;
        this.y = y;
        this.nbTresors = nbTresors;
    }
    
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    public int getNbTresors() {
        return nbTresors;
    }
    public void setNbTresors(int nbTresors) {
        this.nbTresors = nbTresors;
    }

    @Override
    public String toString() {
        return "Tresor [x=" + x + ", y=" + y + ", nbTresors=" + nbTresors + "]";
    }
    
}
