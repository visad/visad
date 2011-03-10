//
// Vis5DFileAccessor.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.vis5d;

import visad.*;
import visad.data.*;
import java.rmi.*;

public class Vis5DFileAccessor extends FileAccessor
{
   private int time_idx;
   private Vis5DFile v5dfile;

   public Vis5DFileAccessor(Vis5DFile v5dfile, int time_idx)
   {
     this.v5dfile = v5dfile;
     this.time_idx = time_idx;
   }

   public FlatField getFlatField()
          throws VisADException, RemoteException
   {
     FlatField ff = null;
     try {
       ff = Vis5DForm.makeFlatField(v5dfile, time_idx);
     }
     catch (java.io.IOException e) {
       System.out.println(e.getMessage());
     }
     return ff;
   }

   public FunctionType getFunctionType()
          throws VisADException
   {
     return v5dfile.grid_type;
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
