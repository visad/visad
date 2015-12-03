/*
 * Copyright 1999, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DefaultUnitsDB.java,v 1.9 2010-05-19 12:29:03 donm Exp $
 */

package visad.data.units;

import visad.BaseUnit;
import visad.OffsetUnit;
import visad.SI;
import visad.ScaledUnit;
import visad.Unit;
import visad.UnitException;

/**
 * Default units database.
 * 
 * This database knows about approximately 500 different units. Users can also
 * add new units to the database at runtime.
 * 
 * The basis for this units database is the International System of Units (SI).
 * 
 * This is a singleton class.
 */
public final class DefaultUnitsDB extends UnitTable {
    /**
     * The singleton instance of this class.
     */
    private static final DefaultUnitsDB   db;
    
    static {
        try {
		    db = new DefaultUnitsDB();
        }
        catch (UnitException e) {
            throw (ExceptionInInitializerError) new ExceptionInInitializerError().initCause(e);
        }
    }

    /**
     * The unit prefix names in order of lexicographic length:
     */
    protected final UnitPrefix[]    prefixNames     = {
            new UnitPrefix("centi", 1e-2),
            new UnitPrefix("femto", 1e-15),
            new UnitPrefix("hecto", 1e2),
            new UnitPrefix("micro", 1e-6),
            new UnitPrefix("milli", 1e-3),
            new UnitPrefix("yocto", 1e-24),
            new UnitPrefix("yotta", 1e24),
            new UnitPrefix("zepto", 1e-21),
            new UnitPrefix("zetta", 1e21),
            new UnitPrefix("atto", 1e-18),
            new UnitPrefix("deca", 1e1), // Spelling according to "ISO 2955:
            // Information processing --
            // Representation of SI and other units
            // in systems with limited character
            // sets"
            new UnitPrefix("deci", 1e-1),
            new UnitPrefix("deka", 1e1), // Spelling according to "ASTM
            // Designation: E 380 - 85: Standard
            // for METRIC PRACTICE", "ANSI/IEEE Std
            // 260-1978 (Reaffirmed 1985): IEEE
            // Standard Letter Symbols for Units of
            // Measurement", and NIST Special
            // Publication 811, 1995 Edition:
            // "Guide for the Use of the
            // International System of Units (SI)".
            new UnitPrefix("giga", 1e9), // 1st syllable pronounced "jig"
            // according to "ASTM Designation: E
            // 380 - 85: Standard for METRIC
            // PRACTICE".
            new UnitPrefix("kilo", 1e3), new UnitPrefix("mega", 1e6),
            new UnitPrefix("nano", 1e-9), new UnitPrefix("peta", 1e15),
            new UnitPrefix("pico", 1e-12), new UnitPrefix("tera", 1e12),
            new UnitPrefix("exa", 1e18),            };

    /**
     * The unit prefix symbols in order of lexicographic length:
     */
    protected final UnitPrefix[]    prefixSymbols   = {
            new UnitPrefix("da", 1e1), new UnitPrefix("E", 1e18),
            new UnitPrefix("G", 1e9), new UnitPrefix("M", 1e6),
            new UnitPrefix("P", 1e15), new UnitPrefix("T", 1e12),
            new UnitPrefix("Y", 1e24), new UnitPrefix("Z", 1e21),
            new UnitPrefix("a", 1e-18), new UnitPrefix("c", 1e-2),
            new UnitPrefix("d", 1e-1), new UnitPrefix("f", 1e-15),
            new UnitPrefix("h", 1e2), new UnitPrefix("k", 1e3),
            new UnitPrefix("m", 1e-3), new UnitPrefix("n", 1e-9),
            new UnitPrefix("p", 1e-12), new UnitPrefix("u", 1e-6),
            new UnitPrefix("y", 1e-24), new UnitPrefix("z", 1e-21), };

