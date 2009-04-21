/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Parser.java,v 1.3 2009-04-21 20:15:10 steve Exp $
 */

package visad.data.units;

import java.io.ByteArrayInputStream;

import visad.Unit;
import visad.UnitException;

/**
 * Class for parsing unit specifications.
 */
public class Parser {
    /**
     * The unit parser.
     */
    protected static final UnitParser   unitParser  = new UnitParser(System.in);

    /**
     * The singleton instance of this class.
     */
    protected static final Parser       parser      = new Parser();

    /**
     * Default constructor. Protected to ensure use of singleton.
     */
    protected Parser() {
    }

    /**
     * Obtain the singleton instance of this class. Strictly speaking, this
     * isn't necessary since <code>parse()</code> is a class method.
     */
    public static Parser instance() {
        return parser;
    }

    /**
     * Parse a string unit-specification.
     * 
     * @param spec
     *            The string unit-specification.
     * @precondition The specification is non-null.
     * @exception ParseException
     *                An error occurred while parsing the specification.
     * @throws UnitException
     *             if {@code spec} requires an unsupported operation.
     */
    public static synchronized Unit parse(final String spec)
            throws ParseException, NoSuchUnitException {
        unitParser.ReInit(new ByteArrayInputStream(spec.trim().getBytes()));

        try {
            return unitParser.unitSpec();
        }
        catch (final TokenMgrError e) {
            throw new ParseException(e.getMessage());
        }
        catch (final UnitException e) {
            throw new ParseException(e.getMessage());
        }
    }

    /**
     * Test this class.
     */
    public static void main(final String[] args) throws ParseException,
            UnitException {
        final Unit m = Parser.parse("m");
        final Unit s = Parser.parse("s");
        class Test {
            String  spec;
            Unit    unit;

            Test(final String spec, final Unit unit) {
                this.spec = spec;
                this.unit = unit;
            }
        }
        final Test[] tests = {
                new Test("m", m),
                new Test("2 m s", m.multiply(s).scale(2)),
                new Test("3.14 m.s", m.multiply(s).scale(3.14)),
                new Test("1e9 (m)", m.scale(1e9)),
                new Test("(m s)2", m.multiply(s).pow(2)),
                new Test("m2.s-1", m.pow(2).divide(s)),
                new Test("m2 s^-1", m.pow(2).divide(s)),
                new Test("(m/s)2", m.divide(s).pow(2)),
                new Test("m2/s-1", m.pow(2).divide(s.pow(-1))),
                new Test("m2/s^-1", m.pow(2).divide(s.pow(-1))),
                new Test(".5 m/(.25 s)2", m.scale(.5).divide(
                        s.scale(.25).pow(2))),
                new Test("m.m-1.m", m.multiply(m.pow(-1)).multiply(m)),
                new Test("2.0 m 1/2 s-1*(m/s^1)^-1 (1e9 m-1)(1e9 s-1)-1.m/s", m
                        .scale(2).scale(1. / 2.).multiply(s.pow(-1)).multiply(
                                m.divide(s.pow(1)).pow(-1)).multiply(
                                m.pow(-1).scale(1e9)).multiply(
                                s.pow(-1).scale(1e9).pow(-1)).multiply(m)
                        .divide(s)), new Test("m/km", m.divide(m.scale(1e3))) };

        for (int i = 0; i < tests.length; ++i) {
            final Test test = tests[i];
            final String spec = test.spec;
            final Unit unit = test.unit;
            if (!Parser.parse(spec).equals(unit)) {
                throw new AssertionError(spec + " != " + unit);
            }
        }
        try {
            Parser.parse("unknown unit");
            throw new AssertionError();
        }
        catch (final ParseException e) {
        }
        System.out.println("Done");
    }
}
