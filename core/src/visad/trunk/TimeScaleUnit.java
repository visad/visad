//
// TimeScaleUnit.java
//

/*
 * Copyright 2000, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TimeScaleUnit.java,v 1.4 2000-04-25 13:03:31 billh Exp $
 */

package visad;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;


/**
 * A class that represents a time unit with an origin.
 *
 * Instances are immutable.
 *
 * @author Steven R. Emmerson
 */
public final class TimeScaleUnit
    extends	Unit
    implements	Serializable
{
    /**
     * The reference time unit.
     * @serial
     */
    private final Unit			_unit;

    /**
     * The time origin.
     * @serial
     */
    private final Date			_origin;

    /**
     * The date formatter.
     * @serial
     */
    private static SimpleDateFormat	dateFormat;

    /**
     * The arbitrary time origin of (now deprecated) OffsetUnit-s which acted
     * as TimeScaleUnit-s.
     * @serial
     */
    private static Date			offsetUnitOrigin;

    /**
     * The millisecond unit.
     */
    private static Unit			millisecond;

    static
    {
	try
	{
	    dateFormat =
		(SimpleDateFormat)DateFormat.getDateInstance(
		    DateFormat.SHORT, Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	    dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss.SSS 'UTC'");
	    Calendar	calendar =
		new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	    calendar.clear();
	    calendar.set(2001, 0, 1, 0, 0, 0);	// data.netcdf.units.UnitParser
	    offsetUnitOrigin = calendar.getTime();
	    millisecond = SI.second.scale(0.001).clone("ms");
	}
	catch (Exception e)
	{
	    System.err.println(
		"TimeScaleUnit.<clinit>: Couldn't initialize class: " + e);
	    System.exit(1);
	}
    }

    /**
     * Returns the instance corresponding to a reference unit and a time origin.
     * @param unit		The reference time unit.
     * @param origin		The time origin.
     * @throws UnitException	<code>unit</code> is not a unit of time.
     */
    public static TimeScaleUnit instance(Unit unit, Date origin)
	throws UnitException
    {
	return new TimeScaleUnit(unit, origin, null);
    }

    /**
     * Returns the instance corresponding to a (now deprecated) OffsetUnit that
     * used to act as a TimeScaleUnit.
     * @param unit		The offset unit.
     * @throws UnitException	<code>unit</code> is not a unit of time.
     */
    public static TimeScaleUnit instance(OffsetUnit unit)
	throws UnitException
    {
	return instance(
	    unit.getAbsoluteUnit(),
	    newDate(unit.offset, unit.getAbsoluteUnit()));
    }

    private static Date newDate(double offset, Unit unit)
	throws UnitException
    {
	Date	date = new Date();
	date.setTime((long)(
	    millisecond.toThis(offset, unit) + offsetUnitOrigin.getTime()));
	return date;
    }

    /**
     * Returns the instance corresponding to a reference unit, a time origin,
     * and an identifier.
     * @param unit		The reference time unit.
     * @param origin		The time origin.
     * @param id		The identifier.
     * @throws UnitException	<code>unit</code> is not a unit of time.
     */
    public static TimeScaleUnit instance(Unit unit, Date origin, String id)
	throws UnitException
    {
	return new TimeScaleUnit(unit, origin, id);
    }

    /**
     * Constructs from a reference unit, a time origin, and an identifier.
     * @param unit		The reference time unit.
     * @param origin		The time origin.
     * @param id		The identifier.
     * @throws UnitException	<code>unit</code> is not a unit of time.
     */
    private TimeScaleUnit(Unit unit, Date origin, String id)
	throws UnitException
    {
	super(id);
	if (!unit.isConvertible(SI.second))
	    throw new UnitException(
		"\"" + unit + "\" is not a unit of time");
	_unit = unit;
	_origin = origin;
    }

    /**
     * Returns the definition of this unit.
     *
     * @return          The definition of this unit (e.g. "s since 2000-04-21
     *			14:54:04.05 UTC").
     */
    public String getDefinition()
    {
	return _unit.toString() + " since " + dateFormat.format(_origin);
    }

    /**
     * Clones this unit, changing the identifier.
     * @param identifier	The name or abbreviation for the cloned unit.
     *				May be <code>null</code> or empty.
     */
    protected Unit protectedClone(String identifier)
    {
	Unit	unit;
	try
	{
	    unit = instance(_unit, _origin, identifier);
	}
	catch (UnitException e)
	{
	    unit = null;
	}	// can't happen
	return unit;
    }

    /**
     * Raises this unit to a power.
     *
     * @param power		The power to raise this unit by.
     * @throws UnitException	Can't raise this unit to a power. Always thrown.
     */
    public Unit pow(int power)
	throws UnitException
    {
	throw new UnitException(
	  "Can't raise TimeScaleUnit (" + this + ") to power (" + power + ")");
    }

    /**
     * Raises this unit to a power.
     *
     * @param power		The power to raise this unit by.
     * @throws UnitException	Can't raise this unit to a power. Always thrown.
     */
    public Unit pow(double power)
	throws UnitException
    {
	throw new UnitException(
	  "Can't raise TimeScaleUnit (" + this + ") to power (" + power + ")");
    }

    /**
     * Scales this unit by an amount.
     *
     * @param amount	The amount by which to scale this unit.  E.g.
     *			Unit yard = meter.scale(0.9144);
     * @exception	UnitException	This unit cannot be scaled.
     */
    public Unit scale(double amount)
	throws UnitException
    {
	return instance(_unit.scale(amount), _origin);
    }

    /**
     * Shifts this unit by an amount.
     *
     * @param offset	The amount by which to shift this unit.  E.g.
     *			Unit celsius = kelvin.shift(273.15);
     * @throws UnitException	This unit cannot be shifted.  Always thrown.
     */
    public Unit shift(double offset)
	throws UnitException
    {
	throw new UnitException("Cannot shift unit (" + this + ")");
    }

    /**
     * Multiplies this unit by another unit.
     *
     * @param that		The unit with which to multiply this unit.
     * @exception UnitException	Can't multiply units.  Always thrown.
     */
    public Unit multiply(Unit that)
	throws UnitException
    {
	throw new UnitException(
	  "Can't multiply TimeScaleUnit (" + this + ") by unit (" + that + ")");
    }

    /**
     * Divides this unit by another unit.
     *
     * @param that		The unit to divide into this unit.
     * @exception UnitException	Can't divide units.  Always thrown.
     */
    public Unit divide(Unit that)
	throws UnitException
    {
	throw new UnitException(
	  "Can't divide TimeScaleUnit (" + this + ") by unit (" + that + ")");
    }

    /**
     * Divides this unit into another unit.
     *
     * @param that      The unit to be divide by this unit.
     * @return          The quotient of the two units.
     * @promise		Neither unit has been modified.
     * @throws UnitException	Meaningless operation.  Always thrown.
     */
    protected Unit divideInto(Unit that)
	throws UnitException
    {
	throw new UnitException(
	  "Can't divide TimeScaleUnit (" + this + ") into unit (" + that + ")");
    }

    /**
     * Converts values to this unit from another unit
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @throws UnitException	The units are not convertible.
     */
    public double[] toThis(double[] values, Unit that)
	throws UnitException
    {
	double[]	newValues;
	if (that instanceof TimeScaleUnit)
	{
	    newValues = toThis(values, (TimeScaleUnit)that);
	}
	else if (that instanceof OffsetUnit)
	{
	    newValues = toThis(values, (OffsetUnit)that);
	}
	else
	{
	    throw new UnitException(
		"Can't convert values to TimeScaleUnit (" + this + 
		") from unit (" + that + ")");
	}
	return newValues;
    }

    /**
     * Converts values to this unit from another unit
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @throws UnitException	The units are not convertible.
     */
    public float[] toThis(float[] values, Unit that)
	throws UnitException
    {
	float[]	newValues;
	if (that instanceof TimeScaleUnit)
	{
	    newValues = toThis(values, (TimeScaleUnit)that);
	}
	else if (that instanceof OffsetUnit)
	{
	    newValues = toThis(values, (OffsetUnit)that);
	}
	else
	{
	    throw new UnitException(
		"Can't convert values to TimeScaleUnit (" + this + 
		") from unit (" + that + ")");
	}
	return newValues;
    }

    /**
     * Converts values to this unit from an OffsetUnit.
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThis(double[] values, OffsetUnit that)
	throws UnitException
    {
	return toThis(values, instance(that));
    }

    /**
     * Converts values to this unit from an OffsetUnit.
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    float[] toThis(float[] values, OffsetUnit that)
        throws UnitException
    {
	return toThis(values, instance(that));
    }

    /**
     * Converts values to this unit from another TimeScaleUnit unit.
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     */
    double[] toThis(double[] values, TimeScaleUnit that)
	throws UnitException
    {
	double[]	newValues;
	double		originOffset;
	try
	{
	    newValues = _unit.toThis(values, that._unit);
	    originOffset =
		_unit.toThis(
		    that._origin.getTime() - _origin.getTime(), millisecond);
	}
	catch (UnitException e)
	{
	    newValues = null;
	    originOffset = 0;
	}	// can't happen
	for (int i = 0; i < newValues.length; ++i)
	    newValues[i] = values[i] + originOffset;
	return newValues;
    }

    /**
     * Converts values to this unit from another TimeScaleUnit unit.
     *
     * @param values	The values to be converted.
     * @param that      The unit of <code>values</code>.
     * @return          The converted values in units of this unit.
     */
    float[] toThis(float[] values, TimeScaleUnit that)
    {
	float[]		newValues;
	float		originOffset;
	try
	{
	    newValues = _unit.toThis(values, that._unit);
	    originOffset = (float)
		_unit.toThis(
		    that._origin.getTime() - _origin.getTime(), millisecond);
	}
	catch (UnitException e)
	{
	    newValues = null;
	    originOffset = 0;
	}	// can't happen
        for (int i = 0; i < newValues.length; ++i)
            newValues[i] = values[i] + originOffset;
        return newValues;
    }

    /**
     * Converts values from this unit to another unit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    public double[] toThat(double values[], Unit that)
	throws UnitException
    {
	double[]	newValues;
	if (that instanceof TimeScaleUnit)
	{
	    newValues = toThat(values, (TimeScaleUnit)that);
	}
	else if (that instanceof OffsetUnit)
	{
	    newValues = toThat(values, (OffsetUnit)that);
	}
        // WLH 25 April 2000
        else if (that instanceof ScaledUnit) {
          newValues = toThat(values, (ScaledUnit)that);
        }
        // WLH 25 April 2000
        else if (that instanceof DerivedUnit) {
          newValues = toThat(values, (DerivedUnit)that);
        }
	else
	{
	  throw new UnitException(
	      "Can't convert values from TimeScaleUnit (" + this + 
	      ") to unit (" + that + ")");
	}
	return newValues;
    }

    /**
     * Converts values from this unit to another unit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    public float[] toThat(float values[], Unit that)
	throws UnitException
    {
	float[]	newValues;
	if (that instanceof TimeScaleUnit)
	{
	    newValues = toThat(values, (TimeScaleUnit)that);
	}
	else if (that instanceof OffsetUnit)
	{
	    newValues = toThat(values, (OffsetUnit)that);
	}
        // WLH 25 April 2000
        else if (that instanceof ScaledUnit) {
          newValues = toThat(values, (ScaledUnit)that);
        }
        // WLH 25 April 2000
        else if (that instanceof DerivedUnit) {
          newValues = toThat(values, (DerivedUnit)that);
        }
	else
	{
	  throw new UnitException(
	      "Can't convert values from TimeScaleUnit (" + this + 
	      ") to unit (" + that + ")");
	}
	return newValues;
    }

    /**
     * Converts values from this unit to an OffsetUnit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThat(double values[], OffsetUnit that)
	throws UnitException
    {
	return toThat(values, instance(that));
    }

    /**
     * Converts values from this unit to an OffsetUnit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    float[] toThat(float values[], OffsetUnit that)
        throws UnitException
    {
	return toThat(values, instance(that));
    }

    // WLH 25 April 2000
    double[] toThat(double values[], ScaledUnit that)
	throws UnitException
    {
	return toThat(values, instance(that, newDate(0.0, that.getAbsoluteUnit())));
    }

    // WLH 25 April 2000
    float[] toThat(float values[], ScaledUnit that)
        throws UnitException
    {
	return toThat(values, instance(that, newDate(0.0, that.getAbsoluteUnit())));
    }

    // WLH 25 April 2000
    double[] toThat(double values[], DerivedUnit that)
        throws UnitException
    {
        return toThat(values, instance(that, newDate(0.0, that.getAbsoluteUnit())));
    }

    // WLH 25 April 2000
    float[] toThat(float values[], DerivedUnit that)
        throws UnitException
    {
        return toThat(values, instance(that, newDate(0.0, that.getAbsoluteUnit())));
    }

    /**
     * Converts values from this unit to another TimeScaleUnit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    double[] toThat(double values[], TimeScaleUnit that)
	throws UnitException
    {
	return that.toThis(values, this);
    }

    /**
     * Converts values from this unit to another TimeScaleUnit.
     *
     * @param values	The values to be converted in units of this unit.
     * @param that      The unit to which to convert the values.
     * @return          The converted values.
     * @require		The units are convertible.
     * @promise		Neither unit has been modified.
     * @exception	The units are not convertible.
     */
    float[] toThat(float values[], TimeScaleUnit that)
	throws UnitException
    {
	return that.toThis(values, this);
    }

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
      return _unit.getAbsoluteUnit();
    }

    /**
     * Indicates whether this unit is convertible with another unit.  If one
     * unit is convertible with another, then the <code>toThis(...)</code>/ and
     * <code>toThat(...)</code> methods will not throw a UnitException.  Unit A
     * is convertible with unit B if and only if unit B is convertible with unit
     * A; hence, calling-order is irrelevant.
     *
     * @param unit	The other unit.
     * @return		True if and only if this unit is convertible with the
     *			other unit.
     */
    public boolean isConvertible(Unit unit)
    {
	return
	    unit instanceof TimeScaleUnit ||
	    (unit instanceof OffsetUnit &&
	     unit.getAbsoluteUnit().isConvertible(SI.second));
    }

    /**
     * Indicates if this instance is semantically identical to another Unit.
     * @param unit		The other Unit.
     * @return			<code>true</code> if and only if this instance 
     *				is semantically identical to the other Unit.
     */
    public boolean equals(Unit unit)
    {
	boolean	equals;
	if (!(unit instanceof TimeScaleUnit))
	{
	    equals = false;
	}
	else
	{
	    TimeScaleUnit	that = (TimeScaleUnit)unit;
	    equals = this == that || (
		_unit.equals(that._unit) &&
		_origin.equals(that._origin));
	}
	return equals;
    }
}
