package visad.data.in;

public abstract class ValueProcessor
{
    protected static final ValueProcessor	trivialProcessor =
	new ValueProcessor()
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

    protected ValueProcessor()
    {}

    public abstract float process(float value);

    public abstract double process(double value);

    /**
     * @return			Processed values (same array as input).
     */
    public abstract float[] process(float[] values);

    /**
     * @return			Processed values (same array as input).
     */
    public abstract double[] process(double[] values);
}
