
//
// Irregular2DSet.java
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
   Irregular2DSet represents a finite set of samples of R^2.<P>

   No Irregular2DSet with ManifoldDimension = 1.  Use
   Gridded2DSet with ManifoldDimension = 1 instead.<P>
*/
public class Irregular2DSet extends IrregularSet {

  private float LowX, HiX, LowY, HiY;

  public Irregular2DSet(MathType type, float[][] samples)
         throws VisADException {
    this(type, samples, null, null, null);
  }

  public Irregular2DSet(MathType type, float[][] samples,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, coord_sys, units, errors, true);
  }

  Irregular2DSet(MathType type, float[][] samples,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors, boolean copy)
         throws VisADException {
    super(type, samples, samples.length, coord_sys, units, errors, copy);
    if (samples.length != 2) {
      throw new SetException("Irregular2DSet: ManifoldDimension " +
                             "must be 2 for this constructor");
    }
    Delan = new Delaunay(Samples);
    LowX = Low[0];
    HiX = Hi[0];
    LowY = Low[1];
    HiY = Hi[1];
    oldToNew = null;
    newToOld = null;
  }

  /** construct Irregular2DSet using Delaunay from existing  
      Irregular2DSet */ 
  public Irregular2DSet(MathType type, float[][] samples,
                 Irregular2DSet delaunay_set) throws VisADException {
    this(type, samples, delaunay_set, null, null, null, true);
  }
 
  /** construct Irregular2DSet using Delaunay from existing 
      Irregular2DSet */
  public Irregular2DSet(MathType type, float[][] samples,
                        Irregular2DSet delaunay_set,
                        CoordinateSystem coord_sys, Unit[] units,
                        ErrorEstimate[] errors) throws VisADException {
    this(type, samples, delaunay_set, coord_sys, units, errors, true);
  }

  Irregular2DSet(MathType type, float[][] samples,
                 Irregular2DSet delaunay_set,
                 CoordinateSystem coord_sys, Unit[] units,
                 ErrorEstimate[] errors, boolean copy)
                 throws VisADException {
    super(type, samples, delaunay_set.getManifoldDimension(), 
          coord_sys, units, errors, copy);
    int dim = delaunay_set.getManifoldDimension();
    if (dim != 2) {
      throw new SetException("Irregular2DSet: delaunay_set ManifoldDimension " +
                             "must be 2");
    }
    if (Length != delaunay_set.Length) {
      throw new SetException("Irregular2DSet: delaunay_set length not match");
    }
    Delan = delaunay_set.Delan;
    LowX = Low[0];
    HiX = Hi[0];
    LowY = Low[1];
    HiY = Hi[1];
    oldToNew = null;
    newToOld = null;
  }

  /** construct Irregular2DSet using sort from existing
      Irregular1DSet */
  public Irregular2DSet(MathType type, float[][] samples,
               int[] new2old, int[] old2new) throws VisADException {
    this(type, samples, new2old, old2new, null, null, null, true);
  }
 
  /** construct Irregular2DSet using sort from existing
      Irregular1DSet */
  public Irregular2DSet(MathType type, float[][] samples,
                        int[] new2old, int[] old2new,
                        CoordinateSystem coord_sys, Unit[] units,
                        ErrorEstimate[] errors) throws VisADException {
    this(type, samples, new2old, old2new, coord_sys, units, errors, true);
  }

  Irregular2DSet(MathType type, float[][] samples,
                 int[] new2old, int[] old2new,
                 CoordinateSystem coord_sys, Unit[] units,
                 ErrorEstimate[] errors, boolean copy)
                 throws VisADException {
    super(type, samples, 1, coord_sys, units, errors, copy);
    if (Length != new2old.length || Length != old2new.length) {
      throw new SetException("Irregular2DSet: sort length not match");
    }
    newToOld = new int[Length];
    oldToNew = new int[Length];
    System.arraycopy(new2old, 0, newToOld, 0, Length);
    System.arraycopy(old2new, 0, oldToNew, 0, Length);
    LowX = Low[0];
    HiX = Hi[0];
    LowY = Low[1];
    HiY = Hi[1];
    Delan = null;
  }

  Set makeSpatial(SetType type, float[][] samples) throws VisADException {
    if (samples.length == 3) {
      if (ManifoldDimension == 1) {
        return new Irregular3DSet(type, samples, newToOld, oldToNew,
                                  null, null, null, false);
      }
      else {
        return new Irregular3DSet(type, samples, this,
                                  null, null, null, false);
      }
    }
    else if (samples.length == 2) {
      if (ManifoldDimension == 1) {
        return new Irregular2DSet(type, samples, newToOld, oldToNew,
                                  null, null, null, false);
      }
      else {
        return new Irregular2DSet(type, samples, this,
                                  null, null, null, false);
      }
    }
    else {
      throw new SetException("Irregular2DSet.makeSpatial: bad samples length");
    }
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    float[][] value = new float[2][index.length];
    for (int i=0; i<index.length; i++) {
      if ( (index[i] >= 0) && (index[i] < Length) ) {
        value[0][i] = Samples[0][index[i]];
        value[1][i] = Samples[1][index[i]];
      }
      else {
        value[0][i] = value[1][i] = Float.NaN;
      }
    }
    return value;
  }

  /* a private method used by valueToIndex and valueToInterp,
     valueToTri returns an array of containing triangles given
     an array of points in R^DomainDimension */
  private int[] valueToTri(float[][] value) throws VisADException {
    if (ManifoldDimension != 2) {
      throw new SetException("Irregular2DSet.valueToTri: " +
                             "ManifoldDimension must be 2");
    }
    int length = value[0].length;
    if (length != value[1].length) {
      throw new SetException("Irregular2DSet.valueToTri: lengths " +
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
        float Ax = Samples[0][t0];
        float Ay = Samples[1][t0];
        float Bx = Samples[0][t1];
        float By = Samples[1][t1];
        float Cx = Samples[0][t2];
        float Cy = Samples[1][t2];
        float Px = value[0][i];
        float Py = value[1][i];

        // tests whether point is contained in current triangle
        float tval1 = (Bx-Ax)*(Py-Ay) - (By-Ay)*(Px-Ax);
        float tval2 = (Cx-Bx)*(Py-By) - (Cy-By)*(Px-Bx);
        float tval3 = (Ax-Cx)*(Py-Cy) - (Ay-Cy)*(Px-Cx);
        boolean test1 = (tval1 == 0) || ( (tval1 > 0) == (
                        (Bx-Ax)*(Cy-Ay) - (By-Ay)*(Cx-Ax) > 0) );
        boolean test2 = (tval2 == 0) || ( (tval2 > 0) == ( 
                        (Cx-Bx)*(Ay-By) - (Cy-By)*(Ax-Bx) > 0) );
        boolean test3 = (tval3 == 0) || ( (tval3 > 0) == (
                        (Ax-Cx)*(By-Cy) - (Ay-Cy)*(Bx-Cx) > 0) );

        // figure out which triangle to go to next
        if (!test1) curtri = Delan.Walk[curtri][0];
        else if (!test2) curtri = Delan.Walk[curtri][1];
        else if (!test3) curtri = Delan.Walk[curtri][2];
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
      throw new SetException("Irregular2DSet.valueToIndex: bad dimension");
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

        // triangle indices
        int t = tri[i];
        int t0 = Delan.Tri[t][0];
        int t1 = Delan.Tri[t][1];
        int t2 = Delan.Tri[t][2];

        // partial distances
        float D00 = Samples[0][t0] - x;
        float D01 = Samples[1][t0] - y;
        float D10 = Samples[0][t1] - x;
        float D11 = Samples[1][t1] - y;
        float D20 = Samples[0][t2] - x;
        float D21 = Samples[1][t2] - y;

        // distances squared
        float Dsq0 = D00*D00 + D01*D01;
        float Dsq1 = D10*D10 + D11*D11;
        float Dsq2 = D20*D20 + D21*D21;

        // find the minimum distance
        float min = Math.min(Dsq0, Dsq1);
        min = Math.min(min, Dsq2);
        if (min == Dsq0) index[i] = t0;
        else if (min == Dsq1) index[i] = t1;
        else index[i] = t2;
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
      throw new SetException("Irregular2DSet.valueToInterp: bad dimension");
    }
    int length = value[0].length; // number of values
    if ( (indices.length < length) || (weights.length < length) ) {
      throw new SetException(
                       "Irregular2DSet.valueToInterp: lengths don't match");
    }
    int[] tri = valueToTri(value);
    for (int i=0; i<tri.length; i++) {
      if (tri[i] < 0) {
        indices[i] = null;
        weights[i] = null;
      }
      else {
        // indices and weights sub-arrays
        int[] ival = new int[3];
        float[] wval = new float[3];
        // current values
        float x = value[0][i];
        float y = value[1][i];

        // triangle indices
        int t = tri[i];
        int t0 = Delan.Tri[t][0];
        int t1 = Delan.Tri[t][1];
        int t2 = Delan.Tri[t][2];
        ival[0] = t0;
        ival[1] = t1;
        ival[2] = t2;

        // triangle vertices
        float x0 = Samples[0][t0];
        float y0 = Samples[1][t0];
        float x1 = Samples[0][t1];
        float y1 = Samples[1][t1];
        float x2 = Samples[0][t2];
        float y2 = Samples[1][t2];

        // perpendicular lines
        float C0x = y2-y1;
        float C0y = x1-x2;
        float C1x = y2-y0;
        float C1y = x0-x2;
        float C2x = y1-y0;
        float C2y = x0-x1;

        // weights
        wval[0] = ( ( (x - x1)*C0x) + ( (y - y1)*C0y) )
                / ( ((x0 - x1)*C0x) + ((y0 - y1)*C0y) );
        wval[1] = ( ( (x - x0)*C1x) + ( (y - y0)*C1y) )
                / ( ((x1 - x0)*C1x) + ((y1 - y0)*C1y) );
        wval[2] = ( ( (x - x0)*C2x) + ( (y - y0)*C2y) )
                / ( ((x2 - x0)*C2x) + ((y2 - y0)*C2y) );

        // fill in arrays
        indices[i] = ival;
        weights[i] = wval;
      }
    }
  }

  public Object clone() {
    try {
      if (ManifoldDimension == 1) {
        return new Irregular2DSet(Type, Samples, newToOld, oldToNew,
                              DomainCoordinateSystem, SetUnits, SetErrors);
      }
      else {
        return new Irregular2DSet(Type, Samples,
                              DomainCoordinateSystem, SetUnits, SetErrors);
      }
    }
    catch (VisADException e) {
      throw new VisADError("Irregular2DSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    if (ManifoldDimension == 1) {
      return new Irregular2DSet(type, Samples, newToOld, oldToNew,
                            DomainCoordinateSystem, SetUnits, SetErrors);
    }
    else {
      return new Irregular2DSet(type, Samples,
                            DomainCoordinateSystem, SetUnits, SetErrors);
    }
  }

  /* run 'java visad.Irregular2DSet' to test the Irregular2DSet class */
  public static void main(String[] argv) throws VisADException {
    float[][] samp = { {139, 357, 416, 276, 495, 395, 578, 199},
                        {102,  44, 306, 174, 108, 460, 333, 351} };
    RealType test1 = new RealType("x", null, null);
    RealType test2 = new RealType("y", null, null);
    RealType[] t_array = {test1, test2};
    RealTupleType t_tuple = new RealTupleType(t_array);
    Irregular2DSet iSet2D = new Irregular2DSet(t_tuple, samp);

    // print out Samples information
    System.out.println("Samples:");
    for (int i=0; i<iSet2D.Samples[0].length; i++) {
      System.out.println("#"+i+":\t"+iSet2D.Samples[0][i]
                               +", "+iSet2D.Samples[1][i]);
    }
    System.out.println();

    // test valueToIndex function
    System.out.println("valueToIndex test:");
    float[][] value = { {164, 287, 311, 417, 522, 366, 445},
                         {131, 323,  90, 264, 294, 421,  91} };
    int[] index = iSet2D.valueToIndex(value);
    for (int i=0; i<index.length; i++) {
      System.out.println(value[0][i]+", "
                        +value[1][i]+"\t--> #"+index[i]);
    }
    System.out.println();

    // test valueToInterp function
    System.out.println("valueToInterp test:");
    int[][] indices = new int[value[0].length][];
    float[][] weights = new float[value[0].length][];
    iSet2D.valueToInterp(value, indices, weights);
    for (int i=0; i<value[0].length; i++) {
      System.out.println(value[0][i]+", "+value[1][i]+"\t--> ["
                        +indices[i][0]+", "
                        +indices[i][1]+", "
                        +indices[i][2]+"]\tweight total: "
                       +(weights[i][0]+weights[i][1]+weights[i][2]));
    }
    System.out.println();

  }

/* Here's the output:

iris 136% java visad.Irregular2DSet
Samples:
#0:     139.0, 102.0
#1:     357.0, 44.0
#2:     416.0, 306.0
#3:     276.0, 174.0
#4:     495.0, 108.0
#5:     395.0, 460.0
#6:     578.0, 333.0
#7:     199.0, 351.0

valueToIndex test:
164.0, 131.0    --> #0
287.0, 323.0    --> #7
311.0, 90.0     --> #1
417.0, 264.0    --> #2
522.0, 294.0    --> #6
366.0, 421.0    --> #5
445.0, 91.0     --> #4

valueToInterp test:
164.0, 131.0    --> [0, 3, 7]   weight total: 1.0
287.0, 323.0    --> [2, 3, 7]   weight total: 1.0
311.0, 90.0     --> [0, 1, 3]   weight total: 1.0
417.0, 264.0    --> [2, 3, 4]   weight total: 1.0
522.0, 294.0    --> [2, 4, 6]   weight total: 1.0
366.0, 421.0    --> [2, 5, 7]   weight total: 1.0
445.0, 91.0     --> [1, 3, 4]   weight total: 1.0

iris 137% 

*/

}

