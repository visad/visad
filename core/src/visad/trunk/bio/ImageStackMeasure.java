//
// ImageStackMeasure.java
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
import visad.java2d.DisplayImplJ2D;

/**
 * ImageStackMeasure is a class for measuring the
 * distance between points in a stack of images.
 */
public class ImageStackMeasure {

  /** This measurement object's associated stack of images. */
  private FieldImpl imageStack;

  /** Domain set for image stack. */
  private GriddedSet gset;

  /** This measurement object's associated FieldMeasure object. */
  private FieldMeasure[] stack;

  /** Current step in image stack. */
  private int current;

  /** Associated VisAD Display, if any. */
  private DisplayImpl display;

  /** AVControl for determining step. */
  private AVControl avc;

  /** Constructs a measurement object to match the given image stack. */
  public ImageStackMeasure(FieldImpl imageStack)
    throws VisADException, RemoteException
  {
    FunctionType type = (FunctionType) imageStack.getType();
    RealTupleType domain = type.getDomain();
    if (domain.getDimension() > 1) {
      throw new VisADException("Field not an image stack");
    }
    Set set = imageStack.getDomainSet();
    if (!(set instanceof GriddedSet)) {
      throw new VisADException("Image stack not ordered");
    }
    this.imageStack = imageStack;
    gset = (GriddedSet) set;
    int[] lengths = gset.getLengths();
    int numImages = lengths[0];
    stack = new FieldMeasure[numImages];
    DataReferenceImpl ref_p1 = new DataReferenceImpl("p1");
    DataReferenceImpl ref_p2 = new DataReferenceImpl("p2");
    DataReferenceImpl ref_line = new DataReferenceImpl("line");
    boolean first = true;
    for (int i=0; i<numImages; i++) {
      Data data = imageStack.getSample(i);
      if (!(data instanceof FieldImpl)) {
        throw new VisADException("Data #" + i + " not a field");
      }
      FieldImpl image = (FieldImpl) data;
      FunctionType itype = (FunctionType) image.getType();
      if (itype.getDomain().getDimension() != 2) {
        throw new VisADException("Field #" + i + " not an image");
      }
      stack[i] = new FieldMeasure(image, ref_p1, ref_p2, ref_line, first);
      first = false;
    }
    stack[0].setActive(true);
  }

  /** Determines the current step and updates measuring data accordingly. */
  private void updateStep() throws VisADException, RemoteException {
    int step;
    if (avc instanceof ValueControl) {
      ValueControl vc = (ValueControl) avc;
      float[][] value = new float[1][1];
      value[0][0] = (float) vc.getValue();
      int[] index = gset.valueToIndex(value);
      step = index[0];
    }
    else { // avc instanceof AnimationControl
      AnimationControl ac = (AnimationControl) avc;
      step = ac.getCurrent();
    }
    if (display != null && current != step) {
      stack[current].setActive(false);
      current = step;
      stack[current].setActive(true);
    }
  }

  /** Adds the distance measuring data to the given display. */
  public void setDisplay(DisplayImpl d)
    throws VisADException, RemoteException
  {
    avc = (AVControl) d.getControl(AVControl.class);
    if (avc == null) {
      throw new VisADException("Display must have " +
        "mapping to Animation or SelectValue");
    }
    avc.addControlListener(new ControlListener() {
      public void controlChanged(ControlEvent e)
        throws VisADException, RemoteException
      {
        try {
          updateStep();
        }
        catch (VisADException exc) {
          exc.printStackTrace();
        }
        catch (RemoteException exc) {
          exc.printStackTrace();
        }
      }
    });
    if (display != null) stack[0].removeFromDisplay(display);
    stack[0].addToDisplay(d);
    current = 0;
    display = d;
  }

  /** Gets the index of the current slice. */
  public int getCurrent() {
    return current;
  }

  /** Gets the current distance between the endpoints for the given slice. */
  public double getDistance(int slice) {
    return stack[slice].getDistance();
  }

  /** Gets the current values of the endpoints for the given slice. */
  public double[][] getValues(int slice) {
    return stack[slice].getValues();
  }

  /** Gets the distances between endpoints as a string. */
  public String getDistanceString() {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<stack.length; i++) {
      double[][] vals = stack[i].getValues();
      double dist = stack[i].getDistance();
      for (int j=0; j<2; j++) {
        int klen = vals.length - 1;
        for (int k=0; k<klen; k++) {
          sb.append(vals[k][j]);
          sb.append(" ");
        }
        sb.append(vals[klen][j]);
        sb.append("\t");
      }
      sb.append(dist);
      sb.append("\n");
    }
    return sb.toString();
  }

  /** Tests the ImageStackMeasure class. */
  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Please specify an image stack on the command line.");
      System.exit(2);
    }
    DefaultFamily loader = new DefaultFamily("loader");
    FieldImpl field = (FieldImpl) loader.open(args[0]);
    DisplayImplJ2D display = new DisplayImplJ2D("display");
    ScalarMap[] maps = field.getType().guessMaps(false);
    for (int i=0; i<maps.length; i++) display.addMap(maps[i]);
    DataReferenceImpl ref = new DataReferenceImpl("ref");
    ref.setData(field);
    display.addReference(ref);
    ImageStackMeasure ism = new ImageStackMeasure(field);
    ism.setDisplay(display);
    JFrame frame = new JFrame("ImageStackMeasure");
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
