//
// LogCoordinateSystem.java
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
 * A CoordinateSystem to transform between values and their logarithms.
 * The logarithm is the reference.
 */
public class LogCoordinateSystem extends CoordinateSystem {
  
  private double base = 10.0;

  /** 
   * Construct a coordinate system with logarithmical reference 
   * of base 10.  
   * @param  reference  MathType of values
   * @throws VisADException  some VisAD error
   */
  public LogCoordinateSystem(RealTupleType reference) 
    throws VisADException 
  {
    this(reference, 10.0);
  }

  /** 
   * Construct a coordinate system with logarithmical reference specified
   * @param  reference  MathType of values
   * @param  base  logrithmic base
   * @throws VisADException  negative or zero base specified
   */
  public LogCoordinateSystem(RealTupleType reference, double base) 
    throws VisADException 
  {
    super(reference, reference.getDefaultUnits());
    if (base <= 0)
      throw new VisADException(
        "LogCoordinateSystem: log base (" + base + ") must be positive");
    this.base = base;
  }
  

  /** 
   * Convert values to logarithmic values. 
   * @param  values  array of values
   * @return array of logarithms of values
   * @throws VisADException values dimension not the same as CS dimension
   */
  public double[][] toReference(double[][] values) 
    throws VisADException 
  {
    if (values == null || values[0].length < 1) return values;
    if (values.length != getDimension())
    {
      throw new CoordinateSystemException(
         "LogCoordinateSystem." + 
         "toReference: values wrong dimension");
    }
    
    int len = values[0].length;
    double[][] logValues = new double[getDimension()][len];

    for(int i = 0; i < getDimension(); i++)
    {
      for (int j = 0; j < len; j++)
      {
        logValues[i][j] = Math.log(values[i][j])/Math.log(base);
      }
    }
    return logValues;
  }

  
  /** 
   * Convert logrithmic values to values. 
   * @param  logValues  array of logrithmic values
   * @return array of values
   * @throws VisADException logValues dimension not the same as CS dimension
   */
  public double[][] fromReference(double[][] logValues) 
    throws VisADException 
  {
    if (logValues == null || logValues[0].length < 1) return logValues;
    if (logValues.length != getDimension())
    {
      throw new CoordinateSystemException(
         "LogCoordinateSystem." + 
         "fromReference: logValues wrong dimension");
    }
  
    int len = logValues[0].length;
    double[][] values = new double[getDimension()][len];
  
    for(int i = 0; i < getDimension(); i++)
    {
      for (int j = 0; j < len; j++)
      {
        values[i][j] = Math.pow(base, logValues[i][j]);
      }
    }
    return values;
   }
  
  /**
   * Get the base used in this LogCoordinateSystem.
   */
  public double getBase() {
    return base;
  }

  /**
   * See if the Object in question is equal to this LogCoordinateSystem
   * @param cs  Object in question
   * @return  true if cs's reference tuples and base is equal to this's
   */
  public boolean equals(Object cs) 
  {
    if (!(cs instanceof LogCoordinateSystem)) return false;
    LogCoordinateSystem that = (LogCoordinateSystem) cs;
    return this == that ||
           (that.getReference().equals(this.getReference()) &&
           (Double.doubleToLongBits(that.base) == 
           Double.doubleToLongBits(this.base)));
  }
}
