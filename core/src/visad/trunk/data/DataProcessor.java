package visad.data;

import visad.*;

public interface DataProcessor
{
  void process(DataImpl data)
    throws VisADException;
  void processDoubleSet(SetType type, CoordinateSystem cs, Unit[] units,
                        DoubleSet set)
    throws VisADException;
  void processFieldImpl(FunctionType type, Set set, FieldImpl fld)
    throws VisADException;
  void processFlatField(FunctionType type, Set domainSet, CoordinateSystem cs,
                        CoordinateSystem[] rangeCS, Set[] rangeSets,
                        Unit[] units, FlatField fld)
    throws VisADException;
  void processFloatSet(SetType type, CoordinateSystem cs, Unit[] units,
                       FloatSet set)
    throws VisADException;
  void processGridded1DDoubleSet(SetType type, double[][] samples,
                                 int[] lengths, CoordinateSystem cs,
                                 Unit[] units, ErrorEstimate[] errors,
                                 Gridded1DDoubleSet set)
    throws VisADException;
  void processGridded2DDoubleSet(SetType type, double[][] samples,
                                 int[] lengths, CoordinateSystem cs,
                                 Unit[] units, ErrorEstimate[] errors,
                                 Gridded2DDoubleSet set)
    throws VisADException;
  void processGridded3DDoubleSet(SetType type, double[][] samples,
                                 int[] lengths, CoordinateSystem cs,
                                 Unit[] units, ErrorEstimate[] errors,
                                 Gridded3DDoubleSet set)
    throws VisADException;
  void processGridded1DSet(SetType type, float[][] samples, int[] lengths,
                           CoordinateSystem cs, Unit[] units,
                           ErrorEstimate[] errors, Gridded1DSet set)
    throws VisADException;
  void processGridded2DSet(SetType type, float[][] samples, int[] lengths,
                           CoordinateSystem cs, Unit[] units,
                           ErrorEstimate[] errors, Gridded2DSet set)
    throws VisADException;
  void processGridded3DSet(SetType type, float[][] samples, int[] lengths,
                           CoordinateSystem cs, Unit[] units,
                           ErrorEstimate[] errors, Gridded3DSet set)
    throws VisADException;

  void processGriddedSet(SetType type, float[][] samples, int[] lengths,
                         CoordinateSystem cs, Unit[] units,
                         ErrorEstimate[] errors, GriddedSet set)
    throws VisADException;
  void processInteger1DSet(SetType type, int[] lengths, CoordinateSystem cs,
                           Unit[] units, ErrorEstimate[] errors,
                           Integer1DSet set)
    throws VisADException;
  void processInteger2DSet(SetType type, int[] lengths, CoordinateSystem cs,
                           Unit[] units, ErrorEstimate[] errors,
                           Integer2DSet set)
    throws VisADException;
  void processInteger3DSet(SetType type, int[] lengths, CoordinateSystem cs,
                           Unit[] units, ErrorEstimate[] errors,
                           Integer3DSet set)
    throws VisADException;
  void processIntegerNDSet(SetType type, int[] lengths, CoordinateSystem cs,
                           Unit[] units, ErrorEstimate[] errors,
                           IntegerNDSet set)
    throws VisADException;
  void processIrregular1DSet(SetType type, float[][] samples,
                             CoordinateSystem cs, Unit[] units,
                             ErrorEstimate[] errors, Irregular1DSet set)
    throws VisADException;
  void processIrregular2DSet(SetType type, float[][] samples,
                             CoordinateSystem cs, Unit[] units,
                             ErrorEstimate[] errors, Delaunay delaunay,
                             Irregular2DSet set)
    throws VisADException;
  void processIrregular3DSet(SetType type, float[][] samples,
                             CoordinateSystem cs, Unit[] units,
                             ErrorEstimate[] errors, Delaunay delaunay,
                             Irregular3DSet set)
    throws VisADException;
  void processIrregularSet(SetType type, float[][] samples,
                           CoordinateSystem cs, Unit[] units,
                           ErrorEstimate[] errors, Delaunay delaunay,
                           IrregularSet set)
    throws VisADException;

  void processLinear1DSet(SetType type, double[] firsts, double[] lasts,
                          int[] lengths, CoordinateSystem cs, Unit[] units,
                          ErrorEstimate[] errors, Linear1DSet set)
    throws VisADException;
  void processLinear2DSet(SetType type, double[] firsts, double[] lasts,
                          int[] lengths, CoordinateSystem cs, Unit[] units,
                          ErrorEstimate[] errors, Linear2DSet set)
    throws VisADException;
  void processLinear3DSet(SetType type, double[] firsts, double[] lasts,
                          int[] lengths, CoordinateSystem cs, Unit[] units,
                          ErrorEstimate[] errors, Linear3DSet set)
    throws VisADException;
  void processLinearLatLonSet(SetType type, double[] firsts, double[] lasts,
                              int[] lengths, CoordinateSystem cs,
                              Unit[] units, ErrorEstimate[] errors,
                              LinearLatLonSet set)
    throws VisADException; 
  void processLinearNDSet(SetType type, double[] firsts, double[] lasts,
                          int[] lengths, CoordinateSystem cs, Unit[] units,
                          ErrorEstimate[] errors, LinearNDSet set)
    throws VisADException;
  void processList1DSet(SetType type, float[] list, CoordinateSystem cs,
                        Unit[] units, List1DSet set)
    throws VisADException;
  void processProductSet(SetType type, SampledSet[] sets, CoordinateSystem cs,
                         Unit[] units, ErrorEstimate[] errors, ProductSet set)
    throws VisADException;
  void processReal(RealType type, double value, Unit unit,
                   ErrorEstimate error, Real real)
    throws VisADException;
  void processRealTuple(RealTupleType type, Real[] components,
                        CoordinateSystem cs, RealTuple rt)
    throws VisADException;
  void processSampledSet(SetType st, int manifold_dimension,
                         CoordinateSystem cs, Unit[] units,
                         ErrorEstimate[] errors, SampledSet set)
    throws VisADException;
  void processSimpleSet(SetType st, int manifold_dimension,
                        CoordinateSystem cs, Unit[] units,
                        ErrorEstimate[] errors, SimpleSet set)
    throws VisADException;
  void processSingletonSet(RealTuple sample, CoordinateSystem cs,
                           Unit[] units, ErrorEstimate[] errors,
                           SingletonSet set)
    throws VisADException;
  void processText(TextType type, String value, boolean missing, Text text)
    throws VisADException;
  void processTuple(TupleType type, Data[] components, Tuple t)
    throws VisADException;
  void processUnionSet(SetType type, SampledSet[] sets, UnionSet set)
    throws VisADException;
  void processUnknownData(DataImpl data)
    throws VisADException;
}
