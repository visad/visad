//
// hdfeosFile.java
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
import visad.*;

  public class hdfeosFile {

    String filename;
    int  file_id;
    int  n_grids;
    int  n_swaths;

    Vector allSwaths;
    Vector allGrids;
    Vector allPoints;

    static Vector openedFiles = new Vector();          // all opened file objects

    static int DFACC_READ = 1;
    static int HDFE_mode = 4;

    hdfeosFile( String filename )  {

      this.filename = filename;

      String[] swath_list = {"empty"};
      n_swaths = library.Lib.SWinqswath( filename, swath_list );

           System.out.println( "n_swaths: "+n_swaths);
           System.out.println( "name_list: "+swath_list[0]);

      if ( n_swaths > 0 )  {

         allSwaths = new Vector();

         file_id = library.Lib.SWopen( filename, DFACC_READ );

            System.out.println( "file_id: "+file_id);

         StringTokenizer swaths = new StringTokenizer( swath_list[0], ",", false );
         while ( swaths.hasMoreElements() )
         {
           String swath = (String) swaths.nextElement();
           eosSwath obj = new eosSwath( file_id, swath );
           allSwaths.addElement( obj );
         }
      } 


      String[] grid_list = {"empty"};
      n_grids = library.Lib.GDinqgrid( filename, grid_list );

           System.out.println( "n_grids: "+n_grids);
           System.out.println( "name_list: "+grid_list[0]);


      if ( n_grids > 0 ) {

         allGrids = new Vector();

         file_id = library.Lib.GDopen( filename, DFACC_READ );

            System.out.println( "file_id: "+file_id);

         StringTokenizer grids = new StringTokenizer( grid_list[0], ",", false );
 
         while ( grids.hasMoreElements() ) 
         {
           String grid = (String) grids.nextElement();
           eosGrid g_obj = new eosGrid( file_id, grid );
           allGrids.addElement( g_obj );
         }

      }

      openedFiles.addElement( this );

    }

    public int getNumberOfGrids() {

       return n_grids; 
    }

    public eosGrid getGrid( int ii ) {

       return (eosGrid) allGrids.elementAt(ii);
    }

    public int getNumberOfSwaths() {

       return n_swaths;
    }

    public eosSwath getSwath( int ii ) {

       return (eosSwath) allSwaths.elementAt(ii);
    }


    public static void close() throws hdfeosException {

      for ( Enumeration e = openedFiles.elements(); e.hasMoreElements(); ) 
      {
        int status = library.Lib.EHclose( ((hdfeosFile) e.nextElement()).file_id );

        if ( status < 0 ) 
        {
          throw new hdfeosException(" trouble closing file, status: "+status );
        }
      }
    }
}
