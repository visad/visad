//
// BasicSSCell.java
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
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.util.*;
import javax.swing.*;
import visad.*;
import visad.data.*;
import visad.data.netcdf.Plain;
import visad.data.visad.VisADForm;
import visad.formula.*;
import visad.java2d.*;
import visad.java3d.*;

/** BasicSSCell represents a single spreadsheet display cell.  BasicSSCells
    can be added to a VisAD user interface to provide some of the capabilities
    presented in the VisAD SpreadSheet program.  Other capabilities, like the
    file loader and data mapping dialog boxes, are available only with a
    FancySSCell.<P> */
public class BasicSSCell extends JPanel {

  /** used for debugging */
  public static boolean DEBUG = false;


  /** constant for use with Dim variable */
  public static final int JAVA3D_3D = 1;

  /** constant for use with Dim variable */
  public static final int JAVA2D_2D = 2;

  /** constant for use with Dim variable */
  public static final int JAVA3D_2D = 3;


  /** default FormulaManager object used by BasicSSCells */
  static final FormulaManager defaultFM = FormulaUtil.createStandardManager();

  /** list of SSCells on this JVM */
  static final Vector SSCellVector = new Vector();

  /** counter for the number of cells currently saving data */
  static int Saving = 0;


  /** name of this BasicSSCell */
  String Name;

  /** formula manager for this BasicSSCell */
  FormulaManager fm;

  /** associated VisAD DisplayPanel */
  JPanel VDPanel;

  /** associated VisAD Display */
  DisplayImpl VDisplay;

  /** associated VisAD RemoteDisplay */
  RemoteDisplay RemoteVDisplay = null;

  /** associated VisAD RemoteServer */
  RemoteServer RemoteVServer = null;

  /** associated VisAD DataReference */
  DataReferenceImpl DataRef;

  /** associated VisAD RemoteDataReference */
  RemoteDataReferenceImpl RemoteDataRef;


  /** URL from where data was imported, if any */
  URL Filename = null;

  /** whether the cell's file is considered &quot;remote data&quot; */
  boolean FileIsRemote = false;

  /** whether the remote data change detection cell has been set up yet */
  boolean setupComplete = false;

  /** RMI address from where data was imported, if any */
  String RMIAddress = null;

  /** formula of this BasicSSCell, if any */
  String Formula = "";

  /** whether the DisplayPanel is 2-D or 3-D, Java2D or Java3D */
  int Dim = -1;

  /** errors currently being displayed in this cell, if any */
  String[] Errors;


  /** list of servers to which this cell has been added */
  Vector Servers = new Vector();

  /** whether this display is remote */
  boolean IsRemote;


  /** remote clone's copy of Filename */
  RemoteDataReference RemoteFilename;

  /** remote clone's copy of RMIAddress */
  RemoteDataReference RemoteRMIAddress;

  /** remote clone's copy of Formula */
  RemoteDataReference RemoteFormula;

  /** remote clone's copy of Dim */
  RemoteDataReference RemoteDim;

  /** remote clone's copy of Errors */
  RemoteDataReference RemoteErrors;


  /** this BasicSSCell's DisplayListeners */
  Vector DListen = new Vector();

  /** whether the BasicSSCell has a valid display on-screen */
  boolean HasDisplay = false;

  /** whether the BasicSSCell has mappings from Data to Display */
  boolean HasMappings = false;

  /** prevent simultaneous GUI manipulation */
  private Object Lock = new Object();


  /** construct a new BasicSSCell with the given name */
  public BasicSSCell(String name) throws VisADException, RemoteException {
    this(name, null, null, null);
  }

  /** construct a new BasicSSCell with the given name and non-default
      formula manager, to allow for custom formulas */
  public BasicSSCell(String name, FormulaManager fman)
    throws VisADException, RemoteException
  {
    this(name, fman, null, null);
  }

  /** construct a new BasicSSCell with the given name, that gets its
      information from the given RemoteServer. The associated SSCell on the
      server end must have already invoked its addToRemoteServer method */
  public BasicSSCell(String name, RemoteServer rs)
    throws VisADException, RemoteException
  {
    this(name, null, rs, null);
  }

  /** construct a new BasicSSCell with the given name and save string, used to
      reconstruct the cell's configuration */
  public BasicSSCell(String name, String save)
    throws VisADException, RemoteException
  {
    this(name, null, null, save);
  }

  /** construct a new BasicSSCell with the given name, formula manager, and
      remote server */
  public BasicSSCell(String name, FormulaManager fman, RemoteServer rs,
    String save) throws VisADException, RemoteException
  {
    // set name
    if (name == null) {
      throw new VisADException("BasicSSCell: name cannot be null");
    }
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (name.equalsIgnoreCase(panel.Name)) {
        throw new VisADException("BasicSSCell: name already used");
      }
    }
    Name = name;
    SSCellVector.add(this);

    // set formula manager
    fm = (fman == null ? defaultFM : fman);

    // set remote server
    if (rs != null) {
      RemoteVServer = rs;
      RemoteVDisplay = rs.getDisplay(Name);
    }
    IsRemote = (RemoteVDisplay != null);

    DataReferenceImpl drFile = null;
    DataReferenceImpl drRMI = null;
    DataReferenceImpl drForm = null;
    DataReferenceImpl drDim = null;
    DataReferenceImpl drErr = null;

