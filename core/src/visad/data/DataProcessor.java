/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data;

import visad.*;

/**
 * A hierarchy of methods used by
 * {@link visad.data.BaseDataProcessor BaseDataProcessor}
 * to write an arbitrary {@link visad.Data Data} object.
 */
public interface DataProcessor
{
  void process(DataImpl data, Object token)
    throws VisADException;
  void processDoubleSet(SetType type, CoordinateSystem cs, Unit[] units,
                        DoubleSet set, Object token)
    throws VisADException;
  void processFieldImpl(FunctionType type, Set set, FieldImpl fld,
                        Object token)
    throws VisADException;
  void processFlatField(FunctionType type, Set domainSet, CoordinateSystem cs,
                        CoordinateSystem[] rangeCS, Set[] rangeSets,
                        Unit[] units, FlatField fld, Object token)
    throws VisADException;
  void processFloatSet(SetType type, CoordinateSystem cs, Unit[] units,
                       FloatSet set, Object token)
    throws VisADException;
  void processGridded1DDoubleSet(SetType type, double[][] samples,
                                 int[] lengths, CoordinateSystem cs,
                                 Unit[] units, ErrorEstimate[] errors,
                                 Gridded1DDoubleSet set, Object token)
    throws VisADException;
  void processGridded2DDoubleSet(SetType type, double[][] samples,
                                 int[] lengths, CoordinateSystem cs,
                                 Unit[] units, ErrorEstimate[] errors,
                                 Gridded2DDoubleSet set, Object token)
    throws VisADException;
  void processGridded3DDoubleSet(SetType type, double[][] samples,
                                 int[] lengths, CoordinateSystem cs,
                                 Unit[] units, ErrorEstimate[] errors,
                                 Gridded3DDoubleSet set, Object token)
    throws VisADException;
  void processGridded1DSet(SetType type, float[][] samples, int[] lengths,
                           CoordinateSystem cs, Unit[] units,
                           ErrorEstimate[] errors, Gridded1DSet set,
                           Object token)
    throws VisADException;
  void processGridded2DSet(SetType type, float[][] samples, int[] lengths,
                           CoordinateSystem cs, Unit[] units,
                           ErrorEstimate[] errors, Gridded2DSet set,
                           Object token)
    throws VisADException;
  void processGridded3DSet(SetType type, float[][] samples, int[] lengths,
                           CoordinateSystem cs, Unit[] units,
                           ErrorEstimate[] errors, Gridded3DSet set,
                           Object token)
    throws VisADException;

  void processGriddedSet(SetType type, float[][] samples, int[] lengths,
                         CoordinateSystem cs, Unit[] units,
                         ErrorEstimate[] errors, GriddedSet set,
                         Object token)
    throws VisADException;
  void processInteger1DSet(SetType type, int[] lengths, CoordinateSystem cs,
                           Unit[] units, ErrorEstimate[] errors,
                           Integer1DSet set, Object token)
    throws VisADException;
  void processInteger2DSet(SetType type, int[] lengths, CoordinateSystem cs,
                           Unit[] units, ErrorEstimate[] errors,
                           Integer2DSet set, Object token)
    throws VisADException;
  void processInteger3DSet(SetType type, int[] lengths, CoordinateSystem cs,
                           Unit[] units, ErrorEstimate[] errors,
                           Integer3DSet set, Object token)
    throws VisADException;
  void processIntegerNDSet(SetType type, int[] lengths, CoordinateSystem cs,
                           Unit[] units, ErrorEstimate[] errors,
                           IntegerNDSet set, Object token)
    throws VisADException;
  void processIrregular1DSet(SetType type, float[][] samples,
                             CoordinateSystem cs, Unit[] units,
                             ErrorEstimate[] errors, Irregular1DSet set,
                             Object token)
    throws VisADException;
  void processIrregular2DSet(SetType type, float[][] samples,
                             CoordinateSystem cs, Unit[] units,
                             ErrorEstimate[] errors, Delaunay delaunay,
                             Irregular2DSet set, Object token)
    throws VisADException;
  void processIrregular3DSet(SetType type, float[][] samples,
                             CoordinateSystem cs, Unit[] units,
                             ErrorEstimate[] errors, Delaunay delaunay,
                             Irregular3DSet set, Object token)
    throws VisADException;
  void processIrregularSet(SetType type, float[][] samples,
                           CoordinateSystem cs, Unit[] units,
                           ErrorEstimate[] errors, Delaunay delaunay,
                           IrregularSet set, Object token)
    throws VisADException;

  void processLinear1DSet(SetType type, double[] firsts, double[] lasts,
                          int[] lengths, CoordinateSystem cs, Unit[] units,
                          ErrorEstimate[] errors, Linear1DSet set,
                          Object token)
    throws VisADException;
  void processLinear2DSet(SetType type, double[] firsts, double[] lasts,
                          int[] lengths, CoordinateSystem cs, Unit[] units,
                          ErrorEstimate[] errors, Linear2DSet set,
                          Object token)
    throws VisADException;
  void processLinear3DSet(SetType type, double[] firsts, double[] lasts,
                          int[] lengths, CoordinateSystem cs, Unit[] units,
                          ErrorEstimate[] errors, Linear3DSet set,
                          Object token)
    throws VisADException;
  void processLinearLatLonSet(SetType type, double[] firsts, double[] lasts,
                              int[] lengths, CoordinateSystem cs,
                              Unit[] units, ErrorEstimate[] errors,
                              LinearLatLonSet set, Object token)
    throws VisADException; 
  void processLinearNDSet(SetType type, double[] firsts, double[] lasts,
                          int[] lengths, CoordinateSystem cs, Unit[] units,
                          ErrorEstimate[] errors, LinearNDSet set,
                          Object token)
    throws VisADException;
  void processList1DSet(SetType type, float[] list, CoordinateSystem cs,
                        Unit[] units, List1DSet set, Object token)
    throws VisADException;
  void processProductSet(SetType type, SampledSet[] sets, CoordinateSystem cs,
                         Unit[] units, ErrorEstimate[] errors, ProductSet set,
                         Object token)
    throws VisADException;
  void processReal(RealType type, double value, Unit unit,
                   ErrorEstimate error, Real real, Object token)
    throws VisADException;
  void processRealTuple(RealTupleType type, Real[] components,
                        CoordinateSystem cs, RealTuple rt, Object token)
    throws VisADException;
  void processSampledSet(SetType st, int manifold_dimension,
                         CoordinateSystem cs, Unit[] units,
                         ErrorEstimate[] errors, SampledSet set, Object token)
    throws VisADException;
  void processSimpleSet(SetType st, int manifold_dimension,
                        CoordinateSystem cs, Unit[] units,
                        ErrorEstimate[] errors, SimpleSet set, Object token)
    throws VisADException;
  void processSingletonSet(RealTuple sample, CoordinateSystem cs,
                           Unit[] units, ErrorEstimate[] errors,
                           SingletonSet set, Object token)
    throws VisADException;
  void processText(TextType type, String value, boolean missing, Text text,
                   Object token)
    throws VisADException;
  void processTuple(TupleType type, Data[] components, Tuple t,
                    Object token)
    throws VisADException;
  void processUnionSet(SetType type, SampledSet[] sets, UnionSet set,
                       Object token)
    throws VisADException;
  void processUnknownData(DataImpl data, Object token)
    throws VisADException;
}
