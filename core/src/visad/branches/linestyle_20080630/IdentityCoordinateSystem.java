//
// IdentityCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
 * A CoordinateSystem that will
 * return the input values when <CODE>toReference()</CODE> and
 * <CODE>fromReference()</CODE> are called.  Useful in constructing
 * <CODE>CartesianProductCoordinateSystem</CODE>s.
 * @see visad.CartesianProductCoordinateSystem
 */
public class IdentityCoordinateSystem extends CoordinateSystem
{

    /**
     * Construct a new <CODE>IdentityCoordinateSystem</CODE> for
     * values of the type specified.
     * @param  type  type of the values
     */
    public IdentityCoordinateSystem(RealTupleType type)
        throws VisADException
    {
        this(type, type.getDefaultUnits());
    }

    /**
     * Construct a new <CODE>IdentityCoordinateSystem</CODE> for
     * values of the type specified.
     * @param  type  type of the values
     */
    public IdentityCoordinateSystem(RealTupleType type, Unit[] units)
        throws VisADException
    {
        super(type, units);
    }

    /**
     * Simple implementation of abstract method.  Returns the input values.
     * @param values  input values
     * @return values
     * @throws VisADException  values are null or wrong dimension
     */
    public double[][] fromReference(double[][] values)
        throws VisADException
    {
        if (values == null || values.length != getDimension())
            throw new VisADException("values are null or wrong dimension");
        return values;
    }
        
    /**
     * Simple implementation of abstract method.  Returns the input values.
     * @param values  input values
     * @return values
     * @throws VisADException  values are null or wrong dimension
     */
    public double[][] toReference(double[][] values)
        throws VisADException
    {
        if (values == null || values.length != getDimension())
            throw new VisADException("values are null or wrong dimension");
        return values;
    }

    /**
     * Simple implementation of abstract method.  Returns the input values.
     * @param values  input values
     * @return values
     * @throws VisADException  values are null or wrong dimension
     */
    public float[][] fromReference(float[][] values)
        throws VisADException
    {
        if (values == null || values.length != getDimension())
            throw new VisADException("values are null or wrong dimension");
        return values;
    }
        
    /**
     * Simple implementation of abstract method.  Returns the input values.
     * @param values  input values
     * @return values
     * @throws VisADException  values are null or wrong dimension
     */
    public float[][] toReference(float[][] values)
        throws VisADException
    {
        if (values == null || values.length != getDimension())
            throw new VisADException("values are null or wrong dimension");
        return values;
    }

    /**
     * Check to see if the object in question is equal to this.
     * @param  o  object in question
     * @return  true if they are equal, otherwise false.
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof IdentityCoordinateSystem)) return false;
        if (!(getReference().equals(
                ((IdentityCoordinateSystem) o).getReference()))) return false;
        return true;
    }
}
