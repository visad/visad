//
// Function.java
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

import java.util.*;
import java.rmi.*;

/**
   Function is the interface for approximate implmentations
   of mathematical function.<P>
*/
public interface Function extends Data {

  /** get dimension of Function domain */
  int getDomainDimension()
         throws VisADException, RemoteException;

  /** get Units of domain Real components */
  Unit[] getDomainUnits()
         throws VisADException, RemoteException;

  /** get domain CoordinateSystem */
  CoordinateSystem getDomainCoordinateSystem()
         throws VisADException, RemoteException;

  /** evaluate this Function at domain, for 1-D domains;
      use default modes for resampling (Data.NEAREST_NEIGHBOR) and
      errors (Data.NO_ERRORS) */
  Data evaluate(Real domain)
         throws VisADException, RemoteException;

  /** evaluate this Function, for 1-D domains, with non-default modes for
      resampling and errors */
  Data evaluate(Real domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException;

  /** evaluate this Function at domain; first check that types match;
      use default modes for resampling (Data.NEAREST_NEIGHBOR) and
      errors (Data.NO_ERRORS) */
  Data evaluate(RealTuple domain)
         throws VisADException, RemoteException;

  /** evaluate this Function with non-default modes for resampling and errors */
  Data evaluate(RealTuple domain, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  /** return a Field of Function values at the samples in set;
      this combines unit conversions, coordinate transforms,
      resampling and interpolation */
  Field resample(Set set, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  /** return the derivative of this Function with respect to d_partial;
      d_partial may occur in this Function's domain RealTupleType, or,
      if the domain has a CoordinateSystem, in its Reference
      RealTupleType; propogate errors accoridng to error_mode;
      propogate errors according to error_mode */
  Function derivative( RealType d_partial, int error_mode )
         throws VisADException, RemoteException;
 
  /** return the derivative of this Function with respect to d_partial;
      set result MathType to derivType; d_partial may occur in this
      Function's domain RealTupleType, or, if the domain has a
      CoordinateSystem, in its Reference RealTupleType;
      propogate errors accoridng to error_mode;
      propogate errors according to error_mode */
  Function derivative( RealType d_partial, MathType derivType,
                                       int error_mode)
         throws VisADException, RemoteException;

  /** return the tuple of derivatives of this Function with respect to
      all RealType components of its domain RealTuple;
      propogate errors according to error_mode */
  Data derivative( int error_mode )
         throws VisADException, RemoteException;

  /** return the tuple of derivatives of this Function with respect to
      all RealType components of its domain RealTuple;
      set result MathTypes of tuple components to derivType_s;
      propogate errors according to error_mode */
  Data derivative( MathType[] derivType_s, int error_mode )
         throws VisADException, RemoteException;

  /** return the tuple of derivatives of this Function with respect
      to the RealTypes in d_partial_s; the RealTypes in d_partial_s
      may occur in this Function's domain RealTupleType, or, if the
      domain has a CoordinateSystem, in its Reference RealTupleType;
      set result MathTypes of tuple components to derivType_s;
      propogate errors according to error_mode */
  Data derivative( RealTuple location, RealType[] d_partial_s,
                                   MathType[] derivType_s, int error_mode )
         throws VisADException, RemoteException;

}

