
//
// IntegerNDSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

/**
   IntegerNDSet represents a finite set of samples of R^n at
   an integer lattice based at the origin.<P>
*/
public class IntegerNDSet extends LinearNDSet
       implements IntegerSet {

  public IntegerNDSet(int[] lengths) throws VisADException {
    this(get_generic_type(lengths), lengths, null, null, null);
  }

  public IntegerNDSet(MathType type, int[] lengths) throws VisADException {
    this(type, lengths, null, null, null);
  }

  public IntegerNDSet(MathType type, int[] lengths,
                    CoordinateSystem coord_sys, Unit[] units,
                    ErrorEstimate[] errors) throws VisADException {
    super(type, LinearNDSet.get_linear1d_array(type, get_firsts(lengths),
          get_lasts(lengths), lengths), coord_sys, units, errors);
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

  public Object clone() {
    try {
      int[] lens = new int[DomainDimension];
      for (int j=0; j<DomainDimension; j++) {
        lens[j] = L[j].getLength();
      }
      return new IntegerNDSet(Type, lens, DomainCoordinateSystem,
                            SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("IntegerNDSet.clone: " + e.toString());
    }
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

