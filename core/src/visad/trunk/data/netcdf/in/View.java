/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: View.java,v 1.9 2006-02-13 22:30:08 curtis Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import ucar.netcdf.Attribute;
import ucar.netcdf.Dimension;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import visad.CoordinateSystem;
import visad.data.in.ArithProg;
import visad.data.netcdf.Quantity;
import visad.data.netcdf.QuantityDB;
import visad.data.units.Parser;
import visad.DoubleSet;
import visad.ErrorEstimate;
import visad.FloatSet;
import visad.Gridded1DDoubleSet;
import visad.Gridded1DSet;
import visad.Integer1DSet;
import visad.Linear1DSet;
import visad.RealType;
import visad.ScalarType;
import visad.SI;
import visad.SimpleSet;
import visad.TextType;
import visad.TypeException;
import visad.Unit;
import visad.VisADException;

/**
 * A convention-dependent view of a netCDF dataset.
 *
 * @author Steven R. Emmerson
 * @version $Revision: 1.9 $ $Date: 2006-02-13 22:30:08 $
 */
public abstract class View
{
    /**
     * The netCDF dataset that is being viewed through these conventions.
     */
    private final Netcdf     netcdf;

    /**
     * The quantity database to use to map netCDF variables to VisAD
     * Quantity-s.
     */
    private final QuantityDB quantityDB;

    /**
     * Flag for whether this View handles char variables as Text
     */
    private final boolean charToText;

    /*
     * Performance caches:
     */
    private final Map        varToRealType;
    private final Map        varToTextType;
    private final Map        varToUnit;
    private final Map        dimToSet;
    private final Map        dimToRealType;

    /*
     * Well-known quantities for comparison purposes:
     */
    private final Quantity   longitude;
    private final Quantity   latitude;

    /**
     * Something for creating unique names.
     */
    private static int       nameCount;

    /** Names of outer dimensions to be factored out
    *
    */

    private java.util.Set factorNameSet = null;

    /**
     * Constructs from a netCDF dataset.
     *
     * @param netcdf                The netCDF dataset.
     * @param quantityDB            The quantity database to use to map netCDF
     *                              variables to VisAD Quantity-s.
     * @throws NullPointerException if either argument is <code>null</code>.
     */
    protected View(Netcdf netcdf, QuantityDB quantityDB)
    {
        this(netcdf, quantityDB, false);
    }

    /**
     * Constructs from a netCDF dataset.
     *
     * @param netcdf                The netCDF dataset.
     * @param quantityDB            The quantity database to use to map netCDF
     *                              variables to VisAD Quantity-s.
     * @param charToText            Specifies whether the View should map char
     *                              variables to VisAD Text objects
     * @throws NullPointerException if either argument is <code>null</code>.
     */
    protected View(Netcdf netcdf, QuantityDB quantityDB, boolean charToText)
    {
        this.netcdf = netcdf;
        this.quantityDB = quantityDB;
        this.charToText = charToText;
        varToUnit = new WeakHashMap();
        dimToSet = new WeakHashMap();
        varToRealType = new WeakHashMap();
        varToTextType = new WeakHashMap();
        dimToRealType = new WeakHashMap();
        longitude = quantityDB.get("longitude");
        latitude = quantityDB.get("latitude");
    }

    /**
     * Returns a view of a netCDF dataset.  The exact view returned depends
     * on the netCDF dataset.
     *
     * @param netcdf            The netCDF dataset.
     * @param quantityDB        A quantity database to be used to map netCDF
     *                          variables to VisAD {@link Quantity}s.
     * @return                  A view of the dataset.
     */
    public static View getInstance(Netcdf netcdf, QuantityDB db)
    {
        return getInstance(netcdf, db, false);
    }

    /**
     * Returns a view of a netCDF dataset.  The exact view returned depends
     * on the netCDF dataset.
     *
     * @param netcdf            The netCDF dataset.
     * @param quantityDB        A quantity database to be used to map netCDF
     *                          variables to VisAD {@link Quantity}s.
     * @param charToText        Specifies whether the View should map char
     *                          variables to VisAD Text objects
     * @return                  A view of the dataset.
     */
    public static View getInstance(
        Netcdf netcdf, QuantityDB db, boolean charToText)
    {
        View   view;
        String conventions = getConventionsString(netcdf);
        if (conventions == null)
        {
            view = new DefaultView(netcdf, db, charToText);
        }
        else
        {
            try
            {
                if (conventions.equals("CF-1.0"))
                    view = new CfView(netcdf, db, charToText);
                else if (conventions.equals("COARDS"))
                    view = new CfView(netcdf, db);
                else if (conventions.equals("COARDS/CF-1.0"))
                    view = new CfView(netcdf, db, charToText);
                else
                {
                    System.err.println(
                        "Unknown netCDF conventions attribute (" +
                        conventions + ").  Using default view...");
                    view = new DefaultView(netcdf, db, charToText);
                }
            }
            catch (IllegalArgumentException e)
            {
                System.err.println(
                    "netCDF dataset doesn't follow stated conventions (" +
                    conventions + "): " + e.getMessage() + 
                    "\nUsing default view...");
                view = new DefaultView(netcdf, db, charToText);
            }
        }
        return view;
    }

