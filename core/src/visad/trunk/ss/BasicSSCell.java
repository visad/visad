
//
// BasicSSCell.java
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

// AWT package
import java.awt.*;

// JFC packages
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

// I/O package
import java.io.*;

// Net package
import java.net.*;

// RMI class
import java.rmi.RemoteException;

// utility classes
import java.util.Enumeration;
import java.util.Vector;

// VisAD packages
import visad.*;
import visad.java2d.*;
import visad.java3d.*;

// VisAD classes
import visad.data.BadFormException;
import visad.data.DefaultFamily;
import visad.data.netcdf.Plain;
import visad.data.visad.VisADForm;

/** BasicSSCell represents a single spreadsheet display cell.  BasicSSCells
    can be added to a VisAD user interface to provide some of the capabilities
    presented in the VisAD SpreadSheet program.  Other capabilities, like the
    file loader and data mapping dialog boxes, are available only with a
    FancySSCell.<P> */
public class BasicSSCell extends JPanel {

  /** A list of SSCells on this machine */
  static Vector SSCellVector = new Vector();

  /** Name of this BasicSSCell */
  String Name;

  /** URL from where data was imported, if any */
  URL Filename = null;

  /** BasicSSCell's associated VisAD Display */
  DisplayImpl VDisplay;

  /** BasicSSCell's associated VisAD Cell, if any, for evaluating formula */
  FormulaCell VCell = null;

  /** BasicSSCell's associated VisAD DataReference */
  DataReferenceImpl DataRef;

  /** BasicSSCell's associated VisAD DisplayPanel */
  JPanel VDPanel;

  /** Constant for use with Dimension2D variable */
  static final int JAVA3D_3D = 1;

  /** Constant for use with Dimension2D variable */
  static final int JAVA2D_2D = 2;

  /** Constant for use with Dimension2D variable */
  static final int JAVA3D_2D = 3;

  /** Specifies whether the DisplayPanel is 2-D or 3-D, Java2D or Java3D */
  int Dimension2D = -1;

  /** Specifies this SSCell's DisplayListener */
  DisplayListener DListen = null;

  /** A counter for the number of cells currently saving data */
  static int Saving = 0;

  /** Specifies whether the BasicSSCell contains any data */
  boolean HasData = false;

  /** Specifies whether the BasicSSCell is busy loading data; the
      BasicSSCell's Data cannot be changed when the BasicSSCell is busy */
  boolean IsBusy = false;

  /** Specifies whether the BasicSSCell has an associated formula */
  boolean HasFormula = false;

  /** Specifies whether the BasicSSCell has mappings from Data to Display */
  boolean HasMappings = false;

  /** Specifies whether formula errors are reported in a dialog box */
  boolean ShowFormulaErrors = true;

  /** Constructs a new BasicSSCell with the given name */
  public BasicSSCell(String name) throws VisADException, RemoteException {
    if (name == null) {
      throw new TypeException("BasicSSCell: name cannot be null");
    }
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (name.equalsIgnoreCase(panel.Name)) {
        throw new TypeException("BasicSSCell: name already used");
      }
    }
    Name = name;
    SSCellVector.addElement(this);

    DataRef = new DataReferenceImpl(Name);
    setDimension(JAVA2D_2D);
    VDPanel = (JPanel) VDisplay.getComponent();

