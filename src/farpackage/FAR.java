package farpackage;

import apackage.*;
import adtpackage.*;
import generalpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class FAR {
    public FAR() {

    }

    public void resetAExtensions(Field field) {
        for (int i = 0; i < field.X_DIMENSION; i++) {
            for (int j = 0; j < field.Y_DIMENSION; j++) {
                field.getElement(i, j).setAExtension(new AExtension());
            }
        }
    }

    public List far(Element start, Element goal, Field field) {
        HashSet closedSet = new HashSet((field.X_DIMENSION - 2) * (field.Y_DIMENSION - 2)); // The set of nodes already evaluated.
        // ORDERED BY ELEMENT KEYS
        MinimalQueue openSet = new MinimalQueue(); // The set of tentative nodes to be evaluated, initially containing the start node
        // ORDERED BY F_SCORES

        double gScore = 0; // Cost from start along best known path.
        double hScore = field.manhattanDistanceBetween(start, goal); // manhattanDistanceBetween == right-angle heuristic cost estimate
        double fScore = gScore + hScore; // Estimated total cost from start to goal through y.

        start.getAExtension().setScores(gScore, hScore, fScore);

        openSet.enqueue(start, start.getAExtension().getFScore());

        while (!openSet.isEmpty()) {
            //Element current = openSetChooser(field, openSet, closedSet); // chooses the smallest from open set
            Element current = (Element) openSet.dequeue();
            List neighborList = current.getFARExtension().getAccessibleElements();

            if (current.isEqual(goal)) {
                return reconstructPath(start, goal);
            }

            while (!neighborList.isEmpty()) {
                Element currentNeighbor = (Element) neighborList.removeFirst();
                double tentativeGScore = -999;

                if ((currentNeighbor.isBoundary()) || (currentNeighbor.isObstacle())) {
                    continue;
                } else if (!field.isLegalMove(current, currentNeighbor)) {
                    continue;
                } else if (closedSet.contains(currentNeighbor.getKey(field.Y_DIMENSION))) {
                    continue;
                } else {
                    tentativeGScore = current.getAExtension().getGScore() + field.manhattanDistanceBetween(current, currentNeighbor);
                    
                    // to penalize non-straight paths - one of the requirements of FAR
                    // also penalizes initial paths (since current.cameFrom is null), but this happens across the board, so it's not a problem
                    if (!field.isStraightLine(current.getAExtension().getCameFrom(), current, currentNeighbor)) tentativeGScore += 0.1;
                }

                if (openSet.search(currentNeighbor) == -1) { // if current neighbor is not in open set

                    currentNeighbor.getAExtension().setCameFrom(current);

                    gScore = tentativeGScore;
                    hScore = field.manhattanDistanceBetween(currentNeighbor, goal);
                    fScore = gScore + hScore;

                    currentNeighbor.getAExtension().setScores(gScore, hScore, fScore);

                    openSet.enqueue(currentNeighbor, currentNeighbor.getAExtension().getFScore());
                } else if (tentativeGScore < currentNeighbor.getAExtension().getGScore()) { // else if current neighbor is in openSet but new GScore is better
                    openSet.delete(currentNeighbor);

                    currentNeighbor.getAExtension().setCameFrom(current);

                    gScore = tentativeGScore;
                    hScore = currentNeighbor.getAExtension().getHScore(); // hScore is already set for this one
                    fScore = gScore + hScore;

                    currentNeighbor.getAExtension().setScores(gScore, hScore, fScore);

                    openSet.enqueue(currentNeighbor, currentNeighbor.getAExtension().getFScore());
                } else { // else if current neighbor is in openSet and new GScore is not better
                    // then nothing happens
                }
            }

            closedSet.add(current, current.getKey(field.Y_DIMENSION));
        }
        return null;
    }

    /*private Element openSetChooser(Field field, MinimalQueue openSet, HashSet closedSet) { // THIS IS WRONG, IF YOU WANT TO ADD THIS, CHANGE THIS
        System.out.println("\t\t\t\t\t\tNew openSetChooser session"); // make a list of tried elements

        Element currentElement = null;
        Element previousElement = null;
        Element firstElement = null;

        HashSet triedElements = new HashSet(openSet.size());

        //int counter = 0;

        do {
            previousElement = currentElement;

            do {
                currentElement = (Element) openSet.dequeue();
                System.out.println("\t\t\t\t\t\t\tDequeued "+currentElement+" from openSet ("+currentElement.getAExtension().getFScore()+")");
            } while (closedSet.contains(currentElement.getKey(field.Y_DIMENSION)));

            System.out.println("\t\t\t\t\t\t\t\tProceeded with "+currentElement);


            if (counter == 0) {
                System.out.println("\t\t\t\t\t\t\t\tCurrent element is the first element");
                firstElement = currentElement;
            }

            if (currentElement.getAExtension().getCameFrom() != null) {
                if (currentElement.getAExtension().getCameFrom().getAExtension().getCameFrom() != null) {
                    if (currentElement.getAExtension().getCameFrom().isEqual(field.getNorthElement(currentElement))
                            && currentElement.getAExtension().getCameFrom().getAExtension().getCameFrom().isEqual(field.getNorthElement(field.getNorthElement(currentElement)))) {
                            // if element's cameFrom and its cameFrom are from the same direction

                        System.out.println("\t\t\t\t\t\t\t\tCurrent element chosen1");
                        return currentElement;
                    } else if (currentElement.getAExtension().getCameFrom().isEqual(field.getEastElement(currentElement))
                            && currentElement.getAExtension().getCameFrom().getAExtension().getCameFrom().isEqual(field.getEastElement(field.getEastElement(currentElement)))) {

                        System.out.println("\t\t\t\t\t\t\t\tCurrent element chosen2");
                        return currentElement;
                    } else if (currentElement.getAExtension().getCameFrom().isEqual(field.getSouthElement(currentElement))
                            && currentElement.getAExtension().getCameFrom().getAExtension().getCameFrom().isEqual(field.getSouthElement(field.getSouthElement(currentElement)))) {

                        System.out.println("\t\t\t\t\t\t\t\tCurrent element chosen3");
                        return currentElement;
                    } else if (currentElement.getAExtension().getCameFrom().isEqual(field.getWestElement(currentElement))
                            && currentElement.getAExtension().getCameFrom().getAExtension().getCameFrom().isEqual(field.getWestElement(field.getWestElement(currentElement)))) {

                        System.out.println("\t\t\t\t\t\t\t\tCurrent element chosen4");
                        return currentElement;
                    } else { // if element's cameFrom and its cameFrom are not from the same direction
                        // put at the end of candidates with the same fScore - only if none of the others offers a straight path, this is selected
                        System.out.println("\t\t\t\t\t\t\t\tCurrent element re-enqueued");
                        openSet.enqueue(currentElement, currentElement.getAExtension().getFScore());
                    }
                } else {
                    System.out.println("\t\t\t\t\t\t\t\tFirst element chosen1");
                    return firstElement;
                }
            } else {
                System.out.println("\t\t\t\t\t\t\t\tFirst element chosen2");
                return firstElement;
            }

            counter++;
        } while (((!currentElement.isEqual(firstElement)) || (counter == 1)) && (!currentElement.isEqual(previousElement)));
            // the first condition is there to break the cycle of looking for a straight path when there is none
            // the second condition is there to ensure the cycle is not ended with the first try - when the current and first are the same by definition
            // the third condition is there to ensure the cycle is ended when we find the best way and it's not the first element

        //if (!openSet.isEmpty()) openSet.dequeue(); // to remove the re-enqueued element

        System.out.println("\t\t\t\t\t\t\t\tCurrent element chosen5");
        return currentElement;
    }*/

    private List reconstructPath(Element startNode, Element currentNode) {
        List path;
        if (currentNode != startNode) { //came_from[current_node] is set {
            List tempPath = reconstructPath(startNode, currentNode.getAExtension().getCameFrom());
            path = tempPath;
            path.insertAtRear(currentNode);
        }
        else {
            path = new List();
            path.insertAtRear(currentNode);
        }
        return path;
    }
}
