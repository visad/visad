//
// hdfeosDefault.java
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

package visad.data.hdfeos;

import java.util.*; 
import visad.*;
import visad.data.*;
import java.rmi.*;
import java.net.URL;

public class hdfeosDefault extends hdfeos {


  public hdfeosDefault() {

     super("Default");
  }
     
  public DataImpl open( String file_path ) {

    DataImpl  data = null;

    try {

      hdfeosFileDefault file = new hdfeosFileDefault( file_path );

      data = file.getDataObject();
    }
    finally {

    }

    return data;

  }

  public DataImpl open( URL url ) {

    return null;
  }

  public void add( String id, DataImpl data, boolean replace ) throws
     BadFormException {

  }

  public void save( String id, DataImpl data, boolean replace ) throws
     BadFormException, RemoteException {

  }

  public FormNode getForms( Data data ) {

    return this;
  }

}
