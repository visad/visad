
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

// AWT packages
import java.awt.*;

// JFC packages
import java.awt.swing.*;
import java.awt.swing.border.*;

// I/O packages
import java.io.*;

// RMI classes
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

/** BasicSSCell represents a single spreadsheet display cell.  BasicSSCells
    can be added to a VisAD user interface to provide some of the capabilities
    presented in the VisAD SpreadSheet program.  Other capabilities, like the
    file loader and data mapping dialog boxes, are available only with a
    FancySSCell.<P> */
public class BasicSSCell extends JPanel {

  /** A list of SSCells on this machine. */
  static Vector SSCellVector = new Vector();

  /** VisAD object for loading data files. */
  static DefaultFamily Loader = new DefaultFamily("Loader");

  /** Name of this BasicSSCell. */
  String Name;

  /** Filename from where data was imported, if any. */
  String Filename = null;

  /** BasicSSCell's associated VisAD Display. */
  DisplayImpl VDisplay;

  /** BasicSSCell's associated VisAD Cell, if any, for evaluating formula. */
  FormulaCell VCell = null;

  /** BasicSSCell's associated VisAD DataReference. */
  DataReferenceImpl DataRef;

  /** BasicSSCell's associated VisAD DisplayPanel. */
  JPanel VDPanel;

  /** Constant for use with Dimension2D variable. */
  static final int JAVA3D_3D = 1;

  /** Constant for use with Dimension2D variable. */
  static final int JAVA2D_2D = 2;

  /** Constant for use with Dimension2D variable. */
  static final int JAVA3D_2D = 3;

  /** Specifies whether the DisplayPanel is 2-D or 3-D, Java2D or Java3D. */
  int Dimension2D = JAVA3D_3D;

  /** Specifies whether the BasicSSCell contains any data. */
  boolean HasData = false;

  /** Specifies whether the BasicSSCell is busy loading data.  The
      BasicSSCell's Data cannot be changed when the BasicSSCell is busy. */
  boolean IsBusy = false;

  /** Specifies whether the BasicSSCell has an associated formula. */
  boolean HasFormula = false;

  /** Specifies whether the BasicSSCell has mappings from Data to Display. */
  boolean HasMappings = false;

  /** Constructs a new BasicSSCell with the given name. */
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
    VDisplay = new DisplayImplJ3D(Name);
    VDPanel = (JPanel) VDisplay.getComponent();

