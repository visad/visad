//
// SSCellData.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;
import visad.*;
import visad.formula.*;
import visad.util.*;

/**
 * Class for encapsulating all needed information
 * about a Data object present in a BasicSSCell.
 */
public class SSCellData {

  // --- FIELDS ---

  /**
   * Associated spreadsheet cell for the data.
   */
  BasicSSCell ssCell;

  /**
   * The id number the Data object.
   */
  private int id;

  /**
   * The DataReference that points to the actual Data.
   */
  private DataReferenceImpl ref;

  /**
   * The ConstantMaps associated with the reference.
   */
  private ConstantMap[] cmaps;

  /**
   * Remote copy of the DataReference.
   */
  private RemoteDataReferenceImpl remoteRef;

  /**
   * The Data object's source, in string form.
   */
  private String source;

  /**
   * The Data object's source type.
   */
  private int type;

  /**
   * The name of the Data object's associated spreadsheet cell.
   */
  private String cellName;

  /**
   * The variable name of the Data object.
   */
  private String varName;

  /**
   * Errors encountered when computing the Data object.
   */
  private String[] errors;

  /**
   * The formula manager of the data's spreadsheet cell.
   */
  private FormulaManager fm;

  /**
   * Whether other data depends on this data.
   */
  boolean othersDepend;

  /**
   * VisAD Cell for monitoring local data changes.
   */
  SSCellImpl cell;


  // --- CONSTRUCTORS ---

  /**
   * Constructs a new SSCellData object, for encapsulating
   * a Data object and related information.
   */
  public SSCellData(int id, BasicSSCell ssCell, DataReferenceImpl ref,
    ConstantMap[] cmaps, String source, int type, boolean checkErrors)
    throws VisADException, RemoteException
  {
    this.ssCell = ssCell;
    this.id = id;
    this.ref = (DataReferenceImpl) ref;
    this.remoteRef = new RemoteDataReferenceImpl(ref);
    this.source = source;
    this.type = type;
    this.cellName = ssCell.getName();
    this.varName = cellName + "d" + id;
    this.errors = new String[0];
    this.othersDepend = false;
    this.fm = ssCell.getFormulaManager();

    // set variable name's reference in formula manager database
    fm.setReference(varName, ref);
    if (this.id == 1) {
      // make CELL default to CELLd1
      fm.setReference(cellName, ref);
    }

    // detect changes to the data
    this.cell = new SSCellImpl(this, ref, varName, checkErrors);
  }


  // --- ACCESSORS ---

  /**
   * Gets the ID number.
   */
  public int getId() {
    return id;
  }

  /**
   * Gets the Data object.
   */
  public Data getData() {
    return ref.getData();
  }

  /**
   * Gets the DataReference pointing to the data. Changes to the
   * reference's data automatically propagate to all linked cells.
   */
  public DataReferenceImpl getReference() {
    return ref;
  }

  /**
   * Gets the ConstantMaps associated with the reference.
   */
  public ConstantMap[] getConstantMaps() {
    return cmaps;
  }

  /**
   * Gets the remote copy of the DataReference.
   */
  public RemoteDataReferenceImpl getRemoteReference() {
    return remoteRef;
  }

  /**
   * Gets the source of the data, in String form.
   */
  public String getSource() {
    return source;
  }

  /**
   * Gets the source type of the data.
   * @return Source type. Valid types are:
   *         <UL>
   *         <LI>BasicSSCell.UNKNOWN_SOURCE
   *         <LI>BasicSSCell.DIRECT_SOURCE
   *         <LI>BasicSSCell.URL_SOURCE
   *         <LI>BasicSSCell.FORMULA_SOURCE
   *         <LI>BasicSSCell.RMI_SOURCE
   *         <LI>BasicSSCell.REMOTE_SOURCE
   *         </UL>
   */
  public int getSourceType() {
    return type;
  }

  /**
   * Gets the variable name used for the data in the formula manager.
   */
  public String getVariableName() {
    return varName;
  }

  /**
   * Gets the errors encountered when generating the Data object.
   */
  public String[] getErrors() {
    return errors;
  }

  /**
   * Gets whether other data depends on this data.
   */
  public boolean othersDepend() {
    return othersDepend;
  }

  /**
   * Returns whether this data's cell has finished initializing.
   */
  public boolean isInited() {
    return cell.isInited();
  }


  // --- MODIFIERS ---

  /**
   * Sets the data.
   */
  public void setData(Data data) throws VisADException, RemoteException {
    setData(data, true);
  }
  
  /**
   * Sets the data, broadcasting data change notification if flag is set.
   */
  void setData(Data data, boolean notify)
    throws VisADException, RemoteException
  {
    DataImpl d = data.local();
    if (!notify) cell.skipNextNotify();
    ref.setData(d);
  }

  /**
   * Sets a single error for the Data object.
   */
  public void setError(String error) {
    setErrors(new String[] {error}, true, true);
  }

  /**
   * Sets the errors for the Data object.
   */
  public void setErrors(String[] errors) {
    setErrors(errors, true, true);
  }

  /**
   * Sets the errors for the Data object, notifying
   * linked cells if notify flag is set.
   */
  void setErrors(String[] errors, boolean notify) {
    setErrors(errors, notify, true);
  }

  /**
   * Sets the errors for the Data object, notifying linked cells if
   * notify flag is set, and updating display if update flag is set.
   */
  void setErrors(String[] errors, boolean notify, boolean update) {
    if (Util.arraysEqual(this.errors, errors)) return;
    this.errors = errors;
    if (update) ssCell.updateDisplay();
    if (notify) {
      try {
        ssCell.sendMessage(BasicSSCell.SET_ERRORS, varName,
          DataUtility.stringsToTuple(errors, BasicSSCell.DEBUG));
      }
      catch (RemoteException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
    }
  }

  /**
   * Sets whether others depend on this data.
   */
  public void setDependencies(Real real) {
    othersDepend = real.equals(SSCellImpl.TRUE);
  }

  /**
   * Stops monitoring the data for changes.
   */
  public void destroy() {
    // set data's variable to null in formula manager database
    try {
      fm.setThing(varName, null);
      if (id == 1) fm.setThing(cellName, null);
    }
    catch (VisADException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }

    // stop local data change monitoring
    try {
      cell.removeAllReferences();
    }
    catch (VisADException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    cell.stop();
    cell = null;

    // broadcast data change event
    ssCell.notifySSCellListeners(SSCellChangeEvent.DATA_CHANGE, varName);
  }

}
