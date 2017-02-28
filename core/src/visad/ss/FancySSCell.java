//
// FancySSCell.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

package visad.ss;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import javax.swing.*;
import javax.swing.border.*;
import visad.*;
import visad.data.*;
import visad.formula.FormulaManager;
import visad.util.*;

/**
 * FancySSCell is an extension of BasicSSCell with extra options, such
 * as a file loader dialog and a dialog to set up ScalarMaps.
 * It provides an example of GUI extensions to BasicSSCell.
 */
public class FancySSCell extends BasicSSCell implements SSCellListener {

  // --- CONSTANTS ---

  /**
   * Dark red.
   */
  public static final Color DARK_RED = new Color(0.5f, 0f, 0f);

  /**
   * Dark green.
   */
  public static final Color DARK_GREEN = new Color(0f, 0.5f, 0f);

  /**
   * Dark blue.
   */
  public static final Color DARK_BLUE = new Color(0f, 0f, 0.5f);

  /**
   * Dark yellow.
   */
  public static final Color DARK_YELLOW = new Color(0.5f, 0.5f, 0f);

  /**
   * Dark purple.
   */
  public static final Color DARK_PURPLE = new Color(0.5f, 0f, 0.5f);

  /**
   * Dark cyan.
   */
  public static final Color DARK_CYAN = new Color(0f, 0.5f, 0.5f);

  /**
   * Border for cell with no data.
   */
  public static final Border B_EMPTY = new LineBorder(Color.gray, 3);

  /**
   * Border for selected cell.
   */
  public static final Border B_HIGHLIGHT = new LineBorder(Color.yellow, 3);

  /**
   * Border for cell with data from an unknown source.
   */
  public static final Border B_UNKNOWN = new LineBorder(DARK_PURPLE, 3);

  /**
   * Border for cell with data set directly.
   */
  public static final Border B_DIRECT = new LineBorder(DARK_CYAN, 3);

  /**
   * Border for cell with file or URL.
   */
  public static final Border B_URL = new LineBorder(DARK_GREEN, 3);

  /**
   * Border for cell with formula.
   */
  public static final Border B_FORMULA = new LineBorder(DARK_RED, 3);

  /**
   * Border for cell with RMI address.
   */
  public static final Border B_RMI = new LineBorder(DARK_BLUE, 3);

  /**
   * Border for cell with data from a remote source.
   */
  public static final Border B_REMOTE = new LineBorder(DARK_YELLOW, 3);

  /**
   * Border for cell with multiple data objects.
   */
  public static final Border B_MULTI = new CompoundBorder(
    new CompoundBorder(new LineBorder(DARK_RED), new LineBorder(DARK_GREEN)),
    new LineBorder(DARK_BLUE));


  // --- FIELDS ---

  /**
   * File chooser for loading and saving data. This variable is static so
   * that the directory is remembered between each load or save command.
   */
  protected static JFileChooser FileBox = Util.getVisADFileChooser();

  /**
   * Parent frame.
   */
  protected Frame Parent;

  /**
   * Associated JFrame, for use with VisAD Controls.
   */
  protected JFrame WidgetFrame;

  /**
   * Whether this cell is selected.
   */
  protected boolean Selected = false;

  /**
   * Whether this cell should auto-switch to 3-D.
   */
  protected boolean AutoSwitch = true;

  /**
   * Whether this cell should auto-detect mappings for data.
   */
  protected boolean AutoDetect = true;

  /**
   * Whether this cell should auto-display its widget frame.
   */
  protected boolean AutoShowControls = true;

  /**
   * Lock object for mapping auto-detection notification.
   */
  private Object MapLock = new Object();

  /**
   * Counter for mapping auto-detection notification.
   */
  private int MapCount = 0;


  // --- CONSTRUCTORS ---

  /**
   * Constructs a new FancySSCell with the given name.
   */
  public FancySSCell(String name) throws VisADException, RemoteException {
    this(name, null, null, false, null, null);
  }

  /**
   * Constructs a new FancySSCell with the given name and parent Frame.
   */
  public FancySSCell(String name, Frame parent)
    throws VisADException, RemoteException
  {
    this(name, null, null, false, null, parent);
  }

  /**
   * Constructs a new FancySSCell with the given name, formula manager,
   * and parent Frame.
   */
  public FancySSCell(String name, FormulaManager fman, Frame parent)
    throws VisADException, RemoteException
  {
    this(name, fman, null, false, null, parent);
  }