    /**
     * Constructs a default, units database.
     * 
     * @throws UnitException
     *             Something went wrong in generating a unit for the database.
     *             This should not occur and indicates an internal
     *             inconsistancy.
     */
    private DefaultUnitsDB() throws UnitException {
        /*
         * Create a unit table of the proper size. Because increasing the size
         * might be expensive, the initial size should be kept in sync with the
         * actual number of entries (e.g. in vi: :.,$w !egrep 'pn\(' | wc -l
         * (times 2 plus) :.,$w !egrep '(put|px)\(' | wc -l :.,$w !egrep 'ps\('
         * | wc -l
         */
        super(677, 98);

        /*
         * The base units:
         */
        put(SI.ampere);
        put(SI.candela);
        put(SI.kelvin);
        put(SI.kilogram);
        put(SI.meter);
        put(SI.mole);
        put(SI.second);
        put(SI.radian);
        put(SI.steradian);

        /*
         * Constants:
         */
        ps("%", new ScaledUnit(0.01));
        pn("percent", "%");
        pn("PI", new ScaledUnit(Math.PI));
        pn("bakersdozen", new ScaledUnit(13));
        pn("pair", new ScaledUnit(2));
        pn("ten", new ScaledUnit(10));
        pn("dozen", new ScaledUnit(12));
        pn("score", new ScaledUnit(20));
        pn("hundred", new ScaledUnit(100));
        pn("thousand", new ScaledUnit(1.0e3));
        pn("million", new ScaledUnit(1.0e6));
        // NB: "billion" is ambiguous (1e9 in U.S. but 1e12 in U.K.)

        /*
         * NB: All subsequent definitions must be given in terms of earlier
         * definitions. Forward referencing is not permitted.
         */

        /*
         * The following are non-base units of the fundamental quantities
         */

        /*
         * UNITS OF ELECTRIC CURRENT
         */
        pn("amp", "ampere");
        pn("abampere", get("A").scale(10)); // exact
        pn("gilbert", get("A").scale(7.957747e-1));
        pn("statampere", get("A").scale(3.335640e-10));
        pn("biot", "abampere");

        /*
         * UNITS OF LUMINOUS INTENSITY
         */
        pn("candle", "candela");

        /*
         * UNITS OF THERMODYNAMIC TEMPERATURE
         */
        px("degree kelvin", "K");
        px("degrees kelvin", "K");
        ps("degK", "K");
        px("degreeK", "K");
        px("degreesK", "K");
        px("deg K", "K");
        px("degree K", "K");
        px("degrees K", "K");

        //ps("Cel", new OffsetUnit(273.15, (BaseUnit) get("K")));
        ps("°C", new OffsetUnit(273.15, (BaseUnit) get("K")));
        pn("celsius", "°C");
        px("degree celsius", "°C");
        px("degrees celsius", "°C");
        pn("centigrade", "°C");
        px("degree centigrade", "°C");
        px("degrees centigrade", "°C");
        px("degC", "°C");
        px("degreeC", "°C");
        px("degreesC", "°C");
        px("deg C", "°C");
        px("degree C", "°C");
        px("degrees C", "°C");
        // ps("C", "Cel"); // `C' means `coulomb'

        pn("rankine", get("K").scale(1 / 1.8));
        px("degree rankine", "rankine");
        px("degrees rankine", "rankine");
        px("degR", "rankine");
        px("degreeR", "rankine");
        px("degreesR", "rankine");
        px("deg R", "rankine");
        px("degree R", "rankine");
        px("degrees R", "rankine");
        // ps("R", "rankine"); // "R" means "roentgen"

        pn("fahrenheit", get("Rankine").shift(459.67));
        px("degree fahrenheit", "fahrenheit");
        px("degrees fahrenheit", "fahrenheit");
        px("degF", "fahrenheit");
        px("degreeF", "fahrenheit");
        px("degreesF", "fahrenheit");
        px("deg F", "fahrenheit");
        px("degree F", "fahrenheit");
        px("degrees F", "fahrenheit");
        // ps("F", "fahrenheit"); // "F" means "farad"

        /*
         * UNITS OF MASS
         */
        pn("assay ton", get("kg").scale(2.916667e-2));
        pn("avoirdupois ounce", get("kg").scale(2.834952e-2));
        pn("avoirdupois pound", get("kg").scale(4.5359237e-1)); // exact
        pn("carat", get("kg").scale(2e-4));
        ps("gr", get("kg").scale(6.479891e-5)); // exact
        ps("g", get("kg").scale(1e-3)); // exact
        pn("long hundredweight", get("kg").scale(5.080235e1));
        ps("tne", get("kg").scale(1e3)); // exact
        pn("pennyweight", get("kg").scale(1.555174e-3));
        pn("short hundredweight", get("kg").scale(4.535924e1));
        pn("slug", get("kg").scale(14.59390));
        pn("troy ounce", get("kg").scale(3.110348e-2));
        pn("troy pound", get("kg").scale(3.732417e-1));
        pn("amu", get("kg").scale(1.66054e-27));
        pn("scruple", get("gr").scale(20));
        pn("apdram", get("gr").scale(60));
        pn("apounce", get("gr").scale(480));
        pn("appound", get("gr").scale(5760));

        pn("gram", "g"); // was "gravity"
        pn("tonne", "tne");
        px("metric ton", "tne");
        pn("apothecary ounce", "troy ounce");
        pn("apothecary pound", "troy pound");
        pn("pound", "avoirdupois pound");
        pn("metricton", "tne");
        ps("grain", "gr");
        pn("atomicmassunit", "amu");
        pn("atomic mass unit", "amu");

        ps("t", "tne");
        ps("lb", "avoirdupois pound");
        pn("bag", get("pound").scale(94));
        pn("short ton", get("pound").scale(2000));
        pn("long ton", get("pound").scale(2240));

        pn("ton", "short ton");
        pn("shortton", "short ton");
        pn("longton", "long ton");

        /*
         * UNITS OF LENGTH
         */
        pn("angstrom", get("m").scale(1e-10));
        pn("au", get("m").scale(1.495979e11));
        pn("fermi", get("m").scale(1e-15)); // exact
        pn("light year", get("m").scale(9.46073e15));
        pn("micron", get("m").scale(1e-6)); // exact
        pn("mil", get("m").scale(2.54e-5)); // exact
        pn("nautical mile", get("m").scale(1.852000e3)); // exact
        pn("parsec", get("m").scale(3.085678e16));
        pn("printers point", get("m").scale(3.514598e-4));

        pn("metre", "m");
        px("prs", "parsec");

        /*
         * God help us! There's an international foot and a US survey foot and
         * they're not the same!
         */

        // US Survey foot stuff:
        px("US survey foot", get("m").scale(1200 / 3937.)); // exact
        pn("US survey yard", get("US survey foot").scale(3)); // exact
        pn("US survey mile", get("US survey foot").scale(5280)); // exact
        pn("rod", get("US survey foot").scale(16.5)); // exact
        pn("furlong", get("US survey foot").scale(660)); // exact
        pn("fathom", get("US survey foot").scale(6)); // exact

        px("US survey feet", "US survey foot");
        pn("US statute mile", "US survey mile");
        pn("pole", "rod");
        px("perch", "rod");
        px("perches", "perch");

        // International foot stuff:
        px("international inch", get("m").scale(.0254)); // exact
        px("international foot", get("international inch").scale(12));
        // exact
        pn("international yard", get("international foot").scale(3));
        // exact
        pn("international mile", get("international foot").scale(5280));
        // exact
        px("international inches", "international inch"); // alias
        px("international feet", "international foot"); // alias

        // Alias unspecified units to the international units:
        px("inch", "international inch"); // alias
        px("foot", "international foot"); // alias
        pn("yard", "international yard"); // alias
        pn("mile", "international mile"); // alias

        // The following should hold regardless:
        px("inches", "inch"); // alias
        ps("in", "inches"); // alias
        px("feet", "foot"); // alias
        ps("ft", "feet"); // alias
        ps("yd", "yard"); // alias
        ps("mi", "mile"); // alias

        pn("chain", get("m").scale(2.011684e1));

        pn("pica", get("printers point").scale(12)); // exact
        pn("printers pica", "pica");
        pn("astronomicalunit", "au");
        ps("astronomical unit", "au");
        px("asu", "au");
        pn("nmile", "nautical mile");
        ps("nmi", "nautical mile");

        pn("big point", get("inch").scale(1. / 72)); // exact
        pn("barleycorn", get("inch").scale(1. / 3));

        pn("arpentlin", get("foot").scale(191.835));

        // The following is for Ozone measurements:
        pn("Dobson", get("m").scale(.00001)); // exact
        pn("DU", "Dobson");

        /*
         * UNITS OF TIME
         */
        /*
         * Interval between 2 successive passages of sun through vernal equinox
         * (365.242198781 days -- see
         * http://www.ast.cam.ac.uk/pubinfo/leaflets/,
         * http://aa.usno.navy.mil/AA/ and
         * http://adswww.colorado.edu/adswww/astro coord.html):
         */
        pn("year", get("s").scale(3.15569259747e7));
        ps("d", get("s").scale(8.64e4)); // exact
        ps("h", get("s").scale(3.6e3)); // exact
        ps("min", get("s").scale(60)); // exact
        pn("shake", get("s").scale(1e-8)); // exact
        pn("sidereal day", get("s").scale(8.616409e4));
        pn("sidereal hour", get("s").scale(3.590170e3));
        pn("sidereal minute", get("s").scale(5.983617e1));
        pn("sidereal second", get("s").scale(0.9972696));
        pn("sidereal year", get("s").scale(3.155815e7));

        pn("day", "d");
        pn("hour", "h");
        pn("minute", "min");
        pn("sec", "s"); // avoid
        pn("lunar month", get("d").scale(29.530589));

        pn("common year", get("d").scale(365));
        // exact: 153600e7 seconds
        pn("leap year", get("d").scale(366)); // exact
        pn("Julian year", get("d").scale(365.25)); // exact
        pn("Gregorian year", get("d").scale(365.2425)); // exact
        pn("tropical year", "year");
        pn("sidereal month", get("d").scale(27.321661));
        pn("tropical month", get("d").scale(27.321582));
        pn("fortnight", get("d").scale(14));
        pn("week", get("d").scale(7)); // exact

        pn("jiffy", get("s").scale(1e-2)); // it's true
        pn("eon", get("year").scale(1e9)); // fuzzy
        pn("month", get("year").scale(1. / 12)); // on average

        pn("tropical year", "year");
        pn("yr", "year");
        ps("a", "year"); // "anno"
        px("ann", "year"); // "anno"
        pn("hr", "h");

        /*
         * UNITS OF PLANE ANGLE
         */
        pn("circle", get("radian").scale(2 * Math.PI));
        pn("deg", get("radian").scale(Math.PI / 180.));
        pn("'", get("deg").scale(1. / 60));
        pn("\"", get("deg").scale(1. / 3600));
        pn("grade", get("deg").scale(0.9)); // exact
        pn("cycle", get("circle"));

        pn("turn", "circle");
        pn("revolution", "cycle");
        px("gon", "grade");
        pn("angular degree", "deg");
        pn("angular minute", "'");
        pn("angular second", "\"");
        pn("arcdeg", "deg");
        pn("degree", "deg");
        pn("arcminute", "'");
        px("mnt", "'");
        pn("arcsecond", "\"");
        // px("sec", "\""); // avoid
        pn("arcmin", "'");
        pn("arcsec", "\"");

        px("degree true", get("deg"));
        px("degrees true", get("deg"));
        px("degrees north", get("deg"));
        px("degrees east", get("deg"));
        px("degrees south", get("degrees north").scale(-1));
        px("degrees west", get("degrees east").scale(-1));

        px("degree north", "degrees north");
        px("degreeN", "degrees north");
        px("degree N", "degrees north");
        px("degreesN", "degrees north");
        px("degrees N", "degrees north");

        px("degree east", "degrees east");
        px("degreeE", "degrees east");
        px("degree E", "degrees east");
        px("degreesE", "degrees east");
        px("degrees E", "degrees east");

        px("degree west", "degrees west");
        px("degreeW", "degrees west");
        px("degree W", "degrees west");
        px("degreesW", "degrees west");
        px("degrees W", "degrees west");

        px("degree true", "degrees true");
        px("degreeT", "degrees true");
        px("degree T", "degrees true");
        px("degreesT", "degrees true");
        px("degrees T", "degrees true");

        /*
         * The following are derived units with special names. They are useful
         * for defining other derived units.
         */
        ps("Hz", get("second").pow(-1));
        ps("N", get("kg").multiply(get("m").divide(get("s").pow(2))));
        ps("C", get("A").multiply(get("s")));
        ps("lm", get("cd").multiply(get("sr")));
        ps("Bq", get("Hz"));
        // SI unit of activity of a radionuclide
        px("standard free fall", get("m").divide(get("s").pow(2)).scale(
                9.806650));
        ps("Pa", get("N").divide(get("m").pow(2)));
        ps("J", get("N").multiply(get("m")));
        ps("lx", get("lm").divide(get("m").pow(2)));
        pn("sphere", get("steradian").scale(4 * Math.PI));
        ps("W", get("J").divide(get("s")));
        ps("Gy", get("J").divide(get("kg")));
        // absorbed dose. derived unit
        ps("Sv", get("J").divide(get("kg")));
        // dose equivalent. derived unit
        ps("V", get("W").divide(get("A")));
        ps("F", get("C").divide(get("V")));
        ps("Ohm", get("V").divide(get("A")));
        ps("S", get("A").divide(get("V")));
        ps("Wb", get("V").multiply(get("s")));
        ps("T", get("Wb").divide(get("m").pow(2)));
        ps("H", get("Wb").divide(get("A")));

        pn("newton", "N");
        pn("hertz", "Hz");
        pn("watt", "W");
        px("force", "standard free fall");
        px("gravity", "standard free fall");
        px("free fall", "standard free fall");

        px("conventional mercury", get("gravity").multiply(
                get("kg").divide(get("m").pow(3))).scale(13595.10));
        px("mercury 0C", get("gravity").multiply(
                get("kg").divide(get("m").pow(3))).scale(13595.1));
        px("mercury 60F", get("gravity").multiply(
                get("kg").divide(get("m").pow(3))).scale(13556.8));
        px("conventional water", get("gravity").multiply(
                get("kg").divide(get("m").pow(3))).scale(1000)); // exact
        px("water 4C", get("gravity").multiply(
                get("kg").divide(get("m").pow(3))).scale(999.972));
        px("water 60F", get("gravity").multiply(
                get("kg").divide(get("m").pow(3))).scale(999.001));
        // ps("g", get("gravity"))); // approx. should be `local'.
        // avoid.

        px("mercury 32F", "mercury 0C");
        px("water 39F", "water 4C"); // actually 39.2 degF
        px("mercury", "conventional mercury");
        px("water", "conventional water");

        pn("farad", "F");
        ps("Hg", "mercury");
        px("H2O", "water");

        /*
         * The following are compound units: units whose definitions consist of
         * two or more base units. They may now be defined in terms of the
         * preceding units.
         */

        /*
         * ACCELERATION
         */
        ps("Gal", get("m").divide(get("s").pow(2)).scale(1e-2));
        // avoid "gal" (gallon)
        px("gals", "Gal"); // avoid "gal" (gallon)

        /*
         * AREA
         */
        pn("are", get("m").pow(2).scale(1e2)); // exact
        pn("barn", get("m").pow(2).scale(1e-28)); // exact
        pn("circular mil", get("m").pow(2).scale(5.067075e-10));
        pn("darcy", get("m").pow(2).scale(9.869233e-13)); // permeability of
        // porous solids
        pn("hectare", get("hectoare")); // exact
        px("har", "hectare"); // exact
        pn("acre", get("rod").pow(2).scale(160)); // exact

        ps("b", get("barn"));

        /*
         * ELECTRICITY AND MAGNETISM
         */
        pn("abfarad", get("F").scale(1e9)); // exact
        pn("abhenry", get("H").scale(1e-9)); // exact
        pn("abmho", get("S").scale(1e9)); // exact
        pn("abohm", get("Ohm").scale(1e-9)); // exact
        pn("megohm", get("Ohm").scale(1e6)); // exact
        pn("kilohm", get("Ohm").scale(1e3)); // exact
        pn("abvolt", get("V").scale(1e-8)); // exact
        ps("e", get("C").scale(1.60217733 - 19));
        pn("chemical faraday", get("C").scale(9.64957e4));
        pn("physical faraday", get("C").scale(9.65219e4));
        pn("C12 faraday", get("C").scale(9.648531e4));
        pn("gamma", get("nT")); // exact
        pn("gauss", get("T").scale(1e-4)); // exact
        pn("maxwell", get("Wb").scale(1e-8)); // exact
        ps("Oe", get("A").divide(get("m")).scale(7.957747e1));
        pn("statcoulomb", get("C").scale(3.335640e-10));
        pn("statfarad", get("F").scale(1.112650e-12));
        pn("stathenry", get("H").scale(8.987554e11));
        pn("statmho", get("S").scale(1.112650e-12));
        pn("statohm", get("Ohm").scale(8.987554e11));
        pn("statvolt", get("V").scale(2.997925e2));
        pn("unit pole", get("Wb").scale(1.256637e-7));

        pn("henry", "H");
        pn("siemens", "S");
        pn("ohm", "Ohm");
        pn("tesla", "T");
        pn("volt", "V");
        pn("weber", "Wb");
        pn("mho", "siemens");
        pn("oersted", "Oe");
        pn("faraday", "C12 faraday"); // charge of 1 mole of electrons
        pn("coulomb", "C");

        /*
         * ENERGY (INCLUDES WORK)
         */
        ps("eV", get("J").scale(1.602177e-19));
        ps("bev", get("eV").scale(1e9));
        pn("erg", get("J").scale(1e-7)); // exact
        pn("IT Btu", get("J").scale(1.05505585262e3)); // exact
        pn("EC therm", get("J").scale(1.05506e8)); // exact
        pn("thermochemical calorie", get("J").scale(4.184000)); // exact
        pn("IT calorie", get("J").scale(4.1868)); // exact
        px("ton TNT", get("J").scale(4.184e9));
        pn("US therm", get("J").scale(1.054804e8)); // exact
        ps("Wh", get("W").multiply(get("h")));

        pn("joule", "J");
        pn("therm", "US therm");
        pn("watthour", "Wh");
        ps("Btu", "IT Btu");
        pn("calorie", "IT calorie");
        pn("electronvolt", "eV");
        pn("electron volt", "eV");
        ps("thm", "therm");
        ps("cal", "calorie");

        /*
         * FORCE
         */
        pn("dyne", get("N").scale(1e-5)); // exact
        pn("pond", get("N").scale(9.806650e-3)); // exact
        px("force kilogram", get("N").scale(9.806650)); // exact
        px("force gram", get("N").scale(9.806650e-3)); // exact
        px("force ounce", get("N").scale(2.780139e-1));
        px("force pound", get("N").scale(4.4482216152605)); // exact
        pn("poundal", get("N").scale(1.382550e-1));
        pn("force ton", get("force pound").scale(2000)); // exact

        ps("gf", "force gram");
        ps("lbf", "force pound");
        px("ounce force", "force ounce");
        px("kilogram force", "force kilogram");
        px("pound force", "force pound");
        ps("ozf", "force ounce");
        ps("kgf", "force kilogram");
        px("ton force", "force ton");
        px("gram force", "force gram");

        pn("kip", get("lbf").scale(1e3));

        /*
         * HEAT
         */
        pn("clo", get("K").multiply(get("m").pow(2).divide(get("W"))).scale(
                1.55e-1));

        /*
         * LIGHT
         */
        pn("lumen", "lm");
        pn("lux", "lx");
        pn("footcandle", get("lux").scale(1.076391e-1));
        pn("footlambert", get("cd").divide(get("m").pow(2)).scale(3.426259));
        pn("lambert", get("cd").divide(get("m").pow(2)).scale(1e4 / Math.PI)); // exact
        pn("stilb", get("cd").divide(get("m").pow(2)).scale(1e4));
        pn("phot", get("lm").divide(get("m").pow(2)).scale(1e4)); // exact
        pn("nit", get("cd").multiply(get("m").pow(2))); // exact
        pn("langley", get("J").divide(get("m").pow(2)).scale(4.184000e4)); // exact
        pn("blondel", get("cd").divide(get("m").pow(2)).scale(1. / Math.PI));

        pn("apostilb", "blondel");
        ps("nt", "nit");
        ps("ph", "phot");
        ps("sb", "stilb");

        /*
         * MASS PER UNIT LENGTH
         */
        pn("denier", get("kg").divide(get("m")).scale(1.111111e-7));
        pn("tex", get("kg").divide(get("m")).scale(1e-6));

        /*
         * MASS PER UNIT TIME (INCLUDES FLOW)
         */
        px("perm 0C", get("kg").divide(
                get("Pa").multiply(get("s")).multiply(get("m").pow(2))).scale(
                5.72135e-11));
        px("perm 23C", get("kg").divide(
                get("Pa").multiply(get("s")).multiply(get("m").pow(2))).scale(
                5.74525e-11));

        /*
         * POWER
         */
        ps("VA", get("V").multiply(get("A")));
        pn("voltampere", "VA");
        pn("boiler horsepower", get("W").scale(9.80950e3));
        pn("shaft horsepower", get("W").scale(7.456999e2));
        pn("metric horsepower", get("W").scale(7.35499));
        pn("electric horsepower", get("W").scale(7.460000e2)); // exact
        pn("water horsepower", get("W").scale(7.46043e2));
        pn("UK horsepower", get("W").scale(7.4570e2));
        pn("refrigeration ton", get("Btu").divide(get("h")).scale(12000));

        pn("horsepower", "shaft horsepower");
        pn("ton of refrigeration", "refrigeration ton");
        ps("hp", "horsepower");

        /*
         * PRESSURE OR STRESS
         */
        pn("bar", get("Pa").scale(1e5)); // exact
        pn("standard atmosphere", get("Pa").scale(1.01325e5)); // exact
        pn("technical atmosphere", get("kg").multiply(
                get("gravity").divide(get("m").scale(.01).pow(2))));
        px("inch H2O 39F", get("inch").multiply(get("water 39F")));
        px("inch H2O 60F", get("inch").multiply(get("water 60F")));
        px("inch Hg 32F", get("inch").multiply(get("mercury 32F")));
        px("inch Hg 60F", get("inch").multiply(get("mercury 60F")));
        px("mm Hg 0C", get("m").scale(1e-3).multiply(get("mercury 0C")));
        ps("cmHg", get("m").scale(1e-2).multiply(get("Hg")));
        ps("cmH2O", get("m").scale(1e-2).multiply(get("water")));
        px("inch Hg", get("inch").multiply(get("Hg")));
        px("torr", get("m").scale(1e-3).multiply(get("Hg")));
        px("foot H2O", get("foot").multiply(get("water")));
        ps("psi", get("pound").multiply(
                get("gravity").divide(get("inch").pow(2))));
        ps("ksi", get("kip").divide(get("inch").pow(2)));
        pn("barie", get("N").divide(get("m").pow(2)).scale(0.1));

        px("footH2O", "foot H2O");
        ps("ftH2O", "foot H2O");
        pn("millimeter Hg", "torr");
        px("mm Hg", "torr");
        px("mm Hg", "torr");
        pn("pascal", "Pa");
        px("pal", "Pa");
        ps("inHg", "inch Hg");
        px("in Hg", "inch Hg");
        ps("at", "technical atmosphere");
        pn("atmosphere", "standard atmosphere");
        ps("atm", "standard atmosphere");
        pn("barye", "barie");

        /*
         * RADIATION UNITS
         */
        ps("Ci", get("Bq").scale(3.7e10)); // exact
        pn("rem", get("Sv").scale(1e-2)); // exact dose equivalent
        ps("rd", get("Gy").scale(1e-2)); // absorbed dose. exact.
        // use instead of "rad"
        ps("R", get("C").divide(get("kg")).scale(2.58e-4));

        ps("gray", "Gy");
        px("sie", "Sv");
        pn("becquerel", "Bq");
        px("rads", "rd"); // avoid "rad" (radian)
        pn("roentgen", "R");
        pn("curie", "Ci");

        /*
         * VELOCITY (INCLUDES SPEED)
         */
        ps("c", get("m").divide(get("s")).scale(2.997925e+8));
        pn("kt", get("nautical mile").divide(get("h")));

        px("knot international", "kt");
        px("international knot", "kt");
        pn("knot", "kt");

        /*
         * VISCOSITY
         */
        ps("P", get("Pa").multiply(get("s")).scale(1e-1));
        // exact
        ps("St", get("m").pow(2).divide(get("s")).scale(1e-4));
        // exact
        ps("rhe", get("Pa").multiply(get("s")).pow(-1).scale(10));

        pn("poise", "P");
        pn("stokes", "St");

        /*
         * VOLUME (INCLUDES CAPACITY)
         */
        px("acre feet", get("m").pow(3).scale(1.233489e3));
        // but `acre foot' is 1233.4867714897 m^3. Odd.
        px("board feet", get("m").pow(3).scale(2.359737e-3));
        pn("bushel", get("m").pow(3).scale(3.523907e-2));
        pn("UK liquid gallon", get("m").pow(3).scale(4.546090e-3)); // exact
        pn("Canadian liquid gallon", get("m").pow(3).scale(4.546090e-3)); // exact
        pn("US dry gallon", get("m").pow(3).scale(4.404884e-3));
        pn("US liquid gallon", get("m").pow(3).scale(3.785412e-3));
        ps("cc", get("m").scale(.01).pow(3));
        pn("liter", get("m").pow(3).scale(1e-3));
        // exact. However, from 1901 to 1964, 1 liter = 1.000028 dm3
        pn("stere", get("m").pow(3)); // exact
        ps("Bz", get("m").scale(1e-6).pow(3).log(10.0));
        pn("register ton", get("m").pow(3).scale(2.831685));
        pn("US dry quart", get("US dry gallon").scale(1. / 4));
        pn("US dry pint", get("US dry gallon").scale(1. / 8));
        pn("US liquid quart", get("US liquid gallon").scale(1. / 4));
        pn("US liquid pint", get("US liquid gallon").scale(1. / 8));
        pn("US liquid cup", get("US liquid gallon").scale(1. / 16));
        pn("US liquid gill", get("US liquid gallon").scale(1. / 32));
        pn("US liquid ounce", get("US liquid gallon").scale(1. / 128));
        pn("UK liquid quart", get("UK liquid gallon").scale(1. / 4));
        pn("UK liquid pint", get("UK liquid gallon").scale(1. / 8));
        pn("UK liquid cup", get("UK liquid gallon").scale(1. / 16));
        pn("UK liquid gill", get("UK liquid gallon").scale(1. / 32));
        pn("UK liquid ounce", get("UK liquid gallon").scale(1. / 160));

        pn("US fluid ounce", "US liquid ounce");
        pn("UK fluid ounce", "UK liquid ounce");
        pn("liquid gallon", "US liquid gallon");
        pn("fluid ounce", "US fluid ounce");
        pn("dry quart", "US dry quart");
        pn("dry pint", "US dry pint");

        pn("liquid quart", get("liquid gallon").scale(1. / 4));
        pn("liquid pint", get("liquid gallon").scale(1. / 8));
        ps("bbl", get("US liquid gallon").scale(42));
        // petroleum industry definition
        ps("pt", get("liquid pint"));

        pn("gallon", "liquid gallon");
        pn("quart", "liquid quart");

        pn("cup", get("liquid gallon").scale(1. / 16));
        pn("gill", get("liquid gallon").scale(1. / 32));
        pn("tablespoon", get("US fluid ounce").scale(0.5));
        pn("teaspoon", get("tablespoon").scale(1. / 3));
        pn("peck", get("bushel").scale(1. / 4));

        px("acre foot", "acre feet");
        px("board foot", "board feet");
        pn("barrel", "bbl");

        ps("gal", get("gallon")); // "gal" is also
        // (unused) acceleration unit
        ps("oz", "fluid ounce");
        px("floz", "fluid ounce");
        pn("Tbl", "tablespoon");
        ps("Tbsp", "tablespoon");
        ps("tbsp", "tablespoon");
        ps("Tblsp", "tablespoon");
        ps("tblsp", "tablespoon");
        pn("litre", "liter");
        ps("L", "liter");
        ps("l", "liter");
        px("tsp", "teaspoon");
        ps("pk", "peck");
        ps("bu", "bushel");

        ps("fldr", get("floz").scale(1. / 8));
        ps("dr", get("floz").scale(1. / 16));

        pn("firkin", get("bbl").scale(1. / 4));
        // exact but "barrel" is vague
        pn("pint", "pt");
        ps("dram", "dr");

        /*
         * VOLUME PER UNIT TIME
         */
        pn("sverdrup", get("m").pow(3).scale(1e6).divide(get("s"))); // oceanographic
        // flow

        /*
         * COMPUTERS AND COMMUNICATION
         */
        pn("bit", new ScaledUnit(1)); // unit of information
        ps("Bd", get("Hz"));
        ps("bps", get("Hz"));
        ps("cps", get("cycle").divide(get("s")));

        pn("baud", "Bd");

        /*
         * MISC
         */
        pn("kayser", get("m").pow(-1).scale(1e2)); // exact
        ps("rps", get("revolution").divide(get("s")));
        ps("rpm", get("revolution").divide(get("min")));
        px("geopotential", get("gravity"));
        pn("work year", get("hours").scale(2056));
        pn("work month", get("work year").scale(1. / 12));

        pn("count", "");
        ps("gp", "geopotential");
        px("dynamic", "geopotential");
        ps("gpm", get("geopotential").multiply(get("meter")));
        // Potential vorticity unit:
        ps("PVU", get("m").pow(2).divide(get("s")).multiply(get("K")).divide(get("kg")).scale(1e-6));
    }

