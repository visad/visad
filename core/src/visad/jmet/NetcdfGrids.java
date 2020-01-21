
//
// NetcdfGrids.java
//

/*

The software in this file is Copyright(C) 2020 by Tom Whittaker.
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

package visad.jmet;

import java.io.File;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import ucar.netcdf.Attribute;
import ucar.netcdf.AttributeSet;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.DimensionSet;
import ucar.netcdf.NetcdfFile;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import visad.CoordinateSystem;
import visad.Data;
import visad.DateTime;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DDoubleSet;
import visad.Integer2DSet;
import visad.Real;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.Tuple;
import visad.Unit;
import visad.data.units.Parser;

/**
 * Reads data from netCDF NCEP model grids one parameter at a time
 * instead of the whole file (see Plain).  This is pretty focused on
 * delivering grids that are renderable in 3D.
 *
 * @author Tom Whittaker
 */
public class NetcdfGrids {
  File filename;
  NetcdfFile nc = null;
  double[] pressureLevels;
  double[] valtime;
  double[][] times;
  Set time_set;
  int[] timeIndex;
  DateTime[] validDateTime;
  int num_levels, num_records, xval, yval;
  RealType x,y,level,time_type,pres;
  RealTupleType grid_domain;
  Integer2DSet dom_set;
  CoordinateSystem gridCoord;
  int gridNumber, gridTypeCode;

  /** set up to read grids from a netCDF file
  *
  * @param filename is the name of the netCDF file to read from
  *
  */


  public NetcdfGrids(String filename) {
    this(new File(filename) );
  }

  public NetcdfGrids(File filename) {
    this.filename = filename;
    dom_set = null;
    gridCoord = null;

    try {
      nc = new NetcdfFile(filename, true);
      AttributeSet as = null;
      DimensionSet ds = null;

      // This block is just for me to look at the file...

/* *****************************************************************

      // global attributes
      as = nc.getAttributes();
      AttributeIterator ai = as.iterator();
      int num = as.size();
      System.out.println("number of attributes = "+num);
      while (ai.hasNext() ) {
        Attribute a = ai.next();
        System.out.println("Attribute "+a);
      }

      // dimensions
      ds = nc.getDimensions();
      DimensionIterator di = ds.iterator();
      int numDims = ds.size();
      while (di.hasNext() ) {
        ucar.netcdf.Dimension d = di.next();
        System.out.println("Dimension "+d.getName()+" has length="+
                d.getLength() );
      }

      // variables
      VariableIterator vi = nc.iterator();
      int numVars = nc.size();
      while (vi.hasNext() ) {

        Variable v = vi.next();
        System.out.println("Variable "+v.getName()+
              " has rank = "+v.getRank() );

          as = v.getAttributes() ;
          ai = as.iterator();
          num = as.size();
          System.out.println("   Number of attributes = "+num);
          while (ai.hasNext() ) {
            Attribute a = ai.next();
            System.out.println("   Attribute "+a);
          }

          di = v.getDimensionIterator();
          numDims = v.getRank();
          while (di.hasNext() ) {
            ucar.netcdf.Dimension d = di.next();
            System.out.println("   Dimension "+d.getName()+" has length = "+
                    d.getLength() );
          }

      }
************************************************************* */

      // get the pressure levels values

      int index[] = new int[1];
      Variable p = nc.get("level");
      if (p != null) {
        int temp[] = p.getLengths();
        num_levels = temp[0];
        pressureLevels = new double[num_levels];
        for (int i=0; i<num_levels; i++) {
          index[0] = i;
          pressureLevels[i] = p.getDouble(index);
        }
      } else {
        num_levels = 1;
        pressureLevels = new double[num_levels];
        pressureLevels[0] = 1020.0;
      }


      // get the number of records
      num_records = (nc.getDimensions()).get("record").getLength();

      // get the pressure levels values

      Variable t = nc.get("valtime");
      valtime = new double[num_records];
      validDateTime = new DateTime[num_records];
      timeIndex = new int[num_records];
      times = new double[1][num_records];
      TreeMap orderTimes = new TreeMap();

      if (t != null) {
	Unit baseTimeUnit =
	    Parser.parse(t.getAttribute("units").getStringValue());
        int temp[] = t.getLengths();
        for (int i=0; i<num_records; i++) {
          index[0] = i;
          valtime[i] = t.getDouble(index);
	  orderTimes.put(new Double(valtime[i]), new Integer(i));
	  validDateTime[i] = new DateTime(new Real(RealType.Time,valtime[i],
	    baseTimeUnit) );
        }
	//java.util.Set timeSet = orderTimes.keySet();
	Iterator setIt = orderTimes.keySet().iterator();

	for (int i=0; i<num_records; i++) {
	  Double sv = (Double) setIt.next();
	  Integer isv = (Integer) orderTimes.get(sv);
	  timeIndex[i] = isv.intValue();
	  times[0][i] = validDateTime[timeIndex[i]].getValue();
	}

      } else {
	System.out.println("cannot find valtime-s...using linear steps");
	valtime = null;
	for (int i=0; i<num_records; i++) {
	  timeIndex[i] = i;
	  times[0][i] = (double)i;
	}
      }


      p = nc.get("grid_number");
      int index2[] = new int[p.getRank()];
      for (int i=0; i<index2.length; i++) {index2[i] = 0; }

      gridNumber = p.getInt(index2);
      System.out.println("grid number = "+gridNumber);

      p = nc.get("grid_type_code");
      index[0] = 0;
      gridTypeCode = p.getInt(index);
      System.out.println("grid type code = "+gridTypeCode);

    } catch (Exception ne) {ne.printStackTrace(); System.exit(1); }

  }

