//
// GridCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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
   GridCoordinateSystem is the VisAD CoordinateSystem class
   for grids defined by GriddedSets.<P>

   It should be used as the CoordinateSystem of an IntegerSet
   describing the set of grid values (so the dimensions of the
   IntegerSet should match the dimensions of the GriddedSet
   argument to the GridCoordinateSystem constructor) where
   the reference RealTupleType describes the value space of the
   GriddedSet.<P>
*/
public class GridCoordinateSystem extends CoordinateSystem {

  private GriddedSet set;
  private int dimension;

  /** construct a CoordinateSystem for grid coordinates (e.g.,
      (row, column, level) in 3-D) relative to the value space
      of set; for example, if satellite pixel locations are
      defined by explicit latitudes and longitude, these could
      be used to construct a Gridded2DSet which could then be
      used to construct a GridCoordinateSystem for (ImageLine,
      ImageElement) coordinates relative to reference coordinates
      (Latitude, Longitude) */
  public GridCoordinateSystem(GriddedSet s) throws VisADException {
    super(((SetType) s.getType()).getDomain(), null);
    set = s;
    dimension = set.getDimension();
  }

  /** Returns the GriddedSet associated with this instance.
      @return			The GriddedSet associated with this instance.
   */
  public GriddedSet getGriddedSet() {
    return set;
  }

  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != dimension) {
      throw new CoordinateSystemException("GridCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    double[][] values =
      Set.floatToDouble(set.gridToValue(Set.doubleToFloat(tuples)));
    Unit[] units_in = set.getSetUnits();
    Unit[] units_out = getReference().getDefaultUnits();
    ErrorEstimate[] errors_out = new ErrorEstimate[1];
    for (int i=0; i<dimension; i++) {
      values[i] =
        Unit.transformUnits(units_out[i], errors_out, units_in[i],
                            null, values[i],false);
    }
    return values;
  }

  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != dimension) {
      throw new CoordinateSystemException("GridCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    double[][] values = new double[dimension][];
    for (int i=0; i<dimension; i++) values[i] = tuples[i];
    Unit[] units_in = getReference().getDefaultUnits();
    Unit[] units_out = set.getSetUnits();
    ErrorEstimate[] errors_out = new ErrorEstimate[1];
    for (int i=0; i<dimension; i++) {
      values[i] =
        Unit.transformUnits(units_out[i], errors_out, units_in[i],
                            null, values[i]);
    }
    values =
      Set.floatToDouble(set.valueToGrid(Set.doubleToFloat(values)));
    return values;
  }

  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != dimension) {
      throw new CoordinateSystemException("GridCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    float[][] values = set.gridToValue(tuples);
    Unit[] units_in = set.getSetUnits();
    Unit[] units_out = getReference().getDefaultUnits();
    ErrorEstimate[] errors_out = new ErrorEstimate[1];
    for (int i=0; i<dimension; i++) {
      values[i] =
        Unit.transformUnits(units_out[i], errors_out, units_in[i],
                            null, values[i],false);
    }
    return values;
  }

  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != dimension) {
      throw new CoordinateSystemException("GridCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    float[][] values = new float[dimension][];
    for (int i=0; i<dimension; i++) values[i] = tuples[i];
    Unit[] units_in = getReference().getDefaultUnits();
    Unit[] units_out = set.getSetUnits();
    ErrorEstimate[] errors_out = new ErrorEstimate[1];
    for (int i=0; i<dimension; i++) {
      values[i] =
        Unit.transformUnits(units_out[i], errors_out, units_in[i],
                            null, values[i]);
    }
    values = set.valueToGrid(values);
    return values;
  }

  public boolean equals(Object cs) {
    return (cs instanceof GridCoordinateSystem &&
            set.equals(((GridCoordinateSystem) cs).set));
  }

}

