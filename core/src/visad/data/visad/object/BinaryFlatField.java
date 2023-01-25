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

package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import java.rmi.RemoteException;

import visad.CoordinateSystem;
import visad.Data;
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
import visad.data.visad.Saveable;

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
                                       FlatField fld)
  {
    int samplesLen = 0;
    if (!fld.isMissing()) {
      final int dim = fld.getRangeDimension();
      final int len = fld.getLength();

      if (dim > 0 && len > 0) {
        samplesLen = 4 + dim * (4 + len * 8);
      }
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
                                              FlatField fld, Object token)
    throws IOException
  {
    if (!fld.getClass().equals(FlatField.class) &&
        !fld.getClass().equals(FileFlatField.class) &&
        !(fld instanceof FlatField && fld instanceof Saveable))
    {
      return;
    }

    Object dependToken;
    if (token == SAVE_DEPEND_BIG) {
      dependToken = token;
    } else {
      dependToken = SAVE_DEPEND;
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
      BinaryGeneric.write(writer, domainSet, dependToken);
    }

    if (rangeSets != null) {
      for (int i = 0; i < rangeSets.length; i++) {
        BinaryGeneric.write(writer, rangeSets[i], dependToken);
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
                       units, fld, token);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND || token == SAVE_DEPEND_BIG) {
      return;
    }

    if (!fld.getClass().equals(FlatField.class) &&
        !fld.getClass().equals(FileFlatField.class) &&
        !(fld instanceof FlatField && fld instanceof Saveable))
    {
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

    final int objLen = computeBytes(domainSet, cs, rangeCS, rangeSets, units,
                                    fld);

    DataOutput file = writer.getOutput();

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

    if (!fld.isMissing() && fld.getLength() > 0) {
      double[][] dblSamples;
      try {
        dblSamples = fld.unpackValues();
      } catch (NullPointerException npe) {
        npe.printStackTrace();
        dblSamples = null;
      } catch (VisADException ve) {
        ve.printStackTrace();
        dblSamples = null;
      }

      if (dblSamples != null) {
if(DEBUG_WR_DATA)System.err.println("wrFlFld: FLD_DOUBLE_SAMPLES (" + FLD_DOUBLE_SAMPLES + ")");
        file.writeByte(FLD_DOUBLE_SAMPLES);
        BinaryDoubleMatrix.write(writer, dblSamples, token);
      }
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
