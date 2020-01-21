//
// FitsAdapter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.fits;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.PrimaryHDU;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;

public class FitsAdapter
{
  Fits fits;
  Data data[];
  ExceptionStack stack;

  public FitsAdapter()
	throws VisADException
  {
    fits = null;
    data = null;
    stack = null;
  }

  public FitsAdapter(String filename)
	throws VisADException
  {
    this();

    try {
      fits = new Fits(filename);
    } catch (FitsException e) {
      throw new VisADException(e.getClass().getName() + "(" + e.getMessage() +
			       ")");
    }
  }

  public FitsAdapter(URL url)
	throws VisADException
  {
    this();

    try {
      fits = new Fits(url);
    } catch (FitsException e) {
      throw new VisADException(e.getClass().getName() + "(" + e.getMessage() +
			       ")");
    }
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
      for (int i = 0; i < bl.length; i++) {
	int val = (bl[i] >= 0 ? bl[i] :
                   (((int )Byte.MAX_VALUE + 1) * 2 + (int )bl[i]));
	list[offset++] = (double )val;
      }
    } else if (data instanceof short[]) {
      short[] sl = (short[] )data;
      for (int i = 0; i < sl.length; i++) {
	int val = (sl[i] >= 0 ? sl[i] : ((Short.MAX_VALUE + 1) * 2) - sl[i]);
	list[offset++] = (double )val;
      }
    } else if (data instanceof int[]) {
      int[] il = (int[] )data;
      for (int i = 0; i < il.length; i++) {
	list[offset++] = (double )il[i];
      }
    } else if (data instanceof long[]) {
      long[] ll = (long[] )data;
      for (int i = 0; i < ll.length; i++) {
	list[offset++] = (double )ll[i];
      }
    } else if (data instanceof float[]) {
      float[] fl = (float[] )data;
      for (int i = 0; i < fl.length; i++) {
	list[offset++] = (double )fl[i];
      }
    } else if (data instanceof double[]) {
      double[] dl = (double[] )data;
      for (int i = 0; i < dl.length; i++) {
	list[offset++] = dl[i];
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
    for (int i = len - 1; i >= 0; i--) {
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

    // reverse order of axes
    for (int i = 0; i < axes.length / 2; i++) {
      int j = axes.length - (i + 1);
      int tmp = axes[j];

      axes[j] = axes[i];
      axes[i] = tmp;
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

      axisType[i] = RealType.getRealType(name, null, null);
    }

    RealTupleType type = new RealTupleType(axisType);;

    RealType value = RealType.getRealType("value", null, null);

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
      throw new VisADException("Expected two-dimensional image, not " +
                               axes.length +" dimensions");
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

    RealType pixel = RealType.getRealType("pixel", null, null);

    FunctionType func = new FunctionType(type, pixel);

    Integer2DSet iSet = new Integer2DSet(type, axes[0], axes[1]);

    FlatField fld = new FlatField(func, iSet);

    fld.setSamples(buildRange(fData));

    return fld;
  }

  private int copyColumn(Object data, double[] list, int offset)
	throws VisADException
  {
    // punt if this isn't a 1D column
    Object[] top = (Object[] )data;
    if (top.length != 1 && !(top[0] instanceof byte[])) {
      System.err.println("FitsAdapter.copyColumn: Punting on wide column (" +
			 top[0].getClass().getName() + ")");
      return offset;
    }

    if (top[0] instanceof byte[]) {
      if (top.length != 1) {
	System.err.println("Ignoring assumed " + top.length +
			   "-char String column");
	return offset;
      } else {
	byte[] bl = (byte[] )top[0];
	for (int i = 0; i < bl.length; ) {
	  list[offset++] = (double )bl[i++];
	}
      }
    } else if (top[0] instanceof short[]) {
      short[] sl = (short[] )top[0];
      for (int i = 0; i < sl.length; ) {
	list[offset++] = (double )sl[i++];
      }
    } else if (top[0] instanceof int[]) {
      int[] il = (int[] )top[0];
      for (int i = 0; i < il.length; ) {
	list[offset++] = (double )il[i++];
      }
    } else if (top[0] instanceof long[]) {
      long[] ll = (long[] )top[0];
      for (int i = 0; i < ll.length; ) {
	list[offset++] = (double )ll[i++];
      }
    } else if (top[0] instanceof float[]) {
      float[] fl = (float[] )top[0];
      for (int i = 0; i < fl.length; ) {
	list[offset++] = (double )fl[i++];
      }
    } else if (top[0] instanceof double[]) {
      double[] dl = (double[] )top[0];
      for (int i = 0; i < dl.length; ) {
	list[offset++] = dl[i++];
      }
    } else {
      throw new VisADException("type '" + top[0].getClass().getName() +
			       "' not handled");
    }

    return offset;
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
	list = hdu.getColumn(i).getData();
      } catch (FitsException e) {
	throw new VisADException("Failed to get column " + i + " type: " +
				 e.getMessage());
      }

      int len;
      if (list instanceof byte[][]) {
	len = copyColumn((byte[][] )list, d[i], 0);
      } else if (list instanceof short[][]) {
	len = copyColumn((short[][] )list, d[i], 0);
      } else if (list instanceof int[][]) {
	len = copyColumn((int[][] )list, d[i], 0);
      } else if (list instanceof long[][]) {
	len = copyColumn((long[][] )list, d[i], 0);
      } else if (list instanceof float[][]) {
	len = copyColumn((float[][] )list, d[i], 0);
      } else if (list instanceof double[][]) {
	len = copyColumn((double[][] )list, d[i], 0);
      } else {
	String type;
	try {
	  type = hdu.getColumnFITSType(i);
	} catch (FitsException e) {
	  type = "?Unknown FITS type?";
	}
	System.err.println("FitsAdapter.buildBTRange: Faking values for" +
			   " column #" + i + " (" + type + "=>" +
			   list.getClass().getName() + ")");
	// fill with NaN
	int c = i;
	for (len = 0 ; len < rows; len++) {
	  d[c][len] = Double.NaN;
	}
      }

      if (len < rows) {
	int c = i;
	System.err.println("FitsAdapter.buildBTRange: Column " + i +
			   " was short " + (rows - len) + " of " + rows +
			   " rows");
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

    RealType index = RealType.getRealType("index", null, null);

    boolean hasTextColumn = false;

    RealType rowType[] = new RealType[numColumns];
    for (int i = 0; i < numColumns; i++) {
      String name = hdu.getColumnName(i);
      if (name == null) {
	name = "Column" + i;
      }

      String colType = hdu.getColumnFITSType(i);
      if (colType.startsWith("A") || colType.endsWith("A")) {
	hasTextColumn = true;
      }

      rowType[i] = RealType.getRealType(name, null, null);
    }

    RealTupleType row = new RealTupleType(rowType);;

    FunctionType func = new FunctionType(index, row);

    Integer1DSet iSet = new Integer1DSet(hdu.getNumRows());

    FlatField fld = new FlatField(func, iSet);

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

    int startDepth;
    if (stack == null) {
      startDepth = 0;
    } else {
      startDepth = stack.depth();
    }

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
	  if (stack.depth() > startDepth + 10) {
	    break;
	  }
	}
      }
    }

    if (vec.size() == 0) {
      data = null;
    } else {
      data = new Data[vec.size()];
      for (int i = 0; i < data.length; i++) {
	data[i] = (Data )vec.elementAt(i);
      }
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

  public void save(String name, Data data, boolean replace)
	throws IOException, RemoteException, VisADException
  {
    File file = new File(name);
    if (file.exists()) {
      throw new IllegalArgumentException("File \"" + name + "\" exists");
    }

    FitsTourGuide guide;

    // make sure this object can be saved as a FITS file
    TourInspector count = new TourInspector(replace);
    guide = new FitsTourGuide(data, count);
    count = null;

    // build the new FITS file
    Fits f = new Fits();
    TourWriter tw = new TourWriter(replace, f);
    guide = new FitsTourGuide(data, tw);
    tw = null;
    guide = null;

    // open the final destination
    BufferedOutputStream bos;
    bos = new BufferedOutputStream(new FileOutputStream(name));

    // write the FITS file
    try {
      f.write(bos);
    } catch (FitsException e) {
      throw new VisADException(e.getClass().getName() + "(" +
			       e.getMessage() + ")");
    }
    bos.close();
  }
}
