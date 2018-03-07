//
// CachingCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.Enumeration;
import java.util.Hashtable;

import visad.util.Util;

/**
 * This class is used by the CachingCoordinateSystem to do the actual caching mapping one array to another one
 * @version $Revision: 1.5 $ $Date: 2010-01-05 21:02:43 $
 */
public class ArrayCache {

  /** Do we cache */
  private boolean enabled =
    Boolean.parseBoolean(System.getProperty("visad.data.arraycache.enabled",
                                            "true"));

  /** lower size threshold */
  private int lowerThreshold =
    Integer.parseInt(System.getProperty("visad.data.arraycache.lowerthreshold",
                                        "1000"));


  /** upper size threshold */
  private int upperThreshold =
    Integer.parseInt(System.getProperty("visad.data.arraycache.upperthreshold",
                                        "1000000"));


  private boolean useDataCacheManager = 
      Boolean.parseBoolean(System.getProperty("visad.data.arraycache.usedatacachemanager",
                                              "false"));



  private Hashtable<String,Integer>  misses = new Hashtable<String,Integer>();





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
   * @return value for supplied key
   */
   
  public FloatResult get(String key, float[][] input) {
    if (!shouldHandle(input)) {
        return new FloatResult(false);
    }
    return getInner(key, input);
  }


   private synchronized FloatResult getInner(String key, float[][] input) {
    key = getKey(key, input[0].length);
    float[][][] pair = getFloatValue(key);
    if (pair == null) {
      return handleCacheMiss(key, input);
    }

    float[][] lastInput = pair[0];
    float[][] lastOutput = pair[1];
    if (lastInput.length != input.length) return null;
    for (int i = 0; i < input.length; i++) {
      if (!Arrays.equals(input[i], lastInput[i])) {
          return handleCacheMiss(key, input);
      }
    }
    misses.remove(key);
    //?? should we clone the output
    return new FloatResult(Util.clone(lastOutput));
  }





  /**
   * Get the converted value for the specified key and input pairs
   *
   * @param key The key (e.g., "toReference", "fromReference")
   * @param input  The input
   *
   * @return value for the supplied key
   */
   
  public DoubleResult get(String key, double[][] input) {
      if (!shouldHandle(input)) {
         return new DoubleResult(false);
    }
      return getInner(key, input);
  }



 private synchronized  DoubleResult getInner(String key, double[][] input) {

    key = getKey(key, input[0].length);
    double[][][] pair = getDoubleValue(key);
    if (pair == null) {
        return handleCacheMiss(key, input);
    }        

    double[][] lastInput = pair[0];
    double[][] lastOutput = pair[1];
    if (lastInput.length != input.length) return null;
    for (int i = 0; i < input.length; i++) {
       if (!Arrays.equals(input[i], lastInput[i])) {
         return handleCacheMiss(key, input);
      }
    }
    misses.remove(key);
    return new DoubleResult(Util.clone(lastOutput));
  }



  private DoubleResult handleCacheMiss(String key, double[][]input) {
        Integer numMisses = misses.get(key);
        if(numMisses==null) {
            misses.put(key, numMisses = new Integer(1));
        } else {
            misses.put(key, new Integer(numMisses.intValue()+1));
        }
        if(numMisses.intValue()>3) {
            removeValue(key);
        }
        return new DoubleResult(numMisses.intValue()<=1);
  }


  private FloatResult handleCacheMiss(String key, float[][]input) {
        Integer numMisses = misses.get(key);
        if(numMisses==null) {
            misses.put(key, numMisses = new Integer(1));
        } else {
            misses.put(key, new Integer(numMisses.intValue()+1));
        }
        if(numMisses.intValue()>3) {
            removeValue(key);
        }
        return new FloatResult(numMisses.intValue()<=1);
  }





    private boolean shouldHandle(double[][]input) {
        if(input == null) return false;
        if(input.length==0) return false;
        if(input[0]==null) return false;
        if(!enabled) return false;
        if (input[0].length <= lowerThreshold) return false;
        if (input[0].length > upperThreshold) return false;
        return true;
    }



    private boolean shouldHandle(float[][]input) {
        if(input == null) return false;
        if(input.length==0) return false;
        if(input[0]==null) return false;
        if(!enabled) return false;
        if (input[0].length <= lowerThreshold) return false;
        if (input[0].length > upperThreshold) return false;
        return true;
    }



