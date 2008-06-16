//
// SimpleSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

/**
 * SimpleSet is the abstract superclass of Sets with a unique ManifoldDimension.
 * For the most part, SimpleSet objects are immutable (but see {@link 
 * SampledSet#getSamples(boolean)}).
 */
public abstract class SimpleSet extends Set implements SimpleSetIface {

  /** dimension of subspace that set is embedded in */
  int ManifoldDimension;

  public SimpleSet(MathType type, int manifold_dimension) throws VisADException {
    this(type, manifold_dimension, null, null, null);
/* WLH 17 Oct 97
    super(type);
    ManifoldDimension = manifold_dimension;
    if (ManifoldDimension > DomainDimension ||
        ManifoldDimension < 0) {
      throw new SetException("SimpleSet: bad ManifoldDimension" +
                             ManifoldDimension);
    }
*/
  }

  public SimpleSet(MathType type, int manifold_dimension,
                   CoordinateSystem coord_sys, Unit[] units,
                   ErrorEstimate[] errors)
         throws VisADException {
    super(type, coord_sys, units, errors);
    ManifoldDimension = manifold_dimension;
    if (ManifoldDimension > DomainDimension ||
        ManifoldDimension < 0) {
      throw new SetException("SimpleSet: bad ManifoldDimension" +
                             ManifoldDimension);
    }
  }

  public SimpleSet(MathType type) throws VisADException {
    this(type, null, null, null);
  }

  public SimpleSet(MathType type, CoordinateSystem coord_sys, Unit[] units,
                   ErrorEstimate[] errors) throws VisADException {
    super(type, coord_sys, units, errors);
    ManifoldDimension = DomainDimension;
  }

  /** get ManifoldDimension */
  public int getManifoldDimension() {
    return ManifoldDimension;
  }

  /** domain == true is this is the domain of a Field */
  void setAnimationSampling(ShadowType type, DataShadow shadow, boolean domain)
       throws VisADException {
    if (shadow.isAnimationSampling(domain)) return;
    if (DomainDimension != 1) return;
    ShadowRealType real;
    if (type instanceof ShadowRealType) {
      real = (ShadowRealType) type;
    }
    else if (type instanceof ShadowSetType) {
      ShadowRealTupleType tuple = ((ShadowSetType) type).getDomain();
      if (tuple.getDimension() != 1) return; // should throw an Exception
      real = (ShadowRealType) tuple.getComponent(0);
    }
    else if (type instanceof ShadowRealTupleType) {
      // should throw an Exception
      if (((ShadowRealTupleType) type).getDimension() != 1) return;
      real = (ShadowRealType) ((ShadowRealTupleType) type).getComponent(0);
    }
    else {
      return;
    }
    Enumeration maps = real.getSelectedMapVector().elements();
    while (maps.hasMoreElements()) {
      ScalarMap map = (ScalarMap) maps.nextElement();
      if (map.getDisplayScalar().equals(Display.Animation)) {
        shadow.setAnimationSampling(this, domain);
        return;
      }
    }
  }

  /** convert an array of values to arrays of indices and weights for
      those indices, appropriate for interpolation; the values array is
      organized as float[domain_dimension][number_of_values]; indices
      and weights must be passed in as int[number_of_values][] and
      float[number_of_values][]; on return, quantity( values[.][i] )
      can be estimated as the sum over j of
      weights[i][j] * quantity (sample at indices[i][j]);
      no estimate possible if indices[i] and weights[i] are null */
  public abstract void valueToInterp(float[][] value, int[][] indices,
                          float weights[][]) throws VisADException;

}

