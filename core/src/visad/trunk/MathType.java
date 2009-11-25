//
// MathType.java
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

import java.rmi.*;
import java.util.Vector;
import java.util.Hashtable;

/**
   MathType is the superclass for VisAD's hierarchy of mathematical types.
   It encompasses the mathematical concepts of scalars, tuples (i.e.,
   n-dimensional vectors), functions, and certain forms of sets.<p>

   VisAD <b>Data</b> objects are finite approximations to math objects.
   Every Data object possesses a MathType, which characterizes the
   mathematical object that the data approximate.  This MathType is not
   synonymous with the class of the Data object, even though the class
   names for a Data object and its corresponding MathType object (Set
   and SetType, e.g.) may be similar.<p>

   MathType objects are immutable; one implication is that the setDefaultSet
   method (in RealTupleType) can be invoked only <b>prior</b> to using the
   related getDefaultSet method.<p>
*/
public abstract class MathType extends Object implements java.io.Serializable {

  /** true if this MathType is defined by the system */
  boolean SystemIntrinsic;

  /** this constructor assumes it is not creating an instrinsic MathType */
  public MathType() {
    this(false);
  }

  /**
   * Create a MathType
   *
   * @param b <tt>true</tt> if this is an intrinsic MathType
   */
  MathType(boolean b) {
    super();
    SystemIntrinsic = b;
  }

  /** ScalarTypes are equal if they have the same name;
      TupleTypes are equal if their components are equal;
      FunctionTypes are equal if their domains and ranges
      are equal */
  public abstract boolean equals(Object type);

  /** this is useful for determining compatibility of
      Data objects for binary mathematical operations;
      any RealTypes are equal; any TextTypes are equal;
      TupleTypes are equal if their components are equal;
      FunctionTypes are equal if their domains and ranges
      are equal */
  public abstract boolean equalsExceptName(MathType type);

  /* TDR - May 1998.  As above, except units must be convertible */
  public abstract boolean equalsExceptNameButUnits( MathType type )
           throws VisADException;

  /* TDR - June 1998           */
  public abstract MathType cloneDerivative( RealType d_partial )
           throws VisADException;

  /* TDR - July 1998  */
  public abstract MathType binary( MathType type, int op, Vector names )
         throws VisADException;

  /* TDR - July 1998 */
  public abstract MathType unary( int op, Vector names )
         throws VisADException;

  /** returns a missing Data object for any MathType */
  public abstract Data missingData() throws VisADException, RemoteException;

  public abstract ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
    throws VisADException, RemoteException;

/* WLH 5 Jan 2000
  public abstract String toString();
*/
  public String toString() {
    return prettyString(0);
  }

  /** return a String that indents complex MathTypes
      for human readability */
  public String prettyString() {
    return prettyString(0);
  }

  public abstract String prettyString(int indent);

  /** create a MathType from its string representation;
      essentially the inverse of the prettyString method */
  public static MathType stringToType(String s) throws VisADException {
    int length = s.length();
    String r = "";
    for (int i=0; i<length; i++) {
      String t = s.substring(i, i+1);
      if (!t.equals(" ") && !t.equals("\t") && !t.equals("\n")) {
        r = r + t;
      }
    }
    length = r.length();
    if (length == 0) {
      throw new TypeException("MathType.stringToType: badly formed string");
    }
    int[] len = {length};
    MathType type = stringToType(r, len);
    if (length != len[0]) {
      throw new TypeException("MathType.stringToType: badly formed string");
    }
    return type;
  }

