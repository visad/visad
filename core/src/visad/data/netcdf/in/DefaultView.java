/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DefaultView.java,v 1.9 2004-11-19 23:31:08 donm Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import ucar.netcdf.Dimension;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import visad.data.netcdf.QuantityDB;
import visad.CoordinateSystem;
import visad.ErrorEstimate;
import visad.Gridded1DSet;
import visad.GriddedSet;
import visad.Integer1DSet;
import visad.IntegerNDSet;
import visad.Linear1DSet;
import visad.LinearLatLonSet;
import visad.LinearNDSet;
import visad.LinearSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.SampledSet;
import visad.SetType;
import visad.TextType;
import visad.TypeException;
import visad.Unit;
import visad.VisADException;

/**
 * Provides support for the default view of a netCDF dataset as documented
 * in the netCDF User's Guide.
 *
 * @author Steven R. Emmerson
 * @version $Revision: 1.9 $ $Date: 2004-11-19 23:31:08 $
 */
public class DefaultView
    extends     View
{
    private final DimsToSet dimsToSet;

    /**
     * Constructs from a netCDF dataset and a quantity database.
     *
     * @param netcdf            The netCDF dataset.
     * @param quantityDB        The quantity database to use to map netCDF
     *                          variables to VisAD Quantity-s.
     */
    public DefaultView(Netcdf netcdf, QuantityDB quantityDB)
    {
        this(netcdf, quantityDB, false);
    }

    /**
     * Constructs from a netCDF dataset and a quantity database.
     *
     * @param netcdf            The netCDF dataset.
     * @param quantityDB        The quantity database to use to map netCDF
     *                          variables to VisAD Quantity-s.
     * @param charToText        Specifies whether the View should map char
     *                          variables to VisAD Text objects
     */
    public DefaultView(Netcdf netcdf, QuantityDB quantityDB, boolean charToText)
    {
        super(netcdf, quantityDB, charToText);
        dimsToSet = new DimsToSet();
    }

    /**
     * <p>Indicates if a given variable should be ignored during iteration.</p>
     *
     * <p>This implementation returns the value of {@link 
     * #isCoordinateVariable(Variable)}.</p>
     *
     * @return                    <code>true</code> if and only if the variable
     *                            should be ignored.
     */
    protected boolean isIgnorable(Variable var)
    {
        return  isCoordinateVariable(var);
    }

    /**
     * Returns the domain of a netCDF variable.
     *
     * @param var               A netCDF variable.
     * @return                  The domain of the given variable.
     * @throws IllegalArgumentException
     *                          if the rank of the variable is zero.
     * @throws TypeException    if a {@link RealType} needed to be created but
     *                          couldn't.
     * @throws IOException      if a netCDF read-error occurs.
     */
    protected Domain getDomain(Variable var)
        throws TypeException, IOException
    {
        return new DefaultDomain(var);
    }

    /**
     * <p>Returns the VisAD domain set corresponding to an array of netCDF 
     * dimension in netCDF order (outermost dimension first).</p>
     *
     * <p>This implementation supports 1-dimensional coordinate variables,
     * 1-dimensional longitude, and 2-dimensional latitude/longitude domains
     * and assumes that each netCDF dimension is independent of all others.
     * This implementation uses {@link #getDomainSet(Dimension)}, {@link
     * #getDomainSet(Dimension)}, and {@link #getDomainType(Dimension[])}. </p>
     *
     * @param dims              A netCDF domain.  Must be in netCDF order
     *                          (outer dimension first).
     * @return                  The VisAD domain set corresponding to
     *                          <code>dims</code>.
     * @throws VisADException   Couldn't create necessary VisAD object.
     * @exception IOException   if a netCDF read-error occurs.
     */
    protected SampledSet getDomainSet(Dimension[] dims)
        throws IOException, VisADException
    {
        if (dims.length == 0)
            return getDomainSet(dims[0]);
        /*
         * This implementation caches earlier results because this operation
         * is potentially expensive and multiple netCDF variables can have the
         * same domain.
         */
        GriddedSet      set;
        synchronized(dimsToSet)
        {
            set = dimsToSet.get(dims);
            if (set == null)
            {
                Gridded1DSet[] sets = new Gridded1DSet[dims.length];
                int            j = dims.length;
                for (int i = 0; i < dims.length; ++i)
                    sets[--j] = getDomainSet(dims[i]);      // reverse order
                boolean     allInteger1DSets = true;
                for (int i = 0; allInteger1DSets && i < sets.length; ++i)
                    allInteger1DSets = sets[i] instanceof Integer1DSet;
                MathType    type = getDomainType(dims);
                if (allInteger1DSets)
                {
                    set = (GriddedSet)getIntegerSet(sets, type);
                }
                else
                {
                    boolean allLinear1DSets = true;
                    for (int i = 0; allLinear1DSets && i < sets.length; ++i)
                        allLinear1DSets = sets[i] instanceof Linear1DSet;
                    if (allLinear1DSets)
                    {
                        set = (GriddedSet)getLinearSet(sets, type);
                    }
                    else
                    {
                        set = getGriddedSet(sets, type);
                    }
                }
                dimsToSet.put(dims, set);
            }
        }
        return set;
    }

    /**
     * Returns an {@link IntegerSet} corresponding to the product of one or more
     * {@link Integer1DSet}s.
     *
     * @param sets              The {@link Integer1DSet}s to be multiplied
     *                          together.
     * @param type              The {@link MathType} for the result.
     * @return                  The {@link IntegerSet} corresponding to the
     *                          input.
     * @throws VisADException   if a VisAD object couldn't be created.
     */
    private static GriddedSet getIntegerSet(Gridded1DSet[] sets, MathType type)
        throws VisADException
    {
        int     rank = sets.length;
        int[]   lengths = new int[rank];
        for (int idim = 0; idim < rank; ++idim)
            lengths[idim] = ((Integer1DSet)sets[idim]).getLength(0);
        // TODO: add CoordinateSystem argument
        return IntegerNDSet.create(type, lengths, (CoordinateSystem)null,
                (Unit[])null, (ErrorEstimate[])null);
    }

    /**
     * <p>Returns a {@link LinearSet} corresponding to the product of one or 
     * more {@link Linear1DSet}s.</p>
     *
     * <p>This implementation uses {@link #isLongitude(RealType)} and {@link
     * #isLatitude(RealType)}.
     *
     * @param sets              The {@link Linear1DSet}s to be multiplied
     *                          together.
     * @param type              The {@link MathType} for the result.
     *                          NB: The units in the sets needn't be the
     *                          same as the units in <code>type</code>.
     * @return                  The {@link LinearSet} corresponding to the
     *                          input.
     * @throws VisADException   if a VisAD object couldn't be created.
     */
    private LinearSet getLinearSet(Gridded1DSet[] sets, MathType type)
        throws VisADException
    {
        LinearSet       set = null;
        int             rank = sets.length;
        double[]        firsts = new double[rank];
        double[]        lasts = new double[rank];
        int[]           lengths = new int[rank];
        Unit[]          units = new Unit[rank];
        for (int idim = 0; idim < rank; ++idim)
        {
            Linear1DSet linear1DSet = (Linear1DSet)sets[idim];
            firsts[idim] = linear1DSet.getFirst();
            lengths[idim] = linear1DSet.getLength(0);
            lasts[idim] = linear1DSet.getLast();
            units[idim] = linear1DSet.getSetUnits()[0];
        }
        // TODO: add CoordinateSystem argument
        if (rank == 2)
        {
            RealType[]  types = ((RealTupleType)type).getRealComponents();
            if ((isLongitude(types[0]) && isLatitude(types[1])) ||
                (isLongitude(types[1]) && isLatitude(types[0])))
            {
                set = new LinearLatLonSet(type,
                                        firsts[0], lasts[0], lengths[0],
                                        firsts[1], lasts[1], lengths[1],
                                        (CoordinateSystem)null,
                                        units,
                                        (ErrorEstimate[])null);
            }
        }
        if (set == null)
        {
            set = LinearNDSet.create(type,
                                      firsts, lasts, lengths,
                                      (CoordinateSystem)null,
                                      units,
                                      (ErrorEstimate[])null);
        }
        return set;
    }

    /**
     * Returns a {@link GriddedSet} corresponding to one or more {@link
     * Gridded1DSet}s.
     *
     * @param sets              The {@link Gridded1DSet}s to be multiplied
     *                          together.
     * @param type              The {@link MathType} for the result.
     *                          NB: The units in the sets needn't be the
     *                          same as the units in <code>type</code>.
     * @return                  The {@link GriddedSet} corresponding to the
     *                          input.
     * @throws VisADException   if a VisAD object couldn't be created.
     * @throws IOException      if a netCDF read-error occurs.
     */
    private static GriddedSet getGriddedSet(Gridded1DSet[] sets, MathType type)
        throws VisADException, IOException
    {
        int             rank = sets.length;
        // Handle the case where we only have one set.  Save us some work
        // and keep the integrity of a Gridded1DDoubleSet for Time
        if (rank == 1 && sets[0].getType().equals(new SetType(type))) {
          return sets[0];
        }
        int[]           lengths = new int[rank];
        float[][]       values = new float[rank][];
        int             ntotal = 1;
        for (int idim = 0; idim < rank; ++idim)
        {
            lengths[idim] = sets[idim].getLength(0);
            ntotal *= lengths[idim];
        }
        int step = 1;
        int laststep = 1;
        for (int idim = 0; idim < rank; ++idim)
        {
            float[]     vals = sets[idim].getSamples(false)[0];
            values[idim] = new float[ntotal];
            step *= lengths[idim];
            for (int i=0; i<lengths[idim]; i++) {
              int istep = i * laststep;
              for (int j=0; j<ntotal; j+=step) {
                for (int k=0; k<laststep; k++) {
                  values[idim][istep+j+k] = vals[i];
                }
              }
            }
            laststep = step;
        }
        Unit[]  units = new Unit[rank];
        for (int idim = 0; idim < rank; ++idim)
            units[idim] = sets[idim].getSetUnits()[0];
        // TODO: add CoordinateSystem argument
        return GriddedSet.create(type, values, lengths,
                 (CoordinateSystem)null, units, (ErrorEstimate[])null);
    }

    /**
     * <p>Returns the VisAD {@link MathType} corresponding to an array of netCDF
     * dimensions.  If the array has zero length, then <code>null</code> is
     * returned.</p>
     *
     * <p>This implementation uses {@link #getRealType(Dimension)}.</p>
     *
     * @param dims              netCDF dimensions in netCDF order (outermost
     *                          dimension first).
     * @return                  The type of the domain corresponding to
     *                          <code>dims</code>.  RETURN_VALUE is
     *                          <code>null</code>, a <code>RealType</code>,
     *                          or a <code>RealTupleType</code> if
     *                          <code>dims.length</code> is 0, 1, or greater
     *                          than 1, respectively.
     * @throws VisADException   Couldn't create necessary VisAD object.
     */
    protected MathType getDomainType(Dimension[] dims)
        throws VisADException
    {
        MathType        type;
        int             rank = dims.length;
        if (rank == 0)
        {
            type = null;        // means scalar domain
        }
        else if ( rank == 1)
        {
            type = (MathType)getRealType(dims[0]);
        }
        else
        {
            RealType[]  types = new RealType[dims.length];
            int j = dims.length;
            for (int i = 0; i < dims.length; ++i)
                types[--j] = getRealType(dims[i]);      // reverse order
            type = new RealTupleType(types);
        }
        return type;
    }

    /**
     * Iterates over the virtual VisAD data objects in a netCDF dataset.
     */
    final class DefaultDataIterator
        extends VirtualDataIterator
    {
        /**
         * The netCDF variable iterator.
         */
        private final VariableIterator  varIter;

        /**
         * Constructs from nothing.
         */
        DefaultDataIterator()
        {
            super(DefaultView.this);
            varIter = DefaultView.this.getNetcdf().iterator();
        }

        /**
         * Returns a clone of the next virtual VisAD data object.
         *
         * <p>This implementation uses {@link #isCharToText()},
         * {@link #isNumeric(Variable)}, {@link #isIgnorable(Variable)}, 
         * and {@link #getData(Variable)}.</p>
         * 
         * @return                      A clone of the next virtual VisAD data
         *                              object or <code> null</code> if there is
         *                              no more data.
         * @throws TypeException        if a {@link ScalarType} needed
         *                              to be created but couldn't.
         * @throws VisADException       Couldn't create necessary VisAD object.
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
                    continue;  // ignore ignorable variables
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
                    (var.getRank() == 0 || 
                     (!isNumeric(var) && var.getRank() == 1))
                        ? (VirtualData)scalar
                        : getDomain(var).getVirtualField(
                            new VirtualTuple(scalar));
            }
            return null;        // no more data
        }
    }

    /**
     * The default domain of a netCDF variable.  A default domain comprises
     * the variable's netCDF dimensions.
     */
    private final class DefaultDomain
        extends Domain
    {
        /**
         * Outermost dimension first; at least one element.
         */
        private final Dimension[]   dims;
        private volatile int        hashCode;
        private volatile SampledSet domainSet;

        /**
         * @throws IllegalArgumentException if the rank of the variable is 0.
         */
        DefaultDomain(Variable var)
            throws TypeException
        {
            super(var);
            dims = getDimensions(var);
        }

        /**
         * Returns a {@link VirtualField} corresponding to this domain and
         * a given range.
         *
         * @param range                 The range for the {@link VirtualField}.
         * @throws NullPointerException if the argument is <code>null</code>.
         * @throws IOException          if a read error occurs.
         * @throws VisADException       if a VisAD object can't be created.
         */
        protected VirtualField getVirtualField(VirtualTuple range)
            throws VisADException, IOException
        {
            VirtualField field;
            int          rank = dims.length;
            if (rank == 2 && range.getType() instanceof TextType) { // char
               field = 
                   VirtualField.newVirtualField(
                       DefaultView.this.getDomainSet(dims[0]), range);
            }
            else if (rank == 1 || !DefaultView.this.isTime(dims[0]))
            {
                field =
                    VirtualField.newVirtualField(
                        DefaultView.this.getDomainSet(dims), range);
            }
            else
            {
                Dimension[] innerDims = new Dimension[rank-1];
                System.arraycopy(dims, 1, innerDims, 0, innerDims.length);
                field =
                    VirtualField.newVirtualField(
                        DefaultView.this.getDomainSet(dims[0]),
                        new VirtualTuple(
                            VirtualField.newVirtualField(
                                DefaultView.this.getDomainSet(innerDims),
                                range)));
            }
            return field;
        }

        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;
            if (!(obj instanceof DefaultDomain))
                return false;
            return Arrays.equals(dims, ((DefaultDomain)obj).dims);
        }

        public int hashCode()
        {
            int hash = hashCode;
            if (hash == 0)
            {
                hash = 1;
                for (int i = 0; i < dims.length; i++)
                    hash = hash*31 + dims[i].hashCode();
                hashCode = hash;
            }
            return hash;
        }
    }

    /**
     * Cache of netCDF dimensions and their corresponding VisAD domain sets.
     */
    private static class DimsToSet
    {
        private Map map;

        DimsToSet()
        {
            map = Collections.synchronizedMap(new WeakHashMap());
        }

        void put(Dimension[] dims, GriddedSet set)
        {
            if (dims.length == 1)
                map.put(dims[0], set);
            else
                map.put(new DimArray(dims), set);
        }

        GriddedSet get(Dimension[] dims)
        {
            return
                dims.length == 1
                    ? (GriddedSet)map.get(dims[0])
                    : (GriddedSet)map.get(new DimArray(dims));
        }

        private static class DimArray
        {
            private Dimension[]  dims;
            private volatile int hashCode;

            DimArray(Dimension[] dims)
            {
                this.dims = (Dimension[])dims.clone();  // defensive copy
            }

            public boolean equals(Object obj)
            {
                if (obj == this)
                    return true;
                if (!(obj instanceof DimArray))
                    return false;
                return Arrays.equals(dims, ((DimArray)obj).dims);
            }

            public int hashCode()
            {
                int hash = hashCode;
                if (hash == 0)
                {
                    hash = 1;
                    for (int i = 0; i < dims.length; i++)
                        hash = hash*31 + dims[i].hashCode();
                    hashCode = hash;
                }
                return hash;
            }
        }
    }
}
