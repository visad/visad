//
// JPythonMethods.java
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

package visad.python;

import java.util.Vector;
import java.awt.event.*;
import java.rmi.RemoteException;
import javax.swing.JFrame;
import java.lang.reflect.InvocationTargetException;

import visad.*;
import visad.math.*;
import visad.matrix.*;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.data.*;
import visad.ss.MappingDialog;
import visad.bom.ImageRendererJ3D;

/**
 * A collection of methods for working with VisAD, callable from the
 * JPython editor.
 */
public abstract class JPythonMethods {

  private static final String ID = JPythonMethods.class.getName();

  private static DefaultFamily form = new DefaultFamily(ID);

  private static JFrame displayFrame = null;

  private static JFrame widgetFrame = null;

  /** reads in data from the given location (filename or URL) */
  public static DataImpl load(String location) throws VisADException {
    return form.open(location);
  }

  private static DisplayImpl display = null;
  private static ScalarMap[] maps = null;
  private static MappingDialog dialog = null;
  private static Vector data_references = null;

  /** displays the given data onscreen */
  public static void plot(DataImpl data)
    throws VisADException, RemoteException {
    plot(data, 1.0, 1.0, 1.0);
  }

  /** displays the given data onscreen, with the given default color
      (red, green and blue values between 0.0 and 1.0), which the user
      may override by color mappings */
  public static void plot(DataImpl data, double red, double green, double blue)
    throws VisADException, RemoteException {
    if (data == null) throw new VisADException("Data cannot be null");
    if (display == null) {
      displayFrame = new JFrame("VisAD Display Plot");
      widgetFrame = new JFrame("VisAD Display Widgets");
      WindowListener l = new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          synchronized (displayFrame) {
            displayFrame.setVisible(false);
            widgetFrame.setVisible(false);
          }
        }
      };
      // set up scalar maps
      maps = data.getType().guessMaps(true);
  
      // allow user to alter default mappings
      dialog = new MappingDialog(null, data, maps, true, true);
      dialog.display();
      if (dialog.Confirm) maps = dialog.ScalarMaps;
      boolean d3d = false;
      for (int i=0; i<maps.length; i++) {
        if (maps[i].getDisplayScalar().equals(Display.ZAxis) ||
            maps[i].getDisplayScalar().equals(Display.Latitude) ||
            maps[i].getDisplayScalar().equals(Display.Alpha)) d3d = true;
      }
      display = d3d ? new DisplayImplJ3D(ID) :
                      new DisplayImplJ3D(ID, new TwoDDisplayRendererJ3D());
      for (int i=0; i<maps.length; i++) display.addMap(maps[i]);
      GraphicsModeControl gmc = display.getGraphicsModeControl();
      gmc.setScaleEnable(true);
  
      // set up widget panel
      widgetFrame.addWindowListener(l);
      widgetFrame.getContentPane().add(display.getWidgetPanel());
      widgetFrame.pack();
      widgetFrame.setVisible(true);
  
