/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DefaultUnitsDB.java,v 1.5 1998-03-10 22:24:18 steve Exp $
 */

package visad.data.netcdf.units;


import visad.BaseUnit;
import visad.DerivedUnit;
import visad.OffsetUnit;
import visad.SI;
import visad.ScaledUnit;
import visad.Unit;
import visad.UnitException;


/**
 * Default units database.
 *
 * This database knows about approximately 500 different units.  Users 
 * can also add new units to the database at runtime.
 *
 * The basis for this units database is the International System of Units 
 * (SI).
 *
 * This is a singleton class.
 */
public class
DefaultUnitsDB
    extends	UnitsDB
    implements	java.io.Serializable
{
    /**
     * The unit table.
     * Effectively "final".
     */
    protected UnitTable			table;

    /**
     * The unit prefixes in order of lexicographic length:
     */
    protected final UnitPrefix[]	prefixes =
    {
	new UnitPrefix("centi",	1e-2),
	new UnitPrefix("femto",	1e-15),
	new UnitPrefix("hecto",	1e2),
	new UnitPrefix("micro",	1e-6),
	new UnitPrefix("milli",	1e-3),
	new UnitPrefix("yocto",	1e-24),
	new UnitPrefix("yotta",	1e24),
	new UnitPrefix("zepto",	1e-21),
	new UnitPrefix("zetta",	1e21),
	new UnitPrefix("atto",	1e-18),
	new UnitPrefix("deca",	1e1),	// Spelling according to "ISO 2955:
					// Information processing --
					// Representation of SI and other units
					// in systems with limited character
					// sets"
	new UnitPrefix("deci",	1e-1),
	new UnitPrefix("deka",	1e1),	// Spelling according to "ASTM
					// Designation: E 380 - 85: Standard
					// for METRIC PRACTICE", "ANSI/IEEE Std
					// 260-1978 (Reaffirmed 1985): IEEE
					// Standard Letter Symbols for Units of
					// Measurement", and NIST Special
					// Publication 811, 1995 Edition:
					// "Guide for the Use of the
					// International System of Units (SI)".
	new UnitPrefix("giga",	1e9),	// 1st syllable pronounced "jig"
					// according to "ASTM Designation: E
					// 380 - 85: Standard for METRIC
					// PRACTICE".
	new UnitPrefix("kilo",	1e3),
	new UnitPrefix("mega",	1e6),
	new UnitPrefix("nano",	1e-9),
	new UnitPrefix("peta",	1e15),
	new UnitPrefix("pico",	1e-12),
	new UnitPrefix("tera",	1e12),
	new UnitPrefix("exa",	1e18),
	new UnitPrefix("da",	1e1),
	new UnitPrefix("E",	1e18),
	new UnitPrefix("G",	1e9),
	new UnitPrefix("M",	1e6),
	new UnitPrefix("P",	1e15),
	new UnitPrefix("T",	1e12),
	new UnitPrefix("Y",	1e24),
	new UnitPrefix("Z",	1e21),
	new UnitPrefix("a",	1e-18),
	new UnitPrefix("c",	1e-2),
	new UnitPrefix("d",	1e-1),
	new UnitPrefix("f",	1e-15),
	new UnitPrefix("h",	1e2),
	new UnitPrefix("k",	1e3),
	new UnitPrefix("m",	1e-3),
	new UnitPrefix("n",	1e-9),
	new UnitPrefix("p",	1e-12),
	new UnitPrefix("u",	1e-6),
	new UnitPrefix("y",	1e-24),
	new UnitPrefix("z",	1e-21),
    };


    /**
     * Construct a units database.
     *
     * @exception	UnitException	Something went wrong in generating
     *			a unit for the database.  This should not occur and
     *			indicates an internal inconsistancy.
     */
    protected
    DefaultUnitsDB()
	throws UnitException
    {
	/*
	 * Create a unit table of the proper size.  Because
	 * increasing the size might be expensive, the initial
	 * size should be kept in sync with the actual number of 
	 * entries (e.g. vi: .,$w !grep 'put(' | wc -l)
	 */
	table = new UnitTable(500);


	/*
	 * The base units:
	 */
	put(new PluralUnit("ampere",	SI.ampere));
	put(new PluralUnit("candela",	SI.candela));
	put(new PluralUnit("kelvin",	SI.kelvin));
	put(new PluralUnit("kilogram",	SI.kilogram));
	put(new PluralUnit("meter",	SI.meter));
	put(new PluralUnit("mole",	SI.mole));
	put(new PluralUnit("second",	SI.second));
	put(new PluralUnit("radian",	SI.radian));


	/*
	 * Constants:
	 */
	put(new SingleUnit("percent",	new ScaledUnit(0.01)));
	put(new SingleUnit("PI",	new ScaledUnit(Math.PI)));
	put(new SingleUnit("bakersdozen",	new ScaledUnit(13)));
	put(new PluralUnit("pair",	new ScaledUnit(2)));
	put(new PluralUnit("ten",	new ScaledUnit(10)));
	put(new SingleUnit("dozen",	new ScaledUnit(12)));
	put(new SingleUnit("score",	new ScaledUnit(20)));
	put(new PluralUnit("hundred",	new ScaledUnit(100)));
	put(new PluralUnit("thousand",	new ScaledUnit(1.0e3)));
	put(new PluralUnit("million",	new ScaledUnit(1.0e6)));

	// NB: "billion" is ambiguous (1e9 in U.S. but 1e12 in U.K.)

	put(new SingleUnit("%",		get("percent")));
	put(new SingleUnit("pi",	get("PI")));

	/*
	 * NB: All subsequent definitions must be given in terms of
	 * earlier definitions.  Forward referencing is not permitted.
	 */

	/*
	 * The following are non-base units of the fundamental quantities
	 */

	/*
	 * UNITS OF ELECTRIC CURRENT
	 */
	put(new SingleUnit("A",		get("ampere")));
	put(new PluralUnit("amp",	get("ampere")));
	put(new PluralUnit("abampere",	get("decaampere")));
							// exact
	put(new PluralUnit("gilbert",	get("ampere").scale(7.957747e-1)));
	put(new PluralUnit("statampere",get("ampere").scale(3.335640e-10)));
	put(new PluralUnit("biot",	get("abampere")));

	/*
	 * UNITS OF LUMINOUS INTENSITY
	 */
	put(new SingleUnit("cd",	get("candela")));
	put(new PluralUnit("candle",	get("candela")));

	/*
	 * UNITS OF THERMODYNAMIC TEMPERATURE
	 */
	put(new PluralUnit("degree_Kelvin",	get("kelvin")));
	put(new SingleUnit("degree_Celsius",
	    new OffsetUnit(273.15, (BaseUnit)get("kelvin"))));
	put(new PluralUnit("degree_Rankine",
	    get("kelvin").scale(1/1.8)));
	put(new PluralUnit("degree_Fahrenheit",
	    get("degree_Rankine").shift(459.67)));

	//put(new SingleUnit("C",	get("degree_Celsius")));
						// `C' means `coulomb'
	put(new SingleUnit("Celsius",	get("degree_Celsius")));
	put(new SingleUnit("celsius",	get("degree_Celsius")));
	put(new SingleUnit("degree_centigrade",	get("degree_Celsius")));
	put(new SingleUnit("degC",	get("degree_Celsius")));
	put(new SingleUnit("degreeC",	get("degree_Celsius")));
	put(new SingleUnit("degree_C",	get("degree_Celsius")));
	put(new SingleUnit("degree_c",	get("degree_Celsius")));
	put(new SingleUnit("deg_C",	get("degree_Celsius")));
	put(new SingleUnit("deg_c",	get("degree_Celsius")));
	put(new SingleUnit("degK",	get("kelvin")));
	put(new SingleUnit("degreeK",	get("kelvin")));
	put(new SingleUnit("degree_K",	get("kelvin")));
	put(new SingleUnit("degree_k",	get("kelvin")));
	put(new SingleUnit("deg_K",	get("kelvin")));
	put(new SingleUnit("deg_k",	get("kelvin")));
	put(new SingleUnit("K",		get("kelvin")));
	put(new PluralUnit("Kelvin",	get("kelvin")));

	put(new SingleUnit("degF",	get("degree_Fahrenheit")));
	put(new SingleUnit("degreeF",	get("degree_Fahrenheit")));
	put(new SingleUnit("degree_F",	get("degree_Fahrenheit")));
	put(new SingleUnit("degree_f",	get("degree_Fahrenheit")));
	put(new SingleUnit("deg_F",	get("degree_Fahrenheit")));
	put(new SingleUnit("deg_f",	get("degree_Fahrenheit")));
	put(new SingleUnit("F",		get("degree_Fahrenheit")));
	put(new PluralUnit("Fahrenheit",get("degree_Fahrenheit")));
	put(new PluralUnit("fahrenheit",get("degree_Fahrenheit")));

	put(new SingleUnit("degR",	get("degree_Rankine")));
	put(new SingleUnit("degreeR",	get("degree_Rankine")));
	put(new SingleUnit("degree_R",	get("degree_Rankine")));
	put(new SingleUnit("degree_r",	get("degree_Rankine")));
	put(new SingleUnit("deg_R",	get("degree_Rankine")));
	put(new SingleUnit("deg_r",	get("degree_Rankine")));
	//put(new SingleUnit("R",	get("degree_Rankine")));
						// "R" means "roentgen"
	put(new PluralUnit("Rankine",	get("degree_Rankine")));
	put(new PluralUnit("rankine",	get("degree_Rankine")));

	/*
	 * UNITS OF MASS
	 */
	put(new PluralUnit("assay_ton",	get("kilogram").scale(2.916667e-2)));
	put(new PluralUnit("avoirdupois_ounce",
	    get("kilogram").scale(2.834952e-2)));
	put(new PluralUnit("avoirdupois_pound",
	    get("kilogram").scale(4.5359237e-1)));	// exact
	put(new PluralUnit("carat",	get("kilogram").scale(2e-4)));
	put(new PluralUnit("grain",	get("kilogram").scale(6.479891e-5)));
						// exact
	put(new PluralUnit("gram",	get("kilogram").scale(1e-3)));
						// exact
	put(new SingleUnit("kg",	get("kilogram")));
	put(new PluralUnit("long_hundredweight",
	    get("kilogram").scale(5.080235e1)));
	put(new PluralUnit("metric_ton",get("megagram")));	// exact
	put(new PluralUnit("pennyweight",
	    get("kilogram").scale(1.555174e-3)));
	put(new PluralUnit("short_hundredweight",
	    get("kilogram").scale(4.535924e1)));
	put(new PluralUnit("slug",	get("kilogram").scale(14.59390)));
	put(new PluralUnit("troy_ounce",get("kilogram").scale(3.110348e-2)));
	put(new PluralUnit("troy_pound",get("kilogram").scale(3.732417e-1)));
	put(new PluralUnit("atomic_mass_unit",
	    get("kilogram").scale(1.66054e-27)));

	put(new PluralUnit("tonne",	get("metric_ton")));
	put(new PluralUnit("apothecary_ounce",	get("troy_ounce")));
	put(new PluralUnit("apothecary_pound",	get("avoirdupois_pound")));
	put(new PluralUnit("pound",	get("avoirdupois_pound")));
	put(new PluralUnit("metricton",	get("metric_ton")));
	put(new SingleUnit("gr",	get("grain")));
	put(new PluralUnit("scruple",	get("grain").scale(20)));
	put(new PluralUnit("apdram",	get("grain").scale(60)));
	put(new PluralUnit("apounce",	get("grain").scale(480)));
	put(new PluralUnit("appound",	get("grain").scale(5760)));
	put(new PluralUnit("atomicmassunit",	get("atomic_mass_unit")));
	put(new PluralUnit("amu",	get("atomic_mass_unit")));

	put(new SingleUnit("t",		get("tonne")));
	put(new PluralUnit("lb",	get("pound")));
	put(new PluralUnit("bag",	get("pound").scale(94)));
	put(new PluralUnit("short_ton",	get("pound").scale(2000)));
	put(new PluralUnit("long_ton",	get("pound").scale(2240)));

	put(new PluralUnit("ton",	get("short_ton")));
	put(new PluralUnit("shortton",	get("short_ton")));
	put(new PluralUnit("longton",	get("long_ton")));

	/*
	 * UNITS OF LENGTH
	 */
	put(new PluralUnit("metre",	get("meter")));
	put(new PluralUnit("angstrom",	get("meter").scale(1e-10)));
	put(new PluralUnit("astronomical_unit",
	    get("meter").scale(1.495979e11)));
	put(new PluralUnit("fermi",	get("femtometer")));	// exact
	put(new SingleUnit("m",		get("meter")));
	put(new PluralUnit("metre",	get("meter")));
	put(new PluralUnit("light_year",get("meter").scale(9.46073e15)));
	put(new PluralUnit("micron",	get("micrometer")));	// exact
	put(new PluralUnit("mil",	get("meter").scale(2.54e-5)));
								// exact
	put(new PluralUnit("nautical_mile",
	    get("meter").scale(1.852000e3)));		// exact
	put(new PluralUnit("parsec",	get("meter").scale(3.085678e16)));
	put(new PluralUnit("printers_point",
	    get("meter").scale(3.514598e-4)));

	/*
	 * God help us!  There's an international foot and a US survey foot and
	 * they're not the same!
	 */

	// US Survey foot stuff:
	put(new SingleUnit("US_survey_foot",
	    get("meter").scale(1200/3937.)));		// exact
	put(new SingleUnit("US_survey_feet",	get("US_survey_foot")));
	put(new PluralUnit("US_survey_yard",
	    get("US_survey_feet").scale(3)));		// exact
	put(new PluralUnit("US_survey_mile",
	    get("US_survey_feet").scale(5280)));		// exact
	put(new PluralUnit("US_statute_mile",	get("US_survey_mile")));
	put(new PluralUnit("rod",
	    get("US_survey_feet").scale(16.5)));		// exact
	put(new PluralUnit("pole",		get("rod")));
	put(new SingleUnit("perch",		get("rod")));
	put(new SingleUnit("perches",		get("perch")));
	put(new PluralUnit("furlong",
	    get("US_survey_feet").scale(660)));		// exact
	put(new PluralUnit("fathom",
	    get("US_survey_feet").scale(6)));		// exact

	// International foot stuff:
	put(new SingleUnit("international_inch",
	    get("meter").scale(.0254)));			// exact
	put(new SingleUnit("international_inches",
	    get("international_inch")));			// alias
	put(new SingleUnit("international_foot",
	    get("international_inches").scale(12)));		// exact
	put(new SingleUnit("international_feet",
	    get("international_foot")));			// alias
	put(new PluralUnit("international_yard",
	    get("international_feet").scale(3)));		// exact
	put(new PluralUnit("international_mile",
	    get("international_feet").scale(5280)));		// exact

	// Alias unspecified units to the international units:
	put(new SingleUnit("inch",	
	    get("international_inch")));	// alias
	put(new SingleUnit("foot",
	    get("international_foot")));	// alias
	put(new PluralUnit("yard",
	    get("international_yard")));	// alias
	put(new PluralUnit("mile",
	    get("international_mile")));	// alias

	// The following should hold regardless:
	put(new SingleUnit("inches",	get("inch")));		// alias
	put(new SingleUnit("in",	get("inches")));	// alias
	put(new SingleUnit("feet",	get("foot")));		// alias
	put(new SingleUnit("ft",	get("feet")));		// alias
	put(new SingleUnit("yd",	get("yard")));		// alias

	put(new PluralUnit("chain",
	    get("meter").scale(2.011684e1)));

	put(new PluralUnit("printers_pica",
	    get("printers_point").scale(12)));		// exact
	put(new PluralUnit("astronomicalunit",	get("astronomical_unit")));
	put(new SingleUnit("au",	get("astronomical_unit")));
	put(new PluralUnit("nmile",	get("nautical_mile")));
	put(new SingleUnit("nmi",	get("nautical_mile")));

	put(new PluralUnit("pica",	get("printers_pica")));
	put(new PluralUnit("big_point",	get("inch").scale(1./72)));
								// exact
	put(new PluralUnit("barleycorn",get("inch").scale(1./3)));

	put(new PluralUnit("arpentlin",	get("foot").scale(191.835)));

	/*
	 * UNITS OF AMOUNT OF SUBSTANCE
	 */
	put(new SingleUnit("mol",	get("mole")));

	/*
	 * UNITS OF TIME
	 */
	put(new PluralUnit("day",		
	    get("second").scale(8.64e4)));		// exact
	put(new PluralUnit("hour",	
	    get("second").scale(3.6e3)));		// exact
	put(new PluralUnit("minute",	get("second").scale(60)));
							// exact
	put(new SingleUnit("s",		get("second")));
	put(new PluralUnit("sec",	get("second")));
	put(new PluralUnit("shake",	get("second").scale(1e-8)));
							// exact
	put(new PluralUnit("sidereal_day",
	    get("second").scale(8.616409e4)));
	put(new PluralUnit("sidereal_hour",
	    get("second").scale(3.590170e3)));
	put(new PluralUnit("sidereal_minute",
	    get("second").scale(5.983617e1)));
	put(new PluralUnit("sidereal_second",
	    get("second").scale(0.9972696)));
	put(new PluralUnit("sidereal_year",	
	    get("second").scale(3.155815e7)));
	/*
	 * Interval between 2 successive passages of sun through vernal equinox
	 * (365.242198781 days -- see 
	 * http://www.ast.cam.ac.uk/pubinfo/leaflets/,
	 * http://aa.usno.navy.mil/AA/
	 * and http://adswww.colorado.edu/adswww/astro_coord.html):
	 */
	put(new PluralUnit("tropical_year",
	    get("second").scale(3.15569259747e7)));
	put(new PluralUnit("lunar_month",
	    get("day").scale(29.530589)));

	put(new PluralUnit("common_year",	get("day").scale(365)));
						    // exact: 153600e7 seconds
	put(new PluralUnit("leap_year",		get("day").scale(366)));
						    // exact
	put(new PluralUnit("Julian_year",	get("day").scale(365.25)));
						    // exact
	put(new PluralUnit("Gregorian_year",	get("day").scale(365.2425)));
						    // exact
	put(new PluralUnit("sidereal_month",	
	    get("day").scale(27.321661)));
	put(new PluralUnit("tropical_month",	
	    get("day").scale(27.321582)));
	put(new SingleUnit("d",		get("day")));
	put(new PluralUnit("min",	get("minute")));
	put(new PluralUnit("hr",	get("hour")));
	put(new SingleUnit("h",		get("hour")));
	put(new PluralUnit("fortnight",	get("day").scale(14)));	
						    // exact
	put(new PluralUnit("week",	get("day").scale(7)));
						    // exact
	put(new SingleUnit("jiffy",	get("centisecond")));
						    // believe it or not!
	put(new SingleUnit("jiffies",	get("jiffy")));
						    // assumed plural spelling

	put(new PluralUnit("year",	get("tropical_year")));

	put(new PluralUnit("yr",	get("year")));
	put(new SingleUnit("a",		get("year")));		// "anno"
	put(new PluralUnit("eon",	get("gigayear")));	// fuzzy
	put(new PluralUnit("month",	get("year").scale(1./12)));
						    // on average

	/*
	 * UNITS OF PLANE ANGLE
	 */
	//put(new PluralUnit("rad", get("radian")));
	    // "rad" means "gray"
		
	put(new PluralUnit("circle",		
	    get("radian").scale(2*Math.PI)));
	put(new PluralUnit("angular_degree",
	    get("radian").scale(Math.PI/180.)));

	put(new PluralUnit("turn",		get("circle")));
	put(new PluralUnit("degree",		get("angular_degree")));
	put(new SingleUnit("degree_north",	get("angular_degree")));
	put(new SingleUnit("degree_east",	get("angular_degree")));
	put(new SingleUnit("degree_true",	get("angular_degree")));
	put(new PluralUnit("arcdeg",		get("angular_degree")));
	put(new PluralUnit("angular_minute",
	    get("angular_degree").scale(1./60)));
	put(new PluralUnit("angular_second",
	    get("angular_minute").scale(1./60)));
	put(new PluralUnit("grade",		
	    get("angular_degree").scale(0.9)));	// exact

	put(new SingleUnit("degrees_north",	get("degree_north")));
	put(new SingleUnit("degreeN",		get("degree_north")));
	put(new SingleUnit("degree_N",		get("degree_north")));
	put(new SingleUnit("degreesN",		get("degree_north")));
	put(new SingleUnit("degrees_N",		get("degree_north")));

	put(new SingleUnit("degrees_east",	get("degree_east")));
	put(new SingleUnit("degreeE",		get("degree_east")));
	put(new SingleUnit("degree_E",		get("degree_east")));
	put(new SingleUnit("degreesE",		get("degree_east")));
	put(new SingleUnit("degrees_E",		get("degree_east")));

	put(new SingleUnit("degree_west",	
	    get("degree_east").scale(-1)));
	put(new SingleUnit("degrees_west",	get("degree_west")));
	put(new SingleUnit("degreeW",		get("degree_west")));
	put(new SingleUnit("degree_W",		get("degree_west")));
	put(new SingleUnit("degreesW",		get("degree_west")));
	put(new SingleUnit("degrees_W",		get("degree_west")));

	put(new SingleUnit("degrees_true",	get("degree_true")));
	put(new SingleUnit("degreeT",		get("degree_true")));
	put(new SingleUnit("degree_T",		get("degree_true")));
	put(new SingleUnit("degreesT",		get("degree_true")));
	put(new SingleUnit("degrees_T",		get("degree_true")));

	put(new PluralUnit("arcminute",		get("angular_minute")));
	put(new PluralUnit("arcsecond",		get("angular_second")));

	put(new PluralUnit("arcmin",		get("arcminute")));
	put(new PluralUnit("arcsec",		get("arcsecond")));

	/*
	 * The following are derived units with special names.  They are
	 * useful for defining other derived units.
	 */
	put(new PluralUnit("steradian",	get("radian").pow(2)));
	put(new SingleUnit("hertz",	get("second").pow(-1)));
	put(new PluralUnit("newton",	get("kilogram").multiply(
	    get("meter").divide(get("second").pow(2)))));
	put(new PluralUnit("coulomb",	get("ampere").multiply(get("second"))));
	put(new PluralUnit("lumen",	
	    get("candela").multiply(get("steradian"))));
	put(new PluralUnit("becquerel",	get("hertz")));
	    // SI unit of activity of a radionuclide
	put(new SingleUnit("standard_free_fall",	get("meter").divide(
	    get("second").pow(2)).scale(9.806650)));

	put(new PluralUnit("pascal",	
	    get("newton").divide(get("meter").pow(2))));
	put(new PluralUnit("joule",	get("newton").multiply(get("meter"))));
	put(new SingleUnit("hz",	get("hertz")));
	put(new SingleUnit("sr",	get("steradian")));
	put(new SingleUnit("force",	get("standard_free_fall")));
	put(new SingleUnit("gravity",	get("standard_free_fall")));
	put(new SingleUnit("free_fall",	get("standard_free_fall")));
	put(new SingleUnit("lux",	
	    get("lumen").divide(get("meter").pow(2))));
	put(new PluralUnit("sphere",	get("steradian").scale(4*Math.PI)));

	put(new SingleUnit("luxes",	get("lux")));
	put(new PluralUnit("watt",	get("joule").divide(get("second"))));
	put(new PluralUnit("gray",	get("joule").divide(get("kilogram"))));
						// absorbed dose. derived unit
	put(new PluralUnit("sievert",	get("joule").divide(get("kilogram"))));
						// dose equivalent. derived unit
	put(new SingleUnit("conventional_mercury",	get("gravity").multiply(
	    get("kilogram").divide(get("meter").pow(3))).scale(13595.10)));
	put(new SingleUnit("mercury_0C",	get("gravity").multiply(
	    get("kilogram").divide(get("meter").pow(3))).scale(13595.1)));
	put(new SingleUnit("mercury_60F",	get("gravity").multiply(
	    get("kilogram").divide(get("meter").pow(3))).scale(13556.8)));
	put(new SingleUnit("conventional_water",get("gravity").multiply(
	    get("kilogram").divide(get("meter").pow(3))).scale(1000)));
							// exact
	put(new SingleUnit("water_4C",		get("gravity").multiply(
	    get("kilogram").divide(get("meter").pow(3))).scale(999.972)));
	put(new SingleUnit("water_60F", 	get("gravity").multiply(
	    get("kilogram").divide(get("meter").pow(3))).scale(999.001)));
	put(new SingleUnit("g",	get("gravity")));	// approx.
							// should be `local'

	put(new PluralUnit("volt",	get("watt").divide(get("ampere"))));
	put(new SingleUnit("mercury_32F",	get("mercury_0C")));
	put(new SingleUnit("water_39F",	get("water_4C")));	
	    // actually 39.2 F
	put(new SingleUnit("mercury",	get("conventional_mercury")));
	put(new SingleUnit("water",	get("conventional_water")));

	put(new PluralUnit("farad",	get("coulomb").divide(get("volt"))));
	put(new PluralUnit("ohm",	get("volt").divide(get("ampere"))));
	put(new SingleUnit("siemens",	get("ampere").divide(get("volt"))));
	put(new PluralUnit("weber",	get("volt").multiply(get("second"))));
	put(new SingleUnit("Hg",	get("mercury")));
	put(new SingleUnit("hg",	get("mercury")));
	put(new SingleUnit("H2O",	get("water")));
	put(new SingleUnit("h2o",	get("water")));

	put(new PluralUnit("tesla",	
	    get("weber").divide(get("meter").pow(2))));
	put(new PluralUnit("henry",	get("weber").divide(get("ampere"))));

	/*
	 * The following are compound units: units whose definitions consist 
	 * of two or more base units.  They may now be defined in terms of the 
	 * preceding units.
	 */

	/*
	 * ACCELERATION
	 */
	put(new PluralUnit("gal",	get("meter").divide(
	    get("second").pow(2)).scale(1e-2)));

	/*
	 * AREA
	 */
	put(new PluralUnit("are",	get("meter").pow(2).scale(1e2)));
						// exact
	put(new PluralUnit("barn",	get("meter").pow(2).scale(1e-28)));
						// exact
	put(new PluralUnit("circular_mil",
	    get("meter").pow(2).scale(5.067075e-10)));
	put(new PluralUnit("darcy",	get("meter").pow(2).scale(
	    9.869233e-13)));			// permeability of porous solids
	put(new PluralUnit("hectare",	get("hectoare")));	// exact
	put(new PluralUnit("acre",	get("rod").pow(2).scale(160)));
						// exact

	/*
	 * ELECTRICITY AND MAGNETISM
	 */
	put(new PluralUnit("abfarad",	get("gigafarad")));	// exact
	put(new PluralUnit("abhenry",	get("nanohenry")));	// exact
	put(new PluralUnit("abmho",	get("gigasiemens")));	// exact
	put(new PluralUnit("abohm",	get("nanoohm")));	// exact
	put(new PluralUnit("abvolt",	get("volt").scale(1e-8)));
								// exact
	put(new SingleUnit("C",		get("coulomb")));
	put(new SingleUnit("e",	get("coulomb").scale(1.60217733-19)));
	put(new PluralUnit("chemical_faraday",
	    get("coulomb").scale(9.64957e4)));
	put(new PluralUnit("physical_faraday",
	    get("coulomb").scale(9.65219e4)));
	put(new PluralUnit("C12_faraday",
	    get("coulomb").scale(9.648531e4)));
	put(new PluralUnit("gamma",	get("nanotesla")));	// exact
	put(new SingleUnit("gauss",	get("tesla").scale(1e-4)));
						// exact
	put(new SingleUnit("H", get("henry")));
	put(new PluralUnit("maxwell",	get("weber").scale(1e-8)));
						// exact
	put(new PluralUnit("oersted",	get("ampere").divide(
	    get("meter")).scale(7.957747e1)));
	put(new SingleUnit("S", get("siemens")));
	put(new PluralUnit("statcoulomb",	
	    get("coulomb").scale(3.335640e-10)));
	put(new PluralUnit("statfarad",	get("farad").scale(1.112650e-12)));
	put(new PluralUnit("stathenry",	get("henry").scale(8.987554e11)));
	put(new PluralUnit("statmho",	get("siemens").scale(1.112650e-12)));
	put(new PluralUnit("statohm",	get("ohm").scale(8.987554e11)));
	put(new PluralUnit("statvolt",	get("volt").scale(2.997925e2)));
	put(new SingleUnit("T", get("tesla")));	put(new PluralUnit("unit_pole",
	    get("weber").scale(1.256637e-7)));
	put(new SingleUnit("V",		get("volt")));
	put(new SingleUnit("Wb",	get("weber")));
	put(new PluralUnit("mho",	get("siemens")));
	put(new SingleUnit("Oe",	get("oersted")));
	put(new PluralUnit("faraday",	get("C12_faraday")));
	    // charge of 1 mole of electrons

	/*
	 * ENERGY (INCLUDES WORK)
	 */
	put(new PluralUnit("electronvolt",	
	    get("joule").scale(1.602177e-19)));
	put(new PluralUnit("erg",	get("joule").scale(1e-7)));
						// exact
	put(new PluralUnit("IT_Btu",	
	    get("joule").scale(1.05505585262e3)));	// exact
	put(new PluralUnit("EC_therm",	get("joule").scale(1.05506e8)));
						// exact
	put(new PluralUnit("thermochemical_calorie",
	    get("joule").scale(4.184000)));		// exact
	put(new PluralUnit("IT_calorie",	get("joule").scale(4.1868)));
						// exact
	put(new SingleUnit("J", get("joule")));	put(new SingleUnit("ton_TNT",
	    get("joule").scale(4.184e9)));
	put(new PluralUnit("US_therm",	get("joule").scale(1.054804e8)));
						// exact
	put(new PluralUnit("watthour",	get("watt").multiply(get("hour"))));

	put(new PluralUnit("therm",	get("US_therm")));
	put(new SingleUnit("Wh",	get("watthour")));
	put(new PluralUnit("Btu",	get("IT_Btu")));
	put(new PluralUnit("calorie",	get("IT_calorie")));
	put(new PluralUnit("electron_volt",	get("electronvolt")));

	put(new SingleUnit("thm",	get("therm")));
	put(new SingleUnit("cal",	get("calorie")));
	put(new SingleUnit("eV",	get("electronvolt")));
	put(new SingleUnit("bev",	get("gigaelectron_volt")));

	/*
	 * FORCE
	 */
	put(new PluralUnit("dyne",	get("newton").scale(1e-5)));
						// exact
	put(new PluralUnit("pond",	get("newton").scale(9.806650e-3)));
						// exact
	put(new SingleUnit("force_kilogram",	
	    get("newton").scale(9.806650)));		// exact
	put(new SingleUnit("force_ounce",	
	    get("newton").scale(2.780139e-1)));
	put(new SingleUnit("force_pound",	
	    get("newton").scale(4.4482216152605)));	// exact
	put(new PluralUnit("poundal",	get("newton").scale(1.382550e-1)));
	put(new SingleUnit("N",		get("newton")));
	put(new SingleUnit("gf",	get("gram").multiply(get("force"))));

	put(new PluralUnit("force_gram",get("milliforce_kilogram")));
	put(new PluralUnit("force_ton",	get("force_pound").scale(2000)));
						// exact
	put(new SingleUnit("lbf",	get("force_pound")));
	put(new SingleUnit("ounce_force",	get("force_ounce")));
	put(new SingleUnit("kilogram_force",	get("force_kilogram")));
	put(new SingleUnit("pound_force",	get("force_pound")));
	put(new SingleUnit("ozf",	get("force_ounce")));
	put(new SingleUnit("kgf",	get("force_kilogram")));

	put(new PluralUnit("kip",	get("kilolbf")));
	put(new SingleUnit("ton_force",	get("force_ton")));
	put(new SingleUnit("gram_force",get("force_gram")));

	/*
	 * HEAT
	 */
	put(new PluralUnit("clo", get("kelvin").multiply(
	    get("meter").pow(2).divide(get("watt"))).scale(1.55e-1)));

	/*
	 * LIGHT
	 */
	put(new SingleUnit("lm",	get("lumen")));
	put(new SingleUnit("lx",	get("lux")));
	put(new PluralUnit("footcandle",get("lux").scale(1.076391e-1)));
	put(new PluralUnit("footlambert",	get("candela").divide(
		get("meter").pow(2)).scale(3.426259)));
	put(new PluralUnit("lambert", 	get("candela").divide(
	    get("meter").pow(2)).scale(1e4/Math.PI)));	// exact
	put(new PluralUnit("stilb",	get("candela").divide(
		get("meter").pow(2)).scale(1e4)));
	put(new PluralUnit("phot",	get("lumen").divide(
		get("meter").pow(2)).scale(1e4)));		// exact
	put(new PluralUnit("nit",	get("candela").multiply(
	    get("meter").pow(2))));				// exact
	put(new PluralUnit("langley",	get("joule").divide(
	    get("meter").pow(2)).scale(4.184000e4)));	// exact
	put(new PluralUnit("blondel",	get("candela").divide(
		get("meter").pow(2)).scale(1./Math.PI)));

	put(new PluralUnit("apostilb",	get("blondel")));
	put(new SingleUnit("nt",	get("nit")));
	put(new SingleUnit("ph",	get("phot")));
	put(new SingleUnit("sb",	get("stilb")));

	/*
	 * MASS PER UNIT LENGTH
	 */
	put(new PluralUnit("denier",	get("kilogram").divide(
	    get("meter")).scale(1.111111e-7)));
	put(new PluralUnit("tex",	get("kilogram").divide(
	    get("meter")).scale(1e-6)));

	/*
	 * MASS PER UNIT TIME (INCLUDES FLOW)
	 */
	put(new SingleUnit("perm_0C",	get("kilogram").divide(
	    get("pascal").multiply(get("second")).multiply(
		get("meter").pow(2))).scale(5.72135e-11)));
	put(new SingleUnit("perm_23C",	get("kilogram").divide(
	    get("pascal").multiply(get("second")).multiply(
		get("meter").pow(2))).scale(5.74525e-11)));

	/*
	 * POWER
	 */
	put(new PluralUnit("voltampere",	
	    get("volt").multiply(get("ampere"))));
	put(new SingleUnit("VA",	get("voltampere")));
	put(new PluralUnit("boiler_horsepower",
	    get("watt").scale(9.80950e3)));
	put(new PluralUnit("shaft_horsepower",
	    get("watt").scale(7.456999e2)));
	put(new PluralUnit("metric_horsepower",	get("watt").scale(7.35499)));
	put(new PluralUnit("electric_horsepower",
	    get("watt").scale(7.460000e2)));		// exact
	put(new SingleUnit("W",	get("watt")));	
	put(new PluralUnit("water_horsepower",	
	    get("watt").scale(7.46043e2)));
	put(new PluralUnit("UK_horsepower",	
	    get("watt").scale(7.4570e2)));
	put(new PluralUnit("refrigeration_ton",
	    get("Btu").divide(get("hour")).scale(12000)));

	put(new PluralUnit("horsepower",get("shaft_horsepower")));
	put(new PluralUnit("ton_of_refrigeration",
	    get("refrigeration_ton")));

	put(new SingleUnit("hp",	get("horsepower")));

	/*
	 * PRESSURE OR STRESS
	 */
	put(new PluralUnit("bar",	get("pascal").scale(1e5)));
						// exact
	put(new PluralUnit("standard_atmosphere",
	    get("pascal").scale(1.01325e5)));		// exact
	put(new PluralUnit("technical_atmosphere",	
	    get("kilogram").multiply(get("gravity").divide(
		get("meter").scale(.01).pow(2)))));
	put(new SingleUnit("inch_H2O_39F",	
	    get("inch").multiply(get("water_39F"))));
	put(new SingleUnit("inch_H2O_60F",
	    get("inch").multiply(get("water_60F"))));
	put(new SingleUnit("inch_Hg_32F",
	    get("inch").multiply(get("mercury_32F"))));
	put(new SingleUnit("inch_Hg_60F",
	    get("inch").multiply(get("mercury_60F"))));
	put(new SingleUnit("millimeter_Hg_0C",
	    get("millimeter").multiply(get("mercury_0C"))));
	put(new SingleUnit("footH2O",
	    get("foot").multiply(get("water"))));
	put(new SingleUnit("cmHg",	get("centimeter").multiply(get("Hg"))));
	put(new SingleUnit("cmH2O",	
	    get("centimeter").multiply(get("water"))));
	put(new SingleUnit("Pa",	get("pascal")));
	put(new SingleUnit("inch_Hg",	get("inch").multiply(get("Hg"))));
	put(new SingleUnit("inch_hg",	get("inch_Hg")));
	put(new SingleUnit("inHg",	get("inch_Hg")));
	put(new SingleUnit("in_Hg",	get("inch_Hg")));
	put(new SingleUnit("in_hg",	get("inch_Hg")));
	put(new SingleUnit("millimeter_Hg",	
	    get("millimeter").multiply(get("Hg"))));
	put(new SingleUnit("mmHg",	get("millimeter_Hg")));
	put(new SingleUnit("mm_Hg",	get("millimeter_Hg")));
	put(new SingleUnit("mm_hg",	get("millimeter_Hg")));
	put(new PluralUnit("torr",	get("millimeter_Hg")));
	put(new SingleUnit("foot_H2O",	get("foot").multiply(get("water"))));
	put(new SingleUnit("ftH2O",	get("foot_H2O")));
	put(new SingleUnit("psi",	get("pound").multiply(
	    get("gravity").divide(get("inch").pow(2)))));
	put(new SingleUnit("ksi",	get("kip").divide(get("inch").pow(2))));
	put(new PluralUnit("barie",	get("newton").divide(
	    get("meter").pow(2)).scale(0.1)));

	put(new SingleUnit("at",	get("technical_atmosphere")));
	put(new PluralUnit("atmosphere",get("standard_atmosphere")));
	put(new PluralUnit("atm",	get("standard_atmosphere")));
	put(new PluralUnit("barye",	get("barie")));

	/*
	 * RADIATION UNITS
	 */
	put(new SingleUnit("Bq",	get("becquerel")));
	put(new PluralUnit("curie",	get("becquerel").scale(3.7e10)));
						// exact
	put(new PluralUnit("rem",	get("centisievert")));
						// dose equivalent.  exact
	put(new PluralUnit("rad",	get("centigray")));
						// absorbed dose. exact
	put(new PluralUnit("roentgen",	get("coulomb").divide(
	    get("kilogram")).scale(2.58e-4)));
	put(new SingleUnit("Sv",	get("sievert")));
	put(new SingleUnit("Gy",	get("gray")));

	put(new SingleUnit("Ci",	get("curie")));
	put(new SingleUnit("R",		get("roentgen")));
	put(new SingleUnit("rd",	get("rad")));

	/*
	 * VELOCITY (INCLUDES SPEED)
	 */
	put(new SingleUnit("c",	get("meter").divide(
	    get("second")).scale(2.997925e+8)));
	put(new PluralUnit("knot",	
	    get("nautical_mile").divide(get("hour"))));

	put(new SingleUnit("knot_international",	get("knot")));
	put(new SingleUnit("international_knot",	get("knot")));
	put(new PluralUnit("kt",			get("knot")));

	/*
	 * VISCOSITY
	 */
	put(new SingleUnit("poise",
	    get("pascal").multiply(get("second")).scale(1e-1)));
							// exact
	put(new SingleUnit("stokes",	get("meter").pow(2).divide(
	    get("second")).scale(1e-4)));		// exact
	put(new SingleUnit("rhe",
	    get("pascal").multiply(get("second")).pow(-1).scale(10)));

	put(new SingleUnit("St",	get("stokes")));

	/*
	 * VOLUME (INCLUDES CAPACITY)
	 */
	put(new SingleUnit("acre_foot",
	    get("meter").pow(3).scale(1.233489e3)));
		// but `acre foot' is 1233.4867714897 meters^3.  Odd.
	put(new SingleUnit("board_foot",
	    get("meter").pow(3).scale(2.359737e-3)));
		
	put(new PluralUnit("bushel",
	    get("meter").pow(3).scale(3.523907e-2)));
	put(new PluralUnit("UK_liquid_gallon",
	    get("meter").pow(3).scale(4.546090e-3)));	// exact
	put(new PluralUnit("Canadian_liquid_gallon",
	    get("meter").pow(3).scale(4.546090e-3)));	// exact
	put(new PluralUnit("US_dry_gallon",
	    get("meter").pow(3).scale(4.404884e-3)));
	put(new PluralUnit("US_liquid_gallon",
	    get("meter").pow(3).scale(3.785412e-3)));
	put(new SingleUnit("cc",	get("meter").scale(.01).pow(3)));
	put(new PluralUnit("liter",	get("meter").pow(3).scale(1e-3)));
		// exact. However, from 1901 to 1964, 1 liter = 1.000028 dm3
	put(new PluralUnit("stere",	get("meter").pow(3)));	// exact
	put(new PluralUnit("register_ton",	
	    get("meter").pow(3).scale(2.831685)));

	put(new PluralUnit("US_dry_quart",	
	    get("US_dry_gallon").scale(1./4)));
	put(new PluralUnit("US_dry_pint",
	    get("US_dry_gallon").scale(1./8)));

	put(new PluralUnit("US_liquid_quart",
	    get("US_liquid_gallon").scale(1./4)));
	put(new PluralUnit("US_liquid_pint",
	    get("US_liquid_gallon").scale(1./8)));
	put(new PluralUnit("US_liquid_cup",
	    get("US_liquid_gallon").scale(1./16)));
	put(new PluralUnit("US_liquid_gill",
	    get("US_liquid_gallon").scale(1./32)));
	put(new PluralUnit("US_fluid_ounce",
	    get("US_liquid_gallon").scale(1./128)));
	put(new PluralUnit("US_liquid_ounce",
	    get("US_fluid_ounce")));

	put(new PluralUnit("UK_liquid_quart",
	    get("UK_liquid_gallon").scale(1./4)));
	put(new PluralUnit("UK_liquid_pint",
	    get("UK_liquid_gallon").scale(1./8)));
	put(new PluralUnit("UK_liquid_cup",
	    get("UK_liquid_gallon").scale(1./16)));
	put(new PluralUnit("UK_liquid_gill",
	    get("UK_liquid_gallon").scale(1./32)));
	put(new PluralUnit("UK_fluid_ounce",
	    get("UK_liquid_gallon").scale(1./160)));
	put(new PluralUnit("UK_liquid_ounce",
	    get("UK_fluid_ounce")));

	put(new PluralUnit("liquid_gallon",	get("US_liquid_gallon")));
	put(new PluralUnit("fluid_ounce",	get("US_fluid_ounce")));

	put(new PluralUnit("dry_quart",	get("US_dry_quart")));
	put(new PluralUnit("dry_pint",	get("US_dry_pint")));

	put(new PluralUnit("liquid_quart",	
	    get("liquid_gallon").scale(1./4)));
	put(new PluralUnit("liquid_pint",
	    get("liquid_gallon").scale(1./8)));

	put(new PluralUnit("gallon",	get("liquid_gallon")));
	put(new PluralUnit("barrel",	get("US_liquid_gallon").scale(42)));
					    // petroleum industry definition
	put(new PluralUnit("quart",	get("liquid_quart")));
	put(new PluralUnit("pint",	get("liquid_pint")));
	put(new PluralUnit("cup",	get("liquid_gallon").scale(1./16)));
	put(new PluralUnit("gill",	get("liquid_gallon").scale(1./32)));
	put(new PluralUnit("tablespoon",get("US_fluid_ounce").scale(0.5)));
	put(new PluralUnit("teaspoon",	get("tablespoon").scale(1./3)));
	put(new PluralUnit("peck",	get("bushel").scale(1./3)));

	put(new PluralUnit("oz",	get("fluid_ounce")));
	put(new SingleUnit("floz",	get("fluid_ounce")));
	put(new SingleUnit("acre_feet",	get("acre_foot")));
	put(new SingleUnit("board_feet",	get("board_foot")));
	put(new PluralUnit("Tbl",	get("tablespoon")));
	put(new SingleUnit("Tbsp",	get("tablespoon")));
	put(new SingleUnit("tbsp",	get("tablespoon")));
	put(new SingleUnit("Tblsp",	get("tablespoon")));
	put(new SingleUnit("tblsp",	get("tablespoon")));
	put(new PluralUnit("litre",	get("liter")));
	put(new SingleUnit("L",		get("liter")));
	put(new SingleUnit("l",		get("liter")));
	put(new SingleUnit("tsp",	get("teaspoon")));
	put(new SingleUnit("pk",	get("peck")));
	put(new SingleUnit("bu",	get("bushel")));

	put(new SingleUnit("fldr",	get("floz").scale(1./8)));
	put(new PluralUnit("dram",	get("floz").scale(1./16)));

	put(new SingleUnit("bbl",	get("barrel")));
	put(new PluralUnit("firkin", 	get("barrel").scale(1./4)));
					    // exact but "barrel" is vague
	put(new SingleUnit("pt",	get("pint")));
	put(new SingleUnit("dr",	get("dram")));

	/*
	 * VOLUME PER UNIT TIME
	 */
	put(new PluralUnit("sverdrup",	get("meter").pow(3).scale(1e6).
	    divide(get("second"))));	// oceanographic flow


	/*
	 * COMPUTERS AND COMMUNICATION
	 */
	put(new PluralUnit("bit",	new ScaledUnit(1)));
					    // unit of information
	put(new SingleUnit("baud",	get("hertz")));
	put(new SingleUnit("b",		get("bit")));
	put(new SingleUnit("bps",	get("hertz")));
	put(new SingleUnit("cps",	get("hertz")));
	put(new SingleUnit("Bd",	get("baud")));

	/*
	 * MISC
	 */
	put(new PluralUnit("kayser",	get("meter").pow(-1).scale(1e2)));
						// exact
	put(new SingleUnit("rps",	get("hertz")));
	put(new SingleUnit("rpm",	get("hertz").scale(1./60)));
	put(new SingleUnit("geopotential",get("gravity")));
	put(new PluralUnit("work_year",	get("hours").scale(2056)));
	put(new PluralUnit("work_month",get("work_year").scale(1./12)));

	put(new SingleUnit("gp",	get("geopotential")));
	put(new SingleUnit("dynamic",	get("geopotential")));
    };


    /**
     * Get an instance of this class.
     *
     * This is the only way to obtain an instance of this class.
     *
     * @exception	UnitException	Something went wrong
     *			in generating the singleton instance of the database.
     *			This should not occur and indicates an internal
     *			inconsistancy.
     */
    public static UnitsDB
    instance()
	throws UnitException
    {
	if (db == null)
	{
	    synchronized(DefaultUnitsDB.class)
	    {
		if (db == null)
		    db = new DefaultUnitsDB();
	    }
	}

	return db;
    }


    /**
     * Put a named unit.
     *
     * @param unit	The named unit to be added to the database.
     * @return		The original named unit in the database (i.e. the
     *			one with the same name) or null.
     * @require		The named unit shall be non-null.
     * @promise		The named unit has been added to the database,
     *			possibly replacing a previous entry with the same name.
     */
    public NamedUnit
    put(NamedUnit unit)
    {
	return table.put(unit);
    }


    /**
     * Put a unit.
     *
     * This is the interface that a user who wishes to add units to the 
     * database will likely use.
     *
     * @param name	The name of the unit (e.g. "foobar") to be added.
     * @param unit	The unit to be added.
     * @param hasPlural	Whether or not the name of the unit has a plural
     *			form that ends with an `s' (e.g. "foobars").  Some
     *			units don't have a plural form (e.g. "feet").
     * @return		The previous entry with the same name or null.
     * @require		The arguments shall be non-null.
     * @promise		The unit has been added to the database, possibly
     *			replacing a previous entry with the same name.
     */
    public Unit
    put(String name, Unit unit, boolean hasPlural)
    {
	return table.put(name, unit, hasPlural);
    }


    /**
     * Get a unit.
     *
     * @param name	The name of the unit to be retrieved.  It may be
     *			the plural form (e.g. "yards").  If an entry in the
     *			database corresponding to the complete name is not
     *			found and the given name ends with an `s', then a
     *			search will be made for the singular form (e.g. 
     *			"yard").  The matching entry will be returned only if
     *			the entry permits a plural form.  The entry may also
     *			have one or more SI prefixes (e.g. "mega", "M").
     * @return		The appropriate unit or null.  The unit will account
     *			for any SI prefixes in the name.
     * @require		The argument is non-null.
     */
    public Unit
    get(String name)
    {
	Unit	unit = table.get(name);

	if (unit == null)
	{
	    //System.out.println("Entry \"" + name + "\" not found");
	    /*
	     * No entry by that name (including any possible plural form).
	     */

	    /*
	     * Strip prefix.
	     */
	    Prefixer	prefixer = new Prefixer(name);

	    if (prefixer.stripPrefix(prefixes))
	    {
		//System.out.println("Prefix found");
		//System.out.println("Looking for \"" + prefixer.getString() +
		    //"\"");
		/*
		 * Prefix found.  Recurse on the rest of the string.
		 */
		if ((unit = get(prefixer.getString())) != null)
		{
		    try
		    {
			unit = unit.scale(prefixer.getValue());
		    }
		    catch (UnitException e)
		    {
			unit = null;
		    }
		}
	    }
	}

	return unit;
    }


    /**
     * Inner (helper) class for parsing unit prefixes.
     */
    protected class
    Prefixer
    {
	/**
	 * The string being parsed.
	 */
	protected final String	string;

	/**
	 * The current position within the string.
	 */
	protected int		pos;

	/**
	 * The current value of the prefix.
	 */
	protected double	value;


	/**
	 * Construct.
	 */
	protected
	Prefixer(String string)
	{
	    this.string = string;
	    this.pos = 0;
	    this.value = 1;
	}


	/**
	 * Strip leading prefix from the string.
	 */
	protected boolean
	stripPrefix(UnitPrefix[] prefixes)
	{
	    for (int icur = 0; icur < prefixes.length; ++icur)
	    {
		UnitPrefix	prefix = prefixes[icur];

		if (string.startsWith(prefix.name, pos))
		{
		    value *= prefix.value;
		    pos += prefix.name.length();
		    return true;
		}
	    }

	    return false;
	}


	/**
	 * Indicate whether or not the beginning of the remainder of the
	 * string is less than a prefix.
	 */
	protected boolean
	isLessThan(UnitPrefix prefix)
	{
	    int	icomp = 1;
	    int	n = Math.min(prefix.name.length(), string.length()-pos);

	    for (int i = 0; i < n; ++i)
	    {
		icomp = Character.getNumericValue(string.charAt(pos+i)) -
			Character.getNumericValue(prefix.name.charAt(i));

		if (icomp != 0)
		    break;
	    }

	    //System.out.println(string.substring(pos) + 
		//(icomp < 0 ? " < " : " >= ") + prefix.name);

	    return icomp < 0;
	}


	/**
	 * Return the current, remaining string.
	 */
	protected String
	getString()
	{
	    return string.substring(pos);
	}


	/**
	 * Return the current prefix value.
	 */
	protected double
	getValue()
	{
	    return value;
	}
    }


    /**
     * Inner class for enumerating the units in the database.
     */
    public class
    EnumerationImpl
	implements Enumeration
    {
	java.util.Enumeration	enum = table.enumeration();

	public boolean
	hasMoreElements()
	{
	    return enum.hasMoreElements();
	}

	public NamedUnit
	nextElement()
	{
	    return (NamedUnit)enum.nextElement();
	}
    }


    /**
     * Return an enumeration of the units in the database.
     */
    public Enumeration
    getEnumeration()
    {
	return new EnumerationImpl();
    }


    /**
     * Test this class.
     * @exception java.lang.Exception	A problem occurred.
     */
    public static void main(String[] args)
	throws Exception
    {
	UnitsDB	db = DefaultUnitsDB.instance();

	System.out.println("% = " + db.get("%"));
	System.out.println("abampere = " + db.get("abampere"));
	System.out.println("firkin = " + db.get("firkin"));
	System.out.println("micromegafirkin = " + db.get("micromegafirkin"));
	System.out.println("celsius = " + db.get("celsius"));
	System.out.println("fahrenheit = " + db.get("fahrenheit"));
	System.out.println("meter = " + db.get("meter"));
	System.out.println("mm = " + db.get("mm"));
	System.out.println("dam = " + db.get("dam"));
	System.out.println("million = " + db.get("million"));
	System.out.println("pascal = " + db.get("pascal"));
	System.out.println("Tperm_0C = " + db.get("Tperm_0C"));
	System.out.println("millipoundal = " + db.get("millipoundal"));

	//db.list();
    }
}
