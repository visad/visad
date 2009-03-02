//
// VisADAppearance.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Image;

/**
   VisADAppearance stands in for j3d.Switch
   and is Serializable.<P>
*/
public class VisADAppearance extends VisADSceneGraphObject {

  public VisADGeometryArray array = null;
  public transient Image image = null;
  public boolean color_flag = false;
  public float red, green, blue;
  public float alpha;
  public float lineWidth = 1.0f;
  public float pointSize = 1.0f;
  public int lineStyle = GraphicsModeControl.SOLID_STYLE;

  // Serializable substitutes for image
  public int image_type = -1;
  public int image_width = 0;
  public int image_height = 0;
  public int[] image_pixels = null;
  public int texture_width = 0;
  public int texture_height = 0;

  public VisADAppearance() {
  }

}

