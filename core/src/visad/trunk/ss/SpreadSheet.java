
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
  int NumVisX;
  int NumVisY;
  int NumVisDisplays;

  // whether this JVM supports Java3D (detected on SpreadSheet launch)
  boolean CanDo3D = true;

  // display-related arrays and variables
  Panel DisplayPanel;
  JPanel ScrollPanel;
  ScrollPane SCPane;
  JViewport HorizLabels, VertLabels;
  JPanel[] HorizLabel, VertLabel;
  JPanel[] HorizDrag, VertDrag;
  FancySSCell[] DisplayCells;
  JTextField FormulaField;
  MenuItem EditPaste;
  MenuItem FileSave1, FileSave2, DispEdit;
  JButton ToolPaste;
  JButton ToolSave, ToolMap;
  JButton FormulaOk;
  CheckboxMenuItem CellDim3D3D, CellDim2D2D, CellDim2D3D;
  int CurDisplay = 0;

  String Clipboard = null;
  File CurrentFile = null;

  /** main method; gateway into Spread Sheet user interface */
  public static void main(String[] argv) { 
    String usage = "\n" +
                   "Usage: java [-mx###m] visad.ss.SpreadSheet " +
                   "[cols rows] [-server server_name]\n\n" +
                   "### = Maximum megabytes of memory to use\n" +
                   "cols = Number of columns in this Spread Sheet\n" +
                   "rows = Number of rows in this Spread Sheet\n" +
                   "-server server_name = Initialize this Spread Sheet as " +
                                         "an RMI server named server_name\n";
    int cols = 2;
    int rows = 2;
    String servname = null;
    int len = argv.length;
    if (len > 0) {
      int ix = 0;

      // parse command line flags
      while (ix < len) {
        if (argv[ix].charAt(0) == '-') {
          if (ix < len-1 && argv[ix].equals("-server")) {
            servname = argv[++ix];
          }
          else {
            // unknown flag
            System.out.println("Unknown option: " + argv[ix]);
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
              rows = Integer.parseInt(argv[++ix]);
              if (rows < 1 || cols < 1 || cols > Letters.length()) {
                success = false;
              }
            }
            catch (NumberFormatException exc) {
              success = false;
            }
            if (!success) {
              System.out.println("Invalid number of columns and rows: " +
                                 argv[ix-1] + " x " + argv[ix]);
              System.out.println(usage);
              System.exit(2);
            }
          }
        }
        ix++;
      }
    }
    SpreadSheet ss = new SpreadSheet(WIDTH_PERCENT, HEIGHT_PERCENT,
                                     cols, rows, servname,
                                     "VisAD Spread Sheet");
  }

  /** constructor for the SpreadSheet class */
  public SpreadSheet(int sWidth, int sHeight, int cols, int rows,
                     String server, String sTitle) {
    bTitle = sTitle;
    NumVisX = cols;
    NumVisY = rows;
    NumVisDisplays = NumVisX*NumVisY;
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

    CheckboxMenuItem optSwitch = new CheckboxMenuItem(
                     "Auto-switch to 3-D", CanDo3D);
    optSwitch.addItemListener(this);
    optSwitch.setEnabled(CanDo3D);
    options.add(optSwitch);

    CheckboxMenuItem optAuto = new CheckboxMenuItem(
                     "Auto-detect mappings", true);
    optAuto.addItemListener(this);
    options.add(optAuto);

    CheckboxMenuItem optASC = new CheckboxMenuItem(
                     "Auto-display controls", true);
    optASC.addItemListener(this);
    options.add(optASC);

    CheckboxMenuItem optFormula = new CheckboxMenuItem(
                     "Show formula and RMI error messages", true);
    optFormula.addItemListener(this);
    options.add(optFormula);
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

    FormulaField.setToolTipText("Enter a file name, URL, RMI address, or formula");
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

    JPanel horizPanel = new JPanel() {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, LABEL_HEIGHT);
      }
    };
    horizPanel.setLayout(new SSLayout(2*NumVisX-1, 1, MIN_VIS_WIDTH,
                                      LABEL_HEIGHT, 0, 0, true));
    HorizLabel = new JPanel[NumVisX];
    HorizDrag = new JPanel[NumVisX-1];
    for (int i=0; i<NumVisX; i++) {
      String curLet = String.valueOf(Letters.charAt(i));
      HorizLabel[i] = new JPanel();
      HorizLabel[i].setBorder(new LineBorder(Color.black, 1));
      HorizLabel[i].setLayout(new BorderLayout());
      HorizLabel[i].setPreferredSize(new Dimension(MIN_VIS_WIDTH,
                                                   LABEL_HEIGHT));
      HorizLabel[i].add("Center", new JLabel(curLet, SwingConstants.CENTER));
      horizPanel.add(HorizLabel[i]);
      if (i < NumVisX-1) {
        HorizDrag[i] = new JPanel() {
          public void paint(Graphics g) {
            Dimension s = getSize();
            g.setColor(Color.black);
            g.drawRect(0, 0, s.width - 1, s.height - 1);
            g.setColor(Color.yellow);
            g.fillRect(1, 1, s.width - 2, s.height - 2);
          }
        };
        HorizDrag[i].setPreferredSize(new Dimension(5, 0));
        HorizDrag[i].addMouseListener(this);
        HorizDrag[i].addMouseMotionListener(this);
        horizPanel.add(HorizDrag[i]);
      }
    }
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
    HorizLabels.setView(horizPanel);
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

    JPanel vertPanel = new JPanel() {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(LABEL_WIDTH, d.height);
      }
    };
    vertPanel.setLayout(new SSLayout(1, 2*NumVisY-1, LABEL_WIDTH,
                                     MIN_VIS_HEIGHT, 0, 0, true));
    VertLabel = new JPanel[NumVisY];
    VertDrag = new JPanel[NumVisY-1];
    for (int i=0; i<NumVisY; i++) {
      VertLabel[i] = new JPanel();
      VertLabel[i].setBorder(new LineBorder(Color.black, 1));
      VertLabel[i].setLayout(new BorderLayout());
      VertLabel[i].setPreferredSize(new Dimension(LABEL_WIDTH,
                                                  MIN_VIS_HEIGHT));
      VertLabel[i].add("Center", new JLabel(""+(i+1), SwingConstants.CENTER));
      vertPanel.add(VertLabel[i]);
      if (i < NumVisY-1) {
        VertDrag[i] = new JPanel() {
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
        vertPanel.add(VertDrag[i]);
      }
    }
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
    VertLabels.setView(vertPanel);
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
    DisplayPanel.setLayout(new SSLayout(NumVisX, NumVisY, MIN_VIS_WIDTH,
                                        MIN_VIS_HEIGHT, 5, 5, false));
    SCPane.add(DisplayPanel);

    // set up display panel's individual VisAD displays
    DisplayCells = new FancySSCell[NumVisDisplays];

    for (int i=0; i<NumVisDisplays; i++) {
      String name = String.valueOf(Letters.charAt(i % NumVisX)) +
                    String.valueOf(i / NumVisX + 1);
      try {
        DisplayCells[i] = new FancySSCell(name, this);
        DisplayCells[i].addSSCellChangeListener(this);
        DisplayCells[i].addMouseListener(this);
        DisplayCells[i].setAutoSwitch(CanDo3D);
        DisplayCells[i].setDimension(!CanDo3D, !CanDo3D);
        DisplayCells[i].setDisplayListener(this);
        DisplayCells[i].setPreferredSize(new Dimension(MIN_VIS_WIDTH,
                                                       MIN_VIS_HEIGHT));
        if (i == 0) DisplayCells[i].setSelected(true);
        DisplayPanel.add(DisplayCells[i]);
      }
      catch (VisADException exc) {
        JOptionPane.showMessageDialog(this, "Cannot create displays.",
                 "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
      }
      catch (RemoteException exc) {
        JOptionPane.showMessageDialog(this, "Cannot create displays.",
                 "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
      }
    }

    // initialize RemoteServer
    if (server != null) {
      RemoteDataReferenceImpl[] rdri =
          new RemoteDataReferenceImpl[NumVisDisplays];
      boolean success = true;
      for (int i=0; i<NumVisDisplays; i++) {
        rdri[i] = DisplayCells[i].getRemoteDataRef();
      }
      try {
        RemoteServerImpl rsi = new RemoteServerImpl(rdri);
        Naming.rebind("//:/" + server, rsi);
      }
      catch (java.rmi.ConnectException exc) {
        final SpreadSheet ss = this;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(ss,
              "Unable to export cells as RMI addresses.  Make sure you are " +
              "running rmiregistry before launching the Spread Sheet in " +
              "server mode.",
              "Failed to initialize RemoteServer", JOptionPane.ERROR_MESSAGE);
          }
        });
        success = false;
      }
      catch (MalformedURLException exc) {
        final SpreadSheet ss = this;
        final String sname = server;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(ss,
              "Unable to export cells as RMI addresses.  " +
              "The name \"" + sname + "\" is not valid.",
              "Failed to initialize RemoteServer", JOptionPane.ERROR_MESSAGE);
          }
        });
        success = false;
      }
      catch (RemoteException exc) {
        success = false;
      }
      if (success) bTitle = bTitle + " (" + server + ")";
    }

    // display window on screen
    setTitle(bTitle);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int appWidth = (int) (0.01*sWidth*screenSize.width);
    int appHeight = (int) (0.01*sHeight*screenSize.height);
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
      DisplayCells[CurDisplay].hideWidgetFrame();
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

    // window menu commands
    else if (cmd.equals("optWidget")) {
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

  /** Creates a new spreadsheet file */
  void newFile(boolean safe) {
    if (safe) {
      int ans = JOptionPane.showConfirmDialog(this,
                "Clear all spreadsheet cells?", "Are you sure?",
                JOptionPane.YES_NO_OPTION);
      if (ans != JOptionPane.YES_OPTION) return;
    }

    // clear all cells (in smart order to prevent error dialogs)
    int len = DisplayCells.length;
    boolean[] b = new boolean[len];
    for (int i=0; i<len; i++) b[i] = false;
    boolean w = true;
    while (w) {
      for (int i=0; i<len; i++) {
        if (!DisplayCells[i].othersDepend()) {
          try {
            DisplayCells[i].clearCell();
            b[i] = true;
          }
          catch (VisADException exc) { }
          catch (RemoteException exc) { }
        }
      }
      w = false;
      for (int i=0; i<len; i++) if (!b[i]) w = true;
    }
    CurrentFile = null;
    setTitle(bTitle);
  }

  /** Opens an existing spreadsheet file */
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
          "The file " + file + " does not exist",
          "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // clear all cells
    newFile(false);

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
    boolean sfe = DisplayCells[0].getShowFormulaErrors();
    int len = DisplayCells.length;
    for (int j=0; j<len; j++) DisplayCells[j].setShowFormulaErrors(false);
    for (int i=0; i<len; i++) {
      try {
        boolean b = DisplayCells[i].getAutoDetect();
        DisplayCells[i].setAutoDetect(false);
        DisplayCells[i].setSSCellString(fileStrings[i]);
        DisplayCells[i].setAutoDetect(b);
      }
      catch (VisADException exc) {
        JOptionPane.showMessageDialog(this,
            "Could not reconstruct spreadsheet",
            "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
        newFile(false);
        for (int j=0; j<len; j++) DisplayCells[j].setShowFormulaErrors(sfe);
        return;
      }
      catch (RemoteException exc) { }
    }
    for (int j=0; j<len; j++) DisplayCells[j].setShowFormulaErrors(sfe);

    CurrentFile = f;
    setTitle(bTitle + " - " + f.getPath());
  }

  /** Saves a spreadsheet file under its current name */
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

  /** Saves a spreadsheet file under a new name */
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

  /** Does any necessary clean-up, then quits the program */
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
  void cutCell() {
    if (DisplayCells[CurDisplay].confirmClear()) {
      copyCell();
      clearCell(false);
    }
  }

  /** Copies a cell from the screen to the clipboard */
  void copyCell() {
    Clipboard = DisplayCells[CurDisplay].getSSCellString();
    EditPaste.setEnabled(true);
    ToolPaste.setEnabled(true);
  }

  /** Copies a cell from the clipboard to the screen */
  void pasteCell() {
    if (Clipboard != null) {
      try {
        boolean b = DisplayCells[CurDisplay].getAutoDetect();
        DisplayCells[CurDisplay].setAutoDetect(false);
        DisplayCells[CurDisplay].setSSCellString(Clipboard);
        DisplayCells[CurDisplay].setAutoDetect(b);
      }
      catch (VisADException exc) {
        JOptionPane.showMessageDialog(this,
            "Cannot paste cell.",
            "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
      }
      catch (RemoteException exc) { }
    }
  }

  /** Clears the mappings and formula of the current cell */
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
    refreshMenuCommands();
  }

  /** Allows the user to specify mappings from Data to Display */
  void createMappings() {
    DisplayCells[CurDisplay].addMapDialog();
  }

  /** Allows the user to import a data set */
  void loadDataSet() {
    DisplayCells[CurDisplay].loadDataDialog();
  }

  /** Allows the user to export a data set to netCDF format */
  void exportDataSetNetcdf() {
    DisplayCells[CurDisplay].saveDataDialog(true);
  }

  /** Allow the user to export a data set to serialized data format */
  void exportDataSetSerial() {
    DisplayCells[CurDisplay].saveDataDialog(false);
  }

  /** Make sure the Edit Mappings menu item and toolbar button are
      grayed-out or enabled depending whether this cell has data */
  void refreshMenuCommands() {
    if (DisplayCells[CurDisplay].hasData()) {
      DispEdit.setEnabled(true);
      ToolMap.setEnabled(true);
      FileSave1.setEnabled(true);
      FileSave2.setEnabled(true);
      ToolSave.setEnabled(true);
    }
    else {
      DispEdit.setEnabled(false);
      ToolMap.setEnabled(false);
      FileSave1.setEnabled(false);
      FileSave2.setEnabled(false);
      ToolSave.setEnabled(false);
    }
  }

  /** Make sure the formula bar is displaying up-to-date info */
  void refreshFormulaBar() {
    if (DisplayCells[CurDisplay].hasFormula()) {
      FormulaField.setText(DisplayCells[CurDisplay].getFormula());
    }
    else {
      URL u = DisplayCells[CurDisplay].getFilename();
      String s = DisplayCells[CurDisplay].getRMIAddress();
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
      DisplayCells[CurDisplay].loadDataURL(u);
    }
    else if (newFormula.startsWith("rmi://")) {
      // try to load the data from a server using RMI
      DisplayCells[CurDisplay].loadDataRMI(newFormula);
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

  /** Update dimension checkbox menu items in Cell menu */
  void refreshDisplayMenuItems() {
    // update dimension check marks
    int dim = DisplayCells[CurDisplay].getDimension();
    if (dim == BasicSSCell.JAVA3D_3D) CellDim3D3D.setState(true);
    else CellDim3D3D.setState(false);
    if (dim == BasicSSCell.JAVA2D_2D) CellDim2D2D.setState(true);
    else CellDim2D2D.setState(false);
    if (dim == BasicSSCell.JAVA3D_2D) CellDim2D3D.setState(true);
    else CellDim2D3D.setState(false);
  }

  /** Handles checkbox menu item changes (dimension checkboxes) */
  public void itemStateChanged(ItemEvent e) {
    String item = (String) e.getItem();
    if (item.equals("Show formula and RMI error messages")) {
      for (int i=0; i<DisplayCells.length; i++) {
        DisplayCells[i].setShowFormulaErrors(e.getStateChange()
                                          == ItemEvent.SELECTED);
      }
    }
    try {
      if (item.equals("3-D (Java3D)")) {
        DisplayCells[CurDisplay].setDimension(false, false);
      }
      else if (item.equals("2-D (Java2D)")) {
        DisplayCells[CurDisplay].setDimension(true, true);
      }
      else if (item.equals("2-D (Java3D)")) {
        DisplayCells[CurDisplay].setDimension(true, false);
      }
      else if (item.equals("Auto-switch to 3-D")) {
        boolean b = e.getStateChange() == ItemEvent.SELECTED;
        for (int i=0; i<NumVisDisplays; i++) DisplayCells[i].setAutoSwitch(b);
      }
      else if (item.equals("Auto-detect mappings")) {
        boolean b = e.getStateChange() == ItemEvent.SELECTED;
        for (int i=0; i<NumVisDisplays; i++) DisplayCells[i].setAutoDetect(b);
      }
      else if (item.equals("Auto-display controls")) {
        boolean b = e.getStateChange() == ItemEvent.SELECTED;
        for (int i=0; i<NumVisDisplays; i++) {
          DisplayCells[i].setAutoShowControls(b);
        }
      }
      refreshDisplayMenuItems();
    }
    catch (VisADException exc) {
      JOptionPane.showMessageDialog(this, "Cannot alter display dimension.",
          "VisAD SpreadSheet error", JOptionPane.ERROR_MESSAGE);
    }
    catch (RemoteException exc) { }
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
      int c = -1;
      for (int i=0; i<NumVisDisplays; i++) {
        if (fcell == DisplayCells[i]) c = i;
      }
      selectCell(c);
    }
  }

  /** Selects the specified cell, updating screen info */
  void selectCell(int cell) {
    if (cell < 0 || cell >= NumVisDisplays || cell == CurDisplay) return;

    // update blue border on screen
    DisplayCells[CurDisplay].setSelected(false);
    DisplayCells[cell].setSelected(true);
    CurDisplay = cell;

    // update spreadsheet info
    refreshFormulaBar();
    refreshMenuCommands();
    refreshDisplayMenuItems();
  }

  /** Handles key presses */
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

  // used with cell resizing logic
  private int oldX;
  private int oldY;

  /** Handles mouse presses and cell resizing */
  public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    for (int i=0; i<DisplayCells.length; i++) {
      if (c == DisplayCells[i]) selectCell(i);
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
      // redisplay spreadsheet cells
      int h = VertLabel[0].getSize().height;
      for (int i=0; i<NumVisX; i++) {
        Dimension d = new Dimension();
        d.width = HorizLabel[i].getSize().width;
        d.height = h;
        DisplayCells[i].setPreferredSize(d);
      }
      int w = HorizLabel[0].getSize().width;
      for (int i=0; i<NumVisY; i++) {
        Dimension d = new Dimension();
        d.width = w;
        d.height = VertLabel[i].getSize().height;
        DisplayCells[NumVisX*i].setPreferredSize(d);
      }
      for (int i=0; i<NumVisX*NumVisY; i++) {
        Dimension d = new Dimension();
        d.width = DisplayCells[i%NumVisX].getPreferredSize().width;
        d.height = DisplayCells[i/NumVisX].getPreferredSize().height;
        DisplayCells[i].setSize(d);
      }
      SCPane.invalidate();
      SCPane.validate();
    }
  }

  /** Handles cell resizing */
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
    if (DisplayCells[CurDisplay] == f) {
      if (e.getChangeType() == SSCellChangeEvent.DATA_CHANGE) {
        refreshFormulaBar();
        refreshMenuCommands();
      }
      else refreshDisplayMenuItems();
    }
  }

}

