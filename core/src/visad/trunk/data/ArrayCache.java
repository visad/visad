//
// CachingCoordinateSystem.java
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

package visad.data;


import java.util.Arrays;
import java.util.Hashtable;

import visad.util.Util;


/**
 * This class is used by the CachingCoordinateSystem to do the actual caching mapping one array to another one
 * @version $Revision: 1.1 $ $Date: 2009-11-30 22:21:47 $
 */
public class ArrayCache {

  /** Do we cache */
  private boolean enabled =
    Boolean.parseBoolean(System.getProperty("visad.arraycache.enabled",
                                            "true"));

  /** lower size threshold */
  private int lowerThreshold =
    Integer.parseInt(System.getProperty("visad.arraycache.lowerthreshold",
                                        "1000"));


  /** upper size threshold */
  private int upperThreshold =
    Integer.parseInt(System.getProperty("visad.arraycache.upperthreshold",
                                        "1000000"));


  /** holds float arrays */
  private Hashtable<Object, float[][][]> floatMap = new Hashtable<Object,
                                                      float[][][]>();

  /** holds double arrays */
  private Hashtable<Object, double[][][]> doubleMap = new Hashtable<Object,
                                                        double[][][]>();




  /**
   *  ctor
   */
  public ArrayCache() {}


  /**
   * ctor
   *
   * @param enabled  If false then never cache
   */
  public ArrayCache(boolean enabled) {
    this.enabled = enabled;
  }

   private String getKey(String key, int size) {
       return key +"_" + size;
   }

  /**
   * Get the converted value for the specified key and input pairs
   *
   * @param key The key (e.g., "toReference", "fromReference")
   * @param input  The input
   *
   * @return
   */
  public synchronized float[][] get(String key, float[][] input) {
    if (!enabled || input==null) return null;
    key = getKey(key, input[0].length);
    float[][][] pair = floatMap.get(key);
    if (input == null || pair == null) {
      return null;
    }
    float[][] lastInput = pair[0];
    float[][] lastOutput = pair[1];
    if (lastInput.length != input.length) return null;
    for (int i = 0; i < input.length; i++) {
      if (!Arrays.equals(input[i], lastInput[i])) {
        return null;
      }
    }
    //?? should we clone the output
    return lastOutput;
  }


  /**
   * Put the converted value for the specified key and input pairs
   *
   * @param key The key
   * @param input  The input array
   * @param output  The array to store
   */
  public synchronized void put(String key, float[][] input, float[][] output) {
    if (!enabled || input==null) return;
    if (input[0].length <= lowerThreshold) return;
    if (input[0].length > upperThreshold) return;
    key = getKey(key, input[0].length);

    if(floatMap.size()>5) 
        floatMap = new Hashtable<Object,
            float[][][]>();

    floatMap.put(key, new float[][][] {
      (float[][])Util.clone(input), (float[][])Util.clone(output)
    });

  }


  /**
   * Get the converted value for the specified key and input pairs
   *
   * @param key The key (e.g., "toReference", "fromReference")
   * @param input  The input
   *
   * @return
   */
  public synchronized  double[][] get(String key, double[][] input) {
    if (!enabled || input==null) return null;
    key = getKey(key, input[0].length);
    double[][][] pair = doubleMap.get(key);
    if (input == null || pair == null) return null;
    double[][] lastInput = pair[0];
    double[][] lastOutput = pair[1];
    if (lastInput.length != input.length) return null;
    for (int i = 0; i < input.length; i++) {
      if (!Arrays.equals(input[i], lastInput[i])) {
        return null;
      }
    }
    return lastOutput;
  }


  /**
   * Put the converted value for the specified key and input pairs
   *
   * @param key The key
   * @param input  The input array
   * @param output  The array to store
   */
  public synchronized void put(String key, double[][] input, double[][] output) {
    if (!enabled || input==null) return;
    if (input[0].length <= lowerThreshold) return;
    if (input[0].length > upperThreshold) return;

    key = getKey(key, input[0].length);
    if(doubleMap.size()>4) 
        doubleMap = new Hashtable<Object,
            double[][][]>();
    doubleMap.put(key, new double[][][] {
      (double[][])Util.clone(input), (double[][])Util.clone(output)
    });

  }

}

