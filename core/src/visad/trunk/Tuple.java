//
// Tuple.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.Vector;

/**
   Tuple is the general VisAD data class for vectors.
   Tuple objects are immutable.<P>
*/
public class Tuple extends DataImpl implements TupleIface {

  Data[] tupleComponents;

  /** construct a Tuple object with missing value */
  public Tuple(TupleType type) {
    super(type);
    if (type instanceof RealTupleType &&
        !(this instanceof RealTuple)) {
      throw new VisADError("must construct as RealTupleType");
    }
  }

  /** construct a Tuple object from a type and an array of Data objects */
  public Tuple(TupleType type, Data[] datums)
         throws VisADException, RemoteException {
    this(type, datums, true);
  }

  /** construct a Tuple object from a type and an array of Data objects */
  public Tuple(TupleType type, Data[] datums, boolean copy)
         throws VisADException, RemoteException {
    super(type);
    if (!checkTupleType(type, datums)) {
      throw new TypeException("Tuple: type does not match data");
    }
    if (type instanceof RealTupleType &&
        !(this instanceof RealTuple)) {
      throw new TypeException("must construct as RealTupleType");
    }
    int n = datums.length;
    tupleComponents = new Data[n];
    for (int i=0; i<n; i++) {
      if (copy) {
        tupleComponents[i] = (Data) datums[i].dataClone();
      }
      else {
        tupleComponents[i] = datums[i];
      }
      if (tupleComponents[i] instanceof DataImpl) {
        ((DataImpl) tupleComponents[i]).setParent(this);
      }
    }
  }

  /** construct a Tuple object from an array of Data objects;
      this constructs its MathType from the MathTypes of the
      data array; only copy data if copy == true */
  public Tuple(Data[] datums, boolean copy)
         throws VisADException, RemoteException {
    this(buildTupleType(datums), datums, copy);
  }

  /** construct a Tuple object from an array of Data objects;
      this constructs its MathType from the MathTypes of the
      data array; components are copies of data */
  public Tuple(Data[] datums)
         throws VisADException, RemoteException {
    this(buildTupleType(datums), datums, true);
  }

  public static Tuple makeTuple(Data[] datums)
         throws VisADException, RemoteException {
    return new Tuple(datums);
  }

  /** check a TupleType for an array of Data */
  static boolean checkTupleType(TupleType type, Data[] datums)
         throws VisADException, RemoteException {
    if (datums == null || type == null) return false;
    int n = datums.length;
    if (n != type.getDimension()) return false;
    for (int i=0; i<n; i++) {
      if (!type.getComponent(i).equals(datums[i].getType())) return false;
    }
    return true;
  }

  /** make a TupleType for an array of Data */
  public static TupleType buildTupleType(Data[] datums)
         throws VisADException, RemoteException {
    if (datums == null) {
      throw new TypeException("Tuple: # components must be > 0");
    }
    int n = datums.length;
    if (n < 1) {
      throw new TypeException("Tuple: # components must be > 0");
    }
    MathType[] types = new MathType[n];
    boolean allReal = true;
    for (int i=0; i<n; i++) {
      types[i] = datums[i].getType();
      if (!(types[i] instanceof RealType)) allReal = false;
    }
    if (allReal) {
      RealType[] real_types = new RealType[n];
      for (int i=0; i<n; i++) real_types[i] = (RealType) types[i];
      return new RealTupleType(real_types);
    }
    else {
      return new TupleType(types);
    }
  }

  public Real[] getRealComponents()
         throws VisADException, RemoteException {
    if (tupleComponents == null) return null;
    Vector reals = new Vector();
    for (int i=0; i<tupleComponents.length; i++) {
      if (tupleComponents[i] instanceof Real) {
        reals.addElement(tupleComponents[i]);
      }
      else if (tupleComponents[i] instanceof RealTuple) {
        RealTuple rt = (RealTuple) tupleComponents[i]; 
        for (int j=0; j<rt.getDimension(); j++) {
          reals.addElement(rt.getComponent(j));
        }
      }
    }
    if (reals.size() == 0) return null;
    Real[] realComponents = new Real[reals.size()];
    for (int i=0; i<reals.size(); i++) {
      realComponents[i] = (Real) reals.elementAt(i);
    }
    return realComponents;
  }

