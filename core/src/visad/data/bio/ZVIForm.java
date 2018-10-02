//
// ZVIForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.bio;

/**
 * ZVIForm is a VisAD data adapter for Zeiss ZVI files.
 *
 * @deprecated Use LociForm with loci.formats.in.ZeissZVIReader
 */
public class ZVIForm extends LociForm {

  public ZVIForm() {
    super(new loci.formats.in.ZeissZVIReader());
  }

  public static void main(String[] args) throws Exception {
    new ZVIForm().testRead(args);
  }

}
