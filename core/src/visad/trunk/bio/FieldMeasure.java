//
// FieldMeasure.java
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

package visad.bio;

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import javax.swing.*;
import visad.*;
import visad.data.DefaultFamily;
import visad.java2d.DirectManipulationRendererJ2D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.DisplayImplJ3D;

/**
 * FieldMeasure is a class for measuring the
 * distance between points in a field.
 */
public class FieldMeasure {

  /** This measurement object's associated field. */
  private FieldImpl field;

  /** The first endpoint of the measurement object. */
  private RealTuple p1;
  
  /** The second endpoint of the measurement object. */
  private RealTuple p2;

  /** The connecting line of the measurement object. */
  private Gridded2DSet line;

  /** Data reference for first endpoint. */
  private DataReferenceImpl ref_p1;

  /** Data reference for second endpoint. */
  private DataReferenceImpl ref_p2;

  /** Data reference for connecting line. */
  private DataReferenceImpl ref_line;

  /** Constructs a measurement object to match the given field. */
  public FieldMeasure(FieldImpl field) throws VisADException, RemoteException {
    FunctionType type = (FunctionType) field.getType();
    final RealTupleType domain = type.getDomain();
    Set set = field.getDomainSet();
    float[][] samples = set.getSamples(false);
    final int len = domain.getDimension();
    Real[] p1r = new Real[len];
    Real[] p2r = new Real[len];
    for (int i=0; i<len; i++) {
      RealType rt = (RealType) domain.getComponent(i);
      float s1 = samples[i][0];
      float s2 = samples[i][samples[i].length - 1];
      if (s1 != s1) s1 = 0;
      if (s2 != s2) s2 = 0;
      p1r[i] = new Real(rt, s1);
      p2r[i] = new Real(rt, s2);
    }
    this.field = field;
    p1 = new RealTuple(p1r);
    p2 = new RealTuple(p2r);
    ref_p1 = new DataReferenceImpl("p1");
    ref_p1.setData(p1);
    ref_p2 = new DataReferenceImpl("p2");
    ref_p2.setData(p2);
    ref_line = new DataReferenceImpl("line");
    ref_line.setData(line);
    final int[] two = new int[] {2};
    CellImpl cell = new CellImpl() {
      public void doAction() {
        float[][] samps = new float[len][2];
        double[][] values = getValues();
        for (int i=0; i<len; i++) {
          samps[i][0] = (float) values[i][0];
          samps[i][1] = (float) values[i][1];
        }
        try {
          GriddedSet line = new GriddedSet(domain, samps, two);
          ref_line.setData(line);
        }
        catch (VisADException exc) {
          exc.printStackTrace();
        }
        catch (RemoteException exc) {
          exc.printStackTrace();
        }
      }
    };
    cell.addReference(ref_p1);
    cell.addReference(ref_p2);
  }

  /** Adds the distance measuring data to the given display. */
  public void addToDisplay(DisplayImpl d)
    throws VisADException, RemoteException
  {
    boolean j3d = d instanceof DisplayImplJ3D;
    d.getGraphicsModeControl().setPointSize(5.0f);

    // add first endpoint
    DataRenderer renderer = j3d ?
      (DataRenderer) new DirectManipulationRendererJ3D() :
      (DataRenderer) new DirectManipulationRendererJ2D();
    d.addReferences(renderer, new DataReference[] {ref_p1}, null);

    // add second endpoint
    renderer = j3d ?
      (DataRenderer) new DirectManipulationRendererJ3D() :
      (DataRenderer) new DirectManipulationRendererJ2D();
    d.addReferences(renderer, new DataReference[] {ref_p2}, null);

    // add connecting line
    d.addReference(ref_line);
  }

  /** Removes this distance measuring data from the given display. */
  public void removeFromDisplay(DisplayImpl d)
    throws VisADException, RemoteException
  {
    d.removeReference(ref_p1);
    d.removeReference(ref_p2);
    d.removeReference(ref_line);
  }

  /** Gets the current distance between the endpoints. */
  public double getDistance() {
    double[][] values = getValues();
    double sum = 0;
    for (int i=0; i<values.length; i++) {
      double distance = values[i][1] - values[i][0];
      sum += distance * distance;
    }
    return Math.sqrt(sum);
  }

  /** Gets the current values of the endpoints. */
  public double[][] getValues() {
    RealTuple rt1 = (RealTuple) ref_p1.getData();
    RealTuple rt2 = (RealTuple) ref_p2.getData();
    int len = rt1.getDimension();
    double[][] values = new double[len][2];
    try {
      for (int i=0; i<len; i++) {
        Real r1 = (Real) rt1.getComponent(i);
        Real r2 = (Real) rt2.getComponent(i);
        values[i][0] = r1.getValue();
        values[i][1] = r2.getValue();
      }
    }
    catch (VisADException exc) {
      exc.printStackTrace();
    }
    catch (RemoteException exc) {
      exc.printStackTrace();
    }
    return values;
  }

  /** Tests the FieldMeasure class. */
  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Please specify a field on the command line.");
      System.exit(2);
    }
    DefaultFamily loader = new DefaultFamily("loader");
    FieldImpl field = (FieldImpl) loader.open(args[0]);
    DisplayImplJ3D display = new DisplayImplJ3D("display");
    ScalarMap[] maps = field.getType().guessMaps(true);
    for (int i=0; i<maps.length; i++) display.addMap(maps[i]);
    DataReferenceImpl ref = new DataReferenceImpl("ref");
    ref.setData(field);
    display.addReference(ref);
    FieldMeasure fm = new FieldMeasure(field);
    fm.addToDisplay(display);
    JFrame frame = new JFrame("FieldMeasure");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    JPanel pane = new JPanel();
    frame.setContentPane(pane);
    pane.add(display.getComponent(), "CENTER");
    frame.pack();
    frame.show();
  }

}
