/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Parser.java,v 1.3 1998-02-23 15:58:59 steve Exp $
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
	unitParser.ReInit(new ByteArrayInputStream(spec.getBytes()));

	return unitParser.unitSpec();
    }


    /**
     * Test this class.
     */
    public static void main(String[] args)
	throws ParseException
    {
	String[]	specs =
	{
	    "furlongs",
	    "furlongs/fortnight",
	    "megaparsec barn"
	};

	for (int i = 0; i < specs.length; ++i)
	{
	    System.out.print(specs[i] + ": ");
	    System.out.println(Parser.parse(specs[i]));
	}
	try
	{
	    System.out.print("unknown unit: ");
	    System.out.println(Parser.parse("unknown unit"));
	}
	catch (ParseException e)
	{
	    System.out.print(e);
	}
    }
}
