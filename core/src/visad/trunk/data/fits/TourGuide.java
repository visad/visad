package visad.data.fits;

import java.rmi.RemoteException;

import visad.Data;
import visad.Function;
import visad.Scalar;
import visad.Set;
import visad.Tuple;
import visad.VisADException;

public abstract class TourGuide
{
  public boolean show(Data data, Tourist tourist, int depth)
	throws RemoteException, VisADException
  {
    if (data instanceof Function) {
      return show((Function )data, tourist,  depth);
    }

    if (data instanceof Scalar) {
      return show((Scalar )data, tourist,  depth);
    }

    if (data instanceof Set) {
      return show((Set )data, tourist,  depth);
    }

    if (data instanceof Tuple) {
      return show((Tuple )data, tourist,  depth);
    }

    throw new VisADException("Unknown datatype " + data.getClass().getName());
  }

  public abstract boolean show(Function func, Tourist tourist, int depth)
	throws RemoteException, VisADException;
  public abstract boolean show(Scalar scalar, Tourist tourist, int depth)
	throws VisADException;
  public abstract boolean show(Set set, Tourist tourist, int depth)
	throws VisADException;
  public abstract boolean show(Tuple tuple, Tourist tourist, int depth)
	throws RemoteException, VisADException;
}
