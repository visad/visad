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
	throws BadFormException, IOException, VisADException
  {
    FitsAdaptor obj = new FitsAdaptor(path);

    Data[] data = obj.getData();

    if (data == null) {
      return null;
    }

    if (data.length == 1) {
      return (DataImpl )data[0];
    }

    return (DataImpl )new Tuple(data);
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
