import java.awt.Component;

import java.rmi.Naming;
import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DirectManipulationRendererJ2D;
import visad.java2d.DisplayImplJ2D;

public class Test56
	extends UISkeleton
{
  private String domain = null;

  boolean hasClientServerMode() { return false; }

  public Test56() { }

  public Test56(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  int checkExtraKeyword(int argc, String args[])
  {
    if (domain == null) {
      domain = args[argc];
    } else {
      System.err.println("Ignoring extra domain \"" + args[argc] + "\"");
    }

    return 1;
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    DisplayImpl[] dpys;
    try {

      System.out.println("RemoteClientTestImpl.main: begin remote activity");
      System.out.println("  to " + domain);

      if (domain == null) {
        domain = "//:/RemoteServerTest";
      }
      else {
        domain = "//" + domain + "/RemoteServerTest";
      }
      RemoteServer remote_obj = (RemoteServer) Naming.lookup(domain);

      System.out.println("connected");

      RemoteDataReference histogram_ref = remote_obj.getDataReference(0);
      RemoteDataReference direct_ref = remote_obj.getDataReference(1);
      RemoteDataReference direct_tuple_ref = remote_obj.getDataReference(2);

      RealTupleType dtype;
      dtype = (RealTupleType) direct_tuple_ref.getData().getType();

      DisplayImpl display1;
      display1 = new DisplayImplJ2D("display");
      display1.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                    Display.XAxis));
      display1.addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                    Display.YAxis));

      GraphicsModeControl mode = display1.getGraphicsModeControl();
      mode.setPointSize(5.0f);
      mode.setPointMode(false);

      RemoteDisplayImpl remote_display1 = new RemoteDisplayImpl(display1);
      DataReference[] refs151 = {histogram_ref};
      remote_display1.addReferences(new DirectManipulationRendererJ2D(),
                                    refs151, null);

      DataReference[] refs152 = {direct_ref};
      remote_display1.addReferences(new DirectManipulationRendererJ2D(),
                                    refs152, null);

      DataReference[] refs153 = {direct_tuple_ref};
      remote_display1.addReferences(new DirectManipulationRendererJ2D(),
                                    refs153, null);

      dpys = new DisplayImpl[1];
      dpys[0] = display1;
    }
    catch (Exception e) {
      System.out.println("collaboration client exception: " + e.getMessage());
      e.printStackTrace(System.out);
      dpys = null;
    }

    return dpys;
  }

  public String toString()
  {
    return " ip.name: collaborative direct manipulation client" +
		"\n\tsecond parameter is server IP name";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test56 t = new Test56(args);
  }
}
