//
// ResSwitcher.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import java.awt.event.*;
import javax.swing.*;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;
import visad.data.DefaultFamily;
import visad.java3d.DisplayImplJ3D;

/**
 * Handles automatic toggling between a high-resolution representation
 * and a low-resolution one for a data object.  When the display is
 * busy, the low-resolution representation is used for faster response
 * time.  When the display is idle, the high-resolution representation
 * is used for greater detail.
 */
public class ResSwitcher extends DualRes implements ActivityHandler {

  /** Display affected by resolution switcher. */
  private LocalDisplay display;

  /** High-resolution data renderer toggled by resolution switcher. */
  private DataRenderer hi_rend;
  
  /** Low-resolution data renderer toggled by resolution switcher. */
  private DataRenderer lo_rend;

  /**
   * Constructs a resolution switcher for swapping between high- and low-
   * resolution representations for the referenced data on the given display.
   */
  public ResSwitcher(LocalDisplay d, DataReferenceImpl ref)
    throws VisADException, RemoteException
  {
    this(d, ref, null, null);
  }

  /**
   * Constructs a resolution switcher for swapping between high- and low-
   * resolution representations for the referenced data on the given display.
   *
   * @param renderer The data renderer to be used for the low-res rendering.
   * @param cmaps    The ConstantMaps to be used for the low-res rendering.
   */
  public ResSwitcher(LocalDisplay d, DataReferenceImpl ref,
    DataRenderer renderer, ConstantMap[] cmaps)
    throws VisADException, RemoteException
  {
    super(ref);
    display = d;
    if (renderer == null) display.addReference(lo_ref, cmaps);
    else display.addReferences(renderer, lo_ref, cmaps);

    // get data renderers
    Vector dataRenderers = display.getRendererVector();
    int len = dataRenderers == null ? 0 : dataRenderers.size();
    int flags = 0;
    for (int i=0; i<len && flags!=3; i++) {
      DataRenderer rend = (DataRenderer) dataRenderers.elementAt(i);
      DataDisplayLink[] links = rend.getLinks();
      for (int j=0; j<links.length && flags!=3; j++) {
        DataReference jref = links[j].getDataReference();
        if (jref == hi_ref) {
          hi_rend = rend;
          flags &= 1;
        }
        else if (jref == lo_ref) {
          lo_rend = rend;
          flags &= 2;
        }
      }
    }

    display.addActivityHandler(this);
  }

  /** Unlinks the resolution switcher from its display. */
  public void unlink() throws VisADException {
    display.removeActivityHandler(this);
  }

  /** Swaps in the low-resolution data when the display is busy. */
  public void busyDisplay(LocalDisplay d) {
    // switch on low-res data
    if (lo_rend != null && hi_rend != null) {
      lo_rend.toggle(true);
      hi_rend.toggle(false);
    }
  }

  /** Swaps in the high-resolution data when the display is idle. */
  public void idleDisplay(LocalDisplay d) {
    // switch on hi-res data
    if (lo_rend != null && hi_rend != null) {
      hi_rend.toggle(true);
      lo_rend.toggle(false);
    }
  }

  /** Run 'java visad.util.ResSwitcher data_file' to test ResSwitcher. */
  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Please specify a datafile on the command line.");
      return;
    }
    DefaultFamily loader = new DefaultFamily("loader");
    Data d = loader.open(args[0]);

    DisplayImplJ3D display = new DisplayImplJ3D("display");
    ScalarMap[] maps = d.getType().guessMaps(true);
    if (maps != null) {
      for (int i=0; i<maps.length; i++) display.addMap(maps[i]);
    }

    DataReferenceImpl ref = new DataReferenceImpl("ref");
    ref.setData(d);
    display.addReference(ref);

    ResSwitcher rs = new ResSwitcher(display, ref);
    if (args.length > 1) {
      try { rs.setResolutionScale(Double.parseDouble(args[1])); }
      catch (NumberFormatException exc) { }
    }

    JFrame frame = new JFrame("ResSwitcher test");
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setAlignmentY(JPanel.TOP_ALIGNMENT);
    p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.setContentPane(p);
    p.add(display.getComponent());

    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    frame.pack();
    frame.show();
  }

}
