 
//
// ControlEvent.java
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

package visad;
 
import java.rmi.*;
import java.awt.Event;

/**
   ControlEvent is the VisAD class for changes in Control
   objects.  They are sourced by Control objects and
   received by ControlListener objects.<P>
*/
public class ControlEvent extends Event {

  private Control control; // source of event

  public ControlEvent(Control c) {
    // don't pass control as the source, since source
    // is transient inside Event
    super(null, 0, null);
    control = c;
  }

  /** get the Control that sent this ControlEvent (or a copy
      if the Control was on a different JVM) */
  public Control getControl() {
    return control;
  }

}

