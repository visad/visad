/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Column;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.PrimaryHDU;

import nom.tam.util.ArrayFuncs;

import visad.FieldImpl;
import visad.FlatField;
import visad.Function;
import visad.FunctionType;
import visad.GriddedSet;
import visad.MathType;
import visad.RealTupleType;
import visad.Scalar;
import visad.ScalarType;
import visad.Set;
import visad.VisADException;
import visad.UnimplementedException;

public class TourWriter
	extends Tourist
{
  private Fits fits;

  public TourWriter(boolean replace, Fits fits)
  {
    super(replace);
    this.fits = fits;
  }

  private String[] getNames(RealTupleType rtt)
	throws VisADException
  {
    int dim = rtt.getDimension();
    if (dim == 0) {
      return null;
    }

    String[] list = new String[dim];
    for (int i = 0; i < dim; i++) {
      MathType type = rtt.getComponent(i);
      if (!(type instanceof ScalarType)) {
	throw new VisADException("Expected a ScalarType name, got " +
				 type.getClass().getName());
      }

      list[i] = ((ScalarType )type).getName();
    }

    return list;
  }

  private String[] getNames(ScalarType st)
	throws VisADException
  {
    String[] list = new String[1];
    list[0] = st.getName();
    return list;
  }

  private String[] getNames(MathType mt)
	throws VisADException
  {
    if (mt instanceof RealTupleType) {
      return getNames((RealTupleType )mt);
    }
    if (mt instanceof ScalarType) {
      return getNames((ScalarType )mt);
    }
    throw new VisADException("Couldn't get list of names from " +
			     mt.getClass().getName());
  }

  private void saveBinaryTable(FlatField fld, int domainDim, int rangeDim)
	throws VisADException
  {
    System.err.println("TourWriter.saveBinaryTable(" + domainDim + ", " + rangeDim + "):");

    FunctionType funcType = (FunctionType )fld.getType();

    String[] rangeNames = getNames(funcType.getRange());

    BinaryTableHDU hdu;
    try {
      hdu = new BinaryTableHDU();
    } catch (FitsException e) {
      throw new VisADException("Couldn't create BinaryTableHDU: " +
			       e.getMessage());
    }

    double[][] values = fld.getValues();
    System.err.println("\tvalues: " + values.length + "x" + values[0].length);

    double[][] column = new double[1][];
    int[] lengths = new int[2];

    Object[][] table = new Object[values.length][];

    lengths[0] = 1;
    for (int i = 0; i < table.length; i++) {
      column[0] = values[i];
      lengths[1] = column[0].length;

      ConvertDoubleArray cvtArray = new ConvertDoubleArray(lengths, column);
      Object o = cvtArray.getConverter().getRowMajor(column);
      if (o == null) {
	throw new VisADException("Couldn't extract array from column #" + i);
      }

      try {
	Column col = new Column();
	col.setData((Object[] )o);

	String num = "" + i;

	StringBuffer buf = new StringBuffer(8);

	if (rangeNames != null) {
	  buf.setLength(0);
	  buf.append("TTYPE");
	  buf.append(num);
	  buf.append("        ");
	  buf.setLength(8);
	  buf.append("= '");
	  buf.append(rangeNames[i]);
	  buf.append("'");
	  col.addKey(buf.toString());
	}

	hdu.addColumn(col);
      } catch (FitsException e) {
	System.err.println("Couldn't add binary table column #" + i + ": " +
			   e.getMessage());
      }
    }

    try {
      fits.addHDU(hdu);
    } catch (FitsException e) {
      throw new VisADException("Couldn't add FITS binary table HDU : " +
			       e.getMessage());
    }
  }

  private void saveImage(FlatField fld, int domainDim, int rangeDim)
	throws VisADException
  {
    Set set = fld.getDomainSet();

    if (!(set instanceof GriddedSet)) {
      throw new VisADException("Cannot build FITS Image" +
			       " from non-Gridded domain");
    }

    int size;
    try {
      size = fits.size();
    } catch (FitsException e) {
      System.err.println("TourWriter.saveImage: Yikes!  Fits.size() threw");
      e.printStackTrace(System.err);
      throw new VisADException("Couldn't get size of FITS file");
    }

    final int[] lengths = ((GriddedSet )set).getLengths();
    if (lengths.length != 2) {
      throw new VisADException("Don't know how to decipher " + lengths.length +
                               "-dimension FlatField!");
    }

    double[][] values = fld.getValues();
    if (values[0].length != lengths[0]*lengths[1]) {
      throw new VisADException("Mismatch between FlatField length array" +
                               " and value array length");
    } else if (values.length != 1 && values.length != 3) {
      throw new VisADException("Don't know how to decipher " + values.length +
                               "-dimension FlatField values!");
    }

    // create 8-bit color value array
    final int len = values[0].length;
    byte[] colorVals = new byte[len];

    // fill in 8-bit color values
    int colRow, valIndex;
    valIndex = 0;
    for (int i = lengths[1] - 1; i >= 0; i--) {
      colRow = i * lengths[0];
      for (int j = 0; j < lengths[0]; j++) {
        int v;
        if (values.length == 3) {
          // map to RGB
          v = (int )((0.299 * values[0][valIndex]) +
                     (0.587 * values[1][valIndex]) +
                     (0.114 * values[2][valIndex]));
        } else {
          v = (int )values[0][valIndex];
        }
        colorVals[colRow+j] = (byte )v;

        valIndex++;
      }
    }

    byte[][] image = (byte[][] )ArrayFuncs.curl(colorVals, lengths);

    try {
      BasicHDU hdu;
      if (size == 0) {
	hdu = new PrimaryHDU((Object )image);
      } else {
	hdu = new ImageHDU((Object )image);
      }

      fits.addHDU(hdu);
    } catch (FitsException e) {
      throw new VisADException("Couldn't build " +
			       (size == 0 ? "primary" : "image") +
			       " FITS HDU : " + e.getMessage());
    }
  }

  private void save(FlatField fld)
	throws RemoteException, VisADException
  {
    MathType type = fld.getType();
    if (!(type instanceof FunctionType)) {
      throw new VisADException("Confused Data object" +
			       " (FlatField with non-FunctionType)");
    }

    int domainDim = fld.getDomainSet().getDimension();
    if (domainDim > 2) {
      throw new VisADException("Can't write FITS file with" +
			       " domain dimension of " + domainDim);
    }

    int rangeDim = fld.getRangeDimension();
    if (rangeDim != 1 && rangeDim != 3) {
      throw new VisADException("Can't write FITS file with" +
			       " range dimension of " + rangeDim);
    }

    if (domainDim != 2) {
      saveBinaryTable(fld, domainDim, rangeDim);
    } else {
      saveImage(fld, domainDim, rangeDim);
    }
  }

  private void save(FieldImpl fld)
	throws RemoteException, VisADException
  {
    if (fld instanceof FlatField) {
      save((FlatField )fld);
      return;
    }

    throw new UnimplementedException("Can only save FlatField data");
  }

  public boolean visit(Function func, int depth)
	throws RemoteException, VisADException
  {

    if (!(func instanceof FieldImpl)) {
      throw new UnimplementedException("Can only save FieldImpl data");
    }

    save((FieldImpl )func);
    return true;
  }

  public boolean visit(Scalar scalar, int depth)
	throws VisADException
  {
    return false;
  }

  public boolean visit(Set set, int depth)
	throws VisADException
  {
    throw new UnimplementedException("Cannot write Set data to FITS files yet");
  }
}
