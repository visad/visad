//
// VisADForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.visad;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.URL;

import java.rmi.RemoteException;

import visad.Data;
import visad.DataImpl;
import visad.VisADException;

import visad.data.BadFormException;
import visad.data.DefaultFamily;
import visad.data.Form;
import visad.data.FormFileInformer;
import visad.data.FormNode;

/**
   VisADForm is the VisAD data format adapter for
   binary visad.Data objects.<P>
*/
public class VisADForm extends Form implements FormFileInformer {

  private static int num = 0;

  private boolean allowBinary = false;

  /**
   * If <tt>allowBinary</tt> is <tt>true</tt>, read/write a VisAD
   * Data object in VisAD's
   * <a href="http://www.ssec.wisc.edu/~dglo/binary_file_format.html">binary file format</a>.<br>
   * <br>
   * If <tt>allowBinary</tt> is <tt>false</tt>, read/write a VisAD
   * Data object using Java serialization.
   *
   * @param allowBinary if <tt>true</tt> use VisAD's binary file format,
   *                    otherwise use Java serialization.
   */
  public VisADForm(boolean allowBinary)
  {
    this();

    this.allowBinary = allowBinary;
  }

  /**
   * Read/write a VisAD Data object using Java serialization.
   */
  public VisADForm()
  {
    super("VisADForm" + num++);
  }

  public boolean isThisType(String name)
  {
    return name.endsWith(".vad") || name.endsWith(".VAD");
  }

  public boolean isThisType(byte[] block)
  {
    return BinaryReader.isMagic(block);
  }

  public String[] getDefaultSuffixes()
  {
    String[] suff = { "vad" };
    return suff;
  }

  public synchronized void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("VisADForm.add");
  }

  public synchronized FormNode getForms(Data data)
  {
    return null;
  }

  public synchronized DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    IOException savedIOE = null;
    VisADException savedVE = null;

    // try to read a binary object
    try {
      return readData(new BinaryReader(id));
    } catch (IOException ioe) {
      savedIOE = ioe;
    } catch (VisADException ve) {
      savedVE = ve;
    }

    // maybe it's a serialized object
    try {
      return readSerial(new FileInputStream(id));
    } catch (ClassNotFoundException cnfe) {
      if (savedIOE != null) {
        throw savedIOE;
      } else if (savedVE != null) {
        throw savedVE;
      }

      throw new BadFormException("Could not read file \"" + id + "\": " +
                                 cnfe.getMessage());
    } catch (IOException ioe) {
      if (savedIOE != null) {
        throw savedIOE;
      } else if (savedVE != null) {
        throw savedVE;
      }

      throw ioe;
    }
  }

  public synchronized DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    IOException savedIOE = null;
    VisADException savedVE = null;

    // try to read a binary object
    try {
      return readData(new BinaryReader(url.openStream()));
    } catch (IOException ioe) {
      savedIOE = ioe;
    } catch (VisADException ve) {
      savedVE = ve;
    }

    // maybe it's a serialized object
    try {
      return readSerial(url.openStream());
    } catch (ClassNotFoundException cnfe) {
      if (savedIOE != null) {
        throw savedIOE;
      } else if (savedVE != null) {
        throw savedVE;
      }

      throw new BadFormException("Could not read URL " + url + ": " +
                                 cnfe.getMessage());
    } catch (IOException ioe) {
      if (savedIOE != null) {
        throw savedIOE;
      } else if (savedVE != null) {
        throw savedVE;
      }

      throw ioe;
    }
  }

  public DataImpl readData(BinaryReader rdr)
    throws IOException, VisADException
  {
    DataImpl di = rdr.getData();
    try { rdr.close(); } catch (IOException ioe) { }
    return di;
  }

  public DataImpl readSerial(InputStream inputStream)
    throws ClassNotFoundException, IOException
  {
    BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
    ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);
    return (DataImpl )objectStream.readObject();
  }

  /**
   * Save a <tt>Data</tt> object in VisAD's binary format.
   *
   * @param id file name
   * @param data <tt>Data</tt> object
   * @param replace <tt>true</tt> if any existing file should be overwritten
   * @param bigObject <tt>true</tt> if the <tt>Data</tt> object is larger
   *                  than the computer's memory, in which case special
   *                  measures will be taken to converse memory usage.
   */
  private synchronized void saveBinary(String id, Data data, boolean replace,
                                       boolean bigObject)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    File file = new File(id);
    if (!replace && file.exists()) {
      throw new IllegalArgumentException("File \"" + id + "\" exists");
    }

    BinaryWriter writer = new BinaryWriter(file);
    writer.save((DataImpl )data, bigObject);
    writer.close();
  }

  /**
   * Save a <tt>Data</tt> object in serialized Java format.
   *
   * @param id file name
   * @param data <tt>Data</tt> object
   * @param replace <tt>true</tt> if any existing file should be overwritten
   */
  private synchronized void saveSerial(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    FileOutputStream fileStream = null;
    if (replace) {
      fileStream = new FileOutputStream(id);
    }
    else {
      File file = new File(id);
      if (file.exists()) {
        throw new BadFormException("VisADSerialForm.save(" + id + "): exists");
      }
      fileStream = new FileOutputStream(file);
    }
    BufferedOutputStream bufferedStream = new BufferedOutputStream(fileStream);
    ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);
    DataImpl local_data = data.local();
    objectStream.writeObject(local_data);
    objectStream.flush();
    fileStream.close();
  }

  /**
   * Save a <tt>Data</tt> object.
   *
   * @param id file name
   * @param data <tt>Data</tt> object
   * @param replace <tt>true</tt> if any existing file should be overwritten
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    save(id, data, replace, false);
  }

  /**
   * Save a <tt>Data</tt> object.
   *
   * @param id file name
   * @param data <tt>Data</tt> object
   * @param replace <tt>true</tt> if any existing file should be overwritten
   * @param bigObject <tt>true</tt> if the <tt>Data</tt> object is larger
   *                  than the computer's memory, in which case special
   *                  measures will be taken to converse memory usage.
   */
  public synchronized void save(String id, Data data, boolean replace,
                                boolean bigObject)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    if (allowBinary) {
      saveBinary(id, data, replace, bigObject);
    } else {
      saveSerial(id, data, replace);
    }
  }

  /** run 'java visad.data.visad.VisADForm in_file out_file' to
      convert in_file to out_file in VisAD serialized data format */
  public static void main(String args[])
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("to convert a file to a VisAD binary file, run:");
      System.out.println("  java visad.data.visad.VisADForm in_file out_file");
      System.out.println("to test read a binary or serial VisAD file, run:");
      System.out.println("or  'java visad.data.visad.VisADForm in_file'");
    }
    else if (args.length == 1) {
      VisADForm form = new VisADForm();
      if (args[0].startsWith("http://")) {
        // with "ftp://" this throws "sun.net.ftp.FtpProtocolException: RETR ..."
        URL url = new URL(args[0]);
        form.open(url);
      }
      else {
        form.open(args[0]);
      }
    }
    else if (args.length == 2) {
      DefaultFamily loader = new DefaultFamily("loader");
      DataImpl data = loader.open(args[0]);
      loader = null;
      VisADForm form = new VisADForm();
      form.save(args[1], data, true);
    }
    System.exit(0);
  }
}
