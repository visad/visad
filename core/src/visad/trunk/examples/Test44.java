import java.awt.Component;

import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;

public class Test44
	extends UISkeleton
{
  public Test44() { }

  public Test44(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    TextType text = new TextType("text");
    RealType[] time = {RealType.Time};
    RealTupleType time_type = new RealTupleType(time);
    MathType[] mtypes = {RealType.Latitude, RealType.Longitude, text};
    TupleType text_tuple = new TupleType(mtypes);
    FunctionType text_function = new FunctionType(RealType.Time, text_tuple);

    String[] names = {"aaa", "bbbb", "ccccc", "defghi"};
    int ntimes1 = names.length;
    Set time_set = 
      new Linear1DSet(time_type, 0.0, (double) (ntimes1 - 1.0), ntimes1);

    FieldImpl text_field = new FieldImpl(text_function, time_set);

    for (int i=0; i<ntimes1; i++) {
      Data[] td = {new Real(RealType.Latitude, (double) i),
                   new Real(RealType.Longitude, (double) (ntimes1 - i)),
                   new Text(text, names[i])};

      Tuple tt = new Tuple(text_tuple, td);
      text_field.setSample(i, tt);
    }

    DisplayImpl display1 = new DisplayImplJ2D("display1");

    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(RealType.Latitude, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap text_map = new ScalarMap(text, Display.Text);
    display1.addMap(text_map);

    DataReferenceImpl ref_text_field =
      new DataReferenceImpl("ref_text_field");
    ref_text_field.setData(text_field);
    display1.addReference(ref_text_field, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "text in Java2D"; }

  Component getSpecialComponent(DisplayImpl[] dpys)
	throws VisADException, RemoteException
  {
    ScalarMap text_map = (ScalarMap )dpys[0].getMapVector().lastElement();

    TextControl text_control = (TextControl) text_map.getControl();
    text_control.setSize(0.75);
    text_control.setCenter(true);

    return null;
  }

  public String toString() { return ": text in Java2D"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test44 t = new Test44(args);
  }
}
