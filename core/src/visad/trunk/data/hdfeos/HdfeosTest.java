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
import visad.data.*;
import visad.Set;
import java.rmi.*;

public class HdfeosTest 
{

  public static void main( String args[] ) throws VisADException, RemoteException 
  {


  //String filename = "/home/rink/HDF-EOS/data/MOP02_partday.hdf";
  //String filename = "/home/rink/HDF-EOS/data/MOD07.V2.hdf";
  //String filename = "/home/rink/HDF-EOS/data/NISE_SSMIF11_19911227.HDFEOS";
    String filename = "/home/rink/HDF-EOS/data/DAS.flk.asm.tsyn2d_mis_x.AM100.1997082900.1997082921";

      double[][] values;
      Data F_data;

      HdfeosDefault default_form = new HdfeosDefault();

      DataImpl data = default_form.open( filename );

      MathType M_type = data.getType();
      System.out.println( M_type.toString() );

      String range_name = "t10m";

      F_data = extractFunction( data, range_name );

      Data FF_data = extractFlatField( (FieldImpl)F_data, range_name, 0 );
      if ( FF_data instanceof FileFlatField )
      {
        System.out.println("works, FileFlatField");

        Set set = ((FileFlatField)FF_data).getDomainSet();
        values = ((FileFlatField)FF_data).getValues();

        for ( int jj = 0; jj < 160; jj++ ) {
           System.out.println( values[0][jj] );
        }
        System.out.println( set.toString() );
      }

      HdfeosFile.close();

  }

  public static Data extractFunction( Data data, String range_name )
         throws VisADException, RemoteException
  {
    Data t_data; 
    Data r_data = null; 
    Data dat;
    MathType M_type;

    if ( data instanceof Tuple ) 
    {
      for ( int ii = 0; ii < ((Tuple)data).getDimension(); ii++ )
      { 
        t_data = ((Tuple)data).getComponent( ii );
        dat = extractFunction( t_data, range_name );
        if ( dat != null ) r_data = dat;
      }
      return r_data;
    }
    else if ( data instanceof FileFlatField )
    {
       M_type = ((FunctionType)(data.getType())).getRange();
       if ( M_type instanceof RealType )
       {
          String name = ((ScalarType)M_type).getName();
          if ( name.equals(range_name) )
          {
             r_data = data;
          }
       } 
       else if ( M_type instanceof RealTupleType )
       {
         throw new VisADException(" unimplemented ");
       }
       else 
       {
         throw new VisADException(" unimplemented " );
       }
    }
    else if ( data instanceof FieldImpl )
    {
        M_type = ((FunctionType)(data.getType())).getRange();
        if ( isNameNested( M_type, range_name ) )
        {
          r_data = data;
        }
    }
    else 
    {
      throw new VisADException("Confused");
    }
    return r_data;
  }

  public static boolean isNameNested( MathType M_type, String name )
                 throws VisADException, RemoteException
  {
    MathType R_type = null;
    MathType t_type;
    MathType type;

    if ( M_type instanceof TupleType )
    {
      for ( int ii = 0; ii < ((TupleType)M_type).getDimension(); ii++ )
      {
        t_type = ((TupleType)M_type).getComponent(ii);
        if ( isNameNested( t_type, name ) ) 
        {
          return true;
        }
      } 
      return false;
    }
    else if ( M_type instanceof RealType )
    {
      if ( (((ScalarType)M_type).getName()).equals( name ) )
      {
         return true;
      }
      else 
      {
         return false;
      }
    }
    else if ( M_type instanceof RealTupleType )
    {
      for ( int jj = 0; jj < ((RealTupleType)M_type).getDimension(); jj++ )
      {
        t_type = ((RealTupleType)M_type).getComponent(jj);
        if ( (((ScalarType)t_type).getName()).equals( name ) )
        {
           return true;
        }
      }
      return false;
    }
    else if ( M_type instanceof FunctionType )
    {
       t_type = ((FunctionType)M_type).getRange();
       return isNameNested( t_type, name );
    }
    return false;
  }

  public static Data extractFlatField( FieldImpl field, String range_name, int index )
              throws VisADException, RemoteException
  {
     Data data;

     data = field.getSample( index );
   
     data = extractFunction( data, range_name );

     if ( data instanceof FlatField )
     {
        return data;
     }
     else 
     {
       return null;
     }
  }

}
