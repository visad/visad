
//
// Delaunay.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
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

// packages for main method
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
      throw new VisADError("Delaunay.clone: " + e.toString());
    }
  }

  /** The factory class method heuristically decides which extension
      to the Delaunay abstract class to use in order to construct the
      fastest triangulation, and calls that extension, returning the
      finished triangulation.  The exact parameter is an indication of
      whether the exact Delaunay triangulation is required.  The
      method chooses from among the Fast, Clarkson, and Watson methods. */
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

    /* Note: The factory method will not take new Delaunay extensions into
             account unless it is extended as well. */

    int choice;
    int FAST = 0;
    int CLARKSON = 1;
    int WATSON = 2;

    int dim = samples.length;
    if (dim < 2) throw new VisADException("Delaunay.factory: "
                                         +"dimension must be 2 or higher");

    // only Clarkson can handle triangulations in high dimensions
    if (dim > 3) {
      choice = CLARKSON;
    }
    else {
      int nrs = samples[0].length;
      for (int i=1; i<dim; i++) {
        nrs = Math.min(nrs, samples[i].length);
      }
      if (dim == 2 && !exact && nrs > 10000) {
        // use fast in 2-D with a very large set and exact not required
        choice = FAST;
      }
      else if (nrs > 3000) {
        // use Clarkson for large sets
        choice = CLARKSON;
      }
      else {
        choice = WATSON;
      }
    }

    try {
      if (choice == FAST) {
        // triangulate with the Fast method
        DelaunayFast delan = new DelaunayFast(samples);
        return (Delaunay) delan;
      }
      if (choice == CLARKSON) {
        // triangulate with the Clarkson method
        DelaunayClarkson delan = new DelaunayClarkson(samples);
        return (Delaunay) delan;
      }
      if (choice == WATSON) {
        // triangulate with the Watson method
        DelaunayWatson delan = new DelaunayWatson(samples);
        return (Delaunay) delan;
      }
    }
    catch (Exception e) {
    }

    return null;
  }

  /** scale alters the values of the samples by multiplying them by
      the mult factor; copy specifies whether scale should modify
      the actual samples or a copy of them. */
  public static float[][] scale(float[][] samples, float mult,
                                boolean copy) throws VisADException {
    int dim = samples.length;
    int nrs = samples[0].length;
    for (int i=1; i<dim; i++) {
      if (samples[i].length < nrs) nrs = samples[i].length;
    }

    // make a copy if needed
    float[][] samp;
    if (copy) {
      samp = new float[dim][nrs];
      for (int i=0; i<dim; i++) {
        System.arraycopy(samples, 0, samp, 0, nrs);
      }
    }
    else {
      samp = samples;
    }

    // scale points
    for (int i=0; i<dim; i++) {
      for (int j=0; j<nrs; j++) {
        samp[i][j] *= mult;
      }
    }

    return samp;
  }

  /** perturb alters the values of the samples by up to epsilon in
      either direction, to eliminate triangulation problems such as
      co-linear points; copy specifies whether perturb should modify
      the actual samples or a copy of them. */
  public static float[][] perturb(float[][] samples, float epsilon,
                                  boolean copy) throws VisADException {
    int dim = samples.length;
    int nrs = samples[0].length;
    for (int i=1; i<dim; i++) {
      if (samples[i].length < nrs) nrs = samples[i].length;
    }

    // make a copy if needed
    float[][] samp;
    if (copy) {
      samp = new float[dim][nrs];
      for (int i=0; i<dim; i++) {
        System.arraycopy(samples, 0, samp, 0, nrs);
      }
    }
    else {
      samp = samples;
    }

    // perturb points
    for (int i=0; i<dim; i++) {
      for (int j=0; j<nrs; j++) {
        samp[i][j] += (float)(2*epsilon*(Math.random()-0.5));
      }
    }

    return samp;
  }

  /** test checks a triangulation in various ways to make sure it
      is constructed correctly; test returns false if there are
      any problems with the triangulation. */
  public boolean test(float[][] samples) throws VisADException {

    int dim = samples.length;
    int dim1 = dim+1;
    int ntris = Tri.length;
    int nrs = samples[0].length;
    for (int i=1; i<dim; i++) {
      nrs = Math.min(nrs, samples[i].length);
    }

    // verify triangulation dimension
    for (int i=0; i<ntris; i++) {
      if (Tri[i].length < dim1) return false;
    }

    // verify no illegal triangle vertices
    for (int i=0; i<ntris; i++) {
      for (int j=0; j<dim1; j++) {
        if (Tri[i][j] < 0 || Tri[i][j] >= nrs) return false;
      }
    }

    // verify that all points are in at least one triangle
    int[] nverts = new int[nrs];
    for (int i=0; i<nrs; i++) nverts[i] = 0;
    for (int i=0; i<ntris; i++) {
      for (int j=0; j<dim1; j++) nverts[Tri[i][j]]++;
    }
    for (int i=0; i<nrs; i++) {
      if (nverts[i] == 0) return false;
    }

    // test for duplicate triangles
    for (int i=0; i<ntris; i++) {
      for (int j=i+1; j<ntris; j++) {
        boolean[] m = new boolean[dim1];
        for (int mi=0; mi<dim1; mi++) m[mi] = false;
        for (int k=0; k<dim1; k++) {
          for (int l=0; l<dim1; l++) {
            if (Tri[i][k] == Tri[j][l] && !m[l]) {
              m[l] = true;
            }
          }
        }
        boolean mtot = true;
        for (int k=0; k<dim1; k++) {
          if (!m[k]) mtot = false;
        }
        if (mtot) return false;
      }
    }

    // test for errors in Walk array
    for (int i=0; i<ntris; i++) {
      for (int j=0; j<dim1; j++) {
        if (Walk[i][j] != -1) {
          boolean found = false;
          for (int k=0; k<dim1; k++) {
            if (Walk[Walk[i][j]][k] == i) found = true;
          }
          if (!found) return false;

          // make sure two walk'ed triangles share dim vertices
          int sb = 0;
          for (int k=0; k<dim1; k++) {
            for (int l=0; l<dim1; l++) {
              if (Tri[i][k] == Tri[Walk[i][j]][l]) sb++;
            }
          }
          if (sb != dim) return false;
        }
      }
    }

    // Note: Another test that could be performed is one that
    //       makes sure, given a triangle T, all points in the
    //       triangulation that are not part of T are located
    //       outside the bounds of T.  This test would verify
    //       that there are no overlapping triangles.

    // all tests passed
    return true;
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
                if (cside != -1) {
                  Edges[othtri][cside] = NumEdges;
                }
                else {
                  throw new SetException("Delaunay.finish_triang: " +
                                         "error in triangulation!");
                }
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
              for (int kk=0; kk<setlen; kk++) {
                int k = set[kk];
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

  /** A graphical demonstration of implemented Delaunay triangulation
      algorithms, in the 2-D case */
  public static void main(String[] argv) throws VisADException {
    boolean problem = false;
    int points = 0;
    int type = 0;
    int l = 1;
    if (argv.length < 2) problem = true;
    else {
      try {
        points = Integer.parseInt(argv[0]);
        type = Integer.parseInt(argv[1]);
        if (argv.length > 2) l = Integer.parseInt(argv[2]);
        if (points < 1 || type < 1 || type > 3 || l < 1 || l > 3) {
          problem = true;
        }
      }
      catch (NumberFormatException exc) {
        problem = true;
      }
    }
    if (problem) {
      System.out.println("Usage:\n" +
                         "   java visad.Delaunay points type [label]\n" +
                         "points = The number of points to triangulate.\n" +
                         "type   = The triangulation method to use:\n" +
                         "         1 = Clarkson\n" +
                         "         2 = Fast\n" +
                         "         3 = Watson\n" +
                         "label  = How to label the diagram:\n" +
                         "         1 = No labels (default)\n" +
                         "         2 = Vertex boxes\n" +
                         "         3 = Triangle numbers\n" +
                         "         4 = Vertex numbers\n");
      System.exit(1);
    }

    float[][] samples;
    int[][] ttri = null;

    samples = new float[2][points];
    final float[] samp0 = samples[0];
    final float[] samp1 = samples[1];

    for (int i=0; i<points; i++) {
      samp0[i] = (float) (500 * Math.random());
      samp1[i] = (float) (500 * Math.random());
    }
    System.out.print("Triangulating " + points + " points with ");
    if (type == 1) {
      System.out.println("the Clarkson algorithm.");
      long start = System.currentTimeMillis();
      DelaunayClarkson delaun = new DelaunayClarkson(samples);
      long end = System.currentTimeMillis();
      float time = (end - start) / 1000f;
      System.out.println("Operation took " + time + " seconds.");
      ttri = delaun.Tri;
    }
    else if (type == 2) {
      System.out.println("the Fast algorithm.");
      long start = System.currentTimeMillis();
      DelaunayFast delaun = new DelaunayFast(samples);
      long end = System.currentTimeMillis();
      float time = (end - start) / 1000f;
      System.out.println("Operation took " + time + " seconds.");
      ttri = delaun.Tri;
    }
    else if (type == 3) {
      System.out.println("the Watson algorithm.");
      long start = System.currentTimeMillis();
      DelaunayWatson delaun = new DelaunayWatson(samples);
      long end = System.currentTimeMillis();
      float time = (end - start) / 1000f;
      System.out.println("Operation took " + time + " seconds.");
      ttri = delaun.Tri;
    }

    // set up final variables
    final int label = l;
    final int[][] tri = ttri;

    // set up GUI components
    JFrame frame = new JFrame();
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    JComponent jc = new JComponent() {
      public void paint(Graphics gr) {
        if (label == 2) {        // vertex boxes
          for (int i=0; i<samp0.length; i++) {
            gr.drawRect((int) samp0[i]-2, (int) samp1[i]-2, 4, 4);
          }
        }
        else if (label == 3) {   // triangle numbers
          for (int i=0; i<tri.length; i++) {
            int t0 = tri[i][0];
            int t1 = tri[i][1];
            int t2 = tri[i][2];
            int avgX = (int) ((samp0[t0] + samp0[t1] + samp0[t2])/3);
            int avgY = (int) ((samp1[t0] + samp1[t1] + samp1[t2])/3);
            gr.drawString(String.valueOf(i), avgX-4, avgY);
          }
        }
        else if (label == 4) {   // vertex numbers
          for (int i=0; i<samp0.length; i++) {
            gr.drawString(String.valueOf(i), (int) samp0[i],
                                             (int) samp1[i]);
          }
        }
    
        for (int i=0; i<tri.length; i++) {
          int[] t = tri[i];
          gr.drawLine((int) samp0[t[0]],
                      (int) samp1[t[0]],
                      (int) samp0[t[1]],
                      (int) samp1[t[1]]);
          gr.drawLine((int) samp0[t[1]],
                      (int) samp1[t[1]],
                      (int) samp0[t[2]],
                      (int) samp1[t[2]]);
          gr.drawLine((int) samp0[t[2]],
                      (int) samp1[t[2]],
                      (int) samp0[t[0]],
                      (int) samp1[t[0]]);
        }
      }
    };
    frame.getContentPane().add(jc);
    frame.setSize(new Dimension(510, 530));
    frame.setVisible(true);
  }

}

