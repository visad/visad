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

import java.rmi.RemoteException;

import visad.*;
import java.awt.*;
import org.jogamp.java3d.*;

import visad.java3d.DisplayImplJ3D;

public class TestStereo
  extends TestSkeleton
{
  public TestStereo() { }

  public TestStereo(String args[])
    throws RemoteException, VisADException
  {
    super(args);
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    GraphicsEnvironment ge =
      GraphicsEnvironment.getLocalGraphicsEnvironment();

    GraphicsDevice gd = ge.getDefaultScreenDevice();

    GraphicsConfigTemplate3D gct3d = new GraphicsConfigTemplate3D();
    gct3d.setStereo(GraphicsConfigTemplate3D.REQUIRED);

    GraphicsConfiguration config =
    gct3d.getBestConfiguration(gd.getConfigurations());

    if (config == null)
    {
        System.err.println("Unable to find a Stereo visual");
        System.exit(1);
    }

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME,
                                 config);
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    RealType vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType count = RealType.getRealType("count");
    RealType[] scatter_list = {vis_radiance, ir_radiance, count, RealType.Latitude,
                               RealType.Longitude, RealType.Radius};
    RealTupleType scatter = new RealTupleType(scatter_list);
    RealType[] time = {RealType.Time};
    RealTupleType time_type = new RealTupleType(time);
    FunctionType scatter_function = new FunctionType(time_type, scatter);

    int size = 64;

    FlatField imaget1;
    imaget1 = FlatField.makeField(scatter_function, size, false);

    dpys[0].addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    dpys[0].addMap(new ScalarMap(RealType.Longitude, Display.Green));
    dpys[0].addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    dpys[0].addMap(new ScalarMap(ir_radiance, Display.XAxis));
    dpys[0].addMap(new ConstantMap(0.5, Display.Blue));
    dpys[0].addMap(new ConstantMap(0.5, Display.Red));

    // WLH 28 April 99 - test alpha with points
    dpys[0].addMap(new ScalarMap(vis_radiance, Display.Alpha));

    GraphicsModeControl mode = dpys[0].getGraphicsModeControl();
    mode.setPointSize(5.0f);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    dpys[0].addReference(ref_imaget1, null);
  }

  public String toString() { return ": stereo scatter diagram"; }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new TestStereo(args);
  }
}