      // set up display frame
      displayFrame.addWindowListener(l);
      displayFrame.getContentPane().add(display.getComponent());
      displayFrame.pack();
      displayFrame.setSize(512, 512);
      displayFrame.setVisible(true);
      data_references = new Vector();
    }

    ConstantMap[] cmaps = {new ConstantMap(red, Display.Red),
                           new ConstantMap(green, Display.Green),
                           new ConstantMap(blue, Display.Blue)};

    DataReferenceImpl ref = new DataReferenceImpl(ID);
    data_references.addElement(ref);
    ref.setData(data);
    MathType type = data.getType();
    try {
      ImageRendererJ3D.verifyImageRendererUsable(type, maps);
      display.addReferences(new ImageRendererJ3D(), ref, cmaps);
    }
    catch (VisADException exc) {
      display.addReference(ref, cmaps);
    }
  }

  // this is just a temporary and dumb hack
  /** clear the plot */
  public static void clearplot() throws VisADException, RemoteException {
    if (display != null) {
      displayFrame.setVisible(false);
      displayFrame.dispose();
      displayFrame = null;
      widgetFrame.setVisible(false);
      widgetFrame.dispose();
      widgetFrame = null;
      display.destroy();
      display = null;
    }
  }

  /** binary and unary methods from Data.java */
  public static Data abs(Data data) throws VisADException, RemoteException {
    return data.abs();
  }

  public static Data acos(Data data) throws VisADException, RemoteException {
    return data.acos();
  }

  public static Data acosDegrees(Data data) throws VisADException, RemoteException {
    return data.acosDegrees();
  }

  public static Data asin(Data data) throws VisADException, RemoteException {
    return data.asin();
  }

  public static Data asinDegrees(Data data) throws VisADException, RemoteException {
    return data.asinDegrees();
  }

  public static Data ceil(Data data) throws VisADException, RemoteException {
    return data.ceil();
  }

  public static Data cos(Data data) throws VisADException, RemoteException {
    return data.cos();
  }

  public static Data cosDegrees(Data data) throws VisADException, RemoteException {
    return data.cosDegrees();
  }

  public static Data exp(Data data) throws VisADException, RemoteException {
    return data.exp();
  }

  public static Data floor(Data data) throws VisADException, RemoteException {
    return data.floor();
  }

  public static Data log(Data data) throws VisADException, RemoteException {
    return data.log();
  }

  public static Data rint(Data data) throws VisADException, RemoteException {
    return data.rint();
  }

  public static Data round(Data data) throws VisADException, RemoteException {
    return data.round();
  }

  public static Data sin(Data data) throws VisADException, RemoteException {
    return data.sin();
  }

  public static Data sinDegrees(Data data) throws VisADException, RemoteException {
    return data.sinDegrees();
  }

  public static Data sqrt(Data data) throws VisADException, RemoteException {
    return data.sqrt();
  }

  public static Data tan(Data data) throws VisADException, RemoteException {
    return data.tan();
  }

  public static Data tanDegrees(Data data) throws VisADException, RemoteException {
    return data.tanDegrees();
  }

  public static Data max(Data data1, Data data2)
         throws VisADException, RemoteException {
    return data1.max(data2);
  }

  public static Data min(Data data1, Data data2)
         throws VisADException, RemoteException {
    return data1.min(data2);
  }

  public static Data atan2(Data data1, Data data2)
         throws VisADException, RemoteException {
    return data1.atan2(data2);
  }

  public static Data atan2Degrees(Data data1, Data data2)
         throws VisADException, RemoteException {
    return data1.atan2Degrees(data2);
  }

  public static Data max(Data data1, double data2) 
         throws VisADException, RemoteException {
    return data1.max(new Real(data2));
  }

  public static Data min(Data data1, double data2) 
         throws VisADException, RemoteException {
    return data1.min(new Real(data2));
  }

  public static Data atan2(Data data1, double data2) 
         throws VisADException, RemoteException {
    return data1.atan2(new Real(data2));
  }

  public static Data atan2Degrees(Data data1, double data2) 
         throws VisADException, RemoteException {
    return data1.atan2Degrees(new Real(data2));
  }

  public static Data max(double data1, Data data2) 
         throws VisADException, RemoteException {
    return new Real(data1).max(data2);
  }

  public static Data min(double data1, Data data2) 
         throws VisADException, RemoteException {
    return new Real(data1).min(data2);
  }

  public static Data atan2(double data1, Data data2) 
         throws VisADException, RemoteException {
    return new Real(data1).atan2(data2);
  }

  public static Data atan2Degrees(double data1, Data data2)
         throws VisADException, RemoteException {
    return new Real(data1).atan2Degrees(data2);
  }
  /* end of binary and unary methods from Data.java */

  /** 1-D and 2-D forward Fourier transform, uses fft when possible */
  public static FlatField fft(Field field)
         throws VisADException, RemoteException {
    return FFT.fourierTransform(field, true);
  }

  /** 1-D and 2-D backward Fourier transform, uses fft when possible */
  public static FlatField ifft(Field field)
         throws VisADException, RemoteException {
    return FFT.fourierTransform(field, false);
  }

  /** multiply matrices using Jama */
  public static JamaMatrix matrixMultiply(FlatField data1, FlatField data2)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix1 = JamaMatrix.convertToMatrix(data1);
    JamaMatrix matrix2 = JamaMatrix.convertToMatrix(data2);
    return matrix1.times(matrix2);
  }

  /** solve a linear system using Jama */
  public static JamaMatrix solve(FlatField data1, FlatField data2)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix1 = JamaMatrix.convertToMatrix(data1);
    JamaMatrix matrix2 = JamaMatrix.convertToMatrix(data2);
    return matrix1.solve(matrix2);
  }

  /** invert a matrix using Jama */
  public static JamaMatrix inverse(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.inverse();
  }

  /** get determinant of a matrix using Jama */
  public static double det(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.det();
  }

  /** return Cholesky Decomposition using Jama */
  public static JamaCholeskyDecomposition chol(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.chol();
  }

  /** return Eigenvalue Decomposition using Jama */
  public static JamaEigenvalueDecomposition eig(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.eig();
  }

  /** return LU Decomposition using Jama */
  public static JamaLUDecomposition lu(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.lu();
  }

  /** return QR Decomposition using Jama */
  public static JamaQRDecomposition qr(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.qr();
  }

  /** return Singular Value Decomposition using Jama */
  public static JamaSingularValueDecomposition svd(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.svd();
  }

  /** return histogram of field, with dimension and bin sampling
      defined by set */
  public static FlatField hist(Field field, Set set)
         throws VisADException, RemoteException {
    return Histogram.makeHistogram(field, set);
  }

  /** return histogram of range values of field selected
      by indices in ranges, and with 64 equally spaced bins
      in each dimension */
  public static FlatField hist(Field field, int[] ranges)
         throws VisADException, RemoteException {
    if (ranges == null || ranges.length == 0) {
      throw new VisADException("bad ranges");
    }
    int dim = ranges.length;
    int[] sizes = new int[dim];
    for (int i=0; i<dim; i++) sizes[i] = 64;
    return hist(field, ranges, sizes);
  }

  /** return histogram of range values of field selected
      by indices in ranges, and with number of equally spaced
      bins defined by sizes */
  public static FlatField hist(Field field, int[] ranges, int[] sizes)
         throws VisADException, RemoteException {
    if (ranges == null || ranges.length == 0) {
      throw new VisADException("bad ranges");
    }
    if (sizes == null || sizes.length != ranges.length) {
      throw new VisADException("bad sizes");
    }
    if (field == null) {
      throw new VisADException("bad field");
    }
    FunctionType ftype = (FunctionType) field.getType();
    RealType[] frealComponents = ftype.getRealComponents();
    int n = frealComponents.length;

    int dim = ranges.length;
    RealType[] srealComponents = new RealType[dim];
    for (int i=0; i<dim; i++) {
      if (0 <= ranges[i] && ranges[i] < n) {
        srealComponents[i] = frealComponents[ranges[i]];
      }
      else {
        throw new VisADException("range index out of range " + ranges[i]);
      }
    }
    RealTupleType rtt = new RealTupleType(srealComponents);
    double[][] data_ranges = field.computeRanges(srealComponents);
    Set set = null;
    if (dim == 1) {
      set = new Linear1DSet(rtt, data_ranges[0][0], data_ranges[0][1], sizes[0]);
    }
    else if (dim == 2) {
      set = new Linear2DSet(rtt, data_ranges[0][0], data_ranges[0][1], sizes[0],
                                 data_ranges[1][0], data_ranges[1][1], sizes[1]);
    }
    else if (dim == 3) {
      set = new Linear3DSet(rtt, data_ranges[0][0], data_ranges[0][1], sizes[0],
                                 data_ranges[1][0], data_ranges[1][1], sizes[1],
                                 data_ranges[2][0], data_ranges[2][1], sizes[2]);
    }
    else {
      double[] firsts = new double[dim];
      double[] lasts = new double[dim];
      for (int i=0; i<dim; i++) {
        firsts[i] = data_ranges[i][0];
        lasts[i] = data_ranges[i][1];
      }
      set = new LinearNDSet(rtt, firsts, lasts, sizes);
    }
    return Histogram.makeHistogram(field, set);
  }

  /** construct a FlatField with given values array */
  public static FlatField field(float[] values)
         throws VisADException, RemoteException {
    return field("value", values);
  }

  /** construct a FlatField with given range RealType name
      and values array */
  public static FlatField field(String name, float[] values)
         throws VisADException, RemoteException {
    if (values == null || values.length == 0) {
      throw new VisADException("bad values");
    }
    RealType domain = RealType.getRealType("domain");
    return field(new Integer1DSet(domain, values.length), name, values);
  }

  /** construct a FlatField with given domain set, range RealType
      name and values array */
  public static FlatField field(Set set, String name, float[] values)
         throws VisADException, RemoteException {
    if (values == null) {
      throw new VisADException("bad values");
    }
    if (set == null || set.getLength() < values.length) {
      throw new VisADException("bad set " + set);
    }
    if (name == null) {
      throw new VisADException("bad name");
    }
    MathType domain = ((SetType) set.getType()).getDomain();
    if (((RealTupleType) domain).getDimension() == 1) {
      domain = ((RealTupleType) domain).getComponent(0);
    }
    RealType range = RealType.getRealType(name);
    FunctionType ftype = new FunctionType(domain, range);
    FlatField field = new FlatField(ftype, set);
    int len = set.getLength();
    boolean copy = true;
    if (values.length < len) {
      float[] new_values = new float[len];
      System.arraycopy(values, 0, new_values, 0, len);
      for (int i=values.length; i<len; i++) new_values[i] = Float.NaN;
      values = new_values;
      copy = false;
    }
    float[][] field_values = {values};
    field.setSamples(field_values, copy);
    return field;
  }

  /** construct a FlatField with given 2-D values array */
  public static FlatField field(float[][] values)
         throws VisADException, RemoteException {
    return field("value", values);
  }

  /** construct a FlatField with given range RealType name
      and 2-D values array */
  public static FlatField field(String name, float[][] values)
         throws VisADException, RemoteException {
    int[] temps = getValuesLengths(values);
    int values_len = temps[0];
    int min = temps[1];
    int max = temps[2];
    RealType line = RealType.getRealType("ImageLine");
    RealType element = RealType.getRealType("ImageElement");
    RealTupleType domain = new RealTupleType(element, line);
    return field(new Integer2DSet(domain, max, values_len), name, values);
  }

  /** construct a FlatField with given domain set, range RealType
      name and 2-D values array */
  public static FlatField field(Set set, String name, float[][] values)
         throws VisADException, RemoteException {
    int[] temps = getValuesLengths(values);
    int values_len = temps[0];
    int min = temps[1];
    int max = temps[2];

    if (set == null || !(set instanceof GriddedSet) ||
        set.getManifoldDimension() != 2) {
      throw new VisADException("bad set " + set);
    }
    int len0 = ((GriddedSet) set).getLength(0);
    int len1 = ((GriddedSet) set).getLength(1);
    if (len0 < max || len1 < values_len) {
      throw new VisADException("bad set length " + len0 + " " + len1);
    }
    if (name == null) {
      throw new VisADException("bad name");
    }

    MathType domain = ((SetType) set.getType()).getDomain();
    if (((RealTupleType) domain).getDimension() == 1) {
      domain = ((RealTupleType) domain).getComponent(0);
    }
    RealType range = RealType.getRealType(name);
    FunctionType ftype = new FunctionType(domain, range);
    FlatField field = new FlatField(ftype, set);
    int len = len0 * len1; // == set.getLength()

    float[] new_values = new float[len];
    for (int j=0; j<values_len; j++) {
      int m = j * len0;
      int n = values[j].length;
      if (n > 0) System.arraycopy(values[j], 0, new_values, m, n);
      for (int i=(m+n); i<(m+len0); i++) new_values[i] = Float.NaN;
    }
    for (int j=values_len; j<len1; j++) {
      int m = j * len0;
      for (int i=m; i<(m+len0); i++) new_values[i] = Float.NaN;
    }
    float[][] field_values = {new_values};
    field.setSamples(field_values, false);
    return field;
  }

  private static int[] getValuesLengths(float[][] values)
          throws VisADException {
    if (values == null) {
      throw new VisADException("bad values");
    }
    int values_len = values.length;
    int min = Integer.MAX_VALUE;
    int max = 0;
    for (int j=0; j<values_len; j++) {
      if (values[j] == null) {
        throw new VisADException("bad values");
      }
      int n = values[j].length;
      if (n > max) max = n;
      if (n < min) min = n;
    }
    if (max < min) {
      min = 0;
    }
    return new int[] {values_len, min, max};
  }

  /** NOT DONE */
  public static Set linear(MathType type, double first, double last, int length)
         throws VisADException, RemoteException {
    return null;
  }

}

