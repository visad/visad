//
// RemoteClientDataImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.*;
import java.rmi.*;

/**
   RemoteClientData is the class for cluster client
   VisAD data objects.<P>
*/
public abstract class RemoteClientDataImpl extends RemoteClusterDataImpl
       implements RemoteClientData {

  public RemoteClientDataImpl() throws RemoteException {
  }

  public Data binary(Data data, int op, MathType new_type,
                    int sampling_mode, int error_mode )
             throws VisADException, RemoteException {
    throw new ClusterException("no binary method for cluster client data");
  }

  public Data binary(Data data, int op, int sampling_mode, int error_mode )
             throws VisADException, RemoteException {
    throw new ClusterException("no binary method for cluster client data");
  }

  public Data unary(int op, MathType new_type,
                    int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    throw new ClusterException("no unary method for cluster client data");
  }

  public Data unary(int op, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    throw new ClusterException("no unary method for cluster client data");
  }

}

