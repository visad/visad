
//
// Unit.java
//

/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Unit.java,v 1.9.2.2 1999-05-12 14:46:15 steve Exp $
 */

package visad;

import java.io.Serializable;

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
  /*
   * The identifier (name or abbreviation) for this unit.
   * @serial
   */
  private final String	identifier;

/*
   added by Bill Hibbard for VisAD
*/

  /** convert a tuple of value arrays (a double[][]) */
  public static double[][] convertTuple(double[][] value, Unit[] units_in,
         Unit[] units_out) throws VisADException {
    double[][] new_value = new double[value.length][];
    for (int i=0; i<value.length; i++) {
      if (units_out[i] == null) {
        if (units_in[i] != null) {
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
    try {
      unita.toThis(0.0, unitb);
    }
    catch (UnitException e) {
      return false;
    }
    return true;
  }
 
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
    if (units == null) return null;
    int n = units.length;
    Unit[] ret_units = new Unit[n];
    for (int i=0; i<n; i++) ret_units[i] = units[i];
    return ret_units;
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
      this(null);
    }

    /**
     * Constructs from an identifier.
     * @param identifier	Name or abbreviation for the unit.  May be
     *				<code>null</code> or empty.
     */
    protected Unit(String identifier)
    {
      this.identifier = identifier;
    }

    /**
     * Clones this unit, changing the identifier.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.
     * @throws UnitException	The unit may not be cloned.  This will only
     *				occur if <code>getIdentifier()!=null</code>.
     */
    public abstract Unit clone(String identifier) throws UnitException;

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
     * @exception	UnitException	The unit subclass is unknown.
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

	throw new UnitException("Unknown unit subclass");
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
	if (this instanceof BaseUnit)
	    return new OffsetUnit(offset, (BaseUnit)this);
	if (this instanceof DerivedUnit)
	    return new OffsetUnit(offset, (DerivedUnit)this);
	if (this instanceof ScaledUnit)
	    return new OffsetUnit(offset, (ScaledUnit)this);
	if (this instanceof OffsetUnit)
	    return new OffsetUnit(offset, (OffsetUnit)this);

	throw new UnitException("Unknown unit subclass");
    }

    /**
     * Multiply this unit by another unit.
     *
     * @param that	The given unit to multiply this unit by.  
     * @return		The resulting unit.
     * @require		This unit is not an offset unit.
     * @promise		Neither unit has been modified.
     * @exception	UnitException	It's meaningless to multiply these
     *					units together.
     */
    public Unit multiply(Unit that)
	throws UnitException
    {
/*
   added by Bill Hibbard for VisAD
*/
        if (this instanceof PromiscuousUnit) {
          return that;
        }
        else if (that instanceof PromiscuousUnit) {
          return this;
        }
/*
   end of added by Bill Hibbard for VisAD
*/
	if (that instanceof BaseUnit)
	    return multiply((BaseUnit)that);
	if (that instanceof DerivedUnit)
	    return multiply((DerivedUnit)that);
	if (that instanceof ScaledUnit)
	    return multiply((ScaledUnit)that);
	if (that instanceof OffsetUnit)
	    return multiply((OffsetUnit)that);
	throw new UnitException("Unknown unit subclass");
    }

    /**
     * Divide this unit by another unit.
     *
     * @param that      The unit to divide into this unit.
     * @return          The quotient of the two units.
     * @require		This unit is not an offset unit.
     * @promise		Neither unit has been modified.
     * @exception	UnitException	It's meaningless to divide these units.
     */
    public Unit divide(Unit that)
	throws UnitException
    {
/*
   added by Bill Hibbard for VisAD
*/
        if (this instanceof PromiscuousUnit) {
          return that;
        }
        else if (that instanceof PromiscuousUnit) {
          return this;
        }
/*
   end of added by Bill Hibbard for VisAD
*/
	if (that instanceof BaseUnit)
	    return divide((BaseUnit)that);
	if (that instanceof DerivedUnit)
	    return divide((DerivedUnit)that);
	if (that instanceof ScaledUnit)
	    return divide((ScaledUnit)that);
	if (that instanceof OffsetUnit)
	    return divide((OffsetUnit)that);
	throw new UnitException("Unknown unit subclass");
    }

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
    public double[] toThis(double[] values, Unit that)
           throws UnitException {
/*
   added by Bill Hibbard for VisAD
*/
        if ((this instanceof PromiscuousUnit) ||
            (that instanceof PromiscuousUnit)) {
          return values;
        }
/*
   end of added by Bill Hibbard for VisAD
*/
	if (that instanceof BaseUnit)
	    return toThis(values, (BaseUnit)that);
	else
	if (that instanceof DerivedUnit)
	    return toThis(values, (DerivedUnit)that);
	else
	if (that instanceof ScaledUnit)
	    return toThis(values, (ScaledUnit)that);
	else
	if (that instanceof OffsetUnit)
	    return toThis(values, (OffsetUnit)that);
	throw new UnitException("Unknown unit subclass " + that);
    }

    public float[] toThis(float[] values, Unit that)
           throws UnitException {
/*
   added by Bill Hibbard for VisAD
*/
        if ((this instanceof PromiscuousUnit) ||
            (that instanceof PromiscuousUnit)) {
          return values;
        }
/*
   end of added by Bill Hibbard for VisAD
*/
        if (that instanceof BaseUnit)
            return toThis(values, (BaseUnit)that);
        else
        if (that instanceof DerivedUnit)
            return toThis(values, (DerivedUnit)that);
        else
        if (that instanceof ScaledUnit)
            return toThis(values, (ScaledUnit)that);
        else
        if (that instanceof OffsetUnit)
            return toThis(values, (OffsetUnit)that);
        throw new UnitException("Unknown unit subclass");
    }

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
    public double[] toThat(double[] values, Unit that)
           throws UnitException {
/*
   added by Bill Hibbard for VisAD
*/
        if ((this instanceof PromiscuousUnit) ||
            (that instanceof PromiscuousUnit)) {
          return values;
        }
/*
   end of added by Bill Hibbard for VisAD
*/
	if (that instanceof BaseUnit)
	    return toThat(values, (BaseUnit)that);
	else
	if (that instanceof DerivedUnit)
	    return toThat(values, (DerivedUnit)that);
	else
	if (that instanceof ScaledUnit)
	    return toThat(values, (ScaledUnit)that);
	else
	if (that instanceof OffsetUnit)
	    return toThat(values, (OffsetUnit)that);
	throw new UnitException("Unknown unit subclass");
    }

    public float[] toThat(float[] values, Unit that)
           throws UnitException {
/*
   added by Bill Hibbard for VisAD
*/
        if ((this instanceof PromiscuousUnit) ||
            (that instanceof PromiscuousUnit)) {
          return values;
        }
/*
   end of added by Bill Hibbard for VisAD
*/
        if (that instanceof BaseUnit)
            return toThat(values, (BaseUnit)that);
        else
        if (that instanceof DerivedUnit)
            return toThat(values, (DerivedUnit)that);
        else
        if (that instanceof ScaledUnit)
            return toThat(values, (ScaledUnit)that);
        else
        if (that instanceof OffsetUnit)
            return toThat(values, (OffsetUnit)that);
        throw new UnitException("Unknown unit subclass");
    }

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

    abstract Unit multiply(BaseUnit that)
	throws UnitException;
    abstract Unit multiply(DerivedUnit that)
	throws UnitException;
    abstract Unit multiply(ScaledUnit that)
	throws UnitException;
    Unit multiply(OffsetUnit that)
	throws UnitException
    {
	throw new UnitException("Attempt to multiply by an offset unit");
    }

    abstract Unit divide(BaseUnit that)
	throws UnitException;
    abstract Unit divide(DerivedUnit that)
	throws UnitException;
    abstract Unit divide(ScaledUnit that)
	throws UnitException;
    Unit divide(OffsetUnit that)
	throws UnitException
    {
	throw new UnitException("Attempt to divide by an offset unit");
    }

    abstract double[] toThis(double[] values, BaseUnit that)
	throws UnitException;
    abstract double[] toThis(double[] values, DerivedUnit that)
	throws UnitException;
    abstract double[] toThis(double[] values, ScaledUnit that)
	throws UnitException;
    abstract double[] toThis(double[] values, OffsetUnit that)
	throws UnitException;

    abstract float[] toThis(float[] values, BaseUnit that)
        throws UnitException;
    abstract float[] toThis(float[] values, DerivedUnit that)
        throws UnitException;
    abstract float[] toThis(float[] values, ScaledUnit that)
        throws UnitException;
    abstract float[] toThis(float[] values, OffsetUnit that)
        throws UnitException;

    abstract double[] toThat(double[] values, BaseUnit that)
	throws UnitException;
    abstract double[] toThat(double[] values, DerivedUnit that)
	throws UnitException;
    abstract double[] toThat(double[] values, ScaledUnit that)
	throws UnitException;
    abstract double[] toThat(double[] values, OffsetUnit that)
	throws UnitException;

    abstract float[] toThat(float[] values, BaseUnit that)
        throws UnitException;
    abstract float[] toThat(float[] values, DerivedUnit that)
        throws UnitException;
    abstract float[] toThat(float[] values, ScaledUnit that)
        throws UnitException;
    abstract float[] toThat(float[] values, OffsetUnit that)
        throws UnitException;

}
