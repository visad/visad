//
// hdfeos.java
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

import java.io.IOException;
import visad.data.BadFormException;
import visad.data.Form;
import visad.data.FormFileInformer;
import visad.DataImpl;
import visad.VisADException;

public abstract class Hdfeos 
       extends Form
       implements FormFileInformer
{


  public Hdfeos( String name ) {

    super( name );
  }

  public boolean isThisType(String name)
  {
    String[] suff_s = getDefaultSuffixes();
    for ( int ii = 0; ii < suff_s.length; ii++ ) 
    {
      if ( name.endsWith("."+suff_s[ii]) ) {
        return true;
      }
    }
    return false;
  }

  public boolean isThisType(byte[] block)
  {
    String first_four_bytes = new String( block, 0, 20 );
    return first_four_bytes.startsWith("ASCN");
  }

  public String[] getDefaultSuffixes()
  {
    String[] suffs = { "hdf",
                       "hdfeos",
                       "eos" };
    return suffs;
  }

  public abstract DataImpl open( String file_path ) 
     throws BadFormException, IOException, VisADException;



}
