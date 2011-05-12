//
// JPythonMethods.java
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

package visad.python;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.lang.Math;

import visad.*;
import visad.math.*;
import visad.matrix.*;
import visad.data.*;
import visad.data.netcdf.Plain;
import visad.ss.*;
import ucar.netcdf.NetcdfFile;
import visad.data.netcdf.QuantityDBManager;
import visad.data.netcdf.in.DefaultView;
import visad.data.netcdf.in.NetcdfAdapter;
import java.util.TreeSet;

/**
 * A collection of methods for working with VisAD, callable from the
 * JPython editor.
 */
public abstract class JPythonMethods {

  private static final String DEFAULT_NAME = "Jython";

  private static final String ID = JPythonMethods.class.getName();

  private static DefaultFamily form = new DefaultFamily(ID);

  private static final String[] ops = {"gt","ge","lt","le","eq","ne","ne"};
  private static final String[] ops_sym = {">",">=","<","<=","==","!=","<>"};

  /**
   * Reads in data from the given location (filename or URL).
   */
  public static DataImpl load(String location) throws VisADException {
    return form.open(location);
  }

  /** Make a Hashtable available for everyone
  */
  public static Hashtable JyVars = new Hashtable();

  private static Hashtable frames = new Hashtable();

  /**
   * Displays the given data onscreen.
   *
   * @param   data            VisAD data object to plot; alternatively
   *                          this may be a float[] or float[][].
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(float[] data)
    throws VisADException, RemoteException
  {
    plot(null, field(data), false, 1.0, 1.0, 1.0);
  }

  public static void plot(float[][] data)
    throws VisADException, RemoteException
  {
    plot(null, field(data), false, 1.0, 1.0, 1.0);
  }

  public static void plot(DataImpl data)
    throws VisADException, RemoteException
  {
    plot(null, data, false, 1.0, 1.0, 1.0);
  }

  /**
   * Displays the given data onscreen.
   *
   * @param   data            VisAD data object to plot; may also be
   *                          a float[] or float[][]
   * @param   maps            ScalarMaps for the display
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(float[] data, ScalarMap[] maps)
    throws VisADException, RemoteException {
    plot(null, field(data), false, 1.0, 1.0, 1.0, maps);
  }

  public static void plot(float[][] data, ScalarMap[] maps)
    throws VisADException, RemoteException {
    plot(null, field(data), false, 1.0, 1.0, 1.0, maps);
  }

  public static void plot(DataImpl data, ScalarMap[] maps)
    throws VisADException, RemoteException {
    plot(null, data, false, 1.0, 1.0, 1.0, maps);
  }

  /**
   * Displays the given data onscreen,
   * displaying the edit mappings dialog if specified.
   *
   * @param   data            VisAD data object to plot; may also be
   *                          a float[] or float[][]
   * @param   editMaps        whether to initially display edit mappings dialog
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(float[] data, boolean editMaps)
    throws VisADException, RemoteException
  {
    plot(null, field(data), editMaps, 1.0, 1.0, 1.0);
  }
  
  public static void plot(float[][] data, boolean editMaps)
    throws VisADException, RemoteException
  {
    plot(null, field(data), editMaps, 1.0, 1.0, 1.0);
  }
  
  public static void plot(DataImpl data, boolean editMaps)
    throws VisADException, RemoteException
  {
    plot(null, data, editMaps, 1.0, 1.0, 1.0);
  }
  
  /**
   * Displays the given data onscreen.
   *
   * @param   name            name of display in which to plot data
   * @param   data            VisAD data object to plot; may also be
   *                          a float[] or float[][]
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(String name, float[] data)
    throws VisADException, RemoteException
  {
    plot(name, field(data), false, 1.0, 1.0, 1.0);
  }

  public static void plot(String name, float[][] data)
    throws VisADException, RemoteException
  {
    plot(name, field(data), false, 1.0, 1.0, 1.0);
  }

  public static void plot(String name, DataImpl data)
    throws VisADException, RemoteException
  {
    plot(name, data, false, 1.0, 1.0, 1.0);
  }

  /**
   * Displays the given data onscreen.
   *
   * @param   name            name of display in which to plot data
   * @param   data            VisAD data object to plot; may also be
   *                          a float[] or float[][]
   * @param   maps            ScalarMaps for display
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(String name, float[] data, ScalarMap[] maps)
    throws VisADException, RemoteException
  {
    plot(name, field(data), false, 1.0, 1.0, 1.0, maps);
  }

  public static void plot(String name, float[][] data, ScalarMap[] maps)
    throws VisADException, RemoteException
  {
    plot(name, field(data), false, 1.0, 1.0, 1.0, maps);
  }

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
   * @param   data            VisAD data object to plot; may also be
   *                          a float[] or float[][]
   * @param   editMaps        whether to initially display edit mappings dialog
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(String name, float[] data, boolean editMaps)
    throws VisADException, RemoteException
  {
    plot(name, field(data), editMaps, 1.0, 1.0, 1.0);
  }

  public static void plot(String name, float[][] data, boolean editMaps)
    throws VisADException, RemoteException
  {
    plot(name, field(data), editMaps, 1.0, 1.0, 1.0);
  }

  public static void plot(String name, DataImpl data, boolean editMaps)
    throws VisADException, RemoteException
  {
    plot(name, data, editMaps, 1.0, 1.0, 1.0);
  }

  /**
   * Displays the given data onscreen, using given color default.
   *
   * @param   data            VisAD data object to plot; may also be
   *                          a float[] or float[][]
   * @param   red             red component of default color to use if there
   *                          are no color mappings from data's RealTypes;
   *                          color component values between 0.0 and 1.0
   * @param   green           green component of default color
   * @param   blue            blue component of default color
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void plot(float[] data, double red, double green, double blue)
    throws VisADException, RemoteException
  {
    plot(null, field(data), false, red, green, blue);
  }

  public static void plot(float[][] data, double red, double green, double blue)
    throws VisADException, RemoteException
  {
    plot(null, field(data), false, red, green, blue);
  }

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
   * @param   data            VisAD data object to plot; may also be
   *                          a float[] or float[][]
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
  public static void plot(String namxe, float[] data,
    boolean editMaps, double red, double green, double blue)
    throws VisADException, RemoteException {
       plot(namxe, field(data), editMaps,red, green, blue, null);
  }

  public static void plot(String namxe, float[][] data,
    boolean editMaps, double red, double green, double blue)
    throws VisADException, RemoteException {
       plot(namxe, field(data), editMaps,red, green, blue, null);
  }

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

  /** save the Data in a netcdf file
  *
  */
  public static void saveNetcdf(String fn, Data d) 
                 throws VisADException, RemoteException, IOException {
    new Plain().save(fn,d,false);
  }

