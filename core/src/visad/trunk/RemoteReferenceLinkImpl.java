//
// RemoteReferenceLinkImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

package visad;
 
import java.rmi.Remote;
import java.rmi.RemoteException;
 
import java.util.Enumeration;
import java.util.Vector;

import java.rmi.server.UnicastRemoteObject;
 
/**
   RemoteReferenceLinkImpl is the VisAD remote adapter for DataDisplayLink-s.
*/
public class RemoteReferenceLinkImpl extends UnicastRemoteObject
        implements RemoteReferenceLink
{
  DataDisplayLink link;

  /** create a Remote reference to a DataDisplayLink */
  public RemoteReferenceLinkImpl(DataDisplayLink ddl)
	throws RemoteException
  {
    if (ddl == null) {
      throw new RemoteException("Cannot link to null link");
    }

    DataReference ref;
    ref = ddl.getDataReference();
    if (!(ref instanceof DataReferenceImpl)) {
      throw new RemoteException("Cannot link to non-DataReferenceImpl");
    }

    link = ddl;
  }

  /** return the name of the DataRenderer used to render this reference */
  public String getRendererClassName()
	throws VisADException, RemoteException
  {
    return link.getRenderer().getClass().getName();
  }

  /** return a reference to the remote Data object */
  public RemoteDataReference getReference()
	throws VisADException, RemoteException
  {
    DataReferenceImpl di = (DataReferenceImpl )link.getDataReference();
    return new RemoteDataReferenceImpl(di);
  }

  /** return the list of ConstantMap-s which apply to this Data object */
  public Vector getConstantMapVector()
	throws VisADException, RemoteException
  {
    return link.getConstantMaps();
  }

  public boolean equals(Object o)
  {
    if (!(o instanceof RemoteReferenceLink)) {
      return false;
    }

    boolean result;
    if (o instanceof RemoteReferenceLinkImpl) {
      RemoteReferenceLinkImpl rrli = (RemoteReferenceLinkImpl )o;
      result = link.equals(rrli.link);
    } else {
      RemoteReferenceLink rrl = (RemoteReferenceLink )o;
      try {
        result = getReference().equals(rrl.getReference());
      } catch (Exception e){
        result = false;
      }
    }

    return result;
  }

  /** return a String representation of the referenced data */
  public String toString()
  {
    String s;
    try {
      s = link.getDataReference().getName() + " -> " + getRendererClassName();
    } catch (RemoteException e) {
      return null;
    } catch (VisADException e) {
      return null;
    }

    Vector v;
    try {
      v = getConstantMapVector();
      Enumeration e = v.elements();
      if (e.hasMoreElements()) {
	s = s + ":";
	while (e.hasMoreElements()) {
	  ConstantMap cm = (ConstantMap )e.nextElement();
	  s = s + " [" + cm.getConstant() + " -> " + cm.getDisplayScalar() + "]";
	}
      }
    } catch (RemoteException e) {
    } catch (VisADException e) {
    }

    return s;
  }
}
