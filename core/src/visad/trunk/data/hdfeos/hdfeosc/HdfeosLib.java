package visad.data.hdfeos.hdfeosc;

public class HdfeosLib  {

   static {

     System.loadLibrary("hdfeos");
   }

/*
   public HdfeosLib()
   {
     System.loadLibrary("hdfeos");
   }
*/
     
   public native int EHclose( int file_id );

   public native int EHchkfid( int file_id, String name, int[] HDFfid, int[] sd_id, byte[] acc );

   public native int GDinqattrs( int grid_id, String[] attr_list );

   public native int GDprojinfo( int grid_id, int[] proj, int[] zone, int[] sphr, double[] parm );

   public native int GDgridinfo( int grid_id, int[] xsiz, int[] ysiz, double[] uprL, double[] lwrR );

   public native int SWinqswath( String filename, String[] name_list );

   public native int GDinqgrid( String filename, String[] name_list );

   public native int SWinqdims( int swath_id, int size, String[] dimList, int[] lengths );

   public native int GDinqdims( int grid_id, int size, String[] dimList, int[] lengths );

   public native int SWopen( String filename, int access );

   public native int GDopen( String filename, int access );

   public native int SWattach( int file_id, String swath_name );

   public native int GDattach( int file_id, String grid_name );

   public native int SWinqdatafields( int swath_id, int size, String[] list, int[] ranks, int[] types );

   public native int SWinqgeofields( int swath_id, int size, String[] list, int[] ranks, int[] types );

   public native int GDinqfields( int grid_id, int size, String[] list, int[] ranks, int[] types );

   public native int SWinqmaps( int swath_id, int size, String[] maps, int[] offsets, int[] increments );

   public native int SWnentries( int swath_id, int HDFE_mode, int[] strSize );

   public native int GDnentries( int grid_id, int HDFE_mode, int[] strSize );

   public native int SWfieldinfo( int swath_id, String name, int size, String[] list, int[] rank, int[] length, int[] type );

   public native int GDfieldinfo( int grid_id, String name, int size, String[] list, int[] rank, int[] length, int[] type );

   public native int SWfdims( int swath_id, String type, String name, int[] strSize );

   public native int GDfdims( int swath_id, String name, int[] strSize );

   public native int SWreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, float[] data );

   public native int SWreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, double[] data );

   public native int SWreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, int[] data );

   public native int SWreadfield( int swath_id, String name, int[] start, int[] stride, int[] edge, short[] data );
}