    setPreferredSize(new Dimension(0, 0));
    setBackground(Color.black);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
  }

  /** Constructs a BasicSSCell with the given name and data string. */
  public BasicSSCell(String name, String info) throws VisADException,
                                                      RemoteException {
    this(name);
    if (info != null) setSSCellString(info);
  }

  /** Returns the BasicSSCell object with the specified name. */
  public static BasicSSCell getSSCellByName(String name) {
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (name.equalsIgnoreCase(panel.Name)) return panel;
    }
    return null;
  }

  /** Changes the BasicSSCell's name. */
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

  /** Reconstructs this SSCell using the specified info string. */
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
      if (filename != null) loadData(new File(filename));
    }
    catch (IOException exc) {
      throw new VisADException(exc.toString());
    }

    // construct VCell
    setFormula(formula);
  }

  /** Returns the data string necessary to reconstruct this cell. */
  public String getSSCellString() {
    String s = "filename = " + Filename + "\n";
    s = s + "formula = " + getFormula() + "\n";
    s = s + "dim = " + Dimension2D + "\n";
    return s;
  }

  /** Maps Reals to the display according to the specified ScalarMaps. */
  public void setMaps(ScalarMap[] maps) throws VisADException,
                                               RemoteException {
    if (maps == null) return;
    for (int i=0; i<maps.length; i++) {
      VDisplay.addMap(maps[i]);
    }
    VDisplay.addReference(DataRef);
    HasMappings = true;
  }

  /** Removes the data reference and all mappings from the display. */
  public void clearDisplay() throws VisADException, RemoteException {
    if (HasMappings) {
      VDisplay.removeReference(DataRef);
      VDisplay.clearMaps();
      HasMappings = false;
    }
  }

  /** Clears this cell's display and data. */
  public void clearData() throws VisADException, RemoteException {
    clearDisplay();
    setData(null);
    Filename = null;
  }

  /** Clears this cell's formula, display and data. */
  public void clearCell() throws VisADException, RemoteException {
    setFormula(null);
    clearData();
  }

  /** Links the Data object to the BasicSSCell using the DataReferenceImpl. */
  public void setData(Data data) throws VisADException, RemoteException {
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

  /** Sets the BasicSSCell to 2-D or 3-D display with Java2D or Java3D. */
  public void setDimension(boolean twoD, boolean java2d)
                              throws VisADException, RemoteException {
    int dim;
    if (!twoD && java2d) return;
    if (!twoD && !java2d) dim = JAVA3D_3D;
    else if (twoD && java2d) dim = JAVA2D_2D;
    else dim = JAVA3D_2D;  // twoD && !java2d

    if (Dimension2D == dim) return;
    Dimension2D = dim;
    clearDisplay();

    if (Dimension2D == JAVA3D_3D) {
      VDisplay = new DisplayImplJ3D(Name);
    }
    else if (Dimension2D == JAVA2D_2D) {
      VDisplay = new DisplayImplJ2D(Name);
    }
    else {  // Dimension2D == JAVA3D_2D
      VDisplay = new DisplayImplJ3D(Name, new TwoDDisplayRendererJ3D());
    }

    if (HasData) remove(VDPanel);
    VDPanel = (JPanel) VDisplay.getComponent();
    if (HasData) {
      add(VDPanel);
      validate();
    }
  }

  /** Boolean matrix used with isDependentOn() method. */
  boolean[] CheckedCell;

  /** Checks whether the cell is dependent on the specified cell. */
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

  /** Recursive method used by isDependentOn() method. */
  boolean scanFormula(BasicSSCell cell, BasicSSCell dependCell) {
    // mark this cell as checked
    CheckedCell[SSCellVector.indexOf(cell)] = true;

    // scan formula for other cells that must be checked
    if (cell.HasFormula) {
      String[] formula = VCell.PFormula;
      for (int i=0; i<formula.length; i++) {
        String token = formula[i];
        if (VCell.f.getTokenType(token) == Formula.VARIABLE_TOKEN) {
          BasicSSCell subCell = getSSCellByName(token);
          if (subCell == dependCell) return true;
          if (subCell != null && !CheckedCell[SSCellVector.indexOf(subCell)]
                              && scanFormula(subCell, dependCell)) return true;
        }
      }
    }
    return false;
  }

  /** Sets the BasicSSCell's formula. */
  public void setFormula(String f) throws VisADException, RemoteException {
    if (f == null || f.equals("")) {
      if (VCell != null) {
        VCell.setX(false);
        VCell = null;
        setData(null);
      }
    }
    else if (VCell == null || !f.equals(VCell.getFormula())) {
      clearCell();
      VCell = new FormulaCell(this, f);
    }
    if (VCell == null) HasFormula = false;
    else HasFormula = true;
  }

  /** Returns the BasicSSCell's formula in infix notation. */
  public String getFormula() {
    if (VCell == null) return "";
    else return VCell.getFormula();
  }

  /** Returns whether the BasicSSCell is in 2-D display mode. */
  public int getDimension() {
    return Dimension2D;
  }

  /** Returns the associated DataReferenceImpl object. */
  public DataReferenceImpl getDataRef() {
    return DataRef;
  }

  /** Returns the file name from which the associated Data came, if any. */
  public String getFilename() {
    return Filename;
  }

  /** Imports a data object from a given file name. */
  public void loadData(File f) throws BadFormException, IOException,
                                      VisADException, RemoteException {
    if (!f.exists()) return;
    clearCell();
    final String filename = f.getPath();
    Runnable loadFile = new Runnable() {
      public void run() {
        IsBusy = true;
        JPanel pleaseWait = new JPanel();
        pleaseWait.setBackground(Color.black);
        pleaseWait.setLayout(new BoxLayout(pleaseWait, BoxLayout.X_AXIS));
        pleaseWait.add(Box.createHorizontalGlue());
        pleaseWait.add(new JLabel("Please wait..."));
        pleaseWait.add(Box.createHorizontalGlue());
        add(pleaseWait);

        validate();
        Data data = null;
        try {
          data = (Data) BasicSSCell.Loader.open(filename);
        }
        catch (RemoteException exc) { }
        catch (IOException exc) { }
        catch (VisADException exc) { }
        remove(pleaseWait);
        validate();
        if (data != null) {
          try {
            setData(data);
            Filename = filename;
          }
          catch (RemoteException exc) { }
          catch (VisADException exc) { }
        }
        IsBusy = false;
      }
    };
    Thread t = new Thread(loadFile);
    t.start();
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

}

