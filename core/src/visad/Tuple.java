//
// Tuple.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;
import java.util.Vector;

/**
   <P>Tuple is the general VisAD data class for vectors.
   Tuple objects are immutable in the sense that the sequence of {@link Data}
   objects cannot be modified; Tuple objects are mutable, however, in the
   sense that a contained {@link Data} object might be mutable (e.g. it might
   be a {@link Field} with modifiable range values).</P>
*/
public class Tuple extends DataImpl implements TupleIface {

  Data[] tupleComponents;  // might be null (see Tuple(TupleType))

  /** 
   * Construct a Tuple object with missing value 
   * @param  type  the TupleType
   */
  public Tuple(TupleType type) {
    super(type);
    if (type instanceof RealTupleType &&
        !(this instanceof RealTuple)) {
      throw new VisADError("must construct as RealTuple");
    }
  }

  /** 
   * Construct a Tuple object from a type and an array of Data objects 
   * @param  type  the TupleType
   * @param  datums  the Data components
   */
  public Tuple(TupleType type, Data[] datums)
         throws VisADException, RemoteException {
    this(type, datums, true);
  }

  /** 
   * Construct a Tuple object from a type and an array of Data objects 
   * @param  type  the TupleType
   * @param  datums  the Data components
   * @param  copy  if true, copy the data objects
   */
  public Tuple(TupleType type, Data[] datums, boolean copy)
         throws VisADException, RemoteException {
    this(type, datums, copy, true);
  }

