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

import java.io.IOException;

import visad.RealType;
import visad.ScalarType;
import visad.TextType;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryWriter;

public class BinaryScalarType
  implements BinaryObject
{
  public static final int computeBytes(ScalarType st)
  {
    if (st instanceof RealType) {
      return BinaryRealType.computeBytes((RealType )st);
    } else if (st instanceof TextType) {
      return BinaryTextType.computeBytes((TextType )st);
    }

    return BinarySerializedObject.computeBytes(st);
  }

  public static final int write(BinaryWriter writer, ScalarType st,
                                Object token)
    throws IOException
  {
    if (st instanceof RealType) {
      return BinaryRealType.write(writer, (RealType )st, token);
    } else if (st instanceof TextType) {
      return BinaryTextType.write(writer, (TextType )st, token);
    }

    BinaryObjectCache cache = writer.getTypeCache();

    int index = cache.getIndex(st);
    if (index < 0) {
      index = cache.add(st);
      if (index < 0) {
        throw new IOException("Couldn't cache ScalarType " + st);
      }

if(DEBUG_WR_MATH)System.err.println("wrScTy: serialized ScalarType (" + st.getClass().getName() + ")");
      BinarySerializedObject.write(writer, OBJ_MATH_SERIAL, st, token);
    }

    return index;
  }
}
