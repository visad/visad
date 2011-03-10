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

// import needed classes
import visad.*;
import visad.java2d.DisplayImplJ2D;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.event.*;
import javax.swing.*;

public class Rivers {

  // type 'java Rivers' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    RealTupleType earth =
      new RealTupleType(RealType.Latitude, RealType.Longitude);

    // construct straight south flowing river1
    float[][] points1 = {{3.0f, 2.0f, 1.0f, 0.0f},
                         {0.0f, 0.0f, 0.0f, 0.0f}};
    Gridded2DSet river1 = new Gridded2DSet(earth, points1, 4);

    // construct east feeder river2
    float[][] points2 = {{3.0f, 2.0f, 1.0f},
                         {2.0f, 1.0f, 0.0f}};
    Gridded2DSet river2 = new Gridded2DSet(earth, points2, 3);

    // construct west feeder river3
    float[][] points3 = {{4.0f, 3.0f, 2.0f},
                         {-2.0f, -1.0f, 0.0f}};
    Gridded2DSet river3 = new Gridded2DSet(earth, points3, 3);

    // construct river system
    Gridded2DSet[] river_system = {river1, river2, river3};
    UnionSet rivers = new UnionSet(earth, river_system);

    // create a DataReference for river system
    final DataReference rivers_ref = new DataReferenceImpl("rivers");
    rivers_ref.setData(rivers);

    // create a Display using Java3D
    // DisplayImpl display = new DisplayImplJ3D("image display");
    // create a Display using Java2D
    DisplayImpl display = new DisplayImplJ2D("image display");

    // map earth coordinates to display coordinates
    display.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));

    // link the Display to rivers_ref
    display.addReference(rivers_ref);
    rivers_ref.setData(rivers);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("Rivers VisAD Application");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);

    // add display to JPanel
    panel.add(display.getComponent());

    // set size of JFrame and make it visible
    frame.setSize(500, 500);
    frame.setVisible(true);
  }
}