  private static MathType stringToType(String s, int[] len)
          throws VisADException {
    MathType ret_type;
    String s0 = s.substring(0, 1);
    if (s.startsWith("Set") || s.startsWith("SET") || s.startsWith("set")) {
      String sr = s.substring(3);
      int[] lensr = {sr.length()};
      MathType type0 = stringToType(sr, lensr);
      if (type0 instanceof RealType) {
        ret_type = new SetType((RealType) type0);
      }
      else if (type0 instanceof RealTupleType) {
        ret_type = new SetType((RealTupleType) type0);
      }
      else {
        throw new TypeException("MathType.stringToType: badly formed string");
      }
      len[0] = 3 + lensr[0];
      return ret_type;
    }
    else if (s0.equals("(")) {
      String sr = s.substring(1);
      int[] lensr = {sr.length()};
      MathType type0 = stringToType(sr, lensr);
      String t = sr.substring(lensr[0]);
      if (type0 == null || t == null || t.equals("")) {
        throw new TypeException("MathType.stringToType: badly formed string");
      }
      if (t.startsWith("->")) {
        if (!(type0 instanceof RealType) &&
            !(type0 instanceof RealTupleType)) {
          throw new TypeException("MathType.stringToType: badly formed string");
        }
        String tr = t.substring(2);
        int[] lentr = {tr.length()};
        MathType type1 = stringToType(tr, lentr);
        t = tr.substring(lentr[0]);
        if (!t.startsWith(")") || type1 == null) {
          throw new TypeException("MathType.stringToType: badly formed string");
        }
        len[0] = 1 + lensr[0] + 2 + lentr[0] + 1;
        ret_type = new FunctionType(type0, type1);
        return ret_type;
      }
      else {
        Vector v = new Vector();
        v.addElement(type0);
        int lentup = 1 + lensr[0];
        while (t.startsWith(",")) {
          String tr = t.substring(1);
          int[] lentr = {tr.length()};
          MathType type1 = stringToType(tr, lentr);
          if (type1 == null) {
            throw new TypeException("MathType.stringToType: badly formed string");
          }
          v.addElement(type1);
          lentup = lentup + 1 + lentr[0];
          t = tr.substring(lentr[0]);
        }
        if (!t.startsWith(")")) {
          System.out.println(t);
          throw new TypeException("MathType.stringToType: badly formed string");
        }
        len[0] = lentup + 1;
        MathType[] types = new MathType[v.size()];
        boolean all_real = true;
        for (int i=0; i<v.size(); i++) {
          types[i] = (MathType) v.elementAt(i);
          all_real &= (types[i] instanceof RealType);
        }
        if (all_real) {
          RealType[] rtypes = new RealType[v.size()];
          for (int i=0; i<v.size(); i++) {
            rtypes[i] = (RealType) types[i];
          }
          ret_type = new RealTupleType(rtypes);
        }
        else {
          ret_type = new TupleType(types);
        }
        return ret_type;
      }
    }
    else if ((0 <= s0.compareTo("a") && s0.compareTo("z") <= 0) ||
             (0 <= s0.compareTo("A") && s0.compareTo("Z") <= 0)) {
      for (int i=1; i<len[0]; i++) {
        s0 = s.substring(i, i+1);
        if (!((0 <= s0.compareTo("a") && s0.compareTo("z") <= 0) ||
              (0 <= s0.compareTo("A") && s0.compareTo("Z") <= 0) ||
              (0 <= s0.compareTo("0") && s0.compareTo("9") <= 0) ||
              s0.equals("_") || 
              // add in some other valid chars
              s0.equals("%") || s0.equals("+") ||
              s0.equals("/") || s0.equals(":") ||
              s0.equals("[") || s0.equals("]") || s0.equals("^"))) {
          len[0] = i;
          break;
        }
      }
      String rs = s.substring(0, len[0]);
      String t = s.substring(len[0]);
      if (t.startsWith("(Text)")) {
        ret_type = TextType.getTextType(rs);
        len[0] += 6;
      }
      else {
        ret_type = RealType.getRealType(rs);
      }
      return ret_type;
    }
    else {
      throw new TypeException("MathType.stringToType: badly formed string");
    }
  }

  private static Vector timeAliases = makeTimeAliasVector();

  private static Vector makeTimeAliasVector() {
    Vector v = new Vector();
    v.add("time");
    v.add("Time");
    v.add("TIME");
    return v;
  }

  /** Adds a ScalarType name that guessMaps should map to Animation. */
  public static void addTimeAlias(String name) {
    synchronized (timeAliases) {
      timeAliases.add(name);
    }
  }