    /**
     * Does this View handle text.
     *
     * @return true if text is handled
     */
    public boolean isCharToText() {
       return charToText;
    }

    /**
     * Returns the underlying netCDF dataset.
     *
     * @return                  The netCDF dataset.
     */
    public Netcdf getNetcdf()
    {
        return netcdf;
    }

    /**
     * <p>Returns the value of the global "Conventions" attribute.  If the
     * attribute doesn't exist or is invalid, then <code>null</code> is
     * returned.  If the attribute exists but is not string-valued, then an
     * error message is printed to {@link System#err} and <code>null</code> is
     * returned.</p>
     *
     * @param netcdf                The netCDF dataset.
     * @return                      The value of the attribute.
     */
    protected static String getConventionsString(Netcdf netcdf)
    {
        Attribute attr = netcdf.getAttribute("Conventions");
        try
        {
            return attr != null ? attr.getStringValue() : null;
        }
        catch (ClassCastException e)
        {
            System.err.println("The \"Conventions\" attribute (" + attr + 
                ") isn't a string");
            return null;
        }
    }

    /**
     * Returns the named netCDF variable.  Returns <code>null</code> if the
     * variable doesn't exist.
     *
     * @param name                  The name of the netCDF variable.
     * @throws NullPointerException if the name is <code>null</code>.
     * @return                      The named netCDF variable or 
     *                              <code>null</code>.
     */
    protected Variable getVariable(String name)
    {
        return netcdf.get(name);
    }

    /**
     * Indicates if the netCDF variable with a given name is numeric.
     *
     * @param name                   The name of the netCDF variable.
     * @return                       <code>true</code> if and only if the
     *                               variable exists has numeric values.
     */
    protected boolean isNumeric(String name)
    {
        Variable var = netcdf.get(name);
        if (var == null)
            return false;
        return isNumeric(var);
    }

    /**
     * Indicates if the given netCDF variable is numeric.
     *
     * @param name                   The netCDF variable.
     * @return                       <code>true</code> if and only if the
     *                               variable is numeric.
     * @throws NullPointerException  if the argument is <code>null</code>.
     */
    protected boolean isNumeric(Variable var)
    {
        return !var.getComponentType().equals(char.class);
    }

    /**
     * Indicates if a netCDF dimension represents longitude.  This method uses
     * {@link #getRealType(Variable)} and {@link #isLongitude(RealType)}.
     *
     * @param dim               A netCDF dimension.
     * @return                  <code>true</code> if an only if <code>dim</code>
     *                          represents longitude.
     * @throws VisADException   Couldn't create necessary VisAD object.
     */
    protected boolean isLongitude(Variable var)
        throws VisADException
    {
        RealType type = getRealType(var);
        return type != null && type.equals(longitude);
    }

    /**
     * Indicates if a VisAD {@link RealType} represents longitude.
     *
     * @param type              A VisAD {@link RealType}.  May be 
     *                          <code>null</code>.
     * @return                  <code>true</code> if an only if the VisAD
     *                          {@link RealType} represents longitude.
     */
    protected boolean isLongitude(RealType type)
    {
        return type != null && type.equals(longitude);
    }

    /**
     * Indicates if a VisAD {@link RealType} represents latitude.
     *
     * @param type              A VisAD {@link RealType}.  May be 
     *                          <code>null</code>.
     * @return                  <code>true</code> if an only if the VisAD
     *                          {@link RealType} represents latitude.
     */
    protected boolean isLatitude(RealType type)
    {
        return type != null && type.equals(latitude);
    }

    /**
     * <p>Returns the VisAD {@link MathType} of the domain corresponding to a
     * netCDF dimension.</p>
     *
     * <p>This implementation supports coordinate variables and uses {@link
     * #getCoordinateVariable(Dimension)} and {@link #getRealType(Variable)}.
     * </p>
     *
     * @param dim               A netCDF dimension.
     * @return                  The VisAD MathType of the domain corresponding
     *                          to <code>dim</code>. Won't be <code>null</code>.
     * @throws TypeException    if a corresponding {@link RealType} needed
     *                          to be created but couldn't.
     */
    protected RealType getRealType(Dimension dim)
        throws TypeException
    {
        RealType type;
        Variable var = getCoordinateVariable(dim);
        if (var != null)
        {
            type = getRealType(var);
        }
        else
        {
            synchronized(dimToRealType)
            {
                type = (RealType)dimToRealType.get(dim);
                if (type == null)
                {
                    String name = dim.getName();
                    type = quantityDB.get(name);
                    if (type == null) {
                        type = RealType.getRealType(name);
                    }
                    if (type == null) {
                        throw new TypeException(
                            "Couldn't create RealType for " + dim.getName());
                    }
                    dimToRealType.put(dim, type);
                }
            }
        }
        return type;
    }

