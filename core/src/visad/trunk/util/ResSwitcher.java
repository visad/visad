//
// ResSwitcher.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
public class ResSwitcher implements ActivityHandler {

  /** Debugging flag. */
  private static final boolean DEBUG = true;

  /** Display affected by resolution switcher. */
  private LocalDisplay display;

  /** High-resolution data reference. */
  private DataReferenceImpl hi_ref;
  
  /** Low-resolution data reference. */
  private DataReferenceImpl lo_ref;

  /** High-resolution data renderer toggled by resolution switcher. */
  private DataRenderer hi_rend;
  
  /** Low-resolution data renderer toggled by resolution switcher. */
  private DataRenderer lo_rend;

  /** Computational cell that scales down high-resolution data. */
  private CellImpl cell;

  /** Scale factor for low-resolution data. */
  private double scale = 0.5;

  /** Rescales a field by the given scale factor. */
  public static FieldImpl rescale(FieldImpl field, double scale)
    throws VisADException, RemoteException
  {
    Set set = field.getDomainSet();
    if (!(set instanceof LinearSet)) return null;
    LinearSet lset = (LinearSet) set;

    // scale set to new resolution
    int dim = set.getDimension();
    Linear1DSet[] lin_sets = new Linear1DSet[dim];
    for (int i=0; i<dim; i++) {
      Linear1DSet lin1set = lset.getLinear1DComponent(i);
      MathType type = lin1set.getType();
      double first = lin1set.getFirst();
      double last = lin1set.getLast();
      int length = (int) (lin1set.getLength() * scale);
      if (length < 1) length = 1;
      CoordinateSystem coord_sys = lin1set.getCoordinateSystem();
      Unit[] units = lin1set.getSetUnits();

      lin_sets[i] = new Linear1DSet(type,
        first, last, length, coord_sys, units, null);
    }

    // compute new linear set at new resolution
    MathType type = set.getType();
    CoordinateSystem coord_sys = set.getCoordinateSystem();
    Unit[] units = set.getSetUnits();
    Set nset;
    if (dim == 1) {
      nset = lin_sets[0];
    }
    else if (dim == 2) {
      nset = new Linear2DSet(type, lin_sets, coord_sys, units, null);
    }
    else if (dim == 3) {
      nset = new Linear3DSet(type, lin_sets, coord_sys, units, null);
    }
    else {
      nset = new LinearNDSet(type, lin_sets, coord_sys, units, null);
    }

    // rescale data
    return (FieldImpl)
      field.resample(nset, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);
  }

  /**
   * Constructs a resolution switcher for swapping between high- and low-
   * resolution representations for the referenced data on the given display.
   */
  public ResSwitcher(LocalDisplay d, DataReferenceImpl ref)
    throws VisADException, RemoteException
  {
    display = d;
    hi_ref = ref;
    lo_ref = new DataReferenceImpl("ResSwitcher_ref");
    display.addReference(lo_ref);

    cell = new CellImpl() {
      public void doAction() {
        try {
          // compute low-resolution data representation
          Data data = hi_ref.getData();
          if (data == null || !(data instanceof FieldImpl)) return;
          FieldImpl field = (FieldImpl) data;

          // check if data is a timestack
          FunctionType ftype = (FunctionType) data.getType();
          RealTupleType domain = ftype.getDomain();
          MathType range = ftype.getRange();
          FieldImpl downfield;
          if (domain.getDimension() == 1 && range instanceof FunctionType) {
            // timestack; downsample each range component
            downfield = new FieldImpl(ftype, field.getDomainSet());
            int len = field.getLength();
            for (int i=0; i<len; i++) {
              Data sample = field.getSample(i);
              if (!(sample instanceof FieldImpl)) return;
              downfield.setSample(i, rescale((FieldImpl) sample, scale));
            }
          }
          else downfield = rescale(field, scale);
          lo_ref.setData(downfield);
        }
        catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
        catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
      }
    };
    cell.addReference(hi_ref);

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

    d.addActivityHandler(this);
  }

  /** Unlinks the resolution switcher from its display. */
  public void unlink() throws VisADException {
    display.removeActivityHandler(this);
  }

  /**
   * Sets the factor by which the low-resolution representation is
   * scaled down from the high-resolution one.
   */
  public void setResolutionScale(double scale) throws VisADException {
    if (scale > 1) this.scale = 1.0 / scale;
    else {
      throw new VisADException(
        "ResSwitcher: scale factor must be greater than 1");
    }
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