  /** Guesses at a set of &quot;default&quot; mappings for this MathType.
      Intuitively, first we look for a FunctionType with domain dimension 3,
      then a nested group of FunctionTypes with 'cumulative' domain
      dimension 3. Next we look for a FunctionType or nested group of
      FunctionTypes with domain dimension 2. Then, we look for a
      FunctionType with domain dimension 1. Nested groups of FunctionTypes
      may be nested with TupleTypes, which is indicated by some of the
      MathType templates. Lastly, if no matching FunctionTypes are found,
      then we look for 3-D, 2-D, or 1-D SetTypes. */
  public ScalarMap[] guessMaps(boolean threeD) {
    MathType m = this;

    // set up aliases for "time" RealType to be mapped to Animation
    DataStruct[][] ds;
    synchronized (timeAliases) {
      int len = timeAliases.size();
      ds = new DataStruct[1][len];
      for (int i=0; i<len; i++) {
        String name = (String) timeAliases.elementAt(i);
        ds[0][i] = new DataStruct(name);
      }
    }

    // find a FunctionType whose 1-D domain is a RealType matching above
    findTimeFunction(m, ds, null);
    int timeFunc = -1;
    for (int i=0; i<ds[0].length; i++) {
      if (ds[0][i].fvalid && ds[0][i].funcs.size() > 0) {
        timeFunc = i;
        break;
      }
    }

    // compile a FunctionType and SetType list to search for template matches
    Vector flist = new Vector(); // functions
    Vector slist = new Vector(); // sets
    Vector tlist = new Vector(); // tuples of reals
    if (timeFunc < 0) buildTypeList(m, flist, slist, tlist);
    else {
      // found a "time" RealType; only search ranges of "time" FunctionTypes
      for (int i=0; i<ds[0][timeFunc].funcs.size(); i++) {
        FunctionType f = (FunctionType) ds[0][timeFunc].funcs.elementAt(i);
        buildTypeList(f.getRange(), flist, slist, tlist);
      }
    }

    // look for matches between Function templates and FunctionTypes list
    int numfuncs = flist.size();
    for (int t=(threeD ? 0 : 4); t<7; t++) {
      for (int fi=0; fi<numfuncs; fi++) {
        FunctionType ft = (FunctionType) flist.elementAt(fi);
        switch (t) {
          case 0: // 3-D ONLY
            //   ((x, y, z) -> (..., a, ...))
            //   ((x, y, z) -> a)
            // x -> X, y -> Y, z -> Z, a -> IsoContour
            if (!ft.getFlat()) break;
            RealTupleType domain = ft.getDomain();
            if (domain.getDimension() != 3) break;
            MathType range = ft.getRange();
            RealType x, y, z;
            CoordinateSystem cs = domain.getCoordinateSystem();
            if (cs != null) {
              RealTupleType ref = cs.getReference();
              try {
                x = (RealType) ref.getComponent(0);
                y = (RealType) ref.getComponent(1);
                z = (RealType) ref.getComponent(2);
              }
              catch (VisADException exc) {
                break;
              }
            }
            else {
              try {
                x = (RealType) domain.getComponent(0);
                y = (RealType) domain.getComponent(1);
                z = (RealType) domain.getComponent(2);
              }
              catch (VisADException exc) {
                break;
              }
            }
            RealType a;
            if (range instanceof RealType) {
              a = (RealType) range;
              try {
                ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 4 : 5];
                if (RealType.Latitude.equals(x)) {
                  smaps[0] = new ScalarMap(x, Display.YAxis);
                  smaps[1] = new ScalarMap(y, Display.XAxis);
                }
                else {
                  smaps[0] = new ScalarMap(x, Display.XAxis);
                  smaps[1] = new ScalarMap(y, Display.YAxis);
                }
                smaps[2] = new ScalarMap(z, Display.ZAxis);
                smaps[3] = new ScalarMap(a, Display.IsoContour);
                if (timeFunc >= 0) {
                  Object o = ds[0][timeFunc].funcs.elementAt(0);
                  RealTupleType rtt = ((FunctionType) o).getDomain();
                  RealType time = (RealType) rtt.getComponent(0);
                  smaps[4] = new ScalarMap(time, Display.Animation);
                }
                return smaps;
              }
              catch (VisADException exc) { }
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType mt;
                try {
                  mt = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  break;
                }
                if (mt instanceof RealType) {
                  a = (RealType) mt;
                  try {
                    ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 4 : 5];
                    if (RealType.Latitude.equals(x)) {
                      smaps[0] = new ScalarMap(x, Display.YAxis);
                      smaps[1] = new ScalarMap(y, Display.XAxis);
                    }
                    else {
                      smaps[0] = new ScalarMap(x, Display.XAxis);
                      smaps[1] = new ScalarMap(y, Display.YAxis);
                    }
                    smaps[2] = new ScalarMap(z, Display.ZAxis);
                    smaps[3] = new ScalarMap(a, Display.IsoContour);
                    if (timeFunc >= 0) {
                      Object o = ds[0][timeFunc].funcs.elementAt(0);
                      RealTupleType rtt = ((FunctionType) o).getDomain();
                      RealType time = (RealType) rtt.getComponent(0);
                      smaps[4] = new ScalarMap(time, Display.Animation);
                    }
                    return smaps;
                  }
                  catch (VisADException exc) {
                    break;
                  }
                }
              }
            }
            break;

          case 1: // 3-D ONLY
            //   (z -> ((x, y) -> (..., a, ...)))
            //   (z -> (..., ((x, y) -> (..., a, ...)), ...))
            //   (z -> ((x, y) -> a))
            //   (z -> (..., ((x, y) -> a), ...))
            // x -> X, y -> Y, z -> Z, a -> IsoContour
            domain = ft.getDomain();
            if (domain.getDimension() != 1) break;
            cs = domain.getCoordinateSystem();
            if (cs != null) {
              RealTupleType ref = cs.getReference();
              try {
                z = (RealType) ref.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            else {
              try {
                z = (RealType) domain.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            range = ft.getRange();
            FunctionType rf = null;
            if (range instanceof FunctionType) {
              rf = (FunctionType) range;
              if (!rf.getFlat() || rf.getDomain().getDimension() != 2) break;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  rf = null;
                  break;
                }
                if (ttci instanceof FunctionType) {
                  FunctionType ftci = (FunctionType) ttci;
                  if (ftci.getFlat() && ftci.getDomain().getDimension() == 2) {
                    rf = ftci;
                  }
                  break;
                }
              }
            }
            if (rf == null) break;
            RealTupleType rfd = rf.getDomain();
            cs = rfd.getCoordinateSystem();
            if (cs != null) {
              RealTupleType ref = cs.getReference();
              try {
                x = (RealType) ref.getComponent(0);
                y = (RealType) ref.getComponent(1);
              }
              catch (VisADException exc) {
                break;
              }
            }
            else {
              try {
                x = (RealType) rfd.getComponent(0);
                y = (RealType) rfd.getComponent(1);
              }
              catch (VisADException exc) {
                break;
              }
            }
            range = rf.getRange();
            a = null;
            if (range instanceof RealType) {
              a = (RealType) range;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  break;
                }
                if (ttci instanceof RealType) {
                  a = (RealType) ttci;
                  break;
                }
              }
            }
            if (a == null) break;
            try {
              ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 4 : 5];
              if (RealType.Latitude.equals(x)) {
                smaps[0] = new ScalarMap(x, Display.YAxis);
                smaps[1] = new ScalarMap(y, Display.XAxis);
              }
              else {
                smaps[0] = new ScalarMap(x, Display.XAxis);
                smaps[1] = new ScalarMap(y, Display.YAxis);
              }
              smaps[2] = new ScalarMap(z, Display.ZAxis);
              smaps[3] = new ScalarMap(a, Display.IsoContour);
              if (timeFunc >= 0) {
                Object o = ds[0][timeFunc].funcs.elementAt(0);
                RealTupleType rtt = ((FunctionType) o).getDomain();
                RealType time = (RealType) rtt.getComponent(0);
                smaps[4] = new ScalarMap(time, Display.Animation);
              }
              return smaps;
            }
            catch (VisADException exc) { }

