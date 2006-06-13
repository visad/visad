//
// DumpType.java
//

/*

The software in this file is Copyright(C) 1998 by Tom Whittaker.
It is designed to be used with the VisAD system for interactive
analysis and visualization of numerical data.

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

package visad.jmet;


import visad.*;
import visad.Set;

import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;

import visad.data.*;


/**
 * Class of static methods for printing out information about VisAD
 * {@link Data} objects.
 *
 * @version $Revision: 1.13 $
 */
public class DumpType {

  /** initialization flag */
  private static boolean init = true;

  /** output stream to write to */
  private static OutputStream os;


  /**
   * Decomposes a VisAD Data object and lists out information
   * about its components.
   *
   * @param d      the VisAD Data object to analyze
   */
  public static void dumpDataType(Data d) {
    dumpDataType(d, System.out);
  }

  /**
   * Decomposes a VisAD Data object and lists out information
   * about its components.
   *
   * @param d      the VisAD Data object to analyze
   * @param uos    the OutputStream to send the text output to
   *               (usually use "System.out")
   *
   */
  public static void dumpDataType(Data d, OutputStream uos) {
    os = uos;
    dumpDT(d, " ");
    return;
  }

  /**
   * Internal method to actually do the dumping
   *
   * @param d         the VisAD Data object to analyze
   * @param prefix    prefix string for any output
   */
  private static void dumpDT(Data d, String prefix) {

    PrintWriter out = new PrintWriter(new OutputStreamWriter(os));
    if (init) {
      out.println("VisAD Data analysis");
    }
    init = false;

    try {

      if (d instanceof FlatField) {
        int nr = ((FlatField) d).getRangeDimension();
        int nd = ((FlatField) d).getDomainDimension();
        Set ds = ((FlatField) d).getDomainSet();
        prefix = prefix + "  ";
        out.println(prefix + " FlatField of length = "
                    + ((FlatField) d).getLength());
        out.println(prefix + " "
                    + ((FlatField) d).getType().prettyString());
        prefix = prefix + "  ";
        out.println(prefix + " Domain has " + nd + " components:");
        dumpDT(ds, prefix + "  ");
        dumpDomainCS(ds, prefix + "  ");
        out.println(prefix + " Range has " + nr + " components:");
        Set[]     dr      = ((FlatField) d).getRangeSets();
        float[][] samples = ((FlatField) d).getFloats(false);
        for (int i = 0; i < dr.length; i++) {
          dumpDT(dr[i], prefix + "   " + i + ".");
          int nmiss = 0;
          if (samples[i] == null) {
            nmiss = ((FlatField) d).getLength();
          } else {
            for (int j = 0; j < samples[i].length; j++) {
              if (samples[i][j] != samples[i][j]) {
                nmiss++;
              }
            }
          }
          out.println(prefix + "   " + i + ". number missing = "
                      + nmiss);
        }

      } else if (d instanceof FieldImpl) {
        int nd = ((FieldImpl) d).getDomainDimension();
        Set ds = ((FieldImpl) d).getDomainSet();
        out.println(prefix + " FieldImpl of length = "
                    + ((FieldImpl) d).getLength());
        out.println(prefix + " "
                    + ((FieldImpl) d).getType().prettyString());
        out.println(prefix + " Domain has " + nd + " components:");
        dumpDT(ds, prefix + "  ");
        out.println(prefix + " first sample = ");
        dumpDT(((FieldImpl) d).getSample(0, false), prefix + "   " + 0 + ".");

      } else if (d instanceof Field) {
        out.println(prefix + " Field: ");

      } else if (d instanceof Function) {
        out.println(prefix + " Function: ");
        out.println(prefix + "    Domain dimension= "
                    + ((Function) d).getDomainDimension());


      } else if (d instanceof Irregular3DSet) {
        out.println(prefix + " Irregular3DSet "
                    + name(((Irregular3DSet) d).getType().toString())
                    + " Length = " + ((Irregular3DSet) d).getLength());

      } else if (d instanceof Irregular2DSet) {
        out.println(prefix + " Irregular2DSet "
                    + name(((Irregular2DSet) d).getType().toString())
                    + " Length = " + ((Irregular2DSet) d).getLength());

      } else if (d instanceof Irregular1DSet) {
        out.println(prefix + " Irregular1DSet "
                    + name(((Irregular1DSet) d).getType().toString())
                    + " Length = " + ((Irregular1DSet) d).getLength());

      } else if (d instanceof IrregularSet) {
        out.println(prefix + " IrregularSet "
                    + name(((IrregularSet) d).getType().toString())
                    + " Length = " + ((IrregularSet) d).getLength());

      } else if (d instanceof Integer3DSet) {
        out.println(prefix + " Integer3DSet: Length = "
                    + ((Integer3DSet) d).getLength());

        for (int i = 0; i < 3; i++) {
          dumpDT(((Linear3DSet) d).getLinear1DComponent(i),
                 prefix + "   " + i + ".");
        }

      } else if (d instanceof Linear3DSet) {
        out.println(prefix + " Linear3DSet: Length = "
                    + ((Linear3DSet) d).getLength());

        for (int i = 0; i < 3; i++) {
          dumpDT(((Linear3DSet) d).getLinear1DComponent(i),
                 prefix + "   " + i + ".");
        }

      } else if (d instanceof Gridded3DDoubleSet) {
        out.println(prefix + " Gridded3DDoubleSet "
                    + name(((Gridded3DDoubleSet) d).getType().toString())
                    + " Length = " + ((Gridded3DDoubleSet) d).getLength());

      } else if (d instanceof Gridded3DSet) {
        out.println(prefix + " Gridded3DSet "
                    + name(((Gridded3DSet) d).getType().toString())
                    + " Length = " + ((Gridded3DSet) d).getLength());

      } else if (d instanceof Integer2DSet) {
        out.println(prefix + " Integer2DSet: Length = "
                    + ((Integer2DSet) d).getLength());

        for (int i = 0; i < 2; i++) {
          dumpDT(((Linear2DSet) d).getLinear1DComponent(i),
                 prefix + "   " + i + ".");
        }

      } else if (d instanceof Linear2DSet) {
        out.println(prefix + " Linear2DSet: Length = "
                    + ((Linear2DSet) d).getLength());

        for (int i = 0; i < 2; i++) {
          dumpDT(((Linear2DSet) d).getLinear1DComponent(i),
                 prefix + "   " + i + ".");
        }

      } else if (d instanceof Gridded2DDoubleSet) {
        out.println(prefix + " Gridded2DDoubleSet "
                    + name(((Gridded2DDoubleSet) d).getType().toString())
                    + " Length = " + ((Gridded2DDoubleSet) d).getLength());

      } else if (d instanceof Gridded2DSet) {
        out.println(prefix + " Gridded2DSet "
                    + name(((Gridded2DSet) d).getType().toString())
                    + " Length = " + ((Gridded2DSet) d).getLength());

      } else if (d instanceof Integer1DSet) {
        out.println(prefix + " Integer1DSet "
                    + name(((Integer1DSet) d).getType().toString())
                    + " Range = 0 to "
                    + (((Integer1DSet) d).getLength() - 1));

      } else if (d instanceof Linear1DSet) {
        out.println(prefix + " Linear1DSet "
                    + name(((Linear1DSet) d).getType().toString())
                    + " Range = " + ((Linear1DSet) d).getFirst()
                    + " to " + ((Linear1DSet) d).getLast() + " step "
                    + ((Linear1DSet) d).getStep());

      } else if (d instanceof Gridded1DDoubleSet) {
        out.println(prefix + " Gridded1DDoubleSet "
                    + name(((Gridded1DDoubleSet) d).getType().toString())
                    + "  Length = " + ((Gridded1DDoubleSet) d).getLength());

      } else if (d instanceof Gridded1DSet) {
        out.println(prefix + " Gridded1DSet "
                    + name(((Gridded1DSet) d).getType().toString())
                    + "  Length = " + ((Gridded1DSet) d).getLength());

      } else if (d instanceof IntegerNDSet) {
        out.println(prefix + " IntegerNDSet: Dimension = "
                    + ((IntegerNDSet) d).getDimension());

        for (int i = 0; i < ((IntegerNDSet) d).getDimension(); i++) {
          dumpDT(((LinearNDSet) d).getLinear1DComponent(i),
                 prefix + "   " + i + ".");
        }

      } else if (d instanceof LinearNDSet) {
        out.println(prefix + " LinearNDSet: Dimension = "
                    + ((LinearNDSet) d).getDimension());

        for (int i = 0; i < ((LinearNDSet) d).getDimension(); i++) {
          dumpDT(((LinearNDSet) d).getLinear1DComponent(i),
                 prefix + "   " + i + ".");
        }

      } else if (d instanceof GriddedSet) {
        out.println(prefix + " GriddedSet "
                    + name(((GriddedSet) d).getType().toString())
                    + "  Dimension = "
                    + ((GriddedSet) d).getDimension());

      } else if (d instanceof UnionSet) {
        out.println(prefix + " UnionSet "
                    + name(((UnionSet) d).getType().toString())
                    + "  Dimension = "
                    + ((UnionSet) d).getDimension());

      } else if (d instanceof ProductSet) {
        out.println(prefix + " ProductSet "
                    + name(((ProductSet) d).getType().toString())
                    + "  Dimension = "
                    + ((ProductSet) d).getDimension());

      } else if (d instanceof SampledSet) {
        out.println(prefix + " SampledSet "
                    + name(((SampledSet) d).getType().toString())
                    + "  Dimension = "
                    + ((SampledSet) d).getDimension());

      } else if (d instanceof FloatSet) {
        out.println(prefix + " FloatSet "
                    + name(((FloatSet) d).getType().toString())
                    + " Dimension = " + ((FloatSet) d).getDimension());

      } else if (d instanceof DoubleSet) {
        out.println(prefix + " DoubleSet "
                    + name(((DoubleSet) d).getType().toString())
                    + "  Dimension = "
                    + ((DoubleSet) d).getDimension());

      } else if (d instanceof SimpleSet) {
        out.println(prefix + " SimpleSet: ");

      } else if (d instanceof Set) {
        out.println(prefix + " Set: ");

      } else if (d instanceof RealTuple) {
        int n = ((RealTuple) d).getDimension();
        out.println(prefix + " RealTuple has " + n + " components:");
        Tuple df = (RealTuple) d;
        for (int i = 0; i < n; i++) {
          dumpDT(((RealTuple) d).getComponent(i), prefix + "   " + i + ".");
        }

      } else if (d instanceof Tuple) {
        int n = ((Tuple) d).getDimension();
        out.println(prefix + " Tuple has " + n + " components:");
        Tuple df = (Tuple) d;
        for (int i = 0; i < n; i++) {
          out.println("  ");
          dumpDT(((Tuple) d).getComponent(i), prefix + "   " + i + ".");
        }

      } else if (d instanceof Text) {
        out.println(prefix + " Text: " + d);

      } else if (d instanceof Real) {
        out.println(prefix + " Real: " + d);

      } else {
        out.println("Unknown type for " + d);
      }

    } catch (Exception e) {
      out.println("Exception:" + e);
      //System.exit(1);
      return;
    }
  }

