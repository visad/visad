import java.awt.Component;

import java.rmi.RemoteException;

import visad.*;

import visad.data.gif.GIFForm;

import visad.java2d.DisplayImplJ2D;

public class Test09
	extends UISkeleton
{
  private String fileName = null;

  public Test09() { }

  public Test09(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  int checkExtraKeyword(int argc, String args[])
  {
    if (fileName == null) {
      fileName = args[argc];
    } else {
      System.err.println("Ignoring extra filename \"" + args[argc] + "\"");
    }

    return 1;
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    if (fileName == null) {
      System.err.println("must specify GIF or JPEG file name");
      System.exit(1);
      return null;
    }

    GIFForm gif_form = new GIFForm();
    FlatField imaget1 = (FlatField) gif_form.open(fileName);

    DisplayImpl display1 = new DisplayImplJ2D("display1");

    // compute ScalarMaps from type components
    FunctionType ftype = (FunctionType) imaget1.getType();
    RealTupleType dtype = ftype.getDomain();
    RealTupleType rtype9 = (RealTupleType) ftype.getRange();
    display1.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                  Display.XAxis));
    display1.addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                  Display.YAxis));
    display1.addMap(new ScalarMap((RealType) rtype9.getComponent(0),
                                   Display.Red));
    display1.addMap(new ScalarMap((RealType) rtype9.getComponent(1),
                                   Display.Green));
    display1.addMap(new ScalarMap((RealType) rtype9.getComponent(2),
                                   Display.Blue));

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "GIF / JPEG in Java2D"; }

  public String toString()
  {
    return " file_name: GIF / JPEG reader using Java2D";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test09 t = new Test09(args);
  }
}
