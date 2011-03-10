//
// HSVDisplay.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

import visad.*;
import visad.java3d.*;
import visad.util.*;

import java.io.IOException;
import java.rmi.RemoteException;


// JFC packages
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

/**
   HSVDisplay is an application for interactively exploring
   the relation between the HSV and RGB color coordiantes.<P>
*/
public class HSVDisplay extends Object implements ActionListener {

  DisplayImplJ3D display1 = null;

  RealType red = null;
  RealType green = null;
  RealType blue = null;

  RealType hue = null;
  RealType saturation = null;
  RealType value = null;

  ContourCell cell_hue = null;
  ContourCell cell_saturation = null;
  ContourCell cell_value = null;

  ContourControl controlhcontour = null;
  ContourControl controlscontour = null;
  ContourControl controlvcontour = null;

  int state = 0; // number of ScalarMaps added, 0 through 9
  ScalarMap[] maps = new ScalarMap[9];

  public static void main(String args[])
         throws IOException, VisADException, RemoteException {

    HSVDisplay dummy = new HSVDisplay();
  }

  public HSVDisplay()
         throws IOException, VisADException, RemoteException {

    // define an rgb color space
    // (not to be confused with system's RGB DisplayTupleType)
    red = RealType.getRealType("red");
    green = RealType.getRealType("green");
    blue = RealType.getRealType("blue");
    RealTupleType rgb = new RealTupleType(red, green, blue);

    // define an hsv color space
    // (not to be confused with system's HSV DisplayTupleType)
    hue = RealType.getRealType("hue", CommonUnit.degree);
    saturation = RealType.getRealType("saturation");
    value = RealType.getRealType("value");
    // note that we use the same HSVCoordinateSystem that the
    // system uses to define the relation between its RGB and HSV
    CoordinateSystem hsv_system = new HSVCoordinateSystem(rgb);
    RealTupleType hsv = new RealTupleType(hue, saturation, value,
                                          hsv_system, null);

    // construct a sampling of the hsv color space;
    // since hue is composed of six linear (in rgb) pieces with
    // discontinuous derivative bwteen pieces, it should be sampled
    // at 6*n+1 points with n not too small;
    // for a given hue, saturation and value are both linear in rgb
    // so 2 samples suffice for each of them;
    // the HSV - RGB transform is degenerate at saturation = 0.0
    // and value = 0.0 so avoid those values;
    // hue is in Units of degrees so that must be used in the Set
    // constructor
    Linear3DSet cube_set =
      new Linear3DSet(hsv, 0.0, 360.0, 37,
                           0.01, 1.0, 2,
                           0.01, 1.0, 2, null,
                      new Unit[] {CommonUnit.degree, null, null},
                      null);

    // construct a DataReference to cube_set so it can be displayed
    DataReference cube_ref = new DataReferenceImpl("cube");
    cube_ref.setData(cube_set);

    DataReference hue_ref =
      new DataReferenceImpl("hue");
    DataReference saturation_ref =
      new DataReferenceImpl("saturation");
    DataReference value_ref =
      new DataReferenceImpl("value");
    VisADSlider hue_slider =
      new VisADSlider("hue", 0, 359, 0, 1.0, hue_ref,
                      RealType.Generic);
    VisADSlider saturation_slider =
      new VisADSlider("saturation", 0, 100, 0, 0.01, saturation_ref,
                      RealType.Generic);
    VisADSlider value_slider =
      new VisADSlider("value", 0, 100, 0, 0.01, value_ref,
                      RealType.Generic);

    // construct a Display
    display1 = new DisplayImplJ3D("display1");

    // makeMaps();
    // makeColorMaps();
    for (int i=0; i<9; i++) addMap();

    display1.getGraphicsModeControl().setScaleEnable(true);

    DisplayRendererJ3D dr = (DisplayRendererJ3D) display1.getDisplayRenderer();
    KeyboardBehaviorJ3D kbd = new KeyboardBehaviorJ3D(dr);
    dr.addKeyboardBehavior(kbd);

    // display cube_set
    display1.addReference(cube_ref);

    JFrame frame = new JFrame("VisAD HSV Color Coordinates");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in frame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);

    panel.add(hue_slider);
    panel.add(saturation_slider);
    panel.add(value_slider);

    panel.add(display1.getComponent());

    JPanel panel2 = new JPanel();
    panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
    panel2.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel2.setAlignmentX(JPanel.LEFT_ALIGNMENT);


    JButton clear = new JButton("Clear");
    clear.addActionListener(this);
    clear.setActionCommand("clear");
    panel2.add(clear);

