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
import java.util.*;

/**
 * An applet for connecting to a VisAD display available through a
 * SocketSlaveDisplay server. The applet functions completely independently
 * of VisAD, using only JDK 1.1 code, so that it can be imbedded
 * within a web page for use in a web browser.
 */
public class VisADApplet extends Applet
  implements ActionListener, MouseListener, MouseMotionListener, WidgetListener
{

  /**
   * Debugging flag.
   */
  private static final boolean DEBUG = false;

  /**
   * The default host name for the SocketSlaveDisplay server.
   */
  private static final String DEFAULT_HOST = "localhost";

  /**
   * The default port at which to connect.
   */
  private static final int DEFAULT_PORT = 4567;

  /**
   * Whether the applet client is connected to a server.
   */
  private boolean connected = false;

  /**
   * IP address of the server.
   */
  private String address = "";

  /**
   * Port of the server.
   */
  private int port = DEFAULT_PORT;

  /**
   * Currently connected socket.
   */
  private Socket socket = null;

  /**
   * Output stream of currently connected socket.
   */
  private DataOutputStream out = null;

  /**
   * Latest image from the server's display.
   */
  private Image image = null;

  /**
   * Text field for typing in IP address of server.
   */
  private TextField addressField;

  /**
   * Text field for typing in port of server.
   */
  private TextField portField;

  /**
   * Button for connecting to the specified IP address and port.
   */
  private Button connectButton;

  /**
   * Canvas for painting remote display image from the server.
   */
  private Component canvas;

  /**
   * Frame for display widgets.
   */
  private Frame frame;

  /**
   * Layout manager for widget frame.
   */
  private GridBagLayout widgetLayout;

  /**
   * GridBagConstraints object for use in widget layout.
   */
  private GridBagConstraints constraints;

  /**
   * Thread for communicating with server.
   */
  private Thread commThread = null;

  /**
   * Hashtable for storing widgets.
   */
  private Hashtable widgets = new Hashtable();

  /**
   * Adds a component to the applet with the specified constraints.
   */
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

  /**
   * Adds a widget to the control panel.
   */
  private void addWidget(Widget widget, String hash) {
    // add widget to hashtable
    widgets.put(hash, widget);

    // add widget to control panel
    if (constraints.gridy > 0) {
      Divider divider = new Divider();
      widgetLayout.setConstraints(divider, constraints);
      constraints.gridy++;
      frame.add(divider);
    }
    widgetLayout.setConstraints(widget, constraints);
    constraints.gridy++;
    frame.add(widget);
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Removes all widgets from the control panel.
   */
  private synchronized void removeAllWidgets() {
    // clear hashtable
    widgets = new Hashtable();

    // clear control panel
    constraints.gridy = 0;
    frame.setVisible(false);
    frame.removeAll();
  }

  /**
   * Initializes the applet and lays out its GUI.
   */
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
          g.drawString("VisADApplet", 80, 20);
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

    // construct widget frame
    frame = new Frame("Controls");
    widgetLayout = new GridBagLayout();
    frame.setLayout(widgetLayout);
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.BOTH;
    frame.setBackground(Widget.PALE_GRAY);
  }

  /**
   * Close the current server connection
   */
  public void disconnect() {
    if (connected) {
      connected = false;
      // remove all widget listeners
      for (int i=0; i<frame.getComponentCount(); i++) {
        Component c = frame.getComponent(i);
        if (c instanceof Widget) {
          Widget w = (Widget) frame.getComponent(i);
          w.removeWidgetListener(this);
        }
      }
      // remove all widgets from control panel
      removeAllWidgets();
      repaint();
    }
  }

  /**
   * Requests a refresh from the server.
   */
  private void requestRefresh() {
    if (out != null) {
      try {
        out.writeInt(0); // 0 = refresh
      }
      catch (SocketException exc) {
        // problem communicating with the server; it has probably disconnected
        disconnect();
      }
      catch (IOException exc) {
        // problem communicating with server; it has probably disconnected
        disconnect();
      }
    }
  }

  /**
   * Sends the specified mouse event through the socket to the server.
   */
  private void sendEvent(MouseEvent e) {
    int id = e.getID();
    long when = e.getWhen();
    int mods = e.getModifiers();
    int x = e.getX();
    int y = e.getY();
    int clicks = e.getClickCount();
    boolean popup = e.isPopupTrigger();
    if (out != null) {
      try {
        out.writeInt(1); // 1 = MouseEvent
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
        disconnect();
      }
      catch (IOException exc) {
        // problem communicating with server; it has probably disconnected
        disconnect();
      }
    }
  }

  /**
   * Sends the specified message through the socket to the server.
   */
  private void sendMessage(String message) {
    if (out != null) {
      try {
        out.writeInt(2); // 2 = message
        out.writeInt(message.length());
        out.writeChars(message);
      }
      catch (SocketException exc) {
        // problem communicating with server; it has probably disconnected
        disconnect();
      }
      catch (IOException exc) {
        // problem communicating with server; it has probably disconnected
        disconnect();
      }
    }
  }

  /**
   * Fired when a button is pressed or enter is pressed in a text box.
   */
  public synchronized void actionPerformed(ActionEvent e) {
    // highlight the connect button to indicate that connection is happening
    connectButton.requestFocus();

    // obtain the new IP address and port
    String address = addressField.getText();
    int port = this.port;
    try {
      port = Integer.parseInt(portField.getText());
    }
    catch (NumberFormatException exc) { }
    portField.setText("" + port);

    // connect to the new IP address and port
    Socket sock = null;
    try {
      sock = new Socket(address, port);
    }
    catch (UnknownHostException exc) {
      addressField.setText("" + this.address);
    }
    catch (IOException exc) {
      if (DEBUG) exc.printStackTrace();
    }
    if (sock == null) return;

    if (connected) {
      // kill the old socket
      try {
        socket.close();
      }
      catch (IOException exc) {
        if (DEBUG) exc.printStackTrace();
      }

      // wait for old communication thread to die
      disconnect();
      while (commThread.isAlive()) {
        try {
          Thread.sleep(100);
        }
        catch (InterruptedException exc) {
          if (DEBUG) exc.printStackTrace();
        }
      }
    }

    // finish setting up new socket
    socket = sock;
    connected = true;

    // set a new thread to manage communication with the server
    final VisADApplet applet = this;
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
            if (w == -1) {
              // server is sending a message
              int len = in.readInt();
              char[] c = new char[len];
              for (int i=0; i<len; i++) c[i] = in.readChar();
              String message = new String(c);

              // apply changes to relevant widget

              // Parse message, which should be of the form:
              //   class\nnumber\nstate
              // where class is the class name of the control that has
              // changed, number is the index into that class name's
              // control list, and state is the save string
              // corresponding to the control's new state.
              StringTokenizer st = new StringTokenizer(message, "\n");
              String controlClass = st.nextToken();
              int index = Convert.getInt(st.nextToken());
              String save = st.nextToken();

              // parse class name
              int dotIndex = controlClass.lastIndexOf(".");
              int ctrlIndex = controlClass.lastIndexOf("Control");
              String widgetName =
                controlClass.substring(dotIndex + 1, ctrlIndex);

              // handle special cases
              if (widgetName.equals("GraphicsMode")) widgetName = "GMC";

              // construct widget class name and hash table key
              String widgetClass = "visad.browser." + widgetName + "Widget";
              String widgetHash = widgetName + index;

              // get widget from hashtable
              Widget widget = (Widget) widgets.get(widgetHash);
              if (widget == null) {
                // widget not found; instantiate widget of the proper type
                try {
                  widget = (Widget) Class.forName(widgetClass).newInstance();
                  widget.addWidgetListener(applet);
                }
                catch (ClassNotFoundException exc) {
                  if (DEBUG) {
                    // widget class does not exist
                    System.err.println("Warning: ignoring status of " +
                      "unknown " + widgetName + " widget.");
                  }
                }
                catch (InstantiationException exc) {
                  if (DEBUG) {
                    // widget class cannot be instantiated
                    System.err.println("Warning: ignoring status of " +
                      "invalid " + widgetName + "widget.");
                  }
                }
                catch (IllegalAccessException exc) {
                  if (DEBUG) {
                    // widget class constructor cannot be accessed
                    System.err.println("Warning: ignoring status of " +
                      "restricted " + widgetName + "widget.");
                  }
                }
                if (widget != null) addWidget(widget, widgetHash);
              }

              if (widget != null) {
                // set widget's state to match save string from message
                widget.setSaveString(save);
              }
            }
            else {
              // server is sending an image
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
        }
        catch (SocketException exc) {
          // problem communicating with server; it has probably disconnected
          applet.disconnect();
        }
        catch (IOException exc) {
          // problem communicating with server; it has probably disconnected
          applet.disconnect();
        }
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

  public void widgetChanged(WidgetEvent e) {
    Widget widget = e.getWidget();
    String widgetClass = widget.getClass().getName();
    // parse class name
    int dotIndex = widgetClass.lastIndexOf(".");
    int wdgtIndex = widgetClass.lastIndexOf("Widget");
    String controlName = widgetClass.substring(dotIndex + 1, wdgtIndex);

    // handle special cases
    if (controlName.equals("GMC")) controlName = "GraphicsMode";

    // construct control class name
    String controlClass = "visad." + controlName + "Control";
    String save = widget.getSaveString();

    // determine widget number
    int i = 0;
    int index = -1;
    Widget w;
    do {
      w = (Widget) widgets.get(controlName + i);
      if (w == widget) {
        index = i;
        break;
      }
      i++;
    }
    while (w != null);

    // send message to server
    String message = controlClass + "\n" + index + "\n" + save;
    sendMessage(message);
  }

}
