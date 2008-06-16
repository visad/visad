/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.fits;

import nom.tam.fits.BadHeaderException;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Data;
import nom.tam.fits.ExtensionHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.PrimaryHDU;
import nom.tam.fits.RandomGroupsHDU;
import nom.tam.fits.TruncatedFileException;

import java.io.IOException;
import java.io.PrintStream;

import java.util.Date;

public class DumpHeader
{
  private static void dumpBasic(PrintStream ps, String indentStr,
				BasicHDU hdu)
  {
    int bitpix;
    try {
      bitpix = hdu.getBitPix();

      String bpName;
      switch (bitpix) {
      case BasicHDU.BITPIX_BYTE:
	bpName = "byte";
	break;
      case BasicHDU.BITPIX_SHORT:
	bpName = "short";
	break;
      case BasicHDU.BITPIX_INT:
	bpName = "int";
	break;
      case BasicHDU.BITPIX_FLOAT:
	bpName = "float";
	break;
      case BasicHDU.BITPIX_DOUBLE:
	bpName = "double";
	break;
      default:
	bpName = "?" + bitpix + '?';
	break;
      }
      ps.println(indentStr + "bitpix " + bpName);
    } catch (FitsException e) {
      ps.println(indentStr + "bitpix *** " + e.getMessage());
    }

    int[] axes;
    try {
      axes = hdu.getAxes();
    } catch (FitsException e) {
      System.err.println("Bad axes: " + e.getMessage());
      axes = null;
    }
    if (axes != null) {
      ps.print(indentStr + "axes ");
      for (int i = 0; i < axes.length; i++) {
	ps.print((i == 0 ? "" : "x") + axes[i]);
      }
      ps.println("");
    }

    int paramCount = hdu.getParameterCount();
    if (paramCount != 0) {
      ps.println(indentStr + "paramCount " + paramCount);
    }

    int groupCount = hdu.getGroupCount();
    if (groupCount != 1) {
      ps.println(indentStr + "groupCount " + groupCount);
    }

    double bzero = hdu.getBZero();
    if (bzero != 0.0) {
      ps.println(indentStr + "bzero " + bzero);
    }

    double bscale = hdu.getBScale();
    if (bscale != 1.0) {
      ps.println(indentStr + "bscale " + bscale);
    }

    String bunit = hdu.getBUnit();
    if (bunit != null) {
      ps.println(indentStr + "bunit " + bunit);
    }

    try {
      int blankValue = hdu.getBlankValue();
      ps.println(indentStr + "blank " + blankValue);
    } catch (FitsException e) {
    }

    Date creation = hdu.getCreationDate();
    if (creation != null) {
      ps.println(indentStr + "creation date " + creation);
    }

    Date observation = hdu.getObservationDate();
    if (observation != null) {
      ps.println(indentStr + "observation date " + observation);
    }

    String origin = hdu.getOrigin();
    if (origin != null) {
      ps.println(indentStr + "origin " + origin);
    }

    String telescope = hdu.getTelescope();
    if (telescope != null) {
      ps.println(indentStr + "telescope " + telescope);
    }

    String instrument = hdu.getInstrument();
    if (instrument != null) {
      ps.println(indentStr + "instrument " + instrument);
    }

    String observer = hdu.getObserver();
    if (observer != null) {
      ps.println(indentStr + "observer " + observer);
    }

    String object = hdu.getObject();
    if (object != null) {
      ps.println(indentStr + "object " + object);
    }

    double equinox = hdu.getEquinox();
    if (equinox != -1.0) {
      ps.println(indentStr + "equinox " + equinox);
    }

    String author = hdu.getAuthor();
    if (author != null) {
      ps.println(indentStr + "author " + author);
    }

    String reference = hdu.getReference();
    if (reference != null) {
      ps.println(indentStr + "reference " + reference);
    }

    double maxValue = hdu.getMaximumValue();
    if (maxValue != 0.0) {
      ps.println(indentStr + "maximum value " + maxValue);
    }

    double minValue = hdu.getMinimumValue();
    if (minValue != 0.0) {
      ps.println(indentStr + "minimum value " + minValue);
    }
  }

  private static void dumpPrimary(PrintStream ps, String indentStr,
				  PrimaryHDU hdu)
	throws IOException
  {
    dumpBasic(ps, indentStr, (BasicHDU )hdu);

    Data foo = hdu.getData();
  }

  private static void dumpBinaryTable(PrintStream ps, String indentStr,
				      BinaryTableHDU hdu)
  {
    int num = hdu.getNumColumns();
    if (num == 0) {
      ps.println(indentStr + "No columns");
      return;
    }

    for (int i = 0; i < num; i++) {
      String name, type;
      try {
	name = hdu.getColumnName(i);
	type = hdu.getColumnFITSType(i);
      } catch (FitsException e) {
	break;
      }

      ps.println(indentStr + i + ": " + name + " = " + type);
    }
  }

