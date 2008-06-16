//
// DataUtility.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Vector;
import java.util.StringTokenizer;

// RMI classes
import java.rmi.RemoteException;

import visad.java2d.DisplayImplJ2D;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;

// GUI handling
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

// VisAD packages
import visad.*;

/** A general utility class for VisAD Data objects */
public class DataUtility {

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

  /** 
   * Create a VisAD Data object from the given Image 
   * @param  image   image to use
   * @return a FlatField representation of the image
   */
  public static FlatField makeField(Image image)
    throws IOException, VisADException
  {
    return makeField(image, false);
  }

  /** 
   * Create a VisAD Data object from the given Image 
   * @param  image   image to use
   * @param  withAlpha   include Alpha in the field if the image ColorModel
   *                     supports it and the image is not opaque.
   * @return a FlatField representation of the image
   */
  public static FlatField makeField(Image image, boolean withAlpha)
    throws IOException, VisADException
  {
    if (image == null) {
      throw new VisADException("image cannot be null");
    }
    ImageHelper ih = new ImageHelper();

    // determine image height and width
    int width = -1;
    int height = -1;
    do {
      if (width < 0) width = image.getWidth(ih);
      if (height < 0) height = image.getHeight(ih);
      if (ih.badImage || (width >= 0 && height >= 0)) break;
      try { Thread.sleep(100); } catch (InterruptedException e) { }
    }
    while (true);
    if (ih.badImage) throw new IOException("Not an image");

    // extract image pixels
    int numPixels = width * height;
    int[] words = new int[numPixels];

    PixelGrabber grabber = new PixelGrabber(
      image.getSource(), 0, 0, width, height, words, 0, width);

    try { grabber.grabPixels(); }
    catch (InterruptedException e) { }

    ColorModel cm = grabber.getColorModel();
    boolean doAlpha = cm.hasAlpha() && withAlpha;
    //System.out.println("do alpha: " + doAlpha);

    float[] red_pix = new float[numPixels];
    float[] green_pix = new float[numPixels];
    float[] blue_pix = new float[numPixels];
    float[] alpha_pix = null;

    for (int i=0; i<numPixels; i++) {
      red_pix[i] = cm.getRed(words[i]);
      green_pix[i] = cm.getGreen(words[i]);
      blue_pix[i] = cm.getBlue(words[i]);
    }
    boolean opaque = true;
    if (doAlpha) {
      alpha_pix = new float[numPixels];
      for (int i=0; i<numPixels; i++) {
        alpha_pix[i] = cm.getAlpha(words[i]);
        if (alpha_pix[i] != 255.0f) opaque = false;
      }
    }

    if (opaque && doAlpha) {  // if opaque, don't include alpha
      doAlpha = false;
      alpha_pix = null;
    }
    //System.out.println("opaque = " + opaque);

    // build FlatField
    RealType line = RealType.getRealType("ImageLine");
    RealType element = RealType.getRealType("ImageElement");
    RealType c_red = RealType.getRealType("Red");
    RealType c_green = RealType.getRealType("Green");
    RealType c_blue = RealType.getRealType("Blue");
    RealType c_alpha = RealType.getRealType("Alpha");

    RealType[] c_all = 
       (doAlpha) ? new RealType[] {c_red, c_green, c_blue, c_alpha}
                  : new RealType[] {c_red, c_green, c_blue};
    RealTupleType radiance = new RealTupleType(c_all);

    RealType[] domain_components = {element, line};
    RealTupleType image_domain = new RealTupleType(domain_components);
    Linear2DSet domain_set = new Linear2DSet(image_domain, 0.0,
      (float) (width - 1.0), width, (float) (height - 1.0), 0.0, height);
    FunctionType image_type = new FunctionType(image_domain, radiance);

    FlatField field = new FlatField(image_type, domain_set);

    float[][] samples = new float[doAlpha?4:3][];
    samples[0] = red_pix;
    samples[1] = green_pix;
    samples[2] = blue_pix;
    if (doAlpha) samples[3] = alpha_pix;
    try { field.setSamples(samples, false); }
    catch (RemoteException e) {
      throw new VisADException("Couldn't finish image initialization");
    }

    return field;
  }

