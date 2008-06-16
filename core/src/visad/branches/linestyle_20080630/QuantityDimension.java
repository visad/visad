//
// QuantityDimension.java
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

import java.io.Serializable;


/**
 * This class represents the dimension of a quantity.  For example,
 * consider a nonrelativistic particle of mass <VAR>m</VAR> in uniform
 * motion which travels a distance <VAR>l</VAR> in a time <VAR>t</VAR>.
 * Its velocity is <VAR>v=l/t</VAR> and its kinetic energy is
 * <VAR>E=mv<SUP>2</SUP>/2=ml<SUP>2</SUP>t<SUP>-2</SUP>/2</VAR>.
 * The dimension of <VAR>E</VAR> is dim
 * <VAR>E</VAR>=ML<SUP>2</SUP>T<SUP>-2</SUP> and the dimensional
 * exponents are 1, 2, and -2.
 *
 * A QuantityDimension is immutable.
 */
public final class QuantityDimension
  implements	Serializable, Comparable
{
  /**
   * The dimensional exponents.
   */
  private /*final*/ byte[]	exponents;


  /**
   * Constructs from nothing (i.e. constructs a dimensionless quantity).
   */
  public QuantityDimension()
  {
    initialize(0);
  }


  /**
   * Constructs from another dimension of a quantity.
   */
  public QuantityDimension(QuantityDimension that)
  {
    initialize(that.exponents.length);

    System.arraycopy(that.exponents, 0, exponents, 0, that.exponents.length);
  }


  /**
   * Constructs from an existing Unit.
   */
  public QuantityDimension(Unit unit)
    throws UnitException
  {
    if (unit instanceof BaseUnit)
      initialize(((BaseUnit)unit).derivedUnit);
    else
    if (unit instanceof DerivedUnit)
      initialize((DerivedUnit)unit);
    else
    if (unit instanceof ScaledUnit)
      initialize(((ScaledUnit)unit).derivedUnit);
    else
    if (unit instanceof OffsetUnit)
      initialize(((OffsetUnit)unit).scaledUnit.derivedUnit);
    else
      throw new UnitException("Can't construct " + getClass() + " from " +
	unit.getClass());
  }


  /**
   * Initialize from a number of base quantities.  Private to ensure use of
   * the public constructors and the concomitant setting of the dimensional
   * exponents.
   */
  private void initialize(int n)
  {
    exponents = new byte[n];
  }


  /**
   * Initialize from a DerivedUnit.
   */
  private void initialize(DerivedUnit unit)
    throws UnitException
  {
    initialize(BaseQuantity.size());

    for (int i = 0; i < unit.factors.length; ++i)
    {
      String		name = unit.factors[i].baseUnit.quantityName();
      BaseQuantity	baseQuantity = BaseQuantity.get(name);

      if (baseQuantity == null)
	throw new UnitException("No base quantity for \"" + name + "\"");

      exponents[baseQuantity.getIndex()] = (byte)unit.factors[i].power;
    }
  }


  /**
   * Compare this dimension of a quantity to another.
   */
  public int compareTo(Object obj)
  {
    QuantityDimension	that = (QuantityDimension)obj;

    return isShorterThan(that)
	      ?  compare(this, that)
	      : -compare(that, this);
  }


  /**
   * Indicate whether or not this dimension of a quantity is shorter or equal
   * to another.
   */
  private boolean isShorterThan(QuantityDimension that)
  {
    return exponents.length < that.exponents.length;
  }


  /**
   * Compare a shorter dimensional quantity to a longer one.
   *
   * @precondition	<code>shorter.exponents.length <=
   *			longer.exponents.length</code>.
   */
  private static int compare(QuantityDimension shorter,
    QuantityDimension longer)
  {
    int	n = 0;

    for (int i = 0; n == 0 && i < shorter.exponents.length; ++i)
      n = shorter.exponents[i] - longer.exponents[i];

    for (int i = shorter.exponents.length;
      n == 0 && i < longer.exponents.length;
      ++i)
    {
      n = -longer.exponents[i];
    }

    return n;
  }


  /**
   * Indicate whether or not this dimension of a quantity is the same as
   * another.
   */
  public boolean equals(Object obj)
  {
    return compareTo(obj) == 0;
  }


  /**
   * Raise this dimension of a quantity by a power.
   */
  public QuantityDimension raise(int power)
  {
    QuantityDimension	newDimension = new QuantityDimension(this);

    for (int i = 0; i < newDimension.exponents.length; ++i)
      newDimension.exponents[i] *= power;

    return newDimension;
  }


  /**
   * Multiply this dimension of a quantity by another.
   */
  public QuantityDimension multiply(QuantityDimension that)
  {
    return isShorterThan(that)
	      ? multiply(this, that)
	      : multiply(that, this);
  }


  /**
   * Multiply a shorter dimension of a quantity by a longer one.
   */
  private static QuantityDimension multiply(QuantityDimension shorter,
    QuantityDimension longer)
  {
    QuantityDimension	newDimension = new QuantityDimension(longer);

    for (int i = 0; i < shorter.exponents.length; ++i)
      newDimension.exponents[i] += shorter.exponents[i];

    return newDimension;
  }


  /**
   * Divide this dimension of a quantity by another.
   */
  public QuantityDimension divide(QuantityDimension that)
  {
    return multiply(that.raise(-1));
  }


  /**
   * Indicate whether or not this dimension of a quantity is dimensionless.
   */
  public boolean isDimensionless()
  {
    for (int i = 0; i < exponents.length; ++i)
      if (exponents[i] != 0)
	return false;

    return true;
  }


  /**
   * Return a string representation of this dimension of a quantity.
   */
  public String toString()
  {
    String	rep;

    if (isDimensionless())
    {
      rep = "1";	// dimensionless quantity
    }
    else
    {
      StringBuffer	buf = new StringBuffer(128);

      for (int i = 0; i < exponents.length; ++i)
      {
	int	exponent = exponents[i];

	if (exponent != 0)
	{
	  if (buf.length() > 0)
	    buf.append(" ");

	  buf.append("(");
	  buf.append(BaseQuantity.get(i).getName());
	  buf.append(")");

	  if (exponent != 1)
	  {
	    buf.append("^");
	    buf.append(exponent);
	  }
	}
      }

      rep = buf.toString();
    }

    return rep;
  }


  /**
   * Test this class.
   */
  // /*
  public static void main(String[] args)
  {
    QuantityDimension	dim1 = new QuantityDimension();

    dim1.exponents[0] = -3;
    dim1.exponents[1] = -2;
    dim1.exponents[2] = -1;
    dim1.exponents[3] = 0;
    dim1.exponents[4] = 1;
    dim1.exponents[5] = 2;
    dim1.exponents[6] = 3;
    dim1.exponents[7] = 4;

    System.out.println("dim1=(" + dim1 + ")");
    System.out.println("dim1.equals(dim1)=" + dim1.equals(dim1));
    System.out.println("dim1.compareTo(dim1)=" + dim1.compareTo(dim1));

    QuantityDimension	dim2 = new QuantityDimension();

    dim2.exponents[0] = -3;
    dim2.exponents[1] = -2;
    dim2.exponents[2] = -1;
    dim2.exponents[3] = 1;
    dim2.exponents[4] = 1;
    dim2.exponents[5] = 2;
    dim2.exponents[6] = 3;
    dim2.exponents[7] = 4;

    System.out.println("dim2=(" + dim2 + ")");
    System.out.println("dim1.equals(dim2)=" + dim1.equals(dim2));
    System.out.println("dim1.compareTo(dim2)=" + dim1.compareTo(dim2));
  }
  // */
}
