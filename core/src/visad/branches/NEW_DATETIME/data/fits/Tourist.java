package visad.data.fits;

import java.rmi.RemoteException;

import visad.Data;
import visad.Function;
import visad.Scalar;
import visad.Set;
import visad.Tuple;
import visad.VisADException;

public class Tourist
{
  boolean replace;

  public Tourist(boolean replace)
  {
    this.replace = replace;
  }

  public boolean visit(Function func, int depth)
	throws RemoteException, VisADException
  {
//    System.err.println("Tourist.visit: function " + func.getType() + ", depth=" + depth);
    return false;
  }

  public boolean visit(Scalar scalar, int depth)
	throws VisADException
  {
//    System.err.println("Tourist.visit: scalar " + scalar.getType() + ", depth=" + depth);
    return false;
  }

  public boolean visit(Set set, int depth)
	throws VisADException
  {
//    System.err.println("Tourist.visit: set " + set.getType() + ", depth=" + depth);
    return false;
  }
}
