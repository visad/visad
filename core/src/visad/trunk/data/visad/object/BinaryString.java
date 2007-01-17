/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryString
  implements BinaryObject
{
  public static final int computeBytes(String str)
  {
    return 4 + (str == null ? 0 : str.getBytes().length);
  }

  public static final String read(BinaryReader reader)
    throws IOException
  {
    DataInput file = reader.getInput();

    final int len = file.readInt();
if(DEBUG_RD_STR)System.err.println("rdStr: len (" + len + ")");
    if (len < 0) {
      return null;
    } else if (len == 0) {
      return "";
    }

    byte[] buf = new byte[len];
    file.readFully(buf);
if(DEBUG_RD_STR)System.err.println("rdStr: str (" + new String(buf) + ")");

    return new String(buf);
  }

  public static final void write(BinaryWriter writer, String str,
                                 Object token)
    throws IOException
  {
    DataOutput file = writer.getOutput();

    if (str == null) {
      file.writeInt(-1);
    } else {
      byte[] bytes = str.getBytes();

if(DEBUG_WR_DATA)System.err.println("wrStr: num bytes (" + bytes.length + ")");
      file.writeInt(bytes.length);
      if (bytes.length > 0) {
if(DEBUG_WR_DATA)System.err.println("wrStr: str (" + str + ")");
        file.write(bytes);
      }
    }
  }
}
