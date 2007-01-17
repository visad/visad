//
// RemoteNodeTupleImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

package visad.cluster;

import visad.*;

import java.rmi.*;

/**
   RemoteNodeTupleImpl is the class for cluster client
   VisAD Tuple data objects.<P>
*/
public class RemoteNodeTupleImpl extends RemoteNodeDataImpl
       implements RemoteNodeTuple {

  private Tuple adaptedTuple = null;
  private DataReferenceImpl adaptedTupleRef = null;

  /**
     must call setupClusterData after constructor to finish the
     "construction"
  */
  public RemoteNodeTupleImpl(Data[] datums)
         throws VisADException, RemoteException {
    super();
    if (datums == null) {
      throw new ClusterException("datums cannot be null");
    }
    int n = datums.length;
    if (n == 0) {
      throw new ClusterException("datums.length must be > 0");
    }
    for (int i=0; i<n; i++) {
      if (!(datums[i] instanceof DataImpl ||
            datums[i] instanceof RemoteNodeDataImpl)) {
        throw new ClusterException("datums must be DataImpl " +
                                   "or RemoteNodeDataImpl");
      }
    }
    adaptedTuple = new Tuple(datums, false); // no copy
    // set this as parent for RemoteNodeDataImpls
    boolean any_local = false;
    for (int i=0; i<n; i++) {
      if (datums[i] instanceof RemoteNodeDataImpl) {
        ((RemoteNodeDataImpl) datums[i]).setParent(this);
      }
      else {
        any_local = true;
      }
    }
    if (any_local) {
      // hack parent notify logic for non-RemoteNodeDataImpl components
      adaptedTupleRef = new DataReferenceImpl("adaptedTupleRef");
      adaptedTupleRef.setData(adaptedTuple);
      CellImpl adaptedTupleCell = new CellImpl() {
        public void doAction() throws VisADException, RemoteException {
          notifyReferences();
        }
      };
      adaptedTupleCell.addReference(adaptedTupleRef);
    }
  }

  public MathType getType() throws VisADException, RemoteException {
    return adaptedTuple.getType();
  }

  public Real[] getRealComponents()
         throws VisADException, RemoteException {
    return adaptedTuple.getRealComponents();
  }

  public int getDimension() throws RemoteException {
    return adaptedTuple.getDimension();
  }

  public Data getComponent(int i) throws VisADException, RemoteException {
    return adaptedTuple.getComponent(i);
  }

  public boolean isMissing() throws RemoteException {
    return adaptedTuple.isMissing();
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    return adaptedTuple.computeRanges(type, shadow);
  }

  public DataShadow computeRanges(ShadowType type, int n)
         throws VisADException, RemoteException {
    return adaptedTuple.computeRanges(type, n);
  }

  public double[][] computeRanges(RealType[] reals)
         throws VisADException, RemoteException {
    return adaptedTuple.computeRanges(reals);
  }

  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException {
    return adaptedTuple.adjustSamplingError(error, error_mode);
  }

  public String longString() throws VisADException, RemoteException {
    return longString("");
  }

  public String longString(String pre)
         throws VisADException, RemoteException {
    return pre + "RemoteNodeTupleImpl";
  }

}

