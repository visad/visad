package visad.data.hdfeos;

import java.util.*; 
import visad.*;
import visad.data.*;
import java.rmi.*;
import java.net.URL;

public class hdfeosDefault extends hdfeos {


  public hdfeosDefault() {

     super("Default");
  }
     
  public DataImpl open( String file_path ) {

    DataImpl  data = null;

    try {

      hdfeosFileDefault file = new hdfeosFileDefault( file_path );

      data = file.getDataObject();
    }
    finally {

    }

    return data;

  }

  public DataImpl open( URL url ) {

    return null;
  }

  public void add( String id, DataImpl data, boolean replace ) throws
     BadFormException {

  }

  public void save( String id, DataImpl data, boolean replace ) throws
     BadFormException, RemoteException {

  }

  public FormNode getForms( Data data ) {

    return this;
  }

}
