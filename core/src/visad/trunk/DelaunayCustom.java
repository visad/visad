//
// DelaunayCustom.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

/**
   DelaunayCustom is a set of constructors to create an instance
   of Delaunay by passing in a pre-computed triangulation.
   DelaunayCustom is useful for creating instances of Delaunay
   that can be passed into IrregularSet.  If you want
   to perform consistency checks on your triangulation, call
   Delaunay.test() on your DelaunayCustom object after it is
   constructed.<P>
*/
public class DelaunayCustom extends Delaunay {

  /** shortcut constructor */
  public DelaunayCustom(float[][] samples, int[][] tri)
                                           throws VisADException {
    this(samples, tri, null, null, null, 0, true);
  }

  /** complete constructor */
  public DelaunayCustom(float[][] samples, int[][] tri, int[][] vertices,
                        int[][] walk, int[][] edges, int num_edges)
                        throws VisADException {
    this(samples, tri, vertices, walk, edges, num_edges, true);
  }

  DelaunayCustom(float[][] samples, int[][] tri, int[][] vertices,
                 int[][] walk, int[][] edges, int num_edges,
                 boolean copy) throws VisADException {
    if (tri == null) {
      throw new VisADException("DelaunayCustom: "
                              +"Tri array must be specified!");
    }
    if (samples == null && vertices == null) {
      throw new VisADException("DelaunayCustom: Cannot construct "
                              +"Vertices without samples!");
    }
    if (samples == null && walk == null) {
      throw new VisADException("DelaunayCustom: Cannot construct "
                              +"Walk without samples!");
    }
    if (samples == null && edges == null) {
      throw new VisADException("DelaunayCustom: Cannot construct "
                              +"Edges without samples!");
    }

    // copy data into Delaunay arrays
    if (copy) {
      Tri = new int[tri.length][];
      for (int i=0; i<tri.length; i++) {
        Tri[i] = new int[tri[i].length];
        System.arraycopy(tri[i], 0, Tri[i], 0, tri[i].length);
      }
      if (vertices != null) {
        Vertices = new int[vertices.length][];
        for (int i=0; i<vertices.length; i++) {
          Vertices[i] = new int[vertices[i].length];
          System.arraycopy(vertices[i], 0,
                           Vertices[i], 0, vertices[i].length);
        }
      }
      if (walk != null) {
        Walk = new int[walk.length][];
        for (int i=0; i<walk.length; i++) {
          Walk[i] = new int[walk[i].length];
          System.arraycopy(walk[i], 0, Walk[i], 0, walk[i].length);
        }
      }
      if (edges != null) {
        Edges = new int[edges.length][];
        for (int i=0; i<edges.length; i++) {
          Edges[i] = new int[edges[i].length];
          System.arraycopy(edges[i], 0, Edges[i], 0, edges[i].length);
        }
      }
    }
    else {
      Tri = tri;
      Vertices = vertices;
      Walk = walk;
      Edges = edges;
    }
    NumEdges = num_edges;

    // call more generic method for constructing any remaining null arrays
/* WLH 7 June 98
    finish_triang(samples);
*/
    if (samples != null) super.finish_triang(samples);
  }

  /** return true if closed path in samples self-intersects */
  public static boolean checkSelfIntersection(float[][] samples)
         throws VisADException {
    if (samples == null) return false;
    if (samples.length != 2 || samples[0].length != samples[1].length) {
      throw new VisADException("samples argument bad dimensions");
    }
    int n = samples[0].length;

        // build circular boundary list
    int[] next = new int[n];
    for (int i=0; i<n-1; i++) {
      next[i] = i+1;
    }
    next[n-1] = 0;
    for (int i=0; i<n; i++) {
      for (int j=0; j<n; j++) {
        if (i == j) continue;
        float a0 = samples[0][i];
        float a1 = samples[1][i];
        float b0 = samples[0][next[i]];
        float b1 = samples[1][next[i]];
        float c0 = samples[0][j];
        float c1 = samples[1][j];
        float d0 = samples[0][next[j]];
        float d1 = samples[1][next[j]];
        float det = (b0 - a0) * (c1 - d1) - (b1 - a1) * (c0 - d0);
        if (Math.abs(det) < 0.0000001) continue;
        float x = ((b0 - a0) * (c1 - a1) - (b1 - a1) * (c0 - a0)) / det;
        float y = ((c0 - a0) * (c1 - d1) - (c1 - a1) * (c0 - d0)) / det;
        if (0.0f < x && x < 1.0f && 0.0f < y && y < 1.0f) {
          return true;
        }
      }
    }
    return false;
  }

  /** assume that float[2][number_of_points] samples describes the
      boundary of a simply connected plane region; return a decomposition
      of that region into triangles whose vertices are all boundary
      points from samples;
      the trick is that the region may not be convex, but the triangles
      must all lie inside the region */
  public static int[][] fill(float[][] samples) throws VisADException {
    if (samples == null) return null;
    if (samples.length != 2 || samples[0].length != samples[1].length) {
      throw new VisADException("samples argument bad dimensions");
    }
    int n = samples[0].length;

    // build circular boundary list
    int[] next = new int[n];
    for (int i=0; i<n-1; i++) {
      next[i] = i+1;
    }
    next[n-1] = 0;

    // compute area of region, to get orientation (positive or negative)
    float area = 0.0f;
    for (int i=0; i<n; i++) {
      area +=
        samples[0][i] * samples[1][next[i]] - samples[0][next[i]] * samples[1][i];
    }
    boolean pos = (area > 0.0);

    int[][] tris = new int[n-2][]; // will be n-2 triangles
    int i = 0; // current candidate for triangle
    int t = 0; // next triangle, boundary length = n - t
    int bad = 0;
    while ((n - t) > 2) {
      int j = next[i];
      int k = next[j];
      float a0 = samples[0][j] - samples[0][i];
      float a1 = samples[1][j] - samples[1][i];
      float b0 = samples[0][k] - samples[0][j];
      float b1 = samples[1][k] - samples[1][j];
      if (((a0 * b1 - b0 * a1) > 0.0) == pos) {
        tris[t++] = new int[] {i, j, k}; // add triangle to tris
        next[i] = k; // and remove j from boundary
        bad = 0;
      }
      else {
        i = j; // try next point along boundary
        if (bad++ > n) {
          throw new VisADException("bad samples");
        }
      }
    }
    return tris;
  }

}

