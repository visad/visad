// Member of the utility package.

package nom.tam.util;

/*
 * Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 */


import java.lang.reflect.*;


/** This is a package of static functions which perform
  * computations on arrays.  Generally these routines attempt
  * to complete without throwing errors by ignoring data
  * they cannot understand.
  */

public class ArrayFuncs {

    /** Compute the size of an object.  Note that this only handles
      * arrays or scalars of the primitive objects and Strings.  It
      * returns 0 for any object array element it does not understand.
      *
      * @param o The object whose size is desired.
      */

    public static int computeSize(Object o) {

        if (o == null) {
             return 0;
        }

        int size = 0;
        String classname = o.getClass().getName();
        if (classname.substring(0, 2).equals("[[") || 
            classname.equals("[Ljava.lang.String;") ||
            classname.equals("[Ljava.lang.Object;")) {

            for (int i=0; i<((Object[])o).length; i += 1) {
                 size += computeSize( ((Object[]) o)[i] );
            }
            return size;
        }

        if (classname.charAt(0) == '['  && classname.charAt(1) != 'L') {
            switch (classname.charAt(1)) {
                 case 'I': return ((int[])o).length  * 4;
                 case 'J': return ((long[])o).length * 8;
                 case 'F': return ((float[])o).length* 4;
                 case 'D': return ((double[])o).length*8;
                 case 'B': return ((byte[])o).length;
                 case 'C': return ((char[])o).length * 2;
                 case 'S': return ((short[])o).length* 2;
                 case 'Z': return ((boolean[])o).length;
                 default: return 0;
            }
        }

        if (classname.substring(0,10).equals("java.lang.") ) {
            classname = classname.substring(10, classname.length()-1);

	      if (classname.equals("Int") || classname.equals("Float")) {
                return 4;
            } else if (classname.equals("Double") || classname.equals("Long")) {
                return 8;
            } else if (classname.equals("Short") || classname.equals("Char")) {
                return 2;
            } else if (classname.equals("Byte") || classname.equals("Boolean")) {
                return 1;
            } else if (classname.equals("String") ) {
                return ((String)o).length();
            } else {
                return 0;
            }
        }
        return 0;
    }

/** Try to create a deep clone of an Array or a standard clone of a scalar.
  * The object may comprise arrays of
  * any primitive type or any Object type which implements Cloneable.
  * However, if the Object is some kind of collection, e.g., a Vector
  * then only a shallow copy of that object is made.  I.e., deep refers
  * only to arrays.
  *
  * @param o The object to be copied.
  */
    public static Object deepClone(Object o) {
  
        if (o == null) {
            return null;
        }

        String classname = o.getClass().getName();

        // Is this an array?
        if (classname.charAt(0) != '[') {
            return genericClone(o);            
        }

        // Check if this is a 1D primitive array.
        if (classname.charAt(1) != '[' && classname.charAt(1) != 'L') {
            switch( classname.charAt(1) ){
                 case 'B': return ((byte[])o).clone();
                 case 'Z': return ((boolean[])o).clone();
                 case 'C': return ((char[])o).clone();
                 case 'S': return ((short[])o).clone();
                 case 'I': return ((int[])o).clone();
                 case 'J': return ((long[])o).clone();
                 case 'F': return ((float[])o).clone();
                 case 'D': return ((double[])o).clone();
                 default: System.err.println("Unknown primtive array class:"+classname);
                          return null;
           }
        }

        // Get the base type.
        int ndim = 1;
        while (classname.charAt(ndim) == '[') {
            ndim += 1;
        }
        Class baseClass;
        if (classname.charAt(ndim) != 'L') {
            baseClass = getBaseClass(o);
        } else {
              try {
                  baseClass = Class.forName(classname.substring(ndim+1, classname.length()-1));
              } catch (ClassNotFoundException e) {
                  System.err.println("Internal error: class definition inconsistency: "+classname);
                  return null;
              }
        }

        // Allocate the array but make all but the first dimension 0.        
        int[] dims = new int[ndim];
        dims[0] = Array.getLength(o);
        for (int i=1; i<ndim; i += 1) {
            dims[i] = 0;
        }


        Object copy = Array.newInstance(baseClass, dims);
        if (copy == null) {
             return null;
        }

        // Now fill in the next level down by recursion.
        for (int i=0; i<dims[0]; i += 1) {
             Array.set(copy, i, deepClone(Array.get(o, i)));
        }               
         
        return copy;
    }

