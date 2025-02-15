package raytracer2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author chanjustin
 */
public class SimulatorFrame extends javax.swing.JFrame {
    static int BAD =0;
    static int NULL =1;
    static int GOOD =2;
    /**
     * Creates new form NewJFrame
     */
    public SimulatorFrame(LinkedList<Obstacle> obstructions, LinkedList<Obstacle> curves,
            int roomLengthInPixels, int roomWidthInPixels, boolean testMode, Cell[][] nGrid) {
        if(!testMode)
        {
            initComponents();
            
            numRaysField.setText(MAX_NUM_RAYS+"");
            maxReflectionsField.setText(MAX_REFLECTIONS+"");
            angleWindowSlider.setValue(ANGLE_WINDOW);
            frequencyField.setText(frequency+"");
            gridBox.setSelected(SHOW_GRID);
            rssBox.setSelected(SHOW_RSS);

            opacitySlider.setValue(OPACITY);
            simModeButton.setSelected(true);
        }
        
        ROOM_LENGTH_IN_METERS = feetToMeters(CELL_LENGTH_IN_FEET*NUM_COLS);
        ROOM_WIDTH_IN_METERS = feetToMeters(CELL_WIDTH_IN_FEET*NUM_ROWS);
        METERS_PER_PIXEL = (double)ROOM_LENGTH_IN_METERS/roomLengthInPixels;
        
        this.roomLengthInPixels = roomLengthInPixels;
        this.roomWidthInPixels = roomWidthInPixels;
        for(int i = 0; i < NUM_ROWS; i++)
        {
            for(int j = 0; j < NUM_COLS; j++)
            {
                realGrid[i][j] = new Cell(0);
            }
        }
        
        if(nGrid == null)
        {
            Scanner input = null;
            try
            {
                input = new Scanner(new BufferedReader(new FileReader(GROUND_TRUTH)));
                String mode = input.nextLine();
                if(mode.equals("coarse"))
                {
                    coarse = true;
                }

                int row = 0;
                while(input.hasNext())
                {
                    String[] line = input.nextLine().split(" ");
                    int col = 0;
                    for(String elt : line)
                    {
                        realGrid[row][col] = new Cell(Integer.parseInt(elt));
                        col += 1;
                    }
                    row += 1;
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if(input != null)
                {
                    input.close();
                }
            }
        }
        else
        {
            realGrid = nGrid;
        }
        
        if(!testMode)
        {
            runTrial(obstructions, curves, testMode, false, false, "", null);
        }
    }
    
    public void runTrial(LinkedList<Obstacle> obstructions, LinkedList<Obstacle> curves, 
            boolean testMode, boolean replaceGrid, boolean randomGrid, String groundTruth, Point2D[] routers)
    {
        random = new Random(20);
        //obstacle defined as offsets from the router
        //turn to absolute coordinates
        //add reflector to list of obstacles
        if(groundTruth.length() > 0)
        {
            GROUND_TRUTH = groundTruth;
        }
        if(routers != null)
        {
            this.routers = new Point2D[routers.length];
            for(int i = 0; i < routers.length; i++)
            {
                this.routers[i] = new Point2D.Double(routers[i].getX(), routers[i].getY());
            }
        }
        else
        {
            populateRouter();
        }
        
        if(replaceGrid)
        {
            realGrid = new Cell[NUM_ROWS][NUM_COLS];
            Scanner input = null;
            try
            {
                input = new Scanner(new BufferedReader(new FileReader(GROUND_TRUTH)));
                String mode = input.nextLine();
                if(mode.equals("coarse"))
                {
                    coarse = true;
                }

                int row = 0;
                while(input.hasNext())
                {   
                    String[] line = input.nextLine().split(" ");
                    int col = 0;
                    for(String elt : line)
                    {
                        realGrid[row][col] = new Cell(Integer.parseInt(elt));
                        col += 1;
                    }
                    row += 1;
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if(input != null)
                {
                    input.close();
                }
            }
        }
        
        LinkedList<Obstacle> combinedObs = new LinkedList<Obstacle>();
        
        for(Obstacle obs : obstructions)
        {
            combinedObs.add(obs.clone());
        }
        
        if(curves != null)
        {
            for(Obstacle curve : curves)
            {
                Line2D line = curve.getGeom(); 
                line.setLine(line.getX1()+CENTER_X, line.getY1()+CENTER_Y, line.getX2()+CENTER_X, line.getY2()+CENTER_Y);
                combinedObs.add(curve);
            }
        }
        
        Common.objs = combinedObs;
        
        if(testMode)
        {
            mapGenerator(null);
            populateGrid();
            printDiff();
        }
    }
    
    public void printRevGrid(Cell[][] g)
    {
        for(int i = g.length-1; i >= 0; i--)
        {
            for(int j = 0; j < g[i].length; j++)
            {
                System.out.printf("%6.1f",g[i][j].getRss());
            }
            System.out.println();
        }
        System.out.println("=================");
    }
    
    public void printGridNoBreaks(Cell[][] g)
    {
        for(int i = 0; i < g.length; i++)
        {
            for(int j = 0; j < g[i].length; j++)
            {
                System.out.printf("%6.1f\n",g[i][j].getRss());
            }
        }
        System.out.println("=================");
    }
    public void printGrid(Cell[][] g)
    {
        for(int i = 0; i < g.length; i++)
        {
            for(int j = 0; j < g[i].length; j++)
            {
                System.out.printf("%6.1f",g[i][j].getRss());
            }
            System.out.println();
        }
        System.out.println("=================");
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        simPanel = new SimPanel();
        controlPanel = new javax.swing.JPanel();
        numRaysField = new javax.swing.JTextField();
        numRaysLabel = new javax.swing.JLabel();
        angleWindowLabel = new javax.swing.JLabel();
        maxReflectionsField = new javax.swing.JTextField();
        maxReflectionsLabel = new javax.swing.JLabel();
        angleWindowSlider = new javax.swing.JSlider();
        frequencyLabel = new javax.swing.JLabel();
        frequencyField = new javax.swing.JTextField();
        display = new javax.swing.JScrollPane();
        displayArea = new javax.swing.JTextArea();
        gridBox = new javax.swing.JCheckBox();
        opacitySlider = new javax.swing.JSlider();
        opacityLabel = new javax.swing.JLabel();
        rssBox = new javax.swing.JCheckBox();
        distanceBox = new javax.swing.JCheckBox();
        rrBox = new javax.swing.JCheckBox();
        errorBox = new javax.swing.JCheckBox();
        realBox = new javax.swing.JCheckBox();
        printButton = new javax.swing.JButton();
        simModeButton = new javax.swing.JRadioButton();
        twoDModeButton = new javax.swing.JRadioButton();
        topLevelMenu = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exportButton = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Simulator");
        setMinimumSize(new java.awt.Dimension(1200, 800));

        simPanel.setPreferredSize(new java.awt.Dimension(910, 650));
        simPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                simPanelMouseDragged(evt);
            }
        });
        simPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                simPanelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout simPanelLayout = new javax.swing.GroupLayout(simPanel);
        simPanel.setLayout(simPanelLayout);
        simPanelLayout.setHorizontalGroup(
            simPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 910, Short.MAX_VALUE)
        );
        simPanelLayout.setVerticalGroup(
            simPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        numRaysField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                numRaysFieldFocusGained(evt);
            }
        });
        numRaysField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numRaysFieldActionPerformed(evt);
            }
        });

        numRaysLabel.setText("Number of rays");

        angleWindowLabel.setText("Angle Window");

        maxReflectionsField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                maxReflectionsFieldFocusGained(evt);
            }
        });
        maxReflectionsField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxReflectionsFieldActionPerformed(evt);
            }
        });

        maxReflectionsLabel.setText("Max reflections");

        angleWindowSlider.setMajorTickSpacing(60);
        angleWindowSlider.setMaximum(360);
        angleWindowSlider.setPaintLabels(true);
        angleWindowSlider.setPaintTicks(true);
        angleWindowSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                angleWindowSliderStateChanged(evt);
            }
        });

        frequencyLabel.setText("Frequency (GHz)");

        frequencyField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                frequencyFieldFocusGained(evt);
            }
        });
        frequencyField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frequencyFieldActionPerformed(evt);
            }
        });

        displayArea.setColumns(20);
        displayArea.setRows(5);
        display.setViewportView(displayArea);

        gridBox.setText("Grid");
        gridBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridBoxActionPerformed(evt);
            }
        });

        opacitySlider.setMaximum(255);
        opacitySlider.setPaintLabels(true);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setSnapToTicks(true);
        opacitySlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                opacitySliderStateChanged(evt);
            }
        });

        opacityLabel.setText("Opacity");

        rssBox.setText("RSS");
        rssBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rssBoxActionPerformed(evt);
            }
        });

        distanceBox.setText("Distance");
        distanceBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distanceBoxActionPerformed(evt);
            }
        });

        rrBox.setText("RR");
        rrBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rrBoxActionPerformed(evt);
            }
        });

        errorBox.setText("Error");
        errorBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                errorBoxActionPerformed(evt);
            }
        });

        realBox.setText("Real");
        realBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                realBoxActionPerformed(evt);
            }
        });

        printButton.setText("Print");
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        simModeButton.setText("Sim");
        simModeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simModeButtonActionPerformed(evt);
            }
        });

        twoDModeButton.setText("2D");
        twoDModeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                twoDModeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(display)
                        .addGap(9, 9, 9))
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addGap(210, 210, 210)
                        .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(errorBox)
                            .addComponent(realBox))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(controlPanelLayout.createSequentialGroup()
                            .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(opacitySlider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, controlPanelLayout.createSequentialGroup()
                                    .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(gridBox)
                                        .addComponent(opacityLabel))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(rssBox)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(distanceBox)))
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(controlPanelLayout.createSequentialGroup()
                            .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(angleWindowLabel)
                                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(rrBox)
                                    .addGroup(controlPanelLayout.createSequentialGroup()
                                        .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(numRaysField, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(numRaysLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(maxReflectionsLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(maxReflectionsField, javax.swing.GroupLayout.Alignment.LEADING))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(frequencyLabel)
                                            .addComponent(frequencyField, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(51, 51, 51))))
                            .addGap(0, 0, Short.MAX_VALUE)))))
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(angleWindowSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(printButton)
                            .addComponent(twoDModeButton)
                            .addComponent(simModeButton))))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addGap(98, 98, 98)
                .addComponent(angleWindowLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(angleWindowSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numRaysLabel)
                    .addComponent(frequencyLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numRaysField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(frequencyField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxReflectionsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxReflectionsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(64, 64, 64)
                .addComponent(realBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(errorBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gridBox)
                    .addComponent(rssBox)
                    .addComponent(distanceBox)
                    .addComponent(rrBox))
                .addGap(13, 13, 13)
                .addComponent(opacityLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(opacitySlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlPanelLayout.createSequentialGroup()
                        .addComponent(simModeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(twoDModeButton)
                .addGap(47, 47, 47)
                .addComponent(display, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(printButton)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        fileMenu.setText("File");

        exportButton.setText("Export");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        fileMenu.add(exportButton);

        topLevelMenu.add(fileMenu);

        setJMenuBar(topLevelMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(simPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(simPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(29, 29, 29))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void simPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_simPanelMouseDragged
        moveRouter(evt);
    }//GEN-LAST:event_simPanelMouseDragged
    
    public void moveRouter(java.awt.event.MouseEvent evt)
    {
        CENTER_X = evt.getX();
        CENTER_Y = evt.getY();
        this.routers[0] = new Point2D.Double(CENTER_X-10,CENTER_Y);
        this.routers[1] = new Point2D.Double(CENTER_X+10,CENTER_Y);
        simPanel.repaint();
    }
    
    private void numRaysFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numRaysFieldActionPerformed
        int temp = Integer.parseInt(numRaysField.getText());
        if(temp < 0)
        {
            numRaysField.setText("Number greater than 0");
        }
        else
        {
            MAX_NUM_RAYS = temp;
            simPanel.repaint();
        }
        numRaysField.setSelectionStart(0);
        numRaysField.setSelectionEnd(numRaysField.getText().length());
    }//GEN-LAST:event_numRaysFieldActionPerformed

    private void maxReflectionsFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxReflectionsFieldActionPerformed
        int temp = Integer.parseInt(maxReflectionsField.getText());
        if(temp < 0)
        {
            maxReflectionsField.setText("Number greater than 0");
        }
        else
        {
            MAX_REFLECTIONS = temp;
            simPanel.repaint();
        }
        maxReflectionsField.setSelectionStart(0);
        maxReflectionsField.setSelectionEnd(maxReflectionsField.getText().length());
    }//GEN-LAST:event_maxReflectionsFieldActionPerformed

    private void angleWindowSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_angleWindowSliderStateChanged
        ANGLE_WINDOW = angleWindowSlider.getValue();
        simPanel.repaint();
    }//GEN-LAST:event_angleWindowSliderStateChanged

    private void numRaysFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_numRaysFieldFocusGained
        numRaysField.setSelectionStart(0);
        numRaysField.setSelectionEnd(numRaysField.getText().length());
    }//GEN-LAST:event_numRaysFieldFocusGained

    private void maxReflectionsFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_maxReflectionsFieldFocusGained
        maxReflectionsField.setSelectionStart(0);
        maxReflectionsField.setSelectionEnd(maxReflectionsField.getText().length());
    }//GEN-LAST:event_maxReflectionsFieldFocusGained

    private void frequencyFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frequencyFieldActionPerformed
        double temp = Double.parseDouble(frequencyField.getText());
        if(temp > 0)
        {
            frequency = temp;
        }
        simPanel.repaint();                                     
        frequencyField.setSelectionStart(0);
        frequencyField.setSelectionEnd(frequencyField.getText().length());
    }//GEN-LAST:event_frequencyFieldActionPerformed

    private void frequencyFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_frequencyFieldFocusGained
        frequencyField.setSelectionStart(0);
        frequencyField.setSelectionEnd(frequencyField.getText().length());
    }//GEN-LAST:event_frequencyFieldFocusGained

    private void gridBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridBoxActionPerformed
        SHOW_GRID = gridBox.isSelected();
        simPanel.repaint();
    }//GEN-LAST:event_gridBoxActionPerformed

    private void opacitySliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_opacitySliderStateChanged
        OPACITY = opacitySlider.getValue();
        simPanel.repaint();
    }//GEN-LAST:event_opacitySliderStateChanged

    private void rssBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rssBoxActionPerformed
        SHOW_RSS = rssBox.isSelected();
        simPanel.repaint();
    }//GEN-LAST:event_rssBoxActionPerformed

    private void distanceBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distanceBoxActionPerformed
        SHOW_DISTANCE = distanceBox.isSelected();
        simPanel.repaint();
    }//GEN-LAST:event_distanceBoxActionPerformed
    int fileExportNumber = 0;
    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        PrintWriter output = null;
        try
        {
            output = new PrintWriter(new FileWriter("/Users/chanjustin/Desktop/"+fileExportNumber+".txt"));
            
            if(PRINT_ZEROS)
            {
                for(int j = 0; j <= NUM_COLS; j++)
                {
                    output.write("0 ");
                    System.out.print("0 ");
                }
            }
            output.println();
            System.out.println();

            for(int i = NUM_ROWS-1; i >= 0; i--)
            {
                if(PRINT_ZEROS)
                {
                    output.write("0 ");
                    System.out.print("0 ");
                }
                for(int j = 0; j < NUM_COLS; j++)
                {
                    if(ROUTER_IN_CENTER_CELL)
                    {
                        output.write(0+"");
                    }
                    else
                    {
                        output.write((int)(simGrid[i][j].getRss())+" ");
                        System.out.print((int)(simGrid[i][j].getRss())+" ");
                    }
                }
                output.println();
                System.out.println();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            output.flush();
            output.close();
        }
        fileExportNumber += 1;
    }//GEN-LAST:event_exportButtonActionPerformed

    private void rrBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rrBoxActionPerformed
        RUSSIAN_ROULETTE = rrBox.isSelected();
        simPanel.repaint();
    }//GEN-LAST:event_rrBoxActionPerformed

    private void errorBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorBoxActionPerformed
        if(!errorBox.isSelected())
        {
            REAL_MAP = false;
            ERROR_MAP = false;
        }
        else
        {
            ERROR_MAP = errorBox.isSelected();
            REAL_MAP = !ERROR_MAP;
            realBox.setSelected(REAL_MAP);
        }
        simPanel.repaint();
    }//GEN-LAST:event_errorBoxActionPerformed

    private void realBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_realBoxActionPerformed
        if(!realBox.isSelected())
        {
            REAL_MAP = false;
            ERROR_MAP = false;
        }
        else
        {
            REAL_MAP = realBox.isSelected();
            ERROR_MAP = !REAL_MAP;
            errorBox.setSelected(ERROR_MAP);
        }
        simPanel.repaint();
    }//GEN-LAST:event_realBoxActionPerformed

    public int[] coarseError()
    {
        int g = 0;
        int b = 0;
        int Rmin = -55;
        int Bmax = -55;
        
        for(int i = 0; i < simGrid.length; i++)
        {
            for(int j = 0; j < simGrid[i].length; j++)
            {
                double rss = simGrid[i][j].getRss();
                if(realGrid[i][j].getRss() == GOOD)
                {
                    int s = (int)(-Rmin+rss);
                    if(rss==0)
                    {
                        s = -20;
                    }
//                    System.out.println("G "+i+","+j+","+s+":"+(int)rss);
                    g += s;
                }
                else if(realGrid[i][j].getRss() == BAD)
                {
                    int s = (int)(Bmax-rss);
                    if(rss==0)
                    {
                        s = 20;
                    }
//                    System.out.println("B "+i+","+j+","+s+":"+(int)rss);
                    b += s;
                }
            }
        }
//        System.out.println(g+":"+b+":"+(g+b));
        
        return new int[]{(int)(w1*g),(int)(w2*b)};
    }
    
    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        coarseError();
