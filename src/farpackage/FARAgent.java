package farpackage;

import generalpackage.*;
import adtpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class FARAgent implements Printable {
    private Field field;

    private FARPathfinder pathfinder;

    private FAR farAlgorithm = new FAR();

    public final int FARAGENT_ID;

    public final Element START;
    public final Element GOAL;

    //private List initialAgentPath; // the initial, pre-computed A* path
    private List agentPath = new List (); // the final, coordinated FAR path

    //private int reservationOffset = 0; // when changing reservations, how much should be subtracted

    private int agentGroup;

    private Reservation firstReservation = null;
    private Reservation lastHonoredReservation = null;
    private Reservation lastReservation = null;

    private boolean isComplete = false;

    public FARAgent(Field field, FARPathfinder pathfinder, Element start, Element goal, int id) {
        this.field = field;

        this.pathfinder = pathfinder;

        FARAGENT_ID = id;

        START = start;
        GOAL = goal;

        farAlgorithm.resetAExtensions(field);

        agentPath = farAlgorithm.far(start, goal, field);
    }
    
    public void setAgentPath(List agentPath) {
        this.agentPath = agentPath;
    }
    public List getAgentPath() { // RETURNING A COPY, NOT THE PATH ITSELF
        List returnPath = new List();

        for (int i = 0; i < agentPath.size(); i++) {
            returnPath.insertAtRear((Element) agentPath.getNodeData(i));
        }
        
        return returnPath;
    }

    public Element getPathElement(int step) {
        return (Element) agentPath.getNodeData(step);
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

    /*public void setReservationOffset(int reservationOffset) {
        this.reservationOffset = reservationOffset;
    }
    public int getReservationOffset() {
        return reservationOffset;
    }*/

    public void setAgentGroup(int agentGroup) {
        this.agentGroup = agentGroup;
    }
    public int getAgentGroup() {
        return agentGroup;
    }

    public void setFirstReservation(Reservation firstReservation) {
        this.firstReservation = firstReservation;
    }
    public Reservation getFirstReservation() {
        return firstReservation;
    }

    public void setLastHonoredReservation(Reservation lastHonoredReservation) {
        this.lastHonoredReservation = lastHonoredReservation;
    }
    public Reservation getLastHonoredReservation() {
        return lastHonoredReservation;
    }

    public void setLastReservation(Reservation lastReservation) {
        this.lastReservation = lastReservation;
    }
    public Reservation getLastReservation() {
        return lastReservation;
    }

    public void setIsComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }
    public boolean isComplete() {
        return isComplete;
    }

    @Override public String print() {
        return toString();
    }

    @Override public String toString() {
        String string = "";

        string += "FARAgent ID: ";
        string += FARAGENT_ID;
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
