//
// DataUtility.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

// General Java
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Comparator;

// RMI classes
import java.rmi.RemoteException;

import visad.java2d.DisplayImplJ2D;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;

// GUI handling
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// VisAD packages
import visad.*;

/** A general utility class for VisAD Data objects */
public class DataUtility extends Object {

  private static boolean init = false;
  private static FunctionType simpleImageType;
  private static RealType radiance;
  private static RealTupleType imageDomain;
  private static RealType line;
  private static RealType element;

  private int num_lines, num_elements;

  private static synchronized void makeTypes()
          throws VisADException {
    if (!init) {
      init = true;
      simpleImageType = (FunctionType)
        MathType.stringToType("((ImageElement, ImageLine) -> ImageRadiance)");
      imageDomain = simpleImageType.getDomain();
      line = (RealType) imageDomain.getComponent(1);
      element = (RealType) imageDomain.getComponent(0);
      MathType range = simpleImageType.getRange();
      if (range instanceof RealType) {
        radiance = (RealType) range;
      }
      else {
        radiance = (RealType) ((RealTupleType) range).getComponent(0);
      }
    }
  }

  /** return a FlatField for a simple image from
      values[nlines][nelements] */
  public static FlatField makeImage(float[][] values)
         throws VisADException, RemoteException {
    if (values == null) return null;
    int nlines = values.length;
    int nelements = 0;
    for (int l=0; l<nlines; l++) {
      if (values[l] != null) {
        if (values[l].length > nelements) nelements = values[l].length;
      }
    }
    if (!init) makeTypes();
    FlatField image = new FlatField(simpleImageType,
                new Integer2DSet(imageDomain, nelements, nlines));
    setPixels(image, values);
    return image;
  }

  /** set pixel values in a simple image,
      indexed as values[line][element] */
  public static void setPixels(FlatField image, float[][] values)
         throws VisADException, RemoteException {
    if (values == null) return;
    Integer2DSet set = (Integer2DSet) image.getDomainSet();
    int num_elements = set.getLength(0);
    int num_lines = set.getLength(1);
    float[][] vals = new float[1][num_lines * num_elements];
    for (int i=0; i<num_lines * num_elements; i++) {
      vals[0][i] = Float.NaN;
    }
    int nl = values.length;
    if (num_lines < nl) nl = num_lines;
    int base = 0;
    for (int l=0; l<nl; l++) {
      if (values[l] != null) {
        int ne = values[l].length;
        if (num_elements < ne) ne = num_elements;
        for (int e=0; e<ne; e++) {
          vals[0][base + e] = values[l][e];
        }
      }
      base += num_elements;
    }
    image.setSamples(vals, false); // no need to copy
  }

  public static float[][] getPixels(FlatField image)
         throws VisADException, RemoteException {
    Integer2DSet set = (Integer2DSet) image.getDomainSet();
    int num_elements = set.getLength(0);
    int num_lines = set.getLength(1);
    float[][] values = new float[num_lines][num_elements];
    double[][] vals = image.getValues();
    int base = 0;
    for (int l=0; l<num_lines; l++) {
      for (int e=0; e<num_elements; e++) {
        values[l][e] = (float) vals[0][base + e];
      }
      base += num_elements;
    }
    return values;
  }

  public static DisplayImpl makeSimpleDisplay(DataImpl data)
         throws VisADException, RemoteException {
    boolean three_d = true;
    DisplayImpl display = null;
    try {
      display = new DisplayImplJ3D("simple data display");
    }
    catch (UnsatisfiedLinkError e) {
      display = new DisplayImplJ2D("simple data display");
      three_d = false;
    }
    MathType type = data.getType();
    ScalarMap[] maps = type.guessMaps(three_d);
    if (maps == null) {
      display.stop();
      return null;
    }
    if (three_d) {
      boolean only_2d = true;
      for (int i=0; i<maps.length; i++) {
        DisplayRealType dtype = maps[i].getDisplayScalar();
        if (Display.ZAxis.equals(maps[i]) || 
            Display.Latitude.equals(maps[i])) {
          only_2d = false;
          break;
        }
      }
      if (only_2d) {
        display.stop();
        display = new DisplayImplJ3D("simple data display",
                                     new TwoDDisplayRendererJ3D());
      }
    }
    for (int i=0; i<maps.length; i++) {
      display.addMap(maps[i]);
    }

    DataReferenceImpl ref = new DataReferenceImpl("simple data display");
    ref.setData(data);
    display.addReference(ref);
    return display;
  }

