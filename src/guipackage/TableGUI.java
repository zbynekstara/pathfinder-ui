package guipackage;

import adtpackage.*;
import generalpackage.*;

/*
 * TableGUI.java
 *
 * Created on Apr 28, 2012
 */

/**
 *
 * @author Zbyněk Stara
 */
public class TableGUI extends GUI {
    // IMPORTANT: the setValueAt methods use ordering of (…, y, x); the Elements have (x, y)

    private final int MAX_FIELD = 50; // 50 = defalut: 2500 elements = 500 obstacles + 1000 starts + 1000 goals
                                // another suggested value would be 35: 1225 elements = 245 obstacles + 980 free spaces
                                // or 34: which fits nicely on the screen

    private final int INIT_FIELD = 34; // remember that these are without the boundaries!

    private final String BOUNDARY = "█";
    private final String OBSTACLE = "█";
    private final String BLANK = "";
    private final String START = "S";
    private final String GOAL = "G";
    private final String PATH = "o";
    private final String AGENT = "•";

    protected void refreshField() {
        drawField(field.X_DIMENSION);
    }

    protected void refreshPathEndpoints() {
        drawField(field.X_DIMENSION);

        drawFieldStarts(field.getAgentStartElements());
        drawFieldGoals(field.getAgentGoalElements());
    }
    
    protected void refreshPaths() {
        drawField(field.X_DIMENSION);

        drawFieldPathsUntilStep(choosePathfinder().getAgentPathsUntilStep(choosePathfinder().FAILURE_CRITERION));

        drawFieldStarts(field.getAgentStartElements());
        drawFieldGoals(field.getAgentGoalElements());
    }

    protected void refreshAgents() {
        drawField(field.X_DIMENSION);

        drawFieldPathsUntilStep(choosePathfinder().getAgentPathsUntilStep(stepCounter));

        drawFieldStarts(field.getAgentStartElements());
        drawFieldGoals(field.getAgentGoalElements());

        drawFieldAgentsAtStep(choosePathfinder().getAgentsAtStep(stepCounter));
    }

