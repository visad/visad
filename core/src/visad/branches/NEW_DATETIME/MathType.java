
//
// MathType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
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

import java.rmi.*;
import java.util.Vector;

/** 
(Fulker)
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
(/Fulker)<p>

   MathType is the superclass of the VisAD hierarchy of data types.
   MathType objects are immutable; note that there is a setDefaultSet method
   in RealTupleType, but if t is a RealTupleType then t.setDefaultSet cannot
   be called after t.getDefaultSet has been called.<P>

   VisAD Data objects are finite approximations to mathematical objects.
   Every Data object includes a MathType, which is the mathematical type
   of the mathematical object it approximates.  This MathType is not
   synonymous with the class of the Data object.<P>

*/
public abstract class MathType extends Object implements java.io.Serializable {

  /** true if this MathType is defined by the system */
  boolean SystemIntrinsic;

  public MathType() {
    super();
    SystemIntrinsic = false;
  }

  MathType(boolean b) {
    super();
    SystemIntrinsic = true;
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

  public abstract String toString();

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
              s0.equals("_"))) {
          len[0] = i;
          break;
        }
      }
      String rs = s.substring(0, len[0]);
      try {
        ret_type = new RealType(rs);
      }
      catch (TypeException e) {
        ret_type = RealType.getRealTypeByName(rs);
      }
      return ret_type;
    }
    else {
      throw new TypeException("MathType.stringToType: badly formed string");
    }
  }

  /** Guesses at a set of &quot;default&quot; mappings for this MathType.
      Intuitively, first we look for a FunctionType with domain dimension 3,
      then a nested group of FunctionTypes with 'cumulative' domain
      dimension 3.  Next we look for a FunctionType or nested group of
      FunctionTypes with domain dimension 2.  Last, we look for a
      FunctionType with domain dimension 1.  Nested groups of FunctionTypes
      may be nested with TupleTypes, which is indicated by some of the
      MathType templates.
  */
  public ScalarMap[] guessMaps(boolean threeD) {
    MathType m = this;

    // set up aliases for "time" RealType to be mapped to Animation
    // NOTE: other acceptable 1-D function domains could be added here,
    //       by allocating a larger DataStruct array, then specifying
    //              ds[*] = new DataStruct("X");
    //       where "X" is the name of the 1-D function domain RealType
    //       that is desired to be automatically mapped to Animation.
    DataStruct[] ds = new DataStruct[3];
    ds[0] = new DataStruct("time");
    ds[1] = new DataStruct("Time");
    ds[2] = new DataStruct("TIME");

    // find a FunctionType whose 1-D domain is a RealType matching above
    findTimeFunction(m, ds);
    int timeFunc = -1;
    for (int i=0; i<ds.length; i++) {
      if (ds[i].fvalid && ds[i].funcs.size() > 0) {
        timeFunc = i;
        break;
      }
    }

    // compile a list of FunctionTypes to search through for template matching
    Vector flist = new Vector();
    if (timeFunc < 0) buildFunctionList(m, flist);
    else {
      // found a "time" RealType; only search ranges of "time" FunctionTypes
      for (int i=0; i<ds[timeFunc].funcs.size(); i++) {
        FunctionType f = (FunctionType) ds[timeFunc].funcs.elementAt(i);
        buildFunctionList(f.getRange(), flist);
      }
    }

    // look for matches between templates and FunctionTypes list
    int numfuncs = flist.size();
    for (int template=(threeD ? 0 : 4); template<7; template++) {
      for (int fi=0; fi<numfuncs; fi++) {
        FunctionType ft = (FunctionType) flist.elementAt(fi);
        switch (template) {
          case 0: // 3-D ONLY
            //   ((x, y, z) -> (..., a, ...))
            //   ((x, y, z) -> a)
            // x -> X, y -> Y, z -> Z, a -> IsoContour
            if (!ft.getFlat()) break;
            RealTupleType domain = ft.getDomain();
            if (domain.getDimension() != 3) break;
            MathType range = ft.getRange();
            RealType x, y, z;
            try {
              x = (RealType) domain.getComponent(0);
              y = (RealType) domain.getComponent(1);
              z = (RealType) domain.getComponent(2);
            }
            catch (VisADException exc) {
              break;
            }
            RealType a;
            if (range instanceof RealType) {
              a = (RealType) range;
              try {
                ScalarMap[] smaps = new ScalarMap[timeFunc < 0 ? 4 : 5];
                smaps[0] = new ScalarMap(x, Display.XAxis);
                smaps[1] = new ScalarMap(y, Display.YAxis);
                smaps[2] = new ScalarMap(z, Display.ZAxis);
                smaps[3] = new ScalarMap(a, Display.IsoContour);
                if (timeFunc >= 0) {
                  Object o = ds[timeFunc].funcs.elementAt(0);
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
                    smaps[0] = new ScalarMap(x, Display.XAxis);
                    smaps[1] = new ScalarMap(y, Display.YAxis);
                    smaps[2] = new ScalarMap(z, Display.ZAxis);
                    smaps[3] = new ScalarMap(a, Display.IsoContour);
                    if (timeFunc >= 0) {
                      Object o = ds[timeFunc].funcs.elementAt(0);
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
            try {
              z = (RealType) domain.getComponent(0);
            }
            catch (VisADException exc) {
              break;
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
            try {
              x = (RealType) rfd.getComponent(0);
              y = (RealType) rfd.getComponent(1);
            }
            catch (VisADException exc) {
              break;
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
              smaps[0] = new ScalarMap(x, Display.XAxis);
              smaps[1] = new ScalarMap(y, Display.YAxis);
              smaps[2] = new ScalarMap(z, Display.ZAxis);
              smaps[3] = new ScalarMap(a, Display.IsoContour);
              if (timeFunc >= 0) {
                Object o = ds[timeFunc].funcs.elementAt(0);
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
            try {
              x = (RealType) domain.getComponent(0);
              y = (RealType) domain.getComponent(1);
            }
            catch (VisADException exc) {
              break;
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
            try {
              z = (RealType) rfd.getComponent(0);
            }
            catch (VisADException exc) {
              break;
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
              smaps[0] = new ScalarMap(x, Display.XAxis);
              smaps[1] = new ScalarMap(y, Display.YAxis);
              smaps[2] = new ScalarMap(z, Display.ZAxis);
              smaps[3] = new ScalarMap(a, Display.IsoContour);
              if (timeFunc >= 0) {
                Object o = ds[timeFunc].funcs.elementAt(0);
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
            try {
              x = (RealType) domain.getComponent(0);
            }
            catch (VisADException exc) {
              break;
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
            try {
              y = (RealType) rfd.getComponent(0);
            }
            catch (VisADException exc) {
              break;
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
            try {
              z = (RealType) rfd.getComponent(0);
            }
            catch (VisADException exc) {
              break;
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
              smaps[0] = new ScalarMap(x, Display.XAxis);
              smaps[1] = new ScalarMap(y, Display.YAxis);
              smaps[2] = new ScalarMap(z, Display.ZAxis);
              smaps[3] = new ScalarMap(a, Display.IsoContour);
              if (timeFunc >= 0) {
                Object o = ds[timeFunc].funcs.elementAt(0);
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
            try {
              x = (RealType) domain.getComponent(0);
              y = (RealType) domain.getComponent(1);
            }
            catch (VisADException exc) {
              break;
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
                smaps[0] = new ScalarMap(x, Display.XAxis);
                smaps[1] = new ScalarMap(y, Display.YAxis);
                smaps[2] = new ScalarMap(rgb[0], Display.RGB);
                if (timeFunc >= 0) {
                  Object o = ds[timeFunc].funcs.elementAt(0);
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
                smaps[0] = new ScalarMap(x, Display.XAxis);
                smaps[1] = new ScalarMap(y, Display.YAxis);
                smaps[2] = new ScalarMap(rgb[0], Display.Red);
                smaps[3] = new ScalarMap(rgb[1], Display.Green);
                smaps[4] = new ScalarMap(rgb[2], Display.Blue);
                if (timeFunc >= 0) {
                  Object o = ds[timeFunc].funcs.elementAt(0);
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
            try {
              x = (RealType) domain.getComponent(0);
            }
            catch (VisADException exc) {
              break;
            }
            range = ft.getRange();
            if (!(range instanceof FunctionType)) break;
            ft = (FunctionType) range;
            if (!ft.getFlat()) break;
            domain = ft.getDomain();
            if (domain.getDimension() != 1) break;
            try {
              y = (RealType) domain.getComponent(0);
            }
            catch (VisADException exc) {
              break;
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
              smaps[0] = new ScalarMap(x, Display.XAxis);
              smaps[1] = new ScalarMap(y, Display.YAxis);
              smaps[2] = new ScalarMap(a, threeD ? Display.ZAxis
                                                 : Display.RGB);
              if (timeFunc >= 0) {
                Object o = ds[timeFunc].funcs.elementAt(0);
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
            try {
              x = (RealType) domain.getComponent(0);
            }
            catch (VisADException exc) {
              break;
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
              smaps[0] = new ScalarMap(x, Display.XAxis);
              smaps[1] = new ScalarMap(a, Display.YAxis);
              if (timeFunc >= 0) {
                Object o = ds[timeFunc].funcs.elementAt(0);
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

    return null;
  }

  /** used by guessMaps to recursively build a list of FunctionTypes to
      attempt template matching with */
  private void buildFunctionList(MathType mt, Vector list) {
    if (mt instanceof TupleType) {
      TupleType tt = (TupleType) mt;
      for (int i=0; i<tt.getDimension(); i++) {
        try {
          buildFunctionList(tt.getComponent(i), list);
        }
        catch (VisADException exc) { }
      }
    }
    else if (mt instanceof SetType) {
      SetType st = (SetType) mt;
      buildFunctionList(st.getDomain(), list);
    }
    else if (mt instanceof FunctionType) list.addElement(mt);
    return;
  }

  /** used by guessMaps to recursively find a "time" RealType inside
      a 1-D function domain */
  private void findTimeFunction(MathType mt, DataStruct[] info) {
    if (mt instanceof TupleType) {
      TupleType tt = (TupleType) mt;
      // search each element of the tuple
      for (int i=0; i<tt.getDimension(); i++) {
        MathType tc = null;
        try {
          tc = tt.getComponent(i);
        }
        catch (VisADException exc) { }
        findTimeFunction(tc, info);
      }
    }
    else if (mt instanceof SetType) {
      SetType st = (SetType) mt;
      // search set's domain
      findTimeFunction(st.getDomain(), info);
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
        for (int i=0; i<info.length; i++) {
          if (rtname.equals(info[i].name)) {
            info[i].funcs.addElement(ft);
            found = true;
          }
        }
      }
      // search function's domain
      if (!found) findTimeFunction(domain, info);
      // search function's range
      findTimeFunction(range, info);
    }
    else if (mt instanceof RealType) {
      RealType rt = (RealType) mt;
      String rtname = rt.getName();
      for (int i=0; i<info.length; i++) {
        // invalidate RealTypes not in a 1-D function domain
        if (rtname.equals(info[i].name)) info[i].fvalid = false;
      }
    }
    return;
  }

  /** run 'java visad.MathType' to test MathType.prettyString()
      and MathType.guessMaps() */
  public static void main(String args[])
         throws VisADException, RemoteException {
    RealType X = new RealType("Xxxxxx", null, null);
    RealType Y = new RealType("Yyyyyy", null, null);
    RealType Z = new RealType("Zzzzzz", null, null);
 
    RealType A = new RealType("Aaaaaa", null, null);
    RealType B = new RealType("Bbbbbb", null, null);
 
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
    RealType T = new RealType("time");
    RealTupleType Domain1d = new RealTupleType(new RealType[] {T});
    RealType Rxx = new RealType("Red");
    RealType Gxx = new RealType("Green");
    RealType Bxx = new RealType("Blue");
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

