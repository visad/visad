import java.awt.Component;

import java.rmi.RemoteException;

import visad.*;

import visad.data.fits.FitsForm;

import visad.java3d.TwoDDisplayRendererJ3D;
import visad.java3d.DisplayImplJ3D;

public class Test32
	extends TestSkeleton
{
  private String fileName = null;

  public Test32() { }

  public Test32(String args[])
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
      System.err.println("Must specify FITS file name");
      return null;
    }

    FitsForm fits = new FitsForm();
    FlatField fits_data = (FlatField) fits.open(fileName);
    // System.out.println("fits_data type = " + fits_data.getType());

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    // display1 = new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D(),
    //                               DisplayImplJ3D.APPLETFRAME);
    // compute ScalarMaps from type components
    FunctionType ftype = (FunctionType) fits_data.getType();
    RealTupleType dtype = ftype.getDomain();
    MathType rntype = ftype.getRange();
    int n = dtype.getDimension();
    display1.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                  Display.XAxis));
    if (n > 1) {
      display1.addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                    Display.YAxis));
    }
    if (n > 2) {
      display1.addMap(new ScalarMap((RealType) dtype.getComponent(2),
                                    Display.ZAxis));
    }
    if (rntype instanceof RealType) {
      display1.addMap(new ScalarMap((RealType) rntype, Display.Green));
    }
    else if (rntype instanceof RealTupleType) {
      int m = ((RealTupleType) rntype).getDimension();
      RealType rr = (RealType) ((RealTupleType) rntype).getComponent(0);
      display1.addMap(new ScalarMap(rr, Display.Green));
      if (n <= 2) {
        if (m > 1) {
          rr = (RealType) ((RealTupleType) rntype).getComponent(1);
        }
        display1.addMap(new ScalarMap(rr, Display.ZAxis));
      }
    }
    display1.addMap(new ConstantMap(0.5, Display.Red));
    display1.addMap(new ConstantMap(0.0, Display.Blue));

    DataReferenceImpl ref_fits = new DataReferenceImpl("ref_fits");
    ref_fits.setData(fits_data);
    display1.addReference(ref_fits, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString() { return " file_name: FITS adapter"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test32 t = new Test32(args);
  }
}
