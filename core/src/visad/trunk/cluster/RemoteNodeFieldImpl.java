//
// RemoteNodeFieldImpl.java
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

import java.util.Enumeration;
import java.rmi.*;

/**
   RemoteNodeFieldImpl is the class for cluster node
   VisAD Field data objects.<P>
*/
public class RemoteNodeFieldImpl extends RemoteNodeDataImpl
       implements RemoteNodeField {

  private Field adaptedField = null;
  private int length;

  /**
     must call setupClusterData after constructor to finish the
     "construction"
  */
  public RemoteNodeFieldImpl(FunctionType type, Set set)
         throws VisADException, RemoteException {
    super();
    if (type == null) {
      throw new ClusterException("type cannot be null");
    }
    if (set == null) {
      throw new ClusterException("set cannot be null");
    }
    adaptedField = new FieldImpl(type, set);
    length = set.getLength();
  }

  // WLH 4 Sept 2001
  /**
     constructor for rendering without using partitionSet
  */
  public RemoteNodeFieldImpl(FieldImpl field)
         throws VisADException, RemoteException {
    super();
    if (field == null) {
      throw new ClusterException("field cannot be null");
    }
    adaptedField = field;
  }

  public void setSamples(RemoteNodeDataImpl[] range)
         throws VisADException, RemoteException {
    setSamples(range, false);
  }

  // public void setSamples(RemoteNodeDataImpl[] range, boolean copy)
  public void setSamples(Data[] range, boolean copy)
         throws VisADException, RemoteException {
    if (range == null) {
      throw new ClusterException("range cannot be null");
    }
    if (range.length != length) {
      throw new ClusterException("range length must match set length");
    }
    for (int i=0; i<range.length; i++) {
      if (!(range[i] instanceof RemoteNodeDataImpl)) {
        throw new ClusterException("range values must be RemoteNodeDataImpl");
      }
    }

    adaptedField.setSamples(range, false); // don't copy
    // set this as parent
    for (int i=0; i<length; i++) {
      ((RemoteNodeDataImpl) range[i]).setParent(this);
    }
  }

  public void setSamples(double[][] range)
         throws VisADException, RemoteException {
    throw new ClusterException("no setSamples(double[][]) method");
  }

  public void setSamples(float[][] range)
         throws VisADException, RemoteException {
    throw new ClusterException("no setSamples(float[][]) method");
  }




  public MathType getType() throws VisADException, RemoteException {
    return adaptedField.getType();
  }

  public boolean isMissing() throws VisADException, RemoteException {
    return adaptedField.isMissing();
  }

  public int getDomainDimension() throws VisADException, RemoteException {
    return adaptedField.getDomainDimension();
  }

  public Set getDomainSet() throws VisADException, RemoteException {
    return adaptedField.getDomainSet();
  }

  public int getLength() throws RemoteException {
    return length;
  }

  public Unit[] getDomainUnits() throws VisADException, RemoteException {
    return adaptedField.getDomainUnits();
  }

  public CoordinateSystem getDomainCoordinateSystem()
         throws VisADException, RemoteException {
    return adaptedField.getDomainCoordinateSystem();
  }

  public Data getSample(int index)
         throws VisADException, RemoteException {
    return adaptedField.getSample(index);
  }

  public void setSample(RealTuple domain, Data range, boolean copy)
         throws VisADException, RemoteException {
    throw new ClusterException("no setSample() method");
  }

  public void setSample(RealTuple domain, Data range)
         throws VisADException, RemoteException {
    throw new ClusterException("no setSample() method");
  }

  public void setSample(int index, Data range, boolean copy)
         throws VisADException, RemoteException {
    throw new ClusterException("no setSample() method");
  }

  public void setSample(int index, Data range)
         throws VisADException, RemoteException {
    throw new ClusterException("no setSample() method");
  }

  public Field extract(int component)
         throws VisADException, RemoteException {
    throw new ClusterException("no extract() method");
  }

  public Field domainMultiply()
         throws VisADException, RemoteException {
    throw new ClusterException("no domainMultiply() method");
  }

  public Field domainMultiply(int depth)
         throws VisADException, RemoteException {
    throw new ClusterException("no domainMultiply() method");
  }

  public Field domainFactor( RealType factor )
         throws VisADException, RemoteException {
    throw new ClusterException("no domainFactor() method");
  }

  public double[][] getValues()
         throws VisADException, RemoteException {
    return adaptedField.getValues();
  }

  public double[][] getValues(boolean copy)
         throws VisADException, RemoteException {
    return adaptedField.getValues(copy);
  }

  public float[][] getFloats()
         throws VisADException, RemoteException {
    return adaptedField.getFloats();
  }

  public float[][] getFloats(boolean copy)
         throws VisADException, RemoteException {
    return adaptedField.getFloats(copy);
  }

  public String[][] getStringValues()
         throws VisADException, RemoteException {
    return adaptedField.getStringValues();
  }

  public Unit[] getDefaultRangeUnits()
         throws VisADException, RemoteException {
    return adaptedField.getDefaultRangeUnits();
  }

  public Unit[][] getRangeUnits()
         throws VisADException, RemoteException {
    return adaptedField.getRangeUnits();
  }

  public CoordinateSystem[] getRangeCoordinateSystem()
         throws VisADException, RemoteException {
    return adaptedField.getRangeCoordinateSystem();
  }

  public CoordinateSystem[] getRangeCoordinateSystem(int i)
         throws VisADException, RemoteException {
    return adaptedField.getRangeCoordinateSystem(i);
  }

  public boolean isFlatField() throws VisADException, RemoteException {
    return false;
  }

  public Enumeration domainEnumeration()
         throws VisADException, RemoteException {
    return adaptedField.domainEnumeration();
  }



  public Data evaluate(Real domain)
         throws VisADException, RemoteException {
    throw new ClusterException("no evaluate() method");
  }

  public Data evaluate(Real domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    throw new ClusterException("no evaluate() method");
  }

  public Data evaluate(RealTuple domain)
         throws VisADException, RemoteException {
    throw new ClusterException("no evaluate() method");
  }

  public Data evaluate(RealTuple domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    throw new ClusterException("no evaluate() method");
  }

  public Field resample(Set set) throws VisADException, RemoteException {
    throw new ClusterException("no resample() method");
  }

  public Field resample(Set set, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    throw new ClusterException("no resample() method");
  }

  public Data derivative( RealTuple location, RealType[] d_partial_s,
                          MathType[] derivType_s, int error_mode )
         throws VisADException, RemoteException {
    throw new ClusterException("no derivative() method");
  }

  public Data derivative( int error_mode )
         throws VisADException, RemoteException {
    throw new ClusterException("no derivative() method");
  }

  public Data derivative( MathType[] derivType_s, int error_mode )
         throws VisADException, RemoteException {
    throw new ClusterException("no derivative() method");
  }

  public Function derivative( RealType d_partial, int error_mode )
         throws VisADException, RemoteException {
    throw new ClusterException("no derivative() method");
  }

  public Function derivative( RealType d_partial, MathType derivType, int error_mode )
         throws VisADException, RemoteException {
    throw new ClusterException("no derivative() method");
  }




  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    return adaptedField.computeRanges(type, shadow);
  }

  public DataShadow computeRanges(ShadowType type, int n)
         throws VisADException, RemoteException {
    return adaptedField.computeRanges(type, n);
  }

  public double[][] computeRanges(RealType[] reals)
         throws VisADException, RemoteException {
    throw new ClusterException("no computeRanges(RealType[]) method");
  }

  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException {
    return adaptedField.adjustSamplingError(error, error_mode);
  }

  public String longString() throws VisADException, RemoteException {
    return longString("");
  }

  public String longString(String pre)
         throws VisADException, RemoteException {
    return pre + "RemoteNodeFieldImpl";
  }

}

