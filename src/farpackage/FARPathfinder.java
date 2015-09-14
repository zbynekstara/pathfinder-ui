package farpackage;

import generalpackage.*;
import adtpackage.*;
import guipackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class FARPathfinder implements Pathfinder {
    /**
     * The AllocationWorker class is an extension of the SwingWorker<V, T> class
     * from the extension package javax.swing. Its purpose is to allow for a
     * computation-extensive part of the program to be executed in the
     * background, which is useful for the allocation algorithm.
     *
     * @author Zbyněk Stara
     */
    private class AllocationWorker extends javax.swing.SwingWorker<Boolean, Boolean> {
        AllocationWorker () {
            
        }

        /**
         * This method specifies what will be done in the background after the
         * allocation worker is executed with the aw.execute() method
         *
         * @return true in any case
         * @throws Exception if the allocation worker is unable to complete the
         * task
         *
         * @author Zbyněk Stara
         */
        @Override
        public Boolean doInBackground() throws Exception {
            startTime = System.currentTimeMillis();
            findAgentPathsHandler();
            return true;
        }

        /**
         * This method specifies what the program will do after the task in
         * the doInBackground() method finishes.
         *
         * @author Zbyněk Stara
         */
        @Override
        protected void done() {
            // the allocation info dialog will be reset and hidden
            gui.getInfoStepTF().setText("");
            gui.getInfoSuccessTF().setText("");
            gui.getInfoTimeTF().setText("");
            gui.getInfoDialog().setVisible(false);
        }
    }

    public void findAgentPaths() {
        // make a new allocation worker that will do the allocation in the background
        aw = new AllocationWorker();

        // this listener is added to update the numbers reported by the progress dialog
        aw.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if ("update".equals(evt.getPropertyName())) {
                    thisPathfinder.updateInfo(); // update simStep
                }
            }
        });

        // this is to execute the allocation in the background
        // calls the doInBackground() function of the AllocationWorker
        aw.execute();
        
        // set up the allocation info dialog
        gui.getInfoStepTF().setText("");
        gui.getInfoSuccessTF().setText("");
        gui.getInfoTimeTF().setText("");
        gui.getInfoDialog().setVisible(true);
    }

    public final int RESERVATION_DEPTH = 3; // FAR's k parameter - reservation depth

    private final GUI gui;
    private final Field field;
    private final FARPathfinder thisPathfinder = this;

    private AllocationWorker aw; // allocation worker to handle the allocation in the background
    private long startTime;
    private int numUpdates;

    private FARAgent [] farAgents;

    private TreeSet [] farAgentGroups;

    private int simStep = 0; // simulation step

    private MinimalQueue agentReservationOrderAtStep = new MinimalQueue();

    private int numSuccesses = 0;
    private int numFailures = 0;

    public FARPathfinder(GUI gui, Field field) {
        // INITIALIZATION:
        this.gui = gui;
        this.field = field;

        // set farExtensions of all elements
        for (int x = 1; x < (field.X_DIMENSION - 1); x++) {
           for (int y = 1; y < (field.Y_DIMENSION - 1); y++) {
               field.getElement(x, y).setFARExtension(new FARExtension(this, field, field.getElement(x, y)));
           }
        }

        // field annotation
        fieldFlowAnnotation();
    }

    public void findAgentPathsHandler() {
        // make array of farAgents
        farAgents = new FARAgent[field.getAgentArray().length];
        for (int i = 0; i < field.getAgentArray().length; i++) {
            Agent agent = field.getAgentArray()[i];

            FARAgent newFARAgent = new FARAgent(field, this, agent.getStart(), agent.getGoal(), i);

            farAgents[i] = newFARAgent;
        }

        // make the farAgentGroups array
        farAgentGroups = new TreeSet[RESERVATION_DEPTH];
        for (int i = 0; i < RESERVATION_DEPTH; i++) {
            farAgentGroups[i] = new TreeSet();
        }
        TreeSet ungrouppedSet = new TreeSet();
        for (FARAgent currentAgent : farAgents) {
            ungrouppedSet.add(currentAgent, currentAgent.FARAGENT_ID);
        }

        // intitialize the farAgentGroupsArray with randomly selected sets of farAgents
        int currentGroupNum = 0;
        while (!ungrouppedSet.isEmpty()) {
            FARAgent addedAgent = null;
            do {
                int randomAgentNum = (int) (Math.random() * (farAgents.length));
                addedAgent = (FARAgent) ungrouppedSet.remove(farAgents[randomAgentNum].FARAGENT_ID);
            } while (addedAgent == null);

            farAgentGroups[currentGroupNum].add(addedAgent, addedAgent.FARAGENT_ID);

            addedAgent.setAgentGroup(currentGroupNum);

            currentGroupNum += 1;
            if (currentGroupNum == RESERVATION_DEPTH) currentGroupNum = 0;
        }

        // coordination stage
        System.out.println("Coordination stage");
        for (simStep = 0; simStep < FAILURE_CRITERION; simStep++) {
            // simStep 0 is the initial reservation step for all agents
            // simStep 1 is the first step for which real reservations are needed

            // initialization
            aw.firePropertyChange("update", simStep-1, simStep);
            System.out.println("Step: "+simStep);

            int agentGroupForStep = (simStep-1) % RESERVATION_DEPTH; // each step only sees coordination from a portion of agents

            // randomization of agents
            agentReservationOrderAtStep = new MinimalQueue(); // it is necessary to mix up the agent reservation order
            MaximalQueue unassignedAgents = new MaximalQueue(); // queue of agents awaiting assignment to reservation order

            if (simStep == 0) {
                // if this is step 0, enqueue everyone for initial reservation order assignment
                for (int i = simStep; i < farAgentGroups.length; i++) {
                    for (int j = 0; j < farAgentGroups[i].size(); j++) {
                        unassignedAgents.enqueue((FARAgent) farAgentGroups[i].getNodeData(j), ((FARAgent) farAgentGroups[i].getNodeData(j)).FARAGENT_ID);
                    }
                }
            } else if (simStep > 0) {
                // if this is not step 0, enqueue only agents from the appropriate agent group
                for (int i = 0; i < farAgentGroups[agentGroupForStep].size(); i++) {
                    unassignedAgents.enqueue((FARAgent) farAgentGroups[agentGroupForStep].getNodeData(i), ((FARAgent) farAgentGroups[agentGroupForStep].getNodeData(i)).FARAGENT_ID);
                }
            }

            while (!unassignedAgents.isEmpty()) {
                FARAgent chosenAgent = null;
                do {
                    int randomAgentIndex = (int) (Math.random() * (unassignedAgents.size())); // get a random index
                    double randomAgentKey = unassignedAgents.getNodeKey(randomAgentIndex); // identify an agent
                    chosenAgent = (FARAgent) unassignedAgents.remove(randomAgentKey); // get the agent
                } while (chosenAgent == null); // the above procedure may fail

                agentReservationOrderAtStep.enqueue(chosenAgent, simStep); // enqueue the chosen agent into reservation order
            }

            // reserving
            // during this part of the algrithm, some agents look ahead (up to RESERVATION_DEPTH steps) and reserve their required paths
            // this is different from the next step of the algorithm, where all agents actually perform the reservation at the current simStep
            while (!agentReservationOrderAtStep.isEmpty()) {
                // while there are agents to reserve for
                FARAgent currentAgent = (FARAgent) agentReservationOrderAtStep.dequeue();

                Reservation previousReservation = currentAgent.getLastReservation();

                // initializing
                int resStep; // reservation step
                // this is different from the simStep
                // resStep can be up to RESERVATION_DEPTH larger than simStep

                if (previousReservation == null) resStep = 0;
                else resStep = previousReservation.getStep() + 1;

                int reservationIndex; // how many steps ahead should we plan

                if (currentAgent.getLastReservation() == null) reservationIndex = RESERVATION_DEPTH - currentAgent.getAgentGroup() - 1;
                // if this is the first time we reserve, reserve 1-RESERVATION_DEPTH steps
                else reservationIndex = (previousReservation.getReservationIndex() + 1) % RESERVATION_DEPTH;
                // if this is during normal reservations, reserve for the correct number of steps according to previous reservation

                System.out.println("\t\tCurrent agent: "+currentAgent.FARAGENT_ID+" (G: "+currentAgent.getAgentGroup()+", RI: "+reservationIndex+")");

                // making the actual reservations
                if ((currentAgent.getLastReservation() == null) || (simStep <= currentAgent.getAgentGroup() && currentAgent.getLastReservation().isInitialReservation())) {
                    // if this is the very first reservation
                    // or the simStep is lower than the agentGroup and previous reservation was initial

                    reserve(currentAgent, ReservationType.INITIAL, previousReservation);

                    if ((RESERVATION_DEPTH - reservationIndex - 1) > 0) { // THIS WAS !=
                        // add the agent back to the reservation order to get another initial reservation as needed
                        agentReservationOrderAtStep.enqueue(currentAgent, resStep);
                    } else {
                        // normally, this would do nothing
                        // but reassignments make it possible for a proxy reservation to be made before some of the later initial reservations are made
                        // that would increase the reservation index
                        // thus, it may be required to delete this extra initial assignment
                        while (agentReservationOrderAtStep.search(currentAgent) != -1) { // -1 means it was not found
                            System.out.println("\t\tDeleting extra assignment of agent "+currentAgent.FARAGENT_ID);
                            agentReservationOrderAtStep.delete(currentAgent);
                        }
                    }

                } else if (resStep < FAILURE_CRITERION) {
                    // normal reservation
                    // we have to end before resStep reaches failure criterion
                    // we do not want to try to reserve at impossible times

                    if (resStep < previousReservation.getReservationPath().size()) {
                        // if resStep is smaller than the size of the reservation path
                        // we still have stuff to do

                        reserve(currentAgent, ReservationType.NORMAL, previousReservation);

                        if ((RESERVATION_DEPTH - reservationIndex - 1) > 0) { // SHOULD THIS BE > ?
                            // if there are still reservations to be made, put the agent back in reservation order
                            agentReservationOrderAtStep.enqueue(currentAgent, resStep);
                        } else {
                            // normally, this would do nothing
                            // but it is possible for proxy assignments to be made before this happens
                            // then the reservation index would be bigger
                            // and thus the extra assignemnts would need to be scrapped
                            while (agentReservationOrderAtStep.search(currentAgent) != -1) { // -1 means not found
                                System.out.println("\t\tDeleting extra assignment of agent "+currentAgent.FARAGENT_ID);
                                agentReservationOrderAtStep.delete(currentAgent);
                            }
                        }

                    } else {
                        // if this step is after the end of the reservation path
                        // end by waiting at the finish point

                        reserve(currentAgent, ReservationType.WAIT, previousReservation);

                        if ((RESERVATION_DEPTH - reservationIndex - 1) > 0) {
                            // if there are still some resSteps for which to reserve
                            // add the agent back to reservation order
                            agentReservationOrderAtStep.enqueue(currentAgent, resStep);
                        } else {
                            while (agentReservationOrderAtStep.search(currentAgent) != -1) { // -1 means not found
                                System.out.println("\t\tDeleting extra assignment of agent "+currentAgent.FARAGENT_ID);
                                agentReservationOrderAtStep.delete(currentAgent);
                            }
                        }
                    }
                }
            }

            // Moving agents
            System.out.println("\tMoving agents");
            for (FARAgent currentAgent : farAgents) {
                // going through all agents

                System.out.println("\t\tAdjusting path of agent "+currentAgent.FARAGENT_ID+"");

                System.out.println("\t\t\tLast honored reservation: "+currentAgent.getLastHonoredReservation());
                
                // perform the reservation that is lined up for this agent
                Reservation reservationToHonor = null;
                if (currentAgent.getLastHonoredReservation() == null) reservationToHonor = currentAgent.getFirstReservation(); // first reservation
                else reservationToHonor = currentAgent.getLastHonoredReservation().getDependentReservation();
                System.out.println("\t\t\tReservation to honor: "+reservationToHonor);

                currentAgent.setAgentPath(reservationToHonor.getReservationPath());

                currentAgent.setLastHonoredReservation(reservationToHonor);

                // if this is the last element of the agent's path, finish
                Element currentElement = currentAgent.getPathElement(simStep);
                if (currentElement == currentAgent.GOAL && !currentAgent.isComplete()) {
                    System.out.println("\t\tSuccess for agent: "+currentAgent.FARAGENT_ID);
                    numSuccesses += 1;
                    currentAgent.setIsComplete(true);
                }
            }
        }

        // wrap up afer all simSteps are exhausted
        System.out.println("After coordination stage");
        for (FARAgent currentAgent : farAgents) {
            if (!currentAgent.isComplete()) {
                //currentAgent.getPathElement(FAILURE_CRITERION - 1).getFARExtension().setIsFailure(true);
                System.out.println("\tFailure for agent: "+currentAgent.FARAGENT_ID);
                numFailures += 1;
            }
        }

        System.out.println("Number of failures: "+numFailures);
    }

    // draw method for gui
    public Element [] getAgentsAtStep(int drawStep) { // returns an array of elements
        Element [] elementArray = new Element[farAgents.length];

        for (int i = 0; i < farAgents.length; i++) {
            FARAgent currentAgent = farAgents[i];
            if (drawStep < currentAgent.getFinalAgentPathSize()) { // this should not be a thing
                elementArray[i] = (Element) currentAgent.getFinalPathElement(drawStep);
            } else {
                elementArray[i] = currentAgent.GOAL;
            }
        }

        return elementArray;
    }

    // draw method for gui
    public List [] getAgentPathsUntilStep(int drawStep) { // returns an array of lists
        List[] pathArray = new List[farAgents.length];

        for (int i = 0; i < farAgents.length; i++) {
            FARAgent currentAgent = farAgents[i];
            pathArray[i] = currentAgent.getFinalAgentPathUntilStep(drawStep);
        }

        return pathArray;
    }

    public int getNumFailures() {
        return numFailures;
    }

    // this roates the priority directions according to simStep
    // lower number is better
    public int getPriority(int step, Element firstElement, Element secondElement) {
        if (firstElement.isEqual(secondElement)) return 4; // waiting

        int incrementor = step % 4;

        if (field.getNorthElement(firstElement).isEqual(secondElement)) {
            int priority = (0 + incrementor) % 4;
            return priority;
        } else if (field.getEastElement(firstElement).isEqual(secondElement)) {
            int priority = (1 + incrementor) % 4;
            return priority;
        } else if (field.getSouthElement(firstElement).isEqual(secondElement)) {
            int priority = (2 + incrementor) % 4;
            return priority;
        } else if (field.getWestElement(firstElement).isEqual(secondElement)) {
            int priority = (3 + incrementor) % 4;
            return priority;
        } else {
            throw new RuntimeException();
        }
    }

    private void reserve(FARAgent agent, ReservationType reservationType, Reservation previousReservation) {
        int resStep;
        if (previousReservation == null) resStep = 0;
        else resStep = previousReservation.getStep() + 1;

        if (reservationType == ReservationType.NORMAL || reservationType == ReservationType.PROXY) { // THIS SHOULD TAKE INTO ACCOUNT THE HEAD-ON COLLISIONS!
            Element reservationElement = (Element) previousReservation.getElement();

            if (reservationType == ReservationType.NORMAL) System.out.println("\t\t\tReserving a normal reservation at element: "+reservationElement+" (step "+resStep+")");
            else System.out.println("\t\t\tReserving a proxy reservation at element: "+reservationElement+" (step "+resStep+")");
            
            Reservation existingReservation = reservationElement.getFARExtension().getReservation(resStep);
            Reservation existingGhostReservation = reservationElement.getFARExtension().getGhostReservation(resStep);

            if (existingGhostReservation.getOriginalReservation().getElement() == previousReservation.getElement()) { // head-on collision!
                int ghostReservationPriority = getPriority(resStep, existingGhostReservation.getElement(), previousReservation.getElement());
                System.out.println("\t\t\t\tExisting ghost reservation: "+existingGhostReservation);
                int newReservationPriority = getPriority(resStep, previousReservation.getElement(), reservationElement);
                System.out.println("\t\t\t\tPrevious reservation: "+previousReservation);

                if (ghostReservationPriority == newReservationPriority) {
                    throw new RuntimeException();

                } else if (ghostReservationPriority < newReservationPriority) { // existing reservation overrules new reservation
                    // signals that new reservation needs to be replanned

                    if (previousReservation.getElement().getFARExtension().isProxyAvailable(resStep)) { // if new reservation has a place to go
                        // keep existing reservation

                        // rewrite new reservation since there is a proxy path available for it
                        FARAgent existingAgent = existingReservation.getAgent();
                        System.out.println("\t\t\t\tConflict; normal reservation changed to proxy because of head-on collision with agent: "+existingAgent.FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");

                        reserveProxyForAgent(previousReservation.getElement(), agent, previousReservation);

                    } else { // there is no proxy the new-reserving agent could go
                        if (reservationElement.getFARExtension().isProxyAvailable(resStep)) { // the colliding agent can go away
                            // WATCH OUT WITH RESERVATION INDICES - MIGHT NOT BE ABLE TO GO AT ALL BECAUSE OF NATURE OF RESERVATION
                            // evict existing reservation
                            FARAgent existingAgent = existingReservation.getAgent();
                            System.out.println("\t\t\t\tConflict (head-on) with no proxy available; reservation evicts agent: "+existingAgent.FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");

                            evictExistingReservations(existingReservation); // PROBLEM - HOW TO ENSURE THE AGENT DOESN'T RE-RESERVE THE ELEMENT?

                            // and set current reservation to a be the current reservation
                            reserveElementForAgent(reservationElement, agent, reservationType, previousReservation);

                            System.out.println("\t\t\t\t\tNew reservation: "+reservationElement.getFARExtension().getReservation(resStep));
                        } // ELSE EVICT BOTH OF THEM

                        // evict existing reservation
                        FARAgent existingAgent = existingReservation.getAgent();
                        System.out.println("\t\t\t\tConflict (head-on) with no proxy available; reservation evicts agent: "+existingAgent.FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");

                        evictExistingReservations(existingReservation);

                        // and set current reservation to a be the current reservation
                        reserveElementForAgent(reservationElement, agent, reservationType, previousReservation);

                        System.out.println("\t\t\t\t\tNew reservation: "+reservationElement.getFARExtension().getReservation(resStep));
                    }

                } else { // new reservation overwrites existing reservation
                    if (reservationElement.getFARExtension().isProxyAvailable(resStep)) {
                        // rewrite existing reservation since there is a proxy path available for it
                        FARAgent existingAgent = existingReservation.getAgent();
                        if (existingReservation.isWaitReservation()) System.out.println("\t\t\t\tConflict; agent overrides existing wait reservation of: "+existingAgent.FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");
                        else System.out.println("\t\t\t\tConflict; agent overrides existing initial reservation of: "+existingAgent.FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");

                        evictExistingReservations(existingReservation);

                        // set reservation to be new reservation
                        reserveElementForAgent(reservationElement, agent, reservationType, previousReservation);

                        System.out.println("\t\t\t\t\tNew reservation: "+reservationElement.getFARExtension().getReservation(resStep));

                    } else { // there is no proxy the existing element could take
                        // change the  keep current reservation

                        // and change current reservation to a wait reservation
                        System.out.println("\t\t\t\tConflict; agent has to replan with a wait reservation because of agent: "+existingReservation.getAgent().FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");
                        reserve(agent, ReservationType.WAIT, previousReservation); // IMPORTANT
                    }
                }
                
            } else if (existingReservation == null) { // if there is no reservation at the resStep
                // reserve this element for the current agent
                reserveElementForAgent(reservationElement, agent, reservationType, previousReservation);

            } else {
                int currentReservationPriority = getPriority(resStep, existingReservation.getCameFrom(), reservationElement);
                System.out.println("\t\t\t\tExisting reservation: "+existingReservation);
                int newReservationPriority = getPriority(resStep, previousReservation.getElement(), reservationElement); // THIS IS THE PROBLEM
                System.out.println("\t\t\t\tPrevious reservation: "+previousReservation);

                if (currentReservationPriority == newReservationPriority) {
                    throw new RuntimeException();

                } else if (currentReservationPriority < newReservationPriority) { // existing reservation overrules new reservation
                    // keep the existing reservation

                    // signals that new reservation needs to be replanned as wait reservation
                    System.out.println("\t\t\t\tConflict; agent has to replan with a wait reservation because of agent: "+existingReservation.getAgent().FARAGENT_ID+" (step "+resStep+")");
                    reserve(agent, ReservationType.WAIT, previousReservation); // IMPORTANT

                } else { // new reservation overwrites existing reservation
                    if (existingReservation.isNormalReservation() || existingReservation.isProxyReservation()) {
                        // remove reservations that are not valid anymore from existingReservation's agent
                        FARAgent existingAgent = existingReservation.getAgent();
                        System.out.println("\t\t\t\tConflict; agent overrides existing non-wait reservation of: "+existingAgent.FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");

                        evictExistingReservations(existingReservation);

                        //rewrite reservation
                        reserveElementForAgent(reservationElement, agent, reservationType, previousReservation);

                        System.out.println("\t\t\t\t\tNew reservation: "+reservationElement.getFARExtension().getReservation(resStep));

                    } else { // if overwritten reservation is a wait reservation or an initial reservation
                        if (reservationElement.getFARExtension().isProxyAvailable(resStep)) {
                            // rewrite existing reservation since there is a proxy path available for it
                            FARAgent existingAgent = existingReservation.getAgent();
                            if (existingReservation.isWaitReservation()) System.out.println("\t\t\t\tConflict; agent overrides existing wait reservation of: "+existingAgent.FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");
                            else System.out.println("\t\t\t\tConflict; agent overrides existing initial reservation of: "+existingAgent.FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");

                            evictExistingReservations(existingReservation);

                            // set reservation to be new reservation
                            reserveElementForAgent(reservationElement, agent, reservationType, previousReservation);

                            System.out.println("\t\t\t\t\tNew reservation: "+reservationElement.getFARExtension().getReservation(resStep));

                        } else { // there is no proxy the waiting element could take
                            // else keep current reservation
                            
                            // and change current reservation to a wait reservation
                            System.out.println("\t\t\t\tConflict; agent has to replan with a wait reservation because of agent: "+existingReservation.getAgent().FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");
                            reserve(agent, ReservationType.WAIT, previousReservation); // IMPORTANT
                        }
                    }
                }
            }

        } else if (reservationType == ReservationType.WAIT) {
            Element reservationElement = (Element) previousReservation.getElement();

            System.out.println("\t\t\tWait at element: "+reservationElement+" (step "+resStep+")");

            Reservation existingReservation = reservationElement.getFARExtension().getReservation(resStep);

            if (existingReservation == null) { // if there is no reservation at the resStep
                // reserve this element for the current agent
                reserveElementForAgent(reservationElement, agent, reservationType, previousReservation);

            } else { // there is a reservation at the resStep                
                int currentReservationPriority = getPriority(resStep, existingReservation.getCameFrom(), reservationElement);
                System.out.println("\t\t\t\tExisting reservation: "+existingReservation);
                int newReservationPriority = getPriority(resStep, previousReservation.getElement(), reservationElement); // THIS IS THE PROBLEM
                System.out.println("\t\t\t\tPrevious reservation: "+previousReservation);

                if (currentReservationPriority == newReservationPriority) {
                    throw new RuntimeException();

                } else if (currentReservationPriority < newReservationPriority) { // existing reservation overrules new reservation - ALWAYS
                    // signals that new reservation needs to be replanned

                    // there are some decisions to be done about optimization here - does wait always give way?
                    if (reservationElement.getFARExtension().isProxyAvailable(resStep)) {
                        // keep existing reservation

                        // rewrite new reservation since there is a proxy path available for it
                        FARAgent existingAgent = existingReservation.getAgent();
                        System.out.println("\t\t\t\tConflict; wait reservation changed to proxy because of agent: "+existingAgent.FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");

                        reserveProxyForAgent(reservationElement, agent, previousReservation);

                    } else { // there is no proxy the waiting element could take
                        // evict existing reservation
                        FARAgent existingAgent = existingReservation.getAgent();
                        System.out.println("\t\t\t\tConflict with no proxy available; wait reservation evicts agent: "+existingAgent.FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");

                        evictExistingReservations(existingReservation);

                        // and set current reservation to a be the wait reservation
                        reserveElementForAgent(reservationElement, agent, reservationType, previousReservation);
                        
                        System.out.println("\t\t\t\t\tNew reservation: "+reservationElement.getFARExtension().getReservation(resStep));
                    }

                } else { // new reservation overwrites existing reservation - NEVER
                    
                }
            }
        } else if (reservationType == ReservationType.INITIAL) {
            Element reservationElement = agent.START;
            if (previousReservation != null) reservationElement = (Element) previousReservation.getElement();

            System.out.println("\t\t\tInitial reservation at element: "+reservationElement+" (step "+resStep+")");

            Reservation existingReservation = reservationElement.getFARExtension().getReservation(resStep);

            if (existingReservation == null) { // if there is no reservation at the resStep
                // reserve this element for the current agent
                reserveElementForAgent(reservationElement, agent, reservationType, previousReservation);

            } else { // there is a reservation at the resStep
                int currentReservationPriority = getPriority(resStep, existingReservation.getCameFrom(), reservationElement);
                System.out.println("\t\t\t\tExisting reservation: "+existingReservation);
                int newReservationPriority = getPriority(resStep, previousReservation.getElement(), reservationElement);
                System.out.println("\t\t\t\tPrevious reservation: "+previousReservation);

                if (currentReservationPriority == newReservationPriority) {
                    throw new RuntimeException();

                } else if (currentReservationPriority < newReservationPriority) { // existing reservation overrules new reservation - ALWAYS
                    // signals that new reservation needs to be replanned

                    // there are some decisions to be done about optimization here - does wait always give way?
                    if (reservationElement.getFARExtension().isProxyAvailable(resStep)) {
                        // keep existing reservation

                        // rewrite new reservation since there is a proxy path available for it
                        FARAgent existingAgent = existingReservation.getAgent();
                        System.out.println("\t\t\t\tConflict; initial reservation changed to proxy because of agent: "+existingAgent.FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");

                        reserveProxyForAgent(reservationElement, agent, previousReservation);

                    } else { // there is no proxy the waiting element could take
                        // evict existing reservation
                        FARAgent existingAgent = existingReservation.getAgent();
                        System.out.println("\t\t\t\tConflict with no proxy available; initial reservation evicts agent: "+existingAgent.FARAGENT_ID+" ("+existingReservation.getReservationIndex()+")"+" (step "+resStep+")");

                        evictExistingReservations(existingReservation);

                        // and set current reservation to be the wait reservation
                        reserveElementForAgent(reservationElement, agent, reservationType, previousReservation);

                        System.out.println("\t\t\t\t\tNew reservation: "+reservationElement.getFARExtension().getReservation(resStep));
                    }

                } else { // new reservation overwrites existing reservation - NEVER

                }
            }
        } else { // if reservationType is NULL
            throw new RuntimeException();
        }
    }

    private void reserveProxyForAgent(Element reservationElement, FARAgent agent, Reservation previousReservation) {
        //reservationElement.getFARExtension().setIsProxy(true);

        int resStep;
        if (previousReservation == null) resStep = 0; // should not happen
        else resStep = previousReservation.getStep() + 1;

        List proxyPath = reservationElement.getFARExtension().getProxyPath(resStep);

        List agentPath = previousReservation.getReservationPath();

        for (int i = 0; i < proxyPath.size(); i++) { // ADDING THE PROXY PATH TO THE AGENT PATH
            Element currentProxyElement = (Element) proxyPath.getNodeData(i);
            agentPath.insertAsNode(currentProxyElement, resStep+i);
        }

        Element proxyStart = (Element) proxyPath.getNodeData(0);

        int reservationIndex = RESERVATION_DEPTH-agent.getAgentGroup()-1;
        if (previousReservation != null) reservationIndex = (previousReservation.getReservationIndex() + 1) % RESERVATION_DEPTH;

        Reservation newReservation = new Reservation(ReservationType.PROXY, proxyStart, agent, reservationIndex, resStep, previousReservation); // CHECK IF THIS IS CORRECT
        newReservation.setReservationPath(agentPath);
        
        Reservation newGhostReservation = null;
        if (previousReservation != null) newGhostReservation = new Reservation(ReservationType.GHOST, previousReservation.getElement(), agent, reservationIndex, resStep);
        if (previousReservation != null) newGhostReservation.setOriginalReservation(newReservation);
        newReservation.setGhostReservation(newGhostReservation);

        proxyStart.getFARExtension().setReservation(resStep, newReservation);

        previousReservation.setDependentReservation(newReservation);
        agent.setLastReservation(newReservation);
    }

    private void reserveElementForAgent(Element reservationElement, FARAgent agent, ReservationType reservationType, Reservation previousReservation) {
        //if (reservationType == ReservationType.WAIT) reservationElement.getFARExtension().setIsWait(true);

        int resStep;
        if (reservationType == ReservationType.INITIAL && previousReservation == null) resStep = 0;
        else resStep = previousReservation.getStep() + 1;

        List agentPath;
        if (reservationType == ReservationType.INITIAL && previousReservation == null) agentPath = agent.getAgentPath();
        else agentPath = previousReservation.getReservationPath();
        if (reservationType == ReservationType.INITIAL && previousReservation != null) agentPath.insertAsNode(reservationElement, resStep); // MAKE SURE THIS IS CORRECT
        else if (reservationType == ReservationType.WAIT) agentPath.insertAsNode(reservationElement, resStep); // MAKE SURE THIS IS CORRECT

        int reservationIndex = RESERVATION_DEPTH-agent.getAgentGroup()-1;
        if (previousReservation != null) reservationIndex = (previousReservation.getReservationIndex() + 1) % RESERVATION_DEPTH;
        Reservation newReservation = new Reservation(reservationType, reservationElement, agent, reservationIndex, resStep, previousReservation);
        newReservation.setReservationPath(agentPath);

        Reservation newGhostReservation = null;
        if (previousReservation != null) newGhostReservation = new Reservation(ReservationType.GHOST, previousReservation.getElement(), agent, reservationIndex, resStep);
        if (previousReservation != null) newGhostReservation.setOriginalReservation(newReservation);
        newReservation.setGhostReservation(newGhostReservation);

        reservationElement.getFARExtension().setReservation(resStep, newReservation);

        if (previousReservation != null) previousReservation.setDependentReservation(newReservation);
        else agent.setFirstReservation(newReservation);
        agent.setLastReservation(newReservation);
    }

    private void evictExistingReservations(Reservation existingReservation) {
        FARAgent existingAgent = existingReservation.getAgent();

        Reservation reservationToRemove = existingReservation;
        Reservation ghostReservationToRemove = existingReservation.getGhostReservation();
        System.out.println("\t\t\t\t\t"+(RESERVATION_DEPTH-existingReservation.getReservationIndex())+" reservations should be removed:");
        //reservationToRemove.getAgent().setLastReservation(reservationToRemove.getPreviousReservation()); // set the removed agent's last reservation to be the last one that was not removed

        int removeStep = reservationToRemove.getStep();

        int counter = 0;
        while (reservationToRemove != null) {
            System.out.println("\t\t\t\t\t\tRemoving reservation: "+reservationToRemove);
            reservationToRemove.getElement().getFARExtension().setReservation(removeStep + counter, null);
            ghostReservationToRemove.getElement().getFARExtension().setGhostReservation(removeStep + counter, null);
            reservationToRemove = reservationToRemove.getDependentReservation();
            counter += 1;
        }

        Reservation prevExistingReservation = existingReservation.getPreviousReservation();

        prevExistingReservation.setDependentReservation(null);
        existingAgent.setLastReservation(prevExistingReservation);

        agentReservationOrderAtStep.enqueue(existingAgent, prevExistingReservation.getStep() + 1);
    }

    private void fieldFlowAnnotation() {
        for (int i = 1; i < (field.X_DIMENSION - 1); i++) {
            for (int j = 1; j < (field.Y_DIMENSION - 1); j++) {
                Element currentElement = field.getElement(i, j);

                boolean flowNorthward = false;
                boolean flowSouthward = false;
                boolean flowWestward = false;
                boolean flowEastward = false;

                boolean nBl = field.getNorthElement(currentElement).isBlocked();
                boolean neBl = field.getNorthEastElement(currentElement).isBlocked();
                boolean eBl = field.getEastElement(currentElement).isBlocked();
                boolean seBl = field.getSouthEastElement(currentElement).isBlocked();
                boolean sBl = field.getSouthElement(currentElement).isBlocked();
                boolean swBl = field.getSouthWestElement(currentElement).isBlocked();
                boolean wBl = field.getWestElement(currentElement).isBlocked();
                boolean nwBl = field.getNorthWestElement(currentElement).isBlocked();

                boolean northBlocked = field.getNorthElement(currentElement).isBlocked();
                boolean eastBlocked = field.getEastElement(currentElement).isBlocked();
                boolean southBlocked = field.getSouthElement(currentElement).isBlocked();
                boolean westBlocked = field.getWestElement(currentElement).isBlocked();

                if (!currentElement.isObstacle()) {
                    if ((i % 2) == 1) {
                        flowNorthward = true;
                    } else {
                        flowSouthward = true;
                    }

                    if ((j % 2) == 1) {
                        flowEastward = true;
                    } else {
                        flowWestward = true;
                    }

                    if (flowEastward && !eastBlocked) {
                        currentElement.getFARExtension().setHorizontalAccessTo(field.getEastElement(currentElement));
                    } else if (flowWestward && !westBlocked) {
                        currentElement.getFARExtension().setHorizontalAccessTo(field.getWestElement(currentElement));
                    }

                    if (flowNorthward && !northBlocked) {
                        currentElement.getFARExtension().setVerticalAccessTo(field.getNorthElement(currentElement));
                    } else if (flowSouthward && !southBlocked) {
                        currentElement.getFARExtension().setVerticalAccessTo(field.getSouthElement(currentElement));
                    }

                    // Zbyněk Stara
                    if (flowNorthward && !northBlocked) {
                        boolean nnBl = field.getNorthElement(field.getNorthElement(currentElement)).isBlocked();
                        if (flowEastward) {
                            boolean nneBl = field.getNorthEastElement(field.getNorthElement(currentElement)).isBlocked();
                            if (!((!wBl && !nwBl) || (!nnBl && !nneBl && !neBl && !eBl && !seBl && !sBl))) {
                                field.getNorthElement(currentElement).getFARExtension().setExtraVerticalAccessTo(currentElement);
                            }
                        } else if (flowWestward) {
                            boolean nnwBl = field.getNorthWestElement(field.getNorthElement(currentElement)).isBlocked();
                            if (!((!eBl && !neBl) || (!nnBl && !nnwBl && !nwBl && ! wBl && !swBl && !sBl))) {
                                field.getNorthElement(currentElement).getFARExtension().setExtraVerticalAccessTo(currentElement);
                            }
                        }
                    } else if (flowSouthward && !southBlocked) {
                        boolean ssBl = field.getSouthElement(field.getSouthElement(currentElement)).isBlocked();
                        if (flowEastward) {
                            boolean sseBl = field.getSouthEastElement(field.getSouthElement(currentElement)).isBlocked();
                            if (!((!wBl && !swBl) || (!ssBl && !sseBl && !seBl && !eBl && !neBl && !nBl))) {
                                field.getSouthElement(currentElement).getFARExtension().setExtraVerticalAccessTo(currentElement);
                            }
                        } else if (flowWestward) {
                            boolean sswBl = field.getSouthWestElement(field.getSouthElement(currentElement)).isBlocked();
                            if (!((!eBl && !seBl) || (!ssBl && !sswBl && !swBl && !wBl && !nwBl && !nBl))) {
                                field.getSouthElement(currentElement).getFARExtension().setExtraVerticalAccessTo(currentElement);
                            }
                        }
                    }

                    if (flowEastward && !eastBlocked) {
                        boolean eeBl = field.getEastElement(field.getEastElement(currentElement)).isBlocked();
                        if (flowNorthward) {
                            boolean eneBl = field.getNorthEastElement(field.getEastElement(currentElement)).isBlocked();
                            if (!((!sBl && !seBl) || (!eeBl && !eneBl && !neBl && !nBl && !nwBl && !wBl))) {
                                field.getEastElement(currentElement).getFARExtension().setExtraHorizontalAccessTo(currentElement);
                            }
                        } else if (flowSouthward) {
                            boolean eseBl = field.getSouthEastElement(field.getEastElement(currentElement)).isBlocked();
                            if (!((!nBl && !neBl) || (!eeBl && !eseBl && !seBl && !sBl && !swBl && !wBl))) {
                                field.getEastElement(currentElement).getFARExtension().setExtraHorizontalAccessTo(currentElement);
                            }
                        }
                    } else if (flowWestward && !westBlocked) {
                        boolean wwBl = field.getWestElement(field.getWestElement(currentElement)).isBlocked();
                        if (flowNorthward) {
                            boolean wnwBl = field.getNorthWestElement(field.getWestElement(currentElement)).isBlocked();
                            if (!((!sBl && !swBl) || (!wwBl && !wnwBl && !nwBl && !nBl && !neBl && !eBl))) {
                                field.getWestElement(currentElement).getFARExtension().setExtraHorizontalAccessTo(currentElement);
                            }
                        } else if (flowSouthward) {
                            boolean wswBl = field.getSouthWestElement(field.getWestElement(currentElement)).isBlocked();
                            if (!((!nBl && !nwBl) || (!wwBl && !wswBl && !swBl && !sBl && !seBl && !eBl))) {
                                field.getWestElement(currentElement).getFARExtension().setExtraHorizontalAccessTo(currentElement);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateInfo() {
        gui.getInfoStepTF().setText(simStep + "");
        gui.getInfoSuccessTF().setText(numSuccesses + "");
        gui.getInfoTimeTF().setText(((System.currentTimeMillis() - startTime) / 1000) + "");
    }

    @Override public String toString() {
        return ("Pathfinder at step " + simStep + ": Number of AAgents: " + farAgents.length);
    }
}
