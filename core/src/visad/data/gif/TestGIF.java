/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.gif;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import visad.FlatField;
import visad.VisADException;

import visad.data.gif.GIFAdapter;

public class TestGIF
{
  public static void main(String args[])
  {
    if (args.length == 0) {
      args = new String[3];

      args[0] = "sseclogo.gif";
      args[1] = "http://www.ssec.wisc.edu/images/ssecsm.gif";
      args[2] = "http://www.ssec.wisc.edu/";
    }

    for (int i = 0; i < args.length; i++) {
      System.out.println("Testing \"" + args[i] + "\"");

      GIFAdapter gif = null;
      try {
	try {
	  gif = new GIFAdapter(new URL(args[i]));
	} catch (MalformedURLException e) {
	  gif = new GIFAdapter(args[i]);
	}
      } catch (IOException e) {
	System.err.println("Caught IOException for \"" + args[i] + "\": " +
			   e.getMessage());
	continue;
      } catch (VisADException e) {
	System.err.println("Caught VisADException for \"" + args[i] + "\": " +
			   e.getMessage());
	continue;
      }

      FlatField ff = gif.getData();
      if (ff == null) {
	System.out.println("\tNULL FlatField!");
      } else {
	System.out.println("\t" + ff.getType());
      }
    }
    System.out.println("Done");
    System.exit(0);
  }
}
