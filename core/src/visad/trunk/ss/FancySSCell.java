
//
// FancySSCell.java
//

package visad.ss;

// AWT packages
import java.awt.*;
import java.awt.swing.*;
import java.awt.swing.border.*;

// I/O packages
import java.io.*;

// RMI classes
import java.rmi.RemoteException;

// Utility classes
import java.util.Vector;
import java.util.Enumeration;

// VisAD packages
import visad.*;

// VisAD classes
import visad.data.BadFormException;

/** FancySSCell is an extension of BasicSSCell with extra options, such
    as a file loader dialog and a dialog to set up ScalarMaps.  It
    provides an example of GUI extensions to BasicSSCell. */
public class FancySSCell extends BasicSSCell implements FilenameFilter {

  /** unselected border */
  static final Border Gray3 = new LineBorder(Color.gray, 3);

  /** selected border */
  static final Border Blue3 = new LineBorder(new Color(0, 127, 255), 3);

  /** This cell's parent frame. */
  Frame Parent;

  /** Specifies whether this cell is selected. */
  boolean Selected = false;

  /** file dialog box */
  static FileDialog FileBox;

  /** error message dialog box */
  ErrorDialog ErrorBox;

  /** list of system intrinsic DisplayRealTypes for use with addMap() */
  static final DisplayRealType[] DisplayRealArray =
    {Display.XAxis, Display.YAxis, Display.ZAxis, Display.Latitude,
     Display.Longitude, Display.Radius, Display.List, Display.Red,
     Display.Green, Display.Blue, Display.RGB, Display.RGBA,
     Display.Hue, Display.Saturation, Display.Value, Display.HSV,
     Display.Cyan, Display.Magenta, Display.Yellow, Display.CMY,
     Display.Alpha, Display.Animation, Display.SelectValue,
     Display.SelectRange, Display.IsoContour, Display.Flow1X,
     Display.Flow1Y, Display.Flow1Z, Display.Flow2X, Display.Flow2Y,
     Display.Flow2Z, Display.Shape, Display.XAxisOffset,
     Display.YAxisOffset, Display.ZAxisOffset};

  /** number of system intrinsic DisplayRealTypes */
  static final int NumMaps = DisplayRealArray.length;

  /** list of system intrinsic DisplayRealTypes in String form */
  static String[] MapList = createMapList();

  static String[] createMapList() {
    String[] list = new String[NumMaps];
    for (int i=0; i<NumMaps; i++) {
      list[i] = DisplayRealArray[i].getName();
    }
    return list;
  }

  /** constructor */
  public FancySSCell(String name, String info, Frame parent)
                                throws VisADException, RemoteException {
    super(name, info);
    Parent = parent;
    setBorder(Gray3);
    ErrorBox = new ErrorDialog(Parent);
  }

  public FancySSCell(String name, Frame parent) throws VisADException,
                                                       RemoteException {
    this(name, null, parent);
  }

  public FancySSCell(String name) throws VisADException, RemoteException {
    this(name, null, null);
  }

  /** Specifies whether the FancySSCell has a blue border or a gray border. */
  public void setSelected(boolean value) {
    if (Selected == value) return;
    Selected = value;
    if (Selected) setBorder(Blue3);
    else setBorder(Gray3);
    paint(getGraphics());
  }

  /** Asks user to confirm clearing the cell if any other cell
      depends on it. */
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

  /** Clears the cell if no other cell depends it;  otherwise, asks the
      user "Are you sure?" */
  public void smartClear() throws VisADException, RemoteException {
    if (confirmClear()) clearCell();
  }

  /** Lets the user create ScalarMaps from the current SSPanel's Data
      to its Display. */
  public void addMapDialog() {
    // check whether this cell has data
    if (!HasData) {
      ErrorBox.showError("This cell has no data");
      return;
    }

    // get mappings from mapping dialog
    Data data = DataRef.getData();
    if (Filename == null) Filename = getFormula();
    if (Filename == null || Filename == "") Filename = "Data hierarchy";
    MappingDialog mapDialog = new MappingDialog(Parent, data, Filename);
    mapDialog.setSize(new Dimension(520, 470));
    mapDialog.setVisible(true);

    // make sure user did not cancel the operation
    if (!mapDialog.Confirm) return;

    // clear old mappings
    try {
      if (HasMappings) clearDisplay();
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }

    // set up new mappings
    ScalarMap[] newMaps;
    int numMaps = 0;
    for (int i=0; i<NumMaps; i++) {
      VisADNode node = mapDialog.DisplayMaps[i];
      if (node != mapDialog.NoneNode) numMaps++;
    }
    newMaps = new ScalarMap[numMaps];
    int j = 0;
    for (int i=0; i<NumMaps; i++) {
      VisADNode node = mapDialog.DisplayMaps[i];
      if (node != mapDialog.NoneNode) {
        try {
          newMaps[j++] = new ScalarMap((RealType) node.mathType,
                                       DisplayRealArray[i]);
        }
        catch (VisADException exc) {
          ErrorBox.showError("Illegal mapping: "
                            +((String) node.getUserObject())+" -> "
                            +MapList[i]);
        }
      }
    }
    try {
      setMaps(newMaps);
    }
    catch (VisADException exc) {
      ErrorBox.showError("Illegal mappings");
    }
    catch (RemoteException exc) { }
  }

  /** Loads a file selected by the user. */
  public void loadDataDialog() {
    // get file name from file dialog
    if (FileBox == null) {
      FileBox = new FileDialog(Parent);
      FileBox.setFilenameFilter(this);
      FileBox.setMode(FileDialog.LOAD);
    }
    FileBox.setVisible(true);

    // make sure file exists
    String file = FileBox.getFile();
    if (file == null) return;
    String directory = FileBox.getDirectory();
    if (directory == null) return;
    File f = new File(directory, file);
    if (!f.exists()) {
      ErrorBox.showError("The file does not exist");
      return;
    }

    // load file
    try {
      loadData(f);
    }
    catch (RemoteException exc) { }
    catch (BadFormException exc) { }
    catch (IOException exc) { }
    catch (VisADException exc) { }
  }

  /** Sets the minimum size of the FancySSCell. */
  public void setMinSize(Dimension size) {
    if (size != null) setPreferredSize(size);
  }

  /** Handles the file dialog filter. */
  public boolean accept(File f, String name) {
    if (f.isDirectory()) return false;
    if (!f.canRead()) return false;
    if ( !(name.endsWith(".nc") || name.endsWith(".netcdf")
        || name.endsWith(".hdf") || name.endsWith(".hdfeos")
        || name.endsWith(".fit") || name.endsWith(".fits")
        || name.endsWith(".jpg") || name.endsWith(".jpeg")
        || name.endsWith(".gif") || name.endsWith(".v5d")) ) {
      return false;
    }
    return true;
  }

}

