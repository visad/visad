
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
*/
public class Irregular2DSet extends IrregularSet {

  double LowX, HiX, LowY, HiY;
  Delaunay Delan;

  /*
   * Size of temporary vertex arrays:  It would be more efficient memory-
   * wise to dynamically allocate the vx,vy,ipnt arrays to size maxv1+maxv2
   * but it would also be slower.
   */
  private static PlotDigits plot = new PlotDigits();

  public Irregular2DSet(MathType type, double[][] samples)
         throws VisADException {
    this(type, samples, null, null, null);
  }

  public Irregular2DSet(MathType type, double[][] samples,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    super(type, samples, coord_sys,
          units, errors);
    Delan = new Delaunay(Samples);
    LowX = Low[0];
    HiX = Hi[0];
    LowY = Low[1];
    HiY = Hi[1];
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public double[][] indexToValue(int[] index) throws VisADException {
    double[][] value = new double[2][index.length];
    for (int i=0; i<index.length; i++)
      if ( (index[i] >= 0) && (index[i] < Samples[0].length) ) {
        value[0][i] = Samples[0][index[i]];
        value[1][i] = Samples[1][index[i]];
      }
      else
        value[0][i] = value[1][i] = Double.NaN;
    return value;
  }

  /* a private method used by valueToIndex and valueToInterp,
     valueToTri returns an array of containing triangles given
     an array of points in R^DomainDimension */
  private int[] valueToTri(double[][] value) throws VisADException {
    // avoid ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(value[0].length, value[1].length);
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
        double Ax = Samples[0][t0];
        double Ay = Samples[1][t0];
        double Bx = Samples[0][t1];
        double By = Samples[1][t1];
        double Cx = Samples[0][t2];
        double Cy = Samples[1][t2];
        double Px = value[0][i];
        double Py = value[1][i];

        // tests whether point is contained in current triangle
        double tval1 = (Bx-Ax)*(Py-Ay) - (By-Ay)*(Px-Ax);
        double tval2 = (Cx-Bx)*(Py-By) - (Cy-By)*(Px-Bx);
        double tval3 = (Ax-Cx)*(Py-Cy) - (Ay-Cy)*(Px-Cx);
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
  public int[] valueToIndex(double[][] value) throws VisADException {
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
        double x = value[0][i];
        double y = value[1][i];

        // triangle indices
        int t = tri[i];
        int t0 = Delan.Tri[t][0];
        int t1 = Delan.Tri[t][1];
        int t2 = Delan.Tri[t][2];

        // partial distances
        double D00 = Samples[0][t0] - x;
        double D01 = Samples[1][t0] - y;
        double D10 = Samples[0][t1] - x;
        double D11 = Samples[1][t1] - y;
        double D20 = Samples[0][t2] - x;
        double D21 = Samples[1][t2] - y;

        // distances squared
        double Dsq0 = D00*D00 + D01*D01;
        double Dsq1 = D10*D10 + D11*D11;
        double Dsq2 = D20*D20 + D21*D21;

        // find the minimum distance
        double min = Math.min(Dsq0, Dsq1);
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
  public void valueToInterp(double[][] value,
                            int[][] indices, double[][] weights)
                   throws VisADException {
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
        double[] wval = new double[3];
        // current values
        double x = value[0][i];
        double y = value[1][i];

        // triangle indices
        int t = tri[i];
        int t0 = Delan.Tri[t][0];
        int t1 = Delan.Tri[t][1];
        int t2 = Delan.Tri[t][2];
        ival[0] = t0;
        ival[1] = t1;
        ival[2] = t2;

        // triangle vertices
        double x0 = Samples[0][t0];
        double y0 = Samples[1][t0];
        double x1 = Samples[0][t1];
        double y1 = Samples[1][t1];
        double x2 = Samples[0][t2];
        double y2 = Samples[1][t2];

        // perpendicular lines
        double C0x = y2-y1;
        double C0y = x1-x2;
        double C1x = y2-y0;
        double C1y = x0-x2;
        double C2x = y1-y0;
        double C2y = x0-x1;

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
      return new Irregular2DSet(Type, Samples,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Irregular2DSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Irregular2DSet(type, Samples,
                            DomainCoordinateSystem, SetUnits, SetErrors);
  }

  /* run 'java visad.Irregular2DSet' to test the Irregular2DSet class */
  public static void main(String[] argv) throws VisADException {
    double[][] samp = { {139, 357, 416, 276, 495, 395, 578, 199},
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
    double[][] value = { {164, 287, 311, 417, 522, 366, 445},
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
    double[][] weights = new double[value[0].length][];
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