          case 2: // 3-D ONLY
            //   ((x, y) -> (z -> (..., a, ...)))
            //   ((x, y) -> (..., (z -> (..., a, ...)), ...))
            //   ((x, y) -> (z -> a))
            //   ((x, y) -> (..., (z -> a), ...))
            // x -> X, y -> Y, z -> Z, a -> RGB
            //
            domain = ft.getDomain();
            if (domain.getDimension() != 2) break;
            cs = domain.getCoordinateSystem();
            if (cs != null) {
              RealTupleType ref = cs.getReference();
              try {
                x = (RealType) ref.getComponent(0);
                y = (RealType) ref.getComponent(1);
              }
              catch (VisADException exc) {
                break;
              }
            }
            else {
              try {
                x = (RealType) domain.getComponent(0);
                y = (RealType) domain.getComponent(1);
              }
              catch (VisADException exc) {
                break;
              }
            }
            range = ft.getRange();
            rf = null;
            if (range instanceof FunctionType) {
              rf = (FunctionType) range;
              if (!rf.getFlat() || rf.getDomain().getDimension() != 1) break;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  rf = null;
                  break;
                }
                if (ttci instanceof FunctionType) {
                  FunctionType ftci = (FunctionType) ttci;
                  if (ftci.getFlat() && ftci.getDomain().getDimension() == 1) {
                    rf = ftci;
                  }
                  break;
                }
              }
            }
            if (rf == null) break;
            rfd = rf.getDomain();
            cs = rfd.getCoordinateSystem();
            if (cs != null) {
              RealTupleType ref = cs.getReference();
              try {
                z = (RealType) ref.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            else {
              try {
                z = (RealType) rfd.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            range = rf.getRange();
            a = null;
            if (range instanceof RealType) {
              a = (RealType) range;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  break;
                }
                if (ttci instanceof RealType) {
                  a = (RealType) ttci;
                  break;
                }
              }
            }
            if (a == null) break;
            try {
              ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 4 : 5];
              if (RealType.Latitude.equals(x)) {
                smaps[0] = new ScalarMap(x, Display.YAxis);
                smaps[1] = new ScalarMap(y, Display.XAxis);
              }
              else {
                smaps[0] = new ScalarMap(x, Display.XAxis);
                smaps[1] = new ScalarMap(y, Display.YAxis);
              }
              smaps[2] = new ScalarMap(z, Display.ZAxis);
              smaps[3] = new ScalarMap(a, Display.IsoContour);
              if (timeFunc >= 0) {
                Object o = ds[0][timeFunc].funcs.elementAt(0);
                RealTupleType rtt = ((FunctionType) o).getDomain();
                RealType time = (RealType) rtt.getComponent(0);
                smaps[4] = new ScalarMap(time, Display.Animation);
              }
              return smaps;
            }
            catch (VisADException exc) { }

          case 3: // 3-D ONLY
            //   (x -> (y -> (z -> (..., a, ...))))
            //   (x -> (..., (y -> (z -> (..., a, ...))), ...))
            //   (x -> (y -> (..., z -> (..., a, ...)), ...))
            //   (x -> (..., (y -> (..., (z -> (..., a, ...)), ...)), ...))
            //   (x -> (y -> (z -> a)))
            //   (x -> (..., (y -> (z -> a)), ...))
            //   (x -> (y -> (..., (z -> a), ...)))
            //   (x -> (..., (y -> (..., (z -> a), ...)), ...))
            // x -> X, y -> Y, z -> Z, a -> RGB
            domain = ft.getDomain();
            if (domain.getDimension() != 1) break;
            cs = domain.getCoordinateSystem();
            if (cs != null) {
              RealTupleType ref = cs.getReference();
              try {
                x = (RealType) ref.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            else {
              try {
                x = (RealType) domain.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            // find nested "y"
            range = ft.getRange();
            rf = null;
            if (range instanceof FunctionType) {
              rf = (FunctionType) range;
              if (rf.getDomain().getDimension() != 1) break;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  rf = null;
                  break;
                }
                if (ttci instanceof FunctionType) {
                  FunctionType ftci = (FunctionType) ttci;
                  if (ftci.getDomain().getDimension() == 1) {
                    rf = ftci;
                  }
                  break;
                }
              }
            }
            if (rf == null) break;
            rfd = rf.getDomain();
            cs = rfd.getCoordinateSystem();
            if (cs != null) {
              RealTupleType ref = cs.getReference();
              try {
                y = (RealType) ref.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            else {
              try {
                y = (RealType) rfd.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            // find nested "z"
            range = rf.getRange();
            rf = null;
            if (range instanceof FunctionType) {
              rf = (FunctionType) range;
              if (rf.getDomain().getDimension() != 1) break;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  rf = null;
                  break;
                }
                if (ttci instanceof FunctionType) {
                  FunctionType ftci = (FunctionType) ttci;
                  if (ftci.getDomain().getDimension() == 1) {
                    rf = ftci;
                  }
                  break;
                }
              }
            }
            if (rf == null) break;
            rfd = rf.getDomain();
            cs = rfd.getCoordinateSystem();
            if (cs != null) {
              RealTupleType ref = cs.getReference();
              try {
                z = (RealType) ref.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            else {
              try {
                z = (RealType) rfd.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            // find nested "a"
            range = rf.getRange();
            a = null;
            if (range instanceof RealType) {
              a = (RealType) range;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  break;
                }
                if (ttci instanceof RealType) {
                  a = (RealType) ttci;
                  break;
                }
              }
            }
            if (a == null) break;
            try {
              ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 4 : 5];
              if (RealType.Latitude.equals(x)) {
                smaps[0] = new ScalarMap(x, Display.YAxis);
                smaps[1] = new ScalarMap(y, Display.XAxis);
              }
              else {
                smaps[0] = new ScalarMap(x, Display.XAxis);
                smaps[1] = new ScalarMap(y, Display.YAxis);
              }
              smaps[2] = new ScalarMap(z, Display.ZAxis);
              smaps[3] = new ScalarMap(a, Display.IsoContour);
              if (timeFunc >= 0) {
                Object o = ds[0][timeFunc].funcs.elementAt(0);
                RealTupleType rtt = ((FunctionType) o).getDomain();
                RealType time = (RealType) rtt.getComponent(0);
                smaps[4] = new ScalarMap(time, Display.Animation);
              }
              return smaps;
            }
            catch (VisADException exc) { }

          case 4: // 2-D or 3-D
            //   ((x, y) -> (..., r, ..., g, ..., b, ...))
            // x -> X, y -> Y, r -> Red, g -> Green, b -> Blue
            //   ((x, y) -> (..., a, ...))
            //   ((x, y) -> a)
            // x -> X, y -> Y, a -> RGB
            if (!ft.getFlat()) break;
            domain = ft.getDomain();
            if (domain.getDimension() != 2) break;
            cs = domain.getCoordinateSystem();
            if (cs != null) {
              RealTupleType ref = cs.getReference();
              try {
                x = (RealType) ref.getComponent(0);
                y = (RealType) ref.getComponent(1);
              }
              catch (VisADException exc) {
                break;
              }
            }
            else {
              try {
                x = (RealType) domain.getComponent(0);
                y = (RealType) domain.getComponent(1);
              }
              catch (VisADException exc) {
                break;
              }
            }
            range = ft.getRange();
            RealType[] rgb = new RealType[3];
            int rgbc = 0;
            if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  break;
                }
                if (ttci instanceof RealType && rgbc < 3) {
                  rgb[rgbc++] = (RealType) ttci;
                }
              }
            }
            else if (range instanceof RealType) {
              rgb[rgbc++] = (RealType) range;
            }
            if (rgbc == 0) break;
            if (rgbc < 3) {
              try {
                ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 3 : 4];
                if (RealType.Latitude.equals(x)) {
                  smaps[0] = new ScalarMap(x, Display.YAxis);
                  smaps[1] = new ScalarMap(y, Display.XAxis);
                }
                else {
                  smaps[0] = new ScalarMap(x, Display.XAxis);
                  smaps[1] = new ScalarMap(y, Display.YAxis);
                }
                smaps[2] = new ScalarMap(rgb[0], Display.RGB);
                if (timeFunc >= 0) {
                  Object o = ds[0][timeFunc].funcs.elementAt(0);
                  RealTupleType rtt = ((FunctionType) o).getDomain();
                  RealType time = (RealType) rtt.getComponent(0);
                  smaps[3] = new ScalarMap(time, Display.Animation);
                }
                return smaps;
              }
              catch (VisADException exc) { }
            }
            else {
              try {
                ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 5 : 6];
                if (RealType.Latitude.equals(x)) {
                  smaps[0] = new ScalarMap(x, Display.YAxis);
                  smaps[1] = new ScalarMap(y, Display.XAxis);
                }
                else {
                  smaps[0] = new ScalarMap(x, Display.XAxis);
                  smaps[1] = new ScalarMap(y, Display.YAxis);
                }
                smaps[2] = new ScalarMap(rgb[0], Display.Red);
                smaps[3] = new ScalarMap(rgb[1], Display.Green);
                smaps[4] = new ScalarMap(rgb[2], Display.Blue);
                if (timeFunc >= 0) {
                  Object o = ds[0][timeFunc].funcs.elementAt(0);
                  RealTupleType rtt = ((FunctionType) o).getDomain();
                  RealType time = (RealType) rtt.getComponent(0);
                  smaps[5] = new ScalarMap(time, Display.Animation);
                }
                return smaps;
              }
              catch (VisADException exc) { }
            }
            break;

          case 5: // 2-D or 3-D
            //   (x -> (y -> (..., a, ...)))
            //   (x -> (y -> a))
            // 3-D: x -> X, y -> Y, a -> Z
            // 2-D: x -> X, y -> Y, a -> RGB
            domain = ft.getDomain();
            if (domain.getDimension() != 1) break;
            cs = domain.getCoordinateSystem();
            if (cs != null) {
              RealTupleType ref = cs.getReference();
              try {
                x = (RealType) ref.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            else {
              try {
                x = (RealType) domain.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            range = ft.getRange();
            if (!(range instanceof FunctionType)) break;
            ft = (FunctionType) range;
            if (!ft.getFlat()) break;
            domain = ft.getDomain();
            if (domain.getDimension() != 1) break;
            cs = domain.getCoordinateSystem();
            if (cs != null) {
              RealTupleType ref = cs.getReference();
              try {
                y = (RealType) ref.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            else {
              try {
                y = (RealType) domain.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            range = ft.getRange();
            a = null;
            if (range instanceof RealType) {
              a = (RealType) range;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  break;
                }
                if (ttci instanceof RealType) {
                  a = (RealType) ttci;
                  break;
                }
              }
            }
            if (a == null) break;
            try {
              ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 3 : 4];
              if (RealType.Latitude.equals(x)) {
                smaps[0] = new ScalarMap(x, Display.YAxis);
                smaps[1] = new ScalarMap(y, Display.XAxis);
              }
              else {
                smaps[0] = new ScalarMap(x, Display.XAxis);
                smaps[1] = new ScalarMap(y, Display.YAxis);
              }
              smaps[2] = new ScalarMap(a, threeD ? Display.ZAxis
                                                 : Display.RGB);
              if (timeFunc >= 0) {
                Object o = ds[0][timeFunc].funcs.elementAt(0);
                RealTupleType rtt = ((FunctionType) o).getDomain();
                RealType time = (RealType) rtt.getComponent(0);
                smaps[3] = new ScalarMap(time, Display.Animation);
              }
              return smaps;
            }
            catch (VisADException exc) { }

          case 6: // 2-D or 3-D
            //   (x -> (..., a, ...))
            //   (x -> a)
            // x -> X, a -> Y
            if (!ft.getFlat()) break;
            domain = ft.getDomain();
            if (domain.getDimension() != 1) break;
            cs = domain.getCoordinateSystem();
            if (cs != null) {
              RealTupleType ref = cs.getReference();
              try {
                x = (RealType) ref.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            else {
              try {
                x = (RealType) domain.getComponent(0);
              }
              catch (VisADException exc) {
                break;
              }
            }
            range = ft.getRange();
            a = null;
            if (range instanceof RealType) {
              a = (RealType) range;
            }
            else if (range instanceof TupleType) {
              TupleType tt = (TupleType) range;
              for (int i=0; i<tt.getDimension(); i++) {
                MathType ttci;
                try {
                  ttci = tt.getComponent(i);
                }
                catch (VisADException exc) {
                  break;
                }
                if (ttci instanceof RealType) {
                  a = (RealType) ttci;
                  break;
                }
              }
            }
            if (a == null) break;
            try {
              ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 2 : 3];
              if (RealType.Latitude.equals(x)) {
                smaps[0] = new ScalarMap(x, Display.YAxis);
                smaps[1] = new ScalarMap(a, Display.XAxis);
              }
              else {
                smaps[0] = new ScalarMap(x, Display.XAxis);
                smaps[1] = new ScalarMap(a, Display.YAxis);
              }
              if (timeFunc >= 0) {
                Object o = ds[0][timeFunc].funcs.elementAt(0);
                RealTupleType rtt = ((FunctionType) o).getDomain();
                RealType time = (RealType) rtt.getComponent(0);
                smaps[2] = new ScalarMap(time, Display.Animation);
              }
              return smaps;
            }
            catch (VisADException exc) { }
            break;
        }
      }
    }

    // look for matches between Set templates and SetTypes list
    final DisplayRealType[] spatial =
      {Display.XAxis, Display.YAxis, Display.ZAxis};
    final boolean[] mark = {false, false, false};
    int maxdim = threeD ? 3 : 2;
    int numsets = slist.size();
    for (int dim=maxdim; dim>=1; --dim) {
      for (int si=0; si<numsets; si++) {
        //   Set(x, y, z)
        // x -> X, y -> Y, z -> Z
        //   Set(x, y)
        // x -> X, y -> Y
        //   Set(x)
        // x -> X
        SetType st = (SetType) slist.elementAt(si);
        RealTupleType domain = st.getDomain();
        CoordinateSystem cs = domain.getCoordinateSystem();
        if (cs != null) {
          // use CoordinateSystem reference instead of original RealTupleType
          domain = cs.getReference();
        }
        if (domain.getDimension() != dim) continue;
        try {
          ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? dim : dim + 1];
          for (int i=0; i<dim; i++) {
            RealType rt = (RealType) domain.getComponent(i);
            if (RealType.Latitude.equals(rt)) {
              smaps[i] = new ScalarMap(rt, spatial[1]);
              mark[1] = true;
            }
            else if (RealType.Longitude.equals(rt)) {
              smaps[i] = new ScalarMap(rt, spatial[0]);
              mark[0] = true;
            }
          }
          for (int i=0; i<dim; i++) {
            RealType rt = (RealType) domain.getComponent(i);
            if (!RealType.Latitude.equals(rt) && !RealType.Longitude.equals(rt)) {
              for (int j=0; j<3; j++) {
                if (!mark[j]) {
                  smaps[i] = new ScalarMap(rt, spatial[j]);
                  mark[j] = true;
                }
              }
            }
          }
          if (timeFunc >= 0) {
            Object o = ds[0][timeFunc].funcs.elementAt(0);
            RealTupleType rtt = ((FunctionType) o).getDomain();
            RealType time = (RealType) rtt.getComponent(0);
            smaps[dim] = new ScalarMap(time, Display.Animation);
          }
          return smaps;
        }
        catch (VisADException exc) { }
      }
    }

    // if the only match is a RealTupleType, map to first few tuple elements
    int numtuples = tlist.size();
    if (numtuples >= 1) {
      // use first RealTupleType - (x, y, z, ...)
      // x -> X, y -> Y, z -> Z
      RealTupleType domain = (RealTupleType) tlist.elementAt(0);
      CoordinateSystem cs = domain.getCoordinateSystem();
      if (cs != null) {
        // use CoordinateSystem reference instead of original RealTupleType
        domain = cs.getReference();
      }
      int dim = domain.getDimension();
      if (dim > maxdim) dim = maxdim;
      try {
        ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? dim : dim + 1];
        for (int i=0; i<dim; i++) {
          RealType rt = (RealType) domain.getComponent(i);
          smaps[i] = new ScalarMap(rt, spatial[i]);
        }
        if (timeFunc >= 0) {
          Object o = ds[0][timeFunc].funcs.elementAt(0);
          RealTupleType rtt = ((FunctionType) o).getDomain();
          RealType time = (RealType) rtt.getComponent(0);
          smaps[dim] = new ScalarMap(time, Display.Animation);
        }
        return smaps;
      }
      catch (VisADException exc) { }
    }

    return null;
  }

  /** used by guessMaps to recursively build a list of FunctionTypes to
      attempt template matching with */
  private void buildTypeList(MathType mt,
    Vector flist, Vector slist, Vector tlist)
  {
    if (mt instanceof TupleType) {
      TupleType tt = (TupleType) mt;
      if (tt instanceof RealTupleType) {
        // found a tuple of reals; add it to RealTuple list
        tlist.addElement(mt);
      }
      else {
        // search each tuple component
        for (int i=0; i<tt.getDimension(); i++) {
          try {
            buildTypeList(tt.getComponent(i), flist, slist, tlist);
          }
          catch (VisADException exc) { }
        }
      }
    }
    else if (mt instanceof SetType) {
      // found a set; add it to set list
      slist.addElement(mt);
    }
    else if (mt instanceof FunctionType) {
      // found a function; add it to function list and recurse function range
      flist.addElement(mt);
      FunctionType ft = (FunctionType) mt;
      buildTypeList(ft.getRange(), flist, slist, tlist);
    }
    return;
  }

  /** used by guessMaps to recursively find a "time" RealType inside
      a 1-D function domain */
  private void findTimeFunction(MathType mt, DataStruct[][] info,
                                Hashtable invalid) {
    boolean wasnull = false;
    if (invalid == null) {
      invalid = new Hashtable();
      wasnull = true;
    }
    if (mt instanceof TupleType) {
      TupleType tt = (TupleType) mt;
      // search each element of the tuple
      for (int i=0; i<tt.getDimension(); i++) {
        MathType tc = null;
        try {
          tc = tt.getComponent(i);
        }
        catch (VisADException exc) { }
        findTimeFunction(tc, info, invalid);
      }
    }
    else if (mt instanceof SetType) {
      SetType st = (SetType) mt;
      // search set's domain
      findTimeFunction(st.getDomain(), info, invalid);
    }
    else if (mt instanceof FunctionType) {
      FunctionType ft = (FunctionType) mt;
      RealTupleType domain = ft.getDomain();
      MathType range = ft.getRange();
      RealType rtc0 = null;
      try {
        rtc0 = (RealType) domain.getComponent(0);
      }
      catch (VisADException exc) { }
      boolean found = false;
      if (rtc0 != null && domain.getDimension() == 1) {
        // search function's domain for RealType with matching name
        String rtname = rtc0.getName();
        for (int i=0; i<info[0].length; i++) {
          if (rtname.equals(info[0][i].name)) {
            info[0][i].funcs.addElement(ft);
            found = true;
          }
        }

        // WLH 19 Oct 2001
        Unit rtc0_unit = rtc0.getDefaultUnit();
        if (SI.second.isConvertible(rtc0_unit) ||
            CommonUnit.secondsSinceTheEpoch.isConvertible(rtc0_unit)) {
          int len = info[0].length;
          DataStruct[] temp = new DataStruct[len + 1];
          for (int i=0; i<len; i++) {
            temp[i] = info[0][i];
          }
          temp[len] = new DataStruct(rtname);
          temp[len].funcs.addElement(ft);
          info[0] = temp;
          found = true;
        }

      }
      // search function's domain
      if (!found) findTimeFunction(domain, info, invalid);
      // search function's range
      findTimeFunction(range, info, invalid);
    }
    else if (mt instanceof RealType) {
      RealType rt = (RealType) mt;
      String rtname = rt.getName();
      invalid.put(rtname, rt);
/*
      for (int i=0; i<info[0].length; i++) {
        // invalidate RealTypes not in a 1-D function domain
        if (rtname.equals(info[0][i].name)) info[0][i].fvalid = false;
      }
*/
    }

    if (wasnull) {
      for (int i=0; i<info[0].length; i++) {
        // invalidate RealTypes not in a 1-D function domain
        if (invalid.get(info[0][i].name) != null) info[0][i].fvalid = false;
      }
    }

    return;
  }

  /** return true if st occurs in mt */
  public static boolean findScalarType(MathType mt, ScalarType st)
         throws VisADException {
    if (mt == null || st == null) return false;
    if (mt instanceof TupleType) {
      TupleType tt = (TupleType) mt;
      // search each element of the tuple
      for (int i=0; i<tt.getDimension(); i++) {
        MathType tc = tt.getComponent(i);
        if (findScalarType(tc, st)) return true;
      }

      // WLH 8 Jan 2003
      if (mt instanceof RealTupleType) {
        CoordinateSystem cs = ((RealTupleType) mt).getCoordinateSystem();
        if (cs != null) {
          if (findScalarType(cs.getReference(), st)) return true;
        }
      }

      return false;
    }
    else if (mt instanceof SetType) {
      SetType et = (SetType) mt;
      // search set's domain
      return findScalarType(et.getDomain(), st);
    }
    else if (mt instanceof FunctionType) {
      FunctionType ft = (FunctionType) mt;
      RealTupleType domain = ft.getDomain();
      MathType range = ft.getRange();
      return findScalarType(domain, st) || findScalarType(range, st);
    }
    else if (mt instanceof ScalarType) {
      return (mt.equals(st));
    }
    return false;
  }


  /** run 'java visad.MathType' to test MathType.prettyString()
      and MathType.guessMaps() */
  public static void main(String args[])
         throws VisADException, RemoteException {
    RealType X = RealType.getRealType("Xxxxxx");
    RealType Y = RealType.getRealType("Yyyyyy");
    RealType Z = RealType.getRealType("Zzzzzz");

    RealType A = RealType.getRealType("Aaaaaa");
    RealType B = RealType.getRealType("Bbbbbb");

    RealType[] domain2d = {X, Y};
    RealTupleType Domain2d = new RealTupleType(domain2d);

    RealType[] range2d = {A, B};
    RealTupleType Range2d = new RealTupleType(range2d);

    // construct first MathType
    FunctionType Field2d1 = new FunctionType(Domain2d, A);
    FunctionType Field2d2 = new FunctionType(Domain2d, Range2d);
    FunctionType Field2d3 = new FunctionType(Domain2d, B);
    FunctionType function = new FunctionType(X, Field2d2);
    MathType[] littles = {Range2d, Field2d1, function};
    TupleType little = new TupleType(littles);
    FunctionType little_function = new FunctionType(X, little);
    SetType set = new SetType(Domain2d);
    MathType[] types = {Range2d, little_function, Field2d1, Field2d2,
                        function, set, Field2d3};
    TupleType tuple = new TupleType(types);
    FunctionType big_function = new FunctionType(Range2d, tuple);

    // test prettyString()
    System.out.println("prettyString for first MathType:");
    String s1 = big_function.prettyString();
    System.out.println(s1 + "\n");
    MathType t1 = stringToType(s1);
    System.out.println("stringToType for first MathType:");
    System.out.println(t1.prettyString() + "\n");

    // construct second MathType
    RealType T = RealType.getRealType("time");
    RealTupleType Domain1d = new RealTupleType(new RealType[] {T});
    RealType Rxx = RealType.getRealType("Red");
    RealType Gxx = RealType.getRealType("Green");
    RealType Bxx = RealType.getRealType("Blue");
    RealTupleType Range3d = new RealTupleType(new RealType[] {Rxx, Gxx, Bxx});
    FunctionType image = new FunctionType(Domain2d, Range3d);
    function = new FunctionType(Domain1d, image);

    // test prettyString() again
    System.out.println("prettyString for second MathType:");
    String s2 = function.prettyString();
    System.out.println(s2 + "\n");
    MathType t2 = stringToType(s2);
    System.out.println("stringToType for second MathType:");
    System.out.println(t2.prettyString() + "\n");

    // test guessMaps()
    System.out.println("Guessing at some good mappings for this MathType...");
    ScalarMap[] smaps = function.guessMaps(true);
    if (smaps == null) {
      System.out.println("Could not identify a good set of mappings!");
    }
    else {
      for (int i=0; i<smaps.length; i++) {
        ScalarType s = smaps[i].getScalar();
        DisplayRealType ds = smaps[i].getDisplayScalar();
        System.out.println(s.getName() + " -> " + ds.getName());
      }
    }

    String s3 = "((Row, Col, Lev) -> Radiance)";
    String s3s = stringToType(s3).prettyString();
    System.out.println("s3 = \n" + s3 + "\ns3s = \n" + s3s);
  }

  /** used by guessMaps to store miscellaneous information
      throughout findTimeFunction's recursive calls */
  private class DataStruct {
    /** whether this DataStruct's funcs are valid */
    boolean fvalid = true;
    /** The name of the RealType in the domain of the FunctionTypes in funcs */
    String name;
    /** FunctionType objects with 1-D domains which match name */
    Vector funcs = new Vector();

    /** constructor */
    DataStruct(String s) { name = s; }
  }

}