    /**
     * Gets an instance of this class.
     * 
     * This is the only way to obtain an instance of this class.
     * 
     * @throws UnitException
     *             Something went wrong in generating the singleton instance of
     *             the database. This should not occur and indicates an internal
     *             inconsistancy.
     */
    public static UnitsDB instance() throws UnitException {
        return db;
    }

    /**
     * Get a unit.
     * 
     * @param name
     *            The name of the unit to be retrieved. It may be the plural
     *            form (e.g. "yards"). If an entry in the database corresponding
     *            to the complete name is not found and the given name ends with
     *            an `s', then a search will be made for the singular form (e.g.
     *            "yard"). The matching entry will be returned only if the entry
     *            permits a plural form. The entry may also have one or more SI
     *            prefixes (e.g. "mega", "M").
     * @return The appropriate unit or <code>null</code>. The unit will account
     *         for any SI prefixes in the name.
     * @require The argument is non-<code>null</code>.
     */
    @Override
    public Unit get(final String name) {
        Unit unit = super.get(name);

        if (unit == null) {
            // System.out.println("Entry \"" + name + "\" not found");
            /*
             * No entry by that name (including any possible plural form).
             */

            /*
             * Strip prefix.
             */
            final Prefixer prefixer = new Prefixer(name);

            if (prefixer.stripPrefix(prefixNames, prefixSymbols)) {
                // System.out.println("Prefix found");
                // System.out.println("Looking for \"" + prefixer.getString() +
                // "\"");
                /*
                 * Prefix found. Recurse on the rest of the string.
                 */
                if ((unit = get(prefixer.getString())) != null) {
                    try {
                        unit = unit.scale(prefixer.getValue());
                    }
                    catch (final UnitException e) {
                        unit = null;
                    }
                }
            }
        }

        return unit;
    }