    /**
     * <p>Returns the VisAD {@link RealType} of a netCDF variable.</p>
     *
     * <p>This implementation returns the value of {@link
     * #getRealTypeFromLongName(Variable)} if that is non-<code>null</code>;
     * otherwise, the value of {@link #getRealTypeFromName(Variable)} is
     * returned.</p>
     *
     * @param var                   The netCDF variable.
     * @return                      The corresponding VisAD RealType.
     * @throws NullPointerException if the argument is <code>null</code>.
     * @throws TypeException        if a corresponding {@link RealType} needed 
     *                              to be created but couldn't.
     */
    protected RealType getRealType(Variable var)
        throws TypeException
    {
        if (var == null)
            throw new NullPointerException();
        RealType type;
        /*
         * To improve performance, this method caches results.
         */
        synchronized(varToRealType)
        {
            type = (RealType)varToRealType.get(var);
            if (type == null)
            {
                type = getRealTypeFromLongName(var);
                if (type == null)
                    type = getRealTypeFromName(var);
                varToRealType.put(var, type);  // cache result
            }
        }
        return type;
    }

    /**
     * Returns the VisAD RealType corresponding to the <code>long_name</code>
     * attribute of a netCDF variable.  If the unit attribute of the variable
     * is incompatible with the unit of the <code>long_name</code> attribute,
     * then a new RealType is created whose default unit is that of the
     * attribute.</p>
     *
     * <p>This implementation first checks if the variable has a
     * <code>long_name</code> attribute via {@link #getLongName(Varaible)},
     * if it doesn't, then <code>null</code> is returned; otherwise, the
     * long name is used to query the quantity database.  If the quantity
     * database doesn't contain a match, then <code>null</code> is returned;
     * otherwise, the variable's unit attribute -- obtained via {@link
     * #getUnitFromAttribute(Variable)} -- is checked.  If the unit attribute
     * doesn't exist, then the {@link RealType} is returned; otherwise, the unit
     * attribute is compared against the default unit of the {@link RealType}.
     * If the two are convertible, then the {@link RealType} is returned;
     * othwerwise, an attempt is made to create a new {@link RealType} with a
     * slightly different name than the variable's but with the same unit as the
     * variable's and that {@link RealType} is returned.</p>
     *
     * @param var                    The netCDF variable.
     * @return                       The corresponding VisAD RealType or
     *                               <code>null</code> if no corresponding type
     *                               was found or could be created.
     */
    protected RealType getRealTypeFromLongName(Variable var)
    {
        RealType type;
        String   name = getLongName(var);
        if (name == null)
        {
            type = null;
        }
        else
        {
            type = quantityDB.get(name);
            if (type != null)
            {
                Unit unit = getUnitFromAttribute(var);
                if (!Unit.canConvert(unit, type.getDefaultUnit()))
                {
                    String  newName = newName(var);
                    System.err.println(
                        "The unit attribute (" + unit + ") " +
                        "of variable \"" + var.getName() + "\" " + 
                        "is incompatible with the unit (" +
                        type.getDefaultUnit() + ") " +
                        "of the quantity referenced by the long_name attribute "
                        + "(" + name + ").  " +
                        "Attempting to create new quantity \"" + newName +
                        "\".");
                    type = RealType.getRealType(newName, unit);
                }
            }
        }
        return type;
    }

    /**
     * <p>Returns the VisAD {@link RealType} corresponding to the name of a
     * netCDF variable. <code>null</code> is never returned.</p>
     *
     * <p>This implementation first obtains the variable's unit via {@link
     * #getUnitFromAttribute(Variable)}.  It then queries the quantity database
     * for a match to the variable's name.  If a match is found, then variable's
     * unit is checked.  If the unit is <code>null</code>, then the {@link
     * RealType} from the database is returned; otherwise, the unit is checked
     * against the default unit of the obtained {@link RealType}.  If the two
     * units are convertible, then the {@link RealType} is returned; otherwise,
     * a new {@link RealType} is created that has a slightly different name than
     * the variable's but with the variable's unit and that {@link RealType}
     * is returned.  If the quantity database doesn't contain a match, then
     * the variable's unit is checked.  If it's <code>null</code>, then
     * the value of {@link RealType#getRealType(String)} -- when given the
     * variable's name -- is returned; otherwise, the return value of {@link
     * RealType#getRealType(String, Unit) -- when invoked with the variable's
     * name and unit -- is checked.  If it's non-<code>null</code>, then that
     * {@link RealType} is returned; otherwise, a new {@link RealType} is
     * created that has a slightly different name than the variable's but with
     * the variable's unit and that {@link RealType} is returned.</p>
     *
     * @param var                    The netCDF variable.
     * @return                       The corresponding VisAD RealType.
     * @throws TypeException         if a corresponding {@link RealType} needed
     *                               to be created but couldn't.
     */
    protected RealType getRealTypeFromName(Variable var)
        throws TypeException
    {
        String   name = var.getName();
        Unit     unit = getUnitFromAttribute(var);
        RealType type = quantityDB.get(name);
        if (type != null)
        {
            if (!Unit.canConvert(unit, type.getDefaultUnit()))
                type = newQuantity(var, unit, type.getDefaultUnit());
        }
        else
        {
            if (unit == null)
            {
                type = RealType.getRealType(name);
            }
            else
            {
                type = RealType.getRealType(name, unit);
                if (type == null)
                {
                    type =
                        newQuantity(
                            var,
                            unit,
                            RealType.getRealTypeByName(name).getDefaultUnit());
                }
            }
        }
        return type;
    }

