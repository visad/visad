//
// BasicSSCell.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

import com.sun.image.codec.jpeg.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.util.*;
import javax.swing.*;
import visad.*;
import visad.bom.ImageRendererJ3D;
import visad.data.*;
import visad.data.netcdf.Plain;
import visad.data.visad.VisADForm;
import visad.formula.*;
import visad.java2d.*;
import visad.java3d.*;
import visad.util.DataUtility;

/** BasicSSCell represents a single spreadsheet display cell. BasicSSCells
    can be added to a VisAD user interface to provide some of the capabilities
    presented in the VisAD SpreadSheet program. Other capabilities, like the
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


  /** string code for text object that marks null data for use with RMI */
  private static final String NULL_DATA = "NULL_DATA";


  /** default FormulaManager object used by BasicSSCells */
  protected static final FormulaManager defaultFM =
    FormulaUtil.createStandardManager();

  /** list of SSCells on this JVM */
  protected static final Vector SSCellVector = new Vector();

  /** counter for the number of cells currently saving data */
  protected static int Saving = 0;

  /** whether Java3D is possible for this JVM */
  protected static boolean Possible3D;

  /** whether Java3D is enabled for this JVM */
  protected static boolean CanDo3D = enable3D();

  /** name of this BasicSSCell */
  protected String Name;

  /** formula manager for this BasicSSCell */
  protected FormulaManager fm;

  /** associated VisAD Display component */
  protected Component VDPanel;

  /** associated VisAD Display */
  protected DisplayImpl VDisplay;

  /** associated VisAD RemoteDisplay */
  protected RemoteDisplay RemoteVDisplay = null;

  /** associated VisAD RemoteSlaveDisplay, if any */
  protected RemoteSlaveDisplayImpl RemoteVSlave = null;

  /** associated VisAD RemoteServer */
  protected RemoteServer RemoteVServer = null;

  /** associated VisAD DataReference */
  protected DataReferenceImpl DataRef;

  /** associated VisAD RemoteDataReference */
  protected RemoteDataReferenceImpl RemoteDataRef;


  /** URL from where data was imported, if any */
  protected String Filename = null;

  /** whether the cell's file is considered &quot;remote data&quot; */
  protected boolean FileIsRemote = false;

  /** whether the remote data change detection cell has been set up yet */
  protected boolean setupComplete = false;

  /** RMI address from where data was imported, if any */
  protected String RMIAddress = null;

  /** formula of this BasicSSCell, if any */
  protected String Formula = "";

  /** whether the DisplayPanel is 2-D or 3-D, Java2D or Java3D */
  protected int Dim = -1;

  /** errors currently being displayed in this cell, if any */
  protected String[] Errors;

  /** string representation of this cell's mappings,
      for use only with remote clones */
  protected String Maps;


  /** list of servers to which this cell has been added */
  protected Vector Servers = new Vector();

  /** whether this display is remote */
  protected boolean IsRemote;

  /** whether this display is slaved */
  protected boolean IsSlave;

  /** ID number for this collaborative cell */
  protected double CollabID = 0.0;


  /** remote clone's copy of Filename */
  protected RemoteDataReference RemoteFilename;

  /** remote clone's copy of RMIAddress */
  protected RemoteDataReference RemoteRMIAddress;

  /** remote clone's copy of Formula */
  protected RemoteDataReference RemoteFormula;

  /** remote clone's copy of Dim */
  protected RemoteDataReference RemoteDim;

  /** remote clone's copy of Errors */
  protected RemoteDataReference RemoteErrors;

  /** remote clone's copy of Maps */
  protected RemoteDataReference RemoteMaps;

  /** data that is local to a remote clone */
  protected RemoteDataReference RemoteLoadedData;


  /** this BasicSSCell's DisplayListeners */
  protected Vector DListen = new Vector();

  /** whether the BasicSSCell has a valid display on-screen */
  protected boolean HasDisplay = false;

  /** whether the BasicSSCell has mappings from Data to Display */
  protected boolean HasMappings = false;

  /** prevent simultaneous GUI manipulation */
  protected Object Lock = new Object();

  /** construct a new BasicSSCell with the given name */
  public BasicSSCell(String name) throws VisADException, RemoteException {
    this(name, null, null, false, null);
  }

  /** construct a new BasicSSCell with the given name and non-default
      formula manager, to allow for custom formulas */
  public BasicSSCell(String name, FormulaManager fman)
    throws VisADException, RemoteException
  {
    this(name, fman, null, false, null);
  }

  /** construct a new BasicSSCell with the given name, that gets its
      information from the given RemoteServer. The associated SSCell on the
      server end must have already invoked its addToRemoteServer method */
  public BasicSSCell(String name, RemoteServer rs)
    throws VisADException, RemoteException
  {
    this(name, null, rs, false, null);
  }

  /** construct a new BasicSSCell with the given name and save string, used to
      reconstruct the cell's configuration */
  public BasicSSCell(String name, String save)
    throws VisADException, RemoteException
  {
    this(name, null, null, false, save);
  }

  /** construct a new BasicSSCell with the given name, formula manager, and
      remote server */
  public BasicSSCell(String name, FormulaManager fman, RemoteServer rs,
    String save) throws VisADException, RemoteException
  {
    this(name, fman, rs, false, save);
  }

  /** construct a new, possibly slaved, BasicSSCell with the given name,
      formula manager, and remote server */
  public BasicSSCell(String name, FormulaManager fman, RemoteServer rs,
    boolean slave, String save) throws VisADException, RemoteException
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
    DataReferenceImpl drMap = null;
    DataReferenceImpl drLoad = null;

    if (IsRemote) {
      // Note: Servers have an ID of zero.
      // Each client has a random ID number between 1 and Integer.MAX_VALUE.
      // There really should be a way for clients to ensure that they don't
      // choose an ID number already taken by another client.
      CollabID = (double) (new Random().nextInt(Integer.MAX_VALUE - 1) + 1);
      RemoteFilename = rs.getDataReference(name + "_Filename");
      RemoteRMIAddress = rs.getDataReference(name + "_RMIAddress");
      RemoteFormula = rs.getDataReference(name + "_Formula");
      RemoteDim = rs.getDataReference(name + "_Dim");
      RemoteErrors = rs.getDataReference(name + "_Errors");
      RemoteMaps = rs.getDataReference(name + "_Maps");
      RemoteLoadedData = rs.getDataReference(name + "_Loaded");
      IsSlave = slave;
      setDimClone();

      addDisplayListener(new DisplayListener() {
        public void displayChanged(DisplayEvent e) {
          int id = e.getId();
          if (id == DisplayEvent.TRANSFORM_DONE ||
            (id == DisplayEvent.FRAME_DONE && IsSlave && !hasDisplay()))
          {
            if (!setupComplete && !IsSlave) setupRemoteDataChangeCell();
            if (!hasDisplay()) {
              constructDisplay();
              initVDPanel();
              setVDPanel(true);
            }
            // display has changed; notify listeners
            notifyListeners(SSCellChangeEvent.DISPLAY_CHANGE);
          }
          else if (id == DisplayEvent.MAPS_CLEARED) setVDPanel(false);
        }
      });
    }
    else {
      // redisplay this cell's data when it changes
      CellImpl ucell = new CellImpl() {
        public void doAction() {
          // clear old errors
          setErrors(null);

          // get new data
          Data value = null;
          try {
            value = (Data) fm.getThing(BasicSSCell.this.Name);
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
          String[] es = fm.getErrors(BasicSSCell.this.Name);
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

      // set up objects for sending data from client to server
      drMap = new DataReferenceImpl(Name + "_Maps");
      RemoteMaps = new RemoteDataReferenceImpl(drMap);
      drLoad = new DataReferenceImpl(Name + "_Loaded");
      RemoteLoadedData = new RemoteDataReferenceImpl(drLoad);

      // default to two dimensions with Java2D
      setDimension(JAVA2D_2D);
    }

    // update cell when remote filename changes
    CellImpl lFilenameCell = new CellImpl() {
      public void doAction() {
        try {
          TupleIface t = (TupleIface) RemoteFilename.getData();
          Real id = (Real) t.getComponent(0);
          FileIsRemote = (id.getValue() != CollabID);
          if (FileIsRemote) {
            // act on filename update from remote cell
            Text nFile = (Text) t.getComponent(1);
            String s = nFile.getValue();
            Filename = s.equals("") ? null : s;
          }
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
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
          TupleIface t = (TupleIface) RemoteRMIAddress.getData();
          Real id = (Real) t.getComponent(0);
          if (id.getValue() == CollabID) {
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
          TupleIface t = (TupleIface) RemoteFormula.getData();
          Real id = (Real) t.getComponent(0);
          if (id.getValue() == CollabID) {
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
          TupleIface t = (TupleIface) RemoteDim.getData();
          Real id = (Real) t.getComponent(0);
          if (id.getValue() == CollabID) {
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
          TupleIface t = (TupleIface) RemoteErrors.getData();
          Real id = (Real) t.getComponent(0);
          if (id.getValue() == CollabID) {
            // cells should ignore their own updates
            return;
          }
          Data d = t.getComponent(1);
          String[] newErrors;
          if (d instanceof TupleIface) {
            TupleIface nErr = (TupleIface) d;
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

    if (!IsRemote) {
      // server can receive a remote mappings update from any client;
      // clients simply receive mappings updates through the RemoteDisplay
      CellImpl lMapsCell = new CellImpl() {
        public void doAction() {
          try {
            Text t = (Text) RemoteMaps.getData();
            if (t != null) {
              String s = t.getValue();
              if (s != null) {
                ScalarMap[] maps = convertStringToMaps(s, false);
                if (maps != null) setMaps(maps);
              }
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
        RemoteCellImpl rMapsCell = new RemoteCellImpl(lMapsCell);
        rMapsCell.addReference(RemoteMaps);
      }
      catch (RemoteException exc) {
        lMapsCell.addReference(drMap);
      }
      // server can receive a remote data object update from any client;
      // clients simply receive data object updates through the RemoteDisplay
      CellImpl lLoadedCell = new CellImpl() {
        public void doAction() {
          try {
            Data d = RemoteLoadedData.getData();
            if (d != null) d = d.local();
            if (d instanceof Text &&
              ((Text) d).getValue().equals(NULL_DATA))
            {
              // client has sent a tag that marks null data
              setData(null);
            }
            else setData(d);
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
        RemoteCellImpl rLoadedCell = new RemoteCellImpl(lLoadedCell);
        rLoadedCell.addReference(RemoteLoadedData);
      }
      catch (RemoteException exc) {
        lLoadedCell.addReference(drLoad);
      }
    }

    // setup save string
    if (save != null) setSaveString(save);

    // finish GUI setup
    initVDPanel();
    setPreferredSize(new Dimension(0, 0));
    setBackground(IsSlave ? Color.darkGray : Color.black);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
  }

  private void setupRemoteDataChangeCell() {
    DataReference dr = getReference();
    if (dr != null) {
      // use a VisAD Cell to listen for data changes
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
      Real id = new Real(CollabID);
      Text nFile = new Text(Filename == null ? "" : Filename);
      TupleIface t = new Tuple(new Data[] {id, nFile}, false);
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
      Real id = new Real(CollabID);
      Text nRMI = new Text(RMIAddress == null ? "" : RMIAddress);
      TupleIface t = new Tuple(new Data[] {id, nRMI}, false);
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
      Real id = new Real(CollabID);
      Text nForm = new Text(Formula);
      TupleIface t = new Tuple(new Data[] {id, nForm}, false);
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
      Real id = new Real(CollabID);
      Real nDim = new Real(Dim);
      TupleIface t = new Tuple(new Data[] {id, nDim}, false);
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
      Real id = new Real(CollabID);
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
      TupleIface t = new Tuple(new Data[] {id, nErrors}, false);
      RemoteErrors.setData(t);
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  private void synchMaps(ScalarMap[] maps) {
    try {
      Text t = new Text(convertMapsToString(maps));
      RemoteMaps.setData(t);
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
  protected void setVDPanel(boolean value) {
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
  protected void setError(String msg) {
    String[] s = (msg == null ? null : new String[] {msg});
    setErrors(s);
  }

  /** display errors in this BasicSSCell, or setErrors(null) for no errors */
  protected void setErrors(String[] msg) {
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
        rs.addDataReference((RemoteDataReferenceImpl) RemoteMaps);
        rs.addDataReference((RemoteDataReferenceImpl) RemoteLoadedData);
        Servers.add(rs);
      }
    }
  }

  /** remove this SSCell from the given RemoteServer */
  public void removeFromRemoteServer(RemoteServerImpl rs)
    throws RemoteException
  {
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
        rs.removeDataReference((RemoteDataReferenceImpl) RemoteMaps);
        rs.removeDataReference((RemoteDataReferenceImpl) RemoteLoadedData);
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

  /** return the BasicSSCell object with the specified name */
  public static BasicSSCell getSSCellByName(String name) {
    Enumeration panels = SSCellVector.elements();
    while (panels.hasMoreElements()) {
      BasicSSCell panel = (BasicSSCell) panels.nextElement();
      if (name.equalsIgnoreCase(panel.Name)) return panel;
    }
    return null;
  }

  /**
   * Obtains a Vector consisting of all ScalarTypes present in the Data's
   * MathType.
   * @param data                The Data from which to extract the ScalarTypes.
   * @param v                   The Vector in which to store the ScalarTypes.
   * @throws VisADException     Couldn't parse the Data's MathType.
   * @throws RemoteException    Couldn't obtain the remote Data's MathType.
   * @return                    The number of duplicate ScalarTypes found.
   * @deprecated Use visad.util.DataUtility.getRealTypes() instead.
   */
  static int getRealTypes(Data data, Vector v) {
    try {
      return DataUtility.getRealTypes(data, v);
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    return -1;
  }

  /** converts the given vector of mappings to an easy-to-read String form */
  private static String convertMapsToString(Vector v) {
    int len = v.size();
    ScalarMap[] sm = new ScalarMap[len];
    for (int i=0; i<len; i++) sm[i] = (ScalarMap) v.elementAt(i);
    return convertMapsToString(sm);
  }

  /** converts the given array of mappings to an easy-to-read String form */
  private static String convertMapsToString(ScalarMap[] sm) {
    StringBuffer sb = new StringBuffer(128);
    for (int i=0; i<sm.length; i++) {
      ScalarMap m = sm[i];
      ScalarType domain = m.getScalar();
      DisplayRealType range = m.getDisplayScalar();
      int q = -1;
      for (int j=0; j<Display.DisplayRealArray.length; j++) {
        if (range.equals(Display.DisplayRealArray[j])) q = j;
      }
      sb.append(' ');
      sb.append(domain.getName());
      sb.append(' ');
      sb.append(q);
    }
    return sb.toString();
  }

  /** converts the given map string to its corresponding array of mappings */
  private ScalarMap[] convertStringToMaps(
    String mapString, boolean showErrors)
  {
    // extract mapping information from string
    if (DEBUG) showErrors = true;
    if (mapString == null) return null;
    StringTokenizer st = new StringTokenizer(mapString);
    Vector dnames = new Vector();
    Vector rnames = new Vector();
    while (true) {
      if (!st.hasMoreTokens()) break;
      String s = st.nextToken();
      if (!st.hasMoreTokens()) {
        if (showErrors) {
          System.err.println("Warning: trailing maps value " + s +
            " has no corresponding number and will be ignored");
        }
        continue;
      }
      String si = st.nextToken();
      Integer i = null;
      try {
        i = new Integer(Integer.parseInt(si));
      }
      catch (NumberFormatException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      if (i == null) {
        if (showErrors) {
          System.err.println("Warning: maps value " + si + " is not a " +
            "valid integer and the maps pair (" + s + ", " + si + ") " +
            "will be ignored");
        }
      }
      else {
        dnames.add(s);
        rnames.add(i);
      }
    }

    // set up mappings
    if (dnames != null) {
      int len = dnames.size();
      if (len > 0) {
        // get Vector of all ScalarTypes in this data object
        Vector types = new Vector();
        Data data = getData();
        if (data != null) {
          try {
            DataUtility.getRealTypes(data, types);
          }
          catch (VisADException exc) {
            if (DEBUG) exc.printStackTrace();
          }
          catch (RemoteException exc) {
            if (DEBUG) exc.printStackTrace();
          }
        }
        int vLen = types.size();
        int dLen = Display.DisplayRealArray.length;

        // construct ScalarMaps
        ScalarMap[] maps = new ScalarMap[len];
        for (int j=0; j<len; j++) {
          // find appropriate ScalarType
          ScalarType mapDomain = null;
          String name = (String) dnames.elementAt(j);
          for (int k=0; k<vLen; k++) {
            ScalarType type = (ScalarType) types.elementAt(k);
            if (name.equals(type.getName())) {
              mapDomain = type;
              break;
            }
          }
          if (mapDomain == null) {
            // still haven't found type; look in static Vector for it
            mapDomain = ScalarType.getScalarTypeByName(name);
          }

          // find appropriate DisplayRealType
          int q = ((Integer) rnames.elementAt(j)).intValue();
          DisplayRealType mapRange = null;
          if (q >= 0 && q < dLen) mapRange = Display.DisplayRealArray[q];

          // construct mapping
          if (mapDomain == null || mapRange == null) {
            if (showErrors) {
              System.err.print("Warning: maps pair (" + name + ", " +
                q + ") has an invalid ");
              if (mapDomain == null && mapRange == null) {
                System.err.print("domain and range");
              }
              else if (mapDomain == null) System.err.print("domain");
              else System.err.print("range");
              System.err.println(" and will be ignored");
            }
            maps[j] = null;
          }
          else {
            try {
              maps[j] = new ScalarMap(mapDomain, mapRange);
            }
            catch (VisADException exc) {
              if (showErrors) {
                System.err.println("Warning: maps pair (" + name + ", " +
                  q + ") cannot be converted to a ScalarMap");
              }
              maps[j] = null;
            }
          }
        }
        return maps;
      }
    }

    return null;
  }

  /** return true if any BasicSSCell is currently saving data */
  public static boolean isSaving() {
    return Saving > 0;
  }

  /** return true if Java3D is possible for this JVM */
  public static boolean possible3D() {
    return Possible3D;
  }

  /** return true if Java3D is enabled for this JVM */
  public static boolean canDo3D() {
    return CanDo3D;
  }

  /** attempt to enable Java3D for this JVM, returning true if successful */
  public static boolean enable3D() {
    // test for Java3D availability
    Possible3D = false;
    CanDo3D = false;
    try {
      DisplayImplJ3D test = new DisplayImplJ3D("test");
      Possible3D = true;
      CanDo3D = true;
    }
    catch (NoClassDefFoundError err) {
      if (DEBUG) err.printStackTrace();
    }
    catch (UnsatisfiedLinkError err) {
      if (DEBUG) System.err.println("Warning: Java3D library not found");
    }
    catch (Exception exc) {
      if (DEBUG) exc.printStackTrace();
    }
    return CanDo3D;
  }

  /** disable Java3D for this JVM */
  public static void disable3D() {
    CanDo3D = false;
  }

  /** @deprecated Use setSaveString(String) instead. */
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
    String mapString = null;
    ScalarMap[] maps = null;
    Vector mapMins = null;
    Vector mapMaxs = null;
    String proj = null;
    String mode = null;
    Vector color = new Vector();
    Vector contour = new Vector();
    Vector range = new Vector();
    Vector anim = new Vector();
    Vector value = new Vector();

    // make sure cell is not remote
    if (IsRemote) {
      throw new VisADException("Cannot setSaveString on a remote cell");
    }

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
      String surplus = line.substring(eq + 1).trim();
      String nextLine = tokens[tokenNum];
      if (nextLine != null && nextLine.indexOf('=') < 0) {
        surplus = surplus + '\n';
      }
      while (nextLine != null && nextLine.indexOf('=') < 0) {
        if (nextLine.length() > 0) surplus = surplus + nextLine + '\n';
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
        catch (NumberFormatException exc) {
          if (DEBUG) exc.printStackTrace();
        }
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
        mapString = surplus;
      }

      // mapping ranges
      else if (keyword.equalsIgnoreCase("map ranges") ||
        keyword.equalsIgnoreCase("map_ranges") ||
        keyword.equalsIgnoreCase("mapranges"))
      {
        st = new StringTokenizer(surplus);
        mapMins = new Vector();
        mapMaxs = new Vector();
        while (true) {
          if (!st.hasMoreTokens()) break;
          String s1 = st.nextToken();
          if (!st.hasMoreTokens()) {
            System.err.println("Warning: trailing map range min value " +
              s1 + " has no corresponding max value and will be ignored");
            break;
          }
          String s2 = st.nextToken();
          Double d1 = null, d2 = null;
          try {
            d1 = new Double(Double.parseDouble(s1));
            d2 = new Double(Double.parseDouble(s2));
          }
          catch (NumberFormatException exc) {
            if (DEBUG) exc.printStackTrace();
          }
          if (d1 == null || d2 == null) {
            System.err.println("Warning: map range min/max pair (" +
              s1 + ", " + s2 + ") is not valid and will be ignored");
          }
          else {
            mapMins.add(d1);
            mapMaxs.add(d2);
          }
        }
      }

      // projection matrix
      else if (keyword.equalsIgnoreCase("projection") ||
        keyword.equalsIgnoreCase("proj"))
      {
        proj = surplus;
      }

      // graphics mode settings
      else if (keyword.equalsIgnoreCase("graphics mode") ||
        keyword.equalsIgnoreCase("graphics_mode") ||
        keyword.equalsIgnoreCase("graphicsmode") ||
        keyword.equalsIgnoreCase("graphics") ||
        keyword.equalsIgnoreCase("mode"))
      {
        mode = surplus;
      }

      // color table
      else if (keyword.equalsIgnoreCase("color") ||
        keyword.equalsIgnoreCase("color table") ||
        keyword.equalsIgnoreCase("color_table") ||
        keyword.equalsIgnoreCase("colortable"))
      {
        color.add(surplus);
      }

      // contour data
      else if (keyword.equalsIgnoreCase("contour") ||
        keyword.equalsIgnoreCase("contours") ||
        keyword.equalsIgnoreCase("iso contour") ||
        keyword.equalsIgnoreCase("iso_contour") ||
        keyword.equalsIgnoreCase("iso-contour") ||
        keyword.equalsIgnoreCase("isocontour") ||
        keyword.equalsIgnoreCase("iso contours") ||
        keyword.equalsIgnoreCase("iso_contours") ||
        keyword.equalsIgnoreCase("iso-contours") ||
        keyword.equalsIgnoreCase("isocontours"))
      {
        contour.add(surplus);
      }

      // range
      else if (keyword.equalsIgnoreCase("range") ||
        keyword.equalsIgnoreCase("select range") ||
        keyword.equalsIgnoreCase("select_range") ||
        keyword.equalsIgnoreCase("select-range"))
      {
        range.add(surplus);
      }

      // animation
      else if (keyword.equalsIgnoreCase("anim") ||
        keyword.equalsIgnoreCase("animation"))
      {
        anim.add(surplus);
      }

      // value
      else if (keyword.equalsIgnoreCase("value") ||
        keyword.equalsIgnoreCase("select value") ||
        keyword.equalsIgnoreCase("select_value") ||
        keyword.equalsIgnoreCase("selectvalue"))
      {
        value.add(surplus);
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
    if (formula != null && !formula.equals("")) {
      setFormula(formula);
      waitForFormula();
    }

    // set up map ranges; then set maps
    maps = convertStringToMaps(mapString, true);
    if (maps != null) {
      int lmin = mapMins == null ? -1 : mapMins.size();
      int lmax = mapMaxs == null ? -1 : mapMaxs.size();
      int cmin = 0, cmax = 0;
      for (int j=0; j<maps.length; j++) {
        if (maps[j] != null) {
          // set map's minimum and maximum range value, if applicable
          ScalarMap sm = maps[j];
          boolean scale = sm.getScale(
            new double[2], new double[2], new double[2]);
          if (scale && cmin < lmin && cmax < lmax) {
            sm.setRange(((Double) mapMins.elementAt(cmin++)).doubleValue(),
              ((Double) mapMaxs.elementAt(cmax++)).doubleValue());
          }
        }
      }
      setMaps(maps);
    }

    // set up projection control
    if (proj != null) {
      ProjectionControl pc = VDisplay.getProjectionControl();
      if (pc != null) pc.setSaveString(proj);
      else System.err.println("Warning: display has no ProjectionControl; " +
        "the provided projection matrix will be ignored");
    }

    // set up graphics mode control
    if (mode != null) {
      GraphicsModeControl gmc = VDisplay.getGraphicsModeControl();
      if (gmc != null) gmc.setSaveString(mode);
      else System.err.println("Warning: display has no GraphicsModeControl; " +
        "the provided graphics mode settings will be ignored");
    }

    // set up color control(s)
    int len = color.size();
    if (len > 0) {
      for (int i=0; i<len; i++) {
        String s = (String) color.elementAt(i);
        ColorControl cc = (ColorControl)
          VDisplay.getControl(ColorControl.class, i);
        if (cc != null) cc.setSaveString(s);
        else System.err.println("Warning: display has no ColorControl #" +
          (i + 1) + "; the provided color table will be ignored");
      }
    }

    // set up contour control(s)
    len = contour.size();
    if (len > 0) {
      for (int i=0; i<len; i++) {
        String s = (String) contour.elementAt(i);
        ContourControl cc = (ContourControl)
          VDisplay.getControl(ContourControl.class, i);
        if (cc != null) cc.setSaveString(s);
        else System.err.println("Warning: display has no ContourControl #" +
          (i + 1) + "; the provided contour settings will be ignored");
      }
    }

    // set up range control(s)
    len = range.size();
    if (len > 0) {
      for (int i=0; i<len; i++) {
        String s = (String) range.elementAt(i);
        RangeControl rc = (RangeControl)
          VDisplay.getControl(RangeControl.class, i);
        if (rc != null) rc.setSaveString(s);
        else System.err.println("Warning: display has no RangeControl #" +
          (i + 1) + "; the provided range will be ignored");
      }
    }

    // set up animation control(s)
    len = anim.size();
    if (len > 0) {
      for (int i=0; i<len; i++) {
        String s = (String) anim.elementAt(i);
        AnimationControl ac = (AnimationControl)
          VDisplay.getControl(AnimationControl.class, i);
        if (ac != null) {
          // Note: There is a race condition that prevents the AnimationControl
          // from correctly setting the current step and step delays.
          // The AnimationControl gets its parameters reset back to default
          // values when its ScalarMap's range is set above.
          // The one-second delay here should solve the problem in most cases.
          try {
            Thread.sleep(1000);
          }
          catch (InterruptedException exc) { }
          ac.setSaveString(s);
        }
        else System.err.println("Warning: display has no AnimationControl #" +
          (i + 1) + "; the provided animation settings will be ignored");
      }
    }

    // set up value control(s)
    len = value.size();
    if (len > 0) {
      for (int i=0; i<len; i++) {
        String s = (String) value.elementAt(i);
        ValueControl vc = (ValueControl)
          VDisplay.getControl(ValueControl.class, i);
        if (vc != null) vc.setSaveString(s);
        else System.err.println("Warning: display has no ValueControl #" +
          (i + 1) + "; the provided value will be ignored");
      }
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
      StringBuffer sb = new StringBuffer(1024);

      if (Filename != null) {
        // add filename to save string
        sb.append("filename = ");
        sb.append(Filename);
        sb.append('\n');
      }

      if (RMIAddress != null) {
        // add rmi address to save string
        sb.append("rmi = ");
        sb.append(RMIAddress);
        sb.append('\n');
      }

      if (!Formula.equals("")) {
        // add formula to save string
        sb.append("formula = ");
        sb.append(Formula);
        sb.append('\n');
      }

      // add dimension to save string
      sb.append("dim = ");
      sb.append(Dim);
      sb.append('\n');

      if (hasMappings()) {
        Vector mapVector = VDisplay.getMapVector();
        int mvs = mapVector.size();
        if (mvs > 0) {
          // add mappings to save string
          sb.append("maps =");
          sb.append(convertMapsToString(mapVector));
          sb.append('\n');

          // add map ranges to save string
          sb.append("map ranges =");
          for (int i=0; i<mvs; i++) {
            ScalarMap m = (ScalarMap) mapVector.elementAt(i);
            double[] range = new double[2];
            boolean scale = m.getScale(new double[2], range, new double[2]);
            if (scale) {
              sb.append(' ');
              sb.append(range[0]);
              sb.append(' ');
              sb.append(range[1]);
            }
          }
          sb.append('\n');
        }
      }

      if (hasDisplay()) {
        // add projection control state to save string
        ProjectionControl pc = VDisplay.getProjectionControl();
        if (pc != null) {
          sb.append("projection = ");
          sb.append(pc.getSaveString());
        }

        // add graphics mode control settings to save string
        GraphicsModeControl gmc = VDisplay.getGraphicsModeControl();
        if (gmc != null) {
          sb.append("graphics mode = ");
          sb.append(gmc.getSaveString());
          sb.append('\n');
        }

        // add color control state(s) to save string
        Vector cv = VDisplay.getControls(ColorControl.class);
        if (cv != null) {
          for (int i=0; i<cv.size(); i++) {
            ColorControl cc = (ColorControl) cv.elementAt(i);
            if (cc != null) {
              sb.append("color = ");
              sb.append(cc.getSaveString());
            }
          }
        }

        // add contour control state(s) to save string
        cv = VDisplay.getControls(ContourControl.class);
        if (cv != null) {
          for (int i=0; i<cv.size(); i++) {
            ContourControl cc = (ContourControl) cv.elementAt(i);
            if (cc != null) {
              sb.append("contour = ");
              sb.append(cc.getSaveString());
              sb.append('\n');
            }
          }
        }

        // add range control state(s) to save string
        cv = VDisplay.getControls(RangeControl.class);
        if (cv != null) {
          for (int i=0; i<cv.size(); i++) {
            RangeControl rc = (RangeControl) cv.elementAt(i);
            if (rc != null) {
              sb.append("range = ");
              sb.append(rc.getSaveString());
              sb.append('\n');
            }
          }
        }

        // add animation control state(s) to save string
        cv = VDisplay.getControls(AnimationControl.class);
        if (cv != null) {
          for (int i=0; i<cv.size(); i++) {
            AnimationControl ac = (AnimationControl) cv.elementAt(i);
            if (ac != null) {
              sb.append("anim = ");
              sb.append(ac.getSaveString());
              sb.append('\n');
            }
          }
        }

        // add value control state(s) to save string
        cv = VDisplay.getControls(ValueControl.class);
        if (cv != null) {
          for (int i=0; i<cv.size(); i++) {
            ValueControl vc = (ValueControl) cv.elementAt(i);
            if (vc != null) {
              sb.append("value = ");
              sb.append(vc.getSaveString());
              sb.append('\n');
            }
          }
        }
      }
      return sb.toString();
    }
  }

  /** add a DisplayListener to this cell */
  public void addDisplayListener(DisplayListener d) {
    synchronized (DListen) {
      if (!DListen.contains(d)) {
        if (IsSlave) RemoteVSlave.addDisplayListener(d);
        else VDisplay.addDisplayListener(d);
        DListen.add(d);
      }
    }
  }

  /** remove a DisplayListener from this cell */
  public void removeDisplayListener(DisplayListener d) {
    synchronized (DListen) {
      if (DListen.contains(d)) {
        if (IsSlave) RemoteVSlave.removeDisplayListener(d);
        else VDisplay.removeDisplayListener(d);
        DListen.remove(d);
      }
    }
  }

  /** re-attach all display listeners after they have been detached */
  private void attachDisplayListeners() {
    for (int i=0; i<DListen.size(); i++) {
      DisplayListener d = (DisplayListener) DListen.elementAt(i);
      if (IsSlave) RemoteVSlave.addDisplayListener(d);
      else VDisplay.addDisplayListener(d);
    }
  }

  /** temporarily detach all display listeners */
  private void detachDisplayListeners() {
    for (int i=0; i<DListen.size(); i++) {
      DisplayListener d = (DisplayListener) DListen.elementAt(i);
      if (IsSlave) RemoteVSlave.removeDisplayListener(d);
      else VDisplay.removeDisplayListener(d);
    }
  }

  /** map RealTypes to the display according to the specified ScalarMaps */
  public synchronized void setMaps(ScalarMap[] maps)
    throws VisADException, RemoteException
  {
    if (maps == null) return;

    VisADException vexc = null;
    RemoteException rexc = null;
    if (IsRemote) synchMaps(maps);
    else {
      DataReference dr = getReference();
      VDisplay.disableAction();
      clearMaps();
      for (int i=0; i<maps.length; i++) {
        if (maps[i] != null) {
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
      }
      MathType type = dr.getData().getType();
      try {
        ImageRendererJ3D.verifyImageRendererUsable(type, maps);
        VDisplay.addReferences(new ImageRendererJ3D(), dr);
      }
      catch (VisADException exc) {
        if (DEBUG) {
          System.err.println(Name + ": Warning: cannot use ImageRendererJ3D");
        }
        VDisplay.addReference(dr);
      }
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
    if (IsRemote) clearMapsClone(true);
    else if (hasMappings()) {
      VDisplay.removeReference(DataRef);
      VDisplay.clearMaps();
      HasMappings = false;
    }
  }

  private void clearMapsClone(boolean display)
    throws VisADException, RemoteException
  {
    if (hasMappings()) {
      RemoteVDisplay.removeAllReferences();
      RemoteVDisplay.clearMaps();
      if (display) {
        setVDPanel(false);
        constructDisplay();
        initVDPanel();
        setVDPanel(true);
      }
      HasMappings = false;
    }
  }

  /** clear this cell's display */
  public void clearDisplay() throws VisADException, RemoteException {
    if (IsRemote) clearMapsClone(false);
    else clearMaps();
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
    RemoteException problem = null;

    if (!IsRemote) {
      clearCell();

      // remove cell from all servers
      int slen = Servers.size();
      if (slen > 0) {
        for (int i=0; i<slen; i++) {
          RemoteServerImpl rs = (RemoteServerImpl) Servers.elementAt(i);
          try {
            removeFromRemoteServer(rs);
          }
          catch (RemoteException exc) {
            problem = exc;
          }
        }
      }

      // remove cell from formula manager database
      fm.remove(Name);
    }
    else if (IsSlave && RemoteVSlave != null) {
      // disconnect remote slave client cleanly
      try {
        RemoteVSlave.unlink();
      }
      catch (RemoteException exc) {
        problem = exc;
      }
    }

    // remove cell from static list
    SSCellVector.remove(this);

    if (problem != null) throw problem;
  }

  /** set this cell's Data to data */
  public void setData(Data data) throws VisADException, RemoteException {
    if (IsRemote) {
      // send local data to server
      if (data == null) data = new Text(NULL_DATA);
      RemoteLoadedData.setData(data);
    }
    else {
      fm.setThing(Name, data);

      if (data != null) {
        // add this Data's RealTypes to FormulaManager variable registry
        Vector v = new Vector();
        DataUtility.getRealTypes(data, v);
        int len = v.size();
        for (int i=0; i<len; i++) {
          RealType rt = (RealType) v.elementAt(i);
          fm.setThing(rt.getName(), new VRealType(rt));
        }
      }
    }
  }

  /** set the BasicSSCell to 2-D or 3-D display with Java2D or Java3D */
  public void setDimension(boolean twoD, boolean java2d)
    throws VisADException, RemoteException
  {
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
        detachDisplayListeners();

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
        initVDPanel();
        if (hasData()) setVDPanel(true);

        // put listeners back
        attachDisplayListeners();
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
  private static final String jir = "visad.bom.ImageRendererJ3D";

  /** update the dimension of a cloned cell to match that of the server */
  private void setDimClone() throws VisADException, RemoteException {
    synchronized (DListen) {
      // remove listeners temporarily
      detachDisplayListeners();

      // remove old display panel from cell
      setVDPanel(false);

      // get updated display from server
      RemoteVDisplay = RemoteVServer.getDisplay(Name);

      // update remote slave display
      if (IsSlave) {
        if (RemoteVSlave != null) RemoteVSlave.unlink();
        RemoteVSlave = new RemoteSlaveDisplayImpl(RemoteVDisplay);
      }

      // autodetect new dimension
      String s = RemoteVDisplay.getDisplayRendererClassName();
      if (s.equals(j33) || s.equals(jir)) Dim = JAVA3D_3D;
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
              g.drawString("This machine does not support Java3D.", 8, 20);
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
      initVDPanel();
      if (success && hasData()) setVDPanel(true);

      // put all listeners back
      attachDisplayListeners();
    }

    // broadcast dimension change event
    notifyListeners(SSCellChangeEvent.DIMENSION_CHANGE);
  }

  /** set the BasicSSCell's formula */
  public synchronized void setFormula(String f)
    throws VisADException, RemoteException
  {
    String nf = (f == null ? "" : f);
    if (Formula.equals(nf)) return;
    Formula = "";
    fm.assignFormula(Name, nf);
    Formula = nf;

    // update remote copy of Formula
    synchFormula();
  }

  /** blocks until this cell's formula is finished computing */
  public void waitForFormula() throws VisADException, RemoteException {
    fm.waitForFormula(Name);
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
    if (Filename == null) return null;
    try {
      return new URL(Filename);
    }
    catch (MalformedURLException exc) {
      if (DEBUG) exc.printStackTrace();
      return null;
    }
  }

  /** return the file name from which the associated Data came */
  public String getFilename() {
    if (Filename == null) return "";
    return (FileIsRemote ? Filename + " (remote)" : Filename);
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
    if (u == null) return;
    loadData(u.toString());
  }

  /** import a data object from the given location */
  public synchronized void loadData(String s)
    throws VisADException, RemoteException
  {
    if (s == null) return;

    clearDisplay();
    setFormula(null);
    Filename = null;
    RMIAddress = null;
    toggleWait();

    Data data = null;
    try {
      int len = s.length();
      boolean isFile = false;
      String location = null;

      // file detection --
      // necessary because some Data Forms lack open(URL) capability
      if (len >= 6 && s.substring(0, 6).equalsIgnoreCase("file:/")) {
        // location is a file:/ address
        isFile = true;
        location = s.substring(6);
      }

      // ADDE detection --
      // necessary because java.net.URL does not understand adde:// addresses
      else if (len > 7 && s.substring(0, 7).equalsIgnoreCase("adde://")) {
        // location is an adde:// address
        isFile = true;
        location = s;
      }

      // location is some other kind of URL
      else location = s;

      // load file
      DefaultFamily loader = new DefaultFamily("loader");
      if (isFile) data = loader.open(location);
      else data = loader.open(new URL(location));
    }
    catch (BadFormException exc) {
      if (DEBUG) exc.printStackTrace();
      setError("The file could not be converted to VisAD data.");
    }
    catch (MalformedURLException exc) {
      if (DEBUG) exc.printStackTrace();
      setError("The given URL is not valid.");
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
      Filename = s;
    }
    else setData(null);

    // update remote copies of Filename and RMIAddress
    synchFilename();
    synchRMIAddress();
  }

  /** import a data object from a given RMI address, and automatically
      update this cell whenever the remote data object changes */
  public synchronized void loadRMI(String s)
    throws VisADException, RemoteException
  {
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
        String object = (end < len - 1) ? s.substring(end + 1) : "";
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
  public void saveData(File f, boolean netcdf)
    throws BadFormException, IOException, VisADException, RemoteException
  {
    Form form;
    if (netcdf) form = new visad.data.netcdf.Plain();
    else form = new visad.data.visad.VisADForm();
    saveData(f, form);
  }

  /** export a data object to a given file name, using the given Data form */
  public void saveData(File f, Form form)
    throws BadFormException, IOException, VisADException, RemoteException
  {
    if (IsSlave) {
      throw new VisADException("Cannot saveData on a slaved cell");
    }
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

  /** capture image and save to a given file name, in JPEG format */
  public void captureImage(File f) throws VisADException, IOException {
    BufferedImage image =
      IsSlave ? RemoteVSlave.getImage() : VDisplay.getImage();
    try {
      JPEGEncodeParam param = JPEGCodec.getDefaultJPEGEncodeParam(image);
      param.setQuality(1.0f, true);
      FileOutputStream fout = new FileOutputStream(f);
      JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fout);
      encoder.encode(image, param);
      fout.close(); 
    }
    catch (NoClassDefFoundError err) {
      if (DEBUG) System.err.println("Warning: JPEG codec not found");
    }
  }

  /** return the data reference of this cell */
  public DataReference getReference() {
    if (IsRemote) {
      try {
        Vector v = RemoteVDisplay.getReferenceLinks();
        if (v == null || v.isEmpty()) return null;
        RemoteReferenceLink rrli = (RemoteReferenceLink) v.elementAt(0);
        RemoteDataReference rdr = rrli.getReference();
        return rdr;
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
        return null;
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
        return null;
      }
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

  /** initializes the cell's display panel */
  private void initVDPanel() {
    if (IsSlave) VDPanel = RemoteVSlave.getComponent();
    else VDPanel = VDisplay.getComponent();
  }

  /** reconstruct this cell's display; called when dimension changes */
  public boolean constructDisplay() {
    boolean success = true;
    DisplayImpl newDisplay = VDisplay;
    RemoteDisplay rmtDisplay = RemoteVDisplay;
    if (IsSlave) {
      try {
        newDisplay = new DisplayImplJ2D("DUMMY");
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
    else if (!CanDo3D && Dim != JAVA2D_2D) {
      // dimension requires Java3D, but Java3D is disabled for this JVM
      success = false;
    }
    else if (IsRemote) {
      if (Dim == JAVA2D_2D) {
        try {
          newDisplay = new DisplayImplJ2D(rmtDisplay);
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
            newDisplay = new DisplayImplJ3D(rmtDisplay);
          }
          else { // Dim == JAVA3D_2D
            TwoDDisplayRendererJ3D tdr = new TwoDDisplayRendererJ3D();
            newDisplay = new DisplayImplJ3D(rmtDisplay, tdr);
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
        if (Dim == JAVA3D_3D) newDisplay = new DisplayImplJ3D(Name);
        else if (Dim == JAVA2D_2D) newDisplay = new DisplayImplJ2D(Name);
        else { // Dim == JAVA3D_2D
          TwoDDisplayRendererJ3D tdr = new TwoDDisplayRendererJ3D();
          newDisplay = new DisplayImplJ3D(Name, tdr);
        }
        rmtDisplay = new RemoteDisplayImpl(newDisplay);
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
    if (success) {
      if (VDisplay != null) {
        try {
          VDisplay.destroy();
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
      VDisplay = newDisplay;
      RemoteVDisplay = rmtDisplay;
    }
    return success;
  }

}

