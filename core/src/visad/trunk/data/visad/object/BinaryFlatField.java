package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import java.rmi.RemoteException;

import visad.CoordinateSystem;
import visad.Data;
import visad.DataImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Set;
import visad.Unit;
import visad.VisADException;

import visad.data.CacheStrategy;
import visad.data.FileAccessor;
import visad.data.FileFlatField;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

class BinaryAccessor
  extends FileAccessor
{
  private transient BinaryReader rdr;
  private transient long filePtr;
  private transient FunctionType ft;

  public BinaryAccessor(BinaryReader rdr, long filePtr, FunctionType ft)
  {
    this.rdr = rdr;
    this.filePtr = filePtr;
    this.ft = ft;
  }

  public FlatField getFlatField()
    throws RemoteException, VisADException
  {
    FlatField ff;
    try {
      final long curPtr = rdr.getFilePointer();

      rdr.seek(filePtr);
      ff = BinaryFlatField.read(rdr, 0, false);
      rdr.seek(curPtr);
    } catch (IOException ioe) {
      throw new VisADException(ioe.getClass().getName() + ": " +
                               ioe.getMessage());
    }

    return ff;
  }

  public FunctionType getFunctionType()
    throws VisADException
  {
    return ft;
  }

  public double[][] readFlatField(FlatField template, int[] fileLocations)
  {
    throw new RuntimeException("Unimplemented");
  }

  public void writeFile(int[] fileLocations, Data range)
  {
    throw new RuntimeException("Unimplemented");
  }

  public void writeFlatField(double[][] values, FlatField template,
                             int[] fileLocations)
  {
    throw new RuntimeException("Unimplemented");
  }
}

