//
// QTForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data. Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data.qt;

/**
 * QTForm is the VisAD data adapter for QuickTime movies.
 *
 * @deprecated Use visad.data.bio.LociForm with
 *   loci.formats.in.QTReader and loci.formats.out.QTWriter
 */
public class QTForm extends visad.data.bio.LociForm {

  public QTForm() {
    super(new loci.formats.in.QTReader(), new loci.formats.out.QTWriter());
  }

  public static void main(String[] args) throws Exception {
    new QTForm().testRead(args);
  }

}
