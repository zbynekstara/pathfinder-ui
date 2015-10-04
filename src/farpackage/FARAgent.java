package farpackage;

import generalpackage.*;
import adtpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class FARAgent implements Printable {
    private final Field field;

    private final FARPathfinder pathfinder;

    private final FAR farAlgorithm = new FAR();

    public final int FARAGENT_ID;

    public final Element START;
    public final Element GOAL;

    private int agentGroup;
    
    private List initialAgentPath = new List();
    // the initial FAR path
    
    private List futureAgentPath = new List();
    // path segment that would be the result of future reservations
    
    private List finalAgentPath = new List();
    // the final, coordinated path, built by the algorithm
    // updated with each step of simulation
    
    private int honoredInitialPathMovement;
    // how far along the initial path are we
    // 0 means we have not started (honoredReservations is empty)
    // 0 means we are at the start place
    // 1 means we have moved at least once
    
    private int futureInitialPathMovement;
    // how much farther along the initial path we would be if future reservations were fulfilled
    
    private List honoredReservations = new List();
    // reservations that were completed and the agent moved to their elements
    
    private List futureReservations = new List();
    // reservations that are currently planned for the agent but may still be removed
    
    //private Reservation firstReservation = null; // first of honored OR if none there, first of future
    //private Reservation lastHonoredReservation = null; // last of honored reservations
    //private Reservation lastReservation = null; // last of future reservations

    private boolean isComplete;
    private boolean isFailure;

    // first non-initial reservation is a normal reservation of the starting position
    
    public FARAgent(Field field, FARPathfinder pathfinder, Element start, Element goal, int id) {
        this.field = field;

        this.pathfinder = pathfinder;

        FARAGENT_ID = id;

        START = start;
        GOAL = goal;

        farAlgorithm.resetAExtensions(field);

        initialAgentPath = farAlgorithm.far(start, goal, field);
        
        honoredInitialPathMovement = 0;
        futureInitialPathMovement = 0;
        
        isComplete = false;
        isFailure = false;
    }

    public void setAgentGroup(int agentGroup) {
        this.agentGroup = agentGroup;
    }
    
    public int getAgentGroup() {
        return agentGroup;
    }

    public void enqueueFutureReservation(Reservation futureReservation) {
        futureReservations.insertAtRear(futureReservation);
        futureAgentPath.insertAtRear(futureReservation.getElement());
        if (futureReservation.isNormalReservation()) futureInitialPathMovement += 1;
    }
    
    public Reservation removeFutureReservations(int step) {
        Reservation firstRemovedReservation = null;
        
        if (step >= honoredReservations.size()) {
            int futureReservationsIndex = step - honoredReservations.size();
            int counter = 0;
            while (futureReservationsIndex < futureReservations.size()) {                
                Reservation reservationToRemove = (Reservation) futureReservations.removeNode(futureReservationsIndex);
                pathfinder.printToScreen("Removing reservation: "+reservationToRemove);
                if (counter == 0) firstRemovedReservation = reservationToRemove;
                
                futureAgentPath.removeNode(futureReservationsIndex);
                if (reservationToRemove.isNormalReservation()) futureInitialPathMovement -= 1;
                
                // not necessary to increase futureReservationsIndex because size decreases with removals
                counter += 1;
            }
        }
        
        return firstRemovedReservation;
    }
    
    public Reservation honorFutureReservation() {
        Reservation reservationToHonor = (Reservation) futureReservations.removeFirst();
        futureAgentPath.removeFirst();
        
        honoredReservations.insertAtRear(reservationToHonor);
        finalAgentPath.insertAtRear(reservationToHonor.getElement());
        
        // only modify honoredInitialPathMovement for normal reservations
        if (reservationToHonor.isNormalReservation()) {
            futureInitialPathMovement -= 1;            
            honoredInitialPathMovement += 1;
        }
        
        return reservationToHonor;
    }
        
    public Reservation getFirstReservation() {
        if (!honoredReservations.isEmpty()) return (Reservation) honoredReservations.getNodeData(0);
        else if (!futureReservations.isEmpty()) return (Reservation) futureReservations.getNodeData(0);
        else return null;
    }

    public Reservation getLastHonoredReservation() {
        if (!honoredReservations.isEmpty()) return (Reservation) honoredReservations.getLastNodeData();
        else return null;
    }

    public Reservation getLastReservation() {
        if (!futureReservations.isEmpty()) return (Reservation) futureReservations.getLastNodeData();
        else if (!honoredReservations.isEmpty()) return (Reservation) honoredReservations.getLastNodeData();
        else return null;
    }
    
    public int getHonoredInitialPathMovement() {
        return honoredInitialPathMovement;
    }
    
    public int getFutureInitialPathMovement() {
        return futureInitialPathMovement;
    }
    
    public double getHonoredInitialPathCompletion() {
        double honoredInitialPathCompletion = honoredInitialPathMovement / honoredReservations.size();
        return honoredInitialPathCompletion;
    }
    
    public double getFutureInitialPathCompletion() {
        double futureInitialPathCompletion = futureInitialPathMovement / futureReservations.size();
        return futureInitialPathCompletion;
    }
    
    public Element getPathElement(int step) {
        if (step < finalAgentPath.size()) {
            // if step has already been commited to final path
            int index = step;
            return (Element) finalAgentPath.getNodeData(index);
        }
        else if (step < (finalAgentPath.size() + futureAgentPath.size())) {
            // if step is not in final path but is in future path as it looks right now
            int index = step - finalAgentPath.size();
            return (Element) futureAgentPath.getNodeData(index);
        }
        else {
            // if step is not in final or future path, it is in initial path
            // use the initialPathMovement data to determine where we are right now along initial path
            // e.g. if we want info for step 20, but we have honored up to 17 and future has additional 1 = 18
            // we need to look what happens 20 - 18 = 2 steps after the future step
            // but initialAgentPath only reflects movement during reservations
            // if we moved by 6 in honored and 0 in future, we need to look at step 6 + 0 + 2 = 8 of initial path
            int index = honoredInitialPathMovement + futureInitialPathMovement + (step - (finalAgentPath.size() + futureAgentPath.size()));
            return (Element) initialAgentPath.getNodeData(index);
        }
    }
    
    public int getHonoredReservationListSize() {
        int honoredPathSize = honoredReservations.size();
        return honoredPathSize;
    }
    
    public int getFutureReservationListSize() {
        int futurePathSize = futureReservations.size();
        return futurePathSize;
    }
    
    public int getAgentReservationListSize() {
        int agentPathSize = honoredReservations.size() + (initialAgentPath.size() - honoredInitialPathMovement);
        return agentPathSize;
    }

    public Element getFinalPathElement(int step) {
        return (Element) finalAgentPath.getNodeData(step);
    }
    
    public List getFinalAgentPathUntilStep(int step) {
        List path = new List();

        for (int i = 0; i <= step; i++) {
            Element currentElement = this.getFinalPathElement(i);
            path.insertAtRear(currentElement);
        }

        return path;
    }
    public int getFinalAgentPathSize() {
        return finalAgentPath.size();
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

        return string;
    }
}
