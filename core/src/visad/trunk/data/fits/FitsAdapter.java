//
// FitsAdapter.java
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

package visad.data.fits;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.PrimaryHDU;

import java.io.IOException;

import java.lang.reflect.Array;

import java.net.URL;

import java.rmi.RemoteException;

import java.util.Vector;

import visad.Data;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.Integer2DSet;
import visad.IntegerNDSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.SetType;
import visad.TypeException;
import visad.VisADException;

public class FitsAdapter
{
  Fits fits;
  Data data[];
  ExceptionStack stack;

  public FitsAdapter(String filename)
	throws VisADException
  {
    try {
      fits = new Fits(filename);
    } catch (FitsException e) {
      throw new VisADException(e.getClass().getName() + "(" + e.getMessage() +
			       ")");
    }

    data = null;
    stack = null;
  }

  public FitsAdapter(URL url)
	throws VisADException
  {
    try {
      fits = new Fits(url);
    } catch (FitsException e) {
      throw new VisADException(e.getClass().getName() + "(" + e.getMessage() +
			       ")");
    }

    data = null;
    stack = null;
  }

  private int get1DLength(Object data)
	throws VisADException
  {
    if (!data.getClass().isArray()) {
      return 1;
    }

    int len = Array.getLength(data);

    int total = 0;
    for (int i = 0; i < len; i++) {
      total += get1DLength(Array.get(data, i));
    }

    return total;
  }

  private int copyArray(Object data, double[] list, int offset)
	throws VisADException
  {
    if (data instanceof byte[]) {
      byte[] bl = (byte[] )data;
      for (int i = 0; i < bl.length; ) {
	list[offset++] = (double )bl[i++];
      }
    } else if (data instanceof short[]) {
      short[] sl = (short[] )data;
      for (int i = 0; i < sl.length; ) {
	list[offset++] = (double )sl[i++];
      }
    } else if (data instanceof int[]) {
      int[] il = (int[] )data;
      for (int i = 0; i < il.length; ) {
	list[offset++] = (double )il[i++];
      }
    } else if (data instanceof long[]) {
      long[] ll = (long[] )data;
      for (int i = 0; i < ll.length; ) {
	list[offset++] = (double )ll[i++];
      }
    } else if (data instanceof float[]) {
      float[] fl = (float[] )data;
      for (int i = 0; i < fl.length; ) {
	list[offset++] = (double )fl[i++];
      }
    } else if (data instanceof double[]) {
      double[] dl = (double[] )data;
      for (int i = 0; i < dl.length; ) {
	list[offset++] = dl[i++];
      }
    } else {
      throw new VisADException("type '" + data.getClass().getName() +
			       "' not handled");
    }

    return offset;
  }

  private int decompose(Object data, double[] list, int offset)
	throws VisADException
  {
    Class component = data.getClass().getComponentType();
    if (component == null) {
      return offset;
    }

    if (!component.isArray()) {
      return copyArray(data, list, offset);
    }

    int len = Array.getLength(data);
    for (int i = 0; i < len; i++) {
      offset = decompose(Array.get(data, i), list, offset);
    }

    return offset;
  }

  private double[][] buildRange(Object data)
	throws VisADException
  {
    int len = get1DLength(data);

    double[] values = new double[len];

    int offset = decompose(data, values, 0);
    while (offset < len) {
      values[offset++] = Double.NaN;
    }

    double[][] range = new double[1][];
    range[0] = values;

    return range;
  }

  private Data addPrimary(PrimaryHDU hdu)
	throws FitsException, VisADException, RemoteException
  {
    int[] axes = hdu.getAxes();
    if (axes == null || axes.length == 0) {
      return null;
    }

    Object fData = hdu.getData().getData();
    if (fData == null) {
      throw new VisADException("No HDU Data");
    }
    if (!fData.getClass().isArray()) {
      throw new VisADException("Unknown HDU Data type: " +
			       fData.getClass().getName());
    }

    RealType axisType[] = new RealType[axes.length];
    for (int i = 0; i < axisType.length; i++) {
      String name = "NAxis" + (i+1);

      try {
	axisType[i] = new RealType(name, null, null);
      } catch (TypeException e) {
	axisType[i] = RealType.getRealTypeByName(name);
      }
    }

    RealTupleType type = new RealTupleType(axisType);;

    RealType value;
    try {
      value = new RealType("value", null, null);
    } catch (TypeException e) {
      value = RealType.getRealTypeByName("value");
    }

    FunctionType func = new FunctionType(type, value);

    IntegerNDSet iSet = new IntegerNDSet(type, axes);

    FlatField fld = new FlatField(func, iSet);

    fld.setSamples(buildRange(fData));

    return fld;
  }

