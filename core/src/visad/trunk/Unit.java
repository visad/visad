
//
// Unit.java
//

/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Unit.java,v 1.16 2000-04-24 22:50:06 steve Exp $
 */

package visad;

import java.io.Serializable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A class that represents a unit of a quantity.
 * @author Steven R. Emmerson
 *
 * This is part of Steve Emerson's Unit package that has been
 * incorporated into VisAD.
 */
public abstract class Unit
       implements Serializable
{
  /**
   * The identifier (name or abbreviation) for this unit.
   * @serial
   */
  private final String		identifier;

  /**
   * The identifier -> unit map.
   */
  private static final Map	identifierMap = new WeakHashMap();

/*
   added by Bill Hibbard for VisAD
*/

  /** convert a tuple of value arrays (a double[][]) */
  public static double[][] convertTuple(double[][] value, Unit[] units_in,
         Unit[] units_out) throws VisADException {
    double[][] new_value = new double[value.length][];
    for (int i=0; i<value.length; i++) {
      if (units_out[i] == null) {
        if (units_in[i] != null && !(units_in[i] instanceof PromiscuousUnit)) {
          throw new UnitException("Unit.convertTuple: illegal Unit conversion");
        }
        new_value[i] = value[i];
      }
      else {
        new_value[i] = units_out[i].toThis(value[i], units_in[i]);
      }
    }
    return new_value;
  }

  /** convert a tuple of value arrays (a float[][]) */
  public static float[][] convertTuple(float[][] value, Unit[] units_in,
         Unit[] units_out) throws VisADException {
    float[][] new_value = new float[value.length][];
    for (int i=0; i<value.length; i++) {
      if (units_out[i] == null) {
        if (units_in[i] != null && !(units_in[i] instanceof PromiscuousUnit)) {
          throw new UnitException("Unit.convertTuple: illegal Unit conversion");
        }
        new_value[i] = value[i];
      }
      else {
        new_value[i] = units_out[i].toThis(value[i], units_in[i]);
      }
    }
    return new_value;
  }

  /** return true if unita and unitb are convertable;
      canConvert & canConvertArray are static in order to
      be able to test null Unit-s, which are 'missing'
      (i.e., unknown) Unit-s;
      Unit.promiscuous is convertable with any Unit, but
      not compatible with non-promiscuous default Unit-s;
      thus this method does not reflect open convertability
      of Unit.promiscuous */
  public static boolean canConvert(Unit unita, Unit unitb) {
    if (CommonUnit.promiscuous.equals(unita)) unita = null;
    if (CommonUnit.promiscuous.equals(unitb)) unitb = null;
    if (unita == null && unitb == null) return true;
    if (unita == null || unitb == null) return false;
    // WLH - real logic goes here
    return unita.isConvertible(unitb);
  }

  /**
   * Indicate whether this unit is convertible with another unit.  If one unit
   * is convertible with another, then the <code>toThis(...)</code>/ and
   * <code>toThat(...)</code> methods will not throw a UnitException.  Unit A
   * is convertible with unit B if and only if unit B is convertible with unit
   * A; hence, calling-order is irrelevant.
   *
   * @param unit	The other unit.
   * @return		True if and only if this unit is convertible with the
   *			other unit.
   */
  public abstract boolean isConvertible(Unit unit);

  /** apply canConvert elementwise to two Unit arrays */
  public static boolean canConvertArray(Unit[] unita, Unit[] unitb) {
    if (unita == null && unitb == null) return true;
    if (unita == null) unita = new Unit[unitb.length];
    if (unitb == null) unitb = new Unit[unita.length];
    int n = unita.length;
    if (n != unitb.length) return false;
    for (int i=0; i<n; i++) {
      if (!canConvert(unita[i], unitb[i])) {
// System.out.println("i = " + i + " " + unita[i] + " != " + unitb[i]);
        return false;
      }
    }
    return true;
  }
 
  /** copy a Unit[] array;
      this is a helper for Set, RealTupleType, CoordinateSystem, etc */
  public static Unit[] copyUnitsArray(Unit[] units) {
    return units == null ? null : (Unit[])units.clone();
  }
 
  public abstract boolean equals(Unit unit);
 
  /** transform Units; unit_in and error_in are the Unit and ErrorEstimate
      associated with value; unit_out is the target Unit;
      value is the array of values to transform; return new value array;
      return transformed ErrorEstimates in errors_out array; */
  public static double[] transformUnits(
                        Unit unit_out, ErrorEstimate[] errors_out,
                        Unit unit_in, ErrorEstimate error_in,
                        double[] value) throws VisADException {
 
    if (unit_out == null || unit_in == null) {
      errors_out[0] = error_in;
      return value;
    }
    else {
      // convert value array
      double[] val = unit_out.toThis(value, unit_in);
 
      // construct new ErrorEstimate, if needed
      if (error_in == null) {
        errors_out[0] = null;
      }
      else {
        // scale data.ErrorEstimate for Unit.toThis
        double error = 0.5 * error_in.getErrorValue();
        double mean = error_in.getMean();
        double new_error =
          Math.abs( unit_out.toThis(mean + error, unit_in) -
                    unit_out.toThis(mean - error, unit_in) );
        errors_out[0] = new ErrorEstimate(val, new_error, unit_out);
      }
 
      // return value array
      return val;
    }
  }

  public static float[] transformUnits(
                        Unit unit_out, ErrorEstimate[] errors_out,
                        Unit unit_in, ErrorEstimate error_in,
                        float[] value) throws VisADException {
 
    if (unit_out == null || unit_in == null) {
      errors_out[0] = error_in;
      return value;
    }
    else {
      // convert value array
      float[] val = unit_out.toThis(value, unit_in);
 
      // construct new ErrorEstimate, if needed
      if (error_in == null) {
        errors_out[0] = null;
      }
      else {
        // scale data.ErrorEstimate for Unit.toThis
        double error = 0.5 * error_in.getErrorValue();
        double mean = error_in.getMean();
        double new_error =
          Math.abs( unit_out.toThis(mean + error, unit_in) -
                    unit_out.toThis(mean - error, unit_in) );
        errors_out[0] = new ErrorEstimate(val, new_error, unit_out);
      }
 
      // return value array
      return val;
    }
  }

  Unit scale(double amount, boolean b) {
    return new ScaledUnit(amount, (BaseUnit)this);
  }

/*
   end of added by Bill Hibbard for VisAD
*/

    /**
     * Constructs from nothing.
     */
    protected Unit()
    {
      this.identifier = null;
    }

    /**
     * Constructs from an identifier.
     * @param identifier	Name or abbreviation for the unit.  May be
     *				<code>null</code> or empty.
     */
    protected Unit(String identifier)
    {
      try
      {
	identifier = adjustCheckAndCache(identifier);
      }
      catch (UnitExistsException e)
      {
	System.err.println("WARNING: " + e);
      }
      this.identifier = identifier;
    }

    /**
     * Adjusts, checks, and caches a unit identifier and its unit.
     * @param identifier	Name or abbreviation for the unit.  May be
     *				<code>null</code> or empty.
     * @param unit		The unit to be associated with the identifier.
     * @throws UnitExistsException
     *				A different unit with the same, non-null and
     *				non-empty identifier already exists.  The
     *				identifier and unit are not cached.
     */
    protected final String
    adjustCheckAndCache(String identifier)
      throws UnitExistsException
    {
      if (identifier != null && identifier.length() > 0)
      {
	identifier = identifier.replace(' ', '_');	// ensure no whitespace
	/*
	synchronized(identifierMap)
	{
	  Unit	previous = (Unit)identifierMap.get(identifier);
	  if (previous != null)
	    throw new UnitExistsException(identifier);
	  identifierMap.put(identifier, this);
	}
	*/
      }
      return identifier;
    }

    /**
     * Clones this unit, changing the identifier.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.
     * @throws UnitException	The unit may not be cloned.  This will only
     *				occur if <code>getIdentifier()!=null</code>.
     */
    public Unit clone(String identifier)
      throws UnitException
    {
      return protectedClone(adjustCheckAndCache(identifier));
    }

    /**
     * Clones this unit, changing the identifier.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.  It shall
     *				have already passed the
     *				adjustCheckAndCache() method.
     * @throws UnitException	The unit may not be cloned.  This will only
     *				occur if <code>getIdentifier()!=null</code>.
     */
    protected abstract Unit protectedClone(String identifier)
      throws UnitException;

    /**
     * Raise this unit to a power.
     *
     * @param power	The power to raise this unit by.
     * @return		The resulting unit.
     * @require		The unit is not an offset unit.
     * @promise		The unit has not been modified.
     * @exception	UnitException	It's meaningless to raise this unit 
     *					by a power.
     */
    public abstract Unit pow(int power)
	throws UnitException;

    /**
     * Raise a unit to a power.
     *
     * @param power	The power to raise this unit by.  If this unit is
     *			not dimensionless, then the value must be integral.
     * @return		The unit resulting from raising this unit to 
     *			<code>power</code>.
     * @throws UnitException	It's meaningless to raise this unit by a power.
     * @throws IllegalArgumentException
     *			This unit is not dimensionless and <code>power</code>
     *			has a non-integral value.
     * @promise		The unit has not been modified.
     */
    public abstract Unit pow(double power)
	throws UnitException, IllegalArgumentException;

    /**
     * Scale this unit by an amount.
     *
     * @param amount	The amount by which to scale this unit.  E.g.
     *			Unit yard = meter.scale(0.9144);
     * @exception	UnitException	This unit cannot be scaled.
     */
    public Unit scale(double amount)
	throws UnitException
    {
	if (this instanceof BaseUnit)
	    return new ScaledUnit(amount, (BaseUnit)this);
	if (this instanceof DerivedUnit)
	    return new ScaledUnit(amount, (DerivedUnit)this);
	if (this instanceof ScaledUnit)
	    return new ScaledUnit(amount, (ScaledUnit)this);
	if (this instanceof OffsetUnit)
	    return new OffsetUnit(((OffsetUnit)this).offset/amount, 
		new ScaledUnit(amount, ((OffsetUnit)this).scaledUnit));

	throw new UnitException("Unknown unit subclass: " + this);
    }

    /**
     * Shift this unit by an amount.
     *
     * @param offset	The amount by which to shift this unit.  E.g.
     *			Unit celsius = kelvin.shift(273.15);
     * @exception	UnitException	The unit subclass is unknown.
     */
    public Unit shift(double offset)
	throws UnitException
    {
	Unit	unit;
	if (this instanceof BaseUnit)
	    unit = new OffsetUnit(offset, (BaseUnit)this);
	else if (this instanceof DerivedUnit)
	    unit = new OffsetUnit(offset, (DerivedUnit)this);
	else if (this instanceof ScaledUnit)
	    unit = new OffsetUnit(offset, (ScaledUnit)this);
	else if (this instanceof OffsetUnit)
	    unit = new OffsetUnit(offset, (OffsetUnit)this);
	else
	{
	    throw new UnitException(
		"Unit.shift(): Unknown unit subclass: " + this);
	}
	if (this.isConvertible(SI.second))
	    unit = TimeScaleUnit.instance((OffsetUnit)unit);

	return unit;
    }

    /**
     * Multiply this unit by another unit.
     *
     * @param that		The given unit to multiply this unit by.  
     * @return			The resulting unit.
     * @throws UnitException	It's meaningless to divide these units.
     */
    public abstract Unit multiply(Unit that)
	throws UnitException;

    /**
     * Divide this unit by another unit.
     *
     * @param that		The unit to divide into this unit.
     * @return			The quotient of the two units.
     * @promise			Neither unit has been modified.
     * @throws UnitException	It's meaningless to divide these units.
     */
    public abstract Unit divide(Unit that)
	throws UnitException;

    /**
     * Divide this unit into another unit.
     *
     * @param that		The unit to be divided by this unit.
     * @return			The quotient of the two units.
     * @throws UnitException	It's meaningless to divide these units.
     */
    protected abstract Unit divideInto(Unit that)
	throws UnitException;

    /**
     * Convert a value to this unit from another unit.
     *
     * @param value	The value in units of the other unit.
     * @param that	The other unit.
     * @return		The value converted from the other unit to this unit.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	UnitException	The units are not convertible.
     */
    public double toThis(double value, Unit that)
	throws UnitException
    {
	return toThis(new double[] {value}, that)[0];
    }

    /**
     * Convert values to this unit from another unit.
     *
     * @param values	Values in units of the other unit.
     * @param that	The other unit.
     * @return		Values in this unit.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	UnitException	The units are not convertible.
     */
    public abstract double[] toThis(double[] values, Unit that)
           throws UnitException;

    /**
     * Convert values to this unit from another unit.
     *
     * @param values	Values in units of the other unit.
     * @param that	The other unit.
     * @return		Values in this unit.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	UnitException	The units are not convertible.
     */
    public abstract float[] toThis(float[] values, Unit that)
           throws UnitException;

    /**
     * Convert a value from this unit to another unit.
     *
     * @param value	The value in this unit.
     * @param that	The other unit.
     * @return		The value in units of the other unit.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	UnitException	The units are not convertible.
     */
    public double toThat(double value, Unit that)
	throws UnitException
    {
	return toThat(new double[] {value}, that)[0];
    }

    /**
     * Convert values from this unit to another unit.
     *
     * @param values	The values in this unit.
     * @param that	The other unit.
     * @return		Values converted to the other unit from this unit.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	UnitException	The units are not convertible.
     */
    public abstract double[] toThat(double[] values, Unit that)
           throws UnitException;

    /**
     * Convert values from this unit to another unit.
     *
     * @param values	The values in this unit.
     * @param that	The other unit.
     * @return		Values converted to the other unit from this unit.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	UnitException	The units are not convertible.
     */
    public abstract float[] toThat(float[] values, Unit that)
           throws UnitException;

    /**
     * Returns a string representation of this unit.
     *
     * @return		The string representation of this unit.  Won't be 
     *			<code>null</code> but may be empty.
     */
    public final String toString()
    {
      String	s = getIdentifier();
      if (s == null)
	s = getDefinition();
      return s;
    }

    /**
     * Returns the identifier (name or abbreviation) of this unit.
     *
     * @return		The identifier of this unit.  May be <code>null</code>
     *			but won't be empty.
     */
    public final String getIdentifier()
    {
      return identifier;
    }

    /**
     * Returns the definition of this unit.
     *
     * @return		The definition of this unit.  Won't be <code>null
     *			</code> but may be empty.
     */
    public abstract String getDefinition();

    /**
     * Gets the absolute unit of this unit.  An interval in the underlying
     * physical quantity has the same numeric value in an absolute unit of a
     * unit as in the unit itself -- but an absolute unit is always referenced
     * to the physical origin of the underlying physical quantity.  For
     * example, the absolute unit corresponding to degrees celsius is degrees
     * kelvin -- and calling this method on a degrees celsius unit obtains a
     * degrees kelvin unit.
     * @return		The absolute unit corresponding to this unit.
     */
    public Unit
    getAbsoluteUnit()
    {
      return this;
    }
}
