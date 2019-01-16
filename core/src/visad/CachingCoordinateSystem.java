//
// CachingCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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


import java.util.Arrays;

import visad.util.Util;

import visad.data.ArrayCache;


/**
 * A wrapper class for CoordinateSystems that will cache the last
 * values input and output values of the toReference and fromReference
 * methods.  If the inputs are the same as the last time these
 * methods were called, the previously calculated values are returned.
 *
 * @author Don Murray
 * @version $Revision: 1.11 $ $Date: 2009-12-07 12:16:19 $
 */
public class CachingCoordinateSystem extends CoordinateSystem {

  /** The coordinate system I wrap */
  private CoordinateSystem myCS = null;

  /**  Does the actual caching         */
  private ArrayCache arrayCache = new ArrayCache();

  /** Show time to transform           */
  public static boolean debugTime = 
    Boolean.parseBoolean(System.getProperty("visad.cachingcoordinatesystem.debugtime",
                                            "false"));

  /**  counter to show which object this is         */
  private static int cnt = 0;

  /**  counter to show which object this is         */
  private int mycnt = cnt++;


  /**
   * Construct a new CachingCoordinateSystem that wraps around the input.
   * @param cs CoordinateSystem to wrap
   *
   * @throws VisADException 
   */
  public CachingCoordinateSystem(CoordinateSystem cs) throws VisADException {
    super(cs.getReference(), cs.getCoordinateSystemUnits());
    myCS = cs;
  }


  /**
   * Wrapper around the toReference method of the input CoordinateSystem.
   * If the inputs are the same as the last time this method was called,
   * the previously computed outputs will be returned, otherwise the
   * toReference method of the wrapped CS is called and it's output
   * is returned
   *
   * @param   inputs  values to transform
   * @return  transformed input values.
   * @throws  VisADException  when wrapped CS does
   */
  public double[][] toReference(double[][] inputs) throws VisADException {
    if (inputs == null) return inputs;
    long t1 = System.currentTimeMillis();
    boolean hit = true;
    String key = "toReferenceD";
    ArrayCache.DoubleResult results = arrayCache.get(key, inputs);    
    if (results.values == null) {
      double[][] tmp = results.cloneForCache(inputs);
      results.values = myCS.toReference(inputs);
      arrayCache.put(key, tmp, results);
      hit = false;
    }
    if(debugTime  && results.getShouldCache())
        debugTime(inputs[0].length, key +" hit?" + hit, t1,System.currentTimeMillis());
    //    System.err.println (Util.getStackTrace());

    return results.values;
  }

    private void debugTime(int size, String msg, long t1, long t2) {
        if(size>100 && debugTime && t1!=t2) {
            System.err.println("CCS #" +cnt + " size:" + size+ " " +  msg+" time:" + (t2 - t1));
        }
    }


  /**
   * Wrapper around the fromReference method of the input CoordinateSystem.
   * If the inputs are the same as the last time this method was called,
   * the previously computed outputs will be returned, otherwise the
   * fromReference method of the wrapped CS is called and it's output
   * is returned
   *
   * @param   inputs  values to transform
   * @return  transformed input values.
   * @throws  VisADException  when wrapped CS does
   */
  public double[][] fromReference(double[][] inputs) throws VisADException {
    if (inputs == null) return inputs;

    long t1 = System.currentTimeMillis();
    boolean hit = true;
    String key = "fromReferenceD";
    ArrayCache.DoubleResult results = arrayCache.get(key, inputs);
    if (results.values == null) {
      double[][] tmp = results.cloneForCache(inputs);
      results.values = myCS.fromReference(inputs);
      arrayCache.put(key, tmp, results);
      hit = false;
    }
    if(debugTime  && results.getShouldCache())
        debugTime(inputs[0].length,key +" hit?" + hit, t1,System.currentTimeMillis());
    return results.values;

  }





  /**
   * Wrapper around the toReference method of the input CoordinateSystem.
   * If the inputs are the same as the last time this method was called,
   * the previously computed outputs will be returned, otherwise the
   * toReference method of the wrapped CS is called and it's output
   * is returned
   *
   * @param   inputs  values to transform
   * @return  transformed input values.
   * @throws  VisADException  when wrapped CS does
   */
  public float[][] toReference(float[][] inputs) throws VisADException {
    if (inputs == null) return inputs;

    long t1 = System.currentTimeMillis();
    boolean hit = true;
    String key = "toReferenceF";

    ArrayCache.FloatResult results = arrayCache.get(key, inputs);
    if (results.values == null) {
      float[][] tmp = results.cloneForCache(inputs);
      results.values = myCS.toReference(inputs);
      arrayCache.put(key, tmp, results);
      hit = false;
    }

    if(debugTime  && results.getShouldCache())
        debugTime(inputs[0].length,key +" hit?" + hit, t1,System.currentTimeMillis());
    return results.values;
  }


  /**
   * Wrapper around the fromReference method of the input CoordinateSystem.
   * If the inputs are the same as the last time this method was called,
   * the previously computed outputs will be returned, otherwise the
   * fromReference method of the wrapped CS is called and it's output
   * is returned
   *
   * @param   inputs  values to transform
   * @return  transformed input values.
   * @throws  VisADException  when wrapped CS does
   */
  public float[][] fromReference(float[][] inputs) throws VisADException {
    if (inputs == null) return inputs;
    long t1 = System.currentTimeMillis();
    boolean hit = true;
    String key = "fromReferenceF";
    ArrayCache.FloatResult results = arrayCache.get(key, inputs);
    if (results.values==null) {
      float[][] tmp = results.cloneForCache(inputs);
      results.values = myCS.fromReference(inputs);
      arrayCache.put(key, tmp, results);
      hit = false;
    }
    if(debugTime  && results.getShouldCache())
        debugTime(inputs[0].length,key +" hit?" + hit, t1,System.currentTimeMillis());
    return results.values;
  }

  /**
   * Check for equality of CoordinateSystem objects
   * @param  obj  other object in question
   * @return  true if the object in question is a CachingCoordinateSystem
   *          and it's CS is equal this object's CS
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof CachingCoordinateSystem)) return false;
    CachingCoordinateSystem that = (CachingCoordinateSystem)obj;
    return that.myCS.equals(myCS);
  }

  /**
   * Access to the "cached" CS
   * @return  cached CoordinateSystem
   */
  public CoordinateSystem getCachedCoordinateSystem() {
    return myCS;
  }

  /**
   * A decriptive string of this CS.
   * @return a descriptive String
   */
  public String toString() {
    return "Cached CS: " + myCS.toString();
  }

}

