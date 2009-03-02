//
// SpreadSheet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
import java.awt.print.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import visad.*;
import visad.data.Form;
import visad.data.netcdf.Plain;
import visad.data.tiff.TiffForm;
import visad.data.visad.VisADForm;
import visad.formula.*;
import visad.util.*;

/**
 * SpreadSheet is a user interface for VisAD that supports
 * multiple 3-D displays (FancySSCells).
 */
public class SpreadSheet extends GUIFrame implements AdjustmentListener,
  DisplayListener, KeyListener, ItemListener, MouseListener,
  MouseMotionListener, SSCellListener
{

  /**
   * Starting width of the application, in percentage of screen size.
   */
  protected static final int WIDTH_PERCENT = 60;

  /**
   * Starting width of the application, in percentage of screen size.
   */
  protected static final int HEIGHT_PERCENT = 80;

  /**
   * Minimum VisAD display width, including display border.
   */
  protected static final int MIN_VIS_WIDTH = 120;

  /**
   * Minimum VisAD display height, including display border.
   */
  protected static final int MIN_VIS_HEIGHT = 120;

  /**
   * Default VisAD display width.
   */
  protected static final int DEFAULT_VIS_WIDTH = 250;

  /**
   * Default VisAD display height.
   */
  protected static final int DEFAULT_VIS_HEIGHT = 250;

  /**
   * Spreadsheet cell letter order.
   */
  protected static final String Letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /**
   * Vertical cell label's width.
   */
  protected static final int LABEL_WIDTH = 30;

  /**
   * Horizontal cell label's height.
   */
  protected static final int LABEL_HEIGHT = 20;

  /**
   * Whether connection status messages are printed about clones.
   */
  protected static final boolean SHOW_CONNECT_MESSAGES = true;

  /**
   * Header for first line of spreadsheet files.
   */
  protected static final String SSFileHeader =
    "# VisAD Visualization SpreadSheet spreadsheet file";

  /**
   * Argument classes for constructing an SSCell.
   */
  protected static final Class[] cellArgs = {
    String.class, FormulaManager.class, RemoteServer.class,
    boolean.class, String.class, Frame.class
  };


  /**
   * Constructor used to create SSCells for SpreadSheets.
   */
  protected static Constructor cellConstructor;

  /**
   * Whether Java3D is possible on this JVM.
   */
  protected static boolean Possible3D;

  /**
   * Whether Java3D is enabled on this JVM.
   */
  protected static boolean CanDo3D;

  /**
   * Whether the HDF-5 native library is present on this JVM.
   */
  protected static boolean CanDoHDF5;

  /**
   * Whether this JVM supports saving JPEG images with JPEGImageEncoder.
   */
  protected static boolean CanDoJPEG;

  /**
   * Whether this JVM supports JPython scripting.
   */
  protected static boolean CanDoPython;

  /**
   * Whether spreadsheet should have toolbar buttons.
   */
  protected static boolean BugFix;


  /**
   * File dialog.
   */
  protected JFileChooser SSFileDialog;

  /**
   * Base title.
   */
  protected String bTitle;

  /**
   * Number of display columns.
   */
  protected int NumVisX;

  /**
   * Number of display rows.
   */
  protected int NumVisY;

  /**
   * Formula manager.
   */
  protected FormulaManager fm;


  /**
   * Server name, if any.
   */
  protected String serverName;

  /**
   * Server address for a cloned sheet, if any.
   */
  protected String cloneAddress;

  /**
   * Server for spreadsheet cells, if any.
   */
  protected RemoteServerImpl rsi = null;

  /**
   * Whether spreadsheet is a clone of another spreadsheet.
   */
  protected boolean IsRemote = false;

  /**
   * Whether spreadsheet is a slaved clone of another spreadsheet.
   */
  protected boolean IsSlave = false;

  /**
   * ID number for this collaborative spreadsheet.
   */
  protected double CollabID = 0;

  /**
   * Row and column information needed for spreadsheet cloning.
   */
  protected RemoteDataReference RemoteColRow;

  /**
   * Remote clone's copy of CanDo3D.
   */
  protected RemoteDataReference RemoteCanDo3D;


  /**
   * Flag marking whether spreadsheet's cells
   * automatically switch dimensions when needed.
   */
  protected boolean AutoSwitch = true;

  /**
   * Flag marking whether spreadsheet's cells
   * automatically detect mappings.
   */
  protected boolean AutoDetect = true;

  /**
   * Flag marking whether spreadsheet's cells
   * automatically show controls.
   */
  protected boolean AutoShowControls = true;


  /**
   * Panel that contains actual VisAD displays.
   */
  protected Panel DisplayPanel;

  /**
   * Panel containing the scrolling pane.
   */
  protected JPanel ScrollPanel;

  /**
   * Scrolling pane, in case sheet gets too small.
   */
  protected ScrollPane SCPane;

  /**
   * View port for horizontal cell labels.
   */
  protected JViewport HorizLabels;

  /**
   * View port for vertical cell labels.
   */
  protected JViewport VertLabels;

  /**
   * Array of panels for horizontal labels.
   */
  protected JPanel[] HorizLabel;

  /**
   * Array of panels for vertical labels.
   */
  protected JPanel[] VertLabel;

  /**
   * Array of horizontal yellow sizing boxes.
   */
  protected JComponent[] HorizDrag;

  /**
   * Array of vertical yellow sizing boxes.
   */
  protected JComponent[] VertDrag;

  /**
   * Panel containing horizontal labels and sizing boxes.
   */
  protected JPanel HorizPanel;

  /**
   * Panel containing vertical labels and sizing boxes.
   */
  protected JPanel VertPanel;

  /**
   * Array of spreadsheet cells.
   */
  protected FancySSCell[][] DisplayCells = null;

  /**
   * Formula bar.
   */
  protected JComboBox FormulaBox;

  /**
   * Formula editor.
   */
  protected ComboBoxEditor FormulaEditor;

  /**
   * Formula text field.
   */
  protected JTextField FormulaText;

  /**
   * Formula action listener.
   */
  protected ActionListener FormulaListener;


  /**
   * Tool bar.
   */
  protected JToolBar Toolbar;

  /**
   * Submenus.
   */
  protected JMenu FileExport;
   
  /**
   * Menu items.
   */
  protected JMenuItem FileSave1, FileSave2, FileSave3, FileSave4, FileSave5,
    FileSnap, EditPaste, EditClear, CellDel, CellPrint, CellEdit, CellReset,
    CellShow, LayAddCol, LayDelCol, LayDelRow;

  /**
   * Checkbox menu items.
   */
  protected JCheckBoxMenuItem CellDim3D3D, CellDim2D2D, CellDim2D3D,
    AutoSwitchBox, AutoDetectBox, AutoShowBox;

  /**
   * Toolbar buttons.
   */
  protected JButton ToolSave, ToolPaste, Tool3D, Tool2D, ToolJ2D, ToolMap,
    ToolShow, ToolReset, FormulaAdd, FormulaDel;


  /**
   * Column of currently selected cell.
   */
  protected int CurX = 0;

  /**
   * Row of currently selected cell.
   */
  protected int CurY = 0;

  /**
   * Contents of clipboard.
   */
  protected String Clipboard = null;

  /**
   * Current spreadsheet file.
   */
  protected File CurrentFile = null;

  /**
   * Object for preventing simultaneous GUI manipulation.
   */
  protected Object Lock = new Object();


  /**
   * Waits the specified number of milliseconds.
   */
  public static void snooze(long ms) {
    try {
      Thread.sleep(ms);
    }
    catch (InterruptedException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
  }

  /**
   * Gateway into VisAD Visualization SpreadSheet user interface.
   */
  public static void main(String[] argv) {
    String usage = "\n" +
      "Usage: java [-mx###m] visad.ss.SpreadSheet [cols rows]\n" +
      "       [-file filename] [-gui] [-no3d] [-debug] [-bugfix]\n" +
      "       [-server name] [-client address] [-slave address]\n\n" +
      "###\n" +
      "     Maximum megabytes of memory to use.\n" +
      "cols\n" +
      "     Number of columns in this SpreadSheet.\n" +
      "rows\n" +
      "     Number of rows in this SpreadSheet.\n" +
      "-file filename\n" +
      "     Load the given filename at launch. If file is a\n" +
      "     spreadsheet file, the layout is configured accordingly.\n" +
      "     If file is data, it is loaded into cell A1.\n" +
      "-gui\n" +
      "     Pop up an options window so that the user can\n" +
      "     select SpreadSheet settings graphically.\n" +
      "-no3d\n" +
      "     Disable Java3D.\n" +
      "-debug\n" +
      "     Print stack traces for all errors.\n" +
      "-bugfix\n" +
      "     Disable toolbar. For some systems, will prevent\n" +
      "     lockups on spreadsheet start.\n" +
      "-server name\n" +
      "     Initialize this SpreadSheet as an RMI server\n" +
      "     with the given name.\n" +
      "-client address\n" +
      "     Initialize this SpreadSheet as a clone of\n" +
      "     the server at the given RMI address.\n" +
      "-slave address\n" +
      "     Initialize this SpreadSheet as a slaved clone\n" +
      "     of the server at the given RMI address.";
    int cols = 2;
    int rows = 2;
    String dfile = null;
    String servname = null;
    String clonename = null;
    boolean guiOptions = false;
    int len = argv.length;
    if (len > 0) {
      int ix = 0;

      // parse command line flags
      while (ix < len) {
        if (argv[ix].charAt(0) == '-') {
          if (argv[ix].equals("-file")) {
            if (ix < len - 1) dfile = argv[++ix];
          }
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
          else if (argv[ix].equals("-client") || argv[ix].equals("-slave")) {
            if (servname != null) {
              System.out.println("A spreadsheet cannot be both a server " +
                "and a clone!");
              System.out.println(usage);
              System.exit(3);
            }
            else if (ix < len - 1) {
              clonename = argv[ix + 1];
              if (argv[ix].equals("-slave")) clonename = "slave:" + clonename;
              ix++;
            }
            else {
              System.out.println("You must specify a server after " +
                "the '" + argv[ix] + "' flag!");
              System.out.println(usage);
              System.exit(5);
            }
          }
          else if (argv[ix].equals("-gui")) guiOptions = true;
          else if (argv[ix].equals("-bugfix")) BugFix = true;
          else if (argv[ix].equals("-no3d")) BasicSSCell.disable3D();
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
          if (ix < len - 1) {
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
    final SpreadSheet ss = new SpreadSheet(WIDTH_PERCENT, HEIGHT_PERCENT,
      cols, rows, servname, clonename, "VisAD SpreadSheet", null, guiOptions);
    if (dfile != null) {
      File f = new File(dfile);
      String line = null;
      try {
        BufferedReader fin = new BufferedReader(new FileReader(f));
        line = fin.readLine();
        fin.close();
      }
      catch (IOException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
        System.out.println("Could not read file " + dfile);
      }
      if (line != null) {
        final boolean ssfile = line.equals(SSFileHeader);
        final String filename = dfile;
        Util.invoke(false, BasicSSCell.DEBUG, new Runnable() {
          public void run() {
            try {
              if (ssfile) {
                // file is a spreadsheet file
                ss.openFile(filename);
              }
              else {
                // file is a data file
                ss.DisplayCells[0][0].addDataSource(filename);
              }
            }
            catch (Exception exc) {
              if (BasicSSCell.DEBUG) exc.printStackTrace();
              System.out.println("Could not load file " + filename +
                " into the SpreadSheet");
            }
          }
        });
      }
    }
  }


  // --- CONSTRUCTORS ---

  /**
   * Constructor with option selection dialog at default values.
   */
  public SpreadSheet() {
    this(WIDTH_PERCENT, HEIGHT_PERCENT, 2, 2, null, null,
      "VisAD SpreadSheet", null, true);
  }

  /**
   * Constructor with default formula manager and no option selection dialog.
   */
  public SpreadSheet(int sWidth, int sHeight, int cols, int rows,
    String server, String clone, String sTitle)
  {
    this(sWidth, sHeight, cols, rows, server, clone, sTitle, null, false);
  }

  /**
   * Constructor with no option selection dialog.
   */
  public SpreadSheet(int sWidth, int sHeight, int cols, int rows,
    String server, String clone, String sTitle, FormulaManager fm)
  {
    this(sWidth, sHeight, cols, rows, server, clone, sTitle, fm, false);
  }

  /**
   * Main constructor.
   */
  public SpreadSheet(int sWidth, int sHeight, int cols, int rows,
    String server, String clone, String sTitle, FormulaManager fm,
    boolean chooseOptions)
  {
    super(true);
    bTitle = sTitle;
    NumVisX = cols;
    NumVisY = rows;
    this.fm = fm;
    Possible3D = BasicSSCell.possible3D();
    CanDo3D = BasicSSCell.canDo3D();
    MappingDialog.initDialog();
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        quitProgram();
      }
    });
    setBackground(Color.white);

    // parse clone address
    boolean slave = clone != null && clone.startsWith("slave:");
    if (slave) clone = clone.substring(6);
    if (clone != null) {
      int slash = clone.lastIndexOf("/");
      if (slash < 0) slash = clone.lastIndexOf(":");
      server = clone.substring(slash + 1);
      clone = clone.substring(0, slash);
    }
    serverName = server;
    cloneAddress = clone;
    IsSlave = slave;

    // test whether HDF-5 native library is present
    CanDoHDF5 = Util.canDoHDF5();
    if (!CanDoHDF5 && BasicSSCell.DEBUG) {
      System.err.println("Warning: HDF-5 library not found");
    }

    // test whether JPEG codec is present
    CanDoJPEG = Util.canDoJPEG();
    if (!CanDoJPEG && BasicSSCell.DEBUG) {
      System.err.println("Warning: JPEG codec not found");
    }

    // test whether JPython is present
    CanDoPython = Util.canDoPython();
    if (!CanDoPython && BasicSSCell.DEBUG) {
      System.err.println("Warning: JPython not found");
    }

    // create file chooser dialog
    SSFileDialog = new JFileChooser(System.getProperty("user.dir"));
    SSFileDialog.addChoosableFileFilter(
      new ExtensionFileFilter("ss", "SpreadSheet files"));

    if (chooseOptions) {
      // get settings from option selection dialog
      getOptions(NumVisX, NumVisY, serverName, cloneAddress, IsSlave);
    }
    clone = cloneAddress == null ? null : cloneAddress + "/" + serverName;
    server = cloneAddress == null ? serverName : null;

    // determine information for spreadsheet cloning
    RemoteServer rs = null;
    String[][] cellNames = null;
    if (clone != null) {
      // CLIENT: initialize

      // connect to server
      boolean success = true;
      if (SHOW_CONNECT_MESSAGES) {
        System.out.print("Connecting to " + clone + " ");
      }

      // connection loop
      while (cellNames == null && success) {
        // wait a second before trying to connect
        snooze(1000);
        if (SHOW_CONNECT_MESSAGES) System.out.print(".");

        try {
          // look up server
          rs = (RemoteServer) Naming.lookup("//" + clone);

          // determine whether server supports Java3D
          RemoteCanDo3D = rs.getDataReference("CanDo3D");
          Real bit = (Real) RemoteCanDo3D.getData();
          if (bit.getValue() == 0) {
            CanDo3D = false;
            BasicSSCell.disable3D();
          }

          // extract cell name information
          RemoteColRow = rs.getDataReference("ColRow");
          cellNames = getNewCellNames();
        }
        catch (UnmarshalException exc) {
          // fatal RMI error, probably a version difference; display error box
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage("Unable to clone the spreadsheet at " + clone +
            ". The server is using an incompatible version of Java", null,
            "Failed to clone spreadsheet");
          success = false;
        }
        catch (MalformedURLException exc) {
          // server name is invalid; display error box
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage("Unable to clone the spreadsheet at " + clone +
            ". The server name is not valid", null,
            "Failed to clone spreadsheet");
          success = false;
        }
        catch (VisADException exc) {
          // fatal error of some other type; display error box
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage("Unable to clone the spreadsheet at " + clone +
            ". An error occurred while downloading the necessary data", exc,
            "Failed to clone spreadsheet");
          success = false;
        }
        catch (NotBoundException exc) {
          // server is not ready yet; try again
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        catch (NullPointerException exc) {
          // server is not ready yet; try again
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          // server is not ready yet; try again
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
      }

      if (success) {
        if (SHOW_CONNECT_MESSAGES) System.out.println(" done");
        bTitle = bTitle + " [" + (IsSlave ? "slaved" : "collaborative") +
          " mode: " + clone + "]";
        IsRemote = true;
      }
      else {
        if (SHOW_CONNECT_MESSAGES) System.out.println(" failed");
        IsSlave = false;
        rs = null;
      }
    }

    // set up the content pane
    JPanel pane = new JPanel();
    pane.setBackground(Color.white);
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    setContentPane(pane);

    // set up file menu
    addMenuItem("File", "Import data...", "loadDataSet", 'i');
    FileExport = addSubMenu("File", "Export data", 'e', false);
    FileSave1 = addMenuItem("Export data", "netCDF...",
      "exportDataSetNetcdf", 'n', true);
    FileSave3 = addMenuItem("Export data", "HDF-5...",
      "exportDataSetHDF5", 'h', CanDoHDF5);
    FileSave4 = addMenuItem("Export data", "TIFF...",
      "exportDataSetTIFF", 't', true);
    FileSave2 = addMenuItem("Export data", "Serialized...",
      "exportDataSetSerial", 's', true);
    FileSave5 = addMenuItem("Export data", "Binary...",
      "exportDataSetBinary", 'b', true);
    addMenuSeparator("File");
    FileSnap = addMenuItem("File", "Take JPEG snapshot...",
      "captureImageJPEG", 'j', false);
    addMenuSeparator("File");
    addMenuItem("File", "Exit", "quitProgram", 'x');

    // set up edit menu
    addMenuItem("Edit", "Cut", "cutCell", 't', !IsRemote);
    addMenuItem("Edit", "Copy", "copyCell", 'c', !IsRemote);
    EditPaste = addMenuItem("Edit", "Paste", "pasteCell", 'p', false);
    EditClear = addMenuItem("Edit", "Clear", "clearCell", 'l', false);

    // set up setup menu
    addMenuItem("Setup", "New spreadsheet file", "newFile", 'n');
    addMenuItem("Setup", "Open spreadsheet file...", "openFile", 'o',
      !IsRemote);
    addMenuItem("Setup", "Save spreadsheet file", "saveFile", 's', !IsRemote);
    addMenuItem("Setup", "Save spreadsheet file as...", "saveAsFile", 'a',
      !IsRemote);

    // set up cell menu
    CellDim3D3D = new JCheckBoxMenuItem("3-D (Java3D)", CanDo3D);
    addMenuItem("Cell", CellDim3D3D, "setDim3D", '3', CanDo3D);
    CellDim2D2D = new JCheckBoxMenuItem("2-D (Java2D)", !CanDo3D);
    addMenuItem("Cell", CellDim2D2D, "setDimJ2D", 'j', true);
    CellDim2D3D = new JCheckBoxMenuItem("2-D (Java3D)", false);
    addMenuItem("Cell", CellDim2D3D, "setDim2D", '2', CanDo3D);
    addMenuSeparator("Cell");
    addMenuItem("Cell", "Add data object", "formulaAdd", 'a');
    CellDel = addMenuItem("Cell", "Delete data object",
      "formulaDel", 'd', false);
    addMenuSeparator("Cell");
    CellPrint = addMenuItem("Cell", "Print cell...",
      "printCurrentCell", 'p', false);
    addMenuSeparator("Cell");
    CellEdit = addMenuItem("Cell", "Edit mappings...",
      "createMappings", 'e', false);
    CellReset = addMenuItem("Cell", "Reset orientation",
      "resetOrientation", 'r', false);
    CellShow = addMenuItem("Cell", "Show controls",
      "showControls", 's', false);

    // set up layout menu
    LayAddCol = addMenuItem("Layout", "Add column", "addColumn", 'c');
    addMenuItem("Layout", "Add row", "addRow", 'r');
    LayDelCol = addMenuItem("Layout", "Delete column",
      "deleteColumn", 'l', NumVisX > 1);
    LayDelRow = addMenuItem("Layout", "Delete row",
      "deleteRow", 'w', NumVisY > 1);
    addMenuSeparator("Layout");
    addMenuItem("Layout", "Tile cells", "tileCells", 't');

    // set up options menu
    if (!CanDo3D) AutoSwitch = false;
    AutoSwitchBox = new JCheckBoxMenuItem("Auto-switch to 3-D",
      AutoSwitch && !IsRemote);
    addMenuItem("Options", AutoSwitchBox,
      "optionsSwitch", '3', CanDo3D && !IsRemote);
    AutoDetectBox = new JCheckBoxMenuItem("Auto-detect mappings",
      AutoDetect && !IsRemote);
    addMenuItem("Options", AutoDetectBox, "optionsDetect", 'm', !IsRemote);
    AutoShowBox = new JCheckBoxMenuItem("Auto-display controls",
      AutoShowControls && !IsSlave);
    addMenuItem("Options", AutoShowBox, "optionsDisplay", 'c', !IsSlave);

    // set up toolbar
    if (!BugFix) {
      Toolbar = new JToolBar();
      Toolbar.setBackground(Color.lightGray);
      Toolbar.setBorder(new EtchedBorder());
      Toolbar.setFloatable(false);
      pane.add(Toolbar);

      // file menu toolbar icons
      addToolbarButton("open", "Import data", "loadDataSet", true, Toolbar);
      ToolSave = addToolbarButton("save", "Export data to netCDF",
        "exportDataSetNetcdf", false, Toolbar);
      Toolbar.addSeparator();

      // edit menu toolbar icons
      addToolbarButton("cut", "Cut", "cutCell", !IsRemote, Toolbar);
      addToolbarButton("copy", "Copy", "copyCell", !IsRemote, Toolbar);
      ToolPaste = addToolbarButton("paste", "Paste",
        "pasteCell", false, Toolbar);
      Toolbar.addSeparator();

      // cell menu toolbar icons
      Tool3D = addToolbarButton("3d", "3-D (Java3D)",
        "setDim3D", false, Toolbar);
      ToolJ2D = addToolbarButton("j2d", "2-D (Java2D)",
        "setDimJ2D", CanDo3D, Toolbar);
      Tool2D = addToolbarButton("2d", "2-D (Java3D)",
        "setDim2D", CanDo3D, Toolbar);
      Toolbar.addSeparator();
      ToolMap = addToolbarButton("mappings", "Edit mappings",
        "createMappings", false, Toolbar);
      ToolReset = addToolbarButton("reset", "Reset orientation",
        "resetOrientation", false, Toolbar);
      ToolShow = addToolbarButton("show", "Show controls",
        "showControls", false, Toolbar);
      Toolbar.addSeparator();

      // layout menu toolbar icon
      addToolbarButton("tile", "Tile cells", "tileCells", true, Toolbar);
      Toolbar.add(Box.createHorizontalGlue());
    }

    // set up formula bar
    JPanel formulaPanel = new JPanel();
    formulaPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 25));
    formulaPanel.setBackground(Color.lightGray);
    formulaPanel.setLayout(new BoxLayout(formulaPanel, BoxLayout.X_AXIS));
    formulaPanel.setBorder(new EtchedBorder());
    pane.add(formulaPanel);
    pane.add(Box.createRigidArea(new Dimension(0, 6)));
    if (!BugFix) {
      FormulaAdd = addToolbarButton("add", "Add data",
        "formulaAdd", true, formulaPanel);
      FormulaDel = addToolbarButton("del", "Remove data",
        "formulaDel", true, formulaPanel);
    }
    FormulaBox = new JComboBox();
    formulaPanel.add(FormulaBox);
    FormulaBox.setLightWeightPopupEnabled(false);
    FormulaBox.setEditable(true);
    FormulaEditor = FormulaBox.getEditor();
    FormulaListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String newItem = ((String) FormulaEditor.getItem()).trim();
        try {
          int id = 0;
          int type = BasicSSCell.UNKNOWN_SOURCE;
          boolean notify = true;

          int index1 = newItem.indexOf("d", 2);
          int index2 = newItem.indexOf(":");
          if (index1 > 0 && index2 > 0 && index1 < index2) {
            String cellName = newItem.substring(0, index1);
            BasicSSCell ssCell = BasicSSCell.getSSCellByName(cellName);
            int dataId = 0;
            try {
              dataId = Integer.parseInt(newItem.substring(index1 + 1, index2));
            }
            catch (NumberFormatException exc) {
              if (BasicSSCell.DEBUG && BasicSSCell.DEBUG_LEVEL >= 3) {
                exc.printStackTrace();
              }
            }
            if (ssCell == DisplayCells[CurX][CurY] && dataId > 0) {
              // user is 'editing' the data object
              String varName = newItem.substring(0, index2);
              String source = ssCell.getDataSource(varName);
              if (source != null) {
                ssCell.removeData(varName);
                String oldItem = null;
                for (int i=0; i<FormulaBox.getItemCount(); i++) {
                  String item = (String) FormulaBox.getItemAt(i);
                  if (item.startsWith(varName + ":")) {
                    oldItem = item;
                    break;
                  }
                }
                if (oldItem != null) {
                  // remove old item from FormulaBox
                  FormulaBox.removeItem(oldItem);
                }
              }
              id = dataId;
              newItem = newItem.substring(index2 + 1).trim();
            }
          }
          String varName = DisplayCells[CurX][CurY].addDataSource(
            id, newItem, type, notify);
          String itemString = varName + ": " + newItem;
          FormulaBox.addItem(itemString);
          FormulaBox.setSelectedItem(itemString);
          FormulaText.getCaret().setVisible(true); // BIG HAMMER HACK
        }
        catch (VisADException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage(
            "Unable to compute data object from \"" + newItem + "\"", exc,
            "VisAD SpreadSheet error");
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage(
            "Unable to compute data object from \"" + newItem + "\"", exc,
            "VisAD SpreadSheet error");
        }
      }
    };
    FormulaEditor.addActionListener(FormulaListener);
    FormulaText = (JTextField) FormulaEditor.getEditorComponent();

    // set up horizontal spreadsheet cell labels
    JPanel horizShell = new JPanel();
    horizShell.setBackground(Color.white);
    horizShell.setLayout(new BoxLayout(horizShell, BoxLayout.X_AXIS));
    horizShell.add(Box.createRigidArea(new Dimension(LABEL_WIDTH + 6, 0)));
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

    // BIG HAMMER HACK
    String os = System.getProperty("os.name");
    if (!os.startsWith("Windows")) { // Windows does not need the hack
      addKeyListener(this);
      SCPane.addKeyListener(this);
      ScrollPanel.addKeyListener(this);
      DisplayPanel.addKeyListener(this);
      FormulaBox.addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) { SCPane.requestFocus(); }
      });
    }

    DataReferenceImpl lColRow = null;
    if (server != null) {
      // SERVER: initialize
      boolean success = true;
      boolean registryStarted = false;
      while (true) {
        try {
          rsi = new RemoteServerImpl();
          Naming.rebind("///" + server, rsi);
          break;
        }
        catch (java.rmi.ConnectException exc) {
          if (!registryStarted) {
            try {
              LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
              registryStarted = true;
            }
            catch (RemoteException rexc) {
              if (BasicSSCell.DEBUG) exc.printStackTrace();
              displayErrorMessage("Unable to autostart rmiregistry. " +
                "Please start rmiregistry before launching the " +
                "SpreadSheet in server mode", null,
                "Failed to initialize RemoteServer");
              success = false;
            }
          }
          else {
            displayErrorMessage("Unable to export cells as RMI addresses. " +
              "Make sure you are running rmiregistry before launching the " +
              "SpreadSheet in server mode", null,
              "Failed to initialize RemoteServer");
            success = false;
          }
        }
        catch (MalformedURLException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage("Unable to export cells as RMI addresses. " +
            "The name \"" + server + "\" is not valid", null,
            "Failed to initialize RemoteServer");
          success = false;
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage("Unable to export cells as RMI addresses", exc,
            "Failed to initialize RemoteServer");
          success = false;
        }

        if (!success) break;
      }

      // set up info for spreadsheet cloning
      try {
        // set flag for whether server has Java3D enabled
        DataReferenceImpl lCanDo3D = new DataReferenceImpl("CanDo3D");
        RemoteCanDo3D = new RemoteDataReferenceImpl(lCanDo3D);
        RemoteCanDo3D.setData(new Real(CanDo3D ? 1 : 0));
        rsi.addDataReference((RemoteDataReferenceImpl) RemoteCanDo3D);
        // set up remote reference for conveying cell layout information
        lColRow = new DataReferenceImpl("ColRow");
        RemoteColRow = new RemoteDataReferenceImpl(lColRow);
        rsi.addDataReference((RemoteDataReferenceImpl) RemoteColRow);
      }
      catch (VisADException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
        displayErrorMessage("Unable to export cells as RMI addresses. " +
          "An error occurred setting up the necessary data", exc,
          "Failed to initialize RemoteServer");
        success = false;
      }
      catch (RemoteException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
        displayErrorMessage("Unable to export cells as RMI addresses. " +
          "A remote error occurred setting up the necessary data", exc,
          "Failed to initialize RemoteServer");
        success = false;
      }

      if (success) bTitle = bTitle + " (" + server + ")";
      else rsi = null;
    }

    // construct spreadsheet cells
    if (rs == null) constructSpreadsheetCells(null);
    else {
      NumVisX = cellNames.length;
      NumVisY = cellNames[0].length;
      reconstructLabels(cellNames, null, null);
      constructSpreadsheetCells(cellNames, rs);
    }
    if (rsi != null) synchColRow();
    CollabID = DisplayCells[0][0].getRemoteId();

    if (rsi != null || IsRemote) {
      // update spreadsheet when remote row and column information changes
      final RemoteServer frs = rs;
      CellImpl lColRowCell = new CellImpl() {
        public void doAction() {
          // extract new cell information
          if (getColRowID() != CollabID) {
            Util.invoke(true, BasicSSCell.DEBUG, new Runnable() {
              public void run() {
                // update is coming from a different sheet
                String[][] cellNamesx = getNewCellNames();
                if (cellNamesx == null) {
                  if (BasicSSCell.DEBUG) System.out.println("Warning: " +
                    "could not obtain new spreadsheet dimensions!");
                  return;
                }
                int oldNVX = NumVisX;
                int oldNVY = NumVisY;
                NumVisX = cellNamesx.length;
                NumVisY = cellNamesx[0].length;
                if (NumVisX != oldNVX || NumVisY != oldNVY) {
                  // reconstruct spreadsheet cells and labels
                  reconstructSpreadsheet(cellNamesx, null, null, frs);
                  if (!IsRemote) synchColRow();
                }
              }
            });
          }
        }
      };
      try {
        RemoteCellImpl rColRowCell = new RemoteCellImpl(lColRowCell);
        rColRowCell.addReference(RemoteColRow);
      }
      catch (VisADException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
        displayErrorMessage("Remote cell error (1)",
          exc, "VisAD SpreadSheet error");
      }
      catch (RemoteException exc) {
        try {
          lColRowCell.addReference(lColRow);
        }
        catch (VisADException exc2) {
          if (BasicSSCell.DEBUG) exc2.printStackTrace();
          displayErrorMessage("Remote cell error (2)",
            exc2, "VisAD SpreadSheet error");
        }
        catch (RemoteException exc2) {
          if (BasicSSCell.DEBUG) exc2.printStackTrace();
          displayErrorMessage("Remote cell error (3)",
            exc2, "VisAD SpreadSheet error");
        }
      }
    }

    // display window on screen
    setTitle(bTitle);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int appWidth = (int) (0.01 * sWidth * screenSize.width);
    int appHeight = (int) (0.01 * sHeight * screenSize.height);
    setSize(appWidth, appHeight);
    Util.centerWindow(this);
    setVisible(true);

    // wait for frame to lay itself out, then tile cells
    snooze(500);
    FormulaText.getCaret().setVisible(true); // BIG HAMMER HACK
    tileCells();
  }


  // --- FILE MENU ---

  /**
   * Imports a data set.
   */
  public void loadDataSet() {
    DisplayCells[CurX][CurY].loadDataDialog();
  }

  /**
   * Exports a data set to netCDF format.
   */
  public void exportDataSetNetcdf() {
    try {
      exportDataSet(new Plain());
    }
    catch (VisADException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
      displayErrorMessage("Error initializing netCDF export", exc,
        "VisAD SpreadSheet error");
    }
  }

  /**
   * Exports a data set to serialized data format.
   */
  public void exportDataSetSerial() {
    exportDataSet(new VisADForm());
  }

  /**
   * Exports a data set to HDF-5 format.
   */
  public void exportDataSetHDF5() {
    Form hdf5form = null;
    try {
      ClassLoader cl = ClassLoader.getSystemClassLoader();
      Class hdf5form_class = cl.loadClass("visad.data.hdf5.HDF5Form");
      hdf5form = (Form) hdf5form_class.newInstance();
    }
    catch (Exception exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
      displayErrorMessage("Error initializing HDF-5 export", exc,
        "VisAD SpreadSheet error");
    }
    if (hdf5form != null) exportDataSet(hdf5form);
  }

  /**
   * Exports a data set to TIFF format.
   */
  public void exportDataSetTIFF() {
    exportDataSet(new TiffForm());
  }

  /**
   * Exports a data set to VisAD binary data format.
   */
  public void exportDataSetBinary() {
    exportDataSet(new VisADForm(true));
  }

  /**
   * Exports a data set using the given form.
   */
  public void exportDataSet(Form form) {
    String item = (String) FormulaBox.getSelectedItem();
    String varName = item.substring(0, item.indexOf(":"));
    DisplayCells[CurX][CurY].saveDataDialog(varName, form);
  }

  /**
   * Captures the display of the current cell and saves it as a JPEG image.
   */
  public void captureImageJPEG() {
    DisplayCells[CurX][CurY].captureDialog();
  }

  /**
   * Does any necessary clean-up, then quits the program.
   */
  public void quitProgram() {
    // hide frames
    try {
      DisplayCells[CurX][CurY].hideWidgetFrame();
    }
    catch (NullPointerException exc) { }
    setVisible(false);

    // wait for files to finish saving
    Thread t = new Thread() {
      public void run() {
        boolean b = BasicSSCell.isSaving();
        JFrame f = null;
        if (b) {
          // display "please wait" message in new frame
          f = new JFrame("Please wait");
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
        while (BasicSSCell.isSaving()) snooze(200);
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
                  displayErrorMessage("Cannot destroy cell (1)", exc,
                    "VisAD SpreadSheet error");
                }
                catch (RemoteException exc) {
                  if (BasicSSCell.DEBUG) exc.printStackTrace();
                  displayErrorMessage("Cannot destroy cell (2)", exc,
                    "VisAD SpreadSheet error");
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


  // --- EDIT MENU ---

  /**
   * Moves a cell from the screen to the clipboard.
   */
  public void cutCell() {
    if (DisplayCells[CurX][CurY].confirmClear()) {
      copyCell();
      clearCell(false);
    }
  }

  /**
   * Copies a cell from the screen to the clipboard.
   */
  public void copyCell() {
    Clipboard = DisplayCells[CurX][CurY].getSaveString();
    EditPaste.setEnabled(true);
    if (!BugFix) ToolPaste.setEnabled(true);
  }

  /**
   * Copies a cell from the clipboard to the screen.
   */
  public void pasteCell() {
    if (Clipboard != null) {
      try {
        boolean b = DisplayCells[CurX][CurY].getAutoDetect();
        DisplayCells[CurX][CurY].setAutoDetect(false);
        DisplayCells[CurX][CurY].setSaveString(Clipboard);
        DisplayCells[CurX][CurY].setAutoDetect(b);
      }
      catch (VisADException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
        displayErrorMessage("Cannot paste cell", exc,
          "VisAD SpreadSheet error");
      }
      catch (RemoteException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
        displayErrorMessage("Cannot paste cell", exc,
          "VisAD SpreadSheet error");
      }
    }
  }

  /**
   * Clears the mappings and formula of the current cell
   * if it is safe to do so, or if the user confirms the clear.
   */
  public void clearCell() {
    clearCell(true);
  }

  /**
   * Clears the mappings and formula of the current cell.
   */
  protected void clearCell(boolean checkSafe) {
    try {
      if (checkSafe) DisplayCells[CurX][CurY].smartClear();
      else DisplayCells[CurX][CurY].clearCell();
    }
    catch (VisADException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
      displayErrorMessage("Cannot clear display mappings", exc,
        "VisAD SpreadSheet error");
    }
    catch (RemoteException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
      displayErrorMessage("Cannot clear display mappings", exc,
        "VisAD SpreadSheet error");
    }
    refreshFormulaBar();
    refreshMenuCommands();
  }


  // --- SETUP MENU ---

  /**
   * Creates a new spreadsheet file, asking user to confirm first.
   * @return true if successful.
   */
  public boolean newFile() {
    return newFile(true);
  }

  /**
   * Creates a new spreadsheet file.
   * @return true if successful.
   */
  protected boolean newFile(boolean safe) {
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
              displayErrorMessage("Cannot clear cell (1)", exc,
                "VisAD SpreadSheet error");
            }
            catch (RemoteException exc) {
              if (BasicSSCell.DEBUG) exc.printStackTrace();
              displayErrorMessage("Cannot clear cell (2)", exc,
                "VisAD SpreadSheet error");
            }
          }
        }
      }
    }
    CurrentFile = null;
    setTitle(bTitle);
    return true;
  }

  /**
   * Opens an existing spreadsheet file chosen by the user.
   */
  public void openFile() {
    SSFileDialog.setDialogType(JFileChooser.OPEN_DIALOG);
    if (SSFileDialog.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
      // user has canceled request
      return;
    }

    // make sure file exists
    File f = SSFileDialog.getSelectedFile();
    if (!f.exists()) {
      displayErrorMessage("The file " + f.getName() + " does not exist", null,
        "VisAD SpreadSheet error");
      return;
    }
    openFile(f.getPath());
  }

  /**
   * Opens the specified spreadsheet file.
   */
  public void openFile(String file) {
    File f = new File(file);

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
      if (BasicSSCell.DEBUG) exc.printStackTrace();
      displayErrorMessage("Unable to read the file " + file + " from disk",
        null, "VisAD SpreadSheet error");
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
          "the required [Global] tag", null, "VisAD SpreadSheet error");
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
            "[Global] tag as its first entry", null,
            "VisAD SpreadSheet error");
          // reset auto-switch, auto-detect and auto-show
          setAutoSwitch(origSwitch);
          setAutoDetect(origDetect);
          setAutoShowControls(origShow);
          return;
        }

        // parse global information
        int endToken = tokenNum;
        while (tokens[endToken] != null &&
          tokens[endToken].trim().indexOf("[") != 0)
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
          eq = line.indexOf("=");
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
            int x = line.indexOf("x", eq);
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
            int x = line.indexOf("x", eq);
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
        "global dimension entry", null, "VisAD SpreadSheet error");
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
            displayErrorMessage("The file " + file + " is incomplete", null,
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
          tokens[last].trim().indexOf("[") != 0)
        {
          last++;
        }

        // build this cell's save string
        String s = "";
        for (int l=tokenNum; l<last; l++) s = s + tokens[l] + "\n";
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
          displayErrorMessage("Invalid save string", exc,
            "VisAD SpreadSheet error");
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage("Invalid save string", exc,
            "VisAD SpreadSheet error");
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

  /**
   * Saves a spreadsheet file under its current name.
   */
  public void saveFile() {
    if (CurrentFile == null) saveAsFile();
    else {
      // construct file header
      StringBuffer sb = new StringBuffer(1024 * NumVisX * NumVisY + 1024);
      sb.append(SSFileHeader);
      sb.append("\n");
      sb.append("# File ");
      sb.append(CurrentFile.getName());
      sb.append(" written at ");
      sb.append(Util.getTimestamp());
      sb.append("\n\n");

      // compile global information
      sb.append("[Global]\n");
      sb.append("sheet size = ");
      sb.append(getWidth());
      sb.append(" x ");
      sb.append(getHeight());
      sb.append("\n");
      sb.append("dimension = ");
      sb.append(NumVisX);
      sb.append(" x ");
      sb.append(NumVisY);
      sb.append("\n");
      sb.append("columns =");
      for (int j=0; j<NumVisX; j++) {
        sb.append(" ");
        sb.append(HorizLabel[j].getSize().width);
      }
      sb.append("\n");
      sb.append("rows =");
      for (int i=0; i<NumVisY; i++) {
        sb.append(" ");
        sb.append(VertLabel[i].getSize().height);
      }
      sb.append("\n");
      sb.append("auto switch = ");
      sb.append(AutoSwitch);
      sb.append("\n");
      sb.append("auto detect = ");
      sb.append(AutoDetect);
      sb.append("\n");
      sb.append("auto show = ");
      sb.append(AutoShowControls);
      sb.append("\n\n");

      // compile cell information
      for (int j=0; j<NumVisY; j++) {
        for (int i=0; i<NumVisX; i++) {
          sb.append("[");
          sb.append(DisplayCells[i][j].getName());
          sb.append("]\n");
          sb.append(DisplayCells[i][j].getSaveString());
          sb.append("\n");
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
        if (BasicSSCell.DEBUG) exc.printStackTrace();
        displayErrorMessage("Could not save file " + CurrentFile.getName() +
          ". Make sure there is enough disk space", null,
          "VisAD SpreadSheet error");
      }
    }
  }

  /**
   * Saves a spreadsheet file under a new name.
   */
  public void saveAsFile() {
    SSFileDialog.setDialogType(JFileChooser.SAVE_DIALOG);
    if (SSFileDialog.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
      // user has canceled request
      return;
    }

    // get file and make sure it is valid
    File f = SSFileDialog.getSelectedFile();
    CurrentFile = f;
    setTitle(bTitle + " - " + f.getPath());
    saveFile();
  }


  // --- CELL MENU ---

  /**
   * Sets the dimension of the current cell to 3-D (Java3D).
   */
  public void setDim3D() {
    setDim(BasicSSCell.JAVA3D_3D);
  }

  /**
   * Sets the dimension of the current cell to 2-D (Java2D).
   */
  public void setDimJ2D() {
    setDim(BasicSSCell.JAVA2D_2D);
  }

  /**
   * Sets the dimension of the current cell to 2-D (Java3D).
   */
  public void setDim2D() {
    setDim(BasicSSCell.JAVA3D_2D);
  }

  /**
   * Sets the dimension of the current cell.
   */
  protected void setDim(int dim) {
    try {
      DisplayCells[CurX][CurY].setDimension(dim);
    }
    catch (VisADException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
      displayErrorMessage("Cannot alter display dimension", exc,
        "VisAD SpreadSheet error");
    }
    catch (RemoteException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
      displayErrorMessage("Cannot alter display dimension", exc,
        "VisAD SpreadSheet error");
    }
    refreshDisplayMenuItems();
  }

  /**
   * Creates a hardcopy of the current spreadsheet cell.
   */
  public void printCurrentCell() {
    if (!DisplayCells[CurX][CurY].hasData()) {
      displayErrorMessage("The current cell contains no data to be printed",
        null, "VisAD SpreadSheet error");
      return;
    }
    final PrinterJob printJob = PrinterJob.getPrinterJob();
    DisplayImpl display = DisplayCells[CurX][CurY].getDisplay();
    Printable p = display.getPrintable();
    printJob.setPrintable(p);
    if (printJob.printDialog()) {
      Runnable printImage = new Runnable() {
        public void run() {
          try {
            printJob.print();
          }
          catch (Exception exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
            displayErrorMessage("Cannot print the current cell", exc,
              "VisAD SpreadSheet error");
          }
        }
      };
      Thread t = new Thread(printImage);
      t.start();

    }
  }

  /**
   * Specifies mappings from Data to Display.
   */
  public void createMappings() {
    DisplayCells[CurX][CurY].addMapDialog();
    refreshMenuCommands();
  }

  private static double[] matrix3D =
    {0.5, 0, 0, 0, 0, 0.5, 0, 0, 0, 0, 0.5, 0, 0, 0, 0, 1};

  private static double[] matrix2D =
    {0.65, 0, 0, 0, 0, 0.65, 0, 0, 0, 0, 0.65, 0, 0, 0, 0, 1};

  private static double[] matrixJ2D =
    {1, 0, 0, -1, 0, 0};


  /**
   * Resets the display projection to its original value.
   */
  public void resetOrientation() {
    DisplayImpl display = DisplayCells[CurX][CurY].getDisplay();
    if (display != null) {
      ProjectionControl pc = display.getProjectionControl();
      if (pc != null) {
        int dim = DisplayCells[CurX][CurY].getDimension();
        double[] matrix;
        if (dim == 1) {
          // 3-D (Java3D)
          matrix = matrix3D;
        }
        else if (dim == 2) {
          // 2-D (Java2D)
          matrix = matrixJ2D;
        }
        else {
          // 2-D (Java3D)
          matrix = matrix2D;
        }
        try {
          pc.setMatrix(matrix);
        }
        catch (VisADException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage("Cannot reset orientation", exc,
            "VisAD SpreadSheet error");
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage("Cannot reset orientation", exc,
            "VisAD SpreadSheet error");
        }
      }
    }
  }

  /**
   * Displays the controls for the currently selected cell.
   */
  public void showControls() {
    DisplayCells[CurX][CurY].showWidgetFrame();
  }


  // --- DISPLAY MENU ---

  /**
   * Adds a column to the spreadsheet.
   */
  public synchronized void addColumn() {
    JLabel l = (JLabel) HorizLabel[NumVisX - 1].getComponent(0);
    int maxVisX = Letters.indexOf(l.getText()) + 1;
    int diffX = Letters.length() - maxVisX;
    if (diffX > 0) {
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
            FancySSCell f = createCell(name, null);
            f.addSSCellListener(this);
            f.addMouseListener(this);
            f.setAutoSwitch(AutoSwitch);
            f.setAutoDetect(AutoDetect);
            f.setAutoShowControls(AutoShowControls);
            f.setDimension(CanDo3D ?
              BasicSSCell.JAVA3D_3D : BasicSSCell.JAVA2D_2D);
            f.addDisplayListener(this);
            f.setPreferredSize(
              new Dimension(DEFAULT_VIS_WIDTH, DEFAULT_VIS_HEIGHT));
            fcells[NumVisX][j] = f;
            if (rsi != null) {
              // add new cell to server
              fcells[NumVisX][j].addToRemoteServer(rsi);
            }
          }
          catch (VisADException exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
            displayErrorMessage("Cannot add the column. Unable to create " +
              "new displays", exc, "VisAD SpreadSheet error");
          }
          catch (RemoteException exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
            displayErrorMessage("Cannot add the column. A remote error " +
              "occurred", exc, "VisAD SpreadSheet error");
          }
        }

        NumVisX++;
        reconstructHoriz(newLabels, newDrag, fcells);
        if (diffX == 1) LayAddCol.setEnabled(false);
        LayDelCol.setEnabled(true);
      }
    }
  }

  /**
   * Adds a row to the spreadsheet.
   */
  public void addRow() {
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
          FancySSCell f = createCell(name, null);
          f.addSSCellListener(this);
          f.addMouseListener(this);
          f.setAutoSwitch(AutoSwitch);
          f.setAutoDetect(AutoDetect);
          f.setAutoShowControls(AutoShowControls);
          f.setDimension(CanDo3D ?
            BasicSSCell.JAVA3D_3D : BasicSSCell.JAVA2D_2D);
          f.addDisplayListener(this);
          f.setPreferredSize(
            new Dimension(DEFAULT_VIS_WIDTH, DEFAULT_VIS_HEIGHT));
          fcells[i][NumVisY] = f;
          if (rsi != null) {
            // add new cell to server
            fcells[i][NumVisY].addToRemoteServer(rsi);
          }
        }
        catch (VisADException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage("Cannot add the row. Unable to create new " +
            "displays", exc, "VisAD SpreadSheet error");
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage("Cannot add the row. A remote error occurred",
            exc, "VisAD SpreadSheet error");
        }
      }

      NumVisY++;
      reconstructVert(newLabels, newDrag, fcells);
      LayDelRow.setEnabled(true);
    }
  }

  /**
   * Deletes a column from the spreadsheet.
   */
  public synchronized boolean deleteColumn() {
    // make sure at least one column will be left
    if (NumVisX == 1) {
      displayErrorMessage("This is the last column", null,
        "Cannot delete column");
      return false;
    }
    // make sure no cells are dependent on columns about to be deleted
    for (int j=0; j<NumVisY; j++) {
      if (DisplayCells[CurX][j].othersDepend()) {
        displayErrorMessage("Other cells depend on cells from this " +
          "column. Make sure that no cells depend on this column before " +
          "attempting to delete it", null, "Cannot delete column");
        return false;
      }
    }

    // get column letter to be deleted
    JLabel label = (JLabel) HorizLabel[CurX].getComponent(0);
    char letter = label.getText().charAt(0);
    char last = Letters.charAt(Letters.length() - 1);

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
          displayErrorMessage("Cannot destroy cell (3)", exc,
            "VisAD SpreadSheet error");
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage("Cannot destroy cell (4)", exc,
            "VisAD SpreadSheet error");
        }
        DisplayCells[CurX][j] = null;
      }
      NumVisX--;
      if (CurX > NumVisX - 1) selectCell(NumVisX - 1, CurY);
      reconstructHoriz(newLabels, newDrag, fcells);
      if (letter == last) LayAddCol.setEnabled(true);
      if (NumVisX == 1) LayDelCol.setEnabled(false);
    }
    return true;
  }

  /**
   * Deletes a row from the spreadsheet.
   */
  public synchronized boolean deleteRow() {
    // make sure at least one row will be left
    if (NumVisY == 1) {
      displayErrorMessage("This is the last row", null, "Cannot delete row");
      return false;
    }

    // make sure no cells are dependent on rows about to be deleted
    for (int i=0; i<NumVisX; i++) {
      if (DisplayCells[i][CurY].othersDepend()) {
        displayErrorMessage("Other cells depend on cells from this row. " +
          "Make sure that no cells depend on this row before attempting " +
          "to delete it", null, "Cannot delete row");
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
          displayErrorMessage("Cannot destroy cell (5)", exc,
            "VisAD SpreadSheet error");
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
          displayErrorMessage("Cannot destroy cell (6)", exc,
            "VisAD SpreadSheet error");
        }
        DisplayCells[i][CurY] = null;
      }
      NumVisY--;
      if (CurY > NumVisY - 1) selectCell(CurX, NumVisY - 1);
      reconstructVert(newLabels, newDrag, fcells);
      if (NumVisY == 1) LayDelRow.setEnabled(false);
    }
    return true;
  }

  /**
   * Resizes all cells to exactly fill the entire pane, if possible.
   */
  public void tileCells() {
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


  // --- OPTIONS MENU ---

  /**
   * Sets auto-dimension switching to match Auto-switch menu item state.
   */
  public void optionsSwitch() {
    setAutoSwitch(AutoSwitchBox.getState());
  }

  /**
   * Sets mapping auto-detection to match Auto-detect menu item state.
   */
  public void optionsDetect() {
    setAutoDetect(AutoDetectBox.getState());
  }

  /**
   * Sets auto-display of controls to match Auto-display menu item state.
   */
  public void optionsDisplay() {
    setAutoShowControls(AutoShowBox.getState());
  }

  /**
   * Toggles auto-dimension switching.
   */
  protected void setAutoSwitch(boolean b) {
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) DisplayCells[i][j].setAutoSwitch(b);
    }
    AutoSwitch = b;
  }

  /**
   * Toggles mapping auto-detection.
   */
  protected void setAutoDetect(boolean b) {
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) DisplayCells[i][j].setAutoDetect(b);
    }
    AutoDetect = b;
  }

  /**
   * Toggles auto-display of controls.
   */
  protected void setAutoShowControls(boolean b) {
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) DisplayCells[i][j].setAutoShowControls(b);
    }
    AutoShowControls = b;
  }


  // --- FORMULA BAR BUTTONS ---

  /**
   * Prompts the user to type a source for a new data object
   * for the current cell.
   */
  public void formulaAdd() {
    FormulaEditor.setItem("");
    FormulaEditor.selectAll();
  }

  /**
   * Deletes the selected data object from the current cell.
   */
  public void formulaDel() {
    String item = (String) FormulaBox.getSelectedItem();
    if (item == null) return;
    int index = item.indexOf(":");
    if (index < 0) return;
    String varName = item.substring(0, index);
    try {
      DisplayCells[CurX][CurY].removeData(varName);
      FormulaBox.removeItem(item);
    }
    catch (VisADException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
      displayErrorMessage("Cannot delete data " + varName, exc,
        "VisAD SpreadSheet error");
    }
    catch (RemoteException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
      displayErrorMessage("Cannot delete data " + varName, exc,
        "VisAD SpreadSheet error");
    }
  }


  // --- GUI MANAGEMENT ---

  /**
   * Refreshes spreadsheet cells.
   */
  protected void refreshCells() {
    Util.invoke(false, BasicSSCell.DEBUG, new Runnable() {
      public void run() {
        for (int i=0; i<NumVisX; i++) {
          for (int j=0; j<NumVisY; j++) DisplayCells[i][j].refresh();
        }
      }
    });
  }

  /**
   * Refreshes check box items in the Options menu.
   */
  protected void refreshOptions() {
    Util.invoke(false, BasicSSCell.DEBUG, new Runnable() {
      public void run() {
        AutoSwitchBox.setState(AutoSwitch);
        AutoDetectBox.setState(AutoDetect);
        AutoShowBox.setState(AutoShowControls);
      }
    });
  }

  /**
   * Refreshes the "Show controls" menu option and toolbar button.
   */
  protected void refreshShowControls() {
    Util.invoke(false, BasicSSCell.DEBUG, new Runnable() {
      public void run() {
        boolean b = DisplayCells[CurX][CurY].hasControls();
        CellShow.setEnabled(b);
        if (!BugFix) ToolShow.setEnabled(b);
      }
    });
  }

  /**
   * Enables or disables certain menu items
   * depending on whether this cell has data.
   */
  protected void refreshMenuCommands() {
    Util.invoke(false, BasicSSCell.DEBUG, new Runnable() {
      public void run() {
        boolean b = DisplayCells[CurX][CurY].hasData();
        FileExport.setEnabled(b);
        FileSnap.setEnabled(b && CanDoJPEG);
        EditClear.setEnabled(b);
        CellPrint.setEnabled(b);
        CellEdit.setEnabled(b);
        CellReset.setEnabled(b && !IsSlave);
        if (!BugFix) {
          ToolSave.setEnabled(b);
          ToolMap.setEnabled(b);
          ToolReset.setEnabled(b && !IsSlave);
        }
        refreshShowControls();
      }
    });
  }

  /**
   * Makes sure the formula bar is displaying up-to-date info.
   */
  protected void refreshFormulaBar() {
    Util.invoke(false, BasicSSCell.DEBUG, new Runnable() {
      public void run() {
        if (FormulaBox.getItemCount() > 0) FormulaBox.removeAllItems();
        String[] varNames = DisplayCells[CurX][CurY].getVariableNames();
        int len = varNames.length;
        for (int i=0; i<len; i++) {
          String varName = varNames[i];
          String source = DisplayCells[CurX][CurY].getDataSource(varName);
          FormulaBox.addItem(varName + ": " + source);
        }
        boolean b = len > 0;
        CellDel.setEnabled(b);
        if (!BugFix) FormulaDel.setEnabled(b);
      }
    });
  }

  /**
   * Updates dimension checkbox menu items and toolbar buttons.
   */
  protected void refreshDisplayMenuItems() {
    Util.invoke(false, BasicSSCell.DEBUG, new Runnable() {
      public void run() {
        // update dimension check marks
        int dim = DisplayCells[CurX][CurY].getDimension();
        boolean j3d3d = (dim == BasicSSCell.JAVA3D_3D);
        boolean j2d2d = (dim == BasicSSCell.JAVA2D_2D);
        boolean j3d2d = (dim == BasicSSCell.JAVA3D_2D);
        CellDim3D3D.setState(j3d3d);
        CellDim2D2D.setState(j2d2d);
        CellDim2D3D.setState(j3d2d);
        if (!BugFix) {
          Tool3D.setEnabled(!j3d3d && CanDo3D);
          Tool2D.setEnabled(!j3d2d && CanDo3D);
          ToolJ2D.setEnabled(!j2d2d);
        }
      }
    });
  }


  // --- COLLABORATION ---

  /**
   * Determines whether or not the last remote event was from the server.
   */
  private double getColRowID() {
    TupleIface t = null;
    Real id = null;
    try {
      t = (TupleIface) RemoteColRow.getData();
      id = (Real) t.getComponent(0);
      return id.getValue();
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
    return Float.NaN;
  }

  /**
   * Gets the latest remote row and column information.
   */
  private String[][] getNewCellNames() {
    // extract new row and column information
    TupleIface t = null;
    TupleIface tc = null;
    RealTuple tr = null;
    try {
      t = (TupleIface) RemoteColRow.getData();
      tc = (TupleIface) t.getComponent(1);
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
    int collen = -1;
    int rowlen = -1;
    try {
      collen = tc.getDimension();
      rowlen = tr.getDimension();
    }
    catch (RemoteException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
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

  /**
   * Updates the remote row and column information.
   */
  private void synchColRow() {
    if (RemoteColRow != null) {
      synchronized (RemoteColRow) {
        int xlen = HorizLabel.length;
        int ylen = VertLabel.length;
        try {
          MathType[] m = new MathType[3];

          Real id = new Real(CollabID);
          m[0] = id.getType();
          Text[] txt = new Text[xlen];
          TextType[] tt = new TextType[xlen];
          for (int i=0; i<xlen; i++) {
            String s = ((JLabel) HorizLabel[i].getComponent(0)).getText();
            txt[i] = new Text(s);
            tt[i] = (TextType) txt[i].getType();
          }
          m[1] = new TupleType(tt);
          TupleIface tc = new Tuple((TupleType) m[1], txt);

          Real[] r = new Real[ylen];
          for (int j=0; j<ylen; j++) {
            String s = ((JLabel) VertLabel[j].getComponent(0)).getText();
            r[j] = new Real(Integer.parseInt(s));
          }
          RealTuple tr = new RealTuple(r);
          m[2] = tr.getType();

          TupleIface t = new Tuple(new TupleType(m), new Data[] {id, tc, tr});
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


  // --- CELL & LABEL CONSTRUCTION ---

  /**
   * Ensures that the cells' preferred sizes match those of the labels.
   */
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
        labels[i][j] = "" + Letters.charAt(i) + (j + 1);
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
              f = createCell(l[i][j], rs);
              f.addSSCellListener(this);
              f.addMouseListener(this);
              f.setAutoSwitch(AutoSwitch);
              f.setAutoDetect(AutoDetect);
              f.setAutoShowControls(AutoShowControls);
              if (rs == null) f.setDimension(CanDo3D ?
                BasicSSCell.JAVA3D_3D : BasicSSCell.JAVA2D_2D);
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
            if (BasicSSCell.DEBUG) exc.printStackTrace();
            displayErrorMessage("Cannot construct spreadsheet cells. " +
              "An error occurred", exc, "VisAD SpreadSheet error");
          }
          catch (RemoteException exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
            displayErrorMessage("Cannot construct spreadsheet cells. " +
              "A remote error occurred", exc, "VisAD SpreadSheet error");
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

  protected void reconstructSpreadsheet(String[][] cellNames, int[] w, int[] h,
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
            displayErrorMessage("Cannot destroy cell (7)", exc,
              "VisAD SpreadSheet error");
          }
          catch (RemoteException exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
            displayErrorMessage("Cannot destroy cell (8)", exc,
              "VisAD SpreadSheet error");
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


  // --- EVENT HANDLING ---

  /**
   * Handles checkbox menu item changes (dimension checkboxes).
   */
  public void itemStateChanged(ItemEvent e) {
    String item = (String) e.getItem();
    if (item.equals("3-D (Java3D)")) setDim(BasicSSCell.JAVA3D_3D);
    else if (item.equals("2-D (Java2D)")) setDim(BasicSSCell.JAVA2D_2D);
    else if (item.equals("2-D (Java3D)")) setDim(BasicSSCell.JAVA3D_2D);
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

  /**
   * Handles scrollbar changes.
   */
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

  /**
   * Handles display changes.
   */
  public void displayChanged(DisplayEvent e) {
    if (e.getId() == DisplayEvent.MOUSE_PRESSED && !e.isRemote()) {
      // highlight cell if it is the source of a local mouse click
      String name = null;
      try {
        Display d = e.getDisplay();
        name = d.getName();
        if (name.endsWith(".remote")) {
          // cloned cells need ".remote" stripped from their names
          name = name.substring(0, name.length() - 7);
        }
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

  /**
   * BIG HAMMER HACK.
   */
  private boolean commandKey;

  /**
   * BIG HAMMER HACK.
   */
  private boolean shiftHeld;

  /**
   * BIG HAMMER HACK.
   */
  public void keyPressed(KeyEvent e) {
    commandKey = true;
    int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_ENTER) {
      // enter pressed; notify action listener
      FormulaListener.actionPerformed(new ActionEvent(FormulaEditor, 0, ""));
    }
    else if (keyCode == KeyEvent.VK_BACK_SPACE) {
      // backspace pressed; delete text
      String text = FormulaText.getText();
      int start = FormulaText.getSelectionStart();
      int end = FormulaText.getSelectionEnd();
      if (start != end || start != 0) {
        int pos = start == end ? start - 1 : start;
        String pre = text.substring(0, pos);
        String post = text.substring(end);
        FormulaText.setText(pre + post);
        FormulaText.getCaret().setDot(pos);
      }
    }
    else if (keyCode == KeyEvent.VK_SHIFT) {
      // shift pressed
      shiftHeld = true;
    }
    else if (keyCode == KeyEvent.VK_LEFT) {
      int pos = FormulaText.getCaretPosition();
      if (shiftHeld) {
        // shift + left arrow pressed; alter selection left
        if (pos > 0) FormulaText.getCaret().moveDot(pos - 1);
      }
      else {
        // left arrow pressed; move caret left
        if (pos > 0) FormulaText.getCaret().setDot(pos - 1);
      }
    }
    else if (keyCode == KeyEvent.VK_RIGHT) {
      int pos = FormulaText.getCaretPosition();
      if (shiftHeld) {
        // shift + right arrow pressed; alter selection right
        if (pos < FormulaText.getText().length()) {
          FormulaText.getCaret().moveDot(pos + 1);
        }
      }
      else {
        // right arrow pressed; move caret right
        if (pos < FormulaText.getText().length()) {
          FormulaText.getCaret().setDot(pos + 1);
        }
      }
    }
    else if (keyCode == KeyEvent.VK_HOME) {
      if (shiftHeld) {
        // shift + home pressed; select to beginning of text
        FormulaText.getCaret().moveDot(0);
      }
      else {
        // home pressed; move to beginning of text
        FormulaText.getCaret().setDot(0);
      }
    }
    else if (keyCode == KeyEvent.VK_END) {
      if (shiftHeld) {
        // shift + end pressed; select to end of text
        FormulaText.getCaret().moveDot(FormulaText.getText().length());
      }
      else {
        // end pressed; move to end of text
        FormulaText.getCaret().setDot(FormulaText.getText().length());
      }
    }
    else commandKey = false;
  }

  /**
   * BIG HAMMER HACK.
   */
  public void keyReleased(KeyEvent e) {
    int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_SHIFT) shiftHeld = false;
  }

  /**
   * BIG HAMMER HACK.
   */
  public void keyTyped(KeyEvent e) {
    char key = e.getKeyChar();
    if (!commandKey && !e.isActionKey() && key >= 32) {
      int start = FormulaText.getSelectionStart();
      int end = FormulaText.getSelectionEnd();
      String text = FormulaText.getText();
      FormulaText.setText(text.substring(0, start) +
        key + text.substring(end));
      FormulaText.getCaret().setDot(start + 1);
    }
    e.consume();
  }

  /**
   * Old x value used with cell resizing logic.
   */
  private int oldX;

  /**
   * Old y value used with cell resizing logic.
   */
  private int oldY;

  /**
   * Handles mouse presses.
   */
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

  /**
   * Handles cell resizing.
   */
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

  /**
   * Handles cell label resizing.
   */
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

  /**
   * Unused MouseListener method.
   */
  public void mouseClicked(MouseEvent e) { }

  /**
   * Unused MouseListener method.
   */
  public void mouseEntered(MouseEvent e) { }

  /**
   * Unused MouseListener method.
   */
  public void mouseExited(MouseEvent e) { }

  /**
   * Unused MouseMotionListener method.
   */
  public void mouseMoved(MouseEvent e) { }

  /**
   * Handles changes in a cell's data.
   */
  public void ssCellChanged(SSCellChangeEvent e) {
    FancySSCell f = (FancySSCell) e.getSSCell();
    if (CurX < NumVisX && CurY < NumVisY && DisplayCells[CurX][CurY] == f) {
      int ct = e.getChangeType();
      if (ct == SSCellChangeEvent.DATA_CHANGE) {
        refreshFormulaBar();
        refreshMenuCommands();
      }
      else if (ct == SSCellChangeEvent.DISPLAY_CHANGE) {
        refreshShowControls();
        if (IsSlave) {
          // slaves cannot send DATA_CHANGE notification
          refreshFormulaBar();
          refreshMenuCommands();
        }
      }
      else if (ct == SSCellChangeEvent.DIMENSION_CHANGE) {
        refreshDisplayMenuItems();
      }
    }
  }


  // --- SPREADSHEET API ---

  /**
   * Sets the SpreadSheet cell class to the given class (which must extend
   * FancySSCell), used for creating SpreadSheet cells.
   */
  public static void setSSCellClass(Class c) {
    try {
      cellConstructor = c.getConstructor(cellArgs);
    }
    catch (NoSuchMethodException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
  }

  /**
   * Selects the specified cell and updates screen info.
   */
  public void selectCell(int x, int y) {
    if (x < 0) x = 0;
    if (y < 0) y = 0;
    if (x >= NumVisX) x = NumVisX - 1;
    if (y >= NumVisY) y = NumVisY - 1;

    // update borders of all cells
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) {
        boolean selected = x == i && y == j;
        DisplayCells[i][j].setSelected(selected);
        DisplayCells[i][j].setAutoShowControls(selected && AutoShowControls);
      }
    }

    // update spreadsheet info
    CurX = x;
    CurY = y;
    FormulaText.getCaret().setVisible(true); // BIG HAMMER HACK
    refreshFormulaBar();
    refreshMenuCommands();
    refreshDisplayMenuItems();
  }

  /**
   * Pops up an option selection dialog for choosing SpreadSheet options.
   */
  protected boolean getOptions(int cols, int rows,
    String server, String clone, boolean slave)
  {
    // Note: When the "Ok" button of this option dialog is pressed, the values
    // of SpreadSheet fields are altered directly. After calling this method,
    // another method like constructSpreadsheet() should be called to implement
    // the user-chosen settings.

    // set up the initial settings
    final SSOptions options = new SSOptions(cols, rows, CanDo3D,
      BugFix, BasicSSCell.DEBUG, server, clone, slave);

    // set up main content pane
    final boolean[] success = new boolean[1];
    success[0] = false;
    final JDialog dialog =
      new JDialog((JFrame) null, "VisAD SpreadSheet Options", true);
    JPanel pane = new JPanel();
    dialog.setContentPane(pane);
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

    // set up first row of options
    JPanel row1 = new JPanel();
    row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
    JPanel row1Cols = new JPanel();
    row1Cols.setLayout(new BoxLayout(row1Cols, BoxLayout.Y_AXIS));
    row1Cols.add(new JLabel("Columns"));
    final JTextField colField = new JTextField("" + cols);
    Util.adjustTextField(colField);
    colField.setEnabled(clone == null);
    row1Cols.add(colField);
    row1.add(row1Cols);
    JPanel row1Xs = new JPanel();
    row1Xs.setLayout(new BoxLayout(row1Xs, BoxLayout.Y_AXIS));
    row1Xs.add(new JLabel(" x "));
    row1Xs.add(new JLabel(" x "));
    row1.add(row1Xs);
    JPanel row1Rows = new JPanel();
    row1Rows.setLayout(new BoxLayout(row1Rows, BoxLayout.Y_AXIS));
    row1Rows.add(new JLabel("Rows"));
    final JTextField rowField = new JTextField("" + rows);
    Util.adjustTextField(rowField);
    rowField.setEnabled(clone == null);
    row1Rows.add(rowField);
    row1.add(row1Rows);
    JPanel row1Boxes = new JPanel();
    row1Boxes.setLayout(new BoxLayout(row1Boxes, BoxLayout.Y_AXIS));
    final JCheckBox java3d = new JCheckBox("Enable Java3D", CanDo3D);
    java3d.setEnabled(Possible3D);
    row1Boxes.add(java3d);
    final JCheckBox toolBox = new JCheckBox("Enable toolbar", !BugFix);
    row1Boxes.add(toolBox);
    final JCheckBox debugBox = new JCheckBox("Debug mode", BasicSSCell.DEBUG);
    row1Boxes.add(debugBox);
    row1.add(row1Boxes);
    pane.add(row1);

    // set up second row of options
    JPanel row2 = new JPanel();
    row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
    ButtonGroup collabGroup = new ButtonGroup();
    final JRadioButton serverChoice = new JRadioButton("Server");
    serverChoice.setSelected(server != null && clone == null);
    collabGroup.add(serverChoice);
    row2.add(serverChoice);
    final JRadioButton cloneChoice = new JRadioButton("Clone");
    cloneChoice.setSelected(clone != null && !slave);
    collabGroup.add(cloneChoice);
    row2.add(cloneChoice);
    final JRadioButton slaveChoice = new JRadioButton("Slave");
    slaveChoice.setSelected(slave);
    collabGroup.add(slaveChoice);
    row2.add(slaveChoice);
    final JRadioButton aloneChoice = new JRadioButton("Stand-alone");
    aloneChoice.setSelected(server == null && clone == null);
    collabGroup.add(aloneChoice);
    row2.add(aloneChoice);
    pane.add(row2);

    // set up third row of options
    JPanel row3 = new JPanel();
    row3.setLayout(new BoxLayout(row3, BoxLayout.X_AXIS));
    row3.add(new JLabel("Server name "));
    final JTextField name = new JTextField(server == null ? "" : server);
    Util.adjustTextField(name);
    name.setEnabled(server != null);
    row3.add(name);
    pane.add(row3);

    // set up fourth row of options
    JPanel row4 = new JPanel();
    row4.setLayout(new BoxLayout(row4, BoxLayout.X_AXIS));
    row4.add(new JLabel("Server address "));
    final JTextField host = new JTextField(clone == null ? "" : clone);
    Util.adjustTextField(host);
    host.setEnabled(clone != null);
    row4.add(host);
    pane.add(row4);

    // set up fifth row of options
    JPanel row5 = new JPanel();
    row5.setLayout(new BoxLayout(row5, BoxLayout.X_AXIS));
    boolean first = true;
    String extras = "Extras: ";
    if (Possible3D) {
      extras = extras + (first ? "" : ", ") + "Java3D";
      first = false;
    }
    if (CanDoHDF5) {
      extras = extras + (first ? "" : ", ") + "HDF-5";
      first = false;
    }
    if (CanDoJPEG) {
      extras = extras + (first ? "" : ", ") + "JPEG snapshot";
      first = false;
    }
    if (CanDoPython) {
      extras = extras + (first ? "" : ", ") + "JPython scripting";
      first = false;
    }
    if (first) extras = extras + "None";
    extras = extras + ".";
    row5.add(new JLabel(extras));
    pane.add(row5);

    // set up button row
    JPanel buttons = new JPanel();
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    final JButton ok = new JButton("Ok");
    dialog.getRootPane().setDefaultButton(ok);
    buttons.add(ok);
    final JButton cancel = new JButton("Cancel");
    buttons.add(cancel);
    final JButton quit = new JButton("Quit");
    buttons.add(quit);
    pane.add(buttons);

    // handle important events
    ActionListener handler = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if (o == serverChoice) {
          colField.setEnabled(true);
          rowField.setEnabled(true);
          name.setEnabled(true);
          host.setEnabled(false);
        }
        else if (o == cloneChoice || o == slaveChoice) {
          colField.setEnabled(false);
          rowField.setEnabled(false);
          name.setEnabled(true);
          host.setEnabled(true);
        }
        else if (o == aloneChoice) {
          colField.setEnabled(true);
          rowField.setEnabled(true);
          name.setEnabled(false);
          host.setEnabled(false);
        }
        else if (o == ok) {
          // determine server type
          boolean serv = serverChoice.isSelected();
          boolean clon = cloneChoice.isSelected();
          boolean slav = slaveChoice.isSelected();
          boolean alon = aloneChoice.isSelected();

          if (clon || slav) {
            // column and row values are irrelevant for client sheets
            options.cols = 2;
            options.rows = 2;
          }
          else {
            // get number of columns
            options.cols = 0;
            try {
              options.cols = Integer.parseInt(colField.getText());
            }
            catch (NumberFormatException exc) { }
            if (options.cols <= 0) {
              displayErrorMessage(dialog, "The columns field must contain a " +
                "number greater than zero", null, "Invalid value");
              return;
            }

            // get number of rows
            options.rows = 0;
            try {
              options.rows = Integer.parseInt(rowField.getText());
            }
            catch (NumberFormatException exc) { }
            if (options.rows <= 0) {
              displayErrorMessage(dialog, "The rows field must contain a " +
                "number greater than zero", null, "Invalid value");
              return;
            }
          }

          // get Java3D toggle value
          options.enable3d = java3d.isSelected();

          // get toolbar toggle value
          options.bugfix = !toolBox.isSelected();

          // get debug toggle value
          options.debug = debugBox.isSelected();

          // get server name
          options.name = alon ? null : name.getText();

          // get server address
          options.address = clon || slav ? host.getText() : null;

          // get slave toggle value
          options.slave = slav;

          // everything ok, assign variables
          NumVisX = options.cols;
          NumVisY = options.rows;
          CanDo3D = options.enable3d;
          BasicSSCell.DEBUG = options.debug;
          BugFix = options.bugfix;
          serverName = options.name;
          cloneAddress = options.address;
          IsSlave = options.slave;

          // return successfully
          success[0] = true;
          dialog.setVisible(false);
        }
        else if (o == cancel) {
          success[0] = false;
          dialog.setVisible(false);
        }
        else if (o == quit) {
          // wow, just up and quit
          System.exit(0);
        }
      }
    };
    serverChoice.addActionListener(handler);
    cloneChoice.addActionListener(handler);
    slaveChoice.addActionListener(handler);
    aloneChoice.addActionListener(handler);
    ok.addActionListener(handler);
    cancel.addActionListener(handler);
    quit.addActionListener(handler);

    // display dialog
    dialog.pack();
    Util.centerWindow(dialog);
    dialog.setVisible(true);

    return success[0];
  }

  /**
   * Inner class for use with getOptions().
   */
  public class SSOptions {
    public int cols;
    public int rows;
    public boolean enable3d;
    public boolean bugfix;
    public boolean debug;
    public String name;
    public String address;
    public boolean slave;

    public SSOptions(int c, int r, boolean e, boolean b,
      boolean d, String n, String a, boolean s)
    {
      cols = c;
      rows = r;
      enable3d = e;
      bugfix = b;
      debug = d;
      name = n;
      address = a;
      slave = s;
    }
  }

  /**
   * Returns the JToolBar object for other programs to use (e.g., add buttons).
   */
  public JToolBar getToolbar() {
    return Toolbar;
  }

  /**
   * Returns a new instance of a spreadsheet cell (which must extend
   * FancySSCell), used when a spreadsheet row or column is added.
   */
  protected FancySSCell createCell(String name, RemoteServer rs)
    throws VisADException, RemoteException
  {
    Object[] args = {name, fm, rs, new Boolean(IsSlave), null, this};
    if (cellConstructor == null) setSSCellClass(FancySSCell.class);
    Object cell = null;
    try {
      cell = cellConstructor.newInstance(args);
    }
    catch (IllegalAccessException exc) {
      exc.printStackTrace();
    }
    catch (InstantiationException exc) {
      exc.printStackTrace();
    }
    catch (InvocationTargetException exc) {
      if (exc.getTargetException() instanceof java.rmi.StubNotFoundException) {
        System.err.println(
          "Your VisAD installation has not properly executed the RMIC\n" +
          "compiler on the appropriate source files. Please re-run\n" +
          "\"make compile\" in the VisAD directory. If you are using\n" +
          "Makefile.WinNT and running JDK 1.2, please double-check that\n" +
          "you have uncommented the RMIC-related environment variables,\n" +
          "or else the RMIC-related classes will be placed in the wrong\n" +
          "directories. A full stack dump follows:\n");
      }
      exc.getTargetException().printStackTrace();
    }
    if (!(cell instanceof FancySSCell)) {
      System.err.print("Cell constructor failed to " +
        "produce a FancySSCell, but instead produced: ");
      if (cell == null) System.err.println("null");
      else System.err.println(cell.getClass().getName());
      System.exit(3);
    }
    return (FancySSCell) cell;
  }

  /**
   * Displays an error in a message dialog.
   */
  protected void displayErrorMessage(String msg, Exception exc, String title) {
    displayErrorMessage(this, msg, exc, title);
  }

  /**
   * Displays an error in a message dialog.
   */
  protected void displayErrorMessage(Component parent, String msg,
    Exception exc, String title)
  {
    String s = (exc == null ? null : exc.getMessage());
    final Component c = parent;
    final String m = msg + (s == null ? "." : (": " + s));
    final String t = title;
    Util.invoke(false, BasicSSCell.DEBUG, new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog(c, m, t, JOptionPane.ERROR_MESSAGE);
      }
    });
  }

  /**
   * Adds a button to a toolbar.
   */
  protected JButton addToolbarButton(String file, String tooltip,
    String command, boolean enabled, JComponent parent)
  {
    URL url = SpreadSheet.class.getResource(file + ".gif");
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


  // --- DEPRECATED METHODS ---

  /**
   * @deprecated Use Util.adjustTextField(JTextField) instead.
   */
  public static void adjustTextField(JTextField field) {
    Util.adjustTextField(field);
  }

  /**
   * @deprecated Use Util.centerWindow(Window) instead.
   */
  public static void centerWindow(Window window) {
    Util.centerWindow(window);
  }

}
