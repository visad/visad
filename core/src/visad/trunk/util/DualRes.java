//
// DualRes.java
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

package visad.util;

import java.rmi.RemoteException;
import visad.*;

/**
 * Maintains two representations for a given data reference: one at
 * high (normal) resolution, and one at low (scaled-down) resolution.
 * When greater rendering speed is necessary, programs can utilize the
 * computed low-resolution data. When more detail is required, programs
 * can switch back to the hi-resolution data.
 */
public class DualRes {

  /** Debugging flag. */
  public static boolean DEBUG = true;

  /** High-resolution data reference. */
  protected DataReferenceImpl hi_ref;
  
  /** Low-resolution data reference. */
  protected DataReferenceImpl lo_ref;

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
    int dim = set.getDimension();

    // compute new lengths
    int[] lengths = new int[dim];
    for (int i=0; i<dim; i++) {
      Linear1DSet lin1set = lset.getLinear1DComponent(i);
      lengths[i] = (int) (lin1set.getLength() * scale);
      if (lengths[i] < 1) lengths[i] = 1;
    }

    return rescale(field, lengths);
  }

  /** Rescales a field by the given scale factor. */
  public static FieldImpl rescale(FieldImpl field, int[] lengths)
    throws VisADException, RemoteException
  {
    Set set = field.getDomainSet();
    if (!(set instanceof LinearSet)) return null;
    LinearSet lset = (LinearSet) set;
    int dim = set.getDimension();
    if (lengths.length != dim) {
      throw new VisADException("bad lengths dimension");
    }

    // scale set to new resolution
    Linear1DSet[] lin_sets = new Linear1DSet[dim];
    for (int i=0; i<dim; i++) {
      Linear1DSet lin1set = lset.getLinear1DComponent(i);
      MathType type = lin1set.getType();
      double first = lin1set.getFirst();
      double last = lin1set.getLast();
      CoordinateSystem coord_sys = lin1set.getCoordinateSystem();
      Unit[] units = lin1set.getSetUnits();

      lin_sets[i] = new Linear1DSet(type,
        first, last, lengths[i], coord_sys, units, null);
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
   * Constructs an object to maintain both high- and low-resolution
   * representations for the referenced data.
   */
  public DualRes(DataReferenceImpl ref)
    throws VisADException, RemoteException
  {
    hi_ref = ref;
    lo_ref = new DataReferenceImpl("DualRes_ref");

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
  }

  /**
   * Sets the factor by which the low-resolution representation is
   * scaled down from the high-resolution one.
   */
  public void setResolutionScale(double scale) throws VisADException {
    if (scale > 1) this.scale = 1.0 / scale;
    else {
      throw new VisADException(
        "DualRes: scale factor must be greater than 1");
    }
  }

  /**
   * Gets the factor by which the low-resolution representation is
   * scaled down from the high-resolution one.
   */
  public double getResolutionScale() { return 1.0 / scale; }

  /** Gets the DataReference corresponding to the full-resolution data. */
  public DataReferenceImpl getHighResReference() { return hi_ref; }

  /** Gets the DataReference corresponding to the scaled-down data. */
  public DataReferenceImpl getLowResReference() { return lo_ref; }

}