    if (IsRemote) {
      RemoteFilename = rs.getDataReference(name + "_Filename");
      RemoteRMIAddress = rs.getDataReference(name + "_RMIAddress");
      RemoteFormula = rs.getDataReference(name + "_Formula");
      RemoteDim = rs.getDataReference(name + "_Dim");
      RemoteErrors = rs.getDataReference(name + "_Errors");

      setDimClone();
      //setupRemoteDataChangeCell();

      VDisplay.addDisplayListener(new DisplayListener() {
        public void displayChanged(DisplayEvent e) {
          int id = e.getId();
          if (id == DisplayEvent.TRANSFORM_DONE) {
            if (!setupComplete) setupRemoteDataChangeCell();
            if (!hasDisplay()) {
              constructDisplay();
              setVDPanel(true);
            }
          }
          else if (id == DisplayEvent.MAPS_CLEARED) setVDPanel(false);
        }
      });
    }
    else {
      // redisplay this cell's data when it changes
      final FormulaManager ffm = fm;
      CellImpl ucell = new CellImpl() {
        public void doAction() {
          // clear old errors
          setErrors(null);

          // get new data
          Data value = null;
          try {
            value = (Data) ffm.getThing(Name);
          }
          catch (ClassCastException exc) {
            if (DEBUG) exc.printStackTrace();
            setError("Final value is not of the correct type.");
          }
          catch (FormulaException exc) {
            if (DEBUG) exc.printStackTrace();
            setError("The formula could not be evaluated.");
          }

          if (value == null) {
            // no value; clear display
            try {
              clearDisplay();
            }
            catch (VisADException exc) {
              if (DEBUG) exc.printStackTrace();
              setError("Unable to clear old data.");
            }
            catch (RemoteException exc) {
              if (DEBUG) exc.printStackTrace();
              setError("Unable to clear old data.");
            }
          }
          else {
            // update cell's data
            setVDPanel(true);
          }

          // display new errors, if any
          String[] es = ffm.getErrors(Name);
          if (es != null) setErrors(es);

          // broadcast data change event
          notifyListeners(SSCellChangeEvent.DATA_CHANGE);
        }
      };
      DataRef = new DataReferenceImpl(name);
      RemoteDataRef = new RemoteDataReferenceImpl(DataRef);
      fm.createVar(Name, DataRef);
      ucell.addReference(DataRef);

      // set up remote copies of data for remote cloning
      drFile = new DataReferenceImpl(Name + "_Filename");
      RemoteFilename = new RemoteDataReferenceImpl(drFile);
      synchFilename();
      drRMI = new DataReferenceImpl(Name + "_RMIAddress");
      RemoteRMIAddress = new RemoteDataReferenceImpl(drRMI);
      synchRMIAddress();
      drForm = new DataReferenceImpl(Name + "_Formula");
      RemoteFormula = new RemoteDataReferenceImpl(drForm);
      synchFormula();
      drDim = new DataReferenceImpl(Name + "_Dim");
      RemoteDim = new RemoteDataReferenceImpl(drDim);
      synchDim();
      drErr = new DataReferenceImpl(Name + "_Errors");
      RemoteErrors = new RemoteDataReferenceImpl(drErr);
      synchErrors();

      setDimension(JAVA2D_2D);
    }

