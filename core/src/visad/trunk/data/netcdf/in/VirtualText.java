/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualText.java,v 1.2 2006-02-13 22:30:08 curtis Exp $
 */

package visad.data.netcdf.in;


import java.io.IOException;
import ucar.netcdf.Variable;
import ucar.multiarray.StringCharAdapter;
import visad.*;


/**
 * Provides support for a virtual VisAD Scalar.
 */
public class
VirtualText
    extends     VirtualScalar
{

    StringCharAdapter stringVar = null;

    /**
     * Constructs from a scalar type and a 2-D char netCDF variable
     *
     * @param type              The type of the nested scalar.
     * @param var               The 1-D netCDF variable.
     */
    public
    VirtualText(ScalarType type, Variable var)
    {
        super(type, var);
        stringVar = new StringCharAdapter(var, ' ');
    }

    /**
     * Gets the Scalar object corresponding to this virtual, data
     * object.
     *
     * @return                  The VisAD Scalar corresponding to this
     *                          virtual, data object.
     * @throws InvalidContextException
     *                          if the indicial context is invalid.
     * @throws VisADException   Couldn't create necessary VisAD object.
     * @throws RemoteException  if a Java RMI failure occurs.
     * @throws IOException      I/O failure.
     */
    protected Scalar getScalar(Context context) 
        throws VisADException, InvalidContextException, IOException
    {
        String[]        values = getStrings(context);

        if (values.length != 1) {
            System.out.println(getScalarType());
            throw new InvalidContextException(context);
        }

        return (Scalar) new Text( (TextType)getScalarType(), values[0].trim());
        
    }

    /**
     * Gets the String value corresponding to this virtual, data
     * object at a given context.
     *
     * @return                  The String value of this virtual, data object.
     * throws VisADException    Couldn't create necessary VisAD object.
     * throws IOException       I/O failure.
     */
    private String[]
    getStrings(Context context)
        throws IOException, VisADException
    {
        int[]   lengths = stringVar.getLengths();
        int     rank = lengths.length;
        int[]   ioOrigin = new int[rank];
        int[]   ioShape = new int[rank];
        int[]   ioContext = context.getContext();

        System.arraycopy(ioContext, 0, ioOrigin, 0, ioContext.length);

        for (int i = 0; i < ioContext.length; ++i)
            ioShape[i] = 1;

        int     total = 1;

        for (int i = ioContext.length; i < rank; ++i)
        {
            ioOrigin[i] = 0;
            ioShape[i] = lengths[i];
            total *= lengths[i];
        }

        String[] values = new String[total];

        return (String[]) stringVar.toArray(values, ioOrigin, ioShape);
    }

    /**
     * Clones this instance.
     *
     * @return                  A (deep) clone of this instance.
     */
    public Object clone()
    {
        return new VirtualText((TextType) getScalarType(), getVariable());
    }

}