    private RealType newQuantity(Variable var, Unit wantUnit, Unit haveUnit)
        throws TypeException
    {
        String  newName = newName(var);
        System.err.println(
            "The unit attribute (" + wantUnit + ") " +
            "of variable \"" + var.getName() + "\" " + 
            "is incompatible with the unit (" +
            haveUnit + ") of the RealType of the same name.  " +
            "Attempting to create new RealType \"" + newName + "\".");
        RealType type = RealType.getRealType(newName, wantUnit);
        if (type == null)
            throw new TypeException(newName);
        return type;
    }

    /**
     * Returns a name for a given variable that is slightly different that the
     * variable's name and is guarenteed not to have been returned before.
     *
     * @param var                    The netCDF variable.
     * @return                       A new and unique name based on the
     *                               variable.
     */
    protected String newName(Variable var)
    {
        return var.getName() + "_" + nameCount++;
            // getUnitFromAttribute(var).toString().replace(' ', '_')
            // .replace('.', '_');
    }

    /**
     * <p>Gets the type of the values of a netCDF variable.</p>
     *
     * <p>This implementation returns the value of #getRealType(Variable)}
     * or {@link #getTextType(Variable)} -- depending on the value of {@link
     * #isNumeric(Variable)}.</p>
     *
     * @param var               A netCDF variable.
     * @throws TypeException    if a corresponding {@link RealType} needed
     *                          to be created but couldn't.
     * @throws VisADException   if a VisAD object can't be created.
     */
    protected ScalarType getScalarType(Variable var)
        throws TypeException, VisADException
    {
        return
            isNumeric(var)
                ? (ScalarType)getRealType(var)
                : (ScalarType)getTextType(var);
    }

    /**
     * Return the VisAD TextType of a netCDF variable.
     *
     * @param var               The netCDF variable.
     * @return                  The VisAD TextType of <code>var</code>.
     * @throws VisADException   if a VisAD object couldn't be created.
     * @throws IllegalArgumentException
     *                          if the netCDF variable is not textual.
     */
    protected TextType getTextType(Variable var)
        throws VisADException
    {
        if (var == null)
            throw new NullPointerException();
        if (isNumeric(var))
            throw new IllegalArgumentException(var.toString());
        TextType type;
        /*
         * To improve performance, this method caches results.
         */
        synchronized(varToTextType)
        {
            type = (TextType)varToTextType.get(var);
            if (type == null)
            {
                type = TextType.getTextType(var.getName());
                varToTextType.put(var, type);  // cache result
            }
        }
        return type;
    }

    /**
     * Gets the representational set for the values of a netCDF variable.  If 
     * the variable isn't numeric, then <code>null</code> is returned.
     *
     * <p>This implementation uses {@link #getRealType(Variable)}, {@link 
     * #getVetter(Variable)}, and {@link #getUnitFromAttribute(Variable)}.</p>
     *
     * @param var               A netCDF variable.
     * @return                  The VisAD representational set for the values of
     *                          the variable or <code>null</code>.
     * @throws TypeException    if a corresponding {@link RealType} needed
     *                          to be created but couldn't.
     * @throws VisADException   Couldn't create necessary VisAD object.
     */
    protected SimpleSet getRangeSet(Variable var)
        throws TypeException, VisADException
    {
        SimpleSet       set;
        Class           cl = var.getComponentType();
        if (cl.equals(char.class))
        {
            set = null;
        }
        else
        {
            RealType    type = getRealType(var);
            if (cl.equals(byte.class))
            {
                set = new Linear1DSet(type,
                                      Byte.MIN_VALUE+1, Byte.MAX_VALUE,
                                      Byte.MAX_VALUE - Byte.MIN_VALUE);
            }
            else if (cl.equals(short.class))
            {
                set = new Linear1DSet(type,
                                      Short.MIN_VALUE+1, Short.MAX_VALUE,
                                      Short.MAX_VALUE - Short.MIN_VALUE);
            }
            else if (cl.equals(int.class))
            {
                /*
                 * The following is complicated due to the fact that the last
                 * argument to the Linear1DSet() constructor:
                 *
                 *     Linear1DSet(MathType type, double start, double stop,
                 *                      int length)
                 *
                 * is an "int" -- and the number of Java "int" values cannot
                 * be represented by a Java "int".
                 */
                Vetter  vetter = getVetter(var);
                long    minValid = (long)vetter.minValid();
                long    maxValid = (long)vetter.maxValid();
                long    length  = maxValid - minValid + 1;
                set = length <= Integer.MAX_VALUE
                            ? (SimpleSet)(new Linear1DSet(type, minValid,
                                            maxValid, (int)length))
                            : (SimpleSet)(new FloatSet(type,
                                            (CoordinateSystem)null,
                                            new Unit[] {
                                                getUnitFromAttribute(var)}));
            }
            else if (cl.equals(float.class))
            {
                set = new FloatSet(type, (CoordinateSystem)null,
                                    new Unit[] {getUnitFromAttribute(var)});
            }
            else
            {
                set = (SimpleSet)new DoubleSet(type, (CoordinateSystem)null,
                                    new Unit[] {getUnitFromAttribute(var)});
            }
        }
        return set;
    }

