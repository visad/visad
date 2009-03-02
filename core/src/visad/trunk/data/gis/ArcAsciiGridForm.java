//
// ArcAsciiGridForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.gis;

import visad.*;
import visad.data.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.net.URL;


/**
 * ArcAsciiGridForm is the ARC/INFO ASCIIGRID data format adapter for
 * serialized visad.Data objects.
 * @see http://www.climatesource.com/format/arc_asciigrid.html for more info
 */
public class ArcAsciiGridForm extends Form implements FormFileInformer {

  /** counter @serialized*/
  private static int num = 0;
  private MathType mathType = null;

  /**
   * Construct a Form for reading in ARC/INFO ASCIIGRID files
   */
  public ArcAsciiGridForm() {
    super("ArcAsciiGridForm" + num++);
  }

  /**
   * Construct a Form for reading in Arc/Info ASCIIGRID files
   */
  public ArcAsciiGridForm(MathType dataType) {
    super("ArcAsciiGridForm" + num++);
    this.mathType = dataType;
  }

  /**
   * Determines if this is a ARC/INFO ASCIIGRID file from the name
   * @param  name  name of the file
   * @return  true if it matches the pattern for ARC/INFO ASCIIGRID files
   */
  public boolean isThisType(String name) {
    return (name.endsWith(".dem") ||
            name.endsWith(".asc"));
  }

  /**
   * Determines if this is a ARC/INFO ASCIIGRID file from the starting block
   * @param  block  block of data to check
   * @return  false  - there is no identifying block in a ARC/INFO ASCIIGRID file
   */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /**
   * Get a list of default suffixes for McIDAS map files
   * @return  valid list of suffixes
   */
  public String[] getDefaultSuffixes() {
    String[] suff = { ".dem", ".asc" };
    return suff;
  }

  /**
   * Save a VisAD data object in this form
   * @throws  UnimplementedException  - can't be done yet.
   */
  public synchronized void save(String id, Data data, boolean replace)
         throws BadFormException, IOException, RemoteException, VisADException {
    throw new UnimplementedException("Can't yet save ARC/INFO ASCIIGRID files");
  }

  /**
   * Add data to an existing data object
   * @throws BadFormException
   */
  public synchronized void add(String id, Data data, boolean replace)
         throws BadFormException {
    throw new BadFormException("ArcAsciiGridForm.add");
  }

  /**
   * Open the file specified by the string
   * @param  id   string representing the path to the file
   * @return a Data object representing the map lines. 
   */
  public synchronized DataImpl open(String id)
         throws BadFormException, VisADException {

    ArcAsciiGridAdapter demAdapter = new ArcAsciiGridAdapter(id);
    return 
      (mathType == null) 
          ? demAdapter.getData()
          : demAdapter.getData(mathType);

  }

  /**
   * Open the file specified by the URL
   * @param  id   URL of the remote map file
   * @return a Data object representing the map lines. 
   */
  public synchronized DataImpl open(URL url)
         throws BadFormException, VisADException, IOException {
    throw new UnimplementedException("Can't open ARC/INFO ASCIIGRID files from URL");
  }

  /**
   * Return the data forms that are compatible with a data object
   * @return null
   */
  public synchronized FormNode getForms(Data data) {
    return null;
  }
}
