package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import visad.SampledSet;
import visad.VisADException;

import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinarySampledSet
  implements BinaryObject
{
  public static final int computeBytes(SampledSet[] sets)
  {
    if (sets == null) {
      return 0;
    }

    int setsLen = 1 + 4;
    for (int i = 0; i < sets.length; i++) {
      int len = BinaryGeneric.computeBytes(sets[i]);
      if (len < 0) {
        return -1;
      }

      setsLen += len;
    }

    return setsLen;
  }

  public static final SampledSet[] readList(BinaryReader reader)
    throws IOException, VisADException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdSplSetS: len (" + len + ")");

    SampledSet[] sets = new SampledSet[len];
    for (int i = 0; i < sets.length; i++) {
      sets[i] = (SampledSet )BinaryGeneric.read(reader);
    }

    return sets;
  }

  public static final void writeList(BinaryWriter writer, SampledSet[] sets,
                                     Object token)
    throws IOException
  {
    if (sets == null) {
      return;
    }

    DataOutputStream file = writer.getOutputStream();

if(DEBUG_WR_DATA)System.err.println("wrSampSet: FLD_SET_SAMPLES (" + FLD_SET_SAMPLES + ")");
    file.writeByte(FLD_SET_SAMPLES);
if(DEBUG_WR_DATA)System.err.println("wrSampSet: len (" + sets.length + ")");
    file.writeInt(sets.length);
    for (int i = 0; i < sets.length; i++) {
      BinaryGeneric.write(writer, sets[i], token);
    }
  }
}
