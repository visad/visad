
//
// F2000Form.java
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

package visad.amanda;

import visad.*;
import visad.java3d.*;
import visad.util.*;
import visad.data.*;
import java.io.*;
import java.rmi.RemoteException;
import java.net.URL;
import java.util.StringTokenizer;

// JFC packages
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

/**
   F2000Form is the VisAD data format adapter for
   F2000 files for Amanda events.<P>
*/
public class F2000Form extends Form implements FormFileInformer {

  private static int num = 0;

  public F2000Form() {
    super("F2000Form" + num++);
  }

  public boolean isThisType(String name) {
    return name.endsWith(".r");
  }

  public boolean isThisType(byte[] block) {
    return false;
  }

  public String[] getDefaultSuffixes() {
    String[] suff = { "r" };
    return suff;
  }

  public synchronized void save(String id, Data data, boolean replace)
         throws BadFormException, IOException, RemoteException, VisADException {
    throw new BadFormException("F2000Form.save");
  }

  public synchronized void add(String id, Data data, boolean replace)
         throws BadFormException {
    throw new BadFormException("F2000Form.add");
  }

  public synchronized DataImpl open(String id)
         throws BadFormException, IOException, VisADException {
    FileInputStream fileStream = new FileInputStream(id);
    return open(fileStream);
  }

  public synchronized DataImpl open(URL url)
         throws BadFormException, VisADException, IOException {
    InputStream inputStream = url.openStream();
    return open(inputStream);
  }

  private synchronized DataImpl open(InputStream is)
         throws BadFormException, VisADException, IOException {
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);

    try {
      StringTokenizer st = getNext(br);
      String first = st.nextToken();
      if (!first.equals("v")) {
        throw new BadFormException("must start with v\n" + line);
      }
      while (true) {
        st = getNext(br);
        first = st.nextToken();
        if (first.equals("array")) {
          break;
        }
      }

      while (true) {
        st = getNext(br);
        first = st.nextToken();
        // while (st.hasMoreTokens()) { ...}
      }


    }
    catch (IOException e) {
      // end of file
    }
    return null;
  }

  private String line = null;

  private StringTokenizer getNext(BufferedReader br) throws IOException {
    while (true) {
      line = br.readLine();
      if (line == null || line.length() == 0) continue;
      line = line.toLowerCase();
      char fchar = line.charAt(0);
      if (fchar < 'a' || 'z' < fchar) continue; // skip comments
      return new StringTokenizer(line);
    }
  }

  public synchronized FormNode getForms(Data data) {
    return null;
  }

  /** run 'java visad.data.visad.F2000Form in_file out_file' to
      convert in_file to out_file in VisAD serialized data format */
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {
    if (args == null || args.length != 1) {
      System.out.println("to test read an F2000 file, run:");
      System.out.println("  'java visad.amanda.F2000Form in_file'");
    }
    F2000Form form = new F2000Form();
    if (args[0].startsWith("http://")) {
      // with "ftp://" this throws "sun.net.ftp.FtpProtocolException: RETR ..."
      URL url = new URL(args[0]);
      form.open(url);
    }
    else {
      form.open(args[0]);
    }
    System.exit(0);
  }

}

