package visad.data.fits;

import java.rmi.RemoteException;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Column;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.PrimaryHDU;

import visad.CoordinateSystem;
import visad.Data;
import visad.FieldImpl;
import visad.FlatField;
import visad.Function;
import visad.FunctionType;
import visad.GriddedSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.Scalar;
import visad.ScalarType;
import visad.Set;
import visad.Tuple;
import visad.VisADException;
import visad.UnimplementedException;
import visad.Unit;

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

  private void saveImage(FlatField fld, int domainDim)
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

    ConvertDoubleArray cvtArray = new ConvertDoubleArray(fld);
    Object o = cvtArray.getConverter().getRowMajor(fld.getValues());
    if (o == null) {
      throw new VisADException("Couldn't extract array from Data object");
    }

    try {
      BasicHDU hdu;
      if (size == 0) {
	hdu = new PrimaryHDU(o);
      } else {
	hdu = new ImageHDU(o);
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

    int rangeDim = fld.getRangeDimension();
    int domainDim = fld.getDomainSet().getDimension();

    if (rangeDim > 1 && domainDim > 1) {
if (0 == 1) {
      throw new VisADException("Can't write FITS file with" +
			       " range dimension of " + rangeDim +
			       " and domain dimension of " + domainDim);
} else { System.err.println("TourWriter.save(FlatField): Not checking dimensions!!!"); }
    }

    if (rangeDim > 1) {
      saveBinaryTable(fld, domainDim, rangeDim);
    } else {
      saveImage(fld, domainDim);
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
