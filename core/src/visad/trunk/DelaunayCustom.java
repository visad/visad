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
        // if (0.0f <= x && x <= 1.0f && 0.0f <= y && y <= 1.0f) {
        if (0.0f < x && x < 1.0f && 0.0f < y && y < 1.0f) {
          return true;
        }
      }
    }
    return false;
  }

  private final static float SELF = 0.999f;
  private final static float PULL = 1.0f - SELF;
  private final static float PULL2 = 0.5f * (1.0f - SELF);

  /** return true if closed path in samples self-intersects */
  public static boolean checkAndFixSelfIntersection(float[][] samples)
         throws VisADException {
    if (samples == null) return false;
    if (samples.length != 2 || samples[0].length != samples[1].length) {
      throw new VisADException("samples argument bad dimensions");
    }
    int n = samples[0].length;
    boolean intersect = false;

        // build circular boundary list
    int[] next = new int[n];
    for (int i=0; i<n-1; i++) {
      next[i] = i+1;
    }
    next[n-1] = 0;

    // remove consecutive identical points
    float[][] new_samples = new float[2][n];
    int k = 0;
    for (int i=0; i<n; i++) {
      if (samples[0][i] != samples[0][next[i]] ||
          samples[1][i] != samples[1][next[i]]) {
        new_samples[0][k] = samples[0][i];
        new_samples[1][k] = samples[1][i];
        k++;
      }
    }
    if (k != n) {
      samples[0] = new float[k];
      samples[1] = new float[k];
      System.arraycopy(new_samples[0], 0, samples[0], 0, k);
      System.arraycopy(new_samples[1], 0, samples[1], 0, k);
      n = k;
      next = new int[n];
      for (int i=0; i<n-1; i++) {
        next[i] = i+1;
      }
      next[n-1] = 0;
    }

    int[] prev = new int[n];
    for (int i=1; i<n; i++) {
      prev[i] = i-1;
    }
    prev[0] = n-1;

    for (int i=0; i<n; i++) {
      if (samples[0][i] == samples[0][next[i]] &&
          samples[1][i] == samples[1][next[i]]) {
        System.out.println("equal consecutive points");
      }
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
        if (0.0f < x && x < 1.0f && 0.0f < y && y < 1.0f) {
          intersect = true;
        }
        else if ((0.0f == x || x == 1.0f) && (0.0f == y || y == 1.0f)) {
          // System.out.println("fix  x = " + x + " y = " + y);
          if (x == 0.0f) {
            samples[0][i] = SELF * a0 + PULL2 * (b0 + samples[0][prev[i]]);
            samples[1][i] = SELF * a1 + PULL2 * (b1 + samples[1][prev[i]]);
          }
          else if (x == 1.0f) {
            k = next[i];
            samples[0][k] = SELF * b0 + PULL2 * (a0 + samples[0][next[k]]);
            samples[1][k] = SELF * b1 + PULL2 * (a1 + samples[1][next[k]]);
          }
          if (y == 0.0f) {
            samples[0][j] = SELF * c0 + PULL2 * (d0 + samples[0][prev[j]]);
            samples[1][j] = SELF * c1 + PULL2 * (d1 + samples[1][prev[j]]);
          }
          else if (y == 1.0f) {
            k = next[j];
            samples[0][k] = SELF * d0 + PULL2 * (c0 + samples[0][next[k]]);
            samples[1][k] = SELF * d1 + PULL2 * (c1 + samples[1][next[k]]);
          }
        }
      }
    }
    return intersect;
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
    return fillCheck(samples, true);
  }

  public static int[][] fillCheck(float[][] samples, boolean check)
         throws VisADException {
    if (samples == null) return null;
    if (samples.length != 2 || samples[0].length != samples[1].length) {
      throw new VisADException("samples argument bad dimensions");
    }
    if (samples[0].length < 3) return null;
    if (checkAndFixSelfIntersection(samples)) {
      if (check) throw new VisADException("path self intersects");
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

  /** link multiple paths into a single path */
  public static float[][] link(float[][][] ss) throws VisADException {
    if (ss == null || ss.length == 0) return null;
    int nn = ss.length;
    for (int ii=0; ii<nn; ii++) {
      if (ss[ii].length != 2 || ss[ii][0].length != ss[ii][1].length) {
        throw new VisADException("samples argument bad dimensions");
      }
      if (checkAndFixSelfIntersection(ss[ii])) {
        throw new VisADException("path self intersects");
      }
    }
    float[][] s = ss[0];
    if (nn == 1) return s;
    if (s[0].length < 3) return null;

    // compute which paths are inside other paths
    // this assumes that paths do not intersect
    boolean[][] in = new boolean[nn][nn];
    for (int ii=0; ii<nn; ii++) {
      for (int jj=0; jj<nn; jj++) {
        if (ii == jj) {
          in[ii][jj] = false;
        }
        else {
          in[ii][jj] = inside(ss[ii], ss[jj][0][0], ss[jj][1][0]);
        }
      }
    }

    // sort paths so no early path is inside a later path
    for (int ii=0; ii<nn; ii++) {
      boolean any_outer = false;
      for (int jj=ii; jj<nn; jj++) {
        // don't allow short path as outer path
        boolean in_any = (ss[jj][0].length < 3);
        for (int kk=ii; kk<nn; kk++) {
          if (in[kk][jj]) { in_any = true; break; }
        }
        if (!in_any) {
          any_outer = true;
          if (ii != jj) {
            float[][] tss = ss[ii];
            ss[ii] = ss[jj];
            ss[jj] = tss;
            boolean[] tin = in[ii];
            in[ii] = in[jj];
            in[jj] = tin;
            for (int kk=0; kk<nn; kk++) {
              boolean tb = in[kk][ii];
              in[kk][ii] = in[kk][jj];
              in[kk][jj] = tb;
            }
          }
          break;
        }
        if (!any_outer) {
          // this should never happen and indicates paths must
          // intersect, but don't throw an Exception
          // just muddle through
        }
      }
    }

    // compute orientations of paths
    boolean[] orient = new boolean[nn];
    for (int ii=1; ii<nn; ii++) {
      float area = 0.0f;
      float[][] t = ss[ii];
      int m = t[0].length;
      for (int i=0; i<m-1; i++) {
        area += t[0][i] * t[1][i+1] - t[0][i+1] * t[1][i];
      }
      area += t[0][m-1] * t[1][0] - t[0][0] * t[1][m-1];
      orient[ii] = (area > 0.0);
    }

    for (int ii=1; ii<nn; ii++) {
      float[][] t = ss[ii];
      if (t[0].length < 3) continue;
      int n = s[0].length;
      int m = t[0].length;

      // find closest points between paths in s and t
      float distance = Float.MAX_VALUE;
      int near_i = -1;
      int near_j = -1;
      for (int i=0; i<n; i++) {
        for (int j=0; j<m; j++) {
          float d = (s[0][i] - t[0][j]) * (s[0][i] - t[0][j]) +
                    (s[1][i] - t[1][j]) * (s[1][i] - t[1][j]);
          if (d < distance) {
            distance = d;
            near_i = i;
            near_j = j;
          }
        }
      }
      if (near_i < 0) continue;

      // decide if need to flip order of traversing path in t
      boolean inn = in[00][ii];
      // inn = inside(s, t[0][near_j], t[1][near_j]);
      boolean flip = ((orient[0] == orient[ii]) == inn);
// System.out.println("orient[0] = " + orient[0] + " orient[" + ii + "] = " +
//                    orient[ii] + " inn = " + inn + " flip = " + flip);

      // link paths in s and t at nearest points
      int new_n = n + m + 2;
      float[][] new_s = new float[2][new_n];
      int k = 0;
      System.arraycopy(s[0], 0, new_s[0], k, near_i + 1);
      System.arraycopy(s[1], 0, new_s[1], k, near_i + 1);
      k += near_i + 1;
      int b1 = k - 1;
      int a1 = k;
      if (flip) {
        for (int j=near_j; j>=0; j--) {
          new_s[0][k] = t[0][j];
          new_s[1][k] = t[1][j];
          k++;
        }
        for (int j=m-1; j>=near_j; j--) {
          new_s[0][k] = t[0][j];
          new_s[1][k] = t[1][j];
          k++;
        }
      }
      else {
        System.arraycopy(t[0], near_j, new_s[0], k, m - near_j);
        System.arraycopy(t[1], near_j, new_s[1], k, m - near_j);
        k += m - near_j;
        System.arraycopy(t[0], 0, new_s[0], k, near_j + 1);
        System.arraycopy(t[1], 0, new_s[1], k, near_j + 1);
        k += near_j + 1;
      }
      int b2 = k - 1;
      int a2 = k;
      System.arraycopy(s[0], near_i, new_s[0], k, n - near_i);
      System.arraycopy(s[1], near_i, new_s[1], k, n - near_i);
      s[0] = new_s[0];
      s[1] = new_s[1];

      // nudge link points away from each other
      int b1m = (b1 > 0) ? b1 - 1 : new_n - 1;
      int a1p = (a1 < new_n - 1) ? a1 + 1 : 0;
      int b2m = (b2 > 0) ? b2 - 1 : new_n - 1;
      int a2p = (a2 < new_n - 1) ? a2 + 1 : 0;
      new_s[0][b1] = SELF * new_s[0][b1] + PULL * new_s[0][b1m];
      new_s[1][b1] = SELF * new_s[1][b1] + PULL * new_s[1][b1m];
      new_s[0][a1] = SELF * new_s[0][a1] + PULL * new_s[0][a1p];
      new_s[1][a1] = SELF * new_s[1][a1] + PULL * new_s[1][a1p];
      new_s[0][b2] = SELF * new_s[0][b2] + PULL * new_s[0][b2m];
      new_s[1][b2] = SELF * new_s[1][b2] + PULL * new_s[1][b2m];
      new_s[0][a2] = SELF * new_s[0][a2] + PULL * new_s[0][a2p];
      new_s[1][a2] = SELF * new_s[1][a2] + PULL * new_s[1][a2p];

    } // end for (int ii=1; ii<nn; ii++)
    return s;
  }

  /** determine if (x, y) is inside the closed path defined by s */
  public static boolean inside(float[][] s, float x, float y)
         throws VisADException {
    if (s == null) return false;
    if (s.length != 2 || s[0].length != s[1].length) {
      throw new VisADException("samples argument bad dimensions");
    }
    int n = s[0].length;
    double angle = 0.0;
    for (int i=0; i<n; i++) {
      int ip = (i < n - 1) ? i + 1 : 0;
      double a = Math.atan2(s[0][i] - x, s[1][i] - y) -
                 Math.atan2(s[0][ip] - x, s[1][ip] - y);
      if (a < -Math.PI) a += Math.PI;
      if (a > Math.PI) a -= Math.PI;
      angle += a;
    }
    return (Math.abs(angle) > 0.5);
  }

}

