//
// F2000Form.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.rmi.RemoteException;

import visad.Data;
import visad.DataImpl;
import visad.RealType;
import visad.VisADException;
import visad.VisADQuadArray;

import visad.data.BadFormException;
import visad.data.Form;
import visad.data.FormNode;
import visad.data.FormFileInformer;

/**
   F2000Form is the VisAD data format adapter for
   F2000 files for Amanda events.<P>
*/
public class F2000Form
  extends Form
  implements FormFileInformer
{
  private static final float CUBE = 0.05f;

  private static int num = 0;

  private AmandaFile file = null;

  public F2000Form()
  {
    super("F2000Form#" + num++);
  }

  public synchronized void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("F2000Form.add");
  }

  public final RealType getAmplitude() { return AmandaFile.getAmplitudeType(); }

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

  public String[] getDefaultSuffixes()
  {
    String[] suff = { "r" };
    return suff;
  }

  public static final RealType getEventIndex() { return AmandaFile.getEventIndexType(); }

  public synchronized FormNode getForms(Data data)
  {
    return null;
  }

  public static final RealType getLet() { return AmandaFile.getLeadEdgeTimeType(); }
  public static final RealType getTrackIndex() { return Event.getTrackIndexType(); }

  public static final RealType getX() { return AmandaFile.getXType(); }
  public final double getXMax() { return file.getXMax(); }
  public final double getXMin() { return file.getXMin(); }

  public static final RealType getY() { return AmandaFile.getYType(); }
  public final double getYMax() { return file.getYMax(); }
  public final double getYMin() { return file.getYMin(); }

  public static final RealType getZ() { return AmandaFile.getZType(); }
  public final double getZMax() { return file.getZMax(); }
  public final double getZMin() { return file.getZMin(); }

  public boolean isThisType(String name)
  {
    return name.endsWith(".r");
  }

  public boolean isThisType(byte[] block)
  {
    return false;
  }

  public synchronized DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    FileInputStream fileStream = new FileInputStream(id);
    return open(fileStream);
  }

  public synchronized DataImpl open(URL url)
    throws BadFormException, VisADException, IOException
  {
    InputStream inputStream = url.openStream();
    return open(inputStream);
  }

  private synchronized DataImpl open(InputStream is)
    throws BadFormException, VisADException, IOException
  {
    file = new AmandaFile(is);
    return file.makeData();
  }

  public synchronized void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    throw new BadFormException("F2000Form.save");
  }
}
