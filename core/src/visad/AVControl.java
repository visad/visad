//
// AVControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;

/**
   AVControl is the VisAD interface for AnimationControl
   and ValueControl.<P>
*/
public interface AVControl {

  /**
   * remove all references to SwitchSet objects involving re
   * @param re - DataRenderer used to select SwitchSet objects
   */
  void clearSwitches(DataRenderer re);

  /**
   * in future, notify listener of changes in this AVControl
   * @param listener - ControlListener to notify
   */
  void addControlListener(ControlListener listener);

  /**
   * stop notifying listener of changes in this AVControl
   * @param listener - ControlListener to stop notifying
   */
  void removeControlListener(ControlListener listener);

  /**
   * @return String representation of this AVControl
   */
  String getSaveString();

  /**
   * reconstruct this AVControl using the specified save string
   * @param save - String representation for reconstruction
   */
  void setSaveString(String save)
    throws VisADException, RemoteException;

}

