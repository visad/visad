//
// SSCellData.java
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

import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;
import visad.formula.*;
import visad.util.DataUtility;

/**
 * Class for encapsulating all needed information
 * about a Data object present in a BasicSSCell.
 */
public class SSCellData {

  /**
   * Associated spreadsheet cell for the data.
   */
  private BasicSSCell ssCell;

  /**
   * The id number the Data object.
   */
  private int id;

  /**
   * The DataReference that points to the actual Data.
   */
  private DataReferenceImpl ref;

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
   * The variable name of the Data object.
   */
  private String name;

  /**
   * The formula manager of the data's spreadsheet cell.
   */
  private FormulaManager fm;

  /**
   * VisAD CellImpl for monitoring changes to the data.
   */
  private CellImpl cell;

  /**
   * Constructs a new SSCellData object, for encapsulating
   * a Data object and related information.
   */
  public SSCellData(BasicSSCell ssCell, DataReferenceImpl ref,
    String source, int type) throws VisADException, RemoteException
  {
    this.ssCell = ssCell;
    this.id = ssCell.getFirstFreeID();
    this.ref = ref;
    this.remoteRef = new RemoteDataReferenceImpl(ref);
    this.source = source;
    this.type = type;
    this.name = ssCell.getName() + "d" + id;
    this.fm = ssCell.getFormulaManager();

    // add data reference to formula manager database
    fm.createVar(name, ref);

    // detect changes to the data
    final DataReference fref = ref;
    final BasicSSCell fssCell = ssCell;
    this.cell = new CellImpl() {
      public void doAction() {
        // clear old errors
        fssCell.setErrors(null);

        // get new data
        Data data = null;
        try {
          data = fref.getData();
        }
        catch (VisADException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
        if (data != null) {
          // update cell's data
          fssCell.setVDPanel(true);

          // add data's RealTypes to FormulaManager variable registry
          Vector v = new Vector();
          try {
            DataUtility.getRealTypes(data, v);
          }
          catch (VisADException exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
          }
          catch (RemoteException exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
          }
          int len = v.size();
          for (int i=0; i<len; i++) {
            RealType rt = (RealType) v.elementAt(i);
            try {
              fm.setThing(rt.getName(), new VRealType(rt));
            }
            catch (VisADException exc) {
              if (BasicSSCell.DEBUG) exc.printStackTrace();
            }
            catch (RemoteException exc) {
              if (BasicSSCell.DEBUG) exc.printStackTrace();
            }
          }
        }

        // display new errors, if any
        String[] es = fm.getErrors(name);
        if (es != null) fssCell.setErrors(es);

        // broadcast data change event
        fssCell.notifySSCellListeners(SSCellChangeEvent.DATA_CHANGE, name);
      }
    };
    cell.addReference(ref);
  }

  /**
   * Gets the ID number.
   */
  protected int getID() {
    return id;
  }

  /**
   * Gets the Data object.
   */
  protected Data getData() {
    return ref.getData();
  }

  /**
   * Gets the DataReference pointing to the data.
   */
  protected DataReferenceImpl getReference() {
    return ref;
  }

  /**
   * Gets the remote copy of the DataReference.
   */
  protected RemoteDataReferenceImpl getRemoteReference() {
    return remoteRef;
  }

  /**
   * Gets the source of the data, in String form.
   */
  protected String getSource() {
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
  protected int getSourceType() {
    return type;
  }

  /**
   * Gets the variable name used for the data in the formula manager.
   */
  protected String getVariableName() {
    return name;
  }

  /**
   * Stops monitoring the data for changes.
   */
  protected void destroy() {
    try {
      fm.remove(name);
    }
    catch (FormulaException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    try {
      this.cell.removeAllReferences();
    }
    catch (VisADException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    this.cell.stop();
    this.cell = null;
  }

}
