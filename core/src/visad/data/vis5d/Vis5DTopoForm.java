//
// Vis5DTopoForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.vis5d;

import visad.*;
import visad.data.*;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;

/**
   Vis5DTopoForm is the VisAD data format adapter for Vis5D topography files.<P>
*/
public class Vis5DTopoForm extends Form implements FormFileInformer {

  private static int num = 0;

  /**
   * Create a new Vis5DTopoForm
   */
  public Vis5DTopoForm() {
    super("Vis5DTopoForm" + num++);
  }

  /**
   * Check to see if the file name might be right for this form.
   * @param name   name of the file
   * @return  true if it might be a Vis5D topography file based on the name.
   */
  public boolean isThisType(String name) {
    return name.endsWith(".v5d") || name.endsWith("TOPO");
  }

  /**
   * Check to see if the block contains the magic number
   * @param block   block of bytes from file
   * @return  true if the first 4 bytes correspond to "TOPO"
   */
  public boolean isThisType(byte[] block) {
    String topo = new String(block, 0, 4);
    return topo.equals("TOPO");
  }

  /**
   * Get default suffixes for Vis5D topography files
   * @return  array of suffixes (.v5d, .TOPO)
   */
  public String[] getDefaultSuffixes() {
    String[] suff = { "v5d", "TOPO" };
    return suff;
  }

  /**
   * Save a VisAD data object in this form.
   * @param id  file id
   * @param data  Data object to save
   * @param replace  true to replace the existing file
   * @throws UnimplementedException  not implemented for this form
   */
  public synchronized void save(String id, Data data, boolean replace)
         throws BadFormException, IOException, RemoteException, VisADException {
    throw new UnimplementedException("Vis5DTopoForm.save");
  }

  /**
   * Add data to an existing data object.
   * @param id  file id
   * @param data  Data object to append to
   * @param replace  true to replace the existing file
   * @throws BadFormException  not applicable to this form
   */
  public synchronized void add(String id, Data data, boolean replace)
         throws BadFormException {
    throw new BadFormException("Vis5DTopoForm.add");
  }

  /**
   * Return the data forms that are compatible with a data object.
   * @param  data  Data object in question
   * @return null for this Form since it doesn't support save.
   */
  public synchronized FormNode getForms(Data data) {
    return null;
  }

  /**
   * Returns a VisAD data object corresponding to a URL pointing to a 
   * Vis5D topography file.
   *
   * @param url               URL pointing to the Vis5D topography
   * @return                  A VisAD data object corresponding to the Vis5D
   *                          topography file.
   * @throws BadFormException if not a Vis5D topo file.
   * @throws VisADException   if a problem occurs in core VisAD.  Probably a
   *                          VisAD object couldn't be created.
   * @throws IOException      if an I/O failure occurs.
   */
  public synchronized DataImpl open(URL url)
         throws BadFormException, VisADException, IOException {
    return open(url.openStream());
  }

  /**
   * Returns a VisAD data object corresponding to a Vis5D topography file.
   *
   * @param id                path to the existing Vis5D file.
   * @return                  A VisAD data object corresponding to the Vis5D
   *                          dataset.
   * @throws BadFormException if not a Vis5D topo file.
   * @throws VisADException   if a problem occurs in core VisAD.  Probably a
   *                          VisAD object couldn't be created.
   * @throws IOException      if an I/O failure occurs.
   */
  public synchronized DataImpl open(String id)
         throws BadFormException, IOException, VisADException {
    return open(new FileInputStream(id));
  }

  /**
   * Returns a VisAD data object corresponding to an input stream for
   * a Vis5DTopography file.
   *
   * @param in              Input stream
   * @return                  A VisAD data object corresponding to the Vis5D
   *                          topo file.
   * @throws BadFormException if not a Vis5D topo file.
   * @throws VisADException   if a problem occurs in core VisAD.  Probably a
   *                          VisAD object couldn't be created.
   * @throws IOException      if an I/O failure occurs.
   */
  public synchronized DataImpl open(InputStream in)
         throws BadFormException, IOException, VisADException {

    DataInputStream din = new DataInputStream (new BufferedInputStream(in));
    byte[] type = new byte[40];
    int ok = din.read(type, 0, 40);
    String header = new String(type);
    boolean oldStyle;
    if (header.startsWith("TOPO2")) {
      oldStyle = false;
    } else if (header.startsWith("TOPO")) {
      oldStyle = true;
    } else {
      throw new BadFormException("Vis5DTopoForm.open: not a Vis5D TOPO file");
    }
    float westLon, eastLon, northLat, southLat;
    if (oldStyle) {
      westLon = din.readInt()/100.f;
      eastLon = din.readInt()/100.f;
      northLat = din.readInt()/100.f;
      southLat = din.readInt()/100.f;
    } else {
      westLon = din.readFloat();
      eastLon = din.readFloat();
      northLat = din.readFloat();
      southLat = din.readFloat();
    }
    int rows = din.readInt();
    int cols = din.readInt();
    /*
    System.out.println(
       "Bounds: " +
          "\n\tWestern Longitude = " + westLon +
          "\n\tEastern Longitude = " + eastLon +
          "\n\tNorthern Latitude = " + northLat +
          "\n\tSouthern Latitude = " + southLat +
          "\n\trows = " + rows + " cols = " + cols);
    */
    Linear2DSet domain = 
      new LinearLatLonSet(RealTupleType.SpatialEarth2DTuple,
                          -westLon, -eastLon, cols,   // Vis5D west positive
                          northLat, southLat, rows);  // Vis5D rows upside down
    FunctionType ftype =
      new FunctionType(((SetType)domain.getType()).getDomain(), 
                         RealType.Altitude);
    FlatField data = new FlatField(ftype, domain);
    float[][] samples = new float[1][rows*cols];
    for (int i = 0; i < rows*cols; i++) {
      short s = (short) (din.readShort()/2);
      samples[0][i] = new Short(s).floatValue();
    } 
    data.setSamples(samples, false);
    return data;

  }
}
