//
// FancySSCell.java
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

package visad.ss;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import visad.*;
import visad.data.*;
import visad.formula.FormulaManager;
import visad.util.*;

/** FancySSCell is an extension of BasicSSCell with extra options, such
    as a file loader dialog and a dialog to set up ScalarMaps.  It
    provides an example of GUI extensions to BasicSSCell.<P> */
public class FancySSCell extends BasicSSCell implements SSCellListener {

  /** border for cell with no data */
  static final Border B_EMPTY = new LineBorder(Color.gray, 3);

  /** border for selected cell */
  static final Border B_HIGH = new LineBorder(Color.yellow, 3);

  /** border for cell with formula */
  static final Border B_FORM = new LineBorder(new Color(0.5f, 0f, 0f), 3);

  /** border for cell with RMI address */
  static final Border B_RMI = new LineBorder(new Color(0f, 0f, 0.5f), 3);

  /** border for cell with file or URL */
  static final Border B_URL = new LineBorder(new Color(0f, 0.5f, 0f), 3);

  /** this variable is static so that the previous directory is remembered */
  protected static JFileChooser FileBox = Util.getVisADFileChooser();


  /** parent frame */
  protected Frame Parent;

  /** associated JFrame, for use with VisAD Controls */
  protected JFrame WidgetFrame;

  /** whether this cell is selected */
  protected boolean Selected = false;

  /** whether this cell should auto-switch to 3-D */
  protected boolean AutoSwitch = true;

  /** whether this cell should auto-detect mappings for data */
  protected boolean AutoDetect = true;

  /** whether this cell should auto-display its widget frame */
  protected boolean AutoShowControls = true;


  /** construct a new FancySSCell with the given name */
  public FancySSCell(String name) throws VisADException, RemoteException {
    this(name, null, null, false, null, null);
  }

  /** construct a new FancySSCell with the given name and parent Frame */
  public FancySSCell(String name, Frame parent)
    throws VisADException, RemoteException
  {
    this(name, null, null, false, null, parent);
  }

  /** construct a new FancySSCell with the given name, formula manager,
      and parent Frame */
  public FancySSCell(String name, FormulaManager fman, Frame parent)
    throws VisADException, RemoteException
  {
    this(name, fman, null, false, null, parent);
  }

  /** construct a new FancySSCell with the given name, remote server,
      and parent Frame */
  public FancySSCell(String name, RemoteServer rs, Frame parent)
    throws VisADException, RemoteException
  {
    this(name, null, rs, false, null, parent);
  }

  /** construct a new FancySSCell with the given name, save string, and
      parent Frame */
  public FancySSCell(String name, String save, Frame parent)
    throws VisADException, RemoteException
  {
    this(name, null, null, false, save, parent);
  }

  /** construct a new FancySSCell with the given name, formula manager,
      remote server, save string, and parent Frame */
  public FancySSCell(String name, FormulaManager fman, RemoteServer rs,
    String save, Frame parent) throws VisADException, RemoteException
  {
    this(name, fman, rs, false, save, parent);
  }

  /** construct a new, possibly slaved, FancySSCell with the given name,
      formula manager, remote server, save string, and parent Frame */
  public FancySSCell(String name, FormulaManager fman, RemoteServer rs,
    boolean slave, String save, Frame parent) throws VisADException,
    RemoteException
  {
    super(name, fman, rs, slave, save);
    Parent = parent;
    setHighlighted(false);
    addSSCellChangeListener(this);
  }

