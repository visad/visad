//
// HSVDisplay.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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
public class HSVDisplay extends Object {

  public static void main(String args[])
         throws IOException, VisADException, RemoteException {

    // define an rgb color space
    // (not to be confused with system's RGB DisplayTupleType)
    RealType red = new RealType("red", null, null);
    RealType green = new RealType("green", null, null);
    RealType blue = new RealType("blue", null, null);
    RealTupleType rgb = new RealTupleType(red, green, blue);

    // define an hsv color space
    // (not to be confused with system's HSV DisplayTupleType)
    RealType hue = new RealType("hue", CommonUnit.degree,       null);
    RealType saturation = new RealType("saturation", null, null);
    RealType value = new RealType("value", null, null);
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
    DisplayImplJ3D display1 = new DisplayImplJ3D("display1");

    // map rgb to the Display spatial coordinates;
    // iso-surfaces of hue, saturation and value will be
    // transformed from hsv to rgb space
    display1.addMap(new ScalarMap(red, Display.XAxis));
    display1.addMap(new ScalarMap(green, Display.YAxis));
    display1.addMap(new ScalarMap(blue, Display.ZAxis));

    // color iso-surfaces
    display1.addMap(new ScalarMap(hue, Display.Hue));
    display1.addMap(new ScalarMap(saturation, Display.Saturation));
    display1.addMap(new ScalarMap(value, Display.Value));

    // construct mappings for interactive iso-surfaces of
    // hue, saturation and value
    ScalarMap maphcontour = new ScalarMap(hue, Display.IsoContour);
    display1.addMap(maphcontour);
    ContourControl controlhcontour = (ContourControl) maphcontour.getControl();

    ScalarMap mapscontour = new ScalarMap(saturation, Display.IsoContour);
    display1.addMap(mapscontour);
    ContourControl controlscontour = (ContourControl) mapscontour.getControl();

    ScalarMap mapvcontour = new ScalarMap(value, Display.IsoContour);
    display1.addMap(mapvcontour);
    ContourControl controlvcontour = (ContourControl) mapvcontour.getControl();

    display1.getGraphicsModeControl().setScaleEnable(true);

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

    int WIDTH = 500;
    int HEIGHT = 600;

    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);
    frame.setVisible(true);

    HSVDisplay dummy = new HSVDisplay();
    ContourCell cell_hue =
      dummy. new ContourCell(controlhcontour, hue_ref);
    cell_hue.addReference(hue_ref);
    ContourCell cell_saturation =
      dummy. new ContourCell(controlscontour, saturation_ref);
    cell_saturation.addReference(saturation_ref);
    ContourCell cell_value =
      dummy. new ContourCell(controlvcontour, value_ref);
    cell_value.addReference(value_ref);
  }

  class ContourCell extends CellImpl {
    ContourControl control;
    DataReference ref;
    double value;

    ContourCell(ContourControl c, DataReference r)
           throws VisADException, RemoteException {
      control = c;
      ref = r;
      value = ((Real) ref.getData()).getValue();
    }

    public void doAction() throws VisADException, RemoteException {
      double val = ((Real) ref.getData()).getValue();
      if (val == val && val != value) {
        control.setSurfaceValue((float) ((Real) ref.getData()).getValue());
        control.enableContours(true);
        value = val;
      }
    }

  }

}

