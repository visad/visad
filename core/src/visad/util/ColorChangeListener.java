/*

@(#) $Id: ColorChangeListener.java,v 1.6 2000-03-14 16:56:47 dglo Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2020 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.EventListener;

/**
 * The interface that all objects must implement to recieve color change
 * events from the color widget
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.6 $, $Date: 2000-03-14 16:56:47 $
 * @since Visad Utility Library, 0.5
 */

public interface ColorChangeListener extends EventListener {

  /** The function called when the color widget has changed */
  void colorChanged(ColorChangeEvent e);
}