    /** Clone an Object if possible.
     *
     * This method returns an Object which is a clone of the
     * input object.  It checks if the method implements the
     * Cloneable interface and then uses reflection to invoke
     * the clone method.  This can't be done directly since
     * as far as the compiler is concerned the clone method for
     * Object is protected and someone could implement Cloneable but
     * leave the clone method protected.  The cloning can fail in a 
     * variety of ways which are trapped so that it returns null instead.
     * This method will generally create a shallow clone.  If you
     * wish a deep copy of an array the method deepClone should be used.
     *
     * @param o The object to be cloned.
     */
    public static Object genericClone(Object o) {
	
        if (! (o instanceof Cloneable) ) {
            return null;
        }

        Class[] argTypes = new Class[0];
        Object[] args = new Object[0];

        try {
            return o.getClass().getMethod("clone", argTypes).invoke(o,args);
        } catch (Exception e) {
            return null;
        }
    }


    /** Find the dimensions of an object.
      *
      * This method returns an integer array with the dimensions
      * of the object o which should usually be an array.
      *
      * It returns an array of dimension 0 for scalar objects
      * and it returns -1 for dimension which have not been allocated,
      *  e.g., int[][][] x = new int[100][][]; should return [100,-1,-1].
      *
      * @param o The object to get the dimensions of.
      */

    public static int[] getDimensions(Object o) {

        if (o == null) {
            return null;
        }
  
        String classname = o.getClass().getName();
        
        int ndim=0;

        while (classname.charAt(ndim) == '[') {
            ndim += 1;
        }

        int[] dimens = new int[ndim];

        for (int i=0; i<ndim; i += 1) {
             dimens[i] = -1;  // So that we can distinguish a null from a 0 length.
        }

        for (int i=0; i < ndim; i += 1) {
            dimens[i] = java.lang.reflect.Array.getLength(o);
            if (dimens[i] == 0) {
                return dimens;
            }
            if (i != ndim-1) {
                o  = ((Object[])o)[0];
                if (o == null) {
                    return dimens;
                }
            }
        }
        return dimens;
    }

    /** This routine returns the base class of an object.  This is just
      * the class of the object for non-arrays.
      */
    public static Class getBaseClass(Object o) {

         if (o == null) {
             return Void.TYPE;
         }

         String className = o.getClass().getName();

         int dims = 0;
         while (className.charAt(dims) == '[') {
             dims += 1;
         }

         if (dims == 0) {
             return o.getClass();
         }

         switch (className.charAt(dims) ) {

            case 'Z': return Boolean.TYPE;
            case 'B': return Byte.TYPE;
            case 'S': return Short.TYPE;
            case 'C': return Character.TYPE;
            case 'I': return Integer.TYPE;
            case 'J': return Long.TYPE;
            case 'F': return Float.TYPE;
            case 'D': return Double.TYPE;
            case 'L':
                try {
                    return Class.forName(className.substring(dims+1, className.length()-1));
                } catch (ClassNotFoundException e) {
                    return null;
                }
            default:  return null;
         }
    }
        
    /** This routine returns the size of the base element of an array.
      * @param o The array object whose base length is desired.
      * @return the size of the object in bytes, 0 if null, or 
      * -1 if not a primitive array.
      */
    public static int getBaseLength(Object o) {

         if (o == null) {
             return 0;
         }

         String className = o.getClass().getName();

         int dims = 0;

         while (className.charAt(dims) == '[') {
             dims += 1;
         }

         if (dims == 0) {
             return -1;
         }

         switch (className.charAt(dims) ) {

            case 'Z': return 1;
            case 'B': return 1;
            case 'S': return 2;
            case 'C': return 2;
            case 'I': return 4;
            case 'J': return 8;
            case 'F': return 4;
            case 'D': return 8;
            default: return -1;
         }
    }
        

    /** Create an array and populate it with a test pattern.
      *
      * @param baseType  The base type of the array.  This is expected to
      *                  be a numeric type, but this is not checked.
      * @param dims      The desired dimensions.
      * @return An array object populated with a simple test pattern.
      */

    public static Object generateArray(Class baseType, int[] dims) {

        // Generate an array and populate it with a test pattern of
        // data.

        Object x = java.lang.reflect.Array.newInstance(baseType, dims);
        testPattern(x,(byte)0);
        return x;
    }

    /** Just create a simple pattern cycling through valid byte values.
      * We use bytes because they can be cast to any other numeric type.
      * @param o      The array in which the test pattern is to be set.
      * @param start  The value for the first element.
      */
    public static byte testPattern(Object o, byte start) { 

        int[] dims = getDimensions(o);
        if (dims.length > 1) {
            for (int i=0; i < ((Object[])o).length; i += 1) {
                start = testPattern(((Object[]) o)[i], start);
            }

        } else if (dims.length == 1) {
            for (int i=0; i < dims[0]; i += 1) {
                java.lang.reflect.Array.setByte(o, i, start);
                start += 1;
            }
        }
        return start;
    }

    /** Generate a description of an array (presumed rectangular).
      * @param o The array to be described.
      */

