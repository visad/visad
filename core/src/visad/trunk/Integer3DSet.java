//
// Integer3DSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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
   Integer3DSet represents a finite set of samples of R^3 at
   an integer lattice based at the origin.<P>

   The order of the samples is the rasterization of the orders of
   the 1D components, with the first component increasing fastest.
   For more detail, see the description in Linear2DSet.java.<P>
*/
public class Integer3DSet extends Linear3DSet
       implements IntegerSet {

  public Integer3DSet(MathType type, Integer1DSet[] sets) throws VisADException {
    this(type, sets, null, null, null);
  }

  /** a 3-D set with null errors and generic type */
  public Integer3DSet(int length1, int length2, int length3)
         throws VisADException {
    this(RealTupleType.Generic3D,
         get_integer1d_array(RealTupleType.Generic3D, length1, length2,
                             length3, null),
         null, null, null);
  }

  public Integer3DSet(MathType type, int length1, int length2, int length3)
         throws VisADException {
    this(type, get_integer1d_array(type, length1, length2, length3, null),
         null, null, null);
  }

  public Integer3DSet(MathType type, Integer1DSet[] sets,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    super(type, sets, coord_sys, units, errors);
  }

  /** construct a 3-dimensional set with values {0, 1, ..., length1-1}
      x {0, 1, ..., length2-1} x {0, 1, ..., length3-1};
      coordinate_system and units must be compatible with defaults for
      type, or may be null; errors may be null */
  public Integer3DSet(MathType type, int length1, int length2, int length3,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, get_integer1d_array(type, length1, length2, length3, units),
         coord_sys, units, errors);
  }

  private static Integer1DSet[] get_integer1d_array(MathType type,
             int length1, int length2, int length3, Unit[] units)
          throws VisADException {
    type = Set.adjustType(type);
    Integer1DSet[] sets = new Integer1DSet[3];
    RealType[] types = new RealType[1];
    SetType set_type;
    Unit[] us = {null};

    types[0] = (RealType) ((SetType) type).getDomain().getComponent(0);
    set_type = new SetType(new RealTupleType(types));
    if (units != null && units.length > 0) us[0] = units[0];
    sets[0] = new Integer1DSet(set_type, length1, null, us, null);

    types[0] = (RealType) ((SetType) type).getDomain().getComponent(1);
    set_type = new SetType(new RealTupleType(types));
    if (units != null && units.length > 1) us[0] = units[1];
    sets[1] = new Integer1DSet(set_type, length2, null, us, null);

    types[0] = (RealType) ((SetType) type).getDomain().getComponent(2);
    set_type = new SetType(new RealTupleType(types));
    if (units != null && units.length > 2) us[0] = units[2];
    sets[2] = new Integer1DSet(set_type, length3, null, us, null);

    return sets;
  }

  public Object cloneButType(MathType type) throws VisADException {
    Integer1DSet[] sets = {(Integer1DSet) X.clone(),
                           (Integer1DSet) Y.clone(),
                           (Integer1DSet) Z.clone()};
    return new Integer3DSet(type, sets, DomainCoordinateSystem,
                            SetUnits, SetErrors);
  }

  public String longString(String pre) throws VisADException {
    String s = pre + "Integer3DSet: Length = " + Length + "\n";
    s = s + pre + "  Dimension 1: Length = " + X.getLength() + "\n";
    s = s + pre + "  Dimension 2: Length = " + Y.getLength() + "\n";
    s = s + pre + "  Dimension 3: Length = " + Z.getLength() + "\n";
    return s;
  }

}

