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

package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import visad.Data;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FunctionType;
import visad.Set;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;
import visad.data.visad.Saveable;

public class BinaryFieldImpl
  implements BinaryObject
{
  public static final int computeBytes(FieldImpl fld)
  {
    try {
      return processDependentData(null, null, fld.getDomainSet(), fld,
                                  SAVE_DEPEND);
    } catch (IOException ioe) {
      return 0;
    }
  }

  public static final int processDependentData(BinaryWriter writer,
                                               FunctionType ft, Set set,
                                               FieldImpl fld, Object token)
    throws IOException
  {
    if (!fld.getClass().equals(FieldImpl.class) &&
        !(fld instanceof FieldImpl && fld instanceof Saveable))
    {
      return 0;
    }

    Object dependToken;
    if (token == SAVE_DEPEND_BIG) {
      dependToken = token;
    } else {
      dependToken = SAVE_DEPEND;
    }

    int numBytes = 1 + 4;

if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrFldI: type (" + ft + ")");
    if (writer != null) {
      BinaryFunctionType.write(writer, ft, SAVE_DATA);
    }
    numBytes += 1 + 4;

    if (set != null) {
      if (writer != null) {
        BinaryGeneric.write(writer, set, dependToken);
      }

      int setBytes = BinaryGeneric.computeBytes(set);
      if (setBytes > 0) {
        numBytes += 1 + setBytes;
      }
    }

    final int numSamples = (fld.isMissing() ? 0 : fld.getLength());
    if (numSamples > 0) {
      numBytes += 1;

      boolean metadataOnly = (token == SAVE_DEPEND_BIG);
      for (int i = 0; i < numSamples; i++) {
        DataImpl sample;
        try {
          sample = (DataImpl )fld.getSample(i, metadataOnly);
        } catch (VisADException ve) {
          continue;
        }

        if (writer != null) {
          BinaryGeneric.write(writer, sample, dependToken);
        }

        numBytes += BinaryGeneric.computeBytes(sample);
      }
    }

    return numBytes;
  }

  public static final FieldImpl read(BinaryReader reader)
    throws IOException, VisADException
  {
    BinaryObjectCache cache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdFldI: type index (" + typeIndex + ")");
    FunctionType ft = (FunctionType )cache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdFldI: type index (" + typeIndex + "=" + ft + ")");

    Set set = null;
    Data[] samples = null;

    boolean reading = true;
    while (reading) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_SET:
if(DEBUG_RD_DATA)System.err.println("rdFldI: FLD_SET (" + FLD_SET + ")");
        set = (Set )BinaryGeneric.read(reader);
        break;
      case FLD_DATA_SAMPLES:
if(DEBUG_RD_DATA)System.err.println("rdFldI: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
        final int numSamples = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdFldI: numSamples (" + numSamples + ")");
        if (numSamples <= 0) {
          throw new IOException("Corrupted file (bad Field sample length " +
                                numSamples + ")");
        }

        samples = new Data[numSamples];
        for (int i = 0; i < numSamples; i++) {
if(DEBUG_WR_DATA)System.err.println("rdFldI#"+i);
          samples[i] = BinaryGeneric.read(reader);
if(DEBUG_WR_DATA_DETAIL)System.err.println("rdFldI: #" + i + " (" + samples[i] + ")");
        }
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdFldI: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown FieldImpl directive " +
                              directive);
      }
    }

    if (ft == null) {
      throw new IOException("No FunctionType found for FieldImpl");
    }

    FieldImpl fld = (set == null ? new FieldImpl(ft) :
                     new FieldImpl(ft, set));
    if (samples != null) {
      final int len = samples.length;
      for (int i = 0; i < len; i++) {
        fld.setSample(i, samples[i]);
      }
    }

    return fld;
  }

  public static final int writeDependentData(BinaryWriter writer,
                                             FunctionType ft, Set set,
                                             FieldImpl fld, Object token)
    throws IOException
  {
    return processDependentData(writer, ft, set, fld, token);
  }

  public static final void write(BinaryWriter writer, FunctionType ft,
                                 Set set, FieldImpl fld, Object token)
    throws IOException
  {
    final int objLen = writeDependentData(writer, ft, set, fld, token);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND || token == SAVE_DEPEND_BIG) {
      return;
    }

    if (!fld.getClass().equals(FieldImpl.class) &&
        !(fld instanceof FieldImpl && fld instanceof Saveable))
    {
if(DEBUG_WR_DATA)System.err.println("wrFldI: punt "+fld.getClass().getName());
      BinaryUnknown.write(writer, fld, token);
      return;
    }

    int typeIndex = writer.getTypeCache().getIndex(ft);
    if (typeIndex < 0) {
      throw new IOException("FunctionType " + ft + " not cached");
    }

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrFldI: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrFldI: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrFldI: DATA_FIELD (" + DATA_FIELD + ")");
    file.writeByte(DATA_FIELD);

if(DEBUG_WR_DATA)System.err.println("wrFldI: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

    if (set != null) {
if(DEBUG_WR_DATA)System.err.println("wrFldI: FLD_SET (" + FLD_SET + ")");
      file.writeByte(FLD_SET);
      BinaryGeneric.write(writer, set, token);
    }

    final int numSamples = (fld.isMissing() ? 0 : fld.getLength());
    if (numSamples > 0) {
if(DEBUG_WR_DATA)System.err.println("wrFldI: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
      file.writeByte(FLD_DATA_SAMPLES);
if(DEBUG_WR_DATA)System.err.println("wrFldI: numSamples (" + numSamples + ")");
      file.writeInt(numSamples);
      for (int i = 0; i < numSamples; i++) {
        DataImpl sample;
        try {
          sample = (DataImpl )fld.getSample(i);
        } catch (VisADException ve) {
          writer.getOutput().writeByte(DATA_NONE);
          continue;
        }

        BinaryGeneric.write(writer, sample, token);
      }
    }

if(DEBUG_WR_DATA)System.err.println("wrFldI: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}
