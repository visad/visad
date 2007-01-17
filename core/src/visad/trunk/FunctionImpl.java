//
// FunctionImpl.java
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
   FunctionImpl is the abstract superclass for approximate
   implmentations of mathematical functions.  It inherits
   the Function interface.<P>
*/
public abstract class FunctionImpl extends DataImpl implements Function {

  /**
   * Construct a new FunctionImpl with the supplied FunctionType.
   * @param type  FunctionType used to define the structure
   */
  public FunctionImpl(FunctionType type) {
    super(type);
  }

  /**
   * Get the dimension (number of RealType components) of this Function's domain
   * @return  number of RealType components (n in R^n space)
   */
  public int getDomainDimension() {
     return ((FunctionType) Type).getDomain().getDimension();
  }

  /**
   * Get the default Units of the Real components of the domain.
   * @return  array of Unit-s in the same order as the RealTypes in the
   *          domain.
   */
  public Unit[] getDomainUnits() {
     return ((FunctionType) Type).getDomain().getDefaultUnits();
  }

  /**
   * Get the CoordinateSystem associated with the domain RealTuple
   * @return CoordinateSystem of the domain
   */
  public CoordinateSystem getDomainCoordinateSystem() {
    return ((FunctionType) Type).getDomain().getCoordinateSystem();
  }

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
  public Data evaluate(Real domain)
         throws VisADException, RemoteException {
    if (domain == null) {
      return ((FunctionType) getType()).getRange().missingData();
    }
    CoordinateSystem domainCoordinateSystem = getDomainCoordinateSystem();
    RealTuple domainPoint =
      domainCoordinateSystem == null
	? new RealTuple(new Real[] {domain})
	: new RealTuple(
	    new RealTupleType(
	      (RealType)domain.getType(), domainCoordinateSystem, null),
	    new Real[] {domain},
	    (CoordinateSystem)null);
    return evaluate(domainPoint, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);
    // return evaluate(domainPoint, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
  }

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
  public Data evaluate(Real domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (domain == null) {
      return ((FunctionType) getType()).getRange().missingData();
    }
    CoordinateSystem domainCoordinateSystem = getDomainCoordinateSystem();
    RealTuple domainPoint =
      domainCoordinateSystem == null
	? new RealTuple(new Real[] {domain})
	: new RealTuple(
	    new RealTupleType(
	      (RealType)domain.getType(), domainCoordinateSystem, null),
	    new Real[] {domain},
	    (CoordinateSystem)null);
    return evaluate(domainPoint, sampling_mode, error_mode);
  }

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
  public Data evaluate(RealTuple domain)
         throws VisADException, RemoteException {
    if (domain == null) {
      return ((FunctionType) getType()).getRange().missingData();
    }
    return evaluate(domain, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);
    // return evaluate(domain, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
  }

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
  public Data evaluate(RealTuple domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (domain == null) {
      return ((FunctionType) getType()).getRange().missingData();
    }
    Field field = resample(new SingletonSet(domain, domain.getCoordinateSystem(),
                           domain.getTupleUnits(), domain.getErrors()),
                           sampling_mode, error_mode);
    return field.getSample(0);
  }

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
  public Field resample(Set set) throws VisADException, RemoteException {
    return resample(set, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);
  }

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
  public abstract Field resample(Set set, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public abstract Data derivative( RealTuple location, RealType[] d_partial_s,
                                   MathType[] derivType_s, int error_mode )
         throws VisADException, RemoteException;

  public abstract Data derivative( int error_mode )
         throws VisADException, RemoteException;

  public abstract Data derivative( MathType[] derivType_s, int error_mode )
         throws VisADException, RemoteException;

  public abstract Function derivative( RealType d_partial, int error_mode )
         throws VisADException, RemoteException;

  public abstract Function derivative( RealType d_partial, MathType derivType,
                                       int error_mode )
         throws VisADException, RemoteException;

  /**
   * A wrapper around {@link #evaluate(Real) evaluate} for JPython.
   */
  public Data __getitem__(Real domain) throws VisADException, RemoteException {
    return evaluate(domain);
  }

  /**
   * A wrapper around {@link #evaluate(RealTuple) evaluate} for JPython.
   */
  public Data __getitem__(RealTuple domain) throws VisADException, RemoteException {
    return evaluate(domain);
  }

}

