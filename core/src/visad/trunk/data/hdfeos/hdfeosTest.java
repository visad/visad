//
// hdfeosTest.java
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
import java.rmi.*;

public class hdfeosTest {


/**
   public DataImpl open( String filename ) {


     hdfeosFile file = new hdfeosFile( filename );


















   }
 **/

  public static void main( String args[] ) throws VisADException, RemoteException 
  {


  /*String filename = "/usr6/rink/HDF-EOS/data/MOP02_partday.hdf"; */
  /*String filename = "/usr6/rink/HDF-EOS/data/MOD07.V2.hdf"; */
    String filename = "/usr6/rink/HDF-EOS/data/NISE_SSMIF11_19911227.HDFEOS";

      hdfeosFileDefault file = new hdfeosFileDefault( filename );

   /* eosSwath swath1 = (eosSwath)file.allSwaths.elementAt(0);*/

      eosGrid grid1 = (eosGrid)file.allGrids.elementAt(0);

   /* String out = swath1.DV_Set.toString();*/
      String out = grid1.DV_Set.toString();
      System.out.println(out);

    /*out = swath1.DV_shapeSet.toString();*/
      out = grid1.DV_shapeSet.toString();
      System.out.println(out);


   /* MathType M_type = swath1.getVisADMathType(); */

    /*DataImpl data = swath1.getVisADDataObject();*/
      DataImpl data = grid1.getVisADDataObject();
 
      MathType M_type = data.getType();
      System.out.println( M_type.toString() );


/*
      for ( int ii = 0; ii < 15; ii++ ) {
  
      for ( int jj = 0; jj < 4; jj++) {

        FileFlatField FF_field = (FileFlatField) 
        ((Tuple)((Tuple)((FieldImpl)data).getSample(ii)).getComponent(0)).getComponent(jj);

        System.out.println( FF_field.getSample(8) );

        Set domainSet = FF_field.getDomainSet();
      }
        System.out.println("**");

      }
        System.out.println("- - - - - - - - - - - ");
*/
/*

      for ( int ii = 0; ii < 15; ii++ ) {

        FileFlatField FF_field =
       (FileFlatField) ((Tuple)((FieldImpl)data).getSample(ii)).getComponent(0);

        System.out.println( FF_field.getSample(8) );

      }
*/


  }


}
