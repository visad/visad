package visad.data.fits;

import java.rmi.RemoteException;

import visad.Data;
import visad.Function;
import visad.Scalar;
import visad.Set;
import visad.Tuple;
import visad.VisADException;

public class TourInspector
	extends Tourist
{
  private int total;

  public TourInspector(boolean replace)
  {
    super(replace);
    total = 0;
  }

  public boolean visit(Function func, int depth)
	throws RemoteException, VisADException
  {
    if (depth > 2) {
      throw new VisADException("Too deep for FITS");
    }

    total++;
    return true;
  }

  public boolean visit(Scalar scalar, int depth)
	throws VisADException
  {
    throw new VisADException("Can't write a single scalar value as a FITS HDU");
  }

  public boolean visit(Set set, int depth)
	throws VisADException
  {
    if (depth > 2) {
      throw new VisADException("Too deep for FITS");
    }

    total++;
    return true;
  }

  public int getTotal()
  {
    return total;
  }
}
