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

import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.DisplayImplJ3D;

public class Test13
  extends TestSkeleton
{
  public Test13() { }

  public Test13(String[] args)
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
    RealType vis_radiance = RealType.getRealType("vis_radiance");

    int size = 64;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);
    Real direct = new Real(ir_radiance, 2.0);
    Real[] realsx3 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                      new Real(vis_radiance, 1.0)};
    RealTuple direct_tuple = new RealTuple(realsx3);

    // these ScalarMap should generate 3 Exceptions
    dpys[0].addMap(new ScalarMap(vis_radiance, Display.XAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.RGB));
    dpys[0].addMap(new ScalarMap(count, Display.Animation));

    DataReferenceImpl ref_direct = new DataReferenceImpl("ref_direct");
    ref_direct.setData(direct);
    DataReference[] refsx1 = {ref_direct};
    dpys[0].addReferences(new DirectManipulationRendererJ3D(), refsx1, null);

    DataReferenceImpl ref_direct_tuple;
    ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
    ref_direct_tuple.setData(direct_tuple);
    DataReference[] refsx2 = {ref_direct_tuple};
    dpys[0].addReferences(new DirectManipulationRendererJ3D(), refsx2, null);

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    DataReference[] refsx3 = {ref_histogram1};
    dpys[0].addReferences(new DirectManipulationRendererJ3D(), refsx3, null);
  }

  public String toString() { return ": Exception display"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test13(args);
  }
}
