//
// Vis5DVerticalSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.vis5d;

import visad.CommonUnit;
import visad.CoordinateSystem;
import visad.ErrorEstimate;
import visad.Gridded1DSet;
import visad.IdentityCoordinateSystem;
import visad.Linear1DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.SampledSet;
import visad.Set;
import visad.Unit;
import visad.VisADException;
import visad.data.units.ParseException;
import visad.data.units.Parser;

/**
 * Class for encapsulating the Vis5D vertical system as VisAD
 * MathTypes and Data objects
 */
public class Vis5DVerticalSystem
{
  private static int counter = 0;

  /** Unit used for the vertical system */
  Unit  vert_unit = null;
  /** Sampled Set of values */
  SampledSet vertSet;
  /** RealType of the vertical system parameter */
  RealType vert_type;
  /** CoordinateSystem to transform to Reference values */
  CoordinateSystem vert_cs;
  /** Reference RealTupleType for the CoordinateSystem */
  RealTupleType reference;
  
  /**
   * Construct the VisAD MathTypes and Data objects that relate
   * to the Vis5D vertical system parameters.
   * @param vert_sys  Vis5D vertical System 
   * @param n_levels  number of levels in vert_args
   * @param vert_args array of vertical level values
   * @throws VisADException  unknown vert_sys or problem creating VisAD
   *                         objects.
   * @see visad.data.vis5d.V5DStruct#VertArgs for explanation of vertical
   *      coordinates.
   */
  public Vis5DVerticalSystem( int vert_sys,
                              int n_levels,
                              double[] vert_args)
         throws VisADException
  {

    switch ( vert_sys )
    {
      case (0):
        vert_unit = CommonUnit.promiscuous;
        vert_type = makeRealType("Height", vert_unit);
        reference = new RealTupleType(RealType.Generic);
        vert_cs = new IdentityCoordinateSystem(reference);
        break;
      case (1):
      case (2):
        try {
          vert_unit = Parser.parse("km");
        }
        catch (ParseException e) {
        }
        vert_type = makeRealType("Height", vert_unit);
        reference = new RealTupleType(RealType.Altitude);
        vert_cs = new IdentityCoordinateSystem(reference);
        break;
      case (3):
        try {
          vert_unit = Parser.parse("mbar");
        }
        catch (ParseException e) {
        }
        vert_type = makeRealType("Pressure", vert_unit);
        reference = new RealTupleType(RealType.Altitude);
        vert_cs = new Vis5DVerticalCoordinateSystem();
        break;
      default:
        throw new VisADException("vert_sys unknown");
    }

    switch ( vert_sys )
    {
      case (0):
      case (1):
        double first = vert_args[0];
        double last = first + vert_args[1]*(n_levels-1);
        vertSet = new Linear1DSet(vert_type, first, last, n_levels,
                           (CoordinateSystem) null, new Unit[] {vert_unit}, 
                           (ErrorEstimate[]) null);
        break;
      case (2):  // Altitude in km - non-linear
        double[][] values = new double[1][n_levels];
        System.arraycopy(vert_args, 0, values[0], 0, n_levels);
        vertSet =
          new Gridded1DSet(vert_type, Set.doubleToFloat(values), n_levels,
                           (CoordinateSystem) null, new Unit[] {vert_unit}, 
                           (ErrorEstimate[]) null);
        break;
      case (3):  // heights of pressure surfaces in km - non-linear
        double[][] pressures = new double[1][n_levels];
        System.arraycopy(vert_args, 0, pressures[0], 0, n_levels);
        for (int i = 0; i < n_levels; i++) pressures[0][i] *=1000; // km->m
        pressures = vert_cs.fromReference(pressures); // convert to pressures
        vertSet =
          new Gridded1DSet(vert_type, Set.doubleToFloat(pressures), n_levels,
                           (CoordinateSystem) null, new Unit[] {vert_unit}, 
                           (ErrorEstimate[]) null);
        break;
      default:
         throw new VisADException("vert_sys unknown");
    }
  }

  /** create a unique RealType for the specified name and unit */
  private RealType makeRealType(String name, Unit unit) 
      throws VisADException {
    RealType rt = null;
    rt = RealType.getRealType(name, unit);
    if (rt == null) {
      rt = RealType.getRealType(name+"_"+counter++, unit);
      if (rt == null) {
        throw new VisADException(
          "Unable to create a unique RealType named " + name + 
          " with unit " + unit);
      }
    }
    return rt;
  }

