/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.visad;

import java.rmi.RemoteException;

import java.util.ArrayList;

import visad.*;

class FakeCoordinateSystem
  extends CoordinateSystem
{
  public FakeCoordinateSystem(RealTupleType rtt, Unit[] unit)
    throws VisADException
  {
    super(rtt, unit);
  }

  public double[][] toReference(double[][] d) { return null; }
  public double[][] fromReference(double[][] d) { return null; }

  public boolean equals(Object obj)
  {
    if (!(obj instanceof FakeCoordinateSystem)) {
      return false;
    }

    FakeCoordinateSystem fcs = (FakeCoordinateSystem )obj;

    if (fcs.getDimension() != getDimension()) {
      return false;
    }

    if (!getReference().equals(fcs.getReference())) {
      return false;
    }

    Unit[] units, fcsUnits;
    units = getCoordinateSystemUnits();
    fcsUnits = fcs.getCoordinateSystemUnits();
    if (units == null) {
      if (fcsUnits != null) {
        return false;
      }
    } else if (fcsUnits == null) {
      return false;
    } else if (units.length != fcsUnits.length) {
      return false;
    } else {
      for (int i = 0; i < units.length; i++) {
        if (units[i] == null) {
          if (fcsUnits[i] != null) {
            return false;
          }
        } else if (fcsUnits[i] == null) {
          return false;
        } else if (!units[i].equals(fcsUnits[i])) {
          return false;
        }
      }
    }

    return true;
  }
}

/**
 * This class creates a list containing all(?) permutations of 
 * VisAD Data objects, which is used by the TestBinary class
 * to the the binary file writer & reader classes.
 */
class FakeData
{
  private Unit foot, pound;
  private RealType meHgt, meWgt, enHgt, enWgt;

  private FakeCoordinateSystem cSys1D, cSys2D, cSys3D;
  private Unit[] enUnits1D, enUnits2D, enUnits3D;
  private RealTupleType tuple1D, tuple2D, tuple3D;

  FakeData()
    throws VisADException
  {
    foot = new ScaledUnit(0.3048, SI.meter, "foot");
    pound = new ScaledUnit(2.204622, SI.kilogram, "pound");

    enHgt = RealType.getRealType("EnglishHeight", foot, null);
    enWgt = RealType.getRealType("EnglishWeight", pound, null);

    meHgt = RealType.getRealType("MetricHeight", SI.meter, null);
    meWgt = RealType.getRealType("MetricWeight", SI.kilogram, null);

    enUnits1D = new Unit[] { foot };
    cSys1D = new FakeCoordinateSystem(new RealTupleType(meHgt), enUnits1D);
    tuple1D = new RealTupleType(meHgt, cSys1D, null);

    enUnits2D = new Unit[] { foot, pound };
    cSys2D = new FakeCoordinateSystem(new RealTupleType(meHgt, meWgt),
                                      enUnits2D);
    tuple2D = new RealTupleType(meHgt, meWgt, cSys2D, null);

    enUnits3D = new Unit[] { foot, pound, SI.second };
    cSys3D = new FakeCoordinateSystem(new RealTupleType(meHgt, meWgt,
                                                        RealType.Time),
                                      enUnits3D);
    tuple3D = new RealTupleType(meHgt, meWgt, RealType.Time, cSys3D, null);
  }

  private void fakeFunctions(ArrayList list)
  {
    try {
      FunctionType ft = new FunctionType(RealType.Time, enHgt);
      Set set = new Integer1DSet(RealType.Time, 15);
      list.add(new FieldImpl(ft, set));
      list.add(new FlatField(ft, set));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Function");
      ve.printStackTrace();
      System.exit(1);
      return;
    }
  }