  /** return array of components */
  public Data[] getComponents() {
    return tupleComponents;
  }

  /** return number of components */
  public int getDimension() {
    if (tupleComponents != null) {
      return tupleComponents.length;
    }
    else {
      return ((TupleType) getType()).getDimension();
    }
  }

  /** return component for i between 0 and getDimension() - 1 */
  public Data getComponent(int i) throws VisADException, RemoteException {
    if (isMissing()) {
      return ((TupleType) Type).getComponent(i).missingData();
    }
    else if (0 <= i && i < tupleComponents.length) {
      return (Data) tupleComponents[i];
    }
    else {
      throw new TypeException("Tuple: component index out of range");
    }
  }

  public boolean isMissing() {
    return (tupleComponents == null);
  }

/*-  TDR May 1998
  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
 */
  public Data binary(Data data, int op, MathType new_type,
                    int sampling_mode, int error_mode )
             throws VisADException, RemoteException {
    /* BINARY - TDR May 28, 1998 */
    if ( new_type == null ) {
      throw new TypeException("binary: new_type may not be null" );
    }
    MathType m_type;
    /* BINARY - end */
    if (data instanceof RealTuple) {
      throw new TypeException("Tuple.binary: types don't match");
    }
    else if (data instanceof Tuple) {
      if (!Type.equalsExceptName(data.getType())) {
        throw new TypeException("Tuple.binary: types don't match");
      }
      /* BINARY - TDR May 28, 1998 */
      if ( !Type.equalsExceptName(new_type) ) {
        throw new TypeException();
      }
      /* BINARY - end */
      if (isMissing() || data.isMissing()) {
        return new Tuple((TupleType) new_type);
      }
      Data[] datums = new Data[tupleComponents.length];
      for (int j=0; j<tupleComponents.length; j++) {
        /* BINARY - TDR June 2, 1998 */
        m_type = ((TupleType)new_type).getComponent(j);
        /* end  */
        datums[j] = tupleComponents[j].binary(((Tuple) data).getComponent(j), op,
                                              m_type, sampling_mode, error_mode);
      }
      return new Tuple(datums);
    }
    else if (data instanceof Real) {
      /* BINARY - TDR May 28, 1998 */
      if ( !Type.equalsExceptName(new_type) ) {
        throw new TypeException();
      }
      /* BINARY - end */
      if (isMissing() || data.isMissing()) {
        return new Tuple((TupleType) new_type);
      }
      Data[] datums = new Data[tupleComponents.length];
      for (int j=0; j<tupleComponents.length; j++) {
        /* BINARY - TDR June 2, 1998 */
        m_type = ((TupleType)new_type).getComponent(j);
        /* end  */
        datums[j] = tupleComponents[j].binary(data, op, m_type, sampling_mode, error_mode);
      }
      return new Tuple(datums);
    }
    else if (data instanceof Text) {
      throw new TypeException("Tuple.binary: types don't match");
    }
    else if (data instanceof Field) {
     /* BINARY - TDR May 28, 1998
      return data.binary(this, invertOp(op), sampling_mode, error_mode);
      */
      /* BINARY - TDR June 3, 1998 */
      if ( !(data.getType()).equalsExceptName(new_type) ) {
        throw new TypeException();
      }
      return data.binary(this, invertOp(op), new_type, sampling_mode, error_mode);
      /* BINARY - end  */
    }
    else {
      throw new TypeException("Tuple.binary");
    }
  }

