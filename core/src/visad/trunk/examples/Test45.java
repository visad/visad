import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test45
	extends TestSkeleton
{
  public Test45() { }

  public Test45(String args[])
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

    String[] names = new String[] {"aaa", "bbbb", "ccccc", "defghi"};
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

    DisplayImpl display1 = new DisplayImplJ3D("display1");

    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(RealType.Latitude, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));
    display1.addMap(new ScalarMap(text, Display.Text));

    DataReferenceImpl ref_text_field = 
      new DataReferenceImpl("ref_text_field");
    ref_text_field.setData(text_field);
    display1.addReference(ref_text_field, null);

    JFrame jframe = new JFrame("text in Java3D");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    jframe.setContentPane((JPanel) display1.getComponent());
    jframe.pack();
    jframe.setVisible(true);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString()
  {
    return ": text in Java3D";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test45 t = new Test45(args);
  }
}
