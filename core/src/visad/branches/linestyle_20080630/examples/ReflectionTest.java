//
// ReflectionTest.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

// we only need ReflectedUniverse and VisADException
import visad.util.ReflectedUniverse;
import visad.VisADException;

// GUI classes--you could modify the example to reflect these also, if desired
import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;

/**
 * This example demonstrates the power and flexibility of
 * visad.util.ReflectedUniverse for coding applications with reflection.
 * The only VisAD classes needed to compile this source file are
 * visad.util.ReflectedUniverse and visad.VisADException.
 */
public class ReflectionTest {

  public static void main(String[] args) throws VisADException {
    // create reflected universe
    ReflectedUniverse r = new ReflectedUniverse();

    // import needed classes
    r.exec("import visad.ConstantMap");
    r.exec("import visad.DataReferenceImpl");
    r.exec("import visad.Display");
    r.exec("import visad.FlatField");
    r.exec("import visad.FunctionType");
    r.exec("import visad.RealType");
    r.exec("import visad.RealTupleType");
    r.exec("import visad.ScalarMap");
    r.exec("import visad.java3d.DisplayImplJ3D");
    r.exec("import visad.util.Util");

    // import some constants into reflected universe
    r.setVar("false", false);
    r.setVar("one_half", 0.5);
    r.setVar("size", 64);

    // construct data object
    r.setVar("ir_radiance_name", "ir_radiance");
    r.exec("ir_radiance = RealType.getRealType(ir_radiance_name)");
    r.setVar("count_name", "count");
    r.exec("count = RealType.getRealType(count_name)");
    r.exec("ir_histogram = new FunctionType(ir_radiance, count)");
    r.exec("histogram1 = FlatField.makeField(ir_histogram, size, false)");
    r.setVar("vis_radiance_name", "vis_radiance");
    r.exec("vis_radiance = RealType.getRealType(vis_radiance_name)");
    r.exec("earth_location = new RealTupleType(" +
      "RealType.Latitude, RealType.Longitude)");
    r.exec("radiance = new RealTupleType(vis_radiance, ir_radiance)");
    r.exec("image_tuple = new FunctionType(earth_location, radiance)");
    r.exec("imaget1 = FlatField.makeField(image_tuple, size, false)");

    // construct display and mappings
    r.setVar("display_name", "display");
    r.exec("d = new DisplayImplJ3D(display_name)");
    r.exec("xmap = new ScalarMap(RealType.Longitude, Display.XAxis)");
    r.exec("ymap = new ScalarMap(RealType.Latitude, Display.YAxis)");
    r.exec("zmap = new ScalarMap(vis_radiance, Display.ZAxis)");
    r.exec("rmap = new ConstantMap(one_half, Display.Red)");
    r.exec("gmap = new ScalarMap(vis_radiance, Display.Green)");
    r.exec("bmap = new ConstantMap(one_half, Display.Blue)");
    r.exec("d.addMap(xmap)");
    r.exec("d.addMap(ymap)");
    r.exec("d.addMap(zmap)");
    r.exec("d.addMap(rmap)");
    r.exec("d.addMap(gmap)");
    r.exec("d.addMap(bmap)");

    // construct data reference
    r.setVar("ref_name", "ref");
    r.exec("ref = new DataReferenceImpl(ref_name)");
    r.exec("ref.setData(imaget1)");
    r.exec("d.addReference(ref)");

    // display results onscreen
    r.exec("comp = d.getComponent()");
    Component c = (Component) r.getVar("comp");
    JFrame f = new JFrame("VisAD with reflection");
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    JPanel p = new JPanel();
    f.setContentPane(p);
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.add(c);
    f.pack();
    r.setVar("f", f);
    r.exec("Util.centerWindow(f)");
    f.show();
  }

}
