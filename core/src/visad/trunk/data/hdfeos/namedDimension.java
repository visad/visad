package visad.data.hdfeos;


  public class namedDimension  
  {

    private String  name;
    private int  length;
    private geoMap g_map;
    private boolean unLimitFlag = false;

    namedDimension( int struct_id, String name, int length, geoMap g_map ) 
    {

      this.name = name;
      if ( length == 0 ) {
        unLimitFlag = true;
      }
      this.length = length;
      this.g_map = g_map;
    }

    public String getName()  {

      return this.name;
    }

    public boolean equals( namedDimension obj ) 
    {

      if( this.name.equals( obj.getName() )) {

         return true;
      }
      else {

         return false;
      }
    }

    public void setLength( int len ) {

      length = len;
      return;
    }

    public int getLength()
    {
      return length;
    }

    public geoMap getGeoMap() {

      return g_map;
   }

   public boolean isGeoMapDefined() {

     if ( g_map == null ) {
       return false;
     }
     else {
       return true;
     }

   }

   public boolean isUnlimited() {

     return this.unLimitFlag;
   }

    public String toString() 
    {

       String str = "dimension: "+name+"\n"+
                    "   length: "+length+"\n";
       return str;
    }


  }
