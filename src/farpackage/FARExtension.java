package farpackage;

import generalpackage.*;
import adtpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class FARExtension {
    private Field field;
    private Element thisElement;

    private Element verticalAccessTo = null; // priority = 3
    private Element horizontalAccessTo = null; // priority = 4
    private Element extraHorizontalAccessTo = null; // priority = 2
    private Element extraVerticalAccessTo = null; // priority = 1

    //private boolean isWait = false;
    //private boolean isProxy = false;
    //private boolean isFailure = false;

    private Reservation [] reservations;
    private Reservation [] ghostReservations;

    public FARExtension(FARPathfinder pathfinder, Field field, Element thisElement) {
        this.field = field;

        this.thisElement = thisElement;

        //reservationDepth = farPathfinder.RESERVATION_DEPTH;

        reservations = new Reservation[pathfinder.FAILURE_CRITERION];
        for (int i = 0; i < pathfinder.FAILURE_CRITERION; i++) {
            reservations[i] = null;
        }

        ghostReservations = new Reservation[pathfinder.FAILURE_CRITERION];
        for (int i = 0; i < pathfinder.FAILURE_CRITERION; i++) {
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
        List returnList = new List();

        if (verticalAccessTo != null) returnList.insertAtFront(verticalAccessTo);
        if (horizontalAccessTo != null) returnList.insertAtFront(horizontalAccessTo);
        if (extraHorizontalAccessTo != null) returnList.insertAtFront(extraHorizontalAccessTo);
        if (extraVerticalAccessTo != null) returnList.insertAtFront(extraVerticalAccessTo);

        return returnList;
    }

    public List getUnreservedElements(int step) { // this now takes head-on collisions into account
        List returnList = new List();
        List accessibleList = getAccessibleElements();

        for (int i = 0; i < accessibleList.size(); i++) {
            Element currentElement = (Element) accessibleList.getNodeData(i);
            if (currentElement.getFARExtension().getReservation(step) == null) {
                if (getGhostReservation(step) == null) returnList.insertAtFront(currentElement);
                else if (getGhostReservation(step).getOriginalReservation().getElement() != currentElement) returnList.insertAtFront(currentElement);
            }
        }

        return returnList;
    }

    public boolean isProxyAvailable(int step) {
        if (getUnreservedElements(step).isEmpty()) return false;
        else return true;
    }

    public List getProxyPath(int step) {
        if (isProxyAvailable(step)) {
            FAR farAlgorithm = new FAR();

            Element proxyStart = (Element) getUnreservedElements(step).getNodeData(0); // PROBLEM WITH HEAD-ON COLLISIONS
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
