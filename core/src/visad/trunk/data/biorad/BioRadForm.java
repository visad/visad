//
// BioRadForm.java
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

package visad.data.biorad;

import visad.*;
import visad.data.*;
import java.io.*;
import java.rmi.RemoteException;
import java.net.URL;
import visad.data.DefaultFamily;

/** BioRadForm is the VisAD data format adapter for Bio-Rad .PIC files. */
public class BioRadForm extends Form implements FormFileInformer {

  /** Numerical ID of a valid BioRad .PIC file. */
  public static int PIC_FILE_ID = 12345;

  private static int num = 0;

  /** Constructs a new BioRad file form. */
  public BioRadForm() {
    super("BioRadForm" + num++);
  }

  private int getUnsignedShort(byte b1, byte b2) {
    int i1 = 0x000000ff & b1;
    int i2 = 0x000000ff & b2;
    return (i2 << 8) | i1;
  }

  private float getFloat(byte b1, byte b2, byte b3, byte b4) {
    int i1 = 0x000000ff & b1;
    int i2 = 0x000000ff & b2;
    int i3 = 0x000000ff & b3;
    int i4 = 0x000000ff & b4;
    int bits = (i4 << 24) | (i3 << 16) | (i2 << 8) | i1;
    return Float.intBitsToFloat(bits);
  }

  /** Checks if the given string is a valid filename for a BioRad .PIC file. */
  public boolean isThisType(String name) {
    return name.toLowerCase().endsWith(".pic");
  }

  /** Checks if the given block is a valid header for a BioRad .PIC file. */
  public boolean isThisType(byte[] block) {
    if (block.length < 56) return false;
    return getUnsignedShort(block[54], block[55]) == PIC_FILE_ID;
  }

  /** Returns the default file suffixes for the BioRad .PIC file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"pic"};
  }

  /** Saves a VisAD Data object to BioRad .PIC format at the given location. */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    // CTR: TODO
    throw new UnimplementedException("BioRadForm.save");
  }

  /**
   * Adds data to an existing BioRad file.
   *
   * @exception BadFormException Always thrown (this method is not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("BioRadForm.add");
  }

  /**
   * Opens an existing BioRad .PIC file from the given location.
   *
   * @return VisAD Data object containing BioRad data.
   */
  public DataImpl open(String id) throws BadFormException, IOException, VisADException {
    return open(new FileInputStream(id));
  }

  /**
   * Opens an existing BioRad .PIC file from the given URL.
   *
   * @return VisAD Data object containing BioRad data.
   */
  public DataImpl open(URL url) throws BadFormException, VisADException, IOException {
    return open(url.openStream());
  }

  /** Reads in BioRad .PIC file data from the given input stream. */
  private DataImpl open(InputStream in)
    throws BadFormException, IOException, VisADException
  {
    DataInputStream fin = new DataInputStream(in);

    // read header
    byte[] header = new byte[76];
    fin.read(header, 0, 76);
    int nx = getUnsignedShort(header[0], header[1]);
    int ny = getUnsignedShort(header[2], header[3]);
    int npic = getUnsignedShort(header[4], header[5]);
    int ramp1_min = getUnsignedShort(header[6], header[7]);
    int ramp1_max = getUnsignedShort(header[8], header[9]);
    boolean notes = (header[10] | header[11] | header[12] | header[13]) != 0;
    boolean byte_format = getUnsignedShort(header[14], header[15]) != 0;
    int n = getUnsignedShort(header[16], header[17]);
    String name = new String(header, 18, 32);
    int merged = getUnsignedShort(header[50], header[51]);
    int color1 = getUnsignedShort(header[52], header[53]);
    int file_id = getUnsignedShort(header[54], header[55]);
    int ramp2_min = getUnsignedShort(header[56], header[57]);
    int ramp2_max = getUnsignedShort(header[58], header[59]);
    int color2 = getUnsignedShort(header[60], header[61]);
    int edited = getUnsignedShort(header[62], header[63]);
    int _lens = getUnsignedShort(header[64], header[65]);
    float mag_factor =
      getFloat(header[66], header[67], header[68], header[69]);

    // check validity of header
    if (file_id != PIC_FILE_ID) {
      throw new BadFormException("Invalid file header: " + file_id);
    }

    // read image data
    int image_len = nx * ny;
    byte[][] image_data = new byte[npic][image_len];
    if (byte_format) {
      // read in image_len bytes
      image_data = new byte[npic][image_len];
      for (int i=0; i<npic; i++) fin.read(image_data[i], 0, image_len);
    }
    else {
      // read in 2 * image_len bytes
      image_data = new byte[npic][2 * image_len];
      for (int i=0; i<npic; i++) fin.read(image_data[i], 0, 2 * image_len);
    }

    // read notes
    // CTR: TODO

    // read color table
    // CTR: TODO

    // close file
    fin.close();

    // convert image bytes to floats
    float[][][] samples = new float[npic][1][image_len];
    if (byte_format) {
      // each pixel is 8 bits
      for (int i=0; i<npic; i++) {
        for (int l=0; l<image_len; l++) {
          int q = 0x000000ff & image_data[i][l];
          samples[i][0][l] = (float) q;
        }
      }
    }
    else {
      // each pixel is 16 bits
      for (int i=0; i<npic; i++) {
        for (int l=0; l<image_len; l++) {
          int q = getUnsignedShort(
            image_data[i][2 * l], image_data[i][2 * l + 1]);
          samples[i][0][l] = (float) q;
        }
      }
    }

    // set up data types
    RealType time = RealType.getRealType("time");
    RealType x = RealType.getRealType("ImageElement");
    RealType y = RealType.getRealType("ImageLine");
    RealType rgb = RealType.getRealType("value");
    RealTupleType xy = new RealTupleType(x, y);
    FunctionType image_function = new FunctionType(xy, rgb);
    FunctionType time_function = new FunctionType(time, image_function);

    // set up domain sets
    Integer2DSet image_set = new Integer2DSet(nx, ny);
    Integer1DSet time_set = new Integer1DSet(npic);

    // set up fields
    FlatField[] image_fields = new FlatField[npic];
    for (int i=0; i<npic; i++) {
      image_fields[i] = new FlatField(image_function, image_set);
      image_fields[i].setSamples(samples[i], false);
    }
    FieldImpl time_field = new FieldImpl(time_function, time_set);
    time_field.setSamples(image_fields, false);

    return time_field;
  }

  public FormNode getForms(Data data) {
    return null;
  }

  /**
   * Run 'java visad.data.visad.BioRadForm in_file out_file' to convert
   * in_file to out_file in BioRad .PIC data format.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("To convert a file to BioRad .PIC, run:");
      System.out.println("  java visad.data.biorad.BioRadForm in_file out_file");
      System.out.println("To test read a BioRad .PIC file, run:");
      System.out.println("  java visad.data.biorad.BioRadForm in_file");
      System.exit(2);
    }

    if (args.length == 1) {
      // Test read BioRad .PIC file
      BioRadForm form = new BioRadForm();
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
      BioRadForm form = new BioRadForm();
      form.save(args[1], data, true);
      System.out.println("[done]");
    }
    System.exit(0);
  }

}

