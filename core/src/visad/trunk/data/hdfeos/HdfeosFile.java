//
// HdfeosFile.java
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
import java.lang.*;
import visad.data.hdfeos.hdfeosc.HdfeosLib;

public class HdfeosFile 
{
  private String filename;
  private int  file_id;
  private int[] sd_id = new int[1];
  private int  n_structs;

  private Vector Structs;

  static Vector openedFiles = new Vector();          // all opened file objects

  HdfeosFile( String filename ) 
            throws HdfeosException
  {
    this.filename = filename;

    String[] swath_list = {"empty"};
    int n_swaths = HdfeosLib.SWinqswath( filename, swath_list );
    n_structs = 0;
    Structs = new Vector();

    if ( n_swaths > 0 )  {

      file_id = HdfeosLib.SWopen( filename, HdfeosLib.DFACC_READ );

      String struct_name = "Swath";
      int[] hdf_id = new int[1];
      byte[] access = new byte[1];

      int stat = HdfeosLib.EHchkfid( file_id, struct_name, hdf_id, sd_id, access); 
      if ( stat < 0 ) 
      {
        throw new HdfeosException("--cannot obtain sdInterfaceId--" );
      }

      StringTokenizer swaths = new StringTokenizer( swath_list[0], ",", false );
      while ( swaths.hasMoreElements() )
      {
        String swath = (String) swaths.nextElement();
        EosSwath obj = new EosSwath( file_id, sd_id[0], swath );
        Structs.addElement( (EosStruct)obj );
        n_structs++;
      }
    } 

    String[] grid_list = {"empty"};
    int n_grids = HdfeosLib.GDinqgrid( filename, grid_list );

    if ( n_grids > 0 ) {

      file_id = HdfeosLib.GDopen( filename, HdfeosLib.DFACC_READ );

      StringTokenizer grids = new StringTokenizer( grid_list[0], ",", false );
 
      while ( grids.hasMoreElements() ) 
      {
        String grid = (String) grids.nextElement();
        EosGrid g_obj = new EosGrid( file_id, sd_id[0], grid );
        Structs.addElement( (EosStruct)g_obj );
        n_structs++;
      }
    }

    openedFiles.addElement( this );
  }

  public int getNumberOfStructs() 
  {
    return n_structs; 
  }

  public EosStruct getStruct( int ii ) 
  {
    return (EosStruct) Structs.elementAt(ii);
  }

  public String getFileName()
  {
    return filename;
  }

  public void closeAll()  
        throws HdfeosException 
  {
    int status = HdfeosLib.EHclose( file_id );
    if ( status < 0 ) {
      throw new HdfeosException("--closing file, "+filename+
                                ", returned status: "+status+" --");
    }
  }

  public static void close() 
         throws HdfeosException 
  {
    for ( Enumeration e = openedFiles.elements(); e.hasMoreElements(); ) 
    {
      ((HdfeosFile)e.nextElement()).close();
    }
  }
}
