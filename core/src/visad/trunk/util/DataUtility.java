
//
// DataUtility.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.util;

// RMI classes
import java.rmi.RemoteException;

import visad.java2d.DisplayImplJ2D;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;

// GUI handling
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// VisAD packages
import visad.*;

/** A class for constructing and manipulating VisAD Data
    objects for images */
public class DataUtility extends Object {

  private static boolean init = false;
  private static FunctionType simpleImageType;
  private static RealType radiance;
  private static RealTupleType imageDomain;
  private static RealType line;
  private static RealType element;

  private int num_lines, num_elements;

  private static synchronized void makeTypes()
          throws VisADException {
    if (!init) {
      init = true;
      simpleImageType = (FunctionType)
        MathType.stringToType("((ImageElement, ImageLine) -> ImageRadiance)");
      imageDomain = simpleImageType.getDomain();
      line = (RealType) imageDomain.getComponent(0);
      element = (RealType) imageDomain.getComponent(1);
      MathType range = simpleImageType.getRange();
      if (range instanceof RealType) {
        radiance = (RealType) range;
      }
      else {
        radiance = (RealType) ((RealTupleType) range).getComponent(0);
      }
    }
  }

  /** return a FlatField for a simple image from
      values[nlines][nelements] */
  public static FlatField makeImage(float[][] values)
         throws VisADException, RemoteException {
    if (values == null) return null;
    int nlines = values.length;
    int nelements = 0;
    for (int l=0; l<nlines; l++) {
      if (values[l] != null) {
        if (values[l].length > nelements) nelements = values[l].length;
      }
    }
    if (!init) makeTypes();
    FlatField image = new FlatField(simpleImageType,
                new Integer2DSet(imageDomain, nelements, nlines));
    setPixels(image, values);
    return image;
  }

  /** set pixel values in a simple image,
      indexed as values[line][element] */
  public static void setPixels(FlatField image, float[][] values)
         throws VisADException, RemoteException {
    if (values == null) return;
    Integer2DSet set = (Integer2DSet) image.getDomainSet();
    int num_elements = set.getLength(0);
    int num_lines = set.getLength(1);
    float[][] vals = new float[1][num_lines * num_elements];
    for (int i=0; i<num_lines * num_elements; i++) {
      vals[0][i] = Float.NaN;
    }
    int nl = values.length;
    if (num_lines < nl) nl = num_lines;
    int base = 0;
    for (int l=0; l<nl; l++) {
      if (values[l] != null) {
        int ne = values[l].length;
        if (num_elements < ne) ne = num_elements;
        for (int e=0; e<ne; e++) {
          vals[0][base + e] = values[l][e];
        }
      }
      base += num_elements;
    }
    image.setSamples(vals, false); // no need to copy
  }

  public static float[][] getPixels(FlatField image)
         throws VisADException, RemoteException {
    Integer2DSet set = (Integer2DSet) image.getDomainSet();
    int num_elements = set.getLength(0);
    int num_lines = set.getLength(1);
    float[][] values = new float[num_lines][num_elements];
    double[][] vals = image.getValues();
    int base = 0;
    for (int l=0; l<num_lines; l++) {
      for (int e=0; e<num_elements; e++) {
        values[l][e] = (float) vals[0][base + e];
      }
      base += num_elements;
    }
    return values;
  }

  public static DisplayImpl makeSimpleDisplay(DataImpl data)
         throws VisADException, RemoteException {
    boolean three_d = true;
    DisplayImpl display = null;
    try {
      display = new DisplayImplJ3D("simple data display");
    }
    catch (UnsatisfiedLinkError e) {
      display = new DisplayImplJ2D("simple data display");
      three_d = false;
    }
    MathType type = data.getType();
    ScalarMap[] maps = type.guessMaps(three_d);
    if (maps == null) {
      display.stop();
      return null;
    }
    if (three_d) {
      boolean only_2d = true;
      for (int i=0; i<maps.length; i++) {
        DisplayRealType dtype = maps[i].getDisplayScalar();
        if (Display.ZAxis.equals(maps[i]) || 
            Display.Latitude.equals(maps[i])) {
          only_2d = false;
          break;
        }
      }
      if (only_2d) {
        display.stop();
        display = new DisplayImplJ3D("simple data display",
                                     new TwoDDisplayRendererJ3D());
      }
    }
    for (int i=0; i<maps.length; i++) {
      display.addMap(maps[i]);
    }

    DataReferenceImpl ref = new DataReferenceImpl("simple data display");
    ref.setData(data);
    display.addReference(ref);
    return display;
  }

  public static void main(String[] argv)
         throws VisADException, RemoteException {
    float[][] pixels = new float[64][64];
    for (int i=0; i<64; i++) {
      for (int j=0; j<64; j++) {
        pixels[i][j] = i * (i - 32) * (i - 64) *
                       j * (j - 32) * (j - 64) + 100000;
      }
    }

    FlatField image = DataUtility.makeImage(pixels);
    DisplayImpl display = DataUtility.makeSimpleDisplay(image);

    JFrame jframe = new JFrame("SimplImage.main");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    jframe.setContentPane((JPanel) display.getComponent());
    jframe.pack();
    jframe.setVisible(true);
  }

}

