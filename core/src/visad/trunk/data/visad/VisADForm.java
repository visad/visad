//
// VisADForm.java
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

package visad.data.visad;

import visad.*;
import visad.java3d.*;
import visad.data.*;
import visad.util.*;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.net.URL;
import visad.data.DefaultFamily;

// JFC packages
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

/**
   VisADForm is the VisAD data format adapter for
   binary visad.Data objects.<P>
*/
public class VisADForm extends Form implements FormFileInformer {

  private static int num = 0;

  public VisADForm() {
    super("VisADForm" + num++);
  }

  public boolean isThisType(String name) {
    return name.endsWith(".vad") || name.endsWith(".VAD");
  }

  public boolean isThisType(byte[] block) {
    return BinaryReader.isMagic(block);
  }

  public String[] getDefaultSuffixes() {
    String[] suff = { "vad" };
    return suff;
  }

  public synchronized void add(String id, Data data, boolean replace)
         throws BadFormException {
    throw new BadFormException("VisADForm.add");
  }

  public synchronized FormNode getForms(Data data) {
    return null;
  }

  public synchronized DataImpl open(String id)
    throws BadFormException
  {
    String errMsg = null;

    // first, try to read a binary object
    BinaryReader rdr;
    try {
      return readData(new BinaryReader(id));
    } catch (Exception ioe) {
ioe.printStackTrace();
      errMsg = ioe.getMessage();
    }

    // if it's not a binary object, maybe it's a serialized object
    try {
      FileInputStream fileStream = new FileInputStream(id);
      BufferedInputStream bufferedStream =
        new BufferedInputStream(fileStream);
      ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);
      return (DataImpl) objectStream.readObject();
    }
    catch (Exception e) {
      throw new BadFormException(errMsg);
    }
  }

  public synchronized DataImpl open(URL url)
    throws BadFormException
  {
    String errMsg = null;

    // first, try to read a binary object
    BinaryReader rdr;
    try {
      return readData(new BinaryReader(url.openStream()));
    } catch (Exception ioe) {
      errMsg = ioe.getMessage();
    }

    // if it's not a binary object, maybe it's a serialized object
    try {
      InputStream inputStream = url.openStream();
      BufferedInputStream bufferedStream =
        new BufferedInputStream(inputStream);
      ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);
      return (DataImpl) objectStream.readObject();
    }
    catch (OptionalDataException e) {
      throw new BadFormException(e.toString());
    }
    catch (ClassNotFoundException e) {
      throw new BadFormException(e.toString());
    }
    catch (IOException e) {
      throw new BadFormException(e.toString());
    }
  }

  private DataImpl readData(BinaryReader rdr)
    throws IOException, VisADException
  {
    DataImpl di = rdr.getData();
    try { rdr.close(); } catch (IOException ioe) { }
    return di;
  }

  public synchronized void save(String id, Data data, boolean replace)
         throws BadFormException, IOException, RemoteException, VisADException {
    File file = new File(id);
    if (!replace && file.exists()) {
      throw new IllegalArgumentException("File \"" + id + "\" exists");
    }

    BinaryWriter writer = new BinaryWriter(file);
    writer.process((DataImpl )data);
    writer.close();
  }

  /** run 'java visad.data.visad.VisADForm in_file out_file' to
      convert in_file to out_file in VisAD serialized data format */
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {
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

