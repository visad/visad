
//
// MathType.java
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
import java.util.Vector;

/** 
(Fulker)
   MathType is the superclass for VisAD's hierarchy of mathematical types.
   It encompasses the mathematical concepts of scalars, tuples (i.e., 
   n-dimensional vectors), functions, and certain forms of sets.<p>

   VisAD <b>Data</b> objects are finite approximations to math objects.  
   Every Data object possesses a MathType, which characterizes the 
   mathematical object that the data approximate.  This MathType is not 
   synonymous with the class of the Data object, even though the class
   names for a Data object and its corresponding MathType object (Set 
   and SetType, e.g.) may be similar.<p>

   MathType objects are immutable; one implication is that the setDefaultSet 
   method (in RealTupleType) can be invoked only <b>prior</b> to using the 
   related getDefaultSet method.<p>
(/Fulker)<p>

   MathType is the superclass of the VisAD hierarchy of data types.
   MathType objects are immutable; note that there is a setDefaultSet method
   in RealTupleType, but if t is a RealTupleType then t.setDefaultSet cannot
   be called after t.getDefaultSet has been called.<P>

   VisAD Data objects are finite approximations to mathematical objects.
   Every Data object includes a MathType, which is the mathematical type
   of the mathematical object it approximates.  This MathType is not
   synonymous with the class of the Data object.<P>

*/
public abstract class MathType extends Object implements java.io.Serializable {

  /** true if this MathType is defined by the system */
  boolean SystemIntrinsic;

  public MathType() {
    super();
    SystemIntrinsic = false;
  }

  MathType(boolean b) {
    super();
    SystemIntrinsic = true;
  }

  /** 
(Fulker)
   Check for equality of MathType, including equality of scalar names.
   All MathType objects are named or are built up of named MathTypes.
   For example, a ScalarType object might be named "Pressure," and a
   FunctionType object might map a "Time" domain onto a range named 
   "Temperature."  Therefore MathType objects have methods to test for
   two types of equality; both compare the underlying mathematical types,
   but only this method tests to be sure that all of the names match.  
   Such tests are useful because some operations on Data objects 
   (differencing, for example) make sense only when those objects 
   approximate MathTypes that are identical in the named as well as the
   mathematical sense.<p>
(/Fulker)<p>

  check for equality of data types, including equality of scalar names
  for example, real types for "pressure" and "temperature" are not equal

  */
  public abstract boolean equals(Object type);

  /** check for equality of data types, excluding equality of scalar names
      for example, real types for "pressure" and "temperature" are equal */
  public abstract boolean equalsExceptName(MathType type);

  /* TDR - May 1998.  As above, except units must be convertible */
  public abstract boolean equalsExceptNameButUnits( MathType type )
           throws VisADException;

  /* TDR - June 1998           */
  public abstract MathType cloneDerivative( RealType d_partial )
           throws VisADException;

  /* TDR - July 1998  */
  public abstract MathType binary( MathType type, int op, Vector names )
         throws VisADException;

  /* TDR - July 1998 */
  public abstract MathType unary( int op, Vector names )
         throws VisADException;

  public abstract Data missingData() throws VisADException, RemoteException;

  public abstract ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
           throws VisADException, RemoteException;

  public abstract String toString();

}

