//
// DoubleSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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
   DoubleSet represents the finite (but large) set of samples of
   R^dimension made by vectors of IEEE double precision floating
   point numbers.  DoubleSet objects are immutable.<P>

   DoubleSet cannot be used for the domain sampling of a Field.<P>
*/
public class DoubleSet extends SimpleSet {

  /**
   * construct a DoubleSet with null CoordinateSystem and Units
   * @param type MathType for this DoubleSet, must be SetType,
   *             RealTupleType or RealType
   * @throws VisADException a VisAD error occurred
   */
  public DoubleSet(MathType type) throws VisADException {
    this(type, null, null);
  }

  /** 
   * construct a DoubleSet with null CoordinateSystem and Units 
   * @param type MathType for this DoubleSet, must be SetType,
   *             RealTupleType or RealType
   * @param coord_sys CoordinateSystem for Set domain, must be
   *                  compatible with default for type
   * @param units array of Units for Real values in Set domain,
   *              must be compatible with defaults for type
   * @throws VisADException a VisAD error occurred
   */
  public DoubleSet(MathType type, CoordinateSystem coord_sys, Unit[] units)
         throws VisADException {
    super(type, coord_sys, units, null); // no ErrorEstimate for DoubleSet
  }

  /**
   * for DoubleSet, this always throws a SetException
   * @param index array of integer indices
   * @return float[domain_dimension][indices.length] array of
   *         Set values (but always throws SetException instead)
   * @throws VisADException a VisAD error occurred
   */
  public float[][] indexToValue(int[] index) throws VisADException {
    throw new SetException("DoubleSet.indexToValue");
  }

  /**
   * for DoubleSet, this always throws a SetException
   * @param value float[domain_dimension][number_of_values] array of
   *        Set values
   * @return array of integer indices (but always throws SetException
   *         instead)
   * @throws VisADException a VisAD error occurred
   */
  public int[] valueToIndex(float[][] value) throws VisADException {
    throw new SetException("DoubleSet.valueToIndex");
  }

  /**
   * for DoubleSet, this always throws a SetException
   * @param value float[domain_dimension][number_of_values] array of
   *        Set values
   * @param indices int[number_of_values][] array for returning Set
   *                indices
   * @param weights float[number_of_values][] array for returning
   *                weights
   * @throws VisADException a VisAD error occurred
   */
  public void valueToInterp(float[][] value, int[][] indices,
                            float weights[][]) throws VisADException {
    throw new SetException("DoubleSet.valueToInterp");
  }

  /**
   * for DoubleSet, this always throws a SetException
   * @return length of Set (but always throws SetException instead)
   * @throws VisADException a VisAD error occurred
   */
  public int getLength() throws VisADException {
    throw new SetException("DoubleSet.getLength");
  }

  /**
   * Indicates whether or not this instance is equal to an object
   * @param set the object in question.
   * @return <code>true</code> if and only if this instance equals set.
   */
  public boolean equals(Object set) {
    if (!(set instanceof DoubleSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    return (DomainDimension == ((DoubleSet) set).getDimension());
  }

  /**
   * @return false (a DoubleSet is never missing)
   */
  public boolean isMissing() {
    return false;
  }

  /**
   * Clones this instance.
   *
   * @return                      A clone of this instance.
   */
  public final Object clone() {
      /*
       * I (Steve Emmerson) believe that this implementation should return
       * "this" to reduce the memory footprint but Bill believes that doing so
       * would be counter-intuitive and might harm applications.
       */
      return super.clone();
  }

  /**
   * Clones this instance with a different MathType.
   *
   * @param type MathType for returned DoubleSet
   * @return                      A clone of this instance.
   */
  public Object cloneButType(MathType type) throws VisADException {
    return new DoubleSet(type, DomainCoordinateSystem, SetUnits);
  }

  /**
   * @param pre String added to start of each line
   * @return a longer String than returned by toString(),
   *         indented by pre (a string of blanks)
   */
  public String longString(String pre) throws VisADException {
    return pre + "DoubleSet: Dimension = " + DomainDimension + "\n";
  }

}

