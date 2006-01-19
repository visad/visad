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

package visad.data;

import visad.Data;

public class DefaultFamilyTest
{
  /**
    * Test the DefaultFamily class by importing and then exporting a dataset.
    */
  public static void main(String[] args)
  {
    if (args.length != 2) {
      System.err.println("Usage: DefaultFamilyTest infile outfile");
      System.exit(1);
    }
    if (args[0].equals(args[1])) {
      System.err.println("Dataset specifications must be distinct");
      System.exit(1);
    }

    String in = args[0];
    String out = args[1];

    DefaultFamily df = new DefaultFamily("DefaultFamilyTest");

    System.out.println("Opening dataset " + in);
    try {
        Data data = df.open(in);
        System.out.println("Saving dataset " + out);
        try {
            df.save(out, data, true);
        }
        catch (Exception e) {
            System.err.println("Couldn't save dataset: " + e);
            System.exit(1);
        }
    }
    catch (Exception e) {
        System.err.println("Couldn't open dataset: " + e);
        System.exit(1);
    }

    System.exit(0);
  }
}