    /**
     * <p>Returns a string-valued global attribute.  If the attribute doesn't
     * exist or is invalid, then <code>null</code> is returned.  If the
     * attribute exists but is not string-valued, then an error message is
     * printed to {@link System#err} and <code>null</code> is returned.</p>
     *
     * <p>This implementation uses {@link getAttributeString(Variable, String)}.
     * </p>
     *
     * @param var               A netCDF variable or <code>null</code> to 
     *                          indicate a global attribute.
     * @param name              The name of the attribute.
     * @return                  The string value of the attribute or
     *                          <code>null</code>.
     */
    protected String getAttributeString(String name)
    {
        return getAttributeString((Variable)null, name);
    }

    /**
     * Returns a string-valued global attribute or a netCDF variable attribute.
     * If the attribute doesn't exist or is invalid, then <code>null</code> is
     * returned.  If the attribute exists but is not string-valued, then an
     * error message is printed to {@link System#err} and <code>null</code> is
     * returned.
     *
     * @param var               A netCDF variable or <code>null</code> to 
     *                          indicate a global attribute.
     * @param name              The name of the attribute.
     * @return                  The string value of the attribute or
     *                          <code>null</code>.
     */
    protected String getAttributeString(Variable var, String name)
    {
        Attribute attr =
            var == null ? netcdf.getAttribute(name) : var.getAttribute(name);
        try
        {
            return attr != null ? attr.getStringValue() : null;
        }
        catch (ClassCastException e)
        {
            System.err.println(
                "Non-string attribute: " + var.getName() + ":" + name);
            return null;
        }
    }

    /**
     * <p>Returns the long name of a netCDF variable according to the variable's
     * <code>long_name</code> attribute.  If the attribute doesn't exist, then
     * <code>null</code> is returned.</p>
     *
     * <p>This method uses {@link #getAttributeString(Variable, String)}.</p>
     *
     * @param var               A netCDF variable.
     * @return                  The long name of <code>var</code> or 
     *                          <code>null</code>.
     * @throws ClassCastException
     *                          if the attribute exists but its value isn't a
     *                          String.
     */
    protected String getLongName(Variable var)
    {
        return getAttributeString(var, "long_name");
    }

    /**
     * <p>Returns the string value of the unit attribute of a netCDF variable.
     * Returns <code>null</code> if the unit attribute is missing or
     * invalid.</p>
     *
     * <p>This method uses {@link #getAttributeString(Variable, String)} --
     * first with the name "units" and then with the name "unit".</p>
     *
     * @param var               A netCDF variable.
     * @return                  The unit of the values of <code>var</code> or
     *                          <code>null</code>.
     */
    protected String getUnitString(Variable var)
    {
        String str = getAttributeString(var, "units");
        if (str == null)
            str = getAttributeString(var, "unit");
        return str;
    }

    /**
     * <p>Returns the unit of a netCDF variable according to the variable's unit
     * attribute.  Returns <code>null</code> if the unit attribute is missing
     * or invalid.  If a unit specification exists but can't be decoded, then
     * a warning message is printed to {@link System#err}.</p>
     *
     * <p>This method uses {@link getUnitString(Variable)}.</p>
     *
     * @param var               A netCDF variable.
     * @return                  The unit of the values of <code>var</code> or
     *                          <code>null</code>.
     */
    protected Unit getUnitFromAttribute(Variable var)
    {
        Unit unit;
        /*
         * This method caches results to improve performance.
         */
        synchronized(varToUnit)
        {
            /*
             * The following two lines exist because the unit map is a
             * WeakHashMap and may contain null values.
             */
            unit = (Unit)varToUnit.get(var);
            if (!varToUnit.containsKey(var))
            {
                String spec = getUnitString(var);
                if (spec != null)
                {
                    try
                    {
                        unit = Parser.parse(spec);
                    }
                    catch (Exception e)
                    {
                        System.err.println(
                            "Couldn't decode unit attribute (" + spec + ")" +
                            " of variable \"" + var.getName() + "\": " + 
                            e.getMessage());
                    }
                }
                varToUnit.put(var, unit);  // cache result
            }
        }
        return unit;
    }

