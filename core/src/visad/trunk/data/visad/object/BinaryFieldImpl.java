package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import visad.Data;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FunctionType;
import visad.Set;
import visad.VisADException;

import visad.data.FileField;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryFieldImpl
  implements BinaryObject
{
  public static final int computeBytes(Set set, DataImpl[] samples)
  {
    final int setLen = BinaryGeneric.computeBytes(set);
    final int samplesLen = (samples == null ? 0 :
                            BinaryDataArray.computeBytes(samples));

    if (setLen < 0 || samplesLen < 0) {
      return -1;
    }

    return 1 + 4 + 1 + 4 +
      (setLen == 0 ? 0 : 1 + setLen) +
      (samplesLen == 0 ? 0 : 1 + samplesLen) +
      1;
  }

  public static DataImpl[] getSamples(FieldImpl fld)
  {
    final int len = fld.getLength();
    if (fld.isMissing() || len <= 0) {
      return null;
    }

    DataImpl[] samples = new DataImpl[len];
    for (int i = 0; i < len; i++) {
      try {
        samples[i] = (DataImpl )fld.getSample(i);
      } catch (java.rmi.RemoteException re) {
        return null;
      } catch (VisADException ve) {
        return null;
      }
    }

    return samples;
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
        samples = BinaryDataArray.read(reader);
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

  public static final void writeDependentData(BinaryWriter writer,
                                              FunctionType ft, Set set,
                                              FieldImpl fld)
    throws IOException
  {
    byte dataType;
    if (!fld.getClass().equals(FieldImpl.class) &&
        !fld.getClass().equals(FileField.class))
    {
      return;
    }

if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrFldI: type (" + ft + ")");
    BinaryFunctionType.write(writer, ft, SAVE_DATA);

    if (set != null) {
      BinaryGeneric.write(writer, set, SAVE_DEPEND);
    }

    DataImpl[] samples = getSamples(fld);
    for (int i = 0; i < samples.length; i++) {
      BinaryGeneric.write(writer, samples[i], SAVE_DEPEND);
    }
  }

  public static final void write(BinaryWriter writer, FunctionType ft,
                                 Set set, FieldImpl fld, Object token)
    throws IOException
  {
    writeDependentData(writer, ft, set, fld);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND) {
      return;
    }

    byte dataType;
    if (fld.getClass().equals(FieldImpl.class)) {
      dataType = DATA_FIELD;
    } else if (fld.getClass().equals(FileField.class)) {
      // treat FileFields like FieldImpls
      dataType = DATA_FIELD;
    } else {
if(DEBUG_WR_DATA)System.err.println("wrFldI: punt "+fld.getClass().getName());
      BinaryUnknown.write(writer, fld, token);
      return;
    }

    DataImpl[] samples = getSamples(fld);

    int typeIndex = writer.getTypeCache().getIndex(ft);
    if (typeIndex < 0) {
      throw new IOException("FunctionType " + ft + " not cached");
    }

    final int objLen = computeBytes(set, samples);

    DataOutputStream file = writer.getOutputStream();

if(DEBUG_WR_DATA)System.err.println("wrFldI: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrFldI: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrFldI: DATA_FIELD (" + dataType + ")");
    file.writeByte(dataType);

if(DEBUG_WR_DATA)System.err.println("wrFldI: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

    if (set != null) {
if(DEBUG_WR_DATA)System.err.println("wrFldI: FLD_SET (" + FLD_SET + ")");
      file.writeByte(FLD_SET);
      BinaryGeneric.write(writer, set, token);
    }

    if (samples != null) {
if(DEBUG_WR_DATA)System.err.println("wrFldI: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
      file.writeByte(FLD_DATA_SAMPLES);
      BinaryDataArray.write(writer, samples, token);
    }

if(DEBUG_WR_DATA)System.err.println("wrFldI: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}
