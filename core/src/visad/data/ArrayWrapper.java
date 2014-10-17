/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

/**
 */
public  class ArrayWrapper {
   private Object cacheId;


  /**
   * add the data to the cache
   *
   * @param values the values to add
   *
   */
   
  public ArrayWrapper(byte[] values) {
      cacheId = DataCacheManager.getCacheManager().addToCache(values);
  }

  /**
   * add the data to the cache
   *
   * @param values the values to add
   *
   */
  
  public ArrayWrapper(byte[][] values) {
      cacheId = DataCacheManager.getCacheManager().addToCache(values);
  }


  /**
   * add the data to the cache
   *
   * @param values the values to add
   *
   */
  
  public ArrayWrapper(float[] values) {
      cacheId = DataCacheManager.getCacheManager().addToCache(values);
  }

  /**
   * add the data to the cache
   *
   * @param values the values to add
   *
   */
  
  public ArrayWrapper(float[][] values) {
      cacheId = DataCacheManager.getCacheManager().addToCache(values);
  }

  /**
   * add the data to the cache
   *
   * @param values the values to add
   *
   */
  
  public ArrayWrapper(double[] values) {
      cacheId = DataCacheManager.getCacheManager().addToCache(values);
  }

  /**
   * add the data to the cache
   *
   * @param values the values to add
   *
   */
  
  public ArrayWrapper(double[][] values) {
      cacheId = DataCacheManager.getCacheManager().addToCache(values);
  }

  /**
   * add the data to the cache
   *
   * @param values the values to add
   *
   */
  
  public ArrayWrapper(int[] values) {
      cacheId = DataCacheManager.getCacheManager().addToCache(values);
  }

  /**
   * add the data to the cache
   *
   * @param values the values to add
   *
   */
  
  public ArrayWrapper(int[][] values) {
      cacheId = DataCacheManager.getCacheManager().addToCache(values);
  }

  /**
   * add the data to the cache
   *
   * @param values the values to add
   *
   */
  
  public ArrayWrapper(short[] values) {
      cacheId = DataCacheManager.getCacheManager().addToCache(values);
  }

  /**
   * add the data to the cache
   *
   * @param values the values to add
   *
   */
  
  public ArrayWrapper(short[][] values) {
      cacheId = DataCacheManager.getCacheManager().addToCache(values);
  }

    public boolean inMemory() {
	return DataCacheManager.getCacheManager().inMemory(cacheId);
    }

    public void updateData(Object newData) {
	DataCacheManager.getCacheManager().updateData(cacheId, newData);
    }




  /**
   * get the value 
   *
   * @return  the value
   */
  public byte[] getByteArray1D() {
      return DataCacheManager.getCacheManager().getByteArray1D(cacheId);
  }


  /**
   * get the value 
   *
   * @return  the value
   */
  public byte[][] getByteArray2D() {
      return DataCacheManager.getCacheManager().getByteArray2D(cacheId);
  }


  /**
   * get the value 
   *
   * @return  the value
   */
  public short[] getShortArray1D() {
      return DataCacheManager.getCacheManager().getShortArray1D(cacheId);
  }


  /**
   * get the value 
   *
   * @return  the value
   */
  public short[][] getShortArray2D() {
      return DataCacheManager.getCacheManager().getShortArray2D(cacheId);
  }


  /**
   * get the value 
   *
   * @return  the value
   */
  public int[] getIntArray1D() {
      return DataCacheManager.getCacheManager().getIntArray1D(cacheId);
  }


  /**
   * get the value 
   *
   * @return  the value
   */
  public int[][] getIntArray2D() {
      return DataCacheManager.getCacheManager().getIntArray2D(cacheId);
  }


  /**
   * get the value 
   *
   * @return  the value
   */
  public float[] getFloatArray1D() {
      return DataCacheManager.getCacheManager().getFloatArray1D(cacheId);
  }


  /**
   * get the value 
   *
   * @return  the value
   */
  public float[][] getFloatArray2D() {
      return DataCacheManager.getCacheManager().getFloatArray2D(cacheId);
  }


  /**
   * get the value 
   *
   * @return  the value
   */
  public double[] getDoubleArray1D() {
      return DataCacheManager.getCacheManager().getDoubleArray1D(cacheId);
  }


  /**
   * get the value 
   *
   * @return  the value
   */
  public double[][] getDoubleArray2D() {
      return DataCacheManager.getCacheManager().getDoubleArray2D(cacheId);
  }


    public void finalize() throws Throwable {
        super.finalize();
        DataCacheManager.getCacheManager().removeFromCache(cacheId);
    }

}

