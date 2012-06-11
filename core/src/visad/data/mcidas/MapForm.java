//
// MapForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.mcidas;

import visad.*;
import visad.data.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.net.URL;

/**
   MapForm is the Map data format adapter for
   serialized visad.Data objects.<P>
*/
public class MapForm extends Form implements FormFileInformer {

  /** counter @serialized*/
  private static int num = 0;

  /**
   * Construct a Form for reading in McIDAS map files
   */
  public MapForm() {
    super("MapForm" + num++);
  }

  /**
   * Determines if this is a McIDAS map file from the name
   * @param  name  name of the file
   * @return  true if it matches the pattern for McIDAS map files (OUTL*)
   */
  public boolean isThisType(String name) {
    return (name.indexOf("OUTL") >= 0);
  }

  /**
   * Determines if this is a McIDAS map file from the starting block
   * @param  block  block of data to check
   * @return  false  - there is no identifying block in a McIDAS map file
   */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /**
   * Get a list of default suffixes for McIDAS map files
   * @return  valid list of suffixes
   */
  public String[] getDefaultSuffixes() {
    String[] suff = { " " };
    return suff;
  }

  /**
   * Save a VisAD data object in this form
   * @throws  UnimplementedException  - can't be done yet.
   */
  public synchronized void save(String id, Data data, boolean replace)
         throws BadFormException, IOException, RemoteException, VisADException {
    throw new UnimplementedException("Can't yet save McIDAS map files");
  }

  /**
   * Add data to an existing data object
   * @throws BadFormException
   */
  public synchronized void add(String id, Data data, boolean replace)
         throws BadFormException {
    throw new BadFormException("MapForm.add");
  }

  /**
   * Open the file specified by the string
   * @param  id   string representing the path to the file
   * @return a Data object representing the map lines. 
   */
  public synchronized DataImpl open(String id)
         throws BadFormException, IOException, VisADException {
    try {
      BaseMapAdapter ba = new BaseMapAdapter(id);
      return ba.getData();

    } catch (IOException e) {
      throw new VisADException("IOException: " + e.getMessage());
    }
  }

  /**
   * Open the file specified by the URL
   * @param  url   URL of the remote map file
   * @return a Data object representing the map lines. 
   */
  public synchronized DataImpl open(URL url)
         throws BadFormException, VisADException, IOException {
    BaseMapAdapter ba = new BaseMapAdapter(url);
    return ba.getData();
  }

  /**
   * Return the data forms that are compatible with a data object
   * @return null
   */
  public synchronized FormNode getForms(Data data) {
    return null;
  }
}
