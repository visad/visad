//
// BioAnimWidget.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

import java.awt.event.*;
import java.rmi.RemoteException;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;

/** BioAnimWidget is a simple widget for controlling animation. */
public class BioAnimWidget extends JPanel implements ControlListener {

  // -- GUI COMPONENTS --

  private JButton go;
  private JLabel fps;
  private BioSpinWidget spin;


  // -- OTHER FIELDS --

  private AnimationControl control;


  // -- CONSTRUCTOR --

  /** Constructs a new animation widget. */
  public BioAnimWidget(BioVisAD biovis) {
    go = new JButton("Animate");
    go.setPreferredSize(go.getPreferredSize());
    fps = new JLabel("FPS:");
    spin = new BioSpinWidget(1, 999, 10);
    final BioVisAD bio = biovis;
    go.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (control != null) {
          bio.sm.startAnimation();
          try { control.toggle(); }
          catch (VisADException exc) { exc.printStackTrace(); }
          catch (RemoteException exc) { exc.printStackTrace(); }
          updateWidget();
        }
      }
    });
    spin.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (control != null) {
          try { control.setStep((int) (1000.0 / spin.getValue())); }
          catch (VisADException exc) { exc.printStackTrace(); }
          catch (RemoteException exc) { exc.printStackTrace(); }
        }
      }
    });
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.add(go);
    p.add(Box.createHorizontalStrut(3));
    p.add(fps);
    p.add(Box.createHorizontalStrut(3));
    p.add(spin);
    add(p);
  }


  // -- API METHODS --

  /** Gets the currently linked animation control. */
  public AnimationControl getControl() { return control; }

  /** Links the animation widget with the given animation control. */
  public void setControl(AnimationControl control) {
    if (this.control != null) this.control.removeControlListener(this);
    this.control = control;
    if (control != null) {
      try { control.setStep((int) (1000.0 / spin.getValue())); }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
      control.addControlListener(this);
      updateWidget();
    }
  }

  /** Enables or disables this widget. */
  public void setEnabled(boolean enabled) {
    go.setEnabled(enabled);
    fps.setEnabled(enabled);
    spin.setEnabled(enabled);
  }


  // -- INTERNAL API METHODS --

  /** Called when the linked animation control changes. */
  public void controlChanged(ControlEvent e)
    throws VisADException, RemoteException
  {
    updateWidget();
  }


  // -- HELPER METHODS --

  /** Refreshes the GUI to match the linked animation control. */
  private void updateWidget() {
    go.setText(control.getOn() ? "Stop" : "Animate");
    spin.setValue((int) (1000.0 / control.getStep()));
  }

}
