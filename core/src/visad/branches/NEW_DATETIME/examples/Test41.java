import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;

public class Test41
	extends UISkeleton
{
  public Test41() { }

  public Test41(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType[] types = {RealType.Latitude, RealType.Longitude};

    // construct types
    int isize = 16;
    RealType dom0 = new RealType("dom0");
    RealType dom1 = new RealType("dom1");
    RealType ran = new RealType("ran");
    RealTupleType dom = new RealTupleType(dom0, dom1);
    FunctionType ftype = new FunctionType(dom, ran);
    FlatField imaget1;
    imaget1 = new FlatField(ftype, new Integer2DSet(isize, isize));
    double[][] vals = new double[1][isize * isize];
    for (int i=0; i<isize; i++) {
      for (int j=0; j<isize; j++) {
        vals[0][j + isize * i] = (i + 1) * (j + 1);
      }
    }
    imaget1.setSamples(vals, false);

    RealType oogle = new RealType("oogle");
    FunctionType ftype2 = new FunctionType(dom, oogle);
    FlatField imaget2 = new FlatField(ftype2, imaget1.getDomainSet());
    imaget2.setSamples(vals, false);

    DisplayImpl display1 = new DisplayImplJ2D("display1");
    display1.addMap(new ScalarMap(dom0, Display.XAxis));
    display1.addMap(new ScalarMap(dom1, Display.YAxis));
    display1.addMap(new ScalarMap(ran, Display.Green));
    display1.addMap(new ConstantMap(0.3, Display.Blue));
    display1.addMap(new ConstantMap(0.3, Display.Red));
    display1.addMap(new ScalarMap(oogle, Display.IsoContour));

    GraphicsModeControl mode = display1.getGraphicsModeControl();
    mode.setTextureEnable(false);

    ConstantMap[] omaps1 = {new ConstantMap(1.0, Display.Blue),
                            new ConstantMap(1.0, Display.Red),
                            new ConstantMap(0.0, Display.Green)};

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DataReferenceImpl ref_imaget2 = new DataReferenceImpl("ref_imaget2");
    ref_imaget2.setData(imaget2);
    display1.addReference(ref_imaget2, omaps1);

    DisplayImpl display2 = new DisplayImplJ2D("display2");
    display2.addMap(new ScalarMap(dom0, Display.XAxis));
    display2.addMap(new ScalarMap(dom1, Display.YAxis));
    display2.addMap(new ScalarMap(ran, Display.Green));
    display2.addMap(new ConstantMap(0.3, Display.Blue));
    display2.addMap(new ConstantMap(0.3, Display.Red));
    display2.addMap(new ScalarMap(oogle, Display.IsoContour));

    ConstantMap[] omaps2 = {new ConstantMap(1.0, Display.Blue),
                            new ConstantMap(1.0, Display.Red),
                            new ConstantMap(0.0, Display.Green)};

    display2.addReference(ref_imaget1, null);
    display2.addReference(ref_imaget2, omaps2);

    DisplayImpl[] dpys = new DisplayImpl[2];
    dpys[0] = display1;
    dpys[1] = display2;

    return dpys;
  }

  String getFrameTitle() { return "image / contour alignment in Java2D"; }

  public String toString() { return ": image / contour alignment in Java2D"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test41 t = new Test41(args);
  }
}
