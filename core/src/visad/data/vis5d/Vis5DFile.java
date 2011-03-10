//
// Vis5DFile.java
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

import visad.FunctionType;
import visad.RealType;
import visad.Set;

public class Vis5DFile
{
   String filename;
   V5DStruct vv;
   Set space_set;
   FunctionType grid_type;
   RealType[] vars;
   int[] vars_indexes;
   int nvars;
   int grid_size;


   public Vis5DFile(String filename,
                    V5DStruct  vv,
                    Set space_set,
                    FunctionType grid_type,
                    RealType[] vars,
                    int[] vars_indexes,
                    int grid_size )
   {
     this.filename = filename;
     this.vv = vv;
     this.space_set = space_set;
     this.grid_type = grid_type;
     this.vars = vars;
     this.nvars = vars.length;
     this.vars_indexes = vars_indexes;
     this.grid_size = grid_size;
   }
}
