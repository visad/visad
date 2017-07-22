//
// AddeTextAdapter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.mcidas;

import edu.wisc.ssec.mcidas.adde.AddeTextReader;
import visad.Text;
import visad.TextType;
import visad.VisADException;

/** 
 *  Adapts text read from an ADDE server into a VisAD Text object
 */

public class AddeTextAdapter {

  private Text data = null;
  private static int nextID = 0;

  /** Create a VisAD Text object from an adde request
    * @param textSource  ADDE URL of request
    * @exception IOException if there was a problem reading the file.
    * @exception VisADException if an unexpected problem occurs.
    */
  public AddeTextAdapter(String textSource) throws VisADException {
      AddeTextReader atr = new AddeTextReader(textSource);
      String name = "AddeText_"+nextID++;
      TextType type = TextType.getTextType(name);
      data = new Text(type, atr.getText());
  }

  /**
   * Return the Text object representing the request. 
   * @return request as Text
   */
  public Text getData() {
    return data;
  }

  /**
   * Return the Text object representing the request with HTML formatting. 
   * @return request as Text bracketed with <PRE> tags
   */
  public Text getDataAsHTML() {
    StringBuffer buf = new StringBuffer();
    buf.append("<html>");
    buf.append("\n");
    buf.append("<pre>");
    buf.append("\n");
    buf.append(data.getValue());
    buf.append("\n");
    buf.append("</pre>");
    buf.append("\n");
    buf.append("</html>");
    Text newText = null;
    try {
       newText = new Text((TextType) data.getType(), buf.toString());
    }
    catch (VisADException ve) {}
    return newText;
  }
}
