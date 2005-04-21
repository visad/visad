//
// RealTuple.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;

/**
   RealTuple is the VisAD data class for vectors in R^n for n>0.
   RealTuple objects are immutable.<P>
*/
public class RealTuple
  extends	Tuple
  implements	RealTupleIface
{

  private CoordinateSystem TupleCoordinateSystem;

  private boolean checkRealUnits;

  /*
   * Simply copies of Unit-s from Real tupleComponents.
   * Will be null if RealTuple(RealTupleType) constructor is used.
   */
  private Unit[] TupleUnits = null;

  /** 
   * construct a RealTuple object with the missing value 
   * @param type  RealTupleType of this RealTuple
   */
  public RealTuple(RealTupleType type) {
    super(type);
    TupleCoordinateSystem = type.getCoordinateSystem();
    if (tupleComponents != null) {
      int n = tupleComponents.length;
      TupleUnits = new Unit[n];
      for (int i=0; i<n; i++) TupleUnits[i] = null;
    }
  }

  /** 
   * construct a RealTuple according to an array of Real objects;
   * coordinate_system may be null; otherwise coordinate_system.getReference() 
   * must equal type.getCoordinateSystem.getReference() 
   *
   * @param type  RealTupleType of this RealTuple
   * @param reals array of reals
   * @param coord_sys  CoordinateSystem for this RealTuple
   */
  public RealTuple(RealTupleType type, Real[] reals, CoordinateSystem coord_sys)
         throws VisADException, RemoteException {
      this(type, reals, coord_sys, null, true);
  }

  /** 
   * Construct a RealTuple according to an array of Real objects;
   * coordinate_system may be null; otherwise coordinate_system.getReference() 
   * must equal type.getCoordinateSystem.getReference() 
   *
   * @param type  RealTupleType of this RealTuple
   * @param reals array of reals
   * @param coord_sys  CoordinateSystem for this RealTuple
   * @param units array of Units corresponding to the array of Reals.
   * @param checkUnits  true to make sure the units of the Reals are convertible
   *                    with the RealType units.  <b>NB: setting this to false
   *                    can cause problems if the units are not convertible.
   *                    Only do this if you know what you are doing.</b>
   */
  public RealTuple(RealTupleType type, Real[] reals, CoordinateSystem coord_sys,
                   Unit[] units, boolean checkUnits)
         throws VisADException, RemoteException {
    super(type, reals, false);  // copy == false because Reals are immutable
    TupleUnits = units;
    checkRealUnits = checkUnits;
    init_coord_sys(coord_sys);
  }

  /** 
   * construct a RealTuple according to an array of Real objects 
   * @param reals array of reals
   */
  public RealTuple(Real[] reals)
         throws VisADException, RemoteException {
    this((RealTupleType)buildTupleType(reals), reals, null, 
          buildTupleUnits(reals), false);
  }

  /** 
   * Construct a RealTuple according to a RealTupleType and a double array 
   * @param type  RealTupleType of this RealTuple
   * @param values values for each component.  Units are the default units
   *               of the RealTupleType components.
   */
  public RealTuple(RealTupleType type, double[] values)
         throws VisADException, RemoteException {
    this(type, buildRealArray(type, values), null, type.getDefaultUnits(), false);
  }

  /** initialize TupleCoordinateSystem and TupleUnits */
  private void init_coord_sys(CoordinateSystem coord_sys)
          throws VisADException {
    CoordinateSystem cs = ((RealTupleType) Type).getCoordinateSystem();
    if (coord_sys == null) {
      TupleCoordinateSystem = cs;
    }
    else {
      if (cs == null || !cs.getReference().equals(coord_sys.getReference())) {
        throw new CoordinateSystemException("RealTuple: coord_sys " +
                                            coord_sys.getReference() +
                                            " must match" +
                                            " Type.DefaultCoordinateSystem " +
                                            (cs == null ? null :
                                             cs.getReference()));
      }
      TupleCoordinateSystem = coord_sys;
    }
    if (TupleCoordinateSystem != null &&
        !Unit.canConvertArray(TupleCoordinateSystem.getCoordinateSystemUnits(),
                              ((RealTupleType) Type).getDefaultUnits())) {
      throw new UnitException("RealTuple: CoordinateSystem Units must be " +
                              "convertable with Type default Units");
    }

    if (TupleUnits == null) {
      int n = tupleComponents.length;
      TupleUnits = new Unit[n];
      for (int i=0; i<n; i++) {
           TupleUnits[i] = ((Real) tupleComponents[i]).getUnit();
      }
    }

    if (checkRealUnits) {
        if(!Unit.canConvertArray(TupleUnits,
                                 ((RealTupleType) Type).getDefaultUnits())) {
          throw new UnitException("Tuple: Units must be convertable with " +
                                  "Type default Units");
        }
    }

    if(TupleCoordinateSystem != null &&
       !Unit.canConvertArray(TupleCoordinateSystem.getCoordinateSystemUnits(),
                             TupleUnits)) {
      throw new UnitException("Tuple: Units must be convertable with " +
                              "CoordinateSystem Units");
    }
  }

  private static Real[] buildRealArray(RealTupleType type, double[] values)
                        throws VisADException {
    Real[] reals = new Real[values.length];
    for (int i=0; i<values.length; i++) {
      reals[i] = new Real((RealType) type.getComponent(i), values[i]);
    }
    return reals;
  }

  private static Unit[] buildTupleUnits(Real[] reals) throws VisADException {
    if (reals == null || reals.length == 0) return (Unit[]) null;
    int n = reals.length;
    Unit[] units = new Unit[reals.length];
    for (int i=0; i<n; i++) units[i] = reals[i].getUnit();
    return units;
  }

  /**
   * Adds a listener for changes to this instance.  Because instances of this
   * class don't change, this method does nothing.
   *
   * @param listener                     The listener for changes.
   */
  public final void addReference(ThingReference listener) {
  }

  /**
   * Removes a listener for changes to this instance.  Because instances of this
   * class don't change, this method does nothing.
   *
   * @param listener                    The change listener to be removed.
   */
  public final void removeReference(ThingReference listener) {
  }

  /**
   * Get the values of the Real components
   * @return double array of the values of each Real component
   */
  public double[] getValues() {
    int n = getDimension();
    double[] values = new double[n];
    for (int i=0; i<n; i++) values[i] = ((Real) tupleComponents[i]).getValue();
    return values;
  }

  /** get Units of Real components */
  public Unit[] getTupleUnits() {
    return Unit.copyUnitsArray(TupleUnits);
  }

  /** get ErrorEstimates of Real components */
  public ErrorEstimate[] getErrors()
         throws VisADException, RemoteException {
    int n = getDimension();
    ErrorEstimate[] errors = new ErrorEstimate[n];
    for (int i=0; i<n; i++) errors[i] = ((Real) getComponent(i)).getError();
    return errors;
  }

  /** get CoordinateSystem */
  public CoordinateSystem getCoordinateSystem() {
    return TupleCoordinateSystem;
  }

  /*- TDR  May 1998
  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
   */
  public Data binary(Data data, int op, MathType new_type,
                     int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    /* BINARY - TDR May 28, 1998  */
    MathType m_type;
    if ( new_type == null ) {
      throw new TypeException("binary: new_type may not be null" );
    }
    /* BINARY - end  */
    if (data instanceof RealTuple) {
      if (!Type.equalsExceptName(data.getType())) {
        throw new TypeException("RealTuple.binary: types don't match");
      }
      /*- TDR May  1998 */
      if ( !Type.equalsExceptName(new_type) ) {
        throw new TypeException("RealTuple.binary: new_type doesn't match return type");
      }
      /*- end  */
      if (isMissing() || data.isMissing()) {
        return new RealTuple((RealTupleType) new_type);
      }
      double[][] vals = new double[tupleComponents.length][1];
      for (int j=0; j<tupleComponents.length; j++) {
        vals[j][0] = ((Real) ((RealTuple) data).getComponent(j)).getValue();
      }
      ErrorEstimate[] errors_out = new ErrorEstimate[tupleComponents.length];
      vals = CoordinateSystem.transformCoordinates(
                      (RealTupleType) Type, TupleCoordinateSystem,
                      TupleUnits, errors_out,
                      (RealTupleType) data.getType(),
                      ((RealTuple) data).getCoordinateSystem(),
                      ((RealTuple) data).getTupleUnits(),
                      ((RealTuple) data).getErrors(), vals);
      Real[] reals = new Real[tupleComponents.length];
      for (int j=0; j<tupleComponents.length; j++) {
        Real real = new Real((RealType) ((RealTupleType) Type).getComponent(j),
                             vals[j][0], TupleUnits[j], errors_out[j]);
        /*- TDR May 1998 */
        m_type = ((RealTupleType)new_type).getComponent(j);
        /*- end */
        reals[j] =
        /*- TDR May 1998
          (Real) tupleComponents[j].binary(real, op, sampling_mode, error_mode);
         */
          (Real) tupleComponents[j].binary(real, op, m_type, sampling_mode, error_mode);
      }
      /* BINARY - TDR May 28, 1998
      return new RealTuple((RealTupleType)Type, reals, TupleCoordinateSystem);
      */
      return new RealTuple((RealTupleType) new_type, reals, null );
    }
    else if (data instanceof TupleIface) {
      throw new TypeException("RealTuple.binary: types don't match");
    }
    else if (data instanceof Real) {
      if (isMissing() || data.isMissing()) {
        return new RealTuple((RealTupleType) Type);
      }
      Real[] reals = new Real[tupleComponents.length];
      for (int j=0; j<tupleComponents.length; j++) {
        m_type = ((RealTupleType)new_type).getComponent(j);

        /*- TDR May 1998
        reals[j] = (Real) tupleComponents[j].binary(data, op, sampling_mode,
                                                    error_mode);
        */
        reals[j] = (Real) tupleComponents[j].binary(data, op, m_type,
                                             sampling_mode, error_mode);

      }
      /*- TDR May 1998
      return new RealTuple((RealTupleType) Type, reals, TupleCoordinateSystem);
      */
      return new RealTuple((RealTupleType) new_type, reals, null );
    }
    else if (data instanceof Field) {
      /*- TDR June 3, 1998 */
      if ( !(data.getType()).equalsExceptName(new_type) ) {
        throw new TypeException();
      }
        return data.binary(this, invertOp(op), new_type, sampling_mode, error_mode);
      /*- end  */
    }
    else {
      throw new TypeException("RealTuple.binary");
    }
  }

  /*-  TDR  July 1998
  public Data unary(int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
  */
  public Data unary(int op, MathType new_type,
                    int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if ( new_type == null ) {
      throw new TypeException("unary: new_type may not be null");
    }

    if ( !Type.equalsExceptName(new_type)) {
      throw new TypeException("unary: new_type doesn't match return type");
    }
    RealTupleType RT_type= (RealTupleType)new_type;

    if (isMissing()) return new RealTuple((RealTupleType) Type);
    Real[] reals = new Real[tupleComponents.length];
    for (int j=0; j<tupleComponents.length; j++) {
      reals[j] = (Real) tupleComponents[j].unary(op, RT_type.getComponent(j),
                                                 sampling_mode, error_mode);
    }
    return new RealTuple((RealTupleType) new_type, reals, TupleCoordinateSystem);
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    shadow = super.computeRanges(type, shadow);
    ShadowRealTupleType shad_ref = ((ShadowRealTupleType) type).getReference();
    if (isMissing() || shad_ref == null) return shadow;
    int n = tupleComponents.length;
    // computeRanges for Reference RealTypes
    double[][] ranges = new double[2][n];
    for (int i=0; i<n; i++) {
      double value = ((Real) tupleComponents[i]).getValue();

      // WLH 20 Nov 2001
      Unit unit =
        ((RealType) ((RealTupleType) Type).getComponent(i)).getDefaultUnit();
      if (unit != null && !unit.equals(TupleUnits[i])) {
        value = unit.toThis(value, TupleUnits[i]);
      }

      if (value != value) return shadow;
      ranges[0][i] = value;
      ranges[1][i] = value;
    }
    return computeReferenceRanges((ShadowRealTupleType) type,
                                  // TupleCoordinateSystem, TupleUnits,
                                  TupleCoordinateSystem,
                                  ((RealTupleType) Type).getDefaultUnits(),
                                  shadow, shad_ref, ranges);
  }

  /**
   * Clones this instance.
   *
   * @return                    A clone of this instance.
   */
  public final Object clone() {
      /*
       * I (Steve Emmerson) believe that this implementation should return
       * "this" to reduce the memory-footprint but Bill believes that doing so
       * would be counter-intuitive and might harm applications.
       */
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException ex) {
      throw new RuntimeException("Assertion failure");
    }
  }

  /**
   * Provide a String representation of this RealTuple.
   */
  public String toString() {
    if (isMissing()) return "missing";
    String s = "(" + tupleComponents[0];
    for (int i=1; i<tupleComponents.length; i++) {
      s = s + ", " + tupleComponents[i];
    }
    return s + ")";
  }

  public String longString(String pre)
         throws VisADException, RemoteException {
    String s = pre + "RealTuple\n" + pre + "  Type: " + Type.toString() + "\n";
    if (isMissing()) return s + "  missing\n";
    for (int i=0; i<tupleComponents.length; i++) {
      s = s + pre + "  Tuple Component " + i + ": Value = " +
          ((Real) tupleComponents[i]).getValue() + "  (TypeName = " +
          ((RealType) tupleComponents[i].getType()).getName() + ")\n";
    }
    return s;
  }

  /** run 'java visad.RealTuple' to test the RealTuple class */
  public static void main(String args[])
         throws VisADException, RemoteException {

    byte b = 10;
    Real w = new Real(b);

    Real[] reals1 = {new Real(1), new Real(2), new Real(3)};
    RealTuple rt1 = new RealTuple(reals1);
    Real[] reals2 = {new Real(6), new Real(5), new Real(4)};
    RealTuple rt2 = new RealTuple(reals2);

    System.out.println("rt1 = " + rt1 + "\nrt2 = " + rt2);

    System.out.println("rt1 + rt2 = " + rt1.add(rt2));
    System.out.println("rt1 - rt2 = " + rt1.subtract(rt2));
    System.out.println("rt1 * rt2 = " + rt1.multiply(rt2));
    System.out.println("rt1 / rt2 = " + rt1.divide(rt2));
    System.out.println("sqrt(rt1) = " + rt1.sqrt());

    System.out.println("rt1 + w = " + rt1.add(w));
    System.out.println("rt1 - w = " + rt1.subtract(w));
    System.out.println("rt1 * w = " + rt1.multiply(w));
    System.out.println("rt1 / w = " + rt1.divide(w));

    System.out.println("w + rt2 = " + w.add(rt2));
    System.out.println("w - rt2 = " + w.subtract(rt2));
    System.out.println("w * rt2 = " + w.multiply(rt2));
    System.out.println("w / rt2 = " + w.divide(rt2));
  }

/* Here's the output:

iris 201% java visad.RealTuple
rt1 = (1, 2, 3)
rt2 = (6, 5, 4)
rt1 + rt2 = (7, 7, 7)
rt1 - rt2 = (-5, -3, -1)
rt1 * rt2 = (6, 10, 12)
rt1 / rt2 = (0.166667, 0.4, 0.75)
sqrt(rt1) = (1, 1.41421, 1.73205)
rt1 + w = (11, 12, 13)
rt1 - w = (-9, -8, -7)
rt1 * w = (10, 20, 30)
rt1 / w = (0.1, 0.2, 0.3)
w + rt2 = (16, 15, 14)
w - rt2 = (4, 5, 6)
w * rt2 = (60, 50, 40)
w / rt2 = (1.66667, 2, 2.5)
iris 202%

*/

}

