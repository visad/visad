package visad.data.dods;

import dods.dap.*;
import visad.data.BadFormException;
import visad.data.in.*;

public class ValueVetter
    extends	ValueProcessor
{
    private double			lower;
    private double			upper;
    private double			fill;
    private double			missing;
    private static final ValueVetter	trivialVetter =
	new ValueVetter()
	{
	    public float process(float value)
	    {
		return value;
	    }
	    public double process(double value)
	    {
		return value;
	    }
	    public float[] process(float[] values)
	    {
		return values;
	    }
	    public double[] process(double[] values)
	    {
		return values;
	    }
	};

    protected ValueVetter()
    {}

    protected ValueVetter(
	double lower, double upper, double fill, double missing)
    {
	this.lower = lower;
	this.upper = upper;
	this.fill = fill;
	this.missing = missing;
    }

    /**
     * @param table		The DODS attribute table.  May be 
     *				<code>null</code>, in which case a trivial
     *				vetter is returned.
     */
    public static ValueVetter instance(AttributeTable table)
	throws BadFormException
    {
	ValueVetter	vetter;
	if (table == null)
	{
	    vetter = trivialVetter;
	}
	else
	{
	    double	fill = DODSUtil.decode("_FillValue", table, 0);
	    double	missing = DODSUtil.decode("missing_value", table, 0);
	    double	lower;
	    double	upper;
	    if (table.getAttribute("valid_range") == null)
	    {
		lower = DODSUtil.decode("valid_min", table, 0);
		upper = DODSUtil.decode("valid_max", table, 0);
	    }
	    else
	    {
		lower = DODSUtil.decode("valid_range", table, 0);
		upper = DODSUtil.decode("valid_range", table, 1);
	    }
	    vetter =
		lower == lower || upper == upper || 
		fill == fill || missing == missing
		    ? new ValueVetter(lower, upper, fill, missing)
		    : trivialVetter;
	}
	return vetter;
    }

    public float process(float value)
    {
	/*
	 * NB: An unset test value will be NaN -- so mind the sense of the 
	 * comparisons.
	 */
	return
	    value < lower || value > upper || value == missing || value == fill
		? Float.NaN
		: value;
    }

    public double process(double value)
    {
	/*
	 * NB: An unset test value will be NaN -- so mind the sense of the 
	 * comparisons.
	 */
	return
	    value < lower || value > upper || value == missing || value == fill
		? Double.NaN
		: value;
    }

    /**
     * @return			Vetted values (same array as input).
     */
    public float[] process(float[] values)
    {
	/*
	 * NB: An unset test value will be NaN -- so mind the sense of the 
	 * comparisons.
	 */
	for (int i = 0; i < values.length; ++i)
	{
	    double	value = values[i];
	    values[i] =
		value < lower || value > upper ||
		value == missing || value == fill
		    ? Float.NaN
		    : (float)value;
	}
	return values;
    }

    /**
     * @return			Vetted values (same array as input).
     */
    public double[] process(double[] values)
    {
	/*
	 * NB: An unset test value will be NaN -- so mind the sense of the 
	 * comparisons.
	 */
	for (int i = 0; i < values.length; ++i)
	{
	    double	value = values[i];
	    values[i] =
		value < lower || value > upper ||
		value == missing || value == fill
		    ? Double.NaN
		    : value;
	}
	return values;
    }

    public final float vet(float value)
    {
	return process(value);
    }

    public final float[] vet(float[] values)
    {
	return process(values);
    }

    public final double vet(double value)
    {
	return process(value);
    }

    public final double[] vet(double[] values)
    {
	return process(values);
    }
}