  /**
   * Find the name of an Object in a String.  Looks for the last
   * index of "(".
   *
   * @param n    String to search
   *
   * @return substring up to the last index of "("
   */
  private static String name(String n) {
    // get stuff in parens...

    return (String) n.substring(n.lastIndexOf("("));
  }


  /**
   * Decomposes a VisAD MathType and lists out information
   * about its components
   *
   * @param t MathType to dump
   */
  public static void dumpMathType(MathType t) {
    dumpMathType(t, System.out);
  }

  /**
   * Decomposes a VisAD MathType and lists out information
   * about its components
   *
   * @param t     the VisAD MathType object to analyze
   * @param uos   the OutputStream to send the text output to
   *              (usually use "System.out")
   */
  public static void dumpMathType(MathType t, OutputStream uos) {
    os = uos;
    dumpMT(t, " ");
    return;
  }

  /**
   * Method to actually do the dumping
   *
   * @param t         MathType to dump.
   * @param prefix   prefix string for any output
   */
  private static void dumpMT(MathType t, String prefix) {
    PrintWriter out = new PrintWriter(new OutputStreamWriter(os));
    if (init) {
      out.println("VisAD MathType analysis");
    }
    init = false;

    try {

      if (t instanceof FunctionType) {
        out.println(prefix + " FunctionType: ");
        RealTupleType domain = ((FunctionType) t).getDomain();
        int           num    = domain.getDimension();
        out.println(prefix + " Domain has " + num + " components:");
        for (int i = 0; i < num; i++) {
          MathType comp = domain.getComponent(i);
          dumpMT(comp, prefix + "  " + i + ".");
        }

        out.println(prefix + " Range:");
        MathType range = ((FunctionType) t).getRange();
        dumpMT(range, prefix + "  ");


      } else if (t instanceof SetType) {
        out.println(prefix + " SetType: " + t);

      } else if (t instanceof RealTupleType) {
        int num = ((RealTupleType) t).getDimension();
        out.println(prefix + " RealTupleType has " + num
                           + " components:");
        for (int i = 0; i < num; i++) {
          MathType comp = ((RealTupleType) t).getComponent(i);
          dumpMT(comp, prefix + "  " + i + ".");
        }


      } else if (t instanceof TupleType) {
        int num = ((TupleType) t).getDimension();
        out.println(prefix + " TupleType has " + num + " components:");
        for (int i = 0; i < num; i++) {
          MathType comp = ((TupleType) t).getComponent(i);
          dumpMT(comp, prefix + "  " + i + ".");
        }

      } else if (t instanceof TextType) {
        out.println(prefix + " TextType: " + t);

      } else if (t instanceof RealType) {
        out.println(prefix + " RealType: " + t);
        prefix = prefix + "  ";
        out.println(prefix + " Name = " + ((RealType) t).toString());
        Unit   du = ((RealType) t).getDefaultUnit();
        String s  = null;
        if (du != null) {
          s = du.toString();
        }
        if (s != null) {
          out.println(prefix + " Unit: " + s);
        }

        Set ds = ((RealType) t).getDefaultSet();
        if (ds != null) {
          MathType dsmt = ds.getType();
          out.println(prefix + " Set: " + dsmt);
          // dumpMT(dsmt,prefix+"  ");
        }

      } else if (t instanceof ScalarType) {
        out.println(prefix + " ScaleType: " + t);

      } else {
        out.println("Unknown type for " + t);
      }

    } catch (Exception e) {
      out.println("Exception:" + e);
      //System.exit(1);

      return;
    }

  }

