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

import java.awt.Dimension;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import javax.swing.*;

import visad.*;
import visad.math.*;
import visad.matrix.*;
import visad.data.*;
import visad.ss.*;

/**
 * A collection of methods for working with VisAD, callable from the
 * JPython editor.
 */
public abstract class JPythonMethods {

  private static final String DEFAULT_NAME = "JPython";

  private static final String ID = JPythonMethods.class.getName();

  private static DefaultFamily form = new DefaultFamily(ID);

  /**
   * Reads in data from the given location (filename or URL).
   */
  public static DataImpl load(String location) throws VisADException {
    return form.open(location);
  }


  private static Hashtable frames = new Hashtable();

  /**
   * Displays the given data onscreen.
   *
   * @param   data            VisAD data object to plot
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(DataImpl data)
    throws VisADException, RemoteException
  {
    plot(null, data, false, 1.0, 1.0, 1.0);
  }

  /**
   * Displays the given data onscreen.
   *
   * @param   data            VisAD data object to plot
   * @param   maps            ScalarMaps for the display
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(DataImpl data, ScalarMap[] maps)
    throws VisADException, RemoteException {
    plot(null, data, false, 1.0, 1.0, 1.0, maps);
  }

  /**
   * Displays the given data onscreen,
   * displaying the edit mappings dialog if specified.
   *
   * @param   data            VisAD data object to plot
   * @param   editMaps        whether to initially display edit mappings dialog
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(DataImpl data, boolean editMaps)
    throws VisADException, RemoteException
  {
    plot(null, data, editMaps, 1.0, 1.0, 1.0);
  }
  
  /**
   * Displays the given data onscreen.
   *
   * @param   name            name of display in which to plot data
   * @param   data            VisAD data object to plot
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(String name, DataImpl data)
    throws VisADException, RemoteException
  {
    plot(name, data, false, 1.0, 1.0, 1.0);
  }

  /**
   * Displays the given data onscreen.
   *
   * @param   name            name of display in which to plot data
   * @param   data            VisAD data object to plot
   * @param   maps            ScalarMaps for display
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(String name, DataImpl data, ScalarMap[] maps)
    throws VisADException, RemoteException
  {
    plot(name, data, false, 1.0, 1.0, 1.0, maps);
  }

  /**
   * Displays the given data onscreen in a display with the given name,
   * displaying the edit mappings dialog if specified.
   *
   * @param   name            name of display in which to plot data
   * @param   data            VisAD data object to plot
   * @param   editMaps        whether to initially display edit mappings dialog
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(String name, DataImpl data, boolean editMaps)
    throws VisADException, RemoteException
  {
    plot(name, data, editMaps, 1.0, 1.0, 1.0);
  }

  /**
   * Displays the given data onscreen, using given color default.
   *
   * @param   data            VisAD data object to plot
   * @param   red             red component of default color to use if there
   *                          are no color mappings from data's RealTypes;
   *                          color component values between 0.0 and 1.0
   * @param   green           green component of default color
   * @param   blue            blue component of default color
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(DataImpl data, double red, double green, double blue)
    throws VisADException, RemoteException
  {
    plot(null, data, false, red, green, blue);
  }

  /**
   * Displays the given data onscreen in a display with the given name, using
   * the given color default and displaying the edit mappings dialog if
   * specified.
   *
   * @param   name            name of display in which to plot data
   * @param   data            VisAD data object to plot
   * @param   editMaps        whether to initially display edit mappings dialog
   * @param   red             red component of default color to use if there
   *                          are no color mappings from data's RealTypes;
   *                          color component values between 0.0 and 1.0
   * @param   green           green component of default color
   * @param   blue            blue component of default color
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(String namxe, DataImpl data,
    boolean editMaps, double red, double green, double blue)
    throws VisADException, RemoteException {
       plot(namxe, data, editMaps,red, green, blue, null);
  }

  public static void plot(String namxe, DataImpl data,
    boolean editMaps, double red, double green, double blue, ScalarMap[] maps)
    throws VisADException, RemoteException
  {

    if (data == null) throw new VisADException("Data cannot be null");
    if (namxe == null) namxe = DEFAULT_NAME;
    final String name = namxe;
    BasicSSCell display;
    synchronized (frames) {
      display = BasicSSCell.getSSCellByName(name);
      JFrame frame;
      if (display == null) {
        display = new FancySSCell(name);
        display.setDimension(BasicSSCell.JAVA3D_3D);
        //display.setDimension(BasicSSCell.JAVA2D_2D);
        display.setPreferredSize(new Dimension(256, 256));
        frame = new JFrame("VisAD Display Plot (" + name + ")");
        frames.put(name, frame);
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        frame.setContentPane(pane);
        pane.add(display);

        // add buttons to cell layout
        JButton mapping = new JButton("Mappings");
        JButton controls = new JButton("Controls");
        JButton clear = new JButton("Clear");
        JButton close = new JButton("Close");
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(mapping);
        buttons.add(controls);
        buttons.add(clear);
        buttons.add(close);
        pane.add(buttons);
        final FancySSCell fdisp = (FancySSCell) display;
        fdisp.setAutoShowControls(false);
        if (maps != null) {
          display.setMaps(maps);
          fdisp.setAutoDetect(false);
        } else {
          fdisp.setAutoDetect(!editMaps);
        }

        mapping.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            fdisp.addMapDialog();
          }
        });
        controls.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            fdisp.showWidgetFrame();
          }
        });
        close.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            try { fdisp.smartClear(); clearplot(name);}
            catch (Exception ec) {;}
          }
        });
        clear.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            try { fdisp.smartClear(); }
            catch (VisADException exc) { }
            catch (RemoteException exc) { }
          }
        });

        frame.pack();
      }
      else {
        frame = (JFrame) frames.get(name);
      }
      frame.setVisible(true);
      frame.toFront();
    }

    ConstantMap[] cmaps = {
      new ConstantMap(red, Display.Red),
      new ConstantMap(green, Display.Green),
      new ConstantMap(blue, Display.Blue)
    };
    display.addData(data, cmaps);
    if (editMaps) ((FancySSCell)display).addMapDialog();
  }

  /**
   * clear the onscreen data display
   *
   * @throws  VisADException  part of data and display APIs, shouldn't occur
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void clearplot() throws VisADException, RemoteException {
    clearplot(null);
  }

  /**
   * clear the onscreen data display with the given name
   *
   * @param   name            name of the display to clear
   *
   * @throws  VisADException  part of data and display APIs, shouldn't occur
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void clearplot(String name)
    throws VisADException, RemoteException
  {
    if (name == null) name = DEFAULT_NAME;
    BasicSSCell display = BasicSSCell.getSSCellByName(name);
    if (display != null) {
      JFrame frame = (JFrame) frames.get(name);
      display.clearCell();
      display.clearMaps();
      frame.setVisible(false);
      frame.dispose();
      frame = null;
      display.destroyCell();
      display = null;
    }
  }

  /**
   * return pointwise absolute value of data
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data abs(Data data) throws VisADException, RemoteException {
    return data.abs();
  }

  /**
   * return pointwise arccos value of data, in radians
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data acos(Data data) throws VisADException, RemoteException {
    return data.acos();
  }

  /**
   * return pointwise arccos value of data, in degrees
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data acosDegrees(Data data) throws VisADException, RemoteException {
    return data.acosDegrees();
  }

  /**
   * return pointwise arcsin value of data, in radians
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data asin(Data data) throws VisADException, RemoteException {
    return data.asin();
  }

  /**
   * return pointwise arcsin value of data, in degrees
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data asinDegrees(Data data) throws VisADException, RemoteException {
    return data.asinDegrees();
  }

  /**
   * return pointwise arctan value of data, in radians
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data atan(Data data) throws VisADException, RemoteException {
    return data.atan();
  }

  /**
   * return pointwise arctan value of data, in degrees
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data atanDegrees(Data data) throws VisADException, RemoteException {
    return data.atanDegrees();
  }

  /**
   * return pointwise ceil value of data (smallest integer not less than)
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data ceil(Data data) throws VisADException, RemoteException {
    return data.ceil();
  }

  /**
   * return pointwise cos value of data, assuming input values are in radians
   * unless they have units convertable with radians, in which case those
   * units are converted to radians
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data cos(Data data) throws VisADException, RemoteException {
    return data.cos();
  }

  /**
   * return pointwise cos value of data, assuming input values are in degrees
   * unless they have units convertable with degrees, in which case those
   * units are converted to degrees
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data cosDegrees(Data data) throws VisADException, RemoteException {
    return data.cosDegrees();
  }

  /**
   * return pointwise exp value of data
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data exp(Data data) throws VisADException, RemoteException {
    return data.exp();
  }

  /**
   * return pointwise floor value of data (largest integer not greater than)
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data floor(Data data) throws VisADException, RemoteException {
    return data.floor();
  }

  /**
   * return pointwise log value of data
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data log(Data data) throws VisADException, RemoteException {
    return data.log();
  }

  /**
   * return pointwise rint value of data (closest integer)
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data rint(Data data) throws VisADException, RemoteException {
    return data.rint();
  }

  /**
   * return pointwise round value of data (closest integer)
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data round(Data data) throws VisADException, RemoteException {
    return data.round();
  }

  /**
   * return pointwise sin value of data, assuming input values are in radians
   * unless they have units convertable with radians, in which case those
   * units are converted to radians
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data sin(Data data) throws VisADException, RemoteException {
    return data.sin();
  }

  /**
   * return pointwise sin value of data, assuming input values are in degrees
   * unless they have units convertable with degrees, in which case those
   * units are converted to degrees
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data sinDegrees(Data data) throws VisADException, RemoteException {
    return data.sinDegrees();
  }

  /**
   * return pointwise square root value of data
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data sqrt(Data data) throws VisADException, RemoteException {
    return data.sqrt();
  }

  /**
   * return pointwise tan value of data, assuming input values are in radians
   * unless they have units convertable with radians, in which case those
   * units are converted to radians
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data tan(Data data) throws VisADException, RemoteException {
    return data.tan();
  }

  /**
   * return pointwise tan value of data, assuming input values are in degrees
   * unless they have units convertable with degrees, in which case those
   * units are converted to degrees
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data tanDegrees(Data data) throws VisADException, RemoteException {
    return data.tanDegrees();
  }

  /**
   * return pointwise maximum value of data1 and data2
   *
   * @param   data1           VisAD data object
   * @param   data2           VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data max(Data data1, Data data2)
         throws VisADException, RemoteException {
    return data1.max(data2);
  }

  /**
   * return pointwise minimum value of data1 and data2 
   *
   * @param   data1           VisAD data object
   * @param   data2           VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data min(Data data1, Data data2)
         throws VisADException, RemoteException {
    return data1.min(data2);
  }

  /**
   * return pointwise tan value of data1 / data2 over full (-pi, pi) range, 
   * assuming input values are in radians unless they have units convertable 
   * with radians, in which case those units are converted to radians
   * 
   * @param   data1           VisAD data object
   * @param   data2           VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data atan2(Data data1, Data data2)
         throws VisADException, RemoteException {
    return data1.atan2(data2);
  }

  /**
   * return pointwise tan value of data1 / data2 over full (-pi, pi) range,
   * assuming input values are in degrees unless they have units convertable
   * with degrees, in which case those units are converted to degrees
   *
   * @param   data1           VisAD data object
   * @param   data2           VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data atan2Degrees(Data data1, Data data2)
         throws VisADException, RemoteException {
    return data1.atan2Degrees(data2);
  }

  /**
   * return pointwise maximum value of data1 and data2 
   *
   * @param   data1           VisAD data object
   * @param   data2           double value
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data max(Data data1, double data2) 
         throws VisADException, RemoteException {
    return data1.max(new Real(data2));
  }

  /**
   * return pointwise minimum value of data1 and data2 
   *
   * @param   data1           VisAD data object
   * @param   data2           double value
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data min(Data data1, double data2) 
         throws VisADException, RemoteException {
    return data1.min(new Real(data2));
  }

  /**
   * return pointwise tan value of data1 / data2 over full (-pi, pi) range,
   * assuming input values are in radians unless they have units convertable
   * with radians, in which case those units are converted to radians
   *
   * @param   data1           VisAD data object
   * @param   data2           double value
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data atan2(Data data1, double data2) 
         throws VisADException, RemoteException {
    return data1.atan2(new Real(data2));
  }

  /**
   * return pointwise tan value of data1 / data2 over full (-pi, pi) range,
   * assuming input values are in degrees unless they have units convertable
   * with degrees, in which case those units are converted to degrees
   *
   * @param   data1           VisAD data object
   * @param   data2           double value
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data atan2Degrees(Data data1, double data2) 
         throws VisADException, RemoteException {
    return data1.atan2Degrees(new Real(data2));
  }

  /**
   * return pointwise maximum value of data1 and data2
   *
   * @param   data1           double value
   * @param   data2           VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data max(double data1, Data data2) 
         throws VisADException, RemoteException {
    return new Real(data1).max(data2);
  }

  /**
   * return pointwise minimum value of data1 and data2
   *
   * @param   data1           double value
   * @param   data2           VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data min(double data1, Data data2) 
         throws VisADException, RemoteException {
    return new Real(data1).min(data2);
  }

  /**
   * return pointwise tan value of data1 / data2 over full (-pi, pi) range,
   * assuming input values are in radians unless they have units convertable
   * with radians, in which case those units are converted to radians
   *
   * @param   data1           double value
   * @param   data2           VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data atan2(double data1, Data data2) 
         throws VisADException, RemoteException {
    return new Real(data1).atan2(data2);
  }

  /**
   * return pointwise tan value of data1 / data2 over full (-pi, pi) range,
   * assuming input values are in degrees unless they have units convertable
   * with degrees, in which case those units are converted to degrees
   *
   * @param   data1           double value
   * @param   data2           VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data atan2Degrees(double data1, Data data2)
         throws VisADException, RemoteException {
    return new Real(data1).atan2Degrees(data2);
  }

  /**
   * return forward Fourier transform of field, which should have
   * either a 1-D or 2-D gridded domain; uses fft when domain size
   * is a power of two; returns real and imaginary parts
   *
   * @param   field           VisAD Field data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote field
   */
  public static FlatField fft(Field field)
         throws VisADException, RemoteException {
    return FFT.fourierTransform(field, true);
  }

