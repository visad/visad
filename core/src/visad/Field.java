//
// Field.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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
   Field is the VisAD interface for finite samplings of functions
   from R^n to a range type, where  n>0.<P>

   A Field domain type may be either a RealType (for a function with
   domain = R) or a RealTupleType (for a function with domain = R^n
   for n > 0).<P>
*/
public interface Field extends Function {

  /** set the range samples of the function; the order of range samples
      must be the same as the order of domain indices in the DomainSet;
      copy range objects if copy is true;
      should use same MathType object in each Data object in range array */
  void setSamples(Data[] range, boolean copy)
         throws VisADException, RemoteException;

  /** set range array as range values of this Field;
      this must have a Flat range; the array is dimensioned
      float[number_of_range_components][number_of_range_samples];
      the order of range values must be the same as the order of domain
      indices in the DomainSet */
  void setSamples(double[][] range)
         throws VisADException, RemoteException;

  /** set range array as range values of this Field;
      this must have a Flat range; the array is dimensioned
      float[number_of_range_components][number_of_range_samples];
      the order of range values must be the same as the order of domain
      indices in the DomainSet */
  void setSamples(float[][] range)
         throws VisADException, RemoteException;

  /** get the domain Set */
  Set getDomainSet() throws VisADException, RemoteException;

  /** get number of samples */
  int getLength() throws VisADException, RemoteException;

  /** get the range value at the index-th sample */
  Data getSample(int index)
         throws VisADException, RemoteException;

  /** set the range value at the sample nearest to domain */
  void setSample(RealTuple domain, Data range)
         throws VisADException, RemoteException;

  /** set the range value at the sample nearest to domain */
  void setSample(RealTuple domain, Data range, boolean copy)
         throws VisADException, RemoteException;

  /** set the range value at the index-th sample */
  void setSample(int index, Data range)
         throws VisADException, RemoteException;

  /** set the range value at the index-th sample */
  void setSample(int index, Data range, boolean copy)
         throws VisADException, RemoteException;

  /** assumes the range type of this is a Tuple and returns
      a Field with the same domain as this, but whose range
      samples consist of the specified Tuple component of the
      range samples of this; in shorthand, this[].component */
  Field extract(int component)
         throws VisADException, RemoteException;

  /** combine domains of two outermost nested Fields into a single
      domain and Field */
  Field domainMultiply()
         throws VisADException, RemoteException;

  /** combine domains to depth, if possible */
  Field domainMultiply(int depth)
         throws VisADException, RemoteException;

  /** factor Field domain into domains of two nested Fields */
  Field domainFactor( RealType factor )
         throws VisADException, RemoteException;

  /** invokes getValues(true) */
  double[][] getValues()
         throws VisADException, RemoteException;

  /** get the 'Flat' components of this Field's range values
      in their default range Units (as defined by the range of
      the Field's FunctionType); if the range type is a RealType
      it is a 'Flat' component, if the range type is a TupleType
      its RealType components and RealType components of its
      RealTupleType components are all 'Flat' components; the
      return array is dimensioned:
      double[number_of_flat_components][number_of_range_samples];
      return a copy if copy == true */
  double[][] getValues(boolean copy)
         throws VisADException, RemoteException;

  /** invokes getFloats(true) */
  float[][] getFloats()
         throws VisADException, RemoteException;

  /** get the 'Flat' components of this Field's range values
      in their default range Units (as defined by the range of
      the Field's FunctionType); if the range type is a RealType
      it is a 'Flat' component, if the range type is a TupleType
      its RealType components and RealType components of its
      RealTupleType components are all 'Flat' components; the
      return array is dimensioned:
      float[number_of_flat_components][number_of_range_samples];
      return a copy if copy == true */
  float[][] getFloats(boolean copy)
         throws VisADException, RemoteException;

  /** get String values for Text components */
  String[][] getStringValues()
         throws VisADException, RemoteException;

  /** get default range Unit-s for 'Flat' components */
  Unit[] getDefaultRangeUnits()
         throws VisADException, RemoteException;

  /** get range Unit-s for 'Flat' components;
      second index may enumerate samples, if they differ */
  Unit[][] getRangeUnits()
         throws VisADException, RemoteException;

  /** get range CoordinateSystem for 'RealTuple' range;
      index may enumerate samples, if they differ */
  CoordinateSystem[] getRangeCoordinateSystem()
         throws VisADException, RemoteException;

  /** get range CoordinateSystem for 'RealTuple' components;
      index may enumerate samples, if they differ */
  CoordinateSystem[] getRangeCoordinateSystem(int component)
         throws VisADException, RemoteException;

  /** return true if this a FlatField or a RemoteField adapting a FlatField */
  boolean isFlatField()
         throws VisADException, RemoteException;


/**
<PRE>
   Here's how to use this:

   for (Enumeration e = field.domainEnumeration() ; e.hasMoreElements(); ) {
     RealTuple domain_sample = (RealTuple) e.nextElement();
     Data range = field.evaluate(domain_sample);
   }
</PRE>
*/
  Enumeration domainEnumeration()
         throws VisADException, RemoteException;

}

