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
    throws VisADException, RemoteException
  {
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

    DataReferenceImpl ref = new DataReferenceImpl(ID);
    data_references.addElement(ref);
    ref.setData(data);
    MathType type = data.getType();
    try {
      ImageRendererJ3D.verifyImageRendererUsable(type, maps);
      display.addReferences(new ImageRendererJ3D(), ref);
    }
    catch (VisADException exc) {
      display.addReference(ref);
    }
  }

  // this is just a temporary and dumb hack
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

}

