
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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import visad.*;
import visad.data.BadFormException;
import visad.java3d.*;

/** SpreadSheet is a user interface for VisAD that supports
    multiple 3-D displays (FancySSCells).<P>*/
public class SpreadSheet extends JFrame implements ActionListener,
                                                   AdjustmentListener,
                                                   DisplayListener,
                                                   KeyListener,
                                                   ItemListener,
                                                   MouseListener,
                                                   MouseMotionListener,
                                                   SSCellListener {

  // starting size of the application, in percentage of screen size
  static final int WIDTH_PERCENT = 60;
  static final int HEIGHT_PERCENT = 75;

  // minimum VisAD display size, including display border
  static final int MIN_VIS_WIDTH = 120;
  static final int MIN_VIS_HEIGHT = 120;

  // spreadsheet letter order
  static final String Letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  // label constants
  static final int LABEL_WIDTH = 30;
  static final int LABEL_HEIGHT = 20;

  // this spreadsheet's base title
  String bTitle;

  // spreadsheet file dialog
  FileDialog SSFileDialog = null;

  // number of VisAD displays
  int NumVisX, NumVisY;

  // server for spreadsheet cells, if any
  RemoteServerImpl rsi = null;

  // whether this spreadsheet is a clone of another spreadsheet
  boolean IsRemote;

  // information needed for spreadsheet cloning
  RemoteDataReference RemoteColRow;

  // whether this JVM supports Java3D (detected on SpreadSheet launch)
  boolean CanDo3D = true;

  // options
  boolean AutoSwitch = true;
  boolean AutoDetect = true;
  boolean AutoShowControls = true;

  // display-related arrays and variables
  Panel DisplayPanel;
  JPanel ScrollPanel;
  ScrollPane SCPane;
  JViewport HorizLabels, VertLabels;
  JPanel[] HorizLabel, VertLabel;
  JComponent[] HorizDrag, VertDrag;
  JPanel HorizPanel, VertPanel;
  FancySSCell[][] DisplayCells = null;
  JTextField FormulaField;
  MenuItem EditPaste;
  MenuItem FileSave1, FileSave2, DispEdit;
  JButton ToolPaste;
  JButton ToolSave, ToolMap;
  JButton FormulaOk;
  CheckboxMenuItem CellDim3D3D, CellDim2D2D, CellDim2D3D;
  int CurX = 0;
  int CurY = 0;

  String Clipboard = null;
  File CurrentFile = null;

  /** main method; gateway into Spread Sheet user interface */
  public static void main(String[] argv) {
    String usage = "\n" +
                   "Usage: java [-mx###m] visad.ss.SpreadSheet [cols rows]\n" +
                   "       [-server server_name] [-client rmi_address]\n\n" +
                   "### = Maximum megabytes of memory to use\n" +
                   "cols = Number of columns in this Spread Sheet\n" +
                   "rows = Number of rows in this Spread Sheet\n" +
                   "-server server_name = Initialize this Spread Sheet as\n" +
                   "                      an RMI server named server_name\n" +
                   "-client rmi_address = Initialize this Spread Sheet as\n" +
                   "                      a clone of the Spread Sheet at\n" +
                   "                      rmi_address\n";
    int cols = 2;
    int rows = 2;
    String servname = null;
    String clonename = null;
    int len = argv.length;
    if (len > 0) {
      int ix = 0;

      // parse command line flags
      while (ix < len) {
        if (argv[ix].charAt(0) == '-') {
          if (argv[ix].equals("-server")) {
            if (ix < len - 1) servname = argv[++ix];
            else {
              System.out.println("You must specify a server name after " +
                                 "the '-server' flag!");
              System.out.println(usage);
              System.exit(4);
            }
          }
          else if (argv[ix].equals("-client")) {
            if (servname != null) {
              System.out.println("A spreadsheet cannot be both a server " +
                                 "and a clone!");
              System.out.println(usage);
              System.exit(3);
            }
            else if (ix < len - 1) {
              clonename = argv[++ix];
              System.out.println("Warning: spreadsheet cloning option is " +
                                 "not fully implemented!");
            }
            else {
              System.out.println("You must specify a server after " +
                                 "the '-client' flag!");
              System.out.println(usage);
              System.exit(5);
            }
          }
          else {
            // unknown flag
            if (!argv[ix].equals("-help")) {
              System.out.println("Unknown option: " + argv[ix]);
            }
            System.out.println(usage);
            System.exit(1);
          }
        }
        else {
          // parse number of rows and columns
          boolean success = true;
          if (ix < len-1) {
            try {
              cols = Integer.parseInt(argv[ix]);
              rows = Integer.parseInt(argv[ix+1]);
              ix++;
              if (rows < 1 || cols < 1 || cols > Letters.length()) {
                success = false;
              }
            }
            catch (NumberFormatException exc) {
              success = false;
            }
            if (!success) {
              System.out.println("Invalid number of columns and rows: " +
                                 argv[ix] + " x " + argv[ix+1]);
              System.out.println(usage);
              System.exit(2);
            }
          }
          else {
            System.out.println("Unknown option: " + argv[ix]);
            System.out.println(usage);
            System.exit(1);
          }
        }
        ix++;
      }
    }
    SpreadSheet ss = new SpreadSheet(WIDTH_PERCENT, HEIGHT_PERCENT,
                                     cols, rows, servname, clonename,
                                     "VisAD Spread Sheet");
  }

  /** constructor for the SpreadSheet class */
  public SpreadSheet(int sWidth, int sHeight, int cols, int rows,
                     String server, String clone, String sTitle) {
    bTitle = sTitle;
    NumVisX = cols;
    NumVisY = rows;
    MappingDialog.initDialog();
    addKeyListener(this);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        quitProgram();
      }
    });
    setBackground(Color.white);

    // test for Java3D availability
    try {
      DisplayImplJ3D test = new DisplayImplJ3D("test");
    }
    catch (NoClassDefFoundError err) {
      CanDo3D = false;
    }
    catch (UnsatisfiedLinkError err) {
      CanDo3D = false;
    }
    catch (Exception exc) {
      CanDo3D = false;
    }

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

    FileSave1 = new MenuItem("Export data to netCDF...");
    FileSave1.addActionListener(this);
    FileSave1.setActionCommand("fileSaveNetcdf");
    FileSave1.setEnabled(false);
    file.add(FileSave1);

    FileSave2 = new MenuItem("Export serialized data...");
    FileSave2.addActionListener(this);
    FileSave2.setActionCommand("fileSaveSerial");
    FileSave2.setEnabled(false);
    file.add(FileSave2);

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

    DispEdit = new MenuItem("Edit mappings...");
    DispEdit.addActionListener(this);
    DispEdit.setActionCommand("dispEdit");
    DispEdit.setEnabled(false);
    disp.add(DispEdit);
    disp.addSeparator();

    MenuItem dispAddCol = new MenuItem("Add column");
    dispAddCol.addActionListener(this);
    dispAddCol.setActionCommand("dispAddCol");
    disp.add(dispAddCol);

    MenuItem dispAddRow = new MenuItem("Add row");
    dispAddRow.addActionListener(this);
    dispAddRow.setActionCommand("dispAddRow");
    disp.add(dispAddRow);

    MenuItem dispDelCol = new MenuItem("Delete column");
    dispDelCol.addActionListener(this);
    dispDelCol.setActionCommand("dispDelCol");
    disp.add(dispDelCol);

    MenuItem dispDelRow = new MenuItem("Delete row");
    dispDelRow.addActionListener(this);
    dispDelRow.setActionCommand("dispDelRow");
    disp.add(dispDelRow);
    disp.addSeparator();

    CellDim3D3D = new CheckboxMenuItem("3-D (Java3D)", CanDo3D);
    CellDim3D3D.addItemListener(this);
    CellDim3D3D.setEnabled(CanDo3D);
    disp.add(CellDim3D3D);

    CellDim2D2D = new CheckboxMenuItem("2-D (Java2D)", !CanDo3D);
    CellDim2D2D.addItemListener(this);
    disp.add(CellDim2D2D);

    CellDim2D3D = new CheckboxMenuItem("2-D (Java3D)", false);
    CellDim2D3D.addItemListener(this);
    CellDim2D3D.setEnabled(CanDo3D);
    disp.add(CellDim2D3D);

    // options menu
    Menu options = new Menu("Options");
    menubar.add(options);

    if (!CanDo3D) AutoSwitch = false;
    CheckboxMenuItem optSwitch = new CheckboxMenuItem(
                     "Auto-switch to 3-D", AutoSwitch);
    optSwitch.addItemListener(this);
    optSwitch.setEnabled(CanDo3D);
    options.add(optSwitch);

    CheckboxMenuItem optAuto = new CheckboxMenuItem(
                     "Auto-detect mappings", AutoDetect);
    optAuto.addItemListener(this);
    options.add(optAuto);

    CheckboxMenuItem optASC = new CheckboxMenuItem(
                     "Auto-display controls", AutoShowControls);
    optASC.addItemListener(this);
    options.add(optASC);
    options.addSeparator();

    MenuItem optWidget = new MenuItem("Show controls");
    optWidget.addActionListener(this);
    optWidget.setActionCommand("optWidget");
    options.add(optWidget);

    // set up toolbar
    URL url;
    JToolBar toolbar = new JToolBar();
    toolbar.setBackground(Color.lightGray);
    toolbar.setBorder(new EtchedBorder());
    toolbar.setFloatable(false);
    pane.add(toolbar);

    // file menu toolbar icons
    url = SpreadSheet.class.getResource("open.gif");
    ImageIcon toolFileOpen = new ImageIcon(url);
    if (toolFileOpen != null) {
      JButton b = new JButton(toolFileOpen);
      b.setAlignmentY(JButton.CENTER_ALIGNMENT);
      b.setToolTipText("Import data");
      b.addActionListener(this);
      b.setActionCommand("fileOpen");
      toolbar.add(b);
    }
    url = SpreadSheet.class.getResource("save.gif");
    ImageIcon toolFileSave = new ImageIcon(url);
    if (toolFileSave != null) {
      ToolSave = new JButton(toolFileSave);
      ToolSave.setAlignmentY(JButton.CENTER_ALIGNMENT);
      ToolSave.setToolTipText("Export data to netCDF");
      ToolSave.addActionListener(this);
      ToolSave.setActionCommand("fileSaveNetcdf");
      ToolSave.setEnabled(false);
      toolbar.add(ToolSave);
    }
    toolbar.addSeparator();

    // edit menu toolbar icons
    url = SpreadSheet.class.getResource("cut.gif");
    ImageIcon toolEditCut = new ImageIcon(url);
    if (toolEditCut != null) {
      JButton b = new JButton(toolEditCut);
      b.setAlignmentY(JButton.CENTER_ALIGNMENT);
      b.setToolTipText("Cut");
      b.addActionListener(this);
      b.setActionCommand("editCut");
      toolbar.add(b);
    }
    url = SpreadSheet.class.getResource("copy.gif");
    ImageIcon toolEditCopy = new ImageIcon(url);
    if (toolEditCopy != null) {
      JButton b = new JButton(toolEditCopy);
      b.setAlignmentY(JButton.CENTER_ALIGNMENT);
      b.setToolTipText("Copy");
      b.addActionListener(this);
      b.setActionCommand("editCopy");
      toolbar.add(b);
    }
    url = SpreadSheet.class.getResource("paste.gif");
    ImageIcon toolEditPaste = new ImageIcon(url);
    if (toolEditPaste != null) {
      ToolPaste = new JButton(toolEditPaste);
      ToolPaste.setAlignmentY(JButton.CENTER_ALIGNMENT);
      ToolPaste.setToolTipText("Paste");
      ToolPaste.addActionListener(this);
      ToolPaste.setActionCommand("editPaste");
      ToolPaste.setEnabled(false);
      toolbar.add(ToolPaste);
    }
    toolbar.addSeparator();

    // mappings menu toolbar icons
    url = SpreadSheet.class.getResource("mappings.gif");
    ImageIcon toolMappingsEdit = new ImageIcon(url);
    if (toolMappingsEdit != null) {
      ToolMap = new JButton(toolMappingsEdit);
      ToolMap.setAlignmentY(JButton.CENTER_ALIGNMENT);
      ToolMap.setToolTipText("Edit mappings");
      ToolMap.addActionListener(this);
      ToolMap.setActionCommand("dispEdit");
      ToolMap.setEnabled(false);
      toolbar.add(ToolMap);
    }

    // window menu toolbar icons
    url = SpreadSheet.class.getResource("show.gif");
    ImageIcon winShowControls = new ImageIcon(url);
    if (winShowControls != null) {
      JButton b = new JButton(winShowControls);
      b.setAlignmentY(JButton.CENTER_ALIGNMENT);
      b.setToolTipText("Show controls");
      b.addActionListener(this);
      b.setActionCommand("optWidget");
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

    url = SpreadSheet.class.getResource("cancel.gif");
    ImageIcon cancelIcon = new ImageIcon(url);
    JButton formulaCancel = new JButton(cancelIcon);
    formulaCancel.setAlignmentY(JButton.CENTER_ALIGNMENT);
    formulaCancel.setToolTipText("Cancel formula entry");
    formulaCancel.addActionListener(this);
    formulaCancel.setActionCommand("formulaCancel");
    Dimension size = new Dimension(cancelIcon.getIconWidth()+4,
                                   cancelIcon.getIconHeight()+4);
    formulaCancel.setPreferredSize(size);
    formulaPanel.add(formulaCancel);

    url = SpreadSheet.class.getResource("ok.gif");
    ImageIcon okIcon = new ImageIcon(url);
    FormulaOk = new JButton(okIcon);
    FormulaOk.setAlignmentY(JButton.CENTER_ALIGNMENT);
    FormulaOk.setToolTipText("Confirm formula entry");
    FormulaOk.addActionListener(this);
    FormulaOk.setActionCommand("formulaOk");
    size = new Dimension(okIcon.getIconWidth()+4, okIcon.getIconHeight()+4);
    FormulaOk.setPreferredSize(size);
    formulaPanel.add(FormulaOk);

    FormulaField = new JTextField();

    // limit formula bar to one line in height
    Dimension msize = FormulaField.getMaximumSize();
    Dimension psize = FormulaField.getPreferredSize();
    msize.height = psize.height;
    FormulaField.setMaximumSize(msize);

    /* When a tool tip is being displayed, there is a bug where GUI
       components cannot be repainted; this tool tip has been removed
       to decrease the frequency of this bug's occurrence.
    FormulaField.setToolTipText("Enter a file name, URL, RMI address, " +
                                "or formula");
    */
    FormulaField.addActionListener(this);
    FormulaField.setActionCommand("formulaChange");
    formulaPanel.add(FormulaField);

    url = SpreadSheet.class.getResource("import.gif");
    ImageIcon importIcon = new ImageIcon(url);
    JButton formulaImport = new JButton(importIcon);
    formulaImport.setAlignmentY(JButton.CENTER_ALIGNMENT);
    formulaImport.setToolTipText("Import data");
    formulaImport.addActionListener(this);
    formulaImport.setActionCommand("fileOpen");
    size = new Dimension(importIcon.getIconWidth()+4,
                         importIcon.getIconHeight()+4);
    formulaImport.setPreferredSize(size);
    formulaPanel.add(formulaImport);

    // set up horizontal spreadsheet cell labels
    JPanel horizShell = new JPanel();
    horizShell.setBackground(Color.white);
    horizShell.setLayout(new BoxLayout(horizShell, BoxLayout.X_AXIS));
    horizShell.add(Box.createRigidArea(new Dimension(LABEL_WIDTH+10, 0)));
    pane.add(horizShell);

    HorizPanel = new JPanel() {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, LABEL_HEIGHT);
      }
    };
    constructHorizontalLabels();
    JViewport hl = new JViewport() {
      public Dimension getMinimumSize() {
        return new Dimension(0, LABEL_HEIGHT+4);
      }
      public Dimension getPreferredSize() {
        return new Dimension(0, LABEL_HEIGHT+4);
      }
      public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, LABEL_HEIGHT+4);
      }
    };
    HorizLabels = hl;
    HorizLabels.setView(HorizPanel);
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
    vertShell.setAlignmentY(JPanel.CENTER_ALIGNMENT);
    vertShell.setBackground(Color.white);
    vertShell.setLayout(new BoxLayout(vertShell, BoxLayout.X_AXIS));
    mainPanel.add(Box.createRigidArea(new Dimension(6, 0)));
    mainPanel.add(vertShell);

    VertPanel = new JPanel() {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(LABEL_WIDTH, d.height);
      }
    };
    constructVerticalLabels();
    JViewport vl = new JViewport() {
      public Dimension getMinimumSize() {
        return new Dimension(LABEL_WIDTH+4, 0);
      }
      public Dimension getPreferredSize() {
        return new Dimension(LABEL_WIDTH+4, 0);
      }
      public Dimension getMaximumSize() {
        return new Dimension(LABEL_WIDTH+4, Integer.MAX_VALUE);
      }
    };
    VertLabels = vl;
    VertLabels.setView(VertPanel);
    vertShell.add(VertLabels);

    // set up scroll pane's panel
    ScrollPanel = new JPanel();
    ScrollPanel.setBackground(Color.white);
    ScrollPanel.setLayout(new BoxLayout(ScrollPanel, BoxLayout.X_AXIS));
    mainPanel.add(ScrollPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(6, 0)));

    // set up scroll pane for VisAD Displays
    SCPane = new ScrollPane() {
      public Dimension getPreferredSize() {
        return new Dimension(0, 0);
      }
    };
    Adjustable hadj = SCPane.getHAdjustable();
    Adjustable vadj = SCPane.getVAdjustable();
    hadj.setBlockIncrement(MIN_VIS_WIDTH);
    hadj.setUnitIncrement(MIN_VIS_WIDTH/4);
    hadj.addAdjustmentListener(this);
    vadj.setBlockIncrement(MIN_VIS_HEIGHT);
    vadj.setUnitIncrement(MIN_VIS_HEIGHT/4);
    vadj.addAdjustmentListener(this);
    ScrollPanel.add(SCPane);

    // set up display panel
    DisplayPanel = new Panel();
    DisplayPanel.setBackground(Color.darkGray);
    SCPane.add(DisplayPanel);

    DataReferenceImpl lColRow = null;
    if (server != null) {
      // initialize RemoteServer
      boolean success = true;
      try {
        rsi = new RemoteServerImpl();
        Naming.rebind("//:/" + server, rsi);
      }
      catch (java.rmi.ConnectException exc) {
        displayErrorMessage("Unable to export cells as RMI addresses. " +
          "Make sure you are running rmiregistry before launching the " +
          "Spread Sheet in server mode.", "Failed to initialize RemoteServer");
        success = false;
      }
      catch (MalformedURLException exc) {
        displayErrorMessage("Unable to export cells as RMI addresses. " +
          "The name \"" + server + "\" is not valid.",
          "Failed to initialize RemoteServer");
        success = false;
      }
      catch (RemoteException exc) {
        displayErrorMessage("Unable to export cells as RMI addresses: " +
          exc.getMessage(), "Failed to initialize RemoteServer");
        success = false;
      }

      // set up info for spreadsheet cloning
      try {
        lColRow = new DataReferenceImpl("ColRow");
        RemoteColRow = new RemoteDataReferenceImpl(lColRow);
        rsi.addDataReference((RemoteDataReferenceImpl) RemoteColRow);
      }
      catch (VisADException exc) {
        displayErrorMessage("Unable to export cells as RMI addresses. " +
          "An error occurred setting up the necessary data: " +
          exc.getMessage(), "Failed to initialize RemoteServer");
        success = false;
      }
      catch (RemoteException exc) {
        displayErrorMessage("Unable to export cells as RMI addresses. " +
          "A remote error occurred setting up the necessary data: " +
          exc.getMessage(), "Failed to initialize RemoteServer");
        success = false;
      }

      if (success) bTitle = bTitle + " (" + server + ")";
      else rsi = null;
    }

    // construct spreadsheet cells
    RemoteServer rs = null;
    if (clone == null) {
      // construct cells from scratch
      constructSpreadsheetCells(null);
      if (rsi != null) synchColRow();
    }
    else {
      // construct cells from specified server
      boolean success = true;
      try {
        rs = (RemoteServer) Naming.lookup("//" + clone);
      }
      catch (NotBoundException exc) {
        displayErrorMessage("Unable to clone the spreadsheet at " + clone +
          ". The server could not be found.", "Failed to clone spreadsheet");
        success = false;
      }
      catch (RemoteException exc) {
        displayErrorMessage("Unable to clone the spreadsheet at " + clone +
          ". A remote error occurred: " + exc.getMessage(),
          "Failed to clone spreadsheet");
        success = false;
      }
      catch (MalformedURLException exc) {
        displayErrorMessage("Unable to clone the spreadsheet at " + clone +
          ". The server name is not valid.", "Failed to clone spreadsheet");
        success = false;
      }

      if (success) {
        // get info for spreadsheet cloning and construct spreadsheet clone
        try {
          RemoteColRow = rs.getDataReference("ColRow");
          
          // extract cell name information
          String[][] cellNames = getNewCellNames();
          if (cellNames == null) {
            displayErrorMessage("Unable to clone the spreadsheet at " + clone +
              ". Could not obtain the server's cell names.",
              "Failed to clone spreadsheet");
            success = false;
          }
          else {
            NumVisX = cellNames.length;
            NumVisY = cellNames[0].length;
            reconstructLabels(cellNames);
            constructSpreadsheetCells(cellNames, rs);
          }
        }
        catch (VisADException exc) {
          displayErrorMessage("Unable to clone the spreadsheet at " + clone +
            ". An error occurred while downloading the necessary data: " +
            exc.getMessage(), "Failed to clone spreadsheet");
          success = false;
        }
        catch (RemoteException exc) {
          displayErrorMessage("Unable to clone the spreadsheet at " + clone +
            ". Could not download the necessary data: " + exc.getMessage(),
            "Failed to clone spreadsheet");
          success = false;
        }
      }

      if (success) bTitle = bTitle + " [collaborative mode: " + clone + "]";
      else {
        // construct a normal spreadsheet (i.e., not a clone)
        constructSpreadsheetCells(null);
      }
      IsRemote = success;
    }

    if (rsi != null || IsRemote) {
      // update spreadsheet when remote row and column information changes
      final RemoteServer frs = rs;
      CellImpl lColRowCell = new CellImpl() {
        public void doAction() {
          // extract new cell information
          boolean b = getColRowServer();
          if (b || !IsRemote) { // keep client from receiving its own updates
            String[][] cellNames = getNewCellNames();
            if (cellNames == null) return;
            int oldNVX = NumVisX;
            int oldNVY = NumVisY;
            NumVisX = cellNames.length;
            NumVisY = cellNames[0].length;
            if (NumVisX != oldNVX || NumVisY != oldNVY) {
              // reconstruct spreadsheet cells and labels
              reconstructSpreadsheet(cellNames, oldNVX, oldNVY, frs);
              if (!IsRemote) synchColRow();
            }
          }
        }
      };
      try {
        RemoteCellImpl rColRowCell = new RemoteCellImpl(lColRowCell);
        rColRowCell.addReference(RemoteColRow);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) {
        try {
          lColRowCell.addReference(lColRow);
        }
        catch (VisADException exc2) { }
        catch (RemoteException exc2) { }
      }
    }

    // display window on screen
    setTitle(bTitle);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int appWidth = (int) (0.01 * sWidth * screenSize.width);
    int appHeight = (int) (0.01 * sHeight * screenSize.height);
    setSize(appWidth, appHeight);
    setLocation(screenSize.width/2 - appWidth/2,
                screenSize.height/2 - appHeight/2);
    setVisible(true);
  }

  /** Handles menubar/toolbar events */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    // file menu commands
    if (cmd.equals("fileOpen")) loadDataSet();
    else if (cmd.equals("fileSaveNetcdf")) exportDataSetNetcdf();
    else if (cmd.equals("fileSaveSerial")) exportDataSetSerial();
    else if (cmd.equals("fileExit")) {
      DisplayCells[CurX][CurY].hideWidgetFrame();
      setVisible(false);
      quitProgram();
    }

    // edit menu commands
    else if (cmd.equals("editCut")) cutCell();
    else if (cmd.equals("editCopy")) copyCell();
    else if (cmd.equals("editPaste")) pasteCell();
    else if (cmd.equals("editClear")) clearCell(true);

    // setup menu commands
    else if (cmd.equals("setupNew")) newFile(true);
    else if (cmd.equals("setupOpen")) openFile();
    else if (cmd.equals("setupSave")) saveFile();
    else if (cmd.equals("setupSaveas")) saveasFile();

    // mappings menu commands
    else if (cmd.equals("dispEdit")) createMappings();
    else if (cmd.equals("dispAddCol")) addColumn();
    else if (cmd.equals("dispAddRow")) addRow();
    else if (cmd.equals("dispDelCol")) deleteColumn();
    else if (cmd.equals("dispDelRow")) deleteRow();

    // window menu commands
    else if (cmd.equals("optWidget")) {
      DisplayCells[CurX][CurY].showWidgetFrame();
    }

    // formula bar commands
    else if (cmd.equals("formulaCancel")) refreshFormulaBar();
    else if (cmd.equals("formulaOk")) updateFormula();
    else if (cmd.equals("formulaChange")) {
      FormulaOk.requestFocus();
      updateFormula();
    }
  }

  /** Creates a new spreadsheet file; returns true if successful */
  public boolean newFile(boolean safe) {
    if (safe) {
      int ans = JOptionPane.showConfirmDialog(this,
                "Clear all spreadsheet cells?", "Are you sure?",
                JOptionPane.YES_NO_OPTION);
      if (ans != JOptionPane.YES_OPTION) return false;
    }

    // clear all cells (in smart order to prevent errors)
    boolean[][] b = new boolean[NumVisX][NumVisY];
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) b[i][j] = false;
    }
    boolean w = true;
    while (w) {
      for (int j=0; j<NumVisY; j++) {
        for (int i=0; i<NumVisX; i++) {
          if (!DisplayCells[i][j].othersDepend()) {
            try {
              DisplayCells[i][j].clearCell();
              b[i][j] = true;
            }
            catch (VisADException exc) { }
            catch (RemoteException exc) { }
          }
        }
      }
      w = false;
      for (int j=0; j<NumVisY; j++) {
        for (int i=0; i<NumVisX; i++) if (!b[i][j]) w = true;
      }
    }
    CurrentFile = null;
    setTitle(bTitle);
    return true;
  }

  /** Opens an existing spreadsheet file */
  public void openFile() {
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
      displayErrorMessage("The file " + file + " does not exist",
        "VisAD SpreadSheet error");
      return;
    }

    // temporarily disable auto-detect and auto-show
    boolean autoD = AutoDetect;
    boolean autoSC = AutoShowControls;
    setAutoDetect(false);
    setAutoShowControls(false);

    // clear all cells
    newFile(false);

    // load file
    String[][] cellNames = null;
    String[][] fileStrings = null;
    int oldNVX = NumVisX;
    int oldNVY = NumVisY;
    try {
      FileReader fr = new FileReader(f);
      char[] buff = new char[8192];
      boolean done = false;

      // get spreadsheet dimensions
      int count = 0;
      int ch;
      do {
        ch = fr.read();
        if (ch == 'x') {
          String s = new String(buff, 0, count);
          NumVisX = Integer.parseInt(s.substring(0, s.length()-1));
          count = 0;
        }
        else if (ch == '\n') {
          String s = new String(buff, 0, count);
          NumVisY = Integer.parseInt(s.substring(1));
        }
        else buff[count++] = (char) ch;
      } while (ch != '\n');

      // get cell information
      cellNames = new String[NumVisX][NumVisY];
      fileStrings = new String[NumVisX][NumVisY];
      for (int j=0; j<NumVisY; j++) {
        for (int i=0; i<NumVisX; i++) {
          count = 0;
          int lncnt = 0;
          // get cell name
          while (ch != '[') ch = fr.read();
          do {
            ch = fr.read();
            buff[count++] = (char) ch;
          } while (ch != '\n');
          String s = new String(buff, 0, count);
          cellNames[i][j] = s.substring(0, s.length()-2);
          count = 0;

          // get cell reconstruction string
          while (lncnt < 5) {
            ch = fr.read();
            buff[count++] = (char) ch;
            if (ch == '\n') lncnt++;
          }
          fileStrings[i][j] = new String(buff, 0, count);
        }
      }
      fr.close();
    }
    catch (NumberFormatException exc) {
      displayErrorMessage("The file " + file + " could not be loaded. " +
        "Its format is incorrect.", "VisAD SpreadSheet error");
      return;
    }
    catch (IOException exc) {
      displayErrorMessage("The file " + file + " could not be loaded. " +
        "Its format is incorrect.", "VisAD SpreadSheet error");
      return;
    }

    // reconstruct spreadsheet cells and labels
    reconstructSpreadsheet(cellNames, oldNVX, oldNVY, null);
    synchColRow();

    // set each cell's string
    for (int i=0; i<NumVisX; i++) {
      for (int j=0; j<NumVisY; j++) {
        try {
          DisplayCells[i][j].setSSCellString(fileStrings[i][j]);
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }
    }

    // re-enable auto-detect and auto-show if necessary
    setAutoDetect(autoD);
    setAutoShowControls(autoSC);

    CurrentFile = f;
    setTitle(bTitle + " - " + f.getPath());
  }

  /** Saves a spreadsheet file under its current name */
  public void saveFile() {
    if (CurrentFile == null) saveasFile();
    else {
      try {
        FileWriter fw = new FileWriter(CurrentFile);
        String s = NumVisX + " x " + NumVisY + "\n\n";
        char[] sc = s.toCharArray();
        fw.write(sc, 0, sc.length);
        for (int j=0; j<NumVisY; j++) {
          for (int i=0; i<NumVisX; i++) {
            s = "[" + DisplayCells[i][j].getName() + "]\n";
            sc = s.toCharArray();
            fw.write(sc, 0, sc.length);
            s = DisplayCells[i][j].getSSCellString() + "\n";
            sc = s.toCharArray();
            fw.write(sc, 0, sc.length);
          }
        }
        fw.close();
      }
      catch (IOException exc) {
        displayErrorMessage("Could not save file " + CurrentFile.getName() +
          ". Make sure there is enough disk space.",
          "VisAD SpreadSheet error");
      }
    }
  }

  /** Saves a spreadsheet file under a new name */
  public void saveasFile() {
    if (SSFileDialog == null) SSFileDialog = new FileDialog(this);
    SSFileDialog.setMode(FileDialog.SAVE);
    SSFileDialog.setVisible(true);

    // get file and make sure it is valid
    String file = SSFileDialog.getFile();
    if (file == null) return;
    String dir = SSFileDialog.getDirectory();
    if (dir == null) return;
    File f = new File(dir, file);
    CurrentFile = f;
    setTitle(bTitle + " - " + f.getPath());
    saveFile();
  }

  /** Does any necessary clean-up, then quits the program */
  public void quitProgram() {
    // wait for files to finish saving
    Thread t = new Thread() {
      public void run() {
        boolean b = BasicSSCell.isSaving();
        JFrame f = new JFrame("Please wait");
        if (b) {
          // display "please wait" message in new frame
          f.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          JPanel p = new JPanel();
          f.setContentPane(p);
          p.setBorder(new EmptyBorder(10, 20, 10, 20));
          p.setLayout(new BorderLayout());
          p.add("Center", new JLabel("Please wait while the VisAD " +
                          "Spread Sheet finishes saving files..."));
          f.setResizable(false);
          f.pack();
          Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
          Dimension fSize = f.getSize();
          f.setLocation(sSize.width/2 - fSize.width/2,
                        sSize.height/2 - fSize.height/2);
          f.setVisible(true);
        }
        while (BasicSSCell.isSaving()) {
          try {
            sleep(200);
          }
          catch (InterruptedException exc) { }
        }
        if (b) {
          f.setCursor(Cursor.getDefaultCursor());
          f.setVisible(false);
        }
        System.exit(0);
      }
    };
    t.start();
  }

  /** Moves a cell from the screen to the clipboard */
  public void cutCell() {
    if (DisplayCells[CurX][CurY].confirmClear()) {
      copyCell();
      clearCell(false);
    }
  }

  /** Copies a cell from the screen to the clipboard */
  public void copyCell() {
    Clipboard = DisplayCells[CurX][CurY].getSSCellString();
    EditPaste.setEnabled(true);
    ToolPaste.setEnabled(true);
  }

  /** Copies a cell from the clipboard to the screen */
  public void pasteCell() {
    if (Clipboard != null) {
      try {
        boolean b = DisplayCells[CurX][CurY].getAutoDetect();
        DisplayCells[CurX][CurY].setAutoDetect(false);
        DisplayCells[CurX][CurY].setSSCellString(Clipboard);
        DisplayCells[CurX][CurY].setAutoDetect(b);
      }
      catch (VisADException exc) {
        displayErrorMessage("Cannot paste cell: " + exc.getMessage(),
          "VisAD SpreadSheet error");
      }
      catch (RemoteException exc) {
        displayErrorMessage("Cannot paste cell: " + exc.getMessage(),
          "VisAD SpreadSheet error");
      }
    }
  }

  /** Clears the mappings and formula of the current cell */
  public void clearCell(boolean checkSafe) {
    try {
      if (checkSafe) DisplayCells[CurX][CurY].smartClear();
      else DisplayCells[CurX][CurY].clearCell();
    }
    catch (VisADException exc) {
      displayErrorMessage("Cannot clear display mappings: " + exc.getMessage(),
        "VisAD SpreadSheet error");
    }
    catch (RemoteException exc) {
      displayErrorMessage("Cannot clear display mappings: " + exc.getMessage(),
        "VisAD SpreadSheet error");
    }
    refreshFormulaBar();
    refreshMenuCommands();
  }

  /** Allows the user to specify mappings from Data to Display */
  public void createMappings() {
    DisplayCells[CurX][CurY].addMapDialog();
  }

  /** Allows the user to import a data set */
  public void loadDataSet() {
    DisplayCells[CurX][CurY].loadDataDialog();
  }

  /** Allows the user to export a data set to netCDF format */
  public void exportDataSetNetcdf() {
    DisplayCells[CurX][CurY].saveDataDialog(true);
  }

  /** Allow the user to export a data set to serialized data format */
  public void exportDataSetSerial() {
    DisplayCells[CurX][CurY].saveDataDialog(false);
  }

  /** Enable or disable certain menu items depending on whether
      this cell has data */
  void refreshMenuCommands() {
    boolean b = DisplayCells[CurX][CurY].hasData();
    DispEdit.setEnabled(b);
    ToolMap.setEnabled(b);
    FileSave1.setEnabled(b);
    FileSave2.setEnabled(b);
    ToolSave.setEnabled(b);
  }

  private boolean getColRowServer() {
    Tuple t = null;
    Real bit = null;
    try {
      t = (Tuple) RemoteColRow.getData();
      bit = (Real) t.getComponent(0);
      return (((int) bit.getValue()) == 0);
    }
    catch (NullPointerException exc) { }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    return true;
  }

  private String[][] getNewCellNames() {
    // extract new row and column information
    Tuple t = null;
    Tuple tc = null;
    RealTuple tr = null;
    try {
      t = (Tuple) RemoteColRow.getData();
      tc = (Tuple) t.getComponent(1);
      tr = (RealTuple) t.getComponent(2);
    }
    catch (NullPointerException exc) { }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    if (tc == null || tr == null) return null;
    int collen = tc.getDimension();
    int rowlen = tr.getDimension();
    if (rowlen < 1 || collen < 1) return null;
    String[] colNames = new String[collen];
    int[] rowNames = new int[rowlen];
    String[][] cellNames = new String[collen][rowlen];
    for (int i=0; i<collen; i++) {
      Text txt = null;
      try {
        txt = (Text) tc.getComponent(i);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
      if (txt == null) return null;
      colNames[i] = txt.getValue();
    }
    for (int j=0; j<rowlen; j++) {
      Real r = null;
      try {
        r = (Real) tr.getComponent(j);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
      if (r == null) return null;
      rowNames[j] = (int) r.getValue();
    }
    for (int i=0; i<collen; i++) {
      for (int j=0; j<rowlen; j++) {
        cellNames[i][j] = colNames[i] + rowNames[j];
      }
    }

    return cellNames;
  }

  private void constructSpreadsheetCells(RemoteServer rs) {
    String[][] labels = new String[NumVisX][NumVisY];
    for (int i=0; i<NumVisX; i++) {
      for (int j=0; j<NumVisY; j++) {
        labels[i][j] = "" + Letters.charAt(i) + (j+1);
      }
    }
    constructSpreadsheetCells(labels, rs);
  }

  private void constructSpreadsheetCells(String[][] l, RemoteServer rs) {
    DisplayPanel.setLayout(new SSLayout(NumVisX, NumVisY, MIN_VIS_WIDTH,
                                        MIN_VIS_HEIGHT, 5, 5, false));
    DisplayCells = new FancySSCell[NumVisX][NumVisY];
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) {
        try {
          DisplayCells[i][j] = new FancySSCell(l[i][j], rs, this);
          DisplayCells[i][j].addSSCellChangeListener(this);
          DisplayCells[i][j].addMouseListener(this);
          DisplayCells[i][j].setAutoSwitch(AutoSwitch);
          DisplayCells[i][j].setAutoDetect(AutoDetect);
          DisplayCells[i][j].setAutoShowControls(AutoShowControls);
          if (rs == null) DisplayCells[i][j].setDimension(!CanDo3D, !CanDo3D);
          DisplayCells[i][j].addDisplayListener(this);
          DisplayCells[i][j].setPreferredSize(new Dimension(MIN_VIS_WIDTH,
                                                            MIN_VIS_HEIGHT));
          if (i == 0 && j == 0) selectCell(i, j);
          if (rsi != null) {
            // add new cell to server
            DisplayCells[i][j].addToRemoteServer(rsi);
          }
          DisplayPanel.add(DisplayCells[i][j]);
        }
        catch (VisADException exc) {
          displayErrorMessage("Cannot construct spreadsheet cells. " +
            "An error occurred: " + exc.getMessage(),
            "VisAD SpreadSheet error");
        }
        catch (RemoteException exc) {
          displayErrorMessage("Cannot construct spreadsheet cells. " +
            "A remote error occurred: " + exc.getMessage(),
            "VisAD SpreadSheet error");
        }
      }
    }
  }

  private void constructHorizontalLabels() {
    String[] labels = new String[NumVisX];
    for (int i=0; i<NumVisX; i++) labels[i] = "" + Letters.charAt(i);
    constructHorizontalLabels(labels);
  }

  private void constructHorizontalLabels(String[] l) {
    HorizPanel.setLayout(new SSLayout(2*NumVisX-1, 1, MIN_VIS_WIDTH,
                                      LABEL_HEIGHT, 0, 0, true));
    HorizLabel = new JPanel[NumVisX];
    HorizDrag = new JComponent[NumVisX-1];
    for (int i=0; i<NumVisX; i++) {
      HorizLabel[i] = new JPanel();
      HorizLabel[i].setBorder(new LineBorder(Color.black, 1));
      HorizLabel[i].setLayout(new BorderLayout());
      HorizLabel[i].setPreferredSize(new Dimension(MIN_VIS_WIDTH,
                                                   LABEL_HEIGHT));
      HorizLabel[i].add("Center", new JLabel(l[i], SwingConstants.CENTER));
      HorizPanel.add(HorizLabel[i]);
      if (i < NumVisX-1) {
        HorizDrag[i] = new JComponent() {
          public void paint(Graphics g) {
            Dimension d = getSize();
            g.setColor(Color.black);
            g.drawRect(0, 0, d.width - 1, d.height - 1);
            g.setColor(Color.yellow);
            g.fillRect(1, 1, d.width - 2, d.height - 2);
          }
        };
        HorizDrag[i].setPreferredSize(new Dimension(5, 0));
        HorizDrag[i].addMouseListener(this);
        HorizDrag[i].addMouseMotionListener(this);
        HorizPanel.add(HorizDrag[i]);
      }
    }
  }

  private void constructVerticalLabels() {
    String[] labels = new String[NumVisY];
    for (int i=0; i<NumVisY; i++) labels[i] = "" + (i+1);
    constructVerticalLabels(labels);
  }

  private void constructVerticalLabels(String[] l) {
    VertPanel.setLayout(new SSLayout(1, 2*NumVisY-1, LABEL_WIDTH,
                                     MIN_VIS_HEIGHT, 0, 0, true));
    VertLabel = new JPanel[NumVisY];
    VertDrag = new JComponent[NumVisY-1];
    for (int i=0; i<NumVisY; i++) {
      VertLabel[i] = new JPanel();
      VertLabel[i].setBorder(new LineBorder(Color.black, 1));
      VertLabel[i].setLayout(new BorderLayout());
      VertLabel[i].setPreferredSize(new Dimension(LABEL_WIDTH,
                                                  MIN_VIS_HEIGHT));
      VertLabel[i].add("Center", new JLabel(l[i], SwingConstants.CENTER));
      VertPanel.add(VertLabel[i]);
      if (i < NumVisY-1) {
        VertDrag[i] = new JComponent() {
          public void paint(Graphics g) {
            Dimension s = getSize();
            g.setColor(Color.black);
            g.drawRect(0, 0, s.width - 1, s.height - 1);
            g.setColor(Color.yellow);
            g.fillRect(1, 1, s.width - 2, s.height - 2);
          }
        };
        VertDrag[i].setBackground(Color.white);
        VertDrag[i].setPreferredSize(new Dimension(0, 5));
        VertDrag[i].addMouseListener(this);
        VertDrag[i].addMouseMotionListener(this);
        VertPanel.add(VertDrag[i]);
      }
    }
  }

  private void reconstructLabels(String[][] cellNames) {
    // reconstruct horizontal labels
    HorizPanel.removeAll();
    String[] hLabels = new String[NumVisX];
    for (int i=0; i<NumVisX; i++) hLabels[i] = "" + cellNames[i][0].charAt(0);
    constructHorizontalLabels(hLabels);

    // reconstruct vertical labels
    VertPanel.removeAll();
    String[] vLabels = new String[NumVisY];
    for (int j=0; j<NumVisY; j++) vLabels[j] = cellNames[0][j].substring(1);
    constructVerticalLabels(vLabels);
  }

  private void reconstructSpreadsheet(String[][] cellNames, int ox, int oy,
                                      RemoteServer rs) {
    // reconstruct labels
    reconstructLabels(cellNames);

    // reconstruct spreadsheet cells
    DisplayPanel.removeAll();
    for (int i=0; i<ox; i++) {
      for (int j=0; j<oy; j++) {
        try {
          DisplayCells[i][j].destroyCell();
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
        DisplayCells[i][j] = null;
      }
    }
    constructSpreadsheetCells(cellNames, rs);

    // refresh display
    HorizPanel.doLayout();
    for (int i=0; i<NumVisX; i++) HorizLabel[i].doLayout();
    VertPanel.doLayout();
    for (int j=0; j<NumVisY; j++) VertLabel[j].doLayout();
    DisplayPanel.doLayout();
    SCPane.doLayout();
  }

  private void reconstructHoriz(JPanel[] newLabels, JComponent[] newDrag,
                                FancySSCell[][] fcells) {
    // reconstruct horizontal spreadsheet label layout
    HorizLabel = newLabels;
    HorizDrag = newDrag;
    HorizPanel.setLayout(new SSLayout(2*NumVisX-1, 1, MIN_VIS_WIDTH,
                                      LABEL_HEIGHT, 0, 0, true));
    for (int i=0; i<NumVisX; i++) {
      HorizPanel.add(HorizLabel[i]);
      if (i < NumVisX-1) HorizPanel.add(HorizDrag[i]);
    }

    // reconstruct spreadsheet cell layout
    DisplayCells = fcells;
    DisplayPanel.setLayout(new SSLayout(NumVisX, NumVisY, MIN_VIS_WIDTH,
                                        MIN_VIS_HEIGHT, 5, 5, false));
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) {
        DisplayPanel.add(DisplayCells[i][j]);
      }
    }

    // refresh display
    HorizPanel.doLayout();
    for (int i=0; i<NumVisX; i++) HorizLabel[i].doLayout();
    DisplayPanel.doLayout();
    SCPane.doLayout();

    synchColRow();
  }

  private void reconstructVert(JPanel[] newLabels, JComponent[] newDrag,
                               FancySSCell[][] fcells) {
    // reconstruct vertical spreadsheet label layout
    VertLabel = newLabels;
    VertDrag = newDrag;
    VertPanel.setLayout(new SSLayout(1, 2*NumVisY-1, LABEL_WIDTH,
                                     MIN_VIS_HEIGHT, 0, 0, true));
    for (int i=0; i<NumVisY; i++) {
      VertPanel.add(VertLabel[i]);
      if (i < NumVisY-1) VertPanel.add(VertDrag[i]);
    }

    // reconstruct spreadsheet cell layout
    DisplayCells = fcells;
    DisplayPanel.setLayout(new SSLayout(NumVisX, NumVisY, MIN_VIS_WIDTH,
                                        MIN_VIS_HEIGHT, 5, 5, false));
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) {
        DisplayPanel.add(DisplayCells[i][j]);
      }
    }

    // refresh display
    VertPanel.doLayout();
    for (int j=0; j<NumVisY; j++) VertLabel[j].doLayout();
    DisplayPanel.doLayout();
    SCPane.doLayout();

    synchColRow();
  }

  private void synchColRow() {
    if (RemoteColRow != null) {
      synchronized (RemoteColRow) {
        int xlen = HorizLabel.length;
        int ylen = VertLabel.length;
        try {
          MathType[] m = new MathType[3];

          Real bit = new Real(IsRemote ? 1 : 0);
          m[0] = bit.getType();
          Text[] txt = new Text[xlen];
          TextType[] tt = new TextType[xlen];
          for (int i=0; i<xlen; i++) {
            String s = ((JLabel) HorizLabel[i].getComponent(0)).getText();
            txt[i] = new Text(s);
            tt[i] = (TextType) txt[i].getType();
          }
          m[1] = new TupleType(tt);
          Tuple tc = new Tuple((TupleType) m[1], txt);

          Real[] r = new Real[ylen];
          for (int j=0; j<ylen; j++) {
            String s = ((JLabel) VertLabel[j].getComponent(0)).getText();
            r[j] = new Real(Integer.parseInt(s));
          }
          RealTuple tr = new RealTuple(r);
          m[2] = tr.getType();

          Tuple t = new Tuple(new TupleType(m), new Data[] {bit, tc, tr});
          RemoteColRow.setData(t);
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }
    }
  }

  /** Adds a column to the spreadsheet */
  public void addColumn() {
    JLabel l = (JLabel) HorizLabel[NumVisX-1].getComponent(0);
    int maxVisX = Letters.indexOf(l.getText()) + 1;
    if (maxVisX < 26) {
      // re-layout horizontal spreadsheet labels
      JPanel[] newLabels = new JPanel[NumVisX+1];
      for (int i=0; i<NumVisX; i++) newLabels[i] = HorizLabel[i];
      newLabels[NumVisX] = new JPanel();
      newLabels[NumVisX].setBorder(new LineBorder(Color.black, 1));
      newLabels[NumVisX].setLayout(new BorderLayout());
      newLabels[NumVisX].setPreferredSize(new Dimension(MIN_VIS_WIDTH,
                                                        LABEL_HEIGHT));
      String s = String.valueOf(Letters.charAt(maxVisX));
      newLabels[NumVisX].add("Center", new JLabel(s, SwingConstants.CENTER));

      if (IsRemote) {
        // let the server handle the actual cell layout
        HorizLabel = newLabels;
        synchColRow();
      }
      else {
        // re-layout horizontal label separators
        JComponent[] newDrag = new JComponent[NumVisX];
        for (int i=0; i<NumVisX-1; i++) newDrag[i] = HorizDrag[i];
        newDrag[NumVisX-1] = new JComponent() {
          public void paint(Graphics g) {
            Dimension d = getSize();
            g.setColor(Color.black);
            g.drawRect(0, 0, d.width - 1, d.height - 1);
            g.setColor(Color.yellow);
            g.fillRect(1, 1, d.width - 2, d.height - 2);
          }
        };
        newDrag[NumVisX-1].setPreferredSize(new Dimension(5, 0));
        newDrag[NumVisX-1].addMouseListener(this);
        newDrag[NumVisX-1].addMouseMotionListener(this);
        HorizPanel.removeAll();

        // re-layout spreadsheet cells
        FancySSCell[][] fcells = new FancySSCell[NumVisX+1][NumVisY];
        DisplayPanel.removeAll();
        for (int j=0; j<NumVisY; j++) {
          for (int i=0; i<NumVisX; i++) fcells[i][j] = DisplayCells[i][j];
          try {
            String name = String.valueOf(Letters.charAt(maxVisX)) +
                          String.valueOf(j + 1);
            fcells[NumVisX][j] = new FancySSCell(name, this);
            fcells[NumVisX][j].addSSCellChangeListener(this);
            fcells[NumVisX][j].addMouseListener(this);
            fcells[NumVisX][j].setAutoSwitch(AutoSwitch);
            fcells[NumVisX][j].setAutoDetect(AutoDetect);
            fcells[NumVisX][j].setAutoShowControls(AutoShowControls);
            fcells[NumVisX][j].setDimension(!CanDo3D, !CanDo3D);
            fcells[NumVisX][j].addDisplayListener(this);
            fcells[NumVisX][j].setPreferredSize(new Dimension(MIN_VIS_WIDTH,
                                                              MIN_VIS_HEIGHT));
            if (rsi != null) {
              // add new cell to server
              fcells[NumVisX][j].addToRemoteServer(rsi);
            }
          }
          catch (VisADException exc) {
            displayErrorMessage("Cannot add the column. Unable to create " +
              "new displays: " + exc.getMessage(), "VisAD SpreadSheet error");
          }
          catch (RemoteException exc) {
            displayErrorMessage("Cannot add the column. A remote error " +
              "occurred: " + exc.getMessage(), "VisAD SpreadSheet error");
          }
        }

        NumVisX++;
        reconstructHoriz(newLabels, newDrag, fcells);
      }
    }
  }

  /** Adds a row to the spreadsheet */
  public void addRow() {
    JLabel l = (JLabel) VertLabel[NumVisY-1].getComponent(0);
    int maxVisY = Integer.parseInt(l.getText());

    // re-layout vertical spreadsheet labels
    JPanel[] newLabels = new JPanel[NumVisY+1];
    JComponent[] newDrag = new JComponent[NumVisY];
    for (int i=0; i<NumVisY; i++) newLabels[i] = VertLabel[i];
    newLabels[NumVisY] = new JPanel();
    newLabels[NumVisY].setBorder(new LineBorder(Color.black, 1));
    newLabels[NumVisY].setLayout(new BorderLayout());
    newLabels[NumVisY].setPreferredSize(new Dimension(LABEL_WIDTH,
                                                      MIN_VIS_HEIGHT));
    String s = String.valueOf(maxVisY + 1);
    newLabels[NumVisY].add("Center", new JLabel(s, SwingConstants.CENTER));

    if (IsRemote) {
      // let server handle the actual cell layout
      VertLabel = newLabels;
      synchColRow();
    }
    else {
      // re-layout vertical label separators
      for (int i=0; i<NumVisY-1; i++) newDrag[i] = VertDrag[i];
      newDrag[NumVisY-1] = new JComponent() {
        public void paint(Graphics g) {
          Dimension d = getSize();
          g.setColor(Color.black);
          g.drawRect(0, 0, d.width - 1, d.height - 1);
          g.setColor(Color.yellow);
          g.fillRect(1, 1, d.width - 2, d.height - 2);
        }
      };
      newDrag[NumVisY-1].setPreferredSize(new Dimension(0, 5));
      newDrag[NumVisY-1].addMouseListener(this);
      newDrag[NumVisY-1].addMouseMotionListener(this);
      VertPanel.removeAll();

      // re-layout spreadsheet cells
      FancySSCell[][] fcells = new FancySSCell[NumVisX][NumVisY+1];
      DisplayPanel.removeAll();
      for (int i=0; i<NumVisX; i++) {
        for (int j=0; j<NumVisY; j++) fcells[i][j] = DisplayCells[i][j];
        try {
          String name = String.valueOf(Letters.charAt(i)) +
                        String.valueOf(maxVisY + 1);
          fcells[i][NumVisY] = new FancySSCell(name, this);
          fcells[i][NumVisY].addSSCellChangeListener(this);
          fcells[i][NumVisY].addMouseListener(this);
          fcells[i][NumVisY].setAutoSwitch(AutoSwitch);
          fcells[i][NumVisY].setAutoDetect(AutoDetect);
          fcells[i][NumVisY].setAutoShowControls(AutoShowControls);
          fcells[i][NumVisY].setDimension(!CanDo3D, !CanDo3D);
          fcells[i][NumVisY].addDisplayListener(this);
          fcells[i][NumVisY].setPreferredSize(new Dimension(MIN_VIS_WIDTH,
                                                            MIN_VIS_HEIGHT));
          if (rsi != null) {
            // add new cell to server
            fcells[i][NumVisY].addToRemoteServer(rsi);
          }
        }
        catch (VisADException exc) {
          displayErrorMessage("Cannot add the row. Unable to create new " +
            "displays: " + exc.getMessage(), "VisAD SpreadSheet error");
        }
        catch (RemoteException exc) {
          displayErrorMessage("Cannot add the row. A remote error occurred: " +
            exc.getMessage(), "VisAD SpreadSheet error");
        }
      }

      NumVisY++;
      reconstructVert(newLabels, newDrag, fcells);
    }
  }

  /** Deletes a column from the spreadsheet */
  public boolean deleteColumn() {
    // make sure at least one column will be left
    if (NumVisX == 1) {
      displayErrorMessage("This is the last column!",
        "Cannot delete column");
      return false;
    }
    // make sure no cells are dependent on columns about to be deleted
    for (int j=0; j<NumVisY; j++) {
      if (DisplayCells[CurX][j].othersDepend()) {
        displayErrorMessage("Other cells depend on cells from this column. " +
          "Make sure that no cells depend on this column before attempting " +
          "to delete it.", "Cannot delete column");
        return false;
      }
    }

    // re-layout horizontal spreadsheet labels
    JPanel[] newLabels = new JPanel[NumVisX-1];
    for (int i=0; i<CurX; i++) newLabels[i] = HorizLabel[i];
    for (int i=CurX+1; i<NumVisX; i++) newLabels[i-1] = HorizLabel[i];

    if (IsRemote) {
      // let server handle the actual cell layout
      HorizLabel = newLabels;
      synchColRow();
    }
    else {
      // re-layout horizontal label separators
      JComponent[] newDrag = new JComponent[NumVisX-2];
      for (int i=0; i<NumVisX-2; i++) newDrag[i] = HorizDrag[i];
      HorizPanel.removeAll();

      // re-layout spreadsheet cells
      FancySSCell[][] fcells = new FancySSCell[NumVisX-1][NumVisY];
      DisplayPanel.removeAll();
      for (int j=0; j<NumVisY; j++) {
        for (int i=0; i<CurX; i++) fcells[i][j] = DisplayCells[i][j];
        for (int i=CurX+1; i<NumVisX; i++) fcells[i-1][j] = DisplayCells[i][j];
        try {
          DisplayCells[CurX][j].destroyCell();
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
        DisplayCells[CurX][j] = null;
      }
      NumVisX--;
      if (CurX > NumVisX-1) CurX = NumVisX-1;
      reconstructHoriz(newLabels, newDrag, fcells);
    }
    return true;
  }

  /** Deletes a row from the spreadsheet */
  public boolean deleteRow() {
    // make sure at least one row will be left
    if (NumVisY == 1) {
      displayErrorMessage("This is the last row!", "Cannot delete row");
      return false;
    }
    
    // make sure no cells are dependent on rows about to be deleted
    for (int i=0; i<NumVisX; i++) {
      if (DisplayCells[i][CurY].othersDepend()) {
        displayErrorMessage("Other cells depend on cells from this row. " +
          "Make sure that no cells depend on this row before attempting " +
          "to delete it.", "Cannot delete row");
        return false;
      }
    }

    // re-layout vertical spreadsheet labels
    JPanel[] newLabels = new JPanel[NumVisY-1];
    for (int i=0; i<CurY; i++) newLabels[i] = VertLabel[i];
    for (int i=CurY+1; i<NumVisY; i++) newLabels[i-1] = VertLabel[i];

    if (IsRemote) {
      // let server handle the actual cell layout
      VertLabel = newLabels;
      synchColRow();
    }
    else {
      // re-layout horizontal label separators
      JComponent[] newDrag = new JComponent[NumVisY-2];
      for (int i=0; i<NumVisY-2; i++) newDrag[i] = VertDrag[i];
      VertPanel.removeAll();

      // re-layout spreadsheet cells
      FancySSCell[][] fcells = new FancySSCell[NumVisX][NumVisY-1];
      DisplayPanel.removeAll();
      for (int i=0; i<NumVisX; i++) {
        for (int j=0; j<CurY; j++) fcells[i][j] = DisplayCells[i][j];
        for (int j=CurY+1; j<NumVisY; j++) fcells[i][j-1] = DisplayCells[i][j];
        try {
          DisplayCells[i][CurY].destroyCell();
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
        DisplayCells[i][CurY] = null;
      }
      NumVisY--;
      if (CurY > NumVisY-1) CurY = NumVisY-1;
      reconstructVert(newLabels, newDrag, fcells);
    }
    return true;
  }

  /** Make sure the formula bar is displaying up-to-date info */
  void refreshFormulaBar() {
    if (DisplayCells[CurX][CurY].hasFormula()) {
      FormulaField.setText(DisplayCells[CurX][CurY].getFormula());
    }
    else {
      URL u = DisplayCells[CurX][CurY].getFilename();
      String s = DisplayCells[CurX][CurY].getRMIAddress();
      String f = (u == null ? (s == null ? "" : s) : u.toString());
      FormulaField.setText(f);
    }
  }

  /** Update formula based on formula entered in formula bar */
  void updateFormula() {
    String newFormula = FormulaField.getText();
    URL u = null;
    // check if new entry is a local file
    File f = new File(newFormula);
    if (f.exists()) {
      // convert local file to a URL
      try {
        u = new URL("file:/" + f.getAbsolutePath());
      }
      catch (MalformedURLException exc) { }
    }
    else {
      // check if new entry is a URL
      try {
        u = new URL(newFormula);
      }
      catch (MalformedURLException exc) { }
    }
    if (u != null) {
      // try to load the data from the URL
      DisplayCells[CurX][CurY].loadDataURL(u);
    }
    else if (newFormula.startsWith("rmi://")) {
      // try to load the data from a server using RMI
      DisplayCells[CurX][CurY].loadDataRMI(newFormula);
    }
    else {
      // check if formula has changed from last entry
      String oldFormula = "";
      if (DisplayCells[CurX][CurY].hasFormula()) {
        oldFormula = DisplayCells[CurX][CurY].getFormula();
      }
      if (oldFormula.equalsIgnoreCase(newFormula)) return;

      // try to set the formula
      try {
        DisplayCells[CurX][CurY].setFormula(newFormula);
      }
      catch (VisADException exc) {
        displayErrorMessage("Unable to assign the new formula: " +
          exc.getMessage(), "VisAD SpreadSheet error");
      }
      catch (RemoteException exc) {
        displayErrorMessage("Unable to assign the new formula: " +
          exc.getMessage(), "VisAD SpreadSheet error");
      }
    }
  }

  /** Update dimension checkbox menu items in Cell menu */
  void refreshDisplayMenuItems() {
    // update dimension check marks
    int dim = DisplayCells[CurX][CurY].getDimension();
    if (dim == BasicSSCell.JAVA3D_3D) CellDim3D3D.setState(true);
    else CellDim3D3D.setState(false);
    if (dim == BasicSSCell.JAVA2D_2D) CellDim2D2D.setState(true);
    else CellDim2D2D.setState(false);
    if (dim == BasicSSCell.JAVA3D_2D) CellDim2D3D.setState(true);
    else CellDim2D3D.setState(false);
  }

  /** Toggles auto-dimension switching */
  void setAutoSwitch(boolean b) {
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) DisplayCells[i][j].setAutoSwitch(b);
    }
    AutoSwitch = b;
  }

  /** Toggles mapping auto-detection */
  void setAutoDetect(boolean b) {
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) DisplayCells[i][j].setAutoDetect(b);
    }
    AutoDetect = b;
  }

  /** Toggles auto-display of controls */
  void setAutoShowControls(boolean b) {
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) DisplayCells[i][j].setAutoShowControls(b);
    }
    AutoShowControls = b;
  }

  /** Handles checkbox menu item changes (dimension checkboxes) */
  public void itemStateChanged(ItemEvent e) {
    String item = (String) e.getItem();
    try {
      if (item.equals("3-D (Java3D)")) {
        DisplayCells[CurX][CurY].setDimension(false, false);
      }
      else if (item.equals("2-D (Java2D)")) {
        DisplayCells[CurX][CurY].setDimension(true, true);
      }
      else if (item.equals("2-D (Java3D)")) {
        DisplayCells[CurX][CurY].setDimension(true, false);
      }
      else if (item.equals("Auto-switch to 3-D")) {
        boolean b = e.getStateChange() == ItemEvent.SELECTED;
        setAutoSwitch(b);
      }
      else if (item.equals("Auto-detect mappings")) {
        boolean b = e.getStateChange() == ItemEvent.SELECTED;
        setAutoDetect(b);
      }
      else if (item.equals("Auto-display controls")) {
        boolean b = e.getStateChange() == ItemEvent.SELECTED;
        setAutoShowControls(b);
      }
      refreshDisplayMenuItems();
    }
    catch (VisADException exc) {
      displayErrorMessage("Cannot alter display dimension: " +
        exc.getMessage(), "VisAD SpreadSheet error");
    }
    catch (RemoteException exc) {
      displayErrorMessage("Cannot alter display dimension: " +
        exc.getMessage(), "VisAD SpreadSheet error");
    }
  }

  /** Handles scrollbar changes */
  public void adjustmentValueChanged(AdjustmentEvent e) {
    Adjustable a = e.getAdjustable();
    int value = a.getValue();

    if (a.getOrientation() == Adjustable.HORIZONTAL) {
      HorizLabels.setViewPosition(new Point(value, 0));
    }
    else {  // a.getOrientation() == Adjustable.VERTICAL
      VertLabels.setViewPosition(new Point(0, value));
    }
  }

  /** Handles display changes */
  public void displayChanged(DisplayEvent e) {
    if (e.getId() == DisplayEvent.MOUSE_PRESSED) {
      FancySSCell fcell = (FancySSCell)
        BasicSSCell.getSSCellByDisplay(e.getDisplay());
      int ci = -1;
      int cj = -1;
      for (int j=0; j<NumVisY; j++) {
        for (int i=0; i<NumVisX; i++) {
          if (fcell == DisplayCells[i][j]) {
            ci = i;
            cj = j;
          }
        }
      }
      selectCell(ci, cj);
    }
  }

  /** Selects the specified cell, updating screen info */
  public void selectCell(int x, int y) {
    if (x < 0) x = 0;
    if (y < 0) y = 0;
    if (x >= NumVisX) x = NumVisX-1;
    if (y >= NumVisY) y = NumVisY-1;

    // update blue border on screen
    if (CurX < NumVisX && CurY < NumVisY) {
      FancySSCell f = DisplayCells[CurX][CurY];
      if (f != null) f.setSelected(false);
    }
    DisplayCells[x][y].setSelected(true);

    if (x != CurX || y != CurY) {
      // update spreadsheet info
      CurX = x;
      CurY = y;
      refreshFormulaBar();
      refreshMenuCommands();
      refreshDisplayMenuItems();
    }
  }

  /** Handles key presses */
  public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();

    int ci = CurX;
    int cj = CurY;
    if (key == KeyEvent.VK_RIGHT && ci < NumVisX - 1) ci++;
    if (key == KeyEvent.VK_LEFT && ci > 0) ci--;
    if (key == KeyEvent.VK_UP && cj > 0) cj--;
    if (key == KeyEvent.VK_DOWN && cj < NumVisY - 1) cj++;
    selectCell(ci, cj);
  }

  public void keyReleased(KeyEvent e) { }

  public void keyTyped(KeyEvent e) { }

  // used with cell resizing logic
  private int oldX;
  private int oldY;

  /** Handles mouse presses */
  public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) {
        if (c == DisplayCells[i][j]) selectCell(i, j);
      }
    }
    oldX = e.getX();
    oldY = e.getY();
  }

  /** Handles cell resizing */
  public void mouseReleased(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();
    Component c = e.getComponent();
    boolean change = false;
    for (int j=0; j<NumVisX-1; j++) {
      if (c == HorizDrag[j]) {
        change = true;
        break;
      }
    }
    for (int j=0; j<NumVisY-1; j++) {
      if (c == VertDrag[j]) {
        change = true;
        break;
      }
    }
    if (change) {
      // resize spreadsheet cells
      int h = VertLabel[0].getSize().height;
      for (int i=0; i<NumVisX; i++) {
        Dimension d = new Dimension();
        d.width = HorizLabel[i].getSize().width;
        d.height = h;
        DisplayCells[i][0].setPreferredSize(d);
      }

      int w = HorizLabel[0].getSize().width;
      for (int j=0; j<NumVisY; j++) {
        Dimension d = new Dimension();
        d.width = w;
        d.height = VertLabel[j].getSize().height;
        DisplayCells[0][j].setPreferredSize(d);
      }
      DisplayPanel.doLayout();
    }
  }

  /** Handles cell label resizing */
  public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    int x = e.getX();
    int y = e.getY();
    for (int j=0; j<NumVisX-1; j++) {
      if (c == HorizDrag[j]) {
        // resize columns (labels)
        Dimension s1 = HorizLabel[j].getSize();
        Dimension s2 = HorizLabel[j+1].getSize();
        int oldW = s1.width;
        s1.width += x - oldX;
        if (s1.width < MIN_VIS_WIDTH) s1.width = MIN_VIS_WIDTH;
        s2.width += oldW - s1.width;
        if (s2.width < MIN_VIS_WIDTH) {
          oldW = s2.width;
          s2.width = MIN_VIS_WIDTH;
          s1.width += oldW - s2.width;
        }
        HorizLabel[j].setSize(s1);
        HorizLabel[j+1].setSize(s2);
        for (int i=0; i<NumVisX; i++) {
          HorizLabel[i].setPreferredSize(HorizLabel[i].getSize());
        }
        HorizLabels.invalidate();
        HorizLabels.validate();
        return;
      }
    }
    for (int j=0; j<NumVisY-1; j++) {
      if (c == VertDrag[j]) {
        // resize rows (labels)
        Dimension s1 = VertLabel[j].getSize();
        Dimension s2 = VertLabel[j+1].getSize();
        int oldH = s1.height;
        s1.height += y - oldY;
        if (s1.height < MIN_VIS_HEIGHT) s1.height = MIN_VIS_HEIGHT;
        s2.height += oldH - s1.height;
        if (s2.height < MIN_VIS_HEIGHT) {
          oldH = s2.height;
          s2.height = MIN_VIS_HEIGHT;
          s1.height += oldH - s2.height;
        }
        VertLabel[j].setSize(s1);
        VertLabel[j+1].setSize(s2);
        for (int i=0; i<NumVisY; i++) {
          VertLabel[i].setPreferredSize(VertLabel[i].getSize());
        }
        VertLabels.invalidate();
        VertLabels.validate();
        return;
      }
    }
  }

  // unused MouseListener methods
  public void mouseClicked(MouseEvent e) { }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }

  // unused MouseMotionListener method
  public void mouseMoved(MouseEvent e) { }

  /** Handles changes in a cell's data */
  public void ssCellChanged(SSCellChangeEvent e) {
    FancySSCell f = (FancySSCell) e.getSSCell();
    if (CurX < NumVisX && CurY < NumVisY && DisplayCells[CurX][CurY] == f) {
      int ct = e.getChangeType();
      if (ct == SSCellChangeEvent.DATA_CHANGE) {
        refreshFormulaBar();
        refreshMenuCommands();
      }
      else if (ct == SSCellChangeEvent.DIMENSION_CHANGE) {
        refreshDisplayMenuItems();
      }
    }
  }

  private void displayErrorMessage(String msg, String title) {
    final SpreadSheet ss = this;
    final String m = msg;
    final String t = title;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog(ss, m, t, JOptionPane.ERROR_MESSAGE);
      }
    });
  }

}

