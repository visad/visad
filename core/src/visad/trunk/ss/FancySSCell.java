
//
// FancySSCell.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
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

package visad.ss;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import visad.*;
import visad.data.BadFormException;
import visad.util.*;

/** FancySSCell is an extension of BasicSSCell with extra options, such
    as a file loader dialog and a dialog to set up ScalarMaps.  It
    provides an example of GUI extensions to BasicSSCell.<P> */
public class FancySSCell extends BasicSSCell implements SSCellListener {

  /** unselected border */
  static final Border NORM = new LineBorder(Color.gray, 3);

  /** selected border */
  static final Border HIGH = new LineBorder(Color.yellow, 3);

  /** This variable is static so that the previous directory is remembered */
  static FileDialog FileBox = null;

  /** This cell's parent frame */
  Frame Parent;

  /** This cell's associated JFrame, for use with VisAD Controls */
  JFrame WidgetFrame = null;

  /** Specify whether this cell is selected */
  boolean Selected = false;

  /** Specify whether this cell should auto-switch to 3-D */
  boolean AutoSwitch = true;

  /** Specify whether this cell should auto-detect mappings for data */
  boolean AutoDetect = true;

  /** constructor */
  public FancySSCell(String name, String info, Frame parent)
                                  throws VisADException, RemoteException {
    super(name, info);
    Parent = parent;
    setBorder(NORM);
    addSSCellChangeListener(this);
  }

  /** shortcut constructor */
  public FancySSCell(String name, Frame parent) throws VisADException,
                                                       RemoteException {
    this(name, null, parent);
  }

  /** shortcut constructor */
  public FancySSCell(String name) throws VisADException, RemoteException {
    this(name, null, null);
  }

