/*

@(#) $Id: WidgetLayout.java,v 1.6 2000-03-14 16:56:49 dglo Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 1998 Nick Rasmussen
VisAD is Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import java.awt.AWTError;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 * A simple layout manager for use in the visad ColorWidget.  Stacks the first two
 * components vertically, and stretches them to fit the panel.
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.6 $, $Date: 2000-03-14 16:56:49 $
 * @since Visad Utility Library, 0.5
 */

public class WidgetLayout implements LayoutManager
{

  /** The container that this layout manager is responsible for */
  private Container container;

  /** The current preferred X size of the container */
  private int preferredX;
  /** The current preferred Y size of the container */
  private int preferredY;

  /** The minimum layout size X component */
  private int minX;
  /** The minimum layout size Y component */
  private int minY;

  /** The maximum layout size X component */
  private int maxX;
  /** The maximum layout size Y component */
  private int maxY;

  /** Component widths (all windows) */
  private int width;
  /** Component0 height */
  private int height0;
  /** Component1 height */
  private int height1;


  /** Make a new WidgetLayout for the specified ColorWidget */
  public WidgetLayout(ColorWidget colorWidget) {
    container = colorWidget;
    calcDimensions();
  }

  /** Not used, no effect */
  public void addLayoutComponent(String name, Component component) {
  }

  /** Not used, no effect */
  public void removeLayoutComponent(Component component) {
  }


  /** Lay out the container */
  public void layoutContainer(Container parent) {

    if (parent != container) {
      throw new AWTError("WidgetLayout: got layoutContainer() with incorrect parent");
    }

    calcDimensions();

    int i = container.getComponentCount();

    switch (i) {
    case 0:
      break;

    case 2:
      container.getComponent(1).setBounds(container.getInsets().left,
                                          (int) Math.min((long) height0 + container.getInsets().top, Integer.MAX_VALUE),
                                          width, height1);
      //fall through
    case 1:
      container.getComponent(0).setBounds(container.getInsets().left,
                                          container.getInsets().top, width, height0);
      break;
    }

    return;
  }

  /** Return the minimum size for this layout */
  public Dimension minimumLayoutSize(Container parent) {

    if (parent != container) {
      throw new AWTError("WidgetLayout: got layoutContainer() with incorrect parent");
    }

    calcDimensions();

    return new Dimension(minX, minY);
  }

  /** Return the preferred size for this layout */
  public Dimension preferredLayoutSize(Container parent) {

    if (parent != container) {
      throw new AWTError("WidgetLayout: got layoutContainer() with incorrect parent");
    }

    calcDimensions();

    return new Dimension(preferredX, preferredY);
  }

  /** Return the maximum size for this layout */
  public Dimension maximumLayoutSize(Container parent) {

    if (parent != container) {
      throw new AWTError("WidgetLayout: got layoutContainer() with incorrect parent");
    }

    calcDimensions();

    return new Dimension(maxX, maxY);
  }


  /** Calculate the desired and required dimensions of all the components in this container */
  private void calcDimensions() {

    int i = container.getComponentCount();

    switch (i) {
    case 0:
      minX = 0;
      minY = 0;
      maxX = 0;
      maxY = 0;
      preferredX = 0;
      preferredY = 0;
      break;

    case 1:
      Component c = container.getComponent(0);
      minX = (int)Math.min((long)c.getMinimumSize().width + container.getInsets().right +
                           container.getInsets().left, (long) Integer.MAX_VALUE);
      minY = (int)Math.min((long)c.getMinimumSize().height + container.getInsets().top +
                           container.getInsets().bottom, (long) Integer.MAX_VALUE);

      minX = (int)Math.min((long)c.getMaximumSize().width + container.getInsets().right +
                           container.getInsets().left, (long) Integer.MAX_VALUE);
      minY = (int)Math.min((long)c.getMaximumSize().height + container.getInsets().top +
                           container.getInsets().bottom, (long) Integer.MAX_VALUE);

      preferredX = (int)Math.min((long)c.getPreferredSize().width + container.getInsets().right +
                                 container.getInsets().left, (long) Integer.MAX_VALUE);
      preferredY = (int)Math.min((long)c.getPreferredSize().height + container.getInsets().top +
                                 container.getInsets().bottom, (long) Integer.MAX_VALUE);

      width = container.getBounds().width;
      height0 = container.getBounds().height;
      height1 = 0;
      break;

    default:
      Component c0 = container.getComponent(0);
      Component c1 = container.getComponent(1);

      minX = (int)Math.min((long)c0.getMinimumSize().width + c1.getMinimumSize().width +
                           container.getInsets().right + container.getInsets().left, (long) Integer.MAX_VALUE);
      minY = (int)Math.min((long)c0.getMinimumSize().height + c1.getMinimumSize().height +
                           container.getInsets().top + container.getInsets().bottom, (long) Integer.MAX_VALUE);

      minX = (int)Math.min((long)c0.getMaximumSize().width + c1.getMaximumSize().width +
                           container.getInsets().right + container.getInsets().left, (long) Integer.MAX_VALUE);
      minY = (int)Math.min((long)c0.getMaximumSize().height + c1.getMaximumSize().height +
                           container.getInsets().top + container.getInsets().bottom, (long) Integer.MAX_VALUE);

      preferredX = (int)Math.min((long)c0.getPreferredSize().width + c1.getPreferredSize().width +
                                 container.getInsets().right + container.getInsets().left, (long) Integer.MAX_VALUE);
      preferredY = (int)Math.min((long)c0.getPreferredSize().height + c1.getPreferredSize().height +
                                 container.getInsets().top + container.getInsets().bottom, (long) Integer.MAX_VALUE);

      width = container.getBounds().width;
      height1 = Math.min(container.getBounds().height, c1.getPreferredSize().height);
      height0 = container.getBounds().height - height1;
      break;
    }

    return;
  }
}
