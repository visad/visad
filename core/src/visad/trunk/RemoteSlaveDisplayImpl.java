//
// RemoteSlaveDisplayImpl.java
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

package visad;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import javax.swing.JComponent;
import visad.util.Util;

/** RemoteSlaveDisplayImpl is an implementation of a slaved display that
    receives its images from a RemoteDisplay.<P> */
public class RemoteSlaveDisplayImpl extends UnicastRemoteObject
       implements RemoteSlaveDisplay, MouseListener, MouseMotionListener {

  private RemoteDisplay display;
  private BufferedImage image;
  private JComponent component;
  private Vector listeners = new Vector();

  /** Construct a new slaved display linked to the given RemoteDisplay */
  public RemoteSlaveDisplayImpl(RemoteDisplay d) throws VisADException,
                                                        RemoteException {
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

  /** Add a mouse listener to this slave display */
  public void addMouseListener(MouseListener l) {
    listeners.add(l);
  }

  /** Remove a mouse listener from this slave display */
  public void removeMouseListener(MouseListener l) {
    listeners.remove(l);
  }

  /** Update this slave display with the given RLE-encoded image pixels */
  public void sendImage(int[] pixels, int width, int height, int type)
    throws RemoteException
  {
    // decode pixels
    int[] decoded = Util.decodeRLE(pixels);

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
  }

  public void mouseClicked(MouseEvent e) {
    // This event currently generates a "type not recognized" error
    // sendMouseEvent(e);
    
    // notify listeners
    synchronized (listeners) {
      for (int i=0; i<listeners.size(); i++) {
        MouseListener l = (MouseListener) listeners.elementAt(i);
        l.mouseClicked(e);
      }
    }
  }

  public void mouseEntered(MouseEvent e) {
    sendMouseEvent(e);

    // notify listeners
    synchronized (listeners) {
      for (int i=0; i<listeners.size(); i++) {
        MouseListener l = (MouseListener) listeners.elementAt(i);
        l.mouseEntered(e);
      }
    }
  }

  public void mouseExited(MouseEvent e) {
    sendMouseEvent(e);

    // notify listeners
    synchronized (listeners) {
      for (int i=0; i<listeners.size(); i++) {
        MouseListener l = (MouseListener) listeners.elementAt(i);
        l.mouseExited(e);
      }
    }
  }

  public void mousePressed(MouseEvent e) {
    sendMouseEvent(e);

    // notify listeners
    synchronized (listeners) {
      for (int i=0; i<listeners.size(); i++) {
        MouseListener l = (MouseListener) listeners.elementAt(i);
        l.mousePressed(e);
      }
    }
  }

  public void mouseReleased(MouseEvent e) {
    sendMouseEvent(e);

    // notify listeners
    synchronized (listeners) {
      for (int i=0; i<listeners.size(); i++) {
        MouseListener l = (MouseListener) listeners.elementAt(i);
        l.mouseReleased(e);
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

