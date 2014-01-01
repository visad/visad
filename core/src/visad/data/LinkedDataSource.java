//
// LinkedDataSource.java
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

package visad.data;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.*;

/**
 * A class for linking a data source (e.g., a URL) with a DataReference.
 * Whenever the data changes at the source, the new data is automatically
 * loaded and the DataReference is set to point to it. VisAD applications
 * can then use a CellImpl with the DataReference object in order to detect
 * changes to the data at its source.
 */
public abstract class LinkedDataSource {

  /**
   * Debugging flag.
   */
  protected static final boolean DEBUG = false;

  /**
   * The name of this LinkedDataSource.
   */
  protected String name;

  /**
   * The DataReference for this LinkedDataSource.
   */
  private DataReferenceImpl ref;

  /**
   * Whether the connection to the data source is still alive.
   */
  private boolean alive;

  /**
   * Construct a LinkedDataSource with the given name.
   */
  public LinkedDataSource(String name) {
    this.name = name;
    try {
      ref = new DataReferenceImpl(name);
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    alive = false;
  }

  /**
   * Load initial data from the given data source and remain linked
   * to the data source, monitoring it for changes to the data.
   */
  public abstract void open(String id)
    throws IOException, VisADException, RemoteException;

  /**
   * Update the data to which this LinkedDataSource is linked.
   */
  public void dataChanged(Data data) throws VisADException, RemoteException {
    if (data == null) alive = false;
    else ref.setData(data);
  }

  /**
   * Return the name of this LinkedDataSource.
   */
  public String getName() {
    return name;
  }

  /**
   * Return the DataReference for this LinkedDataSource.
   */
  public DataReferenceImpl getReference() {
    return ref;
  }

  /**
   * Return whether the connection to the data source is still alive.
   */
  public boolean isAlive() {
    return alive;
  }

}

