
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

package visad.util;

import java.rmi.RemoteException;
import visad.Data;
import visad.Field;
import visad.FieldImpl;
import visad.Function;
import visad.FunctionType;
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarType;
import visad.SetType;
import visad.Tuple;
import visad.TupleType;
import visad.TypeException;
import visad.VisADException;

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
    RealTupleType	result;
    if (type instanceof RealTupleType)
      result = (RealTupleType)type;
    else if (type instanceof RealType)
      result = new RealTupleType((RealType)type);
    else if (type instanceof SetType)
      result = ((SetType)type).getDomain();
    else
      throw new TypeException(
	Utility.class.getName() +
	".ensureRealTupleType(MathType): Can't convert MathType \"" +
	type + "\" into a RealTupleType");
    return result;
  }

  /**
   * Ensures that a MathType is a TupleType.  Converts if necessary.
   * @param type		The math type to be "converted" to a 
   *				TupleType.
   * @return                    The TupleType version of <code>type</code>.
   *                            If <code>type</code> is a TupleType,
   *                            then it is returned; otherwise, if
   *                            <code>type</code> is a SetType, then
   *                            <code>((SetType)type).getDomain()</code> is
   *                            returned; otherwise, a TupleType containing
   *                            <code>type</code> as the only component is
   *                            returned (if <code>MathType</code> is a
   *				RealType, then the returned TupleType is a 
   *				RealTupleType);
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public static TupleType
  ensureTupleType(MathType type)
    throws VisADException
  {
    return
      type instanceof TupleType
	? (TupleType)type
	: type instanceof SetType
	  ? ((SetType)type).getDomain()		// actually a RealTupleType
	  : type instanceof RealType
	    ? new RealTupleType((RealType)type)
	    : new TupleType(new MathType[] {type});
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
   * Gets the MathType of the range of a Function.
   * @param function		A function.
   * @return			The MathType of the range of the function.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static MathType
  getRangeType(Function function)
    throws VisADException, RemoteException
  {
    return ((FunctionType)function.getType()).getRange();
  }

  /**
   * Gets the number of components in the range of a Function.  NB: This differs
   * from visad.FlatField.getRangeDimension() in that it returns the number of
   * components in the range rather than the number of components in the flat
   * range.
   * @param function		A function.
   * @return			The number of components in the range of the
   *				function.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static int
  getRangeDimension(Function function)
    throws VisADException, RemoteException
  {
    return ensureTupleType(getRangeType(function)).getDimension();
  }

  /**
   * Gets the index of a component in the range of a Field.  If the range
   * contains multiple instances of the component, then it is unspecified
   * which component index is returned.
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
    TupleType	rangeTupleType = ensureTupleType(getRangeType(field));
    for (int i = rangeTupleType.getDimension(); --i >= 0; )
      if (rangeTupleType.getComponent(i).equals(componentType))
	return i;
    return -1;
  }

  /**
   * Ensures that the range of a FieldImpl is a given type.
   * @param field		The input field.
   * @param rangeType		The desired type of range for the resulting
   *				field.
   * @param alwaysNew           Whether or not to always create a new field.
   *                            If <code>false</code> and the input field
   *                            is exactly what is desired, then the input
   *                            field will simply be returned; otherwise, an
   *                            extraction will always be performed.
   * @return			A field with the desired range.
   * @throws TypeException	A field with the given range cannot be created
   *				from the input field.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static FieldImpl
  ensureRange(FieldImpl field, MathType rangeType, boolean alwaysNew)
    throws TypeException, VisADException, RemoteException
  {
    FieldImpl	result;
    if (rangeType.equals(getRangeType(field)))
    {
      result = alwaysNew ? (FieldImpl)field.clone() : field;
    }
    else
    {
      result = null;
      TupleType	rangeTuple = ensureTupleType(rangeType);
      int	componentCount = rangeTuple.getDimension();
      for (int i = 0; i < componentCount; i++)
      {
	int	componentIndex =
	  getComponentIndex(field, rangeTuple.getComponent(i));
	if (componentIndex < 0)
	  throw new TypeException("The range of field \"" + field + 
	    "\" doesn't contain component \"" + rangeType + '"');
	result =
	  result == null
	    ? (FieldImpl)field.extract(componentIndex)
	    : (FieldImpl)FieldImpl.combine(
		new Field[] {result, field.extract(componentIndex)});
      }
    }
    return result;
  }
}