  /** re-auto-detect mappings when this cell's data changes */
  public void ssCellChanged(SSCellChangeEvent e) {
    int type = e.getChangeType();
    if (type == SSCellChangeEvent.DATA_CHANGE) {
      // refresh border color
      setHighlighted(Selected);

      if (!IsRemote) {
        // attempt to auto-detect mappings for new data
        Data value = null;
        try {
          value = (Data) fm.getThing(Name);
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

  /** switch to 3-D mode if necessary and available, then call setMaps() */
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
      if (need == 2) setDimension(false, false);
      else if (need == 1 && Dim != JAVA3D_3D) setDimension(true, false);
    }
    setMaps(maps);
  }

  /** set the ScalarMaps for this cell and creates needed control widgets */
  public void setMaps(ScalarMap[] maps)
    throws VisADException, RemoteException
  {
    super.setMaps(maps);
    if (WidgetFrame != null && WidgetFrame.isVisible() || AutoShowControls) {
      showWidgetFrame();
    }
  }

  /** show the widgets for altering controls (if there are any) */
  public synchronized void showWidgetFrame() {
    if (VDisplay == null) return;
    if (WidgetFrame == null) {
      WidgetFrame = new JFrame("Controls (" + Name + ")");
    }
    synchronized (WidgetFrame) {
      Container jc = VDisplay.getWidgetPanel();
      if (jc != null && jc.getComponentCount() > 0) {
        WidgetFrame.setContentPane(jc);
        WidgetFrame.pack();
        WidgetFrame.setVisible(true);
      }
    }
  }

  /** hide the widgets for altering controls */
  public void hideWidgetFrame() {
    if (WidgetFrame != null) WidgetFrame.setVisible(false);
  }

  /** whether the cell has any associated controls */
  public boolean hasControls() {
    if (VDisplay == null) return false;
    Container jc = VDisplay.getWidgetPanel();
    if (jc == null) return false;
    return (jc.getComponentCount() > 0);
  }

  /** remove all widgets for altering controls and hide widget frame */
  private void clearWidgetFrame() {
    hideWidgetFrame();
    WidgetFrame = null;
  }

  /** guess a good set of mappings for this cell's Data and apply them */
  void autoDetectMappings() throws VisADException, RemoteException {
    if (AutoDetect) {
      Data data = null;
      data = getData();
      MathType mt = null;
      try {
        if (data != null) mt = data.getType();
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      if (mt != null) {
        boolean allow3D = (Dim != JAVA2D_2D || AutoSwitch);
        setMapsAuto(mt.guessMaps(allow3D));
      }
    }
  }

  /** set this cell's formula */
  public void setFormula(String f) throws VisADException, RemoteException {
    super.setFormula(f);
  }

  /** specify whether the FancySSCell has a highlighted border */
  public void setSelected(boolean value) {
    if (Selected == value) return;
    Selected = value;
    setHighlighted(Selected);
    if (!Selected) hideWidgetFrame();
    else if (AutoShowControls) showWidgetFrame();
    refresh();
  }

  /** specify whether this FancySSCell should auto-switch to 3-D */
  public synchronized void setAutoSwitch(boolean value) {
    AutoSwitch = value;
  }

  /** return whether this FancySSCell auto-switches to 3-D */
  public boolean getAutoSwitch() {
    return AutoSwitch;
  }

  /** specify whether this FancySSCell should auto-detect its mappings */
  public synchronized void setAutoDetect(boolean value) {
    AutoDetect = value;
  }

  /** return whether this FancySSCell auto-detects its mappings */
  public boolean getAutoDetect() {
    return AutoDetect;
  }

  /** specify whether this FancySSCell should auto-display its widget frame */
  public synchronized void setAutoShowControls(boolean value) {
    AutoShowControls = value;
  }

  /** return whether this FancySSCell auto-displays its widget frame */
  public boolean getAutoShowControls() {
    return AutoShowControls;
  }

  /** ask user to confirm clearing the cell if any other cell depends on it */
  public boolean confirmClear() {
    if (othersDepend()) {
      int ans = JOptionPane.showConfirmDialog(null, "Other cells depend on " +
        "this cell. Are you sure you want to clear it?", "Warning",
        JOptionPane.YES_NO_OPTION);
      if (ans != JOptionPane.YES_OPTION) return false;
    }
    return true;
  }

  /** clear the cell if no other cell depends on it; otherwise, ask the
      user &quot;Are you sure?&quot; return true if the cell was cleared */
  public boolean smartClear() throws VisADException, RemoteException {
    if (confirmClear()) {
      clearWidgetFrame();
      clearCell();
      return true;
    }
    else return false;
  }

  /** permanently destroy this cell, asking user for confirmation first
      if other cells depend on it; return true if the cell was destroyed */
  public boolean smartDestroy() throws VisADException, RemoteException {
    if (confirmClear()) {
      clearWidgetFrame();
      destroyCell();
      return true;
    }
    else return false;
  }

  /** used by addMapDialog */
  private boolean mapDialogUp = false;

  /** let the user create ScalarMaps from the current SSPanel's Data
      to its Display */
  public void addMapDialog() {
    if (mapDialogUp) return;
    mapDialogUp = true;

    try {
      // check whether this cell has data
      Data data = getData();
      if (data == null) {
        JOptionPane.showMessageDialog(Parent, "This cell has no data",
          "FancySSCell error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // get mappings from mapping dialog
      MappingDialog mapDialog = new MappingDialog(Parent, data, getMaps(),
                                Dim != JAVA2D_2D || AutoSwitch,
                                Dim == JAVA3D_3D || AutoSwitch);
      mapDialog.display();

      // make sure user did not cancel the operation
      if (!mapDialog.Confirm) return;

      // set up new mappings
      try {
        setMapsAuto(mapDialog.ScalarMaps);
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

  /** import a data object from a given URL, in a separate thread */
  public void loadDataURL(URL u) {
    loadDataString(u.toString());
  }

  /** import a data object from the given string, in a separate thread */
  public synchronized void loadDataString(String s) {
    final String file = s;
    final BasicSSCell cell = this;
    Runnable loadFile = new Runnable() {
      public void run() {
        try {
          cell.loadData(file);
          if (!cell.hasData() && !IsRemote) {
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
    Thread t = new Thread(loadFile);
    t.start();
  }

  /** import a data object from a server using RMI, in a separate thread */
  public void loadDataRMI(String s) {
    final String sname = s;
    Runnable loadRMI = new Runnable() {
      public void run() {
        try {
          loadRMI(sname);
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
          JOptionPane.showMessageDialog(Parent, exc.getMessage(),
            "Error importing data", JOptionPane.ERROR_MESSAGE);
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
          JOptionPane.showMessageDialog(Parent, exc.getMessage(),
            "Error importing data", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    Thread t = new Thread(loadRMI);
    t.start();
  }

  /** load a file selected by the user */
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
    String filename = "file:/" + f.getAbsolutePath();
    URL u = null;
    try {
      u = new URL(filename);
    }
    catch (MalformedURLException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    if (u != null) loadDataURL(u);
  }

  /** @deprecated use saveDataDialog(Form) instead */
  public void saveDataDialog(boolean netcdf) {
    try {
      Form f;
      if (netcdf) f = new visad.data.netcdf.Plain();
      else f = new visad.data.visad.VisADForm();
      saveDataDialog(f);
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  /** pops up a dialog box for user to select file where data will be saved */
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

  /** save to a file selected by the user, using the given data form */
  public void saveDataDialog(Form saveForm) {
    // get file where data should be saved
    final File f = getSaveFile();
    if (f == null) return;

    // start new thread to save the file
    final BasicSSCell cell = this;
    final Form form = saveForm;
    Runnable saveFile = new Runnable() {
      public void run() {
        String msg = "Could not save the dataset \"" + f.getName() +
                     "\" as a " + form.getName() + " file. ";
        try {
          cell.saveData(f, form);
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

  /** capture image and save to a file selected by the user, in JPEG format */
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

  /** set whether this cell is highlighted */
  private void setHighlighted(boolean hl) {
    if (hl) setBorder(B_HIGH);
    else {
      if (hasFormula()) setBorder(B_FORM);
      else if (RMIAddress != null) setBorder(B_RMI);
      else if (hasData()) setBorder(B_URL);
      else setBorder(B_EMPTY);
    }
  }

}

