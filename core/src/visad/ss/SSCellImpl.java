//
// SSCellImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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
 * A VisAD Cell which updates an SSCell
 * when one of its Data objects change.
 */
public class SSCellImpl extends CellImpl {

  /**
   * VisAD Data object representing boolean true.
   */
  public static final Real TRUE = new Real(1.0);

  /**
   * VisAD Data object representing boolean false.
   */
  public static final Real FALSE = new Real(0.0);

  /**
   * SSCellData that this cell updates when its data changes.
   */
  private SSCellData cellData;

  /**
   * Data reference that this cell monitors for changes.
   */
  private DataReferenceImpl ref;

  /**
   * Name of the linked SSCell.
   */
  private String cellName;

  /**
   * Variable name of the linked data.
   */
  private String varName;

  /**
   * Errors generated from computing the linked data.
   */
  private String[] errors;

  /**
   * Formula manager for the linked SSCell.
   */
  private FormulaManager fm;

  /**
   * Flag marking whether this cell has finished initializing.
   */
  private boolean inited = false;

  /**
   * Whether this cell should broadcast data changes.
   */
  private int skipNotify = 1; // skip notify from initial doAction() call

  /**
   * Whether this cell should check for updated data computation errors.
   */
  private int skipErrors = 0;

  /**
   * Constructs an SSCellImpl.
   */
  public SSCellImpl(SSCellData cellData, DataReferenceImpl ref, String varName,
    boolean checkErrors) throws VisADException, RemoteException
  {
    this.cellData = cellData;
    this.ref = ref;
    cellName = cellData.ssCell.getName();
    this.varName = varName;
    fm = cellData.ssCell.getFormulaManager();
    inited = false;
    if (!checkErrors) skipNextErrors();
    addReference(ref);
  }

  /**
   * Returns whether this cell has finished initializing.
   */
  public boolean isInited() {
    return inited;
  }

  /**
   * Returns the errors relevant to the linked data.
   */
  String[] getErrors() {
    return errors;
  }

  /**
   * Disables broadcasting of data changes during next data update.
   */
  void skipNextNotify() {
    skipNotify++;
  }

  /**
   * Disables detection of errors during next data update.
   */
  void skipNextErrors() {
    skipErrors++;
  }

  /**
   * Invoked when linked data changes.
   */
  public synchronized void doAction() {
    // get new data
    Data data = ref.getData();

    // broadcast new errors, if any
    if (skipErrors == 0) {
      cellData.setErrors(fm.getErrors(varName), true, false);
    }
    else skipErrors--;

    if (data != null) {
      // update cell display
      cellData.ssCell.updateDisplay(true);

      // add data's ScalarTypes to FormulaManager variable registry
      Vector v = new Vector();
      try {
        DataUtility.getScalarTypes(new Data[] {data}, v, false, true);
      }
      catch (VisADException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
      int len = v.size();
      for (int i=0; i<len; i++) {
        ScalarType st = (ScalarType) v.elementAt(i);
        if (st instanceof RealType) {
          RealType rt = (RealType) st;
          try {
            fm.setThing(rt.getName(), VRealType.get(rt));
          }
          catch (VisADException exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
          }
          catch (RemoteException exc) {
            if (BasicSSCell.DEBUG) exc.printStackTrace();
          }
        }
      }

      // notify linked cells of data change
      if (skipNotify == 0) {
        try {
          cellData.ssCell.sendMessage(BasicSSCell.UPDATE_DATA, varName, data);
        }
        catch (RemoteException exc) {
          if (BasicSSCell.DEBUG) exc.printStackTrace();
        }
      }
      else skipNotify--;
    }
    else cellData.ssCell.updateDisplay();

    // update dependencies for all cells
    cellData.ssCell.updateDependencies();

    // broadcast data change event
    cellData.ssCell.notifySSCellListeners(
      SSCellChangeEvent.DATA_CHANGE, varName);

    inited = true;
  }

}