  /** save the display genreated by a quick graph or showDisplay
  *
  * @param disp is the DisplayImpl to save
  * @param filename is the name of the JPG file to write
  *
  */
  public static void saveplot(DisplayImpl disp, String filename) 
                 throws VisADException, RemoteException, IOException {
    visad.util.Util.captureDisplay(disp, filename);
  }

  /**
   * save the onscreen data display generated by plot()
   *
   * @throws  VisADException  part of data and display APIs, shouldn't occur
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static void saveplot(String filename) 
                 throws VisADException, RemoteException, IOException {
    saveplot((String)null, filename);
  }

  /**
   * clear the onscreen data display with the given name
   *
   * @param   name            name of the display to clear
   * @param   filename        name of the file to save display into
   *
   * @throws  VisADException  part of data and display APIs, shouldn't occur
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   * @throws  IOException part of data and display APIs, shouldn't occur
   */
  public static void saveplot(String name, String filename)
    throws VisADException, RemoteException, IOException {

    if (name == null) name = DEFAULT_NAME;
    final BasicSSCell sscell = BasicSSCell.getSSCellByName(name);
    final String fn = filename;
    if (sscell != null) {
      Runnable captureDisp = new Runnable() {
        public void run() {
          try {
            sscell.captureImage(new File(fn));
          } catch (Exception se) {
            System.out.println("Error saving plot = "+se);
          }
        }
      };

      Thread ts = new Thread(captureDisp);
      ts.start();
    }
  }

  /**
   * return pointwise absolute value of data
   * name changed 1/11/02 to avoid conflicts with Jython built-in
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data abs_data(Data data) 
           throws VisADException, RemoteException {
    return data.abs();
  }

  /**
   * return pointwise absolute value of data
   *
   * @param   data            VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data abs(Data data) 
           throws VisADException, RemoteException {
    return data.abs();
  }
  
  /**
   * return absolute value of value
   *
   * @param   value     value
   *
   */
  public static double abs(double value) {
    return Math.abs(value);
  }

  /**
   * return absolute value of value
   *
   * @param   value     value
   *
   */
  public static int abs(int value) {
    return Math.abs(value);
  }

