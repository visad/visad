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
  private Container cf;
  private JSlider speedSlider;
  private JLabel speedSliderLabel;
  private int speedValue, frameValue;
  private int maxFrames;
  private JButton start_stop, snapButton, forward, backward;
  private boolean isLooping;
  private ContourControl ci;
  private ProjectionControl pc;
  private AnimationControl ca;
  private double[] pcMatrix;
  private GraphicsModeControl gmc;
  private DisplayImpl di;
  private NetcdfGrids ng;
  private JPanel vdisplay;
  private RealType x,y,level,record,pres;
  private RealType Values, SfcValues;
  private boolean firstFile;
  private boolean gotSfcGrids, gotAloftGrids;
  private JLabel statLabel;
  private String cmd;
  private NCEPPanel ncp, ncpSfc, ncp2;
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

    argument = " ";
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

    JMenu mapMenu = new JMenu("Map");
    JMenuItem menuNA = new JMenuItem("North America");
    menuNA.addActionListener(this);
    menuNA.setActionCommand("menuNA");
    mapMenu.add(menuNA);

    JMenuItem menuWorld = new JMenuItem("World");
    menuWorld.addActionListener(this);
    menuWorld.setActionCommand("menuWorld");
    mapMenu.add(menuWorld);

    MapFile = "OUTLUSAM";

    mb.add(fileMenu);
    mb.add(mapMenu);
    setJMenuBar(mb);


  // define the VisAD mappings for the Data and Display

    di = new DisplayImplJ3D("display1");
    di.addDisplayListener(this);
    pc = di.getProjectionControl();

    // make the cube the size of the window...for 'snapping'
    pcMatrix = pc.getMatrix(); 
    pcMatrix[0] = .9;
    pcMatrix[5] = .9;
    pcMatrix[10] = .9;

    x = new RealType("x");
    y = new RealType("y");
    level = new RealType("level");
    record = new RealType("record");
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

    ScalarMap ani = new ScalarMap(record, Display.Animation);
    di.addMap(ani);
    ca = (AnimationControl) ani.getControl();

    statLabel = new JLabel("Please choose a data file...");
    statLabel.setForeground(Color.black);

    // make the ncep Panel(s) here because they do 'addMaps'
    ncp = new NCEPPanel(0, di, statLabel ,"Data Aloft");
    ncp2 = new NCEPPanel(0, di, statLabel ,"More Data Aloft");
    ncpSfc = new NCEPPanel(1, di, statLabel, "Single-level Data ");

    enableMap = new RealType("enableMap");
    mapMap = new ScalarMap(enableMap, Display.SelectValue);
    di.addMap(mapMap);

    vdisplay = (JPanel) di.getComponent();
    vdisplay.setPreferredSize(new Dimension(700,700) );
    vdisplay.setAlignmentX(Component.LEFT_ALIGNMENT);
    vdisplay.setAlignmentY(Component.TOP_ALIGNMENT);


  /****************** UI widgets ************************/

      
    cf = getContentPane();
    cf.setLayout(new BoxLayout(cf, BoxLayout.X_AXIS) );

    JPanel p1 = new JPanel();
    p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS) );
    p1.setAlignmentX(Component.CENTER_ALIGNMENT);
    p1.setAlignmentY(Component.TOP_ALIGNMENT);
    p1.setMaximumSize(new Dimension(300,Short.MAX_VALUE) );
    p1.setMinimumSize(new Dimension(300,400) );
    p1.setPreferredSize(new Dimension(300,400) );

    start_stop = new JButton("Animate");
    start_stop.setAlignmentX(Component.CENTER_ALIGNMENT);
    start_stop.setMaximumSize(start_stop.getMaximumSize() );
    start_stop.setMinimumSize(start_stop.getMaximumSize() );
    start_stop.setPreferredSize(start_stop.getMaximumSize() );
    start_stop.addActionListener(this);
    start_stop.setActionCommand("start_stop");
    isLooping = false;
    ca.setOn(false);

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
    isLooping = false;
    ca.setOn(false);

    backward = new JButton(" < ");
    backward.setAlignmentX(Component.CENTER_ALIGNMENT);
    backward.setMaximumSize(backward.getMaximumSize() );
    backward.setMinimumSize(backward.getMaximumSize() );
    backward.setPreferredSize(backward.getMaximumSize() );
    backward.addActionListener(this);
    backward.setActionCommand("backward");

    forward = new JButton(" > ");
    forward.setAlignmentX(Component.CENTER_ALIGNMENT);
    forward.setMaximumSize(forward.getMaximumSize() );
    forward.setMinimumSize(forward.getMaximumSize() );
    forward.setPreferredSize(forward.getMaximumSize() );
    forward.addActionListener(this);
    forward.setActionCommand("forward");

    JPanel p3 = new JPanel();
    p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS) );
    p3.setAlignmentX(Component.CENTER_ALIGNMENT);
    p3.setAlignmentY(Component.CENTER_ALIGNMENT);
    speedSlider = new JSlider(JSlider.HORIZONTAL,1,20,10);
    speedSlider.setMaximumSize(speedSlider.getPreferredSize() );
    speedSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
    speedSlider.addChangeListener(this);
    speedValue = 3;
    p3.add(new JLabel("Speed"));
    p3.add(speedSlider);

    JPanel p2 = new JPanel();
    p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS) );
    p2.setAlignmentX(Component.CENTER_ALIGNMENT);
    p2.add(backward);
    p2.add(start_stop);
    p2.add(forward);
    p2.add(p3);
    
    p1.add(Box.createRigidArea(new Dimension(10,10) ) );
    p1.add(snapButton);
    p1.add(Box.createRigidArea(new Dimension(10,10) ) );
    p1.add(showMap);
    p1.add(p2);

    p1.add(Box.createRigidArea(new Dimension(10,10) ) );
    p1.add(ncp);
    p1.add(Box.createRigidArea(new Dimension(10,10) ) );
    p1.add(ncpSfc);

    if (argument.equals("-2")) {
      p1.add(Box.createRigidArea(new Dimension(10,10) ) );
      p1.add(ncp2);
    }

    p1.add(Box.createVerticalGlue() );
    p1.add(Box.createRigidArea(new Dimension(10,10) ) );
    statLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    p1.add(statLabel);

    cf.add(p1);
    cf.add(vdisplay);
    setSize(1024,768);
    setVisible(true);

    } catch (Exception e) {e.printStackTrace(System.out); System.exit(1);}

  }


  public void actionPerformed(ActionEvent e) {
    cmd = e.getActionCommand();
    //System.out.println("Action command: "+cmd);

    if (cmd.equals("menuFile") ) {
      getNewFile();

    } else if (cmd.equals("menuQuit") ) {
      System.exit(0);

    } else if (cmd.equals("menuNA") ) {
      MapFile = "OUTLUSAM";

    } else if (cmd.equals("menuWorld") ) {
      MapFile = "OUTLSUPW";

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

      ng.setRealTypes(x,y,level,record,pres);

      Dimension dim = ng.getDimension();
      xAxis.setRange(0, dim.width);
      yAxis.setRange(0, dim.height);

      ncp.setNetcdfGrid(ng);
      ncpSfc.setNetcdfGrid(ng);
      ncp2.setNetcdfGrid(ng);

      maxFrames = ng.getNumberOfTimes() - 1;

      Vector v = ng.get4dVariables();
      ncp.setParams(v);
      ncp2.setParams(v);
      Vector vSfc = ng.get3dVariables();
      ncpSfc.setParams(vSfc);

      // can do map now, since grid geometry is known...

      statLabel.setText("Rendering base map...");

      baseMap = new BaseMapAdapter(MapFile);
      baseMap.setDomainSet(ng.getDomainSet() );
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
      rendMap = new ConstantMap[4];
      rendMap[0] = new ConstantMap(.6, Display.Blue);
      rendMap[1] = new ConstantMap(.6, Display.Red);
      rendMap[2] = new ConstantMap(0., Display.Green);
      rendMap[3] = new ConstantMap(-.99, Display.ZAxis);
      di.addReference(mapRef, rendMap);
      mapControl.setValue(0.0);

    } catch (Exception op) {op.printStackTrace(); System.exit(1); }

  }


}
