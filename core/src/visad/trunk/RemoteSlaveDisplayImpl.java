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
import javax.swing.JComponent;

/** RemoteSlaveDisplayImpl is an implementation of a slaved display that
    receives its images from a RemoteDisplay.<P> */
public class RemoteSlaveDisplayImpl extends UnicastRemoteObject
       implements RemoteSlaveDisplay, MouseListener, MouseMotionListener {

  private RemoteDisplay display;
  private BufferedImage image;
  private JComponent component;

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

  /** Removes the link from this slaved display to its remote display */
  public void unlink() throws VisADException, RemoteException {
    display.removeSlave(this);
  }

  /** Get this slave display's component, for adding to a user interface */
  public JComponent getComponent() {
    return component;
  }

  /** Update this slave display with the given image pixels */
  public void sendImage(int[] pixels, int width, int height, int type)
              throws RemoteException {
    BufferedImage img = new BufferedImage(width, height, type);
    img.setRGB(0, 0, width, height, pixels, 0, width);

    MediaTracker mt = new MediaTracker(component);
    mt.addImage(image, 0);
    try {
      mt.waitForID(0);
    }
    catch (InterruptedException exc) { }
    image = img;
    component.repaint();
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
  }

  public void mouseReleased(MouseEvent e) {
    sendMouseEvent(e);
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

