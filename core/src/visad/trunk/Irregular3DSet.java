
//
// Irregular3DSet.java
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
   Irregular3DSet represents a finite set of samples of R^3.<P>

   No Irregular3DSet with ManifoldDimension = 1.  Use
   Gridded3DSet with ManifoldDimension = 1 instead.<P>
*/
public class Irregular3DSet extends IrregularSet {

  private float LowX, HiX, LowY, HiY, LowZ, HiZ;

  /** shortcut constructor */
  public Irregular3DSet(MathType type, float[][] samples)
                      throws VisADException {
    this(type, samples, null, null, null, null, true);
  }

  /** complete constructor */
  public Irregular3DSet(MathType type, float[][] samples,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors, Delaunay delan)
                                         throws VisADException {
    this(type, samples, coord_sys, units, errors, delan, true);
  }

  Irregular3DSet(MathType type, float[][] samples,
                 CoordinateSystem coord_sys, Unit[] units,
                 ErrorEstimate[] errors, Delaunay delan,
		 boolean copy) throws VisADException {
    /* ManifoldDimension might not be equal to samples.length
       if a 2D triangulation has been specified */
    super(type, samples, (delan == null) ? samples.length
                                         : delan.Tri[0].length-1,
          coord_sys, units, errors, delan, copy);
/* CTR: 1-12-98
    if (samples.length != 3) {
      throw new SetException("Irregular3DSet: ManifoldDimension " +
                             "must be 3 for this constructor");
    }
*/
    LowX = Low[0];
    HiX = Hi[0];
    LowY = Low[1];
    HiY = Hi[1];
    LowZ = Low[2];
    HiZ = Hi[2];
    oldToNew = null;
    newToOld = null;
  }

  /** construct Irregular3DSet using Delaunay from existing 
      IrregularSet (must have ManifoldDimension = 2 or 3) */
/* CTR: 1-12-98
  public Irregular3DSet(MathType type, float[][] samples,
                 IrregularSet delaunay_set) throws VisADException {
    this(type, samples, delaunay_set, null, null, null, true);
  }
*/
 
  /** construct Irregular3DSet using Delaunay from existing
      IrregularSet (must have ManifoldDimension = 2 or 3) */
/* CTR: 1-12-98
  public Irregular3DSet(MathType type, float[][] samples,
                        IrregularSet delaunay_set,
                        CoordinateSystem coord_sys, Unit[] units,
                        ErrorEstimate[] errors) throws VisADException {
    this(type, samples, delaunay_set, coord_sys, units, errors, true);
  }

  Irregular3DSet(MathType type, float[][] samples,
                 IrregularSet delaunay_set,
                 CoordinateSystem coord_sys, Unit[] units,
                 ErrorEstimate[] errors, boolean copy)
                 throws VisADException {
    super(type, samples, delaunay_set.getManifoldDimension(),
          coord_sys, units, errors, copy);
    int dim = delaunay_set.getManifoldDimension();
    if (dim != 2 && dim != 3) {
      throw new SetException("Irregular3DSet: delaunay_set ManifoldDimension " +
                             "must be 2 or 3");
    }
    if (Length != delaunay_set.Length) {
      throw new SetException("Irregular3DSet: delaunay_set length not match");
    }
    Delan = delaunay_set.Delan;
    LowX = Low[0];
    HiX = Hi[0];
    LowY = Low[1];
    HiY = Hi[1];
    LowZ = Low[2];
    HiZ = Hi[2];
    oldToNew = null;
    newToOld = null;
  }
*/

  /** construct Irregular3DSet using sort from existing
      Irregular1DSet */
  public Irregular3DSet(MathType type, float[][] samples,
               int[] new2old, int[] old2new) throws VisADException {
    this(type, samples, new2old, old2new, null, null, null, true);
  }
 
  /** construct Irregular3DSet using sort from existing
      Irregular1DSet */
  public Irregular3DSet(MathType type, float[][] samples,
                        int[] new2old, int[] old2new,
                        CoordinateSystem coord_sys, Unit[] units,
                        ErrorEstimate[] errors) throws VisADException {
    this(type, samples, new2old, old2new, coord_sys, units, errors, true);
  }

  Irregular3DSet(MathType type, float[][] samples,
                 int[] new2old, int[] old2new,
                 CoordinateSystem coord_sys, Unit[] units,
                 ErrorEstimate[] errors, boolean copy)
                 throws VisADException {
    super(type, samples, 1, coord_sys, units, errors, null, copy);
    if (Length != new2old.length || Length != old2new.length) {
      throw new SetException("Irregular3DSet: sort length not match");
    }
    newToOld = new int[Length];
    oldToNew = new int[Length];
    System.arraycopy(new2old, 0, newToOld, 0, Length);
    System.arraycopy(old2new, 0, oldToNew, 0, Length);
    LowX = Low[0];
    HiX = Hi[0];
    LowY = Low[1];
    HiY = Hi[1];
    LowZ = Low[2];
    HiZ = Hi[2];
    Delan = null;
  }

  public Set makeSpatial(SetType type, float[][] samples) throws VisADException {
    if (samples.length == 3) {
      if (ManifoldDimension == 1) {
        return new Irregular3DSet(type, samples, newToOld, oldToNew,
                                  null, null, null, false);
      }
      else {
        return new Irregular3DSet(type, samples, null, null, null,
	                          Delan, false);
      }
    }
    else {
      throw new SetException("Irregular3DSet.makeSpatial: bad samples length");
    }
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    float[][] value = new float[3][index.length];
    for (int i=0; i<index.length; i++) {
      if ( (index[i] >= 0) && (index[i] < Length) ) {
        value[0][i] = Samples[0][index[i]];
        value[1][i] = Samples[1][index[i]];
        value[2][i] = Samples[2][index[i]];
      }
      else {
        value[0][i] = value[1][i] = value[2][i] = Float.NaN;
      }
    }
    return value;
  }

