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
  public static boolean checkSelfIntersection(Gridded2DSet set)
         throws VisADException {
    if (set == null) return false;
    if (set.getManifoldDimension() != 1) {
      throw new SetException("Gridded2DSet musy have manifold dimension = 1");
    }
    return checkSelfIntersection(set.getSamples());
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
        if (i == j || i == next[j] || next[i] == j) continue;
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
        if (0.0f <= x && x <= 1.0f && 0.0f <= y && y <= 1.0f) {
          return true;
        }
      }
    }
    return false;
  }

  /** compute area inside closed path */
  public static float computeArea(Gridded2DSet set) throws VisADException {
    if (set == null) return 0.0f;
    if (set.getManifoldDimension() != 1) {
      throw new SetException("Gridded2DSet musy have manifold dimension = 1");
    }
    return computeArea(set.getSamples());
  }

  /** compute area inside closed path */
  public static float computeArea(float[][] samples) throws VisADException {
    if (samples == null) return 0.0f;
    if (samples.length != 2 || samples[0].length != samples[1].length) {
      throw new VisADException("samples argument bad dimensions");
    }
    if (samples[0].length < 3) return 0.0f;
    if (checkSelfIntersection(samples)) {
      throw new VisADException("path self intersects");
    }
    int n = samples[0].length;

    // build circular boundary list
    int[] next = new int[n];
    for (int i=0; i<n-1; i++) {
      next[i] = i+1;
    }
    next[n-1] = 0;

    float area = 0.0f;
    for (int i=0; i<n; i++) {
      area +=
        samples[0][i] * samples[1][next[i]] - samples[0][next[i]] * samples[1][i];
    }
    return (float) Math.abs(0.5 * area);
  }

  /** check that set describes the boundary of a simply connected plane
      region; return a decomposition of that region into triangles whose
      vertices are all boundary points from samples, as an Irregular2DSet */
  public static Irregular2DSet fill(Gridded2DSet set) throws VisADException {
    if (set == null) return null;
    if (set.getManifoldDimension() != 1) {
      throw new SetException("Gridded2DSet musy have manifold dimension = 1");
    }
    float[][] samples = set.getSamples();
    int[][] tris = fill(samples);
    if (tris == null || tris[0].length == 0) return null;
    DelaunayCustom delaunay = new DelaunayCustom(samples, tris);
    if (delaunay == null) return null;
    return new Irregular2DSet(set.getType(), samples,
                              null, null, null, delaunay);
  }

  /** check that float[2][number_of_points] samples describes the
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
    if (samples[0].length < 3) return null;
    if (checkSelfIntersection(samples)) {
      throw new VisADException("path self intersects");
    }
    int n = samples[0].length;

    // build circular boundary list
    int[] next = new int[n];
    for (int i=0; i<n-1; i++) {
      next[i] = i+1;
    }
    next[n-1] = 0;
    int[] prev = new int[n];
    for (int i=1; i<n; i++) {
      prev[i] = i-1;
    }
    prev[0] = n-1;

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
    boolean bug = false;
    while ((n - t) > 2) {
      int j = next[i];
      int k = next[j];
      float a0 = samples[0][j] - samples[0][i];
      float a1 = samples[1][j] - samples[1][i];
      float b0 = samples[0][k] - samples[0][j];
      float b1 = samples[1][k] - samples[1][j];
      boolean in = (((a0 * b1 - b0 * a1) > 0.0) == pos);
if (bug && !in) System.out.println("bug " + i + " tri orient in = " + in);
      if (in && i != next[k]) {
        float ik1 = samples[1][i] - samples[1][k];
        float ik0 = samples[0][i] - samples[0][k];
        float ik = samples[1][k] * ik0 - samples[0][k] * ik1;

        float ji1 = samples[1][j] - samples[1][i];
        float ji0 = samples[0][j] - samples[0][i];
        float ji = samples[1][i] * ji0 - samples[0][i] * ji1;

        float kj1 = samples[1][k] - samples[1][j];
        float kj0 = samples[0][k] - samples[0][j];
        float kj = samples[1][j] * kj0 - samples[0][j] * kj1;

        int kn = next[k];
        float kn0 = samples[0][kn];
        float kn1 = samples[1][kn];
        if (((ik + kn0 * ik1 - kn1 * ik0) > 0.0) != pos &&
            ((kj + kn0 * kj1 - kn1 * kj0) > 0.0) != pos) {
          in = false;
        }
        int ip = prev[i];
        float ip0 = samples[0][ip];
        float ip1 = samples[1][ip];
        if (((ik + ip0 * ik1 - ip1 * ik0) > 0.0) != pos &&
            ((ji + ip0 * ji1 - ip1 * ji0) > 0.0) != pos) {
          in = false;
        }
if (bug && !in) System.out.println("bug " + i + " adjacent in = " + in);
        if (in) {
          int p = next[k];
          int q = next[p];
          a0 = samples[0][i];
          a1 = samples[1][i];
          b0 = samples[0][k];
          b1 = samples[1][k];
          while (q != i) {
            float c0 = samples[0][p];
            float c1 = samples[1][p];
            float d0 = samples[0][q];
            float d1 = samples[1][q];
            float det = (b0 - a0) * (c1 - d1) - (b1 - a1) * (c0 - d0);
            p = q;
            q = next[p];
            // if (Math.abs(det) < 0.0000001) continue;
            if (Math.abs(det) == 0.0) continue;
            float x = ((b0 - a0) * (c1 - a1) - (b1 - a1) * (c0 - a0)) / det;
            float y = ((c0 - a0) * (c1 - d1) - (c1 - a1) * (c0 - d0)) / det;
            if (0.0f <= x && x <= 1.0f && 0.0f <= y && y <= 1.0f) {
              in = false;
              break;
            }
          } // end while (q != i)
if (bug && !in) System.out.println("bug " + i + " intersect in = " + in);
        } // end if (in)
      } // end if (in && i != next[k])
      if (in) {
// System.out.println("tri " + i + " " + j + " " + k);
        tris[t++] = new int[] {i, j, k}; // add triangle to tris
        next[i] = k; // and remove j from boundary
        prev[k] = i;
        bad = 0;
      }
      else {
// System.out.println("bump " + i);
        i = j; // try next point along boundary
        if (bad++ > (n - t)) {
          if (bug) {
            // throw new VisADException("bad samples");
            System.out.println("bad samples t = " + t + " n = " + n);
            if (t > 0) {
              int[][] new_tris = new int[t][];
              System.arraycopy(tris, 0, new_tris, 0, t);
              return new_tris;
            }
            else {
              return null;
            }
          }
          else {
            bug = true;
            bad = 0;
          }
        }
      }
    }
    return tris;
  }

}

