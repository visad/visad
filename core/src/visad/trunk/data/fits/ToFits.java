package visad.data.fits;

import java.io.IOException;

import java.rmi.RemoteException;

import java.net.URL;

import visad.Data;
import visad.VisADException;

import visad.data.DefaultFamily;
import visad.data.DataNode;

public class ToFits
{
  public static void main(String args[])
	throws VisADException, RemoteException, IOException
  {
    DefaultFamily dflt = new DefaultFamily("default");

    if (args.length == 0) {
      args = new String[1];
      args[0] = "testdata/sseclogo.fits";
    }

    for (int i = 0; i < args.length; i++) {
      Data data = dflt.open(args[i]);

      try {
	System.out.println("ToFits " + args[i] + ": " + data.getType());
      } catch (Exception e) {
	System.err.println(args[i] + " print threw " + e.getMessage());
	e.printStackTrace(System.err);
	data = null;
	continue;
      }

      String name = "foo" + i;
      FitsForm form = new FitsForm();
      form.save(name, data, true);
      System.out.println("Wrote " + name);
    }

    System.exit(0);
  }
}