  /* a private method used by valueToIndex and valueToInterp,
     valueToTri returns an array of containing triangles given
     an array of points in R^DomainDimension */
  private int[] valueToTri(float[][] value) throws VisADException {
    if (ManifoldDimension != 3) {
      throw new SetException("Irregular3DSet.valueToTri: " +
                             "ManifoldDimension must be 3");
    }
    int length = value[0].length;
    if (length != value[1].length || length != value[2].length) {
      throw new SetException("Irregular3DSet.valueToTri: lengths " +
                             "don't match");
    }
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
        float Ax = Samples[0][t0];
        float Ay = Samples[1][t0];
        float Az = Samples[2][t0];
        float Bx = Samples[0][t1];
        float By = Samples[1][t1];
        float Bz = Samples[2][t1];
        float Cx = Samples[0][t2];
        float Cy = Samples[1][t2];
        float Cz = Samples[2][t2];
        float Dx = Samples[0][t3];
        float Dy = Samples[1][t3];
        float Dz = Samples[2][t3];
        float Px = value[0][i];
        float Py = value[1][i];
        float Pz = value[2][i];

        // test whether point is contained in current triangle
        float tval1 = ( (By-Ay)*(Cz-Bz)-(Bz-Az)*(Cy-By) )*(Px-Ax)
                     + ( (Bz-Az)*(Cx-Bx)-(Bx-Ax)*(Cz-Bz) )*(Py-Ay)
                     + ( (Bx-Ax)*(Cy-By)-(By-Ay)*(Cx-Bx) )*(Pz-Az);
        float tval2 = ( (Cy-By)*(Dz-Cz)-(Cz-Bz)*(Dy-Cy) )*(Px-Bx)
                     + ( (Cz-Bz)*(Dx-Cx)-(Cx-Bx)*(Dz-Cz) )*(Py-By)
                     + ( (Cx-Bx)*(Dy-Cy)-(Cy-By)*(Dx-Cx) )*(Pz-Bz);
        float tval3 = ( (Dy-Cy)*(Az-Dz)-(Dz-Cz)*(Ay-Dy) )*(Px-Cx)
                     + ( (Dz-Cz)*(Ax-Dx)-(Dx-Cx)*(Az-Dz) )*(Py-Cy)
                     + ( (Dx-Cx)*(Ay-Dy)-(Dy-Cy)*(Ax-Dx) )*(Pz-Cz);
        float tval4 = ( (Ay-Dy)*(Bz-Az)-(Az-Dz)*(By-Ay) )*(Px-Dx)
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
  public int[] valueToIndex(float[][] value) throws VisADException {
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
        float x = value[0][i];
        float y = value[1][i];
        float z = value[2][i];

        // triangle indices
        int t = tri[i];
        int t0 = Delan.Tri[t][0];
        int t1 = Delan.Tri[t][1];
        int t2 = Delan.Tri[t][2];
        int t3 = Delan.Tri[t][3];

        // partial distances
        float D00 = Samples[0][t0] - x;
        float D01 = Samples[1][t0] - y;
        float D02 = Samples[2][t0] - z;
        float D10 = Samples[0][t1] - x;
        float D11 = Samples[1][t1] - y;
        float D12 = Samples[2][t1] - z;
        float D20 = Samples[0][t2] - x;
        float D21 = Samples[1][t2] - y;
        float D22 = Samples[2][t2] - z;
        float D30 = Samples[0][t3] - x;
        float D31 = Samples[1][t3] - y;
        float D32 = Samples[2][t3] - z;

        // distances squared
        float Dsq0 = D00*D00 + D01*D01 + D02*D02;
        float Dsq1 = D10*D10 + D11*D11 + D12*D12;
        float Dsq2 = D20*D20 + D21*D21 + D22*D22;
        float Dsq3 = D30*D30 + D31*D31 + D32*D32;

        // find the minimum distance
        float min = Math.min(Dsq0, Dsq1);
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
  public void valueToInterp(float[][] value, int[][] indices,
                            float[][] weights) throws VisADException {
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
        float[] wval = new float[4];
        // current values
        float x = value[0][i];
        float y = value[1][i];
        float z = value[2][i];

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
        float x0 = Samples[0][t0];
        float y0 = Samples[1][t0];
        float z0 = Samples[2][t0];
        float x1 = Samples[0][t1];
        float y1 = Samples[1][t1];
        float z1 = Samples[2][t1];
        float x2 = Samples[0][t2];
        float y2 = Samples[1][t2];
        float z2 = Samples[2][t2];
        float x3 = Samples[0][t3];
        float y3 = Samples[1][t3];
        float z3 = Samples[2][t3];

        // perpendicular lines
        float C0x = (y3-y1)*(z2-z1) - (z3-z1)*(y2-y1);
        float C0y = (z3-z1)*(x2-x1) - (x3-x1)*(z2-z1);
        float C0z = (x3-x1)*(y2-y1) - (y3-y1)*(x2-x1);
        float C1x = (y3-y0)*(z2-z0) - (z3-z0)*(y2-y0);
        float C1y = (z3-z0)*(x2-x0) - (x3-x0)*(z2-z0);
        float C1z = (x3-x0)*(y2-y0) - (y3-y0)*(x2-x0);
        float C2x = (y3-y0)*(z1-z0) - (z3-z0)*(y1-y0);
        float C2y = (z3-z0)*(x1-x0) - (x3-x0)*(z1-z0);
        float C2z = (x3-x0)*(y1-y0) - (y3-y0)*(x1-x0);
        float C3x = (y2-y0)*(z1-z0) - (z2-z0)*(y1-y0);
        float C3y = (z2-z0)*(x1-x0) - (x2-x0)*(z1-z0);
        float C3z = (x2-x0)*(y1-y0) - (y2-y0)*(x1-x0);

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

  /** return basic lines in array[0], fill-ins in array[1]
      and labels in array[2] */
  public VisADGeometryArray[] makeIsoLines(float interval,
                  float lowlimit, float highlimit, float base,
                  float[] fieldValues, float[][] color_values,
                  boolean swap) throws VisADException {
    if (ManifoldDimension != 2) {
      throw new SetException("Irregular3DSet.makeIsoLines: " +
                             "ManifoldDimension must be 2");
    }


    int[][] Tri = Delan.Tri;
    float[][] samples = getSamples(false);
    int npolygons = Tri.length;
    int nvertex = Delan.Vertices.length;
    if (npolygons < 1 || nvertex < 3) return null;

    // estimate number of vertices
    int maxv = 2 * 2 * Length;

    int color_length = (color_values != null) ? color_values.length : 0;
    float[][] color_levels = null;
    if (color_length > 0) {
      if (color_length > 3) color_length = 3; // no alpha for lines
      color_levels = new float[color_length][maxv];
    }
    float[] vx = new float[maxv];
    float[] vy = new float[maxv];
    float[] vz = new float[maxv];

    int numv = 0;

    for (int jj=0; jj<npolygons; jj++) {
      int va = Tri[jj][0];
      int vb = Tri[jj][1];
      int vc = Tri[jj][2];

      float ga = fieldValues[va];
      // test for missing
      if (ga != ga) continue;

      float gb = fieldValues[vb];
      // test for missing
      if (gb != gb) continue;

      float gc = fieldValues[vc];
      // test for missing
      if (gc != gc) continue;

      float[] auxa = null;
      float[] auxb = null;
      float[] auxc = null;
      if (color_length > 0) {
        auxa = new float[color_length];
        auxb = new float[color_length];
        auxc = new float[color_length];
        for (int i=0; i<color_length; i++) {
          auxa[i] = color_values[i][va];
          auxb[i] = color_values[i][vb];
          auxc[i] = color_values[i][vc];
        }
      }

      float gn = ga < gb ? ga : gb;
      gn = gc < gn ? gc : gn;

      float gx = ga > gb ? ga : gb;
      gx = gc > gx ? gc : gx;

      // compute clow and chi, low and high contour values in the box
      float tmp1 = (gn-base) / interval;
      float clow = base + interval * (( (tmp1) >= 0 ? (int) ((tmp1) + 0.5)
                                              : (int) ((tmp1)-0.5) )-1);
      while (clow<gn) {
        clow += interval;
      }

      tmp1 = (gx-base) / interval;
      float chi = base + interval * (( (tmp1) >= 0 ? (int) ((tmp1) + 0.5)
                                             : (int) ((tmp1)-0.5) )+1);
      while (chi>gx) {
        chi -= interval;
      }
 
      // how many contour lines in the box:
      tmp1 = (chi-clow) / interval;
      int numc = 1+( (tmp1) >= 0 ? (int) ((tmp1) + 0.5) : (int) ((tmp1)-0.5) );

      // gg is current contour line value
      float gg = clow;

      for (int il=0; il<numc && numv+8<maxv; il++, gg += interval) {
        float gba, gca, gcb;
        float ratioba, ratioca, ratiocb;
        int ii;
 
        // make sure gg is within contouring limits
        if (gg < gn) continue;
        if (gg > gx) break;
        if (gg < lowlimit) continue;
        if (gg > highlimit) break;

        // compute orientation of lines inside box
        ii = 0;
        if (gg > ga) ii = 1;
        if (gg > gb) ii += 2;
        if (gg > gc) ii += 4;
        if (ii > 3) ii = 7 - ii;
        if (ii <= 0) continue;

 
        switch (ii) {
          case 1:
            gba = gb-ga;
            gca = gc-ga;

            ratioba = (gg-ga)/gba;
            ratioca = (gg-ga)/gca;

            if (color_length > 0) {
              for (int i=0; i<color_length; i++) {
                color_levels[i][numv] = auxa[i] + (auxb[i]-auxa[i]) * ratioba;
                color_levels[i][numv+1] = auxa[i] + (auxc[i]-auxa[i]) * ratioca;
              }
            }

            vx[numv] = samples[0][va] + (samples[0][vb]-samples[0][va]) * ratioba;
            vy[numv] = samples[1][va] + (samples[1][vb]-samples[1][va]) * ratioba;
            vz[numv] = samples[2][va] + (samples[2][vb]-samples[2][va]) * ratioba;
            numv++;
            vx[numv] = samples[0][va] + (samples[0][vc]-samples[0][va]) * ratioca;
            vy[numv] = samples[1][va] + (samples[1][vc]-samples[1][va]) * ratioca;
            vz[numv] = samples[2][va] + (samples[2][vc]-samples[2][va]) * ratioca;
            numv++;
          break;

          case 2:
            gba = gb-ga;
            gcb = gc-gb;
 
            ratioba = (gg-ga)/gba;
            ratiocb = (gg-gb)/gcb;
 
            if (color_length > 0) {
              for (int i=0; i<color_length; i++) {
                color_levels[i][numv] = auxa[i] + (auxb[i]-auxa[i]) * ratioba;
                color_levels[i][numv+1] = auxb[i] + (auxc[i]-auxb[i]) * ratiocb;
              }
            }
 
            vx[numv] = samples[0][va] + (samples[0][vb]-samples[0][va]) * ratioba;
            vy[numv] = samples[1][va] + (samples[1][vb]-samples[1][va]) * ratioba;
            vz[numv] = samples[2][va] + (samples[2][vb]-samples[2][va]) * ratioba;
            numv++;
            vx[numv] = samples[0][vb] + (samples[0][vc]-samples[0][vb]) * ratiocb;
            vy[numv] = samples[1][vb] + (samples[1][vc]-samples[1][vb]) * ratiocb;
            vz[numv] = samples[2][vb] + (samples[2][vc]-samples[2][vb]) * ratiocb;
            numv++;
          break;

          case 3:
            gca = gc-ga;
            gcb = gc-gb;
 
            ratioca = (gg-ga)/gca;
            ratiocb = (gg-gb)/gcb;
 
            if (color_length > 0) {
              for (int i=0; i<color_length; i++) {
                color_levels[i][numv] = auxa[i] + (auxc[i]-auxa[i]) * ratioca;
                color_levels[i][numv+1] = auxb[i] + (auxc[i]-auxb[i]) * ratiocb;
              }
            }
 
            vx[numv] = samples[0][va] + (samples[0][vc]-samples[0][va]) * ratioca;
            vy[numv] = samples[1][va] + (samples[1][vc]-samples[1][va]) * ratioca;
            vz[numv] = samples[2][va] + (samples[2][vc]-samples[2][va]) * ratioca;
            numv++;
            vx[numv] = samples[0][vb] + (samples[0][vc]-samples[0][vb]) * ratiocb;
            vy[numv] = samples[1][vb] + (samples[1][vc]-samples[1][vb]) * ratiocb;
            vz[numv] = samples[2][vb] + (samples[2][vc]-samples[2][vb]) * ratiocb;
            numv++;
          break;

        } // end switch (ii)
      } // end for (int il=0; il<numc && numv+8<mav; il++, gg += interval)

    } // end for (int jj=0; jj<npolygons; jj++)

    VisADLineArray[] arrays = new VisADLineArray[3];
    arrays[0] = new VisADLineArray();
    float[][] coordinates = new float[3][numv];
    System.arraycopy(vx, 0, coordinates[0], 0, numv);
    System.arraycopy(vy, 0, coordinates[1], 0, numv);
    System.arraycopy(vz, 0, coordinates[2], 0, numv);
    vx = null;
    vy = null;
    vz = null;
    float[][] colors = null;
    if (color_length > 0) {
      colors = new float[3][numv];
      System.arraycopy(color_levels[0], 0, colors[0], 0, numv);
      System.arraycopy(color_levels[1], 0, colors[1], 0, numv);
      System.arraycopy(color_levels[2], 0, colors[2], 0, numv);
      color_levels = null;
    }
    setGeometryArray(arrays[0], coordinates, 3, colors);
    arrays[1] = null;
    arrays[2] = null;
    return arrays;
  }

  public VisADGeometryArray makeIsoSurface(float isolevel,
         float[] fieldValues, float[][] color_values)
         throws VisADException {

    if (ManifoldDimension != 3) {
      throw new SetException("Irregular3DSet.main_isosurf: " +
                             "ManifoldDimension must be 3");
    }

    float[][] fieldVertices = new float[3][];
    float[][] color_levels = null;
    if (color_values != null) {
      color_levels = new float[color_values.length][];
    }
    int[][][] polyToVert = new int[1][][];
    int[][][] vertToPoly = new int[1][][];
    makeIsosurface(isolevel, fieldValues, color_values, fieldVertices,
                   color_levels, polyToVert, vertToPoly);

    int nvertex = vertToPoly[0].length;
    int npolygons = polyToVert[0].length;
    float[] NX = new float[nvertex];
    float[] NY = new float[nvertex];
    float[] NZ = new float[nvertex];

    if (nvertex == 0 || npolygons == 0) return null;


    // with make_normals
    float[] NxA = new float[npolygons];
    float[] NxB = new float[npolygons];
    float[] NyA = new float[npolygons];
    float[] NyB = new float[npolygons];
    float[] NzA = new float[npolygons];
    float[] NzB = new float[npolygons];
    float[] Pnx = new float[npolygons];
    float[] Pny = new float[npolygons];
    float[] Pnz = new float[npolygons];

    make_normals(fieldVertices[0], fieldVertices[1], fieldVertices[2],
                 NX, NY, NZ, nvertex, npolygons, Pnx, Pny, Pnz,
                 NxA, NxB, NyA, NyB, NzA, NzB, vertToPoly[0], polyToVert[0]);

    // take the garbage out
    NxA = NxB = NyA = NyB = NzA = NzB = Pnx = Pny = Pnz = null;


/*
    // without poly_triangle_stripe
    int ntris = npolygons;
    for (int i=0; i<npolygons; i++) {
      if (polyToVert[0][i].length > 3) ntris++;
    }
    VisADTriangleArray array = new VisADTriangleArray();
    array.vertexCount = 3 * ntris;
    float[][] coordinates = new float[3][3 * ntris];
    float[] normals = new float[9 * ntris];
    float[][] colors = null;
    if (color_levels != null) {
      colors = new float[color_levels.length][3 * ntris];
    }
    int j = 0;
    int jn = 0;
    for (int i=0; i<npolygons; i++) {
      int a = polyToVert[0][i][0];
      int b = polyToVert[0][i][1];
      int c = polyToVert[0][i][2];
      for (int k=0; k<3; k++) {
        coordinates[k][j] = fieldVertices[k][a];
        coordinates[k][j + 1] = fieldVertices[k][b];
        coordinates[k][j + 2] = fieldVertices[k][c];
      }
      normals[jn] = NX[a];
      normals[jn + 1] = NY[a];
      normals[jn + 2] = NZ[a];
      normals[jn + 3] = NX[b];
      normals[jn + 4] = NY[b];
      normals[jn + 5] = NZ[b];
      normals[jn + 6] = NX[c];
      normals[jn + 7] = NY[c];
      normals[jn + 8] = NZ[c];
      if (color_levels != null) {
        for (int k=0; k<3; k++) {
          colors[k][j] = color_levels[k][a];
          colors[k][j + 1] = color_levels[k][b];
          colors[k][j + 2] = color_levels[k][c];
        }
        if (color_levels.length > 3) {
          colors[4][j] = color_levels[4][a];
          colors[4][j + 1] = color_levels[4][b];
          colors[4][j + 2] = color_levels[4][c];
        }
      }
      j += 3;
      jn += 9;
      if (polyToVert[0][i].length > 3) {
        int d = polyToVert[0][i][3];
        for (int k=0; k<3; k++) {
          coordinates[k][j] = fieldVertices[k][a];
          coordinates[k][j + 1] = fieldVertices[k][c];
          coordinates[k][j + 2] = fieldVertices[k][d];
        }
        normals[jn] = NX[a];
        normals[jn + 1] = NY[a];
        normals[jn + 2] = NZ[a];
        normals[jn + 3] = NX[c];
        normals[jn + 4] = NY[c];
        normals[jn + 5] = NZ[c];
        normals[jn + 6] = NX[d];
        normals[jn + 7] = NY[d];
        normals[jn + 8] = NZ[d];
        if (color_levels != null) {
          for (int k=0; k<3; k++) {
            colors[k][j] = color_levels[k][a];
            colors[k][j + 1] = color_levels[k][c];
            colors[k][j + 2] = color_levels[k][d];
          }
          if (color_levels.length > 3) {
            colors[4][j] = color_levels[4][a];
            colors[4][j + 1] = color_levels[4][c];
            colors[4][j + 2] = color_levels[4][d];
          }
        }
        j += 3;
        jn += 9;
      }
    }
    vertToPoly = null;
    polyToVert = null;
    setGeometryArray(array, coordinates, 4, colors);
    array.normals = normals;
    normals = null;
*/
 



    // with poly_triangle_stripe
    float[] normals = new float[3 * nvertex];
    int j = 0;
    for (int i=0; i<nvertex; i++) {
      normals[j++] = (float) NX[i];
      normals[j++] = (float) NY[i];
      normals[j++] = (float) NZ[i];
    }
    // take the garbage out
    NX = NY = NZ = null;

    int[] stripe = new int[6 * npolygons];
 
    int size_stripe =
      poly_triangle_stripe(stripe, nvertex, npolygons,
                           vertToPoly[0], polyToVert[0]);
    // take the garbage out
    vertToPoly = null;
    polyToVert = null;

    VisADIndexedTriangleStripArray array =
      new VisADIndexedTriangleStripArray();
 
    // set up indices
    array.indexCount = size_stripe;
    array.indices = new int[size_stripe];
    System.arraycopy(stripe, 0, array.indices, 0, size_stripe);
    array.stripVertexCounts = new int[1];
    array.stripVertexCounts[0] = size_stripe;
    // take the garbage out
    stripe = null;
 
    // set coordinates and colors
    setGeometryArray(array, fieldVertices, 4, color_levels);
    // take the garbage out
    fieldVertices = null;
    color_levels = null;
 
    // array.vertexFormat |= NORMALS;
    array.normals = normals;



    return array;
  }

  /** compute an Isosurface through the Irregular3DSet
      given an array of fieldValues at each sample,
      an isolevel at which to form the surface, and
      other values (e.g., colors) at each sample in
      auxValues;
      return vertex locations in fieldVertices[3][nvertex];
      return values of auxValues at vertices in auxLevels;
      return pointers from vertices to polys in
             vertToPoly[1][nvertex][nverts[i]];
      return pointers from polys to vertices in
             polyToVert[1][npolygons][4]; */
  private void makeIsosurface(float isolevel, float[] fieldValues,
                              float[][] auxValues, float[][] fieldVertices,
                              float[][] auxLevels, int[][][] polyToVert,
                              int[][][] vertToPoly) throws VisADException {
    boolean DEBUG = false;
    if (ManifoldDimension != 3) {
      throw new SetException("Irregular3DSet.makeIsosurface: " +
                             "ManifoldDimension must be 3");
    }
    if (fieldValues.length != Length) {
      throw new SetException("Irregular3DSet.makeIsosurface: "
                            +"fieldValues length does't match");
    }
    if (Double.isNaN(isolevel)) {
      throw new SetException("Irregular3DSet.makeIsosurface: "
                            +"isolevel cannot be missing");
    }
    if (fieldVertices.length != 3 || polyToVert.length != 1
                                  || vertToPoly.length != 1) {
      throw new SetException("Irregular3DSet.makeIsosurface: return value"
                             + " arrays not correctly initialized " +
                             fieldVertices.length + " " + polyToVert.length +
                             " " + vertToPoly.length);
    }
    int naux = (auxValues != null) ? auxValues.length : 0;
    if (naux > 0) {
      if (auxLevels == null || auxLevels.length != naux) {
        throw new SetException("Irregular3DSet.makeIsosurface: "
                              +"auxLevels length doesn't match");
      }
      for (int i=0; i<naux; i++) {
        if (auxValues[i].length != Length) {
          throw new SetException("Irregular3DSet.makeIsosurface: "
                                +"auxValues lengths don't match");
        }
      }
    }
    else {
      if (auxLevels != null) {
        throw new SetException("Irregular3DSet.makeIsosurface: "
                              +"auxValues null but auxLevels not null");
      }
    }


    if (DEBUG) {
      System.out.println("isolevel = " + isolevel + "\n");
      System.out.println("fieldValues " + fieldValues.length);
      for (int i=0; i<fieldValues.length; i++) {
        System.out.println("  " + i + " -> " + fieldValues[i]);
      }
      System.out.println(Delan.sampleString(Samples));
    }


    int trilength = Delan.Tri.length;

    // temporary storage of polyToVert structure
    int[][] polys = new int[trilength][4];

    // pointers from global edge number to nvertex
    int[] globalToVertex = new int[Delan.NumEdges];
    for (int i=0; i<Delan.NumEdges; i++) globalToVertex[i] = -1;

    // global edges temporary storage array
    float[][] edgeInterp = new float[DomainDimension][Delan.NumEdges];
    for (int i=0; i<Delan.NumEdges; i++) edgeInterp[0][i] = Float.NaN;

    // global edges temporary storage array for aux levels
    float[][] auxInterp = (naux > 0) ? new float[naux][Delan.NumEdges] : null;

    int nvertex = 0;
    int npolygons = 0;
    for (int i=0; i<trilength; i++) {
      int v0 = Delan.Tri[i][0];
      int v1 = Delan.Tri[i][1];
      int v2 = Delan.Tri[i][2];
      int v3 = Delan.Tri[i][3];
      float f0 = (float) fieldValues[v0];
      float f1 = (float) fieldValues[v1];
      float f2 = (float) fieldValues[v2];
      float f3 = (float) fieldValues[v3];
      int e0, e1, e2, e3, e4, e5;

      // compute tetrahedron signature
      // vector from v0 to v3
      float vx = Samples[0][v3] - Samples[0][v0];
      float vy = Samples[1][v3] - Samples[1][v0];
      float vz = Samples[2][v3] - Samples[2][v0];
      // cross product (v2 - v0) x (v1 - v0)
      float sx = Samples[0][v2] - Samples[0][v0];
      float sy = Samples[1][v2] - Samples[1][v0];
      float sz = Samples[2][v2] - Samples[2][v0];
      float tx = Samples[0][v1] - Samples[0][v0];
      float ty = Samples[1][v1] - Samples[1][v0];
      float tz = Samples[2][v1] - Samples[2][v0];
      float cx = sy * tz - sz * ty;
      float cy = sz * tx - sx * tz;
      float cz = sx * ty - sy * tx;
      // signature is sign of v (dot) c
      float sig = vx * cx + vy * cy + vz * cz;

      // 8 possibilities
      int index = ((f0 > isolevel) ? 1 : 0)
                + ((f1 > isolevel) ? 2 : 0)
                + ((f2 > isolevel) ? 4 : 0)
                + ((f3 > isolevel) ? 8 : 0);
      // apply signature to index
      if (sig < 0.0f) index = 15 - index;

      switch (index) {
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
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e0])) {
*/
          // test for missing
          if (edgeInterp[0][e0] != edgeInterp[0][e0]) {
            float a = (isolevel - f1)/(f0 - f1);
            if (a < 0) a = -a;
            edgeInterp[0][e0] = (float) a*Samples[0][v0] + (1-a)*Samples[0][v1];
            edgeInterp[1][e0] = (float) a*Samples[1][v0] + (1-a)*Samples[1][v1];
            edgeInterp[2][e0] = (float) a*Samples[2][v0] + (1-a)*Samples[2][v1];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e0] = (float) a*auxValues[j][v0] + (1-a)*auxValues[j][v1];
            }
            globalToVertex[e0] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e1])) {
*/
          // test for missing
          if (edgeInterp[0][e1] != edgeInterp[0][e1]) {
            float a = (isolevel - f2)/(f0 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e1] = (float) a*Samples[0][v0] + (1-a)*Samples[0][v2];
            edgeInterp[1][e1] = (float) a*Samples[1][v0] + (1-a)*Samples[1][v2];
            edgeInterp[2][e1] = (float) a*Samples[2][v0] + (1-a)*Samples[2][v2];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e1] = (float) a*auxValues[j][v0] + (1-a)*auxValues[j][v2];
            }
            globalToVertex[e1] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e2])) {
*/
          // test for missing
          if (edgeInterp[0][e2] != edgeInterp[0][e2]) {
            float a = (isolevel - f3)/(f0 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e2] = (float) a*Samples[0][v0] + (1-a)*Samples[0][v3];
            edgeInterp[1][e2] = (float) a*Samples[1][v0] + (1-a)*Samples[1][v3];
            edgeInterp[2][e2] = (float) a*Samples[2][v0] + (1-a)*Samples[2][v3];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e2] = (float) a*auxValues[j][v0] + (1-a)*auxValues[j][v3];
            }
            globalToVertex[e2] = nvertex;
            nvertex++;
          }

          // fill in the polys and vertToPoly arrays
          polys[npolygons][0] = e0;
          if (index == 1) {
            polys[npolygons][1] = e1;
            polys[npolygons][2] = e2;
          }
          else { // index == 14
            polys[npolygons][1] = e2;
            polys[npolygons][2] = e1;
          }
          polys[npolygons][3] = -1;

          // on to the next tetrahedron
          npolygons++;
          break;

        case 2:
        case 13:             // plane slices a triangle
          // define edge values needed
          e0 = Delan.Edges[i][0];
          e3 = Delan.Edges[i][3];
          e4 = Delan.Edges[i][4];

          // fill in edge interpolation values if they haven't been found
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e0])) {
*/
          // test for missing
          if (edgeInterp[0][e0] != edgeInterp[0][e0]) {
            float a = (isolevel - f1)/(f0 - f1);
            if (a < 0) a = -a;
            edgeInterp[0][e0] = (float) a*Samples[0][v0] + (1-a)*Samples[0][v1];
            edgeInterp[1][e0] = (float) a*Samples[1][v0] + (1-a)*Samples[1][v1];
            edgeInterp[2][e0] = (float) a*Samples[2][v0] + (1-a)*Samples[2][v1];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e0] = (float) a*auxValues[j][v0] + (1-a)*auxValues[j][v1];
            }
            globalToVertex[e0] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e3])) {
*/
          // test for missing
          if (edgeInterp[0][e3] != edgeInterp[0][e3]) {
            float a = (isolevel - f2)/(f1 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e3] = (float) a*Samples[0][v1] + (1-a)*Samples[0][v2];
            edgeInterp[1][e3] = (float) a*Samples[1][v1] + (1-a)*Samples[1][v2];
            edgeInterp[2][e3] = (float) a*Samples[2][v1] + (1-a)*Samples[2][v2];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e3] = (float) a*auxValues[j][v1] + (1-a)*auxValues[j][v2];
            }
            globalToVertex[e3] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e4])) {
*/
          // test for missing
          if (edgeInterp[0][e4] != edgeInterp[0][e4]) {
            float a = (isolevel - f3)/(f1 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e4] = (float) a*Samples[0][v1] + (1-a)*Samples[0][v3];
            edgeInterp[1][e4] = (float) a*Samples[1][v1] + (1-a)*Samples[1][v3];
            edgeInterp[2][e4] = (float) a*Samples[2][v1] + (1-a)*Samples[2][v3];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e4] = (float) a*auxValues[j][v1] + (1-a)*auxValues[j][v3];
            }
            globalToVertex[e4] = nvertex;
            nvertex++;
          }

          // fill in the polys array
          polys[npolygons][0] = e0;
          if (index == 2) {
            polys[npolygons][1] = e4;
            polys[npolygons][2] = e3;
          }
          else { // index == 13
            polys[npolygons][1] = e3;
            polys[npolygons][2] = e4;
          }
          polys[npolygons][3] = -1;

          // on to the next tetrahedron
          npolygons++;
          break;

        case 3:
        case 12:             // plane slices a quadrilateral
          // define edge values needed
          e1 = Delan.Edges[i][1];
          e2 = Delan.Edges[i][2];
          e3 = Delan.Edges[i][3];
          e4 = Delan.Edges[i][4];

          // fill in edge interpolation values if they haven't been found
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e1])) {
*/
          // test for missing
          if (edgeInterp[0][e1] != edgeInterp[0][e1]) {
            float a = (isolevel - f2)/(f0 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e1] = (float) a*Samples[0][v0] + (1-a)*Samples[0][v2];
            edgeInterp[1][e1] = (float) a*Samples[1][v0] + (1-a)*Samples[1][v2];
            edgeInterp[2][e1] = (float) a*Samples[2][v0] + (1-a)*Samples[2][v2];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e1] = (float) a*auxValues[j][v0] + (1-a)*auxValues[j][v2];
            }
            globalToVertex[e1] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e2])) {
*/
          // test for missing
          if (edgeInterp[0][e2] != edgeInterp[0][e2]) {
            float a = (isolevel - f3)/(f0 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e2] = (float) a*Samples[0][v0] + (1-a)*Samples[0][v3];
            edgeInterp[1][e2] = (float) a*Samples[1][v0] + (1-a)*Samples[1][v3];
            edgeInterp[2][e2] = (float) a*Samples[2][v0] + (1-a)*Samples[2][v3];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e2] = (float) a*auxValues[j][v0] + (1-a)*auxValues[j][v3];
            }
            globalToVertex[e2] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e4])) {
*/
          // test for missing
          if (edgeInterp[0][e4] != edgeInterp[0][e4]) {
            float a = (isolevel - f3)/(f1 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e4] = (float) a*Samples[0][v1] + (1-a)*Samples[0][v3];
            edgeInterp[1][e4] = (float) a*Samples[1][v1] + (1-a)*Samples[1][v3];
            edgeInterp[2][e4] = (float) a*Samples[2][v1] + (1-a)*Samples[2][v3];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e4] = (float) a*auxValues[j][v1] + (1-a)*auxValues[j][v3];
            }
            globalToVertex[e4] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e3])) {
*/
          // test for missing
          if (edgeInterp[0][e3] != edgeInterp[0][e3]) {
            float a = (isolevel - f2)/(f1 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e3] = (float) a*Samples[0][v1] + (1-a)*Samples[0][v2];
            edgeInterp[1][e3] = (float) a*Samples[1][v1] + (1-a)*Samples[1][v2];
            edgeInterp[2][e3] = (float) a*Samples[2][v1] + (1-a)*Samples[2][v2];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e3] = (float) a*auxValues[j][v1] + (1-a)*auxValues[j][v2];
            }
            globalToVertex[e3] = nvertex;
            nvertex++;
          }


          // fill in the polys array
          polys[npolygons][0] = e1;
          if (index == 3) {
            polys[npolygons][1] = e2;
            polys[npolygons][2] = e4;
            polys[npolygons][3] = e3;
          }
          else { // index == 12
            polys[npolygons][1] = e3;
            polys[npolygons][2] = e4;
            polys[npolygons][3] = e2;
          }

          // on to the next tetrahedron
          npolygons++;
          break;

        case 4:
        case 11:             // plane slices a triangle
          // define edge values needed
          e1 = Delan.Edges[i][1];
          e3 = Delan.Edges[i][3];
          e5 = Delan.Edges[i][5];

          // fill in edge interpolation values if they haven't been found
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e1])) {
*/
          // test for missing
          if (edgeInterp[0][e1] != edgeInterp[0][e1]) {
            float a = (isolevel - f2)/(f0 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e1] = (float) a*Samples[0][v0] + (1-a)*Samples[0][v2];
            edgeInterp[1][e1] = (float) a*Samples[1][v0] + (1-a)*Samples[1][v2];
            edgeInterp[2][e1] = (float) a*Samples[2][v0] + (1-a)*Samples[2][v2];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e1] = (float) a*auxValues[j][v0] + (1-a)*auxValues[j][v2];
            }
            globalToVertex[e1] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e3])) {
*/
          // test for missing
          if (edgeInterp[0][e3] != edgeInterp[0][e3]) {
            float a = (isolevel - f2)/(f1 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e3] = (float) a*Samples[0][v1] + (1-a)*Samples[0][v2];
            edgeInterp[1][e3] = (float) a*Samples[1][v1] + (1-a)*Samples[1][v2];
            edgeInterp[2][e3] = (float) a*Samples[2][v1] + (1-a)*Samples[2][v2];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e3] = (float) a*auxValues[j][v1] + (1-a)*auxValues[j][v2];
            }
            globalToVertex[e3] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e5])) {
*/
          // test for missing
          if (edgeInterp[0][e5] != edgeInterp[0][e5]) {
            float a = (isolevel - f3)/(f2 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e5] = (float) a*Samples[0][v2] + (1-a)*Samples[0][v3];
            edgeInterp[1][e5] = (float) a*Samples[1][v2] + (1-a)*Samples[1][v3];
            edgeInterp[2][e5] = (float) a*Samples[2][v2] + (1-a)*Samples[2][v3];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e5] = (float) a*auxValues[j][v2] + (1-a)*auxValues[j][v3];
            }
            globalToVertex[e5] = nvertex;
            nvertex++;
          }

          // fill in the polys array
          polys[npolygons][0] = e1;
          if (index == 4) {
            polys[npolygons][1] = e3;
            polys[npolygons][2] = e5;
          }
          else { // index == 11
            polys[npolygons][1] = e5;
            polys[npolygons][2] = e3;
          }
          polys[npolygons][3] = -1;

          // on to the next tetrahedron
          npolygons++;
          break;

        case 5:
        case 10:             // plane slices a quadrilateral
          // define edge values needed
          e0 = Delan.Edges[i][0];
          e2 = Delan.Edges[i][2];
          e3 = Delan.Edges[i][3];
          e5 = Delan.Edges[i][5];

          // fill in edge interpolation values if they haven't been found
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e0])) {
*/
          // test for missing
          if (edgeInterp[0][e0] != edgeInterp[0][e0]) {
            float a = (isolevel - f1)/(f0 - f1);
            if (a < 0) a = -a;
            edgeInterp[0][e0] = (float) a*Samples[0][v0] + (1-a)*Samples[0][v1];
            edgeInterp[1][e0] = (float) a*Samples[1][v0] + (1-a)*Samples[1][v1];
            edgeInterp[2][e0] = (float) a*Samples[2][v0] + (1-a)*Samples[2][v1];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e0] = (float) a*auxValues[j][v0] + (1-a)*auxValues[j][v1];
            }
            globalToVertex[e0] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e2])) {
*/
          // test for missing
          if (edgeInterp[0][e2] != edgeInterp[0][e2]) {
            float a = (isolevel - f3)/(f0 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e2] = (float) a*Samples[0][v0] + (1-a)*Samples[0][v3];
            edgeInterp[1][e2] = (float) a*Samples[1][v0] + (1-a)*Samples[1][v3];
            edgeInterp[2][e2] = (float) a*Samples[2][v0] + (1-a)*Samples[2][v3];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e2] = (float) a*auxValues[j][v0] + (1-a)*auxValues[j][v3];
            }
            globalToVertex[e2] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e5])) {
*/
          // test for missing
          if (edgeInterp[0][e5] != edgeInterp[0][e5]) {
            float a = (isolevel - f3)/(f2 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e5] = (float) a*Samples[0][v2] + (1-a)*Samples[0][v3];
            edgeInterp[1][e5] = (float) a*Samples[1][v2] + (1-a)*Samples[1][v3];
            edgeInterp[2][e5] = (float) a*Samples[2][v2] + (1-a)*Samples[2][v3];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e5] = (float) a*auxValues[j][v2] + (1-a)*auxValues[j][v3];
            }
            globalToVertex[e5] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e3])) {
*/
          // test for missing
          if (edgeInterp[0][e3] != edgeInterp[0][e3]) {
            float a = (isolevel - f2)/(f1 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e3] = (float) a*Samples[0][v1] + (1-a)*Samples[0][v2];
            edgeInterp[1][e3] = (float) a*Samples[1][v1] + (1-a)*Samples[1][v2];
            edgeInterp[2][e3] = (float) a*Samples[2][v1] + (1-a)*Samples[2][v2];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e3] = (float) a*auxValues[j][v1] + (1-a)*auxValues[j][v2];
            }
            globalToVertex[e3] = nvertex;
            nvertex++;
          }


          // fill in the polys array
          polys[npolygons][0] = e0;
          if (index == 5) {
            polys[npolygons][1] = e3;
            polys[npolygons][2] = e5;
            polys[npolygons][3] = e2;
          }
          else { // index == 10
            polys[npolygons][1] = e2;
            polys[npolygons][2] = e5;
            polys[npolygons][3] = e3;
          }

          // on to the next tetrahedron
          npolygons++;
          break;

        case 6:
        case 9:              // plane slices a quadrilateral
          // define edge values needed
          e0 = Delan.Edges[i][0];
          e1 = Delan.Edges[i][1];
          e4 = Delan.Edges[i][4];
          e5 = Delan.Edges[i][5];

          // fill in edge interpolation values if they haven't been found
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e0])) {
*/
          // test for missing
          if (edgeInterp[0][e0] != edgeInterp[0][e0]) {
            float a = (isolevel - f1)/(f0 - f1);
            if (a < 0) a = -a;
            edgeInterp[0][e0] = (float) a*Samples[0][v0] + (1-a)*Samples[0][v1];
            edgeInterp[1][e0] = (float) a*Samples[1][v0] + (1-a)*Samples[1][v1];
            edgeInterp[2][e0] = (float) a*Samples[2][v0] + (1-a)*Samples[2][v1];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e0] = (float) a*auxValues[j][v0] + (1-a)*auxValues[j][v1];
            }
            globalToVertex[e0] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e1])) {
*/
          // test for missing
          if (edgeInterp[0][e1] != edgeInterp[0][e1]) {
            float a = (isolevel - f2)/(f0 - f2);
            if (a < 0) a = -a;
            edgeInterp[0][e1] = (float) a*Samples[0][v0] + (1-a)*Samples[0][v2];
            edgeInterp[1][e1] = (float) a*Samples[1][v0] + (1-a)*Samples[1][v2];
            edgeInterp[2][e1] = (float) a*Samples[2][v0] + (1-a)*Samples[2][v2];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e1] = (float) a*auxValues[j][v0] + (1-a)*auxValues[j][v2];
            }
            globalToVertex[e1] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e5])) {
*/
          // test for missing
          if (edgeInterp[0][e5] != edgeInterp[0][e5]) {
            float a = (isolevel - f3)/(f2 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e5] = (float) a*Samples[0][v2] + (1-a)*Samples[0][v3];
            edgeInterp[1][e5] = (float) a*Samples[1][v2] + (1-a)*Samples[1][v3];
            edgeInterp[2][e5] = (float) a*Samples[2][v2] + (1-a)*Samples[2][v3];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e5] = (float) a*auxValues[j][v2] + (1-a)*auxValues[j][v3];
            }
            globalToVertex[e5] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e4])) {
*/
          // test for missing
          if (edgeInterp[0][e4] != edgeInterp[0][e4]) {
            float a = (isolevel - f3)/(f1 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e4] = (float) a*Samples[0][v1] + (1-a)*Samples[0][v3];
            edgeInterp[1][e4] = (float) a*Samples[1][v1] + (1-a)*Samples[1][v3];
            edgeInterp[2][e4] = (float) a*Samples[2][v1] + (1-a)*Samples[2][v3];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e4] = (float) a*auxValues[j][v1] + (1-a)*auxValues[j][v3];
            }
            globalToVertex[e4] = nvertex;
            nvertex++;
          }


          // fill in the polys array
          polys[npolygons][0] = e0;
          if (index == 6) {
            polys[npolygons][1] = e4;
            polys[npolygons][2] = e5;
            polys[npolygons][3] = e1;
          }
          else { // index == 9
            polys[npolygons][1] = e1;
            polys[npolygons][2] = e5;
            polys[npolygons][3] = e4;
          }

          // on to the next tetrahedron
          npolygons++;
          break;

        case 7:
        case 8:              // plane slices a triangle
          // interpolate between 3:0, 3:1, 3:2 for tri edges, same as case 8
          // define edge values needed
          e2 = Delan.Edges[i][2];
          e4 = Delan.Edges[i][4];
          e5 = Delan.Edges[i][5];

          // fill in edge interpolation values if they haven't been found
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e2])) {
*/
          // test for missing
          if (edgeInterp[0][e2] != edgeInterp[0][e2]) {
            float a = (isolevel - f3)/(f0 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e2] = (float) a*Samples[0][v0] + (1-a)*Samples[0][v3];
            edgeInterp[1][e2] = (float) a*Samples[1][v0] + (1-a)*Samples[1][v3];
            edgeInterp[2][e2] = (float) a*Samples[2][v0] + (1-a)*Samples[2][v3];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e2] = (float) a*auxValues[j][v0] + (1-a)*auxValues[j][v3];
            }
            globalToVertex[e2] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e4])) {
*/
          // test for missing
          if (edgeInterp[0][e4] != edgeInterp[0][e4]) {
            float a = (isolevel - f3)/(f1 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e4] = (float) a*Samples[0][v1] + (1-a)*Samples[0][v3];
            edgeInterp[1][e4] = (float) a*Samples[1][v1] + (1-a)*Samples[1][v3];
            edgeInterp[2][e4] = (float) a*Samples[2][v1] + (1-a)*Samples[2][v3];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e4] = (float) a*auxValues[j][v1] + (1-a)*auxValues[j][v3];
            }
            globalToVertex[e4] = nvertex;
            nvertex++;
          }