    private void drawField(int size) {
        for (int i = 0; i < MAX_FIELD; i++) {
            for (int j = 0; j < MAX_FIELD; j++) {
                fieldTable.setValueAt("", j, i);
            }
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (field.getElement(i, j).isBoundary()) {
                    fieldTable.setValueAt(BOUNDARY, j, i);
                } else if (field.getElement(i, j).isObstacle()) {
                    fieldTable.setValueAt(OBSTACLE, j, i);
                } else {
                    fieldTable.setValueAt(BLANK, j, i);
                    if (field.getElement(i, j).getFARExtension() != null) {
                        String debug = "";
                        List accessibleList = field.getElement(i, j).getFARExtension().getAccessibleElements();
                        int accessibleListSize = accessibleList.size();
                        for (int k = 0; k < accessibleListSize; k++) {
                            Element currentAccessible = (Element) accessibleList.removeFirst();
                            if (field.getNorthElement(field.getElement(i, j)) == currentAccessible) debug = debug + "↑";
                            else if(field.getNorthEastElement(field.getElement(i, j)) == currentAccessible) debug = debug + "↗";
                            else if(field.getEastElement(field.getElement(i, j)) == currentAccessible) debug = debug + "→";
                            else if(field.getSouthEastElement(field.getElement(i, j)) == currentAccessible) debug = debug + "↘";
                            else if(field.getSouthElement(field.getElement(i, j)) == currentAccessible) debug = debug + "↓";
                            else if(field.getSouthWestElement(field.getElement(i, j)) == currentAccessible) debug = debug + "↙";
                            else if(field.getWestElement(field.getElement(i, j)) == currentAccessible) debug = debug + "←";
                            else if(field.getNorthWestElement(field.getElement(i, j)) == currentAccessible) debug = debug + "↖";
                        }
                        fieldTable.setValueAt(debug, j, i);
                    }
                }
            }
        }
    }

    private void drawFieldStarts(Element [] startElements) {
        for (int i = 0; i < startElements.length; i++) {
            Element currentStartElement = startElements[i];
            fieldTable.setValueAt(START + i, currentStartElement.Y_ID, currentStartElement.X_ID);
        }
    }

    private void drawFieldGoals(Element [] goalElements) {
        for (int i = 0; i < goalElements.length; i++) {
            Element currentStartElement = goalElements[i];
            fieldTable.setValueAt(GOAL + i, currentStartElement.Y_ID, currentStartElement.X_ID);
        }
    }

    private void drawFieldAgentsAtStep(Element [] agentElements) {
        for (int i = 0; i < agentElements.length; i++) {
            Element currentAgentElement = agentElements[i];
            fieldTable.setValueAt(AGENT + i, currentAgentElement.Y_ID, currentAgentElement.X_ID);
        }
    }

    private void drawFieldPathsUntilStep(List [] agentPathArray) { // includes start and step
        for (int i = 0; i < agentPathArray.length; i++) {
            List currentPath = agentPathArray[i];

            for (int j = 0; j < currentPath.size(); j++) {
                Element currentPathElement = (Element) currentPath.getNodeData(j);
                fieldTable.setValueAt(PATH, currentPathElement.Y_ID, currentPathElement.X_ID);
            }
        }
    }

    private void myInitComponents() {
        sizeSlider.setMaximum(MAX_FIELD);
        sizeSlider.setMinimum(1);
        sizeSlider.setValue(INIT_FIELD);

        fieldSizeTF.setEditable(false);
        numberAgentsTF.setText(1 + "");
        numberPathFailuresTF.setEditable(false);
        currentStepTF.setText(-1 + "");
        currentStepTF.setEditable(false);
    }

    public TableGUI() {
        initComponents();
        myInitComponents();
    }

    public javax.swing.JDialog getInfoDialog() {
        return infoDialog;
    }

    public javax.swing.JTextField getInfoStepTF() {
        return infoStepTF;
    }

    public javax.swing.JTextField getInfoSuccessTF() {
        return infoSuccessTF;
    }

    public javax.swing.JTextField getInfoTimeTF() {
        return infoTimeTF;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoDialog = new javax.swing.JDialog();
        jLabel6 = new javax.swing.JLabel();
        infoStepTF = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        infoTimeTF = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        infoSuccessTF = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        fieldTable = new javax.swing.JTable();
        sizeSlider = new javax.swing.JSlider();
        refreshFieldButton = new javax.swing.JButton();
        fieldSizeTF = new javax.swing.JTextField();
        findPathsButton = new javax.swing.JButton();
        numberAgentsTF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        currentStepTF = new javax.swing.JTextField();
        nextStepButton = new javax.swing.JButton();
        initialStateButton = new javax.swing.JButton();
        numberPathFailuresTF = new javax.swing.JTextField();
        resetPathButton = new javax.swing.JButton();
        algorithmComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        refreshAgentsButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        infoDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        infoDialog.setTitle("Information");
        infoDialog.setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        infoDialog.setSize(new java.awt.Dimension(400, 190));

        jLabel6.setText("Finding agent reservations, please wait...");

        infoStepTF.setFocusable(false);

        jLabel7.setText("Current simulation step:");

        infoTimeTF.setFocusable(false);

        jLabel8.setText("Time elapsed:");

        jLabel9.setText("Simulation successful for:");

        infoSuccessTF.setFocusable(false);

        org.jdesktop.layout.GroupLayout infoDialogLayout = new org.jdesktop.layout.GroupLayout(infoDialog.getContentPane());
        infoDialog.getContentPane().setLayout(infoDialogLayout);
        infoDialogLayout.setHorizontalGroup(
            infoDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(infoDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(infoDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(infoDialogLayout.createSequentialGroup()
                        .add(infoDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(infoDialogLayout.createSequentialGroup()
                                .add(jLabel7)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(infoStepTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(infoDialogLayout.createSequentialGroup()
                                .add(jLabel9)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(infoSuccessTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(166, Short.MAX_VALUE))
                    .add(infoDialogLayout.createSequentialGroup()
                        .add(jLabel8)
                        .add(16, 16, 16)
                        .add(infoTimeTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                        .add(237, 237, 237))
                    .add(infoDialogLayout.createSequentialGroup()
                        .add(jLabel6)
                        .addContainerGap(123, Short.MAX_VALUE))))
        );
        infoDialogLayout.setVerticalGroup(
            infoDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(infoDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(infoDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(infoStepTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(infoDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(infoSuccessTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(infoDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(infoTimeTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8))
                .add(36, 36, 36))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        fieldTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        fieldTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        fieldTable.setGridColor(new java.awt.Color(153, 153, 153));
        fieldTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(fieldTable);

        sizeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sizeSliderStateChanged(evt);
            }
        });

        refreshFieldButton.setText("Field");
        refreshFieldButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                refreshFieldButtonMouseReleased(evt);
            }
        });

        findPathsButton.setText("Find paths");
        findPathsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                findPathsButtonMouseReleased(evt);
            }
        });

        numberAgentsTF.setText("1");

        jLabel2.setText("Step:");

        nextStepButton.setText("Next step");
        nextStepButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                nextStepButtonMouseReleased(evt);
            }
        });

        initialStateButton.setText("Initial state");
        initialStateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                initialStateButtonMouseReleased(evt);
            }
        });

        resetPathButton.setText("-1");
        resetPathButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                resetPathButtonMouseReleased(evt);
            }
        });

        algorithmComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "A*", "LRA*", "CA*", "WHCA*", "FAR" }));
        algorithmComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                algorithmComboBoxItemStateChanged(evt);
            }
        });

        jLabel3.setText("Failures:");

        jLabel4.setText("Algorithm:");

        refreshAgentsButton.setText("Agents");
        refreshAgentsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                refreshAgentsButtonMouseReleased(evt);
            }
        });

        fileMenu.setText("File");

        openMenuItem.setText("Open");
        fileMenu.add(openMenuItem);

        saveMenuItem.setText("Save");
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText("Save As ...");
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");

        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setText("Help");

        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(refreshFieldButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sizeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fieldSizeTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(refreshAgentsButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(numberAgentsTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(algorithmComboBox, 0, 122, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(findPathsButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(numberPathFailuresTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(currentStepTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextStepButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(initialStateButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(resetPathButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 1239, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(resetPathButton)
                        .add(initialStateButton)
                        .add(nextStepButton)
                        .add(fieldSizeTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(refreshAgentsButton)
                        .add(numberAgentsTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel4)
                        .add(algorithmComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(findPathsButton)
                        .add(jLabel3)
                        .add(numberPathFailuresTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel2)
                        .add(currentStepTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(sizeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(refreshFieldButton))
                .add(18, 18, 18)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 596, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void sizeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sizeSliderStateChanged
        fieldSizeTF.setText(sizeSlider.getValue() + "");
    }//GEN-LAST:event_sizeSliderStateChanged

    private void refreshFieldButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refreshFieldButtonMouseReleased
        numElements = sizeSlider.getValue();

        isFieldSet = true;
        areAgentsSet = false;
        arePathsSet = false;

        stepCounter = -1;
        currentStepTF.setText(stepCounter + "");

        field = new Field(numElements+2, numElements+2);

        field.generateObstacles();
        field.assignFreeGroups();
        field.fillClosedGroups();

        refreshField();
    }//GEN-LAST:event_refreshFieldButtonMouseReleased

    private void findPathsButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_findPathsButtonMouseReleased
        if (isFieldSet && areAgentsSet) {
            arePathsSet = true;

            stepCounter = -1;
            currentStepTF.setText(stepCounter + "");

            refreshPaths();
        }
    }//GEN-LAST:event_findPathsButtonMouseReleased

    private void nextStepButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nextStepButtonMouseReleased
        if (isFieldSet && areAgentsSet && arePathsSet) {
            stepCounter++;
            currentStepTF.setText(stepCounter + "");

            refreshAgents();
        }
    }//GEN-LAST:event_nextStepButtonMouseReleased

    private void initialStateButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_initialStateButtonMouseReleased
        if (isFieldSet && areAgentsSet && arePathsSet) {
            stepCounter = 0;
            currentStepTF.setText(stepCounter + "");

            refreshAgents();
        }
    }//GEN-LAST:event_initialStateButtonMouseReleased

    private void resetPathButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resetPathButtonMouseReleased
        if (isFieldSet && areAgentsSet && arePathsSet) {
            stepCounter = -1;
            currentStepTF.setText(stepCounter + "");

            refreshPaths();
        }
    }//GEN-LAST:event_resetPathButtonMouseReleased

    private void algorithmComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_algorithmComboBoxItemStateChanged
        if (isFieldSet && areAgentsSet) {
            algorithmToUse = algorithmComboBox.getSelectedIndex();

            initializePathfinder(this);
            choosePathfinder().findAgentPaths();

            arePathsSet = false;

            stepCounter = -1;
            currentStepTF.setText(stepCounter + "");

            refreshPathEndpoints();
        }
    }//GEN-LAST:event_algorithmComboBoxItemStateChanged

    private void refreshAgentsButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refreshAgentsButtonMouseReleased
        if (isFieldSet) {
            stepCounter = -1;
            currentStepTF.setText(stepCounter + "");

            field.assignAgents(Integer.parseInt(numberAgentsTF.getText()));
            areAgentsSet = true;
            initializePathfinder(this);
            choosePathfinder().findAgentPaths();

            refreshPathEndpoints();
        }
    }//GEN-LAST:event_refreshAgentsButtonMouseReleased

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TableGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JComboBox algorithmComboBox;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JTextField currentStepTF;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JTextField fieldSizeTF;
    private javax.swing.JTable fieldTable;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton findPathsButton;
    private javax.swing.JMenu helpMenu;
    public static javax.swing.JDialog infoDialog;
    public static javax.swing.JTextField infoStepTF;
    public static javax.swing.JTextField infoSuccessTF;
    public static javax.swing.JTextField infoTimeTF;
    private javax.swing.JButton initialStateButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton nextStepButton;
    private javax.swing.JTextField numberAgentsTF;
    private javax.swing.JTextField numberPathFailuresTF;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JButton refreshAgentsButton;
    private javax.swing.JButton refreshFieldButton;
    private javax.swing.JButton resetPathButton;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JSlider sizeSlider;
    // End of variables declaration//GEN-END:variables

}
