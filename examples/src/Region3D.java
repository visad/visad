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
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.event.*;
import javax.swing.*;

public class Region3D {

  // type 'java Region3D' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    RealTupleType earth =
      new RealTupleType(RealType.Latitude, RealType.Longitude);

    // construct boundary of 7-pointed star
    int np = 14;
    float[][] samples = new float[2][np];
    float radius = 1.0f;
    for (int i=0; i<np; i++) {
      double b = 2.0 * Math.PI * i / np;
      samples[0][i] = radius * ((float) Math.cos(b));
      samples[1][i] = radius * ((float) Math.sin(b));
      radius = 1.5f - radius;
System.out.println("sample[" + i + "] = (" + samples[1][i] + ", " +
                   samples[0][i] + ")");
    }

    // compute triangles to fill star, and use them to construct Delaunay
    int[][] tris = DelaunayCustom.fill(samples);
for (int i=0; i<tris.length; i++) {
  System.out.println("triangle[" + i + "] = (" + tris[i][0] + ", " +
                     tris[i][1] + ", " + tris[i][2] + ")");
}
    DelaunayCustom delaunay = new DelaunayCustom(samples, tris);

    Irregular2DSet region =
      new Irregular2DSet(earth, samples, null, null, null, delaunay);

    // create a DataReference for region
    final DataReference region_ref = new DataReferenceImpl("region");
    region_ref.setData(region);

    // create a Display using Java3D
    // DisplayImpl display = new DisplayImplJ3D("image display");
    // create a Display using Java2D
    DisplayImpl display = new DisplayImplJ3D("image display");

    // map earth coordinates to display coordinates
    ScalarMap xmap = new ScalarMap(RealType.Longitude, Display.XAxis);
    display.addMap(xmap);
    xmap.setRange(-1.0, 1.0);
    ScalarMap ymap = new ScalarMap(RealType.Latitude, Display.YAxis);
    display.addMap(ymap);
    ymap.setRange(-1.0, 1.0);
    display.addMap(new ConstantMap(0.5f, Display.Alpha));

    // link the Display to region_ref
    display.addReference(region_ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("Region3D VisAD Application");
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

/*
demedici% java Region3D
sample[0] = (0.0, 1.0)
sample[1] = (0.21694186, 0.45048442)
sample[2] = (0.7818315, 0.6234898)
sample[3] = (0.48746395, 0.11126047)
sample[4] = (0.9749279, -0.22252093)
sample[5] = (0.39091575, -0.3117449)
sample[6] = (0.43388373, -0.90096885)
sample[7] = (6.123234E-17, -0.5)
sample[8] = (-0.43388373, -0.90096885)
sample[9] = (-0.39091575, -0.3117449)
sample[10] = (-0.9749279, -0.22252093)
sample[11] = (-0.48746395, 0.11126047)
sample[12] = (-0.7818315, 0.6234898)
sample[13] = (-0.21694186, 0.45048442)

triangle[0] = (1, 2, 3)
triangle[1] = (3, 4, 5)
triangle[2] = (5, 6, 7)
triangle[3] = (7, 8, 9)
triangle[4] = (9, 10, 11)
triangle[5] = (11, 12, 13)
triangle[6] = (13, 0, 1)
triangle[7] = (13, 1, 3)
triangle[8] = (13, 3, 5)
triangle[9] = (13, 5, 7)
triangle[10] = (13, 7, 9)
triangle[11] = (13, 9, 11)

strip[0] = (0.7818315, 0.6234898, 0.0)       2
strip[1] = (0.48746395, 0.11126047, 0.0)     3
strip[2] = (0.21694186, 0.45048442, 0.0)     1
strip[3] = (-0.21694186, 0.45048442, 0.0)    13
strip[4] = (0.0, 1.0, 0.0)                   0
strip[5] = (0.0, 1.0, 0.0)                   0
strip[6] = (0.9749279, -0.22252093, 0.0)     4
strip[7] = (0.9749279, -0.22252093, 0.0)     4
strip[8] = (0.39091575, -0.3117449, 0.0)     5  bad
strip[9] = (0.48746395, 0.11126047, 0.0)     3  bad
strip[10] = (-0.21694186, 0.45048442, 0.0)   13 bad
strip[11] = (0.39091575, -0.3117449, 0.0)    5  bad
strip[12] = (6.123234E-17, -0.5, 0.0)        7
strip[13] = (0.43388373, -0.90096885, 0.0)   6
strip[14] = (0.43388373, -0.90096885, 0.0)   6
strip[15] = (-0.43388373, -0.90096885, 0.0)  8
strip[16] = (-0.43388373, -0.90096885, 0.0)  8
strip[17] = (-0.39091575, -0.3117449, 0.0)   9  bad
strip[18] = (6.123234E-17, -0.5, 0.0)        7  bad
strip[19] = (-0.21694186, 0.45048442, 0.0)   13 bad
strip[20] = (-0.39091575, -0.3117449, 0.0)   9  bad
strip[21] = (-0.48746395, 0.11126047, 0.0)   11
strip[22] = (-0.9749279, -0.22252093, 0.0)   10
strip[23] = (-0.9749279, -0.22252093, 0.0)   10
strip[24] = (-0.48746395, 0.11126047, 0.0)   11
strip[25] = (-0.48746395, 0.11126047, 0.0)   11
strip[26] = (-0.7818315, 0.6234898, 0.0)     12
strip[27] = (-0.21694186, 0.45048442, 0.0)   13

shaded regions in inner heptagon:

               0

   12                      2
           13      1
          ... ...
          ...  ......
      11 .....   .......3
         .....    ......
        ......      ....
10      .......      ..       4
        9......       5
           .....
               7

        8             6

*/