  /** set the RealTypes to use for coordinates (x,y,z,t) and pressure
  *   values
  *
  * @param x X-coordinate
  * @param y Y-coordinate
  * @param level level dimension
  * @param time_type forecast valid time
  * @param pres MathType of vertical coordinate in display
  */

  public void setRealTypes(RealType x, RealType y, RealType level,
    RealType time_type, RealType pres) {
      this.x = x;
      this.y = y;
      this.level = level;
      this.time_type = time_type;
      this.pres = pres;

      // define the VisAD type for (x,y) -> values

      try {

        Variable p = nc.get("Nx");
        if (p == null) p = nc.get("Ni");

        int[] index = new int[1];
        index[0] = 0;
        xval = p.getInt(index);

        p = nc.get("Ny");
        if (p == null) p = nc.get("Nj");
        yval = p.getInt(index);

        System.out.println("x,y dimensions = "+xval+" "+yval);

        RealType[] domain_components = {x,y};

        if (GRIBCoordinateSystem.isGridNumberKnown(gridNumber) ) {

          gridCoord = new GRIBCoordinateSystem(gridNumber);

        } else {

          if (gridTypeCode == 0) {
            int[] inx = new int[1];
            inx[0] = 0;
            p = nc.get("Ni");
            int Ni = p.getInt(inx);
            p = nc.get("Nj");
            int Nj = p.getInt(inx);
            p = nc.get("La1");
            double La1 = p.getDouble(inx);
            p = nc.get("Lo1");
            double Lo1 = p.getDouble(inx);
            p = nc.get("La2");
            double La2 = p.getDouble(inx);
            p = nc.get("Lo2");
            double Lo2 = p.getDouble(inx);
            p = nc.get("Di");
            double Di = p.getDouble(inx);
            p = nc.get("Dj");
            double Dj = p.getDouble(inx);
            gridCoord = new
                 GRIBCoordinateSystem(0,Ni,Nj,La1,Lo1,La2,Lo2,Di,Dj);
          }

        }

        grid_domain = new RealTupleType(domain_components, gridCoord, null);
        dom_set = new Integer2DSet(grid_domain, xval, yval);
        time_set = new Gridded1DDoubleSet(time_type, times, num_records);

      } catch (Exception et) {et.printStackTrace(); }
  }


