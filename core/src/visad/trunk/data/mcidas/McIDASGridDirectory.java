
//
// McIDASGridDirectory.java
//

/*
The code in this file is Copyright(C) 1999 by Don
Murray.  It is designed to be used with the VisAD system for
interactive analysis and visualization of numerical data.

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

import java.util.*;
import java.lang.*;
import edu.wisc.ssec.mcidas.*;
import visad.*;
import visad.data.units.Parser;
import visad.data.units.*;
import visad.data.mcidas.*;
import visad.jmet.*;


/**
 * McIDASGridDirectory for McIDAS 'grid' directory entries
 *
 * @author Tom Whittaker
 *
 */
public class McIDASGridDirectory extends visad.jmet.MetGridDirectory {
  int validHour;
  double paramScale, levelScale;
  int gridType;
  int [] navBlock;
  MetUnits mu;


  public McIDASGridDirectory(byte[] h) {
     coordSystem = null;
     paramName = new String(h,24,4);
     rows = McIDASUtil.bytesToInteger(h,4);
     columns = McIDASUtil.bytesToInteger(h,8);
     levels = 1;
     int refDay = McIDASUtil.bytesToInteger(h,12);
     int refHMS = McIDASUtil.bytesToInteger(h,16);

     validHour = McIDASUtil.bytesToInteger(h,20);
     referenceTime = new Date(McIDASUtil.
                      mcDayTimeToSecs(refDay, refHMS) * 1000);

     validTime = new Date( (McIDASUtil.mcDayTimeToSecs(refDay,refHMS)+
            (validHour * 3600)) * 1000 );

     levelValue = (double) McIDASUtil.bytesToInteger(h,36);

     paramScale = Math.pow(10., McIDASUtil.bytesToInteger(h,28));
     levelScale = Math.pow(10., McIDASUtil.bytesToInteger(h,40));
     levelValue = levelValue * levelScale;
     gridType = McIDASUtil.bytesToInteger(h,132);
     //System.out.println("Grid type = "+gridType);
     navBlock = new int[8];
     for (int n=0; n<7; n++) {
       navBlock[n] = McIDASUtil.bytesToInteger(h, (132 + (4*n)) );
       //System.out.println("nav word "+n+" = "+navBlock[n]);
     }
     try {
       mu = new MetUnits();
       String su = new String(h,32,4);
       String sl =new String(h,44,4);
       //System.out.println("param and level units incoming = "+su+" & "+sl);
       //System.out.println("param and level units converted = "+mu.makeSymbol(su)+" & "+mu.makeSymbol(sl));
       try {
         paramUnit = Parser.parse(mu.makeSymbol(su));
       } catch (ParseException pe) {
         paramUnit = null;
       }
       try {
         levelUnit = Parser.parse(mu.makeSymbol(sl));
       } catch (ParseException pe) {
         levelUnit = null;
       }
     } catch (VisADException e) {System.out.println(e);}

     // Special case for character levels
     if (Math.abs(levelValue) > 10000 && levelUnit == null)
     {
       levelValue = 999;
     }
     int paramType = McIDASUtil.bytesToInteger(h,48);
     if (paramType == 4 || paramType == 8) // level difference or average
     {
       secondLevelValue = 
         (double) McIDASUtil.bytesToInteger(h,56) * levelScale;
     }
     if (paramType == 1 || paramType == 2) // time difference or average
     {
       secondTime =
         new Date( (McIDASUtil.mcDayTimeToSecs(refDay,refHMS)+
           ((McIDASUtil.bytesToInteger(h,52)/10000) * 3600)) * 1000 );
             // NB: second time is 10000*time in hours to make HHMMSS
             // so we need to divide it out
     }
   }

   public int[] getNavBlock() {
     return navBlock;
   }

   public double getParamScale() {
     return paramScale;
   }

   public int getGridType() {
     return gridType;
   }

   public CoordinateSystem getCoordinateSystem() {
     if (coordSystem == null) {
       try {

       RealTupleType ref = new RealTupleType(RealType.Latitude,
             RealType.Longitude);
       if (gridType == 1 || gridType == 4) {
         double la1 = ( (double) navBlock[3])/10000.;
         double lo1 = - ( (double) navBlock[2])/10000.;
         double la2 = ( (double) navBlock[1])/10000.;
         double lo2 = - ( (double) navBlock[4])/10000.;
         double dj = ( (double) navBlock[5])/10000.;
         double di = dj;
         if (gridType == 4) di = ( (double) navBlock[6])/10000.;
         //System.out.println("lat/lon = "+la1+"  "+lo1+"  incs = "+di+"  "+dj);
         coordSystem = new GRIBCoordinateSystem(ref,0,columns,rows,
              la1, lo1, la2, lo2, di, dj);
       } else {
         coordSystem = new GRIBCoordinateSystem(ref,0);
       }
       } catch (Exception ev) {;}
     }
     return coordSystem;
   }


   public String toString() {
     return new String(paramName + " "+paramUnit+" "+rows+" "+
     columns+" "+
     levelValue+" "+levelUnit+" "+
     referenceTime.toGMTString()+ " "+validHour
     + " or "+validTime.toGMTString() );
   }

}