  private Data addImage(ImageHDU hdu)
	throws VisADException, RemoteException
  {
    int[] axes;
    try {
      axes = hdu.getAxes();
    } catch (FitsException e) {
      axes = null;
    }

    if (axes == null) {
      throw new VisADException("Couldn't get image axes");
    }
    if (axes.length != 2) {
      throw new VisADException("Not a two-dimensional image");
    }

    Object fData = hdu.getData().getData();
    if (fData == null) {
      throw new VisADException("No HDU Data");
    }
    if (!fData.getClass().isArray()) {
      throw new VisADException("Unknown HDU Data type: " +
			       fData.getClass().getName());
    }

    RealTupleType type = RealTupleType.SpatialCartesian2DTuple;

    RealType pixel;
    try {
      pixel = new RealType("pixel", null, null);
    } catch (TypeException e) {
      pixel = RealType.getRealTypeByName("pixel");
    }

    FunctionType func = new FunctionType(type, pixel);

    Integer2DSet iSet = new Integer2DSet(type, axes[0], axes[1]);

    FlatField fld = new FlatField(func, iSet);

    fld.setSamples(buildRange(fData));

    return fld;
  }

  private double[][] buildBTRange(BinaryTableHDU hdu)
	throws VisADException
  {
    int rows = hdu.getNumRows();
    int cols = hdu.getNumColumns();

    double[][] d = new double[cols][rows];
    for (int i = 0; i < cols; i++) {

      Object list;
      try {
	list = hdu.getVarData(i);
      } catch (FitsException e) {
	throw new VisADException("Failed to get column " + i + " type: " +
				 e.getMessage());
      }

      int len;
      if (list instanceof byte[]) {
	len = copyArray((byte[] )list, d[i], 0);
      } else if (list instanceof short[]) {
	len = copyArray((short[] )list, d[i], 0);
      } else if (list instanceof int[]) {
	len = copyArray((int[] )list, d[i], 0);
      } else if (list instanceof long[]) {
	len = copyArray((long[] )list, d[i], 0);
      } else if (list instanceof float[]) {
	len = copyArray((float[] )list, d[i], 0);
      } else if (list instanceof double[]) {
	len = copyArray((double[] )list, d[i], 0);
      } else {
System.err.println("Faking values for column #" + i + " (" + list.getClass().getName() + ")");
	// fill with NaN
	int c = i;
	for (len = 0 ; len < rows; len++) {
	  d[c][len] = Double.NaN;
	}
      }

      if (len < rows) {
	int c = i;
	System.err.println("Column " + i + " was short " + (rows - len) +
			   " of " + rows + " rows");
	while (len < rows) {
	  d[c][len++] = Double.NaN;
	}
      }
    }
    return d;
  }

  private Data addBinaryTable(BinaryTableHDU hdu)
	throws FitsException, VisADException, RemoteException
  {
    int[] axes = hdu.getAxes();
    if (axes == null) {
      throw new FitsException("Couldn't get binary table axes");
    }
    if (axes.length != 2) {
      throw new FitsException("Not a two-dimensional binary table");
    }

    int numColumns = hdu.getNumColumns();

    RealType index;
    try {
      index = new RealType("index", null, null);
    } catch (TypeException e) {
      index = RealType.getRealTypeByName("index");
    }

    RealType rowType[] = new RealType[numColumns];
    for (int i = 0; i < numColumns; i++) {
      String name = hdu.getColumnName(i);
      if (name == null) {
	name = "Column" + i;
      }

// XXX -- need to check datatypes here...

      try {
	rowType[i] = new RealType(name, null, null);
      } catch (TypeException e) {
	rowType[i] = RealType.getRealTypeByName(name);
      }
    }

    RealTupleType row = new RealTupleType(rowType);;

    FunctionType func = new FunctionType(index, row);

    Integer1DSet iSet = new Integer1DSet(hdu.getNumRows());

    FlatField fld = new FlatField(func, iSet);

System.out.println("BinaryTable.getType().toString()\n" + fld.getType());
System.out.println("BinaryTable field\n" + fld.longString());

    fld.setSamples(buildBTRange(hdu));

    return fld;
  }

  private Data convertHDU(BasicHDU hdu)
	throws FitsException, VisADException, RemoteException
  {
    if (hdu instanceof ImageHDU) {
      return addImage((ImageHDU )hdu);
    }

    if (hdu instanceof PrimaryHDU) {
      return addPrimary((PrimaryHDU )hdu);
    }

    if (hdu instanceof BinaryTableHDU) {
      return addBinaryTable((BinaryTableHDU )hdu);
    }

    return null;
  }

  public void buildData()
  {
    Vector vec = new Vector();

    for (int n = 0; true; n++) {
      try {
	BasicHDU hdu = fits.getHDU(n);
	if (hdu == null) {
	  break;
	}

	Data d = convertHDU(hdu);
	if (d != null) {
	  vec.addElement(d);
	}
      } catch (Exception e) {
	if (stack == null) {
	  stack = new ExceptionStack(e);
	} else {
	  stack.addException(e);
	}
      }
    }

    data = new Data[vec.size()];
    for (int i = 0; i < data.length; i++) {
      data[i] = (Data )vec.elementAt(i);
    }
  }

  public void clearExceptionStack()
  {
    stack = null;
  }

  Data[] getData()
	throws ExceptionStack, RemoteException, VisADException
  {
    if (data == null) {
      buildData();
      if (data == null) {
	throw new VisADException("No data");
      }
    }

    if (stack != null) {
      throw stack;
    }

    return data;
  }
}