  /** 
   * Construct a Tuple object from a type and an array of Data objects 
   * @param  type  the TupleType
   * @param  datums  the Data components
   * @param  copy  if true, copy the data objects
   * @param  checkType  if true, make sure the type matches the datum types.
   */
  public Tuple(TupleType type, Data[] datums, boolean copy, boolean checkType)
         throws VisADException, RemoteException {
    super(type);
    if (checkType && !checkTupleType(type, datums)) {
      throw new TypeException("Tuple: type does not match data");
    }
    if (type instanceof RealTupleType &&
        !(this instanceof RealTuple)) {
      throw new TypeException("must construct as RealTuple");
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

  /** 
   * Construct a Tuple object from an array of Data objects;
   * this constructs its MathType from the MathTypes of the
   * data array
   *
   * @param  datums  the Data components
   * @param  copy  if true, copy the data objects
   */
  public Tuple(Data[] datums, boolean copy)
         throws VisADException, RemoteException {
    this(buildTupleType(datums), datums, copy, false);
  }

  /** 
   * Construct a Tuple object from an array of Data objects;
   * this constructs its MathType from the MathTypes of the
   * data array
   *
   * @param  datums  the Data components
   */
  public Tuple(Data[] datums)
         throws VisADException, RemoteException {
    this(buildTupleType(datums), datums, true, false);
  }

  /**
   * Create a Tuple from an array of Data objects.  
   * @return a Tuple.
   */
  public static Tuple makeTuple(Data[] datums)
         throws VisADException, RemoteException {
    return new Tuple(datums);
  }

  /** Check a TupleType for an array of Data */
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

  /** 
   * Make a TupleType for an array of Data 
   * @param datums  array of Data objects
   */
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

  /**
   * Get all the Real components from this Tuple.
   * @return  an array of Real-s
   */
  public Real[] getRealComponents()
         throws VisADException, RemoteException {
    if (getComponents(false) == null) return null;
    Vector reals = new Vector();
    for (int i=0; i<getDimension(); i++) {
      Data comp = getComponent(i);
      if (comp instanceof Real) {
        reals.addElement(comp);
      }
      else if (comp instanceof RealTuple) {
        RealTuple rt = (RealTuple) comp;
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

  /** 
   * Returns the components that constitute this instance.  If this instance
   * has no components, then <code>null</code> is returned.  A returned array
   * may be modified without affecting the behavior of this instance.
   *
   * @return                    The components of this instance or <code>
   *                            null</code>.
   */
  public final  Data[] getComponents() {
      return getComponents(true);
  }

  public static int cloneCnt = 0;

  /** 
   * Returns the components that constitute this instance.  If this instance
   * has no components, then <code>null</code> is returned.  IF copy==true a returned array
   * may be modified without affecting the behavior of this instance. Else, the returned array is the
   * actual component array
   * @param copy  if true then return a copy of the tuple array. Else return the actual array
   *
   * @return                    The components of this instance or <code>
   *                            null</code>.
   */
    
  public Data[] getComponents(boolean copy) {
      if(!copy) return tupleComponents;
      else if(tupleComponents == null)
	  return null;
      else {
	  cloneCnt++;
	  return (Data[])tupleComponents.clone();

      }
  }


  /**
   * Check if there is no Data in this Tuple.
   * @return true if there is no data.
   */
  public  boolean isMissing() {
      //jeffmc: 11-25-09: This method used to just call getComponents()==null which  resulted in a clone
      //of the tuple array every time.
      //      return (getComponents()== null);
      //Instead pass in copy==false
      return (getComponents(false)== null);
  }


  /** 
   * Return number of components in this Tuple.
   * @return the number of components.
   */
  public int getDimension() {
    if (tupleComponents != null) {
      return tupleComponents.length;
    }
    else {
      return ((TupleType) getType()).getDimension();
    }
  }

  /**
   * Returns a component of this instance.  If this instance has no components,
   * then an object of the appropriate {@link MathType} is created that has
   * no data and is returned.  A returned component is the actual component of
   * this instance and is not a copy or clone.
   *
   * @param i                     The zero-based index of the coponent to
   *                              return.
   * @return                      The <code>i</code>-th component of this
   *                              instance.
   * @throws VisADException       if this instance has no components and
   *                              couldn't create one with no data.
   * @throws RemoteException      if a Java RMI failure occurs.
   * @throws TypeException        if the index is less than zero or greater than
   *                              <code>getDimension()-1</code>.
   */
  public  Data getComponent(int i) throws VisADException, RemoteException {
    if (isMissing()) {
      return ((TupleType) Type).getComponent(i).missingData();
    }
    else if (0 <= i && i < getDimension()) {
      return (Data) tupleComponents[i];
    }
    else {
      throw new TypeException("Tuple: component index out of range: " + i);
    }
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
      int dim = getDimension();
      Data[] datums = new Data[dim];
      for (int j=0; j<dim; j++) {
        /* BINARY - TDR June 2, 1998 */
        m_type = ((TupleType)new_type).getComponent(j);
        //System.out.println("m_type = " + m_type);
        /* end  */
        datums[j] = getComponent(j).binary(((Tuple) data).getComponent(j), op,
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
      int dim = getDimension();
      Data[] datums = new Data[getDimension()];
      for (int j=0; j<dim; j++) {
        /* BINARY - TDR June 2, 1998 */
        m_type = ((TupleType)new_type).getComponent(j);
        /* end  */
        datums[j] = getComponent(j).binary(data, op, m_type, sampling_mode, error_mode);
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

    int dim = getDimension();
    Data[] datums = new Data[dim];
    for (int j=0; j<dim; j++) {
      datums[j] = getComponent(j).unary(op, T_type.getComponent(j),
                                           sampling_mode, error_mode);
    }
    return new Tuple(datums);
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    if (isMissing()) return shadow;
    for (int i=0; i<getDimension(); i++) {
      shadow =
        getComponent(i).computeRanges(((ShadowTupleType) type).getComponent(i),
                                         shadow);
    }
    return shadow;
  }

  /** return a Tuple that clones this, except its ErrorEstimate-s
      are adjusted for sampling errors in error */
  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException {
    if (isMissing() || error == null || error.isMissing()) return this;
    int n = getDimension();
    Data[] newComponents = new Data[n];
    for (int i=0; i<n; i++) {
      Data errorComponent = ((Tuple) error).getComponent(i);
      newComponents[i] =
        getComponent(i).adjustSamplingError(errorComponent, error_mode);
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

  /**
   * <p>Clones this instance.  The <code>clone()</code> method of each 
   * component is invoked.</p>
   *
   * <p>This implementation throws {@link CloneNotSupportedException} if and
   * only if one of the components throws it.</p>
   *
   * @return                            A clone of this instance.
   * @throws CloneNotSupportedException if cloning isn't supported.
   */
  public Object clone() throws CloneNotSupportedException {
    Tuple clone = (Tuple)super.clone();

    if (clone.tupleComponents != null) {
      clone.tupleComponents = new Data[tupleComponents.length];

      for (int i = 0; i < tupleComponents.length; i++) {
        Data comp = tupleComponents[i];

        if (comp == null) {
          clone.tupleComponents[i] = null;
        }
        else {
          try {
            /*
             * Data.dataClone() is invoked because Data.clone() doesn't and
             * can't exist.
             */
            clone.tupleComponents[i] = (Data)tupleComponents[i].dataClone();
          }
          catch (RemoteException ex) {
            throw new RuntimeException(ex.toString());
          }
        }
      }
    }

    return clone;
  }

  public String longString(String pre)
         throws VisADException, RemoteException {
    String s = pre + "Tuple\n" + pre + "  Type: " + Type.toString() + "\n";
    if (isMissing()) return s + "  missing\n";
    for (int i=0; i<getDimension(); i++) {
      s = s + pre + "  Tuple Component " + i + ":\n" +
              getComponent(i).longString(pre + "    ");
    }
    return s;
  }

  /**
   * Indicates if this Tuple is identical to an object.
   *
   * @param obj         The object.
   * @return            <code>true</code> if and only if the object is
   *                    a Tuple and both Tuple-s have identical component
   *                    sequences.
   */
  public boolean equals(Object obj) {
    boolean     equals;
    if (!(obj instanceof Tuple)) {
      equals = false;
    }
    else {
      Tuple     that = (Tuple)obj;
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
   * @return            The hash code of this object.
   */
  public int hashCode() {
    int hashCode = 0;
    if (tupleComponents != null)
      for (int i = 0; i < tupleComponents.length; ++i)
        hashCode ^= tupleComponents[i].hashCode();
    return hashCode;
  }

}

