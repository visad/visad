/*
 * @(#)MapCanvas.java - MapCanvas with MapScroller as inner class
 *
 *
 *  This software was developed by the Thermal Modeling and Analysis
 *  Project(TMAP) of the National Oceanographic and Atmospheric
 *  Administration's (NOAA) Pacific Marine Environmental Lab(PMEL),
 *  hereafter referred to as NOAA/PMEL/TMAP.
 *
 *  Access and use of this software shall impose the following
 *  obligations and understandings on the user. The user is granted the
 *  right, without any fee or cost, to use, copy, modify, alter, enhance
 *  and distribute this software, and any derivative works thereof, and
 *  its supporting documentation for any purpose whatsoever, provided
 *  that this entire notice appears in all copies of the software,
 *  derivative works and supporting documentation.  Further, the user
 *  agrees to credit NOAA/PMEL/TMAP in any publications that result from
 *  the use of this software or in any product that includes this
 *  software. The names TMAP, NOAA and/or PMEL, however, may not be used
 *  in any advertising or publicity to endorse or promote any products
 *  or commercial entity unless specific written permission is obtained
 *  from NOAA/PMEL/TMAP. The user also understands that NOAA/PMEL/TMAP
 *  is not obligated to provide the user with any support, consulting,
 *  training or assistance of any kind with regard to the use, operation
 *  and performance of this software nor to provide the user with any
 *  updates, revisions, new versions or "bug fixes".
 *
 *  THIS SOFTWARE IS PROVIDED BY NOAA/PMEL/TMAP "AS IS" AND ANY EXPRESS
 *  OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL NOAA/PMEL/TMAP BE LIABLE FOR ANY SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 *  RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
 *  CONTRACT, NEGLIGENCE OR OTHER TORTUOUS ACTION, ARISING OUT OF OR IN
 *  CONNECTION WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */
package dods.clients.importwizard.TMAP.map;

//1.1 import java.awt.AWTEvent;
//Change to jdk1.3
import java.awt.*;
import javax.swing.*;
//import java.awt.Canvas;
//import java.awt.Container;
//import java.awt.Color;
//import java.awt.Cursor;
//import java.awt.Dimension;
//import java.awt.Graphics;
//import java.awt.Image;
//import java.awt.Rectangle;
//import java.awt.Frame;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.image.ImageProducer;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.awt.MediaTracker;

//import dods.clients.importwizard.TMAP.map.MapConstants;
//import dods.clients.importwizard.TMAP.map.MaxZoomException;
//import dods.clients.importwizard.TMAP.map.MinZoomException;

/**
 * An extensible Canvas class for obtaining/displaying 2D coordinates.
 * <p>
 * This canvas has a base image and uses a tool to get/set coordinates 
 * on the base image.
 * <p>
 * This is the second official release (version 2) of the MapCanvas.
 * It includes zoom/pan/scroll and snap-to-grid capabilities.
 *
 * @version     2.2, 17 June 1997
 * @author      Jonathan Callahan
 *
 * Note: Using a tool to select 360 degrees of longitude will result
 * in <code>user_x[HI] = user_x[LO]+360</code> where user_x[LO] is
 * correctly interpolated into the domain of the x axis.  Thus,
 * user_x[HI] will be outside the domain of the x axis specified
 * in MapGrid.
 */


/**
 * Modified to a JLabel class
 *
 * modified by Sheila (zhifang) Jiang
 *
 */

