
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

/**
   Delaunay represents an abstract class for calculating an
   N-dimensional Delaunay triangulation, that can be extended
   to allow for various triangulation methods.<P>
*/
public abstract class Delaunay implements java.io.Serializable {

  // Delaunay core components
  public int[][] Tri;        // triangles/tetrahedra --> vertices
                             //   Tri = new int[ntris][dim + 1]
  public int[][] Vertices;   // vertices --> triangles/tetrahedra
                             //   Vertices = new int[nrs][nverts[i]]
  public int[][] Walk;       // triangles/tetrahedra --> triangles/tetrahedra
                             //   Walk = new int[ntris][dim + 1]
  public int[][] Edges;      // tri/tetra edges --> global edge number
                             //   Edges = new int[ntris][3 * (dim - 1)];
  public int NumEdges;       // number of unique global edge numbers

  /** The abstract constructor initializes the class's data arrays. */
  public Delaunay() throws VisADException {
    Tri = null;
    Vertices = null;
    Walk = null;
    Edges = null;
    NumEdges = 0;
  }

  public Object clone() {
    try {
      return new DelaunayCustom(null, Tri, Vertices, Walk, Edges, NumEdges);
    }
    catch (VisADException e) {
      throw new VisADError("Delaunay.clone: "+e.toString());
    }
  }

  /** The factory class method heuristically decides which extension
      to the Delaunay abstract class to use in order to construct the
      fastest triangulation, and calls that extension, returning the
      finished triangulation.  The exact parameter is an indication of
      whether the exact Delaunay triangulation is required. */
  public static Delaunay factory(float[][] samples, boolean exact)
                                                  throws VisADException {

    /* Note: Clarkson doesn't work well for very closely clumped site values,
             since the algorithm rounds each value to the nearest integer
             before computing the triangulation.  This fact should probably
             be taken into account in this factory algorithm, but as of yet
             is not.  In other words, if you need an exact triangulation
             and have more than 3000 data sites, and they have closely
             clumped values, be sure to scale them up before calling the
             factory method. */

    /* Note: The factory method should be modified and/or extended if
             a new Delaunay extension is created, so that the algorithm
             takes that new extension into account. */

    int choice;      // 0 = fast, 1 = clarkson, 2 = watson

    int dim = samples.length;
    if (dim < 2) throw new VisADException("Delaunay.factory: "
                                         +"dimension must be 2 or higher");

    // only Clarkson can handle triangulations in high dimensions
    if (dim > 3) choice = 1;
    else {
      // in 2-D, Fast is preferred unless exact triangulation is needed
      if (dim == 2 && !exact) choice = 0;
      else {
        int nrs = samples[0].length;
        for (int i=1; i<dim; i++) {
          nrs = Math.min(nrs, samples[i].length);
        }
        // Clarkson isn't faster than Watson until around 3000 sites
        if (nrs > 3000) choice = 1;
        else choice = 2;
      }
    }

    if (choice == 0) {
      // triangulate with the Fast method
      DelaunayFast delan = new DelaunayFast(samples);
      return (Delaunay) delan;
    }
    if (choice == 1) {
      // triangulate with the Clarkson method
      DelaunayClarkson delan = new DelaunayClarkson(samples);
      return (Delaunay) delan;
    }
    if (choice == 2) {
      // triangulate with the Watson method
      DelaunayWatson delan = new DelaunayWatson(samples);
      return (Delaunay) delan;
    }

    return null;
  }

