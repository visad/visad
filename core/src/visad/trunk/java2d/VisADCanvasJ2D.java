//
// VisADCanvasJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

package visad.java2d;
 
import visad.*;
import visad.util.Delay;
 
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import javax.swing.*;
import java.rmi.RemoteException;
 
import java.util.*;

/**
   VisADCanvasJ2D is the VisAD "Canvas" for Java2D.  But
   not really a Canvas, since Canvas is heavyweight.<P>
*/

public class VisADCanvasJ2D extends JPanel
       implements Runnable {

  private DisplayRendererJ2D displayRenderer;
  private DisplayImplJ2D display;
  private Component component;
  Dimension prefSize = new Dimension(0, 0);

  // parent nodes of all data depictions
  private VisADGroup direct = null;
  private VisADGroup non_direct = null;
  // Shape to clip against, if any
  private Rectangle2D.Float clip_rectangle = null;

  private transient Thread renderThread;

  private BufferedImage[] images; // animation sequence
  private boolean[] valid_images;
  private int width, height; // size of images
  private int length; // length of images & valid_images
  private AffineTransform tgeometry; // transform for current display
  private Image aux_image;

  boolean captureFlag = false;
  BufferedImage captureImage = null;

  MouseHelper helper;

  // wake up flag for renderTrigger
  boolean wakeup = false;

  VisADCanvasJ2D(DisplayRendererJ2D renderer, Component c) {
    displayRenderer = renderer;
    display = (DisplayImplJ2D) renderer.getDisplay();
    component = c;

    width = getSize().width;
    height = getSize().height;
    length = 1;
    images = new BufferedImage[] {(BufferedImage) createImage(width, height)};
    aux_image = createImage(width, height);
    valid_images = new boolean[] {false};
    tgeometry = null;

    ComponentListener cl = new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        createImages(-1);
      }
    };
    addComponentListener(cl);

    setBackground(Color.black);
    setForeground(Color.white);

    new Delay();

    renderThread = new Thread(this);
    renderThread.start();
  }

  /** constructor for offscreen rendering */
  VisADCanvasJ2D(DisplayRendererJ2D renderer, int w, int h) {
    displayRenderer = renderer;
    display = (DisplayImplJ2D) renderer.getDisplay();
    component = null;

    width = w;
    height = h;
    length = 1;
    images = new BufferedImage[]
      {new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)};
    aux_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    valid_images = new boolean[] {false};
    tgeometry = null;

    setBackground(Color.black);
    setForeground(Color.white);

    renderThread = new Thread(this);
    renderThread.start();
  }

  public void setBackgroundColor(float r, float g, float b) {
    setBackground(new Color(r, g, b));
  }

  void setDirect(VisADGroup d, VisADGroup nd) {
    direct = d;
    non_direct = nd;
  }

  void setClip(float xlow, float xhi, float ylow, float yhi) {
    if (xhi > xlow && yhi > ylow) {
      clip_rectangle =
        new Rectangle2D.Float(xlow, ylow, xhi-xlow, yhi-ylow);
    }
  }

  public void addMouseBehavior(MouseBehaviorJ2D mouse) {
    helper = mouse.getMouseHelper();

    MouseListener ml = new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        helper.processEvent(e);
      }
      public void mouseExited(MouseEvent e) {
        helper.processEvent(e);
      }
      public void mousePressed(MouseEvent e) {
        helper.processEvent(e);
      }
      public void mouseReleased(MouseEvent e) {
        helper.processEvent(e);
      }
    };
    addMouseListener(ml);

    MouseMotionListener mml = new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        helper.processEvent(e);
      }
    };
    addMouseMotionListener(mml);
  }

  public void createImages(int len) {
    synchronized (images) {
      length = (len < 0) ? images.length : len;
      if (component != null) {
        width = getSize().width;
        height = getSize().height;
      }
      if (width <= 0) width = 1;
      if (height <= 0) height = 1;
      images = new BufferedImage[length];
      valid_images = new boolean[length];
      for (int i=0; i<length; i++) {
        if (component != null) {
          images[i] = (BufferedImage) createImage(width, height);
        }
        else {
          images[i] =
            new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        valid_images[i] = false;
      }
      if (component != null) {
        aux_image = createImage(width, height);
      }
      else {
        aux_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      }
/*
System.out.println("VisADCanvasJ2D.createImages: len = " + len +
                   " length = " + length + " width, height = " +
                   width + " " + height);
*/
    }
    renderTrigger();
  }

  public void scratchImages() {
    synchronized (images) {
      for (int i=0; i<length; i++) valid_images[i] = false;
    }
    renderTrigger();
  }

  /** trigger render to screen */
  public void renderTrigger() {
    synchronized (this) {
      wakeup = true;
      notify();
    }
  }

  public void stop() {
    renderThread = null;
  }
 
  public void run() {
    Thread me = Thread.currentThread();
    while (renderThread == me) {
      try {
        synchronized (this) {
          if (!wakeup) {
/* WLH 28 Aug 98
            wait(2000);
*/
            wait();
          }
        }
      }
      catch(InterruptedException e) {
        // note notify generates a normal return from wait rather
        // than an Exception - control doesn't normally come here
      }
      synchronized (this) {
        wakeup = false;
      }
      if (component != null) {
        Graphics g = getGraphics();
        if (g != null) {
          paintComponent(g);
          g.dispose();
        }
      }
      else {
        paintComponent(null);
      }
    }
  }

  public void paintComponent(Graphics g) {
    AffineTransform tsave = null;
    BufferedImage image = null;
    boolean valid = false;
    int w = 0, h = 0;
    int current_image = 0;
    AnimationControlJ2D animate_control = null;
    try {
      animate_control = (AnimationControlJ2D)
        display.getControl(AnimationControlJ2D.class);
      if (animate_control != null) {
        animate_control.setNoTick(true);
        current_image = animate_control.getCurrent();
      }
    }
    catch (Exception e) {
      if (animate_control != null) animate_control.setNoTick(false);
    }
/*
System.out.println("VisADCanvasJ2D.paint: current = " + current_image +
                   " (animate_control == null) = " + (animate_control == null));
*/
    synchronized (images) {
      if (0 <= current_image && current_image < length) {
        image = images[current_image];
        valid = valid_images[current_image];
        w = width;
        h = height;
        tsave = (tgeometry == null) ? null : new AffineTransform(tgeometry);
        if (image != null && !valid) {
          valid_images[current_image] = true;
        }
      }
    }
/*
System.out.println("VisADCanvasJ2D.paint: current_image = " + current_image +
                   " length = " + length + " w, h = " + w + " " + h +
                   " valid = " + valid + " image != null " + (image != null));
*/
    if (image != null) {
      if (!valid) {
        VisADGroup root = displayRenderer.getRoot();
        AffineTransform trans = displayRenderer.getTrans();
        Graphics ggg = image.createGraphics(); // ordinary Graphics for fast lines
        Graphics2D g2 = image.createGraphics(); // Graphics2D for the fancy stuff
// System.out.println("(g2 == null) = " + (g2 == null));
        g2.setBackground(getBackground());
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_SPEED);
        g2.clearRect(0, 0, width, height);

        // render into image;
        synchronized (images) {
          tgeometry = new AffineTransform();
          tgeometry.setToTranslation(0.5 * w, 0.5 * h);
          AffineTransform s1 = new AffineTransform();
          int wh = (w < h) ? w : h;
          s1.setToScale(0.33 * wh, 0.33 * wh);
          tgeometry.concatenate(s1);
          tgeometry.concatenate(trans);
          tsave = new AffineTransform(tgeometry);
          g2.setTransform(tgeometry);
        }
        try {
          if (animate_control != null) animate_control.init();
          render(g2, ggg, root, 0, null);
          render(g2, ggg, root, 1, null);
          // draw Animation string in upper right corner of screen
          String[] animation_string = displayRenderer.getAnimationString();
          if (animation_string[0] != null) {
/*
System.out.println("VisADCanvasJ2D.paint: " + animation_string[0] +
                   " " + animation_string[1]);
*/
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(new Font("Times New Roman", Font.PLAIN, 12));
            g2.setTransform(new AffineTransform());
            int nchars = animation_string[0].length();
            if (nchars < 12) nchars = 12;
            // float x = w - 9 * nchars; WLH 30 April 99
            float x = w - 7 * nchars; 
            float y = h - 12;
            g2.drawString(animation_string[0], x, y);
            g2.drawString(animation_string[1], x, y+10);
          }
        }
        catch (VisADException e) {
        }
        g2.dispose();
        ggg.dispose();
      } // end if (!valid)
      if (tsave == null || !displayRenderer.anyCursorStringVector()) {
        if (g != null) g.drawImage(image, 0, 0, this);
        if (captureFlag) {
// System.out.println("image capture " + width + " " + height);
          captureFlag = false;
          if (component != null) {
            captureImage = (BufferedImage) createImage(width, height);
          }
          else {
            captureImage =
              new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
          }
          Graphics gc = captureImage.getGraphics();
          gc.drawImage(image, 0, 0, this);
          gc.dispose();
          displayRenderer.notifyCapture();
// System.out.println("image capture end");
        }
      }
      else {
        Image aux_copy = null;
        synchronized (images) {
          aux_copy = aux_image;
        }
        Graphics ga = aux_copy.getGraphics();
        ga.drawImage(image, 0, 0, this);
        displayRenderer.drawCursorStringVector(ga, tsave, w, h);
        ga.dispose();
        if (g != null) g.drawImage(aux_copy, 0, 0, this);
        if (captureFlag) {
// System.out.println("aux_copy capture " + width + " " + height);
          captureFlag = false;
          if (component != null) {
            captureImage = (BufferedImage) createImage(width, height);
          }
          else {
            captureImage =
              new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
          }
          Graphics gc = captureImage.getGraphics();
          gc.drawImage(aux_copy, 0, 0, this);
          gc.dispose();
          displayRenderer.notifyCapture();
// System.out.println("aux_copy capture end");
        }
      }
      // WLH 15 March 99
      try {
        display.notifyListeners(DisplayEvent.FRAME_DONE, 0, 0);
      }
      catch (VisADException e) {}
      catch (RemoteException e) {}
    } // end if (image != null)
    if (animate_control != null) animate_control.setNoTick(false);
  }

  private void render(Graphics2D g2, Graphics ggg,
                      VisADSceneGraphObject scene, int pass,
                      Rectangle2D.Float clip)
          throws VisADException {
    if (scene instanceof VisADSwitch) {
      VisADSceneGraphObject child =
        ((VisADSwitch) scene).getSelectedChild();
      if (child != null) render(g2, ggg, child, pass, clip);
    }
    else if (scene instanceof VisADGroup) {
      if (clip_rectangle != null &&
          (scene.equals(direct) || scene.equals(non_direct))) {
        clip = clip_rectangle;
      }
      Vector children = ((VisADGroup) scene).getChildren();
      for (int i=children.size()-1; i>=0; i--) {
        VisADSceneGraphObject child =
          (VisADSceneGraphObject) children.elementAt(i);
        if (child != null) render(g2, ggg, child, pass, clip);
      }
    }
    else { // scene instanceof VisADAppearance
      g2.setClip(clip);
      VisADAppearance appearance = (VisADAppearance) scene;
      VisADGeometryArray array = appearance.array;
      if (array == null) return;
      BufferedImage image = (BufferedImage) appearance.image;
      AffineTransform tg = g2.getTransform();
      if (image != null) {
        if (pass != 0) return; // non-lines on first pass
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_OFF);
        if (!(array instanceof VisADQuadArray)) {
          throw new VisADError("VisADCanvasJ2D.render: array must be quad");
        }
        float x00 = array.coordinates[0];
        float y00 = array.coordinates[1];
        float xw0 = array.coordinates[3];
        float yw0 = array.coordinates[4];
        float xwh = array.coordinates[6];
        float ywh = array.coordinates[7];
        float x0h = array.coordinates[9];
        float y0h = array.coordinates[10];
        int width = image.getWidth();
        int height = image.getHeight();

/*
now:
  x00 = m00 * 0 + m01 * 0 + m02
  y00 = m10 * 0 + m11 * 0 + m12

  xw0 = m00 * width + m01 * 0 + m02
  yw0 = m10 * width + m11 * 0 + m12

  xwh = m00 * width + m01 * height + m02
  ywh = m10 * width + m11 * height + m12

  x0h = m00 * 0 + m01 * height + m02
  y0h = m10 * 0 + m11 * height + m12
so:
*/
        float m02 = x00;
        float m12 = y00;
        float m00 = (xw0 - x00) / width;
        float m10 = (yw0 - y00) / width;
        float m01 = (x0h - x00) / height;
        float m11 = (y0h - y00) / height;
        float xerr = xwh - (m00 * width + m01 * height + m02);
        float yerr = ywh - (m10 * width + m11 * height + m12);

        AffineTransform timage = new AffineTransform(m00, m10, m01, m11, m02, m12);
        g2.transform(timage); // concatenate timage onto tg
        g2.drawImage(image, 0, 0, this);
        g2.setTransform(tg); // restore tg
      }
      else { // image == null
        if (array instanceof VisADPointArray ||
            array instanceof VisADLineArray ||
            array instanceof VisADLineStripArray) {
          if (pass != 1) return; // lines on second pass
        }
        else {
          if (pass != 0) return; // non-lines on first pass
        }
        int count = array.vertexCount;
        if (count == 0) return;
        float[] coordinates = array.coordinates;
        byte[] colors = array.colors;
        if (colors == null) {
          if (appearance.color_flag) {
            float red = (float) Math.max(Math.min(appearance.red, 1.0f), 0.0f);
            float green = (float) Math.max(Math.min(appearance.green, 1.0f), 0.0f);
            float blue = (float) Math.max(Math.min(appearance.blue, 1.0f), 0.0f);
            g2.setColor(new Color(red, green, blue));
          }
          else {
            g2.setColor(new Color(1.0f, 1.0f, 1.0f));
          }
        }
        else {
/* MEM_WLH
          for (int i=0; i<colors.length; i++) {
            colors[i] = (float) Math.max(Math.min(colors[i], 1.0f), 0.0f);
          }
*/
        }
        if (array instanceof VisADPointArray ||
            array instanceof VisADLineArray ||
            array instanceof VisADLineStripArray) {
/* WLH 21 Aug 98
          GraphicsModeControl mode = display.getGraphicsModeControl();
          double dsize = (array instanceof VisADPointArray) ?
                           mode.getPointSize() :
                           mode.getLineWidth();
*/
          float fsize = (array instanceof VisADPointArray) ?
                           appearance.pointSize :
                           appearance.lineWidth;
          double dsize = fsize;
/* WLH 19 March 99
          double dsize = (array instanceof VisADPointArray) ?
                           appearance.pointSize :
                           appearance.lineWidth;
*/
          if (dsize < 1.05) dsize = 1.05; // hack for Java2D problem
          double[] pts = {0.0, 0.0, 0.0, dsize, dsize, 0.0};
          double[] newpts = new double[6];
          double xx = 0.0, yy = 0.0;
          try {
            tg.inverseTransform(pts, 0, newpts, 0, 3);
            xx = (newpts[2] - newpts[0]) * (newpts[2] - newpts[0]) +
                 (newpts[3] - newpts[1]) * (newpts[3] - newpts[1]);
            yy = (newpts[4] - newpts[0]) * (newpts[4] - newpts[0]) +
                 (newpts[5] - newpts[1]) * (newpts[5] - newpts[1]);
          }
          catch (NoninvertibleTransformException e) {
            xx = 1.05;
            yy = 1.05;
          }
          float size = (float) (0.5 * (Math.sqrt(xx) + Math.sqrt(yy)));
          g2.setStroke(new BasicStroke(size));
/*
System.out.println("dsize = " + dsize + " size = " + size + " xx, yy = " +
                   xx + " " + yy +
                   (array instanceof VisADPointArray ? " point" : " line"));
*/
          if (array instanceof VisADPointArray) {
/* WLH 19 March 99 */
            if (Math.abs(fsize - 1.0f) < 0.1f) {
              drawAppearance(ggg, appearance, tg, clip);
            }
            else {
              g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    
                                  RenderingHints.VALUE_ANTIALIAS_OFF);
              if (colors == null) {
                for (int i=0; i<3*count; i += 3) {
                  if (coordinates[i] == coordinates[i] &&
                      coordinates[i+1] == coordinates[i+1]) {
                    g2.fill(new Rectangle2D.Float(coordinates[i], coordinates[i+1],
                                                  size, size));
                  }
                }
              }
              else { // colors != null
                int j = 0;
                int jinc = (colors.length == coordinates.length) ? 3 : 4;
                for (int i=0; i<3*count; i += 3) {
                  if (coordinates[i] == coordinates[i] &&
                      coordinates[i+1] == coordinates[i+1]) {
                    g2.setColor(new Color(
                      ((colors[j] < 0) ? (((int) colors[j]) + 256) :
                                         ((int) colors[j]) ),
                      ((colors[j+1] < 0) ? (((int) colors[j+1]) + 256) :
                                         ((int) colors[j+1]) ),
                      ((colors[j+2] < 0) ? (((int) colors[j+2]) + 256) :
                                         ((int) colors[j+2]) ) ));
/* MEM_WLH
                    g2.setColor(new Color(ShadowType.byteToFloat(colors[j]),
                                          ShadowType.byteToFloat(colors[j+1]),
                                          ShadowType.byteToFloat(colors[j+2])));
*/
                    g2.fill(new Rectangle2D.Float(coordinates[i], coordinates[i+1],
                                                  size, size));
                  }
                  j += jinc;
                }
              }
            }
          }
          else if (array instanceof VisADLineArray) {
/* WLH 19 March 99 */
            if (Math.abs(fsize - 1.0f) < 0.1f) {
              drawAppearance(ggg, appearance, tg, clip);
            }
            else {
              g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    
                                  RenderingHints.VALUE_ANTIALIAS_ON);
              if (colors == null) {
                for (int i=0; i<3*count; i += 6) {
                  g2.draw(new Line2D.Float(coordinates[i], coordinates[i+1],
                                           coordinates[i+3], coordinates[i+4]));
                }
              }
              else { // colors != null
                int j = 0;
                int jinc = (colors.length == coordinates.length) ? 3 : 4;
                for (int i=0; i<3*count; i += 6) {
                  g2.setColor(new Color(
                    (((colors[j] < 0) ? (((int) colors[j]) + 256) :
                                        ((int) colors[j]) ) +
                     ((colors[j+jinc] < 0) ? (((int) colors[j+jinc]) + 256) :
                                        ((int) colors[j+jinc]) ) ) / 2,
                    (((colors[j+1] < 0) ? (((int) colors[j+1]) + 256) :
                                        ((int) colors[j+1]) ) +
                     ((colors[j+jinc+1] < 0) ? (((int) colors[j+jinc+1]) + 256) :
                                        ((int) colors[j+jinc+1]) ) ) / 2,
                    (((colors[j+2] < 0) ? (((int) colors[j+2]) + 256) :
                                        ((int) colors[j+2]) ) +
                     ((colors[j+jinc+2] < 0) ? (((int) colors[j+jinc+2]) + 256) :
                                        ((int) colors[j+jinc+2]) ) ) / 2 ));
/* MEM_WLH
                  g2.setColor(new Color(
                    0.5f * (ShadowType.byteToFloat(colors[j]) +
                            ShadowType.byteToFloat(colors[j+jinc])),
                    0.5f * (ShadowType.byteToFloat(colors[j+1]) +
                            ShadowType.byteToFloat(colors[j+jinc+1])),
                    0.5f * (ShadowType.byteToFloat(colors[j+2]) +
                            ShadowType.byteToFloat(colors[j+jinc+2]))));
*/
                  j += 2 * jinc;
                  g2.draw(new Line2D.Float(coordinates[i], coordinates[i+1],
                                           coordinates[i+3], coordinates[i+4]));
                }
              }
            }
          }
          else { // (array instanceof VisADLineStripArray)
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int[] stripVertexCounts =
               ((VisADLineStripArray) array).stripVertexCounts;
            int base = 0;
            int basec = 0;
            int jinc = 0;
            if (colors != null) {
              jinc = (colors.length == coordinates.length) ? 3 : 4;
            }
            for (int strip=0; strip<stripVertexCounts.length; strip++) {
              count = stripVertexCounts[strip];

              float lastx = coordinates[base];
              float lasty = coordinates[base+1];
              int lastr = 0, lastg = 0, lastb = 0;
              int thisr, thisg, thisb;
              if (colors != null) {
                lastr = (colors[basec] < 0) ? (((int) colors[basec]) + 256) :
                                             ((int) colors[basec]);
                lastg = (colors[basec+1] < 0) ? (((int) colors[basec+1]) + 256) :
                                               ((int) colors[basec+1]);
                lastb = (colors[basec+2] < 0) ? (((int) colors[basec+2]) + 256) :
                                               ((int) colors[basec+2]);
/* MEM_WLH
                lastr = ShadowType.byteToFloat(colors[basec]);
                lastg = ShadowType.byteToFloat(colors[basec+1]);
                lastb = ShadowType.byteToFloat(colors[basec+2]);
*/
              }
              if (colors == null) {
                for (int i=3; i<3*count; i += 3) {
                  g2.draw(new Line2D.Float(lastx, lasty,
                                           coordinates[base+i],
                                           coordinates[base+i+1]));
                  lastx = coordinates[base+i];
                  lasty = coordinates[base+i+1];
                }
              }
              else {
                int j = jinc;
                for (int i=3; i<3*count; i += 3) {
                  thisr = (colors[basec+j] < 0) ? (((int) colors[basec+j]) + 256) :
                                               ((int) colors[basec+j]);
                  thisg = (colors[basec+j+1] < 0) ? (((int) colors[basec+j+1]) + 256) :
                                                 ((int) colors[basec+j+1]);
                  thisb = (colors[basec+j+2] < 0) ? (((int) colors[basec+j+2]) + 256) :
                                                 ((int) colors[basec+j+2]);
/* MEM_WLH
                  thisr = ShadowType.byteToFloat(colors[basec+j]);
                  thisg = ShadowType.byteToFloat(colors[basec+j+1]);
                  thisb = ShadowType.byteToFloat(colors[basec+j+2]);
*/
                  g2.setColor(new Color((lastr + thisr) / 2,
                                        (lastg + thisg) / 2,
                                        (lastb + thisb) / 2));
                  lastr = thisr;
                  lastg = thisg;
                  lastb = thisb;
                  j += jinc;
                  g2.draw(new Line2D.Float(lastx, lasty,
                                           coordinates[base+i],
                                           coordinates[base+i+1]));
                  lastx = coordinates[base+i];
                  lasty = coordinates[base+i+1];
                }
              }
              base += 3 * count;
              basec += jinc * count;
            } // end for (int strip=0; strip<stripVertexCounts.length; strip++)
          } // end if (array instanceof VisADLineStripArray)
        }
        else if (array instanceof VisADTriangleArray) {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    
                              RenderingHints.VALUE_ANTIALIAS_OFF);
          if (colors == null) { 
            for (int i=0; i<3*count; i += 9) { 
              GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
              path.moveTo(coordinates[i], coordinates[i+1]);
              path.lineTo(coordinates[i+3], coordinates[i+4]);
              path.lineTo(coordinates[i+6], coordinates[i+7]);
              path.closePath();
              g2.fill(path);
            }
          }
          else { // colors != null
            int j = 0; 
            int jinc = (colors.length == coordinates.length) ? 3 : 4;
            for (int i=0; i<3*count; i += 9) {
              g2.setColor(new Color(
                (((colors[j] < 0) ? (((int) colors[j]) + 256) :
                                    ((int) colors[j]) ) +
                 ((colors[j+jinc] < 0) ? (((int) colors[j+jinc]) + 256) :
                                    ((int) colors[j+jinc]) ) +
                 ((colors[j+2*jinc] < 0) ? (((int) colors[j+2*jinc]) + 256) :
                                    ((int) colors[j+2*jinc]) ) ) / 3,
                (((colors[j+1] < 0) ? (((int) colors[j+1]) + 256) :
                                    ((int) colors[j+1]) ) +
                 ((colors[j+jinc+1] < 0) ? (((int) colors[j+jinc+1]) + 256) :
                                    ((int) colors[j+jinc+1]) ) +
                 ((colors[j+2*jinc+1] < 0) ? (((int) colors[j+2*jinc+1]) + 256) :
                                    ((int) colors[j+2*jinc+1]) ) ) / 3,
                (((colors[j+2] < 0) ? (((int) colors[j+2]) + 256) :
                                    ((int) colors[j+2]) ) +
                 ((colors[j+jinc+2] < 0) ? (((int) colors[j+jinc+2]) + 256) :
                                    ((int) colors[j+jinc+2]) ) +
                 ((colors[j+2*jinc+2] < 0) ? (((int) colors[j+2*jinc+2]) + 256) :
                                    ((int) colors[j+2*jinc+2]) ) ) / 3 ));
/* MEM_WLH
              g2.setColor(new Color(
                0.33f * (ShadowType.byteToFloat(colors[j]) +
                         ShadowType.byteToFloat(colors[j+jinc]) +
                         ShadowType.byteToFloat(colors[j+2*jinc])),
                0.33f * (ShadowType.byteToFloat(colors[j+1]) +
                         ShadowType.byteToFloat(colors[j+jinc+1]) +
                         ShadowType.byteToFloat(colors[j+2*jinc+1])),
                0.33f * (ShadowType.byteToFloat(colors[j+2]) +
                         ShadowType.byteToFloat(colors[j+jinc+2]) +
                         ShadowType.byteToFloat(colors[j+2*jinc+2]))));
*/
              j += 3 * jinc;
              GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
              path.moveTo(coordinates[i], coordinates[i+1]);
              path.lineTo(coordinates[i+3], coordinates[i+4]);
              path.lineTo(coordinates[i+6], coordinates[i+7]);
              path.closePath();
              g2.fill(path);
            }
          }
        }
        else if (array instanceof VisADQuadArray) {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    
                              RenderingHints.VALUE_ANTIALIAS_OFF);
          if (colors == null) {
            for (int i=0; i<3*count; i += 12) {
              GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
              path.moveTo(coordinates[i], coordinates[i+1]);
              path.lineTo(coordinates[i+3], coordinates[i+4]);
              path.lineTo(coordinates[i+6], coordinates[i+7]);
              path.lineTo(coordinates[i+9], coordinates[i+10]);
              path.closePath();
              g2.fill(path);
            }
          }
          else { // colors != null
            int j = 0;
            int jinc = (colors.length == coordinates.length) ? 3 : 4;
            for (int i=0; i<3*count; i += 12) {
              g2.setColor(new Color(
                (((colors[j] < 0) ? (((int) colors[j]) + 256) :
                                    ((int) colors[j]) ) +
                 ((colors[j+jinc] < 0) ? (((int) colors[j+jinc]) + 256) :
                                    ((int) colors[j+jinc]) ) +
                 ((colors[j+2*jinc] < 0) ? (((int) colors[j+2*jinc]) + 256) :
                                    ((int) colors[j+2*jinc]) ) +
                 ((colors[j+3*jinc] < 0) ? (((int) colors[j+3*jinc]) + 256) :
                                    ((int) colors[j+3*jinc]) ) ) / 4,
                (((colors[j+1] < 0) ? (((int) colors[j+1]) + 256) :
                                    ((int) colors[j+1]) ) +
                 ((colors[j+jinc+1] < 0) ? (((int) colors[j+jinc+1]) + 256) :
                                    ((int) colors[j+jinc+1]) ) +
                 ((colors[j+2*jinc+1] < 0) ? (((int) colors[j+2*jinc+1]) + 256) :
                                    ((int) colors[j+2*jinc+1]) ) +
                 ((colors[j+3*jinc+1] < 0) ? (((int) colors[j+3*jinc+1]) + 256) :
                                    ((int) colors[j+3*jinc+1]) ) ) / 4,
                (((colors[j+2] < 0) ? (((int) colors[j+2]) + 256) :
                                    ((int) colors[j+2]) ) +
                 ((colors[j+jinc+2] < 0) ? (((int) colors[j+jinc+2]) + 256) :
                                    ((int) colors[j+jinc+2]) ) +
                 ((colors[j+2*jinc+2] < 0) ? (((int) colors[j+2*jinc+2]) + 256) :
                                    ((int) colors[j+2*jinc+2]) ) +
                 ((colors[j+3*jinc+2] < 0) ? (((int) colors[j+3*jinc+2]) + 256) :
                                    ((int) colors[j+3*jinc+2]) ) ) / 4 ));
/* MEM_WLH
              g2.setColor(new Color(
                0.25f * (ShadowType.byteToFloat(colors[j]) +
                         ShadowType.byteToFloat(colors[j+jinc]) +
                         ShadowType.byteToFloat(colors[j+2*jinc]) +
                         ShadowType.byteToFloat(colors[j+3*jinc])),
                0.25f * (ShadowType.byteToFloat(colors[j+1]) +
                         ShadowType.byteToFloat(colors[j+jinc+1]) +
                         ShadowType.byteToFloat(colors[j+2*jinc+1]) +
                         ShadowType.byteToFloat(colors[j+3*jinc+1])),
                0.25f * (ShadowType.byteToFloat(colors[j+2]) +
                         ShadowType.byteToFloat(colors[j+jinc+2]) +
                         ShadowType.byteToFloat(colors[j+2*jinc+2]) +
                         ShadowType.byteToFloat(colors[j+3*jinc+2]))));
*/
              j += 4 * jinc;
              GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
              path.moveTo(coordinates[i], coordinates[i+1]);
              path.lineTo(coordinates[i+3], coordinates[i+4]);
              path.lineTo(coordinates[i+6], coordinates[i+7]);
              path.lineTo(coordinates[i+9], coordinates[i+10]);
              path.closePath();
              g2.fill(path);
            }
          }
        }
        else if (array instanceof VisADIndexedTriangleStripArray) {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    
                              RenderingHints.VALUE_ANTIALIAS_OFF);
          int[] indices = ((VisADIndexedTriangleStripArray) array).indices;
          int indexCount = ((VisADIndexedTriangleStripArray) array).indexCount;
          int[] stripVertexCounts =
             ((VisADIndexedTriangleStripArray) array).stripVertexCounts;
          int base = 0;
          for (int strip=0; strip<stripVertexCounts.length; strip++) {
            count = stripVertexCounts[strip];
            int index0 = indices[base];
            int index1 = indices[base+1];
            if (colors == null) {
              for (int i=base+2; i<base+count; i++) {
                int index2 = indices[i];
                GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                path.moveTo(coordinates[3*index0], coordinates[3*index0+1]);
                path.lineTo(coordinates[3*index1], coordinates[3*index1+1]);
                path.lineTo(coordinates[3*index2], coordinates[3*index2+1]);
                path.closePath();
                g2.fill(path);
                index0 = index1;
                index1 = index2;
              }
            }
            else { // colors != null
              int jinc = (colors.length == coordinates.length) ? 3 : 4;
              for (int i=base+2; i<base+count; i++) {
                int index2 = indices[i];
                g2.setColor(new Color(
                  (((colors[jinc*index0] < 0) ? (((int) colors[jinc*index0]) + 256) :
                                      ((int) colors[jinc*index0]) ) +
                   ((colors[jinc*index1] < 0) ? (((int) colors[jinc*index1]) + 256) :
                                      ((int) colors[jinc*index1]) ) +
                   ((colors[jinc*index2] < 0) ? (((int) colors[jinc*index2]) + 256) :
                                      ((int) colors[jinc*index2]) ) ) / 3,
                  (((colors[jinc*index0+1] < 0) ? (((int) colors[jinc*index0+1]) + 256) :
                                      ((int) colors[jinc*index0+1]) ) +
                   ((colors[jinc*index1+1] < 0) ? (((int) colors[jinc*index1+1]) + 256) :
                                      ((int) colors[jinc*index1+1]) ) +
                   ((colors[jinc*index2+1] < 0) ? (((int) colors[jinc*index2+1]) + 256) :
                                      ((int) colors[jinc*index2+1]) ) ) / 3,
                  (((colors[jinc*index0+2] < 0) ? (((int) colors[jinc*index0+2]) + 256) :
                                      ((int) colors[jinc*index0+2]) ) +
                   ((colors[jinc*index1+2] < 0) ? (((int) colors[jinc*index1+2]) + 256) :
                                      ((int) colors[jinc*index1+2]) ) +
                   ((colors[jinc*index2+2] < 0) ? (((int) colors[jinc*index2+2]) + 256) :
                                      ((int) colors[jinc*index2+2]) ) ) / 3 ));
/* MEM_WLH
                g2.setColor(new Color(
                  0.33f * (ShadowType.byteToFloat(colors[jinc*index0]) +
                           ShadowType.byteToFloat(colors[jinc*index1]) +
                           ShadowType.byteToFloat(colors[jinc*index2])),
                  0.33f * (ShadowType.byteToFloat(colors[jinc*index0+1]) +
                           ShadowType.byteToFloat(colors[jinc*index1+1]) +
                           ShadowType.byteToFloat(colors[jinc*index2+1])),
                  0.33f * (ShadowType.byteToFloat(colors[jinc*index0+2]) +
                           ShadowType.byteToFloat(colors[jinc*index1+2]) +
                           ShadowType.byteToFloat(colors[jinc*index2+2]))));
*/
                GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                path.moveTo(coordinates[3*index0], coordinates[3*index0+1]);
                path.lineTo(coordinates[3*index1], coordinates[3*index1+1]);
                path.lineTo(coordinates[3*index2], coordinates[3*index2+1]);
                path.closePath();
                g2.fill(path);
                index0 = index1;
                index1 = index2;
              }
            }
            base += count;
          }
        } // end if (array instanceof VisADIndexedTriangleStripArray)
        else if (array instanceof VisADTriangleStripArray) {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    
                              RenderingHints.VALUE_ANTIALIAS_OFF);
          int[] stripVertexCounts =
             ((VisADTriangleStripArray) array).stripVertexCounts;
          int base = 0;
          int basec = 0;
          int jinc = 0;
          if (colors != null) {
            jinc = (colors.length == coordinates.length) ? 3 : 4;
          }
          for (int strip=0; strip<stripVertexCounts.length; strip++) {
            count = stripVertexCounts[strip];

            float oldx = coordinates[base];
            float oldy = coordinates[base+1];
            float lastx = coordinates[base+3];
            float lasty = coordinates[base+4];
            int oldr = 0, oldg = 0, oldb = 0;
            int lastr = 0, lastg = 0, lastb = 0;
            int thisr, thisg, thisb;

            if (colors != null) {
              oldr = (colors[basec] < 0) ? (((int) colors[basec]) + 256) :
                                           ((int) colors[basec]);
              oldg = (colors[basec+1] < 0) ? (((int) colors[basec+1]) + 256) :
                                             ((int) colors[basec+1]);
              oldb = (colors[basec+2] < 0) ? (((int) colors[basec+2]) + 256) :
                                             ((int) colors[basec+2]);
              lastr = (colors[basec+jinc] < 0) ? (((int) colors[basec+jinc]) + 256) :
                                           ((int) colors[basec+jinc]);
              lastg = (colors[basec+jinc+1] < 0) ? (((int) colors[basec+jinc+1]) + 256) :
                                             ((int) colors[basec+jinc+1]);
              lastb = (colors[basec+jinc+2] < 0) ? (((int) colors[basec+jinc+2]) + 256) :
                                             ((int) colors[basec+jinc+2]);
            }

            if (colors == null) {
              for (int i=6; i<3*count; i+=3) {
                GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                path.moveTo(oldx, oldy);
                path.lineTo(lastx, lasty);
                path.lineTo(coordinates[base+i], coordinates[base+i+1]);
                path.closePath();
                g2.fill(path);
                oldx = lastx;
                oldy = lasty;
                lastx = coordinates[base+i];
                lasty = coordinates[base+i+1];
              } // end for (int i=6; i<3*count; i+=3)
            }
            else { // colors != null
              int j = 2 * jinc;
/*
System.out.println(j + " " + jinc + " " + basec);
*/
              for (int i=6; i<3*count; i+=3) {
                thisr = (colors[basec+j] < 0) ? (((int) colors[basec+j]) + 256) :
                                                ((int) colors[basec+j]);
                thisg = (colors[basec+j+1] < 0) ? (((int) colors[basec+j+1]) + 256) :
                                                  ((int) colors[basec+j+1]);
                thisb = (colors[basec+j+2] < 0) ? (((int) colors[basec+j+2]) + 256) :
                                                  ((int) colors[basec+j+2]);
                g2.setColor(new Color((thisr + lastr + oldr)/3,
                                      (thisg + lastg + oldg)/3,
                                      (thisb + lastb + oldb)/3));
/*
System.out.println(i + " " + oldr + " " + oldg + " " + oldb + " " + lastr + " " +
                   lastg + " " + lastb + " " + thisr + " " + thisg + " " + thisb);
*/
                oldr = lastr;
                oldg = lastg;
                oldb = lastb;
                lastr = thisr;
                lastg = thisg;
                lastb = thisb;
                j += jinc;
                GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                path.moveTo(oldx, oldy);
                path.lineTo(lastx, lasty);
                path.lineTo(coordinates[base+i], coordinates[base+i+1]);
                path.closePath();
                g2.fill(path);
/*
System.out.println(i + " " + oldx + " " + oldy + " " + lastx + " " + lasty +
                   " " + coordinates[base+i] + " " + coordinates[base+i+1]);
*/
                oldx = lastx;
                oldy = lasty;
                lastx = coordinates[base+i];
                lasty = coordinates[base+i+1];
              } // end for (int i=6; i<3*count; i+=3)
            }
            base += 3 * count;
            basec += jinc * count;
          } // end for (int strip=0; strip<stripVertexCounts.length; strip++)
        } // end if (array instanceof VisADTriangleStripArray)
        else {
          throw new VisADError("VisADCanvasJ2D.render: bad array class");
        }
      } // end if (image == null)
    } // end if (scene instanceof VisADAppearance)
  }

  /** this assumes only VisADPointArray or VisADLineArray */
  public static void drawAppearance(Graphics graphics, VisADAppearance appearance,
                                    AffineTransform t, Rectangle2D.Float clip) {
    VisADGeometryArray array = appearance.array;
    if (array == null) return;
    byte[] colors = array.colors;
    if (colors == null) {
      if (appearance.color_flag) {
        graphics.setColor(new Color(appearance.red, appearance.green,
                                    appearance.blue));
/*
System.out.println("drawAppearance: color = " + appearance.red + " " +
                   appearance.green + " " + appearance.blue);
*/
      }
      else {
        graphics.setColor(new Color(1.0f, 1.0f, 1.0f));
      }
    }
    int count = array.vertexCount;
    float[] coordinates = array.coordinates;
    float[] oldcoords = new float[2*count];
    int j = 0;
    for (int i=0; i<3*count; i += 3) {
      oldcoords[j++] = coordinates[i];
      oldcoords[j++] = coordinates[i+1];
    }
    float[] newcoords = new float[2 * count];
    t.transform(oldcoords, 0, newcoords, 0, count);

    if (clip == null) {
      graphics.setClip(null);
    }
    else {
      // transform clip
      float x = (float) clip.getX();
      float y = (float) clip.getY();
      float width = (float) clip.getWidth();
      float height = (float) clip.getHeight();
      float[] oldclip =
        {x, y, x, y+height, x+width, y+height, x+width, y};
      float[] newclip = new float[2 * 4];
      t.transform(oldclip, 0, newclip, 0, 4);
      GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
      path.moveTo(newclip[0], newclip[1]);
      path.lineTo(newclip[2], newclip[3]);
      path.lineTo(newclip[4], newclip[5]);
      path.lineTo(newclip[6], newclip[7]);
      path.closePath();
      graphics.setClip(path);
    }

    if (array instanceof VisADPointArray) {
/*
System.out.println("drawAppearance: VisADPointArray, count = " + count);
*/
      if (colors == null) {
        for (int i=0; i<2*count; i += 2) {
          graphics.drawLine((int) newcoords[i], (int) newcoords[i+1],
                            (int) newcoords[i], (int) newcoords[i+1]);
        }
      }
      else { // colors != null
        int jinc = (colors.length == coordinates.length) ? 3 : 4;
        j = 0;
        for (int i=0; i<2*count; i += 2) {
          graphics.setColor(new Color(
            ((colors[j] < 0) ? (((int) colors[j]) + 256) :
                               ((int) colors[j]) ),
            ((colors[j+1] < 0) ? (((int) colors[j+1]) + 256) :
                                 ((int) colors[j+1]) ),
            ((colors[j+2] < 0) ? (((int) colors[j+2]) + 256) :
                                 ((int) colors[j+2]) ) ));
/* MEM_WLH
          graphics.setColor(new Color(ShadowType.byteToFloat(colors[j]),
                                      ShadowType.byteToFloat(colors[j+1]),
                                      ShadowType.byteToFloat(colors[j+2])));
*/
          j += jinc;
          graphics.drawLine((int) newcoords[i], (int) newcoords[i+1],
                            (int) newcoords[i], (int) newcoords[i+1]);
        } 
      }
    }
    else if (array instanceof VisADLineArray) {
/*
System.out.println("drawAppearance: VisADLineArray, count = " + count);
*/
      if (colors == null) {
        for (int i=0; i<2*count; i += 4) {
          graphics.drawLine((int) newcoords[i], (int) newcoords[i+1],
                            (int) newcoords[i+2], (int) newcoords[i+3]);
/*
System.out.println(" " + newcoords[i] + " " + newcoords[i+1] + " " +
                   newcoords[i+2] + " " + newcoords[i+3]);
*/
        }
      }
      else { // colors != null
        int jinc = (colors.length == coordinates.length) ? 3 : 4;
        j = 0;
        for (int i=0; i<2*count; i += 4) {
          graphics.setColor(new Color(
            (((colors[j] < 0) ? (((int) colors[j]) + 256) :
                                ((int) colors[j]) ) +
             ((colors[j+jinc] < 0) ? (((int) colors[j+jinc]) + 256) :
                                     ((int) colors[j+jinc]) ) ) / 2,
            (((colors[j+1] < 0) ? (((int) colors[j+1]) + 256) :
                                  ((int) colors[j+1]) ) +
             ((colors[j+jinc+1] < 0) ? (((int) colors[j+jinc+1]) + 256) :
                                       ((int) colors[j+jinc+1]) ) ) / 2,
            (((colors[j+2] < 0) ? (((int) colors[j+2]) + 256) :
                                  ((int) colors[j+2]) ) +
             ((colors[j+jinc+2] < 0) ? (((int) colors[j+jinc+2]) + 256) :
                                       ((int) colors[j+jinc+2]) ) ) / 2 ));
/* MEM_WLH
          graphics.setColor(new Color(
                    0.5f * (ShadowType.byteToFloat(colors[j]) +
                            ShadowType.byteToFloat(colors[j+jinc])),
                    0.5f * (ShadowType.byteToFloat(colors[j+1]) +
                            ShadowType.byteToFloat(colors[j+jinc+1])),
                    0.5f * (ShadowType.byteToFloat(colors[j+2]) +
                            ShadowType.byteToFloat(colors[j+jinc+2]))));
*/
          j += 2 * jinc;
          graphics.drawLine((int) newcoords[i], (int) newcoords[i+1],
                            (int) newcoords[i+2], (int) newcoords[i+3]);
        }
      }
    }
    else {
      throw new VisADError("DisplayRendererJ2D.drawAppearance: " +
                           "bad VisADGeometryArray type");
    }
  }

  public AffineTransform getTransform() {
    synchronized (images) {
      return tgeometry;
    }
  }

  public Dimension getPreferredSize() {
    return prefSize;
  }

  public void setPreferredSize(Dimension size) {
    prefSize = size;
  }

}

