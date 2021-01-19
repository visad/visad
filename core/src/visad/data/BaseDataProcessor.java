/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

public abstract class BaseDataProcessor
  implements DataProcessor
{
  /**
   * Write the <tt>Data</tt> object using the most appropriate
   * {@link visad.data.DataProcessor DataProcessor} method.
   */
  public void process(DataImpl di, Object token)
    throws VisADException
  {
    MathType mt = di.getType();

    boolean done = false;
    if (mt instanceof ScalarType) {
      try {
        if (mt instanceof TextType) {
          Text t = (Text )di;

          processText((TextType )t.getType(), t.getValue(), t.isMissing(), t,
                      token);
          done = true;
        } else if (mt instanceof RealType) {
          Real r = (Real )di;

          processReal((RealType )r.getType(), r.getValue(), r.getUnit(),
                      r.getError(), r, token);
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
            processRealTuple(rtt, reals, rti.getCoordinateSystem(), rti,
                             token);
            done = true;
          } catch (UnimplementedException ue) {
          }
        }
      }

      if (!done) {
        try {
          processTuple(tt, comps, ti, token);
          done = true;
        } catch (UnimplementedException ue) {
        }
      }
    } else if (mt instanceof SetType) {
      SetType st = (SetType )mt;

      try {
        if (di instanceof SampledSet) {
          processSampledSet(st, (SampledSet )di, token);
          done = true;
        } else if (di instanceof SimpleSet) {
          processSimpleSet(st, (SimpleSet )di, token);
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
                           ff.getRangeSets(), ff.getDefaultRangeUnits(), ff,
                           token);
          done = true;
        } else if (di instanceof FieldImpl) {
          FieldImpl fi = (FieldImpl )di;

          processFieldImpl(ft, fi.getDomainSet(), fi, token);
          done = true;
        }
      } catch (UnimplementedException ue) {
      }
    }

    if (!done) {
      try {
        processUnknownData(di, token);
      } catch (UnimplementedException ue) {
        throw new UnimplementedException("Couldn't process " +
                                         di.getClass().getName() +
                                         " in " + getClass().getName());
      }
    }
  }

  public abstract void processDoubleSet(SetType type, CoordinateSystem cs,
                                        Unit[] units, DoubleSet set,
                                        Object token)
    throws VisADException;

  public abstract void processFieldImpl(FunctionType type, Set set,
                                        FieldImpl fld, Object token)
    throws VisADException;

  public abstract void processFlatField(FunctionType type, Set domainSet,
                                        CoordinateSystem cs,
                                        CoordinateSystem[] rangeCS,
                                        Set[] rangeSets, Unit[] units,
                                        FlatField fld, Object token)
    throws VisADException;

  public abstract void processFloatSet(SetType type, CoordinateSystem cs,
                                       Unit[] units, FloatSet set,
                                       Object token)
    throws VisADException;

  public abstract void processGridded1DDoubleSet(SetType type,
                                                 double[][] samples,
                                                 int[] lengths,
                                                 CoordinateSystem cs,
                                                 Unit[] units,
                                                 ErrorEstimate[] errors,
                                                 Gridded1DDoubleSet set,
                                                 Object token)
    throws VisADException;

  public abstract void processGridded2DDoubleSet(SetType type,
                                                 double[][] samples,
                                                 int[] lengths,
                                                 CoordinateSystem cs,
                                                 Unit[] units,
                                                 ErrorEstimate[] errors,
                                                 Gridded2DDoubleSet set,
                                                 Object token)
    throws VisADException;

  public abstract void processGridded3DDoubleSet(SetType type,
                                                 double[][] samples,
                                                 int[] lengths,
                                                 CoordinateSystem cs,
                                                 Unit[] units,
                                                 ErrorEstimate[] errors,
                                                 Gridded3DDoubleSet set,
                                                 Object token)
    throws VisADException;

  public abstract void processGridded1DSet(SetType type, float[][] samples,
                                           int[] lengths, CoordinateSystem cs,
                                           Unit[] units,
                                           ErrorEstimate[] errors,
                                           Gridded1DSet set, Object token)
    throws VisADException;

  private void processGridded1DSet(SetType st, Gridded1DSet set, Object token)
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
                                  errors, dset, token);
        done = true;
      } else if (set instanceof Integer1DSet) {
        processInteger1DSet(st, lengths, cs, units, errors,
                            (Integer1DSet )set, token);
        done = true;
      } else if (set instanceof Linear1DSet) {
        Linear1DSet lset = (Linear1DSet )set;

        double[] firsts = new double[] { lset.getFirst() };
        double[] lasts = new double[] { lset.getLast() };

        processLinear1DSet(st, firsts, lasts, lengths, cs, units, errors,
                           lset, token);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processGridded1DSet(st, set.getSamples(), set.getLengths(),
                          set.getCoordinateSystem(), set.getSetUnits(),
                          set.getSetErrors(), set, token);
    }
  }

  public abstract void processGridded2DSet(SetType type, float[][] samples,
                                           int[] lengths, CoordinateSystem cs,
                                           Unit[] units,
                                           ErrorEstimate[] errors,
                                           Gridded2DSet set, Object token)
    throws VisADException;

  private void processGridded2DSet(SetType st, Gridded2DSet set, Object token)
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
                                  errors, dset, token);
        done = true;
      } else if (set instanceof Integer2DSet) {
        processInteger2DSet(st, lengths, cs, units, errors,
                            (Integer2DSet )set, token);
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
                                 errors, (LinearLatLonSet )lset, token);
        } else {
          processLinear2DSet(st, firsts, lasts, lengths, cs, units, errors,
                             lset, token);
        }
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processGridded2DSet(st, set.getSamples(), set.getLengths(),
                          set.getCoordinateSystem(), set.getSetUnits(),
                          set.getSetErrors(), set, token);
    }
  }

  public abstract void processGridded3DSet(SetType type, float[][] samples,
                                           int[] lengths, CoordinateSystem cs,
                                           Unit[] units,
                                           ErrorEstimate[] errors,
                                           Gridded3DSet set, Object token)
    throws VisADException;

  private void processGridded3DSet(SetType st, Gridded3DSet set, Object token)
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
                                  errors, dset, token);
        done = true;
      } else if (set instanceof Integer3DSet) {
        processInteger3DSet(st, lengths, cs, units, errors,
                            (Integer3DSet )set, token);
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
                           lset, token);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processGridded3DSet(st, set.getSamples(), set.getLengths(),
                          set.getCoordinateSystem(), set.getSetUnits(),
                          set.getSetErrors(), set, token);
    }
  }

  public abstract void processGriddedSet(SetType type, float[][] samples,
                                         int[] lengths, CoordinateSystem cs,
                                         Unit[] units, ErrorEstimate[] errors,
                                         GriddedSet set, Object token)
    throws VisADException;

  private void processGriddedSet(SetType st, GriddedSet set, Object token)
    throws VisADException
  {
    boolean done = false;
    try {
      if (set instanceof Gridded1DSet) {
        processGridded1DSet(st, (Gridded1DSet )set, token);
        done = true;
      } else if (set instanceof Gridded2DSet) {
        processGridded2DSet(st, (Gridded2DSet )set, token);
        done = true;
      } else if (set instanceof Gridded3DSet) {
        processGridded3DSet(st, (Gridded3DSet )set, token);
        done = true;
      } else if (set instanceof LinearNDSet) {
        processLinearNDSet(st, (LinearNDSet )set, token);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processGriddedSet(st, set.getSamples(), set.getLengths(),
                        set.getCoordinateSystem(), set.getSetUnits(),
                        set.getSetErrors(), set, token);
    }
  }

  public abstract void processInteger1DSet(SetType type, int[] lengths,
                                           CoordinateSystem cs, Unit[] units,
                                           ErrorEstimate[] errors,
                                           Integer1DSet set, Object token)
    throws VisADException;

  public abstract void processInteger2DSet(SetType type, int[] lengths,
                                           CoordinateSystem cs, Unit[] units,
                                           ErrorEstimate[] errors,
                                           Integer2DSet set, Object token)
    throws VisADException;

  public abstract void processInteger3DSet(SetType type, int[] lengths,
                                           CoordinateSystem cs, Unit[] units,
                                           ErrorEstimate[] errors,
                                           Integer3DSet set, Object token)
    throws VisADException;

  public abstract void processIntegerNDSet(SetType type, int[] lengths,
                                           CoordinateSystem cs, Unit[] units,
                                           ErrorEstimate[] errors,
                                           IntegerNDSet set, Object token)
    throws VisADException;

  public abstract void processIrregular1DSet(SetType type, float[][] samples,
                                             CoordinateSystem cs,
                                             Unit[] units,
                                             ErrorEstimate[] errors,
                                             Irregular1DSet set, Object token)
    throws VisADException;

  public abstract void processIrregular2DSet(SetType type, float[][] samples,
                                             CoordinateSystem cs,
                                             Unit[] units,
                                             ErrorEstimate[] errors,
                                             Delaunay delaunay,
                                             Irregular2DSet set, Object token)
    throws VisADException;

  public abstract void processIrregular3DSet(SetType type, float[][] samples,
                                             CoordinateSystem cs,
                                             Unit[] units,
                                             ErrorEstimate[] errors,
                                             Delaunay delaunay,
                                             Irregular3DSet set, Object token)
    throws VisADException;

  public abstract void processIrregularSet(SetType type, float[][] samples,
                                           CoordinateSystem cs, Unit[] units,
                                           ErrorEstimate[] errors,
                                           Delaunay delaunay,
                                           IrregularSet set, Object token)
    throws VisADException;

  private void processIrregularSet(SetType st, IrregularSet set, Object token)
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
                              (Irregular1DSet )set, token);
        done = true;
      } else if (set instanceof Irregular2DSet) {
        processIrregular2DSet(st, samples, cs, units, errors, set.Delan,
                              (Irregular2DSet )set, token);
        done = true;
      } else if (set instanceof Irregular3DSet) {
        processIrregular3DSet(st, samples, cs, units, errors, set.Delan,
                              (Irregular3DSet )set, token);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processIrregularSet(st, samples, cs, units, errors, set.Delan, set,
                          token);
    }
  }

  public abstract void processLinear1DSet(SetType type, double[] firsts,
                                          double[] lasts, int[] lengths,
                                          CoordinateSystem cs, Unit[] units,
                                          ErrorEstimate[] errors,
                                          Linear1DSet set, Object token)
    throws VisADException;

  public void processLinear1DSet(SetType st, Linear1DSet set, Object token)
    throws VisADException
  {
    processLinear1DSet(st, new double[] { set.getFirst() },
                       new double[] { set.getLast() }, set.getLengths(),
                       set.getCoordinateSystem(), set.getSetUnits(),
                       set.getSetErrors(), set, token);
  }

  public abstract void processLinear2DSet(SetType type, double[] firsts,
                                          double[] lasts, int[] lengths,
                                          CoordinateSystem cs, Unit[] units,
                                          ErrorEstimate[] errors,
                                          Linear2DSet set, Object token)
    throws VisADException;

  public abstract void processLinear3DSet(SetType type, double[] firsts,
                                          double[] lasts, int[] lengths,
                                          CoordinateSystem cs, Unit[] units,
                                          ErrorEstimate[] errors,
                                          Linear3DSet set, Object token)
    throws VisADException;

  public abstract void processLinearLatLonSet(SetType type, double[] firsts,
                                              double[] lasts, int[] lengths,
                                              CoordinateSystem cs,
                                              Unit[] units,
                                              ErrorEstimate[] errors,
                                              LinearLatLonSet set,
                                              Object token)
    throws VisADException; 

  public abstract void processLinearNDSet(SetType type, double[] firsts,
                                          double[] lasts, int[] lengths,
                                          CoordinateSystem cs, Unit[] units,
                                          ErrorEstimate[] errors,
                                          LinearNDSet set, Object token)
    throws VisADException;

  private void processLinearNDSet(SetType st, LinearNDSet set, Object token)
    throws VisADException
  {
    boolean done = false;
    try {
      if (set instanceof IntegerNDSet) {
        processIntegerNDSet(st, set.getLengths(), set.getCoordinateSystem(),
                            set.getSetUnits(), set.getSetErrors(),
                            (IntegerNDSet )set, token);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processLinearNDSet(st, set.getFirsts(), set.getLasts(),
                         set.getLengths(), set.getCoordinateSystem(),
                         set.getSetUnits(), set.getSetErrors(), set, token);
    }
  }

  public abstract void processList1DSet(SetType type, float[] list,
                                        CoordinateSystem cs, Unit[] units,
                                        List1DSet set, Object token)
    throws VisADException;

  public abstract void processProductSet(SetType type, SampledSet[] sets,
                                         CoordinateSystem cs, Unit[] units,
                                         ErrorEstimate[] errors,
                                         ProductSet set, Object token)
    throws VisADException;

  public abstract void processReal(RealType type, double value, Unit unit,
                                   ErrorEstimate error, Real real,
                                   Object token)
    throws VisADException;

  public abstract void processRealTuple(RealTupleType type, Real[] components,
                                        CoordinateSystem cs, RealTuple rt,
                                        Object token)
    throws VisADException;

  public abstract void processSampledSet(SetType st, int manifold_dimension,
                                         CoordinateSystem cs, Unit[] units,
                                         ErrorEstimate[] errors,
                                         SampledSet set, Object token)
    throws VisADException;

  private void processSampledSet(SetType st, SampledSet set, Object token)
    throws VisADException
  {
    boolean done = false;
    try {
      if (set instanceof GriddedSet) {
        processGriddedSet(st, (GriddedSet )set, token);
        done = true;
      } else if (set instanceof IrregularSet) {
        processIrregularSet(st, (IrregularSet )set, token);
        done = true;
      } else if (set instanceof ProductSet) {
        ProductSet ps = (ProductSet )set;

        processProductSet(st, ps.getSets(), ps.getCoordinateSystem(),
                          ps.getSetUnits(), ps.getSetErrors(), ps, token);
        done = true;
      } else if (set instanceof SingletonSet) {
        SingletonSet ss = (SingletonSet )set;

        processSingletonSet(ss.getData(), ss.getCoordinateSystem(),
                            ss.getSetUnits(), ss.getSetErrors(), ss, token);
        done = true;
      } else if (set instanceof UnionSet) {
        UnionSet us = (UnionSet )set;

        processUnionSet(st, us.getSets(), us, token);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processSampledSet(st, set.getManifoldDimension(),
                        set.getCoordinateSystem(), set.getSetUnits(),
                        set.getSetErrors(), set, token);
    }
  }

  public abstract void processSimpleSet(SetType st, int manifold_dimension,
                                        CoordinateSystem cs, Unit[] units,
                                        ErrorEstimate[] errors, SimpleSet set,
                                        Object token)
    throws VisADException;

  private void processSimpleSet(SetType st, SimpleSet set, Object token)
    throws VisADException
  {
    boolean done = false;
    try {
      if (set instanceof DoubleSet) {
        DoubleSet ds = (DoubleSet )set;

        processDoubleSet(st, ds.getCoordinateSystem(), ds.getSetUnits(), ds,
                         token);
        done = true;
      } else if (set instanceof FloatSet) {
        FloatSet fs = (FloatSet )set;

        processFloatSet(st, fs.getCoordinateSystem(), fs.getSetUnits(), fs,
                        token);
        done = true;
      } else if (set instanceof List1DSet) {
        List1DSet ls = (List1DSet )set;

        float[][] samples = ls.getSamples();
        processList1DSet(st, samples[0], ls.getCoordinateSystem(),
                         ls.getSetUnits(), ls, token);
        done = true;
      }
    } catch (UnimplementedException ue) {
    }

    if (!done) {
      processSimpleSet(st, set.getManifoldDimension(),
                       set.getCoordinateSystem(), set.getSetUnits(),
                       set.getSetErrors(), set, token);
    }
  }

  public abstract void processSingletonSet(RealTuple sample,
                                           CoordinateSystem cs, Unit[] units,
                                           ErrorEstimate[] errors,
                                           SingletonSet set, Object token)
    throws VisADException;

  public abstract void processText(TextType type, String value,
                                   boolean missing, Text text, Object token)
    throws VisADException;

  public abstract void processTuple(TupleType type, Data[] components, Tuple t,
                                    Object token)
    throws VisADException;

  public abstract void processUnionSet(SetType type, SampledSet[] sets,
                                       UnionSet set, Object token)
    throws VisADException;

  public abstract void processUnknownData(DataImpl data, Object token)
    throws VisADException;
}
