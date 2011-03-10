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

import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;

public class Test53
  extends UISkeleton
{
  private boolean isServer = false;

  public Test53() { }

  public Test53(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = new DisplayImplJ2D("display");
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType count = RealType.getRealType("count");
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);

    int size = 64;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);

    dpys[0].addMap(new ScalarMap(count, Display.YAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.XAxis));

    dpys[0].addMap(new ConstantMap(0.0, Display.Red));
    dpys[0].addMap(new ConstantMap(1.0, Display.Green));
    dpys[0].addMap(new ConstantMap(0.0, Display.Blue));

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    dpys[0].addReference(ref_histogram1, null);

    isServer = true;
  }

  private static String colorName(float[] color)
  {
    int red = (int )(256.0f * color[0]) / 64;
    int green = (int )(256.0f * color[1]) / 64;
    int blue = (int )(256.0f * color[2]) / 64;

    if (red == green && green == blue) {
      switch (red) {
      case 0: return "black";
      case 1: return "dark gray";
      case 2: return "light gray";
      default: return "white";
      }
    }
    if (red > 0) {
      if (green > 0) {
        if (blue > 0) {
        } else {
          return "yellow";
        }
      } else {                    // green == 0
        if (blue > 0) {   // green == 0
          return "magenta";
        } else {
          return "red";
        }
      }
    } else {                             // red == 0
      if (green > 0) {
        if (blue > 0) {
          return "cyan";
        } else {
          return "green";
        }
      } else {
        if (blue > 0) {
          return "blue";
        }
      }
    }

    return "color[" + color[0] + "/" + color[1] + "/" + color[2] + "]";
  }

  void setupUI(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    super.setupUI(dpys);

    float[][] bg_color = {{1.0f, 0.0f, 1.0f},
                          {1.0f, 1.0f, 0.0f},
                          {0.0f, 1.0f, 1.0f}};
    float[][] box_color = {{1.0f, 0.0f, 0.0f},
                           {0.0f, 1.0f, 0.0f},
                           {0.0f, 0.0f, 1.0f},
                           {0.5f, 0.5f, 0.5f}};
    float[][] cursor_color = {{0.5f, 0.5f, 0.5f},
                              {1.0f, 0.0f, 0.0f},
                              {0.0f, 1.0f, 0.0f},
                              {0.0f, 0.0f, 1.0f}};
    DisplayRenderer displayRenderer = dpys[0].getDisplayRenderer();
    int i3 = 0;
    int i4 = 0;
    while (isServer) {
      // delay(5000);
      try {
        Thread.sleep(5000);
      }
      catch (InterruptedException e) {
      }

      boolean boxOn = (i3 != 2);
//      System.out.println("\ndelay\n");
      System.out.println("\n" +
                         colorName(bg_color[i3]) + " background, " +
                         (boxOn ? colorName(box_color[i4]) + " box" :
                          "box off") + ", " +
                         colorName(cursor_color[i4]) + " cursor\n");
      displayRenderer.setBackgroundColor(bg_color[i3][0],
                                         bg_color[i3][1],
                                         bg_color[i3][2]);
      displayRenderer.setBoxOn(boxOn);
      displayRenderer.setBoxColor(box_color[i4][0],
                                  box_color[i4][1],
                                  box_color[i4][2]);
      displayRenderer.setCursorColor(cursor_color[i4][0],
                                     cursor_color[i4][1],
                                     cursor_color[i4][2]);
      if (i3 == 2) {
        i3 = 0;
      } else {
        i3++;
      }
      if (i4 == 3) {
        i4 = 0;
      } else {
        i4++;
      }
    }
  }

  String getFrameTitle() { return "background color in Java2D"; }

  public String toString() { return ": background color in Java2D"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test53(args);
  }
}
