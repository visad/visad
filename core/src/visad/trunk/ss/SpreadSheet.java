//
// SpreadSheet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import visad.*;
import visad.formula.*;
import visad.java3d.*;

/** SpreadSheet is a user interface for VisAD that supports
    multiple 3-D displays (FancySSCells).<P>*/
public class SpreadSheet extends JFrame implements ActionListener,
  AdjustmentListener, DisplayListener, KeyListener, ItemListener,
  MouseListener, MouseMotionListener, SSCellListener
{

  /** starting width of the application, in percentage of screen size */
  static final int WIDTH_PERCENT = 60;

  /** starting width of the application, in percentage of screen size */
  static final int HEIGHT_PERCENT = 80;

  /** minimum VisAD display width, including display border */
  static final int MIN_VIS_WIDTH = 120;

  /** minimum VisAD display height, including display border */
  static final int MIN_VIS_HEIGHT = 120;

  /** default VisAD display width */
  static final int DEFAULT_VIS_WIDTH = 250;

  /** default VisAD display height */
  static final int DEFAULT_VIS_HEIGHT = 250;

  /** spreadsheet cell letter order */
  static final String Letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /** vertical cell label's width */
  static final int LABEL_WIDTH = 30;

  /** horizontal cell label's height */
  static final int LABEL_HEIGHT = 20;


  /** whether JVM supports Java3D (detected on SpreadSheet launch) */
  static boolean CanDo3D = true;


  /** file dialog */
  FileDialog SSFileDialog = null;

  /** base title */
  String bTitle;

  /** number of display columns */
  int NumVisX;

  /** number of display rows */
  int NumVisY;

  /** formula manager */
  FormulaManager fm;


  /** server for spreadsheet cells, if any */
  RemoteServerImpl rsi = null;

  /** whether spreadsheet is a clone of another spreadsheet */
  boolean IsRemote;

  /** information needed for spreadsheet cloning */
  RemoteDataReference RemoteColRow;


  /** whether spreadsheet's cells automatically switch dimensions
      when needed */
  boolean AutoSwitch = true;

  /** whether spreadsheet's cells automatically detect mappings */
  boolean AutoDetect = true;

  /** whether spreadsheet's cells automatically show controls */
  boolean AutoShowControls = true;


  /** panel that contains actual VisAD displays */
  Panel DisplayPanel;

  /** panel containing the scrolling pane */
  JPanel ScrollPanel;

  /** scrolling pane, in case sheet gets too small */
  ScrollPane SCPane;

  /** view port for horizontal cell labels */
  JViewport HorizLabels;

  /** view port for vertical cell labels */
  JViewport VertLabels;

  /** array of panels for horizontal labels */
  JPanel[] HorizLabel;

  /** array of panels for vertical labels */
  JPanel[] VertLabel;

  /** array of horizontal yellow sizing boxes */
  JComponent[] HorizDrag;

  /** array of vertical yellow sizing boxes */
  JComponent[] VertDrag;

  /** panel containing horizontal labels and sizing boxes */
  JPanel HorizPanel;

  /** panel containing vertical labels and sizing boxes */
  JPanel VertPanel;

  /** array of spreadsheet cells */
  FancySSCell[][] DisplayCells = null;

  /** formula bar */
  JTextField FormulaField;


  /** Edit Paste menu item */
  MenuItem EditPaste;

  /** File Save as netCDF menu item */
  MenuItem FileSave1;

  /** File Save as serialized menu item */
  MenuItem FileSave2;

  /** Cell Edit mappings menu item */
  MenuItem CellEdit;

  /** Cell Reset orientation menu item */
  MenuItem CellReset;

  /** Cell Show controls menu item */
  MenuItem CellShow;

  /** File Save as netCDF toolbar button */
  JButton ToolSave;

  /** Edit Paste toolbar button */
  JButton ToolPaste;

  /** Cell 3-D (Java3D) toolbar button */
  JButton Tool3D;

  /** Cell 2-D (Java3D) toolbar button */
  JButton Tool2D;

  /** Cell 2-D (Java2D) toolbar button */
  JButton ToolJ2D;

  /** Cell Edit mappings toolbar button */
  JButton ToolMap;

  /** Cell Show controls toolbar button */
  JButton ToolShow;

  /** Cell Reset orientation toolbar button */
  JButton ToolReset;

  /** formula bar checkbox toolbar button */
  JButton FormulaOk;

  /** Cell 3-D (Java3D) menu item */
  CheckboxMenuItem CellDim3D3D;

  /** Cell 2-D (Java2D) menu item */
  CheckboxMenuItem CellDim2D2D;

  /** Cell 2-D (Java3D) menu item */
  CheckboxMenuItem CellDim2D3D;

  /** Auto-switch dimension menu item */
  CheckboxMenuItem AutoSwitchBox;

  /** Auto-detect mappings menu item */
  CheckboxMenuItem AutoDetectBox;

  /** Auto-display controls menu item */
  CheckboxMenuItem AutoShowBox;

  /** column of currently selected cell */
  int CurX = 0;

  /** row of currently selected cell */
  int CurY = 0;

  /** contents of clipboard */
  String Clipboard = null;

  /** current spreadsheet file */
  File CurrentFile = null;

  /** object for preventing simultaneous GUI manipulation */
  private Object Lock = new Object();

  /** gateway into VisAD Visualization SpreadSheet user interface */
  public static void main(String[] argv) {
    String usage = '\n' +
      "Usage: java [-mx###m] visad.ss.SpreadSheet [cols rows] [-no3d]\n" +
      "       [-server server_name] [-client rmi_address] [-debug]\n\n" +
      "### = Maximum megabytes of memory to use\n" +
      "cols = Number of columns in this SpreadSheet\n" +
      "rows = Number of rows in this SpreadSheet\n" +
      "-no3d = Disable Java3D\n" +
      "-server server_name = Initialize this SpreadSheet as an RMI\n" +
      "                      server named server_name\n" +
      "-client rmi_address = Initialize this SpreadSheet as a clone\n" +
      "                      of the SpreadSheet at rmi_address\n" +
      "-debug = Print stack traces for all errors\n";
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
          if (argv[ix].equals("-no3d")) CanDo3D = false;
          else if (argv[ix].equals("-server")) {
            if (clonename != null) {
              System.out.println("A spreadsheet cannot be both a server " +
                                 "and a clone!");
              System.out.println(usage);
              System.exit(3);
            }
            else if (ix < len - 1) servname = argv[++ix];
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
            else if (ix < len - 1) clonename = argv[++ix];
            else {
              System.out.println("You must specify a server after " +
                                 "the '-client' flag!");
              System.out.println(usage);
              System.exit(5);
            }
          }
          else if (argv[ix].equals("-debug")) {
            BasicSSCell.DEBUG = true;
            FormulaVar.DEBUG = true;
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
              rows = Integer.parseInt(argv[ix + 1]);
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
                                 argv[ix] + " x " + argv[ix + 1]);
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
                                     "VisAD SpreadSheet");
  }

  /** constructor with default formula manager */
  public SpreadSheet(int sWidth, int sHeight, int cols, int rows,
    String server, String clone, String sTitle)
  {
    this(sWidth, sHeight, cols, rows, server, clone, sTitle, null);
  }

  /** constructor */
  public SpreadSheet(int sWidth, int sHeight, int cols, int rows,
    String server, String clone, String sTitle, FormulaManager fm)
  {
    bTitle = sTitle;
    NumVisX = cols;
    NumVisY = rows;
    this.fm = fm;
    MappingDialog.initDialog();
    addKeyListener(this);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        quitProgram();
      }
    });
    setBackground(Color.white);

    if (CanDo3D) {
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

    // cell menu
    Menu cell = new Menu("Cell");
    menubar.add(cell);

    CellDim3D3D = new CheckboxMenuItem("3-D (Java3D)", CanDo3D);
    CellDim3D3D.addItemListener(this);
    CellDim3D3D.setEnabled(CanDo3D);
    cell.add(CellDim3D3D);

    CellDim2D2D = new CheckboxMenuItem("2-D (Java2D)", !CanDo3D);
    CellDim2D2D.addItemListener(this);
    cell.add(CellDim2D2D);

    CellDim2D3D = new CheckboxMenuItem("2-D (Java3D)", false);
    CellDim2D3D.addItemListener(this);
    CellDim2D3D.setEnabled(CanDo3D);
    cell.add(CellDim2D3D);
    cell.addSeparator();

    CellEdit = new MenuItem("Edit mappings...");
    CellEdit.addActionListener(this);
    CellEdit.setActionCommand("cellEdit");
    CellEdit.setEnabled(false);
    cell.add(CellEdit);

    CellReset = new MenuItem("Reset orientation");
    CellReset.addActionListener(this);
    CellReset.setActionCommand("cellReset");
    CellReset.setEnabled(false);
    cell.add(CellReset);

    CellShow = new MenuItem("Show controls");
    CellShow.addActionListener(this);
    CellShow.setActionCommand("cellShow");
    CellShow.setEnabled(false);
    cell.add(CellShow);

    // layout menu
    Menu lay = new Menu("Layout");
    menubar.add(lay);

    MenuItem layAddCol = new MenuItem("Add column");
    layAddCol.addActionListener(this);
    layAddCol.setActionCommand("layAddCol");
    lay.add(layAddCol);

    MenuItem layAddRow = new MenuItem("Add row");
    layAddRow.addActionListener(this);
    layAddRow.setActionCommand("layAddRow");
    lay.add(layAddRow);

    MenuItem layDelCol = new MenuItem("Delete column");
    layDelCol.addActionListener(this);
    layDelCol.setActionCommand("layDelCol");
    lay.add(layDelCol);

    MenuItem layDelRow = new MenuItem("Delete row");
    layDelRow.addActionListener(this);
    layDelRow.setActionCommand("layDelRow");
    lay.add(layDelRow);
    lay.addSeparator();

    MenuItem layTile = new MenuItem("Tile cells");
    layTile.addActionListener(this);
    layTile.setActionCommand("layTile");
    lay.add(layTile);

    // options menu
    Menu options = new Menu("Options");
    menubar.add(options);

    if (!CanDo3D) AutoSwitch = false;
    AutoSwitchBox = new CheckboxMenuItem("Auto-switch to 3-D", AutoSwitch);
    AutoSwitchBox.addItemListener(this);
    AutoSwitchBox.setEnabled(CanDo3D);
    options.add(AutoSwitchBox);

    AutoDetectBox = new CheckboxMenuItem("Auto-detect mappings", AutoDetect);
    AutoDetectBox.addItemListener(this);
    options.add(AutoDetectBox);

    AutoShowBox = new CheckboxMenuItem("Auto-display controls",
      AutoShowControls);
    AutoShowBox.addItemListener(this);
    options.add(AutoShowBox);

    // set up toolbar
    JToolBar toolbar = new JToolBar();
    toolbar.setBackground(Color.lightGray);
    toolbar.setBorder(new EtchedBorder());
    toolbar.setFloatable(false);
    pane.add(toolbar);

    // file menu toolbar icons
    addToolbarButton("open.gif", "Import data", "fileOpen", true, toolbar);
    ToolSave = addToolbarButton("save.gif", "Export data to netCDF",
      "fileSaveNetcdf", false, toolbar);
    toolbar.addSeparator();

    // edit menu toolbar icons
    addToolbarButton("cut.gif", "Cut", "editCut", true, toolbar);
    addToolbarButton("copy.gif", "Copy", "editCopy", true, toolbar);
    ToolPaste = addToolbarButton("paste.gif", "Paste",
      "editPaste", false, toolbar);
    toolbar.addSeparator();

    // cell menu toolbar icons
    Tool3D = addToolbarButton("3d.gif", "3-D (Java3D)",
      "cell3D", false, toolbar);
    ToolJ2D = addToolbarButton("j2d.gif", "2-D (Java2D)",
      "cellJ2D", CanDo3D, toolbar);
    Tool2D = addToolbarButton("2d.gif", "2-D (Java3D)",
      "cell2D", CanDo3D, toolbar);
    toolbar.addSeparator();
    ToolMap = addToolbarButton("mappings.gif", "Edit mappings",
      "cellEdit", false, toolbar);
    ToolReset = addToolbarButton("reset.gif", "Reset orientation",
      "cellReset", false, toolbar);
    ToolShow = addToolbarButton("show.gif", "Show controls",
      "cellShow", false, toolbar);
    toolbar.addSeparator();

    // layout menu toolbar icon
    addToolbarButton("tile.gif", "Tile cells", "layTile", true, toolbar);
    toolbar.add(Box.createHorizontalGlue());

    // set up formula bar
    JPanel formulaPanel = new JPanel();
    formulaPanel.setBackground(Color.white);
    formulaPanel.setLayout(new BoxLayout(formulaPanel, BoxLayout.X_AXIS));
    formulaPanel.setBorder(new EtchedBorder());
    pane.add(formulaPanel);
    pane.add(Box.createRigidArea(new Dimension(0, 6)));
    addToolbarButton("cancel.gif", "Cancel formula entry",
      "formulaCancel", true, formulaPanel);
    FormulaOk = addToolbarButton("ok.gif", "Confirm formula entry",
      "formulaOk", true, formulaPanel);
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
    addToolbarButton("import.gif", "Import data",
      "fileOpen", true, formulaPanel);

    // set up horizontal spreadsheet cell labels
    JPanel horizShell = new JPanel();
    horizShell.setBackground(Color.white);
    horizShell.setLayout(new BoxLayout(horizShell, BoxLayout.X_AXIS));
    horizShell.add(Box.createRigidArea(new Dimension(LABEL_WIDTH+6, 0)));
    pane.add(horizShell);

    HorizPanel = new JPanel() {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, LABEL_HEIGHT);
      }
    };
    HorizPanel.setBackground(Color.white);
    constructHorizontalLabels();
    JViewport hl = new JViewport() {
      public Dimension getMinimumSize() {
        return new Dimension(0, LABEL_HEIGHT);
      }
      public Dimension getPreferredSize() {
        return new Dimension(0, LABEL_HEIGHT);
      }
      public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, LABEL_HEIGHT);
      }
    };
    HorizLabels = hl;
    HorizLabels.setView(HorizPanel);
    horizShell.add(HorizLabels);
    horizShell.add(new JComponent() {
      public Dimension getMinimumSize() {
        return new Dimension(6 + SCPane.getVScrollbarWidth(), 0);
      }
      public Dimension getPreferredSize() {
        return new Dimension(6 + SCPane.getVScrollbarWidth(), 0);
      }
      public Dimension getMaximumSize() {
        return new Dimension(6 + SCPane.getVScrollbarWidth(), 0);
      }
    });

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

    VertPanel = new JPanel() {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(LABEL_WIDTH, d.height);
      }
    };
    VertPanel.setBackground(Color.white);
    constructVerticalLabels();
    JViewport vl = new JViewport() {
      public Dimension getMinimumSize() {
        return new Dimension(LABEL_WIDTH, 0);
      }
      public Dimension getPreferredSize() {
        return new Dimension(LABEL_WIDTH, 0);
      }
      public Dimension getMaximumSize() {
        return new Dimension(LABEL_WIDTH, Integer.MAX_VALUE);
      }
    };
    VertLabels = vl;
    VertLabels.setView(VertPanel);
    vertShell.add(VertLabels);
    vertShell.add(new JComponent() {
      public Dimension getMinimumSize() {
        return new Dimension(0, SCPane.getHScrollbarHeight());
      }
      public Dimension getPreferredSize() {
        return new Dimension(0, SCPane.getHScrollbarHeight());
      }
      public Dimension getMaximumSize() {
        return new Dimension(0, SCPane.getHScrollbarHeight());
      }
    });

    // set up scroll pane's panel
    ScrollPanel = new JPanel();
    ScrollPanel.setBackground(Color.white);
    ScrollPanel.setLayout(new BoxLayout(ScrollPanel, BoxLayout.X_AXIS));
    mainPanel.add(ScrollPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(6, 0)));

    // set up scroll pane for VisAD Displays
    SCPane = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS) {
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
      boolean registryStarted = false;
      while (true) {
        try {
          rsi = new RemoteServerImpl();
          Naming.rebind("//:/" + server, rsi);
          break;
        }
        catch (java.rmi.ConnectException exc) {
          if (!registryStarted) {
            try {
              LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
              registryStarted = true;
            }
            catch (RemoteException rexc) {
              displayErrorMessage("Unable to autostart rmiregistry. " +
                "Please start rmiregistry before launching the " +
                "SpreadSheet in server mode.",
                "Failed to initialize RemoteServer");
              success = false;
            }
          }
          else {
            displayErrorMessage("Unable to export cells as RMI addresses. " +
              "Make sure you are running rmiregistry before launching the " +
              "SpreadSheet in server mode.",
              "Failed to initialize RemoteServer");
            success = false;
          }
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

        if (!success) break;
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

      if (success) bTitle = bTitle + " (" + server + ')';
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

      // support ':' as separator in addition to '/'
      char[] c = clone.toCharArray();
      for (int i=0; i<c.length; i++) if (c[i] == ':') c[i] = '/';
      clone = new String(c);
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
            reconstructLabels(cellNames, null, null);
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

      if (success) bTitle = bTitle + " [collaborative mode: " + clone + ']';
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
          if (b == IsRemote) { // keep sheet from receiving its own updates
            String[][] cellNames = getNewCellNames();
            if (cellNames == null) return;
            int oldNVX = NumVisX;
            int oldNVY = NumVisY;
            NumVisX = cellNames.length;
            NumVisY = cellNames[0].length;
            if (NumVisX != oldNVX || NumVisY != oldNVY) {
              // reconstruct spreadsheet cells and labels
              reconstructSpreadsheet(cellNames, null, null, frs);
              if (!IsRemote) synchColRow();
            }
          }
        }
      };
      try {
        RemoteCellImpl rColRowCell = new RemoteCellImpl(lColRowCell);
        rColRowCell.addReference(RemoteColRow);
      }
      catch (VisADException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        try {
          lColRowCell.addReference(lColRow);
        }
        catch (VisADException exc2) {
          if (BasicSSCell.DEBUG) exc2.printStackTrace();
        }
        catch (RemoteException exc2) {
          if (BasicSSCell.DEBUG) exc2.printStackTrace();
        }
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

    // wait for frame to lay itself out, then tile cells
    try {
      Thread.sleep(500);
    }
    catch (InterruptedException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    FormulaOk.requestFocus();
    tileCells();
  }


  // *** File menu methods ***

  /** import a data set */
  void loadDataSet() {
    DisplayCells[CurX][CurY].loadDataDialog();
  }

  /** export a data set to netCDF format */
  void exportDataSetNetcdf() {
    try {
      DisplayCells[CurX][CurY].saveDataDialog(new visad.data.netcdf.Plain());
    }
    catch (VisADException exc) {
      displayErrorMessage("Cannot save the data. Unable to create " +
        "a netCDF saver: " + exc.getMessage(), "VisAD SpreadSheet error");
    }
  }

  /** export a data set to serialized data format */
  void exportDataSetSerial() {
    DisplayCells[CurX][CurY].saveDataDialog(new visad.data.visad.VisADForm());
  }

  /** do any necessary clean-up, then quit the program */
  void quitProgram() {
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
                          "SpreadSheet finishes saving files..."));
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
          catch (InterruptedException exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
          }
        }
        if (b) {
          f.setCursor(Cursor.getDefaultCursor());
          f.setVisible(false);
        }

        // destroy all spreadsheet cells intelligently and cleanly
        boolean[][] alive = new boolean[NumVisX][NumVisY];
        for (int j=0; j<NumVisY; j++) {
          for (int i=0; i<NumVisX; i++) alive[i][j] = true;
        }
        int aliveCount = NumVisX * NumVisY;
        while (aliveCount > 0) {
          for (int j=0; j<NumVisY; j++) {
            for (int i=0; i<NumVisX; i++) {
              if (alive[i][j] && !DisplayCells[i][j].othersDepend()) {
                try {
                  DisplayCells[i][j].destroyCell();
                  alive[i][j] = false;
                  aliveCount--;
                }
                catch (VisADException exc) {
                  if (BasicSSCell.DEBUG) exc.printStackTrace();
                }
                catch (RemoteException exc) {
                  if (BasicSSCell.DEBUG) exc.printStackTrace();
                }
              }
            }
          }
        }
        System.exit(0);
      }
    };
    t.start();
  }


  // *** Edit menu methods ***

  /** move a cell from the screen to the clipboard */
  void cutCell() {
    if (DisplayCells[CurX][CurY].confirmClear()) {
      copyCell();
      clearCell(false);
    }
  }

  /** copy a cell from the screen to the clipboard */
  void copyCell() {
    Clipboard = DisplayCells[CurX][CurY].getSaveString();
    EditPaste.setEnabled(true);
    ToolPaste.setEnabled(true);
  }

  /** copy a cell from the clipboard to the screen */
  void pasteCell() {
    if (Clipboard != null) {
      try {
        boolean b = DisplayCells[CurX][CurY].getAutoDetect();
        DisplayCells[CurX][CurY].setAutoDetect(false);
        DisplayCells[CurX][CurY].setSaveString(Clipboard);
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

  /** clear the mappings and formula of the current cell */
  void clearCell(boolean checkSafe) {
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


  // *** Setup menu methods ***

  /** create a new spreadsheet file; return true if successful */
  boolean newFile(boolean safe) {
    if (safe) {
      int ans = JOptionPane.showConfirmDialog(this,
                "Clear all spreadsheet cells?", "Are you sure?",
                JOptionPane.YES_NO_OPTION);
      if (ans != JOptionPane.YES_OPTION) return false;
    }

    // hide control widgets
    DisplayCells[CurX][CurY].hideWidgetFrame();

    // clear all cells (in smart order to prevent errors)
    boolean[][] dirty = new boolean[NumVisX][NumVisY];
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) dirty[i][j] = true;
    }
    int dirtyCount = NumVisX * NumVisY;
    while (dirtyCount > 0) {
      for (int j=0; j<NumVisY; j++) {
        for (int i=0; i<NumVisX; i++) {
          if (dirty[i][j] && !DisplayCells[i][j].othersDepend()) {
            try {
              DisplayCells[i][j].clearCell();
              dirty[i][j] = false;
              dirtyCount--;
            }
            catch (VisADException exc) {
              if (BasicSSCell.DEBUG) exc.printStackTrace();
            }
            catch (RemoteException exc) {
              if (BasicSSCell.DEBUG) exc.printStackTrace();
            }
          }
        }
      }
    }
    CurrentFile = null;
    setTitle(bTitle);
    return true;
  }

  /** open an existing spreadsheet file */
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
      displayErrorMessage("The file " + file + " does not exist",
        "VisAD SpreadSheet error");
      return;
    }

    // disable auto-switch, auto-detect and auto-show
    boolean origSwitch = AutoSwitch;
    boolean origDetect = AutoDetect;
    boolean origShow = AutoShowControls;
    setAutoSwitch(false);
    setAutoDetect(false);
    setAutoShowControls(false);

    // clear all cells
    newFile(false);

    // load entire file into buffer
    int fLen = (int) f.length();
    char[] buff = new char[fLen];
    try {
      FileReader fr = new FileReader(f);
      fr.read(buff, 0, fLen);
      fr.close();
    }
    catch (IOException exc) {
      displayErrorMessage("Unable to read the file " + file + " from disk",
        "VisAD SpreadSheet error");
      // reset auto-switch, auto-detect and auto-show
      setAutoSwitch(origSwitch);
      setAutoDetect(origDetect);
      setAutoShowControls(origShow);
      return;
    }

    // cut buffer up into lines
    StringTokenizer st = new StringTokenizer(new String(buff), "\n\r");
    int numTokens = st.countTokens();
    String[] tokens = new String[numTokens + 1];
    for (int i=0; i<numTokens; i++) tokens[i] = st.nextToken();
    tokens[numTokens] = null;
    st = null;

    // get global information
    int sizeX = -1, sizeY = -1;
    int dimX = -1, dimY = -1;
    Vector colWidths = new Vector(), rowHeights = new Vector();
    boolean autoSwitch = true, autoDetect = true, autoShow = true;
    int tokenNum = 0;
    String line;
    int len;
    int eq;
    boolean gotGlobal = false;
    while (!gotGlobal) {
      line = tokens[tokenNum++];
      if (line == null) {
        displayErrorMessage("The file " + file + " does not contain " +
          "the required [Global] tag", "VisAD SpreadSheet error");
        // reset auto-switch, auto-detect and auto-show
        setAutoSwitch(origSwitch);
        setAutoDetect(origDetect);
        setAutoShowControls(origShow);
        return;
      }
      len = line.length();
      String trimLine = line.trim();
      int trimLen = trimLine.length();
      if (trimLine.charAt(0) == '[' && trimLine.charAt(trimLen - 1) == ']') {
        String sub = trimLine.substring(1, trimLen - 1).trim();
        if (!sub.equalsIgnoreCase("global")) {
          displayErrorMessage("The file " + file + " does not contain the " +
            "[Global] tag as its first entry", "VisAD SpreadSheet error");
          // reset auto-switch, auto-detect and auto-show
          setAutoSwitch(origSwitch);
          setAutoDetect(origDetect);
          setAutoShowControls(origShow);
          return;
        }

        // parse global information
        int endToken = tokenNum;
        while (tokens[endToken] != null &&
          tokens[endToken].trim().indexOf('[') != 0)
        {
          endToken++;
        }
        for (int i=tokenNum; i<endToken; i++) {
          line = tokens[i].trim();
          if (line == null) continue;
          if (line.charAt(0) == '#') {
            // ignore comments
            continue;
          }
          eq = line.indexOf('=');
          if (eq < 0) {
            // ignore worthless lines
            continue;
          }
          String keyword = line.substring(0, eq).trim();

          // sheet size
          if (keyword.equalsIgnoreCase("sheet size") ||
            keyword.equalsIgnoreCase("sheet_size") ||
            keyword.equalsIgnoreCase("sheetsize") ||
            keyword.equalsIgnoreCase("size"))
          {
            int x = line.indexOf('x', eq);
            if (x >= 0) {
              String sX = line.substring(eq + 1, x).trim();
              String sY = line.substring(x + 1).trim();
              try {
                sizeX = Integer.parseInt(sX);
                sizeY = Integer.parseInt(sY);
              }
              catch (NumberFormatException exc) {
                if (BasicSSCell.DEBUG) exc.printStackTrace();
              }
            }
          }

          // cell dimension
          if (keyword.equalsIgnoreCase("dimension") ||
            keyword.equalsIgnoreCase("dimensions") ||
            keyword.equalsIgnoreCase("dim"))
          {
            int x = line.indexOf('x', eq);
            if (x >= 0) {
              String sX = line.substring(eq + 1, x).trim();
              String sY = line.substring(x + 1).trim();
              try {
                dimX = Integer.parseInt(sX);
                dimY = Integer.parseInt(sY);
              }
              catch (NumberFormatException exc) {
                if (BasicSSCell.DEBUG) exc.printStackTrace();
              }
            }
          }

          // column widths
          if (keyword.equalsIgnoreCase("columns") ||
            keyword.equalsIgnoreCase("column") ||
            keyword.equalsIgnoreCase("column widths") ||
            keyword.equalsIgnoreCase("column_widths") ||
            keyword.equalsIgnoreCase("columnwidths") ||
            keyword.equalsIgnoreCase("column width") ||
            keyword.equalsIgnoreCase("column_width") ||
            keyword.equalsIgnoreCase("columnwidth"))
          {
            StringTokenizer ln = new StringTokenizer(line.substring(eq + 1));
            int nt = ln.countTokens();
            for (int z=0; z<nt; z++) {
              int cw = 0;
              try {
                cw = Integer.parseInt(ln.nextToken());
              }
              catch (NumberFormatException exc) {
                if (BasicSSCell.DEBUG) exc.printStackTrace();
              }
              if (cw >= MIN_VIS_WIDTH) colWidths.add(new Integer(cw));
            }
          }

          // rows heights
          if (keyword.equalsIgnoreCase("rows") ||
            keyword.equalsIgnoreCase("row") ||
            keyword.equalsIgnoreCase("row heights") ||
            keyword.equalsIgnoreCase("row_heights") ||
            keyword.equalsIgnoreCase("rowheights") ||
            keyword.equalsIgnoreCase("row height") ||
            keyword.equalsIgnoreCase("row_height") ||
            keyword.equalsIgnoreCase("rowheight"))
          {
            StringTokenizer ln = new StringTokenizer(line.substring(eq + 1));
            int nt = ln.countTokens();
            for (int z=0; z<nt; z++) {
              int rh = 0;
              try {
                rh = Integer.parseInt(ln.nextToken());
              }
              catch (NumberFormatException exc) {
                if (BasicSSCell.DEBUG) exc.printStackTrace();
              }
              if (rh >= MIN_VIS_WIDTH) rowHeights.add(new Integer(rh));
            }
          }

          // auto switch
          if (keyword.equalsIgnoreCase("auto switch") ||
            keyword.equalsIgnoreCase("auto_switch") ||
            keyword.equalsIgnoreCase("auto-switch") ||
            keyword.equalsIgnoreCase("autoswitch"))
          {
            String val = line.substring(eq + 1).trim();
            if (val.equalsIgnoreCase("false") ||
              val.equalsIgnoreCase("F"))
            {
              autoSwitch = false;
            }
          }

          // auto detect
          if (keyword.equalsIgnoreCase("auto detect") ||
            keyword.equalsIgnoreCase("auto_detect") ||
            keyword.equalsIgnoreCase("auto-detect") ||
            keyword.equalsIgnoreCase("autodetect"))
          {
            String val = line.substring(eq + 1).trim();
            if (val.equalsIgnoreCase("false") ||
              val.equalsIgnoreCase("F"))
            {
              autoDetect = false;
            }
          }

          // auto show
          if (keyword.equalsIgnoreCase("auto show") ||
            keyword.equalsIgnoreCase("auto_show") ||
            keyword.equalsIgnoreCase("auto-show") ||
            keyword.equalsIgnoreCase("autoshow"))
          {
            String val = line.substring(eq + 1).trim();
            if (val.equalsIgnoreCase("false") ||
              val.equalsIgnoreCase("F"))
            {
              autoShow = false;
            }
          }
        }
        gotGlobal = true;
      }
    }

    // make sure cell dimensions are valid
    if (dimX < 1 || dimY < 1) {
      displayErrorMessage("The file " + file + " has an invalid " +
        "global dimension entry", "VisAD SpreadSheet error");
      // reset auto-switch, auto-detect and auto-show
      setAutoSwitch(origSwitch);
      setAutoDetect(origDetect);
      setAutoShowControls(origShow);
      return;
    }

    // create label width and height arrays
    len = colWidths.size();
    int[] cw = new int[dimX];
    for (int i=0; i<dimX; i++) {
      if (i < len) cw[i] = ((Integer) colWidths.elementAt(i)).intValue();
      else cw[i] = DEFAULT_VIS_WIDTH;
    }
    len = rowHeights.size();
    int[] rh = new int[dimY];
    for (int i=0; i<dimY; i++) {
      if (i < len) rh[i] = ((Integer) rowHeights.elementAt(i)).intValue();
      else rh[i] = DEFAULT_VIS_HEIGHT;
    }

    // examine each cell entry
    String[][] cellNames = new String[dimX][dimY];
    String[][] fileStrings = new String[dimX][dimY];
    for (int j=0; j<dimY; j++) {
      for (int i=0; i<dimX; i++) {
        // find next cell name
        cellNames[i][j] = null;
        do {
          line = tokens[tokenNum++];
          if (line == null) {
            displayErrorMessage("The file " + file + " is incomplete",
              "VisAD SpreadSheet error");
            // reset auto-switch, auto-detect and auto-show
            setAutoSwitch(origSwitch);
            setAutoDetect(origDetect);
            setAutoShowControls(origShow);
            return;
          }
          String trimLine = line.trim();
          int trimLen = trimLine.length();
          if (trimLine.charAt(0) == '[' &&
            trimLine.charAt(trimLen - 1) == ']')
          {
            // this line identifies a cell name
            cellNames[i][j] = trimLine.substring(1, trimLen - 1).trim();
          }
        }
        while (cellNames[i][j] == null);

        // find last line of this cell's save string
        int last = tokenNum + 1;
        while (tokens[last] != null &&
          tokens[last].trim().indexOf('[') != 0)
        {
          last++;
        }

        // build this cell's save string
        String s = "";
        for (int l=tokenNum; l<last; l++) s = s + tokens[l] + '\n';
        fileStrings[i][j] = s;
      }
    }

    // resize the sheet to the correct size
    if (sizeX > 0 && sizeY > 0) setSize(sizeX, sizeY);

    // reconstruct spreadsheet cells and labels
    NumVisX = dimX;
    NumVisY = dimY;
    reconstructSpreadsheet(cellNames, cw, rh, null);
    synchColRow();

    // set each cell's string
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) {
        try {
          DisplayCells[i][j].setSaveString(fileStrings[i][j]);
        }
        catch (VisADException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
      }
    }

    // set auto-switch, auto-detect and auto-show
    setAutoSwitch(autoSwitch);
    setAutoDetect(autoDetect);
    setAutoShowControls(autoShow);

    // update current file and title
    CurrentFile = f;
    setTitle(bTitle + " - " + f.getPath());

    // refresh GUI components
    refreshDisplayMenuItems();
    refreshFormulaBar();
    refreshMenuCommands();
    refreshOptions();
    validate();
    repaint();
  }

  /** save a spreadsheet file under its current name */
  void saveFile() {
    if (CurrentFile == null) saveasFile();
    else {
      // construct file header
      StringBuffer sb = new StringBuffer(1024 * NumVisX * NumVisY + 1024);
      sb.append("# VisAD Visualization SpreadSheet spreadsheet file\n");
      Calendar cal = Calendar.getInstance();
      int year = cal.get(Calendar.YEAR);
      int month = cal.get(Calendar.MONTH);
      int day = cal.get(Calendar.DAY_OF_MONTH);
      int hour = cal.get(Calendar.HOUR_OF_DAY);
      int min = cal.get(Calendar.MINUTE);
      int sec = cal.get(Calendar.SECOND);
      int milli = cal.get(Calendar.MILLISECOND);
      sb.append("# File ");
      sb.append(CurrentFile.getName());
      sb.append(" written at ");
      sb.append(year);
      sb.append('/');
      if (month < 10) sb.append('0');
      sb.append(month + 1);
      sb.append('/');
      if (day < 10) sb.append('0');
      sb.append(day);
      sb.append(", ");
      if (hour < 10) sb.append('0');
      sb.append(hour);
      sb.append(':');
      if (min < 10) sb.append('0');
      sb.append(min);
      sb.append(':');
      if (sec < 10) sb.append('0');
      sb.append(sec);
      sb.append('.');
      if (milli < 100) sb.append('0');
      if (milli < 10) sb.append('0');
      sb.append(milli);
      sb.append("\n\n");

      // compile global information
      sb.append("[Global]\n");
      sb.append("sheet size = ");
      sb.append(getWidth());
      sb.append(" x ");
      sb.append(getHeight());
      sb.append('\n');
      sb.append("dimension = ");
      sb.append(NumVisX);
      sb.append(" x ");
      sb.append(NumVisY);
      sb.append('\n');
      sb.append("columns =");
      for (int j=0; j<NumVisX; j++) {
        sb.append(' ');
        sb.append(HorizLabel[j].getSize().width);
      }
      sb.append('\n');
      sb.append("rows =");
      for (int i=0; i<NumVisY; i++) {
        sb.append(' ');
        sb.append(VertLabel[i].getSize().height);
      }
      sb.append('\n');
      sb.append("auto switch = ");
      sb.append(AutoSwitch);
      sb.append('\n');
      sb.append("auto detect = ");
      sb.append(AutoDetect);
      sb.append('\n');
      sb.append("auto show = ");
      sb.append(AutoShowControls);
      sb.append("\n\n");

      // compile cell information
      for (int j=0; j<NumVisY; j++) {
        for (int i=0; i<NumVisX; i++) {
          sb.append('[');
          sb.append(DisplayCells[i][j].getName());
          sb.append("]\n");
          sb.append(DisplayCells[i][j].getSaveString());
          sb.append('\n');
        }
      }

      // convert information to a character array
      char[] sc = sb.toString().toCharArray();

      try {
        // write file to disk
        FileWriter fw = new FileWriter(CurrentFile);
        fw.write(sc, 0, sc.length);
        fw.close();
      }
      catch (IOException exc) {
        displayErrorMessage("Could not save file " + CurrentFile.getName() +
          ". Make sure there is enough disk space.",
          "VisAD SpreadSheet error");
      }
    }
  }

  /** save a spreadsheet file under a new name */
  void saveasFile() {
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


  // *** Cell menu methods ***

  /** set the dimension of the current cell */
  private void setDim(boolean threeD, boolean java3D) {
    try {
      DisplayCells[CurX][CurY].setDimension(threeD, java3D);
    }
    catch (VisADException exc) {
      displayErrorMessage("Cannot alter display dimension: " +
        exc.getMessage(), "VisAD SpreadSheet error");
    }
    catch (RemoteException exc) {
      displayErrorMessage("Cannot alter display dimension: " +
        exc.getMessage(), "VisAD SpreadSheet error");
    }
    refreshDisplayMenuItems();
  }

  /** specify mappings from Data to Display */
  void createMappings() {
    DisplayCells[CurX][CurY].addMapDialog();
    refreshMenuCommands();
  }

  /** resets the display projection to its original value */
  void resetOrientation() {
    DisplayImpl display = DisplayCells[CurX][CurY].getDisplay();
    if (display != null) {
      ProjectionControl pc = display.getProjectionControl();
      if (pc != null) {
        int dim = DisplayCells[CurX][CurY].getDimension();
        double[] matrix;
        if (dim == 1) {
          // 3-D (Java3D)
          matrix = new double[]
            {0.5, 0, 0, 0, 0, 0.5, 0, 0, 0, 0, 0.5, 0, 0, 0, 0, 1};
        }
        else if (dim == 2) {
          // 2-D (Java2D)
          matrix = new double[] {1, 0, 0, -1, 0, 0};
        }
        else {
          // 2-D (Java3D)
          matrix = new double[]
            {0.65, 0, 0, 0, 0, 0.65, 0, 0, 0, 0, 0.65, 0, 0, 0, 0, 1};
        }
        try {
          pc.setMatrix(matrix);
        }
        catch (VisADException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
      }
    }
  }


  // *** Display menu methods ***

  /** add a column to the spreadsheet */
  synchronized void addColumn() {
    JLabel l = (JLabel) HorizLabel[NumVisX - 1].getComponent(0);
    int maxVisX = Letters.indexOf(l.getText()) + 1;
    if (maxVisX < Letters.length()) {
      // re-layout horizontal spreadsheet labels
      JPanel[] newLabels = new JPanel[NumVisX + 1];
      for (int i=0; i<NumVisX; i++) newLabels[i] = HorizLabel[i];
      newLabels[NumVisX] = new JPanel();
      newLabels[NumVisX].setBorder(new LineBorder(Color.black, 1));
      newLabels[NumVisX].setLayout(new BorderLayout());
      newLabels[NumVisX].setPreferredSize(
        new Dimension(DEFAULT_VIS_WIDTH, LABEL_HEIGHT));
      String s = String.valueOf(Letters.charAt(maxVisX));
      newLabels[NumVisX].add("Center", new JLabel(s, SwingConstants.CENTER));

      if (IsRemote) {
        // let the server handle the actual cell layout
        HorizLabel = newLabels;
        synchColRow();
      }
      else {
        // re-layout horizontal label separators
        JComponent[] newDrag = new JComponent[NumVisX + 1];
        for (int i=0; i<NumVisX; i++) newDrag[i] = HorizDrag[i];
        newDrag[NumVisX] = new JComponent() {
          public void paint(Graphics g) {
            Dimension d = this.getSize();
            g.setColor(Color.black);
            g.drawRect(0, 0, d.width - 1, d.height - 1);
            g.setColor(Color.yellow);
            g.fillRect(1, 1, d.width - 2, d.height - 2);
          }
        };
        newDrag[NumVisX].setPreferredSize(new Dimension(5, 0));
        newDrag[NumVisX].addMouseListener(this);
        newDrag[NumVisX].addMouseMotionListener(this);
        HorizPanel.removeAll();

        // re-layout spreadsheet cells
        FancySSCell[][] fcells = new FancySSCell[NumVisX + 1][NumVisY];
        DisplayPanel.removeAll();
        for (int j=0; j<NumVisY; j++) {
          for (int i=0; i<NumVisX; i++) fcells[i][j] = DisplayCells[i][j];
          try {
            String name = String.valueOf(Letters.charAt(maxVisX)) +
              String.valueOf(j + 1);
            fcells[NumVisX][j] = new FancySSCell(name, fm, this);
            fcells[NumVisX][j].addSSCellChangeListener(this);
            fcells[NumVisX][j].addMouseListener(this);
            fcells[NumVisX][j].setAutoSwitch(AutoSwitch);
            fcells[NumVisX][j].setAutoDetect(AutoDetect);
            fcells[NumVisX][j].setAutoShowControls(AutoShowControls);
            fcells[NumVisX][j].setDimension(!CanDo3D, !CanDo3D);
            fcells[NumVisX][j].addDisplayListener(this);
            fcells[NumVisX][j].setPreferredSize(
              new Dimension(DEFAULT_VIS_WIDTH, DEFAULT_VIS_HEIGHT));
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

  /** add a row to the spreadsheet */
  synchronized void addRow() {
    JLabel l = (JLabel) VertLabel[NumVisY - 1].getComponent(0);
    int maxVisY = Integer.parseInt(l.getText());

    // re-layout vertical spreadsheet labels
    JPanel[] newLabels = new JPanel[NumVisY + 1];
    JComponent[] newDrag = new JComponent[NumVisY + 1];
    for (int i=0; i<NumVisY; i++) newLabels[i] = VertLabel[i];
    newLabels[NumVisY] = new JPanel();
    newLabels[NumVisY].setBorder(new LineBorder(Color.black, 1));
    newLabels[NumVisY].setLayout(new BorderLayout());
    newLabels[NumVisY].setPreferredSize(
      new Dimension(LABEL_WIDTH, DEFAULT_VIS_HEIGHT));
    String s = String.valueOf(maxVisY + 1);
    newLabels[NumVisY].add("Center", new JLabel(s, SwingConstants.CENTER));

    if (IsRemote) {
      // let server handle the actual cell layout
      VertLabel = newLabels;
      synchColRow();
    }
    else {
      // re-layout vertical label separators
      for (int i=0; i<NumVisY; i++) newDrag[i] = VertDrag[i];
      newDrag[NumVisY] = new JComponent() {
        public void paint(Graphics g) {
          Dimension d = this.getSize();
          g.setColor(Color.black);
          g.drawRect(0, 0, d.width - 1, d.height - 1);
          g.setColor(Color.yellow);
          g.fillRect(1, 1, d.width - 2, d.height - 2);
        }
      };
      newDrag[NumVisY].setPreferredSize(new Dimension(0, 5));
      newDrag[NumVisY].addMouseListener(this);
      newDrag[NumVisY].addMouseMotionListener(this);
      VertPanel.removeAll();

      // re-layout spreadsheet cells
      FancySSCell[][] fcells = new FancySSCell[NumVisX][NumVisY + 1];
      DisplayPanel.removeAll();
      for (int i=0; i<NumVisX; i++) {
        for (int j=0; j<NumVisY; j++) fcells[i][j] = DisplayCells[i][j];
        try {
          String name = String.valueOf(Letters.charAt(i)) +
            String.valueOf(maxVisY + 1);
          fcells[i][NumVisY] = new FancySSCell(name, fm, this);
          fcells[i][NumVisY].addSSCellChangeListener(this);
          fcells[i][NumVisY].addMouseListener(this);
          fcells[i][NumVisY].setAutoSwitch(AutoSwitch);
          fcells[i][NumVisY].setAutoDetect(AutoDetect);
          fcells[i][NumVisY].setAutoShowControls(AutoShowControls);
          fcells[i][NumVisY].setDimension(!CanDo3D, !CanDo3D);
          fcells[i][NumVisY].addDisplayListener(this);
          fcells[i][NumVisY].setPreferredSize(
            new Dimension(DEFAULT_VIS_WIDTH, DEFAULT_VIS_HEIGHT));
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

  /** deletes a column from the spreadsheet */
  synchronized boolean deleteColumn() {

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
    JPanel[] newLabels = new JPanel[NumVisX - 1];
    for (int i=0; i<CurX; i++) newLabels[i] = HorizLabel[i];
    for (int i=CurX+1; i<NumVisX; i++) newLabels[i - 1] = HorizLabel[i];

    if (IsRemote) {
      // let server handle the actual cell layout
      HorizLabel = newLabels;
      synchColRow();
    }
    else {
      // re-layout horizontal label separators
      JComponent[] newDrag = new JComponent[NumVisX - 1];
      for (int i=0; i<NumVisX-1; i++) newDrag[i] = HorizDrag[i];
      HorizPanel.removeAll();

      // re-layout spreadsheet cells
      FancySSCell[][] fcells = new FancySSCell[NumVisX - 1][NumVisY];
      DisplayPanel.removeAll();
      for (int j=0; j<NumVisY; j++) {
        for (int i=0; i<CurX; i++) fcells[i][j] = DisplayCells[i][j];
        for (int i=CurX+1; i<NumVisX; i++) {
          fcells[i - 1][j] = DisplayCells[i][j];
        }
        try {
          DisplayCells[CurX][j].destroyCell();
        }
        catch (VisADException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        DisplayCells[CurX][j] = null;
      }
      NumVisX--;
      if (CurX > NumVisX - 1) selectCell(NumVisX - 1, CurY);
      reconstructHoriz(newLabels, newDrag, fcells);
    }
    return true;
  }

  /** delete a row from the spreadsheet */
  synchronized boolean deleteRow() {
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
    JPanel[] newLabels = new JPanel[NumVisY - 1];
    for (int i=0; i<CurY; i++) newLabels[i] = VertLabel[i];
    for (int i=CurY+1; i<NumVisY; i++) newLabels[i - 1] = VertLabel[i];

    if (IsRemote) {
      // let server handle the actual cell layout
      VertLabel = newLabels;
      synchColRow();
    }
    else {
      // re-layout horizontal label separators
      JComponent[] newDrag = new JComponent[NumVisY];
      for (int i=0; i<NumVisY; i++) newDrag[i] = VertDrag[i];
      VertPanel.removeAll();

      // re-layout spreadsheet cells
      FancySSCell[][] fcells = new FancySSCell[NumVisX][NumVisY - 1];
      DisplayPanel.removeAll();
      for (int i=0; i<NumVisX; i++) {
        for (int j=0; j<CurY; j++) fcells[i][j] = DisplayCells[i][j];
        for (int j=CurY+1; j<NumVisY; j++) {
          fcells[i][j - 1] = DisplayCells[i][j];
        }
        try {
          DisplayCells[i][CurY].destroyCell();
        }
        catch (VisADException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        DisplayCells[i][CurY] = null;
      }
      NumVisY--;
      if (CurY > NumVisY - 1) selectCell(CurX, NumVisY - 1);
      reconstructVert(newLabels, newDrag, fcells);
    }
    return true;
  }

  /** resize all cells to exactly fill the entire pane, if possible */
  void tileCells() {
    Dimension paneSize = SCPane.getSize();
    int w = paneSize.width - SCPane.getVScrollbarWidth() - 5 * NumVisX;
    int h = paneSize.height - SCPane.getHScrollbarHeight() - 5 * NumVisY;
    int wx = w / NumVisX;
    int hy = h / NumVisY;
    if (wx < MIN_VIS_WIDTH) wx = MIN_VIS_WIDTH;
    if (hy < MIN_VIS_HEIGHT) hy = MIN_VIS_HEIGHT;
    Dimension hSize = new Dimension(wx, LABEL_HEIGHT);
    Dimension vSize = new Dimension(LABEL_WIDTH, hy);
    for (int i=0; i<NumVisX; i++) {
      HorizLabel[i].setSize(hSize);
      HorizLabel[i].setPreferredSize(hSize);
    }
    for (int j=0; j<NumVisY; j++) {
      VertLabel[j].setSize(vSize);
      VertLabel[j].setPreferredSize(vSize);
    }
    synchLabelAndCellSizes();
  }


  // *** Options menu methods ***

  /** toggle auto-dimension switching */
  void setAutoSwitch(boolean b) {
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) DisplayCells[i][j].setAutoSwitch(b);
    }
    AutoSwitch = b;
  }

  /** toggle mapping auto-detection */
  void setAutoDetect(boolean b) {
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) DisplayCells[i][j].setAutoDetect(b);
    }
    AutoDetect = b;
  }

  /** toggle auto-display of controls */
  void setAutoShowControls(boolean b) {
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) DisplayCells[i][j].setAutoShowControls(b);
    }
    AutoShowControls = b;
  }


  // *** Toolbar methods ***

  /** update formula based on formula entered in formula bar */
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
      catch (MalformedURLException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
    }
    else {
      // check if new entry is a URL
      try {
        u = new URL(newFormula);
      }
      catch (MalformedURLException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
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


  // *** Methods for refreshing GUI components when things change ***

  /** refresh spreadsheet cells */
  private void refreshCells() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        for (int i=0; i<NumVisX; i++) {
          for (int j=0; j<NumVisY; j++) DisplayCells[i][j].refresh();
        }
      }
    });
  }

  /** refresh check box items in the Options menu */
  private void refreshOptions() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        AutoSwitchBox.setState(AutoSwitch);
        AutoDetectBox.setState(AutoDetect);
        AutoShowBox.setState(AutoShowControls);
      }
    });
  }

  /** enable or disable certain menu items depending on whether
      this cell has data */
  private void refreshMenuCommands() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        boolean b = DisplayCells[CurX][CurY].hasData();
        CellEdit.setEnabled(b);
        ToolMap.setEnabled(b);
        FileSave1.setEnabled(b);
        FileSave2.setEnabled(b);
        ToolSave.setEnabled(b);
        CellReset.setEnabled(b);
        ToolReset.setEnabled(b);
        b = DisplayCells[CurX][CurY].hasControls();
        CellShow.setEnabled(b);
        ToolShow.setEnabled(b);
      }
    });
  }

  /** make sure the formula bar is displaying up-to-date info */
  private void refreshFormulaBar() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (DisplayCells[CurX][CurY].hasFormula()) {
          FormulaField.setText(DisplayCells[CurX][CurY].getFormula());
        }
        else {
          String f = DisplayCells[CurX][CurY].getFilename();
          String s = DisplayCells[CurX][CurY].getRMIAddress();
          String t = (f.equals("") ? (s == null ? "" : s) : f);
          FormulaField.setText(t);
        }
      }
    });
  }

  /** update dimension checkbox menu items and toolbar buttons */
  private void refreshDisplayMenuItems() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // update dimension check marks
        int dim = DisplayCells[CurX][CurY].getDimension();
        boolean j3d3d = (dim == BasicSSCell.JAVA3D_3D);
        boolean j2d2d = (dim == BasicSSCell.JAVA2D_2D);
        boolean j3d2d = (dim == BasicSSCell.JAVA3D_2D);
        CellDim3D3D.setState(j3d3d);
        CellDim2D2D.setState(j2d2d);
        CellDim2D3D.setState(j3d2d);
        Tool3D.setEnabled(!j3d3d && CanDo3D);
        Tool2D.setEnabled(!j3d2d && CanDo3D);
        ToolJ2D.setEnabled(!j2d2d);
      }
    });
  }


  // *** Spreadsheet collaboration-related methods ***

  /** determine whether or not the last remote event was from the server */
  private boolean getColRowServer() {
    Tuple t = null;
    Real bit = null;
    try {
      t = (Tuple) RemoteColRow.getData();
      bit = (Real) t.getComponent(0);
      return (bit.getValue() == 0);
    }
    catch (NullPointerException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    catch (VisADException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    return true;
  }

  /** get the latest remote row and column information */
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
    catch (NullPointerException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    catch (VisADException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
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
      catch (VisADException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
      if (txt == null) return null;
      colNames[i] = txt.getValue();
    }
    for (int j=0; j<rowlen; j++) {
      Real r = null;
      try {
        r = (Real) tr.getComponent(j);
      }
      catch (VisADException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
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

  /** update the remote row and column information */
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
        catch (VisADException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
      }
    }
  }


  // *** Methods for (re)constructing spreadsheet cells and labels ***

  /** ensure that the cells' preferred sizes match those of the labels */
  private void synchLabelAndCellSizes() {
    // resize spreadsheet cells
    for (int j=0; j<NumVisY; j++) {
      int h = VertLabel[j].getSize().height;
      for (int i=0; i<NumVisX; i++) {
        int w = HorizLabel[i].getSize().width;
        DisplayCells[i][j].setPreferredSize(new Dimension(w, h));
      }
    }

    // refresh display
    HorizLabels.validate();
    VertLabels.validate();
    DisplayPanel.doLayout();
    SCPane.validate();
    refreshCells();
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
    synchronized (Lock) {
      DisplayPanel.setLayout(new SSLayout(NumVisX, NumVisY, 5, 5));
      DisplayCells = new FancySSCell[NumVisX][NumVisY];
      for (int j=0; j<NumVisY; j++) {
        int ph = VertLabel[j].getPreferredSize().height;
        for (int i=0; i<NumVisX; i++) {
          int pw = HorizLabel[i].getPreferredSize().width;
          try {
            FancySSCell f = (FancySSCell) BasicSSCell.getSSCellByName(l[i][j]);
            if (f == null) {
              f = new FancySSCell(l[i][j], fm, rs, null, this);
              f.addSSCellChangeListener(this);
              f.addMouseListener(this);
              f.setAutoSwitch(AutoSwitch);
              f.setAutoDetect(AutoDetect);
              f.setAutoShowControls(AutoShowControls);
              if (rs == null) f.setDimension(!CanDo3D, !CanDo3D);
              f.addDisplayListener(this);
              if (rsi != null) {
                // add new cell to server
                f.addToRemoteServer(rsi);
              }
            }
            f.setPreferredSize(new Dimension(pw, ph));
            DisplayCells[i][j] = f;

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
      selectCell(0, 0);
    }
  }

  private void constructHorizontalLabels() {
    constructHorizontalLabels(null, null);
  }

  private void constructHorizontalLabels(String[] l, int[] widths) {
    if (l == null) {
      l = new String[NumVisX];
      for (int i=0; i<NumVisX; i++) l[i] = "" + Letters.charAt(i);
    }
    if (widths == null) {
      widths = new int[NumVisX];
      for (int i=0; i<NumVisX; i++) widths[i] = DEFAULT_VIS_WIDTH;
    }
    synchronized (Lock) {
      HorizPanel.setLayout(new SSLayout(2 * NumVisX, 1, 0, 0));
      HorizLabel = new JPanel[NumVisX];
      HorizDrag = new JComponent[NumVisX];
      for (int i=0; i<NumVisX; i++) {
        HorizLabel[i] = new JPanel();
        HorizLabel[i].setBorder(new LineBorder(Color.black, 1));
        HorizLabel[i].setLayout(new BorderLayout());
        HorizLabel[i].setPreferredSize(new Dimension(widths[i], LABEL_HEIGHT));
        HorizLabel[i].add("Center", new JLabel(l[i], SwingConstants.CENTER));
        HorizPanel.add(HorizLabel[i]);
        HorizDrag[i] = new JComponent() {
          public void paint(Graphics g) {
            Dimension d = this.getSize();
            g.setColor(Color.black);
            g.drawRect(0, 0, d.width - 1, d.height - 1);
            g.setColor(Color.yellow);
            g.fillRect(1, 1, d.width - 2, d.height - 2);
          }
          public Dimension getPreferredSize() {
            return new Dimension(5, LABEL_HEIGHT);
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
    constructVerticalLabels(null, null);
  }

  private void constructVerticalLabels(String[] l, int[] heights) {
    if (l == null) {
      l = new String[NumVisY];
      for (int i=0; i<NumVisY; i++) l[i] = "" + (i+1);
    }
    if (heights == null) {
      heights = new int[NumVisY];
      for (int i=0; i<NumVisY; i++) heights[i] = DEFAULT_VIS_HEIGHT;
    }
    synchronized (Lock) {
      VertPanel.setLayout(new SSLayout(1, 2 * NumVisY, 0, 0));
      VertLabel = new JPanel[NumVisY];
      VertDrag = new JComponent[NumVisY];
      for (int i=0; i<NumVisY; i++) {
        VertLabel[i] = new JPanel();
        VertLabel[i].setBorder(new LineBorder(Color.black, 1));
        VertLabel[i].setLayout(new BorderLayout());
        VertLabel[i].setPreferredSize(new Dimension(LABEL_WIDTH, heights[i]));
        VertLabel[i].add("Center", new JLabel(l[i], SwingConstants.CENTER));
        VertPanel.add(VertLabel[i]);
        VertDrag[i] = new JComponent() {
          public void paint(Graphics g) {
            Dimension d = this.getSize();
            g.setColor(Color.black);
            g.drawRect(0, 0, d.width - 1, d.height - 1);
            g.setColor(Color.yellow);
            g.fillRect(1, 1, d.width - 2, d.height - 2);
          }
          public Dimension getPreferredSize() {
            return new Dimension(LABEL_WIDTH, 5);
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

  private void reconstructLabels(String[][] cellNames, int[] w, int[] h) {
    // reconstruct horizontal labels
    String[] hLabels = new String[NumVisX];
    synchronized (Lock) {
      HorizPanel.removeAll();
      for (int i=0; i<NumVisX; i++) {
        hLabels[i] = "" + cellNames[i][0].charAt(0);
      }
    }
    constructHorizontalLabels(hLabels, w);

    // reconstruct vertical labels
    String[] vLabels = new String[NumVisY];
    synchronized (Lock) {
      VertPanel.removeAll();
      for (int j=0; j<NumVisY; j++) vLabels[j] = cellNames[0][j].substring(1);
    }
    constructVerticalLabels(vLabels, h);
  }

  private void reconstructSpreadsheet(String[][] cellNames, int[] w, int[] h,
    RemoteServer rs)
  {
    // reconstruct labels
    reconstructLabels(cellNames, w, h);

    // reconstruct spreadsheet cells
    synchronized (Lock) {
      DisplayPanel.removeAll();
      int ox, oy;
      if (DisplayCells == null) {
        ox = 0;
        oy = 0;
      }
      else {
        ox = DisplayCells.length;
        oy = DisplayCells[0].length;
      }
      for (int i=0; i<ox; i++) {
        for (int j=0; j<oy; j++) {
          try {
            String s = DisplayCells[i][j].getName();
            boolean kill = true;
            // only delete cells that are truly gone
            for (int ii=0; ii<cellNames.length; ii++) {
              for (int jj=0; jj<cellNames[ii].length; jj++) {
                if (s.equals(cellNames[ii][jj])) kill = false;
              }
            }
            if (kill) DisplayCells[i][j].destroyCell();
          }
          catch (VisADException exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
          }
          catch (RemoteException exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
          }
          DisplayCells[i][j] = null;
        }
      }
    }
    constructSpreadsheetCells(cellNames, rs);

    synchronized (Lock) {
      // refresh display
      HorizPanel.doLayout();
      for (int i=0; i<NumVisX; i++) HorizLabel[i].doLayout();
      VertPanel.doLayout();
      for (int j=0; j<NumVisY; j++) VertLabel[j].doLayout();
      SCPane.doLayout();
      DisplayPanel.doLayout();
      refreshCells();
    }
  }

  private void reconstructHoriz(JPanel[] newLabels, JComponent[] newDrag,
    FancySSCell[][] fcells)
  {
    synchronized (Lock) {
      // reconstruct horizontal spreadsheet label layout
      HorizLabel = newLabels;
      HorizDrag = newDrag;
      HorizPanel.setLayout(new SSLayout(2*NumVisX, 1, 0, 0));
      for (int i=0; i<NumVisX; i++) {
        HorizPanel.add(HorizLabel[i]);
        HorizPanel.add(HorizDrag[i]);
      }

      // reconstruct spreadsheet cell layout
      DisplayCells = fcells;
      DisplayPanel.setLayout(new SSLayout(NumVisX, NumVisY, 5, 5));
      for (int j=0; j<NumVisY; j++) {
        for (int i=0; i<NumVisX; i++) DisplayPanel.add(DisplayCells[i][j]);
      }

      // refresh display
      HorizPanel.validate();
      HorizLabels.validate();
      HorizLabels.repaint();
      for (int i=0; i<NumVisX; i++) HorizLabel[i].validate();
      SCPane.validate();
      DisplayPanel.repaint();
      refreshCells();
    }

    synchColRow();
  }

  private void reconstructVert(JPanel[] newLabels, JComponent[] newDrag,
    FancySSCell[][] fcells)
  {
    synchronized (Lock) {
      // reconstruct vertical spreadsheet label layout
      VertLabel = newLabels;
      VertDrag = newDrag;
      VertPanel.setLayout(new SSLayout(1, 2*NumVisY, 0, 0));
      for (int i=0; i<NumVisY; i++) {
        VertPanel.add(VertLabel[i]);
        VertPanel.add(VertDrag[i]);
      }

      // reconstruct spreadsheet cell layout
      DisplayCells = fcells;
      DisplayPanel.setLayout(new SSLayout(NumVisX, NumVisY, 5, 5));
      for (int j=0; j<NumVisY; j++) {
        for (int i=0; i<NumVisX; i++) DisplayPanel.add(DisplayCells[i][j]);
      }

      // refresh display
      VertPanel.validate();
      VertLabels.validate();
      VertLabels.repaint();
      for (int j=0; j<NumVisY; j++) VertLabel[j].validate();
      SCPane.validate();
      DisplayPanel.repaint();
      refreshCells();
    }

    synchColRow();
  }


  // *** Event handling ***

  /** handle menubar/toolbar events */
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

    // cell menu commands
    else if (cmd.equals("cellEdit")) createMappings();
    else if (cmd.equals("cell3D")) setDim(false, false);
    else if (cmd.equals("cellJ2D")) setDim(true, true);
    else if (cmd.equals("cell2D")) setDim(true, false);
    else if (cmd.equals("cellShow")) {
      DisplayCells[CurX][CurY].showWidgetFrame();
    }
    else if (cmd.equals("cellReset")) resetOrientation();

    // layout menu commands
    else if (cmd.equals("layAddCol")) addColumn();
    else if (cmd.equals("layAddRow")) addRow();
    else if (cmd.equals("layDelCol")) deleteColumn();
    else if (cmd.equals("layDelRow")) deleteRow();
    else if (cmd.equals("layTile")) tileCells();

    // formula bar commands
    else if (cmd.equals("formulaCancel")) refreshFormulaBar();
    else if (cmd.equals("formulaOk")) updateFormula();
    else if (cmd.equals("formulaChange")) {
      FormulaOk.requestFocus();
      updateFormula();
    }
  }

  /** handle checkbox menu item changes (dimension checkboxes) */
  public void itemStateChanged(ItemEvent e) {
    String item = (String) e.getItem();
    if (item.equals("3-D (Java3D)")) setDim(false, false);
    else if (item.equals("2-D (Java2D)")) setDim(true, true);
    else if (item.equals("2-D (Java3D)")) setDim(true, false);
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
  }

  /** handle scrollbar changes */
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

  /** handle display changes */
  public void displayChanged(DisplayEvent e) {
    if (e.getId() == DisplayEvent.MOUSE_PRESSED) {
      String name = null;
      try {
        name = e.getDisplay().getName();
      }
      catch (VisADException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
      FancySSCell fcell = (FancySSCell) BasicSSCell.getSSCellByName(name);
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
      if (BasicSSCell.DEBUG && (ci < 0 || cj < 0)) {
        System.err.println("Warning: an unknown display change occurred: " +
          "display (" + name + ") has changed, but there is no " +
          "corresponding SSCell with that name!");
      }
      selectCell(ci, cj);
    }
  }

  /** handle key presses */
  public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();

    int ci = CurX;
    int cj = CurY;
    boolean changed = false;
    if (key == KeyEvent.VK_RIGHT && ci < NumVisX - 1) {
      ci++;
      changed = true;
    }
    if (key == KeyEvent.VK_LEFT && ci > 0) {
      ci--;
      changed = true;
    }
    if (key == KeyEvent.VK_UP && cj > 0) {
      cj--;
      changed = true;
    }
    if (key == KeyEvent.VK_DOWN && cj < NumVisY - 1) {
      cj++;
      changed = true;
    }
    if (changed) selectCell(ci, cj);
  }

  /** unused KeyListener method */
  public void keyReleased(KeyEvent e) { }

  /** unused KeyListener method */
  public void keyTyped(KeyEvent e) { }

  /** old x value used with cell resizing logic */
  private int oldX;

  /** old y value used with cell resizing logic */
  private int oldY;

  /** handle mouse presses */
  public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) {
        if (c == DisplayCells[i][j]) {
          selectCell(i, j);
          return;
        }
      }
    }
    oldX = e.getX();
    oldY = e.getY();
  }

  /** handle cell resizing */
  public void mouseReleased(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();
    Component c = e.getComponent();
    boolean change = false;
    for (int j=0; j<NumVisX; j++) {
      if (c == HorizDrag[j]) {
        change = true;
        break;
      }
    }
    for (int j=0; j<NumVisY; j++) {
      if (c == VertDrag[j]) {
        change = true;
        break;
      }
    }
    if (change) synchLabelAndCellSizes();
  }

  /** handle cell label resizing */
  public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    int x = e.getX();
    int y = e.getY();
    for (int j=0; j<NumVisX; j++) {
      if (c == HorizDrag[j]) {
        // resize columns (labels)
        Dimension s = HorizLabel[j].getSize();
        int oldW = s.width;
        s.width += x - oldX;
        if (s.width < MIN_VIS_WIDTH) s.width = MIN_VIS_WIDTH;
        HorizLabel[j].setSize(s);
        HorizLabel[j].setPreferredSize(s);
        HorizLabels.validate();
        return;
      }
    }
    for (int j=0; j<NumVisY; j++) {
      if (c == VertDrag[j]) {
        // resize rows (labels)
        Dimension s = VertLabel[j].getSize();
        int oldH = s.height;
        s.height += y - oldY;
        if (s.height < MIN_VIS_HEIGHT) s.height = MIN_VIS_HEIGHT;
        VertLabel[j].setSize(s);
        VertLabel[j].setPreferredSize(s);
        VertLabels.validate();
        return;
      }
    }
  }

  /** unused MouseListener method */
  public void mouseClicked(MouseEvent e) { }

  /** unused MouseListener method */
  public void mouseEntered(MouseEvent e) { }

  /** unused MouseListener method */
  public void mouseExited(MouseEvent e) { }

  /** unused MouseMotionListener method */
  public void mouseMoved(MouseEvent e) { }

  /** handle changes in a cell's data */
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


  // *** Miscellaneous methods ***

  /** select the specified cell and update screen info */
  void selectCell(int x, int y) {
    if (x < 0) x = 0;
    if (y < 0) y = 0;
    if (x >= NumVisX) x = NumVisX - 1;
    if (y >= NumVisY) y = NumVisY - 1;

    // update borders of all cells
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) {
        DisplayCells[i][j].setSelected(x == i && y == j);
      }
    }

    // update spreadsheet info
    CurX = x;
    CurY = y;
    FormulaOk.requestFocus();
    refreshFormulaBar();
    refreshMenuCommands();
    refreshDisplayMenuItems();
  }

  /** display an error in a message dialog */
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

  /** add a button to a toolbar */
  private JButton addToolbarButton(String file, String tooltip,
    String command, boolean enabled, JComponent parent)
  {
    URL url = SpreadSheet.class.getResource(file);
    ImageIcon icon = new ImageIcon(url);
    if (icon != null) {
      JButton b = new JButton(icon);
      b.setAlignmentY(JButton.CENTER_ALIGNMENT);
      b.setToolTipText(tooltip);
      b.addActionListener(this);
      b.setActionCommand(command);
      b.setEnabled(enabled);
      if (parent instanceof JPanel) {
        int w = icon.getIconWidth() + 4;
        int h = icon.getIconHeight() + 4;
        b.setPreferredSize(new Dimension(w, h));
      }
      parent.add(b);
      return b;
    }
    else return null;
  }

}

