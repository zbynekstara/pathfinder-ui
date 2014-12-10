package apackage;

import generalpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class AExtension {
    private double gScore = -999; // cost along best known path = distance
    private double hScore = -999; // heuristics
    private double fScore = -999; // distance + heuristics combined

    private Element cameFrom = null;

    public AExtension() {

    }

    public void setScores(double gScore, double hScore, double fScore) {
        this.gScore = gScore;
        this.hScore = hScore;
        this.fScore = fScore;
    }

    public void setGScore(double gScore) {
        this.gScore = gScore;
    }
    public void setHScore(double hScore) {
        this.hScore = hScore;
    }
    public void setFScore(double fScore) {
        this.fScore = fScore;
    }

    public double getGScore() {
        return gScore;
    }
    public double getHScore() {
        return hScore;
    }
    public double getFScore() {
        return fScore;
    }

    public void setCameFrom(Element element) {
        cameFrom = element;
    }
    public Element getCameFrom() {
        return cameFrom;
    }
}
