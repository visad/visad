//
// ShowNCEPModel.java
//

/*

The software in this file is Copyright(C) 1999 by Tom Whittaker.
It is designed to be used with the VisAD system for interactive 
analysis and visualization of numerical data.  
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.jmet;

import edu.wisc.ssec.mcidas.*;

import visad.java3d.*;

import visad.*;
import visad.util.*;  
import visad.Set;
import visad.Real;
import visad.VisADException;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*; 
import javax.swing.filechooser.*; 
import javax.swing.filechooser.FileFilter; 


import java.io.*;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import visad.data.netcdf.*;
import visad.data.mcidas.*;
import visad.jmet.*;


public class ShowNCEPModel 
       extends JFrame implements ActionListener, ChangeListener,
       DisplayListener {

  private BaseMapAdapter baseMap;
  private ColorControl ccmap;
  private float[][] colorTable;
  private Container cf;
  private JSlider speedSlider;
  private JLabel speedSliderLabel;
  private int speedValue, frameValue;
  private int maxFrames;
  private JButton start_stop, snapButton, forward, backward, mapColor;
  private boolean isLooping;
  private ContourControl ci;
  private ProjectionControl pc;
  private AnimationControl ca;
  private double[] pcMatrix;
  private GraphicsModeControl gmc;
  private DisplayImpl di;
  private NetcdfGrids ng;
  private JPanel vdisplay;
  private RealType x,y,level,time_type,pres;
  private RealType Values, SfcValues;
  private boolean firstFile;
  private boolean gotSfcGrids, gotAloftGrids;
  private JLabel statLabel;
  private String cmd;
  private NCEPPanel[] ncp;
  private JTabbedPane tabby;
  private FieldImpl mapField;
  private ValueControl mapControl;
  private DataReference mapRef;
  private RealType enableMap;
  private ScalarMap mapMap, xAxis, yAxis;
  private JCheckBox showMap;
  static String argument;
  private String directory;
  private String MapFile;

  public static void main(String args[]) {

    argument = "-1";
    if (args != null && args.length > 0) {
      argument = args[0].trim();
    }

    ShowNCEPModel ss = new ShowNCEPModel();
  }

  public ShowNCEPModel ( ) {

    super("Show NCEP Model Data");
    try {

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0); }
    } );
        

    frameValue = 0;
    maxFrames = -1;
    firstFile = true;
    gotSfcGrids = false;
    gotAloftGrids = false;
    directory = ".";
    ng = null;

    //MapFile = "../data/mcidas/OUTLAUST";
    MapFile = "../data/mcidas/OUTLUSAM";

  // define the VisAD mappings for the Data and Display

    di = new DisplayImplJ3D("display1");
    di.addDisplayListener(this);
    pc = di.getProjectionControl();

    // make the cube the size of the window...for 'snapping'
    pcMatrix = pc.getMatrix(); 
    pcMatrix[0] = .95;
    pcMatrix[5] = .95;
    pcMatrix[10] = .95;

    x = new RealType("x");
    y = new RealType("y");
    level = new RealType("level");
    time_type = RealType.Time;
    //time_type = new RealType("Valid_time", CommonUnit.secondsSinceTheEpoch, null);
    pres = new RealType("pres");

    gmc = di.getGraphicsModeControl();
    gmc.setProjectionPolicy(0);

    xAxis = new ScalarMap(x, Display.XAxis);
    yAxis = new ScalarMap(y, Display.YAxis);
    di.addMap(xAxis);
    di.addMap(yAxis);
    ScalarMap lvl = new ScalarMap(pres, Display.ZAxis);
    lvl.setRange(1020., 10.);
    di.addMap(lvl);

    ScalarMap ani = new ScalarMap(time_type, Display.Animation);
    di.addMap(ani);
    ca = (AnimationControl) ani.getControl();
    ca.setOn(false);

    statLabel = new JLabel("Please choose a data file...");
    statLabel.setForeground(Color.black);

    // make the ncep Panel(s) here because they do 'addMaps'

    tabby = new JTabbedPane();

    int num=1;
    try {
      num =  - (Integer.parseInt(argument));
      if (num < 1 || num > 9) {
        System.out.println("invalid number of tabs (1-9) = "+num);
        System.exit(1);
      }
    } catch (Exception nex) {System.exit(1);}

    ncp = new NCEPPanel[num+1];

    ncp[0] = new NCEPPanel(1, di, statLabel, tabby,  "Single-level Data");
    for (int i=1; i<ncp.length; i++) {
     ncp[i] = new NCEPPanel(0, di, statLabel, tabby,  "Data Aloft");
    }

    enableMap = new RealType("enableMap");
    mapMap = new ScalarMap(enableMap, Display.SelectValue);
    di.addMap(mapMap);
    ScalarMap scalarMapColor = new ScalarMap(enableMap, Display.RGB);
    di.addMap(scalarMapColor);

    ccmap = (ColorControl) (scalarMapColor.getControl() );
    colorTable = new float[3][256];
    for (int i=0; i<256; i++) {
      colorTable[0][i] = .6f;
      colorTable[1][i] = 0.f;
      colorTable[2][i] = .6f;
    }
    ccmap.setTable(colorTable);

    buildUI();

    } catch (Exception e) {e.printStackTrace(System.out); System.exit(1);}

  }

  private void buildMenuBar()
  {
    JMenuBar mb = new JMenuBar();
    JMenu fileMenu = new JMenu("File");

    JMenuItem menuFile = new JMenuItem("Open File");
    menuFile.addActionListener(this);
    menuFile.setActionCommand("menuFile");
    fileMenu.add(menuFile);

    JMenuItem menuQuit = new JMenuItem("Exit");
    menuQuit.addActionListener(this);
    menuQuit.setActionCommand("menuQuit");
    fileMenu.add(menuQuit);

    ButtonGroup menuSelectors = new ButtonGroup();

    JMenu mapMenu = new JMenu("Map");
    JRadioButtonMenuItem menuNA = new JRadioButtonMenuItem("North America",true);
    menuNA.addActionListener(this);
    menuNA.setActionCommand("menuNA");
    mapMenu.add(menuNA);
    menuSelectors.add(menuNA);

    JRadioButtonMenuItem menuWorld = new JRadioButtonMenuItem("World",false);
    menuWorld.addActionListener(this);
    menuWorld.setActionCommand("menuWorld");
    mapMenu.add(menuWorld);
    menuSelectors.add(menuWorld);

    mb.add(fileMenu);
    mb.add(mapMenu);
    setJMenuBar(mb);
  }

  private Component buildMapControls()
  {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS) );
    panel.setAlignmentX(Component.CENTER_ALIGNMENT);

    panel.add(mapColor);
    panel.add(Box.createRigidArea(new Dimension(10,10) ) );
    panel.add(showMap);

    return panel;
  }

  private Component buildSpeedControl()
  {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS) );
    panel.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.setAlignmentY(Component.CENTER_ALIGNMENT);

    speedSlider = new JSlider(JSlider.HORIZONTAL,1,20,10);
    speedSlider.setMaximumSize(speedSlider.getPreferredSize() );
    speedSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
    speedSlider.addChangeListener(this);

    speedValue = 3;

    panel.add(new JLabel("Speed"));
    panel.add(speedSlider);

    return panel;
  }

  private Component buildAnimationControls()
  {
    backward = new JButton(" < ");
    backward.setAlignmentX(Component.CENTER_ALIGNMENT);
    backward.setMaximumSize(backward.getMaximumSize() );
    backward.setMinimumSize(backward.getMaximumSize() );
    backward.setPreferredSize(backward.getMaximumSize() );
    backward.addActionListener(this);
    backward.setActionCommand("backward");

    start_stop = new JButton("Animate");
    start_stop.setAlignmentX(Component.CENTER_ALIGNMENT);
    start_stop.setMaximumSize(start_stop.getMaximumSize() );
    start_stop.setMinimumSize(start_stop.getMaximumSize() );
    start_stop.setPreferredSize(start_stop.getMaximumSize() );
    start_stop.addActionListener(this);
    start_stop.setActionCommand("start_stop");

    isLooping = false;

    forward = new JButton(" > ");
    forward.setAlignmentX(Component.CENTER_ALIGNMENT);
    forward.setMaximumSize(forward.getMaximumSize() );
    forward.setMinimumSize(forward.getMaximumSize() );
    forward.setPreferredSize(forward.getMaximumSize() );
    forward.addActionListener(this);
    forward.setActionCommand("forward");

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS) );
    panel.setAlignmentX(Component.CENTER_ALIGNMENT);

    panel.add(backward);
    panel.add(start_stop);
    panel.add(forward);
    panel.add(buildSpeedControl());

    return panel;
  }

  private Component buildControlPanel()
  {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS) );
    panel.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.setAlignmentY(Component.TOP_ALIGNMENT);
    panel.setMaximumSize(new Dimension(300,Short.MAX_VALUE) );
    panel.setMinimumSize(new Dimension(300,400) );
    panel.setPreferredSize(new Dimension(300,400) );

    mapColor = new JButton("Map Color");
    mapColor.addActionListener(this);
    mapColor.setActionCommand("mapColor");
    mapColor.setBackground(new Color(154,0,154));

    showMap = new JCheckBox("Make map visible");
    showMap.setAlignmentX(Component.CENTER_ALIGNMENT);
    showMap.addActionListener(this);
    showMap.setActionCommand("showmap");
    showMap.setSelected(true);

    snapButton = new JButton("Snap!");
    snapButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    snapButton.setMaximumSize(snapButton.getMaximumSize() );
    snapButton.setMinimumSize(snapButton.getMaximumSize() );
    snapButton.setPreferredSize(snapButton.getMaximumSize() );
    snapButton.addActionListener(this);
    snapButton.setActionCommand("snapButton");

    panel.add(Box.createRigidArea(new Dimension(10,10) ) );
    panel.add(snapButton);
    panel.add(Box.createRigidArea(new Dimension(10,10) ) );

    panel.add(buildMapControls());
    panel.add(buildAnimationControls());

    for (int i=0; i<ncp.length; i++) {
      tabby.addTab("Data", ncp[i]);
    }

    panel.add(Box.createRigidArea(new Dimension(10,30) ) );
    panel.add(tabby);

    panel.add(Box.createVerticalGlue() );
    panel.add(Box.createRigidArea(new Dimension(10,10) ) );
    statLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(statLabel);

    return panel;
  }

  private void buildUI()
  {
    buildMenuBar();

    vdisplay = (JPanel) di.getComponent();
    vdisplay.setPreferredSize(new Dimension(700,700) );
    vdisplay.setAlignmentX(Component.LEFT_ALIGNMENT);
    vdisplay.setAlignmentY(Component.TOP_ALIGNMENT);

    cf = getContentPane();
    cf.setLayout(new BoxLayout(cf, BoxLayout.X_AXIS) );

    cf.add(buildControlPanel());
    cf.add(vdisplay);
    setSize(1024,768);
    setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    cmd = e.getActionCommand();
    //System.out.println("Action command: "+cmd);

    if (cmd.equals("menuFile") ) {
      try {
	ca.setOn(false);
	start_stop.setText("Animate");
	isLooping = false;
      } catch (Exception mfs) {mfs.printStackTrace(); System.exit(1); }
      getNewFile();

    } else if (cmd.equals("menuQuit") ) {
      System.exit(0);

    } else if (cmd.equals("menuNA") ) {
      MapFile = "../data/mcidas/OUTLUSAM";
      doBaseMap();

    } else if (cmd.equals("menuWorld") ) {
      MapFile = "../data/mcidas/OUTLSUPW";
      doBaseMap();

    } else if (cmd.startsWith("Param:") ) {
    
       System.out.println("not implemented");

    } else if (cmd.equals("start_stop") ) {

      try {
       if (isLooping) {
          ca.setOn(false);
          start_stop.setText("Animate");
          isLooping = false;
        } else {
          ca.setOn(true);
          start_stop.setText("Stop");
          isLooping = true;
        }
      } catch (Exception sse) {sse.printStackTrace(); System.exit(1); }
       
    } else if (cmd.equals("backward")) {
      try {
        ca.setDirection(false);
        ca.takeStep();
      } catch (Exception eb) {eb.printStackTrace(); }

    } else if (cmd.equals("forward")) {
      try {
        ca.setDirection(true);
        ca.takeStep();
      } catch (Exception eb) {eb.printStackTrace(); }

    } else if (cmd.equals("snapButton") ) {

      try {
        pc.setMatrix(pcMatrix);
      } catch (Exception sse) {sse.printStackTrace(); System.exit(1); }

    } else if (cmd.equals("mapColor")) {

      JColorChooser cc = new JColorChooser();
      Color nc = cc.showDialog(this, "Choose contour color", Color.white);
      if (nc != null) {
        mapColor.setBackground(nc);
        try {
          for (int i=0; i<256; i++) {
            colorTable[0][i] = (float) nc.getRed()/255.f;
            colorTable[1][i] = (float) nc.getGreen()/255.f;
            colorTable[2][i] = (float) nc.getBlue()/255.f;
          }
          ccmap.setTable(colorTable);

       }  catch (Exception mce) {mce.printStackTrace(); }
     }

    } else if (cmd.equals("showmap") ) {
      try {
        if (showMap.isSelected() ) {
          mapControl.setValue(0.0);
        } else {
          mapControl.setValue(1.0);
        }
      } catch (Exception mcon) {mcon.printStackTrace(); }

    }

  }

  public void displayChanged(DisplayEvent e) {
    
    if (e.getId() == DisplayEvent.TRANSFORM_DONE) {
      statLabel.setText("Display Frame done...");
    }
  }


  public void stateChanged(ChangeEvent e) {
    Object source = e.getSource();

    if (source.equals(speedSlider) ) {

      int val = (speedSlider.getValue()) ;
      if (val != speedValue ) {
        speedValue = val;
               try {
          ca.setStep(50*(21 - speedValue) );
        } catch (Exception slis) {slis.printStackTrace(); System.exit(1);} 
      }
    }

  }

  void getNewFile() {

    isLooping = false;

    FileDialog fileBox = new FileDialog(this);
    fileBox.setDirectory(directory);
    fileBox.setMode(FileDialog.LOAD);
    fileBox.setVisible(true);

    try {

      String filename = fileBox.getFile();
      if (filename == null) return;
      directory = fileBox.getDirectory();
      if (directory == null) return;
      File file = new File(directory, filename);

      // remove the DataReference for new files...

      ng = new NetcdfGrids(file);

      setTitle("Show NCEP Model Data from "+filename);

      ng.setRealTypes(x,y,level,time_type,pres);

      Dimension dim = ng.getDimension();
      xAxis.setRange(0, dim.width-1);
      yAxis.setRange(0, dim.height-1);

      // set up aspect ratio for square-ness
      double aspect = ng.getAspect();
      aspect = aspect * ( (double)dim.width/(double)dim.height) ;
      if (aspect >= 1.0) {
        pcMatrix[0] = .95;
        pcMatrix[5] = .95/aspect;
      } else {
        pcMatrix[0] = .95 * aspect;
        pcMatrix[5] = .95;
      }

      pc.setMatrix(pcMatrix);

      for (int i=0; i<ncp.length; i++) {
        ncp[i].setNetcdfGrid(ng);
      }

      maxFrames = ng.getNumberOfTimes() - 1;

      Vector vSfc = ng.get3dVariables();
      ncp[0].setParams(vSfc);

      Vector v = ng.get4dVariables();
      for (int i=1; i<ncp.length; i++) {
       ncp[i].setParams(v);
      }

      // can do map now, since grid geometry is known...

      statLabel.setText("Rendering base map...");
      doBaseMap();

    } catch (Exception op) {op.printStackTrace(); System.exit(1); }

  }
  private void doBaseMap() {
    if (ng == null) return;
    try {
      baseMap = new BaseMapAdapter(MapFile);
      baseMap.setDomainSet((Linear2DSet)ng.getDomainSet() );
      Data mapData = baseMap.getData();

      // set up so map can be toggled on/off

      FunctionType mapType =
        new FunctionType(enableMap, mapData.getType() );
      Integer1DSet mapSet= new Integer1DSet(enableMap, 2);
      mapField = new FieldImpl(mapType, mapSet);
      mapField.setSample(0,mapData);
      mapControl = (ValueControl) mapMap.getControl();
      if (mapRef != null) di.removeReference(mapRef);
      mapRef = new DataReferenceImpl("mapData");
      mapRef.setData(mapField);
      ConstantMap[] rendMap;
      rendMap = new ConstantMap[1];
      rendMap[0] = new ConstantMap(-.99, Display.ZAxis);
      di.addReference(mapRef, rendMap);
      //di.addReference(mapRef);
      mapControl.setValue(0.0);

    } catch (Exception mapop) {mapop.printStackTrace(); System.exit(1); }

  }


}
