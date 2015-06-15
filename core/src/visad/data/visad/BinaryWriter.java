/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import visad.*;

import visad.data.BaseDataProcessor;
import visad.data.DataWriter;

import visad.data.visad.object.*;

/**
 * Write a {@link visad.Data Data} object in VisAD's binary format.
 *
 * @see <a href="http://www.ssec.wisc.edu/~dglo/binary_file_format.html">Binary File Format Spec</a>
 */
public class BinaryWriter
  extends BaseDataProcessor
  implements BinaryFile, DataWriter
{
  private boolean initialized;
  private DataOutputStream file;

  private BinaryObjectCache unitCache, errorCache, cSysCache, typeCache;

  public BinaryWriter()
  {
    file = null;
  }

  public BinaryWriter(String name)
    throws IOException
  {
    this(new File(name));
  }

  public BinaryWriter(File ref)
    throws IOException
  {
    this(new BufferedOutputStream(new FileOutputStream(ref)));
  }

  public BinaryWriter(OutputStream stream)
    throws IOException
  {
    setOutputStream(stream);
  }

  public void close()
    throws IOException
  {
    file.close();
    file = null;
  }

  public void flush()
    throws IOException
  {
    if (file == null) {
      throw new IOException("No active file");
    }

    file.flush();
  }

  public final BinaryObjectCache getCoordinateSystemCache() { return cSysCache; }
  public final BinaryObjectCache getErrorEstimateCache() { return errorCache; }
  public final DataOutput getOutput() { return file; }
  public final BinaryObjectCache getTypeCache() { return typeCache; }
  public final BinaryObjectCache getUnitCache() { return unitCache; }

  private final void initVars()
  {
    if (!initialized) {
      this.file = null;
    }

    this.unitCache = new BinaryObjectCache();
    this.errorCache = new BinaryObjectCache();
    this.cSysCache = new BinaryObjectCache();
    this.typeCache = new BinaryObjectCache();
  }

  public void processDoubleSet(SetType type, CoordinateSystem cs,
                               Unit[] units, DoubleSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinarySimpleSet.write(this, type, cs, units, set, DoubleSet.class,
                            DATA_DOUBLE_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processFieldImpl(FunctionType type, Set set, FieldImpl fld,
                               Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (fld == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryFieldImpl.write(this, type, set, fld, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + fld.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processFlatField(FunctionType type, Set domainSet,
                               CoordinateSystem cs,
                               CoordinateSystem[] rangeCS, Set[] rangeSets,
                               Unit[] units, FlatField fld, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (fld == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryFlatField.write(this, type, domainSet, cs, rangeCS, rangeSets,
                            units, fld, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + fld.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processFloatSet(SetType type, CoordinateSystem cs,
                              Unit[] units, FloatSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinarySimpleSet.write(this, type, cs, units, set, FloatSet.class,
                            DATA_FLOAT_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processGridded1DDoubleSet(SetType type, double[][] samples,
                                        int[] lengths, CoordinateSystem cs,
                                        Unit[] units, ErrorEstimate[] errors,
                                        Gridded1DDoubleSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryGriddedDoubleSet.write(this, type, samples, lengths, cs, units,
                                   errors, set, Gridded1DDoubleSet.class,
                                   DATA_GRIDDED_1D_DOUBLE_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Gridded1DDoubleSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processGridded2DDoubleSet(SetType type, double[][] samples,
                                        int[] lengths, CoordinateSystem cs,
                                        Unit[] units, ErrorEstimate[] errors,
                                        Gridded2DDoubleSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryGriddedDoubleSet.write(this, type, samples, lengths, cs, units,
                                   errors, set, Gridded2DDoubleSet.class,
                                   DATA_GRIDDED_2D_DOUBLE_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Gridded2DDoubleSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processGridded3DDoubleSet(SetType type, double[][] samples,
                                        int[] lengths, CoordinateSystem cs,
                                        Unit[] units, ErrorEstimate[] errors,
                                        Gridded3DDoubleSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryGriddedDoubleSet.write(this, type, samples, lengths, cs, units,
                                   errors, set, Gridded3DDoubleSet.class,
                                   DATA_GRIDDED_3D_DOUBLE_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Gridded3DDoubleSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processGridded1DSet(SetType type, float[][] samples,
                                  int[] lengths, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  Gridded1DSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryGriddedSet.write(this, type, samples, lengths, cs, units, errors,
                             set, Gridded1DSet.class, DATA_GRIDDED_1D_SET,
                             token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Gridded1DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processGridded2DSet(SetType type, float[][] samples,
                                  int[] lengths, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  Gridded2DSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryGriddedSet.write(this, type, samples, lengths, cs, units, errors,
                             set, Gridded2DSet.class, DATA_GRIDDED_2D_SET,
                             token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Gridded2DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processGridded3DSet(SetType type, float[][] samples,
                                  int[] lengths, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  Gridded3DSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryGriddedSet.write(this, type, samples, lengths, cs, units, errors,
                             set, Gridded3DSet.class, DATA_GRIDDED_3D_SET,
                             token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Gridded3DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processGriddedSet(SetType type, float[][] samples,
                                int[] lengths, CoordinateSystem cs,
                                Unit[] units, ErrorEstimate[] errors,
                                GriddedSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryGriddedSet.write(this, type, samples, lengths, cs, units, errors,
                             set, GriddedSet.class, DATA_GRIDDED_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write GriddedSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processInteger1DSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Integer1DSet set,
                                  Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryIntegerSet.write(this, type, lengths, null, cs, units, errors,
                             set, Integer1DSet.class, DATA_INTEGER_1D_SET,
                             token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Integer1DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processInteger2DSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Integer2DSet set,
                                  Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    Integer1DSet[] comps = BinaryIntegerSet.getComponents(set);

    try {
      BinaryIntegerSet.write(this, type, lengths, comps, cs, units, errors,
                             set, Integer2DSet.class, DATA_INTEGER_2D_SET,
                             token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Integer2DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processInteger3DSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Integer3DSet set,
                                  Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    Integer1DSet[] comps = BinaryIntegerSet.getComponents(set);

    try {
      BinaryIntegerSet.write(this, type, lengths, comps, cs, units, errors,
                             set, Integer3DSet.class, DATA_INTEGER_3D_SET,
                             token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Integer3DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processIntegerNDSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, IntegerNDSet set,
                                  Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    Integer1DSet[] comps = BinaryIntegerSet.getComponents(set);

    try {
      BinaryIntegerSet.write(this, type, lengths, comps, cs, units, errors,
                             set, IntegerNDSet.class, DATA_INTEGER_ND_SET,
                             token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write IntegerNDSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processIrregular1DSet(SetType type, float[][] samples,
                                    CoordinateSystem cs, Unit[] units,
                                    ErrorEstimate[] errors,
                                    Irregular1DSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryIrregularSet.write(this, type, samples, cs, units, errors, null,
                               set, Irregular1DSet.class,
                               DATA_IRREGULAR_1D_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Irregular1DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processIrregular2DSet(SetType type, float[][] samples,
                                    CoordinateSystem cs, Unit[] units,
                                    ErrorEstimate[] errors, Delaunay delaunay,
                                    Irregular2DSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryIrregularSet.write(this, type, samples, cs, units, errors,
                               delaunay, set, Irregular2DSet.class,
                               DATA_IRREGULAR_2D_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Irregular2DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processIrregular3DSet(SetType type, float[][] samples,
                                    CoordinateSystem cs, Unit[] units,
                                    ErrorEstimate[] errors, Delaunay delaunay,
                                    Irregular3DSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryIrregularSet.write(this, type, samples, cs, units, errors,
                               delaunay, set, Irregular3DSet.class,
                               DATA_IRREGULAR_3D_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Irregular3DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processIrregularSet(SetType type, float[][] samples,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Delaunay delaunay,
                                  IrregularSet set, Object token)
    throws VisADException
  {
    try {
      BinaryIrregularSet.write(this, type, samples, cs, units, errors,
                               delaunay, set, IrregularSet.class,
                               DATA_IRREGULAR_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write IrregularSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processLinear1DSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Linear1DSet set,
                                 Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryLinearSet.write(this, type, firsts, lasts, lengths, null, cs,
                            units, errors, set, Linear1DSet.class,
                            DATA_LINEAR_1D_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Linear1DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processLinear2DSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Linear2DSet set,
                                 Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    Linear1DSet[] comps = new Linear1DSet[2];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = set.getLinear1DComponent(i);
    }

    try {
      BinaryLinearSet.write(this, type, firsts, lasts, lengths, comps, cs,
                            units, errors, set, Linear2DSet.class,
                            DATA_LINEAR_2D_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Linear2DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processLinear3DSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Linear3DSet set,
                                 Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    Linear1DSet[] comps = new Linear1DSet[3];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = set.getLinear1DComponent(i);
    }

    try {
      BinaryLinearSet.write(this, type, firsts, lasts, lengths, comps, cs,
                            units, errors, set, Linear3DSet.class,
                            DATA_LINEAR_3D_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Linear3DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processLinearLatLonSet(SetType type, double[] firsts,
                                     double[] lasts, int[] lengths,
                                     CoordinateSystem cs, Unit[] units,
                                     ErrorEstimate[] errors,
                                     LinearLatLonSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    Linear1DSet[] comps = new Linear1DSet[2];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = set.getLinear1DComponent(i);
    }

    try {
      BinaryLinearSet.write(this, type, firsts, lasts, lengths, comps, cs,
                            units, errors, set, LinearLatLonSet.class,
                            DATA_LINEAR_LATLON_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write LinearLatLonSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processLinearNDSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, LinearNDSet set,
                                 Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    Linear1DSet[] comps = new Linear1DSet[set.getDimension()];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = set.getLinear1DComponent(i);
    }

    try {
      BinaryLinearSet.write(this, type, firsts, lasts, lengths, comps, cs,
                            units, errors, set, LinearNDSet.class,
                            DATA_LINEAR_ND_SET, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write LinearNDSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processList1DSet(SetType type, float[] list,
                               CoordinateSystem cs, Unit[] units,
                               List1DSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryList1DSet.write(this, type, list, cs, units, set, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write List1DSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processProductSet(SetType type, SampledSet[] sets,
                                CoordinateSystem cs, Unit[] units,
                                ErrorEstimate[] errors, ProductSet set,
                                Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryProductSet.write(this, type, sets, cs, units, errors, set, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processReal(RealType type, double value, Unit unit,
                          ErrorEstimate error, Real real, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    try {
      BinaryReal.write(this, type, value, unit, error, real, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + real.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processRealTuple(RealTupleType type, Real[] components,
                               CoordinateSystem cs, RealTuple rt,
                               Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (rt == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryRealTuple.write(this, type, components, cs, rt, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + rt.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processSampledSet(SetType st, int manifold_dimension,
                                CoordinateSystem cs, Unit[] units,
                                ErrorEstimate[] errors, SampledSet set,
                                Object token)
    throws VisADException
  {
    try {
      BinaryUnknown.write(this, set, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write SampledSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processSimpleSet(SetType st, int manifold_dimension,
                               CoordinateSystem cs, Unit[] units,
                               ErrorEstimate[] errors, SimpleSet set,
                               Object token)
    throws VisADException
  {
    try {
      BinaryUnknown.write(this, set, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write SimpleSet object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processSingletonSet(RealTuple sample, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  SingletonSet set, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinarySingletonSet.write(this, sample, cs, units, errors, set, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processText(TextType type, String value, boolean missing,
                          Text text, Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    try {
      BinaryText.write(this, type, value, missing, text, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + text.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processTuple(TupleType type, Data[] components, Tuple t,
                           Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (t == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryTuple.write(this, type, components, t, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + t.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processUnionSet(SetType type, SampledSet[] sets, UnionSet set,
                              Object token)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    try {
      BinaryUnionSet.write(this, type, sets, set, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processUnknownData(DataImpl data, Object token)
    throws VisADException
  {
    try {
      BinaryUnknown.write(this, data, token);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Data object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  /**
   * Save a Data object to the file.
   *
   * @param data <tt>Data</tt> object to save
   *
   * @exception VisADException if the save fails.
   */
  public void save(DataImpl data)
    throws VisADException
  {
    save(data, false);
  }

  /**
   * Save a big Data object to the file.
   * If called with <tt>bigObject</tt> set to <tt>true</tt>,
   * special measures will be taken to make sure that only
   * the necessary parts of a <tt>Data</tt> object are
   * loaded, so that objects too large to fit in memory have
   * a better chance of being saved.
   *
   * @param data <tt>Data</tt> object to save
   * @param bigObject <tt>true</tt> if this is a really big object
   *
   * @exception VisADException if the save fails.
   */
  public void save(DataImpl data, boolean bigObject)
    throws VisADException
  {
    Object dependToken;
    if (bigObject) {
      dependToken = BinaryObject.SAVE_DEPEND_BIG;
    } else {
      dependToken = BinaryObject.SAVE_DEPEND;
    }

    process(data, dependToken);
    process(data, BinaryObject.SAVE_DATA);
  }

  public void setFile(String name)
    throws IOException
  {
    setFile(new File(name));
  }

  public void setFile(File ref)
    throws IOException
  {
    setOutputStream(new FileOutputStream(ref));
  }

  public void setOutputStream(OutputStream stream)
    throws IOException
  {
    if (file != null) {
      file.flush();
      file.close();
      file = null;
    }

    initVars();

    if (stream == null) {
      throw new IOException("Null OutputStream");
    }

    file = new DataOutputStream(new BufferedOutputStream(stream));

    file.writeBytes(MAGIC_STR);
    file.writeInt(FORMAT_VERSION);
  }
}
