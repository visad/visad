
//
// FancySSCell.java
//

package visad.ss;

// AWT packages
import java.awt.*;
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

// I/O packages
import java.io.*;

// RMI classes
import java.rmi.RemoteException;

// Utility classes
import java.util.Vector;
import java.util.Enumeration;

// VisAD packages
import visad.*;
import visad.util.*;

// VisAD classes
import visad.data.BadFormException;

/** FancySSCell is an extension of BasicSSCell with extra options, such
    as a file loader dialog and a dialog to set up ScalarMaps.  It
    provides an example of GUI extensions to BasicSSCell.<P> */
public class FancySSCell extends BasicSSCell {

  /** unselected border */
  static final Border NORM = new LineBorder(Color.gray, 3);

  /** selected border */
  static final Border HIGH = new LineBorder(Color.yellow, 3);

  /** This cell's parent frame */
  Frame Parent;

  /** This cell's associated JFrame, for use with VisAD Controls */
  JFrame WidgetFrame = null;

  /** Specifies whether this cell is selected */
  boolean Selected = false;

  /** Specifies whether this cell should auto-switch to 3-D */
  boolean AutoSwitch = true;

  /** Specifies whether this cell should auto-detect mappings for data */
  boolean AutoDetect = true;

  /** constructor */
  public FancySSCell(String name, String info, Frame parent)
                                throws VisADException, RemoteException {
    super(name, info);
    Parent = parent;
    setBorder(NORM);
  }

  public FancySSCell(String name, Frame parent) throws VisADException,
                                                       RemoteException {
    this(name, null, parent);
  }

  public FancySSCell(String name) throws VisADException, RemoteException {
    this(name, null, null);
  }

  /** Switches to 3-D mode if necessary and available, then calls setMaps() */
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