  /** fetch a list of 4D Variable names, long_names, and units
  * for those parameters that have dimensions (x,y,level,record)
  *
  * @return names[0][k] is name; [1][k] is long_name, [2][k] is unit
  *
  */
  public synchronized Vector get4dVariables() {
    Vector retVec = new Vector();

    VariableIterator vi = nc.iterator();
    int numVars = nc.size();

    for (int k=0; k<numVars; k++) {

      Variable v = vi.next();
      DimensionIterator di = v.getDimensionIterator();
      int numDims = v.getRank();

      if (numDims != 4) continue;

      int[] dims = v.getLengths();

      String dimNames[] = new String[numDims];
      int xDim = -1;
      int yDim = -1;
      int levelDim = -1;
      int recordDim = -1;

      for (int i=0; i<numDims; i++) {
        dimNames[i] = (di.next().getName()).trim();
        if (dimNames[i] == "x" || dimNames[i] == "lon") xDim = i;
        if (dimNames[i] == "y" || dimNames[i] == "lat") yDim = i;
        if (dimNames[i] == "level") levelDim = i;
        if (dimNames[i] == "record") recordDim = i;
      }
      if (xDim != -1 && yDim != -1 && levelDim != -1 && recordDim != -1) {
        retVec.addElement( v.getName() );
        retVec.addElement( v.getAttribute("long_name").getStringValue());
        retVec.addElement( v.getAttribute("units").getStringValue());
      }


    }

    return retVec;
  }

  /** fetch a list of 3D Variable names, long_names, and units
  * for those parameters that have dimensions (x,y,level,record)
  *
  * @return names[0][k] is name; [1][k] is long_name, [2][k] is unit
  *
  */
  public synchronized Vector get3dVariables() {
    Vector retVec = new Vector();

    VariableIterator vi = nc.iterator();
    int numVars = nc.size();

    for (int k=0; k<numVars; k++) {

      Variable v = vi.next();
      DimensionIterator di = v.getDimensionIterator();
      int numDims = v.getRank();

      if (numDims != 3) continue;

      int[] dims = v.getLengths();

      String dimNames[] = new String[numDims];
      int xDim = -1;
      int yDim = -1;
      int recordDim = -1;

      for (int i=0; i<numDims; i++) {
        dimNames[i] = (di.next().getName()).trim();
        if (dimNames[i] == "x" || dimNames[i] == "lon") xDim = i;
        if (dimNames[i] == "y" || dimNames[i] == "lat") yDim = i;
        if (dimNames[i] == "record") recordDim = i;
      }
      if (xDim != -1 && yDim != -1 && recordDim != -1) {
        retVec.addElement( v.getName() );
        retVec.addElement( v.getAttribute("long_name").getStringValue());
        Attribute uni = v.getAttribute("units");
        if (uni != null) {
          retVec.addElement( uni.getStringValue());
        } else {
          retVec.addElement("null");
        }
      }


    }

    return retVec;
  }


  /** fetch a list of Variable names, long_names, and units
  *
  * @return names[0][k] is name; [1][k] is long_name, [2][k] is unit
  *
  */
  public synchronized String[][] getVariableNames() {
    VariableIterator vi = nc.iterator();
    int numVars = nc.size();
    String names[][] = new String[3][numVars];

    for (int i=0; i<numVars; i++) {

      Variable v = vi.next();
      names[0][i] = v.getName();
      names[1][i] = (v.getAttribute("long_name")).getStringValue();
      names[2][i] = (v.getAttribute("units")).getStringValue();
    }

    return names;
  }

