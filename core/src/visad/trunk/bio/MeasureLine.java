//
// MeasureLine.java
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

package visad.bio;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;

/**
 * MeasureLine maintains a (p1, line, p2) triple of
 * DataReferences for measuring distances in a field.
 */
public class MeasureLine extends MeasureThing {

  /** List of all measurement lines. */
  private static final Vector lines = new Vector();

  /** First free id number for lines. */
  private static int maxId = 0;

  /** Id number for the line. */
  int id;

  /** Data reference for connecting line. */
  private DataReferenceImpl ref_line;

  /** Cell that ties line to endpoints. */
  private CellImpl cell;

  /** Constructs a measurement object to match the given field. */
  public MeasureLine() throws VisADException, RemoteException {
    super(2, 2);
    ref_line = new DataReferenceImpl("line");

    cell = new CellImpl() {
      public void doAction() {
        if (dtype == null) return;
        float[][] vals = null;
        RealTuple p1 = (RealTuple) refs[0].getData();
        RealTuple p2 = (RealTuple) refs[1].getData();
        if (m != null) m.values = new RealTuple[] {p1, p2};
        if (p1 == null || p2 == null) return;

        // extract samples
        int dim = p1.getDimension();
        float[][] samps = new float[dim][2];
        try {
          for (int i=0; i<dim; i++) {
            Real r1 = (Real) p1.getComponent(i);
            Real r2 = (Real) p2.getComponent(i);
            samps[i][0] = (float) r1.getValue();
            samps[i][1] = (float) r2.getValue();
          }
        }
        catch (VisADException exc) {
          exc.printStackTrace();
          samps = null;
        }
        catch (RemoteException exc) {
          exc.printStackTrace();
          samps = null;
        }
        if (samps == null) return;

        // set line data
        try {
          FunctionType ftype =
            new FunctionType(dtype, FileSeriesWidget.COLOR_TYPE);
          Gridded2DSet set = new Gridded2DSet(dtype, samps, 2);
          FlatField field = new FlatField(ftype, set);
          field.setSamples(new double[][] {{id, id}});
          ref_line.setData(field);
        }
        catch (VisADException exc) { exc.printStackTrace(); }
        catch (RemoteException exc) { exc.printStackTrace(); }
      }
    };
    cell.addReference(refs[0]);
    cell.addReference(refs[1]);

    id = maxId++;
    lines.add(this);
  }

  /** Adds the distance measuring data to the given display. */
  public void setDisplay(DisplayImpl d)
    throws VisADException, RemoteException
  {
    if (display != null) {
      // remove measuring data from old display
      display.disableAction();
      display.removeReference(refs[0]);
      display.removeReference(refs[1]);
      display.removeReference(ref_line);
      display.enableAction();
    }
    display = d;
    if (d == null) return;
    d.disableAction();

    // configure display appropriately
    d.getGraphicsModeControl().setPointSize(5.0f);
    d.getDisplayRenderer().setPickThreshhold(5.0f);

    // add endpoints
    addDirectManipRef(d, refs[0]);
    addDirectManipRef(d, refs[1]);

    // add connecting line
    d.addReference(ref_line);

    d.enableAction();
  }

  /** Sets the line's color. */
  public void setColor(Color color) {
    if (display == null) return;
    Vector v = display.getControls(ColorControl.class);
    ColorControl cc = (ColorControl) v.lastElement();
    if (cc == null) return;
    float[][] table = cc.getTable();
    int len = table[0].length;
    if (len != maxId) {
      // adjust table as necessary
      float[][] t = new float[3][maxId];
      int s = len < maxId ? len : maxId;
      System.arraycopy(table[0], 0, t[0], 0, s);
      System.arraycopy(table[1], 0, t[1], 0, s);
      System.arraycopy(table[2], 0, t[2], 0, s);
      table = t;
    }
    float[] cvals = color.getColorComponents(null);
    table[0][id] = cvals[0];
    table[1][id] = cvals[1];
    table[2][id] = cvals[2];
    if (m != null) m.setColor(color);
    try {
      cc.setTable(table);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Links the given measurement. */
  public void setMeasurement(Measurement m) {
    super.setMeasurement(m);
    if (m != null) setColor(m.color);
  }

}
