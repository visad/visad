//
// CachingCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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
 * @version $Revision: 1.5 $ $Date: 2007-01-17 22:57:00 $
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
    if (toRefDInput == null || toRefDInput.length != inputs.length
        || toRefDInput[0].length != inputs[0].length)  {
       toRefDOutput = myCS.toReference(inputs);
       toRefDInput = (double[][]) inputs.clone();
    } else {
      for(int i = 0; i<inputs.length; i++) {
        if (!Arrays.equals(inputs[i], toRefDInput[i])) {
          toRefDOutput = myCS.toReference(inputs);
          toRefDInput = (double[][]) inputs.clone();
          break;
        }
      }
    }
    return (double[][]) toRefDOutput.clone();
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
    if (fromRefDInput == null || fromRefDInput.length != inputs.length
        || fromRefDInput[0].length != inputs[0].length)  {
       fromRefDOutput = myCS.fromReference(inputs);
       fromRefDInput = (double[][]) inputs.clone();
    } else {
      for(int i = 0; i<inputs.length; i++) {
        if (!Arrays.equals(inputs[i], fromRefDInput[i])) {
          fromRefDOutput = myCS.fromReference(inputs);
          fromRefDInput = (double[][]) inputs.clone();
          break;
        }
      }
    }
    return (double[][]) fromRefDOutput.clone();
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
    if (toRefFInput == null || toRefFInput.length != inputs.length
        || toRefFInput[0].length != inputs[0].length)  {
       toRefFOutput = myCS.toReference(inputs);
       toRefFInput = (float[][]) inputs.clone();
    } else {
      for(int i = 0; i<inputs.length; i++) {
        if (!Arrays.equals(inputs[i], toRefFInput[i])) {
          toRefFOutput = myCS.toReference(inputs);
          toRefFInput = (float[][]) inputs.clone();
          break;
        }
      }
    }
    return (float[][]) toRefFOutput.clone();
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
    if (fromRefFInput == null || fromRefFInput.length != inputs.length
        || fromRefFInput[0].length != inputs[0].length)  {
       fromRefFOutput = myCS.fromReference(inputs);
       fromRefFInput = (float[][]) inputs.clone();
    } else {
      for(int i = 0; i<inputs.length; i++) {
        if (!Arrays.equals(inputs[i], fromRefFInput[i])) {
          fromRefFOutput = myCS.fromReference(inputs);
          fromRefFInput = (float[][]) inputs.clone();
          break;
        }
      }
    }
    return (float[][]) fromRefFOutput.clone();
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
