package generalpackage;

import adtpackage.*;
import java.awt.*;

/**
 *
 * @author Zbyněk Stara
 */
public class Agent {
    private Field field;

    private int agentID = -999;

    private Color agentColor = Color.WHITE;

    public int startX = -999;
    public int startY = -999;

    public int goalX = -999;
    public int goalY = -999;

    public Agent(Field field, HashSet freeSet, int id, Color agentColor) throws Exception {
        this.field = field;

        agentID = id;

        this.agentColor = agentColor;

        if (freeSet.size() > 1) {
            do {
                startX = (int) ((Math.random() * (field.X_DIMENSION - 2)) + 1);
                startY = (int) ((Math.random() * (field.Y_DIMENSION - 2)) + 1);
            } while (!freeSet.contains(field.getElement(startX, startY).getKey(field.Y_DIMENSION)));

            field.getElement(startX, startY).setStartForAgent(id);

            freeSet.remove(field.getElement(startX, startY).getKey(field.Y_DIMENSION));

            do {
                goalX = (int) ((Math.random() * (field.X_DIMENSION - 2)) + 1);
                goalY = (int) ((Math.random() * (field.Y_DIMENSION - 2)) + 1);
            } while (!freeSet.contains(field.getElement(goalX, goalY).getKey(field.Y_DIMENSION)));

            field.getElement(goalX, goalY).setGoalForAgent(id);

            freeSet.remove(field.getElement(goalX, goalY).getKey(field.Y_DIMENSION));

            /*AAgent newAAgent = new AAgent(field.getElement(startX, startY), field.getElement(goalX, goalY), field, newAAgentID);
            aAgentSet.add(newAAgent, newAAgentID);
            newAAgentID++;*/

        } else { // not enough free spaces to make the starts and goals
            throw new Exception();
        }
    }

    public int getStartX() {
        return startX;
    }
    public int getStartY() {
        return startY;
    }
    public Element getStart() {
        return field.getElement(startX, startY);
}
    
    public int getGoalX() {
        return goalX;
    }
    public int getGoalY() {
        return goalY;
    }
    public Element getGoal() {
        return field.getElement(goalX, goalY);
    }

    public int getAgentID() {
        return agentID;
    }

    public Color getAgentColor() {
        return agentColor;
    }
}
