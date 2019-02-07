/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.amanda;

import java.rmi.RemoteException;

import visad.BaseColorControl;
import visad.ScalarMap;
import visad.VisADException;
import visad.VisADQuadArray;

public abstract class F2000Util
{
  private static final float CUBE = 0.05f;

  public static final VisADQuadArray[] getCubeArray()
  {
    VisADQuadArray cube = new VisADQuadArray();
    cube.coordinates = new float[]
      {CUBE,  CUBE, -CUBE,     CUBE, -CUBE, -CUBE,
       CUBE, -CUBE, -CUBE,    -CUBE, -CUBE, -CUBE,
       -CUBE, -CUBE, -CUBE,    -CUBE,  CUBE, -CUBE,
       -CUBE,  CUBE, -CUBE,     CUBE,  CUBE, -CUBE,

       CUBE,  CUBE,  CUBE,     CUBE, -CUBE,  CUBE,
       CUBE, -CUBE,  CUBE,    -CUBE, -CUBE,  CUBE,
       -CUBE, -CUBE,  CUBE,    -CUBE,  CUBE,  CUBE,
       -CUBE,  CUBE,  CUBE,     CUBE,  CUBE,  CUBE,

       CUBE,  CUBE,  CUBE,     CUBE,  CUBE, -CUBE,
       CUBE,  CUBE, -CUBE,     CUBE, -CUBE, -CUBE,
       CUBE, -CUBE, -CUBE,     CUBE, -CUBE,  CUBE,
       CUBE, -CUBE,  CUBE,     CUBE,  CUBE,  CUBE,

       -CUBE,  CUBE,  CUBE,    -CUBE,  CUBE, -CUBE,
       -CUBE,  CUBE, -CUBE,    -CUBE, -CUBE, -CUBE,
       -CUBE, -CUBE, -CUBE,    -CUBE, -CUBE,  CUBE,
       -CUBE, -CUBE,  CUBE,    -CUBE,  CUBE,  CUBE,

       CUBE,  CUBE,  CUBE,     CUBE,  CUBE, -CUBE,
       CUBE,  CUBE, -CUBE,    -CUBE,  CUBE, -CUBE,
       -CUBE,  CUBE, -CUBE,    -CUBE,  CUBE,  CUBE,
       -CUBE,  CUBE,  CUBE,     CUBE,  CUBE,  CUBE,

       CUBE, -CUBE,  CUBE,     CUBE, -CUBE, -CUBE,
       CUBE, -CUBE, -CUBE,    -CUBE, -CUBE, -CUBE,
       -CUBE, -CUBE, -CUBE,    -CUBE, -CUBE,  CUBE,
       -CUBE, -CUBE,  CUBE,     CUBE, -CUBE,  CUBE};

    cube.vertexCount = cube.coordinates.length / 3;
    cube.normals = new float[144];
    cube.normals = new float[144];
    for (int i=0; i<24; i+=3) {
      cube.normals[i]     =  0.0f;
      cube.normals[i+1]   =  0.0f;
      cube.normals[i+2]   = -1.0f;

      cube.normals[i+24]  =  0.0f;
      cube.normals[i+25]  =  0.0f;
      cube.normals[i+26]  =  1.0f;

      cube.normals[i+48]  =  1.0f;
      cube.normals[i+49]  =  0.0f;
      cube.normals[i+50]  =  0.0f;

      cube.normals[i+72]  = -1.0f;
      cube.normals[i+73]  =  0.0f;
      cube.normals[i+74]  =  0.0f;

      cube.normals[i+96]  =  0.0f;
      cube.normals[i+97]  =  1.0f;
      cube.normals[i+98]  =  0.0f;

      cube.normals[i+120] =  0.0f;
      cube.normals[i+121] = -1.0f;
      cube.normals[i+122] =  0.0f;
    }

    return new VisADQuadArray[] {cube};
  }

  public static final void invertColorTable(ScalarMap colorMap)
  {
    BaseColorControl colorCtl = (BaseColorControl )colorMap.getControl();
    final int numColors = colorCtl.getNumberOfColors();
    final int numComps = colorCtl.getNumberOfComponents();
    float[][] table = colorCtl.getTable();
    for (int i = 0; i < numColors / 2; i++) {
      final int swaploc = numColors - (i + 1);
      for (int j = 0; j < numComps; j++) {
        float tmp = table[j][i];
        table[j][i] = table[j][swaploc];
        table[j][swaploc] = tmp;
      }
    }

    try {
      colorCtl.setTable(table);
    } catch (RemoteException re) {
      System.err.println("Couldn't invert color table");
      re.printStackTrace();
    } catch (VisADException ve) {
      System.err.println("Couldn't invert color table");
      ve.printStackTrace();
    }
  }
}
