
//
// Integer3DSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

  public Integer3DSet(MathType type, int length1, int length2, int length3)
         throws VisADException {
    this(type, get_integer1d_array(type, length1, length2, length3),
         null, null, null);
  }

  public Integer3DSet(MathType type, Integer1DSet[] sets,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    super(type, sets, coord_sys, units, errors);
  }

  public Integer3DSet(MathType type, int length1, int length2, int length3,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, get_integer1d_array(type, length1, length2, length3),
         coord_sys, units, errors);
  }

  private static Integer1DSet[] get_integer1d_array(MathType type, int length1,
                           int length2, int length3) throws VisADException {
    type = Set.adjustType(type);
    Integer1DSet[] sets = new Integer1DSet[3];
    RealType[] types = new RealType[1];
    SetType set_type;

    types[0] = (RealType) ((SetType) type).getDomain().getComponent(0);
    set_type = new SetType(new RealTupleType(types));
    sets[0] = new Integer1DSet(set_type, length1);

    types[0] = (RealType) ((SetType) type).getDomain().getComponent(1);
    set_type = new SetType(new RealTupleType(types));
    sets[1] = new Integer1DSet(set_type, length2);

    types[0] = (RealType) ((SetType) type).getDomain().getComponent(2);
    set_type = new SetType(new RealTupleType(types));
    sets[2] = new Integer1DSet(set_type, length3);

    return sets;
  }

  public Object clone() {
    try {
      Integer1DSet[] sets = {(Integer1DSet) X.clone(),
                             (Integer1DSet) Y.clone(),
                             (Integer1DSet) Z.clone()};
      return new Integer3DSet(Type, sets, DomainCoordinateSystem,
                              SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Integer3DSet.clone: " + e.toString());
    }
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