  /**
   * Print out a String representation of the CoordinateSystem of a
   * Domain set.
   *
   * @param s        Set to check
   * @param prefix   prefix for any output
   */
  private static void dumpDomainCS(Set s, String prefix) {
    RealTupleType    rtt = ((SetType) s.getType()).getDomain();
    CoordinateSystem cs  = s.getCoordinateSystem();
    if (cs != null) {
      RealTupleType ref = cs.getReference();
      if (ref != null) {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(os));
        out.println(prefix + " CoordinateSystem: "
                    + rtt.prettyString() + " ==> "
                    + ref.prettyString());
      }
    }
  }

  /**
   * Test this class by running 'java visad.jmet.DumpType'.  Reads
   * in a data file using the default VisAD data reader family and
   * dumps out the type of the data object.
   *
   * @param args name of file or URL to read in and analyze
   */
  public static void main(String args[]) {

    if (args.length < 1) {
      System.err.println("Usage: visad.jmet.DumpType <infile> ");
      System.exit(1);
      return;
    }

    DefaultFamily fr  = new DefaultFamily("sample");

    URL           url = null;
    try {
      url = new URL(args[0]);
    } catch (MalformedURLException exc) {
      ;
    }

    try {
      Data data;
      if (url != null) {
        System.out.println("Trying URL " + url.toString());
      } else {
        System.out.println("Trying file " + args[0]);
      }
      if (url == null) {
        data = fr.open(args[0]);
      } else {
        data = fr.open(url);
      }
      System.out.println(args[0] + ": " + data.getType().prettyString());
      System.out.println("  ");
      if (data != null) {

        dumpDataType(data, System.out);
        MathType t = data.getType();
        init = true;
        System.out.println("  ");
        dumpMathType(t, System.out);
      }
    } catch (Exception e) {
      System.out.println(e);
      System.exit(1);
    }
    System.exit(0);
  }
}
