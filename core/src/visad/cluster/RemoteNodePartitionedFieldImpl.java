//
// RemoteNodePartitionedFieldImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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
   RemoteNodePartitionedFieldImpl is the class for cluster node
   VisAD Field data objects that are paritioned.<P>
*/
public class RemoteNodePartitionedFieldImpl extends RemoteNodeDataImpl
       implements RemoteNodePartitionedField {

  private boolean flat; // true if adaptedField is a FlatField
  private FieldImpl adaptedField = null; // can be FileFlatField?
  private DataReferenceImpl adaptedFieldRef = null;
  private int length;

  /**
     must call setupClusterData after constructor to finish the
     "construction"
  */
  public RemoteNodePartitionedFieldImpl(FunctionType type, Set set)
         throws VisADException, RemoteException {
    this(makeField(type, set));
  }

  /**
     must call setupClusterData after constructor to finish the
     "construction"
  */
  public RemoteNodePartitionedFieldImpl(FieldImpl adapted)
         throws VisADException, RemoteException {
    super();
    adaptedField = adapted;
    flat = ((FunctionType) adaptedField.getType()).getFlat();

    // hack parent notify logic for non-RemoteNodeDataImpl range values
    adaptedFieldRef = new DataReferenceImpl("adaptedFieldRef");
    adaptedFieldRef.setData(adaptedField);
    CellImpl adaptedFieldCell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        notifyReferences();
      }
    };
    adaptedFieldCell.addReference(adaptedFieldRef);

    length = adaptedField.getLength();
  }

  private static FieldImpl makeField(FunctionType type, Set set)
          throws VisADException, RemoteException {
    if (type == null) {
      throw new ClusterException("type cannot be null");
    }
    if (set == null) {
      throw new ClusterException("set cannot be null");
    }
    if (type.getFlat()) {
      return new FlatField(type, set);
    }
    else {
      return new FieldImpl(type, set);
    }
  }

  public FieldImpl getAdaptedField() {
    return adaptedField;
  }

/* only DataImpl under RemoteNodePartitionedFieldImpl
   so no setSamples(RemoteNodeDataImpl[] range) methods
*/

  public void setSamples(Data[] range, boolean copy)
         throws VisADException, RemoteException {
    adaptedField.setSamples(range, copy);
  }

  public void setSamples(double[][] range)
         throws VisADException, RemoteException {
    adaptedField.setSamples(range);
  }

  public void setSamples(float[][] range)
         throws VisADException, RemoteException {
    adaptedField.setSamples(range);
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
    adaptedField.setSample(domain, range, copy);
  }

  public void setSample(RealTuple domain, Data range)
         throws VisADException, RemoteException {
    adaptedField.setSample(domain, range);
  }

  public void setSample(int index, Data range, boolean copy)
         throws VisADException, RemoteException {
    adaptedField.setSample(index, range, copy);
  }

  public void setSample(int index, Data range)
         throws VisADException, RemoteException {
    adaptedField.setSample(index, range);
  }

  public Field extract(int component)
         throws VisADException, RemoteException {
    return adaptedField.extract(component);
  }

  public Field domainMultiply()
         throws VisADException, RemoteException {
    return adaptedField.domainMultiply();
  }

  public Field domainMultiply(int depth)
         throws VisADException, RemoteException {
    return adaptedField.domainMultiply(depth);
  }

  public Field domainFactor( RealType factor )
         throws VisADException, RemoteException {
    return adaptedField.domainFactor(factor);
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

  /** even if flat == true, a cast of this to FlatField will fail */
  public boolean isFlatField() throws VisADException, RemoteException {
    return false;
  }

  public Enumeration domainEnumeration()
         throws VisADException, RemoteException {
    return adaptedField.domainEnumeration();
  }



  public Data evaluate(Real domain)
         throws VisADException, RemoteException {
    return adaptedField.evaluate(domain);
  }

  public Data evaluate(Real domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return adaptedField.evaluate(domain, sampling_mode, error_mode);
  }

  public Data evaluate(RealTuple domain)
         throws VisADException, RemoteException {
    return adaptedField.evaluate(domain);
  }

  public Data evaluate(RealTuple domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return adaptedField.evaluate(domain, sampling_mode, error_mode);
  }

  public Field resample(Set set) throws VisADException, RemoteException {
    return adaptedField.resample(set);
  }

  public Field resample(Set set, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    return adaptedField.resample(set, sampling_mode, error_mode);
  }

  public Data derivative( RealTuple location, RealType[] d_partial_s,
                          MathType[] derivType_s, int error_mode )
         throws VisADException, RemoteException {
    return adaptedField.derivative(location, d_partial_s, derivType_s, error_mode);
  }

  public Data derivative( int error_mode )
         throws VisADException, RemoteException {
    return adaptedField.derivative(error_mode);
  }

  public Data derivative( MathType[] derivType_s, int error_mode )
         throws VisADException, RemoteException {
    return adaptedField.derivative(derivType_s, error_mode);
  }

  public Function derivative( RealType d_partial, int error_mode )
         throws VisADException, RemoteException {
    return adaptedField.derivative(d_partial, error_mode);
  }

  public Function derivative( RealType d_partial, MathType derivType, int error_mode )
         throws VisADException, RemoteException {
    return adaptedField.derivative(d_partial, derivType, error_mode);
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
    return adaptedField.computeRanges(reals);
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
    return pre + "RemoteNodePartitionedFieldImpl";
  }

}

