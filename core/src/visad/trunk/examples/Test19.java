import java.awt.Component;

import java.rmi.RemoteException;

import visad.*;

import visad.util.VisADSlider;
import visad.java3d.DisplayImplJ3D;

public class Test19
	extends UISkeleton
{
  public Test19() { }

  public Test19(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);
    RealType[] types4 = {ir_radiance, vis_radiance};
    RealTupleType ecnaidar = new RealTupleType(types4);
    FunctionType image_bumble = new FunctionType(earth_location, ecnaidar);
    RealType[] time = {RealType.Time};
    RealTupleType time_type = new RealTupleType(time);
    FunctionType time_images = new FunctionType(time_type, image_tuple);
    FunctionType time_bee = new FunctionType(time_type, image_bumble);

    int size = 64;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);
    FlatField wasp = FlatField.makeField(image_bumble, size, false);

    int ntimes1 = 4;
    int ntimes2 = 6;
    // different time resolutions for test
    Set time_set = 
      new Linear1DSet(time_type, 0.0, 1.0, ntimes1);
    Set time_hornet = 
      new Linear1DSet(time_type, 0.0, 1.0, ntimes2);

    FieldImpl image_sequence = new FieldImpl(time_images, time_set);
    FieldImpl image_stinger = new FieldImpl(time_bee, time_hornet);
    FlatField temp = imaget1;
    FlatField tempw = wasp;
    Real[] reals19 = {new Real(vis_radiance, (float) size / 4.0f),
                      new Real(ir_radiance, (float) size / 8.0f)};
    RealTuple val = new RealTuple(reals19);
    for (int i=0; i<ntimes1; i++) {
      image_sequence.setSample(i, temp);
      temp = (FlatField) temp.add(val);
    }
    for (int i=0; i<ntimes2; i++) {
      image_stinger.setSample(i, tempw);
      tempw = (FlatField) tempw.add(val);
    }
    FieldImpl[] images19 = {image_sequence, image_stinger};
    Tuple big_tuple = new Tuple(images19);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap map1value = new ScalarMap(RealType.Time, Display.SelectValue);
    display1.addMap(map1value);

    DataReferenceImpl ref_big_tuple;
    ref_big_tuple = new DataReferenceImpl("ref_big_tuple");
    ref_big_tuple.setData(big_tuple);
    display1.addReference(ref_big_tuple, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "VisAD select slider"; }

  Component getSpecialComponent(DisplayImpl[] dpys)
	throws VisADException, RemoteException
  {
    ScalarMap map1value = (ScalarMap )dpys[0].getMapVector().lastElement();

    final ValueControl value1control =
      (ValueControl) map1value.getControl();
    value1control.setValue(0.0);

    final DataReference value_ref = new DataReferenceImpl("value");

    VisADSlider slider =
      new VisADSlider("value", 0, 100, 0, 0.01, value_ref, RealType.Generic);

    CellImpl cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        value1control.setValue(((Real) value_ref.getData()).getValue());
      }
    };
    cell.addReference(value_ref);

    return slider;
  }


  public String toString() { return ": SelectValue"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test19 t = new Test19(args);
  }
}
