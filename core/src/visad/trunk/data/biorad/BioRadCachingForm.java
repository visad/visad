//
// BioRadCachingForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.biorad;

import java.io.IOException;

import java.net.URL;

import java.rmi.RemoteException;

import ucar.netcdf.RandomAccessFile;

import visad.Data;
import visad.DataImpl;
import visad.data.DefaultFamily;
import visad.VisADException;

import visad.data.BadFormException;

/**
 * BioRadCachingForm is the VisAD data format adapter for
 * large Bio-Rad .PIC files which may not fit in memory.
 */
public class BioRadCachingForm extends BioRadForm {
  public BioRadCachingForm()
  {
    super();
  }

  /**
   * Opens an existing large BioRad .PIC file from the given
   * location, caching the data where possible.
   *
   * @return VisAD Data object containing BioRad data.
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    return readFile(new RandomAccessFile(id, "r"), true);
  }                       

  /**
   * URLs cannot be cached, so this method throws a
   * {@link visad.VisADException VisADException}
   *
   * @exception VisADException because URLs cannot be cached.
   */
  public DataImpl open(URL url)
    throws BadFormException, VisADException, IOException
  {
    throw new VisADException("Cannot cache URL " + url);
  }

  /**
   * Run 'java visad.data.biorad.BioRadCachingForm in_file out_file'
   * to convert in_file to out_file in BioRad .PIC data format.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("To convert a file to BioRad .PIC, run:");
      System.out.println(
        "  java visad.data.biorad.BioRadCachingForm in_file out_file");
      System.out.println("To test read a BioRad .PIC file, run:");
      System.out.println("  java visad.data.biorad.BioRadCachingForm in_file");
      System.exit(2);
    }

    if (args.length == 1) {
      // Test read BioRad .PIC file
      BioRadCachingForm form = new BioRadCachingForm();
      System.out.print("Reading " + args[0] + " ");
      Data data = form.open(args[0]);
      System.out.println("[done]");
      System.out.println("MathType =\n" + data.getType().prettyString());
    }
    else if (args.length == 2) {
      // Convert file to BioRad .PIC format
      System.out.print(args[0] + " -> " + args[1] + " ");
      DefaultFamily loader = new DefaultFamily("loader");
      DataImpl data = loader.open(args[0]);
      loader = null;
      BioRadCachingForm form = new BioRadCachingForm();
      form.save(args[1], data, true);
      System.out.println("[done]");
    }

    System.exit(0);
  }
}
