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
  static final int HEIGHT_PERCENT = 75;

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

  /** Display Edit mappings menu item */
  MenuItem DispEdit;

  /** Options Show controls menu item */
  MenuItem OptWidget;

  /** Edit Paste toolbar button */
  JButton ToolPaste;

  /** Edit Save as netCDF toolbar button */
  JButton ToolSave;

  /** Display Edit mappings toolbar button */
  JButton ToolMap;

  /** Options Show controls toolbar button */
  JButton ToolShow;

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

  /** gateway into VisAD Visualization Spread Sheet user interface */
  public static void main(String[] argv) {
    String usage = "\n" +
      "Usage: java [-mx###m] visad.ss.SpreadSheet [cols rows] [-no3d]\n" +
      "       [-server server_name] [-client rmi_address] [-debug]\n\n" +
      "### = Maximum megabytes of memory to use\n" +
      "cols = Number of columns in this Spread Sheet\n" +
      "rows = Number of rows in this Spread Sheet\n" +
      "-no3d = Disable Java3D\n" +
      "-server server_name = Initialize this Spread Sheet as an RMI\n" +
      "                      server named server_name\n" +
      "-client rmi_address = Initialize this Spread Sheet as a clone\n" +
      "                      of the Spread Sheet at rmi_address\n" +
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
    options.addSeparator();

    OptWidget = new MenuItem("Show controls");
    OptWidget.addActionListener(this);
    OptWidget.setActionCommand("optWidget");
    OptWidget.setEnabled(false);
    options.add(OptWidget);

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
    ImageIcon toolShowControls = new ImageIcon(url);
    if (toolShowControls != null) {
      ToolShow = new JButton(toolShowControls);
      ToolShow.setAlignmentY(JButton.CENTER_ALIGNMENT);
      ToolShow.setToolTipText("Show controls");
      ToolShow.addActionListener(this);
      ToolShow.setActionCommand("optWidget");
      ToolShow.setEnabled(false);
      toolbar.add(ToolShow);
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
                "Spread Sheet in server mode.",
                "Failed to initialize RemoteServer");
              success = false;
            }
          }
          else {
            displayErrorMessage("Unable to export cells as RMI addresses. " +
              "Make sure you are running rmiregistry before launching the " +
              "Spread Sheet in server mode.",
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
          if (b == IsRemote) { // keep sheet from receiving its own updates
            String[][] cellNames = getNewCellNames();
            if (cellNames == null) return;
            int oldNVX = NumVisX;
            int oldNVY = NumVisY;
            NumVisX = cellNames.length;
            NumVisY = cellNames[0].length;
            if (NumVisX != oldNVX || NumVisY != oldNVY) {
              // reconstruct spreadsheet cells and labels
              reconstructSpreadsheet(cellNames, frs);
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
  }

  /** create a new spreadsheet file; return true if successful */
  boolean newFile(boolean safe) {
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
            catch (VisADException exc) {
              if (BasicSSCell.DEBUG) exc.printStackTrace();
            }
            catch (RemoteException exc) {
              if (BasicSSCell.DEBUG) exc.printStackTrace();
            }
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
    int dimX = -1, dimY = -1;
    int sizeX = -1, sizeY = -1;
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
        return;
      }
      len = line.length();
      if (line.trim().charAt(0) == '#') {
        // ignore comments
        eq = -1;
      }
      else eq = line.indexOf('[');
      if (eq >= 0) {
        boolean success = true;
        int end = line.indexOf(']', eq);
        if (end < 0) success = false;
        else {
          String sub = line.substring(eq + 1, end).trim();
          if (!sub.equalsIgnoreCase("global")) success = false;
        }
        if (!success) {
          displayErrorMessage("The file " + file + " does not contain the " +
            "[Global] tag as its first entry", "VisAD SpreadSheet error");
          return;
        }

        // parse global information
        int endToken = tokenNum;
        while (tokens[endToken] != null &&
          tokens[endToken].indexOf('[') < 0)
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
      return;
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
            return;
          }
          int lbrack;
          if (line.trim().charAt(0) == '#') {
            // ignore comments
            lbrack = -1;
          }
          else lbrack = line.indexOf('[');
          if (lbrack >= 0) {
            int rbrack = line.indexOf(']', lbrack);
            if (rbrack >= 0) {
              // this line identifies a cell name
              cellNames[i][j] = line.substring(lbrack + 1, rbrack).trim();
            }
          }
        }
        while (cellNames[i][j] == null);

        // find last line of this cell's save string
        int last = tokenNum + 1;
        while (tokens[last] != null && tokens[last].indexOf('[') < 0) last++;

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
    reconstructSpreadsheet(cellNames, null);
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

    // hack to avoid race condition that sets mappings incorrectly
    try {
      Thread.sleep(200);
    }
    catch (InterruptedException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
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
      String header = "# VisAD Visualization Spread Sheet spreadsheet file\n";
      Calendar cal = Calendar.getInstance();
      int year = cal.get(Calendar.YEAR);
      int month = cal.get(Calendar.MONTH);
      int day = cal.get(Calendar.DAY_OF_MONTH);
      int hour = cal.get(Calendar.HOUR_OF_DAY);
      int min = cal.get(Calendar.MINUTE);
      int sec = cal.get(Calendar.SECOND);
      int milli = cal.get(Calendar.MILLISECOND);
      String sYear = "" + year;
      String sMonth = "" + (month + 1);
      if (month < 10) sMonth = "0" + sMonth;
      String sDay = "" + day;
      if (day < 10) sDay = "0" + sDay;
      String date = sYear + "/" + sMonth + "/" + sDay;
      String sHour = "" + hour;
      if (hour < 10) sHour = "0" + sHour;
      String sMin = "" + min;
      if (min < 10) sMin = "0" + sMin;
      String sSec = "" + sec;
      if (sec < 10) sSec = "0" + sSec;
      String sMilli = "" + milli;
      if (milli < 100) sMilli = "0" + sMilli;
      if (milli < 10) sMilli = "0" + sMilli;
      String time = sHour + ":" + sMin + ":" + sSec + "." + sMilli;
      header = header + "# File " + CurrentFile.getName() +
        " written at " + date + ", " + time + "\n";

      // compile global information
      String global = "[Global]\n" + 
        "dimension = " + NumVisX + " x " + NumVisY + "\n" +
        "sheet size = " + getWidth() + " x " + getHeight() + "\n" +
        "auto switch = " + AutoSwitch + "\n" +
        "auto detect = " + AutoDetect + "\n" +
        "auto show = " + AutoShowControls + "\n";

      // compile cell information
      String cellInfo = "";
      for (int j=0; j<NumVisY; j++) {
        for (int i=0; i<NumVisX; i++) {
          cellInfo = cellInfo + "[" + DisplayCells[i][j].getName() + "]\n" +
            DisplayCells[i][j].getSaveString() + "\n";
        }
      }

      // convert information to a character array
      char[] sc = (header + "\n" + global + "\n" + cellInfo).toCharArray();

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
          catch (InterruptedException exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
          }
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

  /** specify mappings from Data to Display */
  void createMappings() {
    DisplayCells[CurX][CurY].addMapDialog();
  }

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

  /** add a column to the spreadsheet */
  void addColumn() {
    JLabel l = (JLabel) HorizLabel[NumVisX - 1].getComponent(0);
    int maxVisX = Letters.indexOf(l.getText()) + 1;
    if (maxVisX < Letters.length()) {
      // re-layout horizontal spreadsheet labels
      int nvx = HorizLabel.length;
      JPanel[] newLabels = new JPanel[nvx+1];
      for (int i=0; i<nvx; i++) newLabels[i] = HorizLabel[i];
      newLabels[nvx] = new JPanel();
      newLabels[nvx].setBorder(new LineBorder(Color.black, 1));
      newLabels[nvx].setLayout(new BorderLayout());
      newLabels[nvx].setPreferredSize(
        new Dimension(DEFAULT_VIS_WIDTH, LABEL_HEIGHT));
      String s = String.valueOf(Letters.charAt(maxVisX));
      newLabels[nvx].add("Center", new JLabel(s, SwingConstants.CENTER));

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
  void addRow() {
    JLabel l = (JLabel) VertLabel[NumVisY - 1].getComponent(0);
    int maxVisY = Integer.parseInt(l.getText());

    // re-layout vertical spreadsheet labels
    int nvy = VertLabel.length;
    JPanel[] newLabels = new JPanel[nvy+1];
    JComponent[] newDrag = new JComponent[nvy+1];
    for (int i=0; i<nvy; i++) newLabels[i] = VertLabel[i];
    newLabels[nvy] = new JPanel();
    newLabels[nvy].setBorder(new LineBorder(Color.black, 1));
    newLabels[nvy].setLayout(new BorderLayout());
    newLabels[nvy].setPreferredSize(
      new Dimension(LABEL_WIDTH, DEFAULT_VIS_HEIGHT));
    String s = String.valueOf(maxVisY + 1);
    newLabels[nvy].add("Center", new JLabel(s, SwingConstants.CENTER));

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
  boolean deleteColumn() {
    int nvx = HorizLabel.length;

    // make sure at least one column will be left
    if (nvx == 1) {
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
    JPanel[] newLabels = new JPanel[nvx-1];
    for (int i=0; i<CurX; i++) newLabels[i] = HorizLabel[i];
    for (int i=CurX+1; i<nvx; i++) newLabels[i-1] = HorizLabel[i];

    if (IsRemote) {
      // let server handle the actual cell layout
      HorizLabel = newLabels;
      synchColRow();
    }
    else {
      // re-layout horizontal label separators
      JComponent[] newDrag = new JComponent[NumVisX-1];
      for (int i=0; i<NumVisX-1; i++) newDrag[i] = HorizDrag[i];
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
        catch (VisADException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        DisplayCells[CurX][j] = null;
      }
      NumVisX--;
      if (CurX > NumVisX-1) CurX = NumVisX - 1;
      reconstructHoriz(newLabels, newDrag, fcells);
    }
    return true;
  }

  /** delete a row from the spreadsheet */
  boolean deleteRow() {
    int nvy = VertLabel.length;

    // make sure at least one row will be left
    if (nvy == 1) {
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
    JPanel[] newLabels = new JPanel[nvy-1];
    for (int i=0; i<CurY; i++) newLabels[i] = VertLabel[i];
    for (int i=CurY+1; i<nvy; i++) newLabels[i-1] = VertLabel[i];

    if (IsRemote) {
      // let server handle the actual cell layout
      VertLabel = newLabels;
      synchColRow();
    }
    else {
      // re-layout horizontal label separators
      JComponent[] newDrag = new JComponent[NumVisY-1];
      for (int i=0; i<NumVisY-1; i++) newDrag[i] = VertDrag[i];
      VertPanel.removeAll();

      // re-layout spreadsheet cells
      FancySSCell[][] fcells = new FancySSCell[NumVisX][NumVisY - 1];
      DisplayPanel.removeAll();
      for (int i=0; i<NumVisX; i++) {
        for (int j=0; j<CurY; j++) fcells[i][j] = DisplayCells[i][j];
        for (int j=CurY+1; j<NumVisY; j++) fcells[i][j-1] = DisplayCells[i][j];
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
      if (CurY > NumVisY-1) CurY = NumVisY-1;
      reconstructVert(newLabels, newDrag, fcells);
    }
    return true;
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
        DispEdit.setEnabled(b);
        ToolMap.setEnabled(b);
        FileSave1.setEnabled(b);
        FileSave2.setEnabled(b);
        ToolSave.setEnabled(b);
        b = DisplayCells[CurX][CurY].hasControls();
        OptWidget.setEnabled(b);
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

  /** update dimension checkbox menu items in Cell menu */
  private void refreshDisplayMenuItems() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // update dimension check marks
        int dim = DisplayCells[CurX][CurY].getDimension();
        CellDim3D3D.setState(dim == BasicSSCell.JAVA3D_3D);
        CellDim2D2D.setState(dim == BasicSSCell.JAVA2D_2D);
        CellDim2D3D.setState(dim == BasicSSCell.JAVA3D_2D);
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
        for (int i=0; i<NumVisX; i++) {
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
              f.setPreferredSize(
                new Dimension(DEFAULT_VIS_WIDTH, DEFAULT_VIS_HEIGHT));
              if (rsi != null) {
                // add new cell to server
                f.addToRemoteServer(rsi);
              }
            }
            DisplayCells[i][j] = f;

            if (i == 0 && j == 0) selectCell(i, j);
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
  }

  private void constructHorizontalLabels() {
    String[] labels = new String[NumVisX];
    for (int i=0; i<NumVisX; i++) labels[i] = "" + Letters.charAt(i);
    constructHorizontalLabels(labels);
  }

  private void constructHorizontalLabels(String[] l) {
    synchronized (Lock) {
      HorizPanel.setLayout(new SSLayout(2*NumVisX, 1, 0, 0));
      HorizLabel = new JPanel[NumVisX];
      HorizDrag = new JComponent[NumVisX];
      for (int i=0; i<NumVisX; i++) {
        HorizLabel[i] = new JPanel();
        HorizLabel[i].setBorder(new LineBorder(Color.black, 1));
        HorizLabel[i].setLayout(new BorderLayout());
        HorizLabel[i].setPreferredSize(
          new Dimension(DEFAULT_VIS_WIDTH, LABEL_HEIGHT));
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
    String[] labels = new String[NumVisY];
    for (int i=0; i<NumVisY; i++) labels[i] = "" + (i+1);
    constructVerticalLabels(labels);
  }

  private void constructVerticalLabels(String[] l) {
    synchronized (Lock) {
      VertPanel.setLayout(new SSLayout(1, 2*NumVisY, 0, 0));
      VertLabel = new JPanel[NumVisY];
      VertDrag = new JComponent[NumVisY];
      for (int i=0; i<NumVisY; i++) {
        VertLabel[i] = new JPanel();
        VertLabel[i].setBorder(new LineBorder(Color.black, 1));
        VertLabel[i].setLayout(new BorderLayout());
        VertLabel[i].setPreferredSize(
          new Dimension(LABEL_WIDTH, DEFAULT_VIS_HEIGHT));
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

  private void reconstructLabels(String[][] cellNames) {
    // reconstruct horizontal labels
    String[] hLabels = new String[NumVisX];
    synchronized (Lock) {
      HorizPanel.removeAll();
      for (int i=0; i<NumVisX; i++) {
        hLabels[i] = "" + cellNames[i][0].charAt(0);
      }
    }
    constructHorizontalLabels(hLabels);

    // reconstruct vertical labels
    String[] vLabels = new String[NumVisY];
    synchronized (Lock) {
      VertPanel.removeAll();
      for (int j=0; j<NumVisY; j++) vLabels[j] = cellNames[0][j].substring(1);
    }
    constructVerticalLabels(vLabels);
  }

  private void reconstructSpreadsheet(String[][] cellNames,
                                      RemoteServer rs) {
    // reconstruct labels
    reconstructLabels(cellNames);

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
                                FancySSCell[][] fcells) {
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
      HorizPanel.doLayout();
      for (int i=0; i<NumVisX; i++) HorizLabel[i].doLayout();
      SCPane.doLayout();
      DisplayPanel.doLayout();
      refreshCells();
    }

    synchColRow();
  }

  private void reconstructVert(JPanel[] newLabels, JComponent[] newDrag,
                               FancySSCell[][] fcells) {
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
      VertPanel.doLayout();
      for (int j=0; j<NumVisY; j++) VertLabel[j].doLayout();
      SCPane.doLayout();
      DisplayPanel.doLayout();
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

  /** handle checkbox menu item changes (dimension checkboxes) */
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
      if (CurX != ci || CurY != cj) selectCell(ci, cj);
    }
  }

  /** handle key presses */
  public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();

    int ci = CurX;
    int cj = CurY;
    if (key == KeyEvent.VK_RIGHT && ci < NumVisX - 1) ci++;
    if (key == KeyEvent.VK_LEFT && ci > 0) ci--;
    if (key == KeyEvent.VK_UP && cj > 0) cj--;
    if (key == KeyEvent.VK_DOWN && cj < NumVisY - 1) cj++;
    if (ci != CurX || cj != CurY) selectCell(ci, cj);
  }

  /** unused KeyListener method */
  public void keyReleased(KeyEvent e) { }

  /** unused KeyListener method */
  public void keyTyped(KeyEvent e) { }

  // used with cell resizing logic
  private int oldX;
  private int oldY;

  /** handle mouse presses */
  public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    for (int j=0; j<NumVisY; j++) {
      for (int i=0; i<NumVisX; i++) {
        if (c == DisplayCells[i][j] && (CurX != i || CurY != j)) {
          selectCell(i, j);
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
      SCPane.doLayout();
      DisplayPanel.doLayout();
      refreshCells();
    }
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

    // update blue border on screen
    if (CurX < NumVisX && CurY < NumVisY) {
      FancySSCell f = DisplayCells[CurX][CurY];
      if (f != null) f.setSelected(false);
    }
    DisplayCells[x][y].setSelected(true);

    // update spreadsheet info
    CurX = x;
    CurY = y;
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

}

