//
// AspectRatio.java
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

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import javax.swing.JFrame;
import visad.*;
import visad.java2d.DisplayImplJ2D;

/** Demonstrates how to link a display's aspect ratio
    to the size of the display frame. */
public class AspectRatio extends JFrame {

  /** tests the AspectRatio application */
  public static void main(String[] argv)
    throws VisADException, RemoteException
  {
    AspectRatio ar = new AspectRatio();

    // close program when frame dies
    ar.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });

    // display the application onscreen
    ar.pack();
    ar.setVisible(true);
  }

  /** the projection control of the display */
  private ProjectionControl pc;

  /** constructs a new AspectRatio application */
  public AspectRatio() throws VisADException, RemoteException {
    // construct data type
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);

    // construct data set
    int size = 32;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    // construct display
    DisplayImplJ2D disp = new DisplayImplJ2D("disp");

    // add scalar maps
    disp.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    disp.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    disp.addMap(new ScalarMap(vis_radiance, Display.RGB));

    // link data to display with a data reference
    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    disp.addReference(ref_imaget1, null);

    // grab the display's projection control
    pc = disp.getProjectionControl();

    // set up the GUI
    getContentPane().add(disp.getComponent());
    setTitle("Aspect ratio linked to frame size");
  }

  /** called when the frame gets resized */
  public void doLayout() {
    super.doLayout();

    // adjust the aspect ratio to match the new size
    Dimension size = getSize();
    try {
      pc.setAspect(new double[] {1.0, (double) size.height / size.width});
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
  }

}