  /** Re-auto-detect mappings when this cell's data changes */
  public void ssCellChanged(SSCellChangeEvent e) {
    if (e.getChangeType() == SSCellChangeEvent.DATA_CHANGE) {
      Data value = null;
      try {
        value = (Data) fm.getThing(Name);
      }
      catch (ClassCastException exc) { }
      catch (VisADException exc) { }
      try {
        if (value != null) autoDetectMappings();
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
  }

  /** Switch to 3-D mode if necessary and available, then call setMaps() */
  public void setMapsAuto(ScalarMap[] maps) throws VisADException,
                                                   RemoteException {
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
      setDimension(need == 1, need == 0);
    }
    setMaps(maps);
  }

  /** Set the ScalarMaps for this cell and creates needed control widgets */
  public void setMaps(ScalarMap[] maps) throws VisADException,
                                               RemoteException {
    super.setMaps(maps);
    hideWidgetFrame();
    WidgetFrame = null;
    if (maps == null) return;

    // create GraphicsModeControl widget
    initWidgetFrame();
    GMCWidget gmcw = new GMCWidget(VDisplay.getGraphicsModeControl());
    addToFrame(gmcw);

    // create any other necessary widgets
    for (int i=0; i<maps.length; i++) {
      DisplayRealType drt = maps[i].getDisplayScalar();
      if (drt.equals(Display.RGB)) {
        LabeledRGBWidget lw = new LabeledRGBWidget(maps[i]);
        addToFrame(lw);
      }
      else if (drt.equals(Display.RGBA)) {
        LabeledRGBAWidget lw = new LabeledRGBAWidget(maps[i]);
        addToFrame(lw);
      }
      else if (drt.equals(Display.SelectValue)) {
        VisADSlider vs = new VisADSlider(maps[i]);
        vs.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        addToFrame(vs);
      }
      else if (drt.equals(Display.SelectRange)) {
        SelectRangeWidget srs = new SelectRangeWidget(maps[i]);
        addToFrame(srs);
      }
      else if (drt.equals(Display.IsoContour)) {
        ContourWidget cw = new ContourWidget(maps[i]);
        WidgetFrame.getContentPane().add(cw);
        addToFrame(cw);
      }
      else if (drt.equals(Display.Animation)) {
        AnimationWidget aw = new AnimationWidget(maps[i]);
        addToFrame(aw);
      }

      // show widget frame
      WidgetFrame.pack();
      WidgetFrame.setVisible(true);
    }
  }

  private boolean first = true;

  /** Used by setMaps() method */
  private void initWidgetFrame() {
    WidgetFrame = new JFrame("Controls (" + Name + ")");
    Container pane = WidgetFrame.getContentPane();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    first = true;
  }

  /** Used by setMaps() method */
  private void addToFrame(Component c) {
    if (!first) WidgetFrame.getContentPane().add(new Divider());
    WidgetFrame.getContentPane().add(c);
    first = false;
  }

  /** Show the widgets for altering controls */
  public void showWidgetFrame() {
    if (WidgetFrame != null) WidgetFrame.setVisible(true);
  }

  /** Hide the widgets for altering controls */
  public void hideWidgetFrame() {
    if (WidgetFrame != null) WidgetFrame.setVisible(false);
  }

  /** Guess a good set of mappings for this cell's Data and apply them */
  void autoDetectMappings() throws VisADException, RemoteException {
    if (AutoDetect) {
      Data data = null;
      data = DataRef.getData();
      MathType mt = null;
      try {
        if (data != null) mt = data.getType();
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
      if (mt != null) {
        boolean allow3D = Dimension2D != JAVA2D_2D || AutoSwitch;
        setMapsAuto(mt.guessMaps(allow3D));
      }
    }
  }

  /** Set the Data for this cell, and apply the default ScalarMaps */
  public void setData(Data data) throws VisADException, RemoteException {
    super.setData(data);
    hideWidgetFrame();
    WidgetFrame = null;
  }

  /** Set this cell's formula */
  public void setFormula(String f) throws VisADException, RemoteException {
    super.setFormula(f);
  }

  /** Specify whether the FancySSCell has a blue border or a gray border */
  public void setSelected(boolean value) {
    if (Selected == value) return;
    Selected = value;
    if (Selected) {
      setBorder(HIGH);
      showWidgetFrame();
    }
    else {
      setBorder(NORM);
      hideWidgetFrame();
    }
    validate();
    repaint();
  }

  /** Specify whether this FancySSCell should auto-switch to 3-D */
  public void setAutoSwitch(boolean value) {
    AutoSwitch = value;
  }

  /** Return whether this FancySSCell auto-switches to 3-D */
  public boolean getAutoSwitch() {
    return AutoSwitch;
  }

  /** Specify whether this FancySSCell should auto-detect its mappings */
  public void setAutoDetect(boolean value) {
    AutoDetect = value;
  }

  /** Return whether this FancySSCell auto-detects its mappings */
  public boolean getAutoDetect() {
    return AutoDetect;
  }

  /** Ask user to confirm clearing the cell if any other cell depends on it */
  public boolean confirmClear() {
    if (othersDepend()) {
      int ans = JOptionPane.showConfirmDialog(null, "Other cells depend on "
                                             +"this cell.  Are you sure you "
                                             +"want to clear it?", "Warning",
                                              JOptionPane.YES_NO_OPTION);
      if (ans != JOptionPane.YES_OPTION) return false;
    }
    return true;
  }

  public void clearCell() throws VisADException, RemoteException {
    super.clearCell();
  }

  /** Clear the cell if no other cell depends it;  otherwise, ask the
      user "Are you sure?" */
  public void smartClear() throws VisADException, RemoteException {
    if (confirmClear()) {
      hideWidgetFrame();
      WidgetFrame = null;
      clearCell();
    }
  }

  /** Let the user create ScalarMaps from the current SSPanel's Data
      to its Display */
  public void addMapDialog() {
    // check whether this cell has data
    if (!HasData) {
      JOptionPane.showMessageDialog(Parent,
          "This cell has no data",
          "FancySSCell error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // get mappings from mapping dialog
    Data data = DataRef.getData();
    ScalarMap[] maps = null;
    if (VDisplay != null) {
      Vector mapVector = VDisplay.getMapVector();
      maps = new ScalarMap[mapVector.size()];
      for (int i=0; i<mapVector.size(); i++) {
        maps[i] = (ScalarMap) mapVector.elementAt(i);
      }
    }
    MappingDialog mapDialog = new MappingDialog(Parent, data, maps);
    mapDialog.pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension mds = mapDialog.getSize();
    mapDialog.setLocation(screenSize.width/2 - mds.width/2,
                          screenSize.height/2 - mds.height/2);
    mapDialog.setVisible(true);

    // make sure user did not cancel the operation
    if (!mapDialog.Confirm) return;

    // clear old mappings
    try {
      clearMaps();
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }

    // set up new mappings
    try {
      setMapsAuto(mapDialog.ScalarMaps);
    }
    catch (VisADException exc) {
      JOptionPane.showMessageDialog(Parent,
          "This combination of mappings is not valid:\n" + exc.toString(),
          "FancySSCell error", JOptionPane.ERROR_MESSAGE);
    }
    catch (RemoteException exc) { }
  }

  /** Import a data object from a given URL, in a separate thread */
  public void loadDataURL(URL u) {
    final URL url = u;
    final BasicSSCell cell = this;
    Runnable loadFile = new Runnable() {
      public void run() {
        String msg = "Could not load the dataset \"" +
                     url.toString() + "\"\n";
        boolean success = true;
        try {
          cell.loadData(url);
          if (!cell.hasData()) {
            System.out.println("Cell \"has no data.\"  Ugh.");
            JOptionPane.showMessageDialog(Parent, "Unable to import data",
                                          "Error importing data",
                                          JOptionPane.ERROR_MESSAGE);
          }
        }
        catch (BadFormException exc) {
          msg = msg + "VisAD does not support this file type.";
          success = false;
        }
        catch (RemoteException exc) {
          msg = msg + "A RemoteException occurred:\n" + exc.toString();
          success = false;
        }
        catch (IOException exc) {
          msg = msg + "The file does not exist, or its data is corrupt.";
          success = false;
        }
        catch (VisADException exc) {
          msg = msg + "An error occurred:\n" + exc.toString();
          success = false;
        }
        if (!success) {
          JOptionPane.showMessageDialog(Parent, msg, "Error importing data",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    Thread t = new Thread(loadFile);
    t.start();
  }

  /** Imports a data object from a server using RMI, in a separate thread */
  public void loadDataRMI(String s) {
    final String sname = s;
    Runnable loadRMI = new Runnable() {
      public void run() {
        String msg = "Could not import data from the specified RMI address.\n";
        boolean success = true;
        try {
          loadRMI(sname);
        }
        catch (MalformedURLException exc) {
          msg = msg + "The address is not valid.";
          success = false;
        }
        catch (NotBoundException exc) {
          msg = msg + "";
          success = false;
        }
        catch (AccessException exc) {
          msg = msg + "Could not obtain access to the data.";
          success = false;
        }
        catch (RemoteException exc) {
          msg = msg + "A remote error occurred.";
          success = false;
        }
        catch (VisADException exc) {
          success = false;
        }
        if (!success) {
          JOptionPane.showMessageDialog(Parent, msg, "Error importing data",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    Thread t = new Thread(loadRMI);
    t.start();
  }

  /** Load a file selected by the user */
  public void loadDataDialog() {
    // get file name from file dialog
    if (FileBox == null) FileBox = new FileDialog(Parent);
    FileBox.setMode(FileDialog.LOAD);
    FileBox.setVisible(true);

    // make sure file exists
    String file = FileBox.getFile();
    if (file == null) return;
    String directory = FileBox.getDirectory();
    if (directory == null) return;
    File f = new File(directory, file);
    if (!f.exists()) {
      JOptionPane.showMessageDialog(Parent, file+" does not exist",
                  "Cannot load file", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // load file
    String filename = "file:/" + f.getAbsolutePath();
    URL u = null;
    try {
      u = new URL(filename);
    }
    catch (MalformedURLException exc) { }
    if (u != null) loadDataURL(u);
  }

  /** Save to a file selected by the user, in netCDF or serialized format */
  public void saveDataDialog(boolean netcdf) {
    if (!HasData) {
      JOptionPane.showMessageDialog(Parent, "This cell is empty.",
                  "Nothing to save", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // get file name from file dialog
    if (FileBox == null) FileBox = new FileDialog(Parent);
    FileBox.setMode(FileDialog.SAVE);
    FileBox.setVisible(true);

    // make sure file is valid
    String file = FileBox.getFile();
    if (file == null) return;
    String directory = FileBox.getDirectory();
    if (directory == null) return;
    File f = new File(directory, file);

    // start new thread to save the file
    final File fn = f;
    final BasicSSCell cell = this;
    final boolean nc = netcdf;
    Runnable saveFile = new Runnable() {
      public void run() {
        String msg = "Could not save the dataset \"" + fn.getName() +
                     "\" as a " + (nc ? "netCDF file"
                                      : "serialized data file") + ".\n";
        try {
          cell.saveData(fn, nc);
        }
        catch (BadFormException exc) {
          msg = msg + "A BadFormException occurred:\n" + exc.toString();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
                                        JOptionPane.ERROR_MESSAGE);
        }
        catch (RemoteException exc) {
          msg = msg + "A RemoteException occurred:\n" + exc.toString();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
                                        JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException exc) {
          msg = msg + "An IOException occurred:\n" + exc.toString();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
                                        JOptionPane.ERROR_MESSAGE);
        }
        catch (VisADException exc) {
          msg = msg + "An error occurred:\n" + exc.toString();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    Thread t = new Thread(saveFile);
    t.start();
  }

  /** A thin, horizontal divider for separating widget frame components */
  private class Divider extends JComponent {

    public void paint(Graphics g) {
      int w = getSize().width;
      g.setColor(Color.white);
      g.drawRect(0, 0, w-2, 6);
      g.drawRect(2, 2, w-4, 2);
      g.setColor(Color.black);
      g.drawRect(1, 1, w-3, 3);
    }

    public Dimension getMinimumSize() {
      return new Dimension(0, 6);
    }

    public Dimension getPreferredSize() {
      return new Dimension(0, 6);
    }

    public Dimension getMaximumSize() {
      return new Dimension(Integer.MAX_VALUE, 6);
    }

  }

}