    /**
     * Adds a symbol to the database for a unit already in the database.
     */
    protected void ps(final String symbol, final String unitID) {
        putSymbol(symbol, super.get(unitID));
    }

    /**
     * Adds a symbol and a new unit to the database.
     * 
     * @throws UnitException
     */
    protected void ps(final String symbol, final Unit unit)
            throws UnitException {
        putSymbol(symbol, unit.clone(symbol));
    }

    /**
     * Adds a name, the plural form of the name, and a new unit to the database.
     * 
     * @throws UnitException
     */
    protected void pn(final String name, Unit unit) throws UnitException {
        unit = unit.clone(name);
        putName(name, unit);
        putName(makePlural(name), unit);
    }

    /**
     * Adds a name and it's regular plural form to the database for a unit
     * that's already in the database.
     */
    protected void pn(final String name, final String unitID) {
        final Unit unit = super.get(unitID);
        putName(name, unit);
        putName(makePlural(name), unit);
    }

    /**
     * Adds a name that has no plural form and a new unit to the database.
     * 
     * @throws UnitException
     */
    protected void px(final String name, final Unit unit) throws UnitException {
        putName(name, unit.clone(name));
    }

    /**
     * Adds a name that has no plural form to the database for a unit that's
     * already in the database.
     */
    protected void px(final String name, final String unitID) {
        putName(name, super.get(unitID));
    }

