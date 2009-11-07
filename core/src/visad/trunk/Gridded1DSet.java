//
// Gridded1DSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
   Gridded1DSet represents a finite set of samples of R.<P>
*/
public class Gridded1DSet extends GriddedSet implements Gridded1DSetIface {

  int LengthX;
  float LowX, HiX;

  /** Whether this set is ascending or descending */
  boolean Ascending;

  /**
   * A canonicalizing cache of previously-created instances.  Because instances
   * are immutable, a cache can be used to reduce memory usage by ensuring
   * that each instance is truely unique.  By implementing the cache using a
   * {@link WeakHashMap}, this can be accomplished without the technique itself
   * adversely affecting memory usage.
   */
  private static final WeakHashMap	cache = new WeakHashMap();

  /**
   * Constructs a 1-D sorted sequence with no regular interval.  The 
   * coordinate system and units are the default from the set type.  The error
   * estimate is null.
   *
   * @param type		The type of the set.  Must be a {@link 
   *				RealType} or a single-component {@link
   *				RealTupleType} or {@link SetType}.
   * @param samples             The values in the set.
   *                            <code>samples[0][i]</code> is the value of
   *                            the ith sample point.  Must be sorted (either
   *                            increasing or decreasing).  May be
   *                            <code>null</code>.
   * @param lengthX		The number of samples.
   */
  public Gridded1DSet(MathType type, float[][] samples, int lengthX)
         throws VisADException {
    this(type, samples, lengthX, null, null, null);
  }

