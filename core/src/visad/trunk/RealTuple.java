
//
// RealTuple.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.rmi.*;

/**
   RealTuple is the VisAD data class for vectors in R^n for n>0.
   RealTuple objects are immutable.<P>
*/
public class RealTuple extends Tuple {

  private CoordinateSystem TupleCoordinateSystem;

  private Unit[] TupleUnits; // simply copies of Unit-s from Real tupleComponents

  /** construct a RealTuple object with the missing value */
  public RealTuple(RealTupleType type) {
    super(type);
    TupleCoordinateSystem = type.getCoordinateSystem();
    if (tupleComponents != null) {
      int n = tupleComponents.length;
      TupleUnits = new Unit[n];
      for (int i=0; i<n; i++) TupleUnits[i] = null;
    }
  }

  /** construct a RealTuple according to an array of Real objects */
  public RealTuple(RealTupleType type, Real[] reals, CoordinateSystem coord_sys)
         throws VisADException, RemoteException {
    super(type, reals);
    init_coord_sys(coord_sys);
  }

  /** construct a RealTuple according to an array of Real objects */
  public RealTuple(Real[] reals)
         throws VisADException, RemoteException {
    super(reals);
    init_coord_sys(null);
  }

  /** construct a RealTuple according to a RealTupleType and a double array */
  public RealTuple(RealTupleType type, double[] values)
         throws VisADException, RemoteException {
    this(type, buildRealArray(type, values), null);
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
        throw new CoordinateSystemException(
          "RealTuple: coord_sys must match Type.DefaultCoordinateSystem");
      }
      TupleCoordinateSystem = coord_sys;
    }
    if (TupleCoordinateSystem != null &&
        !Unit.canConvertArray(TupleCoordinateSystem.getCoordinateSystemUnits(),
                              ((RealTupleType) Type).getDefaultUnits())) {
      throw new UnitException("RealTuple: CoordinateSystem Units must be " +
                              "convertable with Type default Units");
    }

    int n = tupleComponents.length;
    TupleUnits = new Unit[n];
    for (int i=0; i<n; i++) TupleUnits[i] = ((Real) tupleComponents[i]).getUnit();
    if(!Unit.canConvertArray(TupleUnits,
                             ((RealTupleType) Type).getDefaultUnits())) {
      throw new UnitException("Tuple: Units must be convertable with " +
                              "Type default Units");
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

  public Unit[] getTupleUnits() {
    return Unit.copyUnitsArray(TupleUnits);
  }

  public ErrorEstimate[] getErrors()
         throws VisADException, RemoteException {
    int n = getDimension();
    ErrorEstimate[] errors = new ErrorEstimate[n];
    for (int i=0; i<n; i++) errors[i] = ((Real) getComponent(i)).getError();
    return errors;
  }

  public CoordinateSystem getCoordinateSystem() {
    return TupleCoordinateSystem;
  }

  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (data instanceof RealTuple) {
      if (!Type.equalsExceptName(data.getType())) {
        throw new TypeException("RealTuple.binary: types don't match");
      }
      if (isMissing() || data.isMissing()) {
        return new RealTuple((RealTupleType) Type);
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
        reals[j] =
          (Real) tupleComponents[j].binary(real, op, sampling_mode, error_mode);
      }
      return new RealTuple((RealTupleType) Type, reals, TupleCoordinateSystem);
    }
    else if (data instanceof Tuple) { 
      throw new TypeException("RealTuple.binary: types don't match");
    }
    else if (data instanceof Real) {
      if (isMissing() || data.isMissing()) {
        return new RealTuple((RealTupleType) Type);
      }
      Real[] reals = new Real[tupleComponents.length];
      for (int j=0; j<tupleComponents.length; j++) {
        reals[j] = (Real) tupleComponents[j].binary(data, op, sampling_mode,
                                                    error_mode);
      }
      return new RealTuple((RealTupleType) Type, reals, TupleCoordinateSystem);
    }
    else if (data instanceof Text) {
      throw new TypeException("RealTuple.binary: types don't match");
    }
    else if (data instanceof Field) {
      return data.binary(this, invertOp(op), sampling_mode, error_mode);
    }
    else {
      throw new TypeException("RealTuple.binary");
    }
  }

  public Data unary(int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (isMissing()) return new RealTuple((RealTupleType) Type);
    Real[] reals = new Real[tupleComponents.length];
    for (int j=0; j<tupleComponents.length; j++) {
      reals[j] = (Real) tupleComponents[j].unary(op, sampling_mode, error_mode);
    }
    return new RealTuple((RealTupleType) Type, reals, TupleCoordinateSystem);
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    shadow = super.computeRanges(type, shadow);
    ShadowRealTupleType shad_ref = ((ShadowRealTupleType) type).getReference();
    int n = tupleComponents.length;
    if (isMissing() || shad_ref == null) return shadow;
    // computeRanges for Reference RealTypes
    double[][] ranges = new double[2][n];
    for (int i=0; i<n; i++) {
      double value = ((Real) tupleComponents[i]).getValue();
      if (value != value) return shadow;
      ranges[0][i] = value;
      ranges[1][i] = value;
    }
    return computeReferenceRanges((ShadowRealTupleType) type,
                                  TupleCoordinateSystem, TupleUnits,
                                  shadow, shad_ref, ranges);
  }

  public Object clone() {
    RealTuple tuple;
    try {
      tuple = new RealTuple((RealTupleType) Type, (Real[]) tupleComponents,
                            TupleCoordinateSystem);
    }
    catch (VisADException e) {
      throw new VisADError("RealTuple.clone: VisADException occurred");
    }
    catch (RemoteException e) {
      throw new VisADError("RealTuple.clone: RemoteException occurred");
    }
    return tuple;
  }

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

