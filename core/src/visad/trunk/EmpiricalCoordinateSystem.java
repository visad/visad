//
// EmpiricalCoordinateSystem.java
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

/**
 * Provides support for empirically-defined CordinateSystem-s.  This is useful
 * for data-dependent coordinate transformations that must be determined
 * empirically rather than analytically (e.g. pressure <-> height).</p>
 *
 * <p>Coordinates in this system are termed "world" coordinates and coordinates
 * in the reference coordinate system are termed "reference" coordinates.</p>
 *
 * <p>Instances of this class are immutable.
 *
 * @author Steven R. Emmerson
 */
public final class
EmpiricalCoordinateSystem
  extends	CoordinateSystem
  implements	java.io.Serializable
{
  /**
   * The world/grid coordinate system.
   * @serial
   */
  private final GridCoordinateSystem	worldCS;

  /**
   * The reference/grid coordinate system.
   * @serial
   */
  private final GridCoordinateSystem	referenceCS;

  /**
   * Constructs from two GriddedSet-s.  The dimensionality (i.e. rank and
   * lengths) of the two sets must be identical.
   * @param world               A set of world coordinates.
   *                            <code>world.getLengths()</code> shall equal
   *                            <code>reference.getLengths()</code>.  Determines
   *                            the default units of world coordinates:
   *                            the default units will be those of <code>
   *                            world.getSetUnits()</code>.
   * @param reference           A set of reference coordinates.  Determines
   *                            the default units of reference coordinates:
   *                            the default units will be those of <code>
   *                            reference.getSetUnits()</code>.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public
  EmpiricalCoordinateSystem(GriddedSet world, GriddedSet reference)
    throws VisADException
  {
    super(((SetType)reference.getType()).getDomain(), world.getSetUnits());
    worldCS = new GridCoordinateSystem(ensureNoCoordinateSystem(world));
    referenceCS = new GridCoordinateSystem(ensureNoCoordinateSystem(reference));
  }

  /**
   * Ensures that a GriddedSet doesn't have a default CoordinateSystem.
   *
   * @param griddedSet		The GriddedSet to not have a CoordinateSystem.
   * @return                    The GriddedSet without a CoordinateSystem.
   *                            May be the original GriddedSet.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  protected static GriddedSet
  ensureNoCoordinateSystem(GriddedSet griddedSet)
    throws VisADException
  {
    if (griddedSet.getCoordinateSystem() != null)
    {
      /*
       * The GriddedSet has a CoordinateSystem.  Clone the GriddedSet without
       * the CoordinateSystem.
       */
      SetType		setType = (SetType)griddedSet.getType();
      RealTupleType	realTupleType = setType.getDomain();
      if (realTupleType.getCoordinateSystem() != null)
	setType =
	  new SetType(new RealTupleType(realTupleType.getRealComponents()));
      griddedSet =
	GriddedSet.create(
	  setType,
	  griddedSet.getSamples(),
	  griddedSet.getLengths(),
	  (CoordinateSystem)null,
	  griddedSet.getSetUnits(),
	  griddedSet.getSetErrors());
    }
    return griddedSet;
  }

  /**
   * Constructs an EmpiricalCoordinateSystem from a FlatField.
   *
   * @param field               The FlatField comprising a coordinate system
   *                            transformation from the domain to the range.
   *                            The <code>toReference()</code> method of the
   *                            resulting CoordinateSystem will transform
   *                            numeric values in units of the domain of
   *                            the field to numeric values in units of
   *                            the range of the field.  Similarly, the
   *                            <code>fromReference()</code> method will
   *                            transform numeric values in units of the range
   *                            of the field to numeric values in units of the
   *                            domain of the field.  The rank of the domain and
   *                            range shall be the same.  The domain set of the
   *                            field shall be a GriddedSet.
   * @throws SetException	The field's domain set isn't a GriddedSet.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public static EmpiricalCoordinateSystem
  create(FlatField field)
    throws VisADException
  {
    float[][]		samples = field.getFloats(false);// in default units
    Set			domainSet = field.getDomainSet();
    if (!(domainSet instanceof GriddedSet))
      throw new SetException("Domain set must be GriddedSet");
    Unit[][]		rangeUnits = field.getRangeUnits();
    Unit[]		rangeSetUnits = new Unit[rangeUnits.length];
    for (int i = rangeUnits.length; --i >= 0; )
      rangeSetUnits[i] = rangeUnits[i][0];
    return
      new EmpiricalCoordinateSystem(
	(GriddedSet)domainSet,
	GriddedSet.create(
	  ((FunctionType)field.getType()).getFlatRange(),
	  Unit.convertTuple(
	    samples, field.getDefaultRangeUnits(), rangeSetUnits),
	  new int[] {samples[0].length},
	  (CoordinateSystem)null,
	  rangeSetUnits,
	  (ErrorEstimate[])null));
  }

  /**
   * Returns the Set of world coordinates.
   * @return			The Set of world coordinates.
   */
  public GriddedSet
  getWorldSet()
  {
    return worldCS.getGriddedSet();
  }

  /**
   * Returns the Set of reference coordinates.
   * @return			The Set of reference coordinates.
   */
  public GriddedSet
  getReferenceSet()
  {
    return referenceCS.getGriddedSet();
  }

  /**
   * Gets the units of the reference coordinate system.  In general, these
   * units may differ from the default units of the RealTupleType returned by
   * <code>getReference()</code>.  In this aspect, this class differs from its
   * parent class.  Numeric values in the reference coordinate system shall be
   * in units of <code>getReferenceUnits()</code> unless specified otherwise.
   * @return			The units of the reference coordinate system.
   */
  public Unit[]
  getReferenceUnits()
  {
    return getReferenceSet().getSetUnits();
  }

  /**
   * Converts reference coordinates to world coordinates.
   * @param values              Numeric reference coordinates to be
   *                            converted to numeric world coordinates.
   *                            <code>values.length</code> shall
   *                            equal <code>getDimension()</code> and
   *                            <code>values[i].length</code> shall be the same
   *                            for all <code>i</code>.
   * @return                    <code>values</code> which now contains
   *                            world coordinates.  Values which could
   *                            not be converted will have the value
   *                            <code>Double.NaN</code>.
   * @throws SetException	Mismatch between input values and field domain.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public double[][]
  fromReference(double[][] values)
    throws VisADException
  {
    return worldCS.toReference(referenceCS.fromReference(values));
  }

  /**
   * Convert world coordinates to reference coordinates.
   * @param values              Numeric world coordinates to be converted
   *                            to numeric reference coordinates.
   *                            <code>values.length</code> shall
   *                            equal <code>getDimension()</code> and
   *                            <code>values[i].length</code> shall be the same
   *                            for all <code>i</code>.
   * @return                    <code>values</code> which now contains
   *                            world coordinates.  Values which could
   *                            not be converted will have the value
   *                            <code>Double.NaN</code>.
   * @throws SetException	Mismatch between input values and field domain.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public double[][]
  toReference(double[][] values)
    throws VisADException
  {
    return referenceCS.toReference(worldCS.fromReference(values));
  }

  /**
   * Indicates if this coordinate system is semantically identical to an
   * object.
   * @param object		The object in question.
   * @return			<code>true</code> if and only if this
   *				coordinate system is semantically identical to
   *				<code>object</code>.
   */
  public boolean
  equals(Object object)
  {
    if (!(object instanceof EmpiricalCoordinateSystem))
      return false;
    EmpiricalCoordinateSystem	that = (EmpiricalCoordinateSystem)object;
    return
      worldCS.equals(that.worldCS) && referenceCS.equals(that.referenceCS);
  }
}