    /**
     * Inner (helper) class for parsing unit prefixes.
     */
    protected class Prefixer {
        /**
         * The string being parsed.
         */
        protected final String  string;

        /**
         * The current position within the string.
         */
        protected int           pos;

        /**
         * The current value of the prefix.
         */
        protected double        value;

        /**
         * Construct.
         */
        protected Prefixer(final String string) {
            this.string = string;
            this.pos = 0;
            this.value = 1;
        }

        /**
         * Strip leading prefix from the string.
         */
        protected boolean stripPrefix(final UnitPrefix[] names,
                final UnitPrefix[] symbols) {
            /*
             * Perform a case-insensitive search on the names.
             */
            for (int icur = 0; icur < names.length; ++icur) {
                final UnitPrefix prefix = names[icur];

                if (string.regionMatches(true, pos, prefix.name, 0, prefix.name
                        .length())) {
                    value *= prefix.value;
                    pos += prefix.name.length();
                    return true;
                }
            }

            /*
             * Perform a case-sensitive search on the symbols.
             */
            for (int icur = 0; icur < symbols.length; ++icur) {
                final UnitPrefix prefix = symbols[icur];

                if (string.startsWith(prefix.name, pos)) {
                    value *= prefix.value;
                    pos += prefix.name.length();
                    return true;
                }
            }

            return false;
        }

