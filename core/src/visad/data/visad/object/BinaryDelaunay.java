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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import visad.Delaunay;
import visad.DelaunayClarkson;
import visad.DelaunayCustom;
import visad.DelaunayFast;
import visad.DelaunayOverlap;
import visad.DelaunayWatson;
import visad.VisADException;

import visad.data.visad.BinaryWriter;

public class BinaryDelaunay
  implements BinaryObject
{
  public static final int computeBytes(Delaunay d)
  {
    if (!isKnownClass(d)) {
      return BinarySerializedObject.computeBytes(d);
    }

    return (1 +
            1 + BinaryIntegerMatrix.computeBytes(d.Tri) +
            1 + BinaryIntegerMatrix.computeBytes(d.Vertices) +
            1 + BinaryIntegerMatrix.computeBytes(d.Walk) +
            1 + BinaryIntegerMatrix.computeBytes(d.Edges) +
            6);
  }

  public static final Delaunay read(DataInput file)
    throws IOException, VisADException
  {
    int[][] tri = null;
    int[][] verts = null;
    int[][] walk = null;
    int[][] edges = null;
    int numEdges = -1;

    boolean reading = true;
    while (reading) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_DELAUNAY_TRI:
if(DEBUG_RD_DATA)System.err.println("rdDel: FLD_DELAUNAY_TRI (" + FLD_DELAUNAY_TRI + ")");
        tri = BinaryIntegerMatrix.read(file);
        break;
      case FLD_DELAUNAY_VERTICES:
if(DEBUG_RD_DATA)System.err.println("rdDel: FLD_DELAUNAY_VERTICES (" + FLD_DELAUNAY_VERTICES + ")");
        verts = BinaryIntegerMatrix.read(file);
        break;
      case FLD_DELAUNAY_WALK:
if(DEBUG_RD_DATA)System.err.println("rdDel: FLD_DELAUNAY_WALK (" + FLD_DELAUNAY_WALK + ")");
        walk = BinaryIntegerMatrix.read(file);
        break;
      case FLD_DELAUNAY_EDGES:
if(DEBUG_RD_DATA)System.err.println("rdDel: FLD_DELAUNAY_EDGES (" + FLD_DELAUNAY_EDGES + ")");
        edges = BinaryIntegerMatrix.read(file);
        break;
      case FLD_DELAUNAY_NUM_EDGES:
if(DEBUG_RD_DATA)System.err.println("rdDel: FLD_DELAUNAY_NUM_EDGES (" + FLD_DELAUNAY_NUM_EDGES + ")");
        numEdges = file.readInt();
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdDel: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown Delaunay directive " +
                              directive);
      }
    }

    return new DelaunayCustom(null, tri, verts, walk, edges, numEdges);
  }

  private static final boolean isKnownClass(Delaunay delaunay)
  {
    final Class dClass = delaunay.getClass();

    return (dClass.equals(DelaunayClarkson.class) ||
            dClass.equals(DelaunayCustom.class) ||
            dClass.equals(DelaunayFast.class) ||
            dClass.equals(DelaunayOverlap.class) ||
            dClass.equals(DelaunayWatson.class));
  }

  public static final void write(BinaryWriter writer, Delaunay delaunay,
                                 Object token)
    throws IOException
  {
    if (!isKnownClass(delaunay)) {
      /* serialize non-standard Delaunay object */
      BinarySerializedObject.write(writer, FLD_DELAUNAY_SERIAL, delaunay,
                                   token);
      return;
    }

    DataOutput file = writer.getOutput();

    file.writeByte(FLD_DELAUNAY);

    file.writeByte(FLD_DELAUNAY_TRI);
    BinaryIntegerMatrix.write(file, delaunay.Tri);

    file.writeByte(FLD_DELAUNAY_VERTICES);
    BinaryIntegerMatrix.write(file, delaunay.Vertices);

    file.writeByte(FLD_DELAUNAY_WALK);
    BinaryIntegerMatrix.write(file, delaunay.Walk);

    file.writeByte(FLD_DELAUNAY_EDGES);
    BinaryIntegerMatrix.write(file, delaunay.Edges);

    file.writeByte(FLD_DELAUNAY_NUM_EDGES);
    file.writeInt(delaunay.NumEdges);

    file.writeByte(FLD_END);
  }
}
