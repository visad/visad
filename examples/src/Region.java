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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Region {

  // type 'java Region' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    RealTupleType earth =
      new RealTupleType(RealType.Latitude, RealType.Longitude);

    // construct boundary of 7-pointed star
    int np = 14;
    float[][] samples = new float[2][np];
    float radius = 7.0f;
    for (int i=0; i<np; i++) {
      double b = 2.0 * Math.PI * i / np;
      samples[0][i] = radius * ((float) Math.cos(b));
      samples[1][i] = radius * ((float) Math.sin(b));
      radius = 10.0f - radius;
    }

    // compute triangles to fill star, and use them to construct Delaunay
    int[][] tris = DelaunayCustom.fill(samples);
    DelaunayCustom delaunay = new DelaunayCustom(samples, tris);

    Irregular2DSet region =
      new Irregular2DSet(earth, samples, null, null, null, delaunay);

    // create a DataReference for region
    final DataReference region_ref = new DataReferenceImpl("region");
    region_ref.setData(region);

    // create a Display using Java3D
    // DisplayImpl display = new DisplayImplJ3D("image display");
    // create a Display using Java2D
    DisplayImpl display = new DisplayImplJ2D("image display");

    // map earth coordinates to display coordinates
    display.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));

    // link the Display to region_ref
    display.addReference(region_ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("Region VisAD Application");
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

