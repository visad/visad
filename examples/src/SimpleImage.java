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
import visad.Set;
import visad.java3d.*;
import visad.util.*;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.print.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

/* Simple image display example.  Define a function that maps from
 * (line,element) to a brightness value.  Create a FlatField that
 * realizes this mapping for a domain of (300,300).  Fill the FlatField
 * with values in the range (0-255).
 *
 * Create ScalaMappings of (line->YAxis), (element->XAxis) and
 * (brightness -> RGB).
 *
 * Also, set a range on the ScalaMap for the YAxis to illustrate the
 * effect/use of this.
*/

public class SimpleImage {

  public static void main(String args[])
         throws VisADException, IOException {

    // define types
    RealType line = RealType.getRealType("row");
    RealType element = RealType.getRealType("col");
    RealTupleType domain = new RealTupleType(line, element);
    RealType range = RealType.getRealType("brightness");

    FunctionType image_func = new FunctionType(domain, range);

    // now, define the Data objects
    Set domain_set = new Integer2DSet(300,300);
    FlatField image_data = new FlatField(image_func, domain_set);

    // make up some data (line,element) => brightness values; dimensioned
    // values[number_of_range_components][number_of_samples_in_domain_set]

    double[][] values = new double[1][300*300];
    for (int i=0; i<300; i++) {
      for (int j=0; j<300; j++) {
        values[0][i + 300*j] = ((16*i)/300.) * ((16*j)/300);
      }
    }

    // put the data values into the FlatField image_data
    image_data.setSamples(values);

    // now make a reference for the data so it can be displayed
    DataReference image_ref =new DataReferenceImpl("image");
    image_ref.setData(image_data);

    // define the mappings of the display
    DisplayImpl di = new DisplayImplJ3D("display");

    // override the default range on display's Y axis
    ScalarMap line_map = new ScalarMap(line, Display.YAxis);
    line_map.setRange(-100,400);

    di.addMap(line_map);
    di.addMap(new ScalarMap(element, Display.XAxis));
    di.addMap(new ScalarMap(range, Display.RGB));

    // add the data reference
    di.addReference(image_ref);

    // create JFrame (i.e., a window) for display and slider
    // (cobbled from the DisplayTest examples)

    JFrame frame = new JFrame("Simple VisAD Application");

    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    frame.getContentPane().add(panel);

    panel.add(di.getComponent());

    // add a button for printing the screen
    JButton butt = new JButton("Print me");
    butt.addActionListener(new visad.util.PrintActionListener(di));
    panel.add(butt);

    // set size of JFrame and make it visible
    frame.setSize(500, 400);
    frame.setVisible(true);
  }

}
