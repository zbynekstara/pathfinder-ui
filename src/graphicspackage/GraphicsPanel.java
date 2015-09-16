   package graphicspackage;

/**
 *
 * @author Zbyněk Stara
 */
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.*;
import javax.swing.*;
import generalpackage.*;
import adtpackage.*;

public class GraphicsPanel extends JPanel {
    private final GraphicsConstants gc = new GraphicsConstants();
    private final GraphicsUtil gu = new GraphicsUtil();

    private int dimension;
    private int numElements; // remember that these INCLUDE boundaries

    private Field f;
    private Pathfinder pf;
    private int step;

    private RepaintState rs = RepaintState.REPAINT_TABLE;

    public GraphicsPanel() {

    }

    public GraphicsPanel(int numElements) {
        redrawPanel(numElements);
    }

    public GraphicsPanel(int numElements, RepaintState rs) {
        redrawPanel(numElements, rs);
    }

    public GraphicsPanel(int numElements, RepaintState rs, Field f) {
        redrawPanel(numElements, rs, f);
    }

    public GraphicsPanel(int numElements, RepaintState rs, Field f, Pathfinder pf, int step) {
        redrawPanel(numElements, rs, f, pf, step);
    }
    
    public final void redrawPanel(int numElements) {
        this.numElements = numElements;
        this.dimension = (numElements*gc.ELEMENT_SIZE) + ((numElements+1)*gc.LINE_WIDTH);
        this.setSize(dimension, dimension);

        repaint();
    }

    public final void redrawPanel(int numElements, RepaintState rs) {
        this.numElements = numElements;
        this.dimension = (numElements*gc.ELEMENT_SIZE) + ((numElements+1)*gc.LINE_WIDTH);
        this.setSize(dimension, dimension);

        this.rs = rs;

        repaint();
    }

    public final void redrawPanel(int numElements, RepaintState rs, Field f) {
        this.numElements = numElements;
        this.dimension = (numElements*gc.ELEMENT_SIZE) + ((numElements+1)*gc.LINE_WIDTH);
        this.setSize(dimension, dimension);

        this.rs = rs;

        this.f = f;

        repaint();
    }

    public final void redrawPanel(int numElements, RepaintState rs, Field f, Pathfinder pf, int step) {
        this.numElements = numElements;
        this.dimension = (numElements*gc.ELEMENT_SIZE) + ((numElements+1)*gc.LINE_WIDTH);
        this.setSize(dimension, dimension);

        this.rs = rs;

        this.f = f;
        this.pf = pf;
        this.step = step;

        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // in order of appearance:
        if (rs.getValue() >= RepaintState.REPAINT_TABLE.getValue()) paintTable(g2);
        if (rs.getValue() >= RepaintState.REPAINT_PATHS.getValue()) paintMarkedElements(g2, f, pf);
        if (rs.getValue() >= RepaintState.REPAINT_OBSTACLES.getValue()) paintObstacles(g2, f);
        if (rs.getValue() >= RepaintState.REPAINT_PATHS.getValue()) paintPaths(g2, f, pf, step);
        if (rs.getValue() >= RepaintState.REPAINT_STARTS.getValue()) paintStarts(g2, f);
        if (rs.getValue() >= RepaintState.REPAINT_GOALS.getValue()) paintGoals(g2, f);
        if (rs.getValue() >= RepaintState.REPAINT_AGENTS.getValue()) paintAgents(g2, f, pf, step);
    }

    private void paintTable(Graphics2D g2) {
        // Draw vertical gridlines:
        for (int i = 0; i < numElements+1; i++) {
            float currentX = i * (gc.ELEMENT_SIZE+gc.LINE_WIDTH);

            g2.setColor(Color.BLACK);
            g2.draw(new Line2D.Float(currentX, (float) 0, currentX, (float) (dimension-1)));
        }

        // Draw horizontal gridlines:
        for (int i = 0; i < numElements+1; i++) {
            float currentY = i * (gc.ELEMENT_SIZE+gc.LINE_WIDTH);

            g2.setColor(Color.BLACK);
            g2.draw(new Line2D.Float((float) 0, currentY, (float) (dimension-1), currentY));
        }
    }

