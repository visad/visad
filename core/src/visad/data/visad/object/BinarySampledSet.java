/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

    DataOutput file = writer.getOutput();

if(DEBUG_WR_DATA)System.err.println("wrSampSet: FLD_SET_SAMPLES (" + FLD_SET_SAMPLES + ")");
    file.writeByte(FLD_SET_SAMPLES);
if(DEBUG_WR_DATA)System.err.println("wrSampSet: len (" + sets.length + ")");
    file.writeInt(sets.length);
    for (int i = 0; i < sets.length; i++) {
      BinaryGeneric.write(writer, sets[i], token);
    }
  }
}