  private static void dumpExtension(PrintStream ps, String indentStr,
				    ExtensionHDU hdu)
	throws IOException
  {
    dumpBasic(ps, indentStr, (BasicHDU )hdu);

    String name = hdu.getExtensionName();
    if (name != null) {
      ps.println(indentStr + "name " + name);
    }

    int vers = hdu.getExtensionVersion();
    if (vers != 1) {
      ps.println(indentStr + "version " + vers);
    }

    int level = hdu.getExtensionLevel();
    if (level != 1) {
      ps.println(indentStr + "level " + level);
    }

    if (hdu instanceof BinaryTableHDU) {
      ps.println(indentStr + "Binary Table:");
      dumpBinaryTable(ps, indentStr + indentStr, (BinaryTableHDU )hdu);
    } else {
      try {
	String type = hdu.getExtensionType();
	if (type == null) {
	  ps.println(indentStr + "Null extension type");
	} else {
	  ps.println(indentStr + "type " + type);
	}
      } catch (FitsException e) {
	ps.println(indentStr + indentStr + "Bad extension type: " +
		   e.getMessage());
      }
    }

    Data foo = hdu.getData();
  }

  private static void dumpImage(PrintStream ps, String indentStr, ImageHDU hdu)
	throws IOException
  {
    dumpBasic(ps, indentStr, (BasicHDU )hdu);

    ps.println(indentStr + "...");

    Data foo = hdu.getData();
  }

  private static void dumpRandomGroups(PrintStream ps, String indentStr,
				       RandomGroupsHDU hdu)
	throws IOException
  {
    dumpBasic(ps, indentStr, (BasicHDU )hdu);

    ps.println(indentStr + "...");

    Data foo = hdu.getData();
  }

  public static void dump(PrintStream ps, String name)
	throws FitsException, IOException
  {
    Fits fits;
    try {
      fits = new Fits(name);
    } catch (FitsException e) {
      System.err.println("Couldn't open \"" + name + "\": " + e.getMessage());
      return;
    }

    ps.println(name + ':');

    BasicHDU hdu;
    for (int hduNum = 0; true; hduNum++) {
      try {
	hdu = fits.readHDU();
      } catch (OutOfMemoryError e) {
	System.err.println("  *** Out of memory for HDU #" + hduNum);
	e.printStackTrace(System.err);
	break;
      } catch (TruncatedFileException e) {
	System.err.println("  *** File truncated at HDU #" + hduNum + " (" + e.getMessage() + ")");
	break;
      } catch (IOException e) {
	System.err.println("  *** I/O error at HDU #" + hduNum + " (" + e.getMessage() + ")");
	break;
      } catch (BadHeaderException e) {
	System.err.println("  *** HDU #" + hduNum + " threw " + e.getMessage());
	continue;
      } catch (FitsException e) {
	System.err.println("  *** HDU #" + hduNum + " threw " + e.getMessage());
	continue;
      }

      if (hdu == null)  {
	break;
      }

      String indentStr = "\t";

      if (hdu instanceof PrimaryHDU) {
	if (hduNum == 0) {
	  ps.println(indentStr + "Primary:");
	} else {
	  ps.println(indentStr + "Primary " + hduNum + ':');
	}
	ps.flush();
	dumpPrimary(ps, indentStr + indentStr, (PrimaryHDU )hdu);
      } else if (hdu instanceof ExtensionHDU) {
	ps.println(indentStr + "Extension " + hduNum + ':');
	ps.flush();
	dumpExtension(ps, indentStr + indentStr, (ExtensionHDU )hdu);
      } else if (hdu instanceof ImageHDU) {
	ps.println(indentStr + "Image " + hduNum + ':');
	ps.flush();
	dumpImage(ps, indentStr + indentStr, (ImageHDU )hdu);
      } else if (hdu instanceof RandomGroupsHDU) {
	ps.println(indentStr + "RandomGroups " + hduNum + ':');
	ps.flush();
	dumpRandomGroups(ps, indentStr + indentStr, (RandomGroupsHDU )hdu);
      } else {
	throw new FitsException("Unknown header found: " + hdu);
      }
    }
  }

  public static void main(String[] args)
  {
    for (int i = 0; i < args.length; i++) {
      try {
	dump(System.out, args[i]);
      } catch (OutOfMemoryError e) {
	e.printStackTrace(System.out);
      } catch (FitsException e) {
	e.printStackTrace(System.out);
      } catch (IOException e) {
	e.printStackTrace(System.out);
      }
    }
  }
}
