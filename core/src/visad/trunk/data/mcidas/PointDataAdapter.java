/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/
package visad.data.mcidas;

import edu.wisc.ssec.mcidas.*;
import edu.wisc.ssec.mcidas.adde.*;
import visad.*;
import visad.data.units.*;
import visad.jmet.MetUnits;

/**
 * A class for adapting the results of an ADDE point data request into a 
 * VisAD Data object.
 *
 * @author  Don Murray, Unidata
 */
public class PointDataAdapter
{

    AddePointDataReader reader;
    FieldImpl field = null;
    long millies;
    long elapsed;

    /**
     * Construct a PointDataAdapter using the adde request passed as a string.
     *
     * @param  addePointRequest  - string representing the ADDE request
     * @throws VisADException  bad request, no data available, VisAD error
     */
    public PointDataAdapter(String addePointRequest)
        throws VisADException
    {
        try
        {
            reader = new AddePointDataReader(addePointRequest);
        }
        catch (AddeException excp)
        {
            throw new VisADException("Problem accessing data");
        }
        makeField();
    }

    // out of this will either come a FieldImpl, a ObservationDBImpl,
    // or a StationObDBImpl
    private void makeField()
        throws VisADException
    {
        // First, let's make a generic FieldImpl from the data
        // the structure will be index -> parameter tuple
        // get all the stuff from the reader
        int[][] data;
        String[] units;
        String[] params;
        int[] scalingFactors;
        try
        {
            data = reader.getData();
            units = reader.getUnits();
            params = reader.getParams();
            scalingFactors = reader.getScales();
        }
        catch (AddeException ae)
        {
            throw new VisADException("Error retrieving data info");
        }
               
        int numObs = data[0].length;
        System.out.println("number of obs = " + numObs);
        if (numObs == 0)
            throw new VisADException("No data available");

        RealType domainType = new RealType("index");
        Integer1DSet domain = new Integer1DSet(domainType, numObs);
        
        // now make range (Tuple) type
        MetUnits unitTranslator = new MetUnits();
        int numParams = params.length;
        ScalarType[] types = new ScalarType[numParams];
        boolean noText = true;
        for (int i = 0; i < numParams; i++)
        {
            // get the name
            String name = params[i];

            // make the unit
            Unit unit = null;
            try
            {
               unit = Parser.parse(unitTranslator.makeSymbol(units[i]));
            }
            catch (NoSuchUnitException ne) {
               System.out.println("Unknown unit: " + units[i] + " for " + name);
            }
            catch (ParseException pe) {;}

            if (units[i].equalsIgnoreCase("CHAR"))
            {
                noText = false;
                types[i] = TextType.getTextType(params[i]);
            }
            else
            {
                types[i] = RealType.getRealType(params[i], unit, (Set) null);
            }
        }
        TupleType rangeType;
        if (noText)  // all reals
        {
            RealType[] newTypes = new RealType[types.length];
            for (int i = 0; i < types.length; i++)
                newTypes[i] = (RealType) types[i];
            rangeType = new RealTupleType(newTypes);
        }
        else
        {
            rangeType = new TupleType(types);
        }
        FunctionType functionType = 
            new FunctionType(domainType, rangeType);
        field = new FieldImpl(functionType, domain); 

        // now, fill in the data
        for (int i = 0; i < numObs; i++)
        {
            Scalar[] scalars = new Scalar[numParams];
            for (int j = 0; j < numParams; j++)
            {
                try
                {
                    if (types[j] instanceof TextType)
                    {
                        scalars[j] = 
                            new Text( (TextType) types[j], 
                                     McIDASUtil.intBitsToString(data[j][i]));
                    }
                    else
                    {
                        double value =
                            data[j][i] == McIDASUtil.MCMISSING
                              ? Double.NaN
                              : data[j][i]/Math.pow(10.0, 
                                  (double) scalingFactors[j] );
                        scalars[j] =
                            new Real((RealType) types[j], value);
                    }
                }
                catch (Exception e) {;}
            }
            try
            {
                field.setSample(i, new Tuple(scalars));
            }
            catch (java.rmi.RemoteException e) {;}
        }
    }

    /**
     * Get the VisAD Data object that represents the output from the
     * request.
     *
     * @return  requested data
     */
    public Data getData()
    {
        return field;
    }

    /**  
     * test with 'java visad.data.mcidas.PointDataAdapter args' 
     * @param args ADDE point data request
     */
    public static void main(String[] args)
        throws Exception
    {
        if (args.length == 0) 
        {
            System.out.println("You must specify an ADDE Point Data URL");
            System.exit(-1);
        }
        try
        {
            PointDataAdapter pda = new PointDataAdapter(args[0]);
            Field data = (Field) pda.getData();
            System.out.println(data.getType());
            /*
            int length = data.getDomainSet().getLength() - 1;
            System.out.println(
                "Sample "+ length + " = " + data.getSample(length));
            */
        }
        catch (VisADException ve)
        {
            System.out.println("Error reading data");
        }
    }
}
