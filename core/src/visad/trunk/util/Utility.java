
//
// Utility.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;

/**
 * Provides support for VisAD utility functions.
 */
public final class
Utility
{
  /**
   * Ensures that a MathType is a RealTupleType.  Converts if necessary.
   * @param type		The math type to be "converted" to a 
   *				RealTupleType.  It shall be either a RealType,
   *				a RealTupleType, or a SetType.
   * @return                    The RealTupleType version of <code>type</code>.
   *                            If <code>type</code> is a RealTupleType, then
   *                            it is returned; otherwise, if <code>type</code>
   *                            is a RealType, then a RealTupleType
   *                            containing <code>type</code> as the
   *                            only component is returned; otherwise,
   *                            if <code>type</code> is a SetType, then
   *                            <code>((SetType)type).getDomain()</code> is
   *                            returned.
   * @throws TypeException	<code>type</code> is the wrong type: it can't
   *				be converted into a RealTupleType.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public static RealTupleType
  ensureRealTupleType(MathType type)
    throws TypeException, VisADException
  {
    if (type instanceof RealTupleType)
      return (RealTupleType)type;
    if (type instanceof RealType)
      return new RealTupleType((RealType)type);
    if (type instanceof SetType)
      return ((SetType)type).getDomain();
    throw new TypeException(
      Utility.class.getName() +
      ".ensureRealTupleType(MathType): Can't convert MathType \"" +
      type + "\" into a RealTupleType");
  }

  /**
   * Ensures that a MathType is a TupleType.  Converts if necessary.
   * @param type		The math type to be "converted" to a 
   *				TupleType.  It shall be either a Scalar,
   *				a TupleType, or a SetType.
   * @return                    The TupleType version of <code>type</code>.
   *                            If <code>type</code> is a TupleType, then
   *                            it is returned; otherwise, if <code>type</code>
   *                            is a Scalar, then a TupleType
   *                            containing <code>type</code> as the
   *                            only component is returned; otherwise,
   *                            if <code>type</code> is a SetType, then
   *                            <code>((SetType)type).getDomain()</code> is
   *                            returned.
   * @throws TypeException	<code>type</code> is the wrong type: it can't
   *				be converted into a TupleType.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public static TupleType
  ensureTupleType(MathType type)
    throws TypeException, VisADException
  {
    if (type instanceof TupleType)
      return (TupleType)type;
    if (type instanceof ScalarType)
      return new TupleType(new MathType[] {type});
    if (type instanceof SetType)
      return ((SetType)type).getDomain();
    throw new TypeException(
      Utility.class.getName() +
      ".ensureTupleType(MathType): Can't convert MathType \"" +
      type + "\" into a TupleType");
  }

  /**
   * Ensures that a Data is a Tuple.  Creates a Tuple if necessary.
   * @param datum		The math type to be "converted" to a Tuple.
   * @return                    The Tuple version of <code>datum</code>.  If
   *                            <code>datum</code> is a Tuple, then it is
   *                            returned; otherwise, if <code>datum</code> is
   *                            a Real, then a RealTuple containing <code>
   *                            datum</code> as the only component is returned;
   *                            otherwise, a Tuple containing <code>datum</code>
   *                            as the only component is returned.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static Tuple
  ensureTuple(Data datum)
    throws VisADException, RemoteException
  {
    return
      datum instanceof Tuple
	? (Tuple)datum
	: datum instanceof Real
	    ? new RealTuple(new Real[] {(Real)datum})
	    : new Tuple(new Data[] {datum});
  }

  /**
   * Gets the index of a range component of a Field.
   * @param componentType	The MathType of the component.
   * @return                    The index of the component in the range of the
   *                            field or -1 if the component is not in the range
   *                            of the field (NB: this is not the flat-range
   *                            index).
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static int
  getComponentIndex(Field field, MathType componentType)
    throws VisADException, RemoteException
  {
    TupleType	rangeTupleType =
      ensureTupleType(((FunctionType)field.getType()).getRange());
    int		componentCount = rangeTupleType.getDimension();
    for (int i = 0; i < componentCount; i++)
      if (rangeTupleType.getComponent(i).equals(componentType))
	return i;
    return -1;
  }
}