//--------------------------------------------------------------------------
public class MapCanvas extends JLabel //Canvas
      implements MapConstants, MouseListener, MouseMotionListener
{

  MapScroller scroller;
  Image base_image, gray_image;
  ImageIcon image_icon;

  Dimension offDimension;
  Image offImage;
  Graphics offGraphics;

  int width;
  int height;
  int clip_width;
  int clip_height;

  double image_scaling = 1.0;
  Rectangle imageRect = new Rectangle(0, 0, 0, 0);

  // suspended/resume
  private boolean scrollSuspended;

  // scrolling rate in panning()
  private int slow_delta=1;
  private int fast_delta=5;

  /**
   * Flag to indicate movement of the base image is desired.
   */
  public boolean pan_down = false;
  /**
   * Flag to indicate movement of the base image is desired.
   */
  public boolean pan_down_fast = false;
  /**
   * Flag to indicate movement of the base image is desired.
   */
  public boolean pan_left = false;
  /**
   * Flag to indicate movement of the base image is desired.
   */
  public boolean pan_left_fast = false;
  /**
   * Flag to indicate movement of the base image is desired.
   */
  public boolean pan_right = false;
  /**
   * Flag to indicate movement of the base image is desired.
   */
  public boolean pan_right_fast = false;
  /**
   * Flag to indicate movement of the base image is desired.
   */
  public boolean pan_up = false;
  /**
   * Flag to indicate movement of the base image is desired.
   */
  public boolean pan_up_fast = false;

  /**
   * Flag determining whether scrolling is controlled by the active MapTool
   * or externally by the application programmer.
   */
  public boolean tool_driven = true;

  /**
   * The zoom factor to be applied when using the methods zoom_in() and zoom_out().
   */
  public double zoom_factor = 1.4;

  /**
   * The maximum image scaling to be allowed.
   * It doesn't make sense to scale more than 2X the original image
   */
  public double max_img_scaling = 4.0;

  /**
   * The minimum image scaling to be allowed.
   * This will be set automatically when the MapCanvas is created.
   * The initial min_img_scaling will be such that the map cannot be made
   * smaller than the MapCanvas area in both the X and Y dimensions.
   */
  public double min_img_scaling = 0.25;

  public MapTool [] toolArray;
  public MapRegion [] regionArray;

  private int selected_tool = 0;

  /**
   * The current grid being used by the map.
   */
  public MapGrid grid;


  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
  class GrayFilter extends RGBImageFilter {
    public GrayFilter() {canFilterIndexColorModel = true;}
    public int filterRGB(int x, int y, int rgb) {
      int a = rgb & 0xff000000;

      int r = (rgb & 0xff0000) >> 16;
      int g = (rgb & 0x00ff00) >> 8;
      int b = (rgb & 0x0000ff);
      //      int gray = (int)(.3 * r + .59 * g + .11 * b);
      int gray = 128 + (int)(.075 * r + .145 * g + .027 * b);
      return a | (gray << 16) | (gray << 8) | gray;
    }
  }
  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
  class MapScroller extends Thread
  {
    int sleep_milliseconds=50;
 
    public void set_sleep_milliseconds(int milliseconds) {
      sleep_milliseconds = milliseconds;
    }
 
    public void run()
    {
      while (true)
      {
        if( panning() ) {
          repaint();
        }
        yield();
        try {
          sleep(sleep_milliseconds);
        } catch (InterruptedException e) {}
        yield();
      }
    }
  }
  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

  public boolean panning( )
  {
      boolean refresh = false;

      if ( tool_driven ) {

    if ( getTool().pan_left) {
      if ( getTool().pan_left_fast )
        scroll_X(fast_delta);
      else
        scroll_X(slow_delta);
      refresh = true;
    } else if ( getTool().pan_right) {
      if ( getTool().pan_right_fast)
        scroll_X(-fast_delta);
      else
        scroll_X(-slow_delta);
      refresh = true;
    }

    if ( getTool().pan_down) {
      if ( getTool().pan_down_fast)
        scroll_Y(-fast_delta);
      else
        scroll_Y(-slow_delta);
      refresh = true;
    } else if ( getTool().pan_up) {
      if ( getTool().pan_up_fast)
        scroll_Y(fast_delta);
      else
        scroll_Y(slow_delta);
      refresh = true;
    }

      } else {

    if ( pan_left ) {
      if ( pan_left_fast )
        scroll_X(fast_delta);
      else
        scroll_X(slow_delta);
      refresh = true;
    } else if ( pan_right ) {
      if ( pan_right_fast )
        scroll_X(-fast_delta);
      else
        scroll_X(-slow_delta);
      refresh = true;
    }

    if ( pan_down ) {
      if ( pan_down_fast ) {
        scroll_Y(-fast_delta);
      } else
        scroll_Y(-slow_delta);
      refresh = true;
    } else if ( pan_up ) {
      if ( pan_up_fast )
        scroll_Y(fast_delta);
      else
        scroll_Y(slow_delta);
      refresh = true;
    }
        getTool().setUser_XY();
      } // tool_driven
      return refresh;
  }

//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


  /**
   * Constructs and initializes a MapCanvas with the specified parameters.
   * @param base_image the image over which the tool will be drawn
   * @param width the width in pixels of the MapCanvas
   * @param height the height in pixels of the MapCanvas
   * @param tool the tool for user interaction
   * @param grid the grid associated with the underlying basemap.
   */
  public MapCanvas(ImageIcon image_icon, int width, int height, MapTool [] toolArray, MapGrid grid)
  {
    super(image_icon);
    MediaTracker tracker;

    this.base_image = image_icon.getImage();
    this.width = width;
    this.height = height;
    this.toolArray = toolArray;
    this.grid = grid;
    this.grid.setCanvasWidth(width);

    ImageFilter f = new GrayFilter();
    ImageProducer producer = new FilteredImageSource(base_image.getSource(),f);
    gray_image = this.createImage(producer);

    tracker = new MediaTracker(this);
    tracker.addImage(gray_image, 1);

    try {
      tracker.waitForID(1);
    } catch (InterruptedException e) {
      System.out.println("MapCanvas: " + e);
    }
    if (tracker.isErrorID(1)) {
      System.out.println("MapCanvas: Error creating gray image.");
    }

    // mouse listener
    addMouseListener( this );
    addMouseMotionListener( this );
    enableEvents(AWTEvent.MOUSE_EVENT_MASK|AWTEvent.MOUSE_MOTION_EVENT_MASK);

    scale_image_to_fit();

    scroller = new MapScroller();
    scroller.setPriority(Thread.MIN_PRIORITY);
    scroller.start();
  }
 

  /**
   * Constructs and initializes a MapCanvas with the specified parameters.
   * @param base_image the image over which the tool will be drawn
   * @param width the width in pixels of the MapCanvas
   * @param height the height in pixels of the MapCanvas
   * @param tool the tool for user interaction
   * @param grid the grid associated with the underlying basemap.
   */
  public MapCanvas(ImageIcon image_icon, int width, int height,
       MapTool [] toolArray, MapGrid grid, int x, int y, double scaling)
  {
    super(image_icon);
    MediaTracker tracker;

    this.base_image = image_icon.getImage();
    this.width = width;
    this.height = height;
    this.toolArray = toolArray;
    this.grid = grid;
    this.grid.setCanvasWidth(width);

    ImageFilter f = new GrayFilter();
    ImageProducer producer = new FilteredImageSource(base_image.getSource(),f);
    gray_image = this.createImage(producer);

    tracker = new MediaTracker(this);
    tracker.addImage(gray_image, 1);

    try {
      tracker.waitForID(1);
    } catch (InterruptedException e) {
      System.out.println("MapCanvas: " + e);
    }
    if (tracker.isErrorID(1)) {
      System.out.println("MapCanvas: Error creating gray image.");
    }

    // mouse listener
    addMouseListener( this );
    addMouseMotionListener( this );
    enableEvents(AWTEvent.MOUSE_EVENT_MASK|AWTEvent.MOUSE_MOTION_EVENT_MASK);

    position_and_scale_image(x, y, scaling);

    scroller = new MapScroller();
    scroller.setPriority(Thread.MIN_PRIORITY);
    scroller.start();
  }
 

  /**
   * Overrides the default method and omits the background fill.
   * @param g the specified Graphics window
   */
  public synchronized void update(Graphics g)
  {    
    paintComponent(g);
  }

  /**
   * Paints the canvas with the base image and the current tool.
   * @param g the specified Graphics window
   */
  public synchronized void paintComponent(Graphics g)
  {
      //Rescale
      offImage = base_image.getScaledInstance(imageRect.width, imageRect.height, base_image.SCALE_DEFAULT);
      //use a lightweight component to carry out the image
      ImageIcon temp = new ImageIcon(offImage);
          
    //Dimension d = size();
    //1.1 We need to create a new graphics context every time
    /*    in java 1.0.
    if ( (offGraphics == null)
     || (d.width != offDimension.width)
     || (d.height != offDimension.height) ) {
      offDimension = d;
      offImage = createImage(d.width, d.height);
      offGraphics = offImage.getGraphics();
      }*/

    //offGraphics.setColor(Color.gray);
    //offGraphics.fillRect(0, 0, width, height);

    //offGraphics.setClip(0, 0, clip_width, clip_height);
    //offGraphics.clipRect(0, 0, clip_width, clip_height);
    //offGraphics.drawImage(gray_image, imageRect.x, imageRect.y,
    //   imageRect.width, imageRect.height, this);
    //offGraphics.drawImage(gray_image, imageRect.x+imageRect.width,
    //     imageRect.y, imageRect.width, imageRect.height, this);

    // This next section deals with calculating a clipping rectangle
    // for the valid range of the data.

    int xlo, xhi, xwidth, ylo, yhi, yheight;
    double domain = grid.domain_X[HI]-grid.domain_X[LO];
    double range = getTool().range_X[HI]-getTool().range_X[LO];

    if ( Math.abs(domain) == Math.abs(range) ) {
      xlo = 0;
      xhi = clip_width;
    } else {
      xlo = grid.userToPixel_X(getTool().range_X[LO]);
      xhi = grid.userToPixel_X(getTool().range_X[HI]);
    }

    // Now for some logic to deal with ranges that span
    // the domain edges.  (e.g. domain=-180:180, range=140E:90W)
    // (xhi < xlo) means that xhi or xlo is outside of the
    // MapCanvas viewing area and we need modify xlo or xhi
    // to match the MapCanvas edge.
    if ( xhi < xlo ) {
      if ( xlo > clip_width )
        xlo = 0;
      else
        xhi = clip_width;
    }
    xwidth = xhi - xlo;

    ylo = grid.userToPixel_Y(getTool().range_Y[HI]);
    yhi = grid.userToPixel_Y(getTool().range_Y[LO]);
    yheight = yhi - ylo;

    // Now we apply a clipping rectangle to match the data range

    //offGraphics.setClip(xlo, ylo, xwidth, yheight);
    //offGraphics.clipRect(xlo, ylo, xwidth, yheight);
    //offGraphics.drawImage(base_image, imageRect.x, imageRect.y,
    //     imageRect.width, imageRect.height, this);
    //offGraphics.drawImage(base_image, imageRect.x+imageRect.width,
    //     imageRect.y, imageRect.width, imageRect.height, this);

    //offGraphics.setClip(0, 0, clip_width, clip_height);
    //offGraphics.clipRect(0, 0, clip_width, clip_height);

    //g.setClip(xlo, ylo, xwidth, yheight);
    //g.clipRect(xlo, ylo, xwidth, yheight);
   
    temp.paintIcon(this, g, imageRect.x, imageRect.y);
    temp.paintIcon(this, g, imageRect.x+imageRect.width, imageRect.y);

    //g.setClip(0, 0, clip_width, clip_height);
    //g.clipRect(0, 0, clip_width, clip_height);
    
    for (int i=0; i<toolArray.length; i++) {
	toolArray[i].draw(g);
	//toolArray[i].draw(offGraphics);
    }

    for (int i=0; i<regionArray.length; i++) {
	regionArray[i].draw(g);
	//regionArray[i].draw(offGraphics);
    }
    
    //g.setClip(0, 0, width, height);
    //g.clipRect(0, 0, width, height);
    //g.drawImage(offImage, 0, 0, this);
    
  }


  /**
   * Causes the map to scroll an amount in the X direction.
   * @param delta the number of pixels to scroll.
   */
  public synchronized void scroll_X(int delta)
  {

    int tool_delta = delta;
    imageRect.x += delta;


    /*
     * The starting coordinates of the base image are always:
     *
     *   -imageRect.width < imageRect.x <= 0  (for modulo_X)
     *   width - imageRect.width < imageRect.x <= 0 (for non-modulo_X)
     *   height - imageRect.height < imageRect.y <= 0
     */

    if ( grid.modulo_X ) {
      if ( imageRect.x <= -imageRect.width ) {
        imageRect.x = imageRect.x + imageRect.width;
      }
      if ( imageRect.x > 0 ) {
	imageRect.x = imageRect.x - imageRect.width; //??? why
      }
    } else {
      if ( (imageRect.x+imageRect.width) < width ) {
        imageRect.x = width - imageRect.width;
        tool_delta = 0;
      }
      if ( imageRect.x > 0 ) {
        imageRect.x = 0;
        tool_delta = 0;
      }
    }


    /*
     * If the right edge of the tool is less than 0 or the
     * left edge of the tool is greater than the width of the canvas
     */
    for (int i=0; i<selected_tool; i++) {
      if ( grid.modulo_X ) {
        if ( (toolArray[i].x+tool_delta+toolArray[i].width) < 0 )
          tool_delta += imageRect.width;
        if ( (toolArray[i].x+tool_delta) > width )
          tool_delta -= imageRect.width;
      }
      toolArray[i].setLocation(toolArray[i].x+tool_delta,toolArray[i].y);
    }

    for (int i=selected_tool+1; i<toolArray.length; i++) {
      if ( grid.modulo_X ) {
        if ( (toolArray[i].x+tool_delta+toolArray[i].width) < 0 )
          tool_delta += imageRect.width;
        if ( (toolArray[i].x+tool_delta) > width )
          tool_delta -= imageRect.width;
      }
      toolArray[i].setLocation(toolArray[i].x+tool_delta,toolArray[i].y);
    }


    /*
     * If the right edge of the region is less than 0 or the
     * left edge of the region is greater than the width of the canvas
     */
    for (int i=0; i<regionArray.length; i++) {
      if ( grid.modulo_X ) {
        if ( (regionArray[i].x+tool_delta+regionArray[i].width) < 0 )
          tool_delta += imageRect.width;
        if ( (regionArray[i].x+tool_delta) > width )
          tool_delta -= imageRect.width;
      }
      regionArray[i].setLocation(regionArray[i].x+tool_delta,regionArray[i].y);
    }

  }

  /**
   * Causes the map to scroll an amount in the Y direction.
   * @param delta the number of pixels to scroll.
   */
  public synchronized void scroll_Y(int delta)
  {
    int tool_delta = delta;
    imageRect.y += delta;

    if ( (imageRect.y+imageRect.height) < height ) {
      imageRect.y = height - imageRect.height;
      tool_delta = 0;
    }
    if ( imageRect.y > 0 ) {
      imageRect.y = 0;
      tool_delta = 0;
    }

    for (int i=0; i<selected_tool; i++)
      toolArray[i].setLocation(toolArray[i].x,toolArray[i].y+tool_delta);

    for (int i=selected_tool+1; i<toolArray.length; i++)
      toolArray[i].setLocation(toolArray[i].x,toolArray[i].y+tool_delta);

    for (int i=0; i<regionArray.length; i++)
      regionArray[i].setLocation(regionArray[i].x,regionArray[i].y+tool_delta);

  }

  /**
   * Suspends scrolling.
   * <p>
   * Scrolling can be resumed with <code>resume_scrolling()</code>.
   */
  public synchronized void suspend_scrolling()
  {
      //scroller.suspend();
    try {
      while( scrollSuspended )
        wait();
    } catch( InterruptedException e) { }
  }

  /**
   * Resumes scrolling.
   * <p>
   * Scrolling can be suspended with <code>suspend_scrolling()</code>.
   */
  public synchronized void resume_scrolling()
  {
      //scroller.resume();
    scrollSuspended = !scrollSuspended;
    if( !scrollSuspended )
      notify();
  }

  /*
   * We need this in order to get the frame so we can change the cursor.
   */
  private JFrame getFrame()
  {
    Container parent = this.getParent();
    while ( (parent != null) && !(parent instanceof JFrame))
      parent = parent.getParent();
    return ((JFrame) parent);
  }


    //1.1 -----------------------------------------------------

  // implementation of MouseListener, MouseMotionListener
  public void mouseMoved(MouseEvent evt)
  {
    int type = getTool().mouseMove( evt.getX(), evt.getY() );
    JFrame frame = this.getFrame();
    if( frame != null)
       frame.setCursor( new Cursor(type) );
  }

  public void mousePressed(MouseEvent evt)
  {
    getTool().mouseDown( evt.getX(), evt.getY() );
    if ( getTool().is_active() ) {
      repaint();
    }
  }

  public void mouseDragged(MouseEvent evt)
  {
    getTool().mouseDrag( evt.getX(), evt.getY() );
    if ( getTool().is_active() ) {
      repaint();
    }
  }

  public void mouseReleased(MouseEvent evt)
  {
    getTool().mouseUp( evt.getX(), evt.getY() );
    repaint();
  }

  public void mouseEntered(MouseEvent evt) { }
  public void mouseExited(MouseEvent evt) { }
  public void mouseClicked(MouseEvent evt)
  {
    mousePressed( evt );
  }

/*----------------------------------------------------- 1.1

/*-------------------------V--------------------------- 1.0

  /**
   * Passes the event to the current tool.
   * <p>
   * If we are over the center tool change the cursor to MOVE_CURSOR.
   * This method returns <code>false</code> so the interface can be updated if desired.
   *
  public synchronized boolean mouseMove(Event evt, int mouse_x, int mouse_y)
  {
    int type = getTool().mouseMove(mouse_x, mouse_y);
    this.getFrame().setCursor(type);
    return false; // The user interface can update a textField at this point
  }

 
  /**
   * Passes the event to the current tool.
   * <p>
   * If the tool is active, the user values are updated.
   * This method returns <code>false</code> so the interface can be updated if desired.
   *
  public synchronized boolean mouseDown(Event evt, int mouse_x, int mouse_y)
  {
    getTool().mouseDown(mouse_x, mouse_y);
    if ( getTool().is_active() ) {
      repaint();
    }
    return false; // The user interface can update a textField at this point
  }

 
  /**
   * Passes the event to the current tool.
   * <p>
   * If the tool is active, the user values are updated.
   * This method returns <code>false</code> so the interface can be updated if desired.
   *
  public synchronized boolean mouseDrag(Event evt, int mouse_x, int mouse_y)
  {
    getTool().mouseDrag(mouse_x, mouse_y);
    if ( getTool().is_active() ) {
      repaint();
    }
    return false; // The user interface can update a textField at this point
  }
 

  /**
   * Passes the event to the current tool.
   * <p>
   * If the tool is active, the user values are updated.
   * This method returns <code>false</code> so the interface can be updated if desired.
   *
  public synchronized boolean mouseUp(Event evt, int mouse_x, int mouse_y)
  {
    getTool().mouseUp(mouse_x, mouse_y);
    repaint();
    return false; // The user interface can update a textField at this point
    }*/
 
/*-------------------------^--------------------------- 1.0

  /**
   * Increases the base image size the internally maintained zoom factor.
   *
   * @exception MaxZoomException already at max zoom.
   * @exception MinZoomException already at min zoom.
   */
  public synchronized void zoom_in() 
       throws MaxZoomException, MinZoomException
  {
    this.zoom(zoom_factor);
  }


  /**
   * Decreases the base image size the internally maintained zoom factor.
   *
   * @exception MaxZoomException already at max zoom.
   * @exception MinZoomException already at min zoom.
   */
  public synchronized void zoom_out()
       throws MaxZoomException, MinZoomException
  {
    this.zoom(1.0/zoom_factor);
  }


  /**
   * Increases/decreases the base image size by the specified zoom factor.
   *
   * @param zoom_factor.
   * @exception MaxZoomException already at max zoom.
   * @exception MinZoomException already at min zoom.
   */
  public synchronized void zoom(double zoom_factor)
       throws MaxZoomException, MinZoomException
  {

    Graphics g = getGraphics();
    double initial_scaling = image_scaling;

    {
      //1.1 This whole section is unnecessary when using
      //    the setClip() method in java 1.1.
      /*
       * We need to create a new off screen graphics context here
       * because of the rather odd, yet documented behavior of the
       * clipRect() method of a Graphics object:
       *
       * g.clipRect(Rectangle) generates a clipping region which is the 
       * INTERSECTION of the rectangle given and the PREVIOUS
       * clipping region!!!
       *
       * This means there is no way to increase a clipping region
       * for a particular graphics context.  You must instead
       * create a new graphics context whenever you want the
       * clipping region increased in size.
       *

      Dimension d = getSize();
      offDimension = d;
      offImage = createImage(d.width, d.height);
      offGraphics = offImage.getGraphics();*/
    }

    if ( (image_scaling * zoom_factor) > max_img_scaling ) {
      this.zoom(max_img_scaling/image_scaling);
      throw new MaxZoomException();
    } else if ( (image_scaling * zoom_factor) < min_img_scaling - 0.01) {
      this.zoom(min_img_scaling/image_scaling);
      throw new MinZoomException();
    } else
      image_scaling = image_scaling * zoom_factor;

    zoom_factor = image_scaling / initial_scaling;
    imageRect.width = (int)(base_image.getWidth(this)*image_scaling);
    imageRect.height = (int)(base_image.getHeight(this)*image_scaling);

    clip_width = (imageRect.width < width) ? imageRect.width : width;
    clip_height = (imageRect.height < height) ? imageRect.height : height;
    for (int i=0; i<toolArray.length; i++) {
      toolArray[i].applyClipRect(0, 0, clip_width, clip_height);
    }
   
    center_tool(zoom_factor);
    repaint();
    //getRootPane().getContentPane().repaint();

  }
 
 
  public void center_tool(double zoom_factor)
  {

    double [] regionArrayUser_X = new double[regionArray.length];
    double [] regionArrayUser_Y = new double[regionArray.length];
    for (int i=0; i<regionArray.length; i++) {
     regionArrayUser_X[i] = regionArray[i].user_X;
     regionArrayUser_Y[i] = regionArray[i].user_Y;
    }

    double [] toolArrayUser_X = new double[toolArray.length];
    double [] toolArrayUser_Y = new double[toolArray.length];
    for (int i=0; i<toolArray.length; i++) {
     toolArrayUser_X[i] = toolArray[i].user_X[LO];
     toolArrayUser_Y[i] = toolArray[i].user_Y[HI];
    }

    // Move the image so that the selected tool will be centered
    //
    if ( imageRect.width >= width ) {
      imageRect.x = (int) (width/2 - (getTool().x + getTool().width/2 - imageRect.x)*zoom_factor);
      imageRect.y = (int) (height/2 - (getTool().y + getTool().height/2 - imageRect.y)*zoom_factor);
    } else {
      imageRect.x = (int) (imageRect.width/2 - (getTool().x + getTool().width/2 - imageRect.x)*zoom_factor);
      imageRect.y = (int) (imageRect.height/2 - (getTool().y + getTool().height/2 - imageRect.y)*zoom_factor);
    }
    // Resize the tool
    //
    for (int i=0; i<toolArray.length; i++) {
      toolArray[i].width *= zoom_factor;
      toolArray[i].height *= zoom_factor;
    }

    for (int i=0; i<regionArray.length; i++) {
      regionArray[i].width *= zoom_factor;
      regionArray[i].height *= zoom_factor;
    }

    /*
     * Check the image width/height and change image_rect.x/.y if appropriate.
     */

    if ( !grid.modulo_X ) {
      if ( imageRect.width >= width ) {
        if ( (imageRect.width + imageRect.x) < width )
          imageRect.x = width - imageRect.width;
        imageRect.x = (imageRect.x > 0) ? 0 : imageRect.x;
      } else {
        imageRect.x = 0;
      }
    }
        
    
    if ( imageRect.height >= height ) {
      if ( (imageRect.height + imageRect.y) < height )
        imageRect.y = height - imageRect.height;
      imageRect.y = (imageRect.y > 0) ? 0 : imageRect.y;
    } else
      imageRect.y = 0;

    // This takes care of some checks associated with changing imageRect.x and imageRect.y
    //
    this.scroll_X(0);
    this.scroll_Y(0);

    clip_width = (imageRect.width < width) ? imageRect.width : width;
    clip_height = (imageRect.height < height) ? imageRect.height : height;
    for (int i=0; i<toolArray.length; i++) {
      toolArray[i].applyClipRect(0, 0, clip_width, clip_height);
    }

    for (int i=0; i<toolArray.length; i++) {
     toolArray[i].setUserLocation(toolArrayUser_X[i],toolArrayUser_Y[i]);
    }

    for (int i=0; i<regionArray.length; i++) {
     regionArray[i].setUserLocation(regionArrayUser_X[i],regionArrayUser_Y[i]);
    }

  }


  public int getSelected()
  {
    return selected_tool;
  }

  public MapTool getTool()
  {
    return toolArray[selected_tool];
  }

  public MapTool getTool(int i)
  {
    return toolArray[i];
  }

  public void newToolFromOld(int i, MapTool new_tool, MapTool old_tool)
  {
    int alteration=0;

    new_tool.setGrid(grid);
    new_tool.setRange_X(old_tool.range_X[LO],old_tool.range_X[HI]);
    new_tool.setRange_Y(old_tool.range_Y[LO],old_tool.range_Y[HI]);
    new_tool.setUser_X(old_tool.user_X[LO],old_tool.user_X[HI]);
    new_tool.setUser_Y(old_tool.user_Y[LO],old_tool.user_Y[HI]);
    new_tool.setSnapping(old_tool.getSnap_X(),old_tool.getSnap_Y());
    new_tool.drawHandles = old_tool.drawHandles;

    toolArray[i] = new_tool;
    toolArray[i].applyClipRect(0, 0, clip_width, clip_height);

    // The check_for_zero_range() function expands the
    // tool when necessary and may force us to update the
    // user_X/Y values.
    alteration = getTool().check_for_zero_range();
    if (alteration == 1 || alteration == 3)
      toolArray[i].setUser_X();
    if (alteration == 2 || alteration == 3)
      toolArray[i].setUser_Y();

    toolArray[i].saveHandles();
  }

  public void setTool(int i, MapTool tool)
  {
    int alteration=0;

    toolArray[i] = tool;
    toolArray[i].setGrid(grid);
    toolArray[i].applyClipRect(0, 0, clip_width, clip_height);
    toolArray[i].setUser_XY();
    toolArray[i].check_for_zero_range();
    toolArray[i].setUser_XY();
    if (i == selected_tool)
      toolArray[i].drawHandles = true;
    toolArray[i].saveHandles();
  }

  public void setToolArray(MapTool [] toolArray)
  {
    this.toolArray = toolArray;
    selected_tool = 0;
    toolArray[selected_tool].drawHandles = true;
    for (int i=0; i<toolArray.length; i++) {
      toolArray[i].setGrid(grid);
      toolArray[i].applyClipRect(0, 0, clip_width, clip_height);
      toolArray[i].setUser_XY();
    }
  }

  public void selectTool(int id)
  {
    for (int i=0; i<id; i++)
      toolArray[i].drawHandles = false;

    toolArray[id].drawHandles = true;

    for (int i=id+1; i<toolArray.length; i++)
      toolArray[i].drawHandles = false;

    if ( toolArray[id].getDelta_X() != 0 )
      grid.setDelta_X(toolArray[id].getDelta_X());

    if ( toolArray[id].getDelta_Y() != 0 )
      grid.setDelta_Y(toolArray[id].getDelta_Y());

    selected_tool = id;
    repaint();
  }

  public void setRegionArray(MapRegion [] regionArray)
  {
    this.regionArray = regionArray;
    for (int i=0; i<regionArray.length; i++) {
      regionArray[i].setGrid(grid);
      regionArray[i].setUserLocation();
    }
  }

  public void setGrid(MapGrid grid)
  {
    this.grid = grid;
    this.grid.imageRect = this.imageRect;
    for (int i=0; i<toolArray.length; i++) {
      toolArray[i].setGrid(this.grid);
    }
    for (int i=0; i<regionArray.length; i++) {
      regionArray[i].setGrid(grid);
    }
  }

  public MapGrid getGrid() {
    return grid;
  }

  public void setImage(ImageIcon image)
  {

    // JC_TODO: throw an exception if image = null
    if ( image != null ) {
	this.base_image = image.getImage();
	this.setIcon(image);
    }
    else
      System.out.println("null image passed to MapCanvas.  Reusing previous image.");

    scale_image_to_fit();

    Graphics g = getGraphics();
    repaint();
  }

  public Image get_image() { return base_image; }


  /**
   * Returns a string with information for initial positioning and
   * and sizing of the base map.  This information can be used to
   * initialize a new MapCanvas with the constructor which includes
   * the x, y and scaling parameters.
   */
  public String get_internals()
  {
    StringBuffer sbuf = new StringBuffer(imageRect.x +" " +imageRect.y +
    " " +image_scaling +
    " " + min_img_scaling +
    " " + max_img_scaling);
    return sbuf.toString();
  }
 

  /**
   * This method is necessary for layout managers.
   */
  public Dimension getMinimumSize() {
      //public Dimension minimumSize()
      //  {
    return new Dimension(width, height);
  }


  /**
   * This method is necessary for layout managers.
   */
  public Dimension getPreferredSize() {
      //public Dimension preferredSize()
      // {
    return this.getMinimumSize();
    //return this.minimumSize();
  }
 

  /*
   * Some intelligence to do initial sizing when a new image is received.
   */
  void scale_image_to_fit()
  {
    double vert_scaling = 1.0;
    double hor_scaling = 1.0;

    vert_scaling = (double)this.height / (double)base_image.getHeight(this);
    hor_scaling = (double)this.width / (double)base_image.getWidth(this);
    image_scaling = (vert_scaling < hor_scaling) ? vert_scaling : hor_scaling;

    if ( image_scaling < 0.1 ) {
      System.out.println("image scaling = " + image_scaling + ", being reset to 0.1.");
      image_scaling = 0.1;
    }
    min_img_scaling = image_scaling;

    imageRect.x = 0;
    imageRect.y = 0;
    imageRect.width = (int)(base_image.getWidth(this)*image_scaling);
    imageRect.height = (int)(base_image.getHeight(this)*image_scaling);
    grid.imageRect = this.imageRect;

    clip_width = (imageRect.width < width) ? imageRect.width : width;
    clip_height = (imageRect.height < height) ? imageRect.height : height;
  }


  /*
   * Some intelligence to do image initializing.
   */
  void position_and_scale_image(int x, int y, double scaling)
  {

    image_scaling = scaling;

    imageRect.x = x;
    imageRect.y = y;
    imageRect.width = (int)(base_image.getWidth(this)*image_scaling);
    imageRect.height = (int)(base_image.getHeight(this)*image_scaling);

    //grid.imageRect = this.imageRect;
    for (int i=0; i<toolArray.length; i++) {
      toolArray[i].grid.imageRect = imageRect;
    }

    clip_width = (imageRect.width < width) ? imageRect.width : width;
    clip_height = (imageRect.height < height) ? imageRect.height : height;

  }

}
//--------------------------------------------------------------------------
