package visad.data.hdfeos;

import java.io.IOException;
import visad.data.BadFormException;
import visad.data.Form;
import visad.DataImpl;
import visad.VisADException;

public abstract class hdfeos extends Form {


  public hdfeos( String name ) {

    super( name );
  }


  public abstract DataImpl open( String file_path ) 
     throws BadFormException, IOException, VisADException;





}