    setPreferredSize(new Dimension(0, 0));
    setBackground(Color.black);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
  }

  /** Constructs a BasicSSCell with the given name and data string */
  public BasicSSCell(String name, String info) throws VisADException,
                                                      RemoteException {
    this(name);
    if (info != null) setSSCellString(info);
  }

  /** Returns the BasicSSCell object with the specified display */
  public static BasicSSCell getSSCellByDisplay(Display d) {
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (d == (Display) panel.VDisplay) return panel;
    }
    return null;
  }

  /** Returns the BasicSSCell object with the specified name */
  public static BasicSSCell getSSCellByName(String name) {
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (name.equalsIgnoreCase(panel.Name)) return panel;
    }
    return null;
  }

  /** Changes the BasicSSCell's name */
  public void setCellName(String name) throws VisADException {
    if (name == null) {
      throw new TypeException("BasicSSCell: name cannot be null");
    }
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (name.equalsIgnoreCase(panel.Name) && panel != this) {
        throw new TypeException("BasicSSCell: name already used");
      }
    }
    Name = name;
  }

  /** Reconstructs this SSCell using the specified info string */
  public void setSSCellString(String info) throws VisADException,
                                                  RemoteException {
    // extract filename from info string
    if (!info.substring(0, 11).equals("filename = ")) {
      throw new VisADException("Invalid info string!");
    }
    int i=10;
    char c = '*';
    while (c != '\n') c = info.charAt(++i);
    String filename = info.substring(11, i++);
    if (filename.equals("null")) filename = null;

    // extract formula from info string
    if (!info.substring(i, i+10).equals("formula = ")) {
      throw new VisADException("Invalid info string!");
    }
    i += 9;
    int oi = i + 1;
    c = '*';
    while (c != '\n') c = info.charAt(++i);
    String formula = info.substring(oi, i++);

    // extract dimension from info string
    if (!info.substring(i, i+6).equals("dim = ")) {
      throw new VisADException("Invalid info string!");
    }
    i += 5;
    oi = i + 1;
    c = '*';
    while (c != '\n') c = info.charAt(++i);
    String b = info.substring(oi, i++);
    int bnum = -1;
    try {
      bnum = Integer.parseInt(b);
    }
    catch (NumberFormatException exc) { }
    if (bnum == JAVA3D_3D) Dimension2D = JAVA3D_3D;
    else if (bnum == JAVA2D_2D) Dimension2D = JAVA2D_2D;
    else if (bnum == JAVA3D_2D) Dimension2D = JAVA3D_2D;
    else {
      throw new VisADException("Invalid info string!");
    }

    // construct Data
    try {
      if (filename != null) {
        URL u = null;
        try {
          u = new URL(filename);
        }
        catch (MalformedURLException exc) { }
        loadData(u);
      }
    }
    catch (IOException exc) {
      throw new VisADException(exc.toString());
    }

    // construct VCell
    setFormula(formula);
  }

  /** Returns the data string necessary to reconstruct this cell */
  public String getSSCellString() {
    String s = "filename = " + Filename.toString() + "\n";
    s = s + "formula = " + getFormula() + "\n";
    s = s + "dim = " + Dimension2D + "\n";
    return s;
  }

  /** Sets up the DisplayListener for this cell */
  public void setDisplayListener(DisplayListener d) {
    DListen = d;
    if (d != null) VDisplay.addDisplayListener(d);
  }

  /** Maps Reals to the display according to the specified ScalarMaps */
  public void setMaps(ScalarMap[] maps) throws VisADException,
                                               RemoteException {
    if (maps == null) return;
    clearDisplay();
    for (int i=0; i<maps.length; i++) {
      VDisplay.addMap(maps[i]);
    }
    VDisplay.addReference(DataRef);
    HasMappings = true;
  }

  /** Removes the data reference and all mappings from the display */
  public void clearDisplay() throws VisADException, RemoteException {
    if (HasMappings) {
      VDisplay.removeReference(DataRef);
      VDisplay.clearMaps();
      HasMappings = false;
    }
  }

  /** Clears this cell's display and data */
  public void clearData() throws VisADException, RemoteException {
    clearDisplay();
    setData(null);
    Filename = null;
  }

  /** Clears this cell's formula, display and data */
  public void clearCell() throws VisADException, RemoteException {
    setFormula(null);
    clearData();
  }

  /** Links the Data object to the BasicSSCell using the DataReferenceImpl */
  public void setData(Data data) throws VisADException, RemoteException {
    if (DataRef.getData() == data) return;
    clearDisplay();
    DataRef.setData(data);
    if (data == null) {
      if (HasData) {
        remove(VDPanel);
        validate();
        HasData = false;
      }
    }
    else {
      if (!HasData) {
        add(VDPanel);
        validate();
        HasData = true;
      }
    }
  }

  /** Sets the BasicSSCell to 2-D or 3-D display with Java2D or Java3D */
  public void setDimension(boolean twoD, boolean java2d)
                           throws VisADException, RemoteException {
    int dim;
    if (!twoD && java2d) return;
    if (!twoD && !java2d) dim = JAVA3D_3D;
    else if (twoD && java2d) dim = JAVA2D_2D;
    else dim = JAVA3D_2D;  // twoD && !java2d
    setDimension(dim);
  }

  private void setDimension(int dim) throws VisADException, RemoteException {
    if (Dimension2D == dim) return;
    Dimension2D = dim;
    ScalarMap[] maps = null;
    if (VDisplay != null) {
      Vector mapVector = VDisplay.getMapVector();
      int mvs = mapVector.size();
      if (mvs > 0) {
        maps = new ScalarMap[mapVector.size()];
        for (int i=0; i<mapVector.size(); i++) {
          maps[i] = (ScalarMap) mapVector.elementAt(i);
        }
      }
    }
    clearDisplay();

    if (DListen != null) VDisplay.removeDisplayListener(DListen);
    if (Dimension2D == JAVA3D_3D) {
      VDisplay = new DisplayImplJ3D(Name);
    }
    else if (Dimension2D == JAVA2D_2D) {
      VDisplay = new DisplayImplJ2D(Name);
    }
    else {  // Dimension2D == JAVA3D_2D
      VDisplay = new DisplayImplJ3D(Name, new TwoDDisplayRendererJ3D());
    }
    if (DListen != null) VDisplay.addDisplayListener(DListen);

    if (HasData) remove(VDPanel);
    VDPanel = (JPanel) VDisplay.getComponent();
    if (HasData) {
      add(VDPanel);
      validate();
    }
    if (maps != null) {
      try {
        setMaps(maps);
      }
      catch (VisADException exc) { }
    }
  }

  /** Boolean matrix used with isDependentOn() method */
  boolean[] CheckedCell;

  /** Checks whether the cell is dependent on the specified cell */
  public boolean isDependentOn(String cellName) {
    // not dependent on null
    if (cellName == null) return false;

    // always dependent on itself for safety reasons
    if (cellName.equalsIgnoreCase(Name)) return true;

    // check whether specified cell exists
    int numCells = 0;
    BasicSSCell dependCell = null;
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (cellName.equalsIgnoreCase(panel.Name)) dependCell = panel;
      numCells++;
    }
    // not dependent on non-existent cell
    if (dependCell == null) return false;

    // recursively scan formula
    CheckedCell = new boolean[numCells];
    for (int i=0; i<numCells; i++) CheckedCell[i] = false;
    return scanFormula(this, dependCell);
  }

  /** Recursive method used by isDependentOn() method */
  boolean scanFormula(BasicSSCell cell, BasicSSCell dependCell) {
    // mark this cell as checked
    CheckedCell[SSCellVector.indexOf(cell)] = true;

    // scan formula for other cells that must be checked
    if (cell.HasFormula) {
      String[] formula = VCell.PFormula;
      for (int i=0; i<formula.length; i++) {
        String token = formula[i];
        if (Formula.getTokenType(token) == Formula.VARIABLE_TOKEN) {
          BasicSSCell subCell = getSSCellByName(token);
          if (subCell == dependCell) return true;
          if (subCell != null && !CheckedCell[SSCellVector.indexOf(subCell)]
                              && scanFormula(subCell, dependCell)) return true;
        }
      }
    }
    return false;
  }

  /** Sets the BasicSSCell's formula */
  public void setFormula(String f) throws VisADException, RemoteException {
    if (f == null || f.equals("")) {
      if (VCell != null) {
        VCell.setX(false);
        VCell.removeAllReferences();
        VCell = null;
        setData(null);
      }
    }
    else if (VCell == null || !f.equals(VCell.getFormula())) {
      clearCell();
      VCell = new FormulaCell(this, f, ShowFormulaErrors);
    }
    if (VCell == null) HasFormula = false;
    else HasFormula = true;
  }

  /** Returns the BasicSSCell's formula in infix notation */
  public String getFormula() {
    if (VCell == null) return "";
    else return VCell.getFormula();
  }

  /** Returns whether the BasicSSCell is in 2-D display mode */
  public int getDimension() {
    return Dimension2D;
  }

  /** Returns the associated DataReferenceImpl object */
  public DataReferenceImpl getDataRef() {
    return DataRef;
  }

  /** Returns the file name from which the associated Data came, if any */
  public URL getFilename() {
    return Filename;
  }

  /** Imports a data object from a given URL */
  public void loadData(URL u) throws BadFormException, IOException,
                                     VisADException, RemoteException {
    if (u == null) return;
    clearCell();
    final URL url = u;
    IsBusy = true;
    JPanel pleaseWait = new JPanel();
    pleaseWait.setBackground(Color.black);
    pleaseWait.setLayout(new BoxLayout(pleaseWait, BoxLayout.X_AXIS));
    pleaseWait.add(Box.createHorizontalGlue());
    pleaseWait.add(new JLabel("Please wait..."));
    pleaseWait.add(Box.createHorizontalGlue());
    add(pleaseWait);

    // big hammer for redrawing cell
    validate();
    Graphics g = getGraphics();
    if (g != null) {
      paint(g);
      g.dispose();
    }
    repaint();

    boolean error = false;
    Data data = null;
    try {
      // file detection -- note that it will eventually be removed
      //                   when all Data Forms have open(URL) capability
      String s = url.toString();
      boolean f = false;
      String file = null;
      if (s.length() >= 6 && s.substring(0, 6).equalsIgnoreCase("file:/")) {
        f = true;
        file = s.substring(6);
      }
      DefaultFamily loader = new DefaultFamily("loader");
      if (f) data = loader.open(file);
      else data = loader.open(url);
      loader = null;
    }
    finally {
      remove(pleaseWait);
      repaint();
    }
    if (data != null) {
      setData(data);
      Filename = url;
    }
    IsBusy = false;
  }

  /** Exports a data object to a given file name, in netCDF format */
  public void saveData(File f, boolean netcdf) throws BadFormException,
                                                      IOException,
                                                      VisADException,
                                                      RemoteException {
    if (f == null || !HasData) return;
    Saving++;
    if (netcdf) {
      Plain saver = new Plain();
      saver.save(f.getPath(), DataRef.getData(), true);
      saver = null;
    }
    else {
      VisADForm saver = new VisADForm();
      saver.save(f.getPath(), DataRef.getData(), true);
      saver = null;
    }
    Saving--;
  }

  public boolean hasData() {
    return HasData;
  }

  public boolean isBusy() {
    return IsBusy;
  }

  public boolean hasFormula() {
    return HasFormula;
  }

  public boolean hasMappings() {
    return HasMappings;
  }

  /** Specifies whether formula errors should be reported in a dialog box */
  public void setShowFormulaErrors(boolean sfe) {
    ShowFormulaErrors = sfe;
    if (VCell != null) VCell.ShowErrors = ShowFormulaErrors;
  }

  /** Returns true if any BasicSSCell is currently saving data */
  public static boolean isSaving() {
    return Saving > 0;
  }

}

