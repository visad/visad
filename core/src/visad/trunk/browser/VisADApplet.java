//
// VisADApplet.java
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

package visad.browser;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;
import java.io.*;
import java.net.*;

/** An applet for connecting to a VisAD display available through a
    SocketSlaveDisplay server. The applet functions completely independently
    of VisAD, using only JDK 1.1 code, so that it can be imbedded
    within a web page for use in a web browser. */
public class VisADApplet extends Applet
  implements ActionListener, MouseListener, MouseMotionListener
{

  /** the default host name for the SocketSlaveDisplay server */
  private static final String DEFAULT_HOST = "localhost";

  /** the default port at which to connect */
  private static final int DEFAULT_PORT = 4567;

  /** whether the applet client is connected to a server */
  private boolean connected = false;

  /** IP address of the server */
  private String address = "";

  /** port of the server */
  private int port = DEFAULT_PORT;

  /** currently connected socket */
  private Socket socket = null;

  /** output stream of currently connected socket */
  private DataOutputStream out = null;

  /** latest image from the server's display */
  private Image image = null;

  /** text field for typing in IP address of server */
  private TextField addressField;

  /** text field for typing in port of server */
  private TextField portField;

  /** button for connecting to the specified IP address and port */
  private Button connectButton;

  /** canvas for painting remote display image from the server */
  private Component canvas;

  /** thread for communicating with server */
  private Thread commThread = null;

  /** adds a component to the applet with the specified constraints */
  private void addComponent(Component c, GridBagLayout layout,
    int x, int y, int w, int h, double wx, double wy)
  {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = w;
    gbc.gridheight = h;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = wx;
    gbc.weighty = wy;
    layout.setConstraints(c, gbc);
    add(c);
  }

  /** initializes the applet and lays out its GUI */
  public void init() {
    // set background to white
    setBackground(Color.white);

    // lay out components with GridBagLayout
    GridBagLayout gridbag = new GridBagLayout();
    setLayout(gridbag);

    // construct GUI components
    addressField = new TextField(DEFAULT_HOST);
    portField = new TextField("" + DEFAULT_PORT, 4);
    connectButton = new Button("Connect");
    canvas = new Component() {
      public void paint(Graphics g) {
        if (connected) {
          // connected; paint the remote display's image
          if (image != null) {
            g.drawImage(image, 0, 0, this);
          }
        }
        else {
          // not connected; paint directions on how to connect
          g.setColor(Color.black);
          g.drawString("VisADApplet v1.0", 80, 20);
          g.drawString("To connect to a VisAD display available", 10, 50);
          g.drawString("through a SocketSlaveDisplay server, type", 10, 70);
          g.drawString("the IP address of the server into the IP", 10, 90);
          g.drawString("address field and type the port of the", 10, 110);
          g.drawString("server into the port field, then press", 10, 130);
          g.drawString("the Connect button.", 10, 150);
        }
      }
    };

    // respond to GUI component events
    addressField.addActionListener(this);
    portField.addActionListener(this);
    connectButton.addActionListener(this);
    canvas.addMouseListener(this);
    canvas.addMouseMotionListener(this);

    // lay out GUI components
    addComponent(new Label("IP address"), gridbag, 0, 0, 1, 1, 0.0, 0.0);
    addComponent(addressField, gridbag, 1, 0, 1, 1, 1.0, 0.0);
    addComponent(new Label("Port"), gridbag, 2, 0, 1, 1, 0.0, 0.0);
    addComponent(portField, gridbag, 3, 0, 1, 1, 0.0, 0.0);
    addComponent(connectButton, gridbag, 4, 0, 1, 1, 0.0, 0.0);
    addComponent(canvas, gridbag, 0, 1, 5, 1, 1.0, 1.0);
  }

  /** requests a refresh from the server */
  private void requestRefresh() {
    sendEvent(null);
  }

  /** sends the specified mouse event through the socket to the server */
  private void sendEvent(MouseEvent e) {
    // if e == null, send a dummy mouseevent with id < 0
    int id;
    long when;
    int mods;
    int x;
    int y;
    int clicks;
    boolean popup;
    if (e == null) {
      id = -1;
      when = 0;
      mods = 0;
      x = 0;
      y = 0;
      clicks = 0;
      popup = false;
    }
    else {
      id = e.getID();
      when = e.getWhen();
      mods = e.getModifiers();
      x = e.getX();
      y = e.getY();
      clicks = e.getClickCount();
      popup = e.isPopupTrigger();
    }
    if (out != null) {
      try {
        out.writeInt(id);
        out.writeLong(when);
        out.writeInt(mods);
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(clicks);
        out.writeBoolean(popup);
      }
      catch (SocketException exc) {
        // problem communicating with server; it has probably disconnected
        connected = false;
        repaint();
      }
      catch (IOException exc) { }
    }
  }

  /** fired when a button is pressed or enter is pressed in a text box */
  public synchronized void actionPerformed(ActionEvent e) {
    // highlight the connect button to indicate that connection is happening
    connectButton.requestFocus();

    // obtain the new IP address and port
    String address = addressField.getText();
    int port = this.port;
    try {
      port = Integer.parseInt(portField.getText());
    }
    catch (NumberFormatException exc) {
    }
    portField.setText("" + port);

    // connect to the new IP address and port
    Socket sock = null;
    try {
      sock = new Socket(address, port);
    }
    catch (UnknownHostException exc) {
      addressField.setText("" + this.address);
    }
    catch (IOException exc) { }
    if (sock == null) return;

    if (connected) {
      // kill the old socket
      try {
        socket.close();
      }
      catch (IOException exc) { }

      // wait for old communication thread to die
      connected = false;
      while (commThread.isAlive()) {
        try {
          Thread.sleep(100);
        }
        catch (InterruptedException exc) { }
      }
    }

    // finish setting up new socket
    socket = sock;
    connected = true;

    // set a new thread to manage communication with the server
    final Applet applet = this;
    commThread = new Thread(new Runnable() {
      public void run() {
        try {
          InputStream socketIn = socket.getInputStream();
          OutputStream socketOut = socket.getOutputStream();
          DataInputStream in = new DataInputStream(socketIn);
          out = new DataOutputStream(socketOut);

          // request a refresh so that the server sends the image
          requestRefresh();

          // loop until the socket gets closed
          while (connected) {
            // read the latest display image
            int w = in.readInt();
            if (w == 0) continue;
            int h = in.readInt();
            int len = in.readInt();
            byte[] pixels = new byte[len];
            int p = 0;
            while (p < len) p += in.read(pixels, p, len - p);
            int[] pix = Convert.bytesToInt(pixels);

            // decode pixels from RLE
            int[] decoded = Convert.decodeRLE(pix);

            // reconstruct the image locally
            image = createImage(new MemoryImageSource(w, h, decoded, 0, w));

            // redraw the applet's display canvas
            Graphics g = canvas.getGraphics();
            if (g != null) {
              g.drawImage(image, 0, 0, applet);
              g.dispose();
            }
          }
        }
        catch (SocketException exc) {
          // problem communicating with server; it has probably disconnected
          connected = false;
          applet.repaint();
        }
        catch (IOException exc) { }
      }
    });
    commThread.start();
  }

  public void mouseClicked(MouseEvent e) {
    // This event currently generates a "type not recognized" error
    // sendEvent(e);
  }

  public void mouseEntered(MouseEvent e) {
    sendEvent(e);
  }

  public void mouseExited(MouseEvent e) {
    sendEvent(e);
  }

  public void mousePressed(MouseEvent e) {
    sendEvent(e);
  }

  public void mouseReleased(MouseEvent e) {
    sendEvent(e);
  }

  public void mouseDragged(MouseEvent e) {
    sendEvent(e);
  }

  public void mouseMoved(MouseEvent e) {
    // This event currently generates a "type not recognized" error
    // sendEvent(e);
  }

}