  /**
   * Vis5DVerticalCoordinateSystem is the VisAD class for coordinate
   * systems for transforming pressure in millibars to Altitude
   * in m.  It uses the standard Vis5D climate formulas:
   * <pre>
   *         P = 1012.5 * e^( H / -7.2 )        (^ denotes exponentiation)
   * 
   *         H = -7.2 * Ln( P / 1012.5 )        (Ln denotes natural log)
   * </pre>
   * for the transformations (in this case H is in km).
   * <P>
   */
  
  public static class Vis5DVerticalCoordinateSystem extends CoordinateSystem
  {
  
    /** Default scale value for logarithmic vertical coordinate system */
    private static final double DEFAULT_LOG_SCALE = 1012.5;
  
    /** Default exponent value for logarithmic vertical coordinate system */
    private static final double DEFAULT_LOG_EXP = -7.2;
  
    private static Unit[] csUnits;
  
    static {
      try {
         csUnits = new Unit[] {Parser.parse("mbar")};
      }
      catch (ParseException pe) {;} // can't happen?
    }
  
    /**
     * Construct a new vertical transformation system
     */
    public Vis5DVerticalCoordinateSystem()
         throws VisADException
    {
      super( new RealTupleType(RealType.Altitude), csUnits );
    }
  
    /**
     * Converts pressures in millibars to altitude in meters.
     * @param  pressures  array of pressures
     * @return array of corresponding altitudes
     * @throws VisADException  illegal input
     */
    public double[][] toReference(double[][] pressures)
           throws VisADException
    {
      int length = pressures[0].length;
      double[][] alts = new double[1][length];
  
      for (int kk = 0; kk < length; kk++) {
        alts[0][kk] = pressureToAltitude(pressures[0][kk]);
      }
      return alts;
    }
  
    /**
     * Converts altitudes in m to pressure in millibars.
     * @param  alts  array of altitudes
     * @return array of corresponding pressures
     * @throws VisADException  illegal input
     */
    public double[][] fromReference(double[][] alts)
           throws VisADException
    {
      int length = alts[0].length;
      double[][] pressures = new double[1][length];
  
      for (int kk = 0; kk < length; kk++) {
        pressures[0][kk] = altitudeToPressure(alts[0][kk]);
      }
      return pressures;
    }
  
    /**
     * Converts pressures in millibars to altitude in meters.
     * @param  pressures  array of pressures
     * @return array of corresponding altitudes
     * @throws VisADException  illegal input
     */
    public float[][] toReference(float[][] pressures)
           throws VisADException
    {
      int length = pressures[0].length;
      float[][] alts = new float[1][length];
  
      for (int kk = 0; kk < length; kk++) {
        alts[0][kk] = (float) pressureToAltitude(pressures[0][kk]);
      }
      return alts;
    }
  
    /**
     * Converts altitudes in m to pressure in millibars.
     * @param  alts  array of altitudes
     * @return array of corresponding pressures
     * @throws VisADException  illegal input
     */
    public float[][] fromReference(float[][] alts)
           throws VisADException
    {
      int length = alts[0].length;
      float[][] pressures = new float[1][length];
  
      for (int kk = 0; kk < length; kk++) {
        pressures[0][kk] = (float) altitudeToPressure(alts[0][kk]);
      }
      return pressures;
    }
  
    /** 
     * Checks the equality of o against this coordinate system
     * @param o object in question
     * @return true if o is a Vis5DVerticalCoordinateSystem
     */
    public boolean equals(Object o) {
      return (o instanceof Vis5DVerticalCoordinateSystem);
    }

    /**
     * Converts an altitude value in meters to a pressure value in
     * millibars. It uses the standard Vis5D climate formula:
     * <pre>
     *         P = 1012.5 * e^( H / -7.2 )     (^ denotes exponentiation)
     *
     * (H is in km in this formula, but input value is meters)
     * </pre>
     * @param  alt	 value to convert
     * @return  corresponding pressure value
     */
    public static double altitudeToPressure(double alt) {
      return (DEFAULT_LOG_SCALE * Math.exp((alt/1000.) / DEFAULT_LOG_EXP));
    }
  
    /**
     * Converts a pressure value in millibars to an altitude in
     * meters. It uses the standard Vis5D climate formula:
     * <pre>
     *         H = -7.2 * Ln( P / 1012.5 )        (Ln denotes natural log)
     *
     * (H is in km in this formula, but returned value is meters)
     * </pre>
     * @param  pressure value to convert
     * @return  corresponding altitude value
     */
    public static double pressureToAltitude(double pressure) {
      return (DEFAULT_LOG_EXP * 
                   Math.log( pressure / DEFAULT_LOG_SCALE)) * 1000.;
    }
  }
}
