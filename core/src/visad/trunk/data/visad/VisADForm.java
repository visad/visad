 
//
// VisADForm.java
//
 
/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
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
import com.sun.java.swing.*;
import com.sun.java.swing.event.*;
import com.sun.java.swing.text.*;
import com.sun.java.swing.border.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

/**
   VisADForm is the VisAD data format adapter for
   serialized visad.Data objects.<P>
*/
public class VisADForm extends Form {
 
  private static int num = 0;

  public VisADForm() {
    super("VisADForm" + num++);
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
        throw new BadFormException("VisADForm.save(" + id + "): exists");
      }
      fileStream = new FileOutputStream(file);
    }
    BufferedOutputStream bufferedStream = new BufferedOutputStream(fileStream);
    ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);
    DataImpl local_data = data.local();
    objectStream.writeObject(local_data);
    objectStream.close();
  }

  public synchronized void add(String id, Data data, boolean replace)
         throws BadFormException {
    throw new BadFormException("VisADForm.add");
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
      throw new BadFormException("VisADForm.open(" + id + "): " +
                                 "OptionalDataException");
    }
    catch (ClassNotFoundException e) {
      throw new BadFormException("VisADForm.open(" + id + "): " +
                                 "ClassNotFoundException");
    }
    catch (IOException e) {
      throw new BadFormException("VisADForm.open(" + id + "): IOException");
    }
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
      throw new BadFormException("VisADForm.open(URL): " +
                                 "OptionalDataException");
    }
    catch (ClassNotFoundException e) {
      throw new BadFormException("VisADForm.open(URL): " +
                                 "ClassNotFoundException");
    }
    catch (IOException e) {
      throw new BadFormException("VisADForm.open(URL): IOException");
    }
    return data;
  }

  public synchronized FormNode getForms(Data data) {
    return null;
  }
 
  /** run 'java visad.data.visad.VisADForm in_file out_file' to
      convert in_file to out_file in VisAD serialized data format */
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("to convert a file to serial VisAD, run:");
      System.out.println("  java visad.data.visad.VisADForm in_file out_file");
      System.out.println("to test read a serial VisAD file, run:");
      System.out.println("or  'java visad.data.visad.VisADForm in_file'");
    }
    else if (args.length == 1) {
      VisADForm form = new VisADForm();
      form.open(args[0]);
    }
    else if (args.length == 2) {
      DefaultFamily loader = new DefaultFamily("loader");
      DataImpl data = loader.open(args[0]);
      loader = null;
      VisADForm form = new VisADForm();
      form.save(args[1], data, true);
    }
  }

}

