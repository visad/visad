
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
    provides an example of GUI extensions to BasicSSCell. */
public class FancySSCell extends BasicSSCell {

  // Custom mapping type
  static final int CUSTOM = 0;

  // Default mapping types
  static final int IMAGE = 1;
  static final int LATLONIMAGE = 2;
  static final int SURFACE3D = 3;

  /** unselected border */
  static final Border NORM = new LineBorder(Color.gray, 3);

  /** selected border */
  static final Border HIGH = new LineBorder(Color.yellow, 3);

  /** This cell's parent frame. */
  Frame Parent;

  /** This cell's associated JFrame, for use with VisAD Controls. */
  JFrame WidgetFrame = null;

  /** default mapping type */
  int MappingType = SURFACE3D;

  /** Specifies whether this cell is selected. */
  boolean Selected = false;

  /** file dialog box */
  static FileDialog FileBox;

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

  /** Sets the ScalarMaps for this cell and creates needed control widgets. */
  public void setMaps(ScalarMap[] maps) throws VisADException,
                                               RemoteException {
    super.setMaps(maps);
    hideWidgetFrame();
    WidgetFrame = null;

    // create any necessary widgets
    for (int i=0; i<maps.length; i++) {
      DisplayRealType drt = maps[i].getDisplayScalar();
      if (drt == Display.RGB) {
        if (WidgetFrame == null) initWidgetFrame();
        LabeledRGBWidget lw = new LabeledRGBWidget(maps[i]);
        WidgetFrame.getContentPane().add(lw);
      }
      else if (drt == Display.RGBA) {
        if (WidgetFrame == null) initWidgetFrame();
        LabeledRGBAWidget lw = new LabeledRGBAWidget(maps[i]);
        WidgetFrame.getContentPane().add(lw);
      }
      else if (drt == Display.SelectValue) {
        if (WidgetFrame == null) initWidgetFrame();
        final DataReference ref = new DataReferenceImpl("value");
        VisADSlider vs = new VisADSlider("value", 0, 100, 0, 0.01, ref,
                                         RealType.Generic);
        final ValueControl control = (ValueControl) maps[i].getControl();
        control.setValue(0.0);
        CellImpl cell = new CellImpl() {
          public void doAction() throws VisADException, RemoteException {
            control.setValue(((Real) ref.getData()).getValue());
          }
        };
        cell.addReference(ref);
        WidgetFrame.getContentPane().add(vs);
      }
      else if (drt == Display.SelectRange) {
        if (WidgetFrame == null) initWidgetFrame();
        SelectRangeWidget srs = new SelectRangeWidget(maps[i]);
        WidgetFrame.getContentPane().add(srs);
      }
      else if (drt == Display.IsoContour) {
        // if (WidgetFrame == null) initWidgetFrame();
        // create IsoLevel slider
      }
      else if (drt == Display.Animation) {
        // if (WidgetFrame == null) initWidgetFrame();
        // create Animation widget: forward/backward, play/stop, speed controls
      }

      // show widget frame
      if (WidgetFrame != null) {
        WidgetFrame.pack();
        WidgetFrame.setVisible(true);
      }
    }
  }

  /** Used by setMaps() method. */
  void initWidgetFrame() {
    WidgetFrame = new JFrame("VisAD controls");
    Container pane = WidgetFrame.getContentPane();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
  }

  /** Shows the widgets for altering controls. */
  void showWidgetFrame() {
    if (WidgetFrame != null) WidgetFrame.setVisible(true);
  }

  /** Hides the widgets for altering controls. */
  void hideWidgetFrame() {
    if (WidgetFrame != null) WidgetFrame.setVisible(false);
  }

  /** Sets the Data for this cell, and applies the default ScalarMaps. */
  public void setData(Data data) throws VisADException, RemoteException {
    super.setData(data);
    hideWidgetFrame();
    WidgetFrame = null;
    setMappingScheme(MappingType);
  }

  /** Sets the dimension for this cell, and applies the default ScalarMaps. */
  public void setDimension(boolean twoD, boolean java2d)
                              throws VisADException, RemoteException {
    super.setDimension(twoD, java2d);
    //if (MappingType != CUSTOM) setMappingScheme(MappingType);
  }

  /** Gets this cell's mapping scheme. */
  public int getMappingScheme() {
    return MappingType;
  }

  /** Sets the mapping scheme for this cell. */
  public void setMappingScheme(int mappingScheme) {
    MappingType = mappingScheme;

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
    if (MappingType == IMAGE) {
      d = new DisplayRealType[2];
      d[0] = Display.XAxis;
      d[1] = Display.YAxis;
      r = new DisplayRealType[3];
      r[0] = Display.Red;
      r[1] = Display.Green;
      r[2] = Display.Blue;
    }
    else if (MappingType == LATLONIMAGE) {
      d = new DisplayRealType[2];
      d[0] = Display.Latitude;
      d[1] = Display.Longitude;
      r = new DisplayRealType[3];
      r[0] = Display.Red;
      r[1] = Display.Green;
      r[2] = Display.Blue;
    }
    else if (MappingType == SURFACE3D) {
      d = new DisplayRealType[3];
      d[0] = Display.XAxis;
      d[1] = Display.YAxis;
      d[2] = Display.ZAxis;
      r = new DisplayRealType[3];
      r[0] = Display.Red;
      r[1] = Display.Green;
      r[2] = Display.Blue;
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
    if (Selected) {
      setBorder(HIGH);
      showWidgetFrame();
    }
    else {
      setBorder(NORM);
      hideWidgetFrame();
    }
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
    if (confirmClear()) {
      hideWidgetFrame();
      WidgetFrame = null;
      clearCell();
    }
  }

  /** Lets the user create ScalarMaps from the current SSPanel's Data
      to its Display. */
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
      setMaps(mapDialog.ScalarMaps);
      MappingType = CUSTOM;
    }
    catch (VisADException exc) {
      JOptionPane.showMessageDialog(Parent,
          "This combination of mappings is not valid:\n"+exc.toString(),
          "VisAD FancySSCell error", JOptionPane.ERROR_MESSAGE);
    }
    catch (RemoteException exc) { }
  }

  /** Loads a file selected by the user. */
  public void loadDataDialog() {
    // get file name from file dialog
    if (FileBox == null) {
      FileBox = new FileDialog(Parent);
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
      JOptionPane.showMessageDialog(Parent,
          "The file does not exist",
          "VisAD FancySSCell error", JOptionPane.ERROR_MESSAGE);
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

}

