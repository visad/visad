import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test49
	extends TestSkeleton
{
  public Test49() { }

  public Test49(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType count = new RealType("count", null, null);
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);

    int size = 64;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    display1.addMap(new ScalarMap(count, Display.YAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.XAxis));

    display1.addMap(new ConstantMap(0.0, Display.Red));
    display1.addMap(new ConstantMap(1.0, Display.Green));
    display1.addMap(new ConstantMap(0.0, Display.Blue));

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    display1.addReference(ref_histogram1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString()
  {
    return ": test 1-D line and ConstantMap colors";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test49 t = new Test49(args);
  }
}
