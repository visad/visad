package visad.data.netcdf;


import java.util.Enumeration;
import java.util.Vector;
import ucar.netcdf.Attribute;
import ucar.netcdf.AttributeIterator;
import ucar.netcdf.AttributeSet;
import ucar.netcdf.Dimension;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.DimensionSet;
import ucar.netcdf.Named;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import ucar.netcdf.VariableSet;
import visad.Data;
import visad.FlatField;
import visad.Tuple;
import visad.UnimplementedException;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.DataNode;


/**
 * Class for adapting a VisAD data object to the Netcdf interface.
 */
public class
VisADAdapter
    implements	Netcdf
{
    /**
     * The "netCDF" attributes.
     */
    protected final AttrSet	attrSet;

    /**
     * The "netCDF" dimensions.
     */
    protected final DimSet	dimSet;

    /**
     * The "netCDF" variables.
     */
    protected final VarSet	varSet;


    /**
     * Construct from a VisAD data object.
     */
    VisADAdapter(Data data)
	throws BadFormException, VisADException
    {
	/*
	 * Define the (global) netCDF attributes.
	 */
	attrSet = new AttrSet();

	DataNode	node = DataNode.create(data);

	/*
	 * Define the netCDF dimensions.
	 */
	dimSet = ((DimDefiner)node.accept(new DimDefiner())).getSet();

	/*
	 * Define the netCDF variables.
	 */
	varSet = ((VarDefiner)node.accept(new VarDefiner(dimSet))).getSet();
    }


    /**
     * Indicate whether or not the given object is in the netCDF object.
     */
    public boolean
    contains(Object obj)
    {
	return attrSet.contains(obj) || dimSet.contains(obj) ||
	       varSet.contains(obj);
    }


    /**
     * Indicate whether or not the named variable is in the netCDF object.
     */
    public boolean
    contains(String name)
    {
	return varSet.contains(name);
    }


    /**
     * Return the variable identified by name.
     */
    public Variable
    get(String name)
    {
	return varSet.get(name);
    }


    /**
     * Return the global attribute identified by name.
     */
    public Attribute
    getAttribute(String name)
    {
	return attrSet.get(name);
    }


    /**
     * Return the set of global attributes associated with this netCDF object.
     */
    public AttributeSet
    getAttributes()
    {
	return attrSet;
    }


    /**
     * Return the "netCDF" dimensions of the VisAD data object.
     */
    public DimensionSet
    getDimensions()
    {
	return dimSet;
    }


    /**
     * Return a VariableIterator for the variables.
     */
    public VariableIterator
    iterator()
    {
	return varSet.iterator();
    }


    /**
     * Return the number of variables.
     */
    public int
    size()
    {
	return varSet.size();
    }


    /**
     * Return an array of the variables.
     *
     * @deprecated
     */
    public Variable[]
    toArray()
    {
	return varSet.toArray();
    }
}
