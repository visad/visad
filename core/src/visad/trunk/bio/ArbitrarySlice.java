//
// ArbitrarySlice.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

import java.io.*;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;
import visad.java3d.DirectManipulationRendererJ3D;

/**
 * ArbitrarySlice is a plane selector that specifies
 * an arbitrary slice through a 3-D box.
 */
public class ArbitrarySlice extends PlaneSelector {

  // -- CONSTANTS --

  /** Error range when detecting colocated points. */
  protected static final double EPS = 1.0e-10;


  // -- CONSTRUCTOR --

  /** Constructs a arbitrary slice selector. */
  public ArbitrarySlice(DisplayImpl display) { super(display); }


  // -- API METHODS --

  /** Extracts a field using the current plane, at the given resolution. */
  public Field extractSlice(FieldImpl field, int resx, int resy,
    int x2, int y2) throws VisADException, RemoteException
  {
    if (lines == null) return null;

    // extract hull points from lines data object
    GriddedSet lineSet = (GriddedSet) lines.getDomainSet();
    float[][] samples = lineSet.getSamples(false);
    float[] x = samples[0];
    float[] y = samples[1];
    float[] z = samples[2];
    int numpts = x.length - 1;

    // find longest line segment
    double longest = 0;
    int index = -1;
    for (int i=0; i<numpts; i++) {
      float dx = x[i] - x[i + 1];
      float dy = y[i] - y[i + 1];
      float dz = z[i] - z[i + 1];
      double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (len > longest) {
        longest = len;
        index = i;
      }
    }
    int p1 = index;
    int p2 = (index + 1) % numpts;
    int p = 0;
    while (p == p1 || p == p2) p++;

    // compute first two corners
    float[] c1 = new float[3];
    float[] c2 = new float[3];
    float[] proj = new float[3];
    BioUtil.project(x, y, z,
      x[p1], y[p1], z[p1], x[p2], y[p2], z[p2], p, c1, c2, proj);

    // compute third corner
    float[] c3 = new float[3];
    float[] l = BioUtil.corner(proj, c1, new float[] {x[p], y[p], z[p]});
    BioUtil.project(x, y, z,
      c1[0], c1[1], c1[2], l[0], l[1], l[2], -1, c1, c3, proj);

    // compute fourth corner
    float[] c4 = BioUtil.corner(c1, c2, c3);

    // construct 3-D planar grid
    SetType type3 = (SetType) lineSet.getType();
    float[][] samp3 = {
      {c1[0], c2[0], c3[0], c4[0]},
      {c1[1], c2[1], c3[1], c4[1]},
      {c1[2], c2[2], c3[2], c4[2]}
    };
    Gridded3DSet box3 = new Gridded3DSet(type3, samp3,
      2, 2, null, null, null, false);
    int rx = resx - 1;
    int ry = resy - 1;
    int len = resx * resy;
    float[][] grid = new float[2][len];
    for (int j=0; j<resy; j++) {
      for (int i=0; i<resx; i++) {
        index = j * resx + i;
        grid[0][index] = (float) i / rx;
        grid[1][index] = (float) j / ry;
      }
    }
    Gridded3DSet set3 = new Gridded3DSet(type3, box3.gridToValue(grid),
      resx, resy, null, null, null, false);

    // extract 3-D slice data
    FieldImpl slice3 = (FieldImpl)
      field.resample(set3, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);

    // construct 2-D planar grid
    RealType[] rt = type3.getDomain().getRealComponents();
    RealTupleType type2 = new RealTupleType(new RealType[] {rt[0], rt[1]});
    float[][] samp2 = new float[2][resx * resy];
    for (int j=0; j<resy; j++) {
      for (int i=0; i<resx; i++) {
        index = resx * j + i;
        samp2[0][index] = (float) x2 * i / resx;
        samp2[1][index] = (float) y2 * j / resy;
      }
    }
    Gridded2DSet set2 = new Gridded2DSet(type2,
      samp2, resx, resy, null, null, null, false);

    // convert slice data to 2-D
    FunctionType ftype3 = (FunctionType) slice3.getType();
    FunctionType ftype2 = new FunctionType(type2, ftype3.getRange());
    FlatField slice2 = new FlatField(ftype2, set2);
    slice2.setSamples(slice3.getValues(false), false);

    return slice2;
  }


  // -- HELPER METHODS --

