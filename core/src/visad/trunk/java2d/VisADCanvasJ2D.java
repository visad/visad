
//
// VisADCanvasJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
 
package visad.java2d;
 
import visad.*;
 
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
 
import java.util.*;

/**
   VisADCanvasJ2D is the VisAD extension of Canvas
   for Java2D.<P>
*/

public class VisADCanvasJ2D extends Canvas
       implements Runnable {

  private DisplayRendererJ2D displayRenderer;
  private DisplayImplJ2D display;
  private Component component;
  Dimension prefSize = new Dimension(0, 0);

  private transient Thread renderThread;

  private BufferedImage[] images; // animation sequence
  private boolean[] valid_images;
  private int current_image; // current index into images
  private int width, height; // size of images
  private int length; // length of images & valid_images
  private AffineTransform tgeometry; // transform for current display

  MouseHelper helper;

  // wake up flag for render_trigger
  boolean wakeup = false;

  VisADCanvasJ2D(DisplayRendererJ2D renderer, Component c) {
    displayRenderer = renderer;
    display = (DisplayImplJ2D) renderer.getDisplay();
    component = c;

    width = getSize().width;
    height = getSize().height;
    images = new BufferedImage[] {(BufferedImage) createImage(width, height)};
    valid_images = new boolean[] {false};
    current_image = -1;
    tgeometry = null;

    ComponentListener cl = new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        createImages(-1);
      }
    };
    addComponentListener(cl);

    renderThread = new Thread(this);
    renderThread.start();
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
      width = getSize().width;
      height = getSize().height;
      images = new BufferedImage[length];
      valid_images = new boolean[length];
      for (int i=0; i<length; i++) {
        images[i] = (BufferedImage) createImage(width, height);
        valid_images[i] = false;
      }
      tgeometry = null;
    }
    render_trigger();
  }

  public void scratchImages() {
    synchronized (images) {
      for (int i=0; i<length; i++) valid_images[i] = false;
      tgeometry = null;
    }
    render_trigger();
  }

  /** render scene graph into canvas.images[current_image] */
  public void render_trigger() {
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
            wait(2000);
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
      Graphics g = getGraphics();
      paint(g);
      g.dispose();
    }
  }

  public void paint(Graphics g) {
    BufferedImage image = null;
    boolean valid = false;
    double w = 0.0, h = 0.0;
    synchronized (images) {
      if (0 <= current_image && current_image < length) {
        image = images[current_image];
        valid = valid_images[current_image];
        w = width;
        h = height;
      }
    }
    if (image != null) {
      if (!valid) {
        VisADGroup root = displayRenderer.getRoot();
        AffineTransform trans = displayRenderer.getTrans();
        Graphics2D g2 = image.createGraphics();
        g2.setBackground(getBackground());
        g2.setRenderingHints(Graphics2D.ANTIALIASING, Graphics2D.ANTIALIAS_OFF);
        g2.setRenderingHints(Graphics2D.RENDERING, Graphics2D.RENDER_SPEED);
        g2.clearRect(0, 0, width, height);

        // render into image;
        synchronized (images) {
          tgeometry = new AffineTransform();
          tgeometry.setToTranslation(0.5 * w, 0.5 * h);
          AffineTransform s1 = new AffineTransform();
          s1.setToScale(0.25 * w, 0.25 * h);
          tgeometry.concatenate(s1);
          tgeometry.concatenate(trans);
        }
        g2.setTransform(tgeometry);
        try {
          AnimationControlJ2D animate_control = (AnimationControlJ2D)
            display.getControl(AnimationControlJ2D.class);
          animate_control.init();
          render(g2, root);
        }
        catch (VisADException e) {
        }

        //
        // note java.awt.GradientPaint for smooth 1-D color
        // just use average color of line or triangle
        //

      } // end if (!valid)
      else { // valid
        synchronized (images) {
          tgeometry = image.createGraphics().getTransform();
        }
      }
      g.drawImage(image, 0, 0, this);
      displayRenderer.drawCursorStringVector(g);
    } // end if (image != null)
  }

  private void render(Graphics2D g2, VisADSceneGraphObject scene)
          throws VisADException {
    if (scene instanceof VisADSwitch) {
      VisADSceneGraphObject child =
        ((VisADSwitch) scene).getSelectedChild();
      render(g2, child);
    }
    else if (scene instanceof VisADGroup) {
      Vector children = ((VisADGroup) scene).getChildren();
      Enumeration childs = children.elements();
      while (childs.hasMoreElements()) {
        VisADSceneGraphObject child =
          (VisADSceneGraphObject) childs.nextElement();
        render(g2, child);
      }
    }
    else { // scene instanceof VisADAppearance
      VisADAppearance appearance = (VisADAppearance) scene;
      VisADGeometryArray array = appearance.array;
      BufferedImage image = (BufferedImage) appearance.image;
      if (array == null) {
        throw new VisADError("VisADCanvasJ2D.render: array is null");
      }
      AffineTransform tg = g2.getTransform();
      if (image != null) {
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
  ywh = m10 * width + m11 * heigth + m12

  x0h = m00 * 0 + m01 * height + m02
  y0h = m10 * 0 + m11 * heigth + m12
so:
  m02 = x00
  m12 = y00
  m00 = (xw0 - x00) / width
  m10 = (yw0 - y00) / width
  m01 = (x0h - x00) / height
  m11 = (y0h - y00) / height
check by:
  xwh = m00 * width + m01 * height + m02
  ywh = m10 * width + m11 * heigth + m12
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
        int count = array.vertexCount;
        if (count == 0) return;
        float[] coordinates = array.coordinates;
        float[] colors = array.colors;
        if (colors == null) {
          if (appearance.color_flag) {
            g2.setColor(new Color(appearance.red, appearance.green, appearance.blue));
          }
          else {
            g2.setColor(new Color(1.0f, 1.0f, 1.0f));
          }
        }
        if (array instanceof VisADPointArray ||
            array instanceof VisADLineArray ||
            array instanceof VisADLineStripArray) {
          GraphicsModeControl mode = display.getGraphicsModeControl();
          double dsize = (array instanceof VisADPointArray) ?
                           mode.getPointSize() :
                           mode.getLineWidth();
          double[] pts = {0.0, 0.0, 0.0, dsize, dsize, 0.0};
          double[] newpts = new double[6];
          try {
            tg.inverseTransform(pts, 0, newpts, 0, 3);
          }
          catch (NoninvertibleTransformException e) {
            throw new VisADError("VisADCanvasJ2D.render: non-invertable transform");
          }
          double xx = (newpts[2] - newpts[0]) * (newpts[2] - newpts[0]) +
                      (newpts[3] - newpts[1]) * (newpts[3] - newpts[1]);
          double yy = (newpts[4] - newpts[0]) * (newpts[4] - newpts[0]) +
                      (newpts[5] - newpts[1]) * (newpts[5] - newpts[1]);
          float size = (float) (0.5 * (Math.sqrt(xx) + Math.sqrt(yy)));
          if (array instanceof VisADPointArray) {
            if (colors == null) {
              for (int i=0; i<3*count; i += 3) {
                g2.fill(new Rectangle2D.Float(coordinates[i], coordinates[i+1],
                                              size, size));
              }
            }
            else { // colors != null
              int j = 0;
              int jinc = (colors.length == coordinates.length) ? 3 : 4;
              for (int i=0; i<3*count; i += 3) {
                g2.setColor(new Color(colors[j], colors[j+1], colors[j+2]));
                j += jinc;
                g2.fill(new Rectangle2D.Float(coordinates[i], coordinates[i+1],
                                              size, size));
              }
            }
          }
          else if (array instanceof VisADLineArray) {
            if (Math.abs(dsize - 1.0) < 0.2) {
              if (colors == null) {
                for (int i=0; i<6*count; i += 6) {
                  g2.draw(new Line2D.Float(coordinates[i], coordinates[i+1],
                                           coordinates[i+3], coordinates[i+4]));
                }
              }
              else { // colors != null
                int j = 0;
                int jinc = (colors.length == coordinates.length) ? 3 : 4;
                for (int i=0; i<6*count; i += 6) {
                  g2.setColor(new Color(0.5f * (colors[j] + colors[j+jinc]),
                                        0.5f * (colors[j+1] + colors[j+jinc+1]),
                                        0.5f * (colors[j+2] + colors[j+jinc+2])));
                  j += jinc;
                  g2.draw(new Line2D.Float(coordinates[i], coordinates[i+1],
                                           coordinates[i+3], coordinates[i+4]));
                }
              }
            }
            else { // !(Math.abs(dsize - 1.0) < 0.2)
              if (colors == null) {
                for (int i=0; i<6*count; i += 6) {
                  float epsx = coordinates[i+4] - coordinates[i+1];
                  float epsy = coordinates[i] - coordinates[i+3];
                  float epsl = (float) Math.sqrt(epsx * epsx + epsy * epsy);
                  epsx *= (0.5f * size / epsl);
                  epsy *= (0.5f * size / epsl);
                  GeneralPath path = new GeneralPath(GeneralPath.EVEN_ODD);
                  path.moveTo(coordinates[i] - epsx, coordinates[i+1] - epsy);
                  path.lineTo(coordinates[i] + epsx, coordinates[i+1] + epsy);
                  path.lineTo(coordinates[i+3] + epsx, coordinates[i+4] + epsy);
                  path.lineTo(coordinates[i+3] - epsx, coordinates[i+4] - epsy);
                  path.closePath();
                  g2.fill(path);
                }
              }
              else { // colors != null
                int j = 0;
                int jinc = (colors.length == coordinates.length) ? 3 : 4;
                for (int i=0; i<6*count; i += 6) {
                  g2.setColor(new Color(0.5f * (colors[j] + colors[j+jinc]), 
                                        0.5f * (colors[j+1] + colors[j+jinc+1]),
                                        0.5f * (colors[j+2] + colors[j+jinc+2])));
                  j += jinc;
                  float epsx = coordinates[i+4] - coordinates[i+1];
                  float epsy = coordinates[i] - coordinates[i+3];
                  float epsl = (float) Math.sqrt(epsx * epsx + epsy * epsy);
                  epsx *= (0.5f * size / epsl);
                  epsy *= (0.5f * size / epsl);
                  GeneralPath path = new GeneralPath(GeneralPath.EVEN_ODD);
                  path.moveTo(coordinates[i] - epsx, coordinates[i+1] - epsy);
                  path.lineTo(coordinates[i] + epsx, coordinates[i+1] + epsy);
                  path.lineTo(coordinates[i+3] + epsx, coordinates[i+4] + epsy);
                  path.lineTo(coordinates[i+3] - epsx, coordinates[i+4] - epsy);
                  path.closePath();
                  g2.fill(path);
                }
              }
            }
          }
          else { // (array instanceof VisADLineStripArray)
            float lastx = coordinates[0];
            float lasty = coordinates[1];
            float lastr = 0.0f, lastg = 0.0f, lastb = 0.0f;
            if (colors != null) {
              lastr = colors[0];
              lastg = colors[1];
              lastb = colors[2];
            }
            if (Math.abs(dsize - 1.0) < 0.2) {
              if (colors == null) {
                for (int i=3; i<3*count; i += 3) {
                  g2.draw(new Line2D.Float(lastx, lasty,
                                           coordinates[i], coordinates[i+1]));
                  lastx = coordinates[i];
                  lasty = coordinates[i+1];
                }
              }
              else {
                int jinc = (colors.length == coordinates.length) ? 3 : 4;
                int j = jinc;
                for (int i=3; i<3*count; i += 3) {
                  g2.setColor(new Color(0.5f * (lastr + colors[j]),
                                        0.5f * (lastg + colors[j+1]),
                                        0.5f * (lastb + colors[j+2])));
                  lastr = colors[j];
                  lastg = colors[j+1];
                  lastb = colors[j+2];
                  j += jinc;
                  g2.draw(new Line2D.Float(lastx, lasty,
                                           coordinates[i], coordinates[i+1]));
                  lastx = coordinates[i];
                  lasty = coordinates[i+1];
                }
              }
            }
            else { // !(Math.abs(dsize - 1.0) < 0.2)
              if (colors == null) {
                for (int i=3; i<3*count; i += 3) {
                  float epsx = coordinates[i+1] - lasty;
                  float epsy = lastx - coordinates[i];
                  float epsl = (float) Math.sqrt(epsx * epsx + epsy * epsy);
                  epsx *= (0.5f * size / epsl);
                  epsy *= (0.5f * size / epsl);
                  GeneralPath path = new GeneralPath(GeneralPath.EVEN_ODD);
                  path.moveTo(lastx - epsx, lasty - epsy);
                  path.lineTo(lastx + epsx, lasty + epsy);
                  path.lineTo(coordinates[i] + epsx, coordinates[i+1] + epsy);
                  path.lineTo(coordinates[i] - epsx, coordinates[i+1] - epsy);
                  path.closePath();
                  g2.fill(path);
                  lastx = coordinates[i];
                  lasty = coordinates[i+1];
                }
              }
              else { // colors != null
                int jinc = (colors.length == coordinates.length) ? 3 : 4;
                int j = jinc;
                for (int i=3; i<3*count; i += 3) {
                  g2.setColor(new Color(0.5f * (lastr + colors[j]),
                                        0.5f * (lastg + colors[j+1]),
                                        0.5f * (lastb + colors[j+2])));
                  lastr = colors[j];
                  lastg = colors[j+1];
                  lastb = colors[j+2];
                  j += jinc;
                  float epsx = coordinates[i+1] - lasty;
                  float epsy = lastx - coordinates[i];
                  float epsl = (float) Math.sqrt(epsx * epsx + epsy * epsy);
                  epsx *= (0.5f * size / epsl);
                  epsy *= (0.5f * size / epsl);
                  GeneralPath path = new GeneralPath(GeneralPath.EVEN_ODD);
                  path.moveTo(lastx - epsx, lasty - epsy);
                  path.lineTo(lastx + epsx, lasty + epsy);
                  path.lineTo(coordinates[i] + epsx, coordinates[i+1] + epsy);
                  path.lineTo(coordinates[i] - epsx, coordinates[i+1] - epsy);
                  path.closePath();
                  g2.fill(path);
                  lastx = coordinates[i];
                  lasty = coordinates[i+1];
                }
              }
            }
          }
        }
        else if (array instanceof VisADTriangleArray) {
          if (colors == null) { 
            for (int i=0; i<9*count; i += 9) { 
              GeneralPath path = new GeneralPath(GeneralPath.EVEN_ODD);
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
            for (int i=0; i<9*count; i += 9) {
              g2.setColor(new Color(
                0.33f * (colors[j] + colors[j+jinc] + colors[j+2*jinc]),
                0.33f * (colors[j+1] + colors[j+jinc+1] + colors[j+2*jinc+1]),
                0.33f * (colors[j+2] + colors[j+jinc+2] + colors[j+2*jinc+2])));
              j += jinc;
              GeneralPath path = new GeneralPath(GeneralPath.EVEN_ODD);
              path.moveTo(coordinates[i], coordinates[i+1]);
              path.lineTo(coordinates[i+3], coordinates[i+4]);
              path.lineTo(coordinates[i+6], coordinates[i+7]);
              path.closePath();
              g2.fill(path);
            }
          }
        }
        else if (array instanceof VisADQuadArray) {
          if (colors == null) {
            for (int i=0; i<12*count; i += 12) {
              GeneralPath path = new GeneralPath(GeneralPath.EVEN_ODD);
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
            for (int i=0; i<12*count; i += 12) {
              g2.setColor(new Color(
                0.25f * (colors[j] + colors[j+jinc] +
                         colors[j+2*jinc] + colors[j+3*jinc]),
                0.25f * (colors[j+1] + colors[j+jinc+1] +
                         colors[j+2*jinc+1] + colors[j+3*jinc+1]),
                0.25f * (colors[j+2] + colors[j+jinc+2] +
                         colors[j+2*jinc+2] + colors[j+3*jinc+2])));
              j += jinc;
              GeneralPath path = new GeneralPath(GeneralPath.EVEN_ODD);
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
                GeneralPath path = new GeneralPath(GeneralPath.EVEN_ODD);
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
                  0.33f * (colors[jinc*index0] + colors[jinc*index1] +
                           colors[jinc*index2]),
                  0.33f * (colors[jinc*index0+1] + colors[jinc*index1+1] +
                           colors[jinc*index2+1]),
                  0.33f * (colors[jinc*index0+2] + colors[jinc*index1+2] +
                           colors[jinc*index2+2])));
                GeneralPath path = new GeneralPath(GeneralPath.EVEN_ODD);
                path.moveTo(coordinates[i], coordinates[i+1]);
                path.lineTo(coordinates[i+3], coordinates[i+4]);
                path.lineTo(coordinates[i+6], coordinates[i+7]);
                path.closePath();
                g2.fill(path);
              }
            }
            base += count;
          }
        }
        else {
          throw new VisADError("VisADCanvasJ2D.render: bad array class");
        }
      }
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

}

