//
// DataImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.*;
import java.rmi.*;
import visad.java2d.DisplayImplJ2D;

/**
(Fulker)
   DataImpl is the superclass for VisAD's data hierarchy, inheriting
   the Data interface.  Data objects are immutable except for the range
   values of Field objects.<p>

   VisAD Data objects are finite approximations to math objects
   that include scalars, tuples (i.e., n-dimensional vectors), functions,
   and certain forms of sets.  Hence, all Data objects possess a MathType,
   which identifies the corresponding concept and is <b>not</b> a synonym
   for the data class, even though the class names for a Data object and
   its corresponding MathType object (Set and SetType, e.g.) may be
   similar.  In order to approximate their corresponding mathematical
   entities, Data objects may use text strings or finite representations
   of real numbers.  Also, any Data object may take the value 'missing',
   and any sub-object of a Data object may take the value 'missing'.<p>

   All of the Java arithmetical operations are defined for Data objects,
   to the extent that they make sense for the types involved.<p>
(/Fulker)<p>

   DataImpl is the abstract superclass of the VisAD data hierarchy,
   inheriting the Data interface.  Data objects are immutable except
   for the range values of Field and FlatField objects.<P>

   Data objects are various forms of approximations to real numbers,
   text strings, vectors, sets and functions.  Any Data object may
   take the value 'missing', and any sub-object of a Data object may
   take the value 'missing'.  All Data objects have a MathType, which
   is a mathematical type rather than a synonym for class.  All of
   the Java arithmetical operations are defined for Data objects, to
   the extent that they make sense for the types involved.<P>
*/
public abstract class DataImpl extends ThingImpl
       implements Data, Cloneable {

  /** each VisAD data object has a VisAD mathematical type */
  MathType Type;

  /** parent is used to propogate notifyReferences;
      parent DataImpl object if parent is local;
      null if parent is remote;
      i.e., notifyReferences does not propogate to remote parents;
      only a single parent is supported - multiple parents are
      not correctly notified of data changes */
  private transient DataImpl parent;

  public DataImpl(MathType type) {
    Type = type;
    parent = null;
  }

  /** DataImpl.local() returns 'this'
      RemoteDataImpl.local() returns 'AdaptedData' */
  public DataImpl local() {
    return this;
  }

  void setParent(DataImpl p) {
    parent = p;
  }

  public MathType getType() {
/* DEBUG
    System.out.println("DataImpl " + " getType " +
                       "(" + System.getProperty("os.name") + ")");
*/
    return Type;
  }

  /** notify local DataReferenceImpl-s that this DataImpl has changed;
      incTick in RemoteDataImpl for RemoteDataReferenceImpl-s;
      would like 'default' visibility here, but must be declared
      'public' because it is defined in the Data interface */
  public void notifyReferences()
         throws VisADException, RemoteException {
    super.notifyReferences();
    // recursively propogate data change to parent
    if (parent != null) parent.notifyReferences();
  }

  /** binary operations */
  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
   /* BINARY - TDR May 1998
    throw new TypeException("DataImpl.binary");
    */
    MathType dtype = data.getType();

    MathType new_type = Type.binary( dtype, op, new Vector() );
    return binary( data, op, new_type, sampling_mode, error_mode );
  }

  public Data binary( Data data, int op, MathType new_type,
                      int sampling_mode, int error_mode )
              throws VisADException, RemoteException {
    throw new TypeException("DataImpl.binary");
  }

  /** a list of binary operations using default modes for
      sampling and error estimation */
  public Data add(Data data) throws VisADException, RemoteException {
    return binary(data, ADD, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data subtract(Data data) throws VisADException, RemoteException {
    return binary(data, SUBTRACT, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data multiply(Data data) throws VisADException, RemoteException {
    return binary(data, MULTIPLY, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data divide(Data data) throws VisADException, RemoteException {
    return binary(data, DIVIDE, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data pow(Data data) throws VisADException, RemoteException {
    return binary(data, POW, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data max(Data data) throws VisADException, RemoteException {
    return binary(data, MAX, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data min(Data data) throws VisADException, RemoteException {
    return binary(data, MIN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data atan2(Data data) throws VisADException, RemoteException {
    return binary(data, ATAN2, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data atan2Degrees(Data data) throws VisADException, RemoteException {
    return binary(data, ATAN2_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data remainder(Data data) throws VisADException, RemoteException {
    return binary(data, REMAINDER, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /** a list of binary operations supporting non-default modes for
      sampling and error estimation */
  public Data add(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, ADD, sampling_mode, error_mode);
  }

  public Data subtract(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, SUBTRACT, sampling_mode, error_mode);
  }

  public Data multiply(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, MULTIPLY, sampling_mode, error_mode);
  }

  public Data divide(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, DIVIDE, sampling_mode, error_mode);
  }

  public Data pow(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, POW, sampling_mode, error_mode);
  }

  public Data max(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, MAX, sampling_mode, error_mode);
  }

  public Data min(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, MIN, sampling_mode, error_mode);
  }

  public Data atan2(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, ATAN2, sampling_mode, error_mode);
  }

  public Data atan2Degrees(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, ATAN2_DEGREES, sampling_mode, error_mode);
  }

  public Data remainder(Data data, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return binary(data, REMAINDER, sampling_mode, error_mode);
  }


  /** unary operations */
  public Data unary(int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    /*-  TDR June 1998
    throw new TypeException("DataImpl.unary");
    */
    MathType new_type = Type.unary( op, new Vector() );
    return unary( op, new_type, sampling_mode, error_mode );
  }

  /*- TDR June 1998  */
  public Data unary( int op, MathType new_type, int sampling_mode, int error_mode )
              throws VisADException, RemoteException {
    throw new TypeException("DataImpl: unary");
  }

  /* WLH 5 Sept 98 */
  public Data changeMathType(MathType new_type)
         throws VisADException, RemoteException {
    return unary(NOP, new_type, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /** a list of unary operations using default modes for
      sampling and error estimation */
  public Data abs() throws VisADException, RemoteException {
    return unary(ABS, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data acos() throws VisADException, RemoteException {
    return unary(ACOS, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data acosDegrees() throws VisADException, RemoteException {
    return unary(ACOS_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data asin() throws VisADException, RemoteException {
    return unary(ASIN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data asinDegrees() throws VisADException, RemoteException {
    return unary(ASIN_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data atan() throws VisADException, RemoteException {
    return unary(ATAN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data atanDegrees() throws VisADException, RemoteException {
    return unary(ATAN_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data ceil() throws VisADException, RemoteException {
    return unary(CEIL, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data cos() throws VisADException, RemoteException {
    return unary(COS, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data cosDegrees() throws VisADException, RemoteException {
    return unary(COS_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data exp() throws VisADException, RemoteException {
    return unary(EXP, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data floor() throws VisADException, RemoteException {
    return unary(FLOOR, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data log() throws VisADException, RemoteException {
    return unary(LOG, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data rint() throws VisADException, RemoteException {
    return unary(RINT, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data round() throws VisADException, RemoteException {
    return unary(ROUND, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data sin() throws VisADException, RemoteException {
    return unary(SIN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data sinDegrees() throws VisADException, RemoteException {
    return unary(SIN_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data sqrt() throws VisADException, RemoteException {
    return unary(SQRT, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data tan() throws VisADException, RemoteException {
    return unary(TAN, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data tanDegrees() throws VisADException, RemoteException {
    return unary(TAN_DEGREES, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  public Data negate() throws VisADException, RemoteException {
    return unary(NEGATE, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /** a list of unary operations supporting non-default modes for
      sampling and error estimation */
  public Data abs(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ABS, sampling_mode, error_mode);
  }

  public Data acos(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ACOS, sampling_mode, error_mode);
  }

  public Data acosDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ACOS_DEGREES, sampling_mode, error_mode);
  }

  public Data asin(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ASIN, sampling_mode, error_mode);
  }

  public Data asinDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ASIN_DEGREES, sampling_mode, error_mode);
  }

  public Data atan(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ATAN, sampling_mode, error_mode);
  }

  public Data atanDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ATAN_DEGREES, sampling_mode, error_mode);
  }

  public Data ceil(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(CEIL, sampling_mode, error_mode);
  }

  public Data cos(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(COS, sampling_mode, error_mode);
  }

  public Data cosDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(COS_DEGREES, sampling_mode, error_mode);
  }

  public Data exp(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(EXP, sampling_mode, error_mode);
  }

  public Data floor(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(FLOOR, sampling_mode, error_mode);
  }

  public Data log(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(LOG, sampling_mode, error_mode);
  }

  public Data rint(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(RINT, sampling_mode, error_mode);
  }

  public Data round(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(ROUND, sampling_mode, error_mode);
  }

  public Data sin(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(SIN, sampling_mode, error_mode);
  }

  public Data sinDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(SIN_DEGREES, sampling_mode, error_mode);
  }

  public Data sqrt(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(SQRT, sampling_mode, error_mode);
  }

  public Data tan(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(TAN, sampling_mode, error_mode);
  }

  public Data tanDegrees(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(TAN_DEGREES, sampling_mode, error_mode);
  }

  public Data negate(int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return unary(NEGATE, sampling_mode, error_mode);
  }

  /** returns new binary operator equivalent to op with
      order of arguments reversed */
  static int invertOp(int op) throws VisADException {
    switch(op) {
      case ADD:
        return ADD;
      case SUBTRACT:
        return INV_SUBTRACT;
      case INV_SUBTRACT:
        return SUBTRACT;
      case MULTIPLY:
        return MULTIPLY;
      case DIVIDE:
        return INV_DIVIDE;
      case INV_DIVIDE:
        return DIVIDE;
      case POW:
        return INV_POW;
      case INV_POW:
        return POW;
      case MAX:
        return MAX;
      case MIN:
        return MIN;
      case ATAN2:
        return INV_ATAN2;
      case ATAN2_DEGREES:
        return INV_ATAN2_DEGREES;
      case INV_ATAN2:
        return ATAN2;
      case INV_ATAN2_DEGREES:
        return ATAN2_DEGREES;
      case REMAINDER:
        return INV_REMAINDER;
      case INV_REMAINDER:
        return REMAINDER;
    }
    throw new ArithmeticException("DataImpl.invertOp: illegal operation");
  }

  // WLH 18 March 2000
  private static DisplayImplJ2D rdisplay = null;
  private static Object lock = new Object();

  // WLH 18 March 2000
  public class Syncher extends Object implements DisplayListener {
    Syncher() {
      rdisplay.addDisplayListener(this);
      rdisplay.enableAction();
      try {
        synchronized (this) {
          this.wait();
        }
      }
      catch(InterruptedException e) {
      }
      rdisplay.removeDisplayListener(this);
    }

    public void displayChanged(DisplayEvent e)
           throws VisADException, RemoteException {
      if (e.getId() == DisplayEvent.TRANSFORM_DONE) {
        synchronized (this) {
          this.notify();
        }
      }
    }
  }

  // WLH 18 March 2000
  public double[][] computeRanges(RealType[] reals)
         throws VisADException, RemoteException {
    synchronized (lock) {
      if (rdisplay == null) {
        // construct offscreen dummy display
        rdisplay = new DisplayImplJ2D("dummy", 4, 4);
      }
      if (reals == null || reals.length == 0) return null;
      int n = reals.length;
      ScalarMap[] maps = new ScalarMap[n];
      for (int i=0; i<n; i++) {
        maps[i] = new ScalarMap(reals[i], Display.Shape);
        rdisplay.addMap(maps[i]);
      }
      rdisplay.disableAction();
      DataReference ref = new DataReferenceImpl("dummy");
      ref.setData(this);
      rdisplay.addReference(ref);
      new Syncher(); // wait for TRANSFORM_DONE
      double[][] ranges = new double[n][];
      for (int i=0; i<n; i++) {
        ranges[i] = maps[i].getRange();
      }
      rdisplay.removeReference(ref);
      rdisplay.clearMaps();
      return ranges;
    }
  }

  /** compute the ranges of values of each RealType in 'this'
      that is mapped in the Display associated with type;
      this is the top-level definition of computeRanges - it works
      by recursively invoking the next definition of computeRanges;
      would like 'default' visibility here, but must be declared
      'public' because it is defined in the Data interface;
      n = display.getScalarCount() */
  public DataShadow computeRanges(ShadowType type, int n)
         throws VisADException, RemoteException {
    double[][] ranges = new double[2][n];
    for (int i=0; i<n; i++) {
      ranges[0][i] = Double.MAX_VALUE; // init minimums
      ranges[1][i] = -Double.MAX_VALUE; // init maximums
    }
    DataShadow shadow = new DataShadow(ranges);
    return computeRanges(type, shadow);
  }

  /** used by RealTuple and SampledSet (was FieldImpl and FlatField) to
      compute ranges for RealTupleType Reference */
  DataShadow computeReferenceRanges(
             ShadowRealTupleType shad_type, CoordinateSystem coord_in,
             Unit[] units_in, DataShadow shadow,
             ShadowRealTupleType shad_ref, double[][] ranges)
             throws VisADException {
    RealTupleType type = (RealTupleType) shad_type.Type;
    RealTupleType ref = (RealTupleType) shad_ref.Type;
    int n = ranges[0].length;
    int len = 1;
    // indices is a 'base-5 integer' with n quints
    int[] indices = new int[n];
    for (int i=0; i<n; i++) {
      len = 5 * len;
      indices[i] = 0;
    }
    // len = 5 ^ n;
    double[][] vals = new double[n][len];
    for (int j=0; j<len; j++) {
      for (int i=0; i<n; i++) {
        switch(indices[i]) {
          case 0:
            vals[i][j] = ranges[0][i];
            break;
          case 1:
            vals[i][j] = 0.75 * ranges[0][i] + 0.25 * ranges[1][i];
            break;
          case 2:
            vals[i][j] = 0.5 * (ranges[0][i] + ranges[1][i]);
            break;
          case 3:
            vals[i][j] = 0.25 * ranges[0][i] + 0.75 * ranges[1][i];
            break;
          case 4:
            vals[i][j] = ranges[1][i];
            break;
        }
      }
      // increment 'base-5 integer' in indices array
      for (int i=0; i<n; i++) {
        indices[i]++;
        if (indices[i] == 5) {
          indices[i] = 0;
        }
        else {
          break;
        }
      }
    }

    // vals are the vertices of the n-dimensional box defined by ranges;
    // tranform them
    vals = CoordinateSystem.transformCoordinates(
                   ref, ref.getCoordinateSystem(), ref.getDefaultUnits(), null,
                   type, coord_in, units_in, null, vals);
    // mix vals into shadow.ranges
    for (int i=0; i<n; i++) {
      double min = Double.MAX_VALUE; // init minimum
      double max = -Double.MAX_VALUE; // init maximum
      for (int j=0; j<len; j++) {
        double val = vals[i][j];
        if (val == val) {
          min = Math.min(min, val);
          max = Math.max(max, val);
        }
      }
      int index = ((ShadowRealType) shad_ref.getComponent(i)).getIndex();
      if (index >= 0) {
        if (min == min) {
          shadow.ranges[0][index] = Math.min(shadow.ranges[0][index], min);
        }
        if (max == max) {
          shadow.ranges[1][index] = Math.max(shadow.ranges[1][index], max);
        }
      }
    }
    return shadow;
  }

  /** would like 'default' visibility here, but must be declared
      'public' because it is defined in the Data interface */
  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException {
    return this;
  }


  /** for JPython */
  public Data __add__(Data data) {
    try {
      return add(data);
    }
    catch (VisADException e) {
      return null;
    }
    catch (RemoteException e) {
      return null;
    }
  }

  public Data __sub__(Data data) {
    try {
      return subtract(data);
    }
    catch (VisADException e) {
      return null;
    }
    catch (RemoteException e) {
      return null;
    }
  }

  public Data __mul__(Data data) {
    try {
      return multiply(data);
    }
    catch (VisADException e) {
      return null;
    }
    catch (RemoteException e) {
      return null;
    }
  }

  public Data __div__(Data data) {
    try {
      return divide(data);
    }
    catch (VisADException e) {
      return null;
    }
    catch (RemoteException e) {
      return null;
    }
  }

  public Data __pow__(Data data) {
    try {
      return pow(data);
    }
    catch (VisADException e) {
      return null;
    }
    catch (RemoteException e) {
      return null;
    }
  }

  public Data __mod__(Data data) {
    try {
      return remainder(data);
    }
    catch (VisADException e) {
      return null;
    }
    catch (RemoteException e) {
      return null;
    }
  }

  public Data __neg__() {
    try {
      return negate();
    }
    catch (VisADException e) {
      return null;
    }
    catch (RemoteException e) {
      return null;
    }
  }
  /** end of for JPython */


  /** a VisAD adaptation of clone that works for local or remote Data;
      DataImpl.dataClone returns clone; RemoteDataImpl.dataClone
      returns clone inherited from UnicastRemoteObject */
  public Object dataClone() {
    return clone();
  }

  /** a method to copy any data object */
  public abstract Object clone();

  public String toString() {
    try {
      return longString("");
    }
    catch(VisADException e) {
      return e.toString();
    }
    catch(RemoteException e) {
      return e.toString();
    }
  }

  /** generates a longer string than generated by toString */
  public String longString()
         throws VisADException, RemoteException {
    return longString("");
  }

  /** generates a longer string than generated by toString,
      indented by pre (a string of blanks) */
  public String longString(String pre)
         throws VisADException, RemoteException {
    throw new TypeException("DataImpl.longString");
  }

  public static void main(String args[])
         throws VisADException, RemoteException {

    RealType[] types3d = {RealType.Latitude, RealType.Longitude, RealType.Radius};
    RealTupleType earth_location3d = new RealTupleType(types3d);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType grid_tuple = new FunctionType(earth_location3d, radiance);

    int size3d = 6;
    float level = 2.5f;
    FlatField grid3d = FlatField.makeField(grid_tuple, size3d, false);

    RealType[] types = {RealType.Latitude, RealType.Longitude, RealType.Radius,
                        vis_radiance, ir_radiance, RealType.Time};
    double[][] ranges = grid3d.computeRanges(types);
    for (int i=0; i<ranges.length; i++) {
      System.out.println(types[i] + ": " + ranges[i][0] + " to " + ranges[i][1]);
    }
    System.out.println(" ");

    FunctionType func = new FunctionType(radiance, RealType.Time);
    Integer2DSet fset = new Integer2DSet(2, 2);
    FlatField ff = new FlatField(func, fset);
    ff.setSamples(new float[][] {{0.0f, -1.0f, 1.0f, 2.0f}});
    ranges = ff.computeRanges(types);
    for (int i=0; i<ranges.length; i++) {
      System.out.println(types[i] + ": " + ranges[i][0] + " to " + ranges[i][1]);
    }
    System.exit(0);
  }

}

