
//
// DelaunayCustom.java
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

/**
   DelaunayCustom is a set of constructors to create an instance
   of Delaunay by passing in a pre-computed triangulation.
   DelaunayCustom is useful for creating instances of Delaunay
   that can be passed into IrregularSet.<P>
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
    if (samples != null) {
      // consistency checks can be performed
      int dim = samples.length;
      int dim1 = dim+1;
      int ntris = tri.length;
      int nrs = samples[0].length;
      for (int i=1; i<dim; i++) {
        nrs = Math.min(nrs, samples[i].length);
      }

      // verify triangulation dimension
      for (int i=0; i<ntris; i++) {
        if (tri[i].length < dim1) {
          throw new VisADException("DelaunayCustom: triangulation dimension "
                                  +"and sampling dimension do not match!");
        }
      }

      // verify no illegal triangle vertices
      for (int i=0; i<ntris; i++) {
        for (int j=0; j<dim1; j++) {
          if (tri[i][j] < 0 || tri[i][j] >= nrs) throw new VisADException(
                      "DelaunayCustom: illegal tri vertex ("+i+", "+j+")");
        }
      }

      // verify that all pointris are in at least one triangle
      int[] nverts = new int[nrs];
      for (int i=0; i<nrs; i++) nverts[i] = 0;
      for (int i=0; i<ntris; i++) {
        for (int j=0; j<dim1; j++) nverts[tri[i][j]]++;
      }
      for (int i=0; i<nrs; i++) {
        if (nverts[i] == 0) throw new VisADException("DelaunayCustom: "
                      +"sample #"+i+" is not contained in triangulation!");
      }

      // DO THIS: need a triangle consistency check here (overlapping tris)

      // copy data into Delaunay arrays
      if (copy) {
        Tri = new int[ntris][dim1];
        for (int i=0; i<ntris; i++) System.arraycopy(tri[i], 0,
                                                     Tri[i], 0, dim1);
        if (vertices != null) {
          Vertices = new int[nrs][];
          for (int i=0; i<nrs; i++) {
            Vertices[i] = new int[vertices[i].length];
            System.arraycopy(vertices[i], 0,
                             Vertices[i], 0, vertices[i].length);
          }
        }
        if (walk != null) {
          Walk = new int[ntris][dim1];
          for (int i=0; i<ntris; i++) System.arraycopy(walk[i], 0,
                                                       Walk[i], 0, dim1);
        }
        if (edges != null) {
          Edges = new int[ntris][3*dim-1];
          for (int i=0; i<ntris; i++) System.arraycopy(edges[i], 0,
                                                       Edges[i], 0, 3*dim-1);
        }
      }
    }
    else {
      // no samples array was passed in;  just copy data to a new instance
      if (copy) {
        Tri = new int[tri.length][];
        for (int i=0; i<tri.length; i++) {
          Tri[i] = new int[tri[i].length];
          System.arraycopy(tri[i], 0, Tri[i], 0, tri[i].length);
        }
        Vertices = new int[vertices.length][];
        for (int i=0; i<vertices.length; i++) {
          Vertices[i] = new int[vertices[i].length];
          System.arraycopy(vertices[i], 0,
                           Vertices[i], 0, vertices[i].length);
        }
        Walk = new int[walk.length][];
        for (int i=0; i<walk.length; i++) {
          Walk[i] = new int[walk[i].length];
          System.arraycopy(walk[i], 0, Walk[i], 0, walk[i].length);
        }
        Edges = new int[edges.length][];
        for (int i=0; i<edges.length; i++) {
          Edges[i] = new int[edges[i].length];
          System.arraycopy(edges[i], 0, Edges[i], 0, edges[i].length);
        }
      }
    }
    if (!copy) {
      Tri = tri;
      Vertices = vertices;
      Walk = walk;
      Edges = edges;
    }
    NumEdges = num_edges;

    // call more generic method for constructing any remaining null arrays
    super.finish_triang(samples);
  }

}

