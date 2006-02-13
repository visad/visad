//
// Histogram.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

package visad.math;

import visad.*;
import visad.java3d.*;

import java.rmi.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 Histogram is the VisAD class for creating histograms of Field
 values.<p>
*/

public class Histogram {

  /** invoke in SpreadSheet by:
      link(visad.math.Histogram.makeHistogram(A1, A2))
  */
  public static FlatField makeHistogram(Data[] datums) {
    FlatField result = null;
    try {
      if (datums == null || datums.length != 2) {
        throw new VisADException("bad arguments");
      }
      if (!(datums[0] instanceof Field)) {
        throw new VisADException("first argument must be a Field");
      }
      if (!(datums[1] instanceof Set)) {
        throw new VisADException("second argument must be a Set");
      }
      result = makeHistogram((Field) datums[0], (Set) datums[1]);
    }
    catch (VisADException e) {
      e.printStackTrace();
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
    if (result == null) {
      System.out.println("result == null");
    }
    return result;
  }

  /** return a histogram of field range values in "bins"
      defined by the samples of set */
  public static FlatField makeHistogram(Field field, Set set)
         throws VisADException, RemoteException {
    FunctionType ftype = (FunctionType) field.getType();
    RealType[] frealComponents = ftype.getRealComponents();

    RealTupleType stype = ((SetType) set.getType()).getDomain();
    RealType[] srealComponents = stype.getRealComponents();

    RealType count = RealType.getRealType("count");
    FunctionType htype = new FunctionType(stype, count);

    int dim = srealComponents.length;
    float[][] field_values = field.getFloats(false);
    float[][] set_values = new float[dim][];
    for (int i=0; i<dim; i++) {
      for (int j=0; j<frealComponents.length; j++) {
        if (srealComponents[i].equals(frealComponents[j])) {
          set_values[i] = field_values[j];
          break;
        }
      }
      if (set_values[i] == null) {
        throw new TypeException("set component " + srealComponents[i] +
                                " does not occur in " + ftype);
      }
    }
    int[] indices = set.valueToIndex(set_values);
    int len = set.getLength();
    float[][] hist_values = new float[1][len];
    for (int i=0; i<len; i++) hist_values[0][i] = 0.0f;
    for (int j=0; j<indices.length; j++) {
      if (indices[j] >= 0) hist_values[0][indices[j]]++;
    }

    FlatField result = new FlatField(htype, set);
    result.setSamples(hist_values, false);
    return result;
  }

  public static void main(String args[])
         throws VisADException, RemoteException {

    int SIZE = 64;

    RealType X = RealType.getRealType("X");
    RealType Y = RealType.getRealType("Y");

    RealType A = RealType.getRealType("A");
    RealType B = RealType.getRealType("B");

    RealType[] domain2d = {X, Y};
    RealTupleType Domain2d = new RealTupleType(domain2d);
    Integer2DSet Domain2dSet = new Integer2DSet(Domain2d, SIZE, SIZE);

    RealType[] range2d = {A, B};
    RealTupleType Range2d = new RealTupleType(range2d);

    FunctionType Field2d2 = new FunctionType(Domain2d, Range2d);

    FlatField image = new FlatField(Field2d2, Domain2dSet);
    int len = Domain2dSet.getLength();
    int ADD = len * len / 16;
    float[][] values = new float[2][len];
    for (int i=0; i<len; i++) {
      values[0][i] = (float) i;
      values[1][i] = (float) ((i * i + ADD) * Math.random());
    }
    image.setSamples(values, false);

    Linear2DSet histSet =
      new Linear2DSet(Range2d, 0.5 * SIZE, len - 0.5 * SIZE, len / SIZE,
           0.5 * SIZE * len, len * len + ADD - 0.5 * SIZE * len, len / SIZE);

    FlatField hist = Histogram.makeHistogram(image, histSet);

    RealType count = RealType.getRealType("count");

    DisplayImplJ3D display1 = new DisplayImplJ3D("display1");
    display1.addMap(new ScalarMap(A, Display.XAxis));
    display1.addMap(new ScalarMap(B, Display.YAxis));
    if (args.length == 0) display1.addMap(new ScalarMap(count, Display.ZAxis));
    display1.addMap(new ScalarMap(count, Display.RGB));

    GraphicsModeControl gmc = display1.getGraphicsModeControl();
    gmc.setScaleEnable(true);
    // gmc.setTextureEnable(false);
    gmc.setCurvedSize(1);

    DataReference hist_ref = new DataReferenceImpl("hist_ref");
    hist_ref.setData(hist);
    display1.addReference(hist_ref);

    JFrame frame = new JFrame("VisAD HSV Color Coordinates");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    frame.getContentPane().add(display1.getComponent());

    int WIDTH = 500;
    int HEIGHT = 600;

    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);
    frame.setVisible(true);
  }

}

