package visad.data;

import visad.*;

public class EmptyDataProcessor
  extends BaseDataProcessor
{
  public EmptyDataProcessor() { }

  public void processDoubleSet(SetType type, CoordinateSystem cs,
                               Unit[] units, DoubleSet set)
    throws VisADException
  {
    System.out.println("processDoubleSet");
    throw new UnimplementedException();
  }

  public void processFieldImpl(FunctionType type, Set set, FieldImpl fld)
    throws VisADException
  {
    System.out.println("processFieldImpl");
    throw new UnimplementedException();
  }

  public void processFlatField(FunctionType type, Set domainSet,
                               CoordinateSystem cs,
                               CoordinateSystem[] rangeCS, Set[] rangeSets,
                               Unit[] units, FlatField fld)
    throws VisADException
  {
    System.out.println("processFlatField");
    throw new UnimplementedException();
  }

  public void processFloatSet(SetType type, CoordinateSystem cs,
                              Unit[] units, FloatSet set)
    throws VisADException
  {
    System.out.println("processFloatSet");
    throw new UnimplementedException();
  }

  public void processGridded1DDoubleSet(SetType type, double[][] samples,
                                        int[] lengths, CoordinateSystem cs,
                                        Unit[] units, ErrorEstimate[] errors,
                                        Gridded1DDoubleSet set)
    throws VisADException
  {
    System.out.println("processGridded1DDoubleSet");
    throw new UnimplementedException();
  }

  public void processGridded2DDoubleSet(SetType type, double[][] samples,
                                        int[] lengths, CoordinateSystem cs,
                                        Unit[] units, ErrorEstimate[] errors,
                                        Gridded2DDoubleSet set)
    throws VisADException
  {
    System.out.println("processGridded2DDoubleSet");
    throw new UnimplementedException();
  }

  public void processGridded3DDoubleSet(SetType type, double[][] samples,
                                        int[] lengths, CoordinateSystem cs,
                                        Unit[] units, ErrorEstimate[] errors,
                                        Gridded3DDoubleSet set)
    throws VisADException
  {
    System.out.println("processGridded3DDoubleSet");
    throw new UnimplementedException();
  }

  public void processGridded1DSet(SetType type, float[][] samples,
                                  int[] lengths, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  Gridded1DSet set)
    throws VisADException
  {
    System.out.println("processGridded1DSet");
    throw new UnimplementedException();
  }

  public void processGridded2DSet(SetType type, float[][] samples,
                                  int[] lengths, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  Gridded2DSet set)
    throws VisADException
  {
    System.out.println("processGridded2DSet");
    throw new UnimplementedException();
  }

  public void processGridded3DSet(SetType type, float[][] samples,
                                  int[] lengths, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  Gridded3DSet set)
    throws VisADException
  {
    System.out.println("processGridded3DSet");
    throw new UnimplementedException();
  }

  public void processGriddedSet(SetType type, float[][] samples,
                                int[] lengths, CoordinateSystem cs,
                                Unit[] units, ErrorEstimate[] errors,
                                GriddedSet set)
    throws VisADException
  {
    System.out.println("processGriddedSet");
    throw new UnimplementedException();
  }

  public void processInteger1DSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Integer1DSet set)
    throws VisADException
  {
    System.out.println("processInteger1DSet");
    throw new UnimplementedException();
  }

  public void processInteger2DSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Integer2DSet set)
    throws VisADException
  {
    System.out.println("processInteger2DSet");
    throw new UnimplementedException();
  }

  public void processInteger3DSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Integer3DSet set)
    throws VisADException
  {
    System.out.println("processInteger3DSet");
    throw new UnimplementedException();
  }

  public void processIntegerNDSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, IntegerNDSet set)
    throws VisADException
  {
    System.out.println("processIntegerNDSet");
    throw new UnimplementedException();
  }

  public void processIrregular1DSet(SetType type, float[][] samples,
                                    CoordinateSystem cs, Unit[] units,
                                    ErrorEstimate[] errors, Irregular1DSet set)
    throws VisADException
  {
    System.out.println("processIrregular1DSet");
    throw new UnimplementedException();
  }

  public void processIrregular2DSet(SetType type, float[][] samples,
                                    CoordinateSystem cs, Unit[] units,
                                    ErrorEstimate[] errors, Delaunay delaunay,
                                    Irregular2DSet set)
    throws VisADException
  {
    System.out.println("processIrregular2DSet");
    throw new UnimplementedException();
  }

  public void processIrregular3DSet(SetType type, float[][] samples,
                                    CoordinateSystem cs, Unit[] units,
                                    ErrorEstimate[] errors, Delaunay delaunay,
                                    Irregular3DSet set)
    throws VisADException
  {
    System.out.println("processIrregular3DSet");
    throw new UnimplementedException();
  }

  public void processIrregularSet(SetType type, float[][] samples,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Delaunay delaunay,
                                  IrregularSet set)
    throws VisADException
  {
    System.out.println("processIrregularSet");
    throw new UnimplementedException();
  }

  public void processLinear1DSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Linear1DSet set)
    throws VisADException
  {
    System.out.println("processLinear1DSet");
    throw new UnimplementedException();
  }

  public void processLinear2DSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Linear2DSet set)
    throws VisADException
  {
    System.out.println("processLinear2DSet");
    throw new UnimplementedException();
  }

  public void processLinear3DSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Linear3DSet set)
    throws VisADException
  {
    System.out.println("processLinear3DSet");
    throw new UnimplementedException();
  }

  public void processLinearLatLonSet(SetType type, double[] firsts,
                                     double[] lasts, int[] lengths,
                                     CoordinateSystem cs, Unit[] units,
                                     ErrorEstimate[] errors,
                                     LinearLatLonSet set)
    throws VisADException
  {
    System.out.println("processLinearLatLonSet");
    throw new UnimplementedException();
  }

  public void processLinearNDSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, LinearNDSet set)
    throws VisADException
  {
    System.out.println("processLinearNDSet");
    throw new UnimplementedException();
  }

  public void processList1DSet(SetType type, float[] list,
                               CoordinateSystem cs, Unit[] units,
                               List1DSet set)
    throws VisADException
  {
    System.out.println("processList1DSet");
    throw new UnimplementedException();
  }

  public void processProductSet(SetType type, SampledSet[] sets,
                                CoordinateSystem cs, Unit[] units,
                                ErrorEstimate[] errors, ProductSet set)
    throws VisADException
  {
    System.out.println("processProductSet");
    throw new UnimplementedException();
  }

  public void processReal(RealType type, double value, Unit unit,
                          ErrorEstimate error, Real real)
    throws VisADException
  {
    System.out.println("processReal");
    throw new UnimplementedException();
  }

  public void processRealTuple(RealTupleType type, Real[] components,
                               CoordinateSystem cs, RealTuple rt)
    throws VisADException
  {
    System.out.println("processRealTuple");
    throw new UnimplementedException();
  }

  public void processSampledSet(SetType st, int manifold_dimension,
                                CoordinateSystem cs, Unit[] units,
                                ErrorEstimate[] errors, SampledSet set)
    throws VisADException
  {
    System.out.println("processSampledSet");
    throw new UnimplementedException();
  }

  public void processSimpleSet(SetType st, int manifold_dimension,
                               CoordinateSystem cs, Unit[] units,
                               ErrorEstimate[] errors, SimpleSet set)
    throws VisADException
  {
    System.out.println("processSimpleSet");
    throw new UnimplementedException();
  }

  public void processSingletonSet(RealTuple sample, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  SingletonSet set)
    throws VisADException
  {
    System.out.println("processSingletonSet");
    throw new UnimplementedException();
  }

  public void processText(TextType type, String value, boolean missing,
                          Text text)
    throws VisADException
  {
    System.out.println("processText");
    throw new UnimplementedException();
  }

  public void processTuple(TupleType type, Data[] components, Tuple t)
    throws VisADException
  {
    System.out.println("processTuple");
    throw new UnimplementedException();
  }

  public void processUnionSet(SetType type, SampledSet[] sets, UnionSet set)
    throws VisADException
  {
    System.out.println("processUnionSet");
    throw new UnimplementedException();
  }

  public void processUnknownData(DataImpl data)
    throws VisADException
  {
    System.out.println("processUnknownData");
    throw new UnimplementedException();
  }
}