  /**
   * Constructs a 1-D sorted sequence with no regular interval.
   *
   * @param type		The type of the set.  Must be a {@link 
   *				RealType} or a single-component {@link
   *				RealTupleType} or {@link SetType}.
   * @param samples             The values in the set.
   *                            <code>samples[0][i]</code> is the value of
   *                            the ith sample point.  Must be sorted (either
   *                            increasing or decreasing).  May be
   *                            <code>null</code>.
   * @param lengthX		The number of samples.
   * @param coord_sys           The coordinate system for this, particular, set.
   *                            Must be compatible with the default coordinate
   *                            system.  May be <code>null</code>.
   * @param units               The units for the tuple components.  Only
   *                            <code>units[0]</code> is meaningfull.  Must
   *                            be compatible with the default unit.  May be
   *                            <code>null</code>.
   * @param errors		The error estimates of the tuple components.
   *				Only <code>errors[0]</code> is meaningful.  May
   *				be <code>null</code>.
   */
  public Gridded1DSet(MathType type, float[][] samples, int lengthX,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, coord_sys, units, errors, true);
  }

  /**
   * Constructs a 1-D sorted sequence with no regular interval.
   *
   * @param type		The type of the set.  Must be a {@link 
   *				RealType} or a single-component {@link
   *				RealTupleType} or {@link SetType}.
   * @param samples             The values in the set.
   *                            <code>samples[0][i]</code> is the value of
   *                            the ith sample point.  Must be sorted (either
   *                            increasing or decreasing).  May be
   *                            <code>null</code>.
   * @param lengthX		The number of samples.
   * @param coord_sys           The coordinate system for this, particular, set.
   *                            Must be compatible with the default coordinate
   *                            system.  May be <code>null</code>.
   * @param units               The units for the tuple components.  Only
   *                            <code>units[0]</code> is meaningfull.  Must
   *                            be compatible with the default unit.  May be
   *                            <code>null</code>.
   * @param errors		The error estimates of the tuple components.
   *				Only <code>errors[0]</code> is meaningful.  May
   *				be <code>null</code>.
   * @param copy		Whether or not to copy the values array.
   */
  public Gridded1DSet(MathType type, float[][] samples, int lengthX,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors, boolean copy)
                      throws VisADException {
    super(type, samples, make_lengths(lengthX), coord_sys, units,
          errors, copy);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];

    float[][]mySamples = getMySamples();
    if (mySamples != null && Lengths[0] > 1) {
      // samples consistency test
      for (int i=0; i<Length; i++) {
        if (mySamples[0][i] != mySamples[0][i]) {
          throw new SetException(
           "Gridded1DSet: samples values may not be missing");
        }
      }
      Ascending = (mySamples[0][LengthX-1] > mySamples[0][0]);
      if (Ascending) {
        for (int i=1; i<LengthX; i++) {
          if (mySamples[0][i] < mySamples[0][i-1]) {
            throw new SetException(
             "Gridded1DSet: samples do not form a valid grid ("+i+")");
          }
        }
      }
      else { // !Pos
        for (int i=1; i<LengthX; i++) {
          if (mySamples[0][i] > mySamples[0][i-1]) {
            throw new SetException(
             "Gridded1DSet: samples do not form a valid grid ("+i+")");
          }
        }
      }
    }
  }

  /**
   * Returns an instance of this class.  This method uses a weak cache of
   * previously-created instances to reduce memory usage.
   *
   * @param type		The type of the set.  Must be a {@link 
   *				RealType} or a single-component {@link
   *				RealTupleType} or {@link SetType}.
   * @param samples             The values in the set.
   *                            <code>samples[i]</code> is the value of
   *                            the ith sample point.  Must be sorted (either
   *                            increasing or decreasing).  May be
   *                            <code>null</code>.  The array is not copied, so
   *				either don't modify it or clone it first.
   * @param coord_sys           The coordinate system for this, particular, set.
   *                            Must be compatible with the default coordinate
   *                            system.  May be <code>null</code>.
   * @param unit                The unit for the samples.  Must be compatible
   *				with the default unit.  May be 
   *				<code>null</code>.
   * @param error		The error estimate of the samples.  May be
   *				<code>null</code>.
   */
  public static synchronized Gridded1DSet create(
      MathType		type,
      float[]		samples,
      CoordinateSystem	coordSys,
      Unit		unit,
      ErrorEstimate	error)
    throws VisADException
  {
    Gridded1DSet	newSet =
      new Gridded1DSet(
	type, new float[][] {samples}, samples.length, coordSys,
	new Unit[] {unit}, new ErrorEstimate[] {error}, false);
    WeakReference	ref = (WeakReference)cache.get(newSet);
    if (ref == null)
    {
      /*
       * The new instance is unique (any and all previously-created identical
       * instances no longer exist).
       *
       * A WeakReference is used in the following because values of a
       * WeakHashMap aren't "weak" themselves and must not strongly reference
       * their associated keys either directly or indirectly.
       */
      cache.put(newSet, new WeakReference(newSet));
    }
    else
    {
      /*
       * The new instance is a duplicate of a previously-created one.
       */
      Gridded1DSet	oldSet = (Gridded1DSet)ref.get();
      if (oldSet == null)
      {
	/* The previous instance no longer exists.  Save the new instance. */
	cache.put(newSet, new WeakReference(newSet));
      }
      else
      {
	/* The previous instance still exists.  Reuse it to save memory. */
	newSet = oldSet;
      }
    }
    return newSet;
  }

  static int[] make_lengths(int lengthX) {
    int[] lens = new int[1];
    lens[0] = lengthX;
    return lens;
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    float[][]mySamples = getMySamples();
    if (mySamples == null) {
      // not used - over-ridden by Linear1DSet.indexToValue
      float[][] grid = new float[ManifoldDimension][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          grid[0][i] = (float) index[i];
        }
        else {
          grid[0][i] = -1;
        }
      }
      return gridToValue(grid);
    }
    else {
      float[][] values = new float[1][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          values[0][i] = mySamples[0][index[i]];
        }
        else {
          values[0][i] = Float.NaN;
        }
      }
      return values;
    }
  }

  /**
   * Convert an array of values in R^DomainDimension to an array of
   * 1-D indices.  This Gridded1DSet must have at least two points in the
   * set.
   * @param value	An array of coordinates.  <code>value[i][j]
   *			</code> contains the <code>i</code>th component of the
   *			<code>j</code>th point.
   * @return		Indices of nearest points.  RETURN_VALUE<code>[i]</code>
   *			will contain the index of the point in the set closest
   *			to <code>value[][i]</code> or <code>-1</code> if
   *			<code>value[][i]</code> lies outside the set.
   */
  public int[] valueToIndex(float[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("Gridded1DSet.valueToIndex: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    int length = value[0].length;
    int[] index = new int[length];

    float[][] grid = valueToGrid(value);
    float[] grid0 = grid[0];
    float g;
    for (int i=0; i<length; i++) {
      g = grid0[i];
      index[i] = Float.isNaN(g) ? -1 : ((int) (g + 0.5));
    }
    return index;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    if (grid.length < DomainDimension) {
      throw new SetException("Gridded1DSet.gridToValue: grid dimension " +
                             grid.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    /* remove DRM 2004-09-14
    if (Lengths[0] < 2) {
      throw new SetException("Gridded1DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    */
    int length = grid[0].length;
    float[][] value = new float[1][length];
    float[][]mySamples = getMySamples();
    for (int i=0; i<length; i++) {
      // let g be the current grid coordinate
      float g = grid[0][i];
      if ( (g < -0.5) || (g > LengthX-0.5) ) {
        value[0][i] = Float.NaN;
      } else if (Length == 1) {  // just return the value if that's all we have
        value[0][i] = mySamples[0][0];
      } else {
        // calculate closest integer variable
        int ig;
        if (g < 0) ig = 0;
        else if (g >= LengthX-1) ig = LengthX - 2;
        else ig = (int) g;
        float A = g - ig;  // distance variable
        // do the linear interpolation
        value[0][i] = (1-A)*mySamples[0][ig] + A*mySamples[0][ig+1];
      }
    }
    return value;
  }

  // WLH 6 Dec 2001
  //private int ig = -1;

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    if (value.length < DomainDimension) {
      throw new SetException("Gridded1DSet.valueToGrid: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    /* remove DRM 2004-09-14
    if (Lengths[0] < 2) {
      throw new SetException("Gridded1DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    */
    float[] vals = value[0];
    int length = vals.length;
    float[][]mySamples = getMySamples();
    float[] samps = mySamples[0];
    float[][] grid = new float[1][length];


    int ig = (LengthX - 1)/2;
/* WLH 6 Dec 2001
    // use value from last call as first guess, if reasonable
    if (ig < 0 || ig >= LengthX) {
      ig = (LengthX - 1)/2;
    }
*/

    for (int i=0; i<length; i++) {
      if (Float.isNaN(vals[i])) {
        grid[0][i] = Float.NaN;
      } else if (Length == 1) {  // just return 0 if that's all we have
        grid[0][i] = 0;
      } else {
	int lower = 0;
	int upper = LengthX-1;
	while (lower < upper) {
	  if ((vals[i] - samps[ig]) * (vals[i] - samps[ig+1]) <= 0) break;
	  if (Ascending ? samps[ig+1] < vals[i] : samps[ig+1] > vals[i]) {
	    lower = ig+1;
          }
	  else if (Ascending ? samps[ig] > vals[i] : samps[ig] < vals[i]) {
	    upper = ig;
          }
	  if (lower < upper) ig = (lower + upper) / 2;
	}
        // Newton's method
	float solv = ig + (vals[i] - samps[ig]) / (samps[ig+1] - samps[ig]);
        if (solv > -0.5 && solv < LengthX - 0.5) grid[0][i] = solv;
        else {
          grid[0][i] = Float.NaN;
          // next guess should be in the middle if previous value was missing
          ig = (LengthX - 1)/2;
        }
      }
    }
    return grid;
  }

  public int getLengthX() {
    return LengthX;
  }

  public float getLowX() {
    return LowX;
  }

  public float getHiX() {
    return HiX;
  }

  public boolean isAscending()
  {
    return Ascending;
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Gridded1DSet(type, getMySamples(), Length, DomainCoordinateSystem,
                             SetUnits, SetErrors);
  }

  /* run 'java visad.Gridded1DSet < formatted_input_stream'
     to test the Gridded1DSet class */
  public static void main(String[] args) throws VisADException {

    // Define input stream
    InputStreamReader inStr = new InputStreamReader(System.in);

    // Define temporary integer array
    int[] ints = new int[80];
    try {
      ints[0] = inStr.read();
    }
    catch(Exception e) {
      System.out.println("Gridded1DSet: "+e);
    }
    int l = 0;
    while (ints[l] != 10) {
      try {
        ints[++l] = inStr.read();
      }
      catch (Exception e) {
        System.out.println("Gridded1DSet: "+e);
      }
    }
    // convert array of integers to array of characters
    char[] chars = new char[l];
    for (int i=0; i<l; i++) {
      chars[i] = (char) ints[i];
    }
    int num_coords = Integer.parseInt(new String(chars));

    // Define size of Samples array
    float[][] samp = new float[1][num_coords];
    System.out.println("num_dimensions = 1, num_coords = "+num_coords+"\n");

    // Skip blank line
    try {
      ints[0] = inStr.read();
    }
    catch (Exception e) {
      System.out.println("Gridded1DSet: "+e);
    }

    for (int c=0; c<num_coords; c++) {
      l = 0;
      try {
        ints[0] = inStr.read();
      }
      catch (Exception e) {
        System.out.println("Gridded1DSet: "+e);
      }
      while (ints[l] != 32) {
        try {
          ints[++l] = inStr.read();
        }
        catch (Exception e) {
          System.out.println("Gridded1DSet: "+e);
        }
      }
      chars = new char[l];
      for (int i=0; i<l; i++) {
        chars[i] = (char) ints[i];
      }
      samp[0][c] = (Float.valueOf(new String(chars))).floatValue();
    }

    // do EOF stuff
    try {
      inStr.close();
    }
    catch (Exception e) {
      System.out.println("Gridded1DSet: "+e);
    }

    // Set up instance of Gridded1DSet
    RealType vis_data = RealType.getRealType("vis_data");
    RealType[] vis_array = {vis_data};
    RealTupleType vis_tuple = new RealTupleType(vis_array);
    Gridded1DSet gSet1D = new Gridded1DSet(vis_tuple, samp, num_coords);

    System.out.println("Lengths = " + num_coords + " wedge = ");
    int[] wedge = gSet1D.getWedge();
    for (int i=0; i<wedge.length; i++) System.out.println(" " + wedge[i]);

    // Print out Samples information
    System.out.println("Samples ("+gSet1D.LengthX+"):");
    for (int i=0; i<gSet1D.LengthX; i++) {
	System.out.println("#"+i+":\t"+gSet1D.getMySamples()[0][i]);
    }

    // Test gridToValue function
    System.out.println("\ngridToValue test:");
    int myLength = gSet1D.LengthX+1;
    float[][] myGrid = new float[1][myLength];
    for (int i=0; i<myLength; i++) {
      myGrid[0][i] = i-0.5f;
    }
    myGrid[0][0] += 0.1;          // don't let grid values get too
    myGrid[0][myLength-1] -= 0.1; // close to interpolation limits
    float[][] myValue = gSet1D.gridToValue(myGrid);
    for (int i=0; i<myLength; i++) {
        System.out.println("("+((float) Math.round(1000000
                                        *myGrid[0][i]) /1000000)+")\t-->  "
                              +((float) Math.round(1000000
                                        *myValue[0][i]) /1000000));
    }

    // Test valueToGrid function
    System.out.println("\nvalueToGrid test:");
    float[][] gridTwo = gSet1D.valueToGrid(myValue);
    for (int i=0; i<gridTwo[0].length; i++) {
      System.out.println(((float) Math.round(1000000
                                  *myValue[0][i]) /1000000)+"  \t-->  ("
                        +((float) Math.round(1000000
                                  *gridTwo[0][i]) /1000000)+")");
    }
    System.out.println();

  }

/* Here's the output with sample file Gridded1D.txt:

iris 25% java visad.Gridded1DSet < Gridded1D.txt
num_dimensions = 1, num_coords = 20

Lengths = 20 wedge =
 0
 1
. . .
 18
 19
Samples (20):
#0:     -40.54849
#1:     -39.462048
. . .
#18:    26.026154
#19:    38.3012

gridToValue test:
(-0.4)  -->  -40.983063
(0.5)   -->  -40.00527
. . .
(18.5)  -->  32.163677
(19.4)  -->  43.211212

valueToGrid test:
-40.983063      -->  (-0.399998)
-40.00527       -->  (0.5)
. . .
32.163677       -->  (18.5)
43.211212       -->  (19.4)

iris 26%

*/

}

