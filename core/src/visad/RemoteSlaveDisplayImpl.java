//
// RemoteSlaveDisplayImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import javax.swing.*;
import visad.browser.Convert;

/** RemoteSlaveDisplayImpl is an implementation of a slaved display that
    receives its images from a RemoteDisplay (via RMI). */
public class RemoteSlaveDisplayImpl extends UnicastRemoteObject
  implements RemoteSlaveDisplay, MouseListener, MouseMotionListener
{

  private RemoteDisplay display;
  private BufferedImage image;
  private JComponent component;
  private Vector listen = new Vector();

  /** Construct a new slaved display linked to the given RemoteDisplay */
  public RemoteSlaveDisplayImpl(RemoteDisplay d)
    throws VisADException, RemoteException
  {
    display = d;
    if (display != null) {
      display.addSlave(this);
    }

    component = new JComponent() {
      public void paint(Graphics g) {
        if (image != null) g.drawImage(image, 0, 0, this);
      }

      public Dimension getPreferredSize() {
        if (image == null) return new Dimension(256, 256);
        return new Dimension(image.getWidth(), image.getHeight());
      }
    };
    component.addMouseListener(this);
    component.addMouseMotionListener(this);
  }

  /** Remove the link from this slaved display to its remote display */
  public void unlink() throws VisADException, RemoteException {
    display.removeSlave(this);
  }

  /** Get this slave display's component, for adding to a user interface */
  public JComponent getComponent() {
    return component;
  }

  /** Add a display listener to this slave display.
      The following display events are supported:<ul>
      <li>DisplayEvent.FRAME_DONE
      <li>DisplayEvent.MOUSE_PRESSED
      <li>DisplayEvent.MOUSE_PRESSED_LEFT
      <li>DisplayEvent.MOUSE_PRESSED_CENTER
      <li>DisplayEvent.MOUSE_PRESSED_RIGHT
      <li>DisplayEvent.MOUSE_RELEASED
      <li>DisplayEvent.MOUSE_RELEASED_LEFT
      <li>DisplayEvent.MOUSE_RELEASED_CENTER
      <li>DisplayEvent.MOUSE_RELEASED_RIGHT
      </ul> */
  public void addDisplayListener(DisplayListener l) {
    synchronized (listen) {
      listen.add(l);
    }
  }

  /** Remove a display listener from this slave display */
  public void removeDisplayListener(DisplayListener l) {
    synchronized (listen) {
      listen.remove(l);
    }
  }

  /** Get this slave display's current image */
  public BufferedImage getImage() {
    return image;
  }

  /** Update this slave display with the given RLE-encoded image pixels */
  public void sendImage(int[] pixels, int width, int height, int type)
    throws RemoteException
  {
    // decode pixels
    int[] decoded = Convert.decodeRLE(pixels);

    // build image from decoded pixels
    BufferedImage img = new BufferedImage(width, height, type);
    img.setRGB(0, 0, width, height, decoded, 0, width);

    // wait for image to finish, just in case
    MediaTracker mt = new MediaTracker(component);
    mt.addImage(image, 0);
    try {
      mt.waitForID(0);
    }
    catch (InterruptedException exc) { }
    image = img;

    // redraw display using new image
    component.repaint();

    // notify listeners of display change
    DisplayEvent e = new DisplayEvent(display, DisplayEvent.FRAME_DONE);
    synchronized (listen) {
      for (int i=0; i<listen.size(); i++) {
        DisplayListener l = (DisplayListener) listen.elementAt(i);
        try {
          l.displayChanged(e);
        }
        catch (VisADException exc) {
          exc.printStackTrace();
        }
      }
    }
  }

  /** Send the given message to this slave display */
  public void sendMessage(String message) throws RemoteException {
    // The message should be of the form:
    //   class\nnumber\nstate
    // where class is the class name of the control that has changed,
    // number is the index into that class name's control list,
    // and state is the save string corresponding to the control's new state.
    StringTokenizer st = new StringTokenizer(message, "\n");
    Class c = null;
    try {
      c = Class.forName(st.nextToken());
    }
    catch (ClassNotFoundException exc) { }
    int index = Convert.getInt(st.nextToken());
    String save = st.nextToken();
    // CTR: ignore message to RMI-based slave display clients for now
  }

  public void mouseClicked(MouseEvent e) {
    // This event currently generates a "type not recognized" error
    // sendMouseEvent(e);
  }

  public void mouseEntered(MouseEvent e) {
    sendMouseEvent(e);
  }

  public void mouseExited(MouseEvent e) {
    sendMouseEvent(e);
  }

  public void mousePressed(MouseEvent e) {
    sendMouseEvent(e);

    // notify display listeners
    DisplayEvent de1 =
      new DisplayEvent(display, DisplayEvent.MOUSE_PRESSED, e);
    DisplayEvent de2 = null;
    if (SwingUtilities.isLeftMouseButton(e)) {
      de2 = new DisplayEvent(display, DisplayEvent.MOUSE_PRESSED_LEFT, e);
    }
    else if (SwingUtilities.isMiddleMouseButton(e)) {
      de2 = new DisplayEvent(display, DisplayEvent.MOUSE_PRESSED_CENTER, e);
    }
    else if (SwingUtilities.isRightMouseButton(e)) {
      de2 = new DisplayEvent(display, DisplayEvent.MOUSE_PRESSED_RIGHT, e);
    }
    synchronized (listen) {
      for (int i=0; i<listen.size(); i++) {
        DisplayListener l = (DisplayListener) listen.elementAt(i);
        try {
          l.displayChanged(de1);
          if (de2 != null) l.displayChanged(de2);
        }
        catch (VisADException exc) {
          exc.printStackTrace();
        }
        catch (RemoteException exc) {
          exc.printStackTrace();
        }
      }
    }
  }

  public void mouseReleased(MouseEvent e) {
    sendMouseEvent(e);

    // notify display listeners
    DisplayEvent de1 =
      new DisplayEvent(display, DisplayEvent.MOUSE_RELEASED, e);
    DisplayEvent de2 = null;
    if (SwingUtilities.isLeftMouseButton(e)) {
      de2 = new DisplayEvent(display, DisplayEvent.MOUSE_RELEASED_LEFT, e);
    }
    else if (SwingUtilities.isMiddleMouseButton(e)) {
      de2 =
        new DisplayEvent(display, DisplayEvent.MOUSE_RELEASED_CENTER, e);
    }
    else if (SwingUtilities.isRightMouseButton(e)) {
      de2 = new DisplayEvent(display, DisplayEvent.MOUSE_RELEASED_RIGHT, e);
    }
    synchronized (listen) {
      for (int i=0; i<listen.size(); i++) {
        DisplayListener l = (DisplayListener) listen.elementAt(i);
        try {
          l.displayChanged(de1);
          if (de2 != null) l.displayChanged(de2);
        }
        catch (VisADException exc) {
          exc.printStackTrace();
        }
        catch (RemoteException exc) {
          exc.printStackTrace();
        }
      }
    }
  }

  public void mouseDragged(MouseEvent e) {
    sendMouseEvent(e);
  }

  public void mouseMoved(MouseEvent e) {
    // This event currently generates a "type not recognized" error
    // sendMouseEvent(e);
  }

  /** feed MouseEvents to this display's MouseHelper */
  private void sendMouseEvent(MouseEvent e) {
    try {
      display.sendMouseEvent(e);
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
  }

}
