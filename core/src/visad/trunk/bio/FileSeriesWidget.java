//
// FileSeriesWidget.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

/**
 * FileSeriesWidget is a GUI component for stepping through data
 * from a series of files.
 */
public class FileSeriesWidget extends BioStepWidget {

  // -- CONSTRUCTOR --

  /** Constructs a new FileSeriesWidget. */
  public FileSeriesWidget(BioVisAD biovis) { super(biovis, true); }


  // -- API METHODS --

  /** Updates the current file of the image series. */
  public void updateStep() { bio.sm.setIndex(cur - 1); }

  /** Gets whether the slider is busy (being adjusted). */
  public boolean isBusy() { return step.getValueIsAdjusting(); }


  // -- INTERNAL API METHODS --

  /** Updates the slider bounds. */
  public void updateSlider(int maximum) {
    super.updateSlider(maximum);
    boolean b = maximum > 0;
    bio.toolView.setEnabled(b);
    bio.toolMeasure.setEnabled(b);
  }

}
