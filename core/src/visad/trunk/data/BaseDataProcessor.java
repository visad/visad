package visad.data;

import visad.*;

public abstract class BaseDataProcessor
  implements DataProcessor
{
  public void process(DataImpl di)
    throws VisADException
  {
    MathType mt = di.getType();

    boolean done = false;
    if (mt instanceof ScalarType) {
      try {
        if (mt instanceof TextType) {
          Text t = (Text )di;

          processText((TextType )t.getType(), t.getValue(), t.isMissing(), t);
          done = true;
        } else if (mt instanceof RealType) {
          Real r = (Real )di;

          processReal((RealType )r.getType(), r.getValue(), r.getUnit(),
                      r.getError(), r);
          done = true;
        }
      } catch (UnimplementedException ue) {
      }
    } else if (mt instanceof TupleType) {
      TupleType tt = (TupleType )mt;
      Tuple ti = (Tuple )di;

      Data[] comps = ti.getComponents();

      if (tt instanceof RealTupleType) {
        RealTuple rti = (RealTuple )ti;
        RealTupleType rtt = (RealTupleType )tt;

        if (comps != null) {
          Real[] reals = new Real[comps.length];
          for (int i = 0; i < comps.length; i++) {
            reals[i] = (Real )comps[i];
          }

          try {
            processRealTuple(rtt, reals, rti.getCoordinateSystem(), rti);
            done = true;
          } catch (UnimplementedException ue) {
          }
        }
      }

      if (!done) {
        try {
          processTuple(tt, comps, ti);
          done = true;
        } catch (UnimplementedException ue) {
        }
      }
    } else if (mt instanceof SetType) {
      SetType st = (SetType )mt;

      try {
        if (di instanceof SampledSet) {
          processSampledSet(st, (SampledSet )di);
          done = true;
        } else if (di instanceof SimpleSet) {
          processSimpleSet(st, (SimpleSet )di);
          done = true;
        }
      } catch (UnimplementedException ue) {
      }

    } else if (mt instanceof FunctionType) {
      FunctionType ft = (FunctionType )mt;

      try {
        if (di instanceof FlatField) {
          FlatField ff = (FlatField )di;

          CoordinateSystem cs = null;
          CoordinateSystem[] rangeCS = null;

          if (ft.getReal()) {
            cs = ff.getRangeCoordinateSystem()[0];
          } else {
            MathType rt = ft.getRange();
            final int dim = ((TupleType )ft.getRange()).getDimension();

            rangeCS = new CoordinateSystem[dim];

            for (int i = 0; i < dim; i++) {
              rangeCS[i] = ff.getRangeCoordinateSystem(i)[0];
            }
          }

          processFlatField(ft, ff.getDomainSet(), cs, rangeCS,
                           ff.getRangeSets(), ff.getDefaultRangeUnits(), ff);
          done = true;
        } else if (di instanceof FieldImpl) {
          FieldImpl fi = (FieldImpl )di;

          processFieldImpl(ft, fi.getDomainSet(), fi);
          done = true;
        }
      } catch (UnimplementedException ue) {
      }
    }

    if (!done) {
      try {
        processUnknownData(di);
      } catch (UnimplementedException ue) {
        throw new UnimplementedException("Couldn't process " +
                                         di.getClass().getName() +
                                         " in " + getClass().getName());
      }
    }
  }

  public abstract void processDoubleSet(SetType type, CoordinateSystem cs,
                                        Unit[] units, DoubleSet set)
    throws VisADException;

  public abstract void processFieldImpl(FunctionType type, Set set,
                                        FieldImpl fld)
    throws VisADException;

  public abstract void processFlatField(FunctionType type, Set domainSet,
                                        CoordinateSystem cs,
                                        CoordinateSystem[] rangeCS,
                                        Set[] rangeSets, Unit[] units,
                                        FlatField fld)
    throws VisADException;

  public abstract void processFloatSet(SetType type, CoordinateSystem cs,
                                       Unit[] units, FloatSet set)
    throws VisADException;

  public abstract void processGridded1DDoubleSet(SetType type,
                                                 double[][] samples,
                                                 int[] lengths,
                                                 CoordinateSystem cs,
                                                 Unit[] units,
                                                 ErrorEstimate[] errors,
                                                 Gridded1DDoubleSet set)
    throws VisADException;

  public abstract void processGridded2DDoubleSet(SetType type,
                                                 double[][] samples,
                                                 int[] lengths,
                                                 CoordinateSystem cs,
                                                 Unit[] units,
                                                 ErrorEstimate[] errors,
                                                 Gridded2DDoubleSet set)
    throws VisADException;

  public abstract void processGridded3DDoubleSet(SetType type,
                                                 double[][] samples,
                                                 int[] lengths,
                                                 CoordinateSystem cs,
                                                 Unit[] units,
                                                 ErrorEstimate[] errors,
                                                 Gridded3DDoubleSet set)
    throws VisADException;

  public abstract void processGridded1DSet(SetType type, float[][] samples,
                                           int[] lengths, CoordinateSystem cs,
                                           Unit[] units,
                                           ErrorEstimate[] errors,
                                           Gridded1DSet set)
    throws VisADException;

  private void processGridded1DSet(SetType st, Gridded1DSet set)
    throws VisADException
  {
    int[] lengths = set.getLengths();
    CoordinateSystem cs = set.getCoordinateSystem();
    Unit[] units = set.getSetUnits();
    ErrorEstimate[] errors = set.getSetErrors();

    boolean done = false;
    try {
      if (set instanceof Gridded1DDoubleSet) {
        Gridded1DDoubleSet dset = (Gridded1DDoubleSet )set;

        processGridded1DDoubleSet(st, dset.getDoubles(), lengths, cs, units,
                                  errors, dset);
        done = true;
      } else if (set instanceof Integer1DSet) {
        processInteger1DSet(st, lengths, cs, units, errors,
                            (Integer1DSet )set);
        done = true;
      } else if (set instanceof Linear1DSet) {
        Linear1DSet lset = (Linear1DSet )set;

        double[] firsts = new double[] { lset.getFirst() };
        double[] lasts = new double[] { lset.getLast() };

        processLinear1DSet(st, firsts, lasts, lengths, cs, units, errors,
                           lset);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processGridded1DSet(st, set.getSamples(), set.getLengths(),
                          set.getCoordinateSystem(), set.getSetUnits(),
                          set.getSetErrors(), set);
    }
  }

  public abstract void processGridded2DSet(SetType type, float[][] samples,
                                           int[] lengths, CoordinateSystem cs,
                                           Unit[] units,
                                           ErrorEstimate[] errors,
                                           Gridded2DSet set)
    throws VisADException;

  private void processGridded2DSet(SetType st, Gridded2DSet set)
    throws VisADException
  {
    int[] lengths = set.getLengths();
    CoordinateSystem cs = set.getCoordinateSystem();
    Unit[] units = set.getSetUnits();
    ErrorEstimate[] errors = set.getSetErrors();

    boolean done = false;
    try {
      if (set instanceof Gridded2DDoubleSet) {
        Gridded2DDoubleSet dset = (Gridded2DDoubleSet )set;

        processGridded2DDoubleSet(st, dset.getDoubles(), lengths, cs, units,
                                  errors, dset);
        done = true;
      } else if (set instanceof Integer2DSet) {
        processInteger2DSet(st, lengths, cs, units, errors,
                            (Integer2DSet )set);
        done = true;
      } else if (set instanceof Linear2DSet) {
        Linear2DSet lset = (Linear2DSet )set;

        double[] firsts = new double[2];
        double[] lasts = new double[2];
        for (int i = 0; i < 2; i++) {
          Linear1DSet tmpSet = lset.getLinear1DComponent(i);
          firsts[i] = tmpSet.getFirst();
          lasts[i] = tmpSet.getLast();
        }

        if (lset instanceof LinearLatLonSet) {
          processLinearLatLonSet(st, firsts, lasts, lengths, cs, units,
                                 errors, (LinearLatLonSet )lset);
        } else {
          processLinear2DSet(st, firsts, lasts, lengths, cs, units, errors,
                             lset);
        }
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processGridded2DSet(st, set.getSamples(), set.getLengths(),
                          set.getCoordinateSystem(), set.getSetUnits(),
                          set.getSetErrors(), set);
    }
  }

  public abstract void processGridded3DSet(SetType type, float[][] samples,
                                           int[] lengths, CoordinateSystem cs,
                                           Unit[] units,
                                           ErrorEstimate[] errors,
                                           Gridded3DSet set)
    throws VisADException;

  private void processGridded3DSet(SetType st, Gridded3DSet set)
    throws VisADException
  {
    int[] lengths = set.getLengths();
    CoordinateSystem cs = set.getCoordinateSystem();
    Unit[] units = set.getSetUnits();
    ErrorEstimate[] errors = set.getSetErrors();

    boolean done = false;
    try {
      if (set instanceof Gridded3DDoubleSet) {
        Gridded3DDoubleSet dset = (Gridded3DDoubleSet )set;

        processGridded3DDoubleSet(st, dset.getDoubles(), lengths, cs, units,
                                  errors, dset);
        done = true;
      } else if (set instanceof Integer3DSet) {
        processInteger3DSet(st, lengths, cs, units, errors,
                            (Integer3DSet )set);
        done = true;
      } else if (set instanceof Linear3DSet) {
        Linear3DSet lset = (Linear3DSet )set;

        double[] firsts = new double[3];
        double[] lasts = new double[3];
        for (int i = 0; i < 3; i++) {
          Linear1DSet tmpSet = lset.getLinear1DComponent(i);
          firsts[i] = tmpSet.getFirst();
          lasts[i] = tmpSet.getLast();
        }

        processLinear3DSet(st, firsts, lasts, lengths, cs, units, errors,
                           lset);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processGridded3DSet(st, set.getSamples(), set.getLengths(),
                          set.getCoordinateSystem(), set.getSetUnits(),
                          set.getSetErrors(), set);
    }
  }

  public abstract void processGriddedSet(SetType type, float[][] samples,
                                         int[] lengths, CoordinateSystem cs,
                                         Unit[] units, ErrorEstimate[] errors,
                                         GriddedSet set)
    throws VisADException;

  private void processGriddedSet(SetType st, GriddedSet set)
    throws VisADException
  {
    boolean done = false;
    try {
      if (set instanceof Gridded1DSet) {
        processGridded1DSet(st, (Gridded1DSet )set);
        done = true;
      } else if (set instanceof Gridded2DSet) {
        processGridded2DSet(st, (Gridded2DSet )set);
        done = true;
      } else if (set instanceof Gridded3DSet) {
        processGridded3DSet(st, (Gridded3DSet )set);
        done = true;
      } else if (set instanceof LinearNDSet) {
        processLinearNDSet(st, (LinearNDSet )set);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processGriddedSet(st, set.getSamples(), set.getLengths(),
                        set.getCoordinateSystem(), set.getSetUnits(),
                        set.getSetErrors(), set);
    }
  }

  public abstract void processInteger1DSet(SetType type, int[] lengths,
                                           CoordinateSystem cs, Unit[] units,
                                           ErrorEstimate[] errors,
                                           Integer1DSet set)
    throws VisADException;

  public abstract void processInteger2DSet(SetType type, int[] lengths,
                                           CoordinateSystem cs, Unit[] units,
                                           ErrorEstimate[] errors,
                                           Integer2DSet set)
    throws VisADException;

  public abstract void processInteger3DSet(SetType type, int[] lengths,
                                           CoordinateSystem cs, Unit[] units,
                                           ErrorEstimate[] errors,
                                           Integer3DSet set)
    throws VisADException;

  public abstract void processIntegerNDSet(SetType type, int[] lengths,
                                           CoordinateSystem cs, Unit[] units,
                                           ErrorEstimate[] errors,
                                           IntegerNDSet set)
    throws VisADException;

  public abstract void processIrregular1DSet(SetType type, float[][] samples,
                                             CoordinateSystem cs,
                                             Unit[] units,
                                             ErrorEstimate[] errors,
                                             Irregular1DSet set)
    throws VisADException;

  public abstract void processIrregular2DSet(SetType type, float[][] samples,
                                             CoordinateSystem cs,
                                             Unit[] units,
                                             ErrorEstimate[] errors,
                                             Delaunay delaunay,
                                             Irregular2DSet set)
    throws VisADException;

  public abstract void processIrregular3DSet(SetType type, float[][] samples,
                                             CoordinateSystem cs,
                                             Unit[] units,
                                             ErrorEstimate[] errors,
                                             Delaunay delaunay,
                                             Irregular3DSet set)
    throws VisADException;

  public abstract void processIrregularSet(SetType type, float[][] samples,
                                           CoordinateSystem cs, Unit[] units,
                                           ErrorEstimate[] errors,
                                           Delaunay delaunay,
                                           IrregularSet set)
    throws VisADException;

  private void processIrregularSet(SetType st, IrregularSet set)
    throws VisADException
  {
    float[][] samples = set.getSamples();
    CoordinateSystem cs = set.getCoordinateSystem();
    Unit[] units = set.getSetUnits();
    ErrorEstimate[] errors = set.getSetErrors();

    boolean done = false;
    try {
      if (set instanceof Irregular1DSet) {
        processIrregular1DSet(st, samples, cs, units, errors,
                              (Irregular1DSet )set);
        done = true;
      } else if (set instanceof Irregular2DSet) {
        processIrregular2DSet(st, samples, cs, units, errors, set.Delan,
                              (Irregular2DSet )set);
        done = true;
      } else if (set instanceof Irregular3DSet) {
        processIrregular3DSet(st, samples, cs, units, errors, set.Delan,
                              (Irregular3DSet )set);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processIrregularSet(st, samples, cs, units, errors, set.Delan, set);
    }
  }

  public abstract void processLinear1DSet(SetType type, double[] firsts,
                                          double[] lasts, int[] lengths,
                                          CoordinateSystem cs, Unit[] units,
                                          ErrorEstimate[] errors,
                                          Linear1DSet set)
    throws VisADException;

  public void processLinear1DSet(SetType st, Linear1DSet set)
    throws VisADException
  {
    processLinear1DSet(st, new double[] { set.getFirst() },
                       new double[] { set.getLast() }, set.getLengths(),
                       set.getCoordinateSystem(), set.getSetUnits(),
                       set.getSetErrors(), set);
  }

  public abstract void processLinear2DSet(SetType type, double[] firsts,
                                          double[] lasts, int[] lengths,
                                          CoordinateSystem cs, Unit[] units,
                                          ErrorEstimate[] errors,
                                          Linear2DSet set)
    throws VisADException;

  public abstract void processLinear3DSet(SetType type, double[] firsts,
                                          double[] lasts, int[] lengths,
                                          CoordinateSystem cs, Unit[] units,
                                          ErrorEstimate[] errors,
                                          Linear3DSet set)
    throws VisADException;

  public abstract void processLinearLatLonSet(SetType type, double[] firsts,
                                              double[] lasts, int[] lengths,
                                              CoordinateSystem cs,
                                              Unit[] units,
                                              ErrorEstimate[] errors,
                                              LinearLatLonSet set)
    throws VisADException; 

  public abstract void processLinearNDSet(SetType type, double[] firsts,
                                          double[] lasts, int[] lengths,
                                          CoordinateSystem cs, Unit[] units,
                                          ErrorEstimate[] errors,
                                          LinearNDSet set)
    throws VisADException;

  private void processLinearNDSet(SetType st, LinearNDSet set)
    throws VisADException
  {
    boolean done = false;
    try {
      if (set instanceof IntegerNDSet) {
        processIntegerNDSet(st, set.getLengths(), set.getCoordinateSystem(),
                            set.getSetUnits(), set.getSetErrors(),
                            (IntegerNDSet )set);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processLinearNDSet(st, set.getFirsts(), set.getLasts(),
                         set.getLengths(), set.getCoordinateSystem(),
                         set.getSetUnits(), set.getSetErrors(), set);
    }
  }

  public abstract void processList1DSet(SetType type, float[] list,
                                        CoordinateSystem cs, Unit[] units,
                                        List1DSet set)
    throws VisADException;

  public abstract void processProductSet(SetType type, SampledSet[] sets,
                                         CoordinateSystem cs, Unit[] units,
                                         ErrorEstimate[] errors,
                                         ProductSet set)
    throws VisADException;

  public abstract void processReal(RealType type, double value, Unit unit,
                                   ErrorEstimate error, Real real)
    throws VisADException;

  public abstract void processRealTuple(RealTupleType type, Real[] components,
                                        CoordinateSystem cs, RealTuple rt)
    throws VisADException;

  public abstract void processSampledSet(SetType st, int manifold_dimension,
                                         CoordinateSystem cs, Unit[] units,
                                         ErrorEstimate[] errors,
                                         SampledSet set)
    throws VisADException;

  private void processSampledSet(SetType st, SampledSet set)
    throws VisADException
  {
    boolean done = false;
    try {
      if (set instanceof GriddedSet) {
        processGriddedSet(st, (GriddedSet )set);
        done = true;
      } else if (set instanceof IrregularSet) {
        processIrregularSet(st, (IrregularSet )set);
        done = true;
      } else if (set instanceof ProductSet) {
        ProductSet ps = (ProductSet )set;

        processProductSet(st, ps.getSets(), ps.getCoordinateSystem(),
                          ps.getSetUnits(), ps.getSetErrors(), ps);
        done = true;
      } else if (set instanceof SingletonSet) {
        SingletonSet ss = (SingletonSet )set;

        processSingletonSet(ss.getData(), ss.getCoordinateSystem(),
                            ss.getSetUnits(), ss.getSetErrors(), ss);
        done = true;
      } else if (set instanceof UnionSet) {
        UnionSet us = (UnionSet )set;

        processUnionSet(st, us.getSets(), us);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processSampledSet(st, set.getManifoldDimension(),
                        set.getCoordinateSystem(), set.getSetUnits(),
                        set.getSetErrors(), set);
    }
  }

  public abstract void processSimpleSet(SetType st, int manifold_dimension,
                                        CoordinateSystem cs, Unit[] units,
                                        ErrorEstimate[] errors, SimpleSet set)
    throws VisADException;

  private void processSimpleSet(SetType st, SimpleSet set)
    throws VisADException
  {
    boolean done = false;
    try {
      if (set instanceof DoubleSet) {
        DoubleSet ds = (DoubleSet )set;

        processDoubleSet(st, ds.getCoordinateSystem(), ds.getSetUnits(), ds);
        done = true;
      } else if (set instanceof FloatSet) {
        FloatSet fs = (FloatSet )set;

        processFloatSet(st, fs.getCoordinateSystem(), fs.getSetUnits(), fs);
        done = true;
      } else if (set instanceof List1DSet) {
        List1DSet ls = (List1DSet )set;

        float[][] samples = ls.getSamples();
        processList1DSet(st, samples[0], ls.getCoordinateSystem(),
                         ls.getSetUnits(), ls);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processSimpleSet(st, set.getManifoldDimension(),
                       set.getCoordinateSystem(), set.getSetUnits(),
                       set.getSetErrors(), set);
    }
  }

  public abstract void processSingletonSet(RealTuple sample,
                                           CoordinateSystem cs, Unit[] units,
                                           ErrorEstimate[] errors,
                                           SingletonSet set)
    throws VisADException;

  public abstract void processText(TextType type, String value,
                                   boolean missing, Text text)
    throws VisADException;

  public abstract void processTuple(TupleType type, Data[] components, Tuple t)
    throws VisADException;

  public abstract void processUnionSet(SetType type, SampledSet[] sets,
                                       UnionSet set)
    throws VisADException;

  public abstract void processUnknownData(DataImpl data)
    throws VisADException;

}
