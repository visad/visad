
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

  // Default display mapping types
  static final int COLOR_IMAGE = 1;
  static final int GRAYSCALE_IMAGE = 2;
  static final int CMY_IMAGE = 3;
  static final int HSV_IMAGE = 4;
  static final int COLOR_SPHERICAL_IMAGE = 5;
  static final int GRAYSCALE_SPHERICAL_IMAGE = 6;
  static final int CMY_SPHERICAL_IMAGE = 7;
  static final int HSV_SPHERICAL_IMAGE = 8;
  static final int COLOR_3DSURFACE = 9;
  static final int GRAYSCALE_3DSURFACE = 10;
  static final int CMY_3DSURFACE = 11;
  static final int HSV_3DSURFACE = 12;
  static final int COLOR_SPHERICAL_3DSURFACE = 13;
  static final int GRAYSCALE_SPHERICAL_3DSURFACE = 14;
  static final int CMY_SPHERICAL_3DSURFACE = 15;
  static final int HSV_SPHERICAL_3DSURFACE = 16;

  /** unselected border */
  static final Border GRAY3 = new LineBorder(Color.gray, 3);

  /** selected border */
  static final Border BLUE3 = new LineBorder(new Color(0, 127, 255), 3);

  /** This cell's parent frame. */
  Frame Parent;

  /** default mapping type */
  int DefaultMappingType = COLOR_3DSURFACE;

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
    setBorder(GRAY3);
    ErrorBox = new ErrorDialog(Parent);
  }

  public FancySSCell(String name, Frame parent) throws VisADException,
                                                       RemoteException {
    this(name, null, parent);
  }

  public FancySSCell(String name) throws VisADException, RemoteException {
    this(name, null, null);
  }

  /** Sets the Data for this cell, and applies the default ScalarMaps. */
  public void setData(Data data) throws VisADException, RemoteException {

    super.setData(data);
    setMappingScheme(DefaultMappingType);
  }

  /** Sets the dimension for this cell, and applies the default ScalarMaps. */
  public void setDimension(boolean twoD, boolean java2d)
                              throws VisADException, RemoteException {
    super.setDimension(twoD, java2d);
    setMappingScheme(DefaultMappingType);
  }

  public void setMappingScheme(int mappingType) {
    // parse data's MathType;  find FunctionType of form:
    // ((RealType, ..., RealType) -> (RealType, ..., RealType))
    Data data = DataRef.getData();
    if (data == null) return;
    MathType mathType;
    try {
      mathType = data.getType();
    }
    catch (VisADException exc) {
      return;
    }
    catch (RemoteException exc) {
      return;
    }

    FunctionType function = findFunction(mathType);
    if (function == null) return;

    MathType domain = function.getDomain();
    MathType range = function.getRange();
    RealType[] dlist;
    if (domain instanceof TupleType) {
      dlist = ((TupleType) domain).getRealComponents();
    }
    else {
      dlist = new RealType[1];
      dlist[0] = (RealType) domain;
    }
    RealType[] rlist;
    if (range instanceof TupleType) {
      rlist = ((TupleType) range).getRealComponents();
    }
    else {
      rlist = new RealType[1];
      rlist[0] = (RealType) range;
    }
    DisplayRealType[] d = null;
    DisplayRealType[] r = null;

    // set up default ScalarMaps
    if (mappingType == COLOR_IMAGE) {
      d = new DisplayRealType[2];
      d[0] = Display.XAxis;
      d[1] = Display.YAxis;
      r = new DisplayRealType[3];
      r[0] = Display.Red;
      r[1] = Display.Green;
      r[2] = Display.Blue;
    }
    else if (mappingType == GRAYSCALE_IMAGE) {
      d = new DisplayRealType[2];
      d[0] = Display.XAxis;
      d[1] = Display.YAxis;
      r = new DisplayRealType[3];
      r[0] = Display.RGB;
      r[1] = Display.RGB;
      r[2] = Display.RGB;
    }
    else if (mappingType == CMY_IMAGE) {
      d = new DisplayRealType[2];
      d[0] = Display.XAxis;
      d[1] = Display.YAxis;
      r = new DisplayRealType[3];
      r[0] = Display.Cyan;
      r[1] = Display.Magenta;
      r[2] = Display.Yellow;
    }
    else if (mappingType == HSV_IMAGE) {
      d = new DisplayRealType[2];
      d[0] = Display.XAxis;
      d[1] = Display.YAxis;
      r = new DisplayRealType[3];
      r[0] = Display.Hue;
      r[1] = Display.Saturation;
      r[2] = Display.Value;
    }
    else if (mappingType == COLOR_SPHERICAL_IMAGE) {
      d = new DisplayRealType[2];
      d[0] = Display.Latitude;
      d[1] = Display.Longitude;
      r = new DisplayRealType[3];
      r[0] = Display.Red;
      r[1] = Display.Green;
      r[2] = Display.Blue;
    }
    else if (mappingType == GRAYSCALE_SPHERICAL_IMAGE) {
      d = new DisplayRealType[2];
      d[0] = Display.Latitude;
      d[1] = Display.Longitude;
      r = new DisplayRealType[3];
      r[0] = Display.RGB;
      r[1] = Display.RGB;
      r[2] = Display.RGB;
    }
    else if (mappingType == CMY_SPHERICAL_IMAGE) {
      d = new DisplayRealType[2];
      d[0] = Display.Latitude;
      d[1] = Display.Longitude;
      r = new DisplayRealType[3];
      r[0] = Display.Cyan;
      r[1] = Display.Magenta;
      r[2] = Display.Yellow;
    }
    else if (mappingType == HSV_SPHERICAL_IMAGE) {
      d = new DisplayRealType[2];
      d[0] = Display.Latitude;
      d[1] = Display.Longitude;
      r = new DisplayRealType[3];
      r[0] = Display.Hue;
      r[1] = Display.Saturation;
      r[2] = Display.Value;
    }
    else if (mappingType == COLOR_3DSURFACE) {
      d = new DisplayRealType[3];
      d[0] = Display.XAxis;
      d[1] = Display.YAxis;
      d[2] = Display.ZAxis;
      r = new DisplayRealType[3];
      r[0] = Display.Red;
      r[1] = Display.Green;
      r[2] = Display.Blue;
    }
    else if (mappingType == GRAYSCALE_3DSURFACE) {
      d = new DisplayRealType[3];
      d[0] = Display.XAxis;
      d[1] = Display.YAxis;
      d[2] = Display.ZAxis;
      r = new DisplayRealType[3];
      r[0] = Display.RGB;
      r[1] = Display.RGB;
      r[2] = Display.RGB;
    }
    else if (mappingType == CMY_3DSURFACE) {
      d = new DisplayRealType[3];
      d[0] = Display.XAxis;
      d[1] = Display.YAxis;
      d[2] = Display.ZAxis;
      r = new DisplayRealType[3];
      r[0] = Display.Cyan;
      r[1] = Display.Magenta;
      r[2] = Display.Yellow;
    }
    else if (mappingType == HSV_3DSURFACE) {
      d = new DisplayRealType[3];
      d[0] = Display.XAxis;
      d[1] = Display.YAxis;
      d[2] = Display.ZAxis;
      r = new DisplayRealType[3];
      r[0] = Display.Hue;
      r[1] = Display.Saturation;
      r[2] = Display.Value;
    }
    else if (mappingType == COLOR_SPHERICAL_3DSURFACE) {
      d = new DisplayRealType[3];
      d[0] = Display.Latitude;
      d[1] = Display.Longitude;
      d[2] = Display.Radius;
      r = new DisplayRealType[3];
      r[0] = Display.Red;
      r[1] = Display.Green;
      r[2] = Display.Blue;
    }
    else if (mappingType == GRAYSCALE_SPHERICAL_3DSURFACE) {
      d = new DisplayRealType[3];
      d[0] = Display.Latitude;
      d[1] = Display.Longitude;
      d[2] = Display.Radius;
      r = new DisplayRealType[3];
      r[0] = Display.RGB;
      r[1] = Display.RGB;
      r[2] = Display.RGB;
    }
    else if (mappingType == CMY_SPHERICAL_3DSURFACE) {
      d = new DisplayRealType[3];
      d[0] = Display.Latitude;
      d[1] = Display.Longitude;
      d[2] = Display.Radius;
      r = new DisplayRealType[3];
      r[0] = Display.Cyan;
      r[1] = Display.Magenta;
      r[2] = Display.Yellow;
    }
    else if (mappingType == HSV_SPHERICAL_3DSURFACE) {
      d = new DisplayRealType[3];
      d[0] = Display.Latitude;
      d[1] = Display.Longitude;
      d[2] = Display.Radius;
      r = new DisplayRealType[3];
      r[0] = Display.Hue;
      r[1] = Display.Saturation;
      r[2] = Display.Value;
    }
    if (d == null || r == null) return;

    // apply ScalarMaps
    int dlen = dlist.length > d.length ? d.length : dlist.length;
    int rlen = rlist.length > r.length ? r.length : rlist.length;
    ScalarMap[] smaps = new ScalarMap[dlen+rlen];
    for (int i=0; i<dlen; i++) {
      try {
        smaps[i] = new ScalarMap(dlist[i], d[i]);
      }
      catch (VisADException exc) {
        return;
      }
    }
    for (int i=0; i<rlen; i++) {
      try {
        smaps[i+dlen] = new ScalarMap(rlist[i], r[i]);
      }
      catch (VisADException exc) {
        return;
      }
    }

    try {
      setMaps(smaps);
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
  }

  /** Used by setData's default ScalarMap logic, to find a valid function. */
  FunctionType findFunction(MathType mathType) {
    if (mathType instanceof ScalarType) return null;
    if (mathType instanceof SetType) return null;
    if (mathType instanceof TupleType) {
      for (int i=0; i<((TupleType) mathType).getDimension(); i++) {
        FunctionType f = null;
        try {
          f = findFunction(((TupleType) mathType).getComponent(i));
        }
        catch (VisADException exc) { }
        if (f != null) return f;
      }
    }
    if (mathType instanceof FunctionType) {
      MathType domain = ((FunctionType) mathType).getDomain();
      MathType range = ((FunctionType) mathType).getRange();
      if (domain instanceof FunctionType) return null;
      if (domain instanceof SetType) return null;
      if (domain instanceof TextType) return null;
      if (range instanceof FunctionType) return null;
      if (range instanceof SetType) return null;
      if (range instanceof TextType) return null;

      if (!(domain instanceof RealType)) {
        // test domain
        int dlen = ((TupleType) domain).getDimension();
        for (int i=0; i<dlen; i++) {
          try {
            if (!(((TupleType) domain).getComponent(i) instanceof RealType)) {
              return null;
            }
          }
          catch (VisADException exc) {
            return null;
          }
        }
      }

      if (!(range instanceof RealType)) {
        // test range
        int rlen = ((TupleType) range).getDimension();
        for (int i=0; i<rlen; i++) {
          try {
            if (!(((TupleType) range).getComponent(i) instanceof RealType)) {
              return null;
            }
          }
          catch (VisADException exc) {
            return null;
          }
        }
      }

      return (FunctionType) mathType;
    }
    return null;
  }

  /** Specifies whether the FancySSCell has a blue border or a gray border. */
  public void setSelected(boolean value) {
    if (Selected == value) return;
    Selected = value;
    if (Selected) setBorder(BLUE3);
    else setBorder(GRAY3);
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

  /** Sets the default scalar mappings for this cell. */
  public void setDefaultMappings(int mappingType) {
    DefaultMappingType = mappingType;
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
      clearDisplay();
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