    JButton add = new JButton("Add");
    add.addActionListener(this);
    add.setActionCommand("add");
    panel2.add(add);

    JButton remove = new JButton("Remove");
    remove.addActionListener(this);
    remove.setActionCommand("remove");
    panel2.add(remove);

    panel.add(panel2);

    int WIDTH = 500;
    int HEIGHT = 700;

    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);
    frame.setVisible(true);

    cell_hue =
      new ContourCell(controlhcontour, hue_ref);
    cell_hue.addReference(hue_ref);
    cell_saturation =
      new ContourCell(controlscontour, saturation_ref);
    cell_saturation.addReference(saturation_ref);
    cell_value =
      new ContourCell(controlvcontour, value_ref);
    cell_value.addReference(value_ref);
  }

  /** This method handles button presses */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    try {
      if (cmd.equals("clear")) {
        display1.clearMaps();
        controlhcontour = null;
        controlscontour = null;
        controlvcontour = null;
        setControls();
        for (int i=0; i<9; i++) maps[i] = null;
        state = 0;
      }
      else if (cmd.equals("add")) {
        addMap();
      }
      else if (cmd.equals("remove")) {
        removeMap();
      }
    }
    catch (VisADException ex) {
      System.out.println("call clearMaps ex = " + ex);
    }
    catch (RemoteException ex) {
      System.out.println("call clearMaps ex = " + ex);
    }
  }

  private void addMap()
          throws VisADException, RemoteException {
    switch (state) {
      case 0:
        maps[0] = new ScalarMap(red, Display.XAxis);
        display1.addMap(maps[0]);
        break;
      case 1:
        maps[1] = new ScalarMap(green, Display.YAxis);
        display1.addMap(maps[1]);
        break;
      case 2:
        maps[2] = new ScalarMap(blue, Display.ZAxis);
        display1.addMap(maps[2]);
        break;
      case 3:
        maps[3] = new ScalarMap(hue, Display.IsoContour);
        display1.addMap(maps[3]);
        controlhcontour = (ContourControl) maps[3].getControl();
        if (cell_hue != null) cell_hue.setControl(controlhcontour);
        break;
      case 4:
        maps[4] = new ScalarMap(saturation, Display.IsoContour);
        display1.addMap(maps[4]);
        controlscontour = (ContourControl) maps[4].getControl();
        if (cell_saturation != null) cell_saturation.setControl(controlscontour);
        break;
      case 5:
        maps[5] = new ScalarMap(value, Display.IsoContour);
        display1.addMap(maps[5]);
        controlvcontour = (ContourControl) maps[5].getControl();
        if (cell_value != null) cell_value.setControl(controlvcontour);
        break;
      case 6:
        maps[6] = new ScalarMap(hue, Display.Hue);
        display1.addMap(maps[6]);
        break;
      case 7:
        maps[7] = new ScalarMap(saturation, Display.Saturation);
        display1.addMap(maps[7]);
        break;
      case 8:
        maps[8] = new ScalarMap(value, Display.Value);
        display1.addMap(maps[8]);
        break;
      case 9:
        return;
    }
    state++;
  }

  private void removeMap() 
          throws VisADException, RemoteException {
    switch (state) {
      case 0:
        return;
      case 4:
        controlhcontour = null;
        if (cell_hue != null) cell_hue.setControl(controlhcontour);
        break;
      case 5:
        controlscontour = null;
        if (cell_saturation != null) cell_saturation.setControl(controlscontour);
        break;
      case 6:
        controlvcontour = null;
        if (cell_value != null) cell_value.setControl(controlvcontour);
        break;
      default:
        break;
    }
    state--;
    display1.removeMap(maps[state]);
    maps[state] = null;
  }

  private void setControls()
          throws VisADException, RemoteException {
    cell_hue.setControl(controlhcontour);
    cell_saturation.setControl(controlscontour);
    cell_value.setControl(controlvcontour);
  }

  class ContourCell extends CellImpl {
    ContourControl control;
    DataReference ref;
    double value;

    ContourCell(ContourControl cc, DataReference r)
           throws VisADException, RemoteException {
      control = cc;
      ref = r;
      value = ((Real) ref.getData()).getValue();
    }

    public void setControl(ContourControl cc)
           throws VisADException, RemoteException {
      control = cc;
      value = Double.NaN;
      doAction();
    }

    public void doAction() throws VisADException, RemoteException {
      double val = ((Real) ref.getData()).getValue();
      ContourControl cc = control;
      if (val == val && val != value && cc != null) {
        cc.setSurfaceValue((float) ((Real) ref.getData()).getValue());
        cc.enableContours(true);
        value = val;
      }
    }

  }

}

