//
// FlatFieldIface.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;

/**
   FlatField is the VisAD class for finite samplings of functions whose
   range type and range coordinate systems are simple enough to allow
   efficient representation.  The DomainSet, DomainCoordinateSystem,
   RangeSet, RangeCoordinateSystem and RangeCoordinateSystems variables
   of FlatField are immutable.<P>

   A FlatField range type may be either a RealType (for a function with
   range = R), a RealTupleType (for a function with range = R^n for n > 0),
   or a TupleType of RealType-s and RealTupleType-s..<P>

   VisAD avoids invoking methods once per datum through the use of
   FlatField's.  These are logically Field's of Tuple's of RealType's
   and RealTupleType's.  Internally FlatField's are stored as arrays of
   numerical values, rather than the arrays of data objects stored in
   Field's.  Many of the methods in the FlatField class and in other
   classes (e.g., CoordinateTransform, Set, Unit) process data in the
   form double[Dimension][Length] where Length is the number of samples
   in a Field and Dimension is the number of Tuple elements in the
   Field range values.  Note that the order of the Length and Dimension
   indices are reversed as array indices.  This allows efficient
   processing of long columns of Field value components.  For example,
   if Latitude is one component of Field values, then any computation
   involving Latitude can be applied in a tight loop to all Latitude's
   in the Field.<P>

   FlatField's support range types more general than RealTuple's.  To
   understand the motive, consider a set of observations that include
   Latitude, Longitude, Altitude, Pressure, Temperature, etc.  We can
   organize these as a Field whose range values have the Tuple type:<P>
 <PRE>

     (Latitude, Longitude, Altitude, Pressure, Temperature, ...)

</PRE>
   However, in order to declare that (Latitude, Longitude, Altitude)
   is a coordinate system with coordinate transform functions to other
   spatial coordinate systems, we need to organize:<P>
<PRE>

     (Latitude, Longitude, Altitude)

</PRE>
   as a RealTupleType.  Hence the range type of the Field of observations
   must be:<P>
<PRE>

     ((Latitude, Longitude, Altitude), Pressure, Temperature, ...)

</PRE>
   which is not a RealTupleType (since one of its components is a
   RealTupleType).  In order to process such data efficiently, FlatField's
   must support range types that are Tuple's of RealType's and
   RealTupleType's.<P>
*/
public interface FlatFieldIface extends Field {

  /**
   * Returns the sampling set of each flat component.
   * @return		The sampling set of each component in the flat range.
   */
  Set[] getRangeSets()
    throws RemoteException, VisADException;

  /** return array of ErrorEstimates associated with each
      RealType component of range; each ErrorEstimate is a
      mean error for all samples of a range RealType
      component */
  ErrorEstimate[] getRangeErrors()
    throws RemoteException, VisADException;

  /** set ErrorEstimates associated with each RealType
      component of range */
  void setRangeErrors(ErrorEstimate[] errors)
    throws RemoteException, VisADException;

  /** set range array as range values of this FlatField;
      the array is dimensioned
      double[number_of_range_components][number_of_range_samples];
      the order of range values must be the same as the order of domain
      indices in the DomainSet; copy array if copy flag is true */
  void setSamples(double[][] range, boolean copy)
    throws RemoteException, VisADException;

  /** set range array as range values of this FlatField;
      the array is dimensioned
      float[number_of_range_components][number_of_range_samples];
      the order of range values must be the same as the order of domain
      indices in the DomainSet; copy array if copy flag is true */
  void setSamples(float[][] range, boolean copy)
    throws RemoteException, VisADException;

  /** set the range values of the function including ErrorEstimate-s;
      the order of range values must be the same as the order of
      domain indices in the DomainSet */
  void setSamples(double[][] range, ErrorEstimate[] errors, boolean copy)
    throws RemoteException, VisADException;

  void setSamples(int start, double[][] range)
    throws RemoteException, VisADException;

  /** set the range values of the function including ErrorEstimate-s;
      the order of range values must be the same as the order of
      domain indices in the DomainSet */
  void setSamples(float[][] range, ErrorEstimate[] errors, boolean copy)
    throws RemoteException, VisADException;

  byte[][] grabBytes()
    throws RemoteException, VisADException;


  /** get values for 'Flat' components in default range Unit-s */
  /*- TDR June 1998  */
  double[] getValues(int s_index)
    throws RemoteException, VisADException;

  /** mark this FlatField as non-missing */
  void clearMissing()
    throws RemoteException, VisADException;

  /** convert this FlatField to a (non-Flat) FieldImpl */
  Field convertToField()
    throws RemoteException, VisADException;

  /**
   * Gets the number of components in the "flat" range.
   *
   * @return The number of components in the "flat" range.
   */
  int getRangeDimension()
    throws RemoteException, VisADException;
}

