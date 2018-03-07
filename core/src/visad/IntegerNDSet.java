//
// IntegerNDSet.java
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

/**
   IntegerNDSet represents a finite set of samples of R^n at
   an integer lattice based at the origin.<P>
*/
public class IntegerNDSet extends LinearNDSet
       implements IntegerSet {

  /** an N-D set with null errors and generic type */
  public IntegerNDSet(int[] lengths) throws VisADException {
    this(get_generic_type(lengths), lengths, null, null, null);
  }

  public IntegerNDSet(MathType type, int[] lengths) throws VisADException {
    this(type, lengths, null, null, null);
  }

  /** construct an N-dimensional set with values in the cross product
      of {0, 1, ..., lengths[i]-1}
      for i=0, ..., lengths[lengths.length-1];
      coordinate_system and units must be compatible with defaults for
      type, or may be null; errors may be null */
  public IntegerNDSet(MathType type, int[] lengths,
                    CoordinateSystem coord_sys, Unit[] units,
                    ErrorEstimate[] errors) throws VisADException {
    super(type, LinearNDSet.get_linear1d_array(type, get_firsts(lengths),
                                               get_lasts(lengths), lengths,
                                               units),
          coord_sys, units, errors);
  }

  public IntegerNDSet(MathType type, Integer1DSet[] sets) throws VisADException {
    this(type, sets, null, null, null);
  }

  /** construct an N-dimensional set with values in the cross product
      of {0, 1, ..., lengths[i]-1}
      for i=0, ..., lengths[lengths.length-1];
      coordinate_system and units must be compatible with defaults for
      type, or may be null; errors may be null */
  public IntegerNDSet(MathType type, Integer1DSet[] sets,
                    CoordinateSystem coord_sys, Unit[] units,
                    ErrorEstimate[] errors) throws VisADException {
    super(type, sets, coord_sys, units, errors);
  }

  /**
   * Abreviated factory method for creating the proper integer set
   * (Integer1DSet, Integer2DSet, etc.).
   */
  public static GriddedSet create(MathType type, int[] lengths)
         throws VisADException {
    return create(type, lengths, null, null, null);
  }

  /**
   * General factory method for creating the proper integer set
   * (Integer1DSet, Integer2DSet, etc.).
   */
  public static GriddedSet create(MathType type, int[] lengths,
                                  CoordinateSystem coord_sys, Unit[] units,
                                  ErrorEstimate[] errors)
         throws VisADException {
    switch (lengths.length) {
      case 1:
        return new Integer1DSet(type, lengths[0],
                                coord_sys, units, errors);
      case 2:
        return new Integer2DSet(type, lengths[0], lengths[1],
                                coord_sys, units, errors);
      case 3:
        return new Integer3DSet(type, lengths[0], lengths[1], lengths[2],
                                coord_sys, units, errors);
      default:
        return new IntegerNDSet(type, lengths,
                              coord_sys, units, errors);
    }
  }

  private static SetType get_generic_type(int[] lengths)
          throws VisADException {
    if (lengths == null || lengths.length == 0) {
      throw new SetException("IntegerNDSet: bad lengths");
    }
    int n = lengths.length;
    RealType[] reals = new RealType[n];
    for (int i=0; i<n; i++) reals[i] = RealType.Generic;
    return new SetType(new RealTupleType(reals));
  }

  private static double[] get_firsts(int[] lengths) {
    double[] firsts = new double[lengths.length];
    for (int j=0; j<lengths.length; j++) firsts[j] = 0.0;
    return firsts;
  }

  private static double[] get_lasts(int[] lengths) {
    double[] lasts = new double[lengths.length];
    for (int j=0; j<lengths.length; j++) lasts[j] = (double) (lengths[j] - 1);
    return lasts;
  }

  private static int[] get_lengths(Real[] lengths) {
    int[] ss = new int[lengths.length];
    for (int j=0; j<lengths.length; j++) ss[j] = (int) lengths[j].getValue();
    return ss;
  }

  public Object cloneButType(MathType type) throws VisADException {
    int[] lens = new int[DomainDimension];
    for (int j=0; j<DomainDimension; j++) {
      lens[j] = L[j].getLength();
    }
    return new IntegerNDSet(type, lens, DomainCoordinateSystem,
                          SetUnits, SetErrors);
  }

  public String longString(String pre) throws VisADException {
    String s = pre + "IntegerNDSet: Dimension = " +
               DomainDimension + " Length = " + Length + "\n";
    for (int j=0; j<DomainDimension; j++) {
      // s = s + pre + "  Dimension " + j + ":" + " Length = " +
      //     L[j].getLength() + "\n";
      s = s + pre + "  Dimension " + j + ":" + " Linear1DSet = " + L[j] + "\n";
    }
    return s;
  }

}

