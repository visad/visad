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

public class Test67
  extends UISkeleton
{
  private int dim;

  public Test67() { }

  public Test67(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public void initializeArgs() { dim = 1; }

  public int checkKeyword(String testName, int argc, String[] args)
  {
    int d = 0;
    try {
      d = Integer.parseInt(args[argc]);
    }
    catch (NumberFormatException exc) { }

    if (d < 1 || d > 3) {
      System.err.println(testName + ": Bad parameter \"" + args[argc] +
        "\": dimension must be 1, 2 or 3");
      return -1;
    }

    dim = d;
    return 1;
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[2];
    dpys[0] = new DisplayImplJ3D("double");
    dpys[1] = new DisplayImplJ3D("float");
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    DataReferenceImpl ref = new DataReferenceImpl("ref");
    DataReferenceImpl f_ref = new DataReferenceImpl("ref");
    RealType x = RealType.getRealType("x");
    RealType y = RealType.getRealType("y");
    RealType z = RealType.getRealType("z");
    RealType v = RealType.getRealType("v");
    FunctionType function;
    GriddedSet set, f_set;
    int size = 20;
    int nrs = (int) Math.pow(size, dim);
    double eps = 1.0 / size;

    // compute samples
    double[][] samples = new double[dim][nrs];
    float[][] f_samples = new float[dim][nrs];
    for (int j=0; j<dim; j++) {
      for (int i=0; i<nrs; i++) {
        int element = i;
        for (int k=0; k<j; k++) element /= size;
        element %= size;
        double frac = (double) element / size;
        samples[j][i] = frac * 2 * Math.PI + eps * (Math.random() - 0.5);
        f_samples[j][i] = (float) samples[j][i];
      }
    }

    // compute field values
    double[][] values = new double[1][nrs];
    float[][] f_values = new float[1][nrs];
    for (int i=0; i<nrs; i++) {
      double sum = 0.0;
      for (int j=0; j<dim; j++) sum += samples[j][i];
      values[0][i] = Math.sin(sum);
      f_values[0][i] = (float) values[0][i];
    }


    if (dim == 1) {
      function = new FunctionType(x, y);
      set = new Gridded1DDoubleSet(x, samples, size);
      f_set = new Gridded1DSet(x, f_samples, size);
    }
    else if (dim == 2) {
      RealTupleType xy = new RealTupleType(x, y);
      function = new FunctionType(xy, z);
      set = new Gridded2DDoubleSet(xy, samples, size, size);
      f_set = new Gridded2DSet(xy, f_samples, size, size);
    }
    else { // dim == 3
      RealTupleType xyz = new RealTupleType(x, y, z);
      function = new FunctionType(xyz, v);
      set = new Gridded3DDoubleSet(xyz, samples, size, size, size);
      f_set = new Gridded3DSet(xyz, f_samples, size, size, size);
    }

    FlatField field = new FlatField(function, set);
    field.setSamples(values);
    ref.setData(field);

    FlatField f_field = new FlatField(function, f_set);
    f_field.setSamples(f_values);
    f_ref.setData(f_field);

    for (int i=0; i<2; i++) {
      dpys[i].addMap(new ScalarMap(x, Display.XAxis));
      dpys[i].addMap(new ScalarMap(y, Display.YAxis));
      if (dim > 1) dpys[i].addMap(new ScalarMap(z, Display.ZAxis));
      if (dim > 2) dpys[i].addMap(new ScalarMap(v, Display.RGB));
    }
    if (dim < 3) {
      dpys[0].addMap(new ConstantMap(0.0, Display.Red));
      dpys[0].addMap(new ConstantMap(0.0, Display.Green));
      dpys[0].addMap(new ConstantMap(1.0, Display.Blue));
      dpys[1].addMap(new ConstantMap(1.0, Display.Red));
      dpys[1].addMap(new ConstantMap(0.0, Display.Green));
      dpys[1].addMap(new ConstantMap(0.0, Display.Blue));
    }
    else {
      dpys[0].getGraphicsModeControl().setPointSize(2.0f);
      dpys[1].getGraphicsModeControl().setPointSize(2.0f);
    }
    dpys[0].addReference(ref);
    dpys[1].addReference(f_ref);
  }

  String getFrameTitle() { return "Gridded" + dim + "DDoubleSet"; }

  public String toString()
  {
    return " dim: GriddedDoubleSets";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test67(args);
  }
}