    public static String arrayDescription(Object o) {

          Class base = getBaseClass(o);
          if (base == Void.TYPE) {
              return "NULL";
          }

          int[] dims = getDimensions(o);

          StringBuffer desc = new StringBuffer();

          // Note that all instances Class describing a given class are 
          // the same so we can use == here.

          if (base == Float.TYPE) {
              desc.append("float");
          } else if (base == Integer.TYPE) {
              desc.append("int");
          } else if (base == Double.TYPE) {
              desc.append("double");
          } else if (base == Short.TYPE) {
              desc.append("short");
          } else if (base == Long.TYPE) {
              desc.append("long");
          } else if (base == Character.TYPE) {
              desc.append("char");
          } else if (base == Byte.TYPE) {
              desc.append("byte");
          } else if (base == Boolean.TYPE) {
              desc.append("boolean");
          } else {
              desc.append(base.getName());
          }

          if (dims != null) {
              desc.append("[");
              for (int i=0; i<dims.length; i += 1) {
                  desc.append(""+dims[i]);
                  if (i < dims.length-1) {
                      desc.append(",");
                  }
              }
              desc.append("]");
          }
          return new String(desc);
     }

     /** Examine the structure of an array in detail.
       * @param o The array to be examined.
       */
     public static void examinePrimitiveArray(Object o) {
         String className = o.getClass().getName();

         // If we have a two-d array, or if the array is a one-d array
         // of Objects, then recurse over the next dimension.  We handle
         // Object specially because each element could itself be an array.
         if (className.substring(0,2).equals("[[") || 
             className.equals("[Ljava.lang.Object;") ) {
             System.out.print("[");
             for (int i=0; i< ((Object[])o).length; i += 1) {
                 examinePrimitiveArray(((Object[])o)[i]);
             }
             System.out.print("]");
         } else if (className.charAt(0) != '[') {
             System.out.println(className);
         } else {
             System.out.println("["+java.lang.reflect.Array.getLength(o)+"]"+
                                className.substring(1));
         }
     }

     /** Given an array of arbitrary dimensionality return
       * the array flattened into a single dimension.
       * @param input The input array.
       */

     public static Object flatten(Object input) {

         int[] dimens = getDimensions(input);
         if (dimens.length <= 1) {
             return input;
         }
         int size=1;
         for (int i=0; i<dimens.length; i += 1) {
             size *= dimens[i];
         }

         Object flat = Array.newInstance(getBaseClass(input), size);
    
         if (size == 0) {
             return flat;
         }

         int offset = 0;
         doFlatten(input, flat, offset);
         return flat;
    }

    /** This routine does the actually flattening of multi-dimensional
      * arrays.
      * @param input  The input array to be flattened.
      * @param output The flattened array.
      * @param offset The current offset within the output array.
      * @return       The number of elements within the array.
      */
    protected static int doFlatten(Object input, Object output, int offset) {

        String classname = input.getClass().getName();
        if (classname.charAt(0) != '[') {
            throw new RuntimeException("Attempt to flatten non-array");
        }
        int size = Array.getLength(input);

        if (classname.charAt(1) != '[') {
            System.arraycopy(input, 0, output, offset, size);
            return size;
        }
        int total = 0;
        Object[] xx = (Object[]) input;
        for(int i=0; i < size; i += 1) {
            int len = doFlatten(xx[i], output, offset+total);
            total += len;
        }
        return total;
    }

    /** Curl an input array up into a multi-dimensional array.
      *
      * @param input The one dimensional array to be curled.
      * @param dimens The desired dimensions
      * @return The curled array.
      */
    public static Object curl(Object input, int[] dimens) {

        String classname= input.getClass().getName();
        if (classname.charAt(0) != '['  || classname.charAt(1) == '[') {
            throw new RuntimeException("Attempt to curl non-1D array");
        }
        
        int size = Array.getLength(input);

        int test = 1;
        for (int i=0; i<dimens.length; i += 1) {
            test *= dimens[i];
        }

        if (test != size) {
            throw new RuntimeException("Curled array does not fit desired dimensions");
        }
        
        Class base = getBaseClass(input);

        Object newArray = Array.newInstance(base, dimens);

        int offset = 0;

        doCurl(input, newArray, dimens, offset);
        return newArray;

    }

    /** Do the curling of the 1-d to multi-d array.
      * @param input  The 1-d array to be curled.
      * @param output The multi-dimensional array to be filled.
      * @param dimens The desired output dimensions.
      * @param offset The current offset in the input array.
      * @return       The number of elements curled.
      */

    protected static int doCurl(Object input, Object output, 
                                int[] dimens, int offset) {

        if (dimens.length == 1) {
            System.arraycopy(input, offset, output, 0, dimens[0]);
            return dimens[0];
        }

        int total = 0;
        int[] xdimens = new int[dimens.length-1];
        for (int i=1; i<dimens.length; i +=1) {
            xdimens[i-1] = dimens[i];
        }

        for (int i=0; i<dimens[0]; i += 1) {            
            total += doCurl(input, ((Object[]) output)[i], xdimens, offset+total);
        }
        return total;
    }                    
}