  /**
   * Converts a flat field of the form <tt>((x, y) -&gt; (r, g, b))</tt>
   * to an AWT Image. If reverse flag is set, image will be upside-down.
   */
  public static Image extractImage(FlatField field, boolean reverse) {
    try {
      GriddedSet set = (GriddedSet) field.getDomainSet();
      int[] wh = set.getLengths();
      int w = wh[0];
      int h = wh[1];
      double[][] samples = field.getValues();
      int[] pixels = new int[samples[0].length];
      if (samples.length == 3) {
        int len = samples[0].length;
        for (int i=0; i<len; i++) {
          int r = (int) samples[0][i] & 0x000000ff;
          int g = (int) samples[1][i] & 0x000000ff;
          int b = (int) samples[2][i] & 0x000000ff;
          int index = reverse ? len - i - 1 : i;
          pixels[index] = r << 16 | g << 8 | b;
        }
      }
      else {
        int len = samples[0].length;
        for (int i=0; i<len; i++) {
          int v = (int) samples[0][i] & 0x000000ff;
          int index = reverse ? len - i - 1 : i;
          pixels[index] = v << 16 | v << 8 | v;
        }
      }
      MemoryImageSource source = new MemoryImageSource(w, h,
        new DirectColorModel(24, 0xff0000, 0xff00, 0xff), pixels, 0, w);
      source.setFullBufferUpdates(true);
      return Toolkit.getDefaultToolkit().createImage(source);
    }
    catch (VisADException exc) {
      return null;
    }
  }