  public double getAspect() {
    if (gridCoord != null) {
      return ( ( (GRIBCoordinateSystem)gridCoord).getAspectRatio());
    } else {
      return (1.0);
    }
  }

  public int getNumberOfLevels() {
    return(num_levels);
  }
  public double[] getPressureLevels() {
    return(pressureLevels);
  }

  public int getNumberOfTimes() {
    return(num_records);
  }

  public java.awt.Dimension getDimension() {
    return (new java.awt.Dimension(xval, yval) );
  }

  public Integer2DSet getDomainSet() {
    return dom_set;
  }


  /** fetch the grids for one parameter
  * @param name the name of the parameter
  * @param values the RealType associated with name
  *
  * @return Tuple[2] where Tuple[0] is the vertical coordinate
  *         value and Tuple[1] is the FlatField(s) of data
  *
  */
  public synchronized Tuple[] getGrids(String name, RealType values,
    double[][] range) {

    // find the named variable
    Variable v = nc.get(name);
    if (v == null) {
      System.out.println("could not find "+v+" in netCDF file");
      System.exit(1);
    }

    // get long_name and units attributes
    Attribute long_name = v.getAttribute("long_name");
    Attribute units = v.getAttribute("units");
    Attribute fillvalue = v.getAttribute("_FillValue");
    double fill = fillvalue.getNumericValue().doubleValue();

    // now get the dimensions, and look for "x", "y", "level" and "record"
    // these are the key dimension names

    DimensionIterator di = v.getDimensionIterator();
    int numDims = v.getRank();
    int[] dims = v.getLengths();

    String dimNames[] = new String[numDims];
    int xDim = -1;
    int yDim = -1;
    int levelDim = -1;
    int recordDim = -1;

    // locate the dimensions and record their values...

    for (int i=0; i<numDims; i++) {
      dimNames[i] = (di.next().getName()).trim();
      if (dimNames[i] == "x" || dimNames[i] == "lon") xDim = i;
      if (dimNames[i] == "y" || dimNames[i] == "lat") yDim = i;
      if (dimNames[i] == "level") levelDim = i;
      if (dimNames[i] == "record") recordDim = i;
    }

    // we need to accomodate 'ls_bndy' and the like as well...

    if (xDim == -1 || yDim == -1 ) {
      System.out.println("one or more x-y dimensions missing");
      System.exit(1);
    }

    int index[] = new int[numDims];

    Tuple tup[] = null;

    try {

      FunctionType grid_type = new FunctionType(grid_domain, values);

      // now make a function time record -> (x,y) -> values
      FunctionType withTime_type = new FunctionType(time_type, grid_type);

      // make a Tuple to hold the (level, withTime) values for each level
      tup = new Tuple[num_levels];

      int gridDim = dims[xDim] * dims[yDim];

      double grids[][] = new double[1][gridDim];
      Data griddata[] = new Data[2];

      if (levelDim != -1 && recordDim != -1) {
        for (int p=0; p<num_levels; p++) {
          index[levelDim] = p;
          range[p][0] = Double.MAX_VALUE;
          // WLH 2 May 2000
          // range[p][1] = Double.MIN_VALUE;
          range[p][1] = -Double.MAX_VALUE;

          FieldImpl withTime = new FieldImpl(withTime_type, time_set);

          for (int t=0; t<num_records; t++) {
	    index[recordDim] = timeIndex[t];

            for (int i=0; i<dims[yDim]; i++) {
              index[yDim] = i;
                for (int j=0; j<dims[xDim]; j++) {
                  index[xDim] = j;
                  double value = v.getDouble(index);
                  if (value == fill) {
                    value = Double.NaN;
                  } else {
                    if (value < range[p][0]) range[p][0] = value;
                    if (value > range[p][1]) range[p][1] = value;
                  }

                  grids[0][i*dims[xDim] + j] = value;
                }
            }

            FlatField nffg = new FlatField(grid_type, dom_set);
            nffg.setSamples(grids, false);
            withTime.setSample(t, nffg);
          }

          griddata[0] = new visad.Real(pres, pressureLevels[p] );
          griddata[1] = withTime;
          tup[p] = new Tuple(griddata);
        }

      } else if (recordDim != -1) {
          range[0][0] = Double.MAX_VALUE;
          // WLH 2 May 2000
          // range[0][1] = Double.MIN_VALUE;
          range[0][1] = -Double.MAX_VALUE;

          FieldImpl withTime = new FieldImpl(withTime_type, time_set);

          for (int t=0; t<num_records; t++) {
	    index[recordDim] = timeIndex[t];

            for (int i=0; i<dims[yDim]; i++) {
              index[yDim] = i;
                for (int j=0; j<dims[xDim]; j++) {
                  index[xDim] = j;
                  double value = v.getDouble(index);
                  if (value == fill) {
                    value = Double.NaN;
                  } else {
                    if (value < range[0][0]) range[0][0] = value;
                    if (value > range[0][1]) range[0][1] = value;
                  }
                  grids[0][i*dims[xDim] + j] = value;
                }
            }

            FlatField nffg = new FlatField(grid_type, dom_set);
            nffg.setSamples(grids, false);
            withTime.setSample(t, nffg);
          }

          griddata[0] = new visad.Real(pres, 1020. );
          griddata[1] = withTime;
          tup[0] = new Tuple(griddata);

      } else {
            for (int i=0; i<dims[yDim]; i++) {
              index[yDim] = i;
                for (int j=0; j<dims[xDim]; j++) {
                  index[xDim] = j;
                  double value = v.getDouble(index);
                  if (value == fill) {
                    value = Double.NaN;
                  } else {
                    if (value < range[0][0]) range[0][0] = value;
                    if (value > range[0][1]) range[0][1] = value;
                  }
                  grids[0][i*dims[xDim] + j] = value;
                }
            }

          FlatField nffg = new FlatField(grid_type, dom_set);
          nffg.setSamples(grids, false);

          griddata[0] = new visad.Real(pres, 1020. );
          griddata[1] = nffg;
          tup[0] = new Tuple(griddata);
      }

    } catch (Exception ve) {ve.printStackTrace(); System.exit(1);}


    return tup;

  }