    private void paintObstacles(Graphics2D g2, Field f) {
        for (int x = 0; x < numElements; x++) {
            for (int y = 0; y < numElements; y++) {
                Point2D.Float ec = gu.getElementCorner(x, y);

                if (f.getElement(x, y).isBoundary()) {
                    // PAINTING BOUNDARIES
                    g2.setColor(Color.BLACK);
                    g2.fill(new Rectangle2D.Float(ec.x, ec.y, gc.ELEMENT_SIZE, gc.ELEMENT_SIZE));
                    
                    if (((y == 0) || (y == numElements-1)) && (x != 0) && (x != numElements-1)) {
                        gu.drawNumber(g2, ec, x, Color.WHITE);
                    } else if (((x == 0) || (x == numElements-1)) && (y != 0) && (y != numElements-1)) {
                        gu.drawNumber(g2, ec, y, Color.WHITE);
                    }
                } else if (f.getElement(x, y).isObstacle()) {
                    // PAINITNG OBSTACLES
                    g2.setColor(Color.BLACK);
                    g2.fill(new Rectangle2D.Float(ec.x, ec.y, gc.ELEMENT_SIZE, gc.ELEMENT_SIZE));
                } else { // if element is blank
                    if (f.getElement(x, y).getFARExtension() != null) {
                        // PAINTING ACCESSIBILITY ARROWS
                        g2.setColor(Color.GRAY);
                        List accessibleList = f.getElement(x, y).getFARExtension().getAccessibleElements();

                        while (!accessibleList.isEmpty()) {
                            int gces = gc.ELEMENT_SIZE;
                            int gcec = (int) (((float) gc.ELEMENT_SIZE)/(float) 2); // element center
                            int gclw = gc.LINE_WIDTH;
                            
                            Element currentAccessible = (Element) accessibleList.removeFirst();
                            if (f.getNorthElement(f.getElement(x, y)) == currentAccessible) {
                                g2.draw(new Line2D.Float(ec.x+gcec-1, ec.y-gclw-1, ec.x+gcec+1, ec.y-gclw-1));
                                g2.draw(new Line2D.Float(ec.x+gcec, ec.y-gclw-2, ec.x+gcec, ec.y-gclw-2));
                            }
                            else if(f.getEastElement(f.getElement(x, y)) == currentAccessible) {
                                g2.draw(new Line2D.Float(ec.x+gces+gclw, ec.y+gcec-1, ec.x+gces+gclw, ec.y+gcec+1));
                                g2.draw(new Line2D.Float(ec.x+gces+gclw+1, ec.y+gcec, ec.x+gces+gclw+1, ec.y+gcec));
                            }
                            else if(f.getSouthElement(f.getElement(x, y)) == currentAccessible) {
                                g2.draw(new Line2D.Float(ec.x+gcec-1, ec.y+gces+gclw, ec.x+gcec+1, ec.y+gces+gclw));
                                g2.draw(new Line2D.Float(ec.x+gcec, ec.y+gces+gclw+1, ec.x+gcec, ec.y+gces+gclw+1));
                            }
                            else if(f.getWestElement(f.getElement(x, y)) == currentAccessible) {
                                g2.draw(new Line2D.Float(ec.x-gclw-1, ec.y+gcec-1, ec.x-gclw-1, ec.y+gcec+1));
                                g2.draw(new Line2D.Float(ec.x-gclw-2, ec.y+gcec, ec.x-gclw-2, ec.y+gcec));
                            }
                        }
                    }
                }
            }
        }
    }

    private void paintStarts(Graphics2D g2, Field f) {
        Element [] agentStarts = f.getAgentStartElements();

        for (int i = 0; i < agentStarts.length; i++) {
            Element currentStart = agentStarts[i];

            Point2D.Float ec = gu.getElementCorner(currentStart.X_ID, currentStart.Y_ID); // ec is elementCorner

            //Color agentColor = f.getAgentArray()[i].getAgentColor();
            //Color contrastColor = gu.contrastColor(agentColor);

            // drawing an endpoint rectangle
            g2.setColor(Color.WHITE);
            g2.fill(new Rectangle2D.Float((float) ec.x+1, (float) ec.y+1, (float) gc.AGENT_SIZE-1, (float) gc.AGENT_SIZE-1));
            g2.setColor(Color.BLACK);
            g2.draw(new Rectangle2D.Float((float) ec.x+1, (float) ec.y+1, (float) gc.AGENT_SIZE-1, (float) gc.AGENT_SIZE-1));

            // drawing the number
            gu.drawNumber(g2, ec, i, Color.BLACK);

            // the start underscore
            /*g2.setColor(agentColor);
            g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+11, (float) ec.x+10, (float) ec.y+11));*/
        }
    }

