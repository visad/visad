//
// AreaAdapter.java
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

package visad.data.mcidas;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import visad.FlatField;
import visad.FunctionType;
import visad.Linear2DSet;
import visad.Integer2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.TypeException;
import visad.VisADException;
import visad.data.mcidas.*;
import visad.Unit;
import visad.CoordinateSystem;
import visad.CommonUnit;

/** this is an adapter for McIDAS AREA images */

public class AreaAdapter {

  private FlatField field = null;
  private GVARCoordinateSystem cs;
  private int numLines, numEles, numBands;

  /** Create a VisAD FlatField from a local McIDAS AREA file
    * @param filename name of local file.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public AreaAdapter(String filename) throws IOException, VisADException {
    try {
      AreaFile af = new AreaFile(filename);
      buildFlatField(af);
    } catch (AreaFileException aef) {throw new
        VisADException("Problem with McIDAS AREA file:"+aef);
    }
  }


  /** Create a VisAD FlatField from a McIDAS AREA on the Web.
    * @param URL & filename name of remote file
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public AreaAdapter(URL url) throws IOException, VisADException {
    try {
      AreaFile af = new AreaFile(url);
      buildFlatField(af);
    } catch (AreaFileException aef) {throw new
        VisADException("Problem with McIDAS AREA file:"+aef);
    }
  }


  /** Build a FlatField from the image pixels
    * @param af is the AreaFile
    * @exception VisADException if an unexpected problem occurs.
    */
  private void buildFlatField(AreaFile af) throws VisADException {

    int[] dir=null;
    int[] nav=null;

    try {
      dir = af.getDir();
      nav = af.getNav();
    } catch (Exception rmd) {
	throw new VisADException("Problem getting Area file directory"); 
    }

    numLines = dir[8];
    numEles = dir[9];

    RealType line;
    try {
      line = new RealType("ImageLine",null,null);
    } catch (TypeException e) {
      line =  RealType.getRealTypeByName("ImageLine");
    }

    RealType element;
    try {
      element = new RealType("ImageElement",null,null);
    } catch (TypeException e) {
      element =  RealType.getRealTypeByName("ImageElement");
    }

    // when it comes time to deal with multiple bands, change this...

    numBands = dir[13];

    RealType[] bands = new RealType[numBands];

    // do we want to 'name' the bands something else?
    int bmap = dir[18];
    int bcount = 0;
    for (int i=1; i<33; i++) {
      if ( (bmap & 1) != 0) {
	bcount = bcount + 1;
	if (bcount > numBands) {
	  throw new VisADException("Invalid Area file bandmap");
        }
	RealType band=null;
	try {
	  band = new RealType("Band"+i);
	} catch (TypeException e) {
	  band= RealType.getRealTypeByName("Band"+i);
	}
        bands[bcount-1] = band;
      }
      bmap = bmap >>> 1;
    }

    RealTupleType radiance = new RealTupleType(bands);
    RealType[] domain_components = {element,line};
    RealTupleType ref = new RealTupleType
		  (RealType.Latitude, RealType.Longitude);
    cs = new GVARCoordinateSystem(ref, dir, nav);
    RealTupleType image_domain = 
		new RealTupleType(domain_components, cs, null);

    Linear2DSet domain_set = new Linear2DSet(image_domain,
				0.0, (float) (numEles - 1), numEles,
				(float) (numLines - 1),0.0, numLines );
    FunctionType image_type =
			new FunctionType(image_domain, radiance);
    field = new FlatField(image_type,domain_set);

    int[][][] int_samples;
    try {
      int_samples = af.getData();

    } catch (AreaFileException samp) {
	throw new VisADException("Problem reading AREA file: "+samp);
    }
      
    try {
      float[][] samples = new float[numBands][numEles*numLines];

      for (int b=0; b<numBands; b++) {
	for (int i=0; i<numLines; i++) {
	  for (int j=0; j<numEles; j++) {

	    samples[b][j + (numEles * i) ] = (float)int_samples[b][i][j];
	  }
	}
      }

      field.setSamples( samples, false);

    } catch (RemoteException e) {
        throw new VisADException("Couldn't finish image initialization");
    }

  }


  /**
    * get the dimensions of the image
    *
    * @return dim[0]=number bands, dim[1] = number elements, 
    *   dim[2] = number lines
   */

  public int[] getDimensions() {
    int[] dim = new int[3];
    dim[0] = numBands;
    dim[1] = numEles;
    dim[2] = numLines;
    return dim;
  }

  /**
    * get the CoordinateSystem of the image
    *
    * @return the CoordinateSystem object
   */

  public CoordinateSystem getCoordinateSystem() {
    return cs;
  }

  public FlatField getData() {
    return field;
  }
}
