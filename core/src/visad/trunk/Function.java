//
// Function.java
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

package visad;

import java.rmi.*;

/**
   Function is the interface for approximate implmentations
   of mathematical function.<P>
*/
public interface Function extends Data {

  /**
   * Get the dimension (number of Real components) of this Function's domain
   * @return  number of RealType components (n in R^n space)
   */
  int getDomainDimension()
         throws VisADException, RemoteException;

  /**
   * Get the default Units of the Real components of the domain.
   * @return  array of Unit-s in the same order as the RealTypes in the
   *          domain.
   */
  Unit[] getDomainUnits()
         throws VisADException, RemoteException;

  /**
   * Get the CoordinateSystem associated with the domain RealTuple
   * @return CoordinateSystem of the domain
   */
  CoordinateSystem getDomainCoordinateSystem()
         throws VisADException, RemoteException;

  /** 
   * Evaluate this Function at domain;  for 1-D domains
   * use default modes for resampling (Data.WEIGHTED_AVERAGE) and 
   * errors (Data.NO_ERRORS)
   * @param domain         value to evaluate at.
   * @return Data object corresponding to the function value at that domain.
   *         may return a missing data object of the same type as the
   *         Function's range.
   * @throws  VisADException   unable to evaluate function
   * @throws  RemoteException  Java RMI exception
   */
  Data evaluate(Real domain)
         throws VisADException, RemoteException;

  /** 
   * Evaluate this Function, for 1-D domains, with non-default modes 
   * for resampling and errors 
   * @param domain         value to evaluate at.
   * @param sampling_mode  type of interpolation to perform (e.g., 
   *                       Data.WEIGHTED_AVERAGE, Data.NEAREST_NEIGHBOR)
   * @param error_mode     type of error estimation to perform (e.g., 
   *                       Data.INDEPENDENT, Data.DEPENDENT, Data.NO_ERRORS)
   * @return Data object corresponding to the function value at that domain,
   *         using the sampling_mode and error_modes specified.
   *         May return a missing data object of the same type as the
   *         Function's range.
   * @throws  VisADException   unable to evaluate function
   * @throws  RemoteException  Java RMI exception
   */
  Data evaluate(Real domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException;

  /** 
   * Evaluate this Function at domain; use default modes for resampling 
   * (Data.WEIGHTED_AVERAGE) and errors (Data.NO_ERRORS)
   * @param domain         value to evaluate at.
   * @return Data object corresponding to the function value at that domain.
   *         may return a missing data object of the same type as the
   *         Function's range.
   * @throws  VisADException   unable to evaluate function
   * @throws  RemoteException  Java RMI exception
   */
  Data evaluate(RealTuple domain)
         throws VisADException, RemoteException;

  /** 
   * Evaluate this Function with non-default modes for resampling and errors 
   * @param domain         value to evaluate at.
   * @param sampling_mode  type of interpolation to perform (e.g., 
   *                       Data.WEIGHTED_AVERAGE, Data.NEAREST_NEIGHBOR)
   * @param error_mode     type of error estimation to perform (e.g., 
   *                       Data.INDEPENDENT, Data.DEPENDENT, Data.NO_ERRORS)
   * @return Data object corresponding to the function value at that domain,
   *         using the sampling_mode and error_modes specified.
   *         May return a missing data object of the same type as the
   *         Function's range.
   * @throws  VisADException   unable to evaluate function
   * @throws  RemoteException  Java RMI exception
   */
  Data evaluate(RealTuple domain, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  /** 
   * Return a Field of Function values at the samples in set
   * using default sampling_mode (WEIGHTED_AVERAGE) and
   * error_mode (NO_ERRORS);
   * This combines unit conversions, coordinate transforms,
   * resampling and interpolation 
   *
   * @param  set    finite sampling values for the function.
   * @return Data object corresponding to the function value at that domain.
   *         may return a missing data object of the same type as the
   *         Function's range.
   * @throws  VisADException   unable to resample function
   * @throws  RemoteException  Java RMI exception
   */
  Field resample(Set set) throws VisADException, RemoteException;

  /** 
   * Resample range values of this Function to domain samples in set;
   * return a Field (i.e., a finite sampling of a Function).  Use
   * the specified sampling_mode and error_mode.
   * This combines unit conversions, coordinate transforms,
   * resampling and interpolation 
   * @param set            finite sampling values for the function.
   * @param sampling_mode  type of interpolation to perform (e.g., 
   *                       Data.WEIGHTED_AVERAGE, Data.NEAREST_NEIGHBOR)
   * @param error_mode     type of error estimation to perform (e.g., 
   *                       Data.INDEPENDENT, Data.DEPENDENT, Data.NO_ERRORS)
   * @return Data object corresponding to the function value at that domain,
   *         using the sampling_mode and error_modes specified.
   *         May return a missing data object of the same type as the
   *         Function's range.
   * @throws  VisADException   unable to resample function
   * @throws  RemoteException  Java RMI exception
   */
  Field resample(Set set, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  /** return the derivative of this Function with respect to d_partial;
      d_partial may occur in this Function's domain RealTupleType, or,
      if the domain has a CoordinateSystem, in its Reference
      RealTupleType; propogate errors according to error_mode */
  Function derivative( RealType d_partial, int error_mode )
         throws VisADException, RemoteException;

  /** return the derivative of this Function with respect to d_partial;
      set result MathType to derivType; d_partial may occur in this
      Function's domain RealTupleType, or, if the domain has a
      CoordinateSystem, in its Reference RealTupleType;
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
