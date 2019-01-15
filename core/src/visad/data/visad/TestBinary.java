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

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

import visad.DataImpl;
import visad.VisADException;

import visad.data.DefaultFamily;
import visad.data.Form;

public class TestBinary
{
  private static final String OUTPUT_SUBDIRECTORY = "tstout";

  private String progName;
  private boolean allowBinary, verbose;
  private String[] files;

  public TestBinary(String[] args)
    throws VisADException
  {
    initArgs();

    if (!processArgs(args)) {
      System.err.println("Exiting...");
      System.exit(1);
    }

    DefaultFamily df = new DefaultFamily("DefaultFamily");

    Form form = new VisADForm(allowBinary);

    if (!makeSubdirectory(OUTPUT_SUBDIRECTORY)) {
      throw new VisADException("Couldn't create test subdirectory \"" +
                               OUTPUT_SUBDIRECTORY + "\"");
    }

    boolean success = true;
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        DataImpl data;
        try {
          data = df.open(files[i]);
        } catch (visad.data.BadFormException bfe) {
          System.err.println("Couldn't read " + files[i] + ": " +
                             bfe.getMessage());
          success = false;
          continue;
        }

        System.out.println(files[i]);

        success &= writeData(form, data, i);

        if (verbose) {
          System.out.println("-- ");
        }
      }
    } else {
      DataImpl[] dataList = new FakeData().getList();

      for (int i = 0; i < dataList.length; i++) {
        success &= writeData(form, dataList[i], i);

        if (verbose) {
          System.out.println("-- ");
        }
      }
    }

    if (success) {
      System.out.println("All tests succeeded!");
    }
  }

  private int getSerializedSize(Object obj)
  {
    java.io.ByteArrayOutputStream outBytes;
    outBytes = new java.io.ByteArrayOutputStream();

    java.io.ObjectOutputStream outStream;
    try {
      outStream = new java.io.ObjectOutputStream(outBytes);
      outStream.writeObject(obj);
      outStream.flush();
      outStream.close();
    } catch (IOException ioe) {
      return 0;
    }

    return outBytes.size();
  }

  public void initArgs()
  {
    allowBinary = verbose = false;
    files = null;
  }

  private boolean makeSubdirectory(String name)
  {
    File subdir = new File(name);
    if (subdir.isDirectory()) {
      return true;
    }

    if (subdir.exists()) {
      System.err.println(progName + ": Subdirectory \"" + name +
                         "\" exists but is not a directory");
      return false;
    }

    return subdir.mkdir();
  }

  public boolean processArgs(String[] args)
  {
    boolean usage = false;

    String className = getClass().getName();
    int pt = className.lastIndexOf('.');
    final int ds = className.lastIndexOf('$');
    if (ds > pt) {
      pt = ds;
    }
    progName = className.substring(pt == -1 ? 0 : pt + 1);

    ArrayList fileList = null;

    for (int i = 0; args != null && i < args.length; i++) {
      if (args[i].length() > 0 && args[i].charAt(0) == '-') {
        char ch = args[i].charAt(1);

        String str, result;

        switch (ch) {
        case 'a':
          allowBinary = true;
          break;
        case 'v':
          verbose = true;
          break;
        default:
          System.err.println(progName +
                             ": Unknown option \"-" + ch + "\"");
          usage = true;
          break;
        }
      } else {
        if (fileList == null) {
          fileList = new ArrayList();
        }
        fileList.add(args[i]);
      }
    }

    if (usage) {
      System.err.println("Usage: " + getClass().getName() +
                         " [-a(llowBinary)]" +
                         " [-v(erbose)]" +
                         "");
    }

    if (fileList != null) {
      files = new String[fileList.size()];
      for (int i = 0; i < files.length; i++) {
        files[i] = (String )fileList.get(i);
      }

      fileList.clear();
    }

    return !usage;
  }

  private boolean writeData(Form form, DataImpl data, int num)
  {
    String path = OUTPUT_SUBDIRECTORY + File.separatorChar + "binary" +
      num + ".vad";

    if (verbose) {
      System.out.println("Writing " + data.getClass().getName() + " to " +
                         path);
    }

    try {
      form.save(path, data, true);
    } catch (Throwable t) {
      t.printStackTrace();
      new File(path).delete();
      return false;
    } 

    if (verbose) {
      System.out.println("Reading " + data.getClass().getName());
    }

    DataImpl newData;
    try {
      newData = form.open(path);
    } catch (Throwable t) {
      t.printStackTrace();
      return false;
    }

    if (newData == null) {
      System.err.println("Got null Data while reading " + data);
      return false;
    }

    if (!data.equals(newData)) {
      System.err.println("MISMATCH");
      return false;
    }

    return true;
  }

  public static void main(String[] args)
  {
    try {
      new TestBinary(args);
    } catch (VisADException ve) {
      ve.printStackTrace();
    }

    System.exit(0);
  }
}
