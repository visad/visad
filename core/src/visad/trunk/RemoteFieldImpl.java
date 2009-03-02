//
// RemoteFieldImpl.java
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

package visad;

import java.util.*;
import java.rmi.*;

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

  public void setSamples(double[][] range)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.setSamples: " +
                                     "AdaptedData is null");
    }
    ((FieldImpl) AdaptedData).setSamples(range);
  }

  public void setSamples(float[][] range)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.setSamples: " +
                                     "AdaptedData is null");
    }
    ((FieldImpl) AdaptedData).setSamples(range);
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

  public void setSample(RealTuple domain, Data range, boolean copy)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.setSample: " +
                                     "AdaptedData is null");
    }
    ((FieldImpl) AdaptedData).setSample(domain, range, copy);
  }

  public void setSample(int index, Data range)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.setSample: " +
                                     "AdaptedData is null");
    }
    ((FieldImpl) AdaptedData).setSample(index, range);
  }

  public void setSample(int index, Data range, boolean copy)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.setSample: " +
                                     "AdaptedData is null");
    }
    ((FieldImpl) AdaptedData).setSample(index, range, copy);
  }

  public Field extract(int component)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.extract: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).extract(component);
  }

  /** combine domains of two outermost nested Fields into a single
      domain and Field */
  public Field domainMultiply()
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.domainMultiply: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).domainMultiply();
  }

  /** combine domains to depth, if possible */
  public Field domainMultiply(int depth)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.domainMultiply: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).domainMultiply(depth);
  }

  /** factor Field domain into domains of two nested Fields */
  public Field domainFactor( RealType factor )
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.domainFactor: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).domainFactor(factor);
  }

  public float[][] getFloats()
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getValues: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getFloats();
  }

  public float[][] getFloats(boolean copy)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getValues: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getFloats(copy);
  }

  public double[][] getValues()
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getValues: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getValues();
  }

  public double[][] getValues(boolean copy)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFieldImpl.getValues: " +
                                     "AdaptedData is null");
    }
    return ((FieldImpl) AdaptedData).getValues(copy);
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