  /**
   * return absolute value of value
   *
   * @param   value     value
   *
   */
  public static long abs(long value) {
    return Math.abs(value);
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
   * return round value of data (closest integer)
   *
   * @param   value value 
   *
   */
  public static double round(double value, int digits) {
    boolean neg = value < 0;
    double multiple = Math.pow(10., digits);
    if (neg)
        value = -value;
    double tmp = Math.floor(value*multiple+0.5);
    if (neg)
        tmp = -tmp;
    return (tmp/multiple);
  }

  /**
   * return round value of data (closest integer)
   *
   * @param   value value 
   *
   */
  public static double round(double value) {
    return round(value,0);
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
   * return pointwise arc tangent value of data1 / data2 over 
   * full (-pi, pi) range, returned in radians.
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
   * return pointwise arc tangent value of data1 / data2 over 
   * full (-pi, pi) range, returned in degrees.
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
   * return pointwise arc tangent value of data1 / data2 over 
   * full (-pi, pi) range, returned in radians.
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
   * return pointwise arc tangent value of data1 / data2 over 
   * full (-pi, pi) range, returned in degrees.
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
   * name changed 1/11/02 to avoid conflicts with Jython built-in
   *
   * @param   data1           VisAD data object
   * @param   data2           VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data max_data(Data data1, Data data2)
         throws VisADException, RemoteException {
    return data1.max(data2);
  }

  /**
   * return pointwise aximum value of data1 and data2 
   * name changed 1/11/02 to avoid conflicts with Jython built-in
   *
   * @param   data1           VisAD data object
   * @param   data2           double value
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data max_data(Data data1, double data2) 
         throws VisADException, RemoteException {
    return data1.max(new Real(data2));
  }

  /**
   * return pointwise maximum value of data1 and data2
   * name changed 1/11/02 to avoid conflicts with Jython built-in
   *
   * @param   data1           double value
   * @param   data2           VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data max_data(double data1, Data data2) 
         throws VisADException, RemoteException {
    return new Real(data1).max(data2);
  }

  /**
   * return pointwise minimum value of data1 and data2 
   * name changed 1/11/02 to avoid conflicts with Jython built-in
   *
   * @param   data1           VisAD data object
   * @param   data2           VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data min_data(Data data1, Data data2)
         throws VisADException, RemoteException {
    return data1.min(data2);
  }

  /**
   * return pointwise minimum value of data1 and data2 
   * name changed 1/11/02 to avoid conflicts with Jython built-in
   *
   * @param   data1           VisAD data object
   * @param   data2           double value
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data min_data(Data data1, double data2) 
         throws VisADException, RemoteException {
    return data1.min(new Real(data2));
  }

  /**
   * return pointwise minimum value of data1 and data2
   * name changed 1/11/02 to avoid conflicts with Jython built-in
   *
   * @param   data1           double value
   * @param   data2           VisAD data object
   *
   * @throws  VisADException  invalid data
   * @throws  RemoteException unable to access remote data
   */
  public static Data min_data(double data1, Data data2) 
         throws VisADException, RemoteException {
    return new Real(data1).min(data2);
  }

  /**
   * return pointwise arc tangent value of data1 / data2 over 
   * full (-pi, pi) range, returned in radians.
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
   * return pointwise arc tangent value of data1 / data2 over 
   * full (-pi, pi) range, returned in degrees.
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
   *                          components as dimensions of the histogram
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
   *                          components as dimensions of the histogram
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
/* WLH 21 Feb 2003
    double[][] data_ranges = field.computeRanges(srealComponents);
*/
    float[][] values = field.getFloats(false);
    int nn = values.length;
    double[][] data_ranges = new double[dim][2];
    for (int i=0; i<dim; i++) {
      if (0 <= ranges[i] && ranges[i] < nn) {
        data_ranges[i][0] = Double.MAX_VALUE;
        data_ranges[i][1] = -Double.MAX_VALUE;
        float[] v = values[ranges[i]];
        for (int j=0; j<v.length; j++) {
          if (v[j] < data_ranges[i][0]) data_ranges[i][0] = v[j];
          if (v[j] > data_ranges[i][1]) data_ranges[i][1] = v[j];
        }
      }
      else {
        throw new VisADException("range index out of range " + ranges[i]);
      }
    }

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
    FlatField result = Histogram.makeHistogram(field, set);
    
    return result;
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
    return field("domain", name, values);
  }

  /**
   * return a VisAD FlatField with default 1-D domain, with range values
   * given by values array, and with given range RealType name
   *
   * @param   dom0            String defining domain RealType name
   * @param   name            String defining range RealType name
   * @param   values          float[] array defining range values of field
   *
   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static FlatField field(String dom0, String name, float[] values)
         throws VisADException, RemoteException {
    if (values == null || values.length == 0) {
      throw new VisADException("bad values");
    }
    RealType domain = RealType.getRealType(dom0);
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
    return field("ImageLine", "ImageElement", name, values);
  }

  /**
   * return a VisAD FlatField with named default 2-D domain, with range values
   * given by values array and with given range RealType name
   *
   * @param   dom0            String defines first domain component
   * @param   dom1            String defines second domain component
   * @param   name            String defining range RealType name
   * @param   values          float[][] array defining range values of field
   *
   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static FlatField field(
       String dom0, String dom1, String rng, float[][] values)
         throws VisADException, RemoteException {
    int[] temps = getValuesLengths(values);
    int values_len = temps[0];
    int min = temps[1];
    int max = temps[2];
    RealType first = RealType.getRealType(dom0);
    RealType second = RealType.getRealType(dom1);
    RealTupleType domain = new RealTupleType(first, second);
    return field(new Integer2DSet(domain, max, values_len), rng, values);
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

  public static int[] getValuesLengths(float[][] values)
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
  public static int getDomainDimension(Data data)
                  throws VisADException, RemoteException {
     return (domainDimension(data));
  }

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
  public static int getRangeDimension(Data data)
                  throws VisADException, RemoteException {
     return (rangeDimension(data));
  }

  /**
   * get the number of range components of the Data object
   * 
   * @param   Data            VisAD Data object
   * @return the number of range components
   *
   * @throws  VisADException  unable to construct field
   * @throws  RemoteException part of data and display APIs, shouldn't occur
   */
  public static int rangeDimension (Data data) 
                  throws VisADException, RemoteException {
    int nr = 1;
    if ( data instanceof FlatField) {
      nr = ((FlatField) data).getRangeDimension();
    }
    return nr;
  }


  /** get the domain Type for the field
  *
  * @param data is the field to get the domain Type for
  *
  * @return the domain
  *
  * @throws  VisADException  unable to construct field
  * @throws  RemoteException part of data and display APIs, shouldn't occur
  */
  public static RealTupleType getDomainType(Data data)
                  throws VisADException, RemoteException {
     return (domainType(data));
  }

  /** get the domain Type for the field
  *
  * @param data is the field to get the domain Type for
  *
  * @return the domain
  *
  * @throws  VisADException  unable to construct field
  * @throws  RemoteException part of data and display APIs, shouldn't occur
  */
  public static RealTupleType domainType(Data data) 
                  throws VisADException, RemoteException {
    return (RealTupleType) ((FunctionType)data.getType()).getDomain();
  
  }


  /** get the domain Type for the FunctionType
  *
  * @param type is the FunctionType
  *
  * @return the domain type
  *
  * @throws  VisADException  unable to construct field
  * @throws  RemoteException part of data and display APIs, shouldn't occur
  */
  public static RealTupleType getDomainType(FunctionType type)
                  throws VisADException, RemoteException {
     return (type.getDomain());
  }

  /** get the range Type for the field
  *
  * @param data is the field to get the range Type for
  *
  * @return the range
  *
  * @throws  VisADException  unable to construct field
  * @throws  RemoteException part of data and display APIs, shouldn't occur
  */
  public static MathType getRangeType(Data data)
                  throws VisADException, RemoteException {
     return (rangeType(data));
  }

  /** get the range Type for the field
  *
  * @param data is the field to get the range Type for
  *
  * @return the range
  *
  * @throws  VisADException  unable to construct field
  * @throws  RemoteException part of data and display APIs, shouldn't occur
  */
  public static MathType rangeType(Data data) 
                  throws VisADException, RemoteException {
    return (MathType) ((FunctionType)data.getType()).getRange();
  
  }

  /** get the range Type for the FunctionType
  *
  * @param type is the FunctionType
  *
  * @return the range Type
  *
  * @throws  VisADException  unable to construct field
  * @throws  RemoteException part of data and display APIs, shouldn't occur
  */
  public static MathType getRangeType(FunctionType type)
                  throws VisADException, RemoteException {
     return (type.getRange());
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
    MathType rt = rangeType(data);
    int rd = rangeDimension(data);
    String dt = rt.toString();
    if (rd > 1) dt = ((TupleType)rt).getComponent(comp).toString();
    return dt;
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
         throws visad.data.units.NoSuchUnitException,
         visad.data.units.ParseException {
    return (visad.data.units.Parser.parse(name));
  }

  /** make an Integer1DSet of given length
  *
  * @param length is the desired length of the 1D Integer Set
  *
  * @return the Integer1DSet
  */
  public static Integer1DSet makeDomain (int length) 
                       throws VisADException {
    return new Integer1DSet(length);
  }

  /** make an Integer1DSet of given length and MathType
  *
  * @param type is the MathType of the Set
  * @param length is the desired length of the 1D Integer Set
  *
  * @return the Integer1DSet
  */
  public static Integer1DSet makeDomain (MathType type, int length) 
                       throws VisADException {
    return new Integer1DSet(type, length);
  }
  /** make an Integer1DSet of given length and make a MathType 
  *
  * @param name is the MathType name to use to create the MathType
  * @param length is the desired length of the 1D Integer Set
  *
  * @return the Integer1DSet
  */
  public static Integer1DSet makeDomain (String name, int length) 
                       throws VisADException {
    return new Integer1DSet(RealType.getRealType(name), length);
  }

  /** make an Integer2DSet of given lengths
  *
  * @param lengthX is the desired length of the 2D Integer Set x
  * @param lengthY is the desired length of the 2D Integer Set y
  *
  * @return the Integer2DSet
  */
  public static Integer2DSet makeDomain (int lengthX, int lengthY) 
                       throws VisADException {
    return new Integer2DSet(lengthX, lengthY);
  }

  /** make an Integer2DSet of given lengths
  *
  * @param type is the MathType of the Set
  * @param lengthX is the desired length of the 2D Integer Set x
  * @param lengthY is the desired length of the 2D Integer Set y
  *
  * @return the Integer2DSet
  */
  public static Integer2DSet makeDomain 
                      (MathType type, int lengthX, int lengthY) 
                       throws VisADException {
    return new Integer2DSet(type, lengthX, lengthY);
  }

  /** make an Integer2DSet of given lengths
  *
  * @param name is the MathType name to use to create the MathType 
  * (should be in the form:  "(xx,yy)" )
  * @param lengthX is the desired length of the 2D Integer Set x
  * @param lengthY is the desired length of the 2D Integer Set y
  *
  * @return the Integer2DSet
  */
  public static Integer2DSet makeDomain 
                      (String name, int lengthX, int lengthY) 
                         throws VisADException, RemoteException {
    return new Integer2DSet((RealTupleType) makeType(name), lengthX, lengthY);
  }


  /** create a Linear1DSet for domain samples
  *
  * @param first is the first value in the linear set
  * @param last is the last value in the linear set
  * @param length is the number of values in the set
  *
  * @return the created visad.Linear1DSet
  *
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
  */
  public static Linear1DSet makeDomain
           (MathType type, double first, double last, int length) 
           throws VisADException {
    return new Linear1DSet(type, first, last, length);
  }

  /** create a Linear1DSet for domain samples
  *
  * @param name is the name of the VisAD MathType of this set
  * @param first is the first value in the linear set
  * @param last is the last value in the linear set
  * @param length is the number of values in the set
  *
  * @return the created visad.Linear1DSet
  *
  */
  public static Linear1DSet makeDomain
           (String name, double first, double last, int length) 
           throws VisADException {
    return new Linear1DSet(RealType.getRealType(name),first, last, length);
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
  */
  public static Linear2DSet makeDomain (MathType type, 
                         double first1, double last1, int length1, 
                         double first2, double last2, int length2) 
                         throws VisADException {
    return new Linear2DSet(type, first1, last1, length1, 
                                  first2, last2, length2);
  }

  /** create a Linear2DSet for domain samples
  *
  * @param name is the name of the VisAD MathType of this set
  * @param first1 is the first value in the linear set's 1st dimension
  * @param last1 is the last value in the linear set's 1st dimension
  * @param length1 is the number of values in the set's 1st dimension
  * @param first2 is the first value in the linear set's 2nd dimension
  * @param last2 is the last value in the linear set's 2nd dimension
  * @param length2 is the number of values in the set's 2nd dimension
  *
  * @return the created visad.Linear2DSet
  *
  */
  public static Linear2DSet makeDomain (String name, 
                         double first1, double last1, int length1, 
                         double first2, double last2, int length2) 
                         throws VisADException, RemoteException {

    return new Linear2DSet((RealTupleType) (makeType(name)), 
                                  first1, last1, length1, 
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
  */
  public static Linear3DSet makeDomain (MathType type, 
                         double first1, double last1, int length1, 
                         double first2, double last2, int length2,
                         double first3, double last3, int length3) 
                         throws VisADException {

    return new Linear3DSet(type,  first1, last1, length1, 
                                  first2, last2, length2,
                                  first3, last3, length3);
  }

  /** create a Linear3DSet for domain samples
  *
  * @param name is the name of the VisAD MathType of this set
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
  */
  public static Linear3DSet makeDomain (String name,
                         double first1, double last1, int length1, 
                         double first2, double last2, int length2,
                         double first3, double last3, int length3) 
                         throws VisADException, RemoteException {

    return new Linear3DSet((RealTupleType) (makeType(name)), 
                                  first1, last1, length1, 
                                  first2, last2, length2,
                                  first3, last3, length3);
  }

  /** return the sampling set for the domain of the Data object
  *
  * @param data is the VisAD data object
  *
  * @return the sampling Set
  *
  * @throws  VisADException  unable to construct field
  * @throws  RemoteException part of data and display APIs, shouldn't occur
  */
  public static Set getDomainSet(Data data) 
             throws VisADException, RemoteException {
    return (Set) ((Field)data).getDomainSet();
  }

  /** return the sampling set for the domain of the Data object
  *
  * @param data is the VisAD data object
  *
  * @return the sampling Set
  *
  * @throws  VisADException  unable to construct field
  * @throws  RemoteException part of data and display APIs, shouldn't occur
  */
  public static Set getDomain(Data data) 
             throws VisADException, RemoteException {
    return (Set) ((Field)data).getDomainSet();
  }

  /** return the lengths of the components of the sampling set 
  *
  * @param data is the VisAD data object
  *
  * @return an int[] of the lengths
  *
  * @throws  VisADException  unable to construct field
  * @throws  RemoteException part of data and display APIs, shouldn't occur
  */
  public static int[] getDomainSizes(Data data)
             throws VisADException, RemoteException {
    return ((GriddedSet) ((Field)data).getDomainSet()).getLengths();
  }


  /**
  * Replaces specified values in a FlatField with the constant given
  *
  * @param f is the input FlatField
  * @param list is the int[] list of indecies into f to replace
  * @param v is the value to insert into f.
  */
  public static FlatField replace(FieldImpl f, int[] list, Real v) 
             throws VisADException, RemoteException {
    return replace(f, list, v.getValue());
  }

  /**
  * Replaces specified values in a FlatField with the constant given
  *
  * @param f is the input FlatField
  * @param list is the int[] list of indecies into f to replace
  * @param v is the value to insert into f.
  */
  public static FlatField replace(FieldImpl f, int[] list, double v) 
             throws VisADException, RemoteException {
    FlatField ff;
    if (f instanceof FlatField) {
      try {
      ff = (FlatField)f.clone();
      } catch (CloneNotSupportedException cns) {
        throw new VisADException ("Cannot clone field object");
      }
     
    } else {
      ff = (FlatField)((FlatField)f.getSample(0)).clone();
    }
    float [][] dv = ff.getFloats(false);

    for (int i=0; i<list.length; i++) {
      dv[0][list[i]] = (float)v;
    }

    ff.setSamples(dv,false);
    return ff;

  }

  /**
  * Replaces all the missing values in a FlatField with the constant given
  *
  * @param f is the input FlatField
  * @param v is the value to insert into f.
  */
  public static FlatField replaceMissing(FieldImpl f, double v) 
             throws VisADException, RemoteException {
    FlatField ff;
    if (f instanceof FlatField) {
      try {
      ff = (FlatField)f.clone();
      } catch (CloneNotSupportedException cns) {
        throw new VisADException ("Cannot clone field object");
      }
     
    } else {
      ff = (FlatField)((FlatField)f.getSample(0)).clone();
    }
    float [][] dv = ff.getFloats(false);
    for (int i=0; i<dv[0].length; i++) {
      if (dv[0][i] != dv[0][i]) dv[0][i] = (float)v;
    }

    ff.setSamples(dv,false);
    return ff;

  }

  /**
  * Replaces all the values in a FlatField with the constant given
  *
  * @param f is the input FlatField
  * @param v is the value to insert into f.
  */
  public static FlatField replace(FieldImpl f, double v) 
             throws VisADException, RemoteException {
    FlatField ff;
    if (f instanceof FlatField) {
      try {
      ff = (FlatField)f.clone();
      } catch (CloneNotSupportedException cns) {
        throw new VisADException ("Cannot clone field object");
      }
     
    } else {
      ff = (FlatField)((FlatField)f.getSample(0)).clone();
    }
    float [][] dv = ff.getFloats(false);
    for (int i=0; i<dv[0].length; i++) {
      dv[0][i] = (float)v;
    }

    ff.setSamples(dv,false);
    return ff;

  }

  /**
  * Replaces all the values in a FlatField with the constant given
  *
  * @param f is the input FlatField
  * @param v is the value to insert into f.
  */
  public static FlatField replace(FieldImpl f, Real v) 
             throws VisADException, RemoteException {
    FlatField ff;
    if (f instanceof FlatField) {
      try {
      ff = (FlatField)f.clone();
      } catch (CloneNotSupportedException cns) {
        throw new VisADException ("Cannot clone field object");
      }
     
    } else {
      ff = (FlatField)((FlatField)f.getSample(0)).clone();
    }
    float [][] dv = ff.getFloats(false);

    float vv = (float)(v.getValue());
    for (int i=0; i<dv[0].length; i++) {
      dv[0][i] = vv;
    }

    ff.setSamples(dv,false);
    return ff;

  }

  /**
  * Find the minium and maximum values of the FlatField or
  * a sequence within a FieldImpl.  
  *
  * @param f the FlatField (or FieldImpl - for a sequence)
  *
  * return double[2].  double[0] = min, double[1] = max
  *   if the fields are all missing, then return min = max = Double.NaN
  */

  public static double[] getMinMax(FieldImpl f)
       throws VisADException, RemoteException {

    boolean isFI = false;
    int numItems;
    if (f instanceof FlatField) {
      numItems = 1;
     
    } else if (domainDimension(f) == 1) {
      isFI = true;
      numItems = getDomainSet(f).getLength();

    } else {
      throw new VisADException("Cannot rescale the data - unknown structure");
    }

    double [] minmax = new double[2];
    minmax[0] = Double.POSITIVE_INFINITY;
    minmax[1] = Double.NEGATIVE_INFINITY;
    float[][] dv;

    for (int m=0; m<numItems; m++) {
      if (isFI) {
        dv = ( (FlatField)(f.getSample(m))).getFloats(false);
      } else {
        dv = f.getFloats(false);
      }

      for (int i=0; i<dv.length; i++) {
        for (int k=0; k<dv[i].length; k++) {
          if (dv[i][k] < minmax[0]) minmax[0] = dv[i][k];
          if (dv[i][k] > minmax[1]) minmax[1] = dv[i][k];
        }
      }
    }

    // if fields were all NaN, return NaN's as well....

    if (minmax[0] > minmax[1]) {
      minmax[0] = Double.NaN;
      minmax[1] = Double.NaN;
    }

    return minmax;
  }

  /**
  * Re-scale the values in a FieldImpl using auto-scaling
  *
  * @param f the FlatField (or FieldImpl sequence)
  * @param outlo the output low-range value
  * @param outhi the output high range value
  *
  * Values of the original field will be linearly
  *    scaled from their "min:max" to "outlo:outhi"
  *
  * If input FieldImpl is a sequence, then all items in sequence are done
  * but the "min" and "max" are computed from all members of the sequence! 
  *
  * return new FieldImpl
  *
  */
  public static FieldImpl rescale(FieldImpl f, double outlo, double outhi)
       throws VisADException, RemoteException {

    double [] minmax = getMinMax(f);
    return rescale(f,minmax[0], minmax[1], outlo, outhi);
  }





  /**
  * Re-scale the values in a FieldIimpl
  *
  * @param f the FieldImpl or FlatField
  * @param inlo the input low-range value
  * @param inhi the input high-range value
  * @param outlo the output low-range value
  * @param outhi the output high range value
  *
  * Values of the original field will be linearly
  *    scaled from "inlo:inhi" to "outlo:outhi"
  * 
  * Values < inlo will be set to outlo; values > inhi set to outhi
  *
  * If input FieldImpl is a sequence, then all items in sequence are done
  *
  * Values returned in a new FieldImpl
  */

  public static FieldImpl rescale(FieldImpl f, 
    double inlo, double inhi, double outlo, double outhi)
             throws VisADException, RemoteException {
        
    FlatField ff = null;
    FieldImpl fi = null;
    boolean isFI = false;
    int numItems = 1;
    if (f instanceof FlatField) {
      try {
      ff = (FlatField)f.clone();
      } catch (CloneNotSupportedException cns) {
        throw new VisADException ("Cannot clone field object");
      }
     
    } else if (domainDimension(f) == 1) {
      isFI = true;
      try {
        fi = (FieldImpl)f.clone();
        numItems = getDomainSet(f).getLength();
      } catch (CloneNotSupportedException cnsfi) {
        throw new VisADException ("Cannot clone FieldImpl object");
      }
    } else {
      throw new VisADException("Cannot rescale the data - unknown structure");
    }

    float [][] dv;
    for (int m=0; m<numItems; m++) {
      if (isFI) {
        dv = ( (FlatField)(fi.getSample(m))).getFloats(false);
      } else {
        dv = ff.getFloats(false);
      }

      double outrange = outhi - outlo;
      double inrange = inhi - inlo;
      for (int i=0; i<dv.length; i++) {
        for (int k=0; k<dv[i].length; k++) {
          dv[i][k] = (float)(outlo + outrange * (dv[i][k] - inlo)/inrange);
          if (dv[i][k] < outlo) dv[i][k] = (float)outlo;
          if (dv[i][k] > outhi) dv[i][k] = (float)outhi;
        }
      }

      if (isFI) {
        ( (FlatField)(fi.getSample(m))).setSamples(dv,false);
      } else {
        ff.setSamples(dv,false);
      }
    }

    if (isFI) {
      return fi;
    } else {
      return (FieldImpl)ff;
    }
  }

  /**
  * Mask out values outside testing limits in a FieldImpl
  *
  * @param f  VisAD data object (FlatField or FieldImpl) as source
  * @param op  Comparison operator as string ('gt','le',...)
  * @param v  Numeric operand for comparison
  *
  * @return a FieldImpl with values of either 0 (did not meet
  * criterion) or 1 (met criteron).
  *
  * Example:  b = mask(a, 'gt', 100)
  * if 'a' is an image, 'b' will be an image with values of
  * 1 where 'a' was > 100, and zero elsewhere.
  *
  */
  public static FieldImpl mask(FieldImpl f, String op, double v) 
             throws VisADException, RemoteException {
    return mask(f, op, new Real(v));
  }


  public static FieldImpl mask(Data f, String op, Data v) 
             throws VisADException, RemoteException {
    if (! (f instanceof FieldImpl) ) {
      throw new VisADException("Data must be a FieldImpl or FlatField");
    }
    return mask((FieldImpl)f, op, v);
  }


  /**
  * Mask out values outside testing limits in a FieldImpl
  *
  * @param f  VisAD data object (FlatField or FieldImpl) as source
  * @param op  Comparison operator as string ('gt','le',...)
  * @param v  VisAd operand for comparison.
  *
  * If the value of 'v' is a Field, then it will be resampled
  * to the domain of 'f' is possible before the comparison.
  *
  * @return a FieldImpl with values of either 0 (did not meet
  * criterion) or 1 (met criteron).
  *
  * Example:  b = mask(a, 'gt', c)
  * if 'a' is an image, 'b' will be an image with values of
  * 1 where 'a' was > the corresponding value of 'c', and zero 
  * elsewhere.
  *
  */
  public static FieldImpl mask(FieldImpl f, String op, Data v)
             throws VisADException, RemoteException {
    FlatField ff = null;
    FieldImpl fi = null;
    boolean isFI = false;
    int numItems;
    if (f instanceof FlatField) {
      numItems = 1;
     
    } else if (domainDimension(f) == 1) {
      isFI = true;
      try {
        fi = (FieldImpl)f.clone();
        numItems = getDomainSizes(fi)[0];
      } catch (CloneNotSupportedException cnsfi) {
        throw new VisADException ("Cannot clone FieldImpl object");
      }
    } else {
      throw new VisADException("Cannot rescale the data - unknown structure");
    }

    int oper = -1;
    for (int i=0; i<ops.length; i++) {
      if (ops[i].equalsIgnoreCase(op)) oper = i;
      if (ops_sym[i].equalsIgnoreCase(op)) oper = i;
    }
    if (oper < 0) throw new VisADException("Invalid operator: "+op);
    float[][] dv;

    for (int m=0; m<numItems; m++) {
      if (isFI) {
        ff =  (FlatField)((fi.getSample(m)).subtract(v));
      } else {
        ff = (FlatField) f.subtract(v);
      }

      dv = ff.getFloats(false);

      for (int i=0; i<dv.length; i++) {
        for (int k=0; k<dv[i].length; k++) {
          if (oper == 0) {
            if (dv[i][k] > 0.0f) {
              dv[i][k] = 1.0f;
            } else {
              dv[i][k] = 0.0f;
            }
          } else if (oper == 1) {
            if (dv[i][k] >= 0.0f) {
              dv[i][k] = 1.0f;
            } else {
              dv[i][k] = 0.0f;
            }
          } else if (oper == 2) {
            if (dv[i][k] < 0.0f) {
              dv[i][k] = 1.0f;
            } else {
              dv[i][k] = 0.0f;
            }
          } else if (oper == 3) {
            if (dv[i][k] <= 0.0f) {
              dv[i][k] = 1.0f;
            } else {
              dv[i][k] = 0.0f;
            }
          } else if (oper == 4) {
            if (dv[i][k] == 0.0f) {
              dv[i][k] = 1.0f;
            } else {
              dv[i][k] = 0.0f;
            }
          } else if (oper == 5) {
            if (dv[i][k] != 0.0f) {
              dv[i][k] = 1.0f;
            } else {
              dv[i][k] = 0.0f;
            }
          } else {
            if (dv[i][k] != 0.0f) {
              dv[i][k] = 1.0f;
            } else {
              dv[i][k] = 0.0f;
            }
          }
        }
      }

      if (isFI) {
        ( (FlatField)(fi.getSample(m))).setSamples(dv,false);
      } else {
        ff.setSamples(dv,false);
      }
    }

    if (isFI) {
      return fi;
    } else {
      return (FieldImpl)ff;
    }

  }


  /**
  * Get a list of points where a comparison is true.
  *
  * @param f  VisAD data object (FlatField) as source
  * @param op  Comparison operator as string ('gt','le',...)
  * @param v  Numeric operand for comparison
  *
  * @return an int[] containing the sampling indecies where
  * the criterion was met.
  *
  * Example:  b = find(a, 'gt', 100)
  * if 'a' is an image, 'b' will be a list of indecies in
  * 'a' where the values are > 100.
  *
  */
  public static int[] find(FieldImpl f, String op, double v) 
             throws VisADException, RemoteException {
    return find(f, op, new Real(v));
  }

  /**
  * Get a list of points where a comparison is true.
  *
  * @param f  VisAD data object (usually FlatField) as source
  * @param op  Comparison operator as string ('gt','le',...)
  * @param v  VisAd operand for comparison.
  *
  * @return an int[] containing the sampling indecies where
  * the criterion was met.
  *
  * If the value of 'v' is a Field, then it will be resampled
  * to the domain of 'f' is possible before the comparison.
  *
  * Example:  b = find(a, 'gt', c)
  * if 'a' is an image, 'b' will be a list of indecies in
  * 'a' where the values are greater than the corresponding
  * values of 'c'.
  *
  */
  public static int[] find(Data f, String op, Data v)
             throws VisADException, RemoteException {
    FlatField fv;
    if (f instanceof FlatField) {
      fv = (FlatField) f.subtract(v);
    } else {
      fv = (FlatField) (((FieldImpl)f).getSample(0)).subtract(v);
    }
    float [][] dv = fv.getFloats(false);
    Vector z = new Vector();
    int oper = -1;
    for (int i=0; i<ops.length; i++) {
      if (ops[i].equalsIgnoreCase(op)) oper = i;
      if (ops_sym[i].equalsIgnoreCase(op)) oper = i;
    }
    if (oper < 0) throw new VisADException("Invalid operator: "+op);

    for (int i=0; i<1; i++) {
      for (int k=0; k<dv[i].length; k++) {

        if (oper == 0) {
            if (dv[i][k] > 0.0f) z.addElement(new Integer(k));
        } else if (oper == 1) {
            if (dv[i][k] >= 0.0f) z.addElement(new Integer(k));
        } else if (oper == 2) {
            if (dv[i][k] < 0.0f) z.addElement(new Integer(k));
        } else if (oper == 3) {
            if (dv[i][k] <= 0.0f) z.addElement(new Integer(k));
        } else if (oper == 4) {
            if (dv[i][k] == 0.0f) z.addElement(new Integer(k));
        } else if (oper == 5) {
            if (dv[i][k] != 0.0f) z.addElement(new Integer(k));
        } else {
            if (dv[i][k] != 0.0f) z.addElement(new Integer(k));
        }
      }
    }

    int m = z.size();
    int [] rv = new int[m];
    for (int i=0; i<m; i++) {
      rv[i] = ((Integer)z.elementAt(i)).intValue();
    }
    return rv;
  }



  /** resample the data field into the defined domain set
  *
  * @param data is the input Field
  * @param s is the Set which must have a domain MathType identical
  *   to data's original
  *
  * @return the new Field
  *
  */
  public static Field resample(Field data, Set s) 
             throws VisADException, RemoteException {
    return data.resample(s,0,0);
  }

  /** returns the double value of a Real value.
  *
  * @param r is the Real
  *
  * @return value of the Real
  *
  */
  public static double getValue(Real r) {
    return r.getValue();
  }

  /** returns the double values of the range
  *
  * @param data is the Field from which to get the numeric values
  *
  * @return values for all range components in the Field
  *
  */
  public static double[][] getValues(Field data) 
             throws VisADException, RemoteException {
    return data.getValues();
  }

  /** sets the sample values into the Field 
  *
  * @param f is the Field to put the samples into
  *
  * @param vals  are the values for all range components in the Field
  *
  */
  public static void setValues(Field f, double[][] vals) 
             throws VisADException, RemoteException {
    f.setSamples(vals);
    return; 
  }

  /** combines fields
  *
  * @param fields[] array of fields
  *
  * @return the new Field
  */

  public static Field combine(Field[] fields)
             throws VisADException, RemoteException {
    return (FieldImpl.combine(fields) );

  }

  /** extracts a component of the Field
  *
  * @param data the field with multiple range componenents
  * @param t the MathType of the field to extract
  *
  * @return the new Field
  *
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
  * @parameter data is the Field Data object
  * @param factor is the domain component Type to factor out
  *
  * @return the new Field
  *
  */
  public static Field domainFactor(Field data, RealType factor) 
             throws VisADException, RemoteException {
    return ((FieldImpl)data).domainFactor(factor);
  }


  /** factors out the given domain component (by index)
  * and creates a new data object.  See above.
  *
  * @parameter data is the Field Data object
  * @parameter comp is the domain component index
  *
  * @return the new Field
  */
  public static Field domainFactor(Field data, int comp) 
             throws VisADException, RemoteException {

    RealType mt = (RealType) (
            (RealTupleType) (
            (FunctionType)data.getType()).getDomain()).getComponent(comp);

    return ((FieldImpl)data).domainFactor(mt);
  }

  /** creates a VisAD Data by evaluating the Field at the
  * point given in the domain.
  *
  * @param data is the field
  * @param domain is the Real domain where the field should be evaluated
  *
  */

  public static Data evaluate(Field data, Real domain) 
             throws VisADException, RemoteException {
    return( data.evaluate(domain) );
  }

  public static Data evaluate(Field data, double domain) 
             throws VisADException, RemoteException {
    return( data.evaluate(new Real(domain) ) );
  }


  /** creates a VisAD MathType from the given string
  *
  * @param s is the string describing the names in
  * the form:  (x,y)->(a)  for a Field.
  *
  * Forms allowed:
  * "Foo" will make and return a RealType
  * "(Foo)" makes a RealType and returns a RealTupleType
  * "Foo,Bar" will make two RealTypes and return a RealTupleType
  * "(Foo,Bar)" does the same thing
  * "(Foo,Bar)->val" makes 3 RealTypes and returns a FunctionType
  * (use getDomainType(type) and getRangeType(type) to get the parts
  *
  * @return the MathType
  *
  */
  public static MathType makeType(String s) 
             throws VisADException, RemoteException {
    String ss = s.trim();

    if ((ss.indexOf(",") != -1 || ss.indexOf(">") != -1 ) && 
                 (!ss.startsWith("(") || !ss.endsWith(")") ) ) {
      ss = "(" + s.trim() + ")";
    }

    return MathType.stringToType(ss);
  }

  /** make a MathType with a Coordinate System. This is just
  * a short-hand for visad.RealTupleType(RealType[], CS, Set)
  *
  * @param s is an array of names for (or of) the RealType
  * @param c is a CoordinateSystem
  *
  * @return RealTupleType of the input RealTypes and CoordinateSystem
  */
  public static RealTupleType makeType(String[] s, CoordinateSystem c) 
             throws VisADException, RemoteException {
    RealType[] rt = new RealType[s.length];
    for (int i=0; i< s.length; i++) {
      rt[i] = visad.RealType.getRealType(s[i]);
    }

    return new visad.RealTupleType(rt, c, null);
  }


  /** make or get the RealType corresponding to the name; if
  * none exists, make one and return it.
  *
  * @param name is the name of the RealType type.
  *
  */
  public static RealType makeRealType(String name) {
    return (visad.RealType.getRealType(name));
  }

  public static RealType getRealType(String name) {
    return (visad.RealType.getRealType(name));
  }

  /** make or get the RealType corresponding to the name; if
  * none exists, make one and return it.
  *
  * @param name is the name of the RealType type.
  * @param unit is the new Unit to associate with this (must
  * be compatible with any existing Unit)
  *
  */
  public static RealType makeRealType(String name, Unit u) {
    return (visad.RealType.getRealType(name, u));
  }

  public static RealType getRealType(String name, Unit u) {
    return (visad.RealType.getRealType(name, u));
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

  /** Turn on/off the axes labels & scales on a Display
  *
  * @param d the DisplayImpl to address
  * @param onoff whether to turn the axes labels on (true)
  *
  */
  public static void showAxesScales(DisplayImpl d, boolean on)
             throws VisADException, RemoteException {
             d.getGraphicsModeControl().setScaleEnable(on);
  }

  /** Set the Label to be used for the axes
  *
  * @param sm the array of ScalarMaps
  * @param labels the array of strings to use for labels
  */
  public static void setAxesScalesLabel(ScalarMap [] sm, String[] labels)
             throws VisADException, RemoteException {

      if (sm.length != labels.length) {
        throw new VisADException("number of ScalarMaps must match number of labels");
      }
      for (int i=0; i<sm.length; i++) {
        AxisScale scale = sm[i].getAxisScale();
        if (scale != null) {
          scale.setLabel(labels[i]);
        }
      }

   }


  /** Set the font to be used for the axes labels and scales
  *
  * @param sm the array of ScalarMaps
  * @param f the java.awt.Font to use
  */
  public static void setAxesScalesFont(ScalarMap [] sm, Font f) 
             throws VisADException, RemoteException {

      for (int i=0; i<sm.length; i++) {
        AxisScale scale = sm[i].getAxisScale();
        if (scale != null) {
          scale.setFont(f);
        }
      }
     
   } 

   public static UnionSet makePairedLines(MathType mt, double[][] points) 
             throws VisADException, RemoteException {
     int dim = points.length;
     int len = points[0].length;
     UnionSet us = null;
     if (dim == 2) {
       float[][] samples = new float[2][2];
       Gridded2DSet[] gs2 = new Gridded2DSet[len/2];
       for (int k=0; k < len; k=k+2) {
         samples[0][0] = (float) points[0][k];
         samples[0][1] = (float) points[0][k+1];
         samples[1][0] = (float) points[1][k];
         samples[1][1] = (float) points[1][k+1];
         gs2[k/2] = new Gridded2DSet(mt, samples, 2);
       }
       us = new UnionSet(gs2);

     } else if (dim == 3) {
       float[][] samples = new float[3][2];
       Gridded3DSet[] gs3 = new Gridded3DSet[len/2];
       for (int k=0; k < len; k=k+2) {
         samples[0][0] = (float) points[0][k];
         samples[0][1] = (float) points[0][k+1];
         samples[1][0] = (float) points[1][k];
         samples[1][1] = (float) points[1][k+1];
         samples[2][0] = (float) points[2][k];
         samples[2][1] = (float) points[2][k+1];
         gs3[k/2] = new Gridded3DSet(mt, samples, 3);
       }
       us = new UnionSet(gs3);
     }

     return us;
   }
  
  /** helper method for the dump(Data|Math)Type() methods
  *   this will list both the MathType and DataType information
  *   to stdout.
  *
  * @param d is the Data object
  *
  */
  public static void dumpTypes(Data d) 
             throws VisADException, RemoteException {
      MathType t = d.getType();
      visad.jmet.DumpType.dumpMathType(t);
      System.out.println("- - - - - - - - - - - - - - - - - - - - - - - ");
      System.out.println("DataType analysis...");
      visad.jmet.DumpType.dumpDataType(d);
  }

  public static ByteArrayOutputStream sdumpTypes(Data d) 
             throws VisADException, RemoteException {
      MathType t = d.getType();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      visad.jmet.DumpType.dumpMathType(t,bos);
      visad.jmet.DumpType.dumpDataType(d,bos);
      return bos;
  }

  /** helper method for dumpMathType() only
  * This just dumps out the MathType of the Data object.
  */
  public static void dumpType(Data d) 
             throws VisADException, RemoteException {
      MathType t = d.getType();
      visad.jmet.DumpType.dumpMathType(t);
  }

  public static ByteArrayOutputStream sdumpType(Data d) 
             throws VisADException, RemoteException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      MathType t = d.getType();
      visad.jmet.DumpType.dumpMathType(t,bos);
      return bos;
  }

  public static 
       visad.data.mcidas.PointDataAdapter getPointDataAdapter(String request) 
             throws VisADException, RemoteException {
       return (new visad.data.mcidas.PointDataAdapter(request));
  }

  /** helper method to read netcdf files with possible factor
  */

  public static Data getNetcdfData(String filename) 
             throws VisADException, RemoteException, IOException {
    return getNetcdfData(filename, null);
  }

  public static Data getNetcdfData(String filename, String factor) 
             throws VisADException, RemoteException, IOException {

    NetcdfFile nf = new NetcdfFile(filename, true);
    DefaultView dv = new DefaultView(nf,QuantityDBManager.instance(),true);
    if (factor != null) {
      TreeSet ts = new TreeSet();
      ts.add(factor);
      dv.setOuterDimensionNameSet(ts);
    }
    NetcdfAdapter na=new NetcdfAdapter(dv);
    return na.getData();
  }

  /** helper method for visad.ScalarMap.getScale
  */

  public static double[][] getScale(ScalarMap smap) {
    double[] so      = new double[2];
    double[] data    = new double[2];
    double[] display = new double[2];

    smap.getScale(so, data, display);

    return new double[][] {so, data, display};
  }

}