  /**
   * Constructs a new FancySSCell with the given name, remote server,
   * and parent Frame.
   */
  public FancySSCell(String name, RemoteServer rs, Frame parent)
    throws VisADException, RemoteException
  {
    this(name, null, rs, false, null, parent);
  }

  /**
   * Constructs a new FancySSCell with the given name, save string, and
   * parent Frame.
   */
  public FancySSCell(String name, String save, Frame parent)
    throws VisADException, RemoteException
  {
    this(name, null, null, false, save, parent);
  }

  /**
   * Constructs a new FancySSCell with the given name, formula manager,
   * remote server, save string, and parent Frame.
   */
  public FancySSCell(String name, FormulaManager fman, RemoteServer rs,
    String save, Frame parent) throws VisADException, RemoteException
  {
    this(name, fman, rs, false, save, parent);
  }

  /**
   * Constructs a new, possibly slaved, FancySSCell with the given name,
   * formula manager, remote server, save string, and parent Frame.
   */
  public FancySSCell(String name, FormulaManager fman, RemoteServer rs,
    boolean slave, String save, Frame parent) throws VisADException,
    RemoteException
  {
    super(name, fman, rs, slave, save);
    Parent = parent;
    WidgetFrame = new JFrame("Controls (" + Name + ")");
    Util.invoke(false, DEBUG, new Runnable() {
      public void run() {
        setHighlighted(false);
      }
    });
    addSSCellListener(this);
  }


  // --- DATA MANAGEMENT ---

  /**
   * Removes the Data object corresponding to the
   * given variable name from this cell.
   */
  public void removeData(String varName)
    throws VisADException, RemoteException
  {
    super.removeData(varName);
    if (CellData.size() == 0) clearWidgetFrame();
  }

  /**
   * Imports a data object from the given source of unknown type,
   * in a separate thread.
   */
  public void loadDataSource(String source) {
    loadDataSource(source, UNKNOWN_SOURCE);
  }

