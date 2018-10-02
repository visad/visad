/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

import java.io.IOException;

import visad.DisplayTupleType;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryWriter;

public class BinaryDisplayTupleType
  implements BinaryObject
{
  public static final int write(BinaryWriter writer, DisplayTupleType dtt,
                                Object token)
    throws IOException
  {
    BinaryObjectCache cache = writer.getTypeCache();

    int index = cache.getIndex(dtt);
    if (index < 0) {
      index = cache.add(dtt);
      if (index < 0) {
        throw new IOException("Couldn't cache DisplayTupleType " + dtt);
      }

if(DEBUG_WR_MATH)System.err.println("wrDpyTuTy: serialized DisplayTupleType");
      BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, dtt, token);
    }

    return index;
  }
}