  /** finish_triang calculates a triangulation's helper arrays, Walk and Edges,
      if the triangulation algorithm hasn't calculated them already.  Any
      extension to the Delaunay class should call finish_triang at the end
      of its triangulation constructor. */
  public void finish_triang(float[][] samples) throws VisADException {
    int dim = samples.length;
    int dim1 = dim+1;
    int ntris = Tri.length;
    int nrs = samples[0].length;
    for (int i=1; i<dim; i++) {
      nrs = Math.min(nrs, samples[i].length);
    }

    if (Vertices == null) {
      // build Vertices component
      Vertices = new int[nrs][];
      int[] nverts = new int[nrs];
      for (int i=0; i<ntris; i++) {
        for (int j=0; j<dim1; j++) nverts[Tri[i][j]]++;
      }
      for (int i=0; i<nrs; i++) {
        Vertices[i] = new int[nverts[i]];
        nverts[i] = 0;
      }
      for (int i=0; i<ntris; i++) {
        for (int j=0; j<dim1; j++) {
          Vertices[Tri[i][j]][nverts[Tri[i][j]]++] = i;
        }
      }
    }

    if (Walk == null && dim <= 3) {
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
                if (dim == 2) {
                  if (temp == Vertices[Tri[i][v2]][l]) {
                    Walk[i][j] = temp;
                    continue WalkDim;
                  }
                }
                else {    // dim == 3
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
                } // end if (dim == 3)
              } // end for (int l=0; l<Vertices[Tri[i][v2]].length; l++)
            } // end if (temp != i)
          } // end for (int k=0; k<Vertices[Tri[i][v1]].length; k++)
        } // end for (int j=0; j<dim1; j++)
      } // end for (int i=0; i<Tri.length; i++)
    } // end if (Walk == null && dim <= 3)

    if (Edges == null && dim <= 3) {
      // build Edges component

      // initialize all edges to "not yet found"
      int edim = 3*(dim-1);
      Edges = new int[ntris][edim];
      for (int i=0; i<ntris; i++) {
        for (int j=0; j<edim; j++) Edges[i][j] = -1;
      }

      // calculate global edge values
      NumEdges = 0;
      if (dim == 2) {
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
                /* Edges[othtri][cside] = NumEdges; */
              }
              Edges[i][j] = NumEdges++;
            }
          }
        }
      }
      else {    // dim == 3
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
                  if (temp == Vertices[endpt2][p2]) {
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
            } // end if (Edges[i][j] < 0)
          } // end for (int j=0; j<6; j++)
        } // end for (int i=0; i<ntris; i++)
      } // end if (dim == 3)
    } // end if (Edges == null && dim <= 3)
  }

  public String toString() {
    return sampleString(null);
  }

  public String sampleString(float[][] samples) {
    StringBuffer s = new StringBuffer("");
    if (samples != null) {
      s.append("\nsamples " + samples[0].length + "\n");
      for (int i=0; i<samples[0].length; i++) {
        s.append("  " + i + " -> " + samples[0][i] + " " +
                 samples[1][i] + " " + samples[2][i] + "\n");
      }
      s.append("\n");
    }

    s.append("\nTri (triangles -> vertices) " + Tri.length + "\n");
    for (int i=0; i<Tri.length; i++) {
      s.append("  " + i + " -> ");
      for (int j=0; j<Tri[i].length; j++) {
        s.append(" " + Tri[i][j]);
      }
      s.append("\n");
    }

    s.append("\nVertices (vertices -> triangles) " + Vertices.length + "\n");
    for (int i=0; i<Vertices.length; i++) {
      s.append("  " + i + " -> ");
      for (int j=0; j<Vertices[i].length; j++) {
        s.append(" " + Vertices[i][j]);
      }
      s.append("\n");
    }

    s.append("\nWalk (triangles -> triangles) " + Walk.length + "\n");
    for (int i=0; i<Walk.length; i++) {
      s.append("  " + i + " -> ");
      for (int j=0; j<Walk[i].length; j++) {
        s.append(" " + Walk[i][j]);
      }
      s.append("\n");
    }

    s.append("\nEdges (triangles -> global edges) " + Edges.length + "\n");
    for (int i=0; i<Edges.length; i++) {
      s.append("  " + i + " -> ");
      for (int j=0; j<Edges[i].length; j++) {
        s.append(" " + Edges[i][j]);
      }
      s.append("\n");
    }
    return s.toString();
  }

/*
  public int[][] Tri;        // triangles/tetrahedra --> vertices
                             //   Tri = new int[ntris][dim + 1]
  public int[][] Vertices;   // vertices --> triangles/tetrahedra
                             //   Vertices = new int[nrs][nverts[i]]
  public int[][] Walk;       // triangles/tetrahedra --> triangles/tetrahedra
                             //   Walk = new int[ntris][dim + 1]
  public int[][] Edges;      // tri/tetra edges --> global edge number
                             //   Edges = new int[ntris][3 * (dim - 1)];
  public int NumEdges;       // number of unique global edge numbers
*/

}