  /**
   * Imports a data object from the given source of the specified type,
   * in a separate thread.
   */
  public void loadDataSource(String source, int type) {
    final String fsource = source;
    final int ftype = type;
    final BasicSSCell cell = this;
    Runnable load = new Runnable() {
      public void run() {
        try {
          cell.addDataSource(fsource, ftype);
          if (!cell.hasData()) {
            JOptionPane.showMessageDialog(Parent, "Unable to import data",
              "Error importing data", JOptionPane.ERROR_MESSAGE);
          }
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
          JOptionPane.showMessageDialog(Parent, exc.getMessage(),
            "Error importing data", JOptionPane.ERROR_MESSAGE);
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
          JOptionPane.showMessageDialog(Parent, exc.getMessage(),
            "Error importing data", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    Thread t = new Thread(load);
    t.start();
  }

  /**
   * Imports data from a file selected by the user.
   */
  public void loadDataDialog() {
    // get file name from file dialog
    FileBox.setDialogType(JFileChooser.OPEN_DIALOG);
    if (FileBox.showOpenDialog(Parent) != JFileChooser.APPROVE_OPTION) return;

    // make sure file exists
    File f = FileBox.getSelectedFile();
    if (!f.exists()) {
      JOptionPane.showMessageDialog(Parent, f.getName() + " does not exist",
        "Cannot load file", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // load file
    loadDataSource(f.getAbsolutePath(), URL_SOURCE);
  }

  /**
   * Pops up a dialog box for user to select file where data will be saved.
   */
  private File getSaveFile() {
    if (!hasData()) {
      JOptionPane.showMessageDialog(Parent, "This cell is empty.",
        "Nothing to save", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    // get file name from file dialog
    FileBox.setDialogType(JFileChooser.SAVE_DIALOG);
    if (FileBox.showSaveDialog(Parent) != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    return FileBox.getSelectedFile();
  }

  /**
   * Saves a Data object to a file selected by the user,
   * using the given data form.
   */
  public void saveDataDialog(String varName, Form saveForm) {
    // get file where data should be saved
    final File file = getSaveFile();
    if (file == null) return;

    // start new thread to save the file
    final BasicSSCell cell = this;
    final String fname = varName;
    final Form form = saveForm;
    Runnable saveFile = new Runnable() {
      public void run() {
        String msg = "Could not save the dataset to the file " +
          "\"" + file.getName() + "\" in " + form.getName() + " format. ";
        try {
          cell.saveData(fname, file.getAbsolutePath(), form);
        }
        catch (BadFormException exc) {
          if (DEBUG) exc.printStackTrace();
          msg = msg + "An error occurred: " + exc.getMessage();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
            JOptionPane.ERROR_MESSAGE);
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
          msg = msg + "A remote error occurred: " + exc.getMessage();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
            JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException exc) {
          if (DEBUG) exc.printStackTrace();
          msg = msg + "An I/O error occurred: " + exc.getMessage();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
            JOptionPane.ERROR_MESSAGE);
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
          msg = msg + "An error occurred: " + exc.getMessage();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
            JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    Thread t = new Thread(saveFile);
    t.start();
  }

  /**
   * Blocks until mapping auto-detection is complete.
   */
  public void waitForMaps() {
    synchronized (MapLock) {
      while (MapCount > 0) {
        try { MapLock.wait(); }
        catch (InterruptedException exc) {
          if (DEBUG && DEBUG_LEVEL >= 3) exc.printStackTrace();
        }
      }
    }
  }

  /**
   * Does the work of adding the given DataReference,
   * from the given source of the specified type.
   *
   * @return The newly created SSCellData object.
   */
  protected SSCellData addReferenceImpl(int id, DataReferenceImpl ref,
    ConstantMap[] cmaps, String source, int type, boolean notify,
    boolean checkErrors) throws VisADException, RemoteException
  {
    synchronized (MapLock) { MapCount++; }
    return super.addReferenceImpl(id, ref, cmaps,
      source, type, notify, checkErrors);
  }

  // --- DISPLAY MANAGEMENT ---

  /**
   * Whether the mapping dialog is currently being displayed.
   */
  private boolean mapDialogUp = false;

  /**
   * Asks user to confirm clearing the cell if any other cell depends on it.
   */
  public boolean confirmClear() {
    if (othersDepend()) {
      int ans = JOptionPane.showConfirmDialog(null, "Other cells depend on " +
        "this cell. Are you sure you want to clear it?", "Warning",
        JOptionPane.YES_NO_OPTION);
      if (ans != JOptionPane.YES_OPTION) return false;
    }
    return true;
  }

  /**
   * Clears the cell if no other cell depends on it; otherwise, ask the
   * user &quot;Are you sure?&quot; return true if the cell was cleared.
   */
  public boolean smartClear() throws VisADException, RemoteException {
    if (confirmClear()) {
      clearWidgetFrame();
      clearCell();
      return true;
    }
    else return false;
  }

  /**
   * Permanently destroy this cell, asking user for confirmation first
   * if other cells depend on it; return true if the cell was destroyed.
   */
  public boolean smartDestroy() throws VisADException, RemoteException {
    if (confirmClear()) {
      clearWidgetFrame();
      destroyCell();
      return true;
    }
    else return false;
  }

  /**
   * Switches to 3-D mode if necessary and available.
   */
  public void setMapsAuto(ScalarMap[] maps)
    throws VisADException, RemoteException
  {
    if (AutoSwitch && maps != null) {
      int need = 0;
      for (int i=0; i<maps.length; i++) {
        DisplayRealType drt = maps[i].getDisplayScalar();
        if (drt.equals(Display.ZAxis) || drt.equals(Display.Latitude)) {
          need = 2;
        }
        if (drt.equals(Display.Alpha) || drt.equals(Display.RGBA)) {
          if (need < 1) need = 1;
        }
      }
      // switch to Java3D mode if needed
      if (need == 2) setDimension(JAVA3D_3D);
      else if (need == 1 && Dim != JAVA3D_3D) setDimension(JAVA3D_2D);
    }
    setMaps(maps);
  }

  /**
   * Sets the ScalarMaps for this cell and creates needed control widgets.
   */
  public void setMaps(ScalarMap[] maps)
    throws VisADException, RemoteException
  {
    super.setMaps(maps);
    if (WidgetFrame.isVisible() || AutoShowControls) showWidgetFrame();
  }

  /**
   * Lets the user specify mappings between this cell's data and display.
   */
  public void addMapDialog() {
    if (mapDialogUp) return;
    mapDialogUp = true;

    try {
      // check whether this cell has data
      if (getDataCount() == 0) {
        JOptionPane.showMessageDialog(Parent, "This cell has no data",
          "FancySSCell error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // get mappings from mapping dialog
      MappingDialog mapDialog = new MappingDialog(Parent, getData(), getMaps(),
        Dim != JAVA2D_2D || AutoSwitch, Dim == JAVA3D_3D || AutoSwitch);
      mapDialog.display();

      // make sure user did not cancel the operation
      if (!mapDialog.okPressed()) return;

      // set up new mappings
      try {
        setMapsAuto(mapDialog.getMaps());
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
        JOptionPane.showMessageDialog(Parent,
          "This combination of mappings is not valid: " + exc.getMessage(),
          "Cannot assign mappings", JOptionPane.ERROR_MESSAGE);
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
        JOptionPane.showMessageDialog(Parent,
          "This combination of mappings is not valid: " + exc.getMessage(),
          "Cannot assign mappings", JOptionPane.ERROR_MESSAGE);
      }
    }
    finally {
      mapDialogUp = false;
    }
  }

  /**
   * Guesses a good set of mappings for this cell's data and applies them.
   */
  protected void autoDetectMappings() throws VisADException, RemoteException {
    if (AutoDetect) {
      boolean allow3d = (Dim != JAVA2D_2D || AutoSwitch);

      // guess mappings for Data objects
      int len = getDataCount();
      Data[] data = getData();
      MathType[] types = new MathType[len];
      for (int i=0; i<len; i++) {
        Data d = data[i];
        types[i] = (d == null ? null : d.getType());
      }
      ScalarMap[] maps = DataUtility.guessMaps(types, allow3d);

      // apply the mappings
      setMapsAuto(maps);
    }

    // notify waitForMaps() method
    synchronized (MapLock) {
      MapCount--;
      MapLock.notifyAll();
    }
  }


  // --- GUI MANAGEMENT ---

  /**
   * Shows the widgets for altering controls (if there are any).
   */
  public synchronized void showWidgetFrame() {
    if (VDisplay == null || CellData.size() == 0) return;
    Util.invoke(false, DEBUG, new Runnable() {
      public void run() {
        Container jc = VDisplay.getWidgetPanel();
        if (jc != null && jc.getComponentCount() > 0) {
          WidgetFrame.setContentPane(jc);
          WidgetFrame.pack();
          WidgetFrame.setVisible(true);
        }
      }
    });
  }

  /**
   * Hides the widgets for altering controls.
   */
  public void hideWidgetFrame() {
    Util.invoke(false, DEBUG, new Runnable() {
      public void run() {
        WidgetFrame.setVisible(false);
      }
    });
  }

  /**
   * Specifies whether the FancySSCell has a border.
   */
  public void setBorderEnabled(boolean value) {
    if (value) setSelected(Selected);
    else setBorder(null);
  }

  /**
   * Specifies whether the FancySSCell has a highlighted border.
   */
  public void setSelected(boolean value) {
    if (Selected == value) return;
    Selected = value;
    Util.invoke(false, DEBUG, new Runnable() {
      public void run() {
        setHighlighted(Selected);
        if (!Selected) hideWidgetFrame();
        else if (AutoShowControls) showWidgetFrame();
        refresh();
      }
    });
  }

  /**
   * Specifies whether this FancySSCell should auto-switch to 3-D.
   */
  public synchronized void setAutoSwitch(boolean value) {
    AutoSwitch = value;
  }

  /**
   * Specifies whether this FancySSCell should auto-detect its mappings.
   */
  public synchronized void setAutoDetect(boolean value) {
    AutoDetect = value;
  }

  /**
   * Specifies whether this FancySSCell should auto-display its widget frame.
   */
  public synchronized void setAutoShowControls(boolean value) {
    AutoShowControls = value;
  }

  /**
   * Removes all widgets for altering controls and hide widget frame.
   */
  private void clearWidgetFrame() {
    Util.invoke(false, DEBUG, new Runnable() {
      public void run() {
        WidgetFrame.setVisible(false);
        JPanel pane = new JPanel();
        pane.add(new JLabel("No controls"), "CENTER");
        WidgetFrame.setContentPane(pane);
      }
    });
  }

  /**
   * Sets whether this cell is highlighted.
   */
  private void setHighlighted(boolean hl) {
    if (hl) setBorder(B_HIGHLIGHT);
    else {
      int dataCount = getDataCount();
      if (dataCount == 0) {
        // no datasets
        setBorder(B_EMPTY);
      }
      else if (dataCount == 1) {
        // single dataset
        int type = getDataSourceType(getFirstVariableName());
        if (type == DIRECT_SOURCE) setBorder(B_DIRECT);
        else if (type == URL_SOURCE) setBorder(B_URL);
        else if (type == FORMULA_SOURCE) setBorder(B_FORMULA);
        else if (type == RMI_SOURCE) setBorder(B_RMI);
        else if (type == REMOTE_SOURCE) setBorder(B_REMOTE);
        else setBorder(B_UNKNOWN);
      }
      else {
        // multiple datasets
        setBorder(B_MULTI);
      }
    }
  }


  // --- EVENT HANDLING ---

  /**
   * Re-detects mappings when this cell's data changes.
   */
  public void ssCellChanged(SSCellChangeEvent e) {
    int type = e.getChangeType();
    if (type == SSCellChangeEvent.DATA_CHANGE) {
      // refresh border color
      Util.invoke(false, DEBUG, new Runnable() {
        public void run() {
          setHighlighted(Selected);
        }
      });

      if (!IsRemote) {
        // attempt to auto-detect mappings for new data
        Data value = null;
        try {
          value = (Data) fm.getThing(e.getVariableName());
        }
        catch (ClassCastException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        try {
          if (value != null) autoDetectMappings();
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
    }
    else if (type == SSCellChangeEvent.DISPLAY_CHANGE) {
      if (IsRemote) {
        // reconstruct controls for cloned display
        if (AutoShowControls) showWidgetFrame();
      }
    }
  }


  // --- MISCELLANEOUS ---

  /**
   * Captures display image and saves to a file selected by the user,
   * in JPEG format.
   */
  public void captureDialog() {
    // get file where captured image should be saved
    final File f = getSaveFile();
    if (f == null) return;

    // start new thread to capture image and save it to the file
    final BasicSSCell cell = this;
    Runnable captureImage = new Runnable() {
      public void run() {
        String msg = "Could not save image snapshot to file \"" + f.getName() +
          "\" in JPEG format. ";
        try {
          cell.captureImage(f);
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
          msg = msg + "An error occurred: " + exc.getMessage();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
            JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException exc) {
          if (DEBUG) exc.printStackTrace();
          msg = msg + "An I/O error occurred: " + exc.getMessage();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
            JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    Thread t = new Thread(captureImage);
    t.start();
  }


  // --- ACCESSORS ---

  /**
   * Returns whether this FancySSCell auto-switches to 3-D.
   */
  public boolean getAutoSwitch() {
    return AutoSwitch;
  }

  /**
   * Returns whether this FancySSCell auto-detects its mappings.
   */
  public boolean getAutoDetect() {
    return AutoDetect;
  }

  /**
   * Returns whether this FancySSCell auto-displays its widget frame.
   */
  public boolean getAutoShowControls() {
    return AutoShowControls;
  }

  /**
   * Returns whether the cell has any associated controls.
   */
  public boolean hasControls() {
    if (VDisplay == null || CellData.size() == 0) return false;
    Container jc = VDisplay.getWidgetPanel();
    if (jc == null) return false;
    return (jc.getComponentCount() > 0);
  }


  // --- DEPRECATED ---

  /**
   * @deprecated Use loadDataSource(String, RMI_SOURCE) instead.
   */
  public void loadDataRMI(String s) {
    loadDataSource(s, RMI_SOURCE);
  }

  /**
   * @deprecated Use loadDataSource(String, URL_SOURCE) instead.
   */
  public synchronized void loadDataString(String s) {
    loadDataSource(s, URL_SOURCE);
  }

  /**
   * @deprecated Use loadDataSource(String, URL_SOURCE) instead.
   */
  public void loadDataURL(URL u) {
    loadDataSource(u.toString(), URL_SOURCE);
  }

  /**
   * @deprecated Use saveDataDialog(String, Form) instead.
   */
  public void saveDataDialog(boolean netcdf) {
    try {
      Form f;
      if (netcdf) f = new visad.data.netcdf.Plain();
      else f = new visad.data.visad.VisADForm();
      saveDataDialog(getFirstVariableName(), f);
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  /**
   * @deprecated Use saveDataDialog(String, Form) instead.
   */
  public void saveDataDialog(Form saveForm) {
    saveDataDialog(getFirstVariableName(), saveForm);
  }

}