  /**
   * Put the converted value for the specified key and input pairs
   *
   * @param key The key
   * @param input  The input array
   * @param results  The array to store
   */
  public void put(String key, double[][] input, DoubleResult results) {
    if(!shouldHandle(input)) return;
    putInner(key, input, results);
  }


  private synchronized void putInner(String key, double[][] input, DoubleResult results) {
    if(!results.shouldCache || results.values==null) return;
    key = getKey(key, input[0].length);
    storeValue(key,
             new double[][][] {
                 (double[][])Util.clone(input), (double[][])Util.clone(results.values)
    });

  }




  /**
   * Put the converted value for the specified key and input pairs
   *
   * @param key The key
   * @param input  The input array
   * @param results  The array to store
   */
  public  void put(String key, float[][] input, FloatResult results) {
    if(!shouldHandle(input)) return;
    putInner(key, input, results);
  }

  private synchronized void putInner(String key, float[][] input, FloatResult results) {
    if(!results.shouldCache || results.values==null) return;
    key = getKey(key, input[0].length);
    storeValue(key, new float[][][] {
      (float[][])Util.clone(input), (float[][])Util.clone(results.values)
    });

  }





    /** holds float arrays or DataCacheManager ids */
    private Hashtable<Object, Object> map = new Hashtable<Object, Object>();

    private void storeValue(String key, double[][][]value) {
        checkCache();
        Object object = value;
        if(useDataCacheManager) {
            removeValue(key);
            object = DataCacheManager.getCacheManager().addToCache("ArrayCache", value, true);
        }
        map.put(key, object);
    }

    private void storeValue(String key, float[][][]value) {
        checkCache();
        Object object = value;
        if(useDataCacheManager) {
            removeValue(key);
            object = DataCacheManager.getCacheManager().addToCache("ArrayCache", value, true);
        }
        map.put(key, object);
    }

    public void finalize() throws Throwable {
        super.finalize();
        if(useDataCacheManager) {
            //            System.err.println ("arraycache finalize");
            clearCache();
        }
    }


    private void checkCache() {
        if(map.size()>4) {
            clearCache();
        }
    }

    private void removeValue(Object key) {
        Object object  = map.get(key);
        if(object!=null) {
            if(useDataCacheManager)
                DataCacheManager.getCacheManager().removeFromCache(object);
            map.remove(key);
        }
    }

    private void clearCache() {
        for(Enumeration keys= map.keys();keys.hasMoreElements(); ) {
            Object key= keys.nextElement();
            Object object  = map.get(key);
            if(useDataCacheManager) {
                DataCacheManager.getCacheManager().removeFromCache(object);
            }
        }
        map = new Hashtable<Object, Object>();
    }


    private double[][][] getDoubleValue(String key) {
        Object object = map.get(key);
        if(object==null) {
            return null;
        }
        if(useDataCacheManager)
            return DataCacheManager.getCacheManager().getDoubleArray3D(object);
        return (double[][][])object;
    }

    private float[][][] getFloatValue(String key) {
        Object object = map.get(key);
        if(object==null) {
            return null;
        }
        if(useDataCacheManager)
            return DataCacheManager.getCacheManager().getFloatArray3D(object);
        return (float[][][])object;
    }







    public static class DoubleResult {
        public boolean shouldCache = true;
        public double[][]values;

        public DoubleResult() {
            shouldCache = false;
            values = null;
        }

        public DoubleResult(boolean shouldCache) {
            this.shouldCache = shouldCache;
        }

        public DoubleResult(double[][]values) {
            this.values = values;
            this.shouldCache = true;
        }


        public double[][]cloneForCache(double[][]a) {
            if(!shouldCache) return null;
            return Util.clone(a);
        }

        public boolean getShouldCache() {
            return shouldCache;
        }

    }




    public static class FloatResult {
        public boolean shouldCache = true;
        public float[][]values;

        public FloatResult() {
            shouldCache = false;
            values = null;
        }

        public FloatResult(boolean shouldCache) {
            this.shouldCache = shouldCache;
        }

        public FloatResult(float[][]values) {
            this.values = values;
            this.shouldCache = true;
        }

        public float[][]cloneForCache(float[][]a) {
            if(!shouldCache) return null;
            return Util.clone(a);
        }

        public boolean getShouldCache() {
            return shouldCache;
        }

    }







}

