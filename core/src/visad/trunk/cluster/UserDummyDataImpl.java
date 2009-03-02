//
// UserDummyDataImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
   RemoteClientData is the class for cluster client
   VisAD data objects.<P>
*/
public class UserDummyDataImpl extends DataImpl {

  private RemoteClientData adaptedRemoteClientData = null;

  private RemoteDataReferenceImpl rref = null;
  private CellImpl cell = null;
  private RemoteCellImpl rcell = null;
  private UserDummyDataImpl uddi = null;

  public UserDummyDataImpl(RemoteClientData rcd)
         throws VisADException, RemoteException {
    super(rcd.getType());
    adaptedRemoteClientData = rcd;
    DataReferenceImpl ref = new DataReferenceImpl("UserDummy");
    rref = new RemoteDataReferenceImpl(ref);
    rref.setData(adaptedRemoteClientData);
    uddi = this;

    cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        uddi.notifyReferences();
      }
    };
    rcell = new RemoteCellImpl(cell);
    rcell.addReference(rref);
  }


  public MathType getType() {
    MathType type = null;
    try {
      type = adaptedRemoteClientData.getType();
    }
    catch (VisADException e) {
      throw new VisADError(e.toString());
    }
    catch (RemoteException e) {
      throw new VisADError(e.toString());
    }
    return type;
  }

  public boolean isMissing() throws VisADException, RemoteException {
    return adaptedRemoteClientData.isMissing();
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    throw new ClusterException("no computeRanges method for user dummy data");
  }

  public Data binary(Data data, int op, MathType new_type,
                    int sampling_mode, int error_mode )
             throws VisADException, RemoteException {
    throw new ClusterException("no binary method for user dummy data");
  }

  public Data binary(Data data, int op, int sampling_mode, int error_mode )
             throws VisADException, RemoteException {
    throw new ClusterException("no binary method for user dummy data");
  }

  public Data unary(int op, MathType new_type,
                    int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    throw new ClusterException("no unary method for user dummy data");
  }

  public Data unary(int op, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    throw new ClusterException("no unary method for user dummy data");
  }

  public Object clone() {
    UserDummyDataImpl new_uddi = null;
    try {
      new_uddi = new UserDummyDataImpl(adaptedRemoteClientData);
    }
    catch (VisADException e) {
      throw new VisADError(e.toString());
    }
    catch (RemoteException e) {
      throw new VisADError(e.toString());
    }
    return new_uddi;
  }

}