    private void paintGoals(Graphics2D g2, Field f) {
        Element [] agentGoals = f.getAgentGoalElements();

        for (int i = 0; i < agentGoals.length; i++) {
            Element currentGoal = agentGoals[i];

            Point2D.Float ec = gu.getElementCorner(currentGoal.X_ID, currentGoal.Y_ID); // ec is elementCorner

            Color agentColor = f.getAgentArray()[i].getAgentColor();
            Color contrastColor = gu.contrastColor(agentColor);

            // drawing an endpoint rectangle
            g2.setColor(agentColor);
            g2.fill(new Rectangle2D.Float((float) ec.x+1, (float) ec.y+1, (float) gc.AGENT_SIZE-1, (float) gc.AGENT_SIZE-1));
            g2.setColor(Color.BLACK);
            g2.draw(new Rectangle2D.Float((float) ec.x+1, (float) ec.y+1, (float) gc.AGENT_SIZE-1, (float) gc.AGENT_SIZE-1));

            // drawing the number
            gu.drawNumber(g2, ec, i, contrastColor);

            // the end overbar
            /*g2.setColor(contrastColor);
            g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+3, (float) ec.x+10, (float) ec.y+3));*/
        }
    }

    private void paintMarkedElements(Graphics2D g2, Field f, Pathfinder pf) {
        /*for (int x = 0; x < numElements; x++) {
            for (int y = 0; y < numElements; y++) {
                Point2D.Float ec = gu.getElementCorner(x, y);

                if (f.getElement(x, y).getFARExtension() != null) {
                    // PAINTING WAIT ELEMENTS
                    if (f.getElement(x, y).getFARExtension().isWait()) {
                        g2.setColor(Color.ORANGE);
                        g2.fill(new Rectangle2D.Float(ec.x, ec.y, gc.ELEMENT_SIZE, gc.ELEMENT_SIZE));
                    }

                    // PAINTING PROXY ELEMENTS
                    if (f.getElement(x, y).getFARExtension().isProxy()) {
                        g2.setColor(Color.PINK);
                        g2.fill(new Rectangle2D.Float(ec.x, ec.y, gc.ELEMENT_SIZE, gc.ELEMENT_SIZE));
                    }

                    // PAINTING FAILURES
                    if (f.getElement(x, y).getFARExtension().isFailure()) {
                        g2.setColor(Color.RED);
                        g2.fill(new Rectangle2D.Float(ec.x, ec.y, gc.ELEMENT_SIZE, gc.ELEMENT_SIZE));
                    }
                }
            }
        }*/
    }

    private void paintPaths(Graphics2D g2, Field f, Pathfinder pf, int step) {
        List [] agentPathLists = pf.getAgentPathsUntilStep(step);
        for (List currentPath : agentPathLists) {
            for (int j = 0; j < currentPath.size()-1; j++) {
                Element firstElement = (Element) currentPath.getNodeData(j);
                Element secondElement = (Element) currentPath.getNodeData(j+1);
                
                Point2D.Float firstCenter = gu.getElementCenter(firstElement.X_ID, firstElement.Y_ID);
                Point2D.Float secondCenter = gu.getElementCenter(secondElement.X_ID, secondElement.Y_ID);
                // throws NullPointerException if the element provided is null
                
                g2.setColor(gu.gradient(Color.GREEN, Color.RED, pf.FAILURE_CRITERION, j));
                g2.draw(new Line2D.Float(firstCenter, secondCenter));
            }
        }
    }

    private void paintAgents(Graphics2D g2, Field f, Pathfinder pf, int step) {
        Element [] agentElements = pf.getAgentsAtStep(step);
        for (int i = 0; i < agentElements.length; i++) {
            Element currentAgentElement = agentElements[i];

            Point2D.Float ec = gu.getElementCorner(currentAgentElement.X_ID, currentAgentElement.Y_ID); // ec is elementCorner

            Color agentColor = f.getAgentArray()[i].getAgentColor();
            Color contrastColor = gu.contrastColor(agentColor);

            g2.setColor(agentColor);
            g2.fill(new Ellipse2D.Float((float) ec.x+1, (float) ec.y+1, (float) gc.AGENT_SIZE-1, (float) gc.AGENT_SIZE-1));
            g2.setColor(Color.BLACK);
            g2.draw(new Ellipse2D.Float((float) ec.x+1, (float) ec.y+1, (float) gc.AGENT_SIZE-1, (float) gc.AGENT_SIZE-1));

            // drawing the number
            gu.drawNumber(g2, ec, i, contrastColor);
        }

    }

    public int getDimension() {
        return dimension;
    }

    public int getNumElements() {
        return numElements;
    }
}
