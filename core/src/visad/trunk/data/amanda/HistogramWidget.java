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
import visad.ScalarType;
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

  private DisplayImplJ2D dpy;
  private DataReferenceImpl ref;
  private ScalarMap xMap, yMap, cMap;

  public HistogramWidget()
    throws RemoteException, VisADException
  {
    dpy = new DisplayImplJ2D("histogram");

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
    float[][] histoData = evt.makeHistogram(xMap, yMap, cMap);

    Gridded2DSet set = new Gridded2DSet(histoType, histoData,
                                        histoData[0].length);
    ref.setData(set);
  }
}