  /*- TDR  July 1998
  public Data unary(int op, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
  */
  public Data unary(int op, MathType new_type,
                    int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if ( new_type == null ) {
      throw new TypeException("unary: new_type may not be null");
    }

    if ( !Type.equalsExceptName( new_type )) {
       throw new TypeException("unary: new_type doesn't match return type");
    }
    TupleType T_type = (TupleType)new_type;

    if (isMissing()) return new Tuple((TupleType) new_type);

    Data[] datums = new Data[tupleComponents.length];
    for (int j=0; j<tupleComponents.length; j++) {
      datums[j] = tupleComponents[j].unary(op, T_type.getComponent(j),
                                           sampling_mode, error_mode);
    }
    return new Tuple(datums);
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    if (isMissing()) return shadow;
    for (int i=0; i<tupleComponents.length; i++) {
      shadow =
        tupleComponents[i].computeRanges(((ShadowTupleType) type).getComponent(i),
                                         shadow);
    }
    return shadow;
  }

  /** return a Tuple that clones this, except its ErrorEstimate-s
      are adjusted for sampling errors in error */
  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException {
    if (isMissing() || error == null || error.isMissing()) return this;
    int n = tupleComponents.length;
    Data[] newComponents = new Data[n];
    for (int i=0; i<n; i++) {
      Data errorComponent = ((Tuple) error).getComponent(i);
      newComponents[i] =
        tupleComponents[i].adjustSamplingError(errorComponent, error_mode);
    }
    return new Tuple(newComponents);
  }

  /**
   * A wrapper around {@link #getComponent(int) getComponent} for JPython.
   *
   * @return The requested Data object.
   */
  public Data __getitem__(int index) throws VisADException, RemoteException {
    return getComponent(index);
  }

  /**
   * A wrapper around {@link #getLength() getLength} for JPython.
   *
   * @return The number of components of the Tuple
   */
  public int __len__() {
    return getDimension();
  }

  /**
   * Return the number of components of the Tuple
   *
   * @return Number of components.
   */
  public int getLength() {
    return getDimension();
  }

  public Object clone() {
    Tuple tuple;
    try {
      if (tupleComponents == null) {
        tuple = new Tuple((TupleType )getType());
      } else {
        int n = tupleComponents.length;
        Data[] datums = new Data[n];
        for (int i=0; i<n; i++) {
          datums[i] = (Data) tupleComponents[i].dataClone();
        }
        tuple = new Tuple(datums);
      }
    }
    catch (VisADException e) {
      throw new VisADError("Tuple.clone: VisADException occurred");
    }
    catch (RemoteException e) {
      throw new VisADError("Tuple.clone: RemoteException occurred");
    }
    return tuple;
  }

  public String longString(String pre)
         throws VisADException, RemoteException {
    String s = pre + "Tuple\n" + pre + "  Type: " + Type.toString() + "\n";
    if (isMissing()) return s + "  missing\n";
    for (int i=0; i<tupleComponents.length; i++) {
      s = s + pre + "  Tuple Component " + i + ":\n" +
              tupleComponents[i].longString(pre + "    ");
    }
    return s;
  }

  /**
   * Indicates if this Tuple is identical to another object.
   * @param obj		The other object.
   * @return		<code>true</code> if and only if the other object is
   *			a Tuple and both Tuple-s have identical component
   *			sequences.
   */
  public boolean equals(Object obj) {
    boolean	equals;
    if (!(obj instanceof Tuple)) {
      equals = false;
    }
    else {
      Tuple	that = (Tuple)obj;
      if (this == that) {
	equals = true;
      }
      else if (tupleComponents == null || that.tupleComponents == null) {
	equals = tupleComponents == that.tupleComponents;
      }
      else if (tupleComponents.length != that.tupleComponents.length) {
	equals = false;
      }
      else {
	equals = true;
	for (int i = 0; i < tupleComponents.length; ++i)
	  if (!(tupleComponents[i].equals(that.tupleComponents[i]))) {
	    equals = false;
	    break;
	  }
      }
    }
    return equals;
  }

  /**
   * Returns the hash code of this object.
   * @return		The hash code of this object.
   */
  public int hashCode() {
    int	hashCode = 0;
    if (tupleComponents != null)
      for (int i = 0; i < tupleComponents.length; ++i)
	hashCode ^= tupleComponents[i].hashCode();
    return hashCode;
  }

}