  public static FlatField[] getImageFields(Data data) {
    FlatField[] fields = null;
    String pcImageType = "((e, l) -> v)";
    String rgbImageType = "((e, l) -> (r, g, b))";
    String pcTimeType = "(t -> " + pcImageType + ")";
    String rgbTimeType = "(t -> " + rgbImageType + ")";
    try {
      MathType type = data.getType();
      if (type.equalsExceptName(MathType.stringToType(pcTimeType)) ||
        type.equalsExceptName(MathType.stringToType(rgbTimeType)))
      {
        FieldImpl fi = (FieldImpl) data;
        int len = fi.getLength();
        fields = new FlatField[len];
        for (int i=0; i<len; i++) fields[i] = (FlatField) fi.getSample(i);
      }
      else if (type.equalsExceptName(MathType.stringToType(pcImageType)) ||
        type.equalsExceptName(MathType.stringToType(rgbImageType)))
      {
        fields = new FlatField[1];
        fields[0] = (FlatField) data;
      }
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    return fields;
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
        display.destroy();
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
  public static TupleIface
  ensureTuple(Data datum)
    throws VisADException, RemoteException
  {
    return
      datum instanceof TupleIface
	? (TupleIface)datum
	: datum instanceof Real
	    ? new RealTuple(new Real[] {(Real)datum})
	    : new Tuple(new Data[] {datum});
  }

  /**
   * Gets the MathType of the domain of a Set.
   * @param set			A set.
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
   * Ensures that the range of a Field is a given type.  Extracts from
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
      int	realTupleIndex = getComponentIndex(field, newRangeType);
      if (realTupleIndex >= 0)
      {
	/*
	 * The desired RealTuple range is a component of the input Field.
	 */
	result = (FlatField)field.extract(realTupleIndex);
      }
      else
      {
	/*
	 * The desired RealTuple range is not a component of the input Field.
	 * Extract each component of the desired RealTuple into a separate
	 * Field and then combine the Field-s.
	 */
	RealTupleType	newRangeRealTupleType = (RealTupleType)newRangeType;
	int		componentCount = newRangeRealTupleType.getDimension();
	ArrayList	flatFields = new ArrayList(componentCount);
	for (int i = 0; i < componentCount; ++i)
	{
	  int	componentIndex =
	    getComponentIndex(field, newRangeRealTupleType.getComponent(i));
	  if (componentIndex >= 0)
	    flatFields.add(field.extract(componentIndex));
	}
	if (flatFields.size() != componentCount)
	{
	  /* Not all desired components exist in the range of the input Field */
	  result =
	    new FlatField(
	      new FunctionType(getDomainType(field), newRangeType),
	      field.getDomainSet());
	}
	else
	{
	  /* All desired components exist in the range of the input Field */
	  result =
	    (FlatField)FieldImpl.combine(
	      (FlatField[])flatFields.toArray(new FlatField[componentCount]));
	}
      }
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
	int	rank = realTuple1.getDimension();
	int	comp = 0;
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
   * Provides support for comparing domain points that are reference to a
   * particular Field.
   */
  private static class
  ReferencedDomainPoint
    implements Comparable
  {
    protected final RealTuple	sample;
    protected final Field	field;
    public ReferencedDomainPoint(RealTuple sample, Field field)
    {
      this.sample = sample;
      this.field = field;
    }
    public int compareTo(Object obj)
    {
      return
	RealTupleComparator.INSTANCE.compare(
	  sample, ((ReferencedDomainPoint)obj).sample);
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
     * Identify valid fields.
     */
    ArrayList	validFields = new ArrayList(fields.length);
    for (int i = 0; i < fields.length; ++i)
    {
      Field	field = fields[i];
      if (!field.isMissing())
	validFields.add(field);
    }
    if (validFields.size() == 0)
      throw new FieldException(
	DataUtility.class.getName() + "(Field[]): Zero fields to consolidate");

    /*
     * Determine the consolidated domain.
     */
    FunctionType		funcType = (FunctionType)fields[0].getType();
    TreeSet	consolidatedDomainTuples = new TreeSet();
    for (Iterator iter = validFields.iterator(); iter.hasNext(); )
    {
      Field	field = (Field)iter.next();
      if (!field.getType().equals(funcType))
	throw new TypeException("Field type mismatch");
      for (Enumeration en = field.domainEnumeration();
	  en.hasMoreElements(); )
      {
	consolidatedDomainTuples.add(
	  new ReferencedDomainPoint((RealTuple)en.nextElement(), field));
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
      RealTuple	domainTuple = ((ReferencedDomainPoint)iter.next()).sample;
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
     */
    for (Iterator iter = consolidatedDomainTuples.iterator(); iter.hasNext(); )
    {
      ReferencedDomainPoint	point = (ReferencedDomainPoint)iter.next();
      RealTuple			domainTuple = point.sample;
      consolidatedField.setSample(
	domainTuple, point.field.evaluate(domainTuple));
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
  public static MathType simplify(MathType type)
    throws VisADException
  {
    while (type instanceof TupleType && ((TupleType)type).getDimension() == 1)
	type = ((TupleType)type).getComponent(0);
    return type;
  }

  /**
   * @deprecated Use getScalarTypes(Data, Vector) instead.
   */
  public static int getRealTypes(Data data, Vector v)
    throws VisADException, RemoteException
  {
    return getRealTypes(new Data[] {data}, v, true, false);
  }

  /**
   * @deprecated Use getScalarTypes(Data[], Vector, boolean, boolean) instead.
   */
  public static int getRealTypes(Data[] data, Vector v, boolean keepDupl,
    boolean doCoordSys) throws VisADException, RemoteException
  {
    int dupl = getScalarTypes(data, v, keepDupl, doCoordSys);
    int i = 0;
    while (i < v.size()) {
      ScalarType st = (ScalarType) v.elementAt(i);
      if (!(st instanceof RealType)) v.remove(i);
      else i++;
    }
    return dupl;
  }

  /**
   * Obtains a Vector consisting of all ScalarTypes present in a Data object's
   * MathType.
   * @param data                The Data from which to extract the ScalarTypes.
   * @param v                   The Vector in which to store the ScalarTypes.
   * @throws VisADException     Couldn't parse the Data's MathType.
   * @throws RemoteException    Couldn't obtain the remote Data's MathType.
   * @return                    The number of duplicate ScalarTypes found.
   */
  public static int getScalarTypes(Data data, Vector v)
    throws VisADException, RemoteException
  {
    return getScalarTypes(new Data[] {data}, v, true, false);
  }

  /**
   * Obtains a Vector consisting of all ScalarTypes present in an array of
   * Data objects' MathTypes.
   * @param data                The array of Data from which to extract the
   *                            ScalarTypes.
   * @param v                   The Vector in which to store the ScalarTypes.
   * @param keepDupl            Whether to add a RealType to the Vector when
   *                            it is already present there.
   * @param doCoordSys          Whether to include ScalarTypes from
   *                            CoordinateSystem references.
   * @throws VisADException     Couldn't parse a Data's MathType.
   * @throws RemoteException    Couldn't obtain a remote Data's MathType.
   * @return                    The number of duplicate ScalarTypes found.
   */
  public static int getScalarTypes(Data[] data, Vector v, boolean keepDupl,
    boolean doCoordSys) throws VisADException, RemoteException
  {
    Vector coord = (doCoordSys ? new Vector() : null);
    int[] dupl = new int[1];
    dupl[0] = 0;
    for (int i=0; i<data.length; i++) {
      Data d = data[i];
      if (d != null) {
        MathType type = d.getType();
        parse(type, v, dupl, keepDupl, coord);
      }
    }
    if (coord != null) {
      // append coordinate system reference ScalarTypes to vector
      for (int i=0; i<coord.size(); i++) {
        Object o = coord.elementAt(i);
        boolean c = v.contains(o);
        if (c) dupl[0]++;
        if (keepDupl || !c) v.add(o);
      }
    }
    return dupl[0];
  }

  /**
   * getScalarTypes helper method.
   */
  private static void parse(MathType mathType, Vector v, int[] i,
    boolean keepDupl, Vector coord) throws VisADException
  {
    if (mathType instanceof FunctionType) {
      parseFunction((FunctionType) mathType, v, i, keepDupl, coord);
    }
    else if (mathType instanceof SetType) {
      parseSet((SetType) mathType, v, i, keepDupl, coord);
    }
    else if (mathType instanceof TupleType) {
      parseTuple((TupleType) mathType, v, i, keepDupl, coord);
    }
    else parseScalar((ScalarType) mathType, v, i, keepDupl);
  }

  /**
   * getScalarTypes helper method.
   */
  private static void parseFunction(FunctionType mathType, Vector v, int[] i,
    boolean keepDupl, Vector coord) throws VisADException
  {
    // extract domain
    RealTupleType domain = mathType.getDomain();
    parseTuple((TupleType) domain, v, i, keepDupl, coord);

    // extract range
    MathType range = mathType.getRange();
    parse(range, v, i, keepDupl, coord);
  }

  /**
   * getScalarTypes helper method.
   */
  private static void parseSet(SetType mathType, Vector v, int[] i,
    boolean keepDupl, Vector coord) throws VisADException
  {
    // extract domain
    RealTupleType domain = mathType.getDomain();
    parseTuple((TupleType) domain, v, i, keepDupl, coord);
  }

  /**
   * getScalarTypes helper method.
   */
  private static void parseTuple(TupleType mathType, Vector v, int[] i,
    boolean keepDupl, Vector coord) throws VisADException
  {
    // extract components
    for (int j=0; j<mathType.getDimension(); j++) {
      MathType cType = mathType.getComponent(j);
      if (cType != null) parse(cType, v, i, keepDupl, coord);
    }
    if (mathType instanceof RealTupleType && coord != null) {
      // add coordinate system references
      RealTupleType realTupleType = (RealTupleType) mathType;
      CoordinateSystem coordSys = realTupleType.getCoordinateSystem();
      if (coordSys != null) {
        RealTupleType ref = coordSys.getReference();
        for (int j=0; j<realTupleType.getDimension(); j++) {
          RealType rType = (RealType) realTupleType.getComponent(j);
          parseScalar(rType, coord, new int[1], keepDupl);
        }
        for (int j=0; j<ref.getDimension(); j++) {
          RealType rType = (RealType) ref.getComponent(j);
          parseScalar(rType, coord, new int[1], keepDupl);
        }
      }
    }
  }

  /**
   * getScalarTypes helper method.
   */
  private static void parseScalar(ScalarType mathType, Vector v, int[] i,
    boolean keepDupl)
  {
    if (v.contains(mathType)) {
      if (keepDupl) v.add(mathType);
      i[0]++;
    }
    else v.add(mathType);
  }

  /**
   * Attempts to guess a good set of mappings for a display containing
   * Data objects of the given types. The algorithm simply returns mappings
   * for the first successfully guessed dataset.
   */
  public static ScalarMap[] guessMaps(MathType[] types, boolean allow3d) {
    int len = types.length;
    ScalarMap[] maps = null;
    for (int i=0; i<len; i++) {
      MathType t = types[i];
      if (t != null) maps = t.guessMaps(allow3d);
      if (maps != null) break;
    }
    return maps;
  }

  /**
   * Converts the given vector of mappings to an easy-to-read String form.
   */
  public static String convertMapsToString(Vector v) {
    int len = v.size();
    ScalarMap[] sm = new ScalarMap[len];
    for (int i=0; i<len; i++) sm[i] = (ScalarMap) v.elementAt(i);
    return convertMapsToString(sm);
  }

  /**
   * Converts the given array of mappings to an easy-to-read String form.
   */
  public static String convertMapsToString(ScalarMap[] sm) {
    StringBuffer sb = new StringBuffer(128);
    for (int i=0; i<sm.length; i++) {
      ScalarMap m = sm[i];
      ScalarType domain = m.getScalar();
      DisplayRealType range = m.getDisplayScalar();
      int q = -1;
      for (int j=0; j<Display.DisplayRealArray.length; j++) {
        if (range.equals(Display.DisplayRealArray[j])) q = j;
      }
      sb.append(' ');
      sb.append(domain.getName());
      sb.append(' ');
      sb.append(q);
    }
    return sb.toString();
  }

  /**
   * Converts the given map string to its corresponding array of mappings.
   * @param mapString      The String from which to extract the ScalarMaps.
   * @param data           The Data object to search for valid ScalarTypes.
   * @param showErrors     Whether to output errors to stdout.
   */
  public static ScalarMap[] convertStringToMaps(
    String mapString, Data data, boolean showErrors)
  {
    return convertStringToMaps(mapString, new Data[] {data}, showErrors);
  }

  /**
   * Converts the given map string to its corresponding array of mappings.
   * @param mapString      The String from which to extract the ScalarMaps.
   * @param data           The Data objects to search for valid ScalarTypes.
   * @param showErrors     Whether to output errors to stdout.
   */
  public static ScalarMap[] convertStringToMaps(
    String mapString, Data[] data, boolean showErrors)
  {
    Vector types = new Vector();
    for (int i=0; i<data.length; i++) {
      try {
        getScalarTypes(data[i], types);
      }
      catch (VisADException exc) {
        if (showErrors) {
          System.out.println("Warning: " +
            "could not extract ScalarTypes from Data object.");
        }
      }
      catch (RemoteException exc) {
        if (showErrors) {
          System.out.println("Warning: " +
            "could not extract ScalarTypes from Data object.");
        }
      }
    }
    return convertStringToMaps(mapString, types, showErrors);
  }

  /**
   * Converts the given map string to its corresponding array of mappings.
   * @param mapString      The String from which to extract the ScalarMaps.
   * @param types          List of valid ScalarTypes.
   * @param showErrors     Whether to output errors to stdout.
   */
  public static ScalarMap[] convertStringToMaps(
    String mapString, Vector types, boolean showErrors)
  {
    // extract mapping information from string
    if (mapString == null) return null;
    StringTokenizer st = new StringTokenizer(mapString);
    Vector dnames = new Vector();
    Vector rnames = new Vector();
    while (true) {
      if (!st.hasMoreTokens()) break;
      String s = st.nextToken();
      if (!st.hasMoreTokens()) {
        if (showErrors) {
          System.err.println("Warning: trailing maps value " + s +
            " has no corresponding number and will be ignored");
        }
        continue;
      }
      String si = st.nextToken();
      Integer i = null;
      try {
        i = new Integer(Integer.parseInt(si));
      }
      catch (NumberFormatException exc) { }
      if (i == null) {
        if (showErrors) {
          System.err.println("Warning: maps value " + si + " is not a " +
            "valid integer and the maps pair (" + s + ", " + si + ") " +
            "will be ignored");
        }
      }
      else {
        dnames.add(s);
        rnames.add(i);
      }
    }

    // set up mappings
    if (dnames != null) {
      int len = dnames.size();
      if (len > 0) {
        int vLen = types.size();
        int dLen = Display.DisplayRealArray.length;

        // construct ScalarMaps
        ScalarMap[] maps = new ScalarMap[len];
        for (int j=0; j<len; j++) {
          // find appropriate ScalarType
          ScalarType mapDomain = null;
          String name = (String) dnames.elementAt(j);
          for (int k=0; k<vLen; k++) {
            ScalarType type = (ScalarType) types.elementAt(k);
            if (name.equals(type.getName())) {
              mapDomain = type;
              break;
            }
          }
          if (mapDomain == null) {
            // still haven't found type; look in static Vector for it
            mapDomain = ScalarType.getScalarTypeByName(name);
          }

          // find appropriate DisplayRealType
          int q = ((Integer) rnames.elementAt(j)).intValue();
          DisplayRealType mapRange = null;
          if (q >= 0 && q < dLen) mapRange = Display.DisplayRealArray[q];

          // construct mapping
          if (mapDomain == null || mapRange == null) {
            if (showErrors) {
              System.err.print("Warning: maps pair (" + name + ", " +
                q + ") has an invalid ");
              if (mapDomain == null && mapRange == null) {
                System.err.print("domain and range");
              }
              else if (mapDomain == null) System.err.print("domain");
              else System.err.print("range");
              System.err.println(" and will be ignored");
            }
            maps[j] = null;
          }
          else {
            try {
              maps[j] = new ScalarMap(mapDomain, mapRange);
            }
            catch (VisADException exc) {
              if (showErrors) {
                System.err.println("Warning: maps pair (" + name + ", " +
                  q + ") cannot be converted to a ScalarMap");
              }
              maps[j] = null;
            }
          }
        }
        return maps;
      }
    }

    return null;
  }

  /**
   * Converts an array of strings into a VisAD Tuple.
   *
   * @param s The array of strings to be converted to a VisAD Tuple.
   *
   * @return VisAD Tuple, or null if Tuple could not be created.
   */
  public static Tuple stringsToTuple(String[] s) {
    return stringsToTuple(s, false);
  }

  /**
   * Converts an array of strings into a VisAD Tuple.
   *
   * @param s The array of strings to be converted to a VisAD Tuple.
   * @param printStackTraces <tt>true</tt> if the stack trace for
   *                         any exception should be printed.
   *
   * @return VisAD Tuple, or null if Tuple could not be created.
   */
  public static Tuple stringsToTuple(String[] s, boolean printStackTraces) {
    try {
      if (s == null) return null;
      int len = s.length;
      if (len == 0) return null;
      Text[] text = new Text[len];
      for (int i=0; i<len; i++) text[i] = new Text(s[i]);
      Tuple tuple = new Tuple(text);
      return tuple;
    }
    catch (VisADException exc) {
      if (printStackTraces) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (printStackTraces) exc.printStackTrace();
    }
    return null;
  }

  /**
   * Converts a VisAD tuple into an array of strings.
   *
   * @param t The VisAD Tuple to be converted to an array of strings.
   *
   * @return Array of Strings, or null if array could not be created.
   */
  public static String[] tupleToStrings(Tuple t) {
    return tupleToStrings(t, false);
  }

  /**
   * Converts a VisAD tuple into an array of strings.
   *
   * @param t The VisAD Tuple to be converted to an array of strings.
   * @param printStackTraces <tt>true</tt> if the stack trace for
   *                         any exception should be printed.
   *
   * @return Array of Strings, or null if array could not be created.
   */
  public static String[] tupleToStrings(Tuple t, boolean printStackTraces) {
    if (t == null) return null;
    int len = t.getDimension();
    try {
      String[] errors = new String[len];
      for (int i=0; i<len; i++) {
        Text text = (Text) t.getComponent(i);
        errors[i] = text.getValue();
      }
      return errors;
    }
    catch (VisADException exc) {
      if (printStackTraces) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (printStackTraces) exc.printStackTrace();
    }
    return null;
  }

  /**
   * Verify that an object is Serializable by attempting to
   * serialize it.
   *
   * @param obj An object which needs to be serialized
   *
   * @return <tt>true</tt> if the object is Serializable, false otherwise.
   */
  public static boolean isSerializable(Object obj) {
    return isSerializable(obj, false);
  }

  /**
   * Verify that an object is Serializable by attempting to
   * serialize it.
   *
   * @param obj An object which needs to be serialized
   * @param printStackTraces <tt>true</tt> if the stack trace for
   *                         any exception should be printed.
   *
   * @return <tt>true</tt> if the object is Serializable, false otherwise.
   */
  public static boolean isSerializable(Object obj, boolean printStackTraces)
  {
    java.io.ByteArrayOutputStream outBytes;
    outBytes = new java.io.ByteArrayOutputStream();

    java.io.ObjectOutputStream outStream;
    try {
      outStream = new java.io.ObjectOutputStream(outBytes);
    } catch (java.io.IOException ioe) {
      if (printStackTraces) ioe.printStackTrace();
      return false;
    }

    try {
      outStream.writeObject(obj);
      outStream.flush();
    } catch (java.io.IOException ioe) {
      if (printStackTraces) ioe.printStackTrace();
      return false;
    }

    java.io.ByteArrayInputStream inBytes;
    inBytes = new java.io.ByteArrayInputStream(outBytes.toByteArray());

    try {
      outStream.close();
      outBytes.close();
    } catch (java.io.IOException ioe) {
      if (printStackTraces) ioe.printStackTrace();
      return false;
    }

    java.io.ObjectInputStream inStream;
    try {
      inStream = new java.io.ObjectInputStream(inBytes);
    } catch (java.io.IOException ioe) {
      if (printStackTraces) ioe.printStackTrace();
      return false;
    }

    Object obj2;
    try {
      obj2 = inStream.readObject();
    } catch (java.io.IOException ioe) {
      if (printStackTraces) ioe.printStackTrace();
      return false;
    } catch (ClassNotFoundException cnfe) {
      if (printStackTraces) cnfe.printStackTrace();
      return false;
    }

    try {
      inStream.close();
      inBytes.close();
    } catch (java.io.IOException ioe) {
      if (printStackTraces) ioe.printStackTrace();
      return false;
    }

    return true;
  }

  /**
   * Converts a remote Data object to a local Data object.
   *
   * @param data The Data object to be made local.
   *
   * @return Local Data object, or null if Data could not be converted.
   */
  public static DataImpl makeLocal(Data data) {
    return makeLocal(data, false);
  }

  /**
   * Converts a remote Data object to a local Data object.
   *
   * @param data The Data object to be made local.
   * @param printStackTraces <tt>true</tt> if the stack trace for
   *                         any exception should be printed.
   *
   * @return Local Data object, or null if Data could not be converted.
   */
  public static DataImpl makeLocal(Data data, boolean printStackTraces) {
    try {
      if (data != null) return data.local();
    }
    catch (VisADException exc) {
      if (printStackTraces) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (printStackTraces) exc.printStackTrace();
    }
    return null;
  }

  /**
   * Gets the specified sample of a VisAD Set.
   *
   * @param set               The set to have a sample returned.
   * @param index             The index of the sample to be returned.
   * @return                  The sample at the specified index.  Will
   *                          be empty if the index is out-of-bounds.
   *                          Will have a single component if the set is
   *                          one-dimensional.
   * @throws VisADException   Couldn't create necessary VisAD object.
   * @throws RemoteException  Java RMI failure.
   */
  public static RealTuple getSample(Set set, int index)
          throws VisADException, RemoteException {

    RealTuple     sample;
    RealTupleType realTupleType = ((SetType) set.getType()).getDomain();
    double[][]    values        =
      Unit.convertTuple(set.indexToDouble(new int[]{ index }),
                        set.getSetUnits(),
                        realTupleType.getDefaultUnits());

    if ((index < 0) || (index >= set.getLength())) {
      sample = new RealTuple(realTupleType);
    } else {
      double[] doubles = new double[values.length];

      for (int i = doubles.length; --i >= 0; ) {
        doubles[i] = values[i][0];
      }

      sample = new RealTuple(realTupleType, doubles);
    }

    return sample;
  }

  /**
   * Gets the units of the (flat) components of the range of a FlatField.
   *
   * @param field             The FlatField.
   * @return                  The units of the (flat) components of the
   *                          range of the FlatField.  Won't be
   *                          <code>null</code>.
   */
  public static Unit[] getRangeUnits(FlatField field) {

      Unit[][] units  = field.getRangeUnits();
      Unit[]   result = new Unit[units.length];

      for (int i = result.length; --i >= 0; ) {
          result[i] = units[i][0];
      }

      return result;
  }
}
