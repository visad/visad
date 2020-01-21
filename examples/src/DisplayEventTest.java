//
// DisplayEventTest.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;

import visad.*;
import visad.java2d.*;
import visad.java3d.*;
import visad.util.Util;

public class DisplayEventTest {

  /**
   * Run 'java visad.DisplayEvent' to test DisplayEvents in Java3D,
   * or 'java visad.DisplayEvent x' to test them in Java2D.
   */
  public static void main(String[] args) throws Exception {
    boolean j2d = args.length > 0;

    DisplayImpl display = j2d ?
      (DisplayImpl) new DisplayImplJ2D("display") :
      (DisplayImpl) new DisplayImplJ3D("display");

    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = RealType.getRealType("vis_radiance");
    RealType ir_radiance = RealType.getRealType("ir_radiance");
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);

    int size = 32;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    display.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    if (!j2d) display.addMap(new ScalarMap(vis_radiance, Display.ZAxis));

    display.addMap(new ScalarMap(RealType.Latitude, Display.Red));
    display.addMap(new ScalarMap(RealType.Longitude, Display.Green));
    display.addMap(new ScalarMap(vis_radiance, Display.Blue));

    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setTextureEnable(false);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display.addReference(ref_imaget1, null);

    final String[] ids = {
      "?", "MOUSE_PRESSED", "TRANSFORM_DONE", "FRAME_DONE",
      "MOUSE_PRESSED_CENTER", "MOUSE_PRESSED_LEFT",  "MOUSE_PRESSED_RIGHT",
      "MOUSE_RELEASED", "MOUSE_RELEASED_CENTER", "MOUSE_RELEASED_LEFT",
      "MOUSE_RELEASED_RIGHT", "MAP_ADDED", "MAPS_CLEARED", "REFERENCE_ADDED",
      "REFERENCE_REMOVED", "DESTROYED", "KEY_PRESSED", "KEY_RELEASED",
      "MOUSE_DRAGGED", "MOUSE_ENTERED", "MOUSE_EXITED", "MOUSE_MOVED",
      "WAIT_ON", "WAIT_OFF"
    };

    // enable extra mouse event handling
    display.enableEvent(DisplayEvent.MOUSE_DRAGGED);
    display.enableEvent(DisplayEvent.MOUSE_ENTERED); 
    display.enableEvent(DisplayEvent.MOUSE_EXITED);
    display.enableEvent(DisplayEvent.MOUSE_MOVED);
    display.enableEvent(DisplayEvent.WAIT_ON);
    display.enableEvent(DisplayEvent.WAIT_OFF);

    // enable extra keyboard event handling
    if (j2d) {
      DisplayRendererJ2D dr =
        (DisplayRendererJ2D) display.getDisplayRenderer();
      KeyboardBehaviorJ2D kb = new KeyboardBehaviorJ2D(dr);
      dr.addKeyboardBehavior(kb);
    }
    else {
      DisplayRendererJ3D dr =
        (DisplayRendererJ3D) display.getDisplayRenderer();
      KeyboardBehaviorJ3D kb = new KeyboardBehaviorJ3D(dr);
      dr.addKeyboardBehavior(kb);
    }

    display.addDisplayListener(new DisplayListener() {
      public void displayChanged(DisplayEvent e) {
        int id = e.getId();
        System.out.print(System.currentTimeMillis() + ": " + ids[id]);
        InputEvent ie = e.getInputEvent();
        if (ie == null) System.out.println();
        else {
          System.out.print(" [ ");
          if (ie instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) ie;
            int x = me.getX();
            int y = me.getY();
            System.out.print("(" + x + ", " + y + ") ");
          }
          else if (ie instanceof KeyEvent) {
            KeyEvent ke = (KeyEvent) ie;
            char key = ke.getKeyChar();
            System.out.print("'" + key + "' ");
          }
          int mods = ie.getModifiers();
          if ((mods & InputEvent.CTRL_MASK) != 0) System.out.print("CTRL ");
          if ((mods & InputEvent.SHIFT_MASK) != 0) System.out.print("SHIFT ");
          System.out.println("]");
        }
      }
    });

    JFrame frame = new JFrame("VisAD DisplayEvent test");
    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
    frame.setContentPane(pane);
    pane.add(display.getComponent());

    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });
    frame.pack();
    Util.centerWindow(frame);
    frame.show();

    display.getComponent().requestFocus();
  }

}
