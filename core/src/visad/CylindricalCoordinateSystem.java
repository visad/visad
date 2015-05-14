//
// CylindricalCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

/**
 * CylindricalCoordinateSystem is the VisAD CoordinateSystem class
 * for (CylRadius, CylAzimuth, CylZAxis) with a Cartesian Reference,
 * (XAxis, YAxis, ZAxis) and with CylAzimuth in degrees.<P>
 */
public class CylindricalCoordinateSystem
    extends CoordinateSystem
{

    private static Unit[] coordinate_system_units =
        {null, CommonUnit.degree, null};

    /** construct a CoordinateSystem for (radius, azimuth, zaxis) relative
     *  to a 3-D Cartesian reference; this constructor supplies units =
     *  {null, CommonUnit.Degree, null} to the super
     *  constructor, in order to ensure Unit compatibility with its
     *  use of trigonometric functions
     *
     * @param    reference    Cartesian reference in the order of x, y, z
     * @throws   VisADException  necessary VisAD object could not be created
     */
    public CylindricalCoordinateSystem(RealTupleType reference)
        throws VisADException
    {
        super(reference, coordinate_system_units);
    }

    /** construct a CoordinateSystem for (radius, azimuth, zaxis) relative
     *  to a 3-D Cartesian reference; this constructor supplies units =
     *  {null, CommonUnit.Degree, null} to the super
     *  constructor, in order to ensure Unit compatibility with its
     *  use of trigonometric functions
     *
     * @param    reference    Cartesian reference in the order of x, y, z
     * @param    b            boolean argument indicating this is the
     *                        trusted constructor for initializers (does
     *                        not declare Exceptions)
     */
    CylindricalCoordinateSystem(RealTupleType reference, boolean b)
    {
        super(reference, coordinate_system_units, b);
    }

    /**
     * Convert cylindrical coordinates (radius, azimuth, z) to
     * Cartesian coordinates (x, y, z).  Input array must have
     * a length of 3 and be in the correct order.
     *
     * @param  tuples  double array containing the radius, azimuth and z values.
     *
     * @return  double array in Cartesian coordinates ordered as x, y, z
     *
     * @throws  CoordinateSystemException  if input array is null or wrong
     *                                     dimension.
     */
    public double[][] toReference(double[][] tuples)
        throws CoordinateSystemException
    {
        if (tuples == null || tuples.length != 3)
        {
            throw new CoordinateSystemException(
                "CylindricalCoordinateSystem.toReference: " +
                    "tuples wrong dimension");
        }
        int len = tuples[0].length;
        double[][] value = new double[3][len];
        for (int i = 0; i < len; i++)
        {
            if (tuples[0][i] < 0.0)   // radius < 0
            {
                value[0][i] = Double.NaN;
                value[1][i] = Double.NaN;
                value[2][i] = Double.NaN;
            }
            else
            {
                double cosaz =
                    Math.cos(Data.DEGREES_TO_RADIANS * tuples[1][i]);
                double sinaz =
                    Math.sin(Data.DEGREES_TO_RADIANS * tuples[1][i]);
                value[0][i] = tuples[0][i] * cosaz;
                value[1][i] = tuples[0][i] * sinaz;
                value[2][i] = tuples[2][i];
            }
        }
        return value;
    }

    /**
     * Convert Cartesian coordinates (x, y, z) to
     * cylindrical coordinates (radius, azimuth, z).  Input array must have
     * a length of 3 and be in the correct order.
     *
     * @param  tuples   double array in Cartesian coordinates ordered as x, y, z
     *
     * @return  double array containing the radius, azimuth and z values.
     *
     * @throws  CoordinateSystemException  if input array is null or wrong
     *                                     dimension.
     */
    public double[][] fromReference(double[][] tuples)
        throws CoordinateSystemException
    {
        if (tuples == null || tuples.length != 3)
        {
            throw new CoordinateSystemException(
                "CylindricalCoordinateSystem.fromReference: " +
                    "tuples wrong dimension");
        }
        int len = tuples[0].length;
        double[][] value = new double[3][len];
        for (int i = 0; i < len; i++)
        {
            value[0][i] = Math.sqrt(tuples[0][i] * tuples[0][i] +
                                    tuples[1][i] * tuples[1][i]);
            value[1][i] =
                Data.RADIANS_TO_DEGREES *
                    Math.atan2(tuples[1][i], tuples[0][i]);
            if (value[1][i] < 0.0) value[1][i] += 360.0;
            value[2][i] = tuples[2][i];
        }
        return value;
    }

    /**
     * Convert cylindrical coordinates (radius, azimuth, z) to
     * Cartesian coordinates (x, y, z).  Input array must have
     * a length of 3 and be in the correct order.
     *
     * @param  tuples  float array containing the radius, azimuth and z values.
     *
     * @return  float array in Cartesian coordinates ordered as x, y, z
     *
     * @throws  CoordinateSystemException  if input array is null or wrong
     *                                     dimension.
     */
    public float[][] toReference(float[][] tuples)
        throws CoordinateSystemException
    {
        if (tuples == null || tuples.length != 3)
        {
            throw new CoordinateSystemException(
                "CylindricalCoordinateSystem.toReference: " +
                    "tuples wrong dimension");
        }
        int len = tuples[0].length;
        float[][] value = new float[3][len];
        for (int i=0; i<len ;i++)
        {
            if (tuples[0][i] < 0.0)   // radius < 0
            {
                value[0][i] = Float.NaN;
                value[1][i] = Float.NaN;
                value[2][i] = Float.NaN;
            }
            else
            {
                float cosaz =
                    (float) Math.cos(Data.DEGREES_TO_RADIANS * tuples[1][i]);
                float sinaz =
                    (float) Math.sin(Data.DEGREES_TO_RADIANS * tuples[1][i]);
                value[0][i] = tuples[0][i] * cosaz;
                value[1][i] = tuples[0][i] * sinaz;
                value[2][i] = tuples[2][i];
            }
        }
        return value;
    }

    /**
     * Convert Cartesian coordinates (x, y, z) to
     * cylindrical coordinates (radius, azimuth, z).  Input array must have
     * a length of 3 and be in the correct order.
     *
     * @param  tuples   float array in Cartesian coordinates ordered as x, y, z
     *
     * @return  float array containing the radius, azimuth and z values.
     *
     * @throws  CoordinateSystemException  if input array is null or wrong
     *                                     dimension.
     */
    public float[][] fromReference(float[][] tuples)
        throws CoordinateSystemException
    {
        if (tuples == null || tuples.length != 3)
        {
            throw new CoordinateSystemException(
                "CylindricalCoordinateSystem.fromReference: " +
                    "tuples wrong dimension");
        }
        int len = tuples[0].length;
        float[][] value = new float[3][len];
        for (int i=0; i<len ;i++)
        {
            value[0][i] = (float) Math.sqrt(tuples[0][i] * tuples[0][i] +
                                            tuples[1][i] * tuples[1][i]);
            value[1][i] = (float)
                (Data.RADIANS_TO_DEGREES *
                    Math.atan2(tuples[1][i], tuples[0][i]));
            if (value[1][i] < 0.0f) value[1][i] += 360.0f;
            value[2][i] = tuples[2][i];
        }
        return value;
    }

    /** determine if the CoordinateSystem in question is a Cylindrical one
     *
     * @param cs the CoordinateSystem in question
     *
     */
    public boolean equals(Object cs)
    {
        return (cs instanceof CylindricalCoordinateSystem);
    }
}
