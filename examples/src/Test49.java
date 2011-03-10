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

public class Test49
  extends TestSkeleton
{
  public Test49() { }

  public Test49(String[] args)
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

    float[][] values = histogram1.getFloats();
    for (int i=0; i<values[0].length; i+=13) values[0][i] = Float.NaN;
    histogram1.setSamples(values);

    dpys[0].addMap(new ScalarMap(count, Display.YAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.XAxis));

    dpys[0].addMap(new ConstantMap(0.0, Display.Red));
    dpys[0].addMap(new ConstantMap(1.0, Display.Green));
    dpys[0].addMap(new ConstantMap(0.0, Display.Blue));

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    dpys[0].addReference(ref_histogram1, null);
  }

  public String toString()
  {
    return ": 1-D line w/missing and ConstantMap colors";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test49(args);
  }
}