  /** Test routine: java NetcdfGrids <name of netCDF file>
  */

  public static void main(String args[]) {

    try {
    NetcdfGrids ng = new NetcdfGrids("97032712_eta.nc");
    RealType x = RealType.getRealType("x");
    RealType y = RealType.getRealType("y");
    RealType level = RealType.getRealType("level");
    RealType time_type = RealType.Time;
    RealType pres = RealType.getRealType("pres");
    RealType Z = RealType.getRealType("Z");
    int num_levels = ng.getNumberOfLevels();
    double [][] range = new double[num_levels][2];

    ng.setRealTypes(x,y,level,time_type,pres);
    Tuple d[] = ng.getGrids("Z", Z, range);
    System.out.println("range = "+range[0][0]+" - "+range[0][1]);

    } catch (Exception me) {me.printStackTrace(); System.exit(1); }

  }

  /** fetches grids are returns a Tuple [level][record] of FlatFields
  *
  * this was implemented to test the offset between mapping to
  * animator or just doing one at a time...
  *
  */

  public synchronized Tuple[][] getGridsWithTime(String name, RealType values,
  double[][] range) {

    // find the named variable
    Variable v = nc.get(name);
    if (v == null) {
      System.out.println("could not find "+v+" in netCDF file");
      System.exit(1);
    }

    // get long_name and units attributes
    Attribute long_name = v.getAttribute("long_name");
    Attribute units = v.getAttribute("units");
    Attribute fillvalue = v.getAttribute("_FillValue");

    // now get the dimensions, and look for "x", "y", "level" and "record"
    // these are the key dimension names
    // allow "lat" to be used for "y" and "lon" in place of "x"...sad

    DimensionIterator di = v.getDimensionIterator();
    int numDims = v.getRank();
    int[] dims = v.getLengths();

    String dimNames[] = new String[numDims];
    int xDim = -1;
    int yDim = -1;
    int levelDim = -1;
    int recordDim = -1;

    for (int i=0; i<numDims; i++) {
      dimNames[i] = (di.next().getName()).trim();
      if (dimNames[i] == "x") xDim = i;
      if (dimNames[i] == "y") yDim = i;
      if (dimNames[i] == "level") levelDim = i;
      if (dimNames[i] == "record") recordDim = i;
    }

    // we need to accomodate 'ls_bndy' and the like as well...

    if (xDim == -1 || yDim == -1 ) {
      System.out.println("one or more x-y dimensions missing");
      System.exit(1);
    }

    int index[] = new int[numDims];

    Tuple tup[][] = null;

    try {

      // define the VisAD type for (x,y) -> values

      RealType[] domain_components = {x,y};

      gridCoord = new GRIBCoordinateSystem(gridNumber);

      RealTupleType grid_domain =
          new RealTupleType(domain_components, gridCoord, null);

      dom_set = new Integer2DSet(grid_domain,
                                      dims[xDim], dims[yDim]);

      FunctionType grid_type = new FunctionType(grid_domain, values);

      // make a Tuple to hold the (level, withTime) values for each level

      tup = new Tuple[num_levels][num_records];

      int gridDim = dims[xDim] * dims[yDim];

      double grids[][] = new double[1][gridDim];
      Data griddata[] = new Data[2];

      if (levelDim != -1 && recordDim != -1) {
        for (int p=0; p<num_levels; p++) {
          index[levelDim] = p;

          for (int t=0; t<num_records; t++) {
	    index[recordDim] = timeIndex[t];

            for (int i=0; i<dims[yDim]; i++) {
              index[yDim] = i;
                for (int j=0; j<dims[xDim]; j++) {
                  index[xDim] = j;
                  grids[0][i*dims[xDim] + j] = v.getDouble(index);
                }
            }

            FlatField nffg = new FlatField(grid_type, dom_set);
            nffg.setSamples(grids, false);

            griddata[0] = new visad.Real(pres, pressureLevels[p] );
            griddata[1] = nffg;
            tup[p][t] = new Tuple(griddata);
          }
        }

      } else if (recordDim != -1) {

          for (int t=0; t<num_records; t++) {
	    index[recordDim] = timeIndex[t];

            for (int i=0; i<dims[yDim]; i++) {
              index[yDim] = i;
                for (int j=0; j<dims[xDim]; j++) {
                  index[xDim] = j;
                  grids[0][i*dims[xDim] + j] = v.getDouble(index);
                  //grids[0][j*dims[yDim] + i] = v.getDouble(index);
                }
            }

            FlatField nffg = new FlatField(grid_type, dom_set);
            nffg.setSamples(grids, false);

            griddata[0] = new visad.Real(pres, 1020. );
            griddata[1] = nffg;
            tup[0][t] = new Tuple(griddata);
          }

      } else {
            for (int i=0; i<dims[yDim]; i++) {
              index[yDim] = i;
                for (int j=0; j<dims[xDim]; j++) {
                  index[xDim] = j;
                  grids[0][i*dims[xDim] + j] = v.getDouble(index);
                  //grids[0][j*dims[yDim] + i] = v.getDouble(index);
                }
            }

          FlatField nffg = new FlatField(grid_type, dom_set);
          nffg.setSamples(grids, false);

          griddata[0] = new visad.Real(pres, 1020. );
          griddata[1] = nffg;

          for (int t=0; t<num_records; t++) {
            tup[0][t] = new Tuple(griddata);
          }
      }

    } catch (Exception ve) {ve.printStackTrace(); System.exit(1);}

    return tup;

  }


}
