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

package visad;

import java.util.Arrays;

/**
 * A wrapper class for CoordinateSystems that will cache the last
 * values input and output values of the toReference and fromReference
 * methods.  If the inputs are the same as the last time these
 * methods were called, the previously calculated values are returned.
 *
 * @author Don Murray
 * @version $Revision: 1.8 $ $Date: 2009-11-30 14:47:10 $
 */
public class CachingCoordinateSystem extends CoordinateSystem {

  private CoordinateSystem myCS = null;
  private double[][] toRefDInput = null;
  private double[][] toRefDOutput = null;
  private double[][] fromRefDInput = null;
  private double[][] fromRefDOutput = null;
  private float[][] toRefFInput = null;
  private float[][] toRefFOutput = null;
  private float[][] fromRefFInput = null;
  private float[][] fromRefFOutput = null;

 public static boolean debug = false;
 public static boolean debugTime = false;
 public static boolean enabled = true;


  /**
   * Construct a new CachingCoordinateSystem that wraps around the input.
   * @param cs CoordinateSystem to wrap
   */
  public CachingCoordinateSystem(CoordinateSystem cs) 
      throws VisADException {
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
  public double[][] toReference(double[][] inputs) 
       throws VisADException {
    if (inputs == null) return inputs;
    long t1 = System.currentTimeMillis();
    try {
    if(!enabled)
	return  myCS.toReference(inputs);

    if (toRefDInput == null || toRefDInput.length != inputs.length
        || toRefDInput[0].length != inputs[0].length)  {
	toRefDInput = clone(inputs);
       toRefDOutput = myCS.toReference(inputs);

    } else {
      for(int i = 0; i<inputs.length; i++) {
        if (!Arrays.equals(inputs[i], toRefDInput[i])) {
          toRefDOutput = myCS.toReference(inputs);
          toRefDInput = clone(inputs);
          break;
        }
      }
    }
    return (double[][]) toRefDOutput.clone();
    } finally {
	long t2 = System.currentTimeMillis();
	if(debugTime)
	    System.err.println ("CCS.toReference(double) time:" + (t2-t1));
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
  public double[][] fromReference(double[][] inputs) 
       throws VisADException {
    if (inputs == null) return inputs;

    long t1 = System.currentTimeMillis();
    try {
    if(!enabled)
	return  myCS.fromReference(inputs);

    if (fromRefDInput == null || fromRefDInput.length != inputs.length
        || fromRefDInput[0].length != inputs[0].length)  {
       fromRefDInput = clone(inputs);
       fromRefDOutput = myCS.fromReference(inputs);
    } else {
      for(int i = 0; i<inputs.length; i++) {
        if (!Arrays.equals(inputs[i], fromRefDInput[i])) {
          fromRefDInput = clone(inputs);
          fromRefDOutput = myCS.fromReference(inputs);
          break;
        }
      }
    }
    return (double[][]) fromRefDOutput.clone();
    } finally {
	long t2 = System.currentTimeMillis();
	if(debugTime)
	    System.err.println ("CCS.fromReference(double) time:" + (t2-t1));
    }
  }


    private static float[][]clone(float[][]input) {
	float[][] output = (float[][])input.clone();
	for(int i=0;i<input.length;i++) {
	    output[i] = (float[]) input[i].clone();
	}
	return output;
    }


    private static  double[][]clone(double[][]input) {
	double[][] output = (double[][])input.clone();
	for(int i=0;i<input.length;i++) {
	    output[i] = (double[]) input[i].clone();
	}
	return output;
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
  public float[][] toReference(float[][] inputs) 

       throws VisADException {
    if (inputs == null) return inputs;

    long t1 = System.currentTimeMillis();
    try {
    if(!enabled)
	return  myCS.toReference(inputs);

    boolean hit = true;
    if(debug) {
	System.err.println (this+" CCS.toReference:" + inputs[0].length);
    }
    if (toRefFInput == null || toRefFInput.length != inputs.length
        || toRefFInput[0].length != inputs[0].length)  {
	if(debug)  {
	    System.err.println ("\ttotal miss");
	    if(toRefFInput!=null) 
		System.err.println ("\t\t" + toRefFInput.length + " " + toRefFInput[0].length);
	}
        hit = false;
        toRefFInput = clone(inputs);
        toRefFOutput = myCS.toReference(inputs);
    } else {
      for(int i = 0; i<inputs.length; i++) {
        if (!Arrays.equals(inputs[i], toRefFInput[i])) {
	    if(debug) 
		System.err.println ("\tarrays not equal");
	    hit = false;
            toRefFInput = clone(inputs);
            toRefFOutput = myCS.toReference(inputs);
            break;
        }
      }
    }
    if(debug && hit) {
	System.err.println ("\twas in cache");
    }
    return (float[][]) toRefFOutput.clone();

    } finally {
	long t2 = System.currentTimeMillis();
	if(debugTime)
	    System.err.println ("CCS.toReference(float) time:" + (t2-t1));
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
  public float[][] fromReference(float[][] inputs) 
       throws VisADException {
    if (inputs == null) return inputs;

    long t1 = System.currentTimeMillis();
    try {
    if(!enabled)
	return  myCS.fromReference(inputs);
    boolean hit = true;
    if(debug) {
	System.err.println (this+" CCS.fromReference:" + inputs[0].length);
    }

    if (fromRefFInput == null || fromRefFInput.length != inputs.length
        || fromRefFInput[0].length != inputs[0].length)  {
	if(debug)  {
	    System.err.println ("\ttotal miss");
	    if(fromRefFInput!=null) 
		System.err.println ("\t\t" + fromRefFInput.length + " " + fromRefFInput[0].length);
	}
        hit = false;

       fromRefFInput = clone(inputs);
       //       fromRefFInput = (float[][]) inputs.clone();
       fromRefFOutput = myCS.fromReference(inputs);
    } else {
      for(int i = 0; i<inputs.length; i++) {
        if (!Arrays.equals(inputs[i], fromRefFInput[i])) {
	    //          fromRefFInput = (float[][]) inputs.clone();
          fromRefFInput = clone(inputs);
          fromRefFOutput = myCS.fromReference(inputs);
	    if(debug) 
		System.err.println ("\tarrays not equal");
	    hit = false;

          break;
        }
      }
    }
    if(debug && hit) {
	System.err.println ("\twas in cache");
    }
    return (float[][]) fromRefFOutput.clone();
    } finally {
	long t2 = System.currentTimeMillis();
	if(debugTime)
	    System.err.println ("CCS.fromReference(float) time:" + (t2-t1));
    }
  }

  /** 
   * Check for equality of CoordinateSystem objects 
   * @param  obj  other object in question
   * @return  true if the object in question is a CachingCoordinateSystem
   *          and it's CS is equal this object's CS
   */
  public boolean equals (Object obj) 
  {
      if (!(obj instanceof CachingCoordinateSystem))
          return false;
      CachingCoordinateSystem that = (CachingCoordinateSystem) obj;
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
