//
// DisplayTupleType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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
   DisplayTupleType is the class for tuples of DisplayRealType's.<P>
*/

public class DisplayTupleType extends RealTupleType {

  /** null CoordinateSystem; a DisplayrealType may
      not be a component of more than one DisplayTupleType */
  public DisplayTupleType(DisplayRealType[] types) throws VisADException {
    this(types, null);
  }

  /** if coord_sys is not null then coord_sys.Reference
      must be another DisplayTupleType; a DisplayrealType may
      not be a component of more than one DisplayTupleType */
  public DisplayTupleType(DisplayRealType[] types, CoordinateSystem coord_sys)
         throws VisADException {
    super(types, coord_sys, null);
    if (coord_sys != null) {
      RealTupleType ref = coord_sys.getReference();
      if (!(ref instanceof DisplayTupleType)) {
        throw new CoordinateSystemException("DisplayTupleType: " +
                    "CoordinateSystem.Reference must be a DisplayTupleType");
      }
      else if (Display.DisplaySpatialOffsetTuple.equals(ref)) {
        throw new CoordinateSystemException("DisplayTupleType: " +
               "CoordinateSystem.Reference cannot be DisplaySpatialOffsetTuple");
      }
/*
      else if (Display.DisplayFlow1Tuple.equals(ref)) {
        throw new CoordinateSystemException("DisplayTupleType: " +
               "CoordinateSystem.Reference cannot be DisplayFlow1Tuple");
      }
      else if (Display.DisplayFlow2Tuple.equals(ref)) {
        throw new CoordinateSystemException("DisplayTupleType: " +
               "CoordinateSystem.Reference cannot be DisplayFlow2Tuple");
      }
*/
      Unit[] default_units = getDefaultUnits();
      Unit[] coord_sys_units = coord_sys.getCoordinateSystemUnits();
      int n = default_units.length;
      boolean match = true;
      for (int i=0; i<n; i++) {
        if (default_units[i] == null) {
          if (coord_sys_units[i] != null) match = false;
        }
        else {
          if (!default_units[i].equals(coord_sys_units[i])) match = false;
        }
      }
      if (!match) {
        throw new UnitException("RealTupleType: CoordinateSystem Units must " +
                                "equal default Units");
      }
    } // end if (coord_sys != null)
    setTuples(types, coord_sys);
  }

  /** trusted constructor for initializers */
  DisplayTupleType(DisplayRealType[] types, boolean b) {
    this(types, null, b);
  }

  /** trusted constructor for initializers */
  DisplayTupleType(DisplayRealType[] types, CoordinateSystem coord_sys,
                   boolean b) {
    super(types, coord_sys, b);
    try {
      setTuples(types, coord_sys);
    }
    catch (VisADException e) {
      System.out.println(e);
    }
  }

  private void setTuples(DisplayRealType[] types, CoordinateSystem coord_sys)
          throws VisADException {
    int n = types.length;
    boolean[] circulars = new boolean[n];
    for (int i=0; i<n; i++) circulars[i] = false;
    if (coord_sys != null && coord_sys.getReference().equals(
          Display.DisplaySpatialCartesianTuple)) {
      double[] defaults = new double[n];
      for (int i=0; i<n; i++) {
        defaults[i] = types[i].getDefaultValue();
      }
      for (int i=0; i<n; i++) {
        Unit u = types[i].getDefaultUnit();
        if (u != null && Unit.canConvert(CommonUnit.degree, u)) {
// System.out.println(types[i] + " unit " + u);
          double[][] test = new double[n][3];
          for (int j=0; j<n; j++) {
            if (j == i) {
              test[j][0] = u.toThis(0.0, CommonUnit.degree);
              test[j][1] = u.toThis(180.0, CommonUnit.degree);
              test[j][2] = u.toThis(360.0, CommonUnit.degree);
            }
            else {
              test[j][0] = defaults[j];
              test[j][1] = defaults[j];
              test[j][2] = defaults[j];
            }
          }
// System.out.println(test[i][0] + " " + test[i][1] + " " + test[i][2]);
          double[][] tt = coord_sys.toReference(test);
          double diff180 = Math.sqrt(
            (tt[0][1] - tt[0][0]) * (tt[0][1] - tt[0][0]) +
            (tt[1][1] - tt[1][0]) * (tt[1][1] - tt[1][0]) +
            (tt[2][1] - tt[2][0]) * (tt[2][1] - tt[2][0]));
          double diff360 = Math.sqrt(
            (tt[0][2] - tt[0][0]) * (tt[0][2] - tt[0][0]) +
            (tt[1][2] - tt[1][0]) * (tt[1][2] - tt[1][0]) +
            (tt[2][2] - tt[2][0]) * (tt[2][2] - tt[2][0]));
          if (diff360 < 0.01 * diff180) circulars[i] = true;
// System.out.println("diff180 = " + diff180 + " diff360 = " + diff360 +
//                    " " + circulars[i]);
        } // end if (u != null && Unit.canConvert(CommonUnit.degree, u))
      } // end for (int i=0; i<n; i++)
    }
    for (int i=0; i<n; i++) {
      types[i].setTuple(this, i, circulars[i]);
    }
  }
}