//        double min = 1000;
//        double max = -1000;
//        HashMap<Integer,LinkedList<Double>> error = new HashMap<Integer,LinkedList<Double>>();
//        for(int i = 0; i < simGrid.length; i++)
//        {
//            for(int j = 0; j < simGrid[i].length; j++)
//            {
//                double diff = simGrid[i][j].getRss()-realGrid[i][j].getRss();
//                if(diff < min)
//                {
//                    min = diff;
//                }
//                if(diff > max)
//                {
//                    max = diff;
//                }
//                int key = (int)simGrid[i][j].distFromRouter;
//                
//                if(!error.containsKey(key))
//                {
//                    error.put(key,new LinkedList<Double>());
//                    error.get(key).add(simGrid[i][j].distFromRouter);
//                }
//                System.out.println(Math.abs(diff));
//                error.get(key).add(Math.abs(diff));
//            }
//        }
//        for(Integer k : error.keySet())
//        {
//            LinkedList<Double> ll = error.get(k);
//            double key = ll.get(0);
//            ll.remove(0);
//            double s = 0;
//            for(Double d : ll)
//            {
//                s += d;
//            }
//            s /= ll.size();
//            System.out.println(df.format(key)+ " "+df.format(s));
//        }
//        System.out.println(min+","+max);
//        for(int i = 0; i < simGrid.length; i++)
//        {
//            for(int j = 0; j < simGrid[i].length; j++)
//            {
//                System.out.print((int)simGrid[i][j].getRss()+",");
//            }
//        }
//        System.out.println();
//        for(int i = 0; i < realGrid.length; i++)
//        {
//            for(int j = 0; j < realGrid[i].length; j++)
//            {
//                System.out.print((int)realGrid[i][j].getRss()+",");
//            }
//        }
//        for(int i = 0; i < realGrid.length; i++)
//        {
//            for(int j = 0; j < realGrid[i].length; j++)
//            {
//                System.out.println(df.format(simGrid[i][j].distFromRouter)+" "+(int)simGrid[i][j].getRss()+" "+(int)realGrid[i][j].getRss());
//            }
//        }
    }//GEN-LAST:event_printButtonActionPerformed

    private void simModeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simModeButtonActionPerformed
        simMode = true;
        twoDMode = false;
        twoDModeButton.setSelected(false);
        simPanel.repaint();
    }//GEN-LAST:event_simModeButtonActionPerformed

    private void twoDModeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_twoDModeButtonActionPerformed
        simMode = false;
        twoDMode = true;
        simModeButton.setSelected(false);
        simPanel.repaint();
    }//GEN-LAST:event_twoDModeButtonActionPerformed

    private void simPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_simPanelMouseClicked
        moveRouter(evt);
    }//GEN-LAST:event_simPanelMouseClicked
    
    public Cell[][] constructGrid()
    {
        Cell[][] gridInConstruction = new Cell[NUM_ROWS][NUM_COLS];
        
        for(int i = 0; i < NUM_ROWS; i++)
        {
            for(int j = 0; j < NUM_COLS; j++)
            {
                gridInConstruction[i][j] = new Cell(new Point2D.Double(
                        (int)((double)roomLengthInPixels/NUM_COLS*j),
                        (int)((double)roomWidthInPixels/NUM_ROWS*i)),
                        (int)((double)roomLengthInPixels/NUM_COLS),
                        (int)((double)roomWidthInPixels/NUM_ROWS));
            }
        }
        return gridInConstruction;
    }
    
    public void generateRays(Graphics2D g2)
    {
        for(int i = 0; i < rootSegs.size(); i++)
        {
            Ray ray = rootSegs.get(i);
            try
            {
                Line2D segment = ray.rootSegment;
                Obstacle obs = ray.nearestObs;
                if(obs != null)
                {
                    if(obs.getReflectance().equals(Common.SPECULAR))
                    {
                        ray.specularUpdate(obs, segment);
                    }
                    else if(obs.getReflectance().equals(Common.DIFFUSE))
                    {
                        //update for the root
                        ray.diffuseUpdate(obs);
                    }
                    else if(obs.getReflectance().equals(Common.DIFFRACTION))
                    {
                        //update for the root
                        if(random.nextDouble() < diffractionProb)
                        {
                            ray.diffractionUpdate(obs);
                        }
                        else
                        {
                            ray.specularUpdate(obs,segment);
                        }
                    }
                    //propagate the rest of the non-root rays through reflection levels
                    for(Ray diffuseRay : ray.childSegments)
                    {
                        if(diffuseRay.type.equals(Common.REFLECTED))
                        {
                            rayGeneratorHelper(diffuseRay, 1);
                        }
                        else if(diffuseRay.type.equals(Common.TRANSMITTED))
                        {
                            rayGeneratorHelper(diffuseRay, 0);
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            if(g2 != null)
            {
                drawRay(g2, ray);
            }
        }
    }
    
    public void generateInitialRays()
    {
        rootSegs = new LinkedList<Ray>();
        //initial emission generator
        double EMISSION_PROBABILITY = 1;
        
        for(int r = 0; r < routers.length; r++)
        {
            for(int i = 0; i < MAX_NUM_RAYS; i++)
            {
                if(random.nextDouble() < EMISSION_PROBABILITY)
                {
                    rootSegs.add(new Ray(routers[r].getX(), routers[r].getY(), 
                            Math.toRadians(ROTATION+(i*ANGLE_WINDOW/MAX_NUM_RAYS)), 
                            Common.RAY_LEN, Common.ROOT));
                }
            }
        }
    }

    public void rayGeneratorHelper(Ray ray, int reflectionLevel)
    {
        if(reflectionLevel < MAX_REFLECTIONS)
        {
            Line2D segment = ray.rootSegment;
            Obstacle obs = ray.nearestObs;
            if(obs != null)
            {
                if(obs.getReflectance().equals(Common.SPECULAR))
                {
                    ray.specularUpdate(obs, segment);
                }
                else if(obs.getReflectance().equals(Common.DIFFUSE))
                {
                    ray.diffuseUpdate(obs);
                }
                else if(obs.getReflectance().equals(Common.DIFFRACTION))
                {
                    if(random.nextDouble() < diffractionProb)
                    {
                        ray.diffractionUpdate(obs);
                    }
                    else
                    {
                        ray.specularUpdate(obs,segment);
                    }
                }
                for(Ray diffuseRay : ray.childSegments)
                {
//                    if(diffuseRay.type.equals(Common.REFLECTED))
//                    {
                        rayGeneratorHelper(diffuseRay, reflectionLevel+1);
//                    }
//                    else if(diffuseRay.type.equals(Common.TRANSMITTED))
//                    {
//                        rayGeneratorHelper(diffuseRay, reflectionLevel);
//                    }
                }
            }
        }
    }
        
    public void printDiff()
    {
        int sum = 0;
        double squaredError = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        double totalCells = NUM_ROWS*NUM_COLS;
//        System.out.println(realGrid.length);
        for(int i = 0; i < NUM_ROWS; i++)
        {
            for(int j = 0; j < NUM_COLS; j++)
            {
//                System.out.println(i+","+j);
                if(realGrid[i][j] == null)
                {
                    System.out.println("real "+i+" "+j);
                }
                if(simGrid[i][j] == null)
                {
                    System.out.println("sim");
                }
                double diff = Math.abs(realGrid[i][j].getRss()-simGrid[i][j].getRss());
                
                sum += diff;
                
                squaredError += Math.pow(realGrid[i][j].getRss()-simGrid[i][j].getRss(),2);
                
                if(diff > max)
                {
                    max = diff;
                }
                if(diff < min)
                {
                    min = diff;
                }
            }
        }
        double mse = squaredError/totalCells;
        double rmse = Math.sqrt(mse);
        double abs_err = (double)sum/totalCells;
//        String output = "==>("+rootRays.size()+", "+MAX_REFLECTIONS+", "+GAMMA+") \n==>("+
//                sum+ ", " + squaredError + ")\n"
//                + "["+df.format(min)+", "+df.format(av_err)+", "+df.format(max)+"]";
//        String output = df.format(max)+","+df.format(abs_err)+","+df.format(mse)+","+df.format(rmse);
//        String output = df.format(abs_err)+"\n";
//        ERROR = df.format(abs_err);
        int[] err = coarseError();
        ERROR1 = err[0];
        ERROR2 = err[1];
        ERROR = err[0]+err[1];
        if(displayArea != null)
        {
            displayArea.setText(ERROR + ":" +ERROR1 + ":" + ERROR2);
        }
//        System.out.println(df.format(mse)+";");
    }
    
    public void printError()
    {
        for(int i = 0; i < NUM_ROWS; i++)
        {
            for(int j = 0; j < NUM_COLS; j++)
            {
                System.out.print(simGrid[i][j].getDistFromRouter()+",");
            }
        }
        System.out.println();
        for(int i = 0; i < NUM_ROWS; i++)
        {
            for(int j = 0; j < NUM_COLS; j++)
            {
                System.out.print(simGrid[i][j].getRss()+",");
            }
        }
    }
    
    public void mapGenerator(Graphics2D g2)
    {
        simGrid = constructGrid();
        generateInitialRays();
        if(MAX_REFLECTIONS > 0)
        {
            generateRays(g2);
        }
    }
    
    /**
     * Prints signal map to console
     */
    public void export(String file)
    {
        try
        {
            PrintWriter out = new PrintWriter(file);
            for(int i = simGrid.length-1; i >= 0; i--)
            {
                for(int j = 0; j < simGrid[i].length; j++)
                {
                    if((int)simGrid[i][j].getRss() == 0)
                    {
                        out.print(-75+" ");
                    }
                    else
                    {
                        out.print((int)simGrid[i][j].getRss() + " ");
                    }
                }
                out.println();
            }
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    class SimPanel extends JPanel
    {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2 = (Graphics2D) g;
            if(simMode)
            {
                Ellipse2D[] routerShapes = new Ellipse2D[routers.length];
                for(int i = 0; i < routers.length; i++)
                {
                    routerShapes[i] = new Ellipse2D.Double(routers[i].getX()-ROUTER_SIZE/2, 
                                             routers[i].getY()-ROUTER_SIZE/2, 
                                             ROUTER_SIZE, 
                                             ROUTER_SIZE);
                }

                mapGenerator(g2);
                if(MAX_REFLECTIONS == 0)
                {
                    for(Ray ray : rootSegs)
                    {
                        drawRay(g2, ray);
                    }
                }

                drawWalls(g2);

                //paint the router
                g2.setPaint(Color.red);
                for(int i = 0; i < routers.length; i++)
                {
                    g2.fill(routerShapes[i]);
                }

                if(SHOW_GRID)
                {
                    populateGrid();
                    if(ERROR_MAP || REAL_MAP)
                    {
                        displayCustomMap(g);
                    }
                    else
                    {
                        displayGrid(g);
                    }
                    printDiff();
                }
            }
            else if(twoDMode)
            {
                try
                {
                    export("data.txt");
                    ProcessBuilder p = new ProcessBuilder("/usr/local/bin/gnuplot","./2dmap.gp");
                    
                    Process process = p.start();
                    process.waitFor();
                    BufferedImage image = ImageIO.read(new File("data.png"));
                    g.drawImage(image, 0, 0, null);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        public void displayCustomMap(Graphics g)
        {
            for(int i = 0; i < simGrid.length; i++)
            {
                for(int j = 0; j < simGrid[i].length; j++)
                {
                    Cell cell = simGrid[i][j];
                    
                    if(i == simGrid.length/2 && j == simGrid[i].length/2 && ROUTER_IN_CENTER_CELL)
                    {
                        g.setColor(new Color(255,255,255,OPACITY));
                        g.fillRect((int)cell.getPoint().getX(), (int)cell.getPoint().getY(), 
                                cell.getWidth(), cell.getHeight());
                    }
                    else
                    {
                        try
                        {
                            if(ERROR_MAP)
                            {
                                displayErrorCell(g, cell, realGrid[i][j]);
                            }
                            else
                            {
                                displayRealCell(g, cell, realGrid[i][j],i,j);
                            }
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        
        public void displayRealCell(Graphics g, Cell cell, Cell realCell,int i,int j)
        {
            double val=0;
            int c = 0;
            val = realCell.getRss();
            c = (int)((val-MIN_RSS)/Math.abs(MAX_RSS-MIN_RSS)*255);

            try
            {
                if(realCell.getRss()==BAD)
                {
                    g.setColor(new Color(0,0,0,OPACITY));
                }
                else if(realCell.getRss()==NULL)
                {
                    g.setColor(new Color(255,255,255,OPACITY));
                }
                else if(realCell.getRss()==GOOD)
                {
                    g.setColor(new Color(255,0,0,OPACITY));
                }
//                g.setColor(new Color(c,c,c,OPACITY));
//                g.setColor(Color.white);
            }
            catch(Exception e)
            {
                System.out.println("1: "+val+" "+c);
//                System.exit(0);
            }
            g.fillRect((int)cell.getPoint().getX(), (int)cell.getPoint().getY(), 
                    cell.getWidth(), cell.getHeight());
            
            int Rmin = -55;
            int Bmax = -55;
            if(realCell.getRss() == BAD)
            {
                val = Bmax-simGrid[i][j].getRss();
                if(simGrid[i][j].getRss() == 0)
                {
                    val = 20;
                }
            }
            else if(realCell.getRss() == GOOD)
            {
                val = -Rmin+simGrid[i][j].getRss();
                if(simGrid[i][j].getRss() == 0)
                {
                    val = -20;
                }
            }
            
            Font tr = new Font("helvetica", Font.PLAIN, FONT_SIZE);
            g.setColor(new Color(255,255,255,OPACITY));
            g.setFont(tr);
            g.drawString((int)val+":"+(int)simGrid[i][j].getRss(), 
                    (int)cell.getMidpoint().getX()-10, 
                    (int)cell.getMidpoint().getY());
            
            if(SHOW_DISTANCE)
            {
                double dist = pixelsToMeters(Common.euclidean(cell.getMidpoint().getX(), cell.getMidpoint().getY(), 
                        CENTER_X, CENTER_Y));
                g.drawString(df.format(dist),
                        (int)cell.getMidpoint().getX(), 
                        (int)cell.getMidpoint().getY()+FONT_SIZE);
            }
        }
        
        public void displayErrorCell(Graphics g, Cell cell, Cell realCell)
        {
            if(coarse)
            {
                int goodRangeBest = -35;
                int goodRangeWorst = -60;
                int badRangeBest = -75;
                int badRangeWorst = -60;
                
                g.setColor(new Color(0,0,0,OPACITY));
                g.fillRect((int)cell.getPoint().getX(), (int)cell.getPoint().getY(), 
                        cell.getWidth(), cell.getHeight());
                if(realCell.getRss() == BAD)
                {
                    g.setColor(Color.black);
                }
                else if(realCell.getRss() == NULL)
                {
                    g.setColor(Color.white);
                }
                else if(realCell.getRss() == GOOD)
                {
                    g.setColor(Color.red);
                }
                int rss = (int)cell.getRss();
                if(realCell.getRss() == GOOD)
                {
                    if(goodRangeBest >= rss && rss >= goodRangeWorst)
                    {
                        g.drawString(Math.abs(rss-goodRangeWorst)+"", 
                            (int)cell.getMidpoint().getX(), 
                            (int)cell.getMidpoint().getY());
                    }
                    else if(rss < goodRangeWorst)
                    {
//                        g.drawString("-G", 
//                            (int)cell.getMidpoint().getX(), 
//                            (int)cell.getMidpoint().getY());
                    }
                    else
                    {
                        g.drawString(rss+"", 
                            (int)cell.getMidpoint().getX(), 
                            (int)cell.getMidpoint().getY());
                    }
                }
                else if(realCell.getRss() == BAD)
                {
                    if(rss >= badRangeBest && badRangeWorst >= rss)
                    {
                        g.drawString(Math.abs(rss-badRangeWorst)+"", 
                            (int)cell.getMidpoint().getX(), 
                            (int)cell.getMidpoint().getY());
                    }
                    else if(rss > badRangeWorst)
                    {
//                        g.drawString("-B", 
//                            (int)cell.getMidpoint().getX(), 
//                            (int)cell.getMidpoint().getY());
                    }
                    else
                    {
                        g.drawString(rss+"", 
                            (int)cell.getMidpoint().getX(), 
                            (int)cell.getMidpoint().getY());
                    }
                }
            }
            else
            {
                int c = 0;
                double diff = Math.abs(cell.getRss()-realCell.getRss());

                int MIN_ERROR = 0;
                int MAX_ERROR = 30;

                if(diff >= 0)
                {
                    c = (int)((((diff-MIN_ERROR)/(MAX_ERROR-MIN_ERROR)))*255);
                    c = 255-c; //because 255,255,255 is white, not black
        //            System.out.println(cell.getRss()+","+c+","+OPACITY);
                }
                try
                {
                    g.setColor(new Color(c,c,c,OPACITY));
    //                g.setColor(Color.white);
                }
                catch(Exception e)
                {
                    System.out.println("1: "+diff+" "+c);
    //                System.exit(0);
                }
                g.fillRect((int)cell.getPoint().getX(), (int)cell.getPoint().getY(), 
                        cell.getWidth(), cell.getHeight());
                Font tr = new Font("helvetica", Font.PLAIN, FONT_SIZE);
                g.setColor(new Color(0,0,0,OPACITY));
                g.setFont(tr);
                g.drawString((int)diff+"", 
                        (int)cell.getMidpoint().getX(), 
                        (int)cell.getMidpoint().getY());

                if(SHOW_DISTANCE)
                {
                    double dist = pixelsToMeters(Common.euclidean(cell.getMidpoint().getX(), cell.getMidpoint().getY(), 
                            CENTER_X, CENTER_Y));
                    g.drawString(df.format(dist),
                            (int)cell.getMidpoint().getX(), 
                            (int)cell.getMidpoint().getY()+FONT_SIZE);
                }
            }
        }
        
        public double stdev(LinkedList<Double>rssVals, double mean)
        {
            double sum = 0;
            for(Double d : rssVals)
            {
                sum += Math.pow(d-mean,2);
            }
            return Math.sqrt(sum/rssVals.size());
        }
        
        public void displayGrid(Graphics g)
        {
            double minRSS = Double.MAX_VALUE;
            boolean error = false;
            for(int i = 0; i < simGrid.length; i++)
            {
                for(int j = 0; j < simGrid[i].length; j++)
                {
                    Cell cell = simGrid[i][j];
                    
                    if(i == simGrid.length/2 && j == simGrid[i].length/2 && ROUTER_IN_CENTER_CELL)
                    {
                        g.setColor(new Color(255,255,255,OPACITY));
                        g.fillRect((int)cell.getPoint().getX(), (int)cell.getPoint().getY(), 
                                cell.getWidth(), cell.getHeight());
                    }
                    else
                    {
                        try
                        {
                            displayCell(g, cell);
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                            double rss = cell.getRss();
                            if(rss < minRSS && rss != 0)
                            {
                                minRSS = rss;
                                error = true;
                            }
                        }
                    }
                }
            }
            if(error)
            {
                displayArea.setText("Please decrease to at least "+Math.floor(minRSS));
            }
        }
        
        public void displayCell(Graphics g, Cell cell)
        {
            double rss = cell.getRss();
            int c = 0;
            
            if(rss != 0)
            {
                c = (int)((rss-MIN_RSS)/Math.abs(MAX_RSS-MIN_RSS)*255);
            }
            
            if(c > 255 || c < 0)
            {
                g.setColor(new Color(0,0,0,OPACITY));
                System.out.println("Color out of range " + rss+","+c);
            }
            else
            {
                g.setColor(new Color(c,c,c,OPACITY));
            }
            
            g.fillRect((int)cell.getPoint().getX(), (int)cell.getPoint().getY(), 
                    cell.getWidth(), cell.getHeight());
            Font tr = new Font("helvetica", Font.PLAIN, FONT_SIZE);
            if(model.equals(FSPL) || model.equals(PARTITION))
            {
                g.setColor(new Color(0,0,0,OPACITY));
            }
            else
            {
                g.setColor(new Color(255,255,255,OPACITY));
            }
            g.setFont(tr);
            if(SHOW_RSS)
            {
                g.drawString((int)rss+"", 
                        (int)cell.getMidpoint().getX(), 
                        (int)cell.getMidpoint().getY());
            }
            if(SHOW_DISTANCE)
            {
                double dist = pixelsToMeters(Common.euclidean(cell.getMidpoint().getX(), cell.getMidpoint().getY(), 
                        CENTER_X, CENTER_Y));
                g.drawString(df.format(dist),
                        (int)cell.getMidpoint().getX(), 
                        (int)cell.getMidpoint().getY()+FONT_SIZE);
            }
        }
    }
    
    public void drawWalls(Graphics2D g2)
    {
        g2.setColor(Color.black);
        //just draw walls once
        for(Obstacle obs : Common.objs)
        {
            Line2D wall = obs.getGeom();
            if(wall != null)
            {
                g2.draw(wall);
            }
        }
    }
    
    public void drawRay(Graphics2D g2, Ray rootRay)
    {
        g2.setColor(Color.orange);
        g2.draw(rootRay.rootSegment);
        for(Ray seg : rootRay.childSegments)
        {
            drawRay(g2, seg);
        }
    }
    
    public void populateGrid()
    {
        for(int i = 0; i < simGrid.length; i++)
        {
            for(int j = 0; j < simGrid[i].length; j++)
            {
                Cell cell = simGrid[i][j];
                Ellipse2D receiver = new Ellipse2D.Double(
                        cell.getMidpoint().getX()-MEASUREMENT_SIZE/2, 
                        cell.getMidpoint().getY()-MEASUREMENT_SIZE/2,
                        MEASUREMENT_SIZE, MEASUREMENT_SIZE);
                cell.setDistFromRouter(pixelsToMeters(Common.euclidean(receiver.getCenterX(), receiver.getCenterY(), CENTER_X, CENTER_Y)));
                double d = populateCell(cell, receiver, 0, new LinkedList<Double>(), new LinkedList<Double>(), 0);
                cell.setRss(d);
            }
        }
    }
    
    public double populateCell(Cell cell, Ellipse2D receiver, 
            double dist, LinkedList<Double> rssList, LinkedList<Double> distList, double lostRss)
    {
        //RUSSIAN ROULETTE
        //ray has a GAMMA chance of being reflected at each object
        //if all segments of ray are reflected
        //then it counts toward the RSS, with NO LOSS OF ENERGY
        //if any segment is reflected
        //the entire ray counts for nothing
        for(Ray root : rootSegs)
        {
            double reflect = random.nextDouble();
            
            if(!RUSSIAN_ROULETTE || RUSSIAN_ROULETTE && reflect < RR_PROB)
            {
                populateCellHelper(root, receiver, 0, rssList, distList, 0, 0, 0, new LinkedList<Double>());
            }
        }
        if(rssList.size() > 0)
        {
            return resolveMultipath(cell, rssList, distList);
        }
        return 0;
    }
    
    public void populateCellHelper(Ray ray, Ellipse2D receiver, 
            double dist, LinkedList<Double> rssList, LinkedList<Double> distList, 
            int numReflections, int numTransmissions, int numDiffractions, LinkedList<Double> diffuseAngles)
    {
        diffuseAngles.add(ray.angle);
        Line2D seg = ray.rootSegment;
        if(seg.intersects(receiver.getBounds()))
        {
            double d = Common.euclidean(seg.getX1(), seg.getY1(), receiver.getCenterX(), receiver.getCenterY());
            d = dist+pixelsToMeters(d);
            double rss = calcRSS(d, frequency, numReflections, numTransmissions, numDiffractions);
            
            rssList.add(rss);
            distList.add(d);
        }
        else
        {
            double d = pixelsToMeters(dist+ray.segLength());
            
            for(Ray child : ray.childSegments)
            {
                double reflect = random.nextDouble();
                
                if(RUSSIAN_ROULETTE && reflect < RR_PROB || !RUSSIAN_ROULETTE)
                {
                    if(child.type.equals(Common.REFLECTED))
                    {
                        populateCellHelper(child, receiver, pixelsToMeters(dist+child.segLength()), 
                                rssList, distList, numReflections+1, numTransmissions,numDiffractions,diffuseAngles);
                    }
                    else if(child.type.equals(Common.TRANSMITTED))
                    {
                        populateCellHelper(child, receiver, pixelsToMeters(dist+child.segLength()), 
                                rssList, distList, numReflections, numTransmissions+1,numDiffractions,diffuseAngles);
                    }
                    else if(child.type.equals(Common.DIFFRACTED))
                    {
                        populateCellHelper(child, receiver, pixelsToMeters(dist+child.segLength()), 
                                rssList, distList, numReflections, numTransmissions, numDiffractions+1, diffuseAngles);
                    }
                    
                }
            }
        }
    }
    
    public double sum(LinkedList<Double> nums)
    {
        double s = 0;
        for(Double d : nums)
        {
            s += d;
        }
        return s;
    }
    
    public double resolveMultipath(Cell cell, 
            LinkedList<Double> rss,
            LinkedList<Double> dists)
    {
        LinkedList<Double> rssInWatts = dbmToWatts(rss, true);
        double wattsSum = 0;
        for(Double val : rssInWatts)
        {
            wattsSum += val;
        }
        double temp = wattsToDbm(wattsSum);
        if(temp >= MIN_RSS && temp <= MAX_RSS)
        {
            return temp;
        }
        else if(temp < MIN_RSS)
        {
            return MIN_RSS;
        }
        else
        {
            return MAX_RSS;
        }
    }
//    
    public LinkedList<Double> calcRSS(LinkedList<Double> distances, double frequency, 
            int numReflections, int numTransmissions, int numDiffractions)
    {
        LinkedList<Double> rss = new LinkedList<Double>();
        for(Double d : distances)
        {
            rss.add(calcRSS(d, frequency, numReflections, numTransmissions, numDiffractions));
        }
        return rss;
    }
        
    public double calcRSS(double distance, double frequency, int numReflections, int numTransmissions, int numDiffractions)
    {
        if(model.equals(FSPL))
        {
            return -fspl(distance, frequency);
        }
        else if(model.equals(DD))
        {
            double n = 3;
            return -(fspl(1,frequency)+10*n*Math.log10(distance));
        }
        else if(model.equals(FADING))
        {
            double n = 3;
            double log = Math.exp(.5+random.nextGaussian());
            return -(fspl(1,frequency)+10*n*Math.log10(distance)+log);
        }
        else if(model.equals(ITU))
        {
            return -itu(distance, frequency);
        }
        else if(model.equals(PARTITION))
        {
            double d0 = 1;
            double val = -(P0+20*Math.log10(distance/d0));
            return val;
        }
        return 0;
    }
    
    private double itu(double distance, double frequency)
    {
        double N=29; //distance power loss coefficient
        double Pf=0; //floor loss penetration factor
	return 20*
               Math.log10(frequency*Math.pow(10,3))+
               N*Math.log10(distance)+
               Pf-28;
    }
    
    public double fspl(double distance, double frequency)
    {
        double ret = 20*Math.log10(4*Math.PI*distance*frequency*Math.pow(10,9)/c);
        return ret;
    }
    
    public LinkedList<Double> dbmToWatts(LinkedList<Double> dbms, boolean boundToMaxRSS)
    {
        LinkedList<Double> watts = new LinkedList<Double>();
        for(int i = 0; i < dbms.size(); i++)
        {
            double d = dbms.get(i);
            if(!boundToMaxRSS || boundToMaxRSS && d >= MIN_RSS)
            {
                watts.add(dbmToWatts(d));
            }
        }
        return watts;
    }
    
    public Cell[][] getGrid()
    {
        return realGrid;
    }
    
    public double dbmToWatts(double dbm)
    {
        return Math.pow(10,dbm/10)/1000;
    }
    
    public double wattsToDbm(double watts)
    {
        return 10*Math.log10(1000*watts);
    }
    
    private double pixelsToMeters(double pixels)
    {
        return pixels*METERS_PER_PIXEL;
    }
    
    private double metersToPixels(double meters)
    {
        return meters*1/METERS_PER_PIXEL;
    }
    
    private double feetToMeters(double feet)
    {
        return feet*0.3048;
    }
    
    private double metersToFeet(double meters)
    {
        return meters*3.28084;
    }
    
    //PREFERENCES
    private final int ROUTER_SIZE = 10;
    private double METERS_PER_PIXEL;
    private final int c = 299792428;
    private double frequency = 2.4;
    private DecimalFormat df = new DecimalFormat("0.##");
    private LinkedList<Ray> rootSegs;
    
    private int NUM_ROWS = 18;
    private int NUM_COLS = 18;
    private Cell[][] simGrid;
    public int MIN_RSS = -75;
    public int MAX_RSS = -35;
    private boolean SHOW_GRID = true;
    private int OPACITY = 200;
    private int MEASUREMENT_SIZE = 30;
    private static final String FSPL = "FSPL";
    private static final String ITU = "ITU";
    private static final String PARTITION = "PARTITION";
    private static final String DD = "DD";
    private static final String FADING = "FADING";
    private boolean SHOW_RSS = true;
    private int FONT_SIZE = 12;
    private boolean SHOW_DISTANCE = false;
    private double roomLengthInPixels;
    private double roomWidthInPixels;
    public double ROOM_LENGTH_IN_METERS;
    public double ROOM_WIDTH_IN_METERS;
    public double CELL_LENGTH_IN_FEET = 4.7;
    public double CELL_WIDTH_IN_FEET = 2.7;
    public boolean ROUTER_IN_CENTER_CELL = false;
    public Cell[][] realGrid=new Cell[NUM_ROWS][NUM_COLS];
    private Random random;
    private int ANGLE_WINDOW = 360; //default at 360, was at 120
    
    private int ROTATION = 154;
    private int MAX_REFLECTIONS = 2;
    
    //router in center
    public Point2D[] routers;
    
    int MAX_NUM_RAYS = 500;
    private String model = PARTITION;
    private int CENTER_X = 445;
    private int CENTER_Y = 250;
    private boolean RUSSIAN_ROULETTE = false;
    private boolean PRINT_ZEROS = true;
    private boolean ERROR_MAP = false;
    private boolean REAL_MAP = false;
    public double ERROR;
    private boolean simMode = true;
    private boolean twoDMode = false;
    double refCoeff=0;
    double transCoeff=0;
    double diffractionCoeff=0;
    double P0 = 48;
    double RR_PROB = .8;
    double diffractionProb = 0.5;
    private String GROUND_TRUTH = "src/raytracer2/coarse";
    boolean coarse;
    
    
    /**
     * Here we define where the dipoles of the router should be located
     * on the geometry.
     */
    public void populateRouter()
    {
        this.routers = new Point2D[2];
        
        this.routers[0] = new Point2D.Double(CENTER_X-10,CENTER_Y);
        this.routers[1] = new Point2D.Double(CENTER_X+10,CENTER_Y);
        CENTER_X = 420;
        CENTER_Y = 250;
    }
    
    double w1=.5;
    double w2=.5;
    double ERROR1;
    double ERROR2;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel angleWindowLabel;
    private javax.swing.JSlider angleWindowSlider;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JScrollPane display;
    private javax.swing.JTextArea displayArea;
    private javax.swing.JCheckBox distanceBox;
    private javax.swing.JCheckBox errorBox;
    private javax.swing.JMenuItem exportButton;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JTextField frequencyField;
    private javax.swing.JLabel frequencyLabel;
    private javax.swing.JCheckBox gridBox;
    private javax.swing.JTextField maxReflectionsField;
    private javax.swing.JLabel maxReflectionsLabel;
    private javax.swing.JTextField numRaysField;
    private javax.swing.JLabel numRaysLabel;
    private javax.swing.JLabel opacityLabel;
    private javax.swing.JSlider opacitySlider;
    private javax.swing.JButton printButton;
    private javax.swing.JCheckBox realBox;
    private javax.swing.JCheckBox rrBox;
    private javax.swing.JCheckBox rssBox;
    private javax.swing.JRadioButton simModeButton;
    private javax.swing.JPanel simPanel;
    private javax.swing.JMenuBar topLevelMenu;
    private javax.swing.JRadioButton twoDModeButton;
    // End of variables declaration//GEN-END:variables
}
