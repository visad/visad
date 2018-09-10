//
// RemoteFlatFieldImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;


/**
 * RemoteFlatFieldImpl is the VisAD remote adapter for FlatField.<br>
 * <br>
 * Another way to approach the problem of moving data to a remote
 * machine would be to have a remote class which serializes a sample and
 * copies it over to the remote machine where it is cached for
 * fast access.  This would be a much better approach if the application
 * does a lot of computations on a set of static samples.
 */
public class RemoteFlatFieldImpl
  extends RemoteFieldImpl
  implements RemoteFlatField
{

  /** construct a RemoteFieldImpl object to provide remote
      access to field */
  public RemoteFlatFieldImpl(FlatField flatField)
    throws RemoteException, VisADException
  {
    super(flatField);
  }

  /**
   * Returns the sampling set of each flat component.
   * @return		The sampling set of each component in the flat range.
   */
  public Set[] getRangeSets()
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.getRangeSets: " +
                                     "AdaptedData is null");
    }
    return ((FlatField )AdaptedData).getRangeSets();
  }

  /** return array of ErrorEstimates associated with each
      RealType component of range; each ErrorEstimate is a
      mean error for all samples of a range RealType
      component */
  public ErrorEstimate[] getRangeErrors()
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.getRangeErrors: " +
                                     "AdaptedData is null");
    }
    return ((FlatField )AdaptedData).getRangeErrors();
  }

  /** set ErrorEstimates associated with each RealType
      component of range */
  public void setRangeErrors(ErrorEstimate[] errors)
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.setRangeErrors: " +
                                     "AdaptedData is null");
    }
    ((FlatField )AdaptedData).setRangeErrors(errors);
  }

  /** set range array as range values of this FlatField;
      the array is dimensioned
      double[number_of_range_components][number_of_range_samples];
      the order of range values must be the same as the order of domain
      indices in the DomainSet; copy array if copy flag is true */
  public void setSamples(double[][] range, boolean copy)
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.setSamples: " +
                                     "AdaptedData is null");
    }
    ((FlatField )AdaptedData).setSamples(range, copy);
  }

  /** set range array as range values of this FlatField;
      the array is dimensioned
      float[number_of_range_components][number_of_range_samples];
      the order of range values must be the same as the order of domain
      indices in the DomainSet; copy array if copy flag is true */
  public void setSamples(float[][] range, boolean copy)
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.setSamples: " +
                                     "AdaptedData is null");
    }
    ((FlatField )AdaptedData).setSamples(range, copy);
  }

  /** set the range values of the function including ErrorEstimate-s;
      the order of range values must be the same as the order of
      domain indices in the DomainSet */
  public void setSamples(double[][] range, ErrorEstimate[] errors,
                         boolean copy)
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.setSamples: " +
                                     "AdaptedData is null");
    }
    ((FlatField )AdaptedData).setSamples(range, errors, copy);
  }

  public void setSamples(int start, double[][] range)
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.setSamples: " +
                                     "AdaptedData is null");
    }
    ((FlatField )AdaptedData).setSamples(start, range);
  }

  /** set the range values of the function including ErrorEstimate-s;
      the order of range values must be the same as the order of
      domain indices in the DomainSet */
  public void setSamples(float[][] range, ErrorEstimate[] errors,
                         boolean copy)
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.setSamples: " +
                                     "AdaptedData is null");
    }
    ((FlatField )AdaptedData).setSamples(range, errors, copy);
  }

  public byte[][] grabBytes()
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.grabBytes: " +
                                     "AdaptedData is null");
    }
    return ((FlatField )AdaptedData).grabBytes();
  }


  /** get values for 'Flat' components in default range Unit-s */
  public double[] getValues(int s_index)
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.getValues: " +
                                     "AdaptedData is null");
    }
    return ((FlatField )AdaptedData).getValues(s_index);
  }

  /** mark this FlatField as non-missing */
  public void clearMissing()
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.clearMissing: " +
                                     "AdaptedData is null");
    }
    ((FlatField )AdaptedData).clearMissing();
  }

  /** convert this FlatField to a (non-Flat) FieldImpl */
  public Field convertToField()
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.convertToField: " +
                                     "AdaptedData is null");
    }
    return ((FlatField )AdaptedData).convertToField();
  }

  /**
   * Gets the number of components in the "flat" range.
   *
   * @return The number of components in the "flat" range.
   */
  public int getRangeDimension()
    throws RemoteException, VisADException
  {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFlatFieldImpl.getRangeDimension: " +
                                     "AdaptedData is null");
    }
    return ((FlatField )AdaptedData).getRangeDimension();
  }
}
