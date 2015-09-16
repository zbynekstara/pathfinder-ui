package farpackage;

import generalpackage.*;
import adtpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class FARExtension {
    private final Field field;
    private final Element thisElement;

    private Element verticalAccessTo = null; // priority = 3
    private Element horizontalAccessTo = null; // priority = 4
    private Element extraHorizontalAccessTo = null; // priority = 2
    private Element extraVerticalAccessTo = null; // priority = 1

    //private boolean isWait = false;
    //private boolean isProxy = false;
    //private boolean isFailure = false;

    private Reservation [] reservations;
    private Reservation [] ghostReservations;
    // FAULURE_CRITERION+1 deep = because we end at the number and start with 0
        
    public FARExtension(FARPathfinder pathfinder, Field field, Element thisElement) {
        this.field = field;

        this.thisElement = thisElement;

        //reservationDepth = farPathfinder.RESERVATION_DEPTH;

        reservations = new Reservation[FARPathfinder.FAILURE_CRITERION+1];
        for (int i = 0; i <= FARPathfinder.FAILURE_CRITERION; i++) {
            reservations[i] = null;
        }

        ghostReservations = new Reservation[FARPathfinder.FAILURE_CRITERION+1];
        for (int i = 0; i <= FARPathfinder.FAILURE_CRITERION; i++) {
            ghostReservations[i] = null;
        }
    }

    public void setReservation(int step, Reservation reservation) {
        reservations[step] = reservation;
    }
    public Reservation getReservation(int step) {
        return reservations[step];
    }

    public void setGhostReservation(int step, Reservation reservation) {
        ghostReservations[step] = reservation;
    }
    public Reservation getGhostReservation(int step) {
        return ghostReservations[step];
    }

    public void setVerticalAccessTo(Element element) {
        verticalAccessTo = element;
    }
    public void setHorizontalAccessTo(Element element) {
        horizontalAccessTo = element;
    }
    public void setExtraHorizontalAccessTo(Element element) {
        extraHorizontalAccessTo = element;
    }
    public void setExtraVerticalAccessTo(Element element) {
        extraVerticalAccessTo = element;
    }

    /*public void setIsWait(boolean isWait) {
        this.isWait = isWait;
    }
    public boolean isWait() {
        return isWait;
    }
    public void setIsProxy(boolean isProxy) {
        this.isProxy = isProxy;
    }
    public boolean isProxy() {
        return isProxy;
    }
    public void setIsFailure(boolean isFailure) {
        this.isFailure = isFailure;
    }
    public boolean isFailure() {
        return isFailure;
    }*/

    public List getAccessibleElements() {
        List accessibleList = new List();

        if (verticalAccessTo != null) accessibleList.insertAtFront(verticalAccessTo);
        if (horizontalAccessTo != null) accessibleList.insertAtFront(horizontalAccessTo);
        if (extraHorizontalAccessTo != null) accessibleList.insertAtFront(extraHorizontalAccessTo);
        if (extraVerticalAccessTo != null) accessibleList.insertAtFront(extraVerticalAccessTo);

        return accessibleList;
    }

    public List getUnreservedElements(int step) {
        List unreservedList = new List();
        List accessibleList = getAccessibleElements();

        for (int i = 0; i < accessibleList.size(); i++) {
            Element currentAccessible = (Element) accessibleList.getNodeData(i);
            // for each accessible element
            
            if (currentAccessible.getFARExtension().getReservation(step) == null) {
                // if there is no reservation at the element yet
                
                if (getGhostReservation(step) == null) {
                    // if there is no ghost reservation either at the element
                    unreservedList.insertAtFront(currentAccessible);
                    // the accessible element can be used as proxy start!
                }
                else if (getGhostReservation(step).getOriginalReservation().getElement() != thisElement) {
                    // if there is a ghost reservation at the element
                    unreservedList.insertAtFront(currentAccessible);
                    // the accessible element can be used as proxy start
                    // as long as the original element of the ghost is not this element!
                    // prevents head-on collisions
                }
            }
        }

        return unreservedList;
    }

    public boolean isProxyAvailable(int step) {
        // looks at the step layer around (but not including) an element
        // if there is a place to go that is not reserved yet, returns true
        // should be used when moving from [element,step-1] to [proxy?,step]
        
        if (getUnreservedElements(step).isEmpty()) return false;
        else return true;
    }

    public List getProxyPath(int step) {
        // returns a new path, starting from first available proxy spot, and ending at this element
        if (isProxyAvailable(step)) {
            FAR farAlgorithm = new FAR();

            Element proxyStart = (Element) getUnreservedElements(step).getNodeData(0);
            // PROBLEM WITH HEAD-ON COLLISIONS - should be solved by the ghost reservation condition in getUnreservedElements
            Element proxyEnd = thisElement;

            System.out.println("\t\t\t\t\tFinding proxy path with start at "+proxyStart+" and end at "+proxyEnd);
            List proxyPath = farAlgorithm.far(proxyStart, proxyEnd, field);

            return proxyPath;
        } else {
            return null;
        }
    }

    public String printReservations() {
        String returnString = "";
        for (int i = 0; i < reservations.length; i++) {
            if (reservations[i] != null) returnString += reservations[i].toString() + "; ";
            else returnString += "null; ";
        }
        return returnString;
    }
}
