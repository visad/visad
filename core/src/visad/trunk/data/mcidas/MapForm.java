//
// MapForm.java
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

package visad.data.mcidas;

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
   MapForm is the Map data format adapter for
   serialized visad.Data objects.<P>
*/
public class MapForm extends Form implements FormFileInformer {

  private BaseMapAdapter ba;

  private static int num = 0;

  public MapForm() {
    super("MapForm" + num++);
  }

  public boolean isThisType(String name) {
    return name.startsWith("OUTL");
  }

  public boolean isThisType(byte[] block) {
    return false;
  }

  public String[] getDefaultSuffixes() {
    String[] suff = { " " };
    return suff;
  }

  public synchronized void save(String id, Data data, boolean replace)
         throws BadFormException, IOException, RemoteException, VisADException {
    throw new UnimplementedException("Can't yet save McIDAS map files");
  }

  public synchronized void add(String id, Data data, boolean replace)
         throws BadFormException {
    throw new BadFormException("MapForm.add");
  }

  public synchronized DataImpl open(String id)
         throws BadFormException, IOException, VisADException {
    try {
      ba = new BaseMapAdapter(id);
      return ba.getData();

    } catch (IOException e) {
      throw new VisADException("IOException: " + e.getMessage());
    }
  }

  public synchronized DataImpl open(URL url)
         throws BadFormException, VisADException, IOException {
    ba = new BaseMapAdapter(url.toString());
    return ba.getData();
  }

  public synchronized FormNode getForms(Data data) {
    return null;
  }

}

