
//
// Irregular3DSet.java
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

/**
   Irregular3DSet represents a finite set of samples of R^3.<P>
*/
public class Irregular3DSet extends IrregularSet {

  double LowX, HiX, LowY, HiY, LowZ, HiZ;
  Delaunay Delan;

  public Irregular3DSet(MathType type, double[][] samples)
                      throws VisADException {
    this(type, samples, null, null, null);
  }

  public Irregular3DSet(MathType type, double[][] samples,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    super(type, samples,
          coord_sys, units, errors);
    Delan = new Delaunay(Samples);
    LowX = Low[0];
    HiX = Hi[0];
    LowY = Low[1];
    HiY = Hi[1];
    LowZ = Low[2];
    HiZ = Hi[2];
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public double[][] indexToValue(int[] index) throws VisADException {
    double[][] value = new double[3][index.length];
    for (int i=0; i<index.length; i++)
      if ( (index[i] >= 0) && (index[i] < Samples[0].length) ) {
        value[0][i] = Samples[0][index[i]];
        value[1][i] = Samples[1][index[i]];
        value[2][i] = Samples[2][index[i]];
      }
      else
        value[0][i] = value[1][i] = value[2][i] = Double.NaN;
    return value;
  }

  /* a private method used by valueToIndex and valueToInterp,
     valueToTri returns an array of containing triangles given
     an array of points in R^DomainDimension */
  private int[] valueToTri(double[][] value) throws VisADException {
    // avoid ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(value[0].length, value[1].length);
    length = Math.min(length, value[2].length);
    int[] tri = new int[length];
    int curtri = 0;
    for (int i=0; i<length; i++) {
      // Return -1 if iteration loop fails
      tri[i] = -1;
      boolean foundit = false;
      if (curtri < 0) curtri = 0;
      for (int itnum=0; (itnum<Delan.Tri.length) && !foundit; itnum++) {
        // define data
        int t0 = Delan.Tri[curtri][0];
        int t1 = Delan.Tri[curtri][1];
        int t2 = Delan.Tri[curtri][2];
        int t3 = Delan.Tri[curtri][3];
        double Ax = Samples[0][t0];
        double Ay = Samples[1][t0];
        double Az = Samples[2][t0];
        double Bx = Samples[0][t1];
        double By = Samples[1][t1];
        double Bz = Samples[2][t1];
        double Cx = Samples[0][t2];
        double Cy = Samples[1][t2];
        double Cz = Samples[2][t2];
        double Dx = Samples[0][t3];
        double Dy = Samples[1][t3];
        double Dz = Samples[2][t3];
        double Px = value[0][i];
        double Py = value[1][i];
        double Pz = value[2][i];

        // test whether point is contained in current triangle
        double tval1 = ( (By-Ay)*(Cz-Bz)-(Bz-Az)*(Cy-By) )*(Px-Ax)
                     + ( (Bz-Az)*(Cx-Bx)-(Bx-Ax)*(Cz-Bz) )*(Py-Ay)
                     + ( (Bx-Ax)*(Cy-By)-(By-Ay)*(Cx-Bx) )*(Pz-Az);
        double tval2 = ( (Cy-By)*(Dz-Cz)-(Cz-Bz)*(Dy-Cy) )*(Px-Bx)
                     + ( (Cz-Bz)*(Dx-Cx)-(Cx-Bx)*(Dz-Cz) )*(Py-By)
                     + ( (Cx-Bx)*(Dy-Cy)-(Cy-By)*(Dx-Cx) )*(Pz-Bz);
        double tval3 = ( (Dy-Cy)*(Az-Dz)-(Dz-Cz)*(Ay-Dy) )*(Px-Cx)
                     + ( (Dz-Cz)*(Ax-Dx)-(Dx-Cx)*(Az-Dz) )*(Py-Cy)
                     + ( (Dx-Cx)*(Ay-Dy)-(Dy-Cy)*(Ax-Dx) )*(Pz-Cz);
        double tval4 = ( (Ay-Dy)*(Bz-Az)-(Az-Dz)*(By-Ay) )*(Px-Dx)
                     + ( (Az-Dz)*(Bx-Ax)-(Ax-Dx)*(Bz-Az) )*(Py-Dy)
                     + ( (Ax-Dx)*(By-Ay)-(Ay-Dy)*(Bx-Ax) )*(Pz-Dz);
        boolean test1 = (tval1 == 0) || ( (tval1 > 0) == (
                      ( (By-Ay)*(Cz-Bz)-(Bz-Az)*(Cy-By) )*(Dx-Ax)
                    + ( (Bz-Az)*(Cx-Bx)-(Bx-Ax)*(Cz-Bz) )*(Dy-Ay)
                    + ( (Bx-Ax)*(Cy-By)-(By-Ay)*(Cx-Bx) )*(Dz-Az) > 0) );
        boolean test2 = (tval2 == 0) || ( (tval2 > 0) == (
                      ( (Cy-By)*(Dz-Cz)-(Cz-Bz)*(Dy-Cy) )*(Ax-Bx)
                    + ( (Cz-Bz)*(Dx-Cx)-(Cx-Bx)*(Dz-Cz) )*(Ay-By)
                    + ( (Cx-Bx)*(Dy-Cy)-(Cy-By)*(Dx-Cx) )*(Az-Bz) > 0) );
        boolean test3 = (tval3 == 0) || ( (tval3 > 0) == (
                      ( (Dy-Cy)*(Az-Dz)-(Dz-Cz)*(Ay-Dy) )*(Bx-Cx)
                    + ( (Dz-Cz)*(Ax-Dx)-(Dx-Cx)*(Az-Dz) )*(By-Cy)
                    + ( (Dx-Cx)*(Ay-Dy)-(Dy-Cy)*(Ax-Dx) )*(Bz-Cz) > 0) );
        boolean test4 = (tval4 == 0) || ( (tval4 > 0) == (
                      ( (Ay-Dy)*(Bz-Az)-(Az-Dz)*(By-Ay) )*(Cx-Dx)
                    + ( (Az-Dz)*(Bx-Ax)-(Ax-Dx)*(Bz-Az) )*(Cy-Dy)
                    + ( (Ax-Dx)*(By-Ay)-(Ay-Dy)*(Bx-Ax) )*(Cz-Dz) > 0) );

        // figure out which triangle to go to next
        if (!test1) curtri = Delan.Walk[curtri][0];
        else if (!test2) curtri = Delan.Walk[curtri][1];
        else if (!test3) curtri = Delan.Walk[curtri][2];
        else if (!test4) curtri = Delan.Walk[curtri][3];
        else foundit = true;

        // Return -1 if outside of the convex hull
        if (curtri < 0) foundit = true;
        if (foundit) tri[i] = curtri;
      }
    }
    return tri;
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(double[][] value) throws VisADException {
    if (value.length < DomainDimension) {
      throw new SetException("Irregular3DSet.valueToIndex: bad dimension");
    }
    int[] tri = valueToTri(value);
    int[] index = new int[tri.length];
    for (int i=0; i<tri.length; i++) {
      if (tri[i] < 0) {
        index[i] = -1;
      }
      else {
        // current values
        double x = value[0][i];
        double y = value[1][i];
        double z = value[2][i];

        // triangle indices
        int t = tri[i];
        int t0 = Delan.Tri[t][0];
        int t1 = Delan.Tri[t][1];
        int t2 = Delan.Tri[t][2];
        int t3 = Delan.Tri[t][3];

        // partial distances
        double D00 = Samples[0][t0] - x;
        double D01 = Samples[1][t0] - y;
        double D02 = Samples[2][t0] - z;
        double D10 = Samples[0][t1] - x;
        double D11 = Samples[1][t1] - y;
        double D12 = Samples[2][t1] - z;
        double D20 = Samples[0][t2] - x;
        double D21 = Samples[1][t2] - y;
        double D22 = Samples[2][t2] - z;
        double D30 = Samples[0][t3] - x;
        double D31 = Samples[1][t3] - y;
        double D32 = Samples[2][t3] - z;

        // distances squared
        double Dsq0 = D00*D00 + D01*D01 + D02*D02;
        double Dsq1 = D10*D10 + D11*D11 + D12*D12;
        double Dsq2 = D20*D20 + D21*D21 + D22*D22;
        double Dsq3 = D30*D30 + D31*D31 + D32*D32;

        // find the minimum distance
        double min = Math.min(Dsq0, Dsq1);
        min = Math.min(min, Dsq2);
        min = Math.min(min, Dsq3);
        if (min == Dsq0) index[i] = t0;
        else if (min == Dsq1) index[i] = t1;
        else if (min == Dsq2) index[i] = t2;
        else index[i] = t3;
      }
    }
    return index;
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if no interpolation is possible */
  public void valueToInterp(double[][] value, int[][] indices,
                            double[][] weights) throws VisADException {
    if (value.length < DomainDimension) {
      throw new SetException("Irregular3DSet.valueToInterp: bad dimension");
    }
    int length = value[0].length; // number of values
    if ( (indices.length < length) || (weights.length < length) ) {
      throw new SetException("Irregular3DSet.valueToInterp:"
                            +" lengths don't match");
    }
    int[] tri = valueToTri(value);
    for (int i=0; i<tri.length; i++) {
      if (tri[i] < 0) {
        indices[i] = null;
        weights[i] = null;
      }
      else {
        // indices and weights sub-arrays
        int[] ival = new int[4];
        double[] wval = new double[4];
        // current values
        double x = value[0][i];
        double y = value[1][i];
        double z = value[2][i];

        // triangle indices
        int t = tri[i];
        int t0 = Delan.Tri[t][0];
        int t1 = Delan.Tri[t][1];
        int t2 = Delan.Tri[t][2];
        int t3 = Delan.Tri[t][3];
        ival[0] = t0;
        ival[1] = t1;
        ival[2] = t2;
        ival[3] = t3;

        // triangle vertices
        double x0 = Samples[0][t0];
        double y0 = Samples[1][t0];
        double z0 = Samples[2][t0];
        double x1 = Samples[0][t1];
        double y1 = Samples[1][t1];
        double z1 = Samples[2][t1];
        double x2 = Samples[0][t2];
        double y2 = Samples[1][t2];
        double z2 = Samples[2][t2];
        double x3 = Samples[0][t3];
        double y3 = Samples[1][t3];
        double z3 = Samples[2][t3];

        // perpendicular lines
        double C0x = (y3-y1)*(z2-z1) - (z3-z1)*(y2-y1);
        double C0y = (z3-z1)*(x2-x1) - (x3-x1)*(z2-z1);
        double C0z = (x3-x1)*(y2-y1) - (y3-y1)*(x2-x1);
        double C1x = (y3-y0)*(z2-z0) - (z3-z0)*(y2-y0);
        double C1y = (z3-z0)*(x2-x0) - (x3-x0)*(z2-z0);
        double C1z = (x3-x0)*(y2-y0) - (y3-y0)*(x2-x0);
        double C2x = (y3-y0)*(z1-z0) - (z3-z0)*(y1-y0);
        double C2y = (z3-z0)*(x1-x0) - (x3-x0)*(z1-z0);
        double C2z = (x3-x0)*(y1-y0) - (y3-y0)*(x1-x0);
        double C3x = (y2-y0)*(z1-z0) - (z2-z0)*(y1-y0);
        double C3y = (z2-z0)*(x1-x0) - (x2-x0)*(z1-z0);
        double C3z = (x2-x0)*(y1-y0) - (y2-y0)*(x1-x0);

        // weights
        wval[0] = ( ( (x - x1)*C0x) + ( (y - y1)*C0y) + ( (z - z1)*C0z) )
                / ( ((x0 - x1)*C0x) + ((y0 - y1)*C0y) + ((z0 - z1)*C0z) );
        wval[1] = ( ( (x - x0)*C1x) + ( (y - y0)*C1y) + ( (z - z0)*C1z) )
                / ( ((x1 - x0)*C1x) + ((y1 - y0)*C1y) + ((z1 - z0)*C1z) );
        wval[2] = ( ( (x - x0)*C2x) + ( (y - y0)*C2y) + ( (z - z0)*C2z) )
                / ( ((x2 - x0)*C2x) + ((y2 - y0)*C2y) + ((z2 - z0)*C2z) );
        wval[3] = ( ( (x - x0)*C3x) + ( (y - y0)*C3y) + ( (z - z0)*C3z) )
                / ( ((x3 - x0)*C3x) + ((y3 - y0)*C3y) + ((z3 - z0)*C3z) );

        // fill in arrays
        indices[i] = ival;
        weights[i] = wval;
      }
    }
  }

  /** compute an Isosurface through the Irregular3DSet
      given an array of fieldValues at each sample and
      an isolevel at which to form the surface */
  public void makeIsosurface(double isolevel, double[] fieldValues,
                             double[][] sliceValues, int[][][] polyToVert,
                             int[][][] vertToPoly) throws VisADException {
    if (fieldValues.length < Length) {
      throw new SetException("Irregular3DSet.makeIsosurface: "
                            +"lengths don't match");
    }
    if (Double.isNaN(isolevel)) {
      throw new SetException("Irregular3DSet.makeIsosurface: "
                            +"isolevel cannot be missing");
    }
    if (sliceValues.length < 3 || polyToVert.length < 1
                               || vertToPoly.length < 1) {
      throw new SetException("Irregular3DSet.makeIsosurface: return value"
                            +" arrays not correctly initialized");
    }

    int trilength = Delan.Tri.length;

    // temporary storage of polyToVert structure
    int[][] polys = new int[trilength][4];

    // temporary storage of sliceValues structure
    double[][] tempslice = new double[3][4*trilength];

    // global edges temporary storage array
    double[][] edgeInterp = new double[DomainDimension][Delan.NumEdges];
    for (int i=0; i<Delan.NumEdges; i++) edgeInterp[0][i] = Double.NaN;

    int numslices = 0;
    int numpolys = 0;
    for (int i=0; i<trilength; i++) {
      int v0 = Delan.Tri[i][0];
      int v1 = Delan.Tri[i][1];
      int v2 = Delan.Tri[i][2];
      int v3 = Delan.Tri[i][3];
      double f0 = fieldValues[v0];
      double f1 = fieldValues[v1];
      double f2 = fieldValues[v2];
      double f3 = fieldValues[v3];
      int e0, e1, e2, e3, e4, e5;

      // 8 possibilities
      switch ( ((f0 > isolevel) ? 1 : 0)
             + ((f1 > isolevel) ? 2 : 0)
             + ((f2 > isolevel) ? 4 : 0)
             + ((f3 > isolevel) ? 8 : 0) ) {
        case 0:
        case 15:             // plane does not intersect this tetrahedron
          break;

        case 1:
        case 14:             // plane slices a triangle
          // define edge values needed
          e0 = Delan.Edges[i][0];
          e1 = Delan.Edges[i][1];
          e2 = Delan.Edges[i][2];

          // fill in edge interpolation values if they haven't been found
          if (Double.isNaN(edgeInterp[0][e0])) {
            double a = (isolevel - f1)/(f0 - f1);
            if (a < 0) a = -a;
            edgeInterp[0][e0] = a*Samples[0][v0] + (1-a)*Samples[0][v1];
            edgeInterp[1][e0] = a*Samples[1][v0] + (1-a)*Samples[1][v1];
            edgeInterp[2][e0] = a*Samples[2][v0] + (1-a)*Samples[2][v1];
          }
          if (Double.isNaN(edgeInterp[0][e1])) {
            double a = (isolevel - f2)/(f0 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e1] = a*Samples[0][v0] + (1-a)*Samples[0][v2];
            edgeInterp[1][e1] = a*Samples[1][v0] + (1-a)*Samples[1][v2];
            edgeInterp[2][e1] = a*Samples[2][v0] + (1-a)*Samples[2][v2];
          }
          if (Double.isNaN(edgeInterp[0][e2])) {
            double a = (isolevel - f3)/(f0 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e2] = a*Samples[0][v0] + (1-a)*Samples[0][v3];
            edgeInterp[1][e2] = a*Samples[1][v0] + (1-a)*Samples[1][v3];
            edgeInterp[2][e2] = a*Samples[2][v0] + (1-a)*Samples[2][v3];
          }

          // fill in tempslice array
          for (int j=0; j<3; j++) {
            tempslice[j][numslices] = edgeInterp[j][e0];
            tempslice[j][numslices+1] = edgeInterp[j][e1];
            tempslice[j][numslices+2] = edgeInterp[j][e2];
          }

          // fill in the polys and vertToPoly arrays
          polys[numpolys][0] = numslices;
          polys[numpolys][1] = numslices+1;
          polys[numpolys][2] = numslices+2;
          polys[numpolys][3] = -1;

          // on to the next tetrahedron
          numslices += 3;
          numpolys++;
          break;

        case 2:
        case 13:             // plane slices a triangle
          // define edge values needed
          e0 = Delan.Edges[i][0];
          e3 = Delan.Edges[i][3];
          e4 = Delan.Edges[i][4];

          // fill in edge interpolation values if they haven't been found
          if (Double.isNaN(edgeInterp[0][e0])) {
            double a = (isolevel - f1)/(f0 - f1);
            if (a < 0) a = -a;
            edgeInterp[0][e0] = a*Samples[0][v0] + (1-a)*Samples[0][v1];
            edgeInterp[1][e0] = a*Samples[1][v0] + (1-a)*Samples[1][v1];
            edgeInterp[2][e0] = a*Samples[2][v0] + (1-a)*Samples[2][v1];
          }
          if (Double.isNaN(edgeInterp[0][e3])) {
            double a = (isolevel - f2)/(f1 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e3] = a*Samples[0][v1] + (1-a)*Samples[0][v2];
            edgeInterp[1][e3] = a*Samples[1][v1] + (1-a)*Samples[1][v2];
            edgeInterp[2][e3] = a*Samples[2][v1] + (1-a)*Samples[2][v2];
          }
          if (Double.isNaN(edgeInterp[0][e4])) {
            double a = (isolevel - f3)/(f1 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e4] = a*Samples[0][v1] + (1-a)*Samples[0][v3];
            edgeInterp[1][e4] = a*Samples[1][v1] + (1-a)*Samples[1][v3];
            edgeInterp[2][e4] = a*Samples[2][v1] + (1-a)*Samples[2][v3];
          }

          // fill in tempslice array
          for (int j=0; j<3; j++) {
            tempslice[j][numslices] = edgeInterp[j][e0];
            tempslice[j][numslices+1] = edgeInterp[j][e3];
            tempslice[j][numslices+2] = edgeInterp[j][e4];
          }

          // fill in the polys array
          polys[numpolys][0] = numslices;
          polys[numpolys][1] = numslices+1;
          polys[numpolys][2] = numslices+2;
          polys[numpolys][3] = -1;

          // on to the next tetrahedron
          numslices += 3;
          numpolys++;
          break;

        case 3:
        case 12:             // plane slices a quadrilateral
          // define edge values needed
          e1 = Delan.Edges[i][1];
          e2 = Delan.Edges[i][2];
          e3 = Delan.Edges[i][3];
          e4 = Delan.Edges[i][4];

          // fill in edge interpolation values if they haven't been found
          if (Double.isNaN(edgeInterp[0][e1])) {
            double a = (isolevel - f2)/(f0 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e1] = a*Samples[0][v0] + (1-a)*Samples[0][v2];
            edgeInterp[1][e1] = a*Samples[1][v0] + (1-a)*Samples[1][v2];
            edgeInterp[2][e1] = a*Samples[2][v0] + (1-a)*Samples[2][v2];
          }
          if (Double.isNaN(edgeInterp[0][e2])) {
            double a = (isolevel - f3)/(f0 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e2] = a*Samples[0][v0] + (1-a)*Samples[0][v3];
            edgeInterp[1][e2] = a*Samples[1][v0] + (1-a)*Samples[1][v3];
            edgeInterp[2][e2] = a*Samples[2][v0] + (1-a)*Samples[2][v3];
          }
          if (Double.isNaN(edgeInterp[0][e3])) {
            double a = (isolevel - f2)/(f1 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e3] = a*Samples[0][v1] + (1-a)*Samples[0][v2];
            edgeInterp[1][e3] = a*Samples[1][v1] + (1-a)*Samples[1][v2];
            edgeInterp[2][e3] = a*Samples[2][v1] + (1-a)*Samples[2][v2];
          }
          if (Double.isNaN(edgeInterp[0][e4])) {
            double a = (isolevel - f3)/(f1 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e4] = a*Samples[0][v1] + (1-a)*Samples[0][v3];
            edgeInterp[1][e4] = a*Samples[1][v1] + (1-a)*Samples[1][v3];
            edgeInterp[2][e4] = a*Samples[2][v1] + (1-a)*Samples[2][v3];
          }

          // fill in tempslice array
          for (int j=0; j<3; j++) {
            tempslice[j][numslices] = edgeInterp[j][e1];
            tempslice[j][numslices+1] = edgeInterp[j][e2];
            tempslice[j][numslices+2] = edgeInterp[j][e3];
            tempslice[j][numslices+3] = edgeInterp[j][e4];
          }

          // fill in the polys array
          polys[numpolys][0] = numslices;
          polys[numpolys][1] = numslices+1;
          polys[numpolys][2] = numslices+2;
          polys[numpolys][3] = numslices+3;

          // on to the next tetrahedron
          numslices += 4;
          numpolys++;
          break;

        case 4:
        case 11:             // plane slices a triangle
          // define edge values needed
          e1 = Delan.Edges[i][1];
          e3 = Delan.Edges[i][3];
          e5 = Delan.Edges[i][5];

          // fill in edge interpolation values if they haven't been found
          if (Double.isNaN(edgeInterp[0][e1])) {
            double a = (isolevel - f2)/(f0 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e1] = a*Samples[0][v0] + (1-a)*Samples[0][v2];
            edgeInterp[1][e1] = a*Samples[1][v0] + (1-a)*Samples[1][v2];
            edgeInterp[2][e1] = a*Samples[2][v0] + (1-a)*Samples[2][v2];
          }
          if (Double.isNaN(edgeInterp[0][e3])) {
            double a = (isolevel - f2)/(f1 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e3] = a*Samples[0][v1] + (1-a)*Samples[0][v2];
            edgeInterp[1][e3] = a*Samples[1][v1] + (1-a)*Samples[1][v2];
            edgeInterp[2][e3] = a*Samples[2][v1] + (1-a)*Samples[2][v2];
          }
          if (Double.isNaN(edgeInterp[0][e5])) {
            double a = (isolevel - f3)/(f2 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e5] = a*Samples[0][v2] + (1-a)*Samples[0][v3];
            edgeInterp[1][e5] = a*Samples[1][v2] + (1-a)*Samples[1][v3];
            edgeInterp[2][e5] = a*Samples[2][v2] + (1-a)*Samples[2][v3];
          }

          // fill in tempslice array
          for (int j=0; j<3; j++) {
            tempslice[j][numslices] = edgeInterp[j][e1];
            tempslice[j][numslices+1] = edgeInterp[j][e3];
            tempslice[j][numslices+2] = edgeInterp[j][e5];
          }

          // fill in the polys array
          polys[numpolys][0] = numslices;
          polys[numpolys][1] = numslices+1;
          polys[numpolys][2] = numslices+2;

          // on to the next tetrahedron
          numslices += 3;
          numpolys++;
          break;

        case 5:
        case 10:             // plane slices a quadrilateral
          // define edge values needed
          e0 = Delan.Edges[i][0];
          e2 = Delan.Edges[i][2];
          e3 = Delan.Edges[i][3];
          e5 = Delan.Edges[i][5];

          // fill in edge interpolation values if they haven't been found
          if (Double.isNaN(edgeInterp[0][e0])) {
            double a = (isolevel - f1)/(f0 - f1);
            if (a < 0) a = -a;
            edgeInterp[0][e0] = a*Samples[0][v0] + (1-a)*Samples[0][v1];
            edgeInterp[1][e0] = a*Samples[1][v0] + (1-a)*Samples[1][v1];
            edgeInterp[2][e0] = a*Samples[2][v0] + (1-a)*Samples[2][v1];
          }
          if (Double.isNaN(edgeInterp[0][e2])) {
            double a = (isolevel - f3)/(f0 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e2] = a*Samples[0][v0] + (1-a)*Samples[0][v3];
            edgeInterp[1][e2] = a*Samples[1][v0] + (1-a)*Samples[1][v3];
            edgeInterp[2][e2] = a*Samples[2][v0] + (1-a)*Samples[2][v3];
          }
          if (Double.isNaN(edgeInterp[0][e3])) {
            double a = (isolevel - f2)/(f1 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e3] = a*Samples[0][v1] + (1-a)*Samples[0][v2];
            edgeInterp[1][e3] = a*Samples[1][v1] + (1-a)*Samples[1][v2];
            edgeInterp[2][e3] = a*Samples[2][v1] + (1-a)*Samples[2][v2];
          }
          if (Double.isNaN(edgeInterp[0][e5])) {
            double a = (isolevel - f3)/(f2 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e5] = a*Samples[0][v2] + (1-a)*Samples[0][v3];
            edgeInterp[1][e5] = a*Samples[1][v2] + (1-a)*Samples[1][v3];
            edgeInterp[2][e5] = a*Samples[2][v2] + (1-a)*Samples[2][v3];
          }

          // fill in tempslice array
          for (int j=0; j<3; j++) {
            tempslice[j][numslices] = edgeInterp[j][e0];
            tempslice[j][numslices+1] = edgeInterp[j][e2];
            tempslice[j][numslices+2] = edgeInterp[j][e3];
            tempslice[j][numslices+3] = edgeInterp[j][e5];
          }

          // fill in the polys array
          polys[numpolys][0] = numslices;
          polys[numpolys][1] = numslices+1;
          polys[numpolys][2] = numslices+2;
          polys[numpolys][3] = numslices+3;

          // on to the next tetrahedron
          numslices += 4;
          numpolys++;
          break;

        case 6:
        case 9:              // plane slices a quadrilateral
          // define edge values needed
          e0 = Delan.Edges[i][0];
          e1 = Delan.Edges[i][1];
          e4 = Delan.Edges[i][4];
          e5 = Delan.Edges[i][5];

          // fill in edge interpolation values if they haven't been found
          if (Double.isNaN(edgeInterp[0][e0])) {
            double a = (isolevel - f1)/(f0 - f1);
            if (a < 0) a = -a;
            edgeInterp[0][e0] = a*Samples[0][v0] + (1-a)*Samples[0][v1];
            edgeInterp[1][e0] = a*Samples[1][v0] + (1-a)*Samples[1][v1];
            edgeInterp[2][e0] = a*Samples[2][v0] + (1-a)*Samples[2][v1];
          }
          if (Double.isNaN(edgeInterp[0][e1])) {
            double a = (isolevel - f2)/(f0 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e1] = a*Samples[0][v0] + (1-a)*Samples[0][v2];
            edgeInterp[1][e1] = a*Samples[1][v0] + (1-a)*Samples[1][v2];
            edgeInterp[2][e1] = a*Samples[2][v0] + (1-a)*Samples[2][v2];
          }
          if (Double.isNaN(edgeInterp[0][e4])) {
            double a = (isolevel - f3)/(f1 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e4] = a*Samples[0][v1] + (1-a)*Samples[0][v3];
            edgeInterp[1][e4] = a*Samples[1][v1] + (1-a)*Samples[1][v3];
            edgeInterp[2][e4] = a*Samples[2][v1] + (1-a)*Samples[2][v3];
          }
          if (Double.isNaN(edgeInterp[0][e5])) {
            double a = (isolevel - f3)/(f2 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e5] = a*Samples[0][v2] + (1-a)*Samples[0][v3];
            edgeInterp[1][e5] = a*Samples[1][v2] + (1-a)*Samples[1][v3];
            edgeInterp[2][e5] = a*Samples[2][v2] + (1-a)*Samples[2][v3];
          }

          // fill in tempslice array
          for (int j=0; j<3; j++) {
            tempslice[j][numslices] = edgeInterp[j][e0];
            tempslice[j][numslices+1] = edgeInterp[j][e1];
            tempslice[j][numslices+2] = edgeInterp[j][e4];
            tempslice[j][numslices+3] = edgeInterp[j][e5];
          }

          // fill in the polys array
          polys[numpolys][0] = numslices;
          polys[numpolys][1] = numslices+1;
          polys[numpolys][2] = numslices+2;
          polys[numpolys][3] = numslices+3;

          // on to the next tetrahedron
          numslices += 4;
          numpolys++;
          break;

        case 7:
        case 8:              // plane slices a triangle
          // interpolate between 3:0, 3:1, 3:2 for tri edges, same as case 8
          // define edge values needed
          e2 = Delan.Edges[i][2];
          e4 = Delan.Edges[i][4];
          e5 = Delan.Edges[i][5];

          // fill in edge interpolation values if they haven't been found
          if (Double.isNaN(edgeInterp[0][e2])) {
            double a = (isolevel - f3)/(f0 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e2] = a*Samples[0][v0] + (1-a)*Samples[0][v3];
            edgeInterp[1][e2] = a*Samples[1][v0] + (1-a)*Samples[1][v3];
            edgeInterp[2][e2] = a*Samples[2][v0] + (1-a)*Samples[2][v3];
          }
          if (Double.isNaN(edgeInterp[0][e4])) {
            double a = (isolevel - f3)/(f1 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e4] = a*Samples[0][v1] + (1-a)*Samples[0][v3];
            edgeInterp[1][e4] = a*Samples[1][v1] + (1-a)*Samples[1][v3];
            edgeInterp[2][e4] = a*Samples[2][v1] + (1-a)*Samples[2][v3];
          }
          if (Double.isNaN(edgeInterp[0][e5])) {
            double a = (isolevel - f3)/(f2 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e5] = a*Samples[0][v2] + (1-a)*Samples[0][v3];
            edgeInterp[1][e5] = a*Samples[1][v2] + (1-a)*Samples[1][v3];
            edgeInterp[2][e5] = a*Samples[2][v2] + (1-a)*Samples[2][v3];
          }

          // fill in tempslice array
          for (int j=0; j<3; j++) {
            tempslice[j][numslices] = edgeInterp[j][e2];
            tempslice[j][numslices+1] = edgeInterp[j][e4];
            tempslice[j][numslices+2] = edgeInterp[j][e5];
          }

          // fill in the polys array
          polys[numpolys][0] = numslices;
          polys[numpolys][1] = numslices+1;
          polys[numpolys][2] = numslices+2;

          // on to the next tetrahedron
          numslices += 3;
          numpolys++;
          break;
      }
    }

    // build nverts helper array
    int[] nverts = new int[numslices];
    for (int i=0; i<numslices; i++) nverts[i] = 0;
    for (int i=0; i<trilength; i++) {
      nverts[polys[i][0]]++;
      nverts[polys[i][1]]++;
      nverts[polys[i][2]]++;
      int temp = polys[i][3];
      if (temp != -1) nverts[temp]++;
    }

    // initialize vertToPoly array
    vertToPoly[0] = new int[numslices][];
    for (int i=0; i<numslices; i++) {
      vertToPoly[0][i] = new int[nverts[i]];
    }

    // fill in vertToPoly array
    for (int i=0; i<numslices; i++) nverts[i] = 0;
    for (int i=0; i<trilength; i++) {
      int a = polys[i][0];
      int b = polys[i][1];
      int c = polys[i][2];
      int d = polys[i][3];
      vertToPoly[0][a][nverts[a]++] = i;
      vertToPoly[0][b][nverts[b]++] = i;
      vertToPoly[0][c][nverts[c]++] = i;
      if (d != -1) vertToPoly[0][d][nverts[d]++] = i;
    }

    // copy tempslice array into sliceValues array
    sliceValues[0] = new double[numslices];
    sliceValues[1] = new double[numslices];
    sliceValues[2] = new double[numslices];
    System.arraycopy(tempslice[0], 0, sliceValues[0], 0, numslices);
    System.arraycopy(tempslice[1], 0, sliceValues[1], 0, numslices);
    System.arraycopy(tempslice[2], 0, sliceValues[2], 0, numslices);

    // copy polys array into polyToVert array
    polyToVert[0] = new int[numpolys][4];
    System.arraycopy(polys, 0, polyToVert[0], 0, numpolys);

    // fill in vertToPoly array
    
  }

  public Object clone() {
    try {
      return new Irregular3DSet(Type, Samples,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Irregular3DSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Irregular3DSet(type, Samples,
                            DomainCoordinateSystem, SetUnits, SetErrors);
  }

  /* run 'java visad.Irregular3DSet' to test the Irregular3DSet class */
  public static void main(String[] argv) throws VisADException {
    double[][] samp = { {179, 232, 183, 244, 106, 344, 166, 304, 286},
                        { 86, 231, 152, 123, 183, 153, 308, 325,  89},
                        {121, 301, 346, 352, 123, 125, 187, 101, 142} };
    RealType test1 = new RealType("x", null, null);
    RealType test2 = new RealType("y", null, null);
    RealType test3 = new RealType("z", null, null);
    RealType[] t_array = {test1, test2, test3};
    RealTupleType t_tuple = new RealTupleType(t_array);
    Irregular3DSet iSet3D = new Irregular3DSet(t_tuple, samp);

    // print out Samples information
    System.out.println("Samples:");
    for (int i=0; i<iSet3D.Samples[0].length; i++) {
      System.out.println("#"+i+":\t"+iSet3D.Samples[0][i]+", "
                                    +iSet3D.Samples[1][i]+", "
                                    +iSet3D.Samples[2][i]);
    }
    System.out.println(iSet3D.Delan.Tri.length
                     +" tetrahedrons in tetrahedralization.");
    

    // test valueToIndex function
    System.out.println("\nvalueToIndex test:");
    double[][] value = { {189, 221, 319, 215, 196},
                         {166, 161, 158, 139, 285},
                         {207, 300, 127, 287, 194} };
    int[] index = iSet3D.valueToIndex(value);
    for (int i=0; i<index.length; i++) {
      System.out.println(value[0][i]+", "+value[1][i]+", "
                        +value[2][i]+"\t--> #"+index[i]);
    }

    // test valueToInterp function
    System.out.println("\nvalueToInterp test:");
    int[][] indices = new int[value[0].length][];
    double[][] weights = new double[value[0].length][];
    iSet3D.valueToInterp(value, indices, weights);
    for (int i=0; i<value[0].length; i++) {
      System.out.println(value[0][i]+", "+value[1][i]+", "
                        +value[2][i]+"\t--> ["
                        +indices[i][0]+", "
                        +indices[i][1]+", "
                        +indices[i][2]+", "
                        +indices[i][3]+"]\tweight total: "
                       +(weights[i][0]+weights[i][1]
                        +weights[i][2]+weights[i][3]));
    }

    // test makeIsosurface function
    System.out.println("\nmakeIsosurface test:");
    double[] field = {100, 300, 320, 250, 80, 70, 135, 110, 105};
    double[][] slice = new double[3][];
    int[][][] polyvert = new int[1][][];
    int[][][] vertpoly = new int[1][][];
    iSet3D.makeIsosurface(288, field, slice, polyvert, vertpoly);
    for (int i=0; i<slice[0].length; i++) {
      for (int j=0; j<3; j++) {
        slice[j][i] = (double) Math.round(1000*slice[j][i]) / 1000;
      }
    }
    System.out.println("polygons:");
    for (int i=0; i<polyvert[0].length; i++) {
      System.out.print("#"+i+":");
      for (int j=0; j<4; j++) {
        if (polyvert[0][i][j] != -1) {
          if (j == 1) {
            if (polyvert[0][i][3] == -1) {
              System.out.print("(tri)");
            }
            else {
              System.out.print("(quad)");
            }
          }
          System.out.println("\t"+slice[0][polyvert[0][i][j]]
                            +", "+slice[1][polyvert[0][i][j]]
                            +", "+slice[2][polyvert[0][i][j]]);
        }
      }
    }
    System.out.println();
    for (int i=0; i<polyvert[0].length; i++) {
      int a = polyvert[0][i][0];
      int b = polyvert[0][i][1];
      int c = polyvert[0][i][2];
      int d = polyvert[0][i][3];
      boolean found = false;
      for (int j=0; j<vertpoly[0][a].length; j++) {
        if (vertpoly[0][a][j] == i) found = true;
      }
      if (!found) {
        System.out.println("vertToPoly array corrupted at triangle #"
                           +i+" vertex #0!");
      }
      found = false;
      for (int j=0; j<vertpoly[0][b].length; j++) {
        if (vertpoly[0][b][j] == i) found = true;
      }
      if (!found) {
        System.out.println("vertToPoly array corrupted at triangle #"
                           +i+" vertex #1!");
      }
      found = false;
      for (int j=0; j<vertpoly[0][c].length; j++) {
        if (vertpoly[0][c][j] == i) found = true;
      }
      if (!found) {
        System.out.println("vertToPoly array corrupted at triangle #"
                           +i+" vertex #2!");
      }
      found = false;
      if (d != -1) {
        for (int j=0; j<vertpoly[0][d].length; j++) {
          if (vertpoly[0][d][j] == i) found = true;
        }
        if (!found) {
          System.out.println("vertToPoly array corrupted at triangle #"
                             +i+" vertex #3!");
        }
      }
    }
  }

/* Here's the output:

iris 45% java visad.Irregular3DSet
Samples:
#0:     179.0, 86.0, 121.0
#1:     232.0, 231.0, 301.0
#2:     183.0, 152.0, 346.0
#3:     244.0, 123.0, 352.0
#4:     106.0, 183.0, 123.0
#5:     344.0, 153.0, 125.0
#6:     166.0, 308.0, 187.0
#7:     304.0, 325.0, 101.0
#8:     286.0, 89.0, 142.0
15 tetrahedrons in tetrahedralization.

valueToIndex test:
189.0, 166.0, 207.0     --> #0
221.0, 161.0, 300.0     --> #2
319.0, 158.0, 127.0     --> #5
215.0, 139.0, 287.0     --> #2
196.0, 285.0, 194.0     --> #6

valueToInterp test:
189.0, 166.0, 207.0     --> [0, 1, 2, 4]        weight total: 1.0
221.0, 161.0, 300.0     --> [1, 2, 3, 8]        weight total: 1.0
319.0, 158.0, 127.0     --> [4, 5, 6, 8]        weight total: 0.9999999999999999
215.0, 139.0, 287.0     --> [1, 2, 3, 8]        weight total: 1.0
196.0, 285.0, 194.0     --> [1, 5, 6, 7]        weight total: 1.0

makeIsosurface test:
polygons:
#0:     237.843, 226.93, 291.817
(tri)   227.2, 236.6, 292.709
        236.547, 236.937, 288.368
#1:     228.82, 222.3, 290.2
(quad)  182.418, 142.4, 313.273
        225.127, 228.382, 291.291
        172.733, 156.133, 316.267
. . .
#10:    237.843, 226.93, 291.817
(tri)   227.2, 236.6, 292.709
        235.323, 222.262, 291.215

iris 46%

*/

}

