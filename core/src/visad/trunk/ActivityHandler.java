/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

/**
 * Display activity handler supplied to
 * {@link DisplayImpl#addActivityHandler(ActivityHandler)
 *   DisplayImpl.addActivityHandler}.<br>
 * <br>
 * A trivial implementation which toggles between two Data objects
 * in a Display would be:<br>
 * <br>
 * <code><pre>
 *    class SwitchGIFs implements ActivityHandler
 *    {
 *      SwitchGIFs(LocalDisplay d) { toggle(d, true); }
 *      public void busyDisplay(LocalDisplay d) { toggle(d, false); }
 *      public void idleDisplay(LocalDisplay d) { toggle(d, true); }
 *      private void toggle(LocalDisplay d, boolean first) {
 *        java.util.Vector v = d.getRenderers();
 *        ((DataRenderer )v.get(0)).toggle(first);
 *        ((DataRenderer )v.get(1)).toggle(!first);
 *      }
 *    }
 * </pre></code>
 */
public interface ActivityHandler
{

  /**
   * Method called when the Display becomes busy.
   *
   * @param dpy Busy Display.
   */
  void busyDisplay(LocalDisplay dpy);

  /**
   * Method called after the Display has been idle long enough.
   *
   * @param dpy Idle Display.
   */
  void idleDisplay(LocalDisplay dpy);

}
