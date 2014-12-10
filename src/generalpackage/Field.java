package generalpackage;

import adtpackage.*;
import graphicspackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class Field {
    // TERMINOLOGY OF ELEMENTS:
        // BLOCKED ELEMENTS
            // OBSTACLE
            // BOUNDARY
        // TRAVERSABLE ELEMENTS - no obstacle or boundary
            // OCCUPIED - blocked by agent
            // ASSIGNED - (creation of new agents) blocked by another agent's start or goal
            // RESERVED - an agent has reserved this element because it intends to move there
            // FREE - no agent there

            // NEIGHBOR - traversable elements around a given element
            // ACCESSIBLE - elements that can be accessed from a given element

    public final int X_DIMENSION; // includes boundaries
    public final int Y_DIMENSION;
    
    private Element [][] elementArray;

    private int freeGroupNumber = -999;

    private List freeGroupList = new List();

    private HashSet traversableSet;

    private Agent [] agentArray;

    private GraphicsUtil gu = new GraphicsUtil();

    public Field() {
        X_DIMENSION = 52;
        Y_DIMENSION = 52;

        freeGroupNumber = 0;

        elementArray = new Element[X_DIMENSION][Y_DIMENSION];

        traversableSet = new HashSet((X_DIMENSION - 2) * (Y_DIMENSION - 2));

        generateBoundaries(); 
    }

    public Field(int x_dimension, int y_dimension) {
        X_DIMENSION = x_dimension;
        Y_DIMENSION = y_dimension;

        freeGroupNumber = 0;

        elementArray = new Element[X_DIMENSION][Y_DIMENSION];

        traversableSet = new HashSet((X_DIMENSION - 2) * (Y_DIMENSION - 2));

        generateBoundaries();
    }

    private void generateBoundaries() { // STEP ONE
        for (int i = 0; i < X_DIMENSION; i++) {
            if ((i == 0) || (i == (X_DIMENSION - 1))) {
                for (int j = 0; j < Y_DIMENSION; j++) {
                    elementArray[i][j] = new Element(i, j, true, false); // the top and bottom boundaries
                }
            } else {
                for (int j = 0; j < Y_DIMENSION; j++) {
                    if ((j == 0) || (j == (Y_DIMENSION - 1))) {
                        elementArray[i][j] = new Element(i, j, true, false); // the left and right boundaries
                    } else {
                        elementArray[i][j] = new Element(i, j, false, false);
                        traversableSet.add(getElement(i, j), getElement(i, j).getKey(Y_DIMENSION));
                    }
                }
            }
        }
    }

    public void generateObstacles() { // STEP TWO - improved version with guaranteed 20% of "raw" obstacles (before freeGroup adjustment) = CA*
        int obstacleX = -999;
        int obstacleY = -999;

        double area = (X_DIMENSION - 2) * (Y_DIMENSION - 2);
        int obstaclesLeft = (int) Math.round(area / 5.0);

        System.out.println("Area is " + area + " and 20% of that is " + (area / 5.0) + " (" + obstaclesLeft + ") obstacles");

        while (obstaclesLeft > 0) {
            do {
                obstacleX = (int) ((Math.random() * (X_DIMENSION - 2)) + 1);
                obstacleY = (int) ((Math.random() * (Y_DIMENSION - 2)) + 1);
            } while (!traversableSet.contains(getElement(obstacleX, obstacleY).getKey(Y_DIMENSION)));

            traversableSet.remove(getElement(obstacleX, obstacleY).getKey(Y_DIMENSION));

            getElement(obstacleX, obstacleY).setIsObstacle(true);

            obstaclesLeft -= 1;
        }
    }

    public void assignFreeGroups() { // STEP THREE
        for (int i = 1; i < (Y_DIMENSION - 1); i++) {
            for (int j = 1; j < (X_DIMENSION - 1); j++) {
                Element currentElement = getElement(j, i);
                if (!currentElement.isObstacle()) { // if this element is not an obstacle
                    if (getWestElement(currentElement).isBoundary() || getWestElement(currentElement).isObstacle()) { // if west element is blocked
                        if (getNorthElement(currentElement).isBoundary() || getNorthElement(currentElement).isObstacle()) { // if north element is also blocked
                            // assign this element to a new freeGroup
                            currentElement.setFreeGroup(freeGroupNumber);

                            FreeGroup newFreeGroup = new FreeGroup(freeGroupNumber, currentElement, this);
                            freeGroupList.insertAtRear(newFreeGroup);

                            freeGroupNumber++;
                        } else { // if west element is blocked but north element is not
                            // assign this element to north element's freeGroup
                            int northFreeGroup = getNorthElement(currentElement).getFreeGroup();
                            currentElement.setFreeGroup(northFreeGroup);

                            ((FreeGroup) freeGroupList.getNodeData(northFreeGroup)).addElement(currentElement);

                        }
                    } else { // if west element is not blocked
                        // assign freeGroup from west element
                        int westFreeGroup = getWestElement(currentElement).getFreeGroup();
                        currentElement.setFreeGroup(westFreeGroup);

                        ((FreeGroup) freeGroupList.getNodeData(westFreeGroup)).addElement(currentElement);

                        if (!(getNorthElement(currentElement).isBoundary()) && !(getNorthElement(currentElement).isObstacle()) && !(getNorthElement(currentElement).getFreeGroup() == currentElement.getFreeGroup())) {
                            // add elements of north element's freeGroup to this element's freeGroup
                            if (getNorthElement(currentElement).getFreeGroup() > currentElement.getFreeGroup()) { // traceBack for north's freeGroup
                                freeGroupTraceBack(getNorthElement(currentElement), currentElement.getFreeGroup());
                            } else { // traceBack for west's freeGroup
                                freeGroupTraceBack(currentElement, getNorthElement(currentElement).getFreeGroup());
                            }
                        } else {
                        }
                    }
                } else { // if this element is an obstacle
                    // skip this element
                }
            }
        }
    }

    private void freeGroupTraceBack(Element element, int newFreeGroup) { // Recursive helper function
        int oldFreeGroup = element.getFreeGroup();

        element.setFreeGroup(newFreeGroup);

        ((FreeGroup) freeGroupList.getNodeData(oldFreeGroup)).removeElement(element);

        ((FreeGroup) freeGroupList.getNodeData(newFreeGroup)).addElement(element);
        
        List elementNeighborList = getAxisNeighborElements(element);
        while (!elementNeighborList.isEmpty()) {
            Element currentNeighbor = (Element) elementNeighborList.removeLast();
            int neighborFreeGroup = currentNeighbor.getFreeGroup();
            if ((neighborFreeGroup != newFreeGroup) && (neighborFreeGroup != -999) && (!currentNeighbor.isBoundary()) && (!currentNeighbor.isObstacle())) {
                freeGroupTraceBack(currentNeighbor, newFreeGroup);
            }
        }
    }

    public void fillClosedGroups() { // STEP FOUR
        FreeGroup mainFreeGroup = (FreeGroup) freeGroupList.getNodeData(0);

        for (int i = 1; i < freeGroupList.size(); i++) {
            FreeGroup currentFreeGroup = (FreeGroup) freeGroupList.getNodeData(i);
            if (currentFreeGroup.getNumberElements() > mainFreeGroup.getNumberElements()) {
                mainFreeGroup = currentFreeGroup;
            }
            if (mainFreeGroup.getNumberElements() > (((X_DIMENSION - 2)*(Y_DIMENSION - 2))/2)) {
                break;
            }
        }

        TreeSet changeElementSet = new TreeSet();
        for (int i = 0; i < freeGroupList.size(); i++) {
            FreeGroup currentFreeGroup = (FreeGroup) freeGroupList.getNodeData(i);
            if (currentFreeGroup.getFreeGroupNumber() == mainFreeGroup.getFreeGroupNumber()) {
                continue; // skip this because it's the main free group
            } else {
                while (currentFreeGroup.getNumberElements() > 0) {
                    Element minElement = currentFreeGroup.removeMinElement();
                    if (minElement != null) {
                        changeElementSet.add(minElement, minElement.getKey(Y_DIMENSION));
                    }
                }
            }
        }

        System.out.println("Changing " + changeElementSet.size() + " elements to obstacles.");

        while (!(changeElementSet.isEmpty())) {
            Element minElement = (Element) changeElementSet.removeMin();
            minElement.setIsObstacle(true);
            traversableSet.remove(minElement.getKey(Y_DIMENSION));
            minElement.setFreeGroup(-1); // OPTIONAL
        }
    }

    public void assignAgents(int numAgents) {
        agentArray = new Agent[numAgents];

        HashSet freeSet = new HashSet((X_DIMENSION - 2) * (Y_DIMENSION - 2));
        for (int i = 1; i < (X_DIMENSION - 1); i++) {
            for (int j = 1; j < (Y_DIMENSION - 1); j++) {
                if (traversableSet.contains(getElement(i, j).getKey(Y_DIMENSION))) {
                    freeSet.add(getElement(i, j), getElement(i, j).getKey(Y_DIMENSION));
                }
            }
        }

        for (int i = 0; i < agentArray.length; i++) {
            try {
                agentArray[i] = new Agent(this, freeSet, i, gu.randomColor());

            } catch (Exception ex) {
                System.out.println("Error in newRandomAAgent. Breaking assignment loop…");
                break;
            }
        }
    }

    public void printFreeGroupNumberList() {
        for (int i = 0; i < freeGroupList.size(); i++) {
            System.out.println("freeGroupNumberList at " + i + ": freeGroupNumber " + ((FreeGroup) freeGroupList.getNodeData(i)).getFreeGroupNumber() + ", numberElements " + ((FreeGroup) freeGroupList.getNodeData(i)).getNumberElements());
        }
    }
    
    public Element [][] getElementArray() {
        return elementArray;
    }

    public HashSet getTraversableSet() {
        return traversableSet;
    }

    public Agent [] getAgentArray() {
        return agentArray;
    }

    public Element [] getAgentStartElements() {
        Element [] startElementArray = new Element[agentArray.length];

        for (int i = 0; i < agentArray.length; i++) {
            Agent currentAgent = agentArray[i];
            startElementArray[i] = currentAgent.getStart();
        }

        return startElementArray;
    }

    public Element [] getAgentGoalElements() {
        Element [] goalElementArray = new Element[agentArray.length];

        for (int i = 0; i < agentArray.length; i++) {
            Agent currentAgent = agentArray[i];
            goalElementArray[i] = currentAgent.getGoal();
        }

        return goalElementArray;
    }

    public Element getElement(int x_position, int y_position) {
        return elementArray[x_position][y_position];
    }

    public List getNeighborElements(Element element) {
        List neighborList = new List();
        neighborList.insertAtFront(getNorthElement(element));
        neighborList.insertAtFront(getNorthEastElement(element));
        neighborList.insertAtFront(getEastElement(element));
        neighborList.insertAtFront(getSouthEastElement(element));
        neighborList.insertAtFront(getSouthElement(element));
        neighborList.insertAtFront(getSouthWestElement(element));
        neighborList.insertAtFront(getWestElement(element));
        neighborList.insertAtFront(getNorthWestElement(element));
        return neighborList;
    }

    public List getAxisNeighborElements(Element element) {
        List neighborList = new List();
        neighborList.insertAtFront(getNorthElement(element));
        neighborList.insertAtFront(getEastElement(element));
        neighborList.insertAtFront(getSouthElement(element));
        neighborList.insertAtFront(getWestElement(element));
        return neighborList;
    }

    //Zbyněk Stara
    public double distanceBetween(Element element1, Element element2) {
        return (Math.sqrt(((element2.X_ID-element1.X_ID)*(element2.X_ID-element1.X_ID))
                + ((element2.Y_ID-element1.Y_ID)*(element2.Y_ID-element1.Y_ID))));
    }

    public double manhattanDistanceBetween(Element element1, Element element2) {
        return (Math.abs(element2.X_ID-element1.X_ID) + Math.abs(element2.Y_ID-element1.Y_ID));
    }

    public boolean isLegalMove(Element element, Element question) {
        if ((question == getNorthEastElement(element)) && getNorthElement(element).isObstacle() && getEastElement(element).isObstacle()) return false;
        else if ((question == getSouthEastElement(element)) && getEastElement(element).isObstacle() && getSouthElement(element).isObstacle()) return false;
        else if ((question == getSouthWestElement(element)) && getSouthElement(element).isObstacle() && getWestElement(element).isObstacle()) return false;
        else if ((question == getNorthWestElement(element)) && getWestElement(element).isObstacle() && getNorthElement(element).isObstacle()) return false;
        else return true;
    }

    public boolean isStraightLine(Element first, Element second, Element third) {
        if (first == null || second == null || third == null) return false;
        else if((getNorthElement(first) == second) && (getNorthElement(second) == third)) return true;
        else if((getEastElement(first) == second) && (getEastElement(second) == third)) return true;
        else if((getSouthElement(first) == second) && (getSouthElement(second) == third)) return true;
        else if((getWestElement(first) == second) && (getWestElement(second) == third)) return true;
        else return false;
    }

    public Element getNorthElement(Element element) {
        return elementArray[element.X_ID][element.Y_ID - 1];
    }

    public Element getNorthEastElement(Element element) {
        if ((element.X_ID + 1) < X_DIMENSION) return elementArray[element.X_ID + 1][element.Y_ID - 1];
        else return null;
    }

    public Element getEastElement(Element element) {
        if ((element.X_ID + 1) < X_DIMENSION) return elementArray[element.X_ID + 1][element.Y_ID];
        else return null;
    }

    public Element getSouthEastElement(Element element) {
        if (((element.X_ID + 1) < X_DIMENSION) && ((element.Y_ID + 1) < Y_DIMENSION)) return elementArray[element.X_ID + 1][element.Y_ID + 1];
        else return null;
    }

    public Element getSouthElement(Element element) {
        if ((element.Y_ID + 1) < Y_DIMENSION) return elementArray[element.X_ID][element.Y_ID + 1];
        else return null;
    }

    public Element getSouthWestElement(Element element) {
        if ((element.Y_ID + 1) < Y_DIMENSION) return elementArray[element.X_ID - 1][element.Y_ID + 1];
        else return null;
    }

    public Element getWestElement(Element element) {
        return elementArray[element.X_ID - 1][element.Y_ID];
    }

    public Element getNorthWestElement(Element element) {
        return elementArray[element.X_ID - 1][element.Y_ID - 1];
    }

    @Override public String toString() {
        return ("Dimensions: " + X_DIMENSION + ", " + Y_DIMENSION + "; FreeGroups assigned: " + freeGroupList.size());
    }
}
