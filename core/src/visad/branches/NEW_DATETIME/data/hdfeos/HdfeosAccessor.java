//
// HdfeosAccessor.java
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

import visad.*;
import visad.data.*;
import java.lang.*;
import java.rmi.*;

public class HdfeosAccessor extends FileAccessor
{
   private HdfeosData data;
   private int[] indexes;

   public HdfeosAccessor( HdfeosData data, int[] indexes )
   {
     this.data = data;
     this.indexes = indexes;
   }

   public FlatField getFlatField() 
          throws VisADException, RemoteException
   {
     return (FlatField) data.getData(indexes);
   }

   public FunctionType getFunctionType() 
          throws VisADException
   {
     return (FunctionType) data.getType();
   }

   public void writeFile( int[] fileLocations, Data range ) 
   {

   }

   public double[][] readFlatField( FlatField template, int[] fileLocation )
   {
     return null;
   }

   public void writeFlatField( double[][] values, FlatField template, int[] fileLocation )
   {

   }
}