  public static void main(String[] argv)
         throws VisADException, RemoteException {
    float[][] pixels = new float[64][64];
    for (int i=0; i<64; i++) {
      for (int j=0; j<64; j++) {
        pixels[i][j] = i * (i - 32) * (i - 64) *
                       j * (j - 32) * (j - 64) + 100000;
      }
    }

    FlatField image = DataUtility.makeImage(pixels);
    DisplayImpl display = DataUtility.makeSimpleDisplay(image);

    JFrame jframe = new JFrame("SimplImage.main");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    jframe.setContentPane((JPanel) display.getComponent());
    jframe.pack();
    jframe.setVisible(true);
  }

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
	DataUtility.class.getName() +
	".ensureRealTupleType(MathType): Can't convert MathType \"" +
	type + "\" into a RealTupleType");
    return result;
  }

  /**
   * Ensures that a MathType is a TupleType.  Converts if necessary.
   * @param type		The math type to be "converted" to a TupleType.
   * @return                    The TupleType version of <code>type</code>.
   *                            If <code>type</code> is a TupleType,
   *                            then it is returned; otherwise, if
   *                            <code>type</code> is a SetType, then
   *                            <code>((SetType)type).getDomain()</code> is
   *                            returned; otherwise, a TupleType containing
   *                            <code>type</code> as the only component is
   *                            returned (if <code>type</code> is a RealType,
   *                            then the returned TupleType is a RealTupleType);
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
   * Gets the MathType of the domain of a Set.
   * @param function		A function.
   * @return			The MathType of the domain of the Set.
   */
  public static RealTupleType
  getDomainType(Set set)
  {
    return ((SetType)set.getType()).getDomain();
  }

  /**
   * Gets the MathType of the domain of a Function.
   * @param function		A function.
   * @return			The MathType of the domain of the function.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static RealTupleType
  getDomainType(Function function)
    throws VisADException, RemoteException
  {
    return ((FunctionType)function.getType()).getDomain();
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
   * Gets the TupleType of the range of a Function.
   * @param function		A function.
   * @return			The TupleType of the range of the function.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static TupleType
  getRangeTupleType(Function function)
    throws VisADException, RemoteException
  {
    return ensureTupleType(getRangeType(function));
  }

  /**
   * Gets the MathType of the flat-range of a Function.
   * @param function		A function.
   * @return			The MathType of the flat-range of the function.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static RealTupleType
  getFlatRangeType(Function function)
    throws VisADException, RemoteException
  {
    return ((FunctionType)function.getType()).getFlatRange();
  }

  /**
   * Gets the number of components in the range of a Function.  NB: This differs
   * from visad.FlatField.getRangeDimension() in that it returns the number of
   * components in the actual range rather than the number of components in the
   * flat range.
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
    return getRangeTupleType(function).getDimension();
  }

  /**
   * Gets the index of a component in a TupleType.  If the TupleType
   * contains multiple instances of the component, then it is unspecified
   * which component index is returned.  This method first looks for an
   * exact match via the <code>equals(Object)</code> method.  If none is
   * found, then this method looks for an approximate match based on the
   * <code>equalsExceptNameButUnits(MathType)</code> method.
   * @param tupleType		The type of the tuple.
   * @param componentType	The MathType of the component.
   * @return                    The index of the component in the tuple
   *                            or -1 if the component is not in the tuple.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static int
  getComponentIndex(TupleType tupleType, MathType componentType)
    throws VisADException, RemoteException
  {
    /*
     * Return first exact match if found.
     */
    for (int i = tupleType.getDimension(); --i >= 0; )
      if (componentType.equals(tupleType.getComponent(i)))
	return i;
    /*
     * Return the first convertible-units match if found.
     */
    for (int i = tupleType.getDimension(); --i >= 0; )
      if (componentType.equalsExceptNameButUnits(tupleType.getComponent(i)))
	return i;
    return -1;
  }

  /**
   * Gets the index of a component in a Set.  If the set contains multiple
   * instances of the component, then it is unspecified which component index is
   * returned.
   * @param set			The Set.
   * @param componentType	The MathType of the component.
   * @return			The index of the component in the set or -1 if
   *				the component is not in the set.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static int
  getComponentIndex(Set set, MathType componentType)
    throws VisADException, RemoteException
  {
    return 
      getComponentIndex(((SetType)set.getType()).getDomain(), componentType);
  }

  /**
   * Gets the index of a component in the range of a Function.  If the range
   * contains multiple instances of the component, then it is unspecified
   * which component index is returned.
   * @param function		The Function.
   * @param componentType	The MathType of the component.
   * @return                    The index of the component in the range of the
   *                            field or -1 if the component is not in the range
   *                            of the field (NB: this is not the flat-range
   *                            index).
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static int
  getComponentIndex(Function function, MathType componentType)
    throws VisADException, RemoteException
  {
    return getComponentIndex(getRangeTupleType(function), componentType);
  }

  /**
   * Ensures that the range of a FieldImpl is a given type.  Extracts from
   * the input field only if necessary.
   * @param field		The input field.
   * @param newRangeType	The desired type of range for the resulting
   *				field.
   * @return			A field with the desired range type.  The range
   *				data will be missing, however, if <em>all</em>
   *				of it couldn't be extracted from the input
   *				Field (i.e. 
   *				RETURN_VALUE<code>.isMissing()</code> will be
   *				true.
   * @throws UnimplementedException
   *				The desired range type is a TupleType and not
   *				a RealTupleType.
   * @throws TypeException	The new range type cannot be the range of a
   *				field.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static Field
  ensureRange(Field field, MathType newRangeType)
    throws UnimplementedException, TypeException, VisADException, 
      RemoteException
  {
    Field	result;
    if (newRangeType.equals(getRangeType(field)))
    {
      result = field;
    }
    else if (newRangeType instanceof RealType)
    {
      int	componentIndex = getComponentIndex(field, newRangeType);
      if (componentIndex >= 0)
      {
	result = field.extract(componentIndex);
      }
      else
      {
	result =
	  new FlatField(
	    new FunctionType(getDomainType(field), newRangeType),
	    field.getDomainSet());
      }
    }
    else if (newRangeType instanceof RealTupleType)
    {
      FlatField	temporaryField;
      int	realTupleIndex = getComponentIndex(field, newRangeType);
      if (realTupleIndex >= 0)
      {
	/*
	 * The desired RealTuple range is a component of the input Field.
	 */
	temporaryField = (FlatField)field.extract(realTupleIndex);
      }
      else
      {
	/*
	 * The desired RealTuple range is not a component of the input Field.
	 */
	RealTupleType	newRangeRealTupleType = (RealTupleType)newRangeType;
	int		componentCount = newRangeRealTupleType.getDimension();
	FlatField[]	flatFields = new FlatField[componentCount];
	for (int i = flatFields.length; --i >= 0; )
	{
	  RealType	componentType =
	    (RealType)newRangeRealTupleType.getComponent(i);
	  int	componentIndex = getComponentIndex(field, componentType);
	  flatFields[i] = 
	    componentIndex >= 0
	      ? (FlatField)field.extract(componentIndex)
	      : new FlatField(
		  new FunctionType(getDomainType(field), componentType),
		  field.getDomainSet());
	}
	temporaryField = (FlatField)FieldImpl.combine(flatFields);
      }
      Unit[][]	temporaryRangeUnits = temporaryField.getRangeUnits();
      Unit[]	rangeUnits = new Unit[temporaryRangeUnits.length];
      for (int i = rangeUnits.length; --i >= 0; )
	rangeUnits[i] = temporaryRangeUnits[i][0];
      FlatField	newFlatField =
	new FlatField(
	  new FunctionType(getDomainType(field), newRangeType),
	  field.getDomainSet(),
	  (CoordinateSystem)null,
	  (CoordinateSystem[])null,
	  temporaryField.getRangeSets(),
	  rangeUnits);
      newFlatField.setRangeErrors(temporaryField.getRangeErrors());
      newFlatField.setSamples(temporaryField.getValues(false), true);
      result = newFlatField;
    }
    else if (newRangeType instanceof TupleType)
    {
      throw new UnimplementedException(
	"Can't yet create Field with range " + newRangeType + 
	" from existing Field");
    }
    else
    {
      throw new TypeException("Can't create Field with range " + newRangeType);
    }
    return result;
  }

  /**
   * Provides support for comparing RealTuple-s of the same RealTupleType.
   *
   * @author Steven R. Emmerson
   */
  public static final class
  RealTupleComparator
    implements Comparator
  {
    /**
     * The single instance.
     */
    public static final RealTupleComparator	INSTANCE =
      new RealTupleComparator();

    /**
     * Constructs from nothing.
     */
    private
    RealTupleComparator()
    {}

    /**
     * Compares two RealTuple-s of the same RealTupleType.
     *
     * @param obj1		The first RealTuple.
     * @param obj2		The second RealTuple.  It shall have the same
     *				RealTupleType as the first RealTuple.
     * @return			A negative integer, zero, or a positive integer
     *				as the first RealTuple is less than, equal to,
     *				or greater than the second RealTuple.
     * @throws ClassCastException
     *                          The types of the arguments prevent this
     *                          comparator from comparing them.
     */
    public int
    compare(Object obj1, Object obj2)
      throws ClassCastException
    {
      RealTuple	realTuple1 = (RealTuple)obj1;
      RealTuple	realTuple2 = (RealTuple)obj2;
      try
      {
	int		rank = realTuple1.getDimension();
	int		comp = 0;
	/*
	 * Because Set rasterization has the last component as the outermost
	 * dimension, the last tuple component is treated as the grossest one
	 * for the purpose of comparison.  Hence, components are compared in
	 * decreasing order.
	 */
	for (int i = rank; --i >= 0 && comp == 0; )
	  comp = ((Real)realTuple1.getComponent(i)).compareTo(
	    ((Real)realTuple2.getComponent(i)));
	return comp;
      }
      catch (Exception e)
      {
	/*
	 * This is the only checked exception a Comparator is allowed to throw.
	 * The original exception could be either a visad.VisADException or a 
	 * java.rmi.RemoteException.
	 */
	String	reason = e.getMessage();
	throw new ClassCastException("Can't compare RealTuple-s" +
	  (reason == null ? "" : ": " + reason));
      }
    }
  }

  /**
   * Consolidates fields.
   * @param fields		The fields to consolidate.  Each field shall
   *				have the same FunctionType.
   * @return                    The input Fields consolidated into one Field.
   *                            The domain shall be a GriddedSet comprising the
   *                            union of the sample points of the fields.  The
   *                            FunctionType shall be the same as that of the
   *                            input Field-s.  When more than one input Field
   *                            has valid range data for the same domain point,
   *                            it is unspecified which range datum is used for
   *                            the output Field.
   * @throws FieldException	The Field array has zero length.
   * @throws TypeException	Input Field-s not all same type.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static Field
  consolidate(Field[] fields)
    throws FieldException, TypeException, VisADException, RemoteException
  {
    /*
     * Determine the consolidated domain.
     */
    if (fields.length == 0)
      throw new FieldException(
	DataUtility.class.getName() + "(Field[]): Zero fields to consolidate");
    FunctionType	funcType = (FunctionType)fields[0].getType();
    TreeSet		consolidatedDomainTuples = 
      new TreeSet(RealTupleComparator.INSTANCE);
    for (int i = fields.length; --i >= 0; )
    {
	Field	field = fields[i];
	if (!field.getType().equals(funcType))
	  throw new TypeException("Field type mismatch");
	if (!field.isMissing())
	{
	  for (Enumeration enum = field.domainEnumeration();
	      enum.hasMoreElements(); )
	  {
	      RealTuple	domainSample = (RealTuple)enum.nextElement();
	      consolidatedDomainTuples.add(domainSample);
	  }
	}
    }
    /*
     * Create the consolidated field (with no range data).
     */
    Field	field = fields[0];
    float[][]	domainFloats =
      new float[field.getDomainDimension()][consolidatedDomainTuples.size()];
    Unit[]	domainUnits = field.getDomainUnits();
    int		sampleIndex = 0;
    for (Iterator iter = consolidatedDomainTuples.iterator();
	iter.hasNext();
	++sampleIndex)
    {
      RealTuple	domainTuple = (RealTuple)iter.next();
      for (int i = domainFloats.length; --i >= 0; )
	domainFloats[i][sampleIndex] =
	  (float)((Real)domainTuple.getComponent(i)).getValue(domainUnits[i]);
    }
    SampledSet	consolidatedDomain =
      domainFloats.length == 1
	? (SampledSet)new Gridded1DSet(
	    getDomainType(field), domainFloats, domainFloats[0].length,
	    (CoordinateSystem)null, field.getDomainUnits(), 
	    (ErrorEstimate[])null)
	: domainFloats.length == 2
	  ? (SampledSet)new Irregular2DSet(
	      getDomainType(field), domainFloats, (CoordinateSystem)null, 
	      field.getDomainUnits(), (ErrorEstimate[])null, (Delaunay)null)
	  : domainFloats.length == 3
	    ? (SampledSet)new Irregular3DSet(
		getDomainType(field), domainFloats, (CoordinateSystem)null,
		field.getDomainUnits(), (ErrorEstimate[])null, (Delaunay)null)
	    : (SampledSet)new IrregularSet(
		getDomainType(field), domainFloats, (CoordinateSystem)null,
		field.getDomainUnits(), (ErrorEstimate[])null);
    Field	consolidatedField = 
      field instanceof FlatField
	? new FlatField(funcType, consolidatedDomain)
	: new FieldImpl(funcType, consolidatedDomain);
    /*
     * Set the range of the consolidated field.
    for (int i = fields.length; --i >= 0; )
    {
      Field	field = fields[i];
      if (!field.isMissing())
      {
	for (Enumeration enum = field.domainEnumeration();
	    enum.hasMoreElements(); )
	{
	    RealTuple	domainSample = (RealTuple)enum.nextElement();
	    Data	rangeSample = field.evaluate(domainSample);
	    if (!rangeSample.isMissing())
	      consolidatedField.setSample(domainSample, rangeSample);
	}
      }
    }
     */
    for (Iterator iter = consolidatedDomainTuples.iterator(); iter.hasNext(); )
    {
      RealTuple	domainTuple = (RealTuple)iter.next();
      for (int i  = fields.length; --i >= 0; )
      {
	Data	datum = fields[i].evaluate(domainTuple);
	if (!datum.isMissing())
	{
	  consolidatedField.setSample(domainTuple, datum);
	  break;
	}
      }
    }
    return consolidatedField;
  }

  /**
   * Creates a GriddedSet from a FlatField.  The GriddedSet will be created from
   * the domain and flat-range of the FlatField.  The first components in the
   * tuples of the GriddedSet will come from the domain of the FlatField and the
   * remaining components will come from the range of the FlatField.  Note that,
   * because a GriddedSet doesn't have the ability to contain values in small
   * primitives (e.g. byte, short), the returned set may be significantly larger
   * than the input field.
   * @param field		The FlatField from which to create a GriddedSet.
   * @param copy		Whether or not to copy the range values from
   *				the field (i.e. <code>field.getFloats(copy)
   *				</code>).
   * @return			The GriddedSet corresponding to the input
   *				FlatField.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @throws RemoteException	Java RMI failure.
   */
  public static GriddedSet
  createGriddedSet(FlatField field, boolean copy)
    throws VisADException, RemoteException
  {
    int			domainRank = field.getDomainDimension();
    int			flatRangeRank = field.getRangeDimension();
    RealType[]		realTypes = new RealType[domainRank + flatRangeRank];
    RealTupleType	tupleType = getDomainType(field);
    for (int i = 0; i < domainRank; i++)
      realTypes[i] = (RealType)tupleType.getComponent(i);
    tupleType = getFlatRangeType(field);
    for (int i = 0; i < flatRangeRank; i++)
      realTypes[domainRank+i] = (RealType)tupleType.getComponent(i);
    float[][]	samples = new float[realTypes.length][field.getLength()];
    /*
     * The following gets the domain values in their actual units.
     */
    System.arraycopy(
      field.getDomainSet().getSamples(), 0, samples, 0, domainRank);
    /*
     * The following gets the range values in their default units.
     */
    System.arraycopy(
      field.getFloats(copy), 0, samples, domainRank, flatRangeRank);
    int[]		lengths = new int[samples.length];
    for (int i = samples.length; --i >= 0; )
      lengths[i] = samples[i].length;
    Unit[]		units = new Unit[realTypes.length];
    System.arraycopy(field.getDomainUnits(), 0, units, 0, domainRank);
    System.arraycopy(
      field.getDefaultRangeUnits(), 0, units, domainRank, flatRangeRank);
    return
      GriddedSet.create(
	new RealTupleType(realTypes),
	samples,
	lengths,
	(CoordinateSystem)null,
	units,
	(ErrorEstimate[])null);
  }

  /**
   * Simplifies a MathType.  Removes all enclosing, single-component TupleType-s
   * until the "core" is revealed (e.g. ScalarType, multi-component TupleType).
   * @param type		The MathType to be simplified.
   * @return			The simplest form corresponding to 
   *				<code>type</code>.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public static MathType
  simplify(MathType type)
    throws VisADException
  {
    while (type instanceof TupleType && ((TupleType)type).getDimension() == 1)
	type = ((TupleType)type).getComponent(0);
    return type;
  }
}
