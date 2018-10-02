/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.Naming;
import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DirectManipulationRendererJ2D;
import visad.java2d.DisplayImplJ2D;

public class Test55
  extends UISkeleton
{
  boolean hasClientServerMode() { return false; }

  public Test55() { }

  public Test55(String[] args)
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
    RealType vis_radiance = RealType.getRealType("vis_radiance");

    try {

      int size = 64;
      FlatField histogram1;
      histogram1 = FlatField.makeField(ir_histogram, size, false);
      Real direct = new Real(ir_radiance, 2.0);
      Real[] reals14 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                       new Real(vis_radiance, 1.0)};
      RealTuple direct_tuple = new RealTuple(reals14);

      dpys[0].addMap(new ScalarMap(ir_radiance, Display.XAxis));
      dpys[0].addMap(new ScalarMap(count, Display.YAxis));

      GraphicsModeControl mode = dpys[0].getGraphicsModeControl();
      mode.setPointSize(5.0f);
      mode.setPointMode(false);

      DataReferenceImpl ref_direct = new DataReferenceImpl("ref_direct");
      ref_direct.setData(direct);
      DataReference[] refs141 = {ref_direct};
      dpys[0].addReferences(new DirectManipulationRendererJ2D(), refs141, null);

      DataReferenceImpl ref_direct_tuple;
      ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
      ref_direct_tuple.setData(direct_tuple);
      DataReference[] refs142 = {ref_direct_tuple};
      dpys[0].addReferences(new DirectManipulationRendererJ2D(), refs142, null);

      DataReferenceImpl ref_histogram1;
      ref_histogram1 = new DataReferenceImpl("ref_histogram1");
      ref_histogram1.setData(histogram1);
      DataReference[] refs143 = {ref_histogram1};
      dpys[0].addReferences(new DirectManipulationRendererJ2D(), refs143, null);

      // create local DataReferenceImpls
      DataReferenceImpl[] data_refs = new DataReferenceImpl[3];
      data_refs[0] = ref_histogram1;
      data_refs[1] = ref_direct;
      data_refs[2] = ref_direct_tuple;

      // create RemoteDataReferences
      RemoteDataReferenceImpl[] rem_data_refs;
      rem_data_refs = new RemoteDataReferenceImpl[3];
      rem_data_refs[0] = new RemoteDataReferenceImpl(data_refs[0]);
      rem_data_refs[1] = new RemoteDataReferenceImpl(data_refs[1]);
      rem_data_refs[2] = new RemoteDataReferenceImpl(data_refs[2]);

      RemoteServerImpl obj = new RemoteServerImpl(rem_data_refs);
      Naming.rebind("///RemoteServerTest", obj);

      System.out.println("RemoteServer bound in registry");
    }
    catch (Exception e) {
      System.out.println("\n\nDid you run 'rmiregistry &' first?\n\n");
      System.out.println("collaboration server exception: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  public String toString()
  {
    return ": collaborative direct manipulation server in Java2D" +
                "\n\trun rmiregistry first" +
                "\n\tany number of clients may connect";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test55(args);
  }
}
