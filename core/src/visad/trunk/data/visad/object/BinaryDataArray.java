package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import visad.Data;
import visad.DataImpl;
import visad.VisADException;

import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryDataArray
  implements BinaryObject
{
  public static final int computeBytes(Data[] array)
  {
    int len = 4;
    for (int i = 0; i < array.length; i++) {
      len += BinaryGeneric.computeBytes((DataImpl )array[i]);
    }
    return len;
  }

  public static final Data[] read(BinaryReader reader)
    throws IOException, VisADException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdDataRA: len (" + len + ")");
    if (len < 1) {
      throw new IOException("Corrupted file (bad Data array length " +
                            len + ")");
    }

long t = (DEBUG_RD_TIME ? System.currentTimeMillis() : 0);
    Data[] array = new Data[len];
    for (int i = 0; i < len; i++) {

if(DEBUG_WR_DATA)System.err.println("rdDataRA#"+i);
      array[i] = BinaryGeneric.read(reader);
if(DEBUG_WR_DATA_DETAIL)System.err.println("rdDataRA: #" + i + " (" + array[i] + ")");

if(DEBUG_WR_DATA)System.err.println("rdDataRA#"+i+": "+array[i].getClass().getName());
    }
if(DEBUG_RD_TIME)System.err.println("rdDataRA: "+len+" arrays "+(System.currentTimeMillis()-t));

    return array;
  }

  private static final void writeDependentData(BinaryWriter writer,
                                               Data[] array)
    throws IOException
  {
    if (array != null) {
      for (int i = 0; i < array.length; i++) {
        BinaryGeneric.write(writer, (DataImpl )array[i], SAVE_DEPEND);
      }
    }
  }

  public static final void write(BinaryWriter writer, Data[] array,
                                 Object token)
    throws IOException
  {
    writeDependentData(writer, array);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND) {
      return;
    }

    DataOutputStream file = writer.getOutputStream();

if(DEBUG_WR_DATA)System.err.println("wrDataRA: len (" + array.length + ")");
    file.writeInt(array.length);
    for (int i = 0; i < array.length; i++) {
if(DEBUG_WR_DATA_DETAIL)System.err.println("wrDataRA: #" + i + " (" + array[i] + ")");

if(DEBUG_WR_DATA)System.err.println("wrDataRA#"+i+": "+array[i].getClass().getName());
      BinaryGeneric.write(writer, (DataImpl )array[i], token);
    }
  }
}