/* WLH 25 Oct 97
          if (Double.isNaN(edgeInterp[0][e5])) {
*/
          // test for missing
          if (edgeInterp[0][e5] != edgeInterp[0][e5]) {
            float a = (isolevel - f3)/(f2 - f3);
            if (a < 0) a = -a;
            edgeInterp[0][e5] = (float) a*Samples[0][v2] + (1-a)*Samples[0][v3];
            edgeInterp[1][e5] = (float) a*Samples[1][v2] + (1-a)*Samples[1][v3];
            edgeInterp[2][e5] = (float) a*Samples[2][v2] + (1-a)*Samples[2][v3];
            for (int j=0; j<naux; j++) {
              auxInterp[j][e5] = (float) a*auxValues[j][v2] + (1-a)*auxValues[j][v3];
            }
            globalToVertex[e5] = nvertex;
            nvertex++;
          }

          // fill in the polys array
          polys[npolygons][0] = e2;
          if (index == 7) {
            polys[npolygons][1] = e4;
            polys[npolygons][2] = e5;
          }
          else { // index == 8
            polys[npolygons][1] = e5;
            polys[npolygons][2] = e4;
          }
          polys[npolygons][3] = -1;

          // on to the next tetrahedron
          npolygons++;
          break;
      }
    }



    if (DEBUG) {
      System.out.println("\npolys (polys -> global edges) " + npolygons + "\n");
      for (int i=0; i<npolygons; i++) {
        String s = "  " + i + " -> ";
        for (int j=0; j<4; j++) {
          s = s + " " + polys[i][j];
        }
        System.out.println(s + "\n");
      }
    }



    // transform polys array into polyToVert array
    polyToVert[0] = new int[npolygons][];
    for (int i=0; i<npolygons; i++) {
      int n = polys[i][3] < 0 ? 3 : 4;
      polyToVert[0][i] = new int[n];
      for (int j=0; j<n; j++) polyToVert[0][i][j] = globalToVertex[polys[i][j]];
    }


    if (DEBUG) {
      System.out.println("\npolyToVert (polys -> vertices) " + npolygons + "\n");
      for (int i=0; i<npolygons; i++) {
        String s = "  " + i + " -> ";
        for (int j=0; j<polyToVert[0][i].length; j++) {
          s = s + " " + polyToVert[0][i][j];
        }
        System.out.println(s + "\n");
      }
    }
 

    // build nverts helper array
    int[] nverts = new int[nvertex];
    for (int i=0; i<nvertex; i++) nverts[i] = 0;
    for (int i=0; i<npolygons; i++) {
      nverts[polyToVert[0][i][0]]++;
      nverts[polyToVert[0][i][1]]++;
      nverts[polyToVert[0][i][2]]++;
      if (polyToVert[0][i].length > 3) nverts[polyToVert[0][i][3]]++;
    }

    // initialize vertToPoly array
    vertToPoly[0] = new int[nvertex][];
    for (int i=0; i<nvertex; i++) {
      vertToPoly[0][i] = new int[nverts[i]];
    }

    // fill in vertToPoly array
    for (int i=0; i<nvertex; i++) nverts[i] = 0;
    for (int i=0; i<npolygons; i++) {
      int a = polyToVert[0][i][0];
      int b = polyToVert[0][i][1];
      int c = polyToVert[0][i][2];
      vertToPoly[0][a][nverts[a]++] = i;
      vertToPoly[0][b][nverts[b]++] = i;
      vertToPoly[0][c][nverts[c]++] = i;
      if (polyToVert[0][i].length > 3) {
        int d = polyToVert[0][i][3];
        if (d != -1) vertToPoly[0][d][nverts[d]++] = i;
      }
    }



    if (DEBUG) {
      System.out.println("\nvertToPoly (vertices -> polys) " + nvertex + "\n");
      for (int i=0; i<nvertex; i++) {
        String s = "  " + i + " -> ";
        for (int j=0; j<vertToPoly[0][i].length; j++) {
          s = s + " " + vertToPoly[0][i][j];
        }
        System.out.println(s + "\n");
      }
    }



    // set up fieldVertices and auxLevels
    fieldVertices[0] = new float[nvertex];
    fieldVertices[1] = new float[nvertex];
    fieldVertices[2] = new float[nvertex];
    for (int j=0; j<naux; j++) {
      auxLevels[j] = new float[nvertex];
    }
    for (int i=0; i<Delan.NumEdges; i++) {
      if (globalToVertex[i] >= 0) {
        fieldVertices[0][globalToVertex[i]] = edgeInterp[0][i];
        fieldVertices[1][globalToVertex[i]] = edgeInterp[1][i];
        fieldVertices[2][globalToVertex[i]] = edgeInterp[2][i];
        for (int j=0; j<naux; j++) {
          auxLevels[j][globalToVertex[i]] = auxInterp[j][i];
        }
      }
    }


    if (DEBUG) {
      System.out.println("\nfieldVertices " + nvertex + "\n");
      for (int i=0; i<nvertex; i++) {
        String s = "  " + i + " -> ";
        for (int j=0; j<3; j++) {
          s = s + " " + fieldVertices[j][i];
        }
        System.out.println(s + "\n");
      }
    }

  }

