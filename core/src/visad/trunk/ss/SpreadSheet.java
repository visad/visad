
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
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;
import com.sun.java.swing.event.*;

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
                                                   AdjustmentListener,
                                                   DisplayListener,
                                                   KeyListener,
                                                   ItemListener,
                                                   MouseListener {

  // starting size of the application, in percentage of screen size
  static final int WIDTH_PERCENT = 75;
  static final int HEIGHT_PERCENT = 75;

  // minimum VisAD display size, including display border
  static final int MIN_VIS_WIDTH = 200;
  static final int MIN_VIS_HEIGHT = 200;
  
  // spreadsheet letter order
  static final String Letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  // spreadsheet file dialog
  FileDialog SSFileDialog = null;

  // number of VisAD displays
  int NumVisX = 4;
  int NumVisY = 3;
  int NumVisDisplays = NumVisX*NumVisY;

  // display-related arrays and variables
  Panel DisplayPanel;
  JPanel ScrollPanel;
  JScrollPane HorizLabels;
  JScrollPane VertLabels;
  FancySSCell[] DisplayCells;
  JTextField FormulaField;
  MenuItem EditPaste;
  JButton ToolPaste;
  JButton FormulaOk;
  CheckboxMenuItem CellDim3D3D, CellDim2D2D, CellDim2D3D;
  CheckboxMenuItem DispImage, DispSphereImage,
                   DispSurface3D, DispSphereSurface3D;
  CheckboxMenuItem DispColor, DispGray, DispCMY, DispHSV;
  int CurDisplay = 0;

  String Clipboard = null;
  File CurrentFile = null;

  public static void main(String[] argv) { 
    SpreadSheet ss = new SpreadSheet(WIDTH_PERCENT, HEIGHT_PERCENT,
                                     "VisAD SpreadSheet");
  }

  /** This is the constructor for the SpreadSheet class. */
  SpreadSheet(int sWidth, int sHeight, String sTitle) {
    addKeyListener(this);
    addWindowListener((WindowListener)
      new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          quitProgram();
        }
      }
    );
    setBackground(Color.white);

    // set up the content pane
    JPanel pane = new JPanel();
    pane.setBackground(Color.white);
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    setContentPane(pane);

    // set up menus
    MenuBar menubar = new MenuBar();
    setMenuBar(menubar);

    // file menu
    Menu file = new Menu("File");
    menubar.add(file);

    MenuItem fileOpen = new MenuItem("Import data...");
    fileOpen.addActionListener(this);
    fileOpen.setActionCommand("fileOpen");
    file.add(fileOpen);

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

    // setup menu
    Menu setup = new Menu("Setup");
    menubar.add(setup);

    MenuItem setupNew = new MenuItem("New spreadsheet file");
    setupNew.addActionListener(this);
    setupNew.setActionCommand("setupNew");
    setup.add(setupNew);

    MenuItem setupOpen = new MenuItem("Open spreadsheet file...");
    setupOpen.addActionListener(this);
    setupOpen.setActionCommand("setupOpen");
    setup.add(setupOpen);

    MenuItem setupSave = new MenuItem("Save spreadsheet file");
    setupSave.addActionListener(this);
    setupSave.setActionCommand("setupSave");
    setup.add(setupSave);

    MenuItem setupSaveas = new MenuItem("Save spreadsheet file as...");
    setupSaveas.addActionListener(this);
    setupSaveas.setActionCommand("setupSaveas");
    setup.add(setupSaveas);

    // display menu
    Menu disp = new Menu("Display");
    menubar.add(disp);

    MenuItem dispEdit = new MenuItem("Edit mappings...");
    dispEdit.addActionListener(this);
    dispEdit.setActionCommand("dispEdit");
    disp.add(dispEdit);
    disp.addSeparator();

    CellDim3D3D = new CheckboxMenuItem("3-D (Java3D)");
    CellDim3D3D.addItemListener(this);
    disp.add(CellDim3D3D);

    CellDim2D2D = new CheckboxMenuItem("2-D (Java2D)", true);
    CellDim2D2D.addItemListener(this);
    disp.add(CellDim2D2D);

    CellDim2D3D = new CheckboxMenuItem("2-D (Java3D)");
    CellDim2D3D.addItemListener(this);
    disp.add(CellDim2D3D);
    disp.addSeparator();

    DispImage = new CheckboxMenuItem("Image");
    DispImage.addItemListener(this);
    disp.add(DispImage);

    DispSphereImage = new CheckboxMenuItem("Spherical image");
    DispSphereImage.addItemListener(this);
    disp.add(DispSphereImage);

    DispSurface3D = new CheckboxMenuItem("3-D surface", true);
    DispSurface3D.addItemListener(this);
    disp.add(DispSurface3D);

    DispSphereSurface3D = new CheckboxMenuItem("3-D spherical surface");
    DispSphereSurface3D.addItemListener(this);
    disp.add(DispSphereSurface3D);
    disp.addSeparator();

    DispColor = new CheckboxMenuItem("Color", true);
    DispColor.addItemListener(this);
    disp.add(DispColor);

    DispGray = new CheckboxMenuItem("Grayscale");
    DispGray.addItemListener(this);
    disp.add(DispGray);

    DispCMY = new CheckboxMenuItem("CMY");
    DispCMY.addItemListener(this);
    disp.add(DispCMY);

    DispHSV = new CheckboxMenuItem("HSV");
    DispHSV.addItemListener(this);
    disp.add(DispHSV);

    // window menu
    Menu window = new Menu("Window");
    menubar.add(window);

    MenuItem winWidget = new MenuItem("Show VisAD controls");
    winWidget.addActionListener(this);
    winWidget.setActionCommand("winWidget");
    window.add(winWidget);

    // set up toolbar
    JToolBar toolbar = new JToolBar();
    toolbar.setBackground(Color.lightGray);
    toolbar.setBorder(new EtchedBorder());
    toolbar.setFloatable(false);
    pane.add(toolbar);

    // file menu toolbar icons
    ImageIcon toolFileOpen = new ImageIcon("open.gif");
    if (toolFileOpen != null) {
      JButton b = new JButton(toolFileOpen);
      b.setToolTipText("Import data");
      b.addActionListener(this);
      b.setActionCommand("fileOpen");
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
      b.setActionCommand("dispEdit");
      toolbar.add(b);
    }
    toolbar.add(Box.createHorizontalGlue());

    // set up formula bar
    JPanel formulaPanel = new JPanel();
    formulaPanel.setBackground(Color.white);
    formulaPanel.setLayout(new BoxLayout(formulaPanel, BoxLayout.X_AXIS));
    formulaPanel.setBorder(new EtchedBorder());
    pane.add(formulaPanel);
    pane.add(Box.createRigidArea(new Dimension(0, 6)));

    ImageIcon cancelIcon = new ImageIcon("cancel.gif");
    JButton formulaCancel = new JButton(cancelIcon);
    formulaCancel.setAlignmentY(JButton.CENTER_ALIGNMENT);
    formulaCancel.setToolTipText("Cancel formula entry");
    formulaCancel.addActionListener(this);
    formulaCancel.setActionCommand("formulaCancel");
    Dimension size = new Dimension(cancelIcon.getIconWidth()+4,
                                   cancelIcon.getIconHeight()+4);
    formulaCancel.setPreferredSize(size);
    formulaPanel.add(formulaCancel);

    ImageIcon okIcon = new ImageIcon("ok.gif");
    FormulaOk = new JButton(okIcon);
    FormulaOk.setAlignmentY(JButton.CENTER_ALIGNMENT);
    FormulaOk.setToolTipText("Confirm formula entry");
    FormulaOk.addActionListener(this);
    FormulaOk.setActionCommand("formulaOk");
    size = new Dimension(okIcon.getIconWidth()+4, okIcon.getIconHeight()+4);
    FormulaOk.setPreferredSize(size);
    formulaPanel.add(FormulaOk);

    FormulaField = new JTextField();
    FormulaField.setToolTipText("Enter a file name or formula");
    FormulaField.addActionListener(this);
    FormulaField.setActionCommand("formulaChange");
    formulaPanel.add(FormulaField);

    ImageIcon importIcon = new ImageIcon("import.gif");
    JButton formulaImport = new JButton(importIcon);
    formulaImport.setAlignmentY(JButton.CENTER_ALIGNMENT);
    formulaImport.setToolTipText("Import data");
    formulaImport.addActionListener(this);
    formulaImport.setActionCommand("fileOpen");
    size = new Dimension(importIcon.getIconWidth()+4,
                         importIcon.getIconHeight()+4);
    formulaImport.setPreferredSize(size);
    formulaPanel.add(formulaImport);

    // label constants
    final int LABEL_WIDTH = 30;
    final int LABEL_HEIGHT = 20;

    // set up horizontal spreadsheet cell labels
    JPanel horizShell = new JPanel();
    horizShell.setBackground(Color.white);
    horizShell.setLayout(new BoxLayout(horizShell, BoxLayout.X_AXIS));
    horizShell.add(Box.createRigidArea(new Dimension(LABEL_WIDTH+10, 0)));
    pane.add(horizShell);

    JPanel horizPanel = new JPanel() {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, LABEL_HEIGHT);
      }
    };
    horizPanel.setLayout(new BoxLayout(horizPanel, BoxLayout.X_AXIS));
    for (int i=0; i<NumVisX; i++) {
      JPanel lp = new JPanel();
      lp.setBorder(new LineBorder(Color.black, 1));
      lp.setLayout(new GridLayout(1, 1));
      lp.setPreferredSize(new Dimension(MIN_VIS_WIDTH+5, 0));
      lp.add(new JLabel(String.valueOf(Letters.charAt(i)),
                        SwingConstants.CENTER));
      horizPanel.add(lp);
    }
    HorizLabels = new JScrollPane(horizPanel,
                                  JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    HorizLabels.setMinimumSize(new Dimension(0, LABEL_HEIGHT+4));
    HorizLabels.setPreferredSize(new Dimension(0, LABEL_HEIGHT+4));
    HorizLabels.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                             LABEL_HEIGHT+4));
    horizShell.add(HorizLabels);
    horizShell.add(Box.createRigidArea(new Dimension(6, 0)));

    // set up window's main panel
    JPanel mainPanel = new JPanel();
    mainPanel.setBackground(Color.white);
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
    pane.add(mainPanel);
    pane.add(Box.createRigidArea(new Dimension(0, 6)));

    // set up vertical spreadsheet cell labels
    JPanel vertShell = new JPanel();
    vertShell.setBackground(Color.white);
    vertShell.setLayout(new BoxLayout(vertShell, BoxLayout.Y_AXIS));
    mainPanel.add(Box.createRigidArea(new Dimension(6, 0)));
    mainPanel.add(vertShell);

    JPanel vertPanel = new JPanel() {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(LABEL_WIDTH, d.height);
      }
    };
    vertPanel.setLayout(new BoxLayout(vertPanel, BoxLayout.Y_AXIS));
    for (int i=0; i<NumVisY; i++) {
      JPanel lp = new JPanel();
      lp.setBorder(new LineBorder(Color.black, 1));
      lp.setLayout(new GridLayout(1, 1));
      lp.setPreferredSize(new Dimension(0, MIN_VIS_HEIGHT+5));
      lp.add(new JLabel(""+(i+1), SwingConstants.CENTER));
      vertPanel.add(lp);
    }
    VertLabels = new JScrollPane(vertPanel,
                                 JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    VertLabels.setMinimumSize(new Dimension(LABEL_WIDTH+4, 0));
    VertLabels.setPreferredSize(new Dimension(LABEL_WIDTH+4, 0));
    VertLabels.setMaximumSize(new Dimension(LABEL_WIDTH+4, Integer.MAX_VALUE));
    vertShell.add(VertLabels);

    // set up scroll pane's panel
    ScrollPanel = new JPanel();
    ScrollPanel.setBackground(Color.white);
    ScrollPanel.setLayout(new BoxLayout(ScrollPanel, BoxLayout.X_AXIS));
    mainPanel.add(ScrollPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(6, 0)));

    // set up scroll pane for VisAD Displays
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
    hadj.addAdjustmentListener(this);
    vadj.setBlockIncrement(MIN_VIS_HEIGHT);
    vadj.setUnitIncrement(MIN_VIS_HEIGHT/4);
    vadj.addAdjustmentListener(this);
    ScrollPanel.add(scpane);

    // set up display panel
    DisplayPanel = new Panel();
    DisplayPanel.setBackground(Color.white);
    DisplayPanel.setLayout(new GridLayout(NumVisY, NumVisX, 5, 5));
    scpane.add(DisplayPanel);

    // set up display panel's individual VisAD displays
    DisplayCells = new FancySSCell[NumVisDisplays];

    for (int i=0; i<NumVisDisplays; i++) {
      String name = String.valueOf(Letters.charAt(i%NumVisX))
                   +String.valueOf(i/NumVisX+1);
      try {
        DisplayCells[i] = new FancySSCell(name, this);
        DisplayCells[i].addMouseListener(this);
        DisplayCells[i].setDisplayListener(this);
        DisplayCells[i].setMinSize(new Dimension(MIN_VIS_WIDTH,
                                                 MIN_VIS_HEIGHT));
        if (i == 0) DisplayCells[i].setSelected(true);
        DisplayPanel.add(DisplayCells[i]);
      }
      catch (VisADException exc) {
        JOptionPane.showMessageDialog(this,
            "Cannot create displays.",
            "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
      }
      catch (RemoteException exc) { }
    }

    // display window on screen
    setTitle(sTitle);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int appWidth = (int) (0.01*sWidth*screenSize.width);
    int appHeight = (int) (0.01*sHeight*screenSize.height);
    setSize(appWidth, appHeight);
    setLocation(screenSize.width/2 - appWidth/2,
                screenSize.height/2 - appHeight/2);
    setVisible(true);
  }

  /** Handles menubar/toolbar events. */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    // setup menu commands
    if (cmd.equals("fileExit")) quitProgram();

    // edit menu commands
    else if (cmd.equals("editCut")) cutCell();
    else if (cmd.equals("editCopy")) copyCell();
    else if (cmd.equals("editPaste")) pasteCell();
    else if (cmd.equals("editClear")) clearCell(true);

    // setup menu commands
    else if (cmd.equals("setupNew")) newFile();
    else if (cmd.equals("setupOpen")) openFile();
    else if (cmd.equals("setupSave")) saveFile();
    else if (cmd.equals("setupSaveas")) saveasFile();

    // cell menu commands
    else if (cmd.equals("fileOpen")) loadDataSet();

    // mappings menu commands
    else if (cmd.equals("dispEdit")) createMappings();

    // window menu commands
    else if (cmd.equals("winWidget")) {
      DisplayCells[CurDisplay].showWidgetFrame();
    }

    // formula bar commands
    else if (cmd.equals("formulaCancel")) refreshFormulaBar();
    else if (cmd.equals("formulaOk")) updateFormula();
    else if (cmd.equals("formulaChange")) {
      FormulaOk.requestFocus();
      updateFormula();
    }
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
      JOptionPane.showMessageDialog(this,
          "The file "+file+" does not exist",
          "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
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
      JOptionPane.showMessageDialog(this,
          "The file "+file+" could not be loaded",
          "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // reconstruct cells
    for (int i=0; i<DisplayCells.length; i++) {
      try {
        DisplayCells[i].setSSCellString(fileStrings[i]);
      }
      catch (VisADException exc) {
        JOptionPane.showMessageDialog(this,
            "Could not reconstruct spreadsheet",
            "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
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
        JOptionPane.showMessageDialog(this,
            "Could not save file "+CurrentFile.getName(),
            "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
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
        //ErrorBox.showError("Could not paste cell: "+exc.toString());
        JOptionPane.showMessageDialog(this,
            "Could not paste cell: "+exc.toString(),
            "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
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
      JOptionPane.showMessageDialog(this,
          "Cannot clear display mappings.",
          "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
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
    String newFormula = FormulaField.getText();
    File f = new File(newFormula);
    if (f.exists()) {
      // check if filename has changed from last entry
      String oldFormula = DisplayCells[CurDisplay].getFilename();
      if (oldFormula == null) oldFormula = "";
      if (oldFormula.equals(newFormula)) return;

      // try to load the file
      try {
        DisplayCells[CurDisplay].loadData(f);
      }
      catch (RemoteException exc) {
        JOptionPane.showMessageDialog(this, exc.toString(),
            "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
      }
      catch (BadFormException exc) {
        JOptionPane.showMessageDialog(this, exc.toString(),
            "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
      }
      catch (IOException exc) {
        JOptionPane.showMessageDialog(this, exc.toString(),
            "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
      }
      catch (VisADException exc) {
        JOptionPane.showMessageDialog(this, exc.toString(),
            "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
      }
    }
    else {
      // check if formula has changed from last entry
      String oldFormula = "";
      if (DisplayCells[CurDisplay].hasFormula()) {
        oldFormula = DisplayCells[CurDisplay].getFormula();
      }
      if (oldFormula.equalsIgnoreCase(newFormula)) return;

      // try to set the formula
      try {
        DisplayCells[CurDisplay].setFormula(newFormula);
      }
      catch (VisADException exc) {
        JOptionPane.showMessageDialog(this, exc.toString(),
            "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
      }
      catch (RemoteException exc) {
        JOptionPane.showMessageDialog(this, exc.toString(),
            "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  /** Update dimension checkbox menu items in Cell menu. */
  void refreshDisplayMenuItems() {
    // update dimension check marks
    int dim = DisplayCells[CurDisplay].getDimension();
    if (dim == BasicSSCell.JAVA3D_3D) CellDim3D3D.setState(true);
    else CellDim3D3D.setState(false);
    if (dim == BasicSSCell.JAVA2D_2D) CellDim2D2D.setState(true);
    else CellDim2D2D.setState(false);
    if (dim == BasicSSCell.JAVA3D_2D) CellDim2D3D.setState(true);
    else CellDim2D3D.setState(false);

    // update domain mapping scheme check marks
    int d = DisplayCells[CurDisplay].getMappingSchemeDomain();
    if (d == FancySSCell.IMAGE) DispImage.setState(true);
    else DispImage.setState(false);
    if (d == FancySSCell.SPHERICAL_IMAGE) DispSphereImage.setState(true);
    else DispSphereImage.setState(false);
    if (d == FancySSCell.SURFACE3D) DispSurface3D.setState(true);
    else DispSurface3D.setState(false);
    if (d == FancySSCell.SPHERICAL_SURFACE3D) {
      DispSphereSurface3D.setState(true);
    }
    else DispSphereSurface3D.setState(false);

    // update range mapping scheme check marks
    int r = DisplayCells[CurDisplay].getMappingSchemeRange();
    if (r == FancySSCell.COLOR) DispColor.setState(true);
    else DispColor.setState(false);
    if (r == FancySSCell.GRAYSCALE) DispGray.setState(true);
    else DispGray.setState(false);
    if (r == FancySSCell.CMY) DispCMY.setState(true);
    else DispCMY.setState(false);
    if (r == FancySSCell.HSV) DispHSV.setState(true);
    else DispHSV.setState(false);
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
      else if (i.equals("2-D (Java3D)")) {
        DisplayCells[CurDisplay].setDimension(true, false);
      }
      else if (i.equals("Image")) {
        DisplayCells[CurDisplay].setMappingSchemeDomain(FancySSCell.IMAGE);
      }
      else if (i.equals("Spherical image")) {
        DisplayCells[CurDisplay].setMappingSchemeDomain(
                                 FancySSCell.SPHERICAL_IMAGE);
      }
      else if (i.equals("3-D surface")) {
        DisplayCells[CurDisplay].setMappingSchemeDomain(
                                 FancySSCell.SURFACE3D);
      }
      else if (i.equals("3-D spherical surface")) {
        DisplayCells[CurDisplay].setMappingSchemeDomain(
                                 FancySSCell.SPHERICAL_SURFACE3D);
      }
      else if (i.equals("Color")) {
        DisplayCells[CurDisplay].setMappingSchemeRange(
                                 FancySSCell.COLOR);
      }
      else if (i.equals("Grayscale")) {
        DisplayCells[CurDisplay].setMappingSchemeRange(
                                 FancySSCell.GRAYSCALE);
      }
      else if (i.equals("CMY")) {
        DisplayCells[CurDisplay].setMappingSchemeRange(
                                 FancySSCell.CMY);
      }
      else if (i.equals("HSV")) {
        DisplayCells[CurDisplay].setMappingSchemeRange(
                                 FancySSCell.HSV);
      }
      refreshDisplayMenuItems();
    }
    catch (VisADException exc) {
      JOptionPane.showMessageDialog(this, "Cannot alter display dimension.",
          "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
    }
    catch (RemoteException exc) { }
  }

  /** Handles scrollbar changes. */
  public void adjustmentValueChanged(AdjustmentEvent e) {
    Adjustable a = e.getAdjustable();
    int value = a.getValue();

    if (a.getOrientation() == Adjustable.HORIZONTAL) {
      HorizLabels.getViewport().setViewPosition(new Point(value, 0));
    }
    else {  // a.getOrientation() == Adjustable.VERTICAL
      VertLabels.getViewport().setViewPosition(new Point(0, value));
    }
  }

  /** Handles display changes. */
  public void displayChanged(DisplayEvent e) {
    FancySSCell fcell = (FancySSCell)
                        BasicSSCell.getSSCellByDisplay(e.getDisplay());
    int c = -1;
    for (int i=0; i<NumVisDisplays; i++) {
      if (fcell == DisplayCells[i]) c = i;
    }
    selectCell(c);
  }

  /** Selects the specified cell, updating screen info. */
  void selectCell(int cell) {
    if (cell < 0 || cell >= NumVisDisplays || cell == CurDisplay) return;

    // update blue border on screen
    DisplayCells[CurDisplay].setSelected(false);
    DisplayCells[cell].setSelected(true);
    CurDisplay = cell;

    // update spreadsheet info
    refreshFormulaBar();
    refreshDisplayMenuItems();
  }

  /** Handles key presses. */
  public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();

    if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_LEFT
     || key == KeyEvent.VK_DOWN  || key == KeyEvent.VK_UP) {
      int c = CurDisplay;
      if (key == KeyEvent.VK_RIGHT && (CurDisplay+1) % NumVisX != 0) {
        c = CurDisplay + 1;
      }
      if (key == KeyEvent.VK_LEFT && CurDisplay % NumVisX != 0) {
        c = CurDisplay - 1;
      }
      if (key == KeyEvent.VK_DOWN) c = CurDisplay + NumVisX;
      if (key == KeyEvent.VK_UP) c = CurDisplay - NumVisX;
      selectCell(c);
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
    selectCell(cnum);
  }

  public void mouseReleased(MouseEvent e) { }

  public void mouseClicked(MouseEvent e) { }

  public void mouseEntered(MouseEvent e) { }

  public void mouseExited(MouseEvent e) { }

}