  /** Computes the appropriate plane from the current endpoints. */
  protected boolean computePlane(RealTuple[] tuple)
    throws VisADException, RemoteException
  {
    int len = tuple.length;
    int vcount = 0;
    float[][] samples = null;
    RealTupleType type = (RealTupleType) tuple[0].getType();

    // snap values to nearest box edge (must touch two of three axes)
    double[] x = new double[len];
    double[] y = new double[len];
    double[] z = new double[len];
    for (int i=0; i<len; i++) {
      double[] values = tuple[i].getValues();
      double vx = values[0];
      double vy = values[1];
      double vz = values[2];
      boolean cx1 = vx < x1;
      boolean cx2 = vx > x2;
      boolean cy1 = vy < y1;
      boolean cy2 = vy > y2;
      boolean cz1 = vz < z1;
      boolean cz2 = vz > z2;
      if (cx1) vx = x1;
      else if (cx2) vx = x2;
      if (cy1) vy = y1;
      else if (cy2) vy = y2;
      if (cz1) vz = z1;
      else if (cz2) vz = z2;
      boolean changed = cx1 || cx2 || cy1 || cy2 || cz1 || cz2;
      double rx = Math.abs(x2 - x1);
      double ry = Math.abs(y2 - y1);
      double rz = Math.abs(z2 - z1);
      double dx1 = Math.abs(vx - x1) / rx;
      double dx2 = Math.abs(vx - x2) / rx;
      double dy1 = Math.abs(vy - y1) / ry;
      double dy2 = Math.abs(vy - y2) / ry;
      double dz1 = Math.abs(vz - z1) / rz;
      double dz2 = Math.abs(vz - z2) / rz;
      boolean xm = dx1 == 0 || dx2 == 0;
      boolean ym = dy1 == 0 || dy2 == 0;
      boolean zm = dz1 == 0 || dz2 == 0;
      if (!xm && !ym || !xm && !zm || !ym && !zm) {
        boolean axisx = dx1 < dx2;
        boolean axisy = dy1 < dy2;
        boolean axisz = dz1 < dz2;
        double distx = axisx ? dx1 : dx2;
        double disty = axisy ? dy1 : dy2;
        double distz = axisz ? dz1 : dz2;
        boolean snapx = distx <= distz || distx <= disty;
        boolean snapy = disty <= distx || disty <= distz;
        boolean snapz = !snapx || !snapy;
        if (snapx) vx = axisx ? x1 : x2;
        if (snapy) vy = axisy ? y1 : y2;
        if (snapz) vz = axisz ? z1 : z2;
        changed = true;
      }
      if (changed) {
        setData(i, vx, vy, vz);
        return false;
      }
      x[i] = vx;
      y[i] = vy;
      z[i] = vz;
    }

    // solve plane equation for box edge intersections
    //
    // x = x0 + s*(x1 - x0) + t*(x2 - x0) = x0 + s*x10 + t*x20
    // y = y0 + s*(y1 - y0) + t*(y2 - y0) = y0 + s*y10 + t*y20
    // z = z0 + s*(z1 - z0) + t*(z2 - z0) = y0 + s*z10 + t*z20
    //
    // t = (x - x0 - s*x10) / x20
    // y = y0 + s*y10 + [(x - x0 - s*x10) / x20] * y20
    //   = y0 + s*y10 + y20 * (x - x0) / x20 - y20 * s * x10 / x20
    //   = y0 + y20 * (x - x0) / x20 + s * (y10 - y20 * x10 / x20)
    // s = [y - y0 - y20 * (x - x0) / x20] / (y10 - y20 * x10 / x20)
    //   = [x20 * (y - y0) - y20 * (x - x0)] / (x20 * y10 - y20 * x10)
    //
    double NaN = Double.NaN;
    double[][] p = { // this sequence ensures the hull is ordered properly
      {NaN, x1, x1, x1, x1, NaN, x2, NaN, x2, NaN, x2, x2},
      {y1, NaN, y1, NaN, y2, y2, y2, y2, NaN, y1, y1, NaN},
      {z1, z1, NaN, z2, NaN, z1, NaN, z2, z2, z2, NaN, z1}
    };
    boolean[] valid = new boolean[12];
    for (int i=0; i<12; i++) {
      double px0 = p[0][i] - x[0];
      double py0 = p[1][i] - y[0];
      double pz0 = p[2][i] - z[0];
      double x10 = x[1] - x[0];
      double y10 = y[1] - y[0];
      double z10 = z[1] - z[0];
      double x20 = x[2] - x[0];
      double y20 = y[2] - y[0];
      double z20 = z[2] - z[0];
      if (px0 != px0) {
        // solve for x
        double s = (y20 * pz0 - z20 * py0) / (y20 * z10 - z20 * y10);
        double t = y20 == 0 ?
          ((pz0 - s * z10) / z20) :
          ((py0 - s * y10) / y20);
        p[0][i] = x[0] + s * x10 + t * x20;
        valid[i] = p[0][i] >= x1 && p[0][i] <= x2;
      }
      else if (py0 != py0) {
        // solve for y
        double s = (z20 * px0 - x20 * pz0) / (z20 * x10 - x20 * z10);
        double t = z20 == 0 ?
        ((px0 - s * x10) / x20) :
        ((pz0 - s * z10) / z20);
        p[1][i] = y[0] + s * y10 + t * y20;
        valid[i] = p[1][i] >= y1 && p[1][i] <= y2;
      }
      else { // pz0 != pz0
        // solve for z
        double s = (x20 * py0 - y20 * px0) / (x20 * y10 - y20 * x10);
        double t = x20 == 0 ?
          ((py0 - s * y10) / y20) :
          ((px0 - s * x10) / x20);
        p[2][i] = z[0] + s * z10 + t * z20;
        valid[i] = p[2][i] >= z1 && p[2][i] <= z2;
      }
      // invalidate duplicate points
      if (valid[i]) {
        for (int j=0; j<i; j++) {
          if (!valid[j]) continue;
          if (Math.abs(p[0][i] - p[0][j]) < EPS &&
            Math.abs(p[1][i] - p[1][j]) < EPS &&
            Math.abs(p[2][i] - p[2][j]) < EPS)
          {
            valid[i] = false;
            break;
          }
        }
      }
      if (valid[i]) vcount++;
    }

    // analyze x, y, z for valid box edge intersections
    // there could be as few as 3 or as many as 6
    if (vcount > 0) {
      // extract valid box edge intersection points
      float[] ux = new float[vcount];
      float[] uy = new float[vcount];
      float[] uz = new float[vcount];
      for (int i=0, c=0; i<12; i++) {
        if (valid[i]) {
          ux[c] = (float) p[0][i];
          uy[c] = (float) p[1][i];
          uz[c] = (float) p[2][i];
          c++;
        }
      }
      // sort points in convex hull order
      boolean[] hull = new boolean[vcount];
      samples = new float[3][vcount + 1];
      samples[0][0] = ux[0];
      samples[1][0] = uy[0];
      samples[2][0] = uz[0];
      hull[0] = true;
      for (int i=1; i<vcount; i++) {
        double sx = samples[0][i - 1];
        double sy = samples[1][i - 1];
        double sz = samples[2][i - 1];
        boolean xok = sx == x1 || sx == x2;
        boolean yok = sy == y1 || sy == y2;
        boolean zok = sz == z1 || sz == z2;
        for (int j=1; j<vcount; j++) {
          if (hull[j]) continue;
          if (xok && sx == ux[j] ||
            yok && sy == uy[j] ||
            zok && sz == uz[j])
          {
            // found next point in hull
            samples[0][i] = ux[j];
            samples[1][i] = uy[j];
            samples[2][i] = uz[j];
            hull[j] = true;
            break;
          }
        }
      }
      samples[0][vcount] = samples[0][0];
      samples[1][vcount] = samples[1][0];
      samples[2][vcount] = samples[2][0];
      FunctionType lineType =
        new FunctionType(type, new RealTupleType(rtype, gtype, btype));
      Gridded3DSet lineSet =
        new Gridded3DSet(type, samples, vcount + 1, null, null, null, false);
      lines = new FlatField(lineType, lineSet);
      double[][] values = new double[3][vcount + 1];
      for (int i=0; i<=vcount; i++) {
        for (int j=0; j<3; j++) values[j][i] = lineValues[j][0];
      }
      lines.setSamples(values);
    }

    if (vcount == 3) {
      // planar slice is a triangle
      float[][] samps = new float[3][4];
      for (int i=0; i<3; i++) {
        samps[i][0] = samples[i][0];
        samps[i][1] = (samples[i][0] + samples[i][1]) / 2;
        samps[i][2] = samples[i][2];
        samps[i][3] = samples[i][1];
      }
      plane = new Gridded3DSet(type, samps, 2, 2, null, null, null, false);
    }
    else if (vcount == 4) {
      // planar slice is a quadrilateral
      float[][] samps = new float[3][4];
      for (int i=0; i<3; i++) {
        samps[i][0] = samples[i][0];
        samps[i][1] = samples[i][1];
        samps[i][2] = samples[i][3];
        samps[i][3] = samples[i][2];
      }
      plane = new Gridded3DSet(type, samps, 2, 2, null, null, null, false);
    }
    else if (vcount == 5) {
      // planar slice is a pentagon
      float[][] samps = new float[3][6];
      for (int i=0; i<3; i++) {
        samps[i][0] = samples[i][0];
        samps[i][1] = samples[i][1];
        samps[i][2] = samples[i][4];
        samps[i][3] = (samples[i][1] + samples[i][2]) / 2;
        samps[i][4] = samples[i][3];
        samps[i][5] = samples[i][2];
      }
      plane = new Gridded3DSet(type, samps, 2, 3, null, null, null, false);
    }
    else if (vcount == 6) {
      // planar slice is a hexagon
      float[][] samps = new float[3][6];
      for (int i=0; i<3; i++) {
        samps[i][0] = samples[i][0];
        samps[i][1] = samples[i][1];
        samps[i][2] = samples[i][5];
        samps[i][3] = samples[i][2];
        samps[i][4] = samples[i][4];
        samps[i][5] = samples[i][3];
      }
      plane = new Gridded3DSet(type, samps, 2, 3, null, null, null, false);
    }
    return true;
  }

}
