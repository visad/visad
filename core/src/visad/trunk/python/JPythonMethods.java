//
// JPythonMethods.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad.python;

import java.util.Vector;
import java.awt.event.*;
import java.rmi.RemoteException;
import javax.swing.JFrame;
import visad.*;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.data.*;
import visad.ss.MappingDialog;
import visad.bom.ImageRendererJ3D;

/**
 * A collection of methods for working with VisAD, callable from the
 * JPython editor.
 */
public abstract class JPythonMethods {

  private static final String ID = JPythonMethods.class.getName();

  private static DefaultFamily form = new DefaultFamily(ID);

  private static JFrame displayFrame = null;

  private static JFrame widgetFrame = null;

  /** reads in data from the given location (filename or URL) */
  public static DataImpl read(String location) throws VisADException {
    return form.open(location);
  }

  private static DisplayImpl display = null;
  private static ScalarMap[] maps = null;
  private static MappingDialog dialog = null;
  private static Vector data_references = null;

  /** displays the given data onscreen */
  public static void plot(DataImpl data)
    throws VisADException, RemoteException
  {
    if (data == null) throw new VisADException("Data cannot be null");
    if (display == null) {
      displayFrame = new JFrame("VisAD Display Plot");
      widgetFrame = new JFrame("VisAD Display Widgets");
      WindowListener l = new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          synchronized (displayFrame) {
            displayFrame.setVisible(false);
            widgetFrame.setVisible(false);
          }
        }
      };
      // set up scalar maps
      maps = data.getType().guessMaps(true);
  
      // allow user to alter default mappings
      dialog = new MappingDialog(null, data, maps, true, true);
      dialog.display();
      if (dialog.Confirm) maps = dialog.ScalarMaps;
      boolean d3d = false;
      for (int i=0; i<maps.length; i++) {
        if (maps[i].getDisplayScalar().equals(Display.ZAxis) ||
            maps[i].getDisplayScalar().equals(Display.Latitude) ||
            maps[i].getDisplayScalar().equals(Display.Alpha)) d3d = true;
      }
      display = d3d ? new DisplayImplJ3D(ID) :
                      new DisplayImplJ3D(ID, new TwoDDisplayRendererJ3D());
      for (int i=0; i<maps.length; i++) display.addMap(maps[i]);
  
      // set up widget panel
      widgetFrame.addWindowListener(l);
      widgetFrame.getContentPane().add(display.getWidgetPanel());
      widgetFrame.pack();
      widgetFrame.setVisible(true);
  
      // set up display frame
      displayFrame.addWindowListener(l);
      displayFrame.getContentPane().add(display.getComponent());
      displayFrame.pack();
      displayFrame.setSize(512, 512);
      displayFrame.setVisible(true);
      data_references = new Vector();
    }

    DataReferenceImpl ref = new DataReferenceImpl(ID);
    data_references.addElement(ref);
    ref.setData(data);
    MathType type = data.getType();
    try {
      ImageRendererJ3D.verifyImageRendererUsable(type, maps);
      display.addReferences(new ImageRendererJ3D(), ref);
    }
    catch (VisADException exc) {
      display.addReference(ref);
    }
  }

  // this is just a temporary and dumb hack
  public static void clearplot() throws VisADException, RemoteException {
    if (display != null) {
      displayFrame.setVisible(false);
      displayFrame.dispose();
      displayFrame = null;
      widgetFrame.setVisible(false);
      widgetFrame.dispose();
      widgetFrame = null;
      display.destroy();
      display = null;
    }
  }

}
