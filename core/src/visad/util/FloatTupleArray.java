package visad.util;

import java.util.logging.Logger;

/**
 * Growable data structure for float tuples.   
 */

public interface FloatTupleArray {

  public static class Factory {
	  
    private static Logger logger = Logger.getLogger(Factory.class.getName());

    /**
     * Create a new instance. 
     * 
     * TODO: Currently only returns the default implementation 
     * but in the future may return the most efficient implementation 
     * available in the runtime.
     * 
     * @param dim Tuple dimension.
     * @param size Initial size of data structure.
     * @return An instance initialized to contain tuples of size <code>dim</code>
     *  and have size <code>size</code>.
     */
    public static FloatTupleArray newInstance(int dim, int size) {
      return new FloatTupleArrayImpl(dim, size);
    }
    private Factory() {}
  }

  /**
   * Add tuples to array. 
   * @param values Values to add to array where dimension 2 == <code>dim()</code>.
   * @param start Index in input array where to start taking tuples.
   * @param num The number of tuples to take.
   */
  public void add(float[][] values, int start, int num); 

  /**
   * Get the elements of this array.  
   * @return This may return a reference or copy depending
   * on the implementation.
   */
  public float[][] elements();

  /**
   * Add tuples to the array. The array will grow as necessary to accommodate the new data.
   * @param elements Values to add to array where dimension 2 == <code>dim()</code>.
   */
  public void add(float[][] elements);

  /**
   * Set an array value.
   * @param i Tuple row index.
   * @param j Tuple col index.
   * @param val Value to set.
   */
  public void set(int i, int j, float val);

  /**
   * Get a value. 
   * @param i Tuple row index.
   * @param j Tuple col index.
   * @return The value for tuple <code>i</code> at tuple index <code>j</code>.
   */
  public float get(int i, int j);

  /**
   * Get array data. This is potentially expensive.
   * @return A copy of the array data.
   */
  public float[][] toArray();

  /**
   * Get array size.
   * @return size of array
   */
  
  public int size();

  /**
   * Get array tuple dimension.
   * @return The array tuple dimension
   */
  public int dim();
}
