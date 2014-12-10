package generalpackage;

import apackage.*;
import farpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class Element implements Printable {
    // IMPORTANT: the setValueAt methods use ordering of (…, y, x); the Elements have (x, y)

    private class ElementExtension {
        private AExtension aExtension;
        private FARExtension farExtension;
        // other extensions will be here

        public ElementExtension() {
            // aExtension = new AExtension();
        }

        // A* algorithm
        public AExtension getAExtension() {
            return aExtension;
        }
        public void setAExtension(AExtension aExtension) {
            this.aExtension = aExtension;
        }

        // FAR algorithm
        public FARExtension getFARExtension() {
            return farExtension;
        }
        public void setFARExtension(FARExtension farExtension) {
            this.farExtension = farExtension;
        }
    }

    public final int X_ID;
    public final int Y_ID;

    private boolean isBoundary = false;
    private boolean isObstacle = false;

    // for use by field hole-filling algorithm
    private int freeGroup = -999;

    // for use by search algorithms
    private ElementExtension extension = new ElementExtension();

    // for drawing the field table
    //private boolean isStart = false;
    private int startForAgent = -999; // ID of agent that starts here

    //private boolean isGoal = false;
    private int goalForAgent = -999;

    public Element(int x_id, int y_id) {
        this.X_ID = x_id;
        this.Y_ID = y_id;
    }
    public Element(int x_id, int y_id, boolean isBoundary, boolean isObstacle) {
        this.X_ID = x_id;
        this.Y_ID = y_id;

        this.isBoundary = isBoundary;
        this.isObstacle = isObstacle;
    }

    public void setIsObstacle(boolean isObstacle) {
        this.isObstacle = isObstacle;
    }

    public void setFreeGroup(int freeGroup) {
        this.freeGroup = freeGroup;
    }

    public void setAExtension(AExtension aExtension) {
        extension.setAExtension(aExtension);
    }
    public void setFARExtension(FARExtension farExtension) {
        extension.setFARExtension(farExtension);
    }

    public void setStartForAgent(int startForAgent) {
        this.startForAgent = startForAgent;
    }
    public void setGoalForAgent(int goalForAgent) {
        this.goalForAgent = goalForAgent;
    }

    /*public void setIsStart(boolean isStart) {
        this.isStart = isStart;
    }
    public void setIsGoal(boolean isGoal) {
        this.isGoal = isGoal;
    }*/

    public int getFreeGroup() {
        return freeGroup;
    }

    public AExtension getAExtension() {
        return extension.getAExtension();
    }
    public FARExtension getFARExtension() {
        return extension.getFARExtension();
    }

    public int getStartForAgent() {
        return startForAgent;
    }
    public int getGoalForAgent() {
        return goalForAgent;
    }

    public boolean isBoundary() {
        return isBoundary;
    }
    public boolean isObstacle() {
        return isObstacle;
    }
    public boolean isBlocked() {
        return (isBoundary || isObstacle);
    }

    /*public boolean isStart() {
        return isStart;
    }
    public boolean isGoal() {
        return isGoal;
    }*/

    public boolean isEqual(Element element) {
        if (element == null) return false;
        else return((X_ID == element.X_ID) && (Y_ID == element.Y_ID));
    }

    public int getKey(int fieldDimension) {
        return ((X_ID - 1) + ((Y_ID - 1) * (fieldDimension - 2)));
    }

    @Override public String print() {
        return toString();
    }

    @Override public String toString() {
        return ("[" + X_ID + ", " + Y_ID + "]");
    }
}
