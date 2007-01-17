/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.hdfeos.hdfeosc;

public class HdfeosLib
{
   static
   {
     System.loadLibrary("hdfeos");
   }

   public final static int DFACC_READ = 1;
   public final static int G_MAPS = 1;
   public final static int D_FIELDS = 4;
   public final static int G_FIELDS = 3;
   public final static int N_DIMS = 0;
   public final static String G_TYPE = "Geolocation Fields";
   public final static String D_TYPE = "Data Fields";
   public final static int HDFE_mode = 4;
   public final static int BYTE = 20;
   public final static int U_BYTE = 21;
   public final static int SHORT = 22;
   public final static int U_SHORT = 23;
   public final static int DOUBLE = 6;
   public final static int INT = 24;
   public final static int FLOAT = 5;

   public native static int EHclose( int file_id );

   public native static int EHchkfid( int file_id, String name, int[] HDFfid, int[] sd_id, byte[] acc );

   public native static int EHgetcal( int sd_id, int sds_idx, double[] cal, double[] cal_err, double[] off, double[] off_err, int[] type );

   public native static int GetNumericAttr( int sd_id, String sds_name, String attr_name, double[] value);

   public native static int SDattrinfo( int sd_id, String sds_name, String attr_name );

   public native static int GDinqattrs( int grid_id, String[] attr_list );

   public native static int GDprojinfo( int grid_id, int[] proj, int[] zone, int[] sphr, double[] parm );

   public native static int GDgridinfo( int grid_id, int[] xsiz, int[] ysiz, double[] uprL, double[] lwrR );

   public native static int SWinqswath( String filename, String[] name_list );

   public native static int GDinqgrid( String filename, String[] name_list );

   public native static int SWinqdims( int swath_id, int size, String[] dimList, int[] lengths );

   public native static int GDinqdims( int grid_id, int size, String[] dimList, int[] lengths );

   public native static int SWopen( String filename, int access );

   public native static int GDopen( String filename, int access );

   public native static int SWattach( int file_id, String swath_name );

   public native static int GDattach( int file_id, String grid_name );

   public native static int SWinqdatafields( int swath_id, int size, String[] list, int[] ranks, int[] types );

   public native static int SWinqgeofields( int swath_id, int size, String[] list, int[] ranks, int[] types );

   public native static int GDinqfields( int grid_id, int size, String[] list, int[] ranks, int[] types );

   public native static int SWinqmaps( int swath_id, int size, String[] maps, int[] offsets, int[] increments );

   public native static int SWnentries( int swath_id, int HDFE_mode, int[] strSize );

   public native static int GDnentries( int grid_id, int HDFE_mode, int[] strSize );

   public native static int SWfieldinfo( int swath_id, String name, String[] list, int[] rank, int[] length, int[] type );

   public native static int GDfieldinfo( int grid_id, String name, String[] list, int[] rank, int[] length, int[] type );

   public native static int SWfdims( int swath_id, String type, String name, int[] strSize );

   public native static int GDfdims( int swath_id, String name, int[] strSize );

   public native static int SWreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, float[] data );

   public native static int SWreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, double[] data );

   public native static int SWreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, int[] data );

   public native static int SWreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, short[] data );

   public native static int SWreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, byte[] data );

   public native static int GDreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, float[] data );

   public native static int GDreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, double[] data );

   public native static int GDreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, int[] data );

   public native static int GDreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, short[] data );

   public native static int GDreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, byte[] data );

}
