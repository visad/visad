//
// HSVDisplay.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

  int state = 0; // 0 - clear, 1 - maps, 2 - maps + color maps

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

    makeMaps();
    makeColorMaps();

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

    JButton maps = new JButton("Maps");
    maps.addActionListener(this);
    maps.setActionCommand("maps");
    panel2.add(maps);

    JButton color = new JButton("Color");
    color.addActionListener(this);
    color.setActionCommand("color");
    panel2.add(color);

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
        state = 0;
      }
      else if (cmd.equals("maps")) {
        makeMaps();
        setControls();
      }
      else if (cmd.equals("color")) {
        makeColorMaps();
      }
    }
    catch (VisADException ex) {
      System.out.println("call clearMaps ex = " + ex);
    }
    catch (RemoteException ex) {
      System.out.println("call clearMaps ex = " + ex);
    }
  }

  private void makeMaps()
          throws VisADException, RemoteException {
    if (state != 0) return;
    // map rgb to the Display spatial coordinates;
    // iso-surfaces of hue, saturation and value will be
    // transformed from hsv to rgb space
    display1.addMap(new ScalarMap(red, Display.XAxis));
    display1.addMap(new ScalarMap(green, Display.YAxis));
    display1.addMap(new ScalarMap(blue, Display.ZAxis));

    // construct mappings for interactive iso-surfaces of
    // hue, saturation and value
    ScalarMap maphcontour = new ScalarMap(hue, Display.IsoContour);
    display1.addMap(maphcontour);
    controlhcontour = (ContourControl) maphcontour.getControl();

    ScalarMap mapscontour = new ScalarMap(saturation, Display.IsoContour);
    display1.addMap(mapscontour);
    controlscontour = (ContourControl) mapscontour.getControl();

    ScalarMap mapvcontour = new ScalarMap(value, Display.IsoContour);
    display1.addMap(mapvcontour);
    controlvcontour = (ContourControl) mapvcontour.getControl();

    state = 1;
  }

  private void makeColorMaps()
          throws VisADException, RemoteException {
    if (state != 1) return;

    // color iso-surfaces
    display1.addMap(new ScalarMap(hue, Display.Hue));
    display1.addMap(new ScalarMap(saturation, Display.Saturation));
    display1.addMap(new ScalarMap(value, Display.Value));

    state = 2;
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