public class BinaryFlatField
  implements BinaryObject
{
  private static CacheStrategy strategy = new CacheStrategy();

  public static final int computeBytes(Set domainSet, CoordinateSystem cs,
                                       CoordinateSystem[] rangeCS,
                                       Set[] rangeSets, Unit[] units,
                                       double[][] dblSamples, Data[] samples)
  {
    int samplesLen;
    if (dblSamples != null) {
      samplesLen = 1 + BinaryDoubleMatrix.computeBytes(dblSamples);
    } else if (samples != null) {
      samplesLen = 1 + BinaryDataArray.computeBytes(samples);
    } else {
      samplesLen = 0;
    }

    int rangeSetsLen;
    if (rangeSets == null) {
      rangeSetsLen = 0;
    } else {
      rangeSetsLen = 1 + 4;
      for (int i = 0; i < rangeSets.length; i++) {
        int len = BinaryGeneric.computeBytes(rangeSets[i]);
        if (len < 0) {
          return -1;
        }

        rangeSetsLen += len;
      }
    }

    final int unitsLen = BinaryUnit.computeBytes(units);
    return 1 + 4 + 1 + 4 +
      (domainSet == null ? 0 : 1 + BinaryGeneric.computeBytes(domainSet)) +
      samplesLen +
      (cs == null ? 0 : 5) +
      (rangeCS == null ? 0 :
       1 + BinaryCoordinateSystem.computeBytes(rangeCS)) +
      rangeSetsLen +
      (unitsLen == 0 ? 0 : 1 + unitsLen) +
      1;
  }

  private static FileFlatField createFileFlatField(BinaryReader rdr,
                                                   int objLen)
    throws IOException, VisADException
  {
    final long filePtr = rdr.getFilePointer();

    BinaryObjectCache typeCache = rdr.getTypeCache();
    DataInput file = rdr.getInput();

    final int typeIndex = file.readInt();

    FunctionType ft = (FunctionType )typeCache.get(typeIndex);

if(DEBUG_RD_DATA){
  final int partLen = objLen - 4;

  byte[] b = new byte[partLen];
  file.readFully(b);

if(DEBUG_RD_MATH)System.err.println("rdFlFld: type index (" + typeIndex + ")");
  System.err.println("rdFlFld: Skipping " + objLen + " bytes");

  System.err.print("  ");
  int cols = 2;

  for (int i = 0; i < partLen; i++) {
    final int bVal;
    if (b[i] < 0) {
      bVal = 256 - b[i];
    } else {
      bVal = b[i];
    }

    final int bCols;
    if (bVal < 10) {
      bCols = 2;
    } else if (bVal < 100) {
      bCols = 3;
    } else {
      bCols = 4;
    }

    if (cols + bCols < 80) {
      cols += bCols;
    } else {
      System.err.println();
      System.err.print("  ");
      cols = 2 + bCols;
    }

    System.err.print(" " + bVal);
  }
  System.err.println();

  final long expectedPtr = filePtr + (long )objLen;
  final long postPtr = rdr.getFilePointer();
  if (postPtr != expectedPtr) {
    System.err.println("Expected ptr " + expectedPtr + ", got " + postPtr);
  }
}
    // skip to the end of this object
    rdr.seek(filePtr + (long )objLen);

    return new FileFlatField(new BinaryAccessor(rdr, filePtr, ft), strategy);
  }

  public static double[][] getDoubleSamples(FlatField fld)
  {
    if (fld.isMissing() || fld.getLength() <= 0) {
      return null;
    }

    double[][] dblSamples;
    try {
      dblSamples = fld.unpackValues();
    } catch (NullPointerException npe) {
      dblSamples = null;
    } catch (VisADException ve) {
      dblSamples = null;
    }

    return dblSamples;
  }

  public static Data[] getSamples(FlatField fld)
  {
    final int len = fld.getLength();

    Data[] samples = new Data[len];
    for (int i = 0; i < len; i++) {
      try {
        samples[i] = (Data )fld.getSample(i);
      } catch (java.rmi.RemoteException re) {
        return null;
      } catch (VisADException ve) {
        return null;
      }
    }

    return samples;
  }

  private static final Set[] readSetArray(BinaryReader reader)
    throws IOException, VisADException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdSetRA: len (" + len + ")");
    Set[] sets = new Set[len];
    for (int i = 0; i < sets.length; i++) {
      sets[i] = (Set )BinaryGeneric.read(reader);
    }
    return sets;
  }

  public static final FlatField read(BinaryReader reader, int objLen,
                                     boolean cacheFile)
    throws IOException, VisADException
  {
    if (cacheFile) {
      return createFileFlatField(reader, objLen);
    }

    BinaryObjectCache cSysCache = reader.getCoordinateSystemCache();
    BinaryObjectCache typeCache = reader.getTypeCache();
    DataInput file = reader.getInput();

long totStart, sTime, dsTime, dbTime, icsTime, rcsTime, slTime, uTime;
totStart = sTime = dsTime = dbTime = icsTime = rcsTime = slTime = uTime = 0;

totStart = System.currentTimeMillis();
    final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdFlFld: type index (" + typeIndex + ")");
    FunctionType ft = (FunctionType )typeCache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdFlFld: type index (" + typeIndex + "=" + ft + ")");

    Set domainSet = null;
    Data[] oldSamples = null;
    CoordinateSystem cs = null;
    CoordinateSystem[] rangeCS = null;
    Set[] rangeSets = null;
    Unit[] units = null;
    double[][] samples = null;

    boolean reading = true;
    while (reading) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

long tmpStart = System.currentTimeMillis();
      switch (directive) {
      case FLD_SET:
if(DEBUG_RD_DATA)System.err.println("rdFlFld: FLD_SET (" + FLD_SET + ")");
        domainSet = (Set )BinaryGeneric.read(reader);
if(DEBUG_RD_TIME)sTime += System.currentTimeMillis() - tmpStart;
        break;
      case FLD_DATA_SAMPLES:
if(DEBUG_RD_DATA)System.err.println("rdFlFld: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
        oldSamples = BinaryDataArray.read(reader);
if(DEBUG_RD_TIME)dsTime += System.currentTimeMillis() - tmpStart;
        break;
      case FLD_DOUBLE_SAMPLES:
if(DEBUG_RD_DATA)System.err.println("rdFlFld: FLD_DOUBLE_SAMPLES (" + FLD_DOUBLE_SAMPLES + ")");
        samples = BinaryDoubleMatrix.read(reader);
if(DEBUG_RD_TIME)dbTime += System.currentTimeMillis() - tmpStart;
        break;
      case FLD_INDEX_COORDSYS:
if(DEBUG_RD_DATA)System.err.println("rdFlFld: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
        final int index = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_CSYS)System.err.println("rdFlFld: cSys index (" + index + ")");
        cs = (CoordinateSystem )cSysCache.get(index);
if(DEBUG_RD_DATA&&!DEBUG_RD_CSYS)System.err.println("rdFlFld: cSys index (" + index + "=" + cs + ")");
if(DEBUG_RD_TIME)icsTime += System.currentTimeMillis() - tmpStart;
        break;
      case FLD_RANGE_COORDSYSES:
if(DEBUG_RD_DATA)System.err.println("rdFlFld: FLD_RANGE_COORDSYSES (" + FLD_RANGE_COORDSYSES + ")");
        rangeCS = BinaryCoordinateSystem.readList(reader);
if(DEBUG_RD_TIME)rcsTime += System.currentTimeMillis() - tmpStart;
        break;
      case FLD_SET_LIST:
if(DEBUG_RD_DATA)System.err.println("rdFlFld: FLD_SET_LIST (" + FLD_SET_LIST + ")");
        rangeSets = readSetArray(reader);
if(DEBUG_RD_TIME)slTime += System.currentTimeMillis() - tmpStart;
        break;
      case FLD_INDEX_UNITS:
if(DEBUG_RD_DATA)System.err.println("rdFlFld: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
        units = BinaryUnit.readList(reader);
if(DEBUG_RD_TIME)uTime += System.currentTimeMillis() - tmpStart;
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdFlFld: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown FlatField directive " +
                              directive);
      }
    }

    if (ft == null) {
      throw new IOException("No FunctionType found for FlatField");
    }

long tmpStart = System.currentTimeMillis();
    FlatField fld = new FlatField(ft, domainSet, rangeCS, rangeSets, units);
long c1Time = System.currentTimeMillis() - tmpStart;
tmpStart = System.currentTimeMillis();
    if (samples != null) {
      fld.setSamples(0, samples);
    } else if (oldSamples != null) {
      final int len = oldSamples.length;
      for (int i = 0; i < len; i++) {
        fld.setSample(i, oldSamples[i]);
      }
    }
long c2Time = System.currentTimeMillis() - tmpStart;
tmpStart = System.currentTimeMillis();
    if (samples != null) {
      fld.setSamples(samples, false);
    }
long c3Time = System.currentTimeMillis() - tmpStart;

if(DEBUG_RD_TIME){
  long totTime = System.currentTimeMillis() - totStart;
  System.err.print("rdFlFld: tot "+totTime);
  if (sTime > 0) System.err.print(" s "+sTime);
  if (dsTime > 0) System.err.print(" ds "+dsTime);
  if (dbTime > 0) System.err.print(" db "+dbTime);
  if (icsTime > 0) System.err.print(" ics "+icsTime);
  if (rcsTime > 0) System.err.print(" rcs "+rcsTime);
  if (slTime > 0) System.err.print(" sl "+slTime);
  if (uTime > 0) System.err.print(" u "+uTime);
  if (c1Time > 0) System.err.print(" c1 "+c1Time);
  if (c2Time > 0) System.err.print(" c2 "+c2Time);
  if (c3Time > 0) System.err.print(" c3 "+c2Time);
  System.err.println();
}
    return fld;
  }

  public static final void writeDependentData(BinaryWriter writer,
                                              FunctionType type,
                                              Set domainSet,
                                              CoordinateSystem cs,
                                              CoordinateSystem[] rangeCS,
                                              Set[] rangeSets, Unit[] units,
                                              FlatField fld)
    throws IOException
  {
    byte dataType;
    if (!fld.getClass().equals(FlatField.class) &&
        !fld.getClass().equals(FileFlatField.class))
    {
      return;
    }

if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrFlFld: type (" + type + ")");
    BinaryFunctionType.write(writer, type, SAVE_DATA);

    if (cs != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_CSYS)System.err.println("wrFlFld: coordSys (" + cs + ")");
      BinaryCoordinateSystem.write(writer, cs, SAVE_DATA);
    }

    if (rangeCS != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_CSYS){
  System.err.println("wrFlFld: List of " + rangeCS.length + " CoordSys");
  for(int x=0;x<rangeCS.length;x++){
    System.err.println("wrFlFld:    #"+x+": "+rangeCS[x]);
  }
}
      BinaryCoordinateSystem.writeList(writer, rangeCS, SAVE_DATA);
    }

    if (units != null) {
if(DEBUG_WR_DATA&&!DEBUG_WR_UNIT){
  System.err.println("wrFlFld: List of " + units.length + " Units");
  for(int x=0;x<units.length;x++){
    System.err.println("wrFlFld:    #"+x+": "+units[x]);
  }
}
      BinaryUnit.writeList(writer, units, SAVE_DATA);
    }

    if (domainSet != null) {
      BinaryGeneric.write(writer, domainSet, SAVE_DEPEND);
    }

    if (rangeSets != null) {
      for (int i = 0; i < rangeSets.length; i++) {
        BinaryGeneric.write(writer, rangeSets[i], SAVE_DEPEND);
      }
    }
  }

  public static final void write(BinaryWriter writer, FunctionType type,
                                 Set domainSet, CoordinateSystem cs,
                                 CoordinateSystem[] rangeCS, Set[] rangeSets,
                                 Unit[] units, FlatField fld, Object token)
    throws IOException
  {
    writeDependentData(writer, type, domainSet, cs, rangeCS, rangeSets,
                       units, fld);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND) {
      return;
    }

    byte dataType;
    if (fld.getClass().equals(FlatField.class)) {
      dataType = DATA_FLAT_FIELD;
    } else if (fld.getClass().equals(FileFlatField.class)) {
      // treat FileFlatFields like FlatFields
      dataType = DATA_FLAT_FIELD;
    } else {
if(DEBUG_WR_DATA)System.err.println("wrFlFld: punt "+fld.getClass().getName());
      BinaryUnknown.write(writer, fld, token);
      return;
    }

    int typeIndex = writer.getTypeCache().getIndex(type);
    if (typeIndex < 0) {
      throw new IOException("FunctionType " + type + " not cached");
    }

    int csIndex = -1;
    if (cs != null) {
      csIndex = writer.getCoordinateSystemCache().getIndex(cs);
      if (csIndex < 0) {
        throw new IOException("CoordinateSystem " + cs + " not cached");
      }
    }

    int[] csList = null;
    if (rangeCS != null) {
      csList = BinaryCoordinateSystem.lookupList(writer.getCoordinateSystemCache(),
                                                 rangeCS);
    }

    int[] unitsIndex = null;
    if (units != null) {
      unitsIndex = BinaryUnit.lookupList(writer.getUnitCache(), units);
    }

    double[][] dblSamples = getDoubleSamples(fld);

    Data[] samples;
    if (dblSamples != null) {
      samples = null;
    } else {
      samples = getSamples(fld);
    }

    final int objLen = computeBytes(domainSet, cs, rangeCS, rangeSets, units,
                                    dblSamples, samples);

    DataOutputStream file = writer.getOutputStream();

