//
// TextForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

package visad.gifts;

import java.util.Vector; 
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;
import visad.*;
import java.rmi.*;

public class TextForm
{
  boolean eastLongitudePositive = true;
  String longitudeName = null;
  String latitudeName = null;

  public TextForm()
  {
    this( true, "Longitude", "Latitude");
  }

  public TextForm(String lon_name, String lat_name)
  {
    this(true, lon_name, lat_name);
  }
  
  public TextForm(boolean elp, String lon_name, String lat_name)
  {
    eastLongitudePositive = elp;
    longitudeName = lon_name;
    latitudeName = lat_name;
  }

  public DataImpl open( String file_path ) 
         throws VisADException, RemoteException 
  {
    DataImpl data = null;
    try 
    {
      data = getFileData(file_path);
    }
    catch ( FileNotFoundException e1 ) 
    {
      System.out.println(e1.getMessage());
    }
    catch ( IOException e2 ) 
    {
      System.out.println(e2.getMessage());
    }

    if ( !eastLongitudePositive ) {  //--  switch to eastLongitudePositive
      return switchLongitudeSign(data);
    }
    else {
      return data;
    }
  }

  private DataImpl getFileData(String file_path) 
          throws VisADException, RemoteException,
                 FileNotFoundException, IOException 
  {
    DataImpl data = null;
    MathType type = null;

    String line = null;
    String token;
    Vector lines = new Vector();
    String header = null;
    int n_lines = 0;
    FileReader reader = new FileReader(file_path);

    LineNumberReader line_reader = new LineNumberReader(reader);

    header = line_reader.readLine();
    lines.addElement(header);

    String line_1 = line_reader.readLine();
    lines.addElement(line_1);

    int[][] indexes = new int[1][];
    type = getFileType(header, line_1, indexes);
    int tup_dim = ((RealTupleType)((FunctionType)type).getRange()).getDimension();
  
    while ( (line = line_reader.readLine()) != null )
    {
      lines.addElement(line);
    }

    int n_samples = lines.size() - 1;
    float[][] values = new float[tup_dim][n_samples];

    for (int ii = 0; ii < n_samples; ii++) 
    { 
      StringTokenizer tokens = new StringTokenizer((String)lines.elementAt(ii+1));
      int cnt = 0;
      while ( tokens.hasMoreElements() )
      {
        token = tokens.nextToken();
        for ( int jj = 0; jj < tup_dim; jj++ ) {
          if ( cnt == indexes[0][jj] ) {
            values[jj][ii] = (Float.valueOf(token)).floatValue();
          }
        }
        cnt++;
      }
    }

    Integer1DSet domainSet = new Integer1DSet(((FunctionType)type).getDomain(), n_samples);
    FlatField f_field = new FlatField((FunctionType)type, domainSet);
    f_field.setSamples(values);
    return f_field;
  }

  private MathType getFileType(String header, String line_1, int[][] indexes)
          throws VisADException, RemoteException
  {
    StringTokenizer tokens = new StringTokenizer(header);
    StringTokenizer values = new StringTokenizer(line_1);
    String token;
    Vector names = new Vector();
    Vector types = new Vector();
    Vector comps = new Vector();
    MathType type;
    float value;

    while ( tokens.hasMoreElements() ) 
    { 
      token = tokens.nextToken();
      names.addElement(token);
    }
    
    int tup_dim = names.size();
    indexes[0] = new int[tup_dim];
    int cnt_num = 0;
    RealType r_type = null;
    for ( int ii = 0; ii < tup_dim; ii++ ) {
      try {
        Float.valueOf((String)values.nextToken());
        String name = (String)names.elementAt(ii);
        if ( name.equals(longitudeName) ) {
          r_type = RealType.Longitude;
        }
        else if ( name.equals(latitudeName) ) {
          r_type = RealType.Latitude;
        }
        else {
          r_type = RealType.getRealType(name);
        }
        types.addElement(r_type);
        indexes[0][cnt_num++] = ii;
      }
      catch ( NumberFormatException e ) {
      }
    }

    RealType[] r_types = new RealType[types.size()];
    for ( int ii = 0; ii < r_types.length; ii++ ) {
      r_types[ii] = (RealType) types.elementAt(ii);
    }

    RealTupleType range = new RealTupleType(r_types);
    RealType domain = RealType.getRealType("index");
    
    type = new FunctionType(domain, range);
    return type;
  }

  private DataImpl switchLongitudeSign(DataImpl data)
          throws VisADException, RemoteException
  {
     int idx = ((RealTupleType)((FunctionType)data.getType()).getRange()).getIndex("Longitude");
     double[][] values = ((FlatField)data).getValues();

     for ( int ii = 0; ii < values[idx].length; ii++ ) {
       values[idx][ii] = -1d*values[idx][ii];
     }
     ((FlatField)data).setSamples(values);
     return data;
  }
}