        /**
         * Indicate whether or not the beginning of the remainder of the string
         * is less than a prefix.
         */
        protected boolean isLessThan(final UnitPrefix prefix) {
            int icomp = 1;
            final int n = Math.min(prefix.name.length(), string.length() - pos);

            for (int i = 0; i < n; ++i) {
                icomp = Character.getNumericValue(string.charAt(pos + i))
                        - Character.getNumericValue(prefix.name.charAt(i));

                if (icomp != 0) {
                    break;
                }
            }

            // System.out.println(string.substring(pos) +
            // (icomp < 0 ? " < " : " >= ") + prefix.name);

            return icomp < 0;
        }

        /**
         * Return the current, remaining string.
         */
        protected String getString() {
            return string.substring(pos);
        }

        /**
         * Return the current prefix value.
         */
        protected double getValue() {
            return value;
        }
    }

    /**
     * Test this class.
     * 
     * @exception java.lang.Exception
     *                A problem occurred.
     */
    public static void main(final String[] args) throws Exception {
        final UnitsDB db = DefaultUnitsDB.instance();

        System.out.println("% = " + db.get("%"));
        System.out.println("abampere = " + db.get("abampere"));
        System.out.println("firkin = " + db.get("firkin"));
        System.out.println("MiCrOmEgAfirkin = " + db.get("MiCrOmEgAfirkin"));
        System.out.println("celsius = " + db.get("celsius"));
        System.out.println("fahrenheit = " + db.get("fahrenheit"));
        System.out.println("m = " + db.get("m"));
        System.out.println("mm = " + db.get("mm"));
        System.out.println("dam = " + db.get("dam"));
        System.out.println("million = " + db.get("million"));
        System.out.println("pascal = " + db.get("pascal"));
        System.out.println("Tperm_0C = " + db.get("Tperm_0C"));
        System.out.println("MILLIpoundal = " + db.get("MILLIpoundal"));

        System.out.println("");
        db.list();
    }
}
