package visad.data.hdfeos;


  public class geoMap {

    String  toDim;
    String  fromDim;
    int  offset;
    int  increment;

    geoMap( String toDim, String fromDim, int offset, int increment ) {
      this.toDim = toDim;
      this.fromDim = fromDim;
      this.offset = offset;
      this.increment = increment;
    } 

  }
