/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Parser.java,v 1.6 1998-12-16 20:27:43 steve Exp $
 */

package visad.data.netcdf.units;


import java.io.ByteArrayInputStream;
import visad.Unit;


/**
 * Class for parsing unit specifications.
 */
public class
Parser
{
    /**
     * The unit parser.
     */
    protected static final UnitParser	unitParser = new UnitParser(System.in);

    /**
     * The singleton instance of this class.
     */
    protected static final Parser	parser = new Parser();


    /**
     * Default constructor.  Protected to ensure use of singleton.
     */
    protected
    Parser()
    {}


    /**
     * Obtain the singleton instance of this class.  Strictly speaking, this
     * isn't necessary since <code>parse()</code> is a class method.
     */
    public static Parser
    instance()
    {
	return parser;
    }


    /**
     * Parse a unit specification.
     *
     * @precondition	The specification is non-null.
     * @exception ParseException	An error occurred while parsing 
     *					the specification.
     */
    public static synchronized Unit
    parse(String spec)
	throws ParseException, NoSuchUnitException
    {
	unitParser.ReInit(new ByteArrayInputStream(spec.trim().getBytes()));

	try
	{
	    return unitParser.unitSpec();
	}
	catch (TokenMgrError e)
	{
	    throw new ParseException(e.getMessage());
	}
    }


    /**
     * Test this class.
     */
    public static void main(String[] args)
	throws ParseException
    {
	String[]	specs =
	{
	    "m",
	    "2 m s",
	    "3.14 m.s",
	    "1e9 (m)",
	    "(m s)2",
	    "m2.s-1",
	    "m2 s^-1",
	    "(m/s)2",
	    "m2/s-1",
	    "m2/s^-1",
	    ".5 m/(.25 s)2",
	    "m.m-1.m",
	    "2.0 m 1/2 s-1*(m/s^1)^-1 (1e9 m-1)(1e9 s-1)-1.m/s"
	};

	for (int i = 0; i < specs.length; ++i)
	{
	    String	spec = specs[i];
	    System.out.print(spec + ": ");
	    System.out.println(Parser.parse(spec));
	}
	try
	{
	    System.out.print("unknown unit: ");
	    System.out.println(Parser.parse("unknown unit"));
	}
	catch (ParseException e)
	{
	    System.out.println(e);
	}
    }
}
