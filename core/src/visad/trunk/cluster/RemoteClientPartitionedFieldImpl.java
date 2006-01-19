//
// RemoteClientPartitionedFieldImpl.java
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

import java.util.Enumeration;
import java.rmi.*;

/**
   RemoteClientPartitionedFieldImpl is the class for cluster client
   VisAD Field data objects.<P>
*/
public class RemoteClientPartitionedFieldImpl extends RemoteClientDataImpl
       implements RemoteClientField {

  private FunctionType type = null;
  private Set set = null;
  private int length;

  /**
     must call setupClusterData after constructor to finish the
     "construction"
  */
  public RemoteClientPartitionedFieldImpl(FunctionType t, Set s)
         throws VisADException, RemoteException {
    super();
    if (t == null) {
      throw new ClusterException("type cannot be null");
    }
    if (s == null) {
      throw new ClusterException("set cannot be null");
    }
    type = t;
    set = s;
    length = set.getLength();
  }

  public void setSamples(RemoteClientDataImpl[] range)
         throws VisADException, RemoteException {
    throw new ClusterException("no setSamples(RemoteClientDataImpl[]) method");
  }

  public void setSamples(RemoteClientDataImpl[] range, boolean copy)
         throws VisADException, RemoteException {
    throw new ClusterException("no setSamples(RemoteClientDataImpl[], boolean) " +
                               "method");
  }

  public void setSamples(Data[] range, boolean copy)
         throws VisADException, RemoteException {
    throw new ClusterException("no setSamples(Data[], boolean) method");
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
    return type;
  }

  public boolean isMissing() throws VisADException, RemoteException {
    return false; // ????
  }

  public int getDomainDimension() throws VisADException, RemoteException {
    return type.getDomain().getDimension();
  }

  public Set getDomainSet() throws VisADException, RemoteException {
    return set;
  }

  public int getLength() throws RemoteException {
    return length;
  }

  public Unit[] getDomainUnits() throws VisADException, RemoteException {
    return type.getDomain().getDefaultUnits();
  }

  public CoordinateSystem getDomainCoordinateSystem()
         throws VisADException, RemoteException {
    return type.getDomain().getCoordinateSystem();
  }

  public Data getSample(int index)
         throws VisADException, RemoteException {
    throw new ClusterException("no getSample() method");
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
    throw new ClusterException("no getValues() method");
  }

  public double[][] getValues(boolean copy)
         throws VisADException, RemoteException {
    throw new ClusterException("no getValues() method");
  }

  public float[][] getFloats()
         throws VisADException, RemoteException {
    throw new ClusterException("no getFloats() method");
  }

  public float[][] getFloats(boolean copy)
         throws VisADException, RemoteException {
    throw new ClusterException("no getFloats() method");
  }

  public String[][] getStringValues()
         throws VisADException, RemoteException {
    throw new ClusterException("no getStringValues() method");
  }

  public Unit[] getDefaultRangeUnits()
         throws VisADException, RemoteException {
    throw new ClusterException("no getRangeCoordinateSystem() method");
  }

  public Unit[][] getRangeUnits()
         throws VisADException, RemoteException {
    throw new ClusterException("no getRangeCoordinateSystem() method");
  }

  public CoordinateSystem[] getRangeCoordinateSystem()
         throws VisADException, RemoteException {
    throw new ClusterException("no getRangeCoordinateSystem() method");
  }

  public CoordinateSystem[] getRangeCoordinateSystem(int i)
         throws VisADException, RemoteException {
    throw new ClusterException("no getRangeCoordinateSystem() method");
  }

  public boolean isFlatField() throws VisADException, RemoteException {
    return false;
  }

  public Enumeration domainEnumeration()
         throws VisADException, RemoteException {
    // return new FieldEnumerator(this);
    throw new ClusterException("no domainEnumeration method");
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
    throw new ClusterException("no computeRanges() method");
  }

  public DataShadow computeRanges(ShadowType type, int n)
         throws VisADException, RemoteException {
    throw new ClusterException("no computeRanges() method");
  }

  public double[][] computeRanges(RealType[] reals)
         throws VisADException, RemoteException {
    throw new ClusterException("no computeRanges(RealType[]) method");
  }

  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException {
    throw new ClusterException("no adjustSamplingError() method");
  }

  public String longString() throws VisADException, RemoteException {
    return longString("");
  }

  public String longString(String pre)
         throws VisADException, RemoteException {
    return pre + "RemoteClientPartitionedFieldImpl";
  }

}

