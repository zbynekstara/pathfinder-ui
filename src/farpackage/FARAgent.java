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

    private List initialAgentPath = new List();
    // the initial FAR path
    // contains elements directly
    private List agentPath = new List();
    // list of paths as they look right now
    // also contains elements directly
    //// list that contains lists of elements and paths as they look right now
    //// contains lists of elements
    //// updated with each reservation and especially with proxy paths
    private List finalAgentPath = new List();
    // the final, coordinated FAR path, built by the algorithm
    // contains elements directly
    // updated with each step of simulation
    
    private int agentPathSize = 0;
    
    //private int reservationOffset = 0; // when changing reservations, how much should be subtracted

    private int agentGroup;

    private Reservation firstReservation = null;
    private Reservation lastHonoredReservation = null;
    private Reservation lastReservation = null;

    private boolean isComplete = false;
    private boolean isFailure = false;

    public FARAgent(Field field, FARPathfinder pathfinder, Element start, Element goal, int id) {
        this.field = field;

        this.pathfinder = pathfinder;

        FARAGENT_ID = id;

        START = start;
        GOAL = goal;

        farAlgorithm.resetAExtensions(field);

        initialAgentPath = farAlgorithm.far(start, goal, field);
                
        /*// making a copy of initial agent path to be the agent path
        // wrapping each element in a separate list
        Element currentElement;
        List currentElementList;
        for (int i = 0; i < initialAgentPath.size(); i++) {
            currentElement = (Element) agentPath.getNodeData(i);
            currentElementList = new List();

            currentElementList.insertAtRear(currentElement);
            agentPath.insertAtRear(currentElementList);
            agentPathSize += 1;
        }*/
        
        Element currentElement;
        for (int i = 0; i < initialAgentPath.size(); i++) {
            currentElement = (Element) initialAgentPath.getNodeData(i);
            agentPath.insertAtRear(currentElement);
        }
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

    // SHOULD BE REPLACED BY METHODS BELOW
    /*public void setAgentPath(List agentPath) {
        this.agentPath = agentPath;
    }
    public List getAgentPath() { // RETURNING A COPY, NOT THE PATH ITSELF
        List returnPath = new List();

        for (int i = 0; i < agentPath.size(); i++) {
            returnPath.insertAtRear((Element) agentPath.getNodeData(i));
        }

        return returnPath;
    }*/

    // FOR SIMPLICITY, ALL THIS FUNCTIONALITY IS SKIPPED N FAVOR OF SIMPLE LIST
    /*public Element getPathElement(int step) {
        int segmentIndex = 0;
        int elementIndex = 0;

        List currentPathSegment;
        Element currentElement = null;

        while (true) {
            currentPathSegment = (List) agentPath.getNodeData(segmentIndex);

            int elementInSegmentIndex = 0;
            while (elementInSegmentIndex < currentPathSegment.size()) {
                if (elementIndex == step) {
                    currentElement = (Element) currentPathSegment.getNodeData(elementInSegmentIndex);
                    break;
                }
                else {
                    elementIndex += 1;
                    elementInSegmentIndex += 1;
                }
            }

            if (elementIndex == step) break;
            else segmentIndex += 1;
        }
        
        return currentElement;
    }
    public void splitPathSegment(int step) {
        // if there is not a boundary of path segments before step, split segment
        int segmentIndex = 0;
        int elementIndex = 0;

        List currentPathSegment;

        while (true) {
            currentPathSegment = (List) agentPath.getNodeData(segmentIndex);

            int elementInSegmentIndex = 0;
            while (elementInSegmentIndex < currentPathSegment.size()) {
                if (elementIndex == step) {
                    break;
                }
                else {
                    elementIndex += 1;
                    elementInSegmentIndex += 1;
                }
            }

            if (elementIndex == step) {
                // the correct segment has been found

                if (elementInSegmentIndex == 0) {
                    // the requested element is already frst in its segment
                    // nothing needs to be done
                    return;
                }

                Element insertElement;
                List firstNewSegment = new List();
                List secondNewSegment = new List();
                int mid = 0;

                while (mid < elementInSegmentIndex) {
                    // add everything in the segment before the element into firstNewSegment
                    insertElement = (Element) currentPathSegment.getNodeData(mid);
                    firstNewSegment.insertAtRear(insertElement);
                    mid += 1;
                }

                while (mid < currentPathSegment.size()) {
                    // add everything in the segment after and including the element into secondNewSegment
                    insertElement = (Element) currentPathSegment.getNodeData(mid);
                    secondNewSegment.insertAtRear(insertElement);
                    mid += 1;
                }

                agentPath.removeNode(segmentIndex);
                agentPath.insertAsNode(firstNewSegment, segmentIndex);
                agentPath.insertAsNode(secondNewSegment, segmentIndex+1);
            }

            else {
                segmentIndex += 1;
            }
        }
    }
    public void insertPathSegment(int step, List segment) {
        // adds a path segment to be the segment at step, moving all others down

        splitPathSegment(step); // if there is a path segment there, split it
        agentPath.insertAsNode(segment, step); // then insert the segment into the place
        agentPathSize += 1;
    }
    public void insertPathElement(int step, Element element) {
        // add an element to a step to be part of the segment there

        int segmentIndex = 0;
        int elementIndex = 0;

        List currentPathSegment;

        while (true) {
            currentPathSegment = (List) agentPath.getNodeData(segmentIndex);

            int elementInSegmentIndex = 0;
            while (elementInSegmentIndex < currentPathSegment.size()) {
                if (elementIndex == step-1) {
                    break;
                }
                else {
                    elementIndex += 1;
                    elementInSegmentIndex += 1;
                }
            }

            if (elementIndex == step-1 || elementIndex == 0) {
                currentPathSegment.insertAsNode(element, step);
                agentPathSize += 1;
            }
            else {
                segmentIndex += 1;
            }
        }
    }*/
    
    public void insertPathElement(int step, Element element) {
        agentPath.insertAsNode(element, step);
    }
    
    public void insertPathSegment(int step, List pathSegment) {
        Element currentElement;
        for (int i = 0; i < pathSegment.size(); i++) {
            currentElement = (Element) pathSegment.getNodeData(i);
            agentPath.insertAsNode(currentElement, i+step);
        }
    }
    
    public Element getPathElement(int step) {
        return (Element) agentPath.getNodeData(step);
    }
    
    public int getAgentPathSize() {
        //return agentPathSize;
        return agentPath.size();
    }

    public void enqueueFinalPathElement(Element element) {
        // must be an element
        finalAgentPath.insertAtRear(element);
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
