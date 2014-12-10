package apackage;

import adtpackage.*;
import generalpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class AAgent implements Printable {
    private Field field;
    private AStar aStarAlgorithm = new AStar();
    private final int AAGENT_ID;
    private List agentPath;
    public final Element START;
    public final Element GOAL;

    public AAgent(Field field, Element start, Element goal, int id) {
        this.field = field;

        AAGENT_ID = id;
        aStarAlgorithm.resetAExtensions(field);

        agentPath = aStarAlgorithm.aStar(start, goal, field);

        START = start;
        GOAL = goal;
    }

    public List getAgentPath() {
        return agentPath;
    }
    public List getAgentPathUntilStep(int step) {
        List path = new List();

        for (int i = 0; i <= step; i++) {
            if (i < (getAgentPath().size())) {
                Element currentElement = (Element) agentPath.getNodeData(i);
                path.insertAtRear(currentElement);
            }
        }

        return path;
    }

    public int getAgentID() {
        return AAGENT_ID;
    }

    @Override public String print() {
        return toString();
    }

    @Override public String toString() {
        String string = "";
        string += "AAgent ID: ";
        string += AAGENT_ID;
        string += "; Start: ";
        string += START.print();
        string += "; Goal: ";
        string += GOAL.print();
        string += "; Path: [";
        string += agentPath.print();
        string += "]";
        return string;
    }
}
