package apackage;

import adtpackage.*;
import generalpackage.*;
import guipackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class APathfinder implements Pathfinder {
    private GUI gui;
    private Field field;
    private AAgent[] aAgents;
    int step = 0;

    public APathfinder(GUI gui, Field field) {
        this.gui = gui;
        this.field = field;

        for (int i = 1; i < (field.X_DIMENSION - 1); i++) {
           for (int j = 1; j < (field.Y_DIMENSION - 1); j++) {
               field.getElement(i, j).setAExtension(new AExtension());
           }
        }
    }

    public void findAgentPaths(){
        aAgents = new AAgent[field.getAgentArray().length];

        for (int i = 0; i < field.getAgentArray().length; i++) {
            Agent agent = field.getAgentArray()[i];

            AAgent newAAgent = new AAgent(field, agent.getStart(), agent.getGoal(), i);

            aAgents[i] = newAAgent;
        }
    }

    public int getNumFailures() {
        return 0;
    }

    public Element [] getAgentsAtStep(int drawStep) {
        Element [] elementArray = new Element[aAgents.length];

        for (int i = 0; i < aAgents.length; i++) {
            AAgent currentAgent = aAgents[i];
            if (drawStep < currentAgent.getAgentPath().size()) {
                elementArray[i] = (Element) currentAgent.getAgentPath().getNodeData(drawStep);
            } else {
                elementArray[i] = currentAgent.GOAL;
            }
        }

        return elementArray;
    }

    public List [] getAgentPathsUntilStep(int drawStep) {
        List[] pathArray = new List[aAgents.length];

        for (int i = 0; i < aAgents.length; i++) {
            AAgent currentAgent = aAgents[i];
            pathArray[i] = currentAgent.getAgentPathUntilStep(drawStep);
        }

        return pathArray;
    }

    @Override
    public String toString() {
        return ("Pathfinder at step " + step + ": Number of AAgents: " + aAgents.length);
    }
}