/*
  make_normals and poly_triangle_stripe altered according to:
  Pol_f_Vert[9*i + 8]   --> vertToPoly[i].length
  Pol_f_Vert[9*i + off] --> vertToPoly[i][off]
  Vert_f_Pol[7*i + 6]   --> polyToVert[i].length
  Vert_f_Pol[7*i + off] --> polyToVert[i][off]
*/

  /* from Contour3D.java, used by make_normals */
  static final float  EPS_0 = (float) 1.0e-5;
 
/* copied from Contour3D.java */
  private static void make_normals(float[] VX, float[] VY, float[] VZ,
                   float[] NX, float[] NY, float[] NZ, int nvertex,
                   int npolygons, float[] Pnx, float[] Pny, float[] Pnz,
                   float[] NxA, float[] NxB, float[] NyA, float[] NyB,
                   float[] NzA, float[] NzB,
                   int[][] vertToPoly, int[][] polyToVert)
/* WLH 25 Oct 97
                   int[] Pol_f_Vert, int[] Vert_f_Pol )
*/
          throws VisADException {

   int   i, k,  n;
   int   i1, i2, ix, iy, iz, ixb, iyb, izb;
   int   max_vert_per_pol, swap_flag;
   float x, y, z, a, minimum_area, len;

   int iv[] = new int[3];
   if (nvertex <= 0) return;


   for (i = 0; i < nvertex; i++) {
      NX[i] = 0;
      NY[i] = 0;
      NZ[i] = 0;
   }

   minimum_area = (float) ((1.e-4 > EPS_0) ? 1.e-4 : EPS_0);

   /* Calculate maximum number of vertices per polygon */
/* WLH 25 Oct 97
   k = 6;
   n = 7*npolygons;
*/
   k = 0;
   while ( true ) {
/* WLH 25 Oct 97
       for (i=k+7; i<n; i+=7)
           if (Vert_f_Pol[i] > Vert_f_Pol[k]) break;
*/
       for (i=k+1; i<npolygons; i++)
           if (polyToVert[i].length > polyToVert[k].length) break;

/* WLH 25 Oct 97
       if (i >= n) break;
*/
       if (i >= npolygons) break;
       k = i;
   }
/* WLH 25 Oct 97
   max_vert_per_pol = Vert_f_Pol[k];
*/
   max_vert_per_pol = polyToVert[k].length;

   /* Calculate the Normals vector components for each Polygon */
   for ( i=0; i<npolygons; i++) {  /* Vectorized */
/* WLH 25 Oct 97
      if (Vert_f_Pol[6+i*7]>0) {
         NxA[i] = VX[Vert_f_Pol[1+i*7]] - VX[Vert_f_Pol[0+i*7]];
         NyA[i] = VY[Vert_f_Pol[1+i*7]] - VY[Vert_f_Pol[0+i*7]];
         NzA[i] = VZ[Vert_f_Pol[1+i*7]] - VZ[Vert_f_Pol[0+i*7]];
      }
*/
      if (polyToVert[i].length>0) {
         int j1 = polyToVert[i][1];
         int j0 = polyToVert[i][0];
         NxA[i] = VX[j1] - VX[j0];
         NyA[i] = VY[j1] - VY[j0];
         NzA[i] = VZ[j1] - VZ[j0];
      }
   }

   swap_flag = 0;
   for ( k = 2; k < max_vert_per_pol; k++ ) {

      if (swap_flag==0) {
         /*$dir no_recurrence */        /* Vectorized */
         for ( i=0; i<npolygons; i++ ) {
/* WLH 25 Oct 97
            if ( Vert_f_Pol[k+i*7] >= 0 ) {
               NxB[i]  = VX[Vert_f_Pol[k+i*7]] - VX[Vert_f_Pol[0+i*7]];
               NyB[i]  = VY[Vert_f_Pol[k+i*7]] - VY[Vert_f_Pol[0+i*7]];
               NzB[i]  = VZ[Vert_f_Pol[k+i*7]] - VZ[Vert_f_Pol[0+i*7]];
*/
            if ( k < polyToVert[i].length ) {
               int jk = polyToVert[i][k];
               int j0 = polyToVert[i][0];
               NxB[i]  = VX[jk] - VX[j0];
               NyB[i]  = VY[jk] - VY[j0];
               NzB[i]  = VZ[jk] - VZ[j0];
               Pnx[i] = NyA[i]*NzB[i] - NzA[i]*NyB[i];
               Pny[i] = NzA[i]*NxB[i] - NxA[i]*NzB[i];
               Pnz[i] = NxA[i]*NyB[i] - NyA[i]*NxB[i];
               NxA[i] = Pnx[i]*Pnx[i] + Pny[i]*Pny[i] + Pnz[i]*Pnz[i];
               if (NxA[i] > minimum_area) {
                  Pnx[i] /= NxA[i];
                  Pny[i] /= NxA[i];
                  Pnz[i] /= NxA[i];
               }
            }
         }
      }
      else {  /* swap_flag!=0 */
         /*$dir no_recurrence */        /* Vectorized */
         for (i=0; i<npolygons; i++) {
/* WLH 25 Oct 97
            if ( Vert_f_Pol[k+i*7] >= 0 ) {
               NxA[i]  = VX[Vert_f_Pol[k+i*7]] - VX[Vert_f_Pol[0+i*7]];
               NyA[i]  = VY[Vert_f_Pol[k+i*7]] - VY[Vert_f_Pol[0+i*7]];
               NzA[i]  = VZ[Vert_f_Pol[k+i*7]] - VZ[Vert_f_Pol[0+i*7]];
*/
            if ( k < polyToVert[i].length ) {
               int jk = polyToVert[i][k];
               int j0 = polyToVert[i][0];
               NxA[i]  = VX[jk] - VX[j0];
               NyA[i]  = VY[jk] - VY[j0];
               NzA[i]  = VZ[jk] - VZ[j0];
               Pnx[i] = NyB[i]*NzA[i] - NzB[i]*NyA[i];
               Pny[i] = NzB[i]*NxA[i] - NxB[i]*NzA[i];
               Pnz[i] = NxB[i]*NyA[i] - NyB[i]*NxA[i];
               NxB[i] = Pnx[i]*Pnx[i] + Pny[i]*Pny[i] + Pnz[i]*Pnz[i];
               if (NxB[i] > minimum_area) {
                  Pnx[i] /= NxB[i];
                  Pny[i] /= NxB[i];
                  Pnz[i] /= NxB[i];
               }
            }
         } // end for (i=0; i<npolygons; i++)
      } // end swap_flag!=0

       /* This Loop <CAN'T> be Vectorized */
      for ( i=0; i<npolygons; i++ ) {
/* WLH 25 Oct 97
         if (Vert_f_Pol[k+i*7] >= 0) {
            iv[0] = Vert_f_Pol[0+i*7];
            iv[1] = Vert_f_Pol[(k-1)+i*7];
            iv[2] = Vert_f_Pol[k+i*7];
*/
         if (k < polyToVert[i].length) {
            iv[0] = polyToVert[i][0];
            iv[1] = polyToVert[i][k-1];
            iv[2] = polyToVert[i][k];
              x = Pnx[i];   y = Pny[i];   z = Pnz[i];

/*
System.out.println("vertices: " + iv[0] + " " + iv[1] + " " + iv[2]);
System.out.println("  normal: " + x + " " + y + " " + z + "\n");
*/

            // Update the origin vertex
               NX[iv[0]] += x;   NY[iv[0]] += y;   NZ[iv[0]] += z;

            // Update the vertex that defines the first vector
               NX[iv[1]] += x;   NY[iv[1]] += y;   NZ[iv[1]] += z;

            // Update the vertex that defines the second vector
               NX[iv[2]] += x;   NY[iv[2]] += y;   NZ[iv[2]] += z;
         }
      } // end for ( i=0; i<npolygons; i++ )

       swap_flag = ( (swap_flag != 0) ? 0 : 1 );
   } // end for ( k = 2; k < max_vert_per_pol; k++ )
 
    /* Normalize the Normals */
    for ( i=0; i<nvertex; i++ ) {  /* Vectorized */
        len = (float) Math.sqrt(NX[i]*NX[i] + NY[i]*NY[i] + NZ[i]*NZ[i]);
        if (len > EPS_0) {
            NX[i] /= len;
            NY[i] /= len;
            NZ[i] /= len;
        }
    }

  }

  /* from Contour3D.java, used by poly_triangle_stripe */
  static final int NTAB[] =
  {   0,1,2,       1,2,0,       2,0,1,
      0,1,3,2,     1,2,0,3,     2,3,1,0,     3,0,2,1,
      0,1,4,2,3,   1,2,0,3,4,   2,3,1,4,0,   3,4,2,0,1,   4,0,3,1,2,
      0,1,5,2,4,3, 1,2,0,3,5,4, 2,3,1,4,0,5, 3,4,2,5,1,0, 4,5,3,0,2,1,
      5,0,4,1,3,2
  };
 
  /* from Contour3D.java, used by poly_triangle_stripe */
  static final int ITAB[] =
  {   0,2,1,       1,0,2,       2,1,0,
      0,3,1,2,     1,0,2,3,     2,1,3,0,     3,2,0,1,
      0,4,1,3,2,   1,0,2,4,3,   2,1,3,0,4,   3,2,4,1,0,   4,3,0,2,1,
      0,5,1,4,2,3, 1,0,2,5,3,4, 2,1,3,0,4,5, 3,2,4,1,5,0, 4,3,5,2,0,1,
      5,4,0,3,1,2
  };
 
  /* from Contour3D.java, used by poly_triangle_stripe */
  static final int STAB[] =  { 0, 9, 25, 50 };