    /**
     * Returns a value-vetter for a netCDF variable.
     *
     * @param var               A netCDF variable.
     * @return                  A value-vetter for the variable.
     */
    protected Vetter getVetter(Variable var)
    {
        return new Vetter(var);
    }

    /**
     * <p>Returns the VisAD {@link Gridded1DSet} corresponding to a netCDF
     * dimension.</p>
     *
     * <p>This implementation supports coordinate variables, longitude,
     * and the discovery of an arithmetic progression.  It uses {@link
     * #isLongitude(Variable)}, {@link #getRealType(Dimension)}, and {@link
     * #getUnitFromAttribute(Variable)}. </p>
     *
     * @param dim               A netCDF dimension.
     * @return                  The VisAD {@link GriddedSet} corresponding to
     *                          the dimension.
     * @throws VisADException   if a VisAD object couldn't be created.
     * @throws IOException      if a netCDF read-error occurs.
     * @throws ClassCastException
     *                          if the dimension has a coordinate variable of
     *                          improper type.
     */
    protected Gridded1DSet getDomainSet(Dimension dim)
        throws VisADException, IOException
    {
        /*
         * This implementation caches earlier results because this operation is
         * potentially expensive and may be invoked many times for any given
         * dimension.
         */
        Gridded1DSet    set = (Gridded1DSet)dimToSet.get(dim);
        if (set == null)
        {
            Variable    coordVar = getCoordinateVariable(dim);
            if (coordVar == null)
            {
                // TODO: add CoordinateSystem argument
                set = new Integer1DSet(getRealType(dim), dim.getLength());
            }
            else
            {
                ArithProg       ap = isLongitude(coordVar)
                                        ? new visad.data.in.LonArithProg()
                                        : new visad.data.in.ArithProg();
                Class           varType = coordVar.getComponentType();
                boolean         isDouble = varType.equals(double.class);
                Object          coordValues;
                if (isDouble)
                {
                    coordValues = coordVar.toArray();
                    ap.accumulate((double[])coordValues);
                }
                else if (varType.equals(float.class))
                {
                    coordValues = coordVar.toArray();
                    ap.accumulate((float[])coordValues);
                }
                else
                {
                    int     length = 1;
                    {
                        int[] lengths = coordVar.getLengths();
                        for (int i = 0; i < lengths.length; i++)
                            length *= lengths[i];
                    }
                    float[] floatVals = new float[length];
                    if (varType.equals(int.class))
                    {
                        int[] values = (int[])coordVar.toArray();
                        for (int i = 0; i < values.length; i++)
                            floatVals[i] = values[i];
                    }
                    else if (varType.equals(short.class))
                    {
                        short[] values = (short[])coordVar.toArray();
                        for (int i = 0; i < values.length; i++)
                            floatVals[i] = values[i];
                    }
                    else
                    {
                        byte[] values = (byte[])coordVar.toArray();
                        for (int i = 0; i < values.length; i++)
                            floatVals[i] = values[i];
                    }
                    ap.accumulate(floatVals);
                    coordValues = floatVals;
                }
                if (ap.isConsistent())
                {
                    /*
                     * The coordinate-variable is an arithmetic progression.
                     */
                    // TODO: add CoordinateSystem argument
                    set = new Linear1DSet(
                            getRealType(dim),
                            ap.getFirst(),
                            ap.getLast(),
                            (int)ap.getNumber(),
                            (CoordinateSystem)null,
                            new Unit[] {getUnitFromAttribute(coordVar)},
                            (ErrorEstimate[])null);
                }
                else
                {
                    /*
                     * The coordinate-variable is not an arithmetic progression.
                     */
                    // TODO: add CoordinateSystem argument
                    set =
                        isDouble
                            ? (Gridded1DSet)new Gridded1DDoubleSet(
                                getRealType(dim),
                                new double[][] {(double[])coordValues},
                                dim.getLength(),
                                (CoordinateSystem)null,
                                new Unit[] {getUnitFromAttribute(coordVar)},
                                (ErrorEstimate[])null)
                            : new Gridded1DSet(
                                getRealType(dim),
                                new float[][] {(float[])coordValues},
                                dim.getLength(),
                                (CoordinateSystem)null,
                                new Unit[] {getUnitFromAttribute(coordVar)},
                                (ErrorEstimate[])null);
                }
            }
            dimToSet.put(dim, set);
        }
        return set;
    }

