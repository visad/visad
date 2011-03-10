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

import visad.java3d.DisplayImplJ3D;

public class Test54
  extends TestSkeleton
{
  public Test54() { }

  public Test54(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = new DisplayImplJ3D("display", DisplayImplJ3D.APPLETFRAME);
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

    dpys[0].getDisplayRenderer().setBackgroundColor(1.0f, 0.0f, 1.0f);

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    dpys[0].addReference(ref_histogram1, null);

    boolean forever = true;
    boolean[] box_on = {true, true, true, false};
    float[][] box_color = {{1.0f, 0.0f, 0.0f},
                           {0.0f, 1.0f, 0.0f},
                           {0.0f, 0.0f, 1.0f},
                           {0.5f, 0.5f, 0.5f}};
    float[][] cursor_color = {{0.5f, 0.5f, 0.5f},
                              {1.0f, 0.0f, 0.0f},
                              {0.0f, 1.0f, 0.0f},
                              {0.0f, 0.0f, 1.0f}};
    DisplayRenderer displayRenderer = dpys[0].getDisplayRenderer();
    int index = 0;
    while (forever) {
      // delay(5000);
      try {
        Thread.sleep(5000);
      }
      catch (InterruptedException e) {
      }
      System.out.println("\ndelay\n");
      displayRenderer.setBoxOn(box_on[index]);
      displayRenderer.setBoxColor(box_color[index][0],
                                  box_color[index][1],
                                  box_color[index][2]);
      displayRenderer.setCursorColor(cursor_color[index][0],
                                     cursor_color[index][1],
                                     cursor_color[index][2]);
      index++;
      if (index > 3) index = 0;
    }
  }

  String getFrameTitle() { return "background color in Java3D"; }

  public String toString() { return ": background color in Java3D"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test54(args);
  }
}
