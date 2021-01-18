//
// BasicSSCell.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

import javax.imageio.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.util.*;
import javax.swing.*;
import visad.*;
import visad.bom.ImageRendererJ3D;
import visad.collab.DisplayMonitor;
import visad.data.*;
import visad.formula.*;
import visad.java2d.*;
import visad.java3d.*;
import visad.util.*;

/**
 * BasicSSCell represents a single spreadsheet display cell.
 * BasicSSCells can be added to a VisAD user interface to provide some of the
 * capabilities presented in the VisAD SpreadSheet. Other capabilities, like
 * the file loader and data mapping dialog boxes, are available only with a
 * FancySSCell.
 */
public class BasicSSCell extends JPanel
  implements DisplayListener, MessageListener
{

  /**
   * Debugging flag.
   */
  public static boolean DEBUG = false;

  /**
   * Debugging level.
   *
   * <li>1 = Normal.
   * <li>2 = Collaboration messages.
   * <li>3 = All exceptions.
   */
  public static int DEBUG_LEVEL = 2;


  // --- STATIC UTILITY METHODS ---

  /**
   * List of SSCells on this JVM.
   */
  protected static final Vector SSCellVector = new Vector();

  /**
   * The number of SSCells currently saving data.
   */
  protected static int Saving = 0;

  /**
   * Whether Java3D is possible for this JVM.
   */
  protected static boolean Possible3D;

  /**
   * Whether Java3D is enabled for this JVM.
   */
  protected static boolean CanDo3D = enable3D();


  /**
   * Gets the SSCell with the specified name.
   */
  public static BasicSSCell getSSCellByName(String name) {
    synchronized (SSCellVector) {
      int len = SSCellVector.size();
      for (int i=0; i<len; i++) {
        BasicSSCell cell = (BasicSSCell) SSCellVector.elementAt(i);
        if (name.equalsIgnoreCase(cell.Name)) return cell;
      }
    }
    return null;
  }

  /**
   * Returns true if any SSCell is currently saving data.
   */
  public static boolean isSaving() {
    return Saving > 0;
  }

  // -- Detect, enable & disable Java3D --

  /**
   * Returns true if Java3D is possible for this JVM.
   */
  public static boolean possible3D() {
    return Possible3D;
  }

  /**
   * Returns true if Java3D is enabled for this JVM.
   */
  public static boolean canDo3D() {
    return CanDo3D;
  }

  /**
   * Attempts to enable Java3D for this JVM, returning true if successful.
   */
  public static boolean enable3D() {
    if (Possible3D) {
      // Java3D test has already succeeded
      CanDo3D = true;
    }
    else {
      // test for Java3D availability
      Possible3D = CanDo3D = Util.canDoJava3D();
      if (DEBUG && !Possible3D) {
        if (DEBUG) System.err.println("Warning: Java3D library not found");
      }
    }
    return CanDo3D;
  }

  /**
   * Disables Java3D for this JVM.
   */
  public static void disable3D() {
    CanDo3D = false;
  }


  // --- CONSTRUCTORS ---

  /**
   * Default FormulaManager object used by SSCells.
   */
  protected static final FormulaManager defaultFM =
    FormulaUtil.createStandardManager();

  /**
   * Name of this cell.
   */
  protected String Name;

  /**
   * Formula manager for this cell.
   */
  protected FormulaManager fm;


  /**
   * Constructs a new BasicSSCell with the given name.
   */
  public BasicSSCell(String name) throws VisADException, RemoteException {
    this(name, null, null, false, null);
  }

  /**
   * Constructs a new BasicSSCell with the given name and non-default
   * formula manager, to allow for custom formulas.
   */
  public BasicSSCell(String name, FormulaManager fman)
    throws VisADException, RemoteException
  {
    this(name, fman, null, false, null);
  }

  /**
   * Constructs a new BasicSSCell with the given name, that gets its
   * information from the given RemoteServer. The associated SSCell on the
   * server end must have already invoked its addToRemoteServer method.
   */
  public BasicSSCell(String name, RemoteServer rs)
    throws VisADException, RemoteException
  {
    this(name, null, rs, false, null);
  }

  /**
   * Constructs a new BasicSSCell with the given name and save string,
   * used to reconstruct this cell's configuration.
   */
  public BasicSSCell(String name, String save)
    throws VisADException, RemoteException
  {
    this(name, null, null, false, save);
  }

  /**
   * Constructs a new BasicSSCell with the given name, formula manager,
   * and remote server.
   */
  public BasicSSCell(String name, FormulaManager fman, RemoteServer rs,
    String save) throws VisADException, RemoteException
  {
    this(name, fman, rs, false, save);
  }

  /**
   * Constructs a new, possibly slaved, BasicSSCell with the given name,
   * formula manager, and remote server.
   */
  public BasicSSCell(String name, FormulaManager fman, RemoteServer rs,
    boolean slave, String save) throws VisADException, RemoteException
  {
    // set name
    if (name == null) {
      throw new VisADException("BasicSSCell: name cannot be null");
    }
    synchronized (SSCellVector) {
      int len = SSCellVector.size();
      for (int i=0; i<len; i++) {
        BasicSSCell cell = (BasicSSCell) SSCellVector.elementAt(i);
        if (name.equalsIgnoreCase(cell.Name)) {
          throw new VisADException("BasicSSCell: name already used");
        }
      }
      Name = name;
      SSCellVector.add(this);
    }

    // set formula manager
    fm = (fman == null ? defaultFM : fman);

    // set remote server
    if (rs != null) {
      RemoteVServer = rs;
      RemoteVDisplay = rs.getDisplay(Name);
    }
    IsRemote = (RemoteVDisplay != null);
    IsSlave = slave;

    // collaboration setup
    if (IsRemote) {
      // CLIENT: initialize
      setupClient();
    }
    else {
      // SERVER: initialize
      setupServer();
    }

    // set save string
    if (save != null) setSaveString(save);

    // finish GUI setup
    initDisplayPanel();
    setPreferredSize(new Dimension(0, 0));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    setBackground(IsSlave ? Color.darkGray : Color.black);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
  }


  // --- COLLABORATION ---

  // Command              Message ID           Message Str  Message Data
  // -------              ----------           -----------  ------------
  // Add file             ADD_DATA             src str      loaded data
  // Add URL              ADD_DATA             src str      loaded data
  // Add RMI addr         ADD_SOURCE           src str      Real(src type)
  // Add formula          ADD_SOURCE           src str      Real(src type)
  // Add direct           ADD_DATA             src str("")  direct data
  // Remove data          REMOVE_DATA          varName      null
  // Set maps             SET_MAPS             map str      null
  // Clear maps           SET_MAPS             null         null
  // Request dim switch   SET_DIM              ""           Real(newDim)
  // Switch dim           SET_DIM              null         Real(newDim)
  // Set errors for cell  SET_ERRORS           null         Tuple(Text[])
  // Set errors for data  SET_ERRORS           varName      Tuple(Text[])
  // Update data          UPDATE_DATA          varName      new data
  // Update dependencies  UPDATE_DEPENDENCIES  varName      true or false
  // Request status       STATUS               ""           null
  // Update status        STATUS               null         status info

  // Relevant methods: sendMessage(), receiveMessage()

  // Most collaboration messages are handled by calling the appropriate
  // methods with the notify bit set to false, so that additional messages
  // are not sent in response, thus avoiding feedback loops.

  // However, there are four exceptions to this model:

  // 1) Clients send SET_MAPS commands but do not alter maps themselves.
  // Servers receive SET_MAPS commands and alter mappings, but do not
  // send SET_MAPS commands, thus avoiding feedback loops.

  // 2) Both servers and clients send SET_DIM commands when setDimension()
  // is called. However, only servers actually alter the dimension.
  // Clients switch dimensions using setDimClone() when a SET_DIM command
  // from the server is received. A server receiving a SET_DIM command
  // will switch the dimension using setDimension(), generating another
  // SET_DIM command to which the clients respond. Thus, whenever a client
  // changes the dimension, two SET_DIM commands actually get sent: the
  // first by that client (msg="") and the second by the server (msg=null).

  // 3) Only servers send UPDATE_DEPENDENCIES commands. Both servers and
  // clients receive them and set their dependencies to match the message.
  // The reason for this behavior is that only the server computes formulas,
  // and consequently only servers can compute whether a given data object
  // is dependent on other data objects.

  // 4) New clients call STATUS to request the current status of the cell.
  // Servers answer with a cell status report by calling STATUS with the
  // current status of the cell as the message data.

  // CTR: TODO: If a cell has a formula depending on another data object in
  // that same cell, and user wants to clear the whole cell, he'll get a
  // message to confirm saying "other data objects depend"... This weird
  // message should be avoided somehow, but it's not easy without extending
  // the current implementation of formulas.


  /**
   * Message ID indicating a data object has been added.
   */
  public static final int ADD_DATA = 0;

  /**
   * Message ID indicating a source has been added.
   */
  public static final int ADD_SOURCE = 1;

  /**
   * Message ID indicating a data object has been removed.
   */
  public static final int REMOVE_DATA = 2;

  /**
   * Message ID indicating mappings have changed.
   */
  public static final int SET_MAPS = 3;

  /**
   * Message ID indicating dimension has changed.
   */
  public static final int SET_DIM = 4;

  /**
   * Message ID indicating errors have changed.
   */
  public static final int SET_ERRORS = 5;

  /**
   * Message ID indicating a data object has changed.
   */
  public static final int UPDATE_DATA = 6;

  /**
   * Message ID indicating a data object's dependencies have changed.
   */
  public static final int UPDATE_DEPENDENCIES = 7;

  /**
   * Message ID indicating a cell's status information
   * is being requested or reported.
   */
  public static final int STATUS = 8;

  /**
   * No message ID should have a value greater than or equal to this number.
   */
  public static final int MAX_ID = 9;

  /**
   * Message ID strings, for debugging.
   */
  public static final String[] messages = {"ADD_DATA", "ADD_SOURCE",
    "REMOVE_DATA", "SET_MAPS", "SET_DIM", "SET_ERRORS", "UPDATE_DATA",
    "UPDATE_DEPENDENCIES", "STATUS"};


  /**
   * List of servers to which this cell has been added.
   */
  protected Vector Servers = new Vector();

  /**
   * Associated DisplayImpl for sending and receiving messages.
   */
  protected DisplayImpl MDisplay = null;

  /**
   * Associated RemoteDisplay for sending and receiving messages.
   */
  protected RemoteDisplay RemoteMDisplay = null;

  /**
   * Associated VisAD RemoteDisplay.
   */
  protected RemoteDisplay RemoteVDisplay = null;

  /**
   * Associated VisAD RemoteSlaveDisplay, if any.
   */
  protected RemoteSlaveDisplayImpl RemoteVSlave = null;

  /**
   * Associated VisAD RemoteServer, if any.
   */
  protected RemoteServer RemoteVServer = null;

  /**
   * ID number for this collaborative cell.
   */
  protected int CollabID = DisplayMonitor.UNKNOWN_LISTENER_ID;

  /**
   * Whether this display is remote.
   */
  protected boolean IsRemote;

  /**
   * Whether this display is slaved.
   */
  protected boolean IsSlave;

  /**
   * Whether this display is still a new client (hasn't been initialized).
   */
  protected boolean NewClient;


  // -- Add & remove cells to remote servers --

  /**
   * Adds this cell to the given RemoteServer. SSCell servers must call this
   * method for each cell before clients can clone the cells with the
   * BasicSSCell(String name, RemoteServer rs) constructor, and before the
   * cells can be exported as RMI addresses.
   */
  public void addToRemoteServer(RemoteServerImpl rs) throws RemoteException {
    if (rs == null) return;
    if (IsRemote) {
      // CLIENT: illegal operation
      throw new RemoteException("Cannot add a cloned cell to a server");
    }

    synchronized (Servers) {
      if (!Servers.contains(rs)) {
        rs.addDisplay((RemoteDisplayImpl) RemoteMDisplay);
        rs.addDisplay((RemoteDisplayImpl) RemoteVDisplay);
        synchronized (CellData) {
          int len = CellData.size();
          for (int i=0; i<len; i++) {
            SSCellData cellData = (SSCellData) CellData.elementAt(i);
            rs.addDataReference(cellData.getRemoteReference());
          }
        }
        Servers.add(rs);
      }
    }
  }

  /**
   * Removes this cell from the given RemoteServer.
   */
  public void removeFromRemoteServer(RemoteServerImpl rs)
    throws RemoteException
  {
    if (rs == null) return;
    if (IsRemote) {
      // CLIENT: illegal operation
      throw new RemoteException("Cannot remove a cloned cell from a server");
    }

    synchronized (Servers) {
      if (Servers.contains(rs)) {
        rs.removeDisplay((RemoteDisplayImpl) RemoteMDisplay);
        rs.removeDisplay((RemoteDisplayImpl) RemoteVDisplay);
        synchronized (CellData) {
          int len = CellData.size();
          for (int i=0; i<len; i++) {
            SSCellData cellData = (SSCellData) CellData.elementAt(i);
            rs.removeDataReference(cellData.getRemoteReference());
          }
        }
        Servers.remove(rs);
      }
    }
  }

  // -- Send & receive cell update messages --

  /**
   * Sends a message of type <tt>id</tt> to server and all clients
   * of this cell.
   */
  void sendMessage(int id, String msg, Data data)
    throws RemoteException
  {
    // convert data to remote data
    RemoteData d;
    if (data instanceof RemoteData) d = (RemoteData) data;
    else d = new RemoteDataImpl((DataImpl) data);

    MDisplay.sendMessage(new MessageEvent(MAX_ID * CollabID + id, msg, d));
    if (DEBUG && DEBUG_LEVEL >= 2) {
      System.out.println(Name + "[" + CollabID + "]: sent " +
        messages[id] + ": msg=" + msg + ", data=" +
        (data == null ? "null" : data.getClass().getName()));
    }
  }

  /**
   * Handles VisAD messages. This method is the heart of BasicSSCell's
   * collaboration support.
   */
  public void receiveMessage(MessageEvent msg) throws RemoteException {
    int id = msg.getId();
    int mid = id % MAX_ID;
    int oid = id / MAX_ID;
    if (oid == CollabID && mid != UPDATE_DEPENDENCIES) {
      // cells ignore their own updates, except for UPDATE_DEPENDENCIES
      return;
    }
    String m = msg.getString();
    RemoteData data = msg.getData();
    if (DEBUG && DEBUG_LEVEL >= 2) {
      DataImpl ld = DataUtility.makeLocal(data, DEBUG);
      System.out.println(Name + "[" + CollabID + "]: received " +
        messages[mid] + " from " + oid + ": msg=" + m + ", data=" +
        (ld == null ? "null" : ld.getClass().getName()));
    }

    try {
      if (mid == ADD_DATA) {
        // add data from remote URL_SOURCE or DIRECT_SOURCE
        synchronized (CellData) {
          SSCellData cellData = addReferenceImpl(0, null, null, m,
            m.equals("") ? DIRECT_SOURCE : URL_SOURCE, false, false);
          cellData.setData(data, false);
        }
      }

      else if (mid == ADD_SOURCE) {
        // add data from remote RMI_SOURCE or FORMULA_SOURCE
        synchronized (CellData) {
          int type = (int)
	    ((Real) DataUtility.makeLocal(data, DEBUG)).getValue();
          addDataSource(0, m, type, false);
        }
      }

      else if (mid == REMOVE_DATA) {
        // remove data
        synchronized (CellData) {
          SSCellData cellData = getCellDataByName(m);
          removeDataImpl(cellData, false, true);
        }
      }

      else if (mid == SET_MAPS) {
        // set maps
        if (!IsRemote) {
          // SERVER: respond to client's SET_MAPS command
          if (m == null) clearMaps();
          else setMaps(DataUtility.convertStringToMaps(m, getData(), true));
        }
      }

      else if (mid == SET_DIM) {
        // set dimension
        int dim = (int)
	  ((Real) DataUtility.makeLocal(data, DEBUG)).getValue();
        if (m != null) {
          // SET_DIM command originates from client
          if (IsRemote) {
            // CLIENT: turn on wait dialog
            beginWait(true);
          }
          else {
            // SERVER: respond to client's SET_DIM command
            setDimension(dim);
          }
        }
        else if (IsRemote) {
          // CLIENT: respond to server's SET_DIM command
          endWait(false);
          setDimClone();
        }
      }

      else if (mid == SET_ERRORS) {
        // set errors
        Tuple tuple = (Tuple) DataUtility.makeLocal(data, DEBUG);
        String[] errors = DataUtility.tupleToStrings(tuple, DEBUG);
        SSCellData cellData;
        synchronized (CellData) {
          cellData = getCellDataByName(m);
        }
        if (cellData != null) cellData.setErrors(errors, false);
      }

      else if (mid == UPDATE_DATA) {
        // update local data to match data from message
        SSCellData cellData;
        synchronized (CellData) {
          cellData = getCellDataByName(m);
        }
        cellData.cell.skipNextErrors();
        cellData.setData(data, false);
      }

      else if (mid == UPDATE_DEPENDENCIES) {
        // update local data dependencies to match dependencies from message
        SSCellData cellData;
        synchronized (CellData) {
          cellData = getCellDataByName(m);
        }
        if (cellData != null) {
          cellData.setDependencies(
	    (Real) DataUtility.makeLocal(data, DEBUG));
        }
      }

      else if (mid == STATUS) {
        if (m == null) {
          // status report from server
          if (IsRemote && NewClient) {
            Tuple tuple = (Tuple) DataUtility.makeLocal(data, DEBUG);
            if (tuple != null) {
              synchronized (CellData) {
                // add Data objects to cell
                try {
                  int len = tuple.getDimension();
                  for (int i=0; i<len; i++) {
                    Tuple t = (Tuple)
		      DataUtility.makeLocal(tuple.getComponent(i), DEBUG);
                    Real rid = (Real)
		      DataUtility.makeLocal(t.getComponent(0), DEBUG);
                    Data d = t.getComponent(1);
                    DataReferenceImpl ref = new DataReferenceImpl(Name);
                    ref.setData(d);
                    Text source = (Text)
		      DataUtility.makeLocal(t.getComponent(2), DEBUG);
                    Real type = (Real)
		      DataUtility.makeLocal(t.getComponent(3), DEBUG);
                    addReferenceImpl((int) rid.getValue(), ref, null,
                      source.getValue(), (int) type.getValue(), false, false);
                  }
                }
                catch (VisADException exc) {
                  if (DEBUG) exc.printStackTrace();
                }
                catch (RemoteException exc) {
                  if (DEBUG) exc.printStackTrace();
                }
              }
            }
            NewClient = false;
          }
        }
        else {
          // status request from new client
          if (!IsRemote) {
            // SERVER: send out status report
            synchronized (CellData) {
              try {
                int len = CellData.size();
                Data[] d = new Data[len];
                for (int i=0; i<len; i++) {
                  SSCellData cellData = (SSCellData) CellData.elementAt(i);
                  Data[] status = new Data[4];
                  status[0] = new Real(cellData.getId());
                  status[1] = cellData.getData();
                  status[2] = new Text(cellData.getSource());
                  status[3] = new Real(cellData.getSourceType());
                  d[i] = new Tuple(status);
                }
                sendMessage(STATUS, null, len == 0 ? null : new Tuple(d));
              }
              catch (VisADException exc) {
                if (DEBUG) exc.printStackTrace();
              }
              catch (RemoteException exc) {
                if (DEBUG) exc.printStackTrace();
              }
            }
          }
        }
      }

      else if (DEBUG) {
        warn("unknown message id (" + mid + ") received.");
      }
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  // -- Set up server & client --

  /**
   * Sets up data needed for this cell to be a server.
   */
  protected void setupServer() throws VisADException, RemoteException {
    MDisplay = new DisplayImplJ2D(Name + "_Messenger", null);
    RemoteMDisplay = new RemoteDisplayImpl(MDisplay);
    MDisplay.addMessageListener(this);
    CollabID = 0;
    setDimension(JAVA2D_2D);
  }

  /**
   * Sets up data needed for this cell to be a client.
   */
  protected void setupClient() throws VisADException, RemoteException {
    RemoteMDisplay = RemoteVServer.getDisplay(Name + "_Messenger");
    MDisplay = new DisplayImplJ2D(RemoteMDisplay, null);
    MDisplay.addMessageListener(this);
    try {
      CollabID = MDisplay.getConnectionID(RemoteMDisplay);
    }
    catch (RemoteException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    setDimClone();
    addDisplayListener(this);
    NewClient = true;
    sendMessage(STATUS, "", null);
  }

  // -- Manage dependencies --

  /**
   * Broadcasts updated cell dependencies to all clients.
   */
  static void updateDependencies() {
    // check every data object of each cell for changes in dependency status
    synchronized (SSCellVector) {
      int len = SSCellVector.size();
      for (int i=0; i<len; i++) {
        BasicSSCell cell = (BasicSSCell) SSCellVector.elementAt(i);
        if (!cell.isRemote()) {
          synchronized (cell.CellData) {
            int len2 = cell.CellData.size();
            for (int j=0; j<len2; j++) {
              SSCellData cellData = (SSCellData) cell.CellData.elementAt(j);
              if (!cellData.ssCell.isRemote()) {
                String varName = cellData.getVariableName();
                boolean canBeRemoved = true;
                try {
                  canBeRemoved = cell.fm.canBeRemoved(varName);
                }
                catch (FormulaException exc) {
                  if (DEBUG) exc.printStackTrace();
                }
                if (canBeRemoved == cellData.othersDepend) {
                  try {
                    // notify linked cells of dependency status change
                    cellData.ssCell.sendMessage(UPDATE_DEPENDENCIES, varName,
                      canBeRemoved ? SSCellImpl.FALSE : SSCellImpl.TRUE);
                  }
                  catch (RemoteException exc) {
                    if (DEBUG) exc.printStackTrace();
                  }
                } // if canBeRemoved == cellData.othersDepend
              } // if cellData.ssCell.isRemote
            } // for j
          } // synchronized cell.CellData
        } // if cell.isRemote
      } // for i
    } // synchronized SSCellVector
  }


  // --- DATA MANAGEMENT ---

  /**
   * Interval at which to check for status changes while waiting.
   */
  protected static final int POLLING_INTERVAL = 100;

  /**
   * Indicates that the source of the data is unknown.
   */
  public static final int UNKNOWN_SOURCE = -1;

  /**
   * Indicates that the data was added to this cell directly
   * using addData() or addReference().
   */
  public static final int DIRECT_SOURCE = 0;

  /**
   * Indicates that the data came from a file or URL.
   */
  public static final int URL_SOURCE = 1;

  /**
   * Indicates that the data was computed from a formula.
   */
  public static final int FORMULA_SOURCE = 2;

  /**
   * Indicates that the data came from an RMI server.
   */
  public static final int RMI_SOURCE = 3;

  /**
   * Indicates that the data came from a remotely linked cell.
   */
  public static final int REMOTE_SOURCE = 4;

  /**
   * The number of data objects this cell is currently loading.
   */
  protected int Loading = 0;

  /**
   * List of this cell's data.
   */
  protected Vector CellData = new Vector();


  // -- Add data --

  /**
   * Adds a Data object to this cell, creating
   * an associated DataReference for it.
   *
   * @return Variable name of the newly added data.
   */
  public String addData(Data data) throws VisADException, RemoteException {
    return addData(0, data, null, "", DIRECT_SOURCE, true);
  }

  /**
   * Adds a Data object to this cell, creating an associated
   * DataReference with the specified ConstantMaps for it.
   *
   * @return Variable name of the newly added data.
   */
  public String addData(Data data, ConstantMap[] cmaps)
    throws VisADException, RemoteException
  {
    return addData(0, data, cmaps, "", DIRECT_SOURCE, true);
  }

  /**
   * Adds a Data object to this cell from the given source of the
   * specified type, creating an associated DataReference for it.
   *
   * @return Variable name of the newly added data.
   */
  protected String addData(int id, Data data, ConstantMap[] cmaps,
    String source, int type, boolean notify)
    throws VisADException, RemoteException
  {
    // add Data object to cell
    DataReferenceImpl ref = new DataReferenceImpl(Name);
    ref.setData(data);
    SSCellData cellData;
    synchronized (CellData) {
      cellData = addReferenceImpl(id, ref, cmaps, source, type, notify, true);
    }
    return cellData.getVariableName();
  }

  /**
   * Adds the given DataReference to this cell.
   *
   * @return Variable name of the newly added reference.
   */
  public String addReference(DataReferenceImpl ref)
    throws VisADException, RemoteException
  {
    SSCellData cellData;
    synchronized (CellData) {
      cellData = addReferenceImpl(0, ref, null, "", DIRECT_SOURCE, true, true);
    }
    return cellData.getVariableName();
  }

  /**
   * Adds the given DataReference to this cell with the specified ConstantMaps.
   *
   * @return Variable name of the newly added reference.
   */
  public String addReference(DataReferenceImpl ref, ConstantMap[] cmaps)
    throws VisADException, RemoteException
  {
    SSCellData cellData;
    synchronized (CellData) {
      cellData =
        addReferenceImpl(0, ref, cmaps, "", DIRECT_SOURCE, true, true);
    }
    return cellData.getVariableName();
  }

  /**
   * Obtains a Data object from the given source of unknown type,
   * and adds it to this cell.
   *
   * @return Variable name of the newly added data.
   */
  public String addDataSource(String source)
    throws VisADException, RemoteException
  {
    return addDataSource(0, source, UNKNOWN_SOURCE, true);
  }

  /**
   * Obtains a Data object from the given source of the specified type,
   * and adds it to this cell.
   *
   * @return Variable name of the newly added data.
   */
  public String addDataSource(String source, int type)
    throws VisADException, RemoteException
  {
    return addDataSource(0, source, type, true);
  }

  /**
   * Obtains a Data object from the given source of the specified type,
   * and adds it to this cell, assigning it the specified id.
   *
   * @return Variable name of the newly added data.
   */
  String addDataSource(int id, String source, int type, boolean notify)
    throws VisADException, RemoteException
  {
    String varName = null;

    if (type == UNKNOWN_SOURCE) {
      // determine source type
      if (source.startsWith("rmi://")) type = RMI_SOURCE;
      else if (source.startsWith("adde://")) type = URL_SOURCE;
      else {
        File f = new File(source);
        if (f.exists()) type = URL_SOURCE;
        else {
          URL url = null;
          try {
            url = new URL(source);
          }
          catch (MalformedURLException exc) {
            if (DEBUG && DEBUG_LEVEL >= 3) exc.printStackTrace();
          }
          if (url != null) type = URL_SOURCE;
          else type = FORMULA_SOURCE;
        }
      }
    }

    if (type == DIRECT_SOURCE || type == REMOTE_SOURCE) {
      // direct and remote source types are invalid
      throw new VisADException("Invalid source type");
    }

    if (type == URL_SOURCE) {
      // obtain data from filename or URL
      beginWait(true);

      Data data = null;
      boolean success = true;
      try {
        // load file or URL
        DefaultFamily loader = new DefaultFamily("loader", true);
        if (source.startsWith("file:")) {
          source = source.substring(5);
          // if source looks like it starts with a Windows drive spec...
          if (source.length() > 2 && source.charAt(2) == ':' &&
            source.charAt(0) == '/')
          {
            source = source.substring(1);
          }
        }
        data = loader.open(source);

        // check if source is a local file
        File file = new File(source);
        if (file.exists()) {
          String path = file.getAbsolutePath();
          char[] p = path.toCharArray();
          for (int i=0; i<p.length; i++) if (p[i] == '\\') p[i] = '/';
          path = new String(p);
          source = "file:" + (path.startsWith("/") ? "" : "/") + path;
        }
      }
      catch (OutOfMemoryError err) {
        if (DEBUG) err.printStackTrace();
        success = false;
        throw new VisADException("Not enough memory to import the data.");
      }
      catch (BadFormException exc) {
        if (DEBUG) exc.printStackTrace();
        success = false;
        throw new VisADException(
          "The source could not be converted to VisAD data.");
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
        success = false;
        throw exc;
      }
      finally {
        endWait(!success);
      }
      if (data == null) {
        throw new VisADException("Could not load data from source " + source);
      }
      varName = addData(id, data, null, source, URL_SOURCE, notify);
    }

    else if (type == FORMULA_SOURCE) {
      synchronized (CellData) {
        SSCellData cellData = addReferenceImpl(id, null, null,
          source, FORMULA_SOURCE, false, false);
        varName = cellData.getVariableName();
        if (!IsRemote) {
          // SERVER: link data to formula computation
          fm.assignFormula(varName, source);
        }
      }

      if (notify) {
        // notify linked cells of source addition
        sendMessage(ADD_SOURCE, source, new Real(type));
      }
    }

    else if (type == RMI_SOURCE) {
      // obtain data from an RMI server
      // example of RMI address: rmi://www.ssec.wisc.edu/MyServer/A1
      if (!source.startsWith("rmi://")) {
        throw new VisADException("RMI address must begin with rmi://");
      }
      final DataReferenceImpl lref = new DataReferenceImpl(Name);

      SSCellData cellData;
      synchronized (CellData) {
        cellData =
          addReferenceImpl(id, lref, null, source, RMI_SOURCE, false, false);
        varName = cellData.getVariableName();
      }

      if (!IsRemote) {
        // SERVER: obtain data from RMI address
        beginWait(true);
        boolean success = true;
        try {
          // attempt to obtain data from RMI server
          int len = source.length();
          int end = source.lastIndexOf("/");
          if (end < 6) end = len;
          String server = source.substring(4, end);
          String object = (end < len - 1) ? source.substring(end + 1) : "";
          RemoteServer rs = null;
          rs = (RemoteServer) Naming.lookup(server);
          RemoteDataReference ref = rs.getDataReference(object);
          if (ref == null) throw new VisADException("The remote object " +
            "called \"" + object + "\" does not exist");

          // set up cell to update local data when remote data changes
          final SSCellData fcd = cellData;
          final RemoteDataReference rref = ref;
          final BasicSSCell cell = this;
          CellImpl lcell = new CellImpl() {
            public void doAction() {
              try {
                lref.setData(DataUtility.makeLocal(rref.getData(), DEBUG));
              }
              catch (NullPointerException exc) {
                if (DEBUG) exc.printStackTrace();
                fcd.setError("Remote data is null");
              }
              catch (VisADException exc) {
                if (DEBUG) exc.printStackTrace();
                fcd.setError("Could not update remote data");
              }
              catch (RemoteException exc) {
                if (DEBUG) exc.printStackTrace();
                fcd.setError("Unable to import updated remote data");
              }
            }
          };
          RemoteCellImpl rcell = new RemoteCellImpl(lcell);
          rcell.addReference(ref);
        }
        catch (ClassCastException exc) {
          if (DEBUG) exc.printStackTrace();
          success = false;
          throw new VisADException("The name of the RMI server is not valid.");
        }
        catch (MalformedURLException exc) {
          if (DEBUG) exc.printStackTrace();
          success = false;
          throw new VisADException("The name of the RMI server is not valid.");
        }
        catch (NotBoundException exc) {
          if (DEBUG) exc.printStackTrace();
          success = false;
          throw new VisADException(
            "The remote data specified does not exist.");
        }
        catch (AccessException exc) {
          if (DEBUG) exc.printStackTrace();
          success = false;
          throw new VisADException(
            "Could not gain access to the remote data.");
        }
        catch (RemoteException exc) {
          if (DEBUG) exc.printStackTrace();
          success = false;
          throw new VisADException("Could not connect to the RMI server.");
        }
        catch (VisADException exc) {
          if (DEBUG) exc.printStackTrace();
          success = false;
          throw exc;
        }
        finally {
          endWait(!success);
        }
      }

      if (notify) {
        // notify linked cells of source addition
        sendMessage(ADD_SOURCE, source, new Real(type));
      }
    }

    return varName;
  }

  /**
   * Does the work of adding the given DataReference,
   * from the given source of the specified type.
   *
   * @return The newly created SSCellData object.
   */
  protected SSCellData addReferenceImpl(int id, DataReferenceImpl ref,
    ConstantMap[] cmaps, String source, int type, boolean notify,
    boolean checkErrors) throws VisADException, RemoteException
  {
    // ensure that id is valid
    if (id == 0) id = getFirstFreeId();

    // ensure that ref is valid
    if (ref == null) ref = new DataReferenceImpl(Name);

    // notify linked cells of data addition (ADD_DATA message must come first)
    if (notify) sendMessage(ADD_DATA, source, ref.getData());

    // add data reference to cell
    SSCellData cellData =
      new SSCellData(id, this, ref, cmaps, source, type, checkErrors);
    CellData.add(cellData);

    if (!IsRemote) {
      // SERVER: add data reference to display
      if (HasMappings) VDisplay.addReference(ref, cmaps);

      // add remote data reference to servers
      synchronized (Servers) {
        RemoteDataReferenceImpl remoteRef =
          (RemoteDataReferenceImpl) cellData.getRemoteReference();
        int len = Servers.size();
        for (int i=0; i<len; i++) {
          RemoteServerImpl rs = (RemoteServerImpl) Servers.elementAt(i);
          rs.addDataReference(remoteRef);
        }
      }
    }

    return cellData;
  }


  // -- Remove data --

  /**
   * Removes the given Data object from this cell.
   */
  public void removeData(Data data) throws VisADException, RemoteException {
    boolean found = false;
    synchronized (CellData) {
      int len = CellData.size();
      for (int i=0; i<len; i++) {
        SSCellData cellData = (SSCellData) CellData.elementAt(i);
        if (cellData.getData() == data) {
          removeDataImpl(cellData, true, true);
          found = true;
          break;
        }
      }
    }
    if (!found) {
      throw new VisADException("The given Data object does not exist");
    }
  }

  /**
   * Removes the Data object corresponding to the
   * given variable name from this cell.
   */
  public void removeData(String varName)
    throws VisADException, RemoteException
  {
    synchronized (CellData) {
      SSCellData cellData = getCellDataByName(varName);
      if (cellData == null) {
        throw new VisADException(
          "Data object called " + varName + " does not exist");
      }
      removeDataImpl(cellData, true, true);
    }
  }

  /**
   * Removes the given DataReference's associated Data object from this cell.
   */
  public void removeReference(DataReferenceImpl ref)
    throws VisADException, RemoteException
  {
    boolean found = false;
    synchronized (CellData) {
      int len = CellData.size();
      for (int i=0; i<len; i++) {
        SSCellData cellData = (SSCellData) CellData.elementAt(i);
        if (cellData.getReference() == ref) {
          removeDataImpl(cellData, true, true);
          found = true;
          break;
        }
      }
    }
    if (!found) {
      throw new VisADException("The given DataReference does not exist");
    }
  }

  /**
   * Removes all Data objects from this cell.
   */
  public void removeAllReferences() throws VisADException, RemoteException {
    removeAllReferences(true, true);
  }

  /**
   * Removes all Data objects from this cell, notifying listeners if the
   * notify flag is set.
   */
  protected void removeAllReferences(boolean notify)
    throws VisADException, RemoteException
  {
    removeAllReferences(notify, true);
  }

  /**
   * Removes all Data objects from this cell, notifying listeners if the
   * notify flag is set, and updating the display if the display flag is set.
   */
  protected void removeAllReferences(boolean notify, boolean display)
    throws VisADException, RemoteException
  {
    synchronized (CellData) {
      int len = CellData.size();
      for (int i=0; i<len; i++) {
        removeDataImpl((SSCellData) CellData.firstElement(), notify, display);
      }
    }
  }

  /**
   * Does the work of removing the Data object at the specified index.
   */
  protected void removeDataImpl(SSCellData cellData, boolean notify,
    boolean display) throws VisADException, RemoteException
  {
    String varName = cellData.getVariableName();

    // notify linked cells of data removal
    if (notify) sendMessage(REMOVE_DATA, varName, null);

    if (!IsRemote) {
      // remove data reference from display
      if (HasMappings) VDisplay.removeReference(cellData.getReference());
    
      // remove data reference from all servers
      synchronized (Servers) {
        RemoteDataReferenceImpl ref =
          (RemoteDataReferenceImpl) cellData.getRemoteReference();
        int len = Servers.size();
        for (int i=0; i<len; i++) {
          RemoteServerImpl rs = (RemoteServerImpl) Servers.elementAt(i);
          rs.removeDataReference(ref);
        }
      }
    }

    // purge cell data
    CellData.remove(cellData);
    cellData.destroy();
    cellData = null;

    // clear cell if no data objects are left
    if (display) {
      if (hasData()) updateDisplay();
      else clearDisplay();
    }
  }

  // -- Wait for data to load --

  /**
   * Blocks until the Data object with the given variable name
   * finishes loading.
   */
  public void waitForData(String varName) throws VisADException {
    SSCellData cellData;
    synchronized (CellData) {
      cellData = getCellDataByName(varName);
    }

    // wait for formula computation to finish
    fm.waitForFormula(varName);

    // wait for cell data to initialize
    while (!cellData.isInited()) {
      try {
        Thread.sleep(POLLING_INTERVAL);
      }
      catch (InterruptedException exc) {
        if (DEBUG && DEBUG_LEVEL >= 3) exc.printStackTrace();
      }
    }
  }

  /**
   * Blocks until all of this cell's Data objects finish loading.
   */
  public void waitForData() throws VisADException {
    // compile list of data variable names
    String[] varNames;
    int len;
    synchronized (CellData) {
      len = CellData.size();
      varNames = new String[len];
      for (int i=0; i<len; i++) {
        // get data's variable name
        SSCellData cellData = (SSCellData) CellData.elementAt(i);
        varNames[i] = cellData.getVariableName();
      }
    }

    // wait for file, URL and RMI loads to finish
    while (Loading > 0) {
      try {
        Thread.sleep(POLLING_INTERVAL);
      }
      catch (InterruptedException exc) {
        if (DEBUG && DEBUG_LEVEL >= 3) exc.printStackTrace();
      }
    }

    // wait for each data object
    for (int i=0; i<len; i++) waitForData(varNames[i]);
  }

  // -- Save data --

  /**
   * Exports a Data object to the given location, using the given Data form.
   */
  public void saveData(String varName, String location, Form form)
    throws BadFormException, IOException, VisADException, RemoteException
  {
    if (IsSlave) {
      // SLAVE: saveData not supported
      throw new VisADException("Cannot saveData on a slaved cell");
    }
    Data data = getData(varName);
    Saving++;
    try {
      form.save(location, data, true);
    }
    finally {
      Saving--;
    }
  }

  // -- Utility --

  /**
   * Obtains the cell data entry corresponding to the given variable name.
   */
  protected SSCellData getCellDataByName(String varName) {
    int len = CellData.size();
    SSCellData cellData = null;
    for (int i=0; i<len; i++) {
      SSCellData cd = (SSCellData) CellData.elementAt(i);
      if (cd.getVariableName().equals(varName)) {
        cellData = cd;
        break;
      }
    }
    return cellData;
  }

  /**
   * Gets the first free cell data ID number.
   */
  protected int getFirstFreeId() {
    synchronized (CellData) {
      if (CellData.size() == 0) return 1;
      SSCellData cellData = (SSCellData) CellData.lastElement();
      return cellData.getId() + 1;
    }
  }


  // --- DISPLAY MANAGEMENT ---

  /**
   * Constant for 3-D (Java3D) dimensionality.
   */
  public static final int JAVA3D_3D = 1;

  /**
   * Constant for 2-D (Java2D) dimensionality.
   */
  public static final int JAVA2D_2D = 2;

  /**
   * Constant for 2-D (Java3D) dimensionality.
   */
  public static final int JAVA3D_2D = 3;

  /**
   * Class name of the 3-D (Java3D) display renderer.
   */
  private static final String j33 = "visad.java3d.DefaultDisplayRendererJ3D";

  /**
   * Class name of the 2-D (Java2D) display renderer.
   */
  private static final String j22 = "visad.java2d.DefaultDisplayRendererJ2D";

  /**
   * Class name of the 2-D (Java3D) display renderer.
   */
  private static final String j32 = "visad.java3d.TwoDDisplayRendererJ3D";

  /**
   * Class name of the image 3-D (Java3D) display renderer.
   */
  private static final String jir = "visad.bom.ImageRendererJ3D";

  /**
   * Associated VisAD Display.
   */
  protected DisplayImpl VDisplay;

  /**
   * The dimensionality of the display: JAVA3D_3D, JAVA2D_2D, or JAVA3D_2D.
   */
  protected int Dim = -1;

  /**
   * Whether this cell has mappings from Data to Display.
   */
  protected boolean HasMappings = false;


  // -- Build display --

  /**
   * Reconstructs this cell's display.
   */
  public synchronized boolean constructDisplay() {
    boolean success = true;
    DisplayImpl newDisplay = VDisplay;
    RemoteDisplay rmtDisplay = RemoteVDisplay;
    if (IsSlave) {
      // SLAVE: construct dummy 2-D display
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
    else {
      // construct display of the proper dimension
      try {
        if (IsRemote) {
          // CLIENT: construct new display from server's remote copy
          if (Dim == JAVA3D_3D) newDisplay = new DisplayImplJ3D(rmtDisplay);
          else if (Dim == JAVA2D_2D) {
            newDisplay = new DisplayImplJ2D(rmtDisplay);
          }
          else { // Dim == JAVA3D_2D
            TwoDDisplayRendererJ3D tdr = new TwoDDisplayRendererJ3D();
            newDisplay = new DisplayImplJ3D(rmtDisplay, tdr);
          }
        }
        else {
          // SERVER: construct new display and make a remote copy
          if (Dim == JAVA3D_3D) newDisplay = new DisplayImplJ3D(Name);
          else if (Dim == JAVA2D_2D) newDisplay = new DisplayImplJ2D(Name);
          else { // Dim == JAVA3D_2D
            TwoDDisplayRendererJ3D tdr = new TwoDDisplayRendererJ3D();
            newDisplay = new DisplayImplJ3D(Name, tdr);
          }
          rmtDisplay = new RemoteDisplayImpl(newDisplay);
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

  // -- Handle display changes --

  /**
   * Handles display changes.
   */
  public void displayChanged(DisplayEvent e) {
    int id = e.getId();
    if (id == DisplayEvent.TRANSFORM_DONE ||
      (id == DisplayEvent.FRAME_DONE && IsSlave && !hasDisplay()))
    {
      if (!hasDisplay()) {
        initDisplayPanel();
        updateDisplay(true);
      }
      // display has changed; notify listeners
      notifySSCellListeners(SSCellChangeEvent.DISPLAY_CHANGE);
    }
    else if (id == DisplayEvent.MAPS_CLEARED) updateDisplay(false);
  }

  // -- Set & clear mappings --

  /**
   * Maps RealTypes to the display according to the specified ScalarMaps.
   */
  public synchronized void setMaps(ScalarMap[] maps)
    throws VisADException, RemoteException
  {
    if (maps == null) return;

    VisADException vexc = null;
    RemoteException rexc = null;

    if (IsRemote) {
      // CLIENT: send new mappings to server
      sendMessage(SET_MAPS, DataUtility.convertMapsToString(maps), null);
    }
    else {
      // SERVER: set up mappings
      DataReference[] dr;
      ConstantMap[][] cmaps;
      synchronized (CellData) {
        int len = CellData.size();
        dr = new DataReference[len];
        cmaps = new ConstantMap[len][];
        for (int i=0; i<len; i++) {
          SSCellData cellData = (SSCellData) CellData.elementAt(i);
          dr[i] = cellData.getReference();
          cmaps[i] = cellData.getConstantMaps();
        }
      }
      String save = getPartialSaveString();
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
      for (int i=0; i<dr.length; i++) {
        // determine if ImageRendererJ3D can be used
        boolean ok = false;
        Data data = dr[i].getData();
        if (data == null) {
          if (DEBUG) warn("data #" + i + " is null; cannot analyze MathType");
        }
        else if (Possible3D) {
          if (data instanceof FieldImpl) {
            visad.Set set = ((FieldImpl) data).getDomainSet();
            if (set instanceof GriddedSet && set.getManifoldDimension() == 2) {
              MathType type = data.getType();
              try {
                ok = ImageRendererJ3D.isRendererUsable(type, maps);
              }
              catch (VisADException exc) {
                if (DEBUG && DEBUG_LEVEL >= 3) exc.printStackTrace();
              }
            }
          }
        }
        // add reference
        if (ok && Dim != JAVA2D_2D) {
          VDisplay.addReferences(new ImageRendererJ3D(), dr[i], cmaps[i]);
        }
        else {
          if (DEBUG) warn("data #" + i + " cannot use ImageRendererJ3D");
          VDisplay.addReference(dr[i], cmaps[i]);
        }
      }
      setPartialSaveString(save, true);
      VDisplay.enableAction();
    }
    HasMappings = true;
    if (vexc != null) throw vexc;
    if (rexc != null) throw rexc;
  }

  /**
   * Clears this cell's mappings.
   */
  public void clearMaps() throws VisADException, RemoteException {
    if (IsRemote) clearMapsClone(true);
    else if (hasMappings()) {
      VDisplay.removeAllReferences();
      VDisplay.clearMaps();
      HasMappings = false;
    }
  }

  /**
   * Clears this cloned cell's mappings.
   */
  private void clearMapsClone(boolean display)
    throws VisADException, RemoteException
  {
    if (hasMappings()) {
      RemoteVDisplay.removeAllReferences();
      RemoteVDisplay.clearMaps();
      if (display) {
        clearDisplay();
        constructDisplay();
        initDisplayPanel();
        updateDisplay(true);
      }
      HasMappings = false;
    }
  }

  // -- Clear cell --

  /**
   * Clears this cell's display.
   */
  public void clearDisplay() throws VisADException, RemoteException {
    if (!DisplayEnabled) return;
    HasDisplay = false;
    Util.invoke(false, DEBUG, new Runnable() {
      public void run() {
        removeAll();
        refresh();
      }
    });
  }

  /**
   * Clears this cell completely.
   */
  public void clearCell() throws VisADException, RemoteException {
    removeAllReferences();
  }

  /**
   * Clears this cell completely and destroys it,
   * removing it from the list of created cells.
   */
  public void destroyCell() throws VisADException, RemoteException {
    RemoteException problem = null;
    setDisplayEnabled(false);

    // remove all data objects from this cell
    removeAllReferences(false, !IsRemote);

    if (!IsRemote) {
      // SERVER: stop serving this cell
      clearCell();
      int slen = Servers.size();
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
    else if (IsSlave && RemoteVSlave != null) {
      // SLAVE: disconnect cleanly
      try {
        RemoteVSlave.unlink();
      }
      catch (RemoteException exc) {
        problem = exc;
      }
    }

    // remove cell from static list
    synchronized (SSCellVector) {
      SSCellVector.remove(this);
    }

    if (problem != null) throw problem;
  }

  // -- Set dimension --

  /**
   * Sets this cell's dimensionality.
   */
  public void setDimension(int dim) throws VisADException, RemoteException {
    if (dim == Dim) return;
    if (dim != JAVA3D_3D && dim != JAVA2D_2D && dim != JAVA3D_2D) {
      throw new VisADException("Invalid dimension");
    }

    if (!IsRemote) {
      // SERVER: do dimension switch
      Dim = dim;
      synchronized (DListen) {
        // remove listeners temporarily
        detachListeners();

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

        synchronized (Servers) {
          // remove old display from all RemoteServers
          int slen = Servers.size();
          for (int i=0; i<slen; i++) {
            RemoteServerImpl rsi = (RemoteServerImpl) Servers.elementAt(i);
            rsi.removeDisplay((RemoteDisplayImpl) RemoteVDisplay);
          }

          // switch display dimension
          constructDisplay();

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
        initDisplayPanel();
        updateDisplay(hasData());

        // put listeners back
        attachListeners();
      }

      // broadcast dimension change event
      notifySSCellListeners(SSCellChangeEvent.DIMENSION_CHANGE);
    }

    // notify linked cells of dimension change
    sendMessage(SET_DIM, IsRemote ? "" : null, new Real(dim));
  }

  /**
   * Updates the dimension of this cloned cell to match that of the server.
   */
  private void setDimClone() throws VisADException, RemoteException {
    synchronized (DListen) {
      // remove listeners temporarily
      detachListeners();

      // remove old display panel from cell
      clearDisplay();

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
        final JComponent ec = errorCanvas;
        Util.invoke(false, DEBUG, new Runnable() {
          public void run() {
            removeAll();
            add(ec);
            refresh();
          }
        });
      }

      // reinitialize display
      initDisplayPanel();
      if (success && hasData()) updateDisplay(true);

      // put all listeners back
      attachListeners();
    }

    // broadcast dimension change event
    notifySSCellListeners(SSCellChangeEvent.DIMENSION_CHANGE);
  }

  // -- Capture display image --

  /**
   * Captures an image and saves it to a given file name, in JPEG format.
   */
  public void captureImage(File f) throws VisADException, IOException {
    BufferedImage image =
      IsSlave ? RemoteVSlave.getImage() : VDisplay.getImage();
    try {
      Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
      ImageWriter writer = iter.next();
      ImageWriteParam param = writer.getDefaultWriteParam();
      param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      param.setCompressionQuality(1.0f);
      FileOutputStream fout = new FileOutputStream(f);
      writer.setOutput(fout);
      IIOImage iio = new IIOImage(image, null, null);
      writer.write(null, iio, param);
      ImageIO.write(image, "image/jpeg", fout);
      fout.close();
    }
    catch (NoClassDefFoundError err) {
      throw new VisADException("JPEG codec not found");
    }
  }


  // --- SAVE STRINGS ---

  /**
   * Gets the save string necessary to reconstruct this cell.
   */
  public String getSaveString() {
    StringBuffer sb = new StringBuffer();

    // append data information
    synchronized (CellData) {
      int len = CellData.size();
      sb.append("# ");
      sb.append(Name);
      sb.append(": data information\n");
      for (int i=0; i<len; i++) {
        SSCellData cellData = (SSCellData) CellData.elementAt(i);
        sb.append("id = ");
        sb.append(cellData.getId());
        sb.append('\n');
        sb.append("source = ");
        sb.append(cellData.getSource());
        sb.append('\n');
        sb.append("source type = ");
        sb.append(cellData.getSourceType());
        sb.append('\n');
      }
    }

    // append display information
    sb.append("\n# ");
    sb.append(Name);
    sb.append(": display information\n");

    // add dimension to save string
    sb.append("dim = ");
    sb.append(Dim);
    sb.append('\n');

    // add mapping and control information to save string
    sb.append(getPartialSaveString());

    return sb.toString();
  }

  /**
   * Reconstructs this cell using the specified save string.
   */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    setPartialSaveString(save, false);
  }

  /**
   * Gets a string for reconstructing ScalarMap range
   * and Control information.
   */
  public String getPartialSaveString() {
    StringBuffer sb = new StringBuffer();
    Vector mapVector = null;
    if (hasMappings()) {
      mapVector = VDisplay.getMapVector();
      int mvs = mapVector.size();
      if (mvs > 0) {
        // add mappings to save string
        sb.append("maps =");
        sb.append(DataUtility.convertMapsToString(mapVector));
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
        int cvlen = cv.size();
        for (int i=0; i<cvlen; i++) {
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
        int cvlen = cv.size();
        for (int i=0; i<cvlen; i++) {
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
        int cvlen = cv.size();
        for (int i=0; i<cvlen; i++) {
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
        int cvlen = cv.size();
        for (int i=0; i<cvlen; i++) {
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
        int cvlen = cv.size();
        for (int i=0; i<cvlen; i++) {
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

  /**
   * Reconstructs parts of this cell using the specified save string.
   */
  public void setPartialSaveString(String save, boolean preserveMaps)
    throws VisADException, RemoteException
  {
    // make sure cell is not remote
    if (IsRemote) {
      throw new VisADException("Cannot setSaveString on a remote cell");
    }

    // data variables
    Vector ids = new Vector();
    Vector sources = new Vector();
    Vector types = new Vector();

    // display variables
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
    Vector selectVal = new Vector();

    // parse the save string into "keyword = value" tokens
    SaveStringTokenizer sst = new SaveStringTokenizer(save);
    for (int i=0; i<sst.keywords.length; i++) {
      String keyword = sst.keywords[i];
      String value = sst.values[i];

      // id
      if (keyword.equalsIgnoreCase("id") ||
        keyword.equalsIgnoreCase("data id") ||
        keyword.equalsIgnoreCase("data_id") ||
        keyword.equalsIgnoreCase("dataid"))
      {
        try {
          ids.add(new Integer(value));
        }
        catch (NumberFormatException exc) {
          // invalid id value
          if (DEBUG) exc.printStackTrace();
          warn("data id value " + value + " is not valid and will be ignored");
        }
      }

      // source
      else if (keyword.equalsIgnoreCase("source") ||
        keyword.equalsIgnoreCase("data source") ||
        keyword.equalsIgnoreCase("data_source") ||
        keyword.equalsIgnoreCase("datasource"))
      {
        sources.add(value);
      }

      // source type
      else if (keyword.equalsIgnoreCase("source type") ||
        keyword.equalsIgnoreCase("source_type") ||
        keyword.equalsIgnoreCase("sourcetype") ||
        keyword.equalsIgnoreCase("data source type") ||
        keyword.equalsIgnoreCase("data_source_type") ||
        keyword.equalsIgnoreCase("datasourcetype"))
      {
        try {
          types.add(new Integer(value));
        }
        catch (NumberFormatException exc) {
          // invalid source type value
          if (DEBUG) exc.printStackTrace();
          warn("source type value " + value +
            " is not valid and will be ignored");
        }
      }

      // filename (old keyword)
      else if (keyword.equalsIgnoreCase("filename") ||
        keyword.equalsIgnoreCase("file name") ||
        keyword.equalsIgnoreCase("file_name") ||
        keyword.equalsIgnoreCase("file"))
      {
        ids.add(new Integer(0));
        sources.add(value);
        types.add(new Integer(URL_SOURCE));
      }

      // rmi address (old keyword)
      else if (keyword.equalsIgnoreCase("rmi") ||
        keyword.equalsIgnoreCase("rmi address") ||
        keyword.equalsIgnoreCase("rmi_address") ||
        keyword.equalsIgnoreCase("rmiaddress"))
      {
        ids.add(new Integer(0));
        sources.add(value);
        types.add(new Integer(RMI_SOURCE));
      }

      // formula (old keyword)
      else if (keyword.equalsIgnoreCase("formula") ||
        keyword.equalsIgnoreCase("equation"))
      {
        ids.add(new Integer(0));
        sources.add(value);
        types.add(new Integer(FORMULA_SOURCE));
      }

      // dimension
      else if (keyword.equalsIgnoreCase("dim") ||
        keyword.equalsIgnoreCase("dimension"))
      {
        int d = -1;
        try {
          d = Integer.parseInt(value);
        }
        catch (NumberFormatException exc) {
          if (DEBUG) exc.printStackTrace();
        }
        if (d > 0 && d < 4) dim = d;
        else {
          // invalid dimension value
          warn("dimension value " + value +
            " is not valid and will be ignored");
        }
      }

      // mappings
      else if (keyword.equalsIgnoreCase("maps") ||
        keyword.equalsIgnoreCase("mappings"))
      {
        mapString = value;
      }

      // mapping ranges
      else if (keyword.equalsIgnoreCase("map ranges") ||
        keyword.equalsIgnoreCase("map_ranges") ||
        keyword.equalsIgnoreCase("mapranges"))
      {
        StringTokenizer st = new StringTokenizer(value);
        mapMins = new Vector();
        mapMaxs = new Vector();
        while (true) {
          if (!st.hasMoreTokens()) break;
          String s1 = st.nextToken();
          if (!st.hasMoreTokens()) {
            warn("trailing map range min value " + s1 +
              " has no corresponding max value and will be ignored");
            break;
          }
          String s2 = st.nextToken();
          Double d1 = null, d2 = null;
          try {
            d1 = new Double(s1.equals("NaN") ?
              Double.NaN : Double.parseDouble(s1));
            d2 = new Double(s2.equals("NaN") ?
              Double.NaN : Double.parseDouble(s2));
          }
          catch (NumberFormatException exc) {
            if (DEBUG) exc.printStackTrace();
          }
          if (d1 == null || d2 == null) {
            warn("map range min/max pair (" + s1 + ", " + s2 +
              ") is not valid and will be ignored");
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
        proj = value;
      }

      // graphics mode settings
      else if (keyword.equalsIgnoreCase("graphics mode") ||
        keyword.equalsIgnoreCase("graphics_mode") ||
        keyword.equalsIgnoreCase("graphicsmode") ||
        keyword.equalsIgnoreCase("graphics") ||
        keyword.equalsIgnoreCase("mode"))
      {
        mode = value;
      }

      // color table
      else if (keyword.equalsIgnoreCase("color") ||
        keyword.equalsIgnoreCase("color table") ||
        keyword.equalsIgnoreCase("color_table") ||
        keyword.equalsIgnoreCase("colortable"))
      {
        color.add(value);
      }

      // contour data
      else if (keyword.equalsIgnoreCase("contour") ||
        keyword.equalsIgnoreCase("contours") ||
        keyword.equalsIgnoreCase("iso contour") ||
        keyword.equalsIgnoreCase("iso_contour") ||
        keyword.equalsIgnoreCase("isocontour") ||
        keyword.equalsIgnoreCase("iso contours") ||
        keyword.equalsIgnoreCase("iso_contours") ||
        keyword.equalsIgnoreCase("isocontours"))
      {
        contour.add(value);
      }

      // range
      else if (keyword.equalsIgnoreCase("range") ||
        keyword.equalsIgnoreCase("select range") ||
        keyword.equalsIgnoreCase("select_range") ||
        keyword.equalsIgnoreCase("selectrange"))
      {
        range.add(value);
      }

      // animation
      else if (keyword.equalsIgnoreCase("anim") ||
        keyword.equalsIgnoreCase("animation"))
      {
        anim.add(value);
      }

      // select value
      else if (keyword.equalsIgnoreCase("value") ||
        keyword.equalsIgnoreCase("select value") ||
        keyword.equalsIgnoreCase("select_value") ||
        keyword.equalsIgnoreCase("selectvalue"))
      {
        selectVal.add(value);
      }

      // unknown keyword
      else {
        warn("keyword " + keyword + " is unknown and will be ignored");
      }
    }

    if (preserveMaps) {
      // detect which maps are the same and set appropriate ranges
      maps = DataUtility.convertStringToMaps(mapString, getData(), true);
      if (maps != null) {
        int lmin = mapMins == null ? -1 : mapMins.size();
        int lmax = mapMaxs == null ? -1 : mapMaxs.size();
        int cmin = 0, cmax = 0;
        Vector mapVector = VDisplay.getMapVector();
        for (int j=0; j<maps.length; j++) {
          if (maps[j] != null) {
            // detect whether map needs a range
            boolean scale = maps[j].getScale(
              new double[2], new double[2], new double[2]);
            if (scale && cmin < lmin && cmax < lmax) {
              // find map in current display vector
              int mapIndex = mapVector.indexOf(maps[j]);
              if (mapIndex >= 0) {
                // set map's minimum and maximum range values
                ScalarMap map = (ScalarMap) mapVector.elementAt(mapIndex);
                map.setRange(
                  ((Double) mapMins.elementAt(cmin++)).doubleValue(),
                  ((Double) mapMaxs.elementAt(cmax++)).doubleValue());
              }
              else {
                // skip current minimum and maximum range values
                cmin++;
                cmax++;
              }
            }
          }
        }
      }
    }
    else {
      // clear old stuff from cell
      clearCell();

      // set up dimension
      setDimension(dim);

      // set up data objects
      int ilen = ids.size();
      int slen = sources.size();
      int tlen = types.size();
      if (ilen != slen || ilen != tlen) {
        warn("some data object entries are corrupt and will be ignored");
      }
      int len =
        ilen < slen && ilen < tlen ? ilen : (slen < tlen ? slen : tlen);
      setDisplayEnabled(false);
      for (int i=0; i<len; i++) {
        int id = ((Integer) ids.elementAt(i)).intValue();
        String source = (String) sources.elementAt(i);
        int type = ((Integer) types.elementAt(i)).intValue();
        addDataSource(id, source, type, true);
      }
      waitForData();

      // set up map ranges; then set maps
      maps = DataUtility.convertStringToMaps(mapString, getData(), true);
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
      setDisplayEnabled(true);
    }

    // set up projection control
    if (proj != null) {
      ProjectionControl pc = VDisplay.getProjectionControl();
      if (pc != null) pc.setSaveString(proj);
      else if (!preserveMaps) warn("display has no ProjectionControl; " +
        "the provided projection matrix will be ignored");
    }

    // set up graphics mode control
    if (mode != null) {
      GraphicsModeControl gmc = VDisplay.getGraphicsModeControl();
      if (gmc != null) {
        try {
          gmc.setSaveString(mode);
        }
        catch (VisADException exc) {
          if (DEBUG && DEBUG_LEVEL >= 3) exc.printStackTrace();
        }
      }
      else if (!preserveMaps) warn("display has no GraphicsModeControl; " +
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
        else if (!preserveMaps) warn("display has no ColorControl #" +
          (i + 1) + "; " + "the provided color table will be ignored");
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
        else if (!preserveMaps) warn("display has no ContourControl #" +
          (i + 1) + "; " + "the provided contour settings will be ignored");
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
        else if (!preserveMaps) warn("display has no RangeControl #" +
          (i + 1) + "; " + "the provided range will be ignored");
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
          catch (InterruptedException exc) {
            if (DEBUG && DEBUG_LEVEL >= 3) exc.printStackTrace();
          }
          ac.setSaveString(s);
        }
        else if (!preserveMaps) warn("display has no AnimationControl #" +
          (i + 1) + "; " + "the provided animation settings will be ignored");
      }
    }

    // set up value control(s)
    len = selectVal.size();
    if (len > 0) {
      for (int i=0; i<len; i++) {
        String s = (String) selectVal.elementAt(i);
        ValueControl vc = (ValueControl)
          VDisplay.getControl(ValueControl.class, i);
        if (vc != null) vc.setSaveString(s);
        else if (!preserveMaps) warn("display has no ValueControl #" +
          (i + 1) + "; " + "the provided value will be ignored");
      }
    }
  }


  // --- UTILITY ---

  /**
   * Adds a variable to this cell's formula manager.
   */
  public void addVar(String name, ThingReference tr) throws VisADException {
    fm.createVar(name, tr);
  }

  /**
   * Prints a warning message.
   */
  private void warn(String s) {
    System.err.println(Name + ": Warning: " + s);
  }


  // --- GUI MANAGEMENT ---

  /**
   * Prevents simultaneous GUI manipulation.
   */
  protected Object Lock = new Object();

  /**
   * Associated VisAD Display component.
   */
  protected Component VDPanel;

  /**
   * Global errors currently being displayed in this cell, if any.
   */
  protected String[] Errors;

  /**
   * Whether a valid VisAD display currently exists.
   */
  protected boolean HasDisplay = false;

  /**
   * Whether display updates are enabled.
   */
  protected boolean DisplayEnabled = true;

  /**
   * A panel that displays the words &quot;Please wait.&quot;
   */
  private JPanel WaitPanel = null;


  // -- GUI refresh --

  /**
   * Refreshes this cell's display.
   */
  void refresh() {
    validate();
    repaint();
  }

  // -- Set errors --

  /**
   * Displays global errors in this cell, notifying
   * linked cells if notify flag is set.
   */
  protected void setErrors(String[] errors, boolean notify) {
    if (Util.arraysEqual(Errors, errors)) return;
    Errors = errors;
    updateDisplay();
    if (notify) {
      try {
        sendMessage(SET_ERRORS, null, stringsToTuple(errors));
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
      }
    }
  }

  // -- Init & toggle display panel --

  /**
   * Initializes this cell's display panel.
   */
  private void initDisplayPanel() {
    if (IsSlave) VDPanel = RemoteVSlave.getComponent();
    else VDPanel = VDisplay.getComponent();
  }

  /**
   * Display the data for this cell, or all relevant errors if there are any.
   */
  void updateDisplay(boolean hasDisplay) {
    HasDisplay = hasDisplay;
    updateDisplay();
  }

  /**
   * Display the data for this cell if hasDisplay flag is set.
   */
  void updateDisplay() {
    if (!DisplayEnabled) return;

    if (WaitPanel == null) {
      // initialize "Please wait" panel
      WaitPanel = new JPanel();
      WaitPanel.setBackground(Color.black);
      WaitPanel.setLayout(new BoxLayout(WaitPanel, BoxLayout.X_AXIS));
      WaitPanel.add(Box.createHorizontalGlue());
      WaitPanel.add(new JLabel("Please wait..."));
      WaitPanel.add(Box.createHorizontalGlue());
    }

    // compile list of errors
    final Vector e = new Vector();
    if (Errors == null) {
      // no global errors; compile list of data-related errors
      synchronized (CellData) {
        int len = CellData.size();
        for (int i=0; i<len; i++) {
          SSCellData cellData = (SSCellData) CellData.elementAt(i);
          String varName = cellData.getVariableName();
          String[] errors = cellData.getErrors();
          if (errors != null) {
            for (int j=0; j<errors.length; j++) {
              e.add(varName + ": " + errors[j]);
            }
          }
        }
      }
    }
    else {
      // global errors exist; they take precedence
      for (int i=0; i<Errors.length; i++) e.add(Errors[i]);
    }

    // set up error canvas
    final int len = e.size();
    JComponent errorCanvas;
    if (len == 0) errorCanvas = null;
    else {
      errorCanvas = new JComponent() {
        public void paint(Graphics g) {
          g.setColor(Color.white);
          String s = (len == 1 ? "An error" : "Errors") +
            " occurred while computing this cell:";
          g.drawString(s, 8, 20);
          for (int i=0; i<len; i++) {
            s = (String) e.elementAt(i);
            g.drawString(s, 8, 15*i + 50);
          }
        }
      };
    }

    final JComponent ec = errorCanvas;
    Util.invoke(true, DEBUG, new Runnable() {
      public void run() {
        // determine whether VDPanel is already present onscreen
        Component[] c = getComponents();
        boolean hasPanel = c.length > 0 && c[0] == VDPanel;

        // redraw cell
        if (Loading > 0) {
          removeAll();
          add(WaitPanel);
        }
        else if (ec != null) {
          removeAll();
          add(ec);
        }
        else if (HasDisplay) {
          if (!hasPanel) {
            // no need to re-add VDPanel if already present
            removeAll();
            add(VDPanel);
          }
        }
        else removeAll();
        refresh();
      }
    });
  }

  /**
   * Enables or disables display updates.
   */
  private void setDisplayEnabled(boolean value) {
    if (value == DisplayEnabled) return;
    DisplayEnabled = value;
    if (DisplayEnabled) updateDisplay();
  }

  // -- Toggle waiting mode --

  /**
   * Increments the loading counter.
   */
  private void beginWait(boolean update) {
    Loading++;
    if (update) updateDisplay();
  }

  /**
   * Decrements the loading counter.
   */
  private void endWait(boolean update) {
    Loading--;
    if (update) updateDisplay();
  }


  // --- EVENT HANDLING ---

  /**
   * List of SSCellListeners.
   */
  protected Vector SListen = new Vector();

  /**
   * List of DisplayListeners.
   */
  protected Vector DListen = new Vector();


  // -- Add & remove DisplayListeners --

  /**
   * Adds a DisplayListener.
   */
  public void addDisplayListener(DisplayListener d) {
    synchronized (DListen) {
      if (!DListen.contains(d)) {
        if (IsSlave) RemoteVSlave.addDisplayListener(d);
        else VDisplay.addDisplayListener(d);
        DListen.add(d);
      }
    }
  }

  /**
   * Removes a DisplayListener from this cell.
   */
  public void removeDisplayListener(DisplayListener d) {
    synchronized (DListen) {
      if (DListen.contains(d)) {
        if (IsSlave) RemoteVSlave.removeDisplayListener(d);
        else VDisplay.removeDisplayListener(d);
        DListen.remove(d);
      }
    }
  }

  /**
   * Re-attaches all display listeners after they have been detached.
   */
  private void attachListeners() {
    int len = DListen.size();
    if (IsSlave) {
      for (int i=0; i<len; i++) {
        DisplayListener l = (DisplayListener) DListen.elementAt(i);
        RemoteVSlave.addDisplayListener(l);
      }
    }
    else {
      for (int i=0; i<len; i++) {
        DisplayListener l = (DisplayListener) DListen.elementAt(i);
        VDisplay.addDisplayListener(l);
      }
    }
  }

  /**
   * Temporarily detaches all display listeners.
   */
  private void detachListeners() {
    int len = DListen.size();
    if (IsSlave) {
      for (int i=0; i<len; i++) {
        DisplayListener l = (DisplayListener) DListen.elementAt(i);
        RemoteVSlave.removeDisplayListener(l);
      }
    }
    else {
      for (int i=0; i<len; i++) {
        DisplayListener l = (DisplayListener) DListen.elementAt(i);
        VDisplay.removeDisplayListener(l);
      }
    }
  }

  // -- Add, remove & notify SSCellListeners --

  /**
   * Adds an SSCellListener.
   */
  public void addSSCellListener(SSCellListener l) {
    synchronized (SListen) {
      if (!SListen.contains(l)) SListen.add(l);
    }
  }

  /**
   * Removes an SSCellListener.
   */
  public void removeSSCellListener(SSCellListener l) {
    synchronized (SListen) {
      SListen.remove(l);
    }
  }

  /**
   * Removes all SSCellListeners.
   */
  public void removeAllSSCellListeners() {
    synchronized (SListen) {
      SListen.removeAllElements();
    }
  }

  /**
   * Informs all SSCellListeners of cell change.
   */
  void notifySSCellListeners(int changeType) {
    notifySSCellListeners(changeType, null);
  }

  /**
   * Informs all SSCellListeners of a cell change.
   */
  void notifySSCellListeners(int changeType, String varName) {
    SSCellChangeEvent e = new SSCellChangeEvent(this, changeType, varName);
    int len;
    SSCellListener[] l;
    synchronized (SListen) {
      len = SListen.size();
      l = new SSCellListener[len];
      for (int i=0; i<len; i++) l[i] = (SSCellListener) SListen.elementAt(i);
    }
    for (int i=0; i<len; i++) l[i].ssCellChanged(e);
  }


  // --- ACCESSORS ---

  /**
   * Gets this cell's name.
   */
  public String getName() {
    return Name;
  }

  /**
   * Gets whether this cell is a cloned display cell.
   */
  public boolean isRemote() {
    return IsRemote;
  }

  /**
   * Gets whether this cell is a slaved display cell.
   */
  public boolean isSlave() {
    return IsSlave;
  }

  /**
   * Gets the id number this cell uses for remote collaboration.
   */
  public int getRemoteId() {
    return CollabID;
  }

  /**
   * Gets this cell's formula manager.
   */
  public FormulaManager getFormulaManager() {
    return fm;
  }

  /**
   * Gets this cell's VisAD Display.
   */
  public DisplayImpl getDisplay() {
    return VDisplay;
  }

  /**
   * Gets this cell's VisAD RemoteDisplay.
   */
  public RemoteDisplay getRemoteDisplay() {
    return RemoteVDisplay;
  }

  /**
   * Gets this cell's mappings.
   */
  public ScalarMap[] getMaps() {
    Vector mapVector = null;
    if (IsSlave) {
      // SLAVE: get mappings from remote display
      try {
        mapVector = RemoteVDisplay.getMapVector();
      }
      catch (VisADException exc) {
        if (DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        if (DEBUG) exc.printStackTrace();
      }
    }
    else if (VDisplay != null) {
      // get mappings from local display
      mapVector = VDisplay.getMapVector();
    }

    int len = (mapVector == null ? 0 : mapVector.size());
    ScalarMap[] maps = (len > 0 ? new ScalarMap[len] : null);
    for (int i=0; i<len; i++) maps[i] = (ScalarMap) mapVector.elementAt(i);
    return maps;
  }

  /**
   * Gets this cell's dimension.
   * @return  Dimension type. Valid types are:
   *          <UL>
   *          <LI>BasicSSCell.JAVA3D_3D
   *          <LI>BasicSSCell.JAVA2D_2D
   *          <LI>BasicSSCell.JAVA3D_2D
   *          </UL>
   */
  public int getDimension() {
    return Dim;
  }

  /**
   * Gets this cell's Data object with the specified variable name.
   */
  public Data getData(String varName) {
    SSCellData cellData;
    synchronized (CellData) {
      cellData = getCellDataByName(varName);
    }
    return cellData == null ? null : cellData.getData();
  }

  /**
   * Gets this cell's Data objects.
   */
  public Data[] getData() {
    synchronized (CellData) {
      int len = CellData.size();
      Data[] data = new Data[len];
      for (int i=0; i<len; i++) {
        SSCellData cellData = (SSCellData) CellData.elementAt(i);
        data[i] = cellData.getData();
      }
      return data;
    }
  }

  /**
   * Gets this cell's DataReference with the specified variable name.
   */
  public DataReference getReference(String varName) {
    SSCellData cellData;
    synchronized (CellData) {
      cellData = getCellDataByName(varName);
    }
    return cellData == null ? null : cellData.getReference();
  }

  /**
   * Gets this cell's DataReferences.
   */
  public DataReferenceImpl[] getReferences() {
    synchronized (CellData) {
      int len = CellData.size();
      DataReferenceImpl[] refs = new DataReferenceImpl[len];
      for (int i=0; i<len; i++) {
        SSCellData cellData = (SSCellData) CellData.elementAt(i);
        refs[i] = cellData.getReference();
      }
      return refs;
    }
  }

  /**
   * Gets this cell's remote DataReference for data
   * with the specified variable name.
   */
  public RemoteDataReference getRemoteReference(String varName) {
    SSCellData cellData;
    synchronized (CellData) {
      cellData = getCellDataByName(varName);
    }
    return cellData == null ? null : cellData.getRemoteReference();
  }

  /**
   * Gets this cell's remote DataReferences.
   */
  public RemoteDataReference[] getRemoteReferences() {
    synchronized (CellData) {
      int len = CellData.size();
      RemoteDataReference[] remoteRefs = new RemoteDataReference[len];
      for (int i=0; i<len; i++) {
        SSCellData cellData = (SSCellData) CellData.elementAt(i);
        remoteRefs[i] = cellData.getRemoteReference();
      }
      return remoteRefs;
    }
  }

  /**
   * Gets this cell's data source type for data
   * with the specified variable name.
   */
  public int getDataSourceType(String varName) {
    SSCellData cellData;
    synchronized (CellData) {
      cellData = getCellDataByName(varName);
    }
    return cellData == null ? UNKNOWN_SOURCE : cellData.getSourceType();
  }

  /**
   * Gets this cell's data source types.
   */
  public int[] getDataSourceTypes() {
    synchronized (CellData) {
      int len = CellData.size();
      int[] types = new int[len];
      for (int i=0; i<len; i++) {
        SSCellData cellData = (SSCellData) CellData.elementAt(i);
        types[i] = cellData.getSourceType();
      }
      return types;
    }
  }

  /**
   * Gets this cell's data source string for data
   * with the specified variable name.
   */
  public String getDataSource(String varName) {
    SSCellData cellData;
    synchronized (CellData) {
      cellData = getCellDataByName(varName);
    }
    return cellData == null ? null : cellData.getSource();
  }

  /**
   * Gets this cell's data source strings.
   */
  public String[] getDataSources() {
    synchronized (CellData) {
      int len = CellData.size();
      String[] sources = new String[len];
      for (int i=0; i<len; i++) {
        SSCellData cellData = (SSCellData) CellData.elementAt(i);
        sources[i] = cellData.getSource();
      }
      return sources;
    }
  }

  /**
   * Gets the variable name of this cell's first Data object.
   */
  public String getFirstVariableName() {
    String varName = null;
    synchronized (CellData) {
      if (CellData.size() > 0) {
        SSCellData cellData = (SSCellData) CellData.firstElement();
        varName = cellData.getVariableName();
      }
    }
    return varName;
  }

  /**
   * Gets the variable name of this cell's last Data object.
   */
  public String getLastVariableName() {
    String varName = null;
    synchronized (CellData) {
      if (CellData.size() > 0) {
        SSCellData cellData = (SSCellData) CellData.lastElement();
        varName = cellData.getVariableName();
      }
    }
    return varName;
  }

  /**
   * Gets the variable names of this cell's Data objects.
   */
  public String[] getVariableNames() {
    synchronized (CellData) {
      int len = CellData.size();
      String[] varNames = new String[len];
      for (int i=0; i<len; i++) {
        SSCellData cellData = (SSCellData) CellData.elementAt(i);
        varNames[i] = cellData.getVariableName();
      }
      return varNames;
    }
  }

  /**
   * Gets the number of Data object this cell has.
   */
  public int getDataCount() {
    return CellData.size();
  }

  /**
   * Whether this cell has any data.
   */
  public boolean hasData() {
    return CellData.size() > 0;
  }

  /**
   * Whether this cell has a valid display on-screen.
   */
  public boolean hasDisplay() {
    return HasDisplay;
  }

  /**
   * Whether this cell has any mappings.
   */
  public boolean hasMappings() {
    if (IsRemote) {
      // CLIENT: check mappings from remote display
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

  /**
   * Whether other cells are dependent on this cell's Data object
   * with the specified variable name.
   */
  public boolean othersDepend(String varName) {
    SSCellData cellData;
    synchronized (CellData) {
      cellData = getCellDataByName(varName);
    }
    return cellData.othersDepend();
  }

  /**
   * Whether other cells are dependent on any of this cell's Data objects.
   */
  public boolean othersDepend() {
    synchronized (CellData) {
      int len = CellData.size();
      for (int i=0; i<len; i++) {
        SSCellData cellData = (SSCellData) CellData.elementAt(i);
        if (cellData.othersDepend()) return true;
      }
      return false;
    }
  }


  // --- DEPRECATED METHODS ---

  /**
   * @deprecated Use addVar(String, ThingReference) instead.
   */
  public static void createVar(String name, ThingReference tr)
    throws VisADException
  {
    defaultFM.createVar(name, tr);
  }

  /**
   * @deprecated Use addSSCellListener(SSCellListener) instead.
   */
  public void addSSCellChangeListener(SSCellListener l) {
    addSSCellListener(l);
  }

  /**
   * @deprecated Use removeSSCellListener(SSCellListener) instead.
   */
  public void removeListener(SSCellListener l) {
    removeSSCellListener(l);
  }

  /**
   * @deprecated Use removeAllSSCellListeners() instead.
   */
  public void removeAllListeners() {
    removeAllSSCellListeners();
  }

  /**
   * @deprecated Use setSaveString(String) instead.
   */
  public void setSSCellString(String save)
    throws VisADException, RemoteException
  {
    setSaveString(save);
  }

  /**
   * @deprecated Use getSaveString() instead.
   */
  public String getSSCellString() {
    return getSaveString();
  }

  /**
   * @deprecated Use saveData(String, Form) instead.
   */
  public void saveData(File f, boolean netcdf)
    throws BadFormException, IOException, VisADException, RemoteException
  {
    Form form;
    if (netcdf) form = new visad.data.netcdf.Plain();
    else form = new visad.data.visad.VisADForm();
    saveData(getFirstVariableName(), f.getPath(), form);
  }

  /**
   * @deprecated Use saveData(String, Form) instead.
   */
  public void saveData(File f, Form form)
    throws BadFormException, IOException, VisADException, RemoteException
  {
    saveData(getFirstVariableName(), f.getPath(), form);
  }

  /**
   * @deprecated Use addData(Data) instead.
   */
  public void setData(Data data) throws VisADException, RemoteException {
    removeAllReferences();
    addData(data);
  }

  /**
   * @deprecated Use addDataSource(String, FORMULA_SOURCE) instead.
   */
  public void setFormula(String f)
    throws VisADException, RemoteException
  {
    removeAllReferences();
    addDataSource(f, FORMULA_SOURCE);
  }

  /**
   * @deprecated Use waitForData(String) instead.
   */
  public void waitForFormula() throws VisADException, RemoteException {
    waitForData();
  }

  /**
   * @deprecated Use getReference(String) instead.
   */
  public DataReferenceImpl getDataRef() {
    return (getDataCount() > 0 ? (DataReferenceImpl)
      getReference(getFirstVariableName()) : null);
  }

  /**
   * @deprecated Use getRemoteReference(String) instead.
   */
  public RemoteDataReferenceImpl getRemoteDataRef() {
    return (getDataCount() > 0 ?  (RemoteDataReferenceImpl)
      getRemoteReference(getFirstVariableName()) : null);
  }

  /**
   * @deprecated Use getDataSource(String) instead.
   */
  public URL getFileURL() {
    URL url = null;
    String varName = getFirstVariableName();
    if (getDataCount() > 0 && getDataSourceType(varName) == URL_SOURCE) {
      try {
        url = new URL(getDataSource(varName));
      }
      catch (MalformedURLException exc) {
        if (DEBUG && DEBUG_LEVEL >= 3) exc.printStackTrace();
      }
    }
    return url;
  }

  /**
   * @deprecated Use getDataSource(String) instead.
   */
  public String getFilename() {
    String filename = "";
    String varName = getFirstVariableName();
    if (getDataCount() > 0 && getDataSourceType(varName) == URL_SOURCE) {
      filename = getDataSource(varName);
    }
    return filename;
  }

  /**
   * @deprecated Use getDataSource(String) instead.
   */
  public String getRMIAddress() {
    String rmi = null;
    String varName = getFirstVariableName();
    if (getDataCount() > 0  && getDataSourceType(varName) == RMI_SOURCE) {
      rmi = getDataSource(varName);
    }
    return rmi;
  }

  /**
   * @deprecated Use getDataSource(String) instead.
   */
  public String getFormula() {
    String formula = "";
    String varName = getFirstVariableName();
    if (getDataCount() > 0 && getDataSourceType(varName) == FORMULA_SOURCE) {
      formula = getDataSource(varName);
    }
    return formula;
  }

  /**
   * @deprecated Use addDataSource(String, URL_SOURCE) instead.
   */
  public void loadData(URL u) throws VisADException, RemoteException {
    addDataSource(u.toString(), URL_SOURCE);
  }

  /**
   * @deprecated Use addDataSource(String, URL_SOURCE) instead.
   */
  public void loadData(String s)
    throws VisADException, RemoteException
  {
    addDataSource(s, URL_SOURCE);
  }

  /**
   * @deprecated Use addDataSource(String, RMI_SOURCE) instead.
   */
  public void loadRMI(String s)
    throws VisADException, RemoteException
  {
    addDataSource(s, RMI_SOURCE);
  }

  /**
   * @deprecated Use setDimension(int) instead.
   */
  public void setDimension(boolean twoD, boolean java2d)
    throws VisADException, RemoteException
  {
    int dim;
    if (!twoD && java2d) return;
    if (!twoD && !java2d) dim = JAVA3D_3D;
    else if (twoD && java2d) dim = JAVA2D_2D;
    else dim = JAVA3D_2D; // twoD && !java2d
    setDimension(dim);
  }

  /**
   * @deprecated Use getDataSourceType(String) instead.
   */
  public boolean hasFormula() {
    return (getDataCount() > 0 &&
      getDataSourceType(getFirstVariableName()) == FORMULA_SOURCE);
  }

  /**
   * @deprecated Use getReference(String) instead.
   */
  public DataReference getReference() {
    return (getDataCount() > 0 ? getReference(getFirstVariableName()) : null);
  }

  /**
   * @deprecated Use visad.DataUtility.stringsToTuple(String[]) instead.
   */
  public static Tuple stringsToTuple(String[] s) {
    return DataUtility.stringsToTuple(s, DEBUG);
  }

  /**
   * @deprecated Use visad.DataUtility.tupleToStrings(Tuple) instead.
   */
  public static String[] tupleToStrings(Tuple t) {
    return DataUtility.tupleToStrings(t, DEBUG);
  }

  /**
   * @deprecated Use visad.DataUtility.makeLocal(data) instead.
   */
  public static DataImpl makeLocal(Data data) {
    return DataUtility.makeLocal(data, DEBUG && DEBUG_LEVEL >= 3);
  }

  /**
   * @deprecated Use visad.Util.arraysEqual(Object[], Object[]) instead.
   */
  public static boolean arraysEqual(Object[] o1, Object[] o2) {
    return Util.arraysEqual(o1, o2);
  }

  /**
   * @deprecated Use visad.Util.invoke(boolean, Runnable) instead.
   */
  public static void invoke(boolean wait, Runnable r) {
    Util.invoke(wait, DEBUG, r);
  }

}
