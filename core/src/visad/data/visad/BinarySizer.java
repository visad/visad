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

package visad.data.visad;

import visad.*;

import visad.data.BaseDataProcessor;

import visad.data.visad.object.*;

public class BinarySizer
  extends BaseDataProcessor
{
  private BinarySize size;

  public BinarySizer()
  {
    size = new BinarySize();
  }

  public final int getSize() { return size.get(); }

  public void processDoubleSet(SetType type, CoordinateSystem cs,
                               Unit[] units, DoubleSet set, Object token)
    throws VisADException
  {
    int bytes = BinarySimpleSet.computeBytes(cs, units);
    size.add(bytes);
  }

  public void processFieldImpl(FunctionType type, Set set, FieldImpl fld,
                               Object token)
    throws VisADException
  {
    int bytes = BinaryFieldImpl.computeBytes(fld);
    size.add(bytes);
  }

  public void processFlatField(FunctionType type, Set domainSet,
                               CoordinateSystem cs,
                               CoordinateSystem[] rangeCS, Set[] rangeSets,
                               Unit[] units, FlatField fld, Object token)
    throws VisADException
  {
    int bytes = BinaryFlatField.computeBytes(domainSet, cs, rangeCS,
                                             rangeSets, units, fld);
    size.add(bytes);
  }

  public void processFloatSet(SetType type, CoordinateSystem cs,
                              Unit[] units, FloatSet set, Object token)
    throws VisADException
  {
    int bytes = BinarySimpleSet.computeBytes(cs, units);
    size.add(bytes);
  }

  public void processGridded1DDoubleSet(SetType type, double[][] samples,
                                        int[] lengths, CoordinateSystem cs,
                                        Unit[] units, ErrorEstimate[] errors,
                                        Gridded1DDoubleSet set, Object token)
    throws VisADException
  {
    int bytes = BinaryGriddedDoubleSet.computeBytes(samples, lengths, cs,
                                                    units, errors);
    size.add(bytes);
  }

  public void processGridded2DDoubleSet(SetType type, double[][] samples,
                                        int[] lengths, CoordinateSystem cs,
                                        Unit[] units, ErrorEstimate[] errors,
                                        Gridded2DDoubleSet set, Object token)
    throws VisADException
  {
    int bytes = BinaryGriddedDoubleSet.computeBytes(samples, lengths, cs,
                                                    units, errors);
    size.add(bytes);
  }

  public void processGridded3DDoubleSet(SetType type, double[][] samples,
                                        int[] lengths, CoordinateSystem cs,
                                        Unit[] units, ErrorEstimate[] errors,
                                        Gridded3DDoubleSet set, Object token)
    throws VisADException
  {
    int bytes = BinaryGriddedDoubleSet.computeBytes(samples, lengths, cs, units,
                                                    errors);
    size.add(bytes);
  }

  public void processGridded1DSet(SetType type, float[][] samples,
                                  int[] lengths, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  Gridded1DSet set, Object token)
    throws VisADException
  {
    int bytes = BinaryGriddedSet.computeBytes(samples, lengths, cs, units,
                                              errors);
    size.add(bytes);
  }

  public void processGridded2DSet(SetType type, float[][] samples,
                                  int[] lengths, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  Gridded2DSet set, Object token)
    throws VisADException
  {
    int bytes = BinaryGriddedSet.computeBytes(samples, lengths, cs, units,
                                              errors);
    size.add(bytes);
  }

  public void processGridded3DSet(SetType type, float[][] samples,
                                  int[] lengths, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  Gridded3DSet set, Object token)
    throws VisADException
  {
    int bytes = BinaryGriddedSet.computeBytes(samples, lengths, cs, units,
                                              errors);
    size.add(bytes);
  }

  public void processGriddedSet(SetType type, float[][] samples,
                                int[] lengths, CoordinateSystem cs,
                                Unit[] units, ErrorEstimate[] errors,
                                GriddedSet set, Object token)
    throws VisADException
  {
    int bytes = BinaryGriddedSet.computeBytes(samples, lengths, cs, units,
                                              errors);
    size.add(bytes);
  }

  public void processInteger1DSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Integer1DSet set,
                                  Object token)
    throws VisADException
  {
    int bytes = BinaryIntegerSet.computeBytes(true, lengths, null, cs, units,
                                              errors);
    size.add(bytes);
  }

  public void processInteger2DSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Integer2DSet set,
                                  Object token)
    throws VisADException
  {
    Integer1DSet[] comps = BinaryIntegerSet.getComponents(set);

    boolean matchedTypes = BinaryIntegerSet.hasMatchedTypes(type, comps);

    int bytes = BinaryIntegerSet.computeBytes(matchedTypes, lengths, comps,
                                              cs, units, errors);
    size.add(bytes);
  }

  public void processInteger3DSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Integer3DSet set,
                                  Object token)
    throws VisADException
  {
    Integer1DSet[] comps = BinaryIntegerSet.getComponents(set);

    boolean matchedTypes = BinaryIntegerSet.hasMatchedTypes(type, comps);

    int bytes = BinaryIntegerSet.computeBytes(matchedTypes, lengths, comps,
                                              cs, units, errors);
    size.add(bytes);
  }

  public void processIntegerNDSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, IntegerNDSet set,
                                  Object token)
    throws VisADException
  {
    Integer1DSet[] comps = BinaryIntegerSet.getComponents(set);

    boolean matchedTypes = BinaryIntegerSet.hasMatchedTypes(type, comps);

    int bytes = BinaryIntegerSet.computeBytes(matchedTypes, lengths, comps,
                                              cs, units, errors);
    size.add(bytes);
  }

  public void processIrregular1DSet(SetType type, float[][] samples,
                                    CoordinateSystem cs, Unit[] units,
                                    ErrorEstimate[] errors,
                                    Irregular1DSet set, Object token)
    throws VisADException
  {
    int bytes = BinaryIrregularSet.computeBytes(samples, cs, units, errors,
                                                null);
    size.add(bytes);
  }

  public void processIrregular2DSet(SetType type, float[][] samples,
                                    CoordinateSystem cs, Unit[] units,
                                    ErrorEstimate[] errors, Delaunay delaunay,
                                    Irregular2DSet set, Object token)
    throws VisADException
  {
    int bytes = BinaryIrregularSet.computeBytes(samples, cs, units, errors,
                                                delaunay);
    size.add(bytes);
  }

  public void processIrregular3DSet(SetType type, float[][] samples,
                                    CoordinateSystem cs, Unit[] units,
                                    ErrorEstimate[] errors, Delaunay delaunay,
                                    Irregular3DSet set, Object token)
    throws VisADException
  {
    int bytes = BinaryIrregularSet.computeBytes(samples, cs, units, errors,
                                                delaunay);
    size.add(bytes);
  }

  public void processIrregularSet(SetType type, float[][] samples,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Delaunay delaunay,
                                  IrregularSet set, Object token)
    throws VisADException
  {
    int bytes = BinaryIrregularSet.computeBytes(samples, cs, units, errors,
                                                delaunay);
    size.add(bytes);
  }

  public void processLinear1DSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Linear1DSet set,
                                 Object token)
    throws VisADException
  {
    int bytes = BinaryLinearSet.computeBytes(true, firsts, lasts, lengths,
                                             null, cs, units, errors);
    size.add(bytes);
  }

  public void processLinear2DSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Linear2DSet set,
                                 Object token)
    throws VisADException
  {
    Linear1DSet[] comps = new Linear1DSet[2];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = set.getLinear1DComponent(i);
    }

    boolean matchedTypes = BinaryLinearSet.hasMatchedTypes(type, comps);

    int bytes = BinaryLinearSet.computeBytes(matchedTypes, firsts, lasts,
                                             lengths, comps, cs, units,
                                             errors);
    size.add(bytes);
  }

  public void processLinear3DSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Linear3DSet set,
                                 Object token)
    throws VisADException
  {
    Linear1DSet[] comps = new Linear1DSet[3];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = set.getLinear1DComponent(i);
    }

    boolean matchedTypes = BinaryLinearSet.hasMatchedTypes(type, comps);

    int bytes = BinaryLinearSet.computeBytes(matchedTypes, firsts, lasts,
                                             lengths, comps, cs, units,
                                             errors);
    size.add(bytes);
  }

  public void processLinearLatLonSet(SetType type, double[] firsts,
                                     double[] lasts, int[] lengths,
                                     CoordinateSystem cs, Unit[] units,
                                     ErrorEstimate[] errors,
                                     LinearLatLonSet set, Object token)
    throws VisADException
  {
    Linear1DSet[] comps = new Linear1DSet[2];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = set.getLinear1DComponent(i);
    }

    boolean matchedTypes = BinaryLinearSet.hasMatchedTypes(type, comps);

    int bytes = BinaryLinearSet.computeBytes(matchedTypes, firsts, lasts,
                                             lengths, comps, cs, units,
                                             errors);
    size.add(bytes);
  }

  public void processLinearNDSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, LinearNDSet set,
                                 Object token)
    throws VisADException
  {
    Linear1DSet[] comps = new Linear1DSet[set.getDimension()];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = set.getLinear1DComponent(i);
    }

    boolean matchedTypes = BinaryLinearSet.hasMatchedTypes(type, comps);

    int bytes = BinaryLinearSet.computeBytes(matchedTypes, firsts, lasts,
                                             lengths, comps, cs, units,
                                             errors);
    size.add(bytes);
  }

  public void processList1DSet(SetType type, float[] list,
                               CoordinateSystem cs, Unit[] units,
                               List1DSet set, Object token)
    throws VisADException
  {
    int bytes = BinaryList1DSet.computeBytes(list, cs, units);
    size.add(bytes);
  }

  public void processProductSet(SetType type, SampledSet[] sets,
                                CoordinateSystem cs, Unit[] units,
                                ErrorEstimate[] errors, ProductSet set,
                                Object token)
    throws VisADException
  {
    int bytes = BinaryProductSet.computeBytes(sets, cs, units, errors);
    size.add(bytes);
  }

  public void processReal(RealType type, double value, Unit unit,
                          ErrorEstimate error, Real real, Object token)
    throws VisADException
  {
    int bytes = BinaryReal.computeBytes(unit, error);
    size.add(bytes);
  }

  public void processRealTuple(RealTupleType type, Real[] components,
                               CoordinateSystem cs, RealTuple rt,
                               Object token)
    throws VisADException
  {
    boolean trivialTuple = BinaryRealTuple.isTrivialTuple(type, components);

    int bytes = BinaryRealTuple.computeBytes(components, cs, trivialTuple);
    size.add(bytes);
  }

  public void processSampledSet(SetType st, int manifold_dimension,
                                CoordinateSystem cs, Unit[] units,
                                ErrorEstimate[] errors, SampledSet set,
                                Object token)
    throws VisADException
  {
    throw new UnimplementedException();
  }

  public void processSimpleSet(SetType st, int manifold_dimension,
                               CoordinateSystem cs, Unit[] units,
                               ErrorEstimate[] errors, SimpleSet set,
                               Object token)
    throws VisADException
  {
    int bytes = BinarySimpleSet.computeBytes(cs, units);
    size.add(bytes);
  }

  public void processSingletonSet(RealTuple sample, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  SingletonSet set, Object token)
    throws VisADException
  {
    RealTupleType sampleType = (RealTupleType )sample.getType();
    Real[] sampleReals = BinarySingletonSet.getSampleReals(sample);

    int bytes = BinarySingletonSet.computeBytes(sampleType, sampleReals, cs,
                                                units, errors);
    size.add(bytes);
  }

  public void processText(TextType type, String value, boolean missing,
                          Text text, Object token)
    throws VisADException
  {
    int bytes = BinaryText.computeBytes(value);
    size.add(bytes);
  }

  public void processTuple(TupleType type, Data[] components, Tuple t,
                           Object token)
    throws VisADException
  {
    int bytes = BinaryTuple.computeBytes(components);
    size.add(bytes);
  }

  public void processUnionSet(SetType type, SampledSet[] sets, UnionSet set,
                              Object token)
    throws VisADException
  {
    int bytes = BinaryUnionSet.computeBytes(sets);
    size.add(bytes);
  }

  public void processUnknownData(DataImpl data, Object token)
    throws VisADException
  {
    int bytes = BinaryUnknown.computeBytes(data);
    size.add(bytes);
  }

  public final void reset() { size.reset(); }

  public final void setSize(int newSize) { size.set(newSize); }
}
