
//
// Delaunay.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.io.*;
import java.util.*;

/* The Delaunay triangulation/tetrahedralization algorithm in this class is
 * originally from nnsort.c by David F. Watson:
 *
 * nnsort() finds the Delaunay triangulation of the two- or three-component vectors
 * in 'data_list' and returns a list of simplex vertices in 'vertices' with
 * the corresponding circumcentre and squared radius in the rows of 'circentres'. 
 * nnsort() also can be used to find the ordered convex hull of the two- or three-
 * component vectors in 'data_list' and returns a list of (d-1)-facet vertices 
 * in 'vertices' (dummy filename for 'circentres' must be used).
 * nnsort() was written by Dave Watson and uses the algorithm described in -
 *    Watson, D.F., 1981, Computing the n-dimensional Delaunay tessellation with 
 *          application to Voronoi polytopes: The Computer J., 24(2), p. 167-172. 
 *
 * additional information about this algorithm can be found in -
 *    CONTOURING: A guide to the analysis and display of spatial data,
 *    by David F. Watson, Pergamon Press, 1992, ISBN 0 08 040286 0
 *                                                                              */

/**
   Delaunay represents a method to find the Delaunay triangulation or
   tetrahedralization of a set of samples of R^2 or R^3.<P>
*/
public class Delaunay implements java.io.Serializable {

  static final double BIGNUM = 1E37;
  static final double EPSILON = 0.00001;
  // temporary storage size factor
  static final int TSIZE = 75;
  // factor (>=1) for radius of control points
  static final double RANGE = 10.0;

  // Delaunay core components
  public int[][] Tri;        // triangles/tetrahedra --> vertices
  public int[][] Vertices;   // vertices --> triangles/tetrahedra
  public int[][] Walk;       // triangles/tetrahedra --> triangles/tetrahedra
  public int[][] Edges;      // tri/tetra edges --> global edge number
  public int NumEdges;       // number of unique global edge numbers

