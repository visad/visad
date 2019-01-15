/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.amanda;

import java.awt.Dimension;

import java.rmi.RemoteException;

import javax.swing.JPanel;

import visad.DataReferenceImpl;
import visad.Display;
import visad.GraphicsModeControl;
import visad.Gridded2DSet;
import visad.RealType;
import visad.RealTupleType;
import visad.ScalarMap;
import visad.VisADException;

import visad.java2d.DisplayImplJ2D;

public class HistogramWidget
  extends JPanel
{
  private static final RealType countType = RealType.getRealType("count");

  private static RealTupleType histoType;

  static {
    try {
      histoType = new RealTupleType(Hit.leadingEdgeTimeType, countType);
    } catch (VisADException ve) {
      System.err.println("Couldn't create histogram MathType");
      ve.printStackTrace();
      histoType = null;
    }
  }

  private ScalarMap dpyColorMap;
  private DataReferenceImpl ref;
  private ScalarMap xMap, yMap, cMap;

  public HistogramWidget(ScalarMap dpyColorMap)
    throws RemoteException, VisADException
  {
    this.dpyColorMap = dpyColorMap;

    DisplayImplJ2D dpy = new DisplayImplJ2D("histogram");

    xMap = new ScalarMap(countType, Display.XAxis);
    yMap = new ScalarMap(Hit.leadingEdgeTimeType, Display.YAxis);

    dpy.addMap(xMap);
    dpy.addMap(yMap);

    cMap = new ScalarMap(Hit.leadingEdgeTimeType, Display.RGB);
    dpy.addMap(cMap);
    F2000Util.invertColorTable(cMap);

    GraphicsModeControl gmc2 = dpy.getGraphicsModeControl();
    gmc2.setScaleEnable(true);

    ref = new DataReferenceImpl("histogram");
    // data is set when a new event is selected
    dpy.addReference(ref);

    JPanel dpyPanel = (JPanel )dpy.getComponent();
    Dimension dim = new Dimension(250, 250);
    dpyPanel.setPreferredSize(dim);
    dpyPanel.setMinimumSize(dim);

    add(dpyPanel);
  }

  public void setEvent(Event evt)
    throws RemoteException, VisADException
  {
    float[][] histoData = evt.makeHistogram(xMap, yMap, cMap, dpyColorMap);

    Gridded2DSet set = new Gridded2DSet(histoType, histoData,
                                        histoData[0].length);
    ref.setData(set);
  }
}
