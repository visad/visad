package visad.data.fits;

import java.rmi.RemoteException;

import visad.Data;
import visad.Function;
import visad.Scalar;
import visad.Set;
import visad.Tuple;
import visad.VisADException;

public class FitsTourGuide
	extends TourGuide
{
  private boolean replace;

  public FitsTourGuide(Data data, Tourist tourist)
	throws RemoteException, VisADException
  {
    this.replace = replace;

    show(data, tourist, 0);
  }

  public boolean show(Function func, Tourist tourist, int depth)
	throws RemoteException, VisADException
  {
    return tourist.visit(func, depth);
  }

  public boolean show(Scalar scalar, Tourist tourist, int depth)
	throws VisADException
  {
    return tourist.visit(scalar, depth);
  }

  public boolean show(Set set, Tourist tourist, int depth)
	throws VisADException
  {
    return tourist.visit(set, depth);
  }

  public boolean show(Tuple tuple, Tourist tourist, int depth)
	throws RemoteException, VisADException
  {
    boolean rtnval = true;

    int dim = tuple.getDimension();
    for (int i = 0; i < dim; i++) {
      rtnval |= show(tuple.getComponent(i), tourist, depth+1);
    }

    return rtnval;
  }
}