if(DEBUG_WR_DATA)System.err.println("wrFlFld: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrFlFld: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrFlFld: DATA_FLAT_FIELD (" + DATA_FLAT_FIELD + ")");
    file.writeByte(DATA_FLAT_FIELD);

if(DEBUG_WR_DATA)System.err.println("wrFlFld: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

    if (domainSet != null) {
if(DEBUG_WR_DATA)System.err.println("wrFlFld: FLD_SET (" + FLD_SET + ")");
      file.writeByte(FLD_SET);
      BinaryGeneric.write(writer, domainSet, token);
    }

    if (dblSamples != null) {
if(DEBUG_WR_DATA)System.err.println("wrFlFld: FLD_DOUBLE_SAMPLES (" + FLD_DOUBLE_SAMPLES + ")");
      file.writeByte(FLD_DOUBLE_SAMPLES);
      BinaryDoubleMatrix.write(writer, dblSamples, token);
    } else if (samples != null) {
if(DEBUG_WR_DATA)System.err.println("wrFlFld: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
      file.writeByte(FLD_DATA_SAMPLES);
      BinaryDataArray.write(writer, samples, token);
    }

    if (csIndex >= 0) {
if(DEBUG_WR_DATA)System.err.println("wrFlFld: FLD_INDEX_COORDSYS (" + FLD_INDEX_COORDSYS + ")");
      file.writeByte(FLD_INDEX_COORDSYS);
if(DEBUG_WR_DATA)System.err.println("wrFlFld: coord sys Index (" + csIndex + ")");
      file.writeInt(csIndex);
    }

    if (csList != null) {
if(DEBUG_WR_DATA)System.err.println("wrFlFld: FLD_RANGE_COORDSYSES (" + FLD_RANGE_COORDSYSES + ")");
      file.writeByte(FLD_RANGE_COORDSYSES);
      BinaryIntegerArray.write(writer, csList, token);
    }

    if (rangeSets != null) {
if(DEBUG_WR_DATA)System.err.println("wrFlFld: FLD_SET_LIST (" + FLD_SET_LIST + ")");
      file.writeByte(FLD_SET_LIST);
if(DEBUG_WR_DATA)System.err.println("wrFlFld: len (" + rangeSets.length + ")");
      file.writeInt(rangeSets.length);
      for (int i = 0; i < rangeSets.length; i++) {
        BinaryGeneric.write(writer, rangeSets[i], token);
      }
    }

    if (unitsIndex != null) {
if(DEBUG_WR_DATA)System.err.println("wrFlFld: FLD_INDEX_UNITS (" + FLD_INDEX_UNITS + ")");
      file.writeByte(FLD_INDEX_UNITS);
      BinaryIntegerArray.write(writer, unitsIndex, token);
    }

if(DEBUG_WR_DATA)System.err.println("wrFlFld: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}