    // update cell when remote filename changes
    CellImpl lFilenameCell = new CellImpl() {
      public void doAction() {
        try {
          Tuple t = (Tuple) RemoteFilename.getData();
          Real bit = (Real) t.getComponent(0);
          boolean b = bit.getValue() == 0;
          FileIsRemote = (b == IsRemote);
          if (FileIsRemote) {
            // act on filename update from remote cell
            Text nFile = (Text) t.getComponent(1);
            String s = nFile.getValue();
            URL newFilename = (s.equals("") ? null : new URL(s));
            Filename = newFilename;
          }
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (MalformedURLException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (IOException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
    };
    try {
      RemoteCellImpl rFilenameCell = new RemoteCellImpl(lFilenameCell);
      rFilenameCell.addReference(RemoteFilename);
    }
    catch (RemoteException exc) {
      if (!IsRemote) lFilenameCell.addReference(drFile);
      else throw exc;
    }

    // update cell when remote RMI address changes
    CellImpl lRMIAddressCell = new CellImpl() {
      public void doAction() {
        try {
          Tuple t = (Tuple) RemoteRMIAddress.getData();
          Real bit = (Real) t.getComponent(0);
          boolean b = bit.getValue() == 0;
          if (b != IsRemote) {
            // cells should ignore their own updates
            return;
          }
          Text nRMI = (Text) t.getComponent(1);
          String newRMIAddress = nRMI.getValue();
          if (newRMIAddress.equals("")) newRMIAddress = null;
          if (IsRemote) RMIAddress = newRMIAddress;
          else {
            String s = (RMIAddress == null ? "" : RMIAddress);
            String s2 = (newRMIAddress == null ? "" : newRMIAddress);
            if (!s.equals(s2)) loadRMI(newRMIAddress);
          }
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
    };
    try {
      RemoteCellImpl rRMIAddressCell = new RemoteCellImpl(lRMIAddressCell);
      rRMIAddressCell.addReference(RemoteRMIAddress);
    }
    catch (RemoteException exc) {
      if (!IsRemote) lRMIAddressCell.addReference(drRMI);
      else throw exc;
    }

    // update cell when remote formula changes
    CellImpl lFormulaCell = new CellImpl() {
      public void doAction() {
        try {
          Tuple t = (Tuple) RemoteFormula.getData();
          Real bit = (Real) t.getComponent(0);
          boolean b = bit.getValue() == 0;
          if (b != IsRemote) {
            // cells should ignore their own updates
            return;
          }
          Text nForm = (Text) t.getComponent(1);
          String newFormula = nForm.getValue();
          setFormula(newFormula);
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
    };
    try {
      RemoteCellImpl rFormulaCell = new RemoteCellImpl(lFormulaCell);
      rFormulaCell.addReference(RemoteFormula);
    }
    catch (RemoteException exc) {
      if (!IsRemote) lFormulaCell.addReference(drForm);
      else throw exc;
    }

    // update cell when remote dimension changes
    CellImpl lDimCell = new CellImpl() {
      public void doAction() {
        try {
          Tuple t = (Tuple) RemoteDim.getData();
          Real bit = (Real) t.getComponent(0);
          boolean b = bit.getValue() == 0;
          if (b != IsRemote) {
            // cells should ignore their own updates
            return;
          }
          Real nDim = (Real) t.getComponent(1);
          int newDim = (int) nDim.getValue();
          if (IsRemote) setDimClone();
          else setDimension(newDim);
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
    };
    try {
      RemoteCellImpl rDimCell = new RemoteCellImpl(lDimCell);
      rDimCell.addReference(RemoteDim);
    }
    catch (RemoteException exc) {
      if (!IsRemote) lDimCell.addReference(drDim);
      else throw exc;
    }

    // update cell when remote errors change
    CellImpl lErrorsCell = new CellImpl() {
      public void doAction() {
        try {
          Tuple t = (Tuple) RemoteErrors.getData();
          Real bit = (Real) t.getComponent(0);
          boolean b = bit.getValue() == 0;
          if (b != IsRemote) {
            // cells should ignore their own updates
            return;
          }
          Data d = t.getComponent(1);
          String[] newErrors;
          if (d instanceof Tuple) {
            Tuple nErr = (Tuple) d;
            int len = nErr.getDimension();
            newErrors = new String[len];
            for (int i=0; i<len; i++) {
              newErrors[i] = ((Text) nErr.getComponent(i)).getValue();
            }
          }
          else newErrors = null;
          setErrors(newErrors);
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
    };
    try {
      RemoteCellImpl rErrorsCell = new RemoteCellImpl(lErrorsCell);
      rErrorsCell.addReference(RemoteErrors);
    }
    catch (RemoteException exc) {
      if (!IsRemote) lErrorsCell.addReference(drErr);
      else throw exc;
    }

    // setup save string
    if (save != null) setSaveString(save);

    // finish GUI setup
    VDPanel = (JPanel) VDisplay.getComponent();
    setPreferredSize(new Dimension(0, 0));
    setBackground(Color.black);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
  }

  private void setupRemoteDataChangeCell() {
    // attempt to obtain DataReference from cloned display
    DataReference dr = null;
    Vector v = VDisplay.getLinks();
    if (v != null && v.size() > 0) {
      DataDisplayLink ddl = (DataDisplayLink) v.elementAt(0);
      dr = (DataReference) ddl.getThingReference();
    }
    if (dr != null) {
      // if successful, use cell to listen for data changes
      CellImpl lrdccell = new CellImpl() {
        public void doAction() {
          // data has changed; notify listeners
          notifyListeners(SSCellChangeEvent.DATA_CHANGE);
        }
      };
      try {
        RemoteCellImpl rrdccell = new RemoteCellImpl(lrdccell);
        rrdccell.addReference(dr);
        setupComplete = true;
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
      }
    }
  }

  private void synchFilename() {
    try {
      Real bit = new Real(IsRemote ? 1 : 0);
      Text nFile = new Text(Filename == null ? "" : Filename.toString());
      Tuple t = new Tuple(new Data[] {bit, nFile}, false);
      RemoteFilename.setData(t);
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  private void synchRMIAddress() {
    try {
      Real bit = new Real(IsRemote ? 1 : 0);
      Text nRMI = new Text(RMIAddress == null ? "" : RMIAddress);
      Tuple t = new Tuple(new Data[] {bit, nRMI}, false);
      RemoteRMIAddress.setData(t);
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  private void synchFormula() {
    try {
      Real bit = new Real(IsRemote ? 1 : 0);
      Text nForm = new Text(Formula);
      Tuple t = new Tuple(new Data[] {bit, nForm}, false);
      RemoteFormula.setData(t);
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  private void synchDim() {
    try {
      Real bit = new Real(IsRemote ? 1 : 0);
      Real nDim = new Real(Dim);
      Tuple t = new Tuple(new Data[] {bit, nDim}, false);
      RemoteDim.setData(t);
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  private void synchErrors() {
    try {
      Real bit = new Real(IsRemote ? 1 : 0);
      Data nErrors;
      if (Errors != null) {
        int len = Errors.length;
        Text[] t = new Text[len];
        TextType[] tt = new TextType[len];
        for (int i=0; i<len; i++) {
          t[i] = new Text(Errors[i]);
          tt[i] = (TextType) t[i].getType();
        }
        nErrors = new Tuple(new TupleType(tt), t);
      }
      else {
        nErrors = new Text("");
      }
      Tuple t = new Tuple(new Data[] {bit, nErrors}, false);
      RemoteErrors.setData(t);
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  /** get this SSCell's name */
  public String getName() {
    return Name;
  }

  /** get this SSCell's VisAD Display */
  public DisplayImpl getDisplay() {
    return VDisplay;
  }

  /** get this SSCell's VisAD RemoteDisplay */
  public RemoteDisplay getRemoteDisplay() {
    return RemoteVDisplay;
  }

  /** refresh this SSCell's display */
  public void refresh() {
    validate();
    repaint();
  }

  /** add or remove VDPanel from this BasicSSCell */
  void setVDPanel(boolean value) {
    HasDisplay = false;

    // redraw cell
    synchronized (Lock) {
      removeAll();
      if (value) add(VDPanel);
      refresh();
    }

    HasDisplay = value;
  }

  /** display an error in this BasicSSCell, or setError(null) for no error */
  void setError(String msg) {
    String[] s = (msg == null ? null : new String[] {msg});
    setErrors(s);
  }

  /** display errors in this BasicSSCell, or setErrors(null) for no errors */
  void setErrors(String[] msg) {
    boolean noChange = true;
    int oLen = (Errors == null ? 0 : Errors.length);
    int nLen = (msg == null ? 0 : msg.length);
    if (oLen != nLen) noChange = false;
    else {
      for (int i=0; i<nLen; i++) {
        if (!Errors[i].equals(msg[i])) noChange = false;
      }
    }
    if (noChange) return;

    JComponent ErrorCanvas;
    if (msg == null) ErrorCanvas = null;
    else {
      final String[] m = msg;
      ErrorCanvas = new JComponent() {
        public void paint(Graphics g) {
          g.setColor(Color.white);
          String s = (m.length == 1 ? "An error" : "Errors") +
                     " occurred while computing this cell:";
          g.drawString(s, 8, 20);
          for (int i=0; i<m.length; i++) g.drawString(m[i], 8, 15*i + 50);
        }
      };
    }
    Errors = msg;

    // update remote copy of Errors
    synchErrors();

    // redraw cell
    synchronized (Lock) {
      removeAll();
      if (ErrorCanvas != null) add(ErrorCanvas);
      refresh();
    }
  }

  /** add this SSCell to the given RemoteServer. SSCell servers must call
      this method for each cell before clients can clone the cells with
      the BasicSSCell(String name, RemoteServer rs) constructor, and
      before the cells can be exported as RMI addresses */
  public void addToRemoteServer(RemoteServerImpl rs) throws RemoteException {
    if (rs == null) return;
    if (IsRemote) {
      throw new RemoteException("Cannot add a cloned cell to a server");
    }

    synchronized (Servers) {
      if (!Servers.contains(rs)) {
        rs.addDataReference(RemoteDataRef);
        rs.addDisplay((RemoteDisplayImpl) RemoteVDisplay);
        rs.addDataReference((RemoteDataReferenceImpl) RemoteFilename);
        rs.addDataReference((RemoteDataReferenceImpl) RemoteRMIAddress);
        rs.addDataReference((RemoteDataReferenceImpl) RemoteFormula);
        rs.addDataReference((RemoteDataReferenceImpl) RemoteDim);
        rs.addDataReference((RemoteDataReferenceImpl) RemoteErrors);
        Servers.add(rs);
      }
    }
  }

  /** remove this SSCell from the given RemoteServer */
  public void removeFromRemoteServer(RemoteServerImpl rs)
                                     throws RemoteException {
    if (rs == null) return;
    if (IsRemote) {
      throw new RemoteException("Cannot remove a cloned cell from a server");
    }

    synchronized (Servers) {
      if (Servers.contains(rs)) {
        rs.removeDataReference(RemoteDataRef);
        rs.removeDisplay((RemoteDisplayImpl) RemoteVDisplay);
        rs.removeDataReference((RemoteDataReferenceImpl) RemoteFilename);
        rs.removeDataReference((RemoteDataReferenceImpl) RemoteRMIAddress);
        rs.removeDataReference((RemoteDataReferenceImpl) RemoteFormula);
        rs.removeDataReference((RemoteDataReferenceImpl) RemoteDim);
        rs.removeDataReference((RemoteDataReferenceImpl) RemoteErrors);
        Servers.remove(rs);
      }
    }
  }

  /** list of SSCellListeners to be notified of changes */
  private Vector list = new Vector();

  /** add an SSCellListener to be notified of changes */
  public void addSSCellChangeListener(SSCellListener l) {
    if (!list.contains(l)) list.add(l);
  }

  /** remove an SSCellListener */
  public void removeListener(SSCellListener l) {
    if (list.contains(l)) list.remove(l);
  }

  /** remove all SSCellListeners */
  public void removeAllListeners() {
    list.removeAllElements();
  }

  /** notify SSCellListeners that change occurred */
  private void notifyListeners(int changeType) {
    SSCellChangeEvent e = new SSCellChangeEvent(this, changeType);
    for (int i=0; i<list.size(); i++) {
      SSCellListener l = (SSCellListener) list.elementAt(i);
      l.ssCellChanged(e);
    }
  }

  /** return the BasicSSCell object with the specified display */
  public static BasicSSCell getSSCellByDisplay(Display d) {
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (d == (Display) panel.VDisplay) return panel;
    }
    return null;
  }

  /** return the BasicSSCell object with the specified name */
  public static BasicSSCell getSSCellByName(String name) {
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (name.equalsIgnoreCase(panel.Name)) return panel;
    }
    return null;
  }

  /** obtain a Vector consisting of all ScalarTypes present in data's MathType;
      return the number of duplicate ScalarTypes found */
  static int getRealTypes(Data data, Vector v) {
    MathType dataType;
    try {
      dataType = data.getType();
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
      return -1;
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
      return -1;
    }
    int[] i = new int[1];
    i[0] = 0;

    if (dataType instanceof FunctionType) {
      parseFunction((FunctionType) dataType, v, i);
    }
    else if (dataType instanceof SetType) {
      parseSet((SetType) dataType, v, i);
    }
    else if (dataType instanceof TupleType) {
      parseTuple((TupleType) dataType, v, i);
    }
    else parseScalar((ScalarType) dataType, v, i);

    return i[0];
  }

  /** used by getRealTypes */
  private static void parseFunction(FunctionType mathType, Vector v, int[] i) {
    // extract domain
    RealTupleType domain = mathType.getDomain();
    parseTuple((TupleType) domain, v, i);

    // extract range
    MathType range = mathType.getRange();
    if (range instanceof FunctionType) {
      parseFunction((FunctionType) range, v, i);
    }
    else if (range instanceof SetType) {
      parseSet((SetType) range, v, i);
    }
    else if (range instanceof TupleType) {
      parseTuple((TupleType) range, v, i);
    }
    else parseScalar((ScalarType) range, v, i);

    return;
  }

  /** used by getRealTypes */
  private static void parseSet(SetType mathType, Vector v, int[] i) {
    // extract domain
    RealTupleType domain = mathType.getDomain();
    parseTuple((TupleType) domain, v, i);

    return;
  }

  /** used by getRealTypes */
  private static void parseTuple(TupleType mathType, Vector v, int[] i) {
    // extract components
    for (int j=0; j<mathType.getDimension(); j++) {
      MathType cType = null;
      try {
        cType = mathType.getComponent(j);
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
      }

      if (cType != null) {
        if (cType instanceof FunctionType) {
          parseFunction((FunctionType) cType, v, i);
        }
        else if (cType instanceof SetType) {
          parseSet((SetType) cType, v, i);
        }
        else if (cType instanceof TupleType) {
          parseTuple((TupleType) cType, v, i);
        }
        else parseScalar((ScalarType) cType, v, i);
      }
    }
    return;
  }

  /** used by getRealTypes */
  private static void parseScalar(ScalarType mathType, Vector v, int[] i) {
    if (mathType instanceof RealType) {
      if (v.contains(mathType)) i[0]++;
      v.add(mathType);
    }
  }

  /** return true if any BasicSSCell is currently saving data */
  public static boolean isSaving() {
    return Saving > 0;
  }

  /** @deprecated use setSaveString(String) instead */
  public void setSSCellString(String save)
    throws VisADException, RemoteException
  {
    setSaveString(save);
  }

  /** reconstruct this BasicSSCell using the specified save string */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    String filename = null;
    String rmi = null;
    String formula = null;
    int dim = -1;
    Vector rnames = null;
    Vector dnames = null;
    String proj = null;
    String color = null;

    // parse the save string into lines
    StringTokenizer st = new StringTokenizer(save, "\n\r");
    int numTokens = st.countTokens();
    String[] tokens = new String[numTokens + 1];
    for (int i=0; i<numTokens; i++) tokens[i] = st.nextToken().trim();
    tokens[numTokens] = null;
    st = null;

    // analyze each line of the save string
    int tokenNum = 0;
    while (true) {
      // get next meaningful line
      String line;
      int len;
      int eq;
      do {
        line = tokens[tokenNum++];
        len = (line == null ? -1 : line.length());
        if (len < 0) {
          // end-of-string reached
          eq = 0;
        }
        else if (len == 0) {
          // ignore blank lines
          eq = -1;
        }
        else if (line.charAt(0) == '#') {
          // ignore comments
          eq = -1;
        }
        else eq = line.indexOf('=');
      }
      while (eq < 0);

      if (line == null) {
        // end-of-string reached
        break;
      }
      String keyword = line.substring(0, eq).trim();

      // get remainder of information after the equals sign
      String surplus = line.substring(eq + 1, len).trim();
      String nextLine = tokens[tokenNum];
      boolean first = true;
      while (nextLine != null && nextLine.indexOf('=') < 0) {
        if (first) {
          surplus = surplus + "\n";
          first = false;
        }
        if (nextLine.length() > 0) surplus = surplus + nextLine + "\n";
        nextLine = tokens[++tokenNum];
      }

      // examine all cases

      // filename
      if (keyword.equalsIgnoreCase("filename") ||
        keyword.equalsIgnoreCase("file name") ||
        keyword.equalsIgnoreCase("file_name") ||
        keyword.equalsIgnoreCase("file"))
      {
        filename = surplus;
        if (filename.equals("null")) filename = null;
      }

      // rmi address
      else if (keyword.equalsIgnoreCase("rmi") ||
        keyword.equalsIgnoreCase("rmi address") ||
        keyword.equalsIgnoreCase("rmi_address") ||
        keyword.equalsIgnoreCase("rmiaddress"))
      {
        rmi = surplus;
        if (rmi.equals("null")) rmi = null;
      }

      // formula
      else if (keyword.equalsIgnoreCase("formula") ||
        keyword.equalsIgnoreCase("equation"))
      {
        formula = surplus;
      }

      // dimension
      else if (keyword.equalsIgnoreCase("dim") ||
        keyword.equalsIgnoreCase("dimension"))
      {
        int d = -1;
        try {
          d = Integer.parseInt(surplus);
        }
        catch (NumberFormatException exc) { }
        if (d > 0 && d < 4) dim = d;
        else {
          // invalid dimension value
          System.err.println("Warning: dimension value " + surplus +
            " is not valid and will be ignored");
        }
      }

      // mappings
      else if (keyword.equalsIgnoreCase("maps") ||
        keyword.equalsIgnoreCase("mappings"))
      {
        st = new StringTokenizer(surplus);
        dnames = new Vector();
        rnames = new Vector();
        while (true) {
          if (!st.hasMoreTokens()) break;
          String s = st.nextToken();
          if (!st.hasMoreTokens()) {
            System.err.println("Warning: trailing maps value " + s +
              " has no corresponding number and will be ignored");
            break;
          }
          String si = st.nextToken();
          Integer i = null;
          try {
            i = new Integer(Integer.parseInt(si));
          }
          catch (NumberFormatException exc) { }
          if (i == null) {
            System.err.println("Warning: maps value " + si + " is not a " +
              "valid integer and the maps pair (" + s + ", " + si + ") " +
              "will be ignored");
          }
          else {
            dnames.add(s);
            rnames.add(i);
          }
        }
      }

      // projection matrix
      else if (keyword.equalsIgnoreCase("projection") ||
        keyword.equalsIgnoreCase("proj"))
      {
        proj = surplus;
      }

      // color table
      else if (keyword.equalsIgnoreCase("color") ||
        keyword.equalsIgnoreCase("color table") ||
        keyword.equalsIgnoreCase("color_table") ||
        keyword.equalsIgnoreCase("colortable"))
      {
        color = surplus;
      }

      // unknown keyword
      else {
        System.err.println("Warning: keyword " +
          line.substring(0, eq).trim() + " is unknown and will be ignored");
      }
    }

    // clear old stuff from cell
    clearCell();

    // set up dimension
    setDimension(dim);

    // set up filename
    if (filename != null) {
      URL u = null;
      try {
        u = new URL(filename);
      }
      catch (MalformedURLException exc) {
        throw new VisADException(exc.toString());
      }
      loadData(u);
    }

    // set up RMI address
    if (rmi != null) loadRMI(rmi);

    // set up formula
    if (formula != null && !formula.equals("")) setFormula(formula);

    // set up mappings
    if (dnames != null) {
      int len = dnames.size();
      if (len > 0) {
        // get Vector of all ScalarTypes in this data object
        Vector types = new Vector();
        Data data = getData();
        if (data != null) getRealTypes(getData(), types);
        int vLen = types.size();
        int dLen = Display.DisplayRealArray.length;

        // construct ScalarMaps
        ScalarMap[] maps = new ScalarMap[len];
        for (int j=0; j<len; j++) {
          // find appropriate ScalarType
          ScalarType domain = null;
          String name = (String) dnames.elementAt(j);
          for (int k=0; k<vLen && domain==null; k++) {
            ScalarType type = (ScalarType) types.elementAt(k);
            if (name.equals(type.getName())) domain = type;
          }
          if (domain == null) {
            // still haven't found type; look in static Vector for it
            domain = ScalarType.getScalarTypeByName(name);
          }

          // find appropriate DisplayRealType
          int q = ((Integer) rnames.elementAt(j)).intValue();
          DisplayRealType range = null;
          if (q >= 0 && q < dLen) range = Display.DisplayRealArray[q];

          // construct mapping
          if (domain == null || range == null) {
            System.err.println("Warning: maps pair (" + name + ", " +
              q + ") is not a valid ScalarMap and will be ignored");
            maps[j] = null;
          }
          else maps[j] = new ScalarMap(domain, range);
        }
        setMaps(maps);
      }
    }

    // set up projection control
    if (proj != null) {
      ProjectionControl pc = VDisplay.getProjectionControl();
      if (pc != null) pc.setSaveString(proj);
      else System.err.println("Warning: display has no ProjectionControl; " +
        "the provided projection matrix will be ignored");
    }

    // set up color control
    if (color != null) {
      ColorControl cc = (ColorControl)
        VDisplay.getControl(ColorControl.class);
      if (cc != null) cc.setSaveString(color);
      else System.err.println("Warning: display has no ColorControl; " +
        "the provided color table will be ignored");
    }
  }

  /** @deprecated use getSaveString() instead */
  public String getSSCellString() {
    return getSaveString();
  }

  /** return the save string necessary to reconstruct this cell */
  public String getSaveString() {
    if (IsRemote) return null;
    else {
      String s = "";
      if (Filename != null) s = s + "filename = " + Filename.toString() + "\n";
      if (RMIAddress != null) s = s + "rmi = " + RMIAddress + "\n";
      if (!Formula.equals("")) s = s + "formula = " + Formula + "\n";
      s = s + "dim = " + Dim + "\n";
      if (VDisplay != null) {
        Vector mapVector = VDisplay.getMapVector();
        int mvs = mapVector.size();
        if (mvs > 0) {
          s = s + "maps = ";
          for (int i=0; i<mvs; i++) {
            ScalarMap m = (ScalarMap) mapVector.elementAt(i);
            ScalarType domain = m.getScalar();
            DisplayRealType range = m.getDisplayScalar();
            int q = -1;
            for (int j=0; j<Display.DisplayRealArray.length; j++) {
              if (range.equals(Display.DisplayRealArray[j])) q = j;
            }
            if (i > 0) s = s + " ";
            s = s + domain.getName() + " " + q;
          }
          s = s + "\n";
        }
      }
      if (VDisplay != null) {
        ProjectionControl pc = VDisplay.getProjectionControl();
        if (pc != null) s = s + "projection = " + pc.getSaveString() + "\n";
        ColorControl cc = (ColorControl)
          VDisplay.getControl(ColorControl.class);
        if (cc != null) s = s + "color = " + cc.getSaveString();
      }
      return s;
    }
  }

  /** add a DisplayListener to this cell */
  public void addDisplayListener(DisplayListener d) {
    synchronized (DListen) {
      if (!DListen.contains(d)) {
        VDisplay.addDisplayListener(d);
        DListen.add(d);
      }
    }
  }

  /** remove a DisplayListener from this cell */
  public void removeDisplayListener(DisplayListener d) {
    synchronized (DListen) {
      if (DListen.contains(d)) {
        VDisplay.removeDisplayListener(d);
        DListen.remove(d);
      }
    }
  }

  /** map RealTypes to the display according to the specified ScalarMaps */
  public void setMaps(ScalarMap[] maps) throws VisADException,
                                               RemoteException {
    if (maps == null) return;

    VisADException vexc = null;
    RemoteException rexc = null;
    DataReference dr = getReference();
    if (IsRemote) {
      if (true) {
        throw new UnimplementedException("Cannot setMaps " +
          "on a cloned cell (yet).");
      }
      setVDPanel(false);
      clearMaps();
      for (int i=0; i<maps.length; i++) {
        if (maps[i] != null) {
          try {
            RemoteVDisplay.addMap(maps[i]);
          }
          catch (VisADException exc) {
            vexc = exc;
          }
          catch (RemoteException exc) {
            rexc = exc;
          }
        }
      }
      RemoteVDisplay.addReference(dr);
      constructDisplay();
      setVDPanel(true);
    }
    else {
      clearMaps();
      VDisplay.disableAction();
      for (int i=0; i<maps.length; i++) {
        try {
          VDisplay.addMap(maps[i]);
        }
        catch (VisADException exc) {
          vexc = exc;
        }
        catch (RemoteException exc) {
          rexc = exc;
        }
      }
      VDisplay.addReference(dr);
      VDisplay.enableAction();
    }
    HasMappings = true;
    if (vexc != null) throw vexc;
    if (rexc != null) throw rexc;
  }

  /** return array of this cell's mappings */
  public ScalarMap[] getMaps() {
    ScalarMap[] maps = null;
    if (VDisplay != null) {
      Vector mapVector = VDisplay.getMapVector();
      int len = mapVector.size();
      maps = (len > 0 ? new ScalarMap[len] : null);
      for (int i=0; i<len; i++) maps[i] = (ScalarMap) mapVector.elementAt(i);
    }
    return maps;
  }

  /** whether other cells are dependent on this one */
  public boolean othersDepend() {
    // NOTE: Cloned cells currently cannot detect whether or not
    //       other cells depend on them.
    if (IsRemote) return false;
    else {
      try {
        return !fm.canBeRemoved(Name);
      }
      catch (FormulaException exc) {
        if (DEBUG) exc.printStackTrace();
        return false;
      }
    }
  }

  /** clear this cell's mappings */
  public void clearMaps() throws VisADException, RemoteException {
    if (hasMappings()) {
      if (IsRemote) {
        RemoteVDisplay.removeAllReferences();
        RemoteVDisplay.clearMaps();
        setVDPanel(false);
        constructDisplay();
        VDPanel = (JPanel) VDisplay.getComponent();
        setVDPanel(true);
      }
      else {
        VDisplay.removeReference(DataRef);
        VDisplay.clearMaps();
      }
      HasMappings = false;
    }
  }

  /** clear this cell's display */
  public void clearDisplay() throws VisADException, RemoteException {
    clearMaps();
    setErrors(null);
    setVDPanel(false);
  }

  /** clear this cell completely */
  public void clearCell() throws VisADException, RemoteException {
    setFormula(null);
    Filename = null;
    RMIAddress = null;
    clearDisplay();
    setData(null);

    // update remote copies of Filename and RMIAddress
    synchFilename();
    synchRMIAddress();
  }

  /** clear this cell completely and permanently remove it from the
      list of created cells */
  public void destroyCell() throws VisADException, RemoteException {
    if (!IsRemote) clearCell();

    // remove cell from all servers
    int slen = Servers.size();
    if (slen > 0) {
      for (int i=0; i<slen; i++) {
        RemoteServerImpl rs = (RemoteServerImpl) Servers.elementAt(i);
        removeFromRemoteServer(rs);
      }
    }

    if (!IsRemote) fm.remove(Name);
    SSCellVector.remove(this);
  }

  /** set this cell's Data to data */
  public void setData(Data data) throws VisADException, RemoteException {
    if (IsRemote) {
      throw new UnimplementedException("Cannot setData " +
        "on a cloned cell (yet).");
    }

    fm.setThing(Name, data);

    if (data != null) {
      // add this Data's RealTypes to FormulaManager variable registry
      Vector v = new Vector();
      getRealTypes(data, v);
      int len = v.size();
      for (int i=0; i<len; i++) {
        RealType rt = (RealType) v.elementAt(i);
        fm.setThing(rt.getName(), new VRealType(rt));
      }
    }
  }

  /** set the BasicSSCell to 2-D or 3-D display with Java2D or Java3D */
  public void setDimension(boolean twoD, boolean java2d)
                           throws VisADException, RemoteException {
    int dim;
    if (!twoD && java2d) return;
    if (!twoD && !java2d) dim = JAVA3D_3D;
    else if (twoD && java2d) dim = JAVA2D_2D;
    else dim = JAVA3D_2D;  // twoD && !java2d
    setDimension(dim);
  }

  private void setDimension(int dim) throws VisADException, RemoteException {
    if (Dim == dim) return;
    Dim = dim;

    if (!IsRemote) {
      synchronized (DListen) {
        // remove listeners temporarily
        int dlen = DListen.size();
        if (dlen > 0) {
          for (int i=0; i<dlen; i++) {
            DisplayListener d = (DisplayListener) DListen.elementAt(i);
            VDisplay.removeDisplayListener(d);
          }
        }

        // save current mappings for restoration after dimension switch
        ScalarMap[] maps = null;
        if (VDisplay != null) {
          Vector mapVector = VDisplay.getMapVector();
          int mvs = mapVector.size();
          if (mvs > 0) {
            maps = new ScalarMap[mvs];
            for (int i=0; i<mvs; i++) {
              maps[i] = (ScalarMap) mapVector.elementAt(i);
            }
          }
        }

        // clear display completely
        clearDisplay();

        synchronized (Servers) {
          // remove old display from all RemoteServers
          int slen = Servers.size();
          for (int i=0; i<slen; i++) {
            RemoteServerImpl rsi = (RemoteServerImpl) Servers.elementAt(i);
            rsi.removeDisplay((RemoteDisplayImpl) RemoteVDisplay);
          }

          // switch display dimension
          constructDisplay();

          // add new display to all RemoteServers
          for (int i=0; i<slen; i++) {
            RemoteServerImpl rsi = (RemoteServerImpl) Servers.elementAt(i);
            rsi.addDisplay((RemoteDisplayImpl) RemoteVDisplay);
          }
        }

        // put mappings back
        if (maps != null) {
          try {
            setMaps(maps);
          }
          catch (VisADException exc) {
            if (DEBUG) exc.printStackTrace();
          }
        }

        // reinitialize display
        VDPanel = (JPanel) VDisplay.getComponent();
        if (hasData()) setVDPanel(true);

        // put listeners back
        for (int i=0; i<dlen; i++) {
          DisplayListener d = (DisplayListener) DListen.elementAt(i);
          VDisplay.addDisplayListener(d);
        }
      }

      // broadcast dimension change event
      notifyListeners(SSCellChangeEvent.DIMENSION_CHANGE);
    }

    // update remote copy of Dim
    synchDim();
  }

  private static final String j33 = "visad.java3d.DefaultDisplayRendererJ3D";
  private static final String j22 = "visad.java2d.DefaultDisplayRendererJ2D";
  private static final String j32 = "visad.java3d.TwoDDisplayRendererJ3D";

  /** update the dimension of a cloned cell to match that of the server */
  private void setDimClone() throws VisADException, RemoteException {
    synchronized (DListen) {
      // remove listeners temporarily
      int dlen = DListen.size();
      if (dlen > 0) {
        for (int i=0; i<dlen; i++) {
          DisplayListener d = (DisplayListener) DListen.elementAt(i);
          VDisplay.removeDisplayListener(d);
        }
      }

      // remove old display panel from cell
      setVDPanel(false);

      // get updated display from server
      RemoteVDisplay = RemoteVServer.getDisplay(Name);

      // autodetect new dimension
      String s = RemoteVDisplay.getDisplayRendererClassName();
      if (s.equals(j33)) Dim = JAVA3D_3D;
      else if (s.equals(j22)) Dim = JAVA2D_2D;
      else if (s.equals(j32)) Dim = JAVA3D_2D;

      // construct new display from server's display
      boolean success = constructDisplay();

      if (!success) {
        // set up error message canvas
        JComponent errorCanvas;
        if (Dim == JAVA2D_2D) {
          errorCanvas = new JComponent() {
            public void paint(Graphics g) {
              g.setColor(Color.white);
              g.drawString("A serious error occurred while " +
                           "constructing this display.", 8, 20);
            }
          };
        }
        else {
          errorCanvas = new JComponent() {
            public void paint(Graphics g) {
              g.setColor(Color.white);
              g.drawString("This machine does not support 3-D " +
                           "displays.", 8, 20);
              g.drawString("Switch the dimension to 2-D (Java2D) to " +
                           "view this display.", 8, 35);
            }
          };
        }

        // set up dummy display
        VDisplay = new DisplayImplJ2D("DUMMY");

        // redraw cell
        synchronized (Lock) {
          removeAll();
          add(errorCanvas);
          refresh();
        }
      }

      // reinitialize display
      VDPanel = (JPanel) VDisplay.getComponent();
      if (success && hasData()) setVDPanel(true);

      // put listeners back
      for (int i=0; i<dlen; i++) {
        DisplayListener d = (DisplayListener) DListen.elementAt(i);
        VDisplay.addDisplayListener(d);
      }
    }

    // broadcast dimension change event
    notifyListeners(SSCellChangeEvent.DIMENSION_CHANGE);
  }

  /** set the BasicSSCell's formula */
  public void setFormula(String f) throws VisADException, RemoteException {
    String nf = (f == null ? "" : f);
    if (Formula.equals(nf)) return;
    Formula = "";
    fm.assignFormula(Name, nf);
    Formula = nf;

    // update remote copy of Formula
    synchFormula();
  }

  /** return whether the BasicSSCell is in 2-D display mode */
  public int getDimension() {
    return Dim;
  }

  /** return the associated DataReference object */
  public DataReferenceImpl getDataRef() {
    return DataRef;
  }

  /** return the associated RemoteDataReference object */
  public RemoteDataReferenceImpl getRemoteDataRef() {
    return RemoteDataRef;
  }

  /** return the URL of the file from which this cell's Data came */
  public URL getFileURL() {
    return Filename;
  }

  /** return the file name from which the associated Data came */
  public String getFilename() {
    String f;
    if (Filename == null) f = "";
    else {
      f = Filename.toString();
      if (FileIsRemote) f = f + " (remote)";
    }
    return f;
  }

  /** return the RMI address from which the associated Data came */
  public String getRMIAddress() {
    return RMIAddress;
  }

  /** return the formula for this BasicSSCell */
  public String getFormula() {
    return Formula;
  }

  /** used by toggleWait */
  private JPanel pWait = null;

  /** used by toggleWait */
  private boolean waiting = false;

  /** used by loadData and loadRMI */
  private void toggleWait() {
    if (pWait == null) {
      pWait = new JPanel();
      pWait.setBackground(Color.black);
      pWait.setLayout(new BoxLayout(pWait, BoxLayout.X_AXIS));
      pWait.add(Box.createHorizontalGlue());
      pWait.add(new JLabel("Please wait..."));
      pWait.add(Box.createHorizontalGlue());
    }
    // redraw cell
    synchronized (Lock) {
      if (waiting) {
        remove(pWait);
        waiting = false;
      }
      else {
        add(pWait);
        waiting = true;
      }
      refresh();
    }
  }

  /** import a data object from a given URL */
  public void loadData(URL u) throws VisADException, RemoteException {
    if (IsRemote) {
      throw new UnimplementedException("Cannot loadData " +
        "on a cloned cell (yet).");
    }

    if (u == null) return;
    clearDisplay();
    setFormula(null);
    Filename = null;
    RMIAddress = null;
    toggleWait();

    Data data = null;
    try {
      // file detection --
      // necessary because some Data Forms lack open(URL) capability
      String s = u.toString();
      boolean f = false;
      String file = null;
      if (s.length() >= 6 && s.substring(0, 6).equalsIgnoreCase("file:/")) {
        f = true;
        file = s.substring(6);
      }

      // load file
      DefaultFamily loader = new DefaultFamily("loader");
      if (f) data = loader.open(file);
      else data = loader.open(u);
    }
    catch (BadFormException exc) {
      if (DEBUG) exc.printStackTrace();
      setError("The file could not be converted to VisAD data.");
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
      setError("A remote error occurred: " + exc.getMessage());
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
      setError("The file does not exist, or its data is corrupt.");
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
      setError("An error occurred: " + exc.getMessage());
    }
    finally {
      toggleWait();
    }
    if (data != null) {
      setData(data);
      Filename = u;
    }
    else setData(null);

    // update remote copies of Filename and RMIAddress
    synchFilename();
    synchRMIAddress();
  }

  /** import a data object from a given RMI address, and automatically
      update this cell whenever the remote data object changes */
  public void loadRMI(String s) throws VisADException, RemoteException {
    // example of RMI address: rmi://www.myaddress.com/MyServer/A1
    if (s == null) return;
    if (!s.startsWith("rmi://")) {
      throw new VisADException("RMI address must begin with \"rmi://\"");
    }

    if (!IsRemote) {
      clearDisplay();
      setFormula(null);
      Filename = null;
      RMIAddress = null;
      toggleWait();

      try {
        int len = s.length();
        int end = s.lastIndexOf("/");
        if (end < 6) end = len;
        String server = s.substring(4, end);
        String object = (end < len - 1) ? s.substring(end + 1, len) : "";
        RemoteServer rs = null;
        rs = (RemoteServer) Naming.lookup(server);
        RemoteDataReference ref = rs.getDataReference(object);
        if (ref == null) {
          throw new VisADException("The remote object called " +
                                   "\"" + object + "\" does not exist");
        }
        final RemoteDataReference rref = ref;
        final BasicSSCell cell = this;
        CellImpl lcell = new CellImpl() {
          public void doAction() {
            // update local data when remote data changes
            try {
              cell.setData(rref.getData().local());
            }
            catch (NullPointerException exc) {
              if (DEBUG) exc.printStackTrace();
              setError("Remote data is null");
            }
            catch (VisADException exc) {
              if (DEBUG) exc.printStackTrace();
              setError("Could not update remote data");
            }
            catch (RemoteException exc) {
              if (DEBUG) exc.printStackTrace();
              setError("Unable to import updated remote data");
            }
          }
        };
        RemoteCellImpl rcell = new RemoteCellImpl(lcell);
        rcell.addReference(ref);
      }
      catch (ClassCastException exc) {
        if (DEBUG) exc.printStackTrace();
        setError("The name of the RMI server is not valid.");
      }
      catch (MalformedURLException exc) {
        if (DEBUG) exc.printStackTrace();
        setError("The name of the RMI server is not valid.");
      }
      catch (NotBoundException exc) {
        if (DEBUG) exc.printStackTrace();
        setError("The remote data specified does not exist.");
      }
      catch (AccessException exc) {
        if (DEBUG) exc.printStackTrace();
        setError("Could not gain access to the remote data.");
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
        setError("Could not connect to the RMI server.");
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
        setError("An error occurred: " + exc.getMessage());
      }
      finally {
        toggleWait();
      }
      // update remote copy of Filename
      synchFilename();
    }
    RMIAddress = s;

    // update remote copy of RMIAddress
    synchRMIAddress();
  }

  /** @deprecated use saveData(File, Form) instead */
  public void saveData(File f, boolean netcdf) throws BadFormException,
                                                      IOException,
                                                      VisADException,
                                                      RemoteException {
    Form form;
    if (netcdf) form = new visad.data.netcdf.Plain();
    else form = new visad.data.visad.VisADForm();
    saveData(f, form);
  }

  /** export a data object to a given file name, using the given Data form */
  public void saveData(File f, Form form) throws BadFormException,
                                                 IOException,
                                                 VisADException,
                                                 RemoteException {
    Data d = getData();
    if (f == null || d == null) return;
    Saving++;
    try {
      form.save(f.getPath(), d, true);
    }
    finally {
      Saving--;
    }
  }

  /** return the data reference of this cell */
  public DataReference getReference() {
    if (IsRemote) {
      Vector v = VDisplay.getLinks();
      if (v == null || v.isEmpty()) return null;
      DataDisplayLink ddli = (DataDisplayLink) v.elementAt(0);
      ThingReference tr = ddli.getThingReference();
      return (tr instanceof DataReference ? (DataReference) tr : null);
    }
    else return DataRef;
  }

  /** return the data of this cell */
  public Data getData() {
    DataReference dr = getReference();
    if (dr == null) return null;
    try {
      return dr.getData();
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    return null;
  }

  /** whether the cell has data */
  public boolean hasData() {
    return getData() != null;
  }

  /** whether the cell has a formula */
  public boolean hasFormula() {
    return !Formula.equals("");
  }

  /** whether the cell has a valid display on-screen */
  public boolean hasDisplay() {
    return HasDisplay;
  }

  /** whether the cell has any mappings */
  public boolean hasMappings() {
    if (IsRemote) {
      Vector v = null;
      try {
        v = RemoteVDisplay.getMapVector();
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      return v != null && !v.isEmpty();
    }
    else return HasMappings;
  }

  /** @deprecated use addVar(String, ThingReference) instead */
  public static void createVar(String name, ThingReference tr)
    throws VisADException
  {
    defaultFM.createVar(name, tr);
  }

  /** add a variable to this cell's formula manager */
  public void addVar(String name, ThingReference tr) throws VisADException {
    fm.createVar(name, tr);
  }

  /** reconstruct this cell's display; called when dimension changes */
  public boolean constructDisplay() {
    boolean success = true;
    if (IsRemote) {
      if (Dim == JAVA2D_2D) {
        try {
          VDisplay = new DisplayImplJ2D(RemoteVDisplay);
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
          success = false;
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
          success = false;
        }
      }
      else {
        try {
          if (Dim == JAVA3D_3D) {
            VDisplay = new DisplayImplJ3D(RemoteVDisplay);
          }
          else { // Dim == JAVA3D_2D
            TwoDDisplayRendererJ3D tdr = new TwoDDisplayRendererJ3D();
            VDisplay = new DisplayImplJ3D(RemoteVDisplay, tdr);
          }
        }
        catch (NoClassDefFoundError err) {
          if (DEBUG) err.printStackTrace();
          success = false;
        }
        catch (UnsatisfiedLinkError err) {
          if (DEBUG) err.printStackTrace();
          success = false;
        }
        catch (Exception exc) {
          if (DEBUG) exc.printStackTrace();
          success = false;
        }
      }
    }
    else {
      try {
        if (Dim == JAVA3D_3D) VDisplay = new DisplayImplJ3D(Name);
        else if (Dim == JAVA2D_2D) VDisplay = new DisplayImplJ2D(Name);
        else { // Dim == JAVA3D_2D
          TwoDDisplayRendererJ3D tdr = new TwoDDisplayRendererJ3D();
          VDisplay = new DisplayImplJ3D(Name, tdr);
        }
        RemoteVDisplay = new RemoteDisplayImpl(VDisplay);
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
        success = false;
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
        success = false;
      }
    }
    return success;
  }

}

