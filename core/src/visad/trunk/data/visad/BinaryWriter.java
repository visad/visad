package visad.data.visad;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.rmi.RemoteException;

import java.util.ArrayList;

import visad.*;

import visad.data.BaseDataProcessor;
import visad.data.DataWriter;
import visad.data.FileField;
import visad.data.FileFlatField;

import visad.data.netcdf.Quantity;

/**
 * Write a {@link visad.Data Data} object in VisAD's binary format.
 *
 * @see <a href="http://www.ssec.wisc.edu/~dglo/binary_file_format.html">Binary File Format Spec</a>
 */
public class BinaryWriter
  extends BaseDataProcessor
  implements BinaryFile, DataWriter
{
  private static final boolean DEBUG_CSYS = false;
  private static final boolean DEBUG_DATA = false;
  private static final boolean DEBUG_DATA_DETAIL = false;
  private static final boolean DEBUG_ERRE = false;
  private static final boolean DEBUG_MATH = false;
  private static final boolean DEBUG_UNIT = false;

  private boolean initialized;
  private DataOutputStream file;
  private char[] outbuf;

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
    this(new FileOutputStream(ref));
  }

  public BinaryWriter(OutputStream stream)
    throws IOException
  {
    setOutputStream(stream);
  }

  private int cacheMathType(MathType mt)
    throws VisADException
  {
    int index = typeCache.getIndex(mt);
    if (index < 0) {

      // cache the MathType so we can find its index number
      index = typeCache.add(mt);
      if (index < 0) {
        throw new VisADException("Couldn't cache MathType " + mt);
      }
    }

    return index;
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

  private void initVars()
  {
    if (!initialized) {
      this.file = null;
      this.outbuf = new char[1];
    }

    this.unitCache = new BinaryObjectCache();
    this.errorCache = new BinaryObjectCache();
    this.cSysCache = new BinaryObjectCache();
    this.typeCache = new BinaryObjectCache();
  }

  public void processDoubleSet(SetType type, CoordinateSystem cs,
                               Unit[] units, DoubleSet set)
    throws VisADException
  {
    writeSimpleSet(type, cs, units, set, DoubleSet.class, DATA_DOUBLE_SET);
  }

  public void processFieldImpl(FunctionType type, Set set, FieldImpl fld)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (fld == null) {
      throw new VisADException("Null data object");
    }

    byte dataType;
    if (fld.getClass().equals(FieldImpl.class)) {
      dataType = DATA_FIELD;
    } else if (fld.getClass().equals(FileField.class)) {
      // treat FileFields like FieldImpls
      dataType = DATA_FIELD;
    } else {
if(DEBUG_DATA)System.err.println("wrFldI: punt "+fld.getClass().getName());
      processUnknownData(fld);
      return;
    }

    int typeIndex;
    try {
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("wrFldI: type (" + type + ")");
      typeIndex = writeFunctionType(type);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write FunctionType " + type);
    }

    try {
if(DEBUG_DATA)System.err.println("wrFldI: OBJ_DATA (" + OBJ_DATA + ")");
      file.writeByte(OBJ_DATA);
if(DEBUG_DATA)System.err.println("wrFldI: DATA_FIELD (" + dataType + ")");
      file.writeByte(dataType);

if(DEBUG_DATA)System.err.println("wrFldI: type index (" + typeIndex + ")");
      file.writeInt(typeIndex);

      if (set != null) {
if(DEBUG_DATA)System.err.println("wrFldI: FLD_SET (" + FLD_SET + ")");
        file.writeByte(FLD_SET);
        process(set);
      }

      final int len = fld.getLength();
      if (!fld.isMissing() && len > 0) {
if(DEBUG_DATA)System.err.println("wrFlFld: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
        file.writeByte(FLD_DATA_SAMPLES);
if(DEBUG_DATA)System.err.println("wrFlFld: len (" + len + ")");
        file.writeInt(len);
        for (int i = 0; i < len; i++) {
          DataImpl sample = (DataImpl )fld.getSample(i);
if(DEBUG_DATA)System.err.println("wrFlFld: S#"+i+" type is "+sample.getType()+" ("+sample.getClass().getName()+")");
          process(sample);
        }
      }

if(DEBUG_DATA)System.err.println("wrFldI: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + fld.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processFlatField(FunctionType type, Set domainSet,
                               CoordinateSystem cs,
                               CoordinateSystem[] rangeCS, Set[] rangeSets,
                               Unit[] units, FlatField fld)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (fld == null) {
      throw new VisADException("Null data object");
    }

    byte dataType;
    if (fld.getClass().equals(FlatField.class)) {
      dataType = DATA_FLAT_FIELD;
    } else if (fld.getClass().equals(FileFlatField.class)) {
      // treat FileFlatFields like FlatFields
      dataType = DATA_FLAT_FIELD;
    } else {
if(DEBUG_DATA)System.err.println("wrFlFld: punt "+fld.getClass().getName());
      processUnknownData(fld);
      return;
    }

    int typeIndex;
    try {
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("wrFlFld: type (" + type + ")");
      typeIndex = writeFunctionType(type);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write FunctionType " + type);
    }

    int csIndex = -1;
    if (cs != null) {
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("wrFlFld: coordSys (" + cs + ")");
      csIndex = writeCoordinateSystem(cs);
    }

    int[] csList = null;
    if (rangeCS != null) {
if(DEBUG_DATA&&!DEBUG_CSYS){
  System.err.println("wrFlFld: List of " + rangeCS.length + " CoordSys");
  for(int x=0;x<rangeCS.length;x++){
    System.err.println("wrFlFld:    #"+x+": "+rangeCS[x]);
  }
}
      csList = writeCoordinateSystems(rangeCS);
    }

    int[] unitsIndex = null;
    if (units != null) {
if(DEBUG_DATA&&!DEBUG_UNIT){
  System.err.println("wrFlFld: List of " + units.length + " Units");
  for(int x=0;x<units.length;x++){
    System.err.println("wrFlFld:    #"+x+": "+units[x]);
  }
}
      unitsIndex = writeUnits(units);
    }

    try {
if(DEBUG_DATA)System.err.println("wrFlFld: OBJ_DATA (" + OBJ_DATA + ")");
      file.writeByte(OBJ_DATA);
if(DEBUG_DATA)System.err.println("wrFlFld: DATA_FLAT_FIELD (" + DATA_FLAT_FIELD + ")");
      file.writeByte(DATA_FLAT_FIELD);

if(DEBUG_DATA)System.err.println("wrFlFld: type index (" + typeIndex + ")");
      file.writeInt(typeIndex);

      if (domainSet != null) {
if(DEBUG_DATA)System.err.println("wrFlFld: FLD_SET (" + FLD_SET + ")");
        file.writeByte(FLD_SET);
        process(domainSet);
      }

      final int len = fld.getLength();
      if (!fld.isMissing() && len > 0) {
if(DEBUG_DATA)System.err.println("wrFlFld: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
        file.writeByte(FLD_DATA_SAMPLES);
if(DEBUG_DATA)System.err.println("wrFlFld: len (" + len + ")");
        file.writeInt(len);
        for (int i = 0; i < len; i++) {
          DataImpl sample = (DataImpl )fld.getSample(i);
if(DEBUG_DATA)System.err.println("wrFlFld: S#"+i+" type is "+sample.getType()+" ("+sample.getClass().getName()+")");
          process(sample);
        }
      }

      if (csIndex >= 0) {
if(DEBUG_DATA)System.err.println("wrFlFld: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_DATA)System.err.println("wrFlFld: coord sys Index (" + csIndex + ")");
        file.writeInt(csIndex);
      }

      if (csList != null) {
if(DEBUG_DATA)System.err.println("wrFlFld: FLD_RANGE_COORDSYSES (" + FLD_RANGE_COORDSYSES + ")");
        file.writeByte(FLD_RANGE_COORDSYSES);
        writeIntegerArray(csList);
      }

      if (rangeSets != null) {
if(DEBUG_DATA)System.err.println("wrFlFld: FLD_SET_LIST (" + FLD_SET_LIST + ")");
        file.writeByte(FLD_SET_LIST);
if(DEBUG_DATA)System.err.println("wrFlFld: len (" + rangeSets.length + ")");
        file.writeInt(rangeSets.length);
        for (int i = 0; i < rangeSets.length; i++) {
          process(rangeSets[i]);
        }
      }

      if (unitsIndex != null) {
if(DEBUG_DATA)System.err.println("wrFlFld: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        file.writeByte(FLD_INDEX_UNITS);
        writeIntegerArray(unitsIndex);
      }

if(DEBUG_DATA)System.err.println("wrFlFld: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + fld.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processFloatSet(SetType type, CoordinateSystem cs,
                              Unit[] units, FloatSet set)
    throws VisADException
  {
    writeSimpleSet(type, cs, units, set, FloatSet.class, DATA_FLOAT_SET);
  }

  public void processGridded1DDoubleSet(SetType type, double[][] samples,
                                        int[] lengths, CoordinateSystem cs,
                                        Unit[] units, ErrorEstimate[] errors,
                                        Gridded1DDoubleSet set)
    throws VisADException
  {
    writeGriddedDoubleSet(type, samples, lengths, cs, units, errors, set,
                          Gridded1DDoubleSet.class,
                          DATA_GRIDDED_1D_DOUBLE_SET);
  }

  public void processGridded2DDoubleSet(SetType type, double[][] samples,
                                        int[] lengths, CoordinateSystem cs,
                                        Unit[] units, ErrorEstimate[] errors,
                                        Gridded2DDoubleSet set)
    throws VisADException
  {
    writeGriddedDoubleSet(type, samples, lengths, cs, units, errors, set,
                          Gridded2DDoubleSet.class,
                          DATA_GRIDDED_2D_DOUBLE_SET);
  }

  public void processGridded3DDoubleSet(SetType type, double[][] samples,
                                        int[] lengths, CoordinateSystem cs,
                                        Unit[] units, ErrorEstimate[] errors,
                                        Gridded3DDoubleSet set)
    throws VisADException
  {
    writeGriddedDoubleSet(type, samples, lengths, cs, units, errors, set,
                          Gridded3DDoubleSet.class,
                          DATA_GRIDDED_3D_DOUBLE_SET);
  }

  public void processGridded1DSet(SetType type, float[][] samples,
                                  int[] lengths, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  Gridded1DSet set)
    throws VisADException
  {
    writeGriddedSet(type, samples, lengths, cs, units, errors, set,
                    Gridded1DSet.class, DATA_GRIDDED_1D_SET);
  }

  public void processGridded2DSet(SetType type, float[][] samples,
                                  int[] lengths, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  Gridded2DSet set)
    throws VisADException
  {
    writeGriddedSet(type, samples, lengths, cs, units, errors, set,
                    Gridded2DSet.class, DATA_GRIDDED_2D_SET);
  }

  public void processGridded3DSet(SetType type, float[][] samples,
                                  int[] lengths, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  Gridded3DSet set)
    throws VisADException
  {
    writeGriddedSet(type, samples, lengths, cs, units, errors, set,
                    Gridded3DSet.class, DATA_GRIDDED_3D_SET);
  }

  public void processGriddedSet(SetType type, float[][] samples,
                                int[] lengths, CoordinateSystem cs,
                                Unit[] units, ErrorEstimate[] errors,
                                GriddedSet set)
    throws VisADException
  {
    writeGriddedSet(type, samples, lengths, cs, units, errors, set,
                    GriddedSet.class, DATA_GRIDDED_SET);
  }

  public void processInteger1DSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Integer1DSet set)
    throws VisADException
  {
    writeIntegerSet(type, lengths, null, cs, units, errors, set,
                    Integer1DSet.class, DATA_INTEGER_1D_SET);
  }

  public void processInteger2DSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Integer2DSet set)
    throws VisADException
  {
    if (set == null) {
      throw new VisADException("Null data object");
    }

    Integer1DSet[] comps = new Integer1DSet[2];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = (Integer1DSet )set.getLinear1DComponent(i);
    }

    writeIntegerSet(type, lengths, comps, cs, units, errors, set,
                    Integer2DSet.class, DATA_INTEGER_2D_SET);
  }

  public void processInteger3DSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Integer3DSet set)
    throws VisADException
  {
    if (set == null) {
      throw new VisADException("Null data object");
    }

    Integer1DSet[] comps = new Integer1DSet[3];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = (Integer1DSet )set.getLinear1DComponent(i);
    }

    writeIntegerSet(type, lengths, comps, cs, units, errors, set,
                    Integer3DSet.class, DATA_INTEGER_3D_SET);
  }

  public void processIntegerNDSet(SetType type, int[] lengths,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, IntegerNDSet set)
    throws VisADException
  {
    if (set == null) {
      throw new VisADException("Null data object");
    }

    Integer1DSet[] comps = new Integer1DSet[set.getDimension()];
    for (int i = 0; i < comps.length; i++) {
      Linear1DSet comp = set.getLinear1DComponent(i);

      if (comp instanceof Integer1DSet) {
        comps[i] = (Integer1DSet )comp;
      } else if (comp.getFirst() == 0.0) {
        // had to put this in because an old serialized object
        // had Linear1DSets instead of Integer1DSets
        comps[i] = new Integer1DSet(comp.getType(), comp.getLength(),
                                    comp.getCoordinateSystem(),
                                    comp.getSetUnits(), comp.getSetErrors());
      } else {
        // XXX what happens here?
        System.err.println("Ignoring comp#" + i + ": " + comp);
        comps[i] = null;
      }
    }

    writeIntegerSet(type, lengths, comps, cs, units, errors, set,
                    IntegerNDSet.class, DATA_INTEGER_ND_SET);
  }

  public void processIrregular1DSet(SetType type, float[][] samples,
                                    CoordinateSystem cs, Unit[] units,
                                    ErrorEstimate[] errors, Irregular1DSet set)
    throws VisADException
  {
    writeIrregularSet(type, samples, cs, units, errors, null, set,
                      Irregular1DSet.class, DATA_IRREGULAR_1D_SET);
  }

  public void processIrregular2DSet(SetType type, float[][] samples,
                                    CoordinateSystem cs, Unit[] units,
                                    ErrorEstimate[] errors, Delaunay delaunay,
                                    Irregular2DSet set)
    throws VisADException
  {
    writeIrregularSet(type, samples, cs, units, errors, delaunay, set,
                      Irregular2DSet.class, DATA_IRREGULAR_2D_SET);
  }

  public void processIrregular3DSet(SetType type, float[][] samples,
                                    CoordinateSystem cs, Unit[] units,
                                    ErrorEstimate[] errors, Delaunay delaunay,
                                    Irregular3DSet set)
    throws VisADException
  {
    writeIrregularSet(type, samples, cs, units, errors, delaunay, set,
                      Irregular3DSet.class, DATA_IRREGULAR_3D_SET);
  }

  public void processIrregularSet(SetType type, float[][] samples,
                                  CoordinateSystem cs, Unit[] units,
                                  ErrorEstimate[] errors, Delaunay delaunay,
                                  IrregularSet set)
    throws VisADException
  {
    writeIrregularSet(type, samples, cs, units, errors, delaunay, set,
                      IrregularSet.class, DATA_IRREGULAR_SET);
  }

  public void processLinear1DSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Linear1DSet set)
    throws VisADException
  {
    writeLinearSet(type, firsts, lasts, lengths, null, cs, units, errors,
                   set, Linear1DSet.class, DATA_LINEAR_1D_SET);
  }

  public void processLinear2DSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Linear2DSet set)
    throws VisADException
  {
    if (set == null) {
      throw new VisADException("Null data object");
    }

    Linear1DSet[] comps = new Linear1DSet[2];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = set.getLinear1DComponent(i);
    }

    writeLinearSet(type, firsts, lasts, lengths, comps, cs, units, errors,
                   set, Linear2DSet.class, DATA_LINEAR_2D_SET);
  }

  public void processLinear3DSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Linear3DSet set)
    throws VisADException
  {
    if (set == null) {
      throw new VisADException("Null data object");
    }

    Linear1DSet[] comps = new Linear1DSet[3];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = set.getLinear1DComponent(i);
    }

    writeLinearSet(type, firsts, lasts, lengths, comps, cs, units, errors,
                   set, Linear3DSet.class, DATA_LINEAR_3D_SET);
  }

  public void processLinearLatLonSet(SetType type, double[] firsts,
                                     double[] lasts, int[] lengths,
                                     CoordinateSystem cs, Unit[] units,
                                     ErrorEstimate[] errors,
                                     LinearLatLonSet set)
    throws VisADException
  {
    if (set == null) {
      throw new VisADException("Null data object");
    }

    Linear1DSet[] comps = new Linear1DSet[2];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = set.getLinear1DComponent(i);
    }

    writeLinearSet(type, firsts, lasts, lengths, comps, cs, units, errors,
                   set, LinearLatLonSet.class, DATA_LINEAR_LATLON_SET);
  }

  public void processLinearNDSet(SetType type, double[] firsts,
                                 double[] lasts, int[] lengths,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, LinearNDSet set)
    throws VisADException
  {
    if (set == null) {
      throw new VisADException("Null data object");
    }

    Linear1DSet[] comps = new Linear1DSet[set.getDimension()];
    for (int i = 0; i < comps.length; i++) {
      comps[i] = set.getLinear1DComponent(i);
    }

    writeLinearSet(type, firsts, lasts, lengths, comps, cs, units, errors,
                   set, LinearNDSet.class, DATA_LINEAR_ND_SET);
  }

  public void processList1DSet(SetType type, float[] list,
                               CoordinateSystem cs, Unit[] units,
                               List1DSet set)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    if (!set.getClass().equals(List1DSet.class)) {
if(DEBUG_DATA)System.err.println("wrL1DSet: punt "+set.getClass().getName());
      processUnknownData(set);
      return;
    }

    if (list == null) {
      throw new VisADException("Null List1DSet list");
    }

    int typeIndex;
    try {
      typeIndex = writeSetType(type, set);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write SetType " + type);
    }

    int csIndex = -1;
    if (cs != null) {
      csIndex = writeCoordinateSystem(cs);
    }

    int[] unitsIndex = null;
    if (units != null) {
      unitsIndex = writeUnits(units);
    }

    try {
if(DEBUG_DATA)System.err.println("wrL1DSet: OBJ_DATA (" + OBJ_DATA + ")");
      file.writeByte(OBJ_DATA);
if(DEBUG_DATA)System.err.println("wrL1DSet: DATA_LIST1D_SET (" + DATA_LIST1D_SET + ")");
      file.writeByte(DATA_LIST1D_SET);

if(DEBUG_DATA)System.err.println("wrL1DSet: type index (" + typeIndex + ")");
      file.writeInt(typeIndex);

if(DEBUG_DATA)System.err.println("wrL1DSet: FLD_FLOAT_LIST (" + FLD_FLOAT_LIST + ")");
      file.writeByte(FLD_FLOAT_LIST);
      writeFloatArray(list);

      if (csIndex >= 0) {
if(DEBUG_DATA)System.err.println("wrL1DSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_DATA)System.err.println("wrL1DSet: coord sys Index (" + csIndex + ")");
        file.writeInt(csIndex);
      }

      if (unitsIndex != null) {
if(DEBUG_DATA)System.err.println("wrL1DSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        file.writeByte(FLD_INDEX_UNITS);
        writeIntegerArray(unitsIndex);
      }

if(DEBUG_DATA)System.err.println("wrL1DSet: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processProductSet(SetType type, SampledSet[] sets,
                                CoordinateSystem cs, Unit[] units,
                                ErrorEstimate[] errors, ProductSet set)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    if (!set.getClass().equals(ProductSet.class)) {
if(DEBUG_DATA)System.err.println("wrPrSet: punt "+set.getClass().getName());
      processUnknownData(set);
      return;
    }

    int typeIndex;
    try {
      typeIndex = writeSetType(type, set);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write SetType " + type);
    }

    int csIndex = -1;
    if (cs != null) {
      csIndex = writeCoordinateSystem(cs);
    }

    int[] unitsIndex = null;
    if (units != null) {
      unitsIndex = writeUnits(units);
    }

    int[] errorsIndex = null;
    if (errors != null) {
      errorsIndex = writeErrorEstimates(errors);
    }

    try {
      file.writeByte(OBJ_DATA);
      file.writeByte(DATA_PRODUCT_SET);

      file.writeInt(typeIndex);

      file.writeByte(FLD_SET_SAMPLES);
      file.writeInt(sets.length);
      for (int i = 0; i < sets.length; i++) {
        process(sets[i]);
      }

      if (csIndex >= 0) {
        file.writeByte(FLD_INDEX_COORDSYS);
        file.writeInt(csIndex);
      }

      if (unitsIndex != null) {
        file.writeByte(FLD_INDEX_UNITS);
        writeIntegerArray(unitsIndex);
      }

      if (errorsIndex != null) {
        file.writeByte(FLD_INDEX_ERRORS);
        writeIntegerArray(errorsIndex);
      }

      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processReal(RealType type, double value, Unit unit,
                          ErrorEstimate error, Real real)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }
    int typeIndex;
    try {
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("wrRl: MathType (" + type + ")");
      typeIndex = writeRealType(type);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write RealType " + type);
    }

    try {
      int uIndex = -1;
      if (unit != null) {
if(DEBUG_DATA&&!DEBUG_UNIT)System.err.println("wrRl: Unit (" + unit + ")");
        uIndex = writeUnit(unit);
      }

      int errIndex = -1;
      if (error != null) {
if(DEBUG_DATA&&!DEBUG_ERRE)System.err.println("wrRl: ErrEst (" + error + ")");
        errIndex = writeErrorEstimate(error);
      }

if(DEBUG_DATA)System.err.println("wrRl: OBJ_DATA (" + OBJ_DATA + ")");
      file.writeByte(OBJ_DATA);
if(DEBUG_DATA)System.err.println("wrRl: DATA_REAL (" + DATA_REAL + ")");
      file.writeByte(DATA_REAL);

if(DEBUG_DATA)System.err.println("wrRl: type index (" + typeIndex + ")");
      file.writeInt(typeIndex);

if(DEBUG_DATA)System.err.println("wrRl: value (" + value + ")");
      file.writeDouble(value);

      if (uIndex >= 0) {
if(DEBUG_DATA)System.err.println("wrRl: FLD_INDEX_UNIT (" + FLD_INDEX_UNIT + ")");
        file.writeByte(FLD_INDEX_UNIT);
if(DEBUG_DATA)System.err.println("wrRl: unit index (" + uIndex + ")");
        file.writeInt(uIndex);
      }

      if (errIndex >= 0) {
if(DEBUG_DATA)System.err.println("wrRl: FLD_INDEX_ERROR (" + FLD_INDEX_ERROR + ")");
        file.writeByte(FLD_INDEX_ERROR);
if(DEBUG_DATA)System.err.println("wrRl: err index (" + errIndex + ")");
        file.writeInt(errIndex);
      }

if(DEBUG_DATA)System.err.println("wrRl: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + real.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processRealTuple(RealTupleType type, Real[] components,
                               CoordinateSystem cs, RealTuple rt)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (rt == null) {
      throw new VisADException("Null data object");
    }

    if (!rt.getClass().equals(RealTuple.class)) {
if(DEBUG_DATA)System.err.println("wrRlTpl: punt "+rt.getClass().getName());
      processUnknownData(rt);
      return;
    }

    int typeIndex;
    try {
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("wrRlTpl: MathType (" + type + ")");
      typeIndex = writeRealTupleType(type);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write RealTupleType " + type);
    }

    int csIndex = -1;
    if (cs != null) {
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("wrRlTpl: coordSys (" + cs + ")");
      csIndex = writeCoordinateSystem(cs);
    }

    boolean trivialTuple = true;
    if (components != null) {
      for (int i = 0; i < components.length; i++) {
        if (components[i] != null) {
          if (!type.getComponent(i).equals(components[i].getType()) ||
              components[i].getUnit() != null ||
              components[i].getError() != null)
          {
            trivialTuple = false;
            break;
          }
        }
      }
    }

    try {
if(DEBUG_DATA)System.err.println("wrRlTpl: OBJ_DATA (" + OBJ_DATA + ")");
      file.writeByte(OBJ_DATA);
if(DEBUG_DATA)System.err.println("wrRlTpl: DATA_REAL_TUPLE (" + DATA_REAL_TUPLE + ")");
      file.writeByte(DATA_REAL_TUPLE);

if(DEBUG_DATA)System.err.println("wrRlTpl: type index (" + typeIndex + ")");
      file.writeInt(typeIndex);

      if (components != null) {
        if (trivialTuple) {
if(DEBUG_DATA)System.err.println("wrRlTpl: FLD_TRIVIAL_SAMPLES (" + FLD_REAL_SAMPLES + ")");
          file.writeByte(FLD_TRIVIAL_SAMPLES);
if(DEBUG_DATA)System.err.println("wrRlTpl: len (" + components.length + ")");
          file.writeInt(components.length);
          for (int i = 0; i < components.length; i++) {
            file.writeDouble(components[i].getValue());
          }
        } else {
if(DEBUG_DATA)System.err.println("wrRlTpl: FLD_REAL_SAMPLES (" + FLD_REAL_SAMPLES + ")");
          file.writeByte(FLD_REAL_SAMPLES);
if(DEBUG_DATA)System.err.println("wrRlTpl: len (" + components.length + ")");
          file.writeInt(components.length);
          for (int i = 0; i < components.length; i++) {
            processReal((RealType )components[i].getType(),
                        components[i].getValue(), components[i].getUnit(),
                        components[i].getError(), components[i]);
          }
        }
      }

      if (csIndex >= 0) {
if(DEBUG_DATA)System.err.println("wrRlTpl: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_DATA)System.err.println("wrRlTpl: coord sys Index (" + csIndex + ")");
        file.writeInt(csIndex);
      }

if(DEBUG_DATA)System.err.println("wrRlTpl: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + rt.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processSampledSet(SetType st, int manifold_dimension,
                                CoordinateSystem cs, Unit[] units,
                                ErrorEstimate[] errors, SampledSet set)
    throws VisADException
  {
    processUnknownData(set);
  }

  public void processSimpleSet(SetType st, int manifold_dimension,
                               CoordinateSystem cs, Unit[] units,
                               ErrorEstimate[] errors, SimpleSet set)
    throws VisADException
  {
    processUnknownData(set);
  }

  public void processSingletonSet(RealTuple sample, CoordinateSystem cs,
                                  Unit[] units, ErrorEstimate[] errors,
                                  SingletonSet set)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    if (!set.getClass().equals(SingletonSet.class)) {
if(DEBUG_DATA)System.err.println("wrSglSet: punt "+set.getClass().getName());
      processUnknownData(set);
      return;
    }

    if (sample == null) {
      throw new VisADException("Null SingletonSet sample");
    }

    Data[] comps = sample.getComponents();
    Real[] sampleReals = null;
    if (comps != null) {
      sampleReals = new Real[comps.length];

      for (int i = 0; i < comps.length; i++) {
        sampleReals[i] = (Real )comps[i];
      }
    }

    int csIndex = -1;
    if (cs != null) {
      csIndex = writeCoordinateSystem(cs);
    }

    int[] unitsIndex = null;
    if (units != null) {
      unitsIndex = writeUnits(units);
    }

    int[] errorsIndex = null;
    if (errors != null) {
      errorsIndex = writeErrorEstimates(errors);
    }

    try {
if(DEBUG_DATA)System.err.println("wrSglSet: OBJ_DATA (" + OBJ_DATA + ")");
      file.writeByte(OBJ_DATA);
if(DEBUG_DATA)System.err.println("wrSglSet: DATA_SINGLETON_SET (" + DATA_SINGLETON_SET + ")");
      file.writeByte(DATA_SINGLETON_SET);

if(DEBUG_DATA)System.err.println("wrSglSet: FLD_SAMPLE (" + FLD_SAMPLE + ")");
      file.writeByte(FLD_SAMPLE);
      processRealTuple((RealTupleType )sample.getType(), sampleReals,
                       sample.getCoordinateSystem(), sample);

      if (csIndex >= 0) {
if(DEBUG_DATA)System.err.println("wrSglSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_DATA)System.err.println("wrSglSet: coord sys Index (" + csIndex + ")");
        file.writeInt(csIndex);
      }

      if (unitsIndex != null) {
if(DEBUG_DATA)System.err.println("wrSglSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        file.writeByte(FLD_INDEX_UNITS);
        writeIntegerArray(unitsIndex);
      }

      if (errorsIndex != null) {
if(DEBUG_DATA)System.err.println("wrSglSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        file.writeByte(FLD_INDEX_ERRORS);
        writeIntegerArray(errorsIndex);
      }

if(DEBUG_DATA)System.err.println("wrSglSet: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processText(TextType type, String value, boolean missing,
                          Text text)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    int typeIndex;
    try {
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("wrTxt: MathType (" + type + ")");
      typeIndex = writeTextType(type);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write TextType " + type);
    }

    try {
if(DEBUG_DATA)System.err.println("wrTxt: OBJ_DATA (" + OBJ_DATA + ")");
      file.writeByte(OBJ_DATA);
if(DEBUG_DATA)System.err.println("wrTxt: DATA_TEXT (" + DATA_TEXT + ")");
      file.writeByte(DATA_TEXT);

if(DEBUG_DATA)System.err.println("wrTxt: type index (" + typeIndex + ")");
      file.writeInt(typeIndex);

if(DEBUG_DATA)System.err.println("wrTxt: value (" + value + ")");
      writeString(value);

      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + text.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processTuple(TupleType type, Data[] components, Tuple t)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (t == null) {
      throw new VisADException("Null data object");
    }

    if (!t.getClass().equals(Tuple.class)) {
if(DEBUG_DATA)System.err.println("wrTup: punt "+t.getClass().getName());
      processUnknownData(t);
      return;
    }

    int typeIndex;
    try {
      typeIndex = writeTupleType(type);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write TupleType " + type);
    }

    try {
if(DEBUG_DATA)System.err.println("wrTup: OBJ_DATA (" + OBJ_DATA + ")");
      file.writeByte(OBJ_DATA);
if(DEBUG_DATA)System.err.println("wrTup: DATA_TUPLE (" + DATA_TUPLE + ")");
      file.writeByte(DATA_TUPLE);

if(DEBUG_DATA)System.err.println("wrTup: DATA_TUPLE (" + DATA_TUPLE + ")");
      file.writeInt(typeIndex);

      if (components != null) {
if(DEBUG_DATA)System.err.println("wrTup: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
        file.writeByte(FLD_DATA_SAMPLES);
if(DEBUG_DATA)System.err.println("wrTup: len (" + components.length + ")");
        file.writeInt(components.length);
        for (int i = 0; i < components.length; i++) {
          process((DataImpl )components[i]);
        }
      }

if(DEBUG_DATA)System.err.println("wrTup: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + t.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processUnionSet(SetType type, SampledSet[] sets, UnionSet set)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    if (!set.getClass().equals(UnionSet.class)) {
if(DEBUG_DATA)System.err.println("wrUSet: punt "+set.getClass().getName());
      processUnknownData(set);
      return;
    }

    int typeIndex;
    try {
      typeIndex = writeSetType(type, set);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write SetType " + type);
    }

    try {
      file.writeByte(OBJ_DATA);
      file.writeByte(DATA_UNION_SET);

      file.writeInt(typeIndex);

      file.writeByte(FLD_SET_SAMPLES);
      file.writeInt(sets.length);
      for (int i = 0; i < sets.length; i++) {
        process(sets[i]);
      }

      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  public void processUnknownData(DataImpl data)
    throws VisADException
  {
    try {
      writeSerializedObject(OBJ_DATA_SERIAL, data);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write Data object: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
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

  private int writeCoordinateSystem(CoordinateSystem cSys)
    throws VisADException
  {
    int index = cSysCache.getIndex(cSys);
    if (index >= 0) {
      return index;
    }

    // cache the CoordinateSystem so we can find its index number
    index = cSysCache.add(cSys);
    if (index < 0) {
      throw new VisADException("Couldn't cache CoordinateSystem " + cSys);
    }

    try {
if(DEBUG_CSYS)System.err.println("wrCSys: OBJ_COORDSYS (" + OBJ_COORDSYS + ")");
      file.writeByte(OBJ_COORDSYS);
if(DEBUG_CSYS)System.err.println("wrCSys: index (" + index + ")");
      file.writeInt(index);

if(DEBUG_CSYS)System.err.println("wrCSys: serialObj (" + serialObj.length + " bytes)");
      writeSerializedObject(FLD_COORDSYS_SERIAL, cSys);

if(DEBUG_CSYS)System.err.println("wrCSys: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write file: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }

    return index;
  }

  private int[] writeCoordinateSystems(CoordinateSystem[] cSys)
    throws VisADException
  {
    // make sure there's something to write
    boolean empty = true;
    for (int i = 0; i < cSys.length; i++) {
      if (cSys[i] != null) {
        empty = false;
        break;
      }
    }
    if (empty) {
      return null;
    }

    int[] indices = new int[cSys.length];

    for (int i = 0; i < cSys.length; i++) {
      if (cSys[i] == null) {
        indices[i] = -1;
      } else {
        indices[i] = writeCoordinateSystem(cSys[i]);
      }
    }

    return indices;
  }

  private void writeDelaunay(Delaunay delaunay)
    throws IOException, VisADException
  {
    Class dClass = delaunay.getClass();
    if (!dClass.equals(DelaunayClarkson.class) &&
        !dClass.equals(DelaunayCustom.class) &&
        !dClass.equals(DelaunayFast.class) &&
        !dClass.equals(DelaunayOverlap.class) &&
        !dClass.equals(DelaunayWatson.class))
    {
      /* serialize non-standard Delaunay object */
      writeSerializedObject(FLD_DELAUNAY_SERIAL, delaunay);
      return;
    }

    file.writeByte(FLD_DELAUNAY);

    file.writeByte(FLD_DELAUNAY_TRI);
    writeIntegerMatrix(delaunay.Tri);

    file.writeByte(FLD_DELAUNAY_VERTICES);
    writeIntegerMatrix(delaunay.Vertices);

    file.writeByte(FLD_DELAUNAY_WALK);
    writeIntegerMatrix(delaunay.Walk);

    file.writeByte(FLD_DELAUNAY_EDGES);
    writeIntegerMatrix(delaunay.Edges);

    file.writeByte(FLD_DELAUNAY_NUM_EDGES);
    file.writeInt(delaunay.NumEdges);

    file.writeByte(FLD_END);
  }

  private int writeDisplayRealType(DisplayRealType drt)
    throws IOException, VisADException
  {
    int index = typeCache.getIndex(drt);
    if (index < 0) {
      index = cacheMathType(drt);

if(DEBUG_MATH)System.err.println("wrDpyRTy: serialized DisplayRealType");
      writeSerializedObject(OBJ_MATH_SERIAL, drt);
    }

    return index;
  }

  private int writeDisplayTupleType(DisplayTupleType dtt)
    throws IOException, VisADException
  {
    int index = typeCache.getIndex(dtt);
    if (index < 0) {
      index = cacheMathType(dtt);

if(DEBUG_MATH)System.err.println("wrDpyTuTy: serialized DisplayTupleType");
      writeSerializedObject(OBJ_MATH_SERIAL, dtt);
    }

    return index;
  }

  private void writeDoubleArray(double[] array)
    throws IOException
  {
if(DEBUG_DATA)System.err.println("wrDblRA: len (" + array.length + ")");
    file.writeInt(array.length);
    for (int i = 0; i < array.length; i++) {
if(DEBUG_DATA_DETAIL)System.err.println("wrDblRA: #" + i + " (" + array[i] + ")");
      file.writeDouble(array[i]);
    }
  }

  private void writeDoubleMatrix(double[][] matrix)
    throws IOException
  {
    if (matrix == null) {
if(DEBUG_DATA)System.err.println("wrDblMtx: null (" + -1 + ")");
      file.writeInt(-1);
    } else {
if(DEBUG_DATA)System.err.println("wrDblMtx: row len (" + matrix.length + ")");
      file.writeInt(matrix.length);
      for (int i = 0; i < matrix.length; i++) {
        final int len = matrix[i].length;
if(DEBUG_DATA)System.err.println("wrDblMtx: #" + i + " len (" + matrix[i].length + ")");
        file.writeInt(len);
        for (int j = 0; j < len; j++) {
if(DEBUG_DATA_DETAIL)System.err.println("wrDblMtx: #" + i + "," + j + " (" + matrix[i][j] + ")");
          file.writeDouble(matrix[i][j]);
        }
      }
    }
  }

  private int writeErrorEstimate(ErrorEstimate error)
    throws VisADException
  {
    int index = errorCache.getIndex(error);
    if (index >= 0) {
      return index;
    }

    // cache the ErrorEstimate so we can find its index number
    index = errorCache.add(error);
    if (index < 0) {
      throw new VisADException("Couldn't cache ErrorEstimate " + error);
    }

    double errValue = error.getErrorValue();
    double mean = error.getMean();
    long number = error.getNumberNotMissing();
    Unit unit = error.getUnit();

    try {
      int uIndex = -1;
      if (unit != null) {
        uIndex = writeUnit(unit);
      }

if(DEBUG_ERRE)System.err.println("wrErrEst: OBJ_ERROR (" + OBJ_ERROR + ")");
      file.writeByte(OBJ_ERROR);
if(DEBUG_ERRE)System.err.println("wrErrEst: index (" + index + ")");
      file.writeInt(index);

if(DEBUG_ERRE)System.err.println("wrErrEst: error value (" + errValue + ")");
      file.writeDouble(errValue);
if(DEBUG_ERRE)System.err.println("wrErrEst: error mean (" + mean + ")");
      file.writeDouble(mean);
if(DEBUG_ERRE)System.err.println("wrErrEst: error number (" + number + ")");
      file.writeLong(number);

      if (uIndex >= 0) {
if(DEBUG_ERRE)System.err.println("wrErrEst: FLD_INDEX_UNIT (" + FLD_INDEX_UNIT + ")");
        file.writeByte(FLD_INDEX_UNIT);
if(DEBUG_ERRE)System.err.println("wrErrEst: unit index (" + uIndex + ")");
        file.writeInt(uIndex);
      }

if(DEBUG_ERRE)System.err.println("wrErrEst: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write file: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }

    return index;
  }

  private int[] writeErrorEstimates(ErrorEstimate[] errors)
    throws VisADException
  {
    // make sure there's something to write
    boolean empty = true;
    for (int i = 0; i < errors.length; i++) {
      if (errors[i] != null) {
        empty = false;
        break;
      }
    }
    if (empty) {
      return null;
    }

    int[] indices = new int[errors.length];

    for (int i = 0; i < errors.length; i++) {
      if (errors[i] == null) {
        indices[i] = -1;
      } else {
        indices[i] = writeErrorEstimate(errors[i]);
      }
    }

    return indices;
  }

  private void writeFloatArray(float[] array)
    throws IOException
  {
if(DEBUG_DATA)System.err.println("wrFltRA: len (" + array.length + ")");
    file.writeInt(array.length);
    for (int i = 0; i < array.length; i++) {
if(DEBUG_DATA_DETAIL)System.err.println("wrFltRA: #" + i + " (" + array[i] + ")");
      file.writeFloat(array[i]);
    }
  }

  private void writeFloatMatrix(float[][] matrix)
    throws IOException
  {
    if (matrix == null) {
if(DEBUG_DATA)System.err.println("wrFltMtx: null (" + -1 + ")");
      file.writeInt(-1);
    } else {
if(DEBUG_DATA)System.err.println("wrFltMtx: row len (" + matrix.length + ")");
      file.writeInt(matrix.length);
      for (int i = 0; i < matrix.length; i++) {
        final int len = matrix[i].length;
if(DEBUG_DATA)System.err.println("wrFltMtx: #" + i + " len (" + matrix[i].length + ")");
        file.writeInt(len);
        for (int j = 0; j < len; j++) {
if(DEBUG_DATA_DETAIL)System.err.println("wrFltMtx: #" + i + "," + j + " (" + matrix[i][j] + ")");
          file.writeFloat(matrix[i][j]);
        }
      }
    }
  }

  private int writeFunctionType(FunctionType ft)
    throws IOException, VisADException
  {
    int index = typeCache.getIndex(ft);
    if (index < 0) {
      index = cacheMathType(ft);

      int dIndex = writeMathType(ft.getDomain());
      int rIndex = writeMathType(ft.getRange());

if(DEBUG_MATH)System.err.println("wrFuTy: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_MATH)System.err.println("wrFuTy: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_MATH)System.err.println("wrFuTy: MATH_FUNCTION (" + MATH_FUNCTION + ")");
      file.writeByte(MATH_FUNCTION);

if(DEBUG_MATH)System.err.println("wrFuTy: domain index (" + dIndex + ")");
      file.writeInt(dIndex);
if(DEBUG_MATH)System.err.println("wrFuTy: range index (" + rIndex + ")");
      file.writeInt(rIndex);

if(DEBUG_MATH)System.err.println("wrFuTy: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    }

    return index;
  }

  private void writeGriddedDoubleSet(SetType type, double[][] samples,
                                     int[] lengths, CoordinateSystem cs,
                                     Unit[] units, ErrorEstimate[] errors,
                                     GriddedSet set, Class canonicalClass,
                                     byte dataType)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    if (!set.getClass().equals(canonicalClass)) {
if(DEBUG_DATA)System.err.println("wrGrDblSet: punt "+set.getClass().getName());
      processUnknownData(set);
      return;
    }

    if (lengths == null) {
      throw new VisADException("Null " + canonicalClass.getName() +
                               " lengths");
    }

    final int validLen;
    switch (dataType) {
    case DATA_GRIDDED_1D_DOUBLE_SET:
      validLen = 1;
      break;
    case DATA_GRIDDED_2D_DOUBLE_SET:
      validLen = 2;
      break;
    case DATA_GRIDDED_3D_DOUBLE_SET:
      validLen = 3;
      break;
    default:
      throw new VisADException("Type " + dataType +
                               " not valid for writeGriddedDoubleSet()");
    }

    if (samples != null && samples.length != validLen) {
      throw new VisADException("Expected " + validLen + " sample list" +
                               (validLen > 1 ? "s" : "") + ", not " +
                               samples.length);
    }

    int typeIndex;
    try {
      typeIndex = writeSetType(type, set);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write SetType " + type);
    }

    int csIndex = -1;
    if (cs != null) {
      csIndex = writeCoordinateSystem(cs);
    }

    int[] unitsIndex = null;
    if (units != null) {
      unitsIndex = writeUnits(units);
    }

    int[] errorsIndex = null;
    if (errors != null) {
      errorsIndex = writeErrorEstimates(errors);
    }

    try {
if(DEBUG_DATA)System.err.println("wrGrDblSet: OBJ_DATA (" + OBJ_DATA + ")");
      file.writeByte(OBJ_DATA);
if(DEBUG_DATA)System.err.println("wrGrDblSet: " +
                                 (dataType == DATA_GRIDDED_1D_DOUBLE_SET ?
                                  "DATA_GRIDDED_1D_DOUBLE" :
                                  (dataType == DATA_GRIDDED_2D_DOUBLE_SET ?
                                   "DATA_GRIDDED_2D_DOUBLE" :
                                   (dataType == DATA_GRIDDED_3D_DOUBLE_SET ?
                                    "DATA_GRIDDED_3D_DOUBLE" : "DATA_???"))) +
                                 "(" + dataType + ")");
      file.writeByte(dataType);

if(DEBUG_DATA)System.err.println("wrGrDblSet: type index (" + typeIndex + ")");
      file.writeInt(typeIndex);

if(DEBUG_DATA)System.err.println("wrGrDblSet: FLD_DOUBLE_SAMPLES (" + FLD_DOUBLE_SAMPLES + ")");
      file.writeByte(FLD_DOUBLE_SAMPLES);
      writeDoubleMatrix(samples);

if(DEBUG_DATA)System.err.println("wrGrDblSet: FLD_LENGTHS (" + FLD_LENGTHS + ")");
      file.writeByte(FLD_LENGTHS);
      writeIntegerArray(lengths);

      if (csIndex >= 0) {
if(DEBUG_DATA)System.err.println("wrGrDblSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_DATA)System.err.println("wrGrDblSet: coord sys Index (" + csIndex + ")");
        file.writeInt(csIndex);
      }

      if (unitsIndex != null) {
if(DEBUG_DATA)System.err.println("wrGrDblSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        file.writeByte(FLD_INDEX_UNITS);
        writeIntegerArray(unitsIndex);
      }

      if (errorsIndex != null) {
if(DEBUG_DATA)System.err.println("wrGrDblSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        file.writeByte(FLD_INDEX_ERRORS);
        writeIntegerArray(errorsIndex);
      }

if(DEBUG_DATA)System.err.println("wrGrDblSet: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  private void writeGriddedSet(SetType type, float[][] samples,
                               int[] lengths, CoordinateSystem cs,
                               Unit[] units, ErrorEstimate[] errors,
                               GriddedSet set, Class canonicalClass,
                               byte dataType)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    if (!set.getClass().equals(canonicalClass)) {
if(DEBUG_DATA)System.err.println("wrGrSet: punt "+set.getClass().getName());
      processUnknownData(set);
      return;
    }

    if (lengths == null) {
      throw new VisADException("Null " + canonicalClass.getName() +
                               " lengths");
    }

    final int validLen;
    switch (dataType) {
    case DATA_GRIDDED_1D_SET:
      validLen = 1;
      break;
    case DATA_GRIDDED_2D_SET:
      validLen = 2;
      break;
    case DATA_GRIDDED_3D_SET:
      validLen = 3;
      break;
    case DATA_GRIDDED_SET:
      validLen = -1;
      break;
    default:
      throw new VisADException("Type " + dataType +
                               " not valid for writeGriddedSet()");
    }

    if (samples != null && validLen > 0 && samples.length != validLen) {
      throw new VisADException("Expected " + validLen + " sample list" +
                               (validLen > 1 ? "s" : "") + ", not " +
                               samples.length);
    }

    int typeIndex;
    try {
      typeIndex = writeSetType(type, set);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write SetType " + type);
    }

    int csIndex = -1;
    if (cs != null) {
      csIndex = writeCoordinateSystem(cs);
    }

    int[] unitsIndex = null;
    if (units != null) {
      unitsIndex = writeUnits(units);
    }

    int[] errorsIndex = null;
    if (errors != null) {
      errorsIndex = writeErrorEstimates(errors);
    }

    try {
      file.writeByte(OBJ_DATA);
      file.writeByte(dataType);

      file.writeInt(typeIndex);

      file.writeByte(FLD_FLOAT_SAMPLES);
      writeFloatMatrix(samples);

      file.writeByte(FLD_LENGTHS);
      writeIntegerArray(lengths);

      if (csIndex >= 0) {
        file.writeByte(FLD_INDEX_COORDSYS);
        file.writeInt(csIndex);
      }

      if (unitsIndex != null) {
        file.writeByte(FLD_INDEX_UNITS);
        writeIntegerArray(unitsIndex);
      }

      if (errorsIndex != null) {
        file.writeByte(FLD_INDEX_ERRORS);
        writeIntegerArray(errorsIndex);
      }

      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  private void writeIntegerArray(int[] array)
    throws IOException
  {
if(DEBUG_DATA)System.err.println("wrIntRA: len (" + array.length + ")");
    file.writeInt(array.length);
    for (int i = 0; i < array.length; i++) {
if(DEBUG_DATA_DETAIL)System.err.println("wrIntRA: #" + i + " (" + array[i] + ")");
      file.writeInt(array[i]);
    }
  }

  private void writeIntegerMatrix(int[][] matrix)
    throws IOException
  {
    if (matrix == null) {
if(DEBUG_DATA)System.err.println("wrIntMtx: null (" + -1 + ")");
      file.writeInt(-1);
    } else {
if(DEBUG_DATA)System.err.println("wrIntMtx: row len (" + matrix.length + ")");
      file.writeInt(matrix.length);
      for (int i = 0; i < matrix.length; i++) {
        final int len = matrix[i].length;
if(DEBUG_DATA)System.err.println("wrIntMtx: #" + i + " len (" + matrix[i].length + ")");
        file.writeInt(len);
        for (int j = 0; j < len; j++) {
if(DEBUG_DATA_DETAIL)System.err.println("wrIntMtx: #" + i + "," + j + " (" + matrix[i][j] + ")");
          file.writeInt(matrix[i][j]);
        }
      }
    }
  }

  private void writeIntegerSet(SetType type, int[] lengths,
                               Integer1DSet[] comps, CoordinateSystem cs,
                               Unit[] units, ErrorEstimate[] errors,
                               GriddedSet set, Class canonicalClass,
                               byte dataType)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    if (!set.getClass().equals(canonicalClass)) {
if(DEBUG_DATA)System.err.println("wrIntSet: punt "+set.getClass().getName());
      processUnknownData(set);
      return;
    }

    // see if domain types and component types match
    boolean matchedTypes = true;
    if (comps != null) {
      MathType[] dComp = type.getDomain().getComponents();
      if (dComp == null || dComp.length != comps.length) {
        throw new VisADException("Expected " + comps.length +
                                 " components in IntegerSet domain " +
                                 type.getDomain());
      }
      for (int i = 0; i < dComp.length; i++) {
        if (!dComp[i].equals(comps[i].getType())) {
          matchedTypes = false;
          break;
        }
      }
    }

    final int dim = set.getDimension();

    if (!matchedTypes) {
      if (dataType == DATA_INTEGER_1D_SET) {
        throw new VisADException("Components specified for Integer1DSet");
      }

      if (comps.length != dim) {
        throw new VisADException("Expected " + dim + " IntegerSet component" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 comps.length);
      }
    } else {
      if (lengths == null) {
        throw new VisADException("Null " + canonicalClass.getName() +
                                 " lengths");
      }

      if (lengths.length != dim) {
        throw new VisADException("Expected " + dim + " IntegerSet length" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 lengths.length);
      }
    }

    int typeIndex;
    try {
      typeIndex = writeSetType(type, set);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write SetType " + type);
    }

    int csIndex = -1;
    if (cs != null) {
      csIndex = writeCoordinateSystem(cs);
    }

    int[] unitsIndex = null;
    if (units != null) {
      unitsIndex = writeUnits(units);
    }

    int[] errorsIndex = null;
    if (errors != null) {
      errorsIndex = writeErrorEstimates(errors);
    }

    try {
if(DEBUG_DATA)System.err.println("wrIntSet: OBJ_DATA (" + OBJ_DATA + ")");
      file.writeByte(OBJ_DATA);
if(DEBUG_DATA)System.err.println("wrIntSet: " +
                                 (dataType == DATA_INTEGER_1D_SET ?
                                  "DATA_INTEGER_1D_SET" :
                                  (dataType == DATA_INTEGER_2D_SET ?
                                   "DATA_INTEGER_2D_SET" :
                                   (dataType == DATA_INTEGER_3D_SET ?
                                    "DATA_INTEGER_3D_SET" :
                                    (dataType == DATA_INTEGER_ND_SET ?
                                     "DATA_INTEGER_ND_SET" : "DATA_???")))) +
                                 "(" + dataType + ")");
      file.writeByte(dataType);

if(DEBUG_DATA)System.err.println("wrIntSet: type index (" + typeIndex + ")");
      file.writeInt(typeIndex);

      if (matchedTypes) {
if(DEBUG_DATA)System.err.println("wrIntSet: FLD_LENGTHS (" + FLD_LENGTHS + ")");
        file.writeByte(FLD_LENGTHS);
        writeIntegerArray(lengths);
      } else {
if(DEBUG_DATA)System.err.println("wrIntSet: FLD_INTEGER_SETS (" + FLD_INTEGER_SETS + ")");
        file.writeByte(FLD_INTEGER_SETS);
if(DEBUG_DATA)System.err.println("wrIntSet: set length (" + comps.length + ")");
        file.writeInt(comps.length);
        for (int i = 0; i < comps.length; i++) {
          process(comps[i]);
        }
      }

      if (csIndex >= 0) {
if(DEBUG_DATA)System.err.println("wrIntSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_DATA)System.err.println("wrIntSet: coord sys Index (" + csIndex + ")");
        file.writeInt(csIndex);
      }

      if (unitsIndex != null) {
if(DEBUG_DATA)System.err.println("wrIntSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        file.writeByte(FLD_INDEX_UNITS);
        writeIntegerArray(unitsIndex);
      }

      if (errorsIndex != null) {
if(DEBUG_DATA)System.err.println("wrIntSet: FLD_INDEX_ERRORS (" + FLD_INDEX_ERRORS + ")");
        file.writeByte(FLD_INDEX_ERRORS);
        writeIntegerArray(errorsIndex);
      }

if(DEBUG_DATA)System.err.println("wrIntSet: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  private void writeIrregularSet(SetType type, float[][] samples,
                                 CoordinateSystem cs, Unit[] units,
                                 ErrorEstimate[] errors, Delaunay delaunay,
                                 IrregularSet set, Class canonicalClass,
                                 byte dataType)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    if (!set.getClass().equals(canonicalClass)) {
if(DEBUG_DATA)System.err.println("wrIrrSet: punt "+set.getClass().getName());
      processUnknownData(set);
      return;
    }

    if (samples == null) {
      throw new VisADException("Null " + canonicalClass.getName() +
                               " samples");
    }

    final int validLen;
    switch (dataType) {
    case DATA_IRREGULAR_1D_SET:
      validLen = 1;
      break;
    case DATA_IRREGULAR_2D_SET:
      validLen = 2;
      break;
    case DATA_IRREGULAR_3D_SET:
      validLen = 3;
      break;
    case DATA_IRREGULAR_SET:
      validLen = -1;
      break;
    default:
      throw new VisADException("Type " + dataType +
                               " not valid for writeIrregularSet()");
    }

    if (validLen > 0 && samples.length != validLen) {
      throw new VisADException("Expected " + validLen + " sample list" +
                               (validLen > 1 ? "s" : "") + ", not " +
                               samples.length);
    }

    int typeIndex;
    try {
      typeIndex = writeSetType(type, set);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write SetType " + type);
    }

    int csIndex = -1;
    if (cs != null) {
      csIndex = writeCoordinateSystem(cs);
    }

    int[] unitsIndex = null;
    if (units != null) {
      unitsIndex = writeUnits(units);
    }

    int[] errorsIndex = null;
    if (errors != null) {
      errorsIndex = writeErrorEstimates(errors);
    }

    try {
      file.writeByte(OBJ_DATA);
      file.writeByte(dataType);

      file.writeInt(typeIndex);

      file.writeByte(FLD_FLOAT_SAMPLES);
      writeFloatMatrix(samples);

      if (csIndex >= 0) {
        file.writeByte(FLD_INDEX_COORDSYS);
        file.writeInt(csIndex);
      }

      if (unitsIndex != null) {
        file.writeByte(FLD_INDEX_UNITS);
        writeIntegerArray(unitsIndex);
      }

      if (errorsIndex != null) {
        file.writeByte(FLD_INDEX_ERRORS);
        writeIntegerArray(errorsIndex);
      }

      if (delaunay != null) {
        writeDelaunay(delaunay);
      }

      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  private void writeLinearSet(SetType type, double[] firsts, double[] lasts,
                              int[] lengths, Linear1DSet[] comps,
                              CoordinateSystem cs, Unit[] units,
                              ErrorEstimate[] errors, GriddedSet set,
                              Class canonicalClass, byte dataType)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    if (!set.getClass().equals(canonicalClass)) {
if(DEBUG_DATA)System.err.println("wrLinSet: punt "+set.getClass().getName());
      processUnknownData(set);
      return;
    }

    // see if domain types and component types match
    boolean matchedTypes = true;
    if (comps != null) {
      MathType[] dComp = type.getDomain().getComponents();
      if (dComp == null || dComp.length != comps.length) {
        throw new VisADException("Expected " + comps.length +
                                 " components in LinearSet domain " +
                                 type.getDomain());
      }
      for (int i = 0; i < dComp.length; i++) {
        if (!dComp[i].equals(comps[i].getType())) {
          matchedTypes = false;
          break;
        }
      }
    }

    final int dim = set.getDimension();

    if (!matchedTypes) {
      if (dataType == DATA_LINEAR_1D_SET) {
        throw new VisADException("Components specified for Linear1DSet");
      }

      if (comps.length != dim) {
        throw new VisADException("Expected " + dim + " LinearSet component" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 comps.length);
      }
    } else {
      if (firsts == null) {
        throw new VisADException("Null " + canonicalClass.getName() +
                                 " firsts");
      }
      if (lasts == null) {
        throw new VisADException("Null " + canonicalClass.getName() +
                                 " lasts");
      }
      if (lengths == null) {
        throw new VisADException("Null " + canonicalClass.getName() +
                                 " lengths");
      }

      if (firsts.length != dim) {
        throw new VisADException("Expected " + dim +
                                 " LinearSet first value" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 firsts.length);
      }
      if (lasts.length != dim) {
        throw new VisADException("Expected " + dim +
                                 " LinearSet last value" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 lasts.length);
      }
      if (lengths.length != dim) {
        throw new VisADException("Expected " + dim +
                                 " LinearSet length" +
                                 (dim > 1 ? "s" : "") + ", not " +
                                 lengths.length);
      }
    }

    int typeIndex;
    try {
      typeIndex = writeSetType(type, set);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write SetType " + type);
    }

    int csIndex = -1;
    if (cs != null) {
      csIndex = writeCoordinateSystem(cs);
    }

    int[] unitsIndex = null;
    if (units != null) {
      unitsIndex = writeUnits(units);
    }

    int[] errorsIndex = null;
    if (errors != null) {
      errorsIndex = writeErrorEstimates(errors);
    }

    try {
      file.writeByte(OBJ_DATA);
      file.writeByte(dataType);

      file.writeInt(typeIndex);

      if (matchedTypes) {
        file.writeByte(FLD_FIRSTS);
        writeDoubleArray(firsts);

        file.writeByte(FLD_LASTS);
        writeDoubleArray(lasts);

        file.writeByte(FLD_LENGTHS);
        writeIntegerArray(lengths);
      } else {
        file.writeByte(FLD_LINEAR_SETS);
        file.writeInt(comps.length);
        for (int i = 0; i < comps.length; i++) {
          process(comps[i]);
        }
      }

      if (csIndex >= 0) {
        file.writeByte(FLD_INDEX_COORDSYS);
        file.writeInt(csIndex);
      }

      if (unitsIndex != null) {
        file.writeByte(FLD_INDEX_UNITS);
        writeIntegerArray(unitsIndex);
      }

      if (errorsIndex != null) {
        file.writeByte(FLD_INDEX_ERRORS);
        writeIntegerArray(errorsIndex);
      }

      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  private int writeMathType(MathType mt)
    throws IOException, VisADException
  {
    int index;

    if (mt instanceof FunctionType) {
      index = writeFunctionType((FunctionType )mt);
    } else if (mt instanceof ScalarType) {
      index = writeScalarType((ScalarType )mt);
    } else if (mt instanceof SetType) {
      index = writeSetType((SetType )mt, null);
    } else if (mt instanceof TupleType) {
      index = writeTupleType((TupleType )mt);
    } else {
      index = typeCache.getIndex(mt);
      if (index < 0) {
        index = cacheMathType(mt);
        writeSerializedObject(OBJ_MATH_SERIAL, mt);
      }
    }

    return index;
  }

  private int writeQuantity(Quantity qt)
    throws IOException, VisADException
  {
    int index = typeCache.getIndex(qt);
    if (index < 0) {
      index = cacheMathType(qt);

      if (!qt.getClass().equals(Quantity.class)) {
if(DEBUG_MATH)System.err.println("wrQuant: serialized Quantity (" + qt.getClass().getName() + ")");
        writeSerializedObject(OBJ_MATH_SERIAL, qt);
        return index;
      }

      String nameStr = qt.getName();
      String unitStr = qt.getDefaultUnitString();

      Set dfltSet = qt.getDefaultSet();

if(DEBUG_MATH)System.err.println("wrQuant: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_MATH)System.err.println("wrQuant: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_MATH)System.err.println("wrQuant: MATH_QUANTITY (" + MATH_QUANTITY + ")");
      file.writeByte(MATH_QUANTITY);

if(DEBUG_MATH)System.err.println("wrQuant: name (" + nameStr + ")");
      writeString(nameStr);

if(DEBUG_MATH)System.err.println("wrQuant: unitSpec (" + unitStr + ")");
      writeString(unitStr);

      if (dfltSet != null) {
if(DEBUG_MATH)System.err.println("wrQuant: FLD_SET_FOLLOWS_TYPE (" + FLD_SET_FOLLOWS_TYPE + ")");
        file.writeByte(FLD_SET_FOLLOWS_TYPE);
      }

if(DEBUG_MATH)System.err.println("wrQuant: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);

      if (dfltSet != null) {
        process(dfltSet);
      }
    }

    return index;
  }

  private int writeRealTupleType(RealTupleType rtt)
    throws IOException, VisADException
  {
    if (rtt instanceof DisplayTupleType) {
      return writeDisplayTupleType((DisplayTupleType )rtt);
    } else if (rtt instanceof RealVectorType) {
      return writeRealVectorType((RealVectorType )rtt);
    }

    int index = typeCache.getIndex(rtt);
    if (index < 0) {
      index = cacheMathType(rtt);

      if (!rtt.getClass().equals(RealTupleType.class)) {
if(DEBUG_MATH)System.err.println("wrRlTuTy: serialized RealTupleType (" + rtt.getClass().getName() + ")");
        writeSerializedObject(OBJ_MATH_SERIAL, rtt);
        return index;
      }

      final int dim = rtt.getDimension();

      Set dfltSet = rtt.getDefaultSet();

      int[] types = new int[dim];
      for (int i = 0; i < dim; i++) {
        RealType comp = (RealType )rtt.getComponent(i);
        types[i] = writeRealType(comp);
      }

      CoordinateSystem cs = rtt.getCoordinateSystem();

      int csIndex = -1;
      if (cs != null) {
if(DEBUG_MATH)System.err.println("wrRlTuTy: coordSys (" + cs + ")");
        csIndex = writeCoordinateSystem(cs);
      }

if(DEBUG_MATH)System.err.println("wrRlTuTy: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_MATH)System.err.println("wrRlTuTy: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_MATH)System.err.println("wrRlTuTy: MATH_REAL_TUPLE (" + MATH_REAL_TUPLE + ")");
      file.writeByte(MATH_REAL_TUPLE);

if(DEBUG_MATH)System.err.println("wrRlTuTy: dim (" + dim + ")");
      file.writeInt(dim);

      for (int i = 0; i < dim; i++) {
if(DEBUG_MATH)System.err.println("wrRlTuTy: tuple #" + i + " index (" + types[i] + ")");
        file.writeInt(types[i]);
      }

      if (csIndex >= 0) {
if(DEBUG_MATH)System.err.println("wrRlTuTy: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_MATH)System.err.println("wrRlTuTy: coordSys index (" + csIndex + ")");
        file.writeInt(csIndex);
      }

      if (dfltSet != null) {
if(DEBUG_MATH)System.err.println("wrRlTuTy: FLD_SET_FOLLOWS_TYPE (" + FLD_SET_FOLLOWS_TYPE + ")");
        file.writeByte(FLD_SET_FOLLOWS_TYPE);
      }

if(DEBUG_MATH)System.err.println("wrRlTuTy: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);

      if (dfltSet != null) {
        process(dfltSet);
      }
    }

    return index;
  }

  private int writeRealType(RealType rt)
    throws IOException, VisADException
  {
    if (rt instanceof DisplayRealType) {
      return writeDisplayRealType((DisplayRealType )rt);
    } else if (rt instanceof Quantity) {
      return writeQuantity((Quantity )rt);
    }

    int index = typeCache.getIndex(rt);
    if (index < 0) {
      index = cacheMathType(rt);

      if (!rt.getClass().equals(RealType.class)) {
if(DEBUG_MATH)System.err.println("wrRlTy: serialized RealType (" + rt.getClass().getName() + ")");
        writeSerializedObject(OBJ_MATH_SERIAL, rt);
        return index;
      }

      String name = rt.getName();

      Set dfltSet = rt.getDefaultSet();

      int uIndex = -1;
      Unit u = rt.getDefaultUnit();
      if (u != null) {
if(DEBUG_MATH&&!DEBUG_UNIT)System.err.println("wrRlTy: Unit (" + u + ")");
        uIndex = writeUnit(u);
      }

if(DEBUG_MATH)System.err.println("wrRlTy: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_MATH)System.err.println("wrRlTy: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_MATH)System.err.println("wrRlTy: MATH_REAL (" + MATH_REAL + ")");
      file.writeByte(MATH_REAL);

if(DEBUG_MATH)System.err.println("wrRlTy: attrMask (" + rt.getAttributeMask() + ")");
      file.writeInt(rt.getAttributeMask());

if(DEBUG_MATH)System.err.println("wrRlTy: name (" + name + ")");
      writeString(name);

      if (uIndex >= 0) {
if(DEBUG_MATH)System.err.println("wrRlTy: FLD_INDEX_UNIT (" + FLD_INDEX_UNIT + ")");
        file.writeByte(FLD_INDEX_UNIT);
if(DEBUG_MATH)System.err.println("wrRlTy: unit index ("+uIndex+"="+u+")");
        file.writeInt(uIndex);
      }

      if (dfltSet != null) {
if(DEBUG_MATH)System.err.println("wrRlTy: FLD_SET_FOLLOWS_TYPE (" + FLD_SET_FOLLOWS_TYPE + ")");
        file.writeByte(FLD_SET_FOLLOWS_TYPE);
      }

if(DEBUG_MATH)System.err.println("wrRlTy: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);

      if (dfltSet != null) {
        process(dfltSet);
      }
    }

    return index;
  }

  private int writeRealVectorType(RealVectorType rvt)
    throws IOException, VisADException
  {
    int index = typeCache.getIndex(rvt);
    if (index < 0) {
      index = cacheMathType(rvt);

if(DEBUG_MATH)System.err.println("wrRlVeTy: serialized RealVectorType (" + rvt.getClass().getName() + ")");
      writeSerializedObject(OBJ_MATH_SERIAL, rvt);
    }

    return index;
  }

  private int writeScalarType(ScalarType st)
    throws IOException, VisADException
  {
    if (st instanceof RealType) {
      return writeRealType((RealType )st);
    } else if (st instanceof TextType) {
      return writeTextType((TextType )st);
    }

    int index = typeCache.getIndex(st);
    if (index < 0) {
      index = cacheMathType(st);

if(DEBUG_MATH)System.err.println("wrScTy: serialized ScalarType (" + st.getClass().getName() + ")");
      writeSerializedObject(OBJ_MATH_SERIAL, st);
    }

    return index;
  }

  private void writeSerializedObject(byte objType, Object obj)
    throws IOException
  {
    if (file == null) {
      throw new IOException("No active file");
    }

    java.io.ByteArrayOutputStream outBytes;
    outBytes = new java.io.ByteArrayOutputStream();

    java.io.ObjectOutputStream outStream;
    outStream = new java.io.ObjectOutputStream(outBytes);

    outStream.writeObject(obj);
    outStream.flush();
    outStream.close();

    byte[] bytes = outBytes.toByteArray();

    file.writeByte(objType);
    file.writeInt(bytes.length);
    for (int i = 0; i < bytes.length; i++) {
      file.writeByte(bytes[i]);
    }

System.err.println("Wrote serialized " + obj.getClass().getName());
if(obj instanceof FloatSet||obj instanceof LinearNDSet)Thread.dumpStack();
  }

  private void writeSet(Set set)
    throws IOException
  {
    writeSerializedObject(OBJ_DATA_SERIAL, set);
  }

  private int writeSetType(SetType st, Set set)
    throws IOException, VisADException
  {
    int index = typeCache.getIndex(st);
    if (index < 0) {
      index = cacheMathType(st);

      if (!st.getClass().equals(SetType.class)) {
if(DEBUG_MATH)System.err.println("wrSetTy: serialized SetType (" + st.getClass().getName() + ")");
        writeSerializedObject(OBJ_MATH_SERIAL, st);
        return index;
      }

      int dIndex;
      MathType domain = st.getDomain();

      boolean isRealTupleType = false;
      if (domain instanceof RealTupleType) {
        RealTupleType rtt = (RealTupleType )domain;

        if (rtt.getDimension() == 1 &&
            rtt.getCoordinateSystem() == null &&
            rtt.getDefaultSet() == null)
        {
          // just use the RealType
          domain = rtt.getComponent(0);
        } else {
          // must really be a multi-dimensional RealTupleType
          isRealTupleType = true;
        }
      }

      if (isRealTupleType) {
        dIndex = writeRealTupleType((RealTupleType )domain);
      } else {
        dIndex = writeMathType(domain);
      }

if(DEBUG_MATH)System.err.println("wrSetTy: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_MATH)System.err.println("wrSetTy: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_MATH)System.err.println("wrSetTy: MATH_SET (" + MATH_SET + ")");
      file.writeByte(MATH_SET);

if(DEBUG_MATH)System.err.println("wrSetTy: domain index (" + dIndex + ")");
      file.writeInt(dIndex);

if(DEBUG_MATH)System.err.println("wrSetTy: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    }

    return index;
  }

  private void writeSimpleSet(SetType type, CoordinateSystem cs,
                              Unit[] units, SimpleSet set,
                              Class canonicalClass, byte dataType)
    throws VisADException
  {
    if (file == null) {
      throw new VisADException("No active file");
    }

    if (set == null) {
      throw new VisADException("Null data object");
    }

    if (!set.getClass().equals(canonicalClass)) {
if(DEBUG_DATA)System.err.println("wrSimSet: punt "+set.getClass().getName());
      processUnknownData(set);
      return;
    }

    int typeIndex;
    try {
if(DEBUG_DATA&&!DEBUG_MATH)System.err.println("wrSimSet: type (" + type + ")");
      typeIndex = writeSetType(type, set);
    } catch (IOException ie) {
      throw new VisADException("Couldn't write SetType " + type);
    }

    int csIndex = -1;
    if (cs != null) {
if(DEBUG_DATA&&!DEBUG_CSYS)System.err.println("wrSimSet: coordSys (" + cs + ")");
      csIndex = writeCoordinateSystem(cs);
    }

    int[] unitsIndex = null;
    if (units != null) {
if(DEBUG_DATA&&!DEBUG_UNIT){
  System.err.println("wrSimSet: List of " + units.length + " Units");
  for(int x=0;x<units.length;x++){
    System.err.println("wrSimSet:    #"+x+": "+units[x]);
  }
}
      unitsIndex = writeUnits(units);
    }

    try {
if(DEBUG_DATA)System.err.println("wrSimSet: OBJ_DATA (" + OBJ_DATA + ")");
      file.writeByte(OBJ_DATA);
if(DEBUG_DATA)System.err.println("wrSimSet: dataType (" + dataType + ")");
      file.writeByte(dataType);

if(DEBUG_DATA)System.err.println("wrSimSet: type index (" + typeIndex + ")");
      file.writeInt(typeIndex);

      if (csIndex >= 0) {
if(DEBUG_DATA)System.err.println("wrSimSet: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_DATA)System.err.println("wrSimSet: coord sys Index (" + csIndex + ")");
        file.writeInt(csIndex);
      }

      if (unitsIndex != null) {
if(DEBUG_DATA)System.err.println("wrSimSet: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        file.writeByte(FLD_INDEX_UNITS);
if(DEBUG_DATA)System.err.println("wrSimSet: array length ("+unitsIndex.length+")");
if(DEBUG_DATA)for(int i=0;i<unitsIndex.length;i++)System.err.println("wrSimSet:    unit #"+i+" index ("+unitsIndex[i]+"="+units[i]+")");
        writeIntegerArray(unitsIndex);
      }

if(DEBUG_DATA)System.err.println("wrSimSet: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write " + set.getClass().getName() +
                               ": " + ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }
  }

  private void writeString(String str)
    throws IOException
  {
    if (str == null) {
      file.writeInt(-1);
    } else {
      file.writeInt(str.length());
      if (str.length() > 0) {
        file.writeChars(str);
      }
    }
  }

  private int writeTextType(TextType tt)
    throws IOException, VisADException
  {
    int index = typeCache.getIndex(tt);
    if (index < 0) {
      index = cacheMathType(tt);

      if (!tt.getClass().equals(TextType.class)) {
if(DEBUG_MATH)System.err.println("wrTxTy: serialized TextType (" + tt.getClass().getName() + ")");
        writeSerializedObject(OBJ_MATH_SERIAL, tt);
        return index;
      }

      String name = tt.getName();

if(DEBUG_MATH)System.err.println("wrTxTy: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_MATH)System.err.println("wrTxTy: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_MATH)System.err.println("wrTxTy: MATH_TEXT (" + MATH_TEXT + ")");
      file.writeByte(MATH_TEXT);

if(DEBUG_MATH)System.err.println("wrTxTy: name (" + name + ")");
      writeString(name);

if(DEBUG_MATH)System.err.println("wrTxTy: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    }

    return index;
  }

  private int writeTupleType(TupleType tt)
    throws IOException, VisADException
  {
    if (tt instanceof RealTupleType) {
      return writeRealTupleType((RealTupleType )tt);
    }

    final int dim = tt.getDimension();

    int[] types = new int[dim];
    for (int i = 0; i < dim; i++) {
      types[i] = writeMathType(tt.getComponent(i));
    }

    int index = typeCache.getIndex(tt);
    if (index < 0) {
      index = cacheMathType(tt);

      if (!tt.getClass().equals(TupleType.class)) {
if(DEBUG_MATH)System.err.println("wrTuTy: serialized TupleType (" + tt.getClass().getName() + ")");
        writeSerializedObject(OBJ_MATH_SERIAL, tt);
        return index;
      }

if(DEBUG_MATH)System.err.println("wrTuTy: OBJ_MATH (" + OBJ_MATH + ")");
      file.writeByte(OBJ_MATH);
if(DEBUG_MATH)System.err.println("wrTuTy: index (" + index + ")");
      file.writeInt(index);
if(DEBUG_MATH)System.err.println("wrTuTy: MATH_TUPLE (" + MATH_TUPLE + ")");
      file.writeByte(MATH_TUPLE);

if(DEBUG_MATH)System.err.println("wrTuTy: dim (" + dim + ")");
      file.writeInt(dim);

      for (int i = 0; i < dim; i++) {
if(DEBUG_MATH)System.err.println("wrTuTy: type index #" + i + " (" + types[i] + ")");
        file.writeInt(types[i]);
      }

if(DEBUG_MATH)System.err.println("wrTuTy: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    }

    return index;
  }

  private int writeUnit(Unit u)
    throws VisADException
  {
    int index = unitCache.getIndex(u);
    if (index >= 0) {
      return index;
    }

    // don't bother saving this Unit if there's no definition
    String uDef = u.getDefinition().trim();
    if (uDef.length() == 0) {
      throw new VisADException("Unwriteable Unit \"" + u + "\"");
    }

    String uId = u.getIdentifier();

    // cache the Unit so we can find its index number
    index = unitCache.add(u);
    if (index < 0) {
      throw new VisADException("Couldn't cache Unit " + u);
    }

    try {
if(DEBUG_UNIT)System.err.println("wrU: OBJ_UNIT (" + OBJ_UNIT + ")");
      file.writeByte(OBJ_UNIT);
if(DEBUG_UNIT)System.err.println("wrU: index (" + index + ")");
      file.writeInt(index);

if(DEBUG_UNIT)System.err.println("wrU: identifier (" + uId + ")");
      writeString(uId);
if(DEBUG_UNIT)System.err.println("wrU: definition (" + uDef + ")");
      writeString(uDef);

if(DEBUG_UNIT)System.err.println("wrU: FLD_END (" + FLD_END + ")");
      file.writeByte(FLD_END);
    } catch (IOException ioe) {
      throw new VisADException("Couldn't write file: " +
                               ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }

    return index;
  }

  private int[] writeUnits(Unit[] units)
    throws VisADException
  {
    // make sure there's something to write
    boolean empty = true;
    for (int i = 0; i < units.length; i++) {
      if (units[i] != null) {
        empty = false;
        break;
      }
    }
    if (empty) {
      return null;
    }

    int[] indices = new int[units.length];

    for (int i = 0; i < units.length; i++) {
      if (units[i] == null) {
        indices[i] = -1;
      } else {
        indices[i] = writeUnit(units[i]);
      }
    }

    return indices;
  }
}
