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
import visad.java3d.*;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Parallel {

  private static final int NCOORDS = 5;
  private static final int NROWS = 6;

  // type 'java Parallel' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    RealType index = RealType.getRealType("index");
    RealType[] coords = new RealType[NCOORDS];
    for (int i=0; i<NCOORDS; i++) {
      coords[i] = RealType.getRealType("coord" + i);
    }
    RealTupleType range = new RealTupleType(coords);
    FunctionType ftype = new FunctionType(index, range);
    Integer1DSet index_set = new Integer1DSet(NROWS);

    float[][] samples = new float[NCOORDS][NROWS];
    for (int i=0; i<NCOORDS; i++) {
      for (int j=0; j<NROWS; j++) {
        samples[i][j] = (float) Math.random();
      }
    }

    FlatField data = new FlatField(ftype, index_set);
    data.setSamples(samples, false);

    // create a 2-D Display using Java3D
    DisplayImpl display =
      new DisplayImplJ3D("display", new TwoDDisplayRendererJ3D());

    parallel(display, data);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("Parallel Coordinates VisAD Application");
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

  /** create parallel coordinates display for data */
  public static void parallel(DisplayImpl display, FlatField data)
         throws VisADException, RemoteException {

    FunctionType ftype = (FunctionType) data.getType();
    RealType index = (RealType) ftype.getDomain().getComponent(0);
    RealTupleType range = (RealTupleType) ftype.getRange();
    int ncoords = range.getDimension();
    int nrows = data.getLength();
    Set index_set = data.getDomainSet();
    float[][] samples = data.getFloats(false);

    RealType x = RealType.getRealType("coordinate");
    RealType y = RealType.getRealType("value");
    SetType xy = new SetType(new RealTupleType(x, y));
    FunctionType ptype = new FunctionType(index, xy);
    FieldImpl pfield = new FieldImpl(ptype, index_set);
    for (int j=0; j<nrows; j++) {
      float[][] locs = new float[2][ncoords];
      for (int i=0; i<ncoords; i++) {
        locs[0][i] = i;
        locs[1][i] = samples[i][j];
      }
      Gridded2DSet set = new Gridded2DSet(xy, locs, ncoords);
      pfield.setSample(j, set, false);
    }

    // create a DataReference for river system
    DataReference parallel_ref = new DataReferenceImpl("parallel");
    parallel_ref.setData(pfield);

    display.addMap(new ScalarMap(x, Display.XAxis));
    display.addMap(new ScalarMap(y, Display.YAxis));

    // enable axis scales
    display.getGraphicsModeControl().setScaleEnable(true);

    // link display to parallel display
    display.addReference(parallel_ref);
  }

}

