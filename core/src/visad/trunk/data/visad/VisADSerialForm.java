//
// VisADSerialForm.java
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
   VisADSerialForm is the VisAD data format adapter for
   serialized visad.Data objects.<P>
*/
public class VisADSerialForm extends Form implements FormFileInformer {

  private static int num = 0;

  public VisADSerialForm() {
    super("VisADSerialForm" + num++);
  }

  public boolean isThisType(String name) {
    return name.endsWith(".vad");
  }

  public boolean isThisType(byte[] block) {
    return false;
  }

  public String[] getDefaultSuffixes() {
    String[] suff = { "vad" };
    return suff;
  }

  public synchronized void save(String id, Data data, boolean replace)
         throws BadFormException, IOException, RemoteException, VisADException {
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

  public synchronized void add(String id, Data data, boolean replace)
         throws BadFormException {
    throw new BadFormException("VisADSerialForm.add");
  }

  public synchronized DataImpl open(String id)
         throws BadFormException, IOException, VisADException {
    FileInputStream fileStream = new FileInputStream(id);
    BufferedInputStream bufferedStream = new BufferedInputStream(fileStream);
    ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);
    DataImpl data = null;
    try {
      data = (DataImpl) objectStream.readObject();
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
    restoreReals(data.getType());
    return data;
  }

  public synchronized DataImpl open(URL url)
         throws BadFormException, VisADException, IOException {
    InputStream inputStream = url.openStream();
    BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
    ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);
    DataImpl data = null;
    try {
      data = (DataImpl) objectStream.readObject();
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
    restoreReals(data.getType());
    return data;
  }

  private void restoreReals(MathType type) throws VisADException {
    if (type instanceof FunctionType) {
      FunctionType function = (FunctionType) type;
      restoreReals(function.getDomain());
      restoreReals(function.getRange());
    }
    else if (type instanceof TupleType) {
      TupleType tuple = (TupleType) type;
      for (int i=0; i<tuple.getDimension(); i++) {
        restoreReals(tuple.getComponent(i));
      }
    }
    else if (type instanceof SetType) {
      SetType set = (SetType) type;
      restoreReals(set.getDomain());
    }
    else if (type instanceof RealType) {
      RealType real = (RealType) type;
      RealType.getRealType(real.getName(), real.getDefaultUnit(),
                           real.getDefaultSet(), real.getAttributeMask());
    }
  }

  public synchronized FormNode getForms(Data data) {
    return null;
  }

  /** run 'java visad.data.visad.VisADSerialForm in_file out_file' to
      convert in_file to out_file in VisAD serialized data format */
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("to convert a file to serial VisAD, run:");
      System.out.println("  java visad.data.visad.VisADSerialForm in_file out_file");
      System.out.println("to test read a serial VisAD file, run:");
      System.out.println("or  'java visad.data.visad.VisADSerialForm in_file'");
    }
    else if (args.length == 1) {
      VisADSerialForm form = new VisADSerialForm();
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
      VisADSerialForm form = new VisADSerialForm();
      form.save(args[1], data, true);
    }
    System.exit(0);
  }

}