  /** Sets the ScalarMaps for this cell and creates needed control widgets */
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
      if (drt == Display.RGB) {
        LabeledRGBWidget lw = new LabeledRGBWidget(maps[i]);
        addToFrame(lw);
      }
      else if (drt == Display.RGBA) {
        LabeledRGBAWidget lw = new LabeledRGBAWidget(maps[i]);
        addToFrame(lw);
      }
      else if (drt == Display.SelectValue) {
        VisADSlider vs = new VisADSlider(maps[i]);
        vs.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        addToFrame(vs);
      }
      else if (drt == Display.SelectRange) {
        SelectRangeWidget srs = new SelectRangeWidget(maps[i]);
        addToFrame(srs);
      }
      else if (drt == Display.IsoContour) {
        ContourWidget cw = new ContourWidget(maps[i]);
        WidgetFrame.getContentPane().add(cw);
        addToFrame(cw);
      }
      else if (drt == Display.Animation) {
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
    WidgetFrame = new JFrame("VisAD controls (" + Name + ")");
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

  /** Shows the widgets for altering controls */
  void showWidgetFrame() {
    if (WidgetFrame != null) WidgetFrame.setVisible(true);
  }

  /** Hides the widgets for altering controls */
  void hideWidgetFrame() {
    if (WidgetFrame != null) WidgetFrame.setVisible(false);
  }

  /** Sets the Data for this cell, and applies the default ScalarMaps */
  public void setData(Data data) throws VisADException, RemoteException {
    super.setData(data);
    hideWidgetFrame();
    WidgetFrame = null;
    if (AutoDetect) {
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

  /** Sets the dimension for this cell */
  public void setDimension(boolean twoD, boolean java2d)
                           throws VisADException, RemoteException {
    super.setDimension(twoD, java2d);
  }

  /** Specifies whether the FancySSCell has a blue border or a gray border */
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
    Graphics g = getGraphics();
    if (g != null) {
      paint(g);
      g.dispose();
    }
  }

  /** Specifies whether this FancySSCell should auto-switch to 3-D */
  public void setAutoSwitch(boolean value) {
    AutoSwitch = value;
  }

  /** Returns whether this FancySSCell auto-switches to 3-D */
  public boolean getAutoSwitch() {
    return AutoSwitch;
  }

  /** Specifies whether this FancySSCell should auto-detect its mappings */
  public void setAutoDetect(boolean value) {
    AutoDetect = value;
  }

  /** Returns whether this FancySSCell auto-detects its mappings */
  public boolean getAutoDetect() {
    return AutoDetect;
  }

  /** Asks user to confirm clearing the cell if any other cell depends on it */
  public boolean confirmClear() {
    boolean unsafe = false;
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (panel != this && panel.isDependentOn(Name)) unsafe = true;
    }
    if (unsafe) {
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

  /** Clears the cell if no other cell depends it;  otherwise, asks the
      user "Are you sure?" */
  public void smartClear() throws VisADException, RemoteException {
    if (confirmClear()) {
      hideWidgetFrame();
      WidgetFrame = null;
      clearCell();
    }
  }

  /** Lets the user create ScalarMaps from the current SSPanel's Data
      to its Display */
  public void addMapDialog() {
    // check whether this cell has data
    if (!HasData) {
      JOptionPane.showMessageDialog(Parent,
          "This cell has no data",
          "VisAD FancySSCell error", JOptionPane.ERROR_MESSAGE);
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
      clearDisplay();
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }

    // set up new mappings
    try {
      setMapsAuto(mapDialog.ScalarMaps);
    }
    catch (VisADException exc) {
      JOptionPane.showMessageDialog(Parent,
          "This combination of mappings is not valid:\n"+exc.toString(),
          "VisAD FancySSCell error", JOptionPane.ERROR_MESSAGE);
    }
    catch (RemoteException exc) { }
  }

  /** Imports a data object from a given file name, in a separate thread */
  public void loadDataFile(File f) {
    final File fn = f;
    final BasicSSCell cell = this;
    Runnable loadFile = new Runnable() {
      public void run() {
        String msg = "VisAD could not load the dataset \""+fn.getName()+"\"\n";
        try {
          cell.loadData(fn);
        }
        catch (BadFormException exc) {
          msg = msg+"VisAD does not support this file type.";
          JOptionPane.showMessageDialog(Parent, msg, "Error importing data",
                                        JOptionPane.ERROR_MESSAGE);
        }
        catch (RemoteException exc) {
          msg = msg+"A RemoteException occurred:\n"+exc.toString();
          JOptionPane.showMessageDialog(Parent, msg, "Error importing data",
                                        JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException exc) {
          msg = msg+"The file's data is corrupt.";
          JOptionPane.showMessageDialog(Parent, msg, "Error importing data",
                                        JOptionPane.ERROR_MESSAGE);
        }
        catch (VisADException exc) {
          msg = msg+"An error occurred:\n"+exc.toString();
          JOptionPane.showMessageDialog(Parent, msg, "Error importing data",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    Thread t = new Thread(loadFile);
    t.start();
  }

  /** Loads a file selected by the user */
  public void loadDataDialog() {
    // get file name from file dialog
    FileDialog fileBox = new FileDialog(Parent);
    fileBox.setMode(FileDialog.LOAD);
    fileBox.setVisible(true);

    // make sure file exists
    String file = fileBox.getFile();
    if (file == null) return;
    String directory = fileBox.getDirectory();
    if (directory == null) return;
    File f = new File(directory, file);
    if (!f.exists()) {
      JOptionPane.showMessageDialog(Parent, file+" does not exist",
                  "Cannot load file", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // load file
    loadDataFile(f);
  }

  /** Saves to a file selected by the user, in netCDF format */
  public void saveDataDialog() {
    if (!HasData) {
      JOptionPane.showMessageDialog(Parent, "This cell is empty.",
                  "Nothing to save", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // get file name from file dialog
    FileDialog fileBox = new FileDialog(Parent);
    fileBox.setMode(FileDialog.SAVE);
    fileBox.setVisible(true);

    // make sure file is valid
    String file = fileBox.getFile();
    if (file == null) return;
    String directory = fileBox.getDirectory();
    if (directory == null) return;
    File f = new File(directory, file);

    // start new thread to save the file
    final File fn = f;
    final BasicSSCell cell = this;
    Runnable saveFile = new Runnable() {
      public void run() {
        String msg = "VisAD could not save the dataset \""+fn.getName()+"\"\n";
        try {
          cell.saveData(fn);
        }
        catch (BadFormException exc) {
          msg = msg+"A BadFormException occurred:\n"+exc.toString();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
                                        JOptionPane.ERROR_MESSAGE);
        }
        catch (RemoteException exc) {
          msg = msg+"A RemoteException occurred:\n"+exc.toString();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
                                        JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException exc) {
          msg = msg+"An IOException occurred:\n"+exc.toString();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
                                        JOptionPane.ERROR_MESSAGE);
        }
        catch (VisADException exc) {
          msg = msg+"An error occurred:\n"+exc.toString();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    Thread t = new Thread(saveFile);
    t.start();
  }

  /** A thin, horizontal divider */
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