  public Delaunay(double[][] samples) throws VisADException {
    int dim = samples.length;
    int nrs = samples[0].length;
    int chl = 0; // find Delaunay triangulation

    double xx, yy, bgs;
    int i0, i1, i2, i3, i4, i5, i6, i7, i8, i9, i11;
    int[] ii = new int[3];
    int dm, dim1, nts, tsz;

    double[][] mxy = new double[2][dim];
    for (i0=0; i0<dim; i0++) mxy[0][i0] = - (mxy[1][i0] = BIGNUM);
    dim1 = dim + 1;
    double[][] wrk = new double[dim][dim1];
    for (i0=0; i0<dim; i0++) for (i1=0; i1<dim1; i1++) wrk[i0][i1] = -RANGE;
    for (i0=0; i0<dim; i0++) wrk[i0][i0] = RANGE * (3 * dim - 1);

    double[][] pts = new double[nrs + dim1][dim];
    for (i0=0; i0<nrs; i0++) {
      if (dim < 3) {
        pts[i0][0] = samples[0][i0];
        pts[i0][1] = samples[1][i0];
      }
      else {
        pts[i0][0] = samples[0][i0];
        pts[i0][1] = samples[1][i0];
        pts[i0][2] = samples[2][i0];
      }
      // compute bounding box
      for (i1=0; i1<dim; i1++) {
        if (mxy[0][i1] < pts[i0][i1]) mxy[0][i1] = pts[i0][i1]; // max
        if (mxy[1][i1] > pts[i0][i1]) mxy[1][i1] = pts[i0][i1]; // min
      }
    }

    for (bgs=0, i0=0; i0<dim; i0++)  {
      mxy[0][i0] -= mxy[1][i0];
      if (bgs < mxy[0][i0]) bgs = mxy[0][i0];
    }
    // now bgs = largest range
    // add random perturbations to points
    bgs *= EPSILON;

    Random rand = new Random(367);
    for (i0=0; i0<nrs; i0++) for (i1=0; i1<dim; i1++) {
      // random numbers [0, 1]
      pts[i0][i1] += bgs * (0.5 - rand.nextDouble() / 0x7fffffff);
    }
    for (i0=0; i0<dim1; i0++) for (i1=0; i1<dim; i1++) {
      pts[nrs+i0][i1] = mxy[1][i1] + wrk[i1][i0] * mxy[0][i1];
    }
    for (i1=1, i0=2; i0<dim1; i0++) i1 *= i0;
    tsz = TSIZE * i1;
    int[][] tmp = new int[tsz + 1][dim];
    // storage allocation - increase value of `i1' for 3D if necessary
    i1 *= (nrs + 50 * i1);
    int[] id = new int[i1];
    for (i0=0; i0<i1; i0++) id[i0] = i0;
    int[][] a3s = new int[i1][dim1];
    double[][] ccr = new double[i1][dim1];
    for (a3s[0][0]=nrs, i0=1; i0<dim1; i0++) a3s[0][i0] = a3s[0][i0-1] + 1;
    for (ccr[0][dim]=BIGNUM, i0=0; i0<dim; i0++) ccr[0][i0] = 0;
    nts = i4 = 1;
    dm = dim - 1;
    for (i0=0; i0<nrs; i0++) {
      i1 = i7 = -1;
      i9 = 0;
Loop3:
      for (i11=0; i11<nts; i11++) {
        i1++;
        while (a3s[i1][0] < 0) i1++;
        xx = ccr[i1][dim];
        for (i2=0; i2<dim; i2++) {
          xx -= (pts[i0][i2] - ccr[i1][i2]) * (pts[i0][i2] - ccr[i1][i2]);
          if (xx<0) continue Loop3;
        }
        i9--;
        i4--;
        id[i4] = i1;
Loop2:
        for (i2=0; i2<dim1; i2++) {
          ii[0] = 0;
          if (ii[0] == i2) ii[0]++;
          for (i3=1; i3<dim; i3++) {
            ii[i3] = ii[i3-1] + 1;
            if (ii[i3] == i2) ii[i3]++;
          }
          if (i7>dm) {
            i8 = i7;
Loop1:
            for (i3=0; i3<=i8; i3++) {
              for (i5=0; i5<dim; i5++) {
                if (a3s[i1][ii[i5]] != tmp[i3][i5]) continue Loop1;
              }
              for (i6=0; i6<dim; i6++) tmp[i3][i6] = tmp[i8][i6];
              i7--;
              continue Loop2;
            }
          }
          if (++i7 > tsz) {
            int newtsz = 2 * tsz;
            int[][] newtmp = new int[newtsz + 1][dim];
            System.arraycopy(tmp, 0, newtmp, 0, tsz);
            tsz = newtsz;
            tmp = newtmp;
            // WLH 23 july 97
            // throw new VisADException(
            //                "Delaunay: Temporary storage exceeded");
          }
          for (i3=0; i3<dim; i3++) tmp[i7][i3] = a3s[i1][ii[i3]];
        }
        a3s[i1][0] = -1;
      }
      for (i1=0; i1<=i7; i1++) {
        for (i2=0; i2<dim; i2++) {
          for (wrk[i2][dim]=0, i3=0; i3<dim; i3++) {
            wrk[i2][i3] = pts[tmp[i1][i2]][i3] - pts[i0][i3];
            wrk[i2][dim] += wrk[i2][i3] * (pts[tmp[i1][i2]][i3]
                                        + pts[i0][i3]) / 2;
          }
        }
        if (dim < 3) {
          xx = wrk[0][0] * wrk[1][1] - wrk[1][0] * wrk[0][1];
          ccr[id[i4]][0] = (wrk[0][2] * wrk[1][1]
                         - wrk[1][2] * wrk[0][1]) / xx;
          ccr[id[i4]][1] = (wrk[0][0] * wrk[1][2]
                         - wrk[1][0] * wrk[0][2]) / xx;
        }
        else {
          xx = (wrk[0][0] * (wrk[1][1] * wrk[2][2] - wrk[2][1] * wrk[1][2])) 
             - (wrk[0][1] * (wrk[1][0] * wrk[2][2] - wrk[2][0] * wrk[1][2])) 
             + (wrk[0][2] * (wrk[1][0] * wrk[2][1] - wrk[2][0] * wrk[1][1]));
          ccr[id[i4]][0] = ((wrk[0][3] * (wrk[1][1] * wrk[2][2]
                           - wrk[2][1] * wrk[1][2])) 
                          - (wrk[0][1] * (wrk[1][3] * wrk[2][2]
                           - wrk[2][3] * wrk[1][2])) 
                          + (wrk[0][2] * (wrk[1][3] * wrk[2][1]
                           - wrk[2][3] * wrk[1][1]))) / xx;
          ccr[id[i4]][1] = ((wrk[0][0] * (wrk[1][3] * wrk[2][2]
                           - wrk[2][3] * wrk[1][2])) 
                          - (wrk[0][3] * (wrk[1][0] * wrk[2][2]
                           - wrk[2][0] * wrk[1][2])) 
                          + (wrk[0][2] * (wrk[1][0] * wrk[2][3]
                           - wrk[2][0] * wrk[1][3]))) / xx;
          ccr[id[i4]][2] = ((wrk[0][0] * (wrk[1][1] * wrk[2][3]
                           - wrk[2][1] * wrk[1][3])) 
                          - (wrk[0][1] * (wrk[1][0] * wrk[2][3]
                           - wrk[2][0] * wrk[1][3])) 
                          + (wrk[0][3] * (wrk[1][0] * wrk[2][1]
                           - wrk[2][0] * wrk[1][1]))) / xx;
        }
        for (ccr[id[i4]][dim]=0, i2=0; i2<dim; i2++) {
          ccr[id[i4]][dim] += (pts[i0][i2] - ccr[id[i4]][i2])
                            * (pts[i0][i2] - ccr[id[i4]][i2]);
          a3s[id[i4]][i2] = tmp[i1][i2];
        }
        a3s[id[i4]][dim] = i0;
        i4++;
        i9++;
      }
      nts += i9;
    }

/* OUTPUT is in a3s ARRAY
   needed output is:
     Tri      - array of pointers from triangles or tetrahedra to their
                corresponding vertices
     Vertices - array of pointers from vertices to their
                corresponding triangles or tetrahedra
     Walk     - array of pointers from triangles or tetrahedra to neighboring
                triangles or tetrahedra
     Edges    - array of pointers from each triangle or tetrahedron's edges
                to their corresponding triangles or tetrahedra

   helpers:
     nverts - number of triangles or tetrahedra per vertex
*/

    // compute number of triangles or tetrahedra
    int[] nverts = new int[nrs];
    for (int i=0; i<nrs; i++) nverts[i] = 0;
    int ntris = 0;
    i0 = -1;
    for (i11=0; i11<nts; i11++) {
      i0++;
      while (a3s[i0][0] < 0) i0++;
      if (a3s[i0][0] < nrs) {
        ntris++;
        if (dim < 3) {
          nverts[a3s[i0][0]]++;
          nverts[a3s[i0][1]]++;
          nverts[a3s[i0][2]]++;
        }
        else {
          nverts[a3s[i0][0]]++;
          nverts[a3s[i0][1]]++;
          nverts[a3s[i0][2]]++;
          nverts[a3s[i0][3]]++;
        }
      }
    }
    Vertices = new int[nrs][];
    for (int i=0; i<nrs; i++) Vertices[i] = new int[nverts[i]];
    for (int i=0; i<nrs; i++) nverts[i] = 0;

    // build Tri & Vertices components
    Tri = new int[ntris][dim1];
    int a, b, c, d;
    int itri = 0;
    i0 = -1;
    for (i11=0; i11<nts; i11++) {
      i0++;
      while (a3s[i0][0] < 0) i0++;
      if (a3s[i0][0] < nrs) {
        if (dim < 3) {
          a = a3s[i0][0];
          b = a3s[i0][1];
          c = a3s[i0][2];
          Vertices[a][nverts[a]] = itri;
          nverts[a]++;
          Vertices[b][nverts[b]] = itri;
          nverts[b]++;
          Vertices[c][nverts[c]] = itri;
          nverts[c]++;
          Tri[itri][0] = a;
          Tri[itri][1] = b;
          Tri[itri][2] = c;
        }
        else {
          a = a3s[i0][0];
          b = a3s[i0][1];
          c = a3s[i0][2];
          d = a3s[i0][3];
          Vertices[a][nverts[a]] = itri;
          nverts[a]++;
          Vertices[b][nverts[b]] = itri;
          nverts[b]++;
          Vertices[c][nverts[c]] = itri;
          nverts[c]++;
          Vertices[d][nverts[d]] = itri;
          nverts[d]++;
          Tri[itri][0] = a;
          Tri[itri][1] = b;
          Tri[itri][2] = c;
          Tri[itri][3] = d;
        }
        itri++;
      }
    }

    // build Walk component
    Walk = new int[ntris][dim1];
    for (int i=0; i<Tri.length; i++) {
WalkDim:
      for (int j=0; j<dim1; j++) {
        int v1 = j;
        int v2 = (v1+1)%dim1;
        Walk[i][j] = -1;
        for (int k=0; k<Vertices[Tri[i][v1]].length; k++) {
          int temp = Vertices[Tri[i][v1]][k];
          if (temp != i) {
            for (int l=0; l<Vertices[Tri[i][v2]].length; l++) {
              if (dim < 3) {
                if (temp == Vertices[Tri[i][v2]][l]) {
                  Walk[i][j] = temp;
                  continue WalkDim;
                }
              }
              else {
                int temp2 = Vertices[Tri[i][v2]][l];
                int v3 = (v2+1)%dim1;
                if (temp == temp2) {
                  for (int m=0; m<Vertices[Tri[i][v3]].length; m++) {
                    if (temp == Vertices[Tri[i][v3]][m]) {
                      Walk[i][j] = temp;
                      continue WalkDim;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // build Edges component

    // initialize all edges to "not yet found"
    int edim = 3*(dim-1);
    Edges = new int[ntris][edim];
    for (int i=0; i<ntris; i++) {
      for (int j=0; j<edim; j++) Edges[i][j] = -1;
    }

    // calculate global edge values
    NumEdges = 0;
    if (dim < 3) {
      for (int i=0; i<ntris; i++) {
        for (int j=0; j<3; j++) {
          if (Edges[i][j] < 0) {
            // this edge doesn't have a "global edge number" yet
            int othtri = Walk[i][j];
            if (othtri >= 0) {
              int cside = -1;
              for (int k=0; k<3; k++) {
                if (Walk[othtri][k] == i) cside = k;
              }
              Edges[othtri][cside] = NumEdges;
              
            }
            Edges[i][j] = NumEdges++;
          }
        }
      }
    }
    else {
      int[] ptlook1 = {0, 0, 0, 1, 1, 2};
      int[] ptlook2 = {1, 2, 3, 2, 3, 3};
      for (int i=0; i<ntris; i++) {
        for (int j=0; j<6; j++) {
          if (Edges[i][j] < 0) {
            // this edge doesn't have a "global edge number" yet
  
            // search through the edge's two end points
            int endpt1 = Tri[i][ptlook1[j]];
            int endpt2 = Tri[i][ptlook2[j]];
  
            // create an intersection of two sets
            int[] set = new int[Vertices[endpt1].length];
            int setlen = 0;
            for (int p1=0; p1<Vertices[endpt1].length; p1++) {
              int temp = Vertices[endpt1][p1];
              for (int p2=0; p2<Vertices[endpt2].length; p2++) {
                if (temp == Vertices[endpt1][p2]) {
                  set[setlen++] = temp;
                  break;
                }
              }
            }
  
            // assign global edge number to all members of set 
            for (int k=0; k<setlen; k++) {
              for (int l=0; l<edim; l++) {
                if ((Tri[k][ptlook1[l]] == endpt1
                  && Tri[k][ptlook2[l]] == endpt2)
                 || (Tri[k][ptlook1[l]] == endpt2
                  && Tri[k][ptlook2[l]] == endpt1)) {
                  Edges[k][l] = NumEdges;
                }
              }
            }
            Edges[i][j] = NumEdges++;
          }
        }
      }
    }
  } // end constructor

  /* run 'java visad.Delaunay < formatted_input_stream'
     to test the Delaunay class */
  public static void main(String[] argv) throws VisADException {

    // Define input stream
    InputStreamReader inStr = new InputStreamReader(System.in);

    // Define temporary integer array
    int[] ints = new int[80];
    try {
      ints[0] = inStr.read();
    }
    catch(Exception e) {
      throw new VisADException("Delaunay: "+e);
    }
    int l = 0;
    while (ints[l] != 32) {
      try {
        ints[++l] = inStr.read();
      }
      catch (Exception e) {
        throw new VisADException("Delaunay: "+e);
      }
    }
    // convert array of integers to array of characters
    char[] chars = new char[l];
    for (int i=0; i<l; i++) {
      chars[i] = (char) ints[i];
    }
    int num_dim = Integer.parseInt(new String(chars));
    // num_dim should be 2 or 3
    if ( (num_dim < 2) || (num_dim > 3) ) {
      throw new VisADException(
            "Delaunay: coordinates must be either R^2 or R^3");
    }

    ints = new int[80];
    try {
      ints[0] = inStr.read();
    }
    catch (Exception e) {
      throw new VisADException("Delaunay: "+e);
    }
    l = 0;
    while (ints[l] != 10) {
      try {
        ints[++l] = inStr.read();
      }
      catch (Exception e) {
        throw new VisADException("Delaunay: "+e);
      }
    }
    // convert array of integers to array of characters
    chars = new char[l];
    for (int i=0; i<l; i++) {
      chars[i] = (char) ints[i];
    }
    int num_coords = Integer.parseInt(new String(chars));

    // num_coords should be at least four
    if (num_coords < 4) {
      throw new VisADException(
            "Delaunay: must be at least four input coordinates");
    }

    // Define size of Samples array
    double[][] samp = new double[num_dim][num_coords];
    System.out.println("num_dimensions = "+num_dim
                      +", num_coords = "+num_coords+"\n");

    // Skip blank line
    try {
      ints[0] = inStr.read();
    }
    catch (Exception e) {
      throw new VisADException("Delaunay: "+e);
    }

    for (int c=0; c<num_coords; c++) {
      for (int d=0; d<num_dim; d++) {
        l = 0;
        try {
          ints[0] = inStr.read();
        }
        catch (Exception e) {
          throw new VisADException("Delaunay: "+e);
        }
        while ( (ints[l] != 32) && (ints[l] != 10) ) {
          try {
            ints[++l] = inStr.read();
          }
          catch (Exception e) {
            throw new VisADException("Delaunay: "+e);
          }
        }
        chars = new char[l];
        for (int i=0; i<l; i++) {
          chars[i] = (char) ints[i];
        }
        samp[d][c] = (Double.valueOf(new String(chars))).doubleValue();
      }
    }

    // do EOF stuff
    try {
      inStr.close();
    }
    catch (Exception e) {
      throw new VisADException("Delaunay: "+e);
    }

    // The Big Command--calculate the triangulation/tetrahedralization
    Delaunay del = new Delaunay(samp);

    // output samp array
    System.out.println("Samples array:");
    for (int i=0; i<samp[0].length; i++) {
      System.out.print("#"+i+":  ");
      for (int j=0; j<num_dim; j++) {
        System.out.print(samp[j][i]);
        if (j==num_dim-1) {
          System.out.println();
        }
        else {
          System.out.print(", ");
        }
      }
    }

    // output Tri and Walk arrays
    System.out.println("\nTri array:");
    for (int i=0; i<del.Tri.length; i++) {
      for (int j=0; j<del.Tri[0].length; j++) {
        System.out.println("["+i+", "+j+"] = "+del.Tri[i][j]);
      }
    }
    System.out.println("\nWalk array:");
    for (int i=0; i<del.Walk.length; i++) {
      for (int j=0; j<del.Walk[0].length; j++) {
        System.out.println("["+i+", "+j+"] = "+del.Walk[i][j]);
      }
    }
    System.out.println();
  }

/* Here's the output with sample file Delaunay2D.txt

iris 38% java visad.Delaunay < Delaunay2D.txt
num_dimensions = 2, num_coords = 6

Samples array:
#0:  397.0, 22.0
#1:  169.0, 105.0
#2:  425.0, 321.0
#3:  159.0, 281.0
#4:  520.0, 173.0
#5:  333.0, 146.0

Tri array:
[0, 0] = 1
[0, 1] = 3
[0, 2] = 5
[1, 0] = 0
[1, 1] = 4
[1, 2] = 5
[2, 0] = 2
[2, 1] = 4
[2, 2] = 5
[3, 0] = 2
[3, 1] = 3
[3, 2] = 5
[4, 0] = 0
[4, 1] = 1
[4, 2] = 5

Walk array:
[0, 0] = -1
[0, 1] = 3
[0, 2] = 4
[1, 0] = -1
[1, 1] = 2
[1, 2] = 4
[2, 0] = -1
[2, 1] = 1
[2, 2] = 3
[3, 0] = -1
[3, 1] = 0
[3, 2] = 2
[4, 0] = -1
[4, 1] = 0
[4, 2] = 1

iris 39% 

Here's the output with sample file Delaunay3D.txt

iris 39% java visad.Delaunay < Delaunay3D.txt
num_dimensions = 3, num_coords = 5

Samples array:
#0:  1.0, 1.0, 1.0
#1:  5.0, 1.0, 1.0
#2:  3.0, 7.0, 1.0
#3:  3.0, 1.0, 10.0
#4:  3.0, -5.0, 1.0

Tri array:
[0, 0] = 0
[0, 1] = 1
[0, 2] = 2
[0, 3] = 3
[1, 0] = 0
[1, 1] = 1
[1, 2] = 3
[1, 3] = 4

Walk array:
[0, 0] = -1
[0, 1] = -1
[0, 2] = -1
[0, 3] = 1
[1, 0] = 0
[1, 1] = -1
[1, 2] = -1
[1, 3] = -1

iris 40% 

*/
}

/*
  DO NOT DELETE THIS COMMENTED CODE
  IT CONTAINS ALGORITHM DETAILS NOT CAST INTO JAVA (YET?)
  i0 = -1;
  for (i11=0; i11<nts; i11++) {
    i0++;
    while (a3s[i0][0] < 0) i0++;
    if (a3s[i0][0] < nrs) {
      for (i1=0; i1<dim; i1++) for (i2=0; i2<dim; i2++) {
        wrk[i1][i2] = pts[a3s[i0][i1]][i2] - pts[a3s[i0][dim]][i2];
      }
      if (dim < 3) {
        xx = wrk[0][0] * wrk[1][1] - wrk[0][1] * wrk[1][0];
        if (fabs(xx) > EPSILON) {
          if (xx < 0)
            fprintf(afile,"%d %d %d\n",a3s[i0][0],a3s[i0][2],a3s[i0][1]);
          else fprintf(afile,"%d %d %d\n",a3s[i0][0],a3s[i0][1],a3s[i0][2]);
          fprintf(bfile,"%e %e %e\n",ccr[i0][0],ccr[i0][1],ccr[i0][2]);
        }
      }
      else {
        xx = ((wrk[0][0] * (wrk[1][1] * wrk[2][2] - wrk[2][1] * wrk[1][2])) 
           -  (wrk[0][1] * (wrk[1][0] * wrk[2][2] - wrk[2][0] * wrk[1][2])) 
           +  (wrk[0][2] * (wrk[1][0] * wrk[2][1] - wrk[2][0] * wrk[1][1])));
        if (fabs(xx) > EPSILON) {
          if (xx < 0)
            fprintf(afile,"%d %d %d %d\n",
                    a3s[i0][0],a3s[i0][1],a3s[i0][3],a3s[i0][2]);
          else fprintf(afile,"%d %d %d %d\n",
                       a3s[i0][0],a3s[i0][1],a3s[i0][2],a3s[i0][3]);
          fprintf(bfile,"%e %e %e %e\n",
                  ccr[i0][0],ccr[i0][1],ccr[i0][2],ccr[i0][3]);
        }
      }
    }
  }
*/

