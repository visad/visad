
//
// AnimationControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

/**
   AnimationControl is the VisAD class for controlling Animation display scalars.<P>

   WLH - manipulate a list of Switch nodes in scene graph.<P>
*/
public class AnimationControl extends Control {

  private double current;
  private boolean direction; // true = forward
  private double ms; // time in milleseconds between animation steps
  private AnimationSetControl animationSet;
  private ToggleControl animate;

  static final AnimationControl prototype = new AnimationControl();

  public AnimationControl(DisplayImpl d) {
    super(d);
  }

  AnimationControl() {
    super();
  }

  // Java3D - need methods to step animtion, etc
 
  /** invoked every time values of this Control change */
  public void changeControl() {
    int step;
    try {
      double[][] value = new double[1][1];
      value[0][0] = current;
      int[] steps = animationSet.getSet().valueToIndex(value);
      step = steps[0];
    }
    catch (Exception e) {
      step = 0;
    }
    displayRenderer.setSwitch(step);
    incTick();
  }

  public Control cloneButContents(DisplayImpl d) {
    AnimationControl control = new AnimationControl(d);
    control.current = Double.NaN;
    control.direction = true;
    control.ms = 100;
    control.animationSet = new AnimationSetControl(d, this);
    d.addControl(control.animationSet);
    control.animate = new ToggleControl(d, this);
    d.addControl(control.animate);
    return control;
  }

  boolean subTicks(Renderer r, DataDisplayLink link) {
    return animationSet.checkTicks(r, link) || animate.checkTicks(r, link);
  }

  public Set getSet() {
    return animationSet.getSet();
  }

  public void setSet(Set s) {
    animationSet.setSet(s);
  }

}