    /**
     * <p>Returns the netCDF coordinate variable associated with a netCDF 
     * dimension.  If no such variable exists, then <code>null</code> is
     * returned.</p>
     *
     * <p>This implementation uses {@link #isNumeric(Variable)}.</p>
     *
     * @param dim               A netCDF dimension.
     * @return                  The netCDF coordinate variable associated
     *                          with the dimension or <code>null</code>
     *                          if there is no coordinate variable.
     */
    protected Variable getCoordinateVariable(Dimension dim)
    {
        Variable        var = netcdf.get(dim.getName());
        if (var != null && !(var.getRank() == 1 && isNumeric(var)))
            var = null;
        return var;
    }

    /** <p> Defines the names of domain components to factor out. 
    * This only works if this names correspond to the outermost
    * dimension.  The list of names may be changed after calling
    * this.</p>
    *
    * <p>The Set should contain only String(s).</p>
    *
    * <p>Typically, a TreeSet will be used. For example:</p>
    * <code>TreeSet ts = new TreeSet();</code>
    * <code>ts.add("myParameter");</code>
    * <code>view.setOuterDimensionNameSet(ts);</code>
    *
    * @param fn    A Set containing the names (as Strings) of
    * the dimensions to factor out.
    *
    */
    public void setOuterDimensionNameSet(java.util.Set nameSet) {
      factorNameSet = nameSet;
    }

    /** 
    * <p> Returns the factorName object
    *
    * @return     The Set of factorNames.
    */

    public java.util.Set getOuterDimensionNameSet() { 
      return factorNameSet;
    }

    /**
     * <p>Indicates if a netCDF dimension represents time.</p>
     *
     * <p>This implementation supports coordinate variables and uses {@link
     * #getRealType(Dimension)}.</p>
     *
     * <p>If setOuterDimensionNameSet() has been called, this list
     * of names will also logically be considered factorable.</p>
     *
     * @param dim               A netCDF dimension.
     * @return                  <code>true</code> if and only if the dimension
     *                          represents time.
     * @throws VisADException   Couldn't create necessary VisAD object.
     * @throws IOException      I/O failure.
     */
    protected boolean isTime(Dimension dim)
        throws VisADException, IOException
    {
        RealType rt = getRealType(dim);
        if (factorNameSet != null) {
          if (factorNameSet.contains(rt.getName()) ) {
             return true;
          }
        }
        
        return (isTime(rt.getDefaultUnit()) );
    }

    /**
     * Indicates if a unit is a unit of time.
     *
     * @param unit              A unit.
     * @return                  <code>true</code> if and only if the unit
     *                          is a unit of time.
     */
    protected boolean isTime(Unit unit)
    {
        return unit != null && SI.second.isConvertible(unit.getAbsoluteUnit());
    }

    /**
     * Returns the netCDF dimensions of a netCDF variable.
     *
     * @param var               A netCDF variable.
     * @return                  The dimensions of <code>var</code> in
     *                          netCDF order.
     */
    protected Dimension[] getDimensions(Variable var)
    {
        int                     rank = var.getRank();
        Dimension[]             dims = new Dimension[rank];
        DimensionIterator       iter = var.getDimensionIterator();
        for (int i = 0; i < rank; ++i)
            dims[i] = iter.next();
        return dims;
    }

    /**
     * <p>Indicates if a netCDF variable is a coordinate variable (i.e. has only
     * one netCDF dimension and that dimension has the same name).</p>
     *
     * <p>This implementation uses {@link #isNumeric(Variable)}.</p>
     *
     * @param var               A netCDF variable.
     * @return                  <code>true</code> if and only if <code>
     *                          var</code> is a coordinate variable.
     */
    protected boolean isCoordinateVariable(Variable var)
    {
        if (var.getRank() != 1 || !isNumeric(var))
            return false;
        return getDimensions(var)[0].getName().equals(var.getName());
    }

    /**
     * Returns an iterator over the virtual VisAD data objects of this view.
     *
     * @return                  An iterator over the virtual VisAD data objects
     *                          of the view.
     */
    public VirtualDataIterator getVirtualDataIterator()
    {
        return new DataIterator();
    }

    /**
     * <p>Indicates if a given variable should be ignored by the {@link
     * VirtualDataIterator} during iteration over the virtual VisAD data objects
     * in the netCDF dataset.</p>
     *
     * @return                    <code>true</code> if and only if the variable
     *                            should be ignored.
     */
    protected abstract boolean isIgnorable(Variable var);

    /**
     * Returns the domain of a netCDF variable.
     *
     * @param var               A netCDF variable.
     * @return                  The domain of the netCDF variable.
     * @throws IllegalArgumentException
     *                          if the rank of the variable is zero.
     * @throws TypeException    if a {@link RealType} needed to be created but
     *                          couldn't.
     * @throws IOException      if a netCDF read-error occurs.
     */
    protected abstract Domain getDomain(Variable var)
        throws TypeException, IOException;