  /**
   * return backward Fourier transform of field, which should have 
   * either a 1-D or 2-D gridded domain; uses fft when domain size 
   * is a power of two; returns real and imaginary parts
   * 
   * @param   field           VisAD Field data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote field
   */
  public static FlatField ifft(Field field)
         throws VisADException, RemoteException {
    return FFT.fourierTransform(field, false);
  }

  /**
   * return matrix multiply of data1 * data2, which should have
   * either 1-D or 2-D gridded domains
   * 
   * @param   data1           VisAD FlatField data object
   * @param   data2           VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static JamaMatrix matrixMultiply(FlatField data1, FlatField data2)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix1 = JamaMatrix.convertToMatrix(data1);
    JamaMatrix matrix2 = JamaMatrix.convertToMatrix(data2);
    return matrix1.times(matrix2);
  }

  /**
   * return matrix soluton X of data1 * X = data2; data12 and data2 should
   * have either 1-D or 2-D gridded domains; return solution if data1 is
   * is square, least squares solution otherwise
   *
   * @param   data1           VisAD FlatField data object
   * @param   data2           VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static JamaMatrix solve(FlatField data1, FlatField data2)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix1 = JamaMatrix.convertToMatrix(data1);
    JamaMatrix matrix2 = JamaMatrix.convertToMatrix(data2);
    return matrix1.solve(matrix2);
  }

  /**
   * return matrix inverse of data, which should have either a
   * 1-D or 2-D gridded domain
   * 
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static JamaMatrix inverse(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.inverse();
  }

  /**
   * return matrix transpose of data, which should have either a
   * 1-D or 2-D gridded domain
   *
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static JamaMatrix transpose(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.transpose();
  }

  /**
   * return matrix determinant of data, which should have either a
   * 1-D or 2-D gridded domain
   *
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static double det(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.det();
  }

  /**
   * return matrix one norm of data (maximum column sum), which
   * should have either a 1-D or 2-D gridded domain
   *
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static double norm1(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.norm1();
  }

  /**
   * return matrix two norm of data (maximum singular value), which 
   * should have either a 1-D or 2-D gridded domain
   * 
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static double norm2(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.norm2();
  }

  /**
   * return matrix infinity norm of data (maximum row sum), which
   * should have either a 1-D or 2-D gridded domain
   * 
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static double normInf(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.normInf();
  }

  /**
   * return matrix Frobenius norm of data (sqrt of sum of squares of all
   * elements), which should have either a 1-D or 2-D gridded domain
   * 
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static double normF(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.normF();
  }

  /**
   * return matrix effective numerical rank (from SVD) of data, which
   * should have either a 1-D or 2-D gridded domain
   * 
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static double rank(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.rank();
  }

  /**
   * return matrix condition of data (ratio of largest to smallest singular
   * value), which should have either a 1-D or 2-D gridded domain
   *
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static double cond(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.cond();
  }

  /**
   * return matrix trace of data (sum of the diagonal elements),
   * which should have either a 1-D or 2-D gridded domain
   * 
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static double trace(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.trace();
  }

  /**
   * return matrix Cholesky Decomposition of data, as a 1-Tuple
   * (lower_triangular_factor);
   * data should have either a 1-D or 2-D gridded domain
   * 
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static JamaCholeskyDecomposition chol(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.chol();
  }

  /**
   * return matrix Eigenvalue Decomposition of data, as a 3-Tuple
   * (eigenvector_matrix, real_eigenvalue_components,
   *  imaginary_eigenvalue_components);
   * data should have either a 1-D or 2-D gridded domain
   * 
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static JamaEigenvalueDecomposition eig(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.eig();
  }

  /**
   * return matrix LU Decomposition of data, as a 3-Tuple
   * (lower_triangular_factor, upper_triangular_factor,
   *  pivot_permutation_vector);
   * data should have either a 1-D or 2-D gridded domain
   * 
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static JamaLUDecomposition lu(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.lu();
  }

  /**
   * return matrix QR Decomposition of data, as a 2-Tuple
   * (orthogonal_factor, upper_triangular_factor);
   * data should have either a 1-D or 2-D gridded domain
   * 
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static JamaQRDecomposition qr(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.qr();
  }

  /**
   * return matrix Singular Value Decomposition of data, as a 3-Tuple
   * (left_singular_vectors, right_singular_vectors, singular_value_vector);
   * data should have either a 1-D or 2-D gridded domain
   * 
   * @param   data            VisAD FlatField data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IllegalAccessException Jama not installed
   * @throws  InstantiationException Jama not installed
   * @throws  InvocationTargetException Jama not installed
   */
  public static JamaSingularValueDecomposition svd(FlatField data)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    JamaMatrix matrix = JamaMatrix.convertToMatrix(data);
    return matrix.svd();
  }

  /**
   * return histogram of range values of field selected by set, with
   * dimension and bin sampling defined by set
   * 
   * @param   field           VisAD Field data object whose range values
   *                          are analyzed in histogram
   * @param   set             VisAD Set data object that defines dimension
   *                          and bin sampling for histogram
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote field
   */
  public static FlatField hist(Field field, Set set)
         throws VisADException, RemoteException {
    return Histogram.makeHistogram(field, set);
  }

  /**
   * return histogram of range values of field selected by ranges array,
   * with dimension = ranges.length, and 64 equally spaced bins in each
   * dimension
   * 
   * @param   field           VisAD Field data object whose range values
   *                          are analyzed in histogram
   * @param   ranges          int[] array whose elements are indices of into
   *                          the range Tuple of field, selecting range
   *                          components as dimesnions of the histogram
   * 
   * @throws  VisADException  invalid data 
   * @throws  RemoteException unable to access remote field
   */
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

  /**
   * return histogram of range values of field selected by ranges array,
   * with dimension = ranges.length, and with number of equally spaced bins
   * in each dimension determined by sizes array
   * 
   * @param   field           VisAD Field data object whose range values
   *                          are analyzed in histogram
   * @param   ranges          int[] array whose elements are indices of into
   *                          the range Tuple of field, selecting range
   *                          components as dimesnions of the histogram
   * @param   sizes           int[] array whose elements are numbers of
   *                          equally spaced bins for each dimension
   * 
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote field
   */
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

  /**
   * return a VisAD FlatField with default 1-D domain and with range
   * values given by values array
   *
   * @param   values          float[] array defining range values of field
   *
   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static FlatField field(float[] values)
         throws VisADException, RemoteException {
    return field("value", values);
  }

  /**
   * return a VisAD FlatField with default 1-D domain, with range values
   * given by values array, and with given range RealType name
   *
   * @param   name            String defining range RealType name
   * @param   values          float[] array defining range values of field
   *
   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static FlatField field(String name, float[] values)
         throws VisADException, RemoteException {
    if (values == null || values.length == 0) {
      throw new VisADException("bad values");
    }
    RealType domain = RealType.getRealType("domain");
    return field(new Integer1DSet(domain, values.length), name, values);
  }

  /**
   * return a VisAD FlatField with given 1-D domain set, with range
   * values given by values array, and with given range RealType name
   *
   * @param   set             VisAD Set defining 1-D domain
   * @param   name            String defining range RealType name 
   * @param   values          float[] array defining range values of field
   * 
   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
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

  /**
   * return a VisAD FlatField with default 2-D domain and with range
   * values given by values array
   *
   * @param   values          float[][] array defining range values of field
   *
   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static FlatField field(float[][] values)
         throws VisADException, RemoteException {
    return field("value", values);
  }

  /**
   * return a VisAD FlatField with default 2-D domain, with range values
   * given by values array, and with given range RealType name
   *
   * @param   name            String defining range RealType name
   * @param   values          float[][] array defining range values of field
   *
   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
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

  /**
   * return a VisAD FlatField with given 2-D domain set, with range 
   * values given by values array, and with given range RealType name
   * 
   * @param   set             VisAD Set defining 2-D domain 
   * @param   name            String defining range RealType name 
   * @param   values          float[][] array defining range values of field
   * 
   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
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


  /**
   * get the number of domain components of the Data object
   * 
   * @param   Data            VisAD Data object
   * @return the number of domain components

   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static int domainDimension (Data data) 
                  throws VisADException, RemoteException {
    return (int) ((RealTupleType)
        ((FunctionType)data.getType()).getDomain()).getDimension();
  }
  /**
   * get the number of range components of the Data object
   * 
   * @param   Data            VisAD Data object
   * @return the number of range components

   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static int rangeDimension (Data data) 
                  throws VisADException, RemoteException {
    return (int) ((RealTupleType)
        ((FunctionType)data.getType()).getRange()).getDimension();
  }

  /** get the domain Type for the field
  *
  * @param data is the field to get the domain Type for
  *
  * @return the domain
  */
  public static RealTupleType domainType(Data data) 
                  throws VisADException, RemoteException {
    return (RealTupleType) ((FunctionType)data.getType()).getDomain();
  
  }

  /** get the range Type for the field
  *
  * @param data is the field to get the range Type for
  *
  * @return the range
  */
  public static RealTupleType rangeType(Data data) 
                  throws VisADException, RemoteException {
    return (RealTupleType) ((FunctionType)data.getType()).getRange();
  
  }

  /**
   * get the name of the given component of the domain RealType.
   * 
   * @param   Data            VisAD Data object
   * @param   comp            the domain component index (0...)
   * 
   * @return the name of the RealType
   *
   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static String domainType (Data data,int comp) 
                   throws VisADException, RemoteException {
    return (String) ((RealTupleType)
        ((FunctionType)data.getType()).getDomain()).
                            getComponent(comp).toString();
  }

  /**
   * get the name of the given component of the range RealType.
   * 
   * @param   Data            VisAD Data object
   * @param   comp            the component index (0...)
   * 
   * @return the name of the RealType
   *
   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static String rangeType (Data data,int comp) 
                   throws VisADException, RemoteException {
    return (String) ((RealTupleType)
        ((FunctionType)data.getType()).getRange()).
                            getComponent(comp).toString();
  }


  /**
   * get a VisAD Unit from the name given
   * 
   * @param   name            name of unit desired (degC, etc)
   * 
   * @return the Unit corresponding to the name
   *
   */
  public static Unit makeUnit(String name) 
    throws visad.data.netcdf.units.NoSuchUnitException, 
    visad.data.netcdf.units.ParseException {
    return (visad.data.netcdf.units.Parser.parse(name));
  }

  /** create a Linear1DSet for domain samples
  *
  * @param first is the first value in the linear set
  * @param last is the last value in the linear set
  * @param length is the number of values in the set
  *
  * @return the created visad.Linear1DSet
  *
  * Note: this is for testing ONLY and may not remain!
  */
  public static Linear1DSet makeDomain 
                       (double first, double last, int length) 
                       throws VisADException {
    return new Linear1DSet(first, last, length );
  }

  /** create a Linear1DSet for domain samples
  *
  * @param type is the VisAD MathType of this set
  * @param first is the first value in the linear set
  * @param last is the last value in the linear set
  * @param length is the number of values in the set
  *
  * @return the created visad.Linear1DSet
  *
  * Note: this is for testing ONLY and may not remain!
  */
  public static Linear1DSet makeDomain
           (MathType type, double first, double last, int length) 
           throws VisADException {
    return new Linear1DSet(type, first, last, length);
  }

  /*
  public static Linear1DSet makeDomain(double[] vals) {
    //if vals is sorted, make a Gridded1DSet; otherwise, Irregular1DSet
    return (Linear1DSet) null;
  }
  public static Linear1DSet makeDomain(MathType type, double[] vals) {
    //if vals is sorted, make a Gridded1DSet; otherwise, Irregular1DSet
    return (Linear1DSet) null;
  }
  */


  /** create a Linear2DSet for domain samples
  *
  * @param first1 is the first value in the linear set's 1st dimension
  * @param last1 is the last value in the linear set's 1st dimension
  * @param length1 is the number of values in the set's 1st dimension
  * @param first2 is the first value in the linear set's 2nd dimension
  * @param last2 is the last value in the linear set's 2nd dimension
  * @param length2 is the number of values in the set's 2nd dimension
  *
  * @return the created visad.Linear2DSet
  *
  * Note: this is for testing ONLY and may not remain!
  */
  public static Linear2DSet makeDomain
                    (double first1, double last1, int length1,
                     double first2, double last2, int length2) 
                     throws VisADException {
    return new Linear2DSet(first1, last1, length1, 
                            first2, last2, length2);
  }

  /** create a Linear2DSet for domain samples
  *
  * @param type is the VisAD MathType of this set
  * @param first1 is the first value in the linear set's 1st dimension
  * @param last1 is the last value in the linear set's 1st dimension
  * @param length1 is the number of values in the set's 1st dimension
  * @param first2 is the first value in the linear set's 2nd dimension
  * @param last2 is the last value in the linear set's 2nd dimension
  * @param length2 is the number of values in the set's 2nd dimension
  *
  * @return the created visad.Linear2DSet
  *
  * Note: this is for testing ONLY and may not remain!
  */
  public static Linear2DSet makeDomain (MathType type, 
                         double first1, double last1, int length1, 
                         double first2, double last2, int length2) 
                         throws VisADException {
    return new Linear2DSet(type, first1, last1, length1, 
                                  first2, last2, length2);
  }

  /** create a Linear3DSet for domain samples
  *
  * @param first1 is the first value in the linear set's 1st dimension
  * @param last1 is the last value in the linear set's 1st dimension
  * @param length1 is the number of values in the set's 1st dimension
  * @param first2 is the first value in the linear set's 2nd dimension
  * @param last2 is the last value in the linear set's 2nd dimension
  * @param length2 is the number of values in the set's 2nd dimension
  * @param first3 is the first value in the linear set's 3rd dimension
  * @param last3 is the last value in the linear set's 3rd dimension
  * @param length3 is the number of values in the set's 3rd dimension
  * @return the created visad.Linear3DSet
  *
  * Note: this is for testing ONLY and may not remain!
  */
  public static Linear3DSet makeDomain 
                    (double first1, double last1, int length1,
                     double first2, double last2, int length2,
                     double first3, double last3, int length3) 
                     throws VisADException {
    return new Linear3DSet(first1, last1, length1, 
                            first2, last2, length2,
                            first3, last3, length3);
  }

  /** create a Linear3DSet for domain samples
  *
  * @param type is the VisAD MathType of this set
  * @param first1 is the first value in the linear set's 1st dimension
  * @param last1 is the last value in the linear set's 1st dimension
  * @param length1 is the number of values in the set's 1st dimension
  * @param first2 is the first value in the linear set's 2nd dimension
  * @param last2 is the last value in the linear set's 2nd dimension
  * @param length2 is the number of values in the set's 2nd dimension
  * @param first3 is the first value in the linear set's 3rd dimension
  * @param last3 is the last value in the linear set's 3rd dimension
  * @param length3 is the number of values in the set's 3rd dimension
  * @return the created visad.Linear3DSet
  *
  * Note: this is for testing ONLY and may not remain!
  */
  public static Linear3DSet makeDomain (MathType type, 
                         double first1, double last1, int length1, 
                         double first2, double last2, int length2,
                         double first3, double last3, int length3) 
                         throws VisADException {
    return new Linear3DSet(type, first1, last1, length1, 
                                  first2, last2, length2,
                                  first3, last3, length3);
  }

  public static Set getDomain(Data data) 
             throws VisADException, RemoteException {
    return (Set) ((Field)data).getDomainSet();
  }


  /** resample the data field into the defined domain set
  *
  * @param data is the input Field
  * @param s is the Set which must have a domain MathType identical
  *   to data's original
  *
  * @return the new Field
  *
  * Note: this is for testing ONLY and may not remain!
  */
  public static Field resample(Field data, Set s) 
             throws VisADException, RemoteException {
    return data.resample(s,0,0);
  }

  /** extracts a component of the Field
  *
  * @param data the field with multiple range componenents
  * @param t the MathType of the field to extract
  *
  * @return the new Field
  *
  * Note: this is for testing ONLY and may not remain!
  */
  public static Field extract(Field data, MathType t) 
             throws VisADException, RemoteException {
    return ((FieldImpl)data).extract(t);

  }

  /** extracts a component of the Field
  *
  * @param data the field with multiple range componenents
  * @param s the name of the components to extract
  *
  * @return the new Field
  *
  * Note: this is for testing ONLY and may not remain!
  */
  public static Field extract(Field data, String s) 
             throws VisADException, RemoteException {
    return ((FieldImpl)data).extract(s);

  }

  /** extracts a component of the Field
  *
  * @param data the field with multiple range componenents
  * @param comp the index of the component to extract
  *
  * @return the new Field
  *
  * Note: this is for testing ONLY and may not remain!
  */
  public static Field extract(Field data, int comp) 
             throws VisADException, RemoteException {
    return ((FieldImpl)data).extract(comp);

  }

  /** factors out the given MathType from the domain of
  * the data object.  For example, if the data has a
  * MathType:  (Line, Element)->(value)
  * then factoring out Element, creates a new data
  * object with a MathType:  Element->(Line->(value))
  *
  * @param factor is the domain component Type to factor out
  *
  * @return the new Field
  *
  * Note: this is for testing ONLY and may not remain!
  */
  public static Field domainFactor(Field data, RealType factor) 
             throws VisADException, RemoteException {
    return ((FieldImpl)data).domainFactor(factor);
  }

  /** creates a VisAD MathType from the given string
  *
  * @param s is the string describing the names in
  * the form:  (x,y)->(a)  for a Field.
  * It can be as simple as "foo" for a single RealType.
  *
  */
  public static MathType makeType(String s) 
             throws VisADException, RemoteException {
    return MathType.stringToType(s);
  }

  /** get the RealType corresponding to the name; if
  * none exists, make one and return it.
  *
  * @param name is the name of the RealType type.
  *
  */
  public static RealType getRealType(String name) {
    return (visad.RealType.getRealType(name));
  }

  /** get the MathType of the named VisAD data object
  *
  * @param data is the VisAD Data object
  *
  */
  public static MathType getType(Data data) 
             throws VisADException, RemoteException {
    return (data.getType());
  }
  
  /** helper method for the dump(Data|Math)Type() methods
  *   this will list both the MathType and DataType information
  *   to stdout.
  *
  * @param d is the Data object
  *
  */
  public static void dumpType(Data d) 
             throws VisADException, RemoteException {
      MathType t = d.getType();
      visad.jmet.DumpType.dumpMathType(t);
      System.out.println("- - - - - - - - - - - - - - - - - - - - - - - ");
      System.out.println("DataType analysis...");
      visad.jmet.DumpType.dumpDataType(d);
  }

}