/* copied from Contour3D.java */
  static int poly_triangle_stripe( int[] Tri_Stripe,
                                   int nvertex, int npolygons,
                                   int[][] vertToPoly, int[][] polyToVert)
/* WLH 25 Oct 97
                            int[] Pol_f_Vert, int[] Vert_f_Pol )
*/
          throws VisADException {
   int  i, j, k, m, ii, npol, cpol, idx, off, Nvt,
        vA, vB, ivA, ivB, iST, last_pol;
   boolean f_line_conection = false;

   boolean[] vet_pol = new boolean[npolygons];

    last_pol = 0;
    npol = 0;
    iST = 0;
    ivB = 0;

    for (i=0; i<npolygons; i++) vet_pol[i] = true;  /* Vectorized */

    while (true)
    {
        /* find_unselected_pol(cpol); */
        for (cpol=last_pol; cpol<npolygons; cpol++) {
           if ( vet_pol[cpol] ) break;
        }
        if (cpol == npolygons) {
            cpol = -1;
        }
        else {
            last_pol = cpol;
        }
        /* end  find_unselected_pol(cpol); */

        if (cpol < 0) break;
/*      update_polygon            */
        vet_pol[cpol] = false;
/* end     update_polygon            */

/*      get_vertices_of_pol(cpol,Vt,Nvt); {    */
/* WLH 25 Oct 97
            Nvt = Vert_f_Pol[(j=cpol*7)+6];
            off = j;
*/
            Nvt = polyToVert[cpol].length;
/*      }                                      */
/* end      get_vertices_of_pol(cpol,Vt,Nvt); {    */


        for (ivA=0; ivA<Nvt; ivA++) {
            ivB = (((ivA+1)==Nvt) ? 0:(ivA+1));
/*          get_pol_vert(Vt[ivA],Vt[ivB],npol) { */
               npol = -1;
/* WLH 25 Oct 97
               if (Vert_f_Pol[ivA+off]>=0 && Vert_f_Pol[ivB+off]>=0) {
                  i=Vert_f_Pol[ivA+off]*9;
                  k=i+Pol_f_Vert [i+8];
                  j=Vert_f_Pol[ivB+off]*9;
                  m=j+Pol_f_Vert [j+8];
                  while (i>0 && j>0 && i<k && j <m ) {
                     if (Pol_f_Vert [i] == Pol_f_Vert [j] &&
                         vet_pol[Pol_f_Vert[i]] ) {
                        npol=Pol_f_Vert [i];
                        break;
                     }
                     else if (Pol_f_Vert [i] < Pol_f_Vert [j])
                          i++;
                     else
                          j++;
                  }
               }
*/
               if (ivA < polyToVert[cpol].length && ivB < polyToVert[cpol].length) {
                  i=polyToVert[cpol][ivA];
                  int ilim = vertToPoly[i].length;
                  k = 0;
                  j=polyToVert[cpol][ivB];
                  int jlim = vertToPoly[j].length;
                  m = 0;
                  // while (0<k && k<ilim && 0<m && m<jlim) {
                  while (0<i && k<ilim && 0<j && m<jlim) {
                     if (vertToPoly[i][k] == vertToPoly[j][m] &&
                         vet_pol[vertToPoly[i][k]] ) {
                        npol=vertToPoly[i][k];
                        break;
                     }
                     else if (vertToPoly[i][k] < vertToPoly[j][m])
                          k++;
                     else
                          m++;
                  }
               }

/*          }                                   */
/* end          get_pol_vert(Vt[ivA],Vt[ivB],npol) { */
            if (npol >= 0) break;
        }
        /* insert polygon alone */
        if (npol < 0)
        { /*ptT = NTAB + STAB[Nvt-3];*/
            idx = STAB[Nvt-3];
            if (iST > 0)
            {   Tri_Stripe[iST]   = Tri_Stripe[iST-1];    iST++;
/* WLH 25 Oct 97
                Tri_Stripe[iST++] = Vert_f_Pol[NTAB[idx]+off];
*/
                Tri_Stripe[iST++] = polyToVert[cpol][NTAB[idx]];
            }
            else f_line_conection = true; /* WLH 3-9-95 added */
            for (ii=0; ii< ((Nvt < 6) ? Nvt:6); ii++) {
/* WLH 25 Oct 97
                Tri_Stripe[iST++] = Vert_f_Pol[NTAB[idx++]+off];
*/
                Tri_Stripe[iST++] = polyToVert[cpol][NTAB[idx++]];
              }
            continue;
        }

        if (( (ivB != 0) && ivA==(ivB-1)) || ( !(ivB != 0) && ivA==Nvt-1)) {
         /* ptT = ITAB + STAB[Nvt-3] + (ivB+1)*Nvt; */
            idx = STAB[Nvt-3] + (ivB+1)*Nvt;

            if (f_line_conection)
            {   Tri_Stripe[iST]   = Tri_Stripe[iST-1];    iST++;
/* WLH 25 Oct 97
                Tri_Stripe[iST++] = Vert_f_Pol[ITAB[idx-1]+off];
*/
                Tri_Stripe[iST++] = polyToVert[cpol][ITAB[idx-1]];
                f_line_conection = false;
            }
            for (ii=0; ii<((Nvt < 6) ? Nvt:6); ii++) {
/* WLH 25 Oct 97
                Tri_Stripe[iST++] = Vert_f_Pol[ITAB[--idx]+off];
*/
                Tri_Stripe[iST++] = polyToVert[cpol][ITAB[--idx]];
            }

        }
        else {
         /* ptT = NTAB + STAB[Nvt-3] + (ivB+1)*Nvt; */
            idx = STAB[Nvt-3] + (ivB+1)*Nvt;

            if (f_line_conection)
            {   Tri_Stripe[iST]   = Tri_Stripe[iST-1];    iST++;
/* WLH 25 Oct 97
                Tri_Stripe[iST++] = Vert_f_Pol[NTAB[idx-1]+off];
*/
                Tri_Stripe[iST++] = polyToVert[cpol][NTAB[idx-1]];
                f_line_conection = false;
            }
            for (ii=0; ii<((Nvt < 6) ? Nvt:6); ii++) {
/* WLH 25 Oct 97
                Tri_Stripe[iST++] = Vert_f_Pol[NTAB[--idx]+off];
*/
                Tri_Stripe[iST++] = polyToVert[cpol][NTAB[--idx]];
            }

        }

        vB = Tri_Stripe[iST-1];
        vA = Tri_Stripe[iST-2];
        cpol = npol;

        while (true)
        {
/*          get_vertices_of_pol(cpol,Vt,Nvt)  {   */
/* WLH 25 Oct 97
                Nvt = Vert_f_Pol [(j=cpol*7)+6];
                off = j;
*/
                Nvt = polyToVert[cpol].length;
/*          }                                     */


/*          update_polygon(cpol)                  */
            vet_pol[cpol] = false;
/* WLH 25 Oct 97
            for (ivA=0; ivA<Nvt && Vert_f_Pol[ivA+off]!=vA; ivA++);
            for (ivB=0; ivB<Nvt && Vert_f_Pol[ivB+off]!=vB; ivB++);
*/
            for (ivA=0; ivA<Nvt && polyToVert[cpol][ivA]!=vA; ivA++);
            for (ivB=0; ivB<Nvt && polyToVert[cpol][ivB]!=vB; ivB++);
                 if (( (ivB != 0) && ivA==(ivB-1)) || (!(ivB != 0) && ivA==Nvt-1)) {
                /* ptT = NTAB + STAB[Nvt-3] + ivA*Nvt + 2; */
                    idx = STAB[Nvt-3] + ivA*Nvt + 2;

                    for (ii=2; ii<((Nvt < 6) ? Nvt:6); ii++) {
/* WLH 25 Oct 97
                        Tri_Stripe[iST++] = Vert_f_Pol[NTAB[idx++]+off];
*/
                        Tri_Stripe[iST++] = polyToVert[cpol][NTAB[idx++]];
                    }
                 }
                 else {
                /*  ptT = ITAB + STAB[Nvt-3] + ivA*Nvt + 2; */
                    idx = STAB[Nvt-3] + ivA*Nvt + 2;

                    for (ii=2; ii<((Nvt < 6) ? Nvt:6); ii++) {
/* WLH 25 Oct 97
                        Tri_Stripe[iST++] = Vert_f_Pol[ITAB[idx++]+off];
*/
                        Tri_Stripe[iST++] = polyToVert[cpol][ITAB[idx++]];
                    }
                 }

            vB = Tri_Stripe[iST-1];
            vA = Tri_Stripe[iST-2];

/*          get_pol_vert(vA,vB,cpol) {     */
               cpol = -1;
/* WLH 25 Oct 97
               if (vA>=0 && vB>=0) {
                 i=vA*9;
                 k=i+Pol_f_Vert [i+8];
                 j=vB*9;
                 m=j+Pol_f_Vert [j+8];
                 while (i>0 && j>0 && i<k && j<m) {
                    if (Pol_f_Vert [i] == Pol_f_Vert [j] &&
                        vet_pol[Pol_f_Vert[i]] ) {
                      cpol=Pol_f_Vert[i];
                      break;
                    }
                    else if (Pol_f_Vert [i] < Pol_f_Vert [j])
                      i++;
                    else
                      j++;
                 }
               }
*/
               if (vA>=0 && vB>=0) {
                 int ilim = vertToPoly[vA].length;
                 k = 0;
                 int jlim = vertToPoly[vB].length;
                 m = 0;
                 // while (0<k && k<ilim && 0<m && m<jlim) {
                 while (0<vA && k<ilim && 0<vB && m<jlim) {
                    if (vertToPoly[vA][k] == vertToPoly[vB][m] &&
                        vet_pol[vertToPoly[vA][k]] ) {
                      cpol=vertToPoly[vA][k];
                      break;
                    }
                    else if (vertToPoly[vA][k] < vertToPoly[vB][m])
                      k++;
                    else
                      m++;
                 }
               }

/*         }                               */

            if (cpol < 0) {

                vA = Tri_Stripe[iST-3];
/*          get_pol_vert(vA,vB,cpol) {   */
               cpol = -1;
/* WLH 25 Oct 97
               if (vA>=0 && vB>=0) {
                 i=vA*9;
                 k=i+Pol_f_Vert [i+8];
                 j=vB*9;
                 m=j+Pol_f_Vert [j+8];
                 while (i>0 && j>0 && i<k && j<m) {
                    if (Pol_f_Vert [i] == Pol_f_Vert [j] &&
                        vet_pol[Pol_f_Vert[i]] ) {
                      cpol=Pol_f_Vert[i];
                      break;
                    }
                    else if (Pol_f_Vert [i] < Pol_f_Vert [j])
                      i++;
                    else
                      j++;
                 }
               }
*/
               if (vA>=0 && vB>=0) {
                 int ilim = vertToPoly[vA].length;
                 k = 0;
                 int jlim = vertToPoly[vB].length;
                 m = 0;
                 // while (0<k && k<ilim && 0<m && m<jlim) {
                 while (0<vA && k<ilim && 0<vB && m<jlim) {
                    if (vertToPoly[vA][k] == vertToPoly[vB][m] &&
                        vet_pol[vertToPoly[vA][k]] ) {
                      cpol=vertToPoly[vA][k];
                      break;
                    }
                    else if (vertToPoly[vA][k] < vertToPoly[vB][m])
                      k++;
                    else
                      m++;
                 }
               }

/*          }                            */
                if (cpol < 0) {
                    f_line_conection  = true;
                    break;
                }
                else {
                    Tri_Stripe[iST++] = vA;
                    i = vA;
                    vA = vB;
                    vB = i;
                }
            }
        }
    }

    return iST;
  }

  /** create a 2-D GeometryArray from this Set and color_values */
  public VisADGeometryArray make2DGeometry(float[][] color_values)
         throws VisADException {
    if (DomainDimension != 3) {
      throw new SetException("Irregular3DSet.make2DGeometry: " +
                              "DomainDimension must be 3");
    }
    if (ManifoldDimension != 2) {
      throw new SetException("Irregular3DSet.make2DGeometry: " +
                              "ManifoldDimension must be 2");
    }

    int npolygons = Delan.Tri.length;
    int nvertex = Delan.Vertices.length;
    if (npolygons < 1 || nvertex < 3) return null;

    // make sure all triangles have the same signature
    // i.e., winding direction
    int[][] Tri = Delan.Tri;
    int[][] Walk = Delan.Walk;
    int dim = Tri[0].length - 1;
    int[][] tri = new int[npolygons][];
    int[] poly_stack = new int[npolygons];
    int[] walk_stack = new int[npolygons];
    int sp; // stack pointer
    for (int ii=0; ii<npolygons; ii++) {
      // find an un-adjusted triangle
      if (tri[ii] == null) {
        // initialize its signature
        tri[ii] = new int[3];
        tri[ii][0] = Tri[ii][0];
        tri[ii][1] = Tri[ii][1];
        tri[ii][2] = Tri[ii][2];
        // first stack entry, for recursive search of triangles
        // via Walk array
        sp = 0;
        walk_stack[sp] = 0;
        poly_stack[sp] = ii;
        while (true) {
          // find neighbor triangle via Walk
          int i = poly_stack[sp];
          int w = walk_stack[sp];
          int j = Walk[i][w];
          if (j >= 0 && tri[j] == null) {
            // compare signatures of neighbors
            int v1 = Tri[i][w];
            int v2 = Tri[i][(w + 1) % 3];
            int i1 = -1;
            int i2 = -1;
            int j1 = -1;
            int j2 = -1;
            for (int k=0; k<3; k++) {
              if (tri[i][k] == v1) i1 = k;
              if (tri[i][k] == v2) i2 = k;
              if (Tri[j][k] == v1) j1 = k;
              if (Tri[j][k] == v2) j2 = k;
            }
            tri[j] = new int[3];
            tri[j][0] = Tri[j][0];
            if ( ( (((i1 + 1) % 3) == i2) && (((j1 + 1) % 3) == j2) ) ||
                 ( (((i2 + 1) % 3) == i1) && (((j2 + 1) % 3) == j1) ) ) {
              tri[j][1] = Tri[j][2];
              tri[j][2] = Tri[j][1];
            }
            else {
              tri[j][1] = Tri[j][1];
              tri[j][2] = Tri[j][2];
            }
            // add j to stack
            sp++;
            walk_stack[sp] = 0;
            poly_stack[sp] = j;
          }
          else { // (j < 0 || tri[j] != null)
            while (true) {
              walk_stack[sp]++;
              if (walk_stack[sp] < 3) {
                break;
              }
              else {
                sp--;
                if (sp < 0) break;
              }
            } // end while (true)
          } // end if (j < 0 || tri[j] != null)
          if (sp < 0) break;
        } // end while (true)
      } // end if (tri[ii] == null) 
    } // end for (int ii=0; ii<npolygons; ii++)

    float[][] samples = getSamples(false);
    float[] NxA = new float[npolygons];
    float[] NxB = new float[npolygons];
    float[] NyA = new float[npolygons];
    float[] NyB = new float[npolygons];
    float[] NzA = new float[npolygons];
    float[] NzB = new float[npolygons];
    float[] Pnx = new float[npolygons];
    float[] Pny = new float[npolygons];
    float[] Pnz = new float[npolygons];
    float[] NX = new float[nvertex];
    float[] NY = new float[nvertex];
    float[] NZ = new float[nvertex];

    make_normals(samples[0], samples[1], samples[2],
                 NX, NY, NZ, nvertex, npolygons, Pnx, Pny, Pnz,
                 NxA, NxB, NyA, NyB, NzA, NzB, Delan.Vertices, tri);
                 // NxA, NxB, NyA, NyB, NzA, NzB, Delan.Vertices, Delan.Tri);

    // take the garbage out
    NxA = NxB = NyA = NyB = NzA = NzB = Pnx = Pny = Pnz = null;
 
    float[] normals = new float[3 * nvertex];
    int j = 0;
    for (int i=0; i<nvertex; i++) {
      normals[j++] = (float) NX[i];
      normals[j++] = (float) NY[i];
      normals[j++] = (float) NZ[i];
    }
    // take the garbage out
    NX = NY = NZ = null;

    VisADIndexedTriangleStripArray array =
      new VisADIndexedTriangleStripArray();
 
    // array.vertexFormat |= NORMALS;
    array.normals = normals;
    // take the garbage out
    normals = null;

    // temporary array to hold maximum possible polytriangle strip
    int[] stripe = new int[6 * npolygons];
    int size_stripe =
      poly_triangle_stripe(stripe, nvertex, npolygons,
                           Delan.Vertices, Delan.Tri);
 
    // set up indices
    array.indexCount = size_stripe;
    array.indices = new int[size_stripe];
    System.arraycopy(stripe, 0, array.indices, 0, size_stripe);
    array.stripVertexCounts = new int[1];
    array.stripVertexCounts[0] = size_stripe;
    // take the garbage out
    stripe = null;

    // set coordinates and colors
    setGeometryArray(array, samples, 4, color_values);
    // take the garbage out
    samples = null;

    return array;
  }

  public Object clone() {
    try {
      if (ManifoldDimension == 1) {
        return new Irregular3DSet(Type, Samples, newToOld, oldToNew,
                              DomainCoordinateSystem, SetUnits, SetErrors);
      }
      else {
        return new Irregular3DSet(Type, Samples, DomainCoordinateSystem,
                                  SetUnits, SetErrors, Delan);
      }
    }
    catch (VisADException e) {
      throw new VisADError("Irregular3DSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    if (ManifoldDimension == 1) {
      return new Irregular3DSet(type, Samples, newToOld, oldToNew,
                            DomainCoordinateSystem, SetUnits, SetErrors);
    }
    else {
      return new Irregular3DSet(type, Samples, DomainCoordinateSystem,
                                SetUnits, SetErrors, Delan);
    }
  }

  /* run 'java visad.Irregular3DSet' to test the Irregular3DSet class */
  public static void main(String[] argv) throws VisADException {
    float[][] samp = { {179, 232, 183, 244, 106, 344, 166, 304, 286},
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
    float[][] value = { {189, 221, 319, 215, 196},
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
    float[][] weights = new float[value[0].length][];
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
    float[] field = {100, 300, 320, 250, 80, 70, 135, 110, 105};
    float[][] slice = new float[3][];
    int[][][] polyvert = new int[1][][];
    int[][][] vertpoly = new int[1][][];
    iSet3D.makeIsosurface(288, field, null, slice, null, polyvert, vertpoly);
    for (int i=0; i<slice[0].length; i++) {
      for (int j=0; j<3; j++) {
        slice[j][i] = (float) Math.round(1000*slice[j][i]) / 1000;
      }
    }
    System.out.println("polygons:");
    for (int i=0; i<polyvert[0].length; i++) {
      System.out.print("#"+i+":");
/* WLH 25 Oct 97
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
*/
      for (int j=0; j<polyvert[0][i].length; j++) {
        if (j == 1) {
          if (polyvert[0][i].length == 3) {
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
    System.out.println();
    for (int i=0; i<polyvert[0].length; i++) {
      int a = polyvert[0][i][0];
      int b = polyvert[0][i][1];
      int c = polyvert[0][i][2];
      int d = polyvert[0][i].length==4 ? polyvert[0][i][3] : -1;
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
#2:     225.127, 228.382, 291.291
(tri)   227.2, 236.6, 292.709
        235.323, 222.262, 291.215
#3:     228.82, 222.3, 290.2
(quad)  182.418, 142.4, 313.273
        235.323, 222.262, 291.215
        198.33, 142.623, 315.637
#4:     234.88, 205.08, 313.24
(tri)   237.843, 226.93, 291.817
        235.323, 222.262, 291.215
#5:     182.418, 142.4, 313.273
(tri)   210.886, 138.743, 348.743
        198.33, 142.623, 315.637
#6:     234.88, 205.08, 313.24
(quad)  235.323, 222.262, 291.215
        210.886, 138.743, 348.743
        198.33, 142.623, 315.637
#7:     225.127, 228.382, 291.291
(quad)  227.2, 236.6, 292.709
        172.733, 156.133, 316.267
        180.059, 178.984, 318.497
#8:     234.88, 205.08, 313.24
(tri)   237.843, 226.93, 291.817
        236.547, 236.937, 288.368
#9:     228.82, 222.3, 290.2
(tri)   225.127, 228.382, 291.291
        235.323, 222.262, 291.215
#10:    237.843, 226.93, 291.817
(tri)   227.2, 236.6, 292.709
        235.323, 222.262, 291.215

iris 46%

*/

}

