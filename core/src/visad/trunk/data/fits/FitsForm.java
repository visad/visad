package visad.data.fits;

import java.io.IOException;

import java.net.URL;

import java.rmi.RemoteException;

import visad.Data;
import visad.DataImpl;
import visad.Tuple;
import visad.UnimplementedException;
import visad.VisADException;

import visad.data.Form;
import visad.data.FormNode;
import visad.data.BadFormException;

public class FitsForm
	extends Form
{
  public FitsForm()
  {
    super("FitsForm");
  }

  public void save(String id, Data data, boolean replace)
	throws  BadFormException, IOException, RemoteException, VisADException
  {
    throw new UnimplementedException("Can't yet save FITS objects");
  }

  public void add(String id, Data data, boolean replace)
	throws BadFormException
  {
    throw new RuntimeException("Can't yet add FITS objects");
  }

  public DataImpl open(String path)
	throws BadFormException, RemoteException, VisADException
  {
    FitsAdapter fits = new FitsAdapter(path);

    // save any exceptions
    ExceptionStack eStack = null;

    // convert the FITS object to a VisAD data object
    Data[] data;
    try {
      data = fits.getData();
    } catch (ExceptionStack e) {
      eStack = e;
      fits.clearExceptionStack();
      data = fits.getData();
    }

    // throw away FitsAdapter object so we can reuse that memory
    fits = null;

    // if there's no data, we're done
    if (data == null || data.length == 0) {
      if (eStack != null) {
	throw eStack;
      }
      return null;
    }

    // either grab solo Data object or wrap a Tuple around all the Data objects
    DataImpl di;
    if (data.length == 1) {
      di = (DataImpl )data[0];
    } else {
      di = new Tuple(data);
    }

    // throw away Data array so we can reuse (a small bit of) that memory
    data = null;

    return di;
  }

  public DataImpl open(URL url)
	throws BadFormException, VisADException, IOException
  {
    throw new UnimplementedException("Can't yet open FITS URLs");
  }

  public FormNode getForms(Data data)
  {
    throw new RuntimeException("Can't yet get FITS forms");
  }
}
