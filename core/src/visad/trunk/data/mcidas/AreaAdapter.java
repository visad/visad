//
// AreaAdapter.java
//

/*

The software in this file is Copyright(C) 1998 by Tom Whittaker.
It is designed to be used with the VisAD system for interactive 
analysis and visualization of numerical data.  
 
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

import edu.wisc.ssec.mcidas.*;
import java.io.IOException;
import java.rmi.RemoteException;
import visad.CoordinateSystem;
import visad.DateTime;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.Linear2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.TypeException;
import visad.Unit;
import visad.VisADException;

/** this is an adapter for McIDAS AREA images */

public class AreaAdapter {

  private FlatField field = null;
  private AREACoordinateSystem cs;
  private AreaDirectory areaDirectory;

  /** Create a VisAD FlatField from a local McIDAS AREA file or a URL.
    * @param imageSource name of local file or a URL to locate file.
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public AreaAdapter(String imageSource) throws IOException, VisADException {
    try {
      AreaFile af = new AreaFile(imageSource);
      buildFlatField(af);
    } catch (McIDASException afe) {throw new
        VisADException("Problem with McIDAS AREA file: " + afe);
    }
  }


  /** Build a FlatField from the image pixels
    * @param af is the AreaFile
    * @exception VisADException if an unexpected problem occurs.
    */
  private void buildFlatField(AreaFile af) throws VisADException {

    int[] nav=null;

    try {
      areaDirectory = af.getAreaDirectory();
      nav = af.getNav();
    } catch (Exception rmd) {
        throw new VisADException(
            "Problem getting Area file directory or navigation"); 
    }

    // extract the size of each dimension from the directory
    int numLines = areaDirectory.getLines();
    int numEles = areaDirectory.getElements();

    // make the VisAD RealTypes for the dimension variables
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

    // extract the number of bands (sensors) and make the VisAD type
    int bandNums[] = areaDirectory.getBands();
    int numBands = bandNums.length;
    RealType[] bands = new RealType[numBands];

    // first cut: the bands are named "Band##" where ## is the
    // band number from the AREA file bandmap
    for (int i = 0; i < numBands; i++)
    {
        RealType band = null;
        try {
          band = new RealType("Band"+bandNums[i]);
        } catch (TypeException e) {
          band= RealType.getRealTypeByName("Band"+bandNums[i]);
        }
        bands[i] = band;
    }

    // the range of the FunctionType is the band(s)
    RealTupleType radiance = new RealTupleType(bands);

    // the domain is (element,line) since elements (X) vary fastest
    RealType[] domain_components = {element,line};

    // Create the appropriate CoordinateSystem and attach it to
    // the domain of the FlatField.  AREACoordinateSystem transforms
    // from (ele,lin) -> (lat,lon)
    try
    {
        cs = new AREACoordinateSystem(
                RealTupleType.LatitudeLongitudeTuple, 
                areaDirectory.getDirectoryBlock(), 
                nav);
    }
    catch (VisADException e)
    {
      System.out.println(e);
      System.out.println("Using null CoordinateSystem");
      cs = null;
    }

    RealTupleType image_domain = 
                new RealTupleType(domain_components, cs, null);

    //  Image numbering is usually the first line is at the "top"
    //  whereas in VisAD, it is at the bottom.  So define the
    //  domain set of the FlatField to map the Y axis accordingly

    Linear2DSet domain_set = new Linear2DSet(image_domain,
                                0.0, (float) (numEles - 1), numEles,
                                (float) (numLines - 1),0.0, numLines );
    FunctionType image_type =
                        new FunctionType(image_domain, radiance);

    // If calibrationType is brightnes (BRIT), then we can store
    // the values as shorts.  To do this, we crunch the values down
    // from 0-255 to 0-254 so we can have 255 left over for missing
    // values.

    if (areaDirectory.getCalibrationType().equalsIgnoreCase("BRIT"))
    {
        Set[] rangeSets = new Set[numBands];
        for (int i = 0; i < numBands; i++)
            rangeSets[i] = new Integer1DSet(bands[i], 255);
        field = new FlatField(image_type,
                              domain_set, 
                              (CoordinateSystem[]) null, 
                              rangeSets, 
                              (Unit[]) null);
    }
    else
        field = new FlatField(image_type,domain_set);   // use default sets

    // get the data
    int[][][] int_samples;
    try {
      int_samples = af.getData();

    } catch (McIDASException samp) {
        throw new VisADException("Problem reading AREA file: "+samp);
    }
      
    // for each band, create a sample array for the FlatField

    try {
      float[][] samples = new float[numBands][numEles*numLines];

      for (int b=0; b<numBands; b++) {
        for (int i=0; i<numLines; i++) {
          for (int j=0; j<numEles; j++) {

            samples[b][j + (numEles * i) ] = 
               (areaDirectory.getCalibrationType().equalsIgnoreCase("BRIT") &&
                int_samples[b][i][j] == 255)
                   ? 254.0f                   // push 255 into 254 for BRIT
                   : (float)int_samples[b][i][j];
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
    * @return dim[0]=number of bands, dim[1] = number of elements, 
    *   dim[2] = number of lines
   */
  public int[] getDimensions() {
    int[] dim = new int[3];
    dim[0] = areaDirectory.getNumberOfBands();
    dim[1] = areaDirectory.getElements();
    dim[2] = areaDirectory.getLines();
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

  /**
   * Return a FlatField representing the image.  The field will look
   * like the following:<P>
   * <UL>
   * <LI>Domain - Linear2DSet of (ImageLine, ImageElement) with 
   *              AREACoordinateSystem to Lat/Lon (may be null).
   * <LI>Range - One or more bands.  If calibration type is BRIT,
   *             Integer1DSets are used as the range sets with length 255.
   *             (brightness 255 is same as 254).
   * </UL>
   *
   * @return image as a FlatField
   */
  public FlatField getData() {
    return field;
  }

  /**
   * Retrieves the "nominal" time of the image as a VisAD DateTime.  This
   * may or may not be the start of the image scan.  Values are derived from
   * the 4th and 5th words in the AREA file directory.
   * @see <a href="http://www.ssec.wisc.edu/mug/prog_man/prog_man.html">
   * McIDAS Programmer's Manual</a> 
   * @see #getImageStartTime()
   *
   * @ returns  nominal image time
   */
  public DateTime getNominalTime() 
      throws VisADException 
  {
      return new DateTime(areaDirectory.getNominalTime());
  }

  /**
   * Retrieves the time of the start of the image scan as a VisAD DateTime.  
   * Values are derived from the 46th and 47th words in the AREA file directory.
   * @see <a href="http://www.ssec.wisc.edu/mug/prog_man/prog_man.html">
   * McIDAS Programmer's Manual</a> 
   * @see #getNominalTime()
   *
   * @ returns  time of the start of the image scan
   */
  public DateTime getImageStartTime() 
      throws VisADException 
  {
      return new DateTime(areaDirectory.getStartTime());
  }
}