  private void fakeGriddedSets(ArrayList list)
  {
    float[][] data;
    int[] lengths;
    ErrorEstimate[] errors;

    data = new float[][] { { 0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f } };
    lengths = new int[] { 10 };
    errors = new ErrorEstimate[] { new ErrorEstimate(1.23, 0.04, foot) };

    try {
      list.add(new GriddedSet(RealType.Altitude, data, lengths));
      list.add(new GriddedSet(tuple1D, data, lengths, cSys1D, enUnits1D,
                              errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build GriddedSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    fakeGridded1DSets(list);
    fakeGridded2DSets(list);
    fakeGridded3DSets(list);
    fakeGriddedNDSets(list);
  }

  private void fakeGridded1DSets(ArrayList list)
  {
    float[][] data;
    double[][] dblData;
    ErrorEstimate[] errors;

    data = new float[][] { { 0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f } };
    dblData = new double[][] { { 1., 2., 3., 4., 5., 6., 7., 8. } };
    errors = new ErrorEstimate[] { new ErrorEstimate(1.23, 0.04, foot) };

    try {
      list.add(new Gridded1DSet(RealType.Altitude, data, data[0].length));
      list.add(new Gridded1DSet(tuple1D, data, data[0].length, cSys1D,
                                enUnits1D, errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Gridded1DSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      list.add(new Gridded1DDoubleSet(RealType.Altitude, data,
                                      data[0].length));
      list.add(new Gridded1DDoubleSet(tuple1D, data, data[0].length, cSys1D,
                                      enUnits1D, errors));
      list.add(new Gridded1DDoubleSet(RealType.Altitude, dblData,
                                      dblData[0].length));
      list.add(new Gridded1DDoubleSet(tuple1D, dblData, dblData[0].length,
                                      cSys1D, enUnits1D, errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Gridded1DDoubleSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      list.add(new Linear1DSet(-1.23, 4.56, 10));
      list.add(new Linear1DSet(RealType.TimeInterval, 1.35, 7.9, 11));
      list.add(new Linear1DSet(tuple1D, 3.21, 6.66, 5, cSys1D, enUnits1D,
                               errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Linear1DSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      list.add(new Integer1DSet(15));
      list.add(new Integer1DSet(RealType.TimeInterval, 7));
      list.add(new Integer1DSet(tuple1D, 5, cSys1D, enUnits1D, errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Integer1DSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }
  }

  private void fakeGridded2DSets(ArrayList list)
  {
    float[][] data;
    double[][] dblData;
    ErrorEstimate[] errors;
    Linear1DSet[] lset;

    data = new float[][] { { 0.1f, 0.2f, 0.3f, 0.4f,
                             0.5f, 0.6f, 0.7f, 0.8f,
                             0.9f, 1.0f, 1.1f, 1.2f },
                           { 0.1f, 0.2f, 0.3f, 0.4f,
                             0.5f, 0.6f, 0.7f, 0.8f,
                             0.9f, 1.0f, 1.1f, 1.2f } };

    dblData = new double[][] { { 1., 2., 3.,
                                 4., 5., 6.,
                                 7., 8., 9.,
                                 10., 11., 12. },
                               { 11., 10., 9.,
                                 8., 7., 6.,
                                 5., 4., 3.,
                                 2., 1., 0. } };

    errors = new ErrorEstimate[] {
      new ErrorEstimate(1.23, 0.04, foot),
      new ErrorEstimate(0.56, 7.8, pound)
        };

    try {
      lset = new Linear1DSet[2];
      lset[0] = new Linear1DSet(1., 4., 3);
      lset[1] = new Linear1DSet(5., 8., 4);
    } catch (VisADException ve) {
      System.err.println("Couldn't build Linear1DSet arguments" +
                         " for 2D set tests");
      ve.printStackTrace();
      lset = null;
    }

    try {
      list.add(new Gridded2DSet(RealTupleType.SpatialCartesian2DTuple,
                                data, data[0].length));
      list.add(new Gridded2DSet(tuple2D, data, data[0].length, cSys2D,
                                enUnits2D, errors));

      list.add(new Gridded2DSet(RealTupleType.SpatialCartesian2DTuple,
                                data, 3, 4));
      list.add(new Gridded2DSet(tuple2D, data, 3, 4, cSys2D,
                                enUnits2D, errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Gridded2DSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      list.add(new Gridded2DDoubleSet(RealTupleType.SpatialCartesian2DTuple,
                                      data, data[0].length));
      list.add(new Gridded2DDoubleSet(tuple2D, data, data[0].length,
                                      cSys2D, enUnits2D, errors));
      list.add(new Gridded2DDoubleSet(RealTupleType.SpatialCartesian2DTuple,
                                      data, 3, 4));
      list.add(new Gridded2DDoubleSet(tuple2D, data, 3, 4, cSys2D,
                                      enUnits2D, errors));

      list.add(new Gridded2DDoubleSet(RealTupleType.SpatialCartesian2DTuple,
                                      dblData, dblData[0].length));
      list.add(new Gridded2DDoubleSet(tuple2D, dblData, dblData[0].length,
                                      cSys2D, enUnits2D, errors));
      list.add(new Gridded2DDoubleSet(RealTupleType.SpatialCartesian2DTuple,
                                      dblData, 4, 3));
      list.add(new Gridded2DDoubleSet(tuple2D, dblData, 4, 3, cSys2D,
                                      enUnits2D, errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Gridded2DDoubleSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      list.add(new Linear2DSet(-1.23, 4.56, 5, 7.89, 12.34, 5));
      list.add(new Linear2DSet(RealTupleType.SpatialCartesian2DTuple,
                               1., 5., 5, 1., 8., 8));
      list.add(new Linear2DSet(tuple2D, 3., 9., 3, 3., 7., 4, cSys2D,
                               enUnits2D, errors));

      if (lset != null) {
        list.add(new Linear2DSet(RealTupleType.SpatialCartesian2DTuple,
                                 lset));
        list.add(new Linear2DSet(tuple2D, lset, cSys2D, enUnits2D, errors));
      }
    } catch (VisADException ve) {
      System.err.println("Couldn't build Linear2DSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      list.add(new Integer2DSet(5, 3));
      list.add(new Integer2DSet(RealTupleType.SpatialCartesian2DTuple,
                                7, 2));
      list.add(new Integer2DSet(tuple2D, 5, 4, cSys2D, enUnits2D, errors));

      Integer1DSet[] iset = new Integer1DSet[2];
      iset[0] = new Integer1DSet(5);
      iset[1] = new Integer1DSet(4);

      list.add(new Integer2DSet(RealTupleType.SpatialCartesian2DTuple,
                                iset));
      list.add(new Integer2DSet(tuple2D, iset, cSys2D, enUnits2D, errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Integer2DSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      list.add(new LinearLatLonSet(RealTupleType.SpatialEarth2DTuple,
                                   4., 6., 5, 1.23, 4.56, 5));
      /* XXX
         list.add(new LinearLatLonSet(tuple2D, 1., 4., 3, 1., 4., 6, cSys2D,
         enUnits2D, errors));
      */

      if (lset != null) {
        list.add(new LinearLatLonSet(RealTupleType.SpatialEarth2DTuple,
                                     lset));
        /* XXX
           list.add(new LinearLatLonSet(tuple2D, lset, cSys2D, enUnits2D,
           errors));
        */
      }
    } catch (VisADException ve) {
      System.err.println("Couldn't build LinearLatLonSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }
  }

  private void fakeGridded3DSets(ArrayList list)
  {
    float[][] data;
    double[][] dblData;
    ErrorEstimate[] errors;
    Linear1DSet[] lset;

    data = new float[][] { { 0.1f, 0.2f, 0.3f, 0.4f,
                             0.5f, 0.6f, 0.7f, 0.8f,
                             0.9f, 1.0f, 1.1f, 1.2f },
                           { 0.31f, 0.32f, 0.33f, 0.34f,
                             0.35f, 0.36f, 0.37f, 0.38f,
                             0.39f, 0.40f, 0.41f, 0.42f },
                           { 0.2f, 0.4f, 0.6f, 0.8f,
                             1.0f, 1.2f, 1.4f, 1.6f,
                             1.8f, 2.0f, 2.2f, 2.4f }
    };

    dblData = new double[][] { { 1., 2., 3.,
                                 4., 5., 6.,
                                 7., 8., 9.,
                                 10., 11., 12. },
                               { 11., 10., 9.,
                                 8., 7., 6.,
                                 5., 4., 3.,
                                 2., 1., 0. },
                               { 4., 4.5f, 5.f,
                                 5.5f, 6.f, 6.5f,
                                 7.f, 7.5f, 8.f,
                                 8.5f, 9.f, 9.5f }
    };

    errors = new ErrorEstimate[] {
      new ErrorEstimate(1.23, 0.04, foot),
      new ErrorEstimate(0.56, 7.8, pound),
      new ErrorEstimate(0.9, 0.9, SI.second)
        };

    try {
      lset = new Linear1DSet[3];
      lset[0] = new Linear1DSet(1., 4., 3);
      lset[1] = new Linear1DSet(5., 8., 4);
      lset[2] = new Linear1DSet(9., 123.4, 5);
    } catch (VisADException ve) {
      System.err.println("Couldn't build Linear1DSet arguments" +
                         " for 3D set tests");
      ve.printStackTrace();
      lset = null;
    }

    try {
      list.add(new Gridded3DSet(RealTupleType.SpatialCartesian3DTuple,
                                data, data[0].length));
      list.add(new Gridded3DSet(tuple3D, data, data[0].length, cSys3D,
                                enUnits3D, errors));

      list.add(new Gridded3DSet(RealTupleType.SpatialCartesian3DTuple,
                                data, 3, 4));
      list.add(new Gridded3DSet(tuple3D, data, 3, 4, cSys3D,
                                enUnits3D, errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Gridded3DSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      list.add(new Gridded3DDoubleSet(RealTupleType.SpatialCartesian3DTuple,
                                      data, data[0].length));
      list.add(new Gridded3DDoubleSet(tuple3D, data, data[0].length,
                                      cSys3D, enUnits3D, errors));
      list.add(new Gridded3DDoubleSet(RealTupleType.SpatialCartesian3DTuple,
                                      data, 3, 4));
      list.add(new Gridded3DDoubleSet(tuple3D, data, 3, 4, cSys3D,
                                      enUnits3D, errors));

      list.add(new Gridded3DDoubleSet(RealTupleType.SpatialCartesian3DTuple,
                                      dblData, dblData[0].length));
      list.add(new Gridded3DDoubleSet(tuple3D, dblData, dblData[0].length,
                                      cSys3D, enUnits3D, errors));
      list.add(new Gridded3DDoubleSet(RealTupleType.SpatialCartesian3DTuple,
                                      dblData, 4, 3));
      list.add(new Gridded3DDoubleSet(tuple3D, dblData, 4, 3, cSys3D,
                                      enUnits3D, errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Gridded3DDoubleSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      list.add(new Linear3DSet(-1.23, 4.56, 5, 7.89, 12.34, 5, 6.7, 8.9, 5));
      list.add(new Linear3DSet(RealTupleType.SpatialCartesian3DTuple,
                               1., 5., 4, 1., 7., 7, 1., 9., 4));
      list.add(new Linear3DSet(tuple3D, 3., 9., 3, 3., 7., 4, 3., 5., 5,
                               cSys3D, enUnits3D, errors));

      if (lset != null) {
        list.add(new Linear3DSet(RealTupleType.SpatialCartesian3DTuple,
                                 lset));
        list.add(new Linear3DSet(tuple3D, lset, cSys3D, enUnits3D, errors));
      }
    } catch (VisADException ve) {
      System.err.println("Couldn't build Linear3DSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      list.add(new Integer3DSet(5, 3, 1));
      list.add(new Integer3DSet(RealTupleType.SpatialCartesian3DTuple,
                                7, 2, 4));
      list.add(new Integer3DSet(tuple3D, 5, 4, 3, cSys3D, enUnits3D,
                                errors));

      Integer1DSet[] iset = new Integer1DSet[3];
      iset[0] = new Integer1DSet(5);
      iset[1] = new Integer1DSet(4);
      iset[2] = new Integer1DSet(3);

      list.add(new Integer3DSet(RealTupleType.SpatialCartesian3DTuple,
                                iset));
      list.add(new Integer3DSet(tuple3D, iset, cSys3D, enUnits3D, errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Integer3DSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }
  }

  private void fakeGriddedNDSets(ArrayList list)
  {
    Unit[] enUnits5D;
    enUnits5D = new Unit[] { foot, pound, SI.second,
                             CommonUnit.degree, CommonUnit.degree };

    RealType[] typeList = new RealType[] { meHgt, meWgt, RealType.Time,
                                           RealType.Latitude,
                                           RealType.Longitude };

    RealTupleType tmpTuple;
    try {
      tmpTuple = new RealTupleType(typeList);
    } catch (VisADException ve) {
      System.err.println("Couldn't build temporary 5D tuple type");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    CoordinateSystem cSys5D;
    try {
      cSys5D = new FakeCoordinateSystem(tmpTuple, enUnits5D);
    } catch (VisADException ve) {
      System.err.println("Couldn't build 5D coordinate system");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    RealTupleType tuple5D;
    try {
      tuple5D = new RealTupleType(typeList, cSys5D, null);
    } catch (VisADException ve) {
      System.err.println("Couldn't build 5D tuple type");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    double[] firsts = new double[] { 1., 0., 2., 123., 42. };
    double[] lasts = new double[] { 2., 100., 16., 246., 49. };
    int[] lengths = new int[] { 3, 2, 3, 2, 3 };

    ErrorEstimate[] errors;
    errors = new ErrorEstimate[] {
      new ErrorEstimate(1.23, 0.04, foot),
      new ErrorEstimate(0.56, 7.8, pound),
      new ErrorEstimate(0.9, 0.9, SI.second),
      new ErrorEstimate(3.14, 1.59, CommonUnit.degree),
      new ErrorEstimate(5.2, 8.0, CommonUnit.degree)
        };

    Linear1DSet[] lset;
    try {
      lset = new Linear1DSet[5];
      lset[0] = new Linear1DSet(1., 2., 3);
      lset[1] = new Linear1DSet(0., 100., 2);
      lset[2] = new Linear1DSet(2., 16.4, 3);
      lset[3] = new Linear1DSet(123., 246., 2);
      lset[4] = new Linear1DSet(42., 49., 3);
    } catch (VisADException ve) {
      System.err.println("Couldn't build Linear1DSet arguments" +
                         " for ND set tests");
      ve.printStackTrace();
      lset = null;
    }

    try {
      list.add(new LinearNDSet(tuple5D, firsts, lasts, lengths));
      list.add(new LinearNDSet(tuple5D, firsts, lasts, lengths,
                               cSys5D, enUnits5D, errors));

      list.add(new LinearNDSet(tuple5D, lset));
      list.add(new LinearNDSet(tuple5D, lset, cSys5D, enUnits5D, errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build LinearNDSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }
  }

  private void fakeIrregularSets(ArrayList list)
  {
    Delaunay del;
    try {
      del = new DelaunayFast(new float[][] { { 1.f, 2.f, 3.f },
                                             { 1.f, 2.f, 3.f} });
    } catch (VisADException ve) {
      System.err.println("Couldn't build DelaunayFast");
      ve.printStackTrace();
      del = null;
    }

    float[][] data;
    ErrorEstimate[] errors;

    try {
      data = new float[][] { { 1.23f, 4.56f, 7.89f } };
      errors = new ErrorEstimate[] { new ErrorEstimate(1.23, 0.04, foot) };

      list.add(new IrregularSet(RealType.Altitude, data));
      list.add(new IrregularSet(RealType.XAxis, data, del));
      list.add(new IrregularSet(tuple1D, data, 1, cSys1D, enUnits1D,
                                errors, del));
    } catch (VisADException ve) {
      System.err.println("Couldn't build IrregularSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      list.add(new Irregular1DSet(RealTupleType.Time1DTuple, data));
      list.add(new Irregular1DSet(tuple1D, data, cSys1D, enUnits1D,
                                  errors));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Irregular1DSet");
      ve.printStackTrace();
      System.exit(1);
    }

    try {
      data = new float[][] { { 1.23f, 4.56f, 7.89f },
                             { 9.87f, 6.54f, 3.21f } };
      errors = new ErrorEstimate[] {
        new ErrorEstimate(4.56, 0.05, foot),
        new ErrorEstimate(7.89, 0.67, pound) };

      list.add(new Irregular2DSet(RealTupleType.LatitudeLongitudeTuple,
                                  data));
      list.add(new Irregular2DSet(tuple2D, data, cSys2D, enUnits2D,
                                  errors, del));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Irregular2DSet");
      ve.printStackTrace();
      System.exit(1);
    }

    try {
      data = new float[][] { { 1.23f, 4.56f, 7.89f },
                             { 9.87f, 6.54f, 3.21f },
                             { 5.43f, 2.19f, 8.76f } };
      errors = new ErrorEstimate[] {
        new ErrorEstimate(4.56, 0.05, foot),
        new ErrorEstimate(7.89, 0.67, pound),
        new ErrorEstimate(1.23, 1.23, SI.second) };

      list.add(new Irregular3DSet(RealTupleType.SpatialEarth3DTuple, data));
      list.add(new Irregular3DSet(tuple3D, data, cSys3D, enUnits3D,
                                  errors, del));
    } catch (VisADException ve) {
      System.err.println("Couldn't build Irregular3DSet");
      ve.printStackTrace();
      System.exit(1);
    }
  }

  private void fakeSampledSets(ArrayList list)
  {
    try {
      Real[] singles = new Real[] { new Real(0.123), new Real(1.234) };
      list.add(new SingletonSet(new RealTuple(singles)));

      singles = new Real[] { new Real(RealType.Time, 6.66),
                             new Real(RealType.Time, 9.99, SI.second) };
      list.add(new SingletonSet(new RealTuple(singles)));
    } catch (RemoteException re) {
      System.err.println("Couldn't build SingletonSet");
      re.printStackTrace();
      System.exit(1);
    } catch (VisADException ve) {
      System.err.println("Couldn't build SingletonSet");
      ve.printStackTrace();
      System.exit(1);
    }

    Integer1DSet iset;
    Linear1DSet lset;

    try {
      iset = new Integer1DSet(10);
    } catch (VisADException ve) {
      System.err.println("Couldn't build Integer1DSet for UnionSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      lset = new Linear1DSet(-15., 15., 10);
    } catch (VisADException ve) {
      System.err.println("Couldn't build Linear1DSet for UnionSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    SampledSet[] sets = new SampledSet[] { iset, lset };

    try {
      list.add(new UnionSet(sets));
    } catch (VisADException ve) {
      System.err.println("Couldn't build UnionSet");
      ve.printStackTrace();
      System.exit(1);
    }

    sets = new SampledSet[2];

    int[] lengths = new int[] { 3 };

    try {
      sets[0] = new GriddedSet(RealType.TimeInterval,
                               new float[][] { { 0f, 1f, 2f } }, lengths);
      sets[1] = new GriddedSet(RealType.TimeInterval,
                               new float[][] { { 6f, 5f, 4f } }, lengths);
    } catch (VisADException ve) {
      System.err.println("Couldn't build GriddedSets for ProductSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    UnionSet uset;
    try {
      uset = new UnionSet(sets);
    } catch (VisADException ve) {
      System.err.println("Couldn't build second UnionSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }
    list.add(uset);

    try {
      list.add(uset.product(sets[0]));
    } catch (VisADException ve) {
      System.err.println("Couldn't get product of UnionSet");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    fakeGriddedSets(list);
    fakeIrregularSets(list);
  }

  private void fakeSets(ArrayList list)
  {
    RealTupleType rttHgt;
    try {
      rttHgt = new RealTupleType(new RealType[] { meHgt }, cSys1D, null);
    } catch (VisADException ve) {
      System.err.println("Couldn't build RealTupleType for height!");
      ve.printStackTrace();
      System.exit(1);
      return;
    }

    try {
      list.add(new DoubleSet(RealType.Time));
      list.add(new FloatSet(rttHgt, cSys1D, null));
      list.add(new FloatSet(rttHgt, cSys1D, new Unit[] { SI.meter }));
      list.add(new FloatSet(RealType.XAxis));
      list.add(new FloatSet(rttHgt, cSys1D, null));
      list.add(new FloatSet(rttHgt, cSys1D, new Unit[] { SI.meter }));
      list.add(new List1DSet(new float[] { 1.1f, 2.2f, 3.3f }, rttHgt,
      cSys1D, null));
    } catch (VisADException ve) {
      ve.printStackTrace();
    }

    fakeSampledSets(list);
  }

  private void fakeTuples(ArrayList list)
  {
    TextType tt;
    try {
      tt = new TextType("Note");
    } catch (VisADException ve) {
      tt = TextType.Generic;
    }

    MathType[] mtypes = {RealType.Latitude, RealType.Longitude, tt};

    try {
      list.add(new Tuple(new TupleType(mtypes),
                         new Data[] {new Real(RealType.Latitude, -60.0),
                                     new Real(RealType.Longitude, 60.0),
                                     new Text(tt, "Some text")}));
    } catch (RemoteException re) {
    } catch (VisADException ve) {
    }

    try {
      Real[] vals = new Real[2];
      vals[0] = new Real(meHgt, (double )4.56);
      vals[1] = new Real(meWgt, (double )1.23);

      list.add(new RealTuple(tuple2D, vals, cSys2D));
    } catch (RemoteException re) {
      re.printStackTrace();
    } catch (VisADException ve) {
      ve.printStackTrace();
    }
  }

  DataImpl[] getList()
  {
    ArrayList list = new ArrayList();

    // add Text varieties
    list.add(new Text("g>a\"r&b<a'ge"));
    try {
      list.add(new Text(new TextType("'Ru&de=>Ty<pe\"")));
    } catch (VisADException ve) {
      ve.printStackTrace();
    }

    // add Real varieties
    list.add(new Real(RealType.XAxis, 123.456));
    try {
      list.add(new Real(RealType.Altitude, 123.456, SI.meter, 43.21));
    } catch (VisADException ve) {
      ve.printStackTrace();
    }
    try {
      list.add(new Real(RealType.TimeInterval, Double.NaN, SI.second, 43.21));
    } catch (VisADException ve) {
      ve.printStackTrace();
    }

    // add Tuple varieties
    fakeTuples(list);

    // add Set varieties
    fakeSets(list);

    // add Function varieties
    fakeFunctions(list);

    // contruct final list
    DataImpl[] dataList = new DataImpl[list.size()];
    for (int i = 0; i < dataList.length; i++) {
      dataList[i] = (DataImpl )list.get(i);
    }

    return dataList;
  }
}
