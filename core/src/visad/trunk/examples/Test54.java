import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Graphics;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.image.BufferedImage;

import java.rmi.RemoteException;

import visad.*;

import visad.java3d.DisplayImplJ3D;

public class Test54
       extends TestSkeleton
{
  boolean hasClientServerMode() { return false; }

  public Test54() { }

  public Test54(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType count = new RealType("count", null, null);
    FunctionType ir_histogram = new FunctionType(ir_radiance, count);

    int size = 64;
    FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    display1.addMap(new ScalarMap(count, Display.YAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.XAxis));

    display1.addMap(new ConstantMap(0.0, Display.Red));
    display1.addMap(new ConstantMap(1.0, Display.Green));
    display1.addMap(new ConstantMap(0.0, Display.Blue));

    display1.getDisplayRenderer().setBackgroundColor(1.0f, 0.0f, 1.0f);

    DataReferenceImpl ref_histogram1;
    ref_histogram1 = new DataReferenceImpl("ref_histogram1");
    ref_histogram1.setData(histogram1);
    display1.addReference(ref_histogram1, null);

    boolean forever = true;
    boolean[] box_on = {true, true, true, false};
    float[][] box_color = {{1.0f, 0.0f, 0.0f},
                           {0.0f, 1.0f, 0.0f},
                           {0.0f, 0.0f, 1.0f},
                           {0.5f, 0.5f, 0.5f}};
    float[][] cursor_color = {{0.5f, 0.5f, 0.5f},
                              {1.0f, 0.0f, 0.0f},
                              {0.0f, 1.0f, 0.0f},
                              {0.0f, 0.0f, 1.0f}};
    DisplayRenderer displayRenderer = display1.getDisplayRenderer();
    int index = 0;
    while (forever) {
      // delay(5000);
      try {
        Thread.sleep(5000);
      }
      catch (InterruptedException e) {
      }
      System.out.println("\ndelay\n");
      displayRenderer.setBoxOn(box_on[index]);
      displayRenderer.setBoxColor(box_color[index][0],
                                  box_color[index][1],
                                  box_color[index][2]);
      displayRenderer.setCursorColor(cursor_color[index][0],
                                     cursor_color[index][1],
                                     cursor_color[index][2]);
      index++;
      if (index > 3) index = 0;
    }

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "background color in Java3D"; }

  public String toString() { return ": test background color in Java3D"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test54 t = new Test54(args);
  }
}
