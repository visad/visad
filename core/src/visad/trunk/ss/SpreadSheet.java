
//
// SpreadSheet.java
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
import java.awt.event.*;

// JFC packages
import java.awt.swing.*;
import java.awt.swing.border.*;
import java.awt.swing.event.*;

// I/O packages
import java.io.*;

// RMI classes
import java.rmi.RemoteException;

// Utility classes
import java.util.Vector;

// VisAD packages
import visad.*;
import visad.java3d.*;

// VisAD classes
import visad.data.BadFormException;

/** SpreadSheet is a user interface for VisAD that supports
    multiple 3-D displays (FancySSCells).<P>*/
public class SpreadSheet extends JFrame implements ActionListener,
                                                   KeyListener,
                                                   ItemListener,
                                                   MouseListener {

  // starting size of the application
  static final int WIDTH = 1000;
  static final int HEIGHT = 900;

  // minimum VisAD display size, including display border
  static final int MIN_VIS_WIDTH = 200;
  static final int MIN_VIS_HEIGHT = 200;
  
  // cursors
  static final Cursor D_CURSOR = Cursor.getDefaultCursor();
  static final Cursor W_CURSOR =
               Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

  // spreadsheet letter order
  static final String Letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  // spreadsheet file dialog
  FileDialog SSFileDialog = null;

  // number of VisAD displays
  int NumVisX = 3;
  int NumVisY = 2;
  int NumVisDisplays = NumVisX*NumVisY;

  // display-related arrays and variables
  Panel DisplayPanel;
  JPanel ScrollPanel;
  FancySSCell[] DisplayCells;
  JTextField FormulaField;
  MenuItem EditPaste;
  JButton ToolPaste;
  JButton FormulaOk;
  CheckboxMenuItem CellDim3D3D;
  CheckboxMenuItem CellDim2D2D;
  CheckboxMenuItem CellDim2D3D;
  int CurDisplay = 0;

  String Clipboard = null;
  File CurrentFile = null;
  ErrorDialog ErrorBox;

  public static void main(String[] argv) { 
    SpreadSheet ss = new SpreadSheet(WIDTH, HEIGHT, "VisAD SpreadSheet");
  }

  /** This is the constructor for the SpreadSheet class. */
  SpreadSheet(int sWidth, int sHeight, String sTitle) {
    ErrorBox = new ErrorDialog(this, "VisAD SpreadSheet Error");
    addKeyListener(this);
    addWindowListener((WindowListener)
      new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          quitProgram();
        }
      }
    );
    setBackground(Color.white);
    setCursor(D_CURSOR);

    // set up the content pane
    JPanel pane = new JPanel();
    pane.setBackground(Color.white);
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.setAlignmentY(JPanel.TOP_ALIGNMENT);
    pane.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    setContentPane(pane);

    // set up menus
    MenuBar menubar = new MenuBar();
    setMenuBar(menubar);

    // file menu
    Menu file = new Menu("File");
    menubar.add(file);

    MenuItem fileNew = new MenuItem("New");
    fileNew.addActionListener(this);
    fileNew.setActionCommand("fileNew");
    file.add(fileNew);

    MenuItem fileOpen = new MenuItem("Open...");
    fileOpen.addActionListener(this);
    fileOpen.setActionCommand("fileOpen");
    file.add(fileOpen);

    MenuItem fileSave = new MenuItem("Save");
    fileSave.addActionListener(this);
    fileSave.setActionCommand("fileSave");
    file.add(fileSave);

    MenuItem fileSaveas = new MenuItem("Save as...");
    fileSaveas.addActionListener(this);
    fileSaveas.setActionCommand("fileSaveas");
    file.add(fileSaveas);

    file.addSeparator();

    MenuItem fileExit = new MenuItem("Exit");
    fileExit.addActionListener(this);
    fileExit.setActionCommand("fileExit");
    file.add(fileExit);

    // edit menu
    Menu edit = new Menu("Edit");
    menubar.add(edit);

    MenuItem editCut = new MenuItem("Cut");
    editCut.addActionListener(this);
    editCut.setActionCommand("editCut");
    edit.add(editCut);

    MenuItem editCopy = new MenuItem("Copy");
    editCopy.addActionListener(this);
    editCopy.setActionCommand("editCopy");
    edit.add(editCopy);

    EditPaste = new MenuItem("Paste");
    EditPaste.addActionListener(this);
    EditPaste.setActionCommand("editPaste");
    EditPaste.setEnabled(false);
    edit.add(EditPaste);

    MenuItem editClear = new MenuItem("Clear");
    editClear.addActionListener(this);
    editClear.setActionCommand("editClear");
    edit.add(editClear);

    // cell menu
    Menu cell = new Menu("Cell");
    menubar.add(cell);

    MenuItem cellImport = new MenuItem("Import data...");
    cellImport.addActionListener(this);
    cellImport.setActionCommand("cellImport");
    cell.add(cellImport);
    cell.addSeparator();

    CellDim3D3D = new CheckboxMenuItem("3-D (Java3D)", true);
    CellDim3D3D.addItemListener(this);
    cell.add(CellDim3D3D);

    CellDim2D2D = new CheckboxMenuItem("2-D (Java2D)");
    CellDim2D2D.addItemListener(this);
    cell.add(CellDim2D2D);

    CellDim2D3D = new CheckboxMenuItem("2-D (Java3D)");
    CellDim2D3D.addItemListener(this);
    cell.add(CellDim2D3D);

    // mappings menu
    Menu map = new Menu("Mappings");
    menubar.add(map);

    MenuItem mapEdit = new MenuItem("Edit map...");
    mapEdit.addActionListener(this);
    mapEdit.setActionCommand("mapEdit");
    map.add(mapEdit);
    map.addSeparator();

    MenuItem mapColorImage = new MenuItem("Color image");
    mapColorImage.addActionListener(this);
    mapColorImage.setActionCommand("mapColorImage");
    map.add(mapColorImage);

    MenuItem mapGrayImage = new MenuItem("Grayscale image");
    mapGrayImage.addActionListener(this);
    mapGrayImage.setActionCommand("mapGrayImage");
    map.add(mapGrayImage);

    MenuItem mapSphereColorImage = new MenuItem("Spherical color image");
    mapSphereColorImage.addActionListener(this);
    mapSphereColorImage.setActionCommand("mapSphereColorImage");
    map.add(mapSphereColorImage);

    MenuItem mapSphereGrayImage = new MenuItem("Spherical grayscale image");
    mapSphereGrayImage.addActionListener(this);
    mapSphereGrayImage.setActionCommand("mapSphereGrayImage");
    map.add(mapSphereGrayImage);

    MenuItem mapColor3DSurface = new MenuItem("Color 3-D surface");
    mapColor3DSurface.addActionListener(this);
    mapColor3DSurface.setActionCommand("mapColor3DSurface");
    map.add(mapColor3DSurface);

    MenuItem mapGray3DSurface = new MenuItem("Grayscale 3-D surface");
    mapGray3DSurface.addActionListener(this);
    mapGray3DSurface.setActionCommand("mapGray3DSurface");
    map.add(mapGray3DSurface);

    MenuItem mapColorSphere3DSurface = new MenuItem(
                                       "Color spherical 3-D surface");
    mapColorSphere3DSurface.addActionListener(this);
    mapColorSphere3DSurface.setActionCommand("mapColorSphere3DSurface");
    map.add(mapColorSphere3DSurface);

    MenuItem mapGraySphere3DSurface = new MenuItem(
                                      "Grayscale spherical 3-D surface");
    mapGraySphere3DSurface.addActionListener(this);
    mapGraySphere3DSurface.setActionCommand("mapGraySphere3DSurface");
    map.add(mapGraySphere3DSurface);

    // set up toolbar
    JToolBar toolbar = new JToolBar();
    toolbar.setBackground(Color.lightGray);
    toolbar.setBorder(new EtchedBorder());
    toolbar.setFloatable(false);
    toolbar.setAlignmentY(JToolBar.TOP_ALIGNMENT);
    toolbar.setAlignmentX(JToolBar.LEFT_ALIGNMENT);
    pane.add(toolbar);

    // file menu toolbar icons
    ImageIcon toolFileNew = new ImageIcon("new.gif");
    if (toolFileNew != null) {
      JButton b = new JButton(toolFileNew);
      b.setToolTipText("New");
      b.addActionListener(this);
      b.setActionCommand("fileNew");
      toolbar.add(b);
    }
    ImageIcon toolFileOpen = new ImageIcon("open.gif");
    if (toolFileOpen != null) {
      JButton b = new JButton(toolFileOpen);
      b.setToolTipText("Open");
      b.addActionListener(this);
      b.setActionCommand("fileOpen");
      toolbar.add(b);
    }
    ImageIcon toolFileSave = new ImageIcon("save.gif");
    if (toolFileSave != null) {
      JButton b = new JButton(toolFileSave);
      b.setToolTipText("Save");
      b.addActionListener(this);
      b.setActionCommand("fileSave");
      toolbar.add(b);
    }
    toolbar.addSeparator();

    // edit menu toolbar icons
    ImageIcon toolEditCut = new ImageIcon("cut.gif");
    if (toolEditCut != null) {
      JButton b = new JButton(toolEditCut);
      b.setToolTipText("Cut");
      b.addActionListener(this);
      b.setActionCommand("editCut");
      toolbar.add(b);
    }
    ImageIcon toolEditCopy = new ImageIcon("copy.gif");
    if (toolEditCopy != null) {
      JButton b = new JButton(toolEditCopy);
      b.setToolTipText("Copy");
      b.addActionListener(this);
      b.setActionCommand("editCopy");
      toolbar.add(b);
    }
    ImageIcon toolEditPaste = new ImageIcon("paste.gif");
    if (toolEditPaste != null) {
      ToolPaste = new JButton(toolEditPaste);
      ToolPaste.setToolTipText("Paste");
      ToolPaste.addActionListener(this);
      ToolPaste.setActionCommand("editPaste");
      ToolPaste.setEnabled(false);
      toolbar.add(ToolPaste);
    }
    toolbar.addSeparator();

    // mappings menu toolbar icons
    ImageIcon toolMappingsEdit = new ImageIcon("mappings.gif");
    if (toolMappingsEdit != null) {
      JButton b = new JButton(toolMappingsEdit);
      b.setToolTipText("Edit mappings");
      b.addActionListener(this);
      b.setActionCommand("mappingsEdit");
      toolbar.add(b);
    }
    toolbar.add(Box.createHorizontalGlue());

    // set up formula bar
    JPanel formulaPanel = new JPanel();
    formulaPanel.setBackground(Color.white);
    formulaPanel.setLayout(new BoxLayout(formulaPanel, BoxLayout.X_AXIS));
    formulaPanel.setBorder(new EtchedBorder());
    formulaPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    formulaPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    pane.add(formulaPanel);

    ImageIcon cancelIcon = new ImageIcon("cancel.gif");
    JButton formulaCancel = new JButton(cancelIcon);
    formulaCancel.setToolTipText("Cancel formula entry");
    formulaCancel.addActionListener(this);
    formulaCancel.setActionCommand("formulaCancel");
    Dimension size = new Dimension(cancelIcon.getIconWidth()+4,
                                   cancelIcon.getIconHeight()+4);
    formulaCancel.setPreferredSize(size);
    formulaCancel.setAlignmentY(JButton.TOP_ALIGNMENT);
    formulaCancel.setAlignmentX(JButton.LEFT_ALIGNMENT);
    formulaPanel.add(formulaCancel);

    ImageIcon okIcon = new ImageIcon("ok.gif");
    FormulaOk = new JButton(okIcon);
    FormulaOk.setToolTipText("Confirm formula entry");
    FormulaOk.addActionListener(this);
    FormulaOk.setActionCommand("formulaOk");
    size = new Dimension(okIcon.getIconWidth()+4, okIcon.getIconHeight()+4);
    FormulaOk.setPreferredSize(size);
    FormulaOk.setAlignmentY(JButton.TOP_ALIGNMENT);
    FormulaOk.setAlignmentX(JButton.LEFT_ALIGNMENT);
    formulaPanel.add(FormulaOk);

    FormulaField = new JTextField();
    FormulaField.setToolTipText("Enter a file name or formula");
    FormulaField.setAlignmentY(JTextField.TOP_ALIGNMENT);
    FormulaField.setAlignmentX(JTextField.LEFT_ALIGNMENT);
    formulaPanel.add(FormulaField);

    ImageIcon importIcon = new ImageIcon("import.gif");
    JButton formulaImport = new JButton(importIcon);
    formulaImport.setToolTipText("Import data");
    formulaImport.addActionListener(this);
    formulaImport.setActionCommand("cellImport");
    size = new Dimension(importIcon.getIconWidth()+4,
                         importIcon.getIconHeight()+4);
    formulaImport.setPreferredSize(size);
    formulaImport.setAlignmentY(JButton.TOP_ALIGNMENT);
    formulaImport.setAlignmentX(JButton.LEFT_ALIGNMENT);
    formulaPanel.add(formulaImport);

    // set up window's main panel
    JPanel mainPanel = new JPanel();
    mainPanel.setBackground(Color.white);
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
    mainPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
    mainPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    mainPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    pane.add(mainPanel);

    // set up scroll pane's panel
    ScrollPanel = new JPanel();
    mainPanel.add(ScrollPanel);
    ScrollPanel.setBackground(Color.white);
    ScrollPanel.setLayout(new BoxLayout(ScrollPanel, BoxLayout.X_AXIS));
    ScrollPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    ScrollPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    // set up scroll pane
    ScrollPane scpane = new ScrollPane() {
      Dimension prefSize = new Dimension(0, 0);

      public Dimension getPreferredSize() {
        return prefSize;
      }
    };
    Adjustable hadj = scpane.getHAdjustable();
    Adjustable vadj = scpane.getVAdjustable();
    hadj.setBlockIncrement(MIN_VIS_WIDTH);
    hadj.setUnitIncrement(MIN_VIS_WIDTH/4);
    vadj.setBlockIncrement(MIN_VIS_HEIGHT);
    vadj.setUnitIncrement(MIN_VIS_HEIGHT/4);
    ScrollPanel.add(scpane);

    // set up display panel
    DisplayPanel = new Panel();
    scpane.add(DisplayPanel);
    DisplayPanel.setBackground(Color.white);
    DisplayPanel.setLayout(new GridLayout(NumVisY, NumVisX, 5, 5));

    // set up display panel's individual VisAD displays
    DisplayCells = new FancySSCell[NumVisDisplays];

    for (int i=0; i<NumVisDisplays; i++) {
      String name = String.valueOf(Letters.charAt(i%NumVisX))
                   +String.valueOf(i/NumVisX+1);
      try {
        DisplayCells[i] = new FancySSCell(name, this);
        DisplayCells[i].addMouseListener(this);
        DisplayCells[i].setMinSize(new Dimension(MIN_VIS_WIDTH,
                                                 MIN_VIS_HEIGHT));
        if (i == 0) DisplayCells[i].setSelected(true);
        DisplayPanel.add(DisplayCells[i]);
      }
      catch (VisADException exc) {
        ErrorBox.showError("Cannot create displays.");
      }
      catch (RemoteException exc) { }
    }

    // display window on screen
    setTitle(sTitle);
    setSize(sWidth, sHeight);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(screenSize.width/2 - sWidth/2,
                screenSize.height/2 - sHeight/2);
    setVisible(true);
  }

  /** Handles menubar/toolbar events. */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    // file menu commands
    if (cmd.equals("fileNew")) newFile();
    else if (cmd.equals("fileOpen")) openFile();
    else if (cmd.equals("fileSave")) saveFile();
    else if (cmd.equals("fileSaveas")) saveasFile();
    else if (cmd.equals("fileExit")) quitProgram();

    // edit menu commands
    else if (cmd.equals("editCut")) cutCell();
    else if (cmd.equals("editCopy")) copyCell();
    else if (cmd.equals("editPaste")) pasteCell();
    else if (cmd.equals("editClear")) clearCell(true);

    // cell menu commands
    else if (cmd.equals("cellImport")) loadDataSet();

    // mappings menu commands
    else if (cmd.equals("mappingsEdit")) createMappings();
    else if (cmd.equals("mapColorImage")) {
      DisplayCells[CurDisplay].setMappingScheme(FancySSCell.COLOR_IMAGE);
    }
    else if (cmd.equals("mapGrayImage")) {
      DisplayCells[CurDisplay].setMappingScheme(FancySSCell.GRAYSCALE_IMAGE);
    }
    else if (cmd.equals("mapSphereColorImage")) {
      DisplayCells[CurDisplay].setMappingScheme(
                               FancySSCell.COLOR_SPHERICAL_IMAGE);
    }
    else if (cmd.equals("mapSphereGrayImage")) {
      DisplayCells[CurDisplay].setMappingScheme(FancySSCell.GRAYSCALE_IMAGE);
    }
    else if (cmd.equals("mapColor3DSurface")) {
      DisplayCells[CurDisplay].setMappingScheme(FancySSCell.COLOR_3DSURFACE);
    }
    else if (cmd.equals("mapGray3DSurface")) {
      DisplayCells[CurDisplay].setMappingScheme(
                               FancySSCell.GRAYSCALE_3DSURFACE);
    }
    else if (cmd.equals("mapColorSphere3DSurface")) {
      DisplayCells[CurDisplay].setMappingScheme(
                               FancySSCell.COLOR_SPHERICAL_3DSURFACE);
    }
    else if (cmd.equals("mapGraySphere3DSurface")) {
      DisplayCells[CurDisplay].setMappingScheme(
                               FancySSCell.GRAYSCALE_SPHERICAL_3DSURFACE);
    }

    // formula bar commands
    else if (cmd.equals("formulaCancel")) refreshFormulaBar();
    else if (cmd.equals("formulaOk")) updateFormula();
  }

  /** Creates a new spreadsheet file, prompting user to save
      old file if necessary. */
  void newFile() {
    // clear all cells
    for (int i=0; i<DisplayCells.length; i++) {
      try {
        DisplayCells[i].clearCell();
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
    CurrentFile = null;
    setTitle("VisAD SpreadSheet");
  }

  /** Opens an existing spreadsheet file, prompting user to save
      old file if necessary. */
  void openFile() {
    if (SSFileDialog == null) SSFileDialog = new FileDialog(this);
    SSFileDialog.setMode(FileDialog.LOAD);
    SSFileDialog.setVisible(true);

    // make sure file exists
    String file = SSFileDialog.getFile();
    if (file == null) return;
    String dir = SSFileDialog.getDirectory();
    if (dir == null) return;
    File f = new File(dir, file);
    if (!f.exists()) {
      ErrorBox.showError("The file "+file+" does not exist");
      return;
    }

    // clear all cells
    newFile();

    // load file
    String[] fileStrings = new String[DisplayCells.length];
    try {
      FileReader fr = new FileReader(f);
      char[] buff = new char[1024];
      int i = 0;
      while (fr.read(buff, 0, buff.length) != -1) {
        fileStrings[i++] = new String(buff);
      }
      fr.close();
    }
    catch (IOException exc) {
      ErrorBox.showError("The file "+file+" could not be loaded");
      return;
    }

    // reconstruct cells
    for (int i=0; i<DisplayCells.length; i++) {
      try {
        DisplayCells[i].setSSCellString(fileStrings[i]);
      }
      catch (VisADException exc) {
        ErrorBox.showError("Could not reconstruct SpreadSheet");
        newFile();
        return;
      }
      catch (RemoteException exc) { }
    }

    CurrentFile = f;
    setTitle("VisAD SpreadSheet - "+f.getPath());
  }

  /** Saves a spreadsheet file under its current name. */
  void saveFile() {
    if (CurrentFile == null) saveasFile();
    else {
      try {
        FileWriter fw = new FileWriter(CurrentFile);
        for (int i=0; i<DisplayCells.length; i++) {
          String s = DisplayCells[i].getSSCellString();
          char[] sc = new char[1024];
          s.getChars(0, s.length(), sc, 0);
          fw.write(sc, 0, sc.length);
        }
        fw.close();
      }
      catch (IOException exc) {
        ErrorBox.showError("Could not save file "+CurrentFile.getName());
      }
    }
  }

  /** Saves a spreadsheet file under a new name. */
  void saveasFile() {
    if (SSFileDialog == null) SSFileDialog = new FileDialog(this);
    SSFileDialog.setMode(FileDialog.SAVE);
    SSFileDialog.setVisible(true);

    // get file
    String file = SSFileDialog.getFile();
    if (file == null) return;
    String dir = SSFileDialog.getDirectory();
    if (dir == null) return;
    File f = new File(dir, file);
    if (f.exists()) {
      // confirm save
      int ans = JOptionPane.showConfirmDialog(null, "This file already "
                                             +"exists.  Do you want to "
                                             +"overwrite it?", "Warning",
                                              JOptionPane.YES_NO_OPTION);
      if (ans != JOptionPane.YES_OPTION) return;
    }
    CurrentFile = f;
    setTitle("VisAD SpreadSheet - "+f.getPath());
    saveFile();
  }

  /** Does any necessary clean-up, then quits the program. */
  void quitProgram() {
    System.exit(0);
  }

  /** Moves a cell from the screen to the clipboard. */
  void cutCell() {
    if (DisplayCells[CurDisplay].confirmClear()) {
      copyCell();
      clearCell(false);
    }
  }

  /** Copies a cell from the screen to the clipboard. */
  void copyCell() {
    Clipboard = DisplayCells[CurDisplay].getSSCellString();
    EditPaste.setEnabled(true);
    ToolPaste.setEnabled(true);
  }

  /** Copies a cell from the clipboard to the screen. */
  void pasteCell() {
    if (Clipboard != null) {
      try {
        DisplayCells[CurDisplay].setSSCellString(Clipboard);
      }
      catch (VisADException exc) {
        ErrorBox.showError("Could not paste cell: "+exc.toString());
      }
      catch (RemoteException exc) { }
    }
  }

  /** Clears the mappings and formula of the current cell. */
  void clearCell(boolean checkSafe) {
    try {
      if (checkSafe) DisplayCells[CurDisplay].smartClear();
      else DisplayCells[CurDisplay].clearCell();
    }
    catch (VisADException exc) {
      ErrorBox.showError("Cannot clear display mappings.");
    }
    catch (RemoteException exc) { }
    refreshFormulaBar();
  }

  /** Allows the user to specify mappings from Data to Display. */
  void createMappings() {
    DisplayCells[CurDisplay].addMapDialog();
  }

  /** Allows the user to import a data set. */
  void loadDataSet() {
    DisplayCells[CurDisplay].loadDataDialog();
  }

  /** Makes sure the formula bar is displaying up-to-date info. */
  void refreshFormulaBar() {
    if (DisplayCells[CurDisplay].hasFormula()) {
      FormulaField.setText(DisplayCells[CurDisplay].getFormula());
    }
    else {
      String f = DisplayCells[CurDisplay].getFilename();
      if (f == null) f = "";
      FormulaField.setText(f);
    }
  }

  /** Update formula based on formula entered in formula bar. */
  void updateFormula() {
    String formula = FormulaField.getText();
    File f = new File(formula);
    if (f.exists()) {
      // try to load the file
      try {
        DisplayCells[CurDisplay].loadData(f);
      }
      catch (RemoteException exc) {
        ErrorBox.showError(exc.toString());
      }
      catch (BadFormException exc) {
        ErrorBox.showError(exc.toString());
      }
      catch (IOException exc) {
        ErrorBox.showError(exc.toString());
      }
      catch (VisADException exc) {
        ErrorBox.showError(exc.toString());
      }
    }
    else {
      // try to set the formula
      try {
        DisplayCells[CurDisplay].setFormula(FormulaField.getText());
      }
      catch (VisADException exc) {
        ErrorBox.showError(exc.toString());
      }
      catch (RemoteException exc) {
        ErrorBox.showError(exc.toString());
      }
    }
  }

  /** Update dimension checkbox menu items in Cell menu. */
  void refreshDimensionMenuItems() {
    int dim = DisplayCells[CurDisplay].getDimension();
    if (dim == BasicSSCell.JAVA3D_3D) {
      CellDim3D3D.setState(true);
      CellDim2D2D.setState(false);
      CellDim2D3D.setState(false);
    }
    else if (dim == BasicSSCell.JAVA2D_2D) {
      CellDim3D3D.setState(false);
      CellDim2D2D.setState(true);
      CellDim2D3D.setState(false);
    }
    else {  // dim == BasicSSCell.JAVA3D_2D
      CellDim3D3D.setState(false);
      CellDim2D2D.setState(false);
      CellDim2D3D.setState(true);
    }
  }

  /** Update spreadsheet info when selected cell changes. */
  void refreshInfo() {
    refreshFormulaBar();
    refreshDimensionMenuItems();
  }

  /** Handles checkbox menu item changes (dimension checkboxes). */
  public void itemStateChanged(ItemEvent e) {
    String i = (String) e.getItem();
    try {
      if (i.equals("3-D (Java3D)")) {
        DisplayCells[CurDisplay].setDimension(false, false);
      }
      else if (i.equals("2-D (Java2D)")) {
        DisplayCells[CurDisplay].setDimension(true, true);
      }
      else {  // i.equals("2-D (Java3D)")
        DisplayCells[CurDisplay].setDimension(true, false);
      }
      refreshDimensionMenuItems();
    }
    catch (VisADException exc) {
      ErrorBox.showError("Cannot alter display dimension.");
    }
    catch (RemoteException exc) { }
  }

  /** Handles key presses. */
  public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();
    int oldDisplay = CurDisplay;

    if (key == KeyEvent.VK_ENTER) {
      FormulaOk.requestFocus();
      updateFormula();
    }

    if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_LEFT
     || key == KeyEvent.VK_DOWN  || key == KeyEvent.VK_UP) {
      // update display variable
      if (key == KeyEvent.VK_RIGHT
       && (CurDisplay+1) % NumVisX != 0) CurDisplay += 1;
      if (key == KeyEvent.VK_LEFT
       && CurDisplay % NumVisX != 0) CurDisplay -= 1;
      if (key == KeyEvent.VK_DOWN) CurDisplay += NumVisX;
      if (key == KeyEvent.VK_UP) CurDisplay -= NumVisX;
      if (CurDisplay >= NumVisDisplays) CurDisplay = oldDisplay;
      if (CurDisplay < 0) CurDisplay = oldDisplay;

      // update blue border on screen
      if (oldDisplay != CurDisplay) {
        DisplayCells[oldDisplay].setSelected(false);
        DisplayCells[CurDisplay].setSelected(true);
      }

      // refresh spreadsheet info
      refreshInfo();
    }
  }

  public void keyReleased(KeyEvent e) { }

  public void keyTyped(KeyEvent e) { }

  /** Handles mouse presses. */
  public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    int cnum = -1;
    for (int i=0; i<DisplayCells.length; i++) {
      if (c == DisplayCells[i]) cnum = i;
    }
    if (cnum < 0) return;

    // update blue border on screen;
    DisplayCells[CurDisplay].setSelected(false);
    DisplayCells[cnum].setSelected(true);
    CurDisplay = cnum;

    // refresh spreadsheet info
    refreshInfo();
  }

  public void mouseReleased(MouseEvent e) { }

  public void mouseClicked(MouseEvent e) { }

  public void mouseEntered(MouseEvent e) { }

  public void mouseExited(MouseEvent e) { }

}