    /**
     * <p>Returns the virtual VisAD data object corresponding to a named netCDF
     * variable.</p>
     *
     * <p>This implementation uses {@link #getData(Variable)}.</p>
     *
     * @param name                  The name of the netCDF variable.
     * @return                      The corresponding virtual VisAD data object.
     * @throws NullPointerException if the argument is <code>null</code>.
     * @throws IllegalArgumentException
     *                              if the netCDF variable doesn't exist.
     * @throws TypeException        if a {@link RealType} needed to be created 
     *                              but couldn't.
     * @throws VisADException       if a VisAD object couldn't be created.
     * @throws IOException          if a netCDF read-error occurs.
     */
    public VirtualData getData(String name)
        throws TypeException, VisADException, IOException
    {
        if (name == null)
            throw new NullPointerException();
        return getData(netcdf.get(name));
    }

    /**
     * <p>Returns the virtual VisAD data object corresponding to a netCDF
     * variable.</p>
     *
     * <p>This implementation uses {@link #getRealType(Variable)}, {@link
     * #getRangeSet(Variable)}, {@link #getUnitFromAttribute(Variable)}, {@link
     * #getVetter(Variable)}, and {@link #getDomain(Variable)}.<p>
     *
     * @param var                   The netCDF variable.
     * @return                      The corresponding virtual VisAD data object.
     * @throws NullPointerException if the argument is <code>null</code>.
     * @throws TypeException        if a {@link RealType} needed to be created 
     *                              but couldn't.
     * @throws VisADException       if a VisAD object couldn't be created.
     * @throws IOException          if a netCDF read-error occurs.
     */
    protected VirtualData getData(Variable var)
        throws TypeException, VisADException, IOException
    {
        VirtualScalar   scalar =
            (isNumeric(var) == true)

                ? (VirtualScalar) 
                    new VirtualReal(getRealType(var),
                                    var,
                                    getRangeSet(var),
                                    getUnitFromAttribute(var),
                                    getVetter(var))

                : (VirtualScalar)
                    new VirtualText(getTextType(var), var);

        return
            (var.getRank() == 0 || (!isNumeric(var) && var.getRank() == 1))
                ? (VirtualData)scalar
                : getDomain(var).getVirtualField(
                    new VirtualTuple(scalar));
    }

    /**
     * Iterates over the virtual VisAD data objects of this view.
     */
    protected class DataIterator
        extends VirtualDataIterator
    {
        /**
         * The netCDF variable iterator.
         */
        private final VariableIterator  varIter;

        /**
         * Constructs from nothing.
         */
        DataIterator()
        {
            super(View.this);
            varIter = View.this.getNetcdf().iterator();
        }

        /**
         * <p>Returns a copy of the next virtual VisAD data object.</p>
         *
         * <p>This implementation uses {@link #isCharToText()},
         * {@link #isNumeric(Variable)}, {@link #isIgnorable(Variable)}, 
         * and {@link #getData(Variable)}.</p>
         *
         * @return                      A copy of the next virtual VisAD data
         *                              object or <code> null</code> if there is
         *                              no more data.
         * @throws TypeException        if a {@link ScalarType} needed
         *                              to be created but couldn't.
         * @throws VisADException       Couldn't create necessary VisAD object.
         * @throws IOException          if a netCDF read-error occurs.
         */
        protected VirtualData getData()
            throws TypeException, VisADException, IOException
        {
            while (varIter.hasNext())
            {
                Variable        var = varIter.next();
                // handle text only if charToText == true and rank <= 2
                if (!isNumeric(var) && (!isCharToText() || var.getRank() > 2))
                    continue;  // TODO: support arrays of text (Tuple?)
                if (isIgnorable(var))
                    continue;  // ignore what's ignorable
                return View.this.getData(var);
            }
            return null;        // no more data
        }
    }

    /**
     * The convention-dependent domain of a netCDF variable.
     */
    protected abstract class Domain
    {
        /**
         * Constructs from a netCDF variable.
         *
         * @throws NullPointerException     if the argument is
         *                                  <code>null</code>.
         * @throws IllegalArgumentException if the rank of the variable is 0.
         */
        protected Domain(Variable var)
        {
            if (var.getRank() == 0)
                throw new IllegalArgumentException(var.toString());
        }

        /**
         * Returns a {@link VirtualField} corresponding to this domain and
         * a given range.
         *
         * @param range                 The range for the {@link VirtualField}.
         * @throws NullPointerException if the argument is <code>null</code>.
         * @throws IOException          if a netCDF read-error occurs.
         * @throws VisADException       if a VisAD object can't be created.
         */
        protected abstract VirtualField getVirtualField(VirtualTuple range)
            throws VisADException, IOException;

        /**
         * Indicates if this instance equals an object.
         *
         * @param obj                The object to be compared against.
         * @return                   <code>true</code> if and only if this
         *                           instance equals the object.
         */
        public abstract boolean equals(Object obj);

        /**
         * Returns the hash code of this instance.
         *
         * @return                   The hash code of this instance.
         */
        public abstract int hashCode();
    }
}
