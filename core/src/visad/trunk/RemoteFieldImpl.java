
//
// RemoteFieldImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.util.*;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

/**
   RemoteFieldImpl is the VisAD remote adapter for FieldImpl.<P>
*/
public class RemoteFieldImpl extends RemoteFunctionImpl
       implements RemoteField {

  /** construct a RemoteFieldImpl object to provide remote
      access to field */
  public RemoteFieldImpl(FieldImpl field) throws RemoteException {
    super(field);
  }

  /** methods adapted from Field */
  public void setSamples(Data[] range, boolean copy)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.setSamples: " +
                                     "AdaptedData is null");
    }
    ((FieldImpl) AdaptedData).setSamples(range, copy);
  }

  public Set getDomainSet() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getDomainSet: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getDomainSet();
  }

  public int getLength() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getLength: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getLength();
  }

  public Unit[] getDomainUnits() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getDomainUnits: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getDomainUnits();
  }

  public CoordinateSystem getDomainCoordinateSystem()
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getDomainCoordinateSystem: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getDomainCoordinateSystem();
  }

  public Data getSample(int index)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getSample: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getSample(index);
  }

  public void setSample(RealTuple domain, Data range)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.setSample: " +
                                     "AdaptedData is null");
    }
    ((FieldImpl) AdaptedData).setSample(domain, range);
  }

  public void setSample(int index, Data range)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.setSample: " +
                                     "AdaptedData is null");
    }
    ((FieldImpl) AdaptedData).setSample(index, range);
  }

  public Field extract(int component)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.extract: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).extract(component);
  }

  public double[][] getValues() 
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getValues: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getValues();
  }
 
  public String[][] getStringValues()
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getStringValues: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getStringValues();
  }

  public Unit[] getDefaultRangeUnits() 
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getDefaultRangeUnits: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getDefaultRangeUnits();
  }

  public Unit[][] getRangeUnits()
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getRangeUnits: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getRangeUnits();
  }

  public CoordinateSystem[] getRangeCoordinateSystem()
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getRangeCoordinateSystem: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getRangeCoordinateSystem();
  }

  public CoordinateSystem[] getRangeCoordinateSystem(int i)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getRangeCoordinateSystem: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getRangeCoordinateSystem(i);
  }

  public boolean isFlatField() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.isFlatField: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).isFlatField();
  }

  public Enumeration domainEnumeration()
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.domainEnumeration: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).domainEnumeration();
  }

}

